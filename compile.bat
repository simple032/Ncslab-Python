@echo off
setlocal

set JAVA_HOME=C:\Program Files\OpenLogic\jdk-11.0.22.7-hotspot
set JAVAC="%JAVA_HOME%\bin\javac.exe"

set CP=target/classes
set CP=%CP%;C:\eclipse\lombok.jar

for /r "%USERPROFILE%\.m2\repository\com\fasterxml\jackson" %%f in (*.jar) do (
    if "%%~xf"==".jar" set CP=!CP!;%%f
)
for /r "%USERPROFILE%\.m2\repository\org\slf4j" %%f in (*.jar) do (
    if "%%~xf"==".jar" set CP=!CP!;%%f
)
for /r "%USERPROFILE%\.m2\repository\org\apache\velocity" %%f in (*.jar) do (
    if "%%~xf"==".jar" set CP=!CP!;%%f
)
for /r "%USERPROFILE%\.m2\repository\org\json" %%f in (*.jar) do (
    if "%%~xf"==".jar" set CP=!CP!;%%f
)

%JAVAC% -cp "%CP%" -proc:full -d target/classes ^
    src/main/java/com/ncslab/block/stateflow/InputVariableConnection.java ^
    src/main/java/com/ncslab/block/stateflow/StateflowChart.java ^
    src/main/java/com/ncslab/ncslablink/NCSLabModel.java

endlocal
