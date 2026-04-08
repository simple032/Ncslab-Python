import numpy as np
from scipy.integrate import solve_ivp

from .acceleration_strategy import (
    can_use_fixed_step_solver,
    cupy_rk4_combine_eligible,
    normalize_solver_name,
)
from .blocks.sink import ScopeBlock
from .cuda_config import try_cupy
from .jsonl import emit_error, emit_progress


def map_solver(solver_name):
    solver_name = normalize_solver_name(solver_name)
    mapping = {
        "ode45": "RK45",
        "VariableStepAuto": "RK45",
        "ode23": "RK23",
        "ode15s": "BDF",
        "ode23s": "Radau",
        "ode113": "DOP853",
        "ode4": "RK45",
        "ode3": "RK23",
        "ode2": "RK23",
        "ode1": "RK23",
    }
    return mapping.get(solver_name, "RK45")


def describe_solver(solver_name, step_type):
    solver_name = normalize_solver_name(solver_name)
    if step_type == "FixedStep" and can_use_fixed_step_solver(solver_name):
        fixed_labels = {
            "ode4": "RK4(FixedStep)",
            "ode3": "RK3(FixedStep)",
            "ode23": "RK2(FixedStep)",
            "ode2": "RK2(FixedStep)",
            "ode1": "Euler(FixedStep)",
        }
        return fixed_labels.get(solver_name, "FixedStep")
    return map_solver(solver_name)


def _step_euler(model, t, state, step):
    return state + step * model.compute_derivatives(t, state)


def _step_rk2(model, t, state, step):
    k1 = model.compute_derivatives(t, state)
    k2 = model.compute_derivatives(t + step, state + step * k1)
    return state + 0.5 * step * (k1 + k2)


def _step_rk3(model, t, state, step):
    half_step = 0.5 * step
    k1 = model.compute_derivatives(t, state)
    k2 = model.compute_derivatives(t + half_step, state + half_step * k1)
    k3 = model.compute_derivatives(t + step, state - step * k1 + 2.0 * step * k2)
    return state + (step / 6.0) * (k1 + 4.0 * k2 + k3)


def _step_rk4(model, t, state, step):
    half_step = 0.5 * step
    k1 = model.compute_derivatives(t, state)
    k2 = model.compute_derivatives(t + half_step, state + half_step * k1)
    k3 = model.compute_derivatives(t + half_step, state + half_step * k2)
    k4 = model.compute_derivatives(t + step, state + step * k3)
    return state + (step / 6.0) * (k1 + 2.0 * k2 + 2.0 * k3 + k4)


def _step_rk4_cupy_combine(model, t, state, step, cp):
    """Same RHS evaluations as _step_rk4; final RK4 linear combination on GPU (for large state vectors)."""
    half_step = 0.5 * step
    k1 = model.compute_derivatives(t, state)
    k2 = model.compute_derivatives(t + half_step, state + half_step * k1)
    k3 = model.compute_derivatives(t + half_step, state + half_step * k2)
    k4 = model.compute_derivatives(t + step, state + step * k3)
    s = cp.asarray(state)
    acc = (
        cp.asarray(k1)
        + 2.0 * cp.asarray(k2)
        + 2.0 * cp.asarray(k3)
        + cp.asarray(k4)
    )
    out = s + (step / 6.0) * acc
    return cp.asnumpy(out)


def _advance_fixed_step(model, solver_name, t, state, step, use_cupy_rk4=False, cp=None):
    solver_key = normalize_solver_name(solver_name).lower()
    if solver_key == "ode1":
        return _step_euler(model, t, state, step)
    if solver_key in {"ode2", "ode23"}:
        return _step_rk2(model, t, state, step)
    if solver_key == "ode3":
        return _step_rk3(model, t, state, step)
    if use_cupy_rk4 and cp is not None:
        return _step_rk4_cupy_combine(model, t, state, step, cp)
    return _step_rk4(model, t, state, step)


def _build_time_grid(t_start, t_stop, step):
    if step <= 0.0:
        step = 0.01
    t_eval = np.arange(t_start, t_stop + step / 2.0, step)
    t_eval = t_eval[t_eval <= t_stop + 1e-12]
    if len(t_eval) == 0 or abs(t_eval[-1] - t_stop) > 1e-12:
        t_eval = np.append(t_eval, t_stop)
    return t_eval


def _augment_time_grid(model, t_eval):
    extra_times = []
    for block in model.sorted_blocks:
        if hasattr(block, "get_preferred_output_times"):
            extra_times.extend(block.get_preferred_output_times(model.start_time, model.stop_time))

    if not extra_times:
        return t_eval

    merged = np.concatenate((np.asarray(t_eval, dtype=float), np.asarray(extra_times, dtype=float)))
    merged = merged[(merged >= model.start_time - 1e-12) & (merged <= model.stop_time + 1e-12)]
    merged = np.unique(np.round(merged, 12))
    merged.sort()
    return merged


