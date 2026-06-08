# Profiling compile time (T_compile) and `ncslab` runtime (T_run)

End-to-end simulation latency includes **code generation + `make`** before **`ncslab.exe`** runs. Use this checklist with the CUDA acceleration plan (do not confuse **T_compile** with **GPU kernel** time).

## A0 — Compile phase

1. **Java build log**  
   After upgrading the IDE/JAR, each `make` prints one line to **stderr**:
   `[CodeStructC] make wall=XXXX ms USE_CUDA=0|1|unset exit=N`  
   Compare **first** attempt (`USE_CUDA=1` when CUDA simulation is requested) with **retry** (`USE_CUDA=0`) if the first fails—double full builds inflate **T_compile**.

2. **Heaviest translation units**  
   In the model directory, check sizes of **`mainccode.cpp`**, **`onestep.cpp`**, **`results.cpp`**. Time a single compile if needed:
   `g++ ... -c mainccode.cpp` (same flags as makefile).

3. **Speed-ups (no code change to numerics)**  
   - **`make -jN`** (export `MAKEFLAGS=-j8` or call `mingw32-make -j8`) if the makefile and toolchain allow parallel `.o` builds.  
   - **`ccache`** with identical compiler flags.  
   - Avoid deleting all `.o` when only one generated file changed.

## A — Runtime (`ncslab.exe`) with Nsight

1. **Administrator** PowerShell or Nsight GUI so **CPU sampling** is not disabled.
2. Prefer **Attach** to `ncslab.exe` **after** Java has connected and the integrator is stepping (30–120 s capture).
3. Commands (model directory):
   ```powershell
   nsys profile -o ncslab_attach_steady --force-overwrite true --trace=cuda,nvtx,cublas --stats=true ...
   nsys stats --force-export=true --report cuda_api_sum ncslab_attach_steady.nsys-rep
   ```
4. Fill **[HOTSPOT_TABLE.md](HOTSPOT_TABLE.md) §2** with CPU Top functions and **steady-state** `cuda_api_sum` (not only process startup).

## Deliverables template

| Metric | Typical model | Extreme model |
|--------|----------------|---------------|
| T_compile (clean) | | |
| T_compile (incremental) | | |
| T_run (full sim) | | |
| Java `NativeCodeBridgeProfiler` wall | | |

Record **USE_CUDA** first-pass success vs retry.
