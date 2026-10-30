#!/usr/bin/env sh
# Lightweight Gradle bootstrapper for this project. It downloads the pinned Gradle
# distribution on first use; it does not require a system Gradle installation.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.2.1
GRADLE_USER_HOME=${GRADLE_USER_HOME:-"$HOME/.gradle"}
DIST_ROOT="$GRADLE_USER_HOME/wrapper/dists/gradle-$GRADLE_VERSION-bin/dispenser-control"
GRADLE_HOME="$DIST_ROOT/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
    mkdir -p "$DIST_ROOT"
    ARCHIVE="$DIST_ROOT/gradle-$GRADLE_VERSION-bin.zip"
    URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    if command -v curl >/dev/null 2>&1; then
        curl -fL "$URL" -o "$ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$ARCHIVE" "$URL"
    else
        echo "curl or wget is required to download Gradle $GRADLE_VERSION" >&2
        exit 1
    fi
    if command -v unzip >/dev/null 2>&1; then
        unzip -q -o "$ARCHIVE" -d "$DIST_ROOT"
    else
        echo "unzip is required to extract Gradle $GRADLE_VERSION" >&2
        exit 1
    fi
fi
exec "$GRADLE_BIN" -p "$APP_HOME" "$@"
