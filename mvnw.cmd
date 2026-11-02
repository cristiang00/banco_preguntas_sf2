@echo off
setlocal
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
)
set "MAVEN_CMD=C:\Users\josel\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd"
if not exist "%MAVEN_CMD%" (
    echo Error: Maven not found at %MAVEN_CMD%
    exit /b 1
)
"%MAVEN_CMD%" %*

