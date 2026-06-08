# Phase 0 — Hotspot table (template + baseline)

## 中文：如何测算、表格里填什么

这份表里的数字以**本团队代表性模型与 `nsys` 短采**为准；标 **「待补」** 的项仍需管理员 CPU 采样或 Attach 长仿真。**我这边无法替你在本机完成全部测算**；`nsys` 需本机安装（见 **§5**）。

### A. Java 侧（`SimulationStepProfiler`）

1. **开启方式（二选一）**  
   - JVM 启动参数：`-Dncslab.sim.profile=true`  
   - 或环境变量：`NCSLAB_SIM_PROFILE=1`

2. **生效路径（务必看清）**  
   统计代码在 `SimulationModel.performFixedStepIntegration` 里，只对**固定步长**积分循环打点。若前端选的是变步长 / `ode45` 等走别的分支，**不会出现**下面的 `[SimulationStepProfiler]` 汇总，或会提示 `no instrumented steps`。要测 Java 占比，请用**固定步长求解器**跑同一模型。

3. **跑什么**  
   用你们认为「慢」、且与线上一致配置的模型跑一段足够长的仿真（例如至少几千步），保证日志里出现两行类似：  
   `[SimulationStepProfiler] fixed-step steps=... total=... ms (integrate=...% outputs=...% circuit=...% ...)`  
   以及 `per-step mean us: ...`。

4. **填表 §1**  
   把上一行里的 **integrate / outputs / circuit / discrete / scopeFlush** 五个百分比直接抄进下面英文表的第一列对应行（可把 `outputs` 记到 `outputs / I/O`，`circuit` 记到 `circuitModel`）。若某类为 0 或极小，在 Notes 里写「未触发」或「步数不足」。

### B. 原生 `ncslab` 侧（Nsight Systems 推荐）

1. **安装**  
   安装与显卡驱动匹配的 **CUDA Toolkit**，其中自带 **NVIDIA Nsight Systems**。安装后把 `nsys` 所在目录加入 `PATH`（Windows 一般在 `...\NVIDIA Corporation\Nsight Systems\...`）。在终端执行 `nsys --version` 确认可用。**本机尚未安装时**：按文末 **§5** 从零安装与第一次采集。

2. **采集命令（在生成 `ncslab.exe` 的模型目录或包含可执行文件的目录）**  
   - Windows 示例（按实际路径改；**勿使用已废弃的 `osrt`**，以本机 `nsys profile --help` 为准）：  
     `nsys profile -o ncslab_report --force-overwrite true --trace=cuda,nvtx,cublas --stats=true ncslab.exe`  
   - 若 `ncslab` 需命令行参数或 stdin，按你们平时启动仿真的方式接在命令末尾；必要时用 `--duration` 或先让程序正常退出以结束采集。

3. **在 Nsight Systems GUI 里看什么**  
   用 Nsight Systems 打开生成的 `.nsys-rep`：  
   - **CPU 时间线 / 采样栈**：找 `gsl_linalg_SV_decomp`、`gsl_blas_dgemm`、生成代码里的 `NCSLabDerivative` 等是否占大头。  
   - **CUDA / Runtime API**：若有 `NCSLAB_SIM_CUDA=1`，可看 memcpy / kernel 是否与步进对齐。  
   把「自解释的热点」在表里填：**函数名、约占总 CPU 的百分比、典型矩阵阶 `n`（可从调试或日志推断）、每外层步调用次数、是否适合 batch**。

4. **没有 Nsight 时的替代**  
   - Windows：**Very Sleepy**、Visual Studio **CPU 采样**、**Windows Performance Recorder**。  
   - Linux：`perf record -g -- ./ncslab` 后用 `perf report`。  
   这些能填「CPU 占比与函数名」，但 CUDA 时间线不如 Nsight 直观。

### C. 填完表之后做什么

- 若 **Java `circuitModel%` 远高于 integrate**，优先评估 JVM/电路侧或 JNI（计划分支 B），而不是继续堆 `util` 的 GPU。  
- 若 **`gsl_*` / `util` 占比高** 且 `n` 大、调用频繁，与当前仓库里已做的 **尾段 cuBLAS `dgemm`** 方向一致，可在 §3 把「P0 锁定」改成你们实测的函数名并更新 [HOTSPOT_EXTENSION.md](HOTSPOT_EXTENSION.md)。