def _build_sample_indices(total_steps, max_points):
    if total_steps <= 0:
        return np.array([], dtype=int)
    if max_points is None or max_points <= 0 or total_steps <= max_points:
        return np.arange(total_steps, dtype=int)
    if max_points == 1:
        return np.array([total_steps - 1], dtype=int)
    return np.unique(np.linspace(0, total_steps - 1, num=max_points, dtype=int))


def _run_fixed_step_simulation(model, t_eval, t_start, t_stop, total_time, use_cuda_acceleration=False):
    cp = try_cupy() if use_cuda_acceleration else None
    solver_key = normalize_solver_name(model.solver_name).lower()
    acc_mgr = None
    native_active = False

    # Native CUDA plan (DLL) is preferred when available.
    if use_cuda_acceleration:
        try:
            from .acceleration import create_acceleration_manager

            acc_mgr = create_acceleration_manager(model)
            if acc_mgr and acc_mgr.can_run_fixed_step_cuda_solver(model.solver_name):
                native_active = True
        except Exception:
            acc_mgr = None
            native_active = False

    state = model.get_initial_states()
    if native_active and acc_mgr is not None:
        try:
            acc_mgr.initialize_fixed_step_cuda_state(state)
        except Exception as exc:
            native_active = False
            acc_mgr = None
            emit_error(f"Native CUDA acceleration init failed; falling back to CPU: {exc}")

    use_cupy_rk4 = cupy_rk4_combine_eligible(model, use_cuda_acceleration, native_active, cp)

    last_progress = 0
    sample_indices = _build_sample_indices(len(t_eval), model.max_data_points)
    sample_cursor = 0
    next_sample_index = sample_indices[sample_cursor] if len(sample_indices) > 0 else -1

    for index, t in enumerate(t_eval):
        t_value = float(t)
        record_observers = index == next_sample_index or index + 1 == len(t_eval)
        model.propagate_signals(t_value, state, include_observers=record_observers)

        if index == next_sample_index and sample_cursor + 1 < len(sample_indices):
            sample_cursor += 1
            next_sample_index = sample_indices[sample_cursor]

        progress = int((t_value - t_start) / total_time * 100) if total_time > 0 else 100
        if progress >= last_progress + 5:
            emit_progress(t_value, float(total_time), progress)
            last_progress = progress

        if index + 1 >= len(t_eval):
            continue

        step = float(t_eval[index + 1] - t)
        if native_active and acc_mgr is not None:
            try:
                state = acc_mgr.advance_fixed_step_cuda(model.solver_name, t_value, step, state)
                # advance_* in CUDA fixed-step plans may advance a resident device state;
                # we must sync host state for correctness because we still call
                # `model.propagate_signals()` (CPU) to compute block outputs and scopes.
                state = acc_mgr.sync_fixed_step_cuda_state(state)
            except Exception as exc:
                native_active = False
                acc_mgr = None
                emit_error(f"Native CUDA acceleration failed during fixed-step; falling back to CPU: {exc}")
                use_cupy_rk4 = cupy_rk4_combine_eligible(model, use_cuda_acceleration, False, cp)
                state = _advance_fixed_step(
                    model,
                    model.solver_name,
                    t_value,
                    state,
                    step,
                    use_cupy_rk4=use_cupy_rk4,
                    cp=cp,
                )
        else:
            state = _advance_fixed_step(
                model,
                model.solver_name,
                t_value,
                state,
                step,
                use_cupy_rk4=use_cupy_rk4,
                cp=cp,
            )

    emit_progress(float(t_stop), float(total_time), 100)
    return model


