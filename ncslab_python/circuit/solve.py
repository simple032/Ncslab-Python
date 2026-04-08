import numpy as np


def solve_linear_system(matrix, rhs):
    """Solve dense linear system with least-squares fallback."""
    if matrix.size == 0:
        return np.zeros((0,), dtype=float)
    try:
        return np.linalg.solve(matrix, rhs)
    except np.linalg.LinAlgError:
        solution, _, _, _ = np.linalg.lstsq(matrix, rhs, rcond=None)
        return solution
