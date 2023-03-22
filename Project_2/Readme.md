## Running servers

Navigate to src folder.

Compile server using the command: 
`javac BulletinBoardServer.java`

Run server using the command: 
`java BulletinBoardServer <hostname> 2000 <consistency name>`
Example: `java BulletinBoardServer localhost 2000 readyourwrites`

## Running clients
Compile client using the command: 
`javac BulletinBoardClient.java`

Run client using the command: 
`java BulletinBoardClient <hostname>`
Example: `java BulletinBoardClient localhost`

When prompted to enter a command, type "join: 2000" for example to join a server running on port 2000. 

The port number used above can be 2000, 2001, 2002, 2003 and 2004. Any other port number will result in the program terminating gracefully.

## Running the tests
Navigate to `test` folder with `cd test` from root. Copy the RMI interfaces from src to this directory with
````
cp ../src/ServerToServerinterface.class
cp ../src/BulletinBoardServerInterface.class
````
but only after compiling in the previous steps.
Compile the tests and system starter with:
````
javac -cp ./../lib/junit-4.13.2.jar:. RunTestClass.java
javac StartSystem.java
````
Run each compiled program with the same consistency (in different terminals):
````
java StartSystem <sequential|quorum|readyourwrites>
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClass <sequential|quorum|readyourwrites>
````
e.g.
````
java StartSystem sequential
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClass sequential
````
If `StartSystem` isn't started before the tests and with the appropriate consistency argument, the tests will fail.
To test a different consistency, end StartSystem by entering anything. Start both programs again with a differnt consistency argument.

## Assumptions
(assumption for now: "In quorum consistency, read operations do not contact the central server for coordination, but write operations to contact the central server.")
