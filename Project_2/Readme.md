# Bulletin Board Server
Authors: Jashwin Acharya (`achar061`), William Stahl (`stahl186`)

## Compile all code
navigate to the `src` directory. Then run our compilation script.
````
cd src
./compile_and_copy.sh
````

## Running servers

Navigate to src folder.

Start one server using the command: 
`java BulletinBoardServer <hostname> 2000 <consistency name>`
Example: `java BulletinBoardServer localhost 2000 readyourwrites`
Alternatively, run `java StartSystem` (from the testing section) in the test directory, which automatically starts 5 servers at ports 2000-2004.

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
### Assumptions/Decisions/Limitations
In order to guarantee read-your-writes consistency, we wait to return to the client until its write request has propagated through the Bulletin Board System.

### Components
#### BulletinBoardServer
An instance of this class is a remote object on which the client invokes remote methods from `BulletinBoardServerInterface`. A running instance of one of these is one of the servers in the Bulletin Board system. It also implements `ServerToServerInterface`, which defines some methods that servers can use in communication with one another. Some inter-server communication is actually from `BulletinBoardServerInterface` since there were issues when implementing them as in `ServerToServerInterface`.

#### ConsistencyStrategy
This is an interface through which the strategy pattern is implemented, allowing `BulletinBoardServer` to instantiate an object that implements the interface according to the desired consistency. `SequentialStrategy`, `QuorumStrategy`, and `ReadYourWritesStrategy` are the classes that implement this interface. Some of these class methods take the server object as an argument so that they can manipulate the server and use it for communication according to the actual consistency. It's in these classes where much of the inter-server communication takes place.

#### ReferencedTree
This is where the actual content of the bulletin board is stored. Each node of the tree is an article, and the tree structure makes for easy tracking of replies and replies to replies. An `ArrayList` also maintains direct references to each node in order to avoid searching the tree for a specific article ID (indexed by ID).

#### BulletinBoardClient


### Analysis
For the analysis, we chose to do a theoratical measure of each consistency based on the number of messages sent, from the client's request to the server's official response. We include registry queries (locating, looking up). The number of messages passed assumes the worst case (client did not contact coordinator) where the server responded after successfully completing a non-trivial request.

<img src="consistency_comp.png"  width="60%" height="60%">

Writes for any consistency require many messages to be passed. One reason for so many messages is that a loop that contacts the other 4 servers sends 3 messages inside the loop: one for registry location, one for registry lookup, and one for the actual RMI. Our quorum implementation manages to eliminate some of these as it maintains some references to replicas between method calls. By the time we realized how this could reduce message passing, our implementations were essentially complete.

Quorum consistency, as expected, suffers in the number of reads. The other two strategies simply contact one replica and get its result, but quorum consistency must contact multiple replicas.

<img src="quorum_comp.png"  width="60%" height="60%">

For quorum consistency, the number of messages sent for a read or write is a linear function of `Nr` or `Nw` respectively. Reads outperform writes since reads use existing references to replicas. If we removed the in-loop registry operations, writes would cost significanty less and be more similar to reads:

<img src="quorum_comp_adjusted.png"  width="60%" height="60%">

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
