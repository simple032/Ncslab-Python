# ncslab-python

Python simulation engine for NCSLab-style block diagrams. It reads a **single JSON object from stdin**, runs the model, and writes **`results.json`** under the configured output directory. JSONL status lines are printed to **stdout** for integration with a host process (e.g. Java `ProcessBuilder`).

This repository is extracted from the `ncslab_link` project (`src/main/resources/python/`).

## Requirements

- **Python** 3.10+ recommended
- **NumPy**, **SciPy** (see `requirements.txt`)
- Optional: **CuPy** for some GPU paths; see `requirements-optional-cuda.txt`

Install:

```bash
python -m venv .venv
.venv\Scripts\activate          # Windows
# source .venv/bin/activate     # Linux / macOS
pip install -r requirements.txt
```

## Running

From the repository root (so `ncslab_python` is importable next to `simulate.py`):

```bash
python -u simulate.py < examples/minimal_input.json
```

On Windows PowerShell you can use:

```powershell
Get-Content examples/minimal_input.json -Raw | python -u simulate.py
```

Outputs:

- JSONL lines on **stdout** (`status`, `result`, `error`, etc.; see `ncslab_python/jsonl.py`)
- **`outputDir/results.json`** (default `./output/` if omitted) with scopes and metadata

## Stdin JSON contract

The process reads **one JSON object** from stdin. Wrapper fields are optional; if there is no `"model"` key, the **entire object** is treated as the model.

| Field | Meaning |
|--------|---------|
| `model` | Object with `config`, `blocks`, `lines` (same shape as frontend export) |
| `outputDir` | Directory for `results.json` (default `./output/`) |
| `userId`, `modelId` | Defaults `0`; used for default `webResultPath` |
| `webResultPath` | Emitted in the final JSONL `result` line (default `/PythonCode/{userId}/{modelId}/results.json`) |
| `useCudaAcceleration` | Request CUDA acceleration when supported (may fall back to CPU) |

The **model** object must include:

- `config`: e.g. `StartTime`, `StopTime`, `Solver`, `Step`, `FixedStep`, `MaxDataPoints`, …
- `blocks`: list of block definitions (`blockType`, `blockName`, `paramValues`, `blockUUID`, …)
- `lines`: list of connections (`fromBlockName`, `toBlockName`, `fromPortNo`, `toPortNo`, …)

Implementation reference: `ncslab_python/main.py`.

## Environment variables (optional CUDA)

| Variable | Purpose |
|----------|---------|
| `NCSLAB_USE_CUDA` | `1` / `true` / `yes` / `on` to request CUDA (CuPy when installed) |
| `NCSLAB_CUDA_DEVICE` | GPU index (default `0`) |
| `NCSLAB_PYTHON_DEBUG` | `1` / `true` for extra stderr debug from the model |

See `ncslab_python/cuda_config.py` and `requirements-optional-cuda.txt`.

## Integration with the Java host

The host typically runs:

`python -u /path/to/simulate.py`

and writes the same JSON to the process **stdin** (as in `PythonSimulateWebSocket` in `ncslab_link`). Point `python.simulate.script.path` in `config.properties` at this script.

## Keeping in sync with `ncslab_link`

Choose one workflow:

1. **Manual copy** — After changing Python code in `ncslab_link`, copy `src/main/resources/python/` into this repo and commit.
2. **Git submodule** — Replace `src/main/resources/python` in the Java repo with a submodule pointing at this repository (single source of truth; requires submodule discipline).
3. **Sync script** — A small script copies from `ncslab_link` into a clone of `ncslab-python` before you commit.

## License

Add a `LICENSE` file in this repository if you publish publicly; match your organization’s policy.
