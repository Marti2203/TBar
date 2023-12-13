#!/bin/bash
ROOT=$(pwd)
PROJECT_DIR=$1
PROJECT_NAME=$2

cd $PROJECT_DIR
mvn clean compile test-compile
rm -rf .build

cd $ROOT

CLASS_DIR=$PROJECT_DIR/target/classes
TEST_DIR=$PROJECT_DIR/target/test-classes

GZOLTAR_VERSION=1.7.4-SNAPSHOT
JUNIT_JAR=gzoltar/com.gzoltar.cli.examples/lib/junit.jar
HAMCREST_JAR=gzoltar/com.gzoltar.cli.examples/lib/hamcrest-core.jar
GZOLTAR_CLI_JAR=gzoltar/com.gzoltar.cli/target/com.gzoltar.cli-$GZOLTAR_VERSION-jar-with-dependencies.jar
GZOLTAR_AGENT_RT_JAR=gzoltar/com.gzoltar.agent.rt/target/com.gzoltar.agent.rt-$GZOLTAR_VERSION-all.jar
UNIT_TESTS_FILE=$PROJECT_DIR/unit-tests.txt

java -cp $TEST_DIR:$JUNIT_JAR:$HAMCREST_JAR:$GZOLTAR_CLI_JAR com.gzoltar.cli.Main listTestMethods $TEST_DIR --outputFile $UNIT_TESTS_FILE

[ -s "$UNIT_TESTS_FILE" ] || die "$UNIT_TESTS_FILE does not exist or it is empty!"

SER_FILE="$PROJECT_DIR/gzoltar.ser"

echo "Perform offline instrumentation ..."

# Backup original classes
TEST_BACKUP_DIR="$PROJECT_DIR/.build_test"
CLASS_BACKUP_DIR="$PROJECT_DIR/.build_classes"
mv "$TEST_DIR" "$TEST_BACKUP_DIR" || die "Backup of original classes has failed!"
mv "$CLASS_DIR" "$CLASS_BACKUP_DIR" || die "Backup of original classes has failed!"
mkdir -p "$CLASS_DIR"
mkdir -p "$TEST_DIR"

# Perform offline instrumentation for test suite
#java -cp $TEST_BACKUP_DIR:$GZOLTAR_AGENT_RT_JAR:$GZOLTAR_CLI_JAR \
#com.gzoltar.cli.Main instrument \
#--outputDirectory "$TEST_DIR" $TEST_BACKUP_DIR || die "Offline instrumentation has failed!"

# Perform offline instrumentation for classes
java -cp $CLASS_BACKUP_DIR:$GZOLTAR_AGENT_RT_JAR:$GZOLTAR_CLI_JAR \
com.gzoltar.cli.Main instrument \
--outputDirectory "$CLASS_DIR" $CLASS_BACKUP_DIR || die "Offline instrumentation has failed!"

echo "Run each unit test case in isolation ..."

# Run each unit test case in isolation
java -cp $TEST_DIR:$CLASS_DIR:$JUNIT_JAR:$HAMCREST_JAR:$GZOLTAR_AGENT_RT_JAR:$GZOLTAR_CLI_JAR \
-Dgzoltar-agent.destfile=$SER_FILE \
-Dgzoltar-agent.output="file" \
com.gzoltar.cli.Main runTestMethods \
    --testMethods "$UNIT_TESTS_FILE" \
    --offline \
    --collectCoverage || die "Coverage collection has failed!"

# Restore original test classes
cp -R $TEST_BACKUP_DIR/* "$TEST_DIR" || die "Restore of original classes has failed!"
rm -rf "$TEST_BACKUP_DIR"

# Restore original classes
cp -R $CLASS_BACKUP_DIR/* "$CLASS_DIR" || die "Restore of original classes has failed!"
rm -rf "$CLASS_BACKUP_DIR"

[ -s "$SER_FILE" ] || die "$SER_FILE does not exist or it is empty!"

#
# Create fault localization report
#

echo "Create fault localization report ..."

SPECTRA_FILE="$PROJECT_DIR/sfl/txt/spectra.csv"
MATRIX_FILE="$PROJECT_DIR/sfl/txt/matrix.txt"
TESTS_FILE="$PROJECT_DIR/sfl/txt/tests.csv"

java -cp $CLASS_DIR:$TEST_DIR:$JUNIT_JAR:$HAMCREST_JAR:$GZOLTAR_CLI_JAR \
  com.gzoltar.cli.Main faultLocalizationReport \
    --buildLocation "$CLASS_DIR" \
    --granularity "line" \
    --inclPublicMethods \
    --inclStaticConstructors \
    --inclDeprecatedMethods \
    --dataFile "$SER_FILE" \
    --outputDirectory "$PROJECT_DIR" \
    --family "sfl" \
    --formula "ochiai" \
    --metric "entropy" \
    --formatter "txt" || die "Generation of fault-localization report has failed!"

[ -s "$SPECTRA_FILE" ] || die "$SPECTRA_FILE does not exist or it is empty!"
[ -s "$MATRIX_FILE" ] || die "$MATRIX_FILE does not exist or it is empty!"
[ -s "$TESTS_FILE" ] || die "$TESTS_FILE does not exist or it is empty!"

mkdir -p ./SuspiciousCodePositions/$PROJECT_NAME

tail $PROJECT_DIR/sfl/txt/ochiai.ranking.csv -n +2 > ./SuspiciousCodePositions/$PROJECT_NAME/Ochiai.txt
sed -r -i 's/(.*)\$(.*)#(.*):(.*);(.*)/\1.\2@\4@\5/g' ./SuspiciousCodePositions/$PROJECT_NAME/Ochiai.txt