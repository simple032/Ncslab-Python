"""
ctypes bindings for ncslab_python_cuda.dll (linear batches + managed execution plan advance).

Additional C API symbols may exist in the DLL (e.g. scalar batch, plan stage/output helpers);
they are intentionally not bound here until used by the Python simulator.
"""

import atexit
import ctypes
import os
from pathlib import Path

import numpy as np


def _discover_repo_root():
    """
    Resolve where the repo root is, even when the python code is copied into a temp directory
    (e.g. launched via Java WebSocket endpoints).
    """
    # 1) Prefer current working directory if it contains the expected tools folder.
    try:
        cwd = Path.cwd().resolve()
        if (cwd / "tools" / "python_cuda_bridge").exists():
            return cwd
    except Exception:
        pass

    # 2) Fallback: walk up from this file location.
    try:
        here = Path(__file__).resolve()
        for p in here.parents:
            if (p / "tools" / "python_cuda_bridge").exists():
                return p
    except Exception:
        pass

    # 3) Last resort: keep the previous heuristic to avoid crashing.
    return Path(__file__).resolve().parents[5]


_ROOT = _discover_repo_root()
_DEFAULT_DLL_CANDIDATES = [
    _ROOT / "tools" / "python_cuda_bridge" / "build" / "Release" / "ncslab_python_cuda.dll",
    _ROOT / "tools" / "python_cuda_bridge" / "build" / "RelWithDebInfo" / "ncslab_python_cuda.dll",
    _ROOT / "tools" / "python_cuda_bridge" / "build" / "Debug" / "ncslab_python_cuda.dll",
    _ROOT / "tools" / "python_cuda_bridge" / "build" / "ncslab_python_cuda.dll",
    _ROOT / "tools" / "python_cuda_bridge" / "build-nmake" / "ncslab_python_cuda.dll",
]


