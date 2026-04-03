import os
import sys
from collections import defaultdict

import numpy as np

from .blocks.continuous import (
    DerivativeBlock,
    IntegratorBlock,
    PIDControllerBlock,
    StateSpaceBlock,
    TransferFcnBlock,
)
from .blocks.source import ClockBlock, ConstantBlock, PulseGeneratorBlock, RampBlock, RepeatingSequenceBlock, SineWaveBlock, StepBlock
from .blocks.source.base import SampledSourceBlock
from .cuda_bridge import get_cuda_bridge
from .signal_utils import infer_signal_shape


_DEBUG_ENABLED = os.environ.get("NCSLAB_PYTHON_DEBUG", "").strip().lower() in {
    "1",
    "true",
    "yes",
    "on",
}


_BATCHABLE_LINEAR_TYPES = tuple(
    block_type
    for block_type in (StateSpaceBlock, IntegratorBlock, TransferFcnBlock, PIDControllerBlock, DerivativeBlock)
    if block_type is not None
)


def _debug_log(message):
    if _DEBUG_ENABLED:
        print(message, file=sys.stderr)


class BatchedSampledSourceEvaluator:
    _TIME_TOLERANCE = 1e-12

    def __init__(self, source_blocks):
        self.source_blocks = tuple(source_blocks)
        self._last_time = None
        self._supported = []
        self._fallback = []
        self._slot_values = {}
        self._slot_buffer_cache = {}
        self._build_groups()

    @staticmethod
    def _is_scalar_signal(value):
        height, width, _ = infer_signal_shape(value)
        return height * width == 1

    @staticmethod
    def _scalar_value(value):
        _, _, flat = infer_signal_shape(value)
        return float(flat[0]) if flat else 0.0

    def _build_groups(self):
        step_blocks = []
        constant_blocks = []
        ramp_blocks = []
        clock_blocks = []
        sine_blocks = []
        pulse_blocks = []
        repeating_blocks = []

        for block in self.source_blocks:
            if isinstance(block, StepBlock) and self._is_scalar_signal(block.initial_value) and self._is_scalar_signal(block.final_value):
                step_blocks.append(block)
                continue
            if isinstance(block, ConstantBlock) and self._is_scalar_signal(block.value):
                constant_blocks.append(block)
                continue
            if isinstance(block, RampBlock) and self._is_scalar_signal(block.slope) and self._is_scalar_signal(block.initial_output):
                ramp_blocks.append(block)
                continue
            if isinstance(block, ClockBlock):
                clock_blocks.append(block)
                continue
            if isinstance(block, SineWaveBlock) and all(
                self._is_scalar_signal(value)
                for value in (block.amplitude, block.frequency, block.phase, block.bias)
            ):
                sine_blocks.append(block)
                continue
            if isinstance(block, PulseGeneratorBlock) and all(
                self._is_scalar_signal(value)
                for value in (block.amplitude, block.period, block.pulse_width, block.phase_delay)
            ):
                pulse_blocks.append(block)
                continue
            if isinstance(block, RepeatingSequenceBlock):
                repeating_blocks.append(block)
                continue
            self._fallback.append(block)

        self._supported = [
            self._build_step_group(step_blocks),
            self._build_constant_group(constant_blocks),
            self._build_ramp_group(ramp_blocks),
            self._build_clock_group(clock_blocks),
            self._build_sine_group(sine_blocks),
            self._build_pulse_group(pulse_blocks),
            self._build_repeating_group(repeating_blocks),
        ]
        self._supported = [group for group in self._supported if group is not None]

    def _build_step_group(self, blocks):
        if not blocks:
            return None
        return {
            "kind": "step",
            "blocks": tuple(blocks),
            "step_times": np.asarray([float(block.step_time) for block in blocks], dtype=np.float64),
            "initials": np.asarray([self._scalar_value(block.initial_value) for block in blocks], dtype=np.float64),
            "finals": np.asarray([self._scalar_value(block.final_value) for block in blocks], dtype=np.float64),
        }

    def _build_constant_group(self, blocks):
        if not blocks:
            return None
        return {
            "kind": "constant",
            "blocks": tuple(blocks),
            "values": np.asarray([self._scalar_value(block.value) for block in blocks], dtype=np.float64),
        }

    def _build_ramp_group(self, blocks):
        if not blocks:
            return None
        return {
            "kind": "ramp",
            "blocks": tuple(blocks),
            "slopes": np.asarray([self._scalar_value(block.slope) for block in blocks], dtype=np.float64),
            "start_times": np.asarray([float(block.start_time) for block in blocks], dtype=np.float64),
            "initials": np.asarray([self._scalar_value(block.initial_output) for block in blocks], dtype=np.float64),
        }

    def _build_clock_group(self, blocks):
        if not blocks:
            return None
        return {
            "kind": "clock",
            "blocks": tuple(blocks),
        }

    def _build_sine_group(self, blocks):
        if not blocks:
            return None
        return {
            "kind": "sine",
            "blocks": tuple(blocks),
            "amplitudes": np.asarray([self._scalar_value(block.amplitude) for block in blocks], dtype=np.float64),
            "frequencies": np.asarray([self._scalar_value(block.frequency) for block in blocks], dtype=np.float64),
            "phases": np.asarray([self._scalar_value(block.phase) for block in blocks], dtype=np.float64),
            "biases": np.asarray([self._scalar_value(block.bias) for block in blocks], dtype=np.float64),
        }

    def _build_pulse_group(self, blocks):
        if not blocks:
            return None
        return {
            "kind": "pulse",
            "blocks": tuple(blocks),
            "amplitudes": np.asarray([self._scalar_value(block.amplitude) for block in blocks], dtype=np.float64),
            "periods": np.asarray([self._scalar_value(block.period) for block in blocks], dtype=np.float64),
            "pulse_widths": np.asarray([self._scalar_value(block.pulse_width) for block in blocks], dtype=np.float64),
            "phase_delays": np.asarray([self._scalar_value(block.phase_delay) for block in blocks], dtype=np.float64),
        }

    def _build_repeating_group(self, blocks):
        if not blocks:
            return None
        return {
            "kind": "repeating",
            "blocks": tuple(blocks),
            "times": tuple(np.asarray(block.time_values, dtype=np.float64) for block in blocks),
            "values": tuple(np.asarray(block.output_values, dtype=np.float64) for block in blocks),
            "periods": np.asarray([float(block.period) for block in blocks], dtype=np.float64),
        }

    @staticmethod
    def _pulse_width_to_time(widths, periods):
        widths = np.asarray(widths, dtype=np.float64)
        periods = np.asarray(periods, dtype=np.float64)
        result = np.zeros_like(widths)
        finite = np.isfinite(widths) & np.isfinite(periods) & (periods > 0.0) & (widths > 0.0)
        if not np.any(finite):
            return result
        unit = finite & (widths <= 1.0)
        percent = finite & (widths > 1.0) & (widths <= 100.0)
        absolute = finite & (widths > 100.0)
        result[unit] = widths[unit] * periods[unit]
        result[percent] = (widths[percent] / 100.0) * periods[percent]
        result[absolute] = widths[absolute]
        return np.clip(result, 0.0, periods, out=result)

    def _evaluate_group_values(self, group, t):
        t_value = float(t)
        kind = group["kind"]
        if kind == "step":
            return np.where(t_value >= group["step_times"], group["finals"], group["initials"])
        if kind == "constant":
            return group["values"]
        if kind == "ramp":
            delta = np.maximum(t_value - group["start_times"], 0.0)
            return np.where(t_value < group["start_times"], group["initials"], group["initials"] + group["slopes"] * delta)
        if kind == "clock":
            return np.full(len(group["blocks"]), t_value, dtype=np.float64)
        if kind == "sine":
            return group["amplitudes"] * np.sin(group["frequencies"] * t_value + group["phases"]) + group["biases"]
        if kind == "pulse":
            amplitudes = group["amplitudes"]
            periods = group["periods"]
            phase_delays = group["phase_delays"]
            on_times = self._pulse_width_to_time(group["pulse_widths"], periods)
            finite = np.isfinite(amplitudes) & np.isfinite(periods) & np.isfinite(phase_delays) & (periods > 0.0)
            shifted = t_value - phase_delays
            active = finite & (shifted >= 0.0) & (on_times > 0.0)
            values = np.zeros(len(amplitudes), dtype=np.float64)
            if np.any(active):
                time_in_period = np.mod(shifted[active], periods[active])
                values[active] = np.where(time_in_period < on_times[active], amplitudes[active], 0.0)
            return values
        if kind == "repeating":
            outputs = np.empty(len(group["blocks"]), dtype=np.float64)
            for index, (time_values, output_values, period) in enumerate(zip(group["times"], group["values"], group["periods"])):
                if period <= 0.0:
                    outputs[index] = float(output_values[0]) if output_values.size else 0.0
                    continue
                local_time = np.mod(t_value, period)
                segment = np.searchsorted(time_values[1:], local_time, side="right")
                segment = min(max(int(segment), 0), len(time_values) - 2)
                start_time = float(time_values[segment])
                end_time = float(time_values[segment + 1])
                start_value = float(output_values[segment])
                end_value = float(output_values[segment + 1])
                duration = end_time - start_time
                if duration <= 0.0:
                    outputs[index] = end_value
                else:
                    ratio = min(max((local_time - start_time) / duration, 0.0), 1.0)
                    outputs[index] = start_value + ratio * (end_value - start_value)
            return outputs
        raise RuntimeError(f"Unsupported sampled source batch kind '{kind}'.")

    def _apply_sampled_outputs(self, blocks, values, t):
        current_time = float(t)
        for block, value in zip(blocks, values):
            scalar_value = float(value)
            if block.sample_time is None:
                block.outputs[0] = scalar_value
                self._slot_values[(id(block), 0)] = np.asarray([scalar_value], dtype=np.float64)
                continue
            if block._should_sample(current_time):
                block.current_output = scalar_value
                block.last_sample_time = current_time
                block.next_sample_time = current_time + block.sample_time
            current_output = float(block.current_output)
            block.outputs[0] = current_output
            self._slot_values[(id(block), 0)] = np.asarray([current_output], dtype=np.float64)

    def get_flat_output(self, block, port=0):
        cached = self._slot_values.get((id(block), int(port)))
        if cached is not None:
            return cached
        _, _, flat = infer_signal_shape(block.outputs.get(port, 0.0))
        values = np.asarray(flat, dtype=np.float64)
        self._slot_values[(id(block), int(port))] = values
        return values

    def collect_slot_buffers(self, slots):
        slot_key = tuple((id(block), int(port)) for block, port in slots)
        cached = self._slot_buffer_cache.get(slot_key)
        if cached is not None:
            return cached
        if not slots:
            payload = (
                np.empty(0, dtype=np.uintp),
                np.empty(0, dtype=np.uintp),
                np.empty(0, dtype=np.float64),
            )
            self._slot_buffer_cache[slot_key] = payload
            return payload
        value_counts = np.empty(len(slots), dtype=np.uintp)
        offsets = np.zeros(len(slots), dtype=np.uintp)
        flat_values = []
        cursor = 0
        for index, (block, port) in enumerate(slots):
            values = self.get_flat_output(block, port)
            value_counts[index] = values.size
            offsets[index] = cursor
            if values.size > 0:
                flat_values.append(values)
                cursor += int(values.size)
        payload = (
            value_counts,
            offsets,
            np.concatenate(flat_values) if flat_values else np.empty(0, dtype=np.float64),
        )
        self._slot_buffer_cache[slot_key] = payload
        return payload

    def sample(self, t):
        t_value = float(t)
        if self._last_time is not None and abs(t_value - self._last_time) <= self._TIME_TOLERANCE:
            return
        self._slot_buffer_cache.clear()
        for group in self._supported:
            values = self._evaluate_group_values(group, t_value)
            self._apply_sampled_outputs(group["blocks"], values, t_value)
        for block in self._fallback:
            block.compute_output(t_value, ())
            _, _, flat = infer_signal_shape(block.outputs.get(0, 0.0))
            self._slot_values[(id(block), 0)] = np.asarray(flat, dtype=np.float64)
        self._last_time = t_value

    def clear_cache(self):
        self._last_time = None
        self._slot_values.clear()
        self._slot_buffer_cache.clear()