---

Fill measured rows after profiling. **§1 / §2 下表已据 2026-05-15 团队实测与 `nsys` 短采填写**；标「待补」项仍需管理员 CPU 采样或 Attach 长仿真。

## 1. Java (`SimulationStepProfiler` 与 TCP 桥)

```text
-Dncslab.sim.profile=true
```

**说明**：WebSocket + `CodeModelCWindowsSimulation` + 子进程 `ncslab` 路径下，**不会出现** `[SimulationStepProfiler]` 的 integrate/circuit 行；Java 侧可见 **`[NativeCodeBridgeProfiler]`**（与上项共用开关）。下表将「不适用」与桥接占比一并记录。

| Region / label | % of step (approx.) | Notes |
|----------------|---------------------|-------|
| integrate | — | **不适用**：积分在 `ncslab.exe`，未走 `SimulationModel.performFixedStepIntegration` |
| circuitModel | — | **不适用**（同上） |
| outputs / I/O | — | **不适用**（同上） |
| other（`NativeCodeBridgeProfiler`：对 Java 桥接 wall 的拆分） | `preambleRead` **99.8%**，`handle` **0.2%** | 模型 s252224；41 messages；桥接 wall ~5395 ms；见 **§4.1** |

## 2. Native `ncslab` (Nsight Systems)

**Windows (example)**

```text
nsys profile -o ncslab_report --force-overwrite true --trace=cuda,nvtx,cublas --stats=true ncslab.exe
```

**Linux (example)**

```text
nsys profile -o ncslab_report --force-overwrite true --trace=cuda,nvtx,cublas --stats=true ./ncslab
```

Then open `ncslab_report.nsys-rep` in Nsight Systems. Add CPU sampling / stacks as needed.

| Function / library | % CPU time (approx.) | % GPU / CUDA API (if any) | Typical matrix dim `n` | Calls / outer step | Batchable? |
|--------------------|----------------------|---------------------------|------------------------|--------------------|--------------|
| `gsl_linalg_SV_decomp` | **待补**（管理员 `nsys` CPU 采样 / Very Sleepy / **Attach** 长仿真步进段） | 短采直连 `ncslab.exe`、无 CPU 栈时为 0 | **2、32**（代码生成 `Msize=2` / `Msize=32`） | 开关/拓扑变化触发求逆 | 否（首阶段） |
| `gsl_blas_dgemm`（伪逆尾段，CPU 回退） | **待补** | 短采中为 0 | 同上 | 与 SVD 同路径 | 待定 |
| `NCSLabDerivative` / generated RHS | **待补** | — | — | — | — |
| **CUDA API 合计（`cuda_api_sum`，典型：进程启动 + 首次 cuBLAS，非稳态步进）** | **非「全进程 CPU%」** | 代表性强的一次测量：**`cuLibraryLoadData` ~45%**，**`cudaFree` ~35%**，**`cuKernelGetFunction` ~7%**，**`cudaMemcpy` ~5%**，余为 `cudaMalloc` / `cuLaunchKernel` 等（详见 §4.2） | — | 首次库装载 + 单次尾乘量级 | — |
| **NVTX `nvtx_sum`（同次短采）** | — | **`cublasCreate_v2` ~50%**、**`cublasLtDDDMatmulAlgoGetHeuristic` ~48%**、**`cublasLtDDDMatmul` ~2%** | — | 首次建柄 + Lt 选算法 | 见阶段 C：启动 **cuBLAS warm-up** |
| **GPU kernel（`cuda_gpu_kern_sum`）** | — | **1 次** CUTLASS `...gemm_32x32...`，GPU 算子时间 **~15 µs** 量级（相对 API ms 级固定开销极小） | **32**（与默认 `Msize=32` 尾乘一致） | 该次轨迹内 1 次 | 同 `n` 可 batch（未实现） |
| `cublasDgemm_v2`（尾段 GPU，稳态） | — | **Attach 步进中段**后应见**多次**调用；直连空等 TCP 的短采常**不出现** | **≥32**（默认 `NCSLAB_CUDA_DGEMM_MIN_N`） | 每次伪逆尾乘 1 次 | 同 `n` 可 batch（未实现） |
| Other | **待补** | 稳态需 **CPU sampling** | — | — | — |

## 3. Phase 0 exit — P0 lock（已更新）

