#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
#
# Optional ENV vars
# -----------------
#   JAVA_HOME - location of a JDK home dir, required when download maven via java source
#   MVNW_REPOURL - repo url base for downloading maven distribution
#   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
#   MVNW_VERBOSE - true: enable verbose log; debug: trace the mvnw script; others: silence the output
# ----------------------------------------------------------------------------

set -euf

[ "${MVNW_VERBOSE-}" != debug ] || set -x

# set JAVACMD and JAVACCMD
setJavaHome() {
  # For Cygwin or MSYS, ensure paths are in Unix format before anything is touched
  if ${cygwin:=false} || ${msys:=false} ; then
    [ -z "${JAVA_HOME-}" ] || JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
  fi

  if [ -z "${JAVA_HOME-}" ]; then
    JAVACMD=$(command -v java) || true
    JAVACCMD=$(command -v javac) || true
    if [ -z "${JAVACMD}" ] || [ -z "${JAVACCMD}" ]; then
      die "The java/javac command does not exist in PATH nor is JAVA_HOME set. Install a JDK and make sure commands are in PATH or set JAVA_HOME."
    fi
  else
    JAVACMD="$JAVA_HOME/bin/java"
    JAVACCMD="$JAVA_HOME/bin/javac"

    if [ ! -x "$JAVACMD" ]; then
      die "JAVA_HOME is set to an invalid directory: $JAVA_HOME. JAVA_HOME shall point to a JDK not a JRE."
    fi
  fi
}

native_path() {
  # On Windows Cygwin, MSYS(2) or WSL, use `cygpath` to convert path, otherwise leave unchanged
  if ${cygwin} || ${msys} ; then
    cygpath --path --windows "$1"
  elif ${wsl} ; then
    wslpath -w "$1"
  else
    printf "%s" "$1"
  fi
}

die() {
  printf '%s\n' "$1" >&2
  exit 1
}

verbose() {
  [ "${MVNW_VERBOSE-}" != true ] || printf '%s\n' "$1"
}

# parse distributionUrl and optional distributionSha256Sum, requires .mvn/wrapper/maven-wrapper.properties
while IFS="=" read -r key value; do
  case "${key-}" in
    distributionUrl) distributionUrl="${value-}" ;;
    distributionSha256Sum) distributionSha256Sum="${value-}" ;;
  esac
done < "${0%/*}/.mvn/wrapper/maven-wrapper.properties"
[ -n "${distributionUrl-}" ] || die "cannot read distributionUrl property in ${0%/*}/.mvn/wrapper/maven-wrapper.properties"

# Maven Wrapper downloads distro into maven-user-home, Because MAVEN_USER_HOME can be set by users, the default should be ~/.m2
MAVEN_USER_HOME="${MAVEN_USER_HOME:-${HOME}/.m2}"
mkdir -p "${MAVEN_USER_HOME}"/wrapper/dists

if ${cygwin:=false} || ${msys:=false} ; then
  distributionUrl=$(cygpath --unix "${distributionUrl}")
  MAVEN_USER_HOME=$(cygpath --unix "${MAVEN_USER_HOME}")
fi

wsl=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true ;;
  MINGW*) msys=true ;;
  MSYS*) msys=true ;;
  Linux*)
    [ -e /proc/version ] && grep -qi microsoft /proc/version && wsl=true
    ;;
esac

mkdir -p "${MAVEN_USER_HOME}/wrapper/dists"
BASE_DIR=$(dirname "$0")

# apply MVNW_REPOURL and calculate MAVEN_USER_HOME url
case "${distributionUrl-}" in
  *"://"*)
    distributionUrl="${MVNW_REPOURL:-}${distributionUrl#*://}"
    distributionUrl="${distributionUrl#http://}"
    distributionUrl="${distributionUrl#https://}"
    distributionUrl="${distributionUrl#ftp://}"
    distributionUrl="${distributionUrl#ftps://}"
    distributionUrl="${distributionUrl#file://}"
    distributionUrl="${distributionUrl#s3://}"
    ;;
  *)
    distributionUrl="${MVNW_REPOURL:-}${distributionUrl}"
    ;;
esac

distributionUrlPath="${distributionUrl#*://*}"
distributionUrlWithoutPath="${distributionUrl%"$distributionUrlPath"}"
distributionUrlFilename="${distributionUrlPath##*/}"
distributionDirName="${distributionUrlFilename%.zip}"

verbose "Distribution URL: ${distributionUrlWithoutPath}${distributionUrlPath}"

# prepare wget recursive url, so sources can be downloaded too
case "${distributionUrlWithoutPath}" in
  https://repo.maven.apache.org/maven2/*)
    distributionUrlRecursive="${distributionUrlWithoutPath}/$(dirname "${distributionUrlPath}")/"
    ;;
  *)
    distributionUrlRecursive=
    ;;