class NumpyAccelerationBackend:
    name = "numpy"

    @staticmethod
    def supports_group(group):
        return True

    @staticmethod
    def compute_linear_derivatives(group, states):
        state_vectors = states[group.state_indices]
        input_vectors = group.read_inputs()
        return (
            np.einsum("bij,bj->bi", group.a_stack, state_vectors)
            + np.einsum("bij,bj->bi", group.b_stack, input_vectors)
        )

    @staticmethod
    def compute_linear_outputs(group, states):
        state_vectors = states[group.state_indices]
        output_vectors = np.einsum("bij,bj->bi", group.c_stack, state_vectors)
        if group.output_requires_inputs:
            input_vectors = group.read_inputs()
            output_vectors = output_vectors + np.einsum("bij,bj->bi", group.d_stack, input_vectors)
        return output_vectors

    @staticmethod
    def compute_linear_derivatives_from_state(group, state_vectors):
        input_vectors = group.read_inputs()
        return (
            np.einsum("bij,bj->bi", group.a_stack, state_vectors)
            + np.einsum("bij,bj->bi", group.b_stack, input_vectors)
        )

    @staticmethod
    def compute_linear_outputs_from_state(group, state_vectors):
        output_vectors = np.einsum("bij,bj->bi", group.c_stack, state_vectors)
        if group.output_requires_inputs:
            output_vectors = output_vectors + np.einsum("bij,bj->bi", group.d_stack, group.read_inputs())
        return output_vectors

    @staticmethod
    def compute_linear_derivatives_persistent(group, states):
        return NumpyAccelerationBackend.compute_linear_derivatives(group, states)

    @staticmethod
    def compute_linear_outputs_persistent(group, states):
        return NumpyAccelerationBackend.compute_linear_outputs(group, states)

    @staticmethod
    def compute_linear_derivative_batches_persistent(groups, group_states):
        return [
            (
                np.einsum("bij,bj->bi", group.a_stack, state_vectors)
                + np.einsum("bij,bj->bi", group.b_stack, group.read_inputs())
            )
            for group, state_vectors in zip(groups, group_states)
        ]

    @staticmethod
    def compute_linear_output_batches_persistent(groups, group_states):
        output_vectors = []
        for group, state_vectors in zip(groups, group_states):
            values = np.einsum("bij,bj->bi", group.c_stack, state_vectors)
            if group.output_requires_inputs:
                values = values + np.einsum("bij,bj->bi", group.d_stack, group.read_inputs())
            output_vectors.append(values)
        return output_vectors

    @staticmethod
    def invalidate_output_caches():
        return None

    @staticmethod
    def describe_extra():
        return {}


