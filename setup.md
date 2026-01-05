# Setup for POC

This setup includes the setup for this POC. This is currently running the Kafka instance via WSL, so if you are using docker, please update the server and ports.

## Kafka Topics
1. Once Kafka server is up, add the following topics:
    - Partner1Incoming
    - Partner2Incoming
    - Partner1Outgoing
    - Partner2Outgoing

## Python Servers
There are 2 Python servers created in the Kafka-testing folder: **main.py** and **python_tcp_server.py**

### Web server 

Before running the **main.py** server, add the package **FastApi** for it to work. 
To run the web server, run the command `uvicorn main:app --reload --port 5000`

### TCP Server

Run the TCP Server with the command `.\python_tcp_server.py`


## Spring Boot Applications

Before running the 3 applications (Ingestor, StreamProcessor, Adapter), make sure to update which server and port the Kafka server is running in their respective `application.properties` files.

> If you are opening via IntelliJ, simply run the application.

> If you are running via the command line, run `./mvnw spring-boot:run` command

## Sending Messages

There are 3 endpoints in the web server that you can use to produce/show messages:
    - `/generate-xml` shows you a randomly-generated XML message.
    - `/send-one` generates one randomly-generated XML and sends it to the Ingester service
    - `/generate-and-send` generates 500 messages and sends them to theh Ingester service one by one