class CudaBridge:
    def __init__(self, dll_path):
        self.dll_path = Path(dll_path)
        self.lib = ctypes.CDLL(str(self.dll_path))
        self._configure_prototypes()
        if not self.lib.ncslab_cuda_initialize():
            raise RuntimeError(self.last_error() or f"Failed to initialize CUDA bridge from {self.dll_path}")
        atexit.register(self.shutdown)

    def _configure_prototypes(self):
        self.lib.ncslab_cuda_initialize.restype = ctypes.c_int
        self.lib.ncslab_cuda_shutdown.restype = None
        self.lib.ncslab_cuda_is_ready.restype = ctypes.c_int
        self.lib.ncslab_cuda_last_error.restype = ctypes.c_char_p
        self.lib.ncslab_cuda_get_device_name.argtypes = [ctypes.c_char_p, ctypes.c_size_t]
        self.lib.ncslab_cuda_get_device_name.restype = ctypes.c_int

        self.lib.ncslab_cuda_configure_linear_batch.argtypes = [
            ctypes.c_size_t,
            ctypes.c_size_t,
            ctypes.c_size_t,
            ctypes.c_size_t,
            ctypes.c_int,
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
        ]
        self.lib.ncslab_cuda_configure_linear_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_upload_linear_state.argtypes = [
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_upload_linear_state.restype = ctypes.c_int
        self.lib.ncslab_cuda_download_linear_state.argtypes = [
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_download_linear_state.restype = ctypes.c_int
        self.lib.ncslab_cuda_upload_linear_inputs.argtypes = [
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_upload_linear_inputs.restype = ctypes.c_int
        self.lib.ncslab_cuda_evaluate_linear_derivative_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_download_linear_derivative_batch.argtypes = [
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_download_linear_derivative_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_evaluate_linear_output_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_download_linear_output_batch.argtypes = [
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_download_linear_output_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_advance_linear_euler_step.argtypes = [
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
            ctypes.c_double,
        ]
        self.lib.ncslab_cuda_advance_linear_euler_step.restype = ctypes.c_int
        self.lib.ncslab_cuda_advance_linear_rk4_step.argtypes = [
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
            ctypes.c_double,
        ]
        self.lib.ncslab_cuda_advance_linear_rk4_step.restype = ctypes.c_int
        self.lib.ncslab_cuda_release_linear_batch.restype = None

        self.lib.ncslab_cuda_create_managed_linear_batch.argtypes = [
            ctypes.c_size_t,
            ctypes.c_size_t,
            ctypes.c_size_t,
            ctypes.c_size_t,
            ctypes.c_int,
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
        ]
        self.lib.ncslab_cuda_create_managed_linear_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_upload_managed_linear_state.argtypes = [
            ctypes.c_int,
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_upload_managed_linear_state.restype = ctypes.c_int
        self.lib.ncslab_cuda_upload_managed_linear_inputs.argtypes = [
            ctypes.c_int,
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_upload_managed_linear_inputs.restype = ctypes.c_int
        self.lib.ncslab_cuda_evaluate_managed_linear_derivative_batch.argtypes = [ctypes.c_int]
        self.lib.ncslab_cuda_evaluate_managed_linear_derivative_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_download_managed_linear_derivative_batch.argtypes = [
            ctypes.c_int,
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_download_managed_linear_derivative_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_evaluate_managed_linear_output_batch.argtypes = [ctypes.c_int]
        self.lib.ncslab_cuda_evaluate_managed_linear_output_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_download_managed_linear_output_batch.argtypes = [
            ctypes.c_int,
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_size_t,
        ]
        self.lib.ncslab_cuda_download_managed_linear_output_batch.restype = ctypes.c_int
        self.lib.ncslab_cuda_release_managed_linear_batch.argtypes = [ctypes.c_int]
        self.lib.ncslab_cuda_release_managed_linear_batch.restype = ctypes.c_int

        self.lib.ncslab_cuda_evaluate_managed_linear_derivative_batches.argtypes = [
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_int),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
        ]
        self.lib.ncslab_cuda_evaluate_managed_linear_derivative_batches.restype = ctypes.c_int
        self.lib.ncslab_cuda_evaluate_managed_linear_output_batches.argtypes = [
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_int),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
        ]
        self.lib.ncslab_cuda_evaluate_managed_linear_output_batches.restype = ctypes.c_int

        self.lib.ncslab_cuda_create_managed_execution_plan.argtypes = [
            ctypes.c_size_t,
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_int),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_int),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_size_t),
        ]
        self.lib.ncslab_cuda_create_managed_execution_plan.restype = ctypes.c_int
        self.lib.ncslab_cuda_release_managed_execution_plan.argtypes = [ctypes.c_int]
        self.lib.ncslab_cuda_release_managed_execution_plan.restype = ctypes.c_int
        self.lib.ncslab_cuda_advance_managed_execution_plan_euler_step.argtypes = [
            ctypes.c_int,
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_double,
            ctypes.POINTER(ctypes.c_double),
        ]
        self.lib.ncslab_cuda_advance_managed_execution_plan_euler_step.restype = ctypes.c_int
        self.lib.ncslab_cuda_advance_managed_execution_plan_rk4_step.argtypes = [
            ctypes.c_int,
            ctypes.c_size_t,
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_size_t),
            ctypes.POINTER(ctypes.c_double),
            ctypes.POINTER(ctypes.c_double),
            ctypes.c_double,
            ctypes.POINTER(ctypes.c_double),
        ]
        self.lib.ncslab_cuda_advance_managed_execution_plan_rk4_step.restype = ctypes.c_int

    def last_error(self):
        message = self.lib.ncslab_cuda_last_error()
        return message.decode("utf-8") if message else ""

    def _raise_last_error(self, fallback):
        raise RuntimeError(self.last_error() or fallback)

    def device_name(self):
        buffer = ctypes.create_string_buffer(256)
        if not self.lib.ncslab_cuda_get_device_name(buffer, len(buffer)):
            self._raise_last_error("Failed to query CUDA device name.")
        return buffer.value.decode("utf-8")

    @staticmethod
    def _as_f64(values):
        return np.ascontiguousarray(values, dtype=np.float64)

    def configure_linear_batch(
        self,
        count,
        state_width,
        input_width,
        output_width,
        output_uses_inputs,
        a_values,
        b_values,
        c_values,
        d_values,
    ):
        a_values = self._as_f64(a_values)
        b_values = self._as_f64(b_values)
        c_values = self._as_f64(c_values)
        d_values = self._as_f64(d_values)
        if not self.lib.ncslab_cuda_configure_linear_batch(
            int(count),
            int(state_width),
            int(input_width),
            int(output_width),
            1 if output_uses_inputs else 0,
            a_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            b_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            c_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            d_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
        ):
            self._raise_last_error("Failed to configure linear batch.")

    def upload_linear_state(self, states):
        states = self._as_f64(states)
        if not self.lib.ncslab_cuda_upload_linear_state(
            states.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            states.size,
        ):
            self._raise_last_error("Failed to upload linear state buffer.")

    def download_linear_state(self, value_count):
        outputs = np.empty(int(value_count), dtype=np.float64)
        if not self.lib.ncslab_cuda_download_linear_state(
            outputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            outputs.size,
        ):
            self._raise_last_error("Failed to download linear state buffer.")
        return outputs

    def upload_linear_inputs(self, inputs):
        inputs = self._as_f64(inputs)
        if not self.lib.ncslab_cuda_upload_linear_inputs(
            inputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs.size,
        ):
            self._raise_last_error("Failed to upload linear input buffer.")

    def evaluate_linear_derivative_batch(self):
        if not self.lib.ncslab_cuda_evaluate_linear_derivative_batch():
            self._raise_last_error("Failed to evaluate linear derivative batch.")

    def download_linear_derivative_batch(self, value_count):
        outputs = np.empty(int(value_count), dtype=np.float64)
        if not self.lib.ncslab_cuda_download_linear_derivative_batch(
            outputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            outputs.size,
        ):
            self._raise_last_error("Failed to download linear derivative batch.")
        return outputs

    def evaluate_linear_output_batch(self):
        if not self.lib.ncslab_cuda_evaluate_linear_output_batch():
            self._raise_last_error("Failed to evaluate linear output batch.")

    def download_linear_output_batch(self, value_count):
        outputs = np.empty(int(value_count), dtype=np.float64)
        if not self.lib.ncslab_cuda_download_linear_output_batch(
            outputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            outputs.size,
        ):
            self._raise_last_error("Failed to download linear output batch.")
        return outputs

    def advance_linear_euler_step(self, inputs, step):
        inputs = self._as_f64(inputs)
        if not self.lib.ncslab_cuda_advance_linear_euler_step(
            inputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs.size,
            float(step),
        ):
            self._raise_last_error("Failed to advance linear Euler step.")

    def advance_linear_rk4_step(self, inputs1, inputs2, inputs3, inputs4, step):
        inputs1 = self._as_f64(inputs1)
        inputs2 = self._as_f64(inputs2)
        inputs3 = self._as_f64(inputs3)
        inputs4 = self._as_f64(inputs4)
        if not self.lib.ncslab_cuda_advance_linear_rk4_step(
            inputs1.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs2.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs3.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs4.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs1.size,
            float(step),
        ):
            self._raise_last_error("Failed to advance linear RK4 step.")

    def release_linear_batch(self):
        self.lib.ncslab_cuda_release_linear_batch()

    def create_managed_linear_batch(
        self,
        count,
        state_width,
        input_width,
        output_width,
        output_uses_inputs,
        a_values,
        b_values,
        c_values,
        d_values,
    ):
        a_values = self._as_f64(a_values)
        b_values = self._as_f64(b_values)
        c_values = self._as_f64(c_values)
        d_values = self._as_f64(d_values)
        handle = self.lib.ncslab_cuda_create_managed_linear_batch(
            int(count),
            int(state_width),
            int(input_width),
            int(output_width),
            1 if output_uses_inputs else 0,
            a_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            b_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            c_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            d_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
        )
        if handle <= 0:
            self._raise_last_error("Failed to create managed linear batch.")
        return handle

    def upload_managed_linear_state(self, handle, states):
        states = self._as_f64(states)
        if not self.lib.ncslab_cuda_upload_managed_linear_state(
            int(handle),
            states.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            states.size,
        ):
            self._raise_last_error("Failed to upload managed linear state buffer.")

    def upload_managed_linear_inputs(self, handle, inputs):
        inputs = self._as_f64(inputs)
        if not self.lib.ncslab_cuda_upload_managed_linear_inputs(
            int(handle),
            inputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs.size,
        ):
            self._raise_last_error("Failed to upload managed linear input buffer.")

    def evaluate_managed_linear_derivative_batch(self, handle):
        if not self.lib.ncslab_cuda_evaluate_managed_linear_derivative_batch(int(handle)):
            self._raise_last_error("Failed to evaluate managed linear derivative batch.")

    def download_managed_linear_derivative_batch(self, handle, value_count):
        outputs = np.empty(int(value_count), dtype=np.float64)
        if not self.lib.ncslab_cuda_download_managed_linear_derivative_batch(
            int(handle),
            outputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            outputs.size,
        ):
            self._raise_last_error("Failed to download managed linear derivative batch.")
        return outputs

    def evaluate_managed_linear_output_batch(self, handle):
        if not self.lib.ncslab_cuda_evaluate_managed_linear_output_batch(int(handle)):
            self._raise_last_error("Failed to evaluate managed linear output batch.")

    def download_managed_linear_output_batch(self, handle, value_count):
        outputs = np.empty(int(value_count), dtype=np.float64)
        if not self.lib.ncslab_cuda_download_managed_linear_output_batch(
            int(handle),
            outputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            outputs.size,
        ):
            self._raise_last_error("Failed to download managed linear output batch.")
        return outputs

    def release_managed_linear_batch(self, handle):
        if not self.lib.ncslab_cuda_release_managed_linear_batch(int(handle)):
            self._raise_last_error("Failed to release managed linear batch.")

    def evaluate_managed_linear_derivative_batches(
        self,
        handles,
        state_value_counts,
        input_value_counts,
        state_offsets,
        input_offsets,
        states,
        inputs,
    ):
        handles = np.ascontiguousarray(handles, dtype=np.int32)
        state_value_counts = np.ascontiguousarray(state_value_counts, dtype=np.uintp)
        input_value_counts = np.ascontiguousarray(input_value_counts, dtype=np.uintp)
        state_offsets = np.ascontiguousarray(state_offsets, dtype=np.uintp)
        input_offsets = np.ascontiguousarray(input_offsets, dtype=np.uintp)
        states = self._as_f64(states)
        inputs = self._as_f64(inputs)
        derivatives = np.empty_like(states)
        if not self.lib.ncslab_cuda_evaluate_managed_linear_derivative_batches(
            handles.size,
            handles.ctypes.data_as(ctypes.POINTER(ctypes.c_int)),
            state_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            input_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            state_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            input_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            states.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            derivatives.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
        ):
            self._raise_last_error("Failed to evaluate managed linear derivative batches.")
        return derivatives

    def evaluate_managed_linear_output_batches(
        self,
        handles,
        state_value_counts,
        input_value_counts,
        output_value_counts,
        state_offsets,
        input_offsets,
        output_offsets,
        states,
        inputs,
    ):
        handles = np.ascontiguousarray(handles, dtype=np.int32)
        state_value_counts = np.ascontiguousarray(state_value_counts, dtype=np.uintp)
        input_value_counts = np.ascontiguousarray(input_value_counts, dtype=np.uintp)
        output_value_counts = np.ascontiguousarray(output_value_counts, dtype=np.uintp)
        state_offsets = np.ascontiguousarray(state_offsets, dtype=np.uintp)
        input_offsets = np.ascontiguousarray(input_offsets, dtype=np.uintp)
        output_offsets = np.ascontiguousarray(output_offsets, dtype=np.uintp)
        states = self._as_f64(states)
        inputs = self._as_f64(inputs)
        outputs = np.empty(int(output_value_counts.sum()) if output_value_counts.size else 0, dtype=np.float64)
        if not self.lib.ncslab_cuda_evaluate_managed_linear_output_batches(
            handles.size,
            handles.ctypes.data_as(ctypes.POINTER(ctypes.c_int)),
            state_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            input_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            output_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            state_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            input_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            output_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            states.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            inputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            outputs.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
        ):
            self._raise_last_error("Failed to evaluate managed linear output batches.")
        return outputs

    def create_managed_execution_plan(
        self,
        total_state_count,
        group_handles,
        group_input_value_counts,
        group_output_value_counts,
        group_state_index_counts,
        group_state_index_offsets,
        state_indices,
        route_source_kinds,
        route_source_indices,
        route_source_offsets,
        route_source_widths,
        route_target_offsets,
        route_target_widths,
        group_route_counts,
        group_route_offsets,
        pre_output_group_indices,
        level_group_counts,
        level_group_offsets,
        level_group_indices,
        derivative_group_indices,
    ):
        group_handles = np.ascontiguousarray(group_handles, dtype=np.int32)
        group_input_value_counts = np.ascontiguousarray(group_input_value_counts, dtype=np.uintp)
        group_output_value_counts = np.ascontiguousarray(group_output_value_counts, dtype=np.uintp)
        group_state_index_counts = np.ascontiguousarray(group_state_index_counts, dtype=np.uintp)
        group_state_index_offsets = np.ascontiguousarray(group_state_index_offsets, dtype=np.uintp)
        state_indices = np.ascontiguousarray(state_indices, dtype=np.uintp)
        route_source_kinds = np.ascontiguousarray(route_source_kinds, dtype=np.int32)
        route_source_indices = np.ascontiguousarray(route_source_indices, dtype=np.uintp)
        route_source_offsets = np.ascontiguousarray(route_source_offsets, dtype=np.uintp)
        route_source_widths = np.ascontiguousarray(route_source_widths, dtype=np.uintp)
        route_target_offsets = np.ascontiguousarray(route_target_offsets, dtype=np.uintp)
        route_target_widths = np.ascontiguousarray(route_target_widths, dtype=np.uintp)
        group_route_counts = np.ascontiguousarray(group_route_counts, dtype=np.uintp)
        group_route_offsets = np.ascontiguousarray(group_route_offsets, dtype=np.uintp)
        pre_output_group_indices = np.ascontiguousarray(pre_output_group_indices, dtype=np.uintp)
        level_group_counts = np.ascontiguousarray(level_group_counts, dtype=np.uintp)
        level_group_offsets = np.ascontiguousarray(level_group_offsets, dtype=np.uintp)
        level_group_indices = np.ascontiguousarray(level_group_indices, dtype=np.uintp)
        derivative_group_indices = np.ascontiguousarray(derivative_group_indices, dtype=np.uintp)
        handle = self.lib.ncslab_cuda_create_managed_execution_plan(
            int(total_state_count),
            group_handles.size,
            group_handles.ctypes.data_as(ctypes.POINTER(ctypes.c_int)),
            group_input_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            group_output_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            group_state_index_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            group_state_index_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            state_indices.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            route_source_kinds.size,
            route_source_kinds.ctypes.data_as(ctypes.POINTER(ctypes.c_int)),
            route_source_indices.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            route_source_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            route_source_widths.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            route_target_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            route_target_widths.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            group_route_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            group_route_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            pre_output_group_indices.size,
            pre_output_group_indices.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            level_group_counts.size,
            level_group_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            level_group_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            level_group_indices.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            derivative_group_indices.size,
            derivative_group_indices.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
        )
        if handle <= 0:
            self._raise_last_error("Failed to create managed execution plan.")
        return handle

    def release_managed_execution_plan(self, handle):
        if not self.lib.ncslab_cuda_release_managed_execution_plan(int(handle)):
            self._raise_last_error("Failed to release managed execution plan.")

    def advance_managed_execution_plan_euler_step(self, handle, source_value_counts, source_offsets, source_values, states, step):
        source_value_counts = np.ascontiguousarray(source_value_counts, dtype=np.uintp)
        source_offsets = np.ascontiguousarray(source_offsets, dtype=np.uintp)
        source_values = self._as_f64(source_values)
        states = self._as_f64(states)
        next_states = np.empty_like(states)
        if not self.lib.ncslab_cuda_advance_managed_execution_plan_euler_step(
            int(handle),
            source_value_counts.size,
            source_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            source_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            source_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            states.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            float(step),
            next_states.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
        ):
            self._raise_last_error("Failed to advance managed execution plan Euler step.")
        return next_states

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
        stage1_source_value_counts = np.ascontiguousarray(stage1_source_value_counts, dtype=np.uintp)
        stage1_source_offsets = np.ascontiguousarray(stage1_source_offsets, dtype=np.uintp)
        stage1_source_values = self._as_f64(stage1_source_values)
        stage2_source_value_counts = np.ascontiguousarray(stage2_source_value_counts, dtype=np.uintp)
        stage2_source_offsets = np.ascontiguousarray(stage2_source_offsets, dtype=np.uintp)
        stage2_source_values = self._as_f64(stage2_source_values)
        stage3_source_value_counts = np.ascontiguousarray(stage3_source_value_counts, dtype=np.uintp)
        stage3_source_offsets = np.ascontiguousarray(stage3_source_offsets, dtype=np.uintp)
        stage3_source_values = self._as_f64(stage3_source_values)
        stage4_source_value_counts = np.ascontiguousarray(stage4_source_value_counts, dtype=np.uintp)
        stage4_source_offsets = np.ascontiguousarray(stage4_source_offsets, dtype=np.uintp)
        stage4_source_values = self._as_f64(stage4_source_values)
        states = self._as_f64(states)
        next_states = np.empty_like(states)
        if not self.lib.ncslab_cuda_advance_managed_execution_plan_rk4_step(
            int(handle),
            stage1_source_value_counts.size,
            stage1_source_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            stage1_source_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            stage1_source_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            stage2_source_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            stage2_source_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            stage2_source_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            stage3_source_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            stage3_source_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            stage3_source_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            stage4_source_value_counts.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            stage4_source_offsets.ctypes.data_as(ctypes.POINTER(ctypes.c_size_t)),
            stage4_source_values.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            states.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
            float(step),
            next_states.ctypes.data_as(ctypes.POINTER(ctypes.c_double)),
        ):
            self._raise_last_error("Failed to advance managed execution plan RK4 step.")
        return next_states

    def shutdown(self):
        if getattr(self, "lib", None) is not None:
            self.lib.ncslab_cuda_shutdown()
            self.lib = None


def _candidate_dll_paths():
    explicit = os.environ.get("NCSLAB_PYTHON_CUDA_DLL", "").strip()
    if explicit:
        yield Path(explicit)
    for path in _DEFAULT_DLL_CANDIDATES:
        yield path


_BRIDGE = None


def get_cuda_bridge():
    global _BRIDGE
    if _BRIDGE is not None:
        return _BRIDGE

    for candidate in _candidate_dll_paths():
        if not candidate.exists():
            continue
        try:
            _BRIDGE = CudaBridge(candidate)
            return _BRIDGE
        except OSError:
            continue

    searched = ", ".join(str(path) for path in _candidate_dll_paths())
    raise RuntimeError(f"Unable to load ncslab CUDA bridge DLL. Searched: {searched}")
