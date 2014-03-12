myeslib
=======

An Event Sourcing experiment using Apache Camel, Hazelcast, JDBI, etc
It was inspired by: http://www.jayway.com/2013/06/20/dont-publish-domain-events-return-them/
and off course: https://github.com/gregoryyoung/m-r

Getting Started
===============

```
cd myeslib
mvn clean install
```
Before running it, you can optionally you can customize database settings on export-db-env-h2.sh: 
```
#!/bin/sh
DB_DATASOURCE_CLASS_NAME=org.h2.jdbcx.JdbcDataSource
#DB_URL=jdbc:h2:mem:test;MODE=Oracle
DB_URL=jdbc:h2:file:~/myeslib-database;MODE=Oracle
DB_USER=scott
DB_PASSWORD=tiger
export DB_DATASOURCE_CLASS_NAME
export DB_URL
export DB_USER
export DB_PASSWORD
```
then export the db variables and call http://flywaydb.org/ to initialize the target database:
```
source ./export-db-env-oracle.sh
cd myeslib-database
mvn clean compile flyway:migrate -Dflyway.locations=db/h2
```
there is a script for Oracle too:
```
mvn clean compile flyway:migrate -Dflyway.locations=db/oracle
```
after this your database should be ready. Now:
```
cd ../myeslib-hazelcast-example
java -jar target/myeslib-hazelcast-example-0.0.1-SNAPSHOT.jar
```
this service will receive commands as JSON on http://localhost:8080/inventory-item-command.
There is another implementation (simpler since it uses Hazelcast just for cache) on myeslib-jdbi-example.
Finally, start bellow script in other console in order to create and send commands to the above endpoint.
```
cd myeslib-cmd-producer
java -jar target/myeslib-cmd-producer-0.0.1-SNAPSHOT.jar
```
Notes
=====
Your IDE must support http://projectlombok.org/
Disclaimer
==========
There are 2 packages within module 3rd-party with (intact) code from :
http://gsd.di.uminho.pt/members/jop/mm4j -> multimethods magic (thanks José Orlando)
https://code.google.com/p/google-gson -> since I did not found RuntimeTypeAdapter classes within gson-2.2.4 jar


