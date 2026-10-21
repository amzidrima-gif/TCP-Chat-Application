\# TCP Chat Application



A Java-based TCP chat application featuring a JavaFX client interface and a server-side dashboard. The project demonstrates client-server communication over TCP, user connectivity, message exchange, and server activity monitoring.



\## Table of Contents



\- \[Overview](#overview)

\- \[Features](#features)

\- \[Technologies](#technologies)

\- \[Project Structure](#project-structure)

\- \[Prerequisites](#prerequisites)

\- \[Configuration](#configuration)

\- \[Running the Application](#running-the-application)

\- \[Architecture](#architecture)

\- \[Future Documentation](#future-documentation)



\## Overview



The TCP Chat Application consists of two independent Maven projects:



\- \*\*TCPServer:\*\* Manages incoming client connections, TCP communication, and the server dashboard.

\- \*\*TCPClient:\*\* Provides the graphical chat interface through which users connect to the server and interact with the application.



The applications communicate over TCP sockets, allowing the client and server to exchange data through a network connection.



\## Features



\### Client Application



\- JavaFX-based graphical user interface.

\- Connection to a TCP server using a host and port.

\- Chat interface for exchanging messages.

\- Display of connection status and chat activity.

\- User interface styling through CSS.



\### Server Application



\- TCP server for accepting client connections.

\- Server-side connection and communication management.

\- JavaFX dashboard for monitoring server activity.

\- Configuration files for server settings.

\- Centralized visibility into server operations.



\## Technologies



\- \*\*Language:\*\* Java

\- \*\*Networking:\*\* TCP sockets

\- \*\*GUI:\*\* JavaFX

\- \*\*Build and dependency management:\*\* Apache Maven

\- \*\*Styling:\*\* CSS

\- \*\*Version control:\*\* Git and GitHub



\## Project Structure



```text

TCP-Chat-Application/

├── TCPClient/

│   ├── pom.xml

│   └── src/

│       └── main/

│           ├── java/org/example/

│           │   ├── Main.java

│           │   ├── ChatClientApp.java

│           │   └── ClientConnection.java

│           └── resources/

│               ├── client.properties

│               └── styles.css

├── TCPServer/

│   ├── pom.xml

│   ├── Config/

│   │   └── server.properties

│   └── src/

│       └── main/

│           ├── java/org/example/

│           │   ├── Main.java

│           │   ├── ChatServer.java

│           │   ├── ServerConfig.java

│           │   └── ServerDashboard.java

│           └── resources/

│               └── server.properties

├── .gitignore

└── README.md

```



\## Prerequisites



Ensure that the following tools are installed:



\- \*\*JDK 22\*\* or a compatible Java version supported by the project's configuration.

\- \*\*Apache Maven\*\*, or the Maven wrapper if configured for use.

\- \*\*IntelliJ IDEA\*\* or another Java IDE.

\- A computer capable of running JavaFX applications.



The client and server must use compatible Java and JavaFX configurations.



\## Configuration



The application includes configuration files for client and server settings:



\- `TCPClient/src/main/resources/client.properties`

\- `TCPServer/src/main/resources/server.properties`

\- `TCPServer/Config/server.properties`



Review the existing configuration values before running the applications. Ensure that the client connects to the same host and port on which the server is listening.



For local testing, the server and client can run on the same computer.



\## Running the Application



\### 1. Open the projects



Open `TCPServer` and `TCPClient` as Maven projects in IntelliJ IDEA. Allow Maven to import the dependencies specified in each `pom.xml`.



\### 2. Start the server



Run the server's `org.example.Main` class first. Confirm that the server starts successfully and is listening on its configured host and port.



\### 3. Start the client



Run the client's `org.example.Main` class. Configure the client to connect to the server's host and port, then verify that the connection is established.



\### 4. Test communication



Once the connection is established, test the chat functionality and observe the server dashboard for connection and activity updates.



\*\*Note:\*\* Exact launch commands and executable JAR distribution instructions should be documented after independently verifying the final Maven packaging configuration.



\## Architecture



The application follows a client-server architecture:



1\. The server listens for incoming TCP connections.

2\. The client establishes a connection to the server.

3\. The client and server exchange data through TCP communication.

4\. The server manages client connections and exposes activity through its dashboard.

5\. The JavaFX interfaces provide graphical interaction and monitoring.



The client and server are maintained as separate Maven projects to keep their responsibilities and dependencies organized.



\## Future Documentation



The repository can be extended with the following project documentation:



\- UML class diagram.

\- UML deployment diagram.

\- Screenshots of the client and server dashboard.

\- Verified build and execution instructions.

\- Demonstration video.

\- Packaging and submission instructions.



\## Author



Developed as a Java networking project demonstrating TCP socket communication, JavaFX interfaces, and client-server application design.



\## License



No license has been specified for this repository.

