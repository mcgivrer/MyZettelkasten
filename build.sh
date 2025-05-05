#!/bin/bash
# build scripts (c) 2025 Frederic Delorme
#
# Please adapt the `project_name`, `project_version` and `main_class` variables to fit your own project.
# The generated JARs will be named as target/build/[project_name]-[main_class]-[project_version].jar
#
# NOTE: `main_class` is a list of space separated classes to generate as many JAR as listed classes.
#
project_name=MyZettelkasten
project_version=0.0.1
main_class=my.zettelkasten.TextEditorApp
#
#--- DO NOT CHANGE THE FOLLOWING LINES ---
#
echo "build project ' ${project_name}' version ${project_version}..."
echo ---
echo "clean previous build..."
rm -vrf target/
mkdir -vp target/{build,classes}
echo "done."
echo ---
echo "sources files:"
find src/main/java src/main/resources -name "*.java"
echo ---
echo "compile..."
# shellcheck disable=SC2046
javac -d target/classes $(find src/main/java src/main/resources -name "*.java")
cp -vr src/main/resources/* target/classes/
echo "done."
echo ---
echo "build jar..."
for app in ${main_class}
do
  echo ">> for ${project_name}.$app..."
  jar cvfe target/build/${project_name}-$app-${project_version}.jar $app -C target/classes .
  echo "done."
done