class CudaAccelerationBackend:
    name = "cuda"

    def __init__(self, bridge):
        self.bridge = bridge
        self._configured_group = None
        self._resident_state_group = None
        self._resident_state_token = None
        self._cached_output_group = None
        self._cached_output_token = None
        self._cached_output_vectors = None
        self._managed_group_handles = {}
        self._managed_state_tokens = {}
        self._managed_output_cache = {}

    @staticmethod
    def supports_group(group):
        return True

    def _reset_cached_state(self):
        self._resident_state_group = None
        self._resident_state_token = None
        self._cached_output_group = None
        self._cached_output_token = None
        self._cached_output_vectors = None

    def _ensure_group(self, group):
        group_id = id(group)
        if self._configured_group == group_id:
            return
        self.bridge.configure_linear_batch(
            group.count,
            group.state_width,
            group.input_width,
            group.output_width,
            group.output_requires_inputs,
            group.a_stack.reshape(-1),
            group.b_stack.reshape(-1),
            group.c_stack.reshape(-1),
            group.d_stack.reshape(-1),
        )
        self._configured_group = group_id
        self._reset_cached_state()

    @staticmethod
    def _state_token(states):
        return id(states)

    def _ensure_state(self, group, states, token):
        if self._resident_state_group == id(group) and self._resident_state_token == token:
            return
        self.upload_group_state(group, states[group.state_indices].reshape(-1))
        self._resident_state_token = token

    @staticmethod
    def _read_input_values(group):
        return np.ascontiguousarray(group.read_inputs().reshape(-1), dtype=np.float64)

    def _ensure_managed_group(self, group):
        group_id = id(group)
        handle = self._managed_group_handles.get(group_id)
        if handle is not None:
            return handle
        handle = self.bridge.create_managed_linear_batch(
            group.count,
            group.state_width,
            group.input_width,
            group.output_width,
            group.output_requires_inputs,
            group.a_stack.reshape(-1),
            group.b_stack.reshape(-1),
            group.c_stack.reshape(-1),
            group.d_stack.reshape(-1),
        )
        self._managed_group_handles[group_id] = handle
        return handle

    def _ensure_managed_state(self, handle, group, states, token):
        if self._managed_state_tokens.get(handle) == token:
            return
        self.bridge.upload_managed_linear_state(handle, states[group.state_indices].reshape(-1))
        self._managed_state_tokens[handle] = token
        self._managed_output_cache.pop(handle, None)

    def get_managed_group_handle(self, group):
        return self._ensure_managed_group(group)

    def create_managed_execution_plan(self, **kwargs):
        return self.bridge.create_managed_execution_plan(**kwargs)

    def release_managed_execution_plan(self, handle):
        self.bridge.release_managed_execution_plan(handle)

    def advance_managed_execution_plan_euler_step(self, handle, source_value_counts, source_offsets, source_values, states, step):
        return self.bridge.advance_managed_execution_plan_euler_step(
            handle,
            source_value_counts,
            source_offsets,
            source_values,
            states,
            step,
        )

    def advance_managed_execution_plan_rk4_step(
        self,
        handle,
        stage1_source_value_counts,
        stage1_source_offsets,
        stage1_source_values,
        stage2_source_value_counts,
        stage2_source_offsets,
        stage2_source_values,
        stage3_source_value_counts,
        stage3_source_offsets,
        stage3_source_values,
        stage4_source_value_counts,
        stage4_source_offsets,
        stage4_source_values,
        states,
        step,
    ):
        return self.bridge.advance_managed_execution_plan_rk4_step(
            handle,
            stage1_source_value_counts,
            stage1_source_offsets,
            stage1_source_values,
            stage2_source_value_counts,
            stage2_source_offsets,
            stage2_source_values,
            stage3_source_value_counts,
            stage3_source_offsets,
            stage3_source_values,
            stage4_source_value_counts,
            stage4_source_offsets,
            stage4_source_values,
            states,
            step,
        )

    def upload_group_state(self, group, state_values):
        self._ensure_group(group)
        self.bridge.upload_linear_state(np.ascontiguousarray(state_values, dtype=np.float64))
        self._resident_state_group = id(group)
        self._cached_output_group = None
        self._cached_output_token = None
        self._cached_output_vectors = None

    def download_group_state(self, group):
        self._ensure_group(group)
        return self.bridge.download_linear_state(group.count * group.state_width).reshape(group.count, group.state_width)

    def advance_group_fixed_step(self, group, solver_name, stage_inputs, step):
        self._ensure_group(group)
        solver_key = (solver_name or "").strip().lower()
        if solver_key == "ode1":
            self.bridge.advance_linear_euler_step(np.asarray(stage_inputs[0], dtype=np.float64).reshape(-1), step)
        elif solver_key == "ode4":
            self.bridge.advance_linear_rk4_step(
                np.asarray(stage_inputs[0], dtype=np.float64).reshape(-1),
                np.asarray(stage_inputs[1], dtype=np.float64).reshape(-1),
                np.asarray(stage_inputs[2], dtype=np.float64).reshape(-1),
                np.asarray(stage_inputs[3], dtype=np.float64).reshape(-1),
                step,
            )
        else:
            raise RuntimeError(f"Unsupported CUDA fixed-step solver '{solver_name}'.")
        self._resident_state_group = id(group)
        self._resident_state_token = None
        self._cached_output_group = None
        self._cached_output_token = None
        self._cached_output_vectors = None

    def compute_linear_derivatives(self, group, states):
        self._ensure_group(group)
        token = self._state_token(states)
        self._ensure_state(group, states, token)
        self.bridge.upload_linear_inputs(self._read_input_values(group))
        self.bridge.evaluate_linear_derivative_batch()
        return self.bridge.download_linear_derivative_batch(group.count * group.state_width).reshape(
            group.count,
            group.state_width,
        )

    def compute_linear_outputs(self, group, states):
        self._ensure_group(group)
        token = self._state_token(states)
        use_cache = not group.output_requires_inputs
        if use_cache and self._cached_output_group == id(group) and self._cached_output_token == token:
            return self._cached_output_vectors.copy()
        self._ensure_state(group, states, token)
        if group.output_requires_inputs:
            self.bridge.upload_linear_inputs(self._read_input_values(group))
        self.bridge.evaluate_linear_output_batch()
        output_vectors = self.bridge.download_linear_output_batch(group.count * group.output_width).reshape(
            group.count,
            group.output_width,
        )
        if use_cache:
            self._cached_output_group = id(group)
            self._cached_output_token = token
            self._cached_output_vectors = output_vectors.copy()
        return output_vectors

    def compute_linear_derivatives_from_state(self, group, state_vectors):
        return self.compute_linear_derivative_batches_persistent((group,), (state_vectors,))[0]

    def compute_linear_outputs_from_state(self, group, state_vectors):
        return self.compute_linear_output_batches_persistent((group,), (state_vectors,))[0]

    def compute_linear_derivatives_persistent(self, group, states):
        handle = self._ensure_managed_group(group)
        token = self._state_token(states)
        self._ensure_managed_state(handle, group, states, token)
        self.bridge.upload_managed_linear_inputs(handle, self._read_input_values(group))
        self.bridge.evaluate_managed_linear_derivative_batch(handle)
        return self.bridge.download_managed_linear_derivative_batch(
            handle,
            group.count * group.state_width,
        ).reshape(group.count, group.state_width)

    def compute_linear_outputs_persistent(self, group, states):
        handle = self._ensure_managed_group(group)
        token = self._state_token(states)
        use_cache = not group.output_requires_inputs
        cached = self._managed_output_cache.get(handle)
        if use_cache and cached is not None and cached[0] == token:
            return cached[1].copy()
        self._ensure_managed_state(handle, group, states, token)
        if group.output_requires_inputs:
            self.bridge.upload_managed_linear_inputs(handle, self._read_input_values(group))
        self.bridge.evaluate_managed_linear_output_batch(handle)
        output_vectors = self.bridge.download_managed_linear_output_batch(
            handle,
            group.count * group.output_width,
        ).reshape(group.count, group.output_width)
        if use_cache:
            self._managed_output_cache[handle] = (token, output_vectors.copy())
        return output_vectors

    def compute_linear_derivative_batches_persistent(self, groups, group_states):
        if not groups:
            return []
        handles = np.asarray([self._ensure_managed_group(group) for group in groups], dtype=np.int32)
        state_value_counts = np.asarray([state_vectors.size for state_vectors in group_states], dtype=np.uintp)
        input_value_counts = np.asarray([group.count * group.input_width for group in groups], dtype=np.uintp)
        state_offsets = np.zeros(len(groups), dtype=np.uintp)
        input_offsets = np.zeros(len(groups), dtype=np.uintp)
        if len(groups) > 1:
            state_offsets[1:] = np.cumsum(state_value_counts[:-1], dtype=np.uintp)
            input_offsets[1:] = np.cumsum(input_value_counts[:-1], dtype=np.uintp)
        flat_states = np.concatenate([np.asarray(state_vectors, dtype=np.float64).reshape(-1) for state_vectors in group_states])
        flat_inputs = np.concatenate([self._read_input_values(group) for group in groups])
        flat_derivatives = self.bridge.evaluate_managed_linear_derivative_batches(
            handles,
            state_value_counts,
            input_value_counts,
            state_offsets,
            input_offsets,
            flat_states,
            flat_inputs,
        )
        derivative_vectors = []
        for group, offset, value_count in zip(groups, state_offsets, state_value_counts):
            start = int(offset)
            end = start + int(value_count)
            derivative_vectors.append(
                flat_derivatives[start:end].reshape(group.count, group.state_width)
            )
        return derivative_vectors

    def compute_linear_output_batches_persistent(self, groups, group_states):
        if not groups:
            return []
        handles = np.asarray([self._ensure_managed_group(group) for group in groups], dtype=np.int32)
        state_value_counts = np.asarray([state_vectors.size for state_vectors in group_states], dtype=np.uintp)
        input_value_counts = np.asarray([group.count * group.input_width for group in groups], dtype=np.uintp)
        output_value_counts = np.asarray([group.count * group.output_width for group in groups], dtype=np.uintp)
        state_offsets = np.zeros(len(groups), dtype=np.uintp)
        input_offsets = np.zeros(len(groups), dtype=np.uintp)
        output_offsets = np.zeros(len(groups), dtype=np.uintp)
        if len(groups) > 1:
            state_offsets[1:] = np.cumsum(state_value_counts[:-1], dtype=np.uintp)
            input_offsets[1:] = np.cumsum(input_value_counts[:-1], dtype=np.uintp)
            output_offsets[1:] = np.cumsum(output_value_counts[:-1], dtype=np.uintp)
        flat_states = np.concatenate([np.asarray(state_vectors, dtype=np.float64).reshape(-1) for state_vectors in group_states])
        flat_inputs = np.concatenate([self._read_input_values(group) for group in groups]) if groups else np.empty(0, dtype=np.float64)
        flat_outputs = self.bridge.evaluate_managed_linear_output_batches(
            handles,
            state_value_counts,
            input_value_counts,
            output_value_counts,
            state_offsets,
            input_offsets,
            output_offsets,
            flat_states,
            flat_inputs,
        )
        output_vectors = []
        for group, offset, value_count in zip(groups, output_offsets, output_value_counts):
            start = int(offset)
            end = start + int(value_count)
            output_vectors.append(
                flat_outputs[start:end].reshape(group.count, group.output_width)
            )
        return output_vectors

    def invalidate_output_caches(self):
        self._resident_state_group = None
        self._resident_state_token = None
        self._cached_output_group = None
        self._cached_output_token = None
        self._cached_output_vectors = None
        self._managed_state_tokens.clear()
        self._managed_output_cache.clear()

    def describe_extra(self):
        return {
            "device": self.bridge.device_name(),
            "dll": str(self.bridge.dll_path),
            "residentState": True,
            "genericLinearBatch": True,
        }


