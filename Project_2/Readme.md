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
For the analysis, we chose to do a theoratical measure of each consistency based on the number of messages sent, from the client's request to the server's official response. We include registry queries (locating, looking up). The number of messages passed assumes the worst case (client did not contact coordinator) where the server responded after successfully completing a non-trivial request.

![Consistency Comparisons](consistency_comp.png)
![Quorum comparisons for different Nr, Nw](quorum_comp.png)

## Testing Description
One test class exists for each consistency. Each of these classes has a test for basic operations between a client and one server. Expected output is tested for
1. Read() with published content
2. Choose() for nonexistant ID
3. Reply() to a nonexistant ID
4. Post()
5. (1), (2), and (3) where the items exist
6. Expected format for all the above

Each of these classes also each has a test where a client switches which server it interacts with multiple times. In the sequential test, some articles are posted, and a client checks that they are in the same order of reading across multiple servers. The quorum test posts some articles and ensures they can be read from all servers. Lastly, the read-your-writes test ends up very similar to the quorum tests in that, as a client, it writes to a server and checks that it can read those writes from other servers.

The client command line validation functions are thoroughly tested as well in `ClientTestCases.java'. Valid and invalid input are fed into these tests where they are correcly valided and can eliminate whitespace.
