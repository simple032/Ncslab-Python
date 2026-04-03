import json
import os


def emit(message):
    """Write one JSON line to stdout."""
    print(json.dumps(message, ensure_ascii=False), flush=True)


def emit_status(message, **kwargs):
    payload = {"type": "status", "msg": message}
    payload.update(kwargs)
    emit(payload)


def emit_progress(time_value, time_length, progress):
    if os.environ.get("NCSLAB_PROFILE_BASELINE", "").strip().lower() in ("1", "true", "yes", "on"):
        return
    emit({
        "type": "progress",
        "time": time_value,
        "timeLength": time_length,
        "progress": progress,
    })


def emit_error(error_message):
    emit({"type": "error", "error": error_message})


def emit_result(results_file):
    emit({"type": "result", "resultsFile": results_file})

