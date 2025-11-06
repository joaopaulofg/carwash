@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@IF EXIST "%~dp0\.mvn\jvm.config" (
  @SET JVM_CONFIG_MAVEN_PROPS="@%~dp0\.mvn\jvm.config"
) ELSE (
  @SET JVM_CONFIG_MAVEN_PROPS=
)

@SET LAUNCH_JAR=%~dp0\.mvn\wrapper\maven-wrapper.jar
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@REM Download maven-wrapper.jar if necessary
@IF NOT EXIST "%LAUNCH_JAR%" (
  @IF NOT "%MVNW_REPOURL%"=="" (
    SET DOWNLOAD_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  )
  @IF "%MVNW_VERBOSE%"=="true" (
    ECHO Downloading Maven Wrapper: %DOWNLOAD_URL%
  )
  powershell -NoLogo -NoProfile -Command "Invoke-WebRequest -Uri %DOWNLOAD_URL% -OutFile '%LAUNCH_JAR%'"
)

@SET MAVEN_PROJECTBASEDIR=%~dp0

@REM Find java.exe
@SET JAVA_EXE=java.exe
@IF DEFINED JAVA_HOME (
  @SET JAVA_HOME=%JAVA_HOME:"=%
  @SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

@IF EXIST "%JAVA_EXE%" goto execute

@ECHO Error: JAVA_HOME is not defined correctly.
@ECHO   We cannot execute %JAVA_EXE%
@EXIT /B 1

:execute
@IF "%MVNW_VERBOSE%"=="true" (
  ECHO %JAVA_EXE% %JVM_CONFIG_MAVEN_PROPS% %MAVEN_OPTS% -classpath "%LAUNCH_JAR%" %WRAPPER_LAUNCHER% %*
)
"%JAVA_EXE%" %JVM_CONFIG_MAVEN_PROPS% %MAVEN_OPTS% -classpath "%LAUNCH_JAR%" %WRAPPER_LAUNCHER% %*
@EXIT /B %ERRORLEVEL%
