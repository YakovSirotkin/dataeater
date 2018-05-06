# dataeater

## Model 
This module is designed for collecting messages from many IoT devices. For sake of performance and simplicity this module 
is extremely lightweight. Scalability can be easily provided if we use several front-end servers and database instances. 
All data processing should be organized in other modules. 

The most important part is [table creation script](src/sql/create.sql). There are following columns in this table: 
primary key **message_id** is auto-generated, **device_id** is unique for each device that works as source of messages,
**device_time** is time on device when message was generated, **db_time** is auto-populated field with time 
when messages was inserted into database and **message** column contains message itself.

**device_time** allows to filter duplicated messages and sort messages in correct order. **db_time** allows to notice 
connectivity issues and check that device timer is working fine. **message** itself should be in JSON format and should 
contain message format version. But we can't explain IoT device that it should change the format of the message, so we 
accept it as plain text, without additional checks.

Device model is not added as column and should be easily discovered from **device_id**.

## Tests

There is no room for unit testing, so the only test is an integration one. It runs embedded PostgreSQL database 
and takes significant time, especially for the first run. For better continuous integration you should consider using 
special test database. 

## Usage

This module follows standard Maven and Spring Boot structure, you can run application with command

`mvn clean spring-boot:run`

Application starts listening to HTTP requests on port `8080` and expects PostgreSQL database listening 
at local port `5432` with database `messages` and user/password `postgres/pass`. Script `src\sql\create.sql` 
should be applied to database. Configuration parameters are listed in `src\main\resources\application.properties`.

Here is sample usage of the service sending message from device that thinks that it works in 1970:

`curl -i --request POST -H "Content-Type: application/json" -d "{version:1,message:\"Hello world!\"}" http://127.0.0.1:8080/message/timeMachine1/1234567`