class LinearBatchGroup:
    def __init__(self, blocks, output_requires_inputs=False):
        self.blocks = tuple(blocks)
        self.count = len(self.blocks)
        self.state_width = self.blocks[0].num_states
        self.input_width = self.blocks[0].input_width
        self.output_width = self.blocks[0].output_width
        self.output_requires_inputs = output_requires_inputs
        self.state_indices = np.asarray(
            [
                np.arange(block.state_offset, block.state_offset + block.num_states, dtype=int)
                for block in self.blocks
            ],
            dtype=int,
        )
        self.a_stack = np.stack([block.A_mat for block in self.blocks], axis=0)
        self.b_stack = np.stack([block.B_mat for block in self.blocks], axis=0)
        self.c_stack = np.stack([block.C_mat for block in self.blocks], axis=0)
        self.d_stack = np.stack([block.D_mat for block in self.blocks], axis=0)

    def read_inputs(self):
        return np.asarray([block.get_input_vector() for block in self.blocks], dtype=float)

    def write_outputs(self, output_vectors):
        for block, output_vector in zip(self.blocks, output_vectors):
            block.set_output_vector(output_vector)

    def write_derivatives(self, derivatives, derivative_vectors):
        derivatives[self.state_indices] = derivative_vectors

    def copy_state_into(self, full_state, group_state_vectors):
        full_state[self.state_indices] = group_state_vectors


class BaseFixedStepCudaPlan:
    plan_type = "base"
    sync_disables_output_batching = False

    def initialize(self, full_state, backend):
        del full_state, backend

    def sync_state(self, full_state, backend):
        del backend
        return full_state

    def advance(self, solver_name, t, step, full_state, backend):
        raise NotImplementedError


class SourceDrivenResidentFixedStepCudaPlan(BaseFixedStepCudaPlan):
    plan_type = "single_batch_resident"
    sync_disables_output_batching = True

    def __init__(self, group, source_blocks):
        self.group = group
        self.source_blocks = tuple(source_blocks)
        self._source_sampler = BatchedSampledSourceEvaluator(self.source_blocks)

    def _refresh_sources(self, t):
        self._source_sampler.sample(float(t))

    def sample_inputs(self, t):
        self._refresh_sources(t)
        for block in self.group.blocks:
            block.inputs.clear()
            for from_block, from_port, to_port in block.input_sources:
                block.inputs[to_port] = from_block.outputs.get(from_port, 0.0)
        return self.group.read_inputs()

    def stage_inputs(self, solver_name, t, step):
        solver_key = (solver_name or "").strip().lower()
        if solver_key == "ode1":
            return (self.sample_inputs(t),)
        if solver_key == "ode4":
            half_step = 0.5 * step
            return (
                self.sample_inputs(t),
                self.sample_inputs(t + half_step),
                self.sample_inputs(t + half_step),
                self.sample_inputs(t + step),
            )
        raise RuntimeError(f"Unsupported CUDA fixed-step solver '{solver_name}'.")

    def initialize(self, full_state, backend):
        backend.upload_group_state(self.group, full_state[self.group.state_indices].reshape(-1))

    def sync_state(self, full_state, backend):
        group_state = backend.download_group_state(self.group)
        self.group.copy_state_into(full_state, group_state)
        return full_state

    def advance(self, solver_name, t, step, full_state, backend):
        stage_inputs = self.stage_inputs(solver_name, t, step)
        backend.advance_group_fixed_step(self.group, solver_name, stage_inputs, step)
        return full_state


