"""CUDA / CuPy enablement from environment (also set by Java from config.properties)."""

import os


def use_cuda_requested():
    """True only when NCSLAB_USE_CUDA is explicitly enabled (default: off)."""
    value = os.environ.get("NCSLAB_USE_CUDA", "").strip().lower()
    if value in ("1", "true", "yes", "on"):
        return True
    if value in ("0", "false", "no", "off", ""):
        return False
    return False


def cuda_device_index():
    try:
        return int(os.environ.get("NCSLAB_CUDA_DEVICE", "0").strip())
    except (TypeError, ValueError):
        return 0


def try_cupy():
    """
    Return cupy module if CUDA is requested and import succeeds; otherwise None.
    Callers should use NumPy on CPU when this returns None.
    """
    if not use_cuda_requested():
        return None
    try:
        import cupy as cp

        dev = cuda_device_index()
        if dev >= 0:
            cp.cuda.Device(dev).use()
        return cp
    except Exception:
        return None