esac

distributionPath="${MAVEN_USER_HOME}/wrapper/dists/${distributionDirName}/$(native_path "${distributionUrlFilename}")"

MAVEN_OPTS="$(printf '%s' "${MAVEN_OPTS-}" | tr '\n' ' ')"

# If distribution exists at the following location, do not download
if [ -f "${distributionPath}" ]; then
  verbose "Found existing Maven distribution at: ${distributionPath}"
else
  verbose "Couldn't find Maven distribution at: ${distributionPath}"

  MAVEN_WRAPPER_JAR="${BASE_DIR}/.mvn/wrapper/maven-wrapper.jar"
  if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
    verbose "Downloading Maven Wrapper... from ${distributionUrlRecursive}"
    MAVEN_WRAPPER_REPOURL="${MVNW_REPOURL:-https://repo.maven.apache.org/maven2}"
    jarUrl="${MAVEN_WRAPPER_REPOURL}/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
    echo "Downloading from: ${jarUrl}"
    if command -v curl >/dev/null 2>&1; then
      curl -sSfLo "$MAVEN_WRAPPER_JAR" "$jarUrl"
    elif command -v wget >/dev/null 2>&1; then
      wget -q -O "$MAVEN_WRAPPER_JAR" "$jarUrl"
    else
      die "Cannot download Maven wrapper jar. Neither curl nor wget available."
    fi
  fi

  verbose "Downloading Maven distribution: ${distributionUrl}"

  if command -v curl >/dev/null 2>&1; then
    if [ -n "${distributionSha256Sum-}" ]; then
      curl -sSfL "${distributionUrlWithoutPath}${distributionUrlPath}" -o "${distributionPath}.tmp"
      printf '%s  %s\n' "${distributionSha256Sum}" "${distributionPath}.tmp" > "${distributionPath}.tmp.sha256"
      sha256sum -c "${distributionPath}.tmp.sha256"
      mv "${distributionPath}.tmp" "${distributionPath}"
      rm "${distributionPath}.tmp.sha256"
    else
      curl -sSfL "${distributionUrlWithoutPath}${distributionUrlPath}" -o "${distributionPath}"
    fi
  elif command -v wget >/dev/null 2>&1; then
    if [ -n "${distributionSha256Sum-}" ]; then
      wget -q -O "${distributionPath}.tmp" "${distributionUrlWithoutPath}${distributionUrlPath}"
      printf '%s  %s\n' "${distributionSha256Sum}" "${distributionPath}.tmp" > "${distributionPath}.tmp.sha256"
      sha256sum -c "${distributionPath}.tmp.sha256"
      mv "${distributionPath}.tmp" "${distributionPath}"
      rm "${distributionPath}.tmp.sha256"
    else
      wget -q -O "${distributionPath}" "${distributionUrlWithoutPath}${distributionUrlPath}"
    fi
  else
    die "Cannot download Maven distribution. Neither curl nor wget available."
  fi
fi

MAVEN_PROJECTBASEDIR=$(cd "${BASE_DIR}" && pwd)

MAVEN_WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
WRAPPER_LAUNCHER_JAR="${BASE_DIR}/.mvn/wrapper/maven-wrapper.jar"

add_to_java_extdirs() {
  if [ -d "$1" ]; then
    verbose "Adding to Java extdirs: $1"
    JAVA_EXT_DIRS="${JAVA_EXT_DIRS-}:$1"
  fi
}

if [ -n "${JAVA_HOME-}" ]; then
  add_to_java_extdirs "${JAVA_HOME}/lib/ext"
fi

JVM_OPTS="${JVM_OPTS-} -classpath ${WRAPPER_LAUNCHER_JAR}"

exec "${JAVACMD}" \
  ${JVM_OPTS-} \
  -Dorg.apache.maven.wrapper.MavenWrapperMain.projectBasedir="${MAVEN_PROJECTBASEDIR}" \
  -Dorg.apache.maven.wrapper.MavenWrapperMain.javaHome="${JAVA_HOME-}" \
  -Dorg.apache.maven.wrapper.MavenWrapperMain.mavenUserHome="${MAVEN_USER_HOME}" \
  -Dorg.apache.maven.wrapper.MavenWrapperMain.checksumPolicy="${MVNW_CHECKSUM_POLICY-}" \
  -Dorg.apache.maven.wrapper.MavenWrapperMain.checksumErrorMessage="${MVNW_CHECKSUM_ERROR_MESSAGE-}" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "${MAVEN_CMD_LINE_ARGS[@]-}" "$@"