class MultiBatchExecutionLevelFixedStepCudaPlan(BaseFixedStepCudaPlan):
    plan_type = "multi_batch_execution_levels"
    sync_disables_output_batching = False

    def __init__(self, model, derivative_groups):
        self.model = model
        self.derivative_groups = tuple(derivative_groups)
        self._derivative_group_indices = {
            tuple(id(block) for block in group.blocks): index
            for index, group in enumerate(self.derivative_groups)
        }
        self.source_blocks = tuple(
            sorted(
                [block for block in model.active_blocks if isinstance(block, SampledSourceBlock)],
                key=lambda block: model.block_order_index.get(block.block_name, 0),
            )
        )
        self._source_sampler = BatchedSampledSourceEvaluator(self.source_blocks)
        self.pre_output_groups = []
        self.level_output_groups = defaultdict(list)
        self._execution_levels = []
        self._native_execution_plan_handle = None
        self._native_execution_plan_failed = False
        self._native_source_slots = ()
        self._native_execution_plan_active = False
        self._source_buffer_cache = {}
        self._source_buffer_cache_order = []
        self._build_output_groups()
        self._build_execution_levels()
        self._use_group_state_stage = len(self.derivative_groups) > 1
        total_output_group_count = len(self.pre_output_groups) + sum(len(groups) for groups in self.level_output_groups.values())
        self._use_native_batch_merge = len(self.derivative_groups) >= 3 or total_output_group_count >= 3

    @staticmethod
    def _signature(block):
        return (block.num_states, block.input_width, block.output_width)

    @staticmethod
    def _flatten_signal_values(value):
        _, _, flat = infer_signal_shape(value)
        return np.asarray(flat, dtype=np.float64)

    def _build_output_groups(self):
        component_by_block = {
            id(block): component
            for component in self.model.execution_components
            for block in component["blocks"]
        }
        derivative_group_by_blocks = {
            tuple(id(block) for block in group.blocks): group
            for group in self.derivative_groups
        }
        pre_output_candidates = defaultdict(list)
        level_output_candidates = defaultdict(list)

        for group in self.derivative_groups:
            for block in group.blocks:
                component = component_by_block.get(id(block))
                if component is None:
                    continue
                if block.is_direct_feedthrough():
                    # Some DTOs / execution component descriptors may omit execution_level.
                    # Default to level 0 to keep CUDA plan building robust.
                    key = (component.get("execution_level", 0),) + self._signature(block)
                    level_output_candidates[key].append(block)
                else:
                    pre_output_candidates[self._signature(block)].append(block)

        self.pre_output_groups = []
        for blocks in pre_output_candidates.values():
            if not blocks:
                continue
            block_key = tuple(id(block) for block in blocks)
            self.pre_output_groups.append(
                derivative_group_by_blocks.get(block_key, LinearBatchGroup(blocks, output_requires_inputs=False))
            )
        for key, blocks in level_output_candidates.items():
            if blocks:
                self.level_output_groups[key[0]].append(LinearBatchGroup(blocks, output_requires_inputs=True))

    def _build_execution_levels(self):
        index = 0
        components = self.model.execution_components
        while index < len(components):
            level = components[index].get("execution_level", 0)
            level_components = []
            while index < len(components) and components[index].get("execution_level", 0) == level:
                level_components.append(components[index])
                index += 1
            self._execution_levels.append((level, tuple(level_components)))

    def _refresh_sources(self, t):
        self._source_sampler.sample(float(t))

    @staticmethod
    def _group_state_key(group):
        return tuple(id(block) for block in group.blocks)

    def _extract_group_states(self, full_state):
        return [np.asarray(full_state[group.state_indices], dtype=np.float64).copy() for group in self.derivative_groups]

    def _group_state_for(self, group, group_states):
        group_index = self._derivative_group_indices.get(self._group_state_key(group))
        if group_index is None:
            raise RuntimeError("Missing derivative-state mapping for execution-level CUDA group.")
        return group_states[group_index]

    @staticmethod
    def _source_time_key(t):
        return round(float(t), 12)

    def _remember_source_buffer(self, key, payload):
        if key in self._source_buffer_cache:
            return
        self._source_buffer_cache[key] = payload
        self._source_buffer_cache_order.append(key)
        while len(self._source_buffer_cache_order) > 4:
            stale = self._source_buffer_cache_order.pop(0)
            self._source_buffer_cache.pop(stale, None)

    def _collect_source_stage_buffers(self, t):
        cache_key = self._source_time_key(t)
        cached = self._source_buffer_cache.get(cache_key)
        if cached is not None:
            return cached
        self._refresh_sources(t)
        payload = self._source_sampler.collect_slot_buffers(self._native_source_slots)
        self._remember_source_buffer(cache_key, payload)
        return payload

    def _build_native_execution_plan(self, backend):
        if backend.name != "cuda" or not hasattr(backend, "create_managed_execution_plan"):
            return None

        plan_groups = []
        group_index_by_id = {}

        def register_group(group):
            group_id = id(group)
            group_index = group_index_by_id.get(group_id)
            if group_index is not None:
                return group_index
            group_index = len(plan_groups)
            group_index_by_id[group_id] = group_index
            plan_groups.append(group)
            return group_index

        for group in self.derivative_groups:
            register_group(group)
        for group in self.pre_output_groups:
            register_group(group)
        for level_groups in self.level_output_groups.values():
            for group in level_groups:
                register_group(group)

        output_group_by_block = {}
        for group in self.pre_output_groups:
            group_index = group_index_by_id[id(group)]
            for block_index, block in enumerate(group.blocks):
                output_group_by_block[id(block)] = (group_index, block_index)
        for level_groups in self.level_output_groups.values():
            for group in level_groups:
                group_index = group_index_by_id[id(group)]
                for block_index, block in enumerate(group.blocks):
                    output_group_by_block[id(block)] = (group_index, block_index)

        source_slots = []
        source_slot_index_by_key = {}
        handles = []
        group_input_value_counts = []
        group_output_value_counts = []
        group_state_index_counts = []
        group_state_index_offsets = []
        flat_state_indices = []
        route_source_kinds = []
        route_source_indices = []
        route_source_offsets = []
        route_source_widths = []
        route_target_offsets = []
        route_target_widths = []
        group_route_counts = []
        group_route_offsets = []

        for group in plan_groups:
            handles.append(backend.get_managed_group_handle(group))
            state_indices = np.asarray(group.state_indices, dtype=np.uintp).reshape(-1)
            group_state_index_offsets.append(len(flat_state_indices))
            group_state_index_counts.append(state_indices.size)
            flat_state_indices.extend(state_indices.tolist())
            group_input_value_counts.append(group.count * group.input_width)
            group_output_value_counts.append(group.count * group.output_width)

            route_offset = len(route_source_kinds)
            for block_index, block in enumerate(group.blocks):
                source_connection = None
                for from_block, from_port, to_port in block.input_sources:
                    if to_port == 0:
                        source_connection = (from_block, int(from_port))
                if source_connection is None:
                    continue

                from_block, from_port = source_connection
                target_offset = block_index * group.input_width
                if isinstance(from_block, SampledSourceBlock):
                    slot_key = (id(from_block), from_port)
                    slot_index = source_slot_index_by_key.get(slot_key)
                    if slot_index is None:
                        slot_index = len(source_slots)
                        source_slot_index_by_key[slot_key] = slot_index
                        source_slots.append((from_block, from_port))
                    route_source_kinds.append(0)
                    route_source_indices.append(slot_index)
                    route_source_offsets.append(0)
                    route_source_widths.append(0)
                else:
                    if from_port != 0:
                        return None
                    producer = output_group_by_block.get(id(from_block))
                    if producer is None:
                        return None
                    producer_group_index, producer_block_index = producer
                    producer_group = plan_groups[producer_group_index]
                    route_source_kinds.append(1)
                    route_source_indices.append(producer_group_index)
                    route_source_offsets.append(producer_block_index * producer_group.output_width)
                    route_source_widths.append(producer_group.output_width)
                route_target_offsets.append(target_offset)
                route_target_widths.append(group.input_width)
            group_route_offsets.append(route_offset)
            group_route_counts.append(len(route_source_kinds) - route_offset)

        pre_output_group_indices = [group_index_by_id[id(group)] for group in self.pre_output_groups]
        level_group_counts = []
        level_group_offsets = []
        level_group_indices = []
        for level, _ in self._execution_levels:
            groups = self.level_output_groups.get(level, ())
            level_group_offsets.append(len(level_group_indices))
            level_group_counts.append(len(groups))
            level_group_indices.extend(group_index_by_id[id(group)] for group in groups)
        derivative_group_indices = [group_index_by_id[id(group)] for group in self.derivative_groups]

        handle = backend.create_managed_execution_plan(
            total_state_count=self.model.total_states,
            group_handles=np.asarray(handles, dtype=np.int32),
            group_input_value_counts=np.asarray(group_input_value_counts, dtype=np.uintp),
            group_output_value_counts=np.asarray(group_output_value_counts, dtype=np.uintp),
            group_state_index_counts=np.asarray(group_state_index_counts, dtype=np.uintp),
            group_state_index_offsets=np.asarray(group_state_index_offsets, dtype=np.uintp),
            state_indices=np.asarray(flat_state_indices, dtype=np.uintp),
            route_source_kinds=np.asarray(route_source_kinds, dtype=np.int32),
            route_source_indices=np.asarray(route_source_indices, dtype=np.uintp),
            route_source_offsets=np.asarray(route_source_offsets, dtype=np.uintp),
            route_source_widths=np.asarray(route_source_widths, dtype=np.uintp),
            route_target_offsets=np.asarray(route_target_offsets, dtype=np.uintp),
            route_target_widths=np.asarray(route_target_widths, dtype=np.uintp),
            group_route_counts=np.asarray(group_route_counts, dtype=np.uintp),
            group_route_offsets=np.asarray(group_route_offsets, dtype=np.uintp),
            pre_output_group_indices=np.asarray(pre_output_group_indices, dtype=np.uintp),
            level_group_counts=np.asarray(level_group_counts, dtype=np.uintp),
            level_group_offsets=np.asarray(level_group_offsets, dtype=np.uintp),
            level_group_indices=np.asarray(level_group_indices, dtype=np.uintp),
            derivative_group_indices=np.asarray(derivative_group_indices, dtype=np.uintp),
        )
        self._native_source_slots = tuple(source_slots)
        self._native_execution_plan_handle = handle
        self._native_execution_plan_active = True
        return handle

    def _ensure_native_execution_plan(self, backend):
        if self._native_execution_plan_handle is not None:
            return self._native_execution_plan_handle
        if self._native_execution_plan_failed:
            return None
        try:
            return self._build_native_execution_plan(backend)
        except Exception as exc:
            self._native_execution_plan_failed = True
            self._native_execution_plan_active = False
            _debug_log(f"[Acceleration] native execution plan unavailable: {exc}")
            return None

    def _advance_with_native_execution_plan(self, solver_name, t, step, full_state, backend):
        handle = self._ensure_native_execution_plan(backend)
        if handle is None:
            return None
        solver_key = (solver_name or "").strip().lower()
        if solver_key == "ode1":
            source_value_counts, source_offsets, source_values = self._collect_source_stage_buffers(t)
            return backend.advance_managed_execution_plan_euler_step(
                handle,
                source_value_counts,
                source_offsets,
                source_values,
                full_state,
                step,
            )
        if solver_key != "ode4":
            raise RuntimeError(f"Unsupported CUDA fixed-step solver '{solver_name}'.")
        half_step = 0.5 * step
        stage1 = self._collect_source_stage_buffers(t)
        stage2 = self._collect_source_stage_buffers(t + half_step)
        stage3 = self._collect_source_stage_buffers(t + half_step)
        stage4 = self._collect_source_stage_buffers(t + step)
        return backend.advance_managed_execution_plan_rk4_step(
            handle,
            stage1[0],
            stage1[1],
            stage1[2],
            stage2[0],
            stage2[1],
            stage2[2],
            stage3[0],
            stage3[1],
            stage3[2],
            stage4[0],
            stage4[1],
            stage4[2],
            full_state,
            step,
        )

    def _compute_group_outputs_from_state_vectors(self, groups, group_states, backend):
        if not groups:
            return
        state_vectors = [self._group_state_for(group, group_states) for group in groups]
        if self._use_native_batch_merge:
            output_vectors = backend.compute_linear_output_batches_persistent(groups, state_vectors)
        else:
            output_vectors = [backend.compute_linear_outputs_from_state(group, state) for group, state in zip(groups, state_vectors)]
        for group, values in zip(groups, output_vectors):
            group.write_outputs(values)

    def _evaluate_stage_inputs_from_state_vectors(self, t, group_states, backend):
        self._refresh_sources(t)
        self._compute_group_outputs_from_state_vectors(self.pre_output_groups, group_states, backend)
        for level, level_components in self._execution_levels:
            for component in level_components:
                if component["is_algebraic_loop"]:
                    raise RuntimeError("Execution-level CUDA plan does not support algebraic loops.")
                for block in component["blocks"]:
                    self.model._load_inputs_for_block(block)
            self._compute_group_outputs_from_state_vectors(self.level_output_groups.get(level, ()), group_states, backend)

    def _evaluate_stage_derivatives_from_state_vectors(self, t, group_states, backend):
        self._evaluate_stage_inputs_from_state_vectors(t, group_states, backend)
        if self._use_native_batch_merge:
            return backend.compute_linear_derivative_batches_persistent(self.derivative_groups, group_states)
        return [backend.compute_linear_derivatives_from_state(group, state) for group, state in zip(self.derivative_groups, group_states)]

    @staticmethod
    def _compose_stage_states(base_group_states, stage_derivatives, scale):
        return [base_states + scale * derivatives for base_states, derivatives in zip(base_group_states, stage_derivatives)]

    @staticmethod
    def _write_next_state(full_state, derivative_groups, next_group_states):
        for group, group_state in zip(derivative_groups, next_group_states):
            full_state[group.state_indices] = group_state
        return full_state

    def _compute_group_outputs_from_full_state(self, groups, full_state, backend):
        for group in groups:
            output_vectors = backend.compute_linear_outputs_persistent(group, full_state)
            group.write_outputs(output_vectors)

    def _evaluate_stage_inputs_from_full_state(self, t, full_state, backend):
        self._refresh_sources(t)
        self._compute_group_outputs_from_full_state(self.pre_output_groups, full_state, backend)
        for level, level_components in self._execution_levels:
            for component in level_components:
                if component["is_algebraic_loop"]:
                    raise RuntimeError("Execution-level CUDA plan does not support algebraic loops.")
                for block in component["blocks"]:
                    self.model._load_inputs_for_block(block)
            self._compute_group_outputs_from_full_state(self.level_output_groups.get(level, ()), full_state, backend)

    def _evaluate_stage_derivatives_from_full_state(self, t, full_state, backend):
        self._evaluate_stage_inputs_from_full_state(t, full_state, backend)
        return [backend.compute_linear_derivatives_persistent(group, full_state).copy() for group in self.derivative_groups]

    def _advance_with_full_state(self, solver_name, t, step, full_state, backend):
        solver_key = (solver_name or "").strip().lower()
        if solver_key == "ode1":
            derivatives = self._evaluate_stage_derivatives_from_full_state(t, full_state, backend)
            for group, derivative_vectors in zip(self.derivative_groups, derivatives):
                full_state[group.state_indices] = full_state[group.state_indices] + step * derivative_vectors
            return full_state
        if solver_key != "ode4":
            raise RuntimeError(f"Unsupported CUDA fixed-step solver '{solver_name}'.")
        half_step = 0.5 * step
        base_state = full_state.copy()
        k1 = self._evaluate_stage_derivatives_from_full_state(t, base_state, backend)
        stage2_state = base_state.copy()
        for group, derivatives in zip(self.derivative_groups, k1):
            stage2_state[group.state_indices] = base_state[group.state_indices] + half_step * derivatives
        k2 = self._evaluate_stage_derivatives_from_full_state(t + half_step, stage2_state, backend)
        stage3_state = base_state.copy()
        for group, derivatives in zip(self.derivative_groups, k2):
            stage3_state[group.state_indices] = base_state[group.state_indices] + half_step * derivatives
        k3 = self._evaluate_stage_derivatives_from_full_state(t + half_step, stage3_state, backend)
        stage4_state = base_state.copy()
        for group, derivatives in zip(self.derivative_groups, k3):
            stage4_state[group.state_indices] = base_state[group.state_indices] + step * derivatives
        k4 = self._evaluate_stage_derivatives_from_full_state(t + step, stage4_state, backend)
        for group, d1, d2, d3, d4 in zip(self.derivative_groups, k1, k2, k3, k4):
            full_state[group.state_indices] = base_state[group.state_indices] + (step / 6.0) * (d1 + 2.0 * d2 + 2.0 * d3 + d4)
        return full_state

    def _advance_with_group_states(self, solver_name, t, step, full_state, backend):
        solver_key = (solver_name or "").strip().lower()
        base_group_states = self._extract_group_states(full_state)
        if solver_key == "ode1":
            derivatives = self._evaluate_stage_derivatives_from_state_vectors(t, base_group_states, backend)
            next_group_states = [
                base_states + step * derivative_vectors
                for base_states, derivative_vectors in zip(base_group_states, derivatives)
            ]
            return self._write_next_state(full_state, self.derivative_groups, next_group_states)
        if solver_key != "ode4":
            raise RuntimeError(f"Unsupported CUDA fixed-step solver '{solver_name}'.")
        half_step = 0.5 * step
        k1 = self._evaluate_stage_derivatives_from_state_vectors(t, base_group_states, backend)
        stage2_states = self._compose_stage_states(base_group_states, k1, half_step)
        k2 = self._evaluate_stage_derivatives_from_state_vectors(t + half_step, stage2_states, backend)
        stage3_states = self._compose_stage_states(base_group_states, k2, half_step)
        k3 = self._evaluate_stage_derivatives_from_state_vectors(t + half_step, stage3_states, backend)
        stage4_states = self._compose_stage_states(base_group_states, k3, step)
        k4 = self._evaluate_stage_derivatives_from_state_vectors(t + step, stage4_states, backend)
        next_group_states = [
            base_states + (step / 6.0) * (d1 + 2.0 * d2 + 2.0 * d3 + d4)
            for base_states, d1, d2, d3, d4 in zip(base_group_states, k1, k2, k3, k4)
        ]
        return self._write_next_state(full_state, self.derivative_groups, next_group_states)

    def advance(self, solver_name, t, step, full_state, backend):
        if backend.name == "cuda":
            native_state = self._advance_with_native_execution_plan(solver_name, t, step, full_state, backend)
            if native_state is not None:
                return native_state
        if not self._use_group_state_stage:
            return self._advance_with_full_state(solver_name, t, step, full_state, backend)
        return self._advance_with_group_states(solver_name, t, step, full_state, backend)


