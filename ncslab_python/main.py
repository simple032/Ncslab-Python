import json
import os
import sys
import traceback

from .acceleration_strategy import describe_cuda_rk4_path, resolve_cuda_run_flags
from .cuda_config import try_cupy, use_cuda_requested
from .jsonl import emit_error, emit_result, emit_status
from .model import SimulationModel
from .runner import describe_solver, map_solver, run_simulation


def main():
    try:
        if use_cuda_requested() and try_cupy() is None:
            # When CuPy isn't installed, native DLL-based acceleration may still work.
            native_available = False
            try:
                from .cuda_bridge import get_cuda_bridge

                get_cuda_bridge()
                native_available = True
            except Exception:
                native_available = False

            if native_available:
                print(
                    "NCSLAB: NCSLAB_USE_CUDA is enabled but CuPy is not available; "
                    "native CUDA DLL acceleration may still run when eligible. "
                    "CuPy-only optimizations are disabled.",
                    file=sys.stderr,
                )
            else:
                print(
                    "NCSLAB: NCSLAB_USE_CUDA is enabled but CuPy/GPU is not available; "
                    "optional CUDA features use CPU.",
                    file=sys.stderr,
                )

        input_str = sys.stdin.read()
        if not input_str.strip():
            emit_error("No input data received")
            sys.exit(1)

        input_data = json.loads(input_str)
        emit_status("generating")

        requested_cuda_acceleration = bool(input_data.get("useCudaAcceleration", False))

        model_json = input_data.get("model", input_data)
        output_dir = input_data.get("outputDir", "./output/")
        user_id = input_data.get("userId", 0)
        model_id = input_data.get("modelId", 0)
        web_result_path = input_data.get(
            "webResultPath",
            f"/PythonCode/{user_id}/{model_id}/results.json",
        )

        config = model_json.get("config", {})
        blocks_data = model_json.get("blocks", [])
        lines_data = model_json.get("lines", [])

        if not blocks_data:
            emit_error("No blocks found in model data")
            sys.exit(1)

        model = SimulationModel(config, blocks_data, lines_data)
        emit_status("generated")
        emit_status("simulating", time=0.0, timeLength=model.stop_time, progress=0)

        run_cuda_acceleration, cuda_run_reason, cuda_probe_meta = resolve_cuda_run_flags(
            model, requested_cuda_acceleration
        )
        did_cuda_fallback = requested_cuda_acceleration and not run_cuda_acceleration

        result = run_simulation(model, use_cuda_acceleration=run_cuda_acceleration)
        if result is None:
            sys.exit(1)

        emit_status("simulated")
        scopes = model.get_scopes()
        if run_cuda_acceleration == requested_cuda_acceleration:
            cuda_meta = dict(cuda_probe_meta)
        else:
            cuda_meta = describe_cuda_rk4_path(model, run_cuda_acceleration)
        cuda_meta["cudaFallbackToCpu"] = did_cuda_fallback
        cuda_meta["cudaAccelerationRequestedFrontend"] = requested_cuda_acceleration
        cuda_meta["cudaProbeRequested"] = cuda_probe_meta
        cuda_meta["cudaRunReason"] = cuda_run_reason
        result_data = {
            "scopes": scopes,
            "metadata": {
                "solver": describe_solver(model.solver_name, model.step_type),
                "scipyMethod": map_solver(model.solver_name),
                "solverInput": model.solver_name,
                "stepType": model.step_type,
                "startTime": model.start_time,
                "stopTime": model.stop_time,
                "totalSteps": len(scopes[0]["time"]) if scopes else 0,
                "cudaAcceleration": cuda_meta,
            },
        }

        os.makedirs(output_dir, exist_ok=True)
        result_file = os.path.join(output_dir, "results.json")
        with open(result_file, "w", encoding="utf-8") as handle:
            json.dump(result_data, handle, ensure_ascii=False)

        emit_result(web_result_path)
    except json.JSONDecodeError as exc:
        emit_error(f"Invalid JSON input: {exc}")
        sys.exit(1)
    except Exception as exc:
        traceback.print_exc(file=sys.stderr)
        emit_error(str(exc))
        sys.exit(1)
