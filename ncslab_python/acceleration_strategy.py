"""Single source of truth for DLL-first / CuPy-second / CPU-last CUDA simulation policy."""

from __future__ import annotations

from typing import Any, Dict, Optional, Tuple

from .cuda_config import try_cupy

# CuPy RK4 combine only helps when state dimension is large (GPU transfer overhead otherwise).
CUDA_RK4_MIN_STATES = 128


def normalize_solver_name(solver_name: Optional[str]) -> str:
    key = (solver_name or "").strip()
    aliases = {
        "rk4": "ode4",
        "FixedStepAuto": "ode4",
        "VariableStepAuto": "VariableStepAuto",
    }
    return aliases.get(key, key)


def can_use_fixed_step_solver(solver_name: Optional[str]) -> bool:
    solver_key = normalize_solver_name(solver_name).lower()
    return solver_key in {"ode1", "ode2", "ode23", "ode3", "ode4"}


def describe_cuda_rk4_path(
    model,
    use_cuda_acceleration: bool,
    acceleration_manager: Any = None,
) -> Dict[str, Any]:
    """Diagnostics for results.json; optionally reuse a pre-built acceleration manager."""
    cp = try_cupy() if use_cuda_acceleration else None
    solver_key = normalize_solver_name(model.solver_name).lower()
    eligible_fixed = (
        model.step_type == "FixedStep"
        and model.fixed_step > 0
        and can_use_fixed_step_solver(model.solver_name)
    )
    native_dll_available = False
    native_fixed_step_plan_active = False
    native_dll_error = None
    acceleration_manager_description = None
    native_fixed_step_plan_reason = None

    mgr = acceleration_manager
    if use_cuda_acceleration and eligible_fixed:
        if mgr is None:
            try:
                from .acceleration import create_acceleration_manager

                mgr = create_acceleration_manager(model)
            except Exception as exc:
                native_dll_error = str(exc)
                mgr = None
        if mgr is not None:
            try:
                native_backend_name = getattr(getattr(mgr, "backend", None), "name", None)
                native_dll_available = native_backend_name == "cuda"
                native_fixed_step_plan_active = bool(mgr.can_run_fixed_step_cuda_solver(model.solver_name))
                if hasattr(mgr, "describe"):
                    acceleration_manager_description = mgr.describe()
                native_fixed_step_plan_reason = getattr(mgr, "fixed_step_cuda_plan_reason", None)
            except Exception as exc:
                native_dll_available = False
                native_fixed_step_plan_active = False
                native_dll_error = str(exc)

    rk4_cupy = (
        cp is not None
        and use_cuda_acceleration
        and solver_key == "ode4"
        and model.total_states >= CUDA_RK4_MIN_STATES
        and eligible_fixed
        and not native_fixed_step_plan_active
    )
    return {
        "cudaAccelerationRequested": bool(use_cuda_acceleration),
        "cupyAvailable": cp is not None,
        "rk4CuPyCombineActive": rk4_cupy,
        "nativeDllAvailable": native_dll_available,
        "nativeFixedStepPlanActive": native_fixed_step_plan_active,
        "nativeDllError": native_dll_error,
        "nativeFixedStepPlanReason": native_fixed_step_plan_reason,
        "accelerationManager": acceleration_manager_description,
        "minStatesForRk4CuPy": CUDA_RK4_MIN_STATES,
        "totalStates": model.total_states,
        "solver": model.solver_name,
        "stepType": model.step_type,
        "note": "Block RHS (compute_derivatives) runs on CPU; outputs are computed in propagate_signals. "
        "When nativeFixedStepPlanActive is true, fixed-step state advancement uses the CUDA bridge DLL "
        "for eligible linear batches; otherwise CuPy may accelerate RK4 vector combine on CPU state vectors.",
    }


def resolve_cuda_run_flags(model, use_cuda_requested: bool) -> Tuple[bool, str, Dict[str, Any]]:
    """
    Returns (run_cuda_acceleration, reason, probe_meta).

    probe_meta is describe_cuda_rk4_path(model, use_cuda_requested) — one manager probe per invocation.
    """
    probe_meta = describe_cuda_rk4_path(model, use_cuda_requested)
    if not use_cuda_requested:
        return False, "not_requested", probe_meta
    if probe_meta.get("nativeFixedStepPlanActive") or probe_meta.get("rk4CuPyCombineActive"):
        return True, "dll_or_cupy", probe_meta
    return False, "fallback_cpu_no_gpu_path", probe_meta


def cupy_rk4_combine_eligible(
    model,
    use_cuda_acceleration: bool,
    native_fixed_step_plan_active: bool,
    cp,
) -> bool:
    """Whether fixed-step loop should use CuPy for RK4 final combine (RHS still on CPU)."""
    if native_fixed_step_plan_active or cp is None or not use_cuda_acceleration:
        return False
    solver_key = normalize_solver_name(model.solver_name).lower()
    return (
        solver_key == "ode4"
        and model.total_states >= CUDA_RK4_MIN_STATES
        and model.step_type == "FixedStep"
        and model.fixed_step > 0
        and can_use_fixed_step_solver(model.solver_name)
    )