def run_simulation(model, use_cuda_acceleration=False):
    """
    Run simulation. If use_cuda_acceleration is True (CUDA WebSocket), fixed-step ode4 may use
    CuPy for the RK4 combination step when state dimension is large enough (see acceleration_strategy.CUDA_RK4_MIN_STATES) and CuPy loads.
    Block RHS (compute_derivatives) remains on CPU.
    """
    t_start = model.start_time
    t_stop = model.stop_time
    initial_states = model.get_initial_states()
    output_step = None

    if model.has_discrete_blocks:
        output_step = model.discrete_step
        t_eval = _build_time_grid(t_start, t_stop, output_step)
    elif model.step_type == "FixedStep" and model.fixed_step > 0:
        t_eval = _build_time_grid(t_start, t_stop, model.fixed_step)
        output_step = model.fixed_step
    else:
        t_eval = np.linspace(t_start, t_stop, model.max_data_points)
        if len(t_eval) >= 2:
            output_step = float(t_eval[1] - t_eval[0])

    t_eval = _augment_time_grid(model, t_eval)

    total_time = t_stop - t_start
    last_progress = 0
    sample_indices = _build_sample_indices(len(t_eval), model.max_data_points)

    def set_scope_recording(enabled):
        for block in model.sorted_blocks:
            if isinstance(block, ScopeBlock):
                block.recording = enabled

    if getattr(model, "uses_circuit_mna", False):
        set_scope_recording(True)
        sample_cursor = 0
        next_sample_index = sample_indices[sample_cursor] if len(sample_indices) > 0 else -1
        state = initial_states.copy()
        for index, t in enumerate(t_eval):
            t_value = float(t)
            next_t = float(t_eval[index + 1]) if index + 1 < len(t_eval) else t_value
            step_size = max(0.0, next_t - t_value)
            record_observers = index == next_sample_index or index + 1 == len(t_eval)
            model.propagate_signals(
                t_value,
                state,
                include_observers=record_observers,
                step_size=step_size,
            )
            if index == next_sample_index and sample_cursor + 1 < len(sample_indices):
                sample_cursor += 1
                next_sample_index = sample_indices[sample_cursor]

            progress = int((t_value - t_start) / total_time * 100) if total_time > 0 else 100
            if progress >= last_progress + 5:
                emit_progress(float(t_value), float(total_time), progress)
                last_progress = progress
        emit_progress(float(t_stop), float(total_time), 100)
        return model

    if model.has_discrete_blocks:
        set_scope_recording(True)
        state = initial_states.copy()
        sample_cursor = 0
        next_sample_index = sample_indices[sample_cursor] if len(sample_indices) > 0 else -1
        for index, t in enumerate(t_eval):
            t_value = float(t)
            record_observers = index == next_sample_index or index + 1 == len(t_eval)
            next_t = float(t_eval[index + 1]) if index + 1 < len(t_eval) else t_value
            step_size = max(0.0, next_t - t_value)
            model.propagate_signals(t_value, state, include_observers=record_observers, step_size=step_size)

            if index == next_sample_index and sample_cursor + 1 < len(sample_indices):
                sample_cursor += 1
                next_sample_index = sample_indices[sample_cursor]

            progress = int((t_value - t_start) / total_time * 100) if total_time > 0 else 100
            if progress >= last_progress + 5:
                emit_progress(t_value, float(total_time), progress)
                last_progress = progress

            if index + 1 >= len(t_eval) or model.total_states == 0:
                continue

            step = float(t_eval[index + 1] - t)
            state = _step_euler(model, t_value, state, step)

        emit_progress(float(t_stop), float(total_time), 100)
        return model

    if model.total_states == 0:
        set_scope_recording(True)
        sample_cursor = 0
        next_sample_index = sample_indices[sample_cursor] if len(sample_indices) > 0 else -1
        for index, t in enumerate(t_eval):
            record_observers = index == next_sample_index or index + 1 == len(t_eval)
            next_t = float(t_eval[index + 1]) if index + 1 < len(t_eval) else float(t)
            step_size = max(0.0, next_t - float(t))
            model.propagate_signals(t, initial_states, include_observers=record_observers, step_size=step_size)

            if index == next_sample_index and sample_cursor + 1 < len(sample_indices):
                sample_cursor += 1
                next_sample_index = sample_indices[sample_cursor]

            progress = int((t - t_start) / total_time * 100) if total_time > 0 else 100
            if progress >= last_progress + 5:
                emit_progress(float(t), float(total_time), progress)
                last_progress = progress
        emit_progress(float(t_stop), float(total_time), 100)
        return model

    if (
        model.step_type == "FixedStep"
        and model.fixed_step > 0
        and len(t_eval) > 1
        and can_use_fixed_step_solver(model.solver_name)
    ):
        set_scope_recording(True)
        return _run_fixed_step_simulation(
            model,
            t_eval,
            t_start,
            t_stop,
            total_time,
            use_cuda_acceleration=use_cuda_acceleration,
        )

    scipy_method = map_solver(model.solver_name)
    set_scope_recording(False)
    call_count = [0]
    last_reported = [0]

    def derivatives(t, y):
        call_count[0] += 1
        if call_count[0] % 50 == 0:
            progress = int((t - t_start) / total_time * 100) if total_time > 0 else 0
            progress = max(0, min(99, progress))
            if progress > last_reported[0]:
                emit_progress(float(t), float(total_time), progress)
                last_reported[0] = progress
        return model.compute_derivatives(t, y)

    kwargs = {
        "method": scipy_method,
        "t_eval": t_eval,
        "rtol": max(model.rel_tol, 1e-12),
        "atol": max(model.abs_tol, 1e-12),
        "dense_output": False,
    }
    max_step = model.max_step
    if output_step is not None and output_step > 0:
        max_step = min(max_step, output_step)
    if max_step > 0:
        kwargs["max_step"] = max_step
    if model.initial_step > 0:
        kwargs["first_step"] = min(model.initial_step, max_step) if max_step > 0 else model.initial_step

    solution = solve_ivp(derivatives, [t_start, t_stop], initial_states, **kwargs)
    if not solution.success:
        emit_error(f"ODE solver failed: {solution.message}")
        return None

    set_scope_recording(True)
    for index, t in enumerate(solution.t):
        states = solution.y[:, index]
        next_t = float(solution.t[index + 1]) if index + 1 < len(solution.t) else float(t)
        step_size = max(0.0, next_t - float(t))
        model.propagate_signals(float(t), states, step_size=step_size)
    emit_progress(float(t_stop), float(total_time), 100)
    return model
