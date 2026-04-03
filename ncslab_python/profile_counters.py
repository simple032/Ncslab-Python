"""Optional simulation counters for baseline profiling (NCSLAB_PROFILE_COUNTERS=1)."""

import os

COUNTERS = {
    "propagate_signals_calls": 0,
    "compute_derivatives_calls": 0,
    "algebraic_loop_iterations": 0,
}


def _is_enabled():
    return os.environ.get("NCSLAB_PROFILE_COUNTERS", "").strip().lower() in (
        "1",
        "true",
        "yes",
        "on",
    )


def enabled():
    return _is_enabled()


def reset():
    if not _is_enabled():
        return
    for key in COUNTERS:
        COUNTERS[key] = 0


def bump(key, delta=1):
    if not _is_enabled() or key not in COUNTERS:
        return
    COUNTERS[key] += delta


def report():
    if not _is_enabled():
        return ""
    parts = [f"{key}={COUNTERS[key]}" for key in COUNTERS]
    return "NCSLAB_PROFILE_COUNTERS " + ", ".join(parts)