class SimulationAccelerationManager:
    def __init__(self, model, backend, min_batch_size=4):
        self.model = model
        self.backend = backend
        self.derivative_groups = []
        self.pre_output_groups = []
        self.level_output_groups = defaultdict(list)
        self._derivative_block_ids = set()
        self._output_block_ids = set()
        self.fixed_step_cuda_plan = None
        self.fixed_step_cuda_plan_reason = None
        self.output_batching_enabled = True

        if not _BATCHABLE_LINEAR_TYPES:
            return

        candidates = [block for block in model.active_blocks if self._can_batch_block(block)]
        derivative_candidates = defaultdict(list)
        for block in candidates:
            derivative_candidates[self._signature(block)].append(block)

        derivative_group_by_blocks = {}
        for blocks in derivative_candidates.values():
            if len(blocks) < min_batch_size:
                continue
            group = LinearBatchGroup(blocks, output_requires_inputs=False)
            if not self.backend.supports_group(group):
                continue
            self.derivative_groups.append(group)
            derivative_group_by_blocks[tuple(id(block) for block in group.blocks)] = group
            self._derivative_block_ids.update(id(block) for block in group.blocks)

        pre_output_candidates = defaultdict(list)
        level_output_candidates = defaultdict(list)
        component_by_block = {
            id(block): component
            for component in model.execution_components
            for block in component["blocks"]
        }
        for block in candidates:
            component = component_by_block.get(id(block))
            if component is None or component["is_algebraic_loop"]:
                continue
            if block.is_direct_feedthrough():
                # execution_level may be absent in some model DTOs.
                # Default to level 0 to avoid hard failure during CUDA plan build.
                key = (component.get("execution_level", 0),) + self._signature(block)
                level_output_candidates[key].append(block)
            else:
                pre_output_candidates[self._signature(block)].append(block)

        for blocks in pre_output_candidates.values():
            if len(blocks) < min_batch_size:
                continue
            block_key = tuple(id(block) for block in blocks)
            group = derivative_group_by_blocks.get(block_key)
            if group is None:
                group = LinearBatchGroup(blocks, output_requires_inputs=False)
                if not self.backend.supports_group(group):
                    continue
            self.pre_output_groups.append(group)
            self._output_block_ids.update(id(block) for block in group.blocks)

        for key, blocks in level_output_candidates.items():
            if len(blocks) < min_batch_size:
                continue
            group = LinearBatchGroup(blocks, output_requires_inputs=True)
            if not self.backend.supports_group(group):
                continue
            self.level_output_groups[key[0]].append(group)
            self._output_block_ids.update(id(block) for block in group.blocks)

        self.fixed_step_cuda_plan = self._build_fixed_step_cuda_plan()

        if self.enabled:
            _debug_log(
                "[Acceleration] backend="
                f"{self.backend.name}, derivative_groups={len(self.derivative_groups)}, "
                f"output_groups={len(self.pre_output_groups) + sum(len(groups) for groups in self.level_output_groups.values())}"
            )

    @staticmethod
    def _signature(block):
        return ("linear_continuous", block.num_states, block.input_width, block.output_width)

    @staticmethod
    def _can_batch_block(block):
        if not isinstance(block, _BATCHABLE_LINEAR_TYPES) or block.num_states <= 0:
            return False
        supports_linear_batching = getattr(block, "supports_linear_batching", None)
        if supports_linear_batching is not None and not supports_linear_batching():
            return False
        return all(
            hasattr(block, attr)
            for attr in ("A_mat", "B_mat", "C_mat", "D_mat", "get_input_vector", "set_output_vector")
        )

    def _build_fixed_step_cuda_plan(self):
        if self.backend.name != "cuda":
            self.fixed_step_cuda_plan_reason = "backend_not_cuda"
            return None
        if len(self.model.algebraic_loops) > 0:
            self.fixed_step_cuda_plan_reason = "has_algebraic_loops"
            return None
        if not self.derivative_groups:
            self.fixed_step_cuda_plan_reason = "no_derivative_groups"
            return None

        stateful_blocks = [block for block in self.model.active_blocks if block.num_states > 0]
        if not stateful_blocks:
            self.fixed_step_cuda_plan_reason = "no_stateful_blocks"
            return None
        if len(stateful_blocks) != len(self._derivative_block_ids):
            self.fixed_step_cuda_plan_reason = (
                "stateful_blocks_mismatch"
                f"(stateful={len(stateful_blocks)}, batched_derivative_blocks={len(self._derivative_block_ids)})"
            )
            return None
        if any(not self.is_derivative_batched(block) for block in stateful_blocks):
            self.fixed_step_cuda_plan_reason = "some_stateful_block_not_derivative_batched"
            return None

        stateful_block_ids = {id(block) for block in stateful_blocks}
        # CUDA fixed-step plans only need to stage inputs for *stateful* blocks.
        # Non-stateful blocks (e.g. Scope, Derivative used only for visualization) can still
        # be evaluated on CPU via `model.propagate_signals()` after we advance and sync states.
        #
        # However, if a non-stateful block *feeds* a stateful block, then CUDA must be able to
        # provide its output during staging, otherwise the plan is unsafe to activate.
        upstream_of_stateful = set()
        for block in stateful_blocks:
            for from_block, _from_port, _to_port in getattr(block, "input_sources", ()):
                upstream_of_stateful.add(id(from_block))

        source_blocks = []
        for block in self.model.active_blocks:
            if id(block) in stateful_block_ids:
                continue
            if isinstance(block, SampledSourceBlock):
                source_blocks.append(block)
                continue
            # If it drives any stateful block, we can't safely run the CUDA plan.
            if id(block) in upstream_of_stateful:
                self.fixed_step_cuda_plan_reason = "non_source_feeds_stateful_block"
                return None
            # Otherwise ignore it for CUDA staging; it will be handled by CPU propagate_signals.

        source_blocks = sorted(source_blocks, key=lambda block: self.model.block_order_index.get(block.block_name, 0))
        if self._is_source_driven_single_batch(stateful_blocks):
            self.fixed_step_cuda_plan_reason = "single_batch_resident_plan"
            return SourceDrivenResidentFixedStepCudaPlan(self.derivative_groups[0], source_blocks)
        if self._supports_multi_batch_execution_levels(stateful_block_ids):
            self.fixed_step_cuda_plan_reason = "multi_batch_execution_levels_plan"
            return MultiBatchExecutionLevelFixedStepCudaPlan(self.model, self.derivative_groups)
        self.fixed_step_cuda_plan_reason = "supports_multi_batch_execution_levels_failed"
        return None

    def _is_source_driven_single_batch(self, stateful_blocks):
        if len(self.derivative_groups) != 1 or len(stateful_blocks) != len(self.derivative_groups[0].blocks):
            return False
        for block in self.derivative_groups[0].blocks:
            for from_block, _, _ in block.input_sources:
                if not isinstance(from_block, SampledSourceBlock):
                    return False
        return True

    def _supports_multi_batch_execution_levels(self, stateful_block_ids):
        # Allow non-stateful blocks beyond SampledSourceBlock (e.g. Derivative/Scope),
        # as long as they do not provide inputs to the CUDA-staged *stateful* blocks.
        for block in self.model.active_blocks:
            if id(block) not in stateful_block_ids:
                continue
            for from_block, _, _ in block.input_sources:
                if isinstance(from_block, SampledSourceBlock):
                    continue
                if id(from_block) not in stateful_block_ids:
                    return False
        return True

    @property
    def enabled(self):
        return bool(self.derivative_groups or self.pre_output_groups or self.level_output_groups)

    def can_run_fixed_step_cuda_solver(self, solver_name):
        solver_key = (solver_name or "").strip().lower()
        return self.fixed_step_cuda_plan is not None and solver_key in {"ode1", "ode4"}

    def initialize_fixed_step_cuda_state(self, full_state):
        if self.fixed_step_cuda_plan is None:
            raise RuntimeError("Fixed-step CUDA plan is unavailable.")
        self.fixed_step_cuda_plan.initialize(full_state, self.backend)

    def sync_fixed_step_cuda_state(self, full_state):
        if self.fixed_step_cuda_plan is None:
            raise RuntimeError("Fixed-step CUDA plan is unavailable.")
        return self.fixed_step_cuda_plan.sync_state(full_state, self.backend)

    def advance_fixed_step_cuda(self, solver_name, t, step, full_state):
        if self.fixed_step_cuda_plan is None:
            raise RuntimeError("Fixed-step CUDA plan is unavailable.")
        result = self.fixed_step_cuda_plan.advance(solver_name, t, step, full_state, self.backend)
        self.backend.invalidate_output_caches()
        return result

    def is_output_batched(self, block):
        if not self.output_batching_enabled:
            return False
        return id(block) in self._output_block_ids

    def is_derivative_batched(self, block):
        return id(block) in self._derivative_block_ids

    def describe(self):
        description = {
            "enabled": self.enabled,
            "backend": self.backend.name,
            "derivativeGroups": len(self.derivative_groups),
            "outputGroups": len(self.pre_output_groups) + sum(len(groups) for groups in self.level_output_groups.values()),
            "batchedBlocks": len(self._derivative_block_ids),
        }
        if self.fixed_step_cuda_plan is not None:
            description["fixedStepCudaSolver"] = True
            description["fixedStepCudaPlanType"] = self.fixed_step_cuda_plan.plan_type
            description["fixedStepCudaNativeExecutionPlan"] = bool(
                getattr(self.fixed_step_cuda_plan, "_native_execution_plan_active", False)
            )
        else:
            # Provide a reason even when fixed-step plan is disabled.
            description["fixedStepCudaSolver"] = False
            description["fixedStepCudaPlanReason"] = self.fixed_step_cuda_plan_reason
        description.update(self.backend.describe_extra())
        return description