**P0（已锁定，与代码一致）**：**`util.cpp`** 中 `ncs_invert_square_svd_pinv` 在 CPU GSL SVD 与对角缩放之后的尾段稠密乘 **`C = W * U^T`**，在 **`NCSLAB_SIM_CUDA=1`** 且 **`n ≥ NCSLAB_CUDA_DGEMM_MIN_N`** 时走 **动态加载的 cuBLAS `cublasDgemm_v2`**，否则 **GSL `gsl_blas_dgemm`**。不替换整条 GSL ODE 积分器。

**阈值（2026-05-15 调整）**：仓库内 **`NCSLAB_CUDA_DGEMM_MIN_N` 默认值由 96 改为 `32`**，使典型 **`Msize=32`** 分块在 CUDA 仿真开关打开时能使用 GPU 尾段；**`Msize=2`** 仍在 CPU。可用环境变量覆盖（大模型若需更晚再上 GPU 可设为 64/96）。

**Java 侧结论（来自 §1）**：桥接线程时间主要在 **等 `ncslab` TCP**（`preambleRead`），下一步性能应优先 **Nsight/CPU 贴 `ncslab.exe`** 与（可选）**cuSOLVER 全 SVD** 等后续项，而非先扩 Java 桥。

If profiling later shows a different dominant cost (pure Java `CircuitModel`, Monte Carlo outer loop, etc.), revise this section and [HOTSPOT_EXTENSION.md](HOTSPOT_EXTENSION.md).

## 4. 实测记录（团队填写区，可覆盖更新）

以下条目来自**真实一次跑机**（WebSocket + `CodeModelCWindowsSimulation` + 子进程 `ncslab.exe`），用于把「Java 控制台能看到什么」和「CUDA 探针是否成功」写进表里；**§2 的 GSL 占比仍须用 Nsight / CPU 采样在 `ncslab.exe` 上补**。

### 4.1 Java：TCP 桥接（`NativeCodeBridgeProfiler`）

与 `SimulationStepProfiler` **共用**开关：`-Dncslab.sim.profile=true` 或 `NCSLAB_SIM_PROFILE=1`。本路径**不会出现** `[SimulationStepProfiler]` 的 integrate/circuit 百分比（积分在 C 子进程），请把下面数据记入**上表 §1 的 Notes** 或单独保留本节。

| 指标 | 数值（示例：模型 s252224，约 2026-05-15） | 说明 |
|------|------------------------------------------|------|
| `messages` | 41 | TCP 上收到的消息条数 |
| Java 桥接 wall（`preambleRead+handle`） | ~5395 ms | 仅 Java 侧等包+处理时间 |
| `preambleRead` 占比 | **~99.8%** | 阻塞在从 `ncslab` 读数据（等对端算完/写回）；**不等于** `gsl` 占 CPU 99% |
| `handle` 占比 | **~0.2%** | 单条消息解析与转发等 |
| 典型日志 | `[NativeCodeBridgeProfiler] CodeModelCWindowsSimulation messages=41 wall=5395.208 ms (preambleRead=99.8% handle=0.2%)` | 与 `per-message mean us` 两行一起出现 |

**对上表 §1 的填法建议**：`integrate / circuitModel / outputs` 若本仿真**未走** `SimulationStepProfiler`，可在 Notes 写：**「不适用：WebSocket+C 子进程路径；见 §4.1」**，避免误以为剖析未开启。

### 4.2 子进程 stderr：CUDA 运行时探针

在 `NCSLAB_SIM_CUDA=1` 且本机可加载 `cudart` 时，`ncslab`  stderr 可出现（数值因探针内求和略有浮动）：

```text
[ncslab_cuda] probe OK (sum=288.640000); GPU path exercised when NCSLAB_SIM_CUDA=1
```

**含义**：驱动栈 + 动态 `cudart` 可用，做过一次很小的 device malloc/memcpy；**不能**代替 Nsight 里的 `gsl_*` 热点占比，也**不**表示尾段 cuBLAS 已参与数值计算。

在 **`ncslab_cuda_cublas_warmup`** 默认启用时（见 [DEPLOYMENT.md](DEPLOYMENT.md)），stderr 还可先出现 **`[ncslab_cuda] cublas warm-up OK (n=...)`**，用于把 **Lt 库装载 / 启发式** 挪到进程启动，减轻首条仿真路径上的固定延迟；随后首次真实伪逆成功仍可能打印 **`tail dgemm on GPU`**。

