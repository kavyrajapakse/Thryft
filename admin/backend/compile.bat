@echo off
echo Compiling AdminStatsServlet...

REM Set paths - CHANGE THESE to match your system
set GLASSFISH_HOME=C:\glassfish6\glassfish
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_xxx

REM Compile servlet
"%JAVA_HOME%\bin\javac" -cp "%GLASSFISH_HOME%\lib\javaee.jar;json-20230227.jar" AdminStatsServlet.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)

pause
