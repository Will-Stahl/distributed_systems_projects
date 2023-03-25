# Bulletin Board Server
Authors: Jashwin Acharya (`achar061`), William Stahl (`stahl186`)

## Compile all code
navigate to the `src` directory. Then run our compilation script.
````
cd src
./compile_and_copy
````

## Running servers

Navigate to src folder.

Run server using the command: 
`java BulletinBoardServer <hostname> 2000 <consistency name>`
Example: `java BulletinBoardServer localhost 2000 readyourwrites`

## Running clients

Run client using the command: 
`java BulletinBoardClient <hostname>`
Example: `java BulletinBoardClient localhost`

When prompted to enter a command, type "join: 2000" for example to join a server running on port 2000. 

The port number used above can be 2000, 2001, 2002, 2003 and 2004. Any other port number will result in the program terminating gracefully.

## Running Client Side Tests
Navigate to `test` folder with `cd test` from root.
Open a terminal window in the `test` folder.

Run the command below to execute the Client Side tests:
```
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunClientTestCases
```


## Running the Server tests
Navigate to `test` folder with `cd test` from root.
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
To test a different consistency, end StartSystem by entering anything. Start both programs again with a different consistency argument.
NOTE: Tests may occasionally fail due to connection errors (`java.net.ConnectException: Connection refused`) If this happens, please run them again after restarting `StartSystem`.

## Design Doc
### Assumptions/Decisions
In order to guarantee read-your-writes consistency, we wait to return to the client until its write request has propagated through the Bulletin Board System.

### Components

### Analysis
For the analysis, we chose to do a theoratical measure of each consistency based on the number of messages sent, from the client's request to the server's official response. We include registry queries (locating, looking up), and we include the best and worst cases of successful client requests (the number of messages passed can depend on whether the client contacts the coordinator or not).


## Testing Description