### 4.3 与矩阵阶 `n`、GPU 尾段阈值（同型模型代码生成日志）

同一类电路在生成阶段可出现 **`Msize=2`**、**`Msize=32`** 等分块信息 → 对应 `util` 里伪逆/逆阵阶数 **`n` 约 2 与 32**。仓库默认 **`NCSLAB_CUDA_DGEMM_MIN_N=32`**（代码内默认值，可用环境变量覆盖）时：**`n=32` 可走 GPU 尾段**，**`n=2` 仍走 CPU GSL 尾乘**。原默认 96 会导致该模型永不触发尾段 GPU；见 [VALIDATION_DGEMM.md](VALIDATION_DGEMM.md)。

---

## 5. Nsight Systems：本机尚未安装时怎么做

**Nsight Systems** 与命令行 **`nsys`** 随 **NVIDIA CUDA Toolkit** 一起发布（独立安装包「Nsight Systems only」也可从 NVIDIA 开发者区下载）。用于在 **时间线** 上看 CPU、线程、CUDA API / GPU 活动，比只看 Java 控制台更接近「`ncslab` 里谁最慢」。

### 5.1 安装（Windows）

1. 打开 NVIDIA 开发者站，下载与**显卡驱动大版本兼容**的 **CUDA Toolkit**（或仅 **Nsight Systems** 安装包）。  
2. 安装时勾选 **Nsight Systems**（默认通常已包含）。  
3. 将 `nsys.exe` 所在目录加入 **用户或系统 `PATH`**。常见路径示例（以实际安装为准）：  
   `C:\Program Files\NVIDIA Corporation\Nsight Systems\`  
4. 新开 **PowerShell / cmd**，执行：`nsys --version`；能输出版本号即 CLI 可用。  
5. **图形界面**：开始菜单启动 **NVIDIA Nsight Systems**，用于打开 `.nsys-rep` 报告。

### 5.2 第一次采集（命令行包一层 `ncslab.exe`）

在 **`ncslab.exe` 所在目录**（例如日志里的 `D:\NewLab\Code\CCode\...\`）打开终端，**不要**依赖 Eclipse 里已设好的环境时，可先手动设与线上一致的环境再 profile，例如：

```bat
set NCSLAB_SIM_CUDA=1
nsys profile -o ncslab_run1 --force-overwrite true --trace=cuda,nvtx,cublas --stats=true ncslab.exe
```

（PowerShell 下请用：`$env:NCSLAB_SIM_CUDA="1"`。）

若出现 `Illegal --trace argument 'osrt'`：表示当前 Nsight 版本**不再支持**追踪项 `osrt`（旧版里的 OS runtime）；改用上面 **`cuda,nvtx,cublas`** 等合法组合即可；完整列表以 **`nsys profile --help`** 为准。

若 `ncslab` 需要与 Java 联调（从 stdin/TCP 启动），可改为先正常启动 Java，再用 Nsight **Attach** 到已运行的 `ncslab.exe` 进程（Nsight GUI：**File → New Project → Attach**），或咨询你们现有启动参数是否支持「独立跑满几秒后退出」以便 `nsys profile` 直接包一层。

采集结束后当前目录会生成 **`ncslab_run1.nsys-rep`**（及可能附带的 sqlite 等）。

**输出文件名已存在（`File exists`）**

若提示 `Failed to create '...\ncslab_run1.nsys-rep': File exists`，说明目录里已有上次采集的 **`ncslab_run1.nsys-rep` / `ncslab_run1.sqlite`**。请任选：**加 `--force-overwrite true`**，或 **`Remove-Item .\ncslab_run1.*`** 后再跑，或 **换新的 `-o ncslab_run2`**。若主文件写入失败，Nsight 可能只在 **`%TEMP%\nsys-*`** 留下临时 `nsys-report-xxxx.*`，与当前工作目录不同名，易导致后续统计读错文件、出现大量 **SKIPPED**。

**统计里全是 `SKIPPED ... does not contain CUDA trace data`**

常见原因：**未设置 `NCSLAB_SIM_CUDA=1`**（进程内无任何 CUDA 调用）；或 **`ncslab.exe` 立刻退出**（例如仍等 Java TCP，无连接则几乎无有效区间）；或 **上一步输出文件冲突** 导致 sqlite 不完整。管理员 PowerShell 下建议显式：

```powershell
$env:NCSLAB_SIM_CUDA="1"
nsys profile -o ncslab_run2 --force-overwrite true --trace=cuda,nvtx,cublas --stats=true .\ncslab.exe
```

（若需**关闭** `n=32` 的 GPU 尾段可设：`$env:NCSLAB_CUDA_DGEMM_MIN_N="64"`；若需**强制更小矩阵也试 GPU** 可设 `16` 等，可能更慢。）

### 5.3 在 GUI 里看什么（填 §2 用）

1. 用 **Nsight Systems** 打开 `*.nsys-rep`。  
2. **CPU / Threads** 时间线：找占用宽的线程；展开 **OS Runtime** / **Sampling**（若采集时启用了 CPU sampling）看热点函数名（如 `gsl_linalg_SV_decomp`、`gsl_blas_dgemm`、生成代码中的 RHS）。  
3. **CUDA HW** / **CUDA API** 行：是否有持续 `cudaMemcpy`、`cuLaunchKernel` 等与步进对齐；仅有启动瞬间尖峰多为探针或小算子。  
4. 用 **Summary / Top functions**（不同版本菜单位置略有差异）导出或截图，把**估算占比**抄入上文 **§2 表格**。

### 5.4 常见警告与统计输出怎么读（示例）

**两条 WARNING（需管理员权限）**

- `CPU context switches trace requires administrative privileges`  
- `CPU sampling requires administrative privileges`  

表示当前会话**没有提升权限**，Nsight **不会**采集 CPU 上下文切换与 **CPU 采样栈**。此时 **`cuda_api_sum` / CUDA 内存** 仍可能有数据，但**很难从同一份报告里直接看到 `gsl_*` 谁占 CPU**——若要补 §2 的 CPU 热点，请任选其一：**以管理员身份**打开 PowerShell 再跑同一条 `nsys profile`；或另用 **Very Sleepy / VS CPU 采样** 贴 `ncslab.exe`；或在 GUI 里看是否仍有基础线程时间线（依版本而定）。

**`SKIPPED: ... does not contain NVTX data`**

工程里未插 **NVTX** 标记，属正常，可忽略；需要时在热点函数外加 `nvtxRangePush/Pop`（以后再议）。

**`cuda_api_sum` 里只有 `cudaMalloc` / `cudaMemcpy` / `cudaFree` / `cudaDeviceSynchronize`，没有 `cublasDgemm` 等**

与 **`ncslab_cuda_runtime_probe`** 行为一致：探针只做**小块** device 内存与 **memcpy**，时间主要在 **API 与同步**上。若 **`n` 仍小于** `NCSLAB_CUDA_DGEMM_MIN_N`（默认 **32**，可环境覆盖），**尾段 cuBLAS 不会调用**，统计里就**不会出现** cuBLAS API 行。

**`cuda_gpu_kern_sum` SKIPPED（无 kernel 数据）**

本次轨迹里**没有**被记录为独立 GPU kernel 的负载（或仅有 memcpy 类传输）。探针路径以 **runtime API + memcpy** 为主时，经常出现本提示；**不代表**显卡没工作，只表示「本报告没有 kernel 汇总这一层」。

**`cuda_gpu_mem_time_sum` 约 0.002 MB 量级**

与「**约 256 个 double** 的探针缓冲」同量级（约 2 KB），与 stderr 里 **`[ncslab_cuda] probe OK`** 一致。

**与填 HOTSPOT 表 §2 的关系**

本例说明 **CUDA 路径已通、探针已跑**；**仍不能**代替「整段仿真里 `gsl_linalg_SV_decomp` 占多少」——需要 **带 CPU 采样的 profile**（管理员）或 **Attach 到由 Java 拉起的长时间 `ncslab`**，或单独做 CPU profiler。

### 5.5 安装前的临时替代（仅 CPU 热点）

在尚未安装 Nsight 时，可对 **`ncslab.exe`** 使用 **Very Sleepy**、**Visual Studio → 调试 → 性能探查器（CPU）** 等做 **CPU 采样**，仍能填 §2 的「函数名 + 约占比」；**看不到**完整 CUDA 时间线，与 Nsight 互补。
