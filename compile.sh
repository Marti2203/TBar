mvn compile
mvn dependency:copy-dependencies
mvn package
mv target/TBar-0.0.1-SNAPSHOT.jar target/dependency/