def create_acceleration_manager(model, min_batch_size=None):
    backend = select_acceleration_backend()
    if backend is None:
        return None
    if min_batch_size is None:
        min_batch_size = int(os.environ.get("NCSLAB_PYTHON_ACCEL_MIN_BATCH", "4"))
    # First attempt with configured min_batch_size.
    mgr = SimulationAccelerationManager(model, backend, min_batch_size=max(int(min_batch_size), 1))

    # For fixed-step native CUDA plan, plan building requires that all stateful blocks
    # participate in derivative batching; otherwise we fall back to CPU.
    #
    # Some models may fail the default min-batch threshold because blocks have different
    # signatures; in that case retry with a smaller threshold to enable the plan.
    try:
        solver_name = getattr(model, "solver_name", None)
        solver_key = (solver_name or "").strip().lower()
        eligible_fixed = (
            getattr(model, "step_type", None) == "FixedStep"
            and getattr(model, "fixed_step", 0) > 0
            and solver_key in {"ode1", "ode2", "ode3", "ode4", "ode23"}
        )
        if (
            eligible_fixed
            and backend.name == "cuda"
            and not mgr.can_run_fixed_step_cuda_solver(solver_name)
            and max(int(min_batch_size), 1) > 1
        ):
            mgr_retry = SimulationAccelerationManager(model, backend, min_batch_size=1)
            if mgr_retry.can_run_fixed_step_cuda_solver(solver_name):
                return mgr_retry
    except Exception:
        # Never block simulation due to diagnostic retry logic.
        pass

    return mgr


def select_acceleration_backend():
    requested = os.environ.get("NCSLAB_PYTHON_ACCEL_BACKEND", "auto").strip().lower()

    if requested in {"off", "none", "disabled"}:
        return None

    if requested == "numpy" or requested == "cpu":
        return NumpyAccelerationBackend()

    if requested == "cuda":
        return CudaAccelerationBackend(get_cuda_bridge())

    if requested in {"", "auto"}:
        try:
            return CudaAccelerationBackend(get_cuda_bridge())
        except Exception as exc:
            _debug_log(f"[Acceleration] CUDA auto-detection failed, falling back to numpy: {exc}")
            return NumpyAccelerationBackend()

    raise RuntimeError(f"Unsupported acceleration backend '{requested}'.")
