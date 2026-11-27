@echo off
setlocal EnableDelayedExpansion
if not defined JAVA_HOME (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
    ) else if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
    )
)
set "MAVEN_CMD=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd"
if exist "%MAVEN_CMD%" (
    call "%MAVEN_CMD%" %*
    exit /b !ERRORLEVEL!
)
where mvn.cmd >nul 2>nul
if !ERRORLEVEL! equ 0 (
    call mvn.cmd %*
    exit /b !ERRORLEVEL!
)
for /d %%D in ("%USERPROFILE%\.m2\wrapper\dists\apache-maven-*") do (
    for /d %%H in ("%%D\*") do (
        if exist "%%H\bin\mvn.cmd" (
            call "%%H\bin\mvn.cmd" %*
            exit /b !ERRORLEVEL!
        )
    )
)
echo Error: Maven no encontrado en PATH ni en ~/.m2/wrapper/dists
exit /b 1
