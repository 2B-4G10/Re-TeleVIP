#!/usr/bin/env bash
# Moves the build to the latest stable Android Gradle Plugin, Gradle and libxposed API.
# Edits the files in place and prints a one-line summary of what changed (nothing if current).
#   AGP        gradle/libs.versions.toml `agp`      <- Google Maven, stable releases only
#   Gradle     gradle/wrapper distributionUrl       <- services.gradle.org, current release
#   libxposed  gradle/libs.versions.toml `libxposed` and module.prop targetApiVersion
#              <- Maven Central; minApiVersion stays, so older frameworks keep loading it
set -euo pipefail

catalog=gradle/libs.versions.toml
prop=app/src/main/resources/META-INF/xposed/module.prop
wrapper=gradle/wrapper/gradle-wrapper.properties

# Highest plain x.y.z version in a Maven metadata file (no alpha, beta or rc).
latest_stable() {
  curl -fsSL --retry 3 "$1" | grep -o '<version>[0-9]*\.[0-9]*\.[0-9]*</version>' \
    | sed 's/<[^>]*>//g' | sort -V | tail -n1
}
current() { sed -n "s/^$1 = \"\([^\"]*\)\"$/\1/p" "$catalog" | head -n1; }

changes=()

agp_now=$(current agp)
agp_new=$(latest_stable https://dl.google.com/android/maven2/com/android/tools/build/gradle/maven-metadata.xml)
if [ -n "$agp_new" ] && [ "$(printf '%s\n%s\n' "$agp_now" "$agp_new" | sort -V | tail -n1)" != "$agp_now" ]; then
  sed -i "s/^agp = \"[^\"]*\"$/agp = \"$agp_new\"/" "$catalog"
  changes+=("AGP $agp_now → $agp_new")
fi

xp_now=$(current libxposed)
xp_new=$(latest_stable https://repo1.maven.org/maven2/io/github/libxposed/api/maven-metadata.xml)
if [ -n "$xp_new" ] && [ "$(printf '%s\n%s\n' "$xp_now" "$xp_new" | sort -V | tail -n1)" != "$xp_now" ]; then
  sed -i "s/^libxposed = \"[^\"]*\"$/libxposed = \"$xp_new\"/" "$catalog"
  level=${xp_new%%.*}
  sed -i "s/^targetApiVersion=.*/targetApiVersion=$level/" "$prop"
  changes+=("libxposed API $xp_now → $xp_new")
fi

gradle_now=$(sed -n 's/.*gradle-\([0-9.]*\)-\(bin\|all\)\.zip/\1/p' "$wrapper")
gradle_new=$(curl -fsSL --retry 3 https://services.gradle.org/versions/current \
  | sed -n 's/.*"version" *: *"\([0-9.]*\)".*/\1/p' | head -n1)
if [ -n "$gradle_new" ] && [ "$(printf '%s\n%s\n' "$gradle_now" "$gradle_new" | sort -V | tail -n1)" != "$gradle_now" ]; then
  # The distribution URL is all the wrapper needs; running the wrapper task instead would first
  # configure the project, which can fail half way through a toolchain bump.
  sed -i "s/gradle-$gradle_now-bin\.zip/gradle-$gradle_new-bin.zip/" "$wrapper"
  changes+=("Gradle $gradle_now → $gradle_new")
fi

if [ ${#changes[@]} -gt 0 ]; then
  (IFS=','; echo "${changes[*]}" | sed 's/,/, /g')
fi
