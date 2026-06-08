# Configuration and Test Layers

## Runtime Configuration

Configuration is loaded through `com.ncslab.config.NCSLabConfig`.

Lookup order:

1. JVM system property with the legacy key, for example `-DCCodePath=...`
2. JVM system property or environment variable with the `NCSLAB_` alias
3. `config.properties` on the classpath
4. Caller default value

Supported high-value aliases:

| Legacy key | Preferred alias |
| --- | --- |
| `CCodePath` | `NCSLAB_CODE_ROOT` |
| `CCodePathWin` | `NCSLAB_CODE_ROOT_WIN` or `NCSLAB_CODE_ROOT` |
| `MCodePath` | `NCSLAB_M_CODE_ROOT` |
| `PLCCodePath` | `NCSLAB_PLC_CODE_ROOT` |
| `MfcalcCodePath` | `NCSLAB_MFCALC_CODE_ROOT` |
| `MakeTool` | `NCSLAB_MAKE_TOOL` |
| `filesystem.api.url` | `NCSLAB_FILESYSTEM_API_URL` |
| `mfcalc.server.host` | `NCSLAB_MFCALC_SERVER_HOST` |
| `mfcalc.server.port` | `NCSLAB_MFCALC_SERVER_PORT` |
| `NCSLAB_DB_URL` | `NCSLAB_DB_URL` |
| `NCSLAB_DB_USERNAME` | `NCSLAB_DB_USERNAME` |
| `NCSLAB_DB_PASSWORD` | `NCSLAB_DB_PASSWORD` |

Legacy calls through `com.utils.Property.instance.getProperty(...)` still work and now use the centralized loader.

## Test Layers

JUnit 4 category markers live under `com.ncslab.test.category`.

Default `mvn test` excludes these tagged heavy layers:

| Layer | Category | Maven profile |
| --- | --- | --- |
| Integration | `IntegrationTest` | `mvn -Pintegration-tests test` |
| Native/codegen | `NativeTest` | `mvn -Pnative-tests test` |
| Hardware/platform | `HardwareTest` | `mvn -Phardware-tests test` |
| Everything | none | `mvn -Pall-tests test` |

When adding tests that require a database, external compiler, CUDA runtime, serial hardware, EtherCAT, STM32, Raspberry Pi, or LoongArch deployment, tag the class with the matching category so the default unit-test loop stays fast.
