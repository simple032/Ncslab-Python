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

---

## 中文说明

面向 NCSLab 风格框图的 Python 仿真引擎：从 **标准输入读取一整段 JSON**，运行模型后，在指定输出目录写入 **`results.json`**；进度与结果以 **JSONL** 行输出到 **stdout**，便于被 Java 等宿主进程通过 `ProcessBuilder` 解析。

本仓库代码来自 `ncslab_link` 工程中的 `src/main/resources/python/`。

### 环境与依赖

- 建议使用 **Python** 3.10+
- 必需 **NumPy**、**SciPy**（见 `requirements.txt`）
- 可选 **CuPy**（部分 GPU 路径），见 `requirements-optional-cuda.txt`

安装示例：

```bash
python -m venv .venv
.venv\Scripts\activate          # Windows
# source .venv/bin/activate     # Linux / macOS
pip install -r requirements.txt
```

### 运行方式

在仓库根目录执行（保证 `simulate.py` 与 `ncslab_python` 包同级，可被导入）：

```bash
python -u simulate.py < examples/minimal_input.json
```

Windows PowerShell：

```powershell
Get-Content examples/minimal_input.json -Raw | python -u simulate.py
```

输出说明：

- **stdout**：JSONL（`status`、`result`、`error` 等，见 `ncslab_python/jsonl.py`）
- **`outputDir/results.json`**：未指定 `outputDir` 时默认为 `./output/`，内含示波器数据与元数据

### 标准输入 JSON 约定

进程一次性读取 **stdin** 中的单个 JSON 对象。外层包装字段可选；若无 **`model`** 键，则 **整段 JSON 即视为模型**。

| 字段 | 含义 |
|------|------|
| `model` | 含 `config`、`blocks`、`lines` 的对象（与前端导出结构一致） |
| `outputDir` | `results.json` 所在目录（默认 `./output/`） |
| `userId`、`modelId` | 默认 `0`，用于默认的 `webResultPath` |
| `webResultPath` | 最后一条 JSONL `result` 中回传的路径（默认 `/PythonCode/{userId}/{modelId}/results.json`） |
| `useCudaAcceleration` | 是否请求 CUDA 加速（不支持时可能回退 CPU） |

**模型对象**须包含：

- `config`：如 `StartTime`、`StopTime`、`Solver`、`Step`、`FixedStep`、`MaxDataPoints` 等
- `blocks`：块列表（`blockType`、`blockName`、`paramValues`、`blockUUID` 等）
- `lines`：连线（`fromBlockName`、`toBlockName`、`fromPortNo`、`toPortNo` 等）

实现细节见 `ncslab_python/main.py`。

### 环境变量（可选 CUDA）

| 变量 | 作用 |
|------|------|
| `NCSLAB_USE_CUDA` | 设为 `1` / `true` / `yes` / `on` 时请求 CUDA（需安装 CuPy 等） |
| `NCSLAB_CUDA_DEVICE` | GPU 编号（默认 `0`） |
| `NCSLAB_PYTHON_DEBUG` | `1` / `true` 时在 stderr 输出更多调试信息 |

详见 `ncslab_python/cuda_config.py` 与 `requirements-optional-cuda.txt`。

### 与 Java 宿主集成

宿主进程通常执行 `python -u /path/to/simulate.py`，并将与线上一致的 JSON 写入子进程 **stdin**（参见 `ncslab_link` 中的 `PythonSimulateWebSocket`）。在 `config.properties` 中将 `python.simulate.script.path` 指向该脚本即可。

### 与 `ncslab_link` 主仓库同步

任选一种方式：

1. **手动复制**：在 `ncslab_link` 中改完 Python 后，将 `src/main/resources/python/` 拷入本仓库再提交。
2. **Git 子模块**：在 Java 仓库中用子模块替换 `src/main/resources/python`，指向本仓库（单一源码，但需熟悉子模块操作）。
3. **同步脚本**：用脚本从 `ncslab_link` 同步到本仓库克隆目录再提交。

### 许可证

若公开发布，请在仓库根目录添加 `LICENSE`，并符合你方组织策略。

### 联系

维护者邮箱：**1404476938@qq.com**

Remote: [https://github.com/simple032/ncslab-python](https://github.com/simple032/ncslab-python)
