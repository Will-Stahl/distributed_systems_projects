# Bulletin Board Server

Authors: Jashwin Acharya (`achar061`), William Stahl (`stahl186`)

## Compile all code

navigate to the `src` directory. Then run our compilation script.

```
cd src
./compile_and_copy.sh
```

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

```
java StartSystem <sequential|quorum|readyourwrites>
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClass <sequential|quorum|readyourwrites>
```

e.g.

```
java StartSystem sequential
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClass sequential
```

If `StartSystem` isn't started before the tests and with the appropriate consistency argument, the tests will fail.
To test a different consistency, end StartSystem by entering anything. Start both programs again with a different consistency argument.
NOTE: Tests may occasionally fail due to connection errors (`java.net.ConnectException: Connection refused`) If this happens, please run them again after restarting `StartSystem`.

## Design Doc

### Assumptions/Decisions/Limitations

In order to guarantee read-your-writes consistency, we wait to return to the client until its write request has propagated through the Bulletin Board System.

### Components

#### BulletinBoardServer

An instance of this class is a remote object on which the client invokes remote methods from `BulletinBoardServerInterface`. A running instance of one of these is one of the servers in the Bulletin Board system. It also implements `ServerToServerInterface`, which defines some methods that servers can use in communication with one another. Some inter-server communication is actually from `BulletinBoardServerInterface` since there were issues when implementing them as in `ServerToServerInterface`.

For our project, we decided to keep the number of servers at 5 since we did not want to burden system resources by having more than 5 servers continuously communicating with one another as well as with multiple clients. Each server internally can also have a max number of 5 clients join them at once for similar aforementioned reasons. One limitation here is that there is no way for our server to know if a client has died as the Server interfaces we have defined only return values. Thus is a client goes offline before leaving a server, then the server will not be able to update its list of currently joined clients and there could be offline client objects taking up space in the list. A simple fix for this is to simply relaunch all the active servers. All 5 servers are assigned fixed ports (valid ports are 2000, 2001, 2002, 2003 and 2004) and the central server by default always runs on the local host on port 2004.

We have also added a "Timer" class variable on line 441 in BulletinBoardServer.java that allows our other 4 servers (or replicas) to continuously ping the coordinator to ensure that it is live and running. Once the coordinate server is offline, then the other replicas automatically disconnect too since we don't have a leader election algorithm in place to assign central server duties to another server. Another reason for having the other replicas shut down when the coordinator dies is that the coordinator is the central point of contact for all our consistency strategies, which we go into more detail later in this document. We decided to invoke this Timer function every 1 second to ensure that the server's are always aware when the coordinator is down since we are continuously exchanging information between servers.

We have an additional "Timer" variable defined at line 460 in BulletinBoardServer.java that repeatedly calls our "Synch" function every 2 seconds to ensure that all servers are updated with the latest bulletin board information by fetching data from the server that contains the most recent article ID. Whenever a replica goes offline while communicating with a client and comes back online later, it is initally updated with the bulletin board server information that the coordinator holds and our "Synch" function makes sure the replica is updated again with the most latest information in-case the coordinator doesn't have the latest bulletin board information. The "Synch" function also keeps track of a list of available servers and removes server objects that are offline to ensure that we don't end up trying to Ping a server whose object is not available. We decided on a 2 second repeat time interval to ensure that the server list is regularly updated with live server objects to not cause issues later on when servers start to communicate with one another.

#### ConsistencyStrategy

This is an interface through which the strategy pattern is implemented, allowing `BulletinBoardServer` to instantiate an object that implements the interface according to the desired consistency. `SequentialStrategy`, `QuorumStrategy`, and `ReadYourWritesStrategy` are the classes that implement this interface. Some of these class methods take the server object as an argument so that they can manipulate the server and use it for communication according to the actual consistency. It's in these classes where much of the inter-server communication takes place.

#### ReferencedTree

This is where the actual content of the bulletin board is stored. Each node of the tree is an article, and the tree structure makes for easy tracking of replies and replies to replies. An `ArrayList` also maintains direct references to each node in order to avoid searching the tree for a specific article ID (indexed by ID).

#### Sequential Strategy Implementation

In our implementation for Sequential Strategy, we have defined three functions:

ServerPublish: This function is invoked from the client side when they want to Post a new article to the bulletin board or reply to an existing one. The client queries any random server from a list of servers and the server in-turn contacts the coordinator to publish the client's request (whether it is to Post or Reply). We maintain a variable called "replyTo" which stores the article ID that we want to reply to. If the article ID the user wants to reply to is 0, then we consider it a new article that should be posted on the bulletin board. If the article ID is not 0, then we consider it to be a reply to an existing article on the bulletin board. Once the coordinator has updated its central copy of the bulletin board, it RMIs the other replicas to ensure they have the up-to-date bulletin board information. If any one of the replicas is offline at the time of update, then it will be updated with the latest bulletin board information once it joins back in (as detailed earlier in the BulletinBoardServer section earlier). Also, we can have anywhere between 1-5 servers live for sequential consistency. There is no requirement that all 5 servers should be live. As long as the coordinator is live, Sequential Consistency can be performed.

ServerRead: This function simply retrieves all the articles currently posted on the bulletin board using the tree structure we have defined in the ReferencedTree class.

ServeChoose: This function returns an article of the client's choosing using its ID. If the article is not found, an appropriate error message is printed in the server and client terminal(s).

#### Read-Your-Writes Implementation

In our implementation for the Read-Your-Writes Strategy, we have defined three functions:

ServerPublish: Similar to Sequential Consistency, this function is invoked when the client wants to Post or Reply to an article. Once again, the client queries a random server (not necessarily the coordinator) and the server retrieves the primary copy of the bulletin board from the coordinator, which we assume contains all the latest bulletin board information. The server locally makes its updates and then queries the other replicas to update their respective copy of the bulletin board to ensure everyone has the latest copy, so that if a client disconnects from the first server and rejoins another server, it is guaranteed to still be able to read that article. The "replyTo" parameter serves the same function as in the SequentialStrategy class. The "Synch" function defined in BulletinBoardServer.java ensures that if any server joins later, then it will be updated with the latest bulletin board server information. One thing to note here is that if the coordinator is unable to update its primary copy, then the update is not propagated to the other replicas. As per our design, this situation can only arise if the coordinator server goes offline, and even if it goes offline, then the other replicas automatically shutdown too (explanation for this is provided in the BulletinBoardServer section above). Similar to sequential consistency, as long as the coordinator is live, the client can keep communicating with it. There is no requirement that all 5 servers have to be live at once.

ServerRead: This function simply retrieves all the articles currently posted on the bulletin board using the tree structure we have defined in the ReferencedTree class.

ServeChoose: This function returns an article of the client's choosing using its ID. If the article is not found, an appropriate error message is printed in the server and client terminal(s).

#### Quorum Implementation

In our implementation for the Quorum Strategy, we have defined three functions:

ServerPublish: Similar to Sequential and Read-Your-Writes consistency, this function posts or replies to an article on the bulletin board. The client contacts any random server and this server contacts our coordinator to initialize the random Read and Write quorums. We have variables defined for the Read and Write quorums called NR and NW respectively and the valid pairs of values for our system are (3,3), (2,4) and (1,5) where the first value corresponds to NR and second corresponds to NW. We decided that since we only have 5 servers in our system, it is important for each member of the write and read quorums to agree on their respective operations. We also mandate that all 5 servers should be online in order to partake in Quorum Consistency. Once all members of the write quorum have updated their respective bulletin boards, we update our read quorum servers using the server that overlaps between both quorums as this server contains the updated version of the bulletin board. There is however one limitation of our implementation: Suppose we have 3 servers in our write quorum and one of the servers goes down. The write operation would be successful, but only on 2 servers. The update would not be propagated to the read quorum servers as its possible that it was the overlapped server that went down. Any subsequent Read or Post/Reply operations would result in failure as the system will contain only 4 live servers and 1 offline server. Even if the "Synch" function defined in BulletinBoardServer updates the list of the latest servers, we don't form our read and write quorums until the client sends a Post or Reply request. In order to combat this, we can relaunch the server that went offline earlier and perform a Post/Reply operation from the client side. This will allow the read and write quorums to get reset and they will now all contain fresh live server objects. Also, the "Synch" operation guarantees that all servers remain up-to-date, so once the offline server joins back in, it will have the latest version of the data and the read quorum servers will also have been updated with the latest bulletin board information. It's also possible for any of the read quorum servers to go down while they are being updated and we can combat that by once again just relaunching the server and allowing the "Synch" function to update the offline server.

ServerRead: This function allows the overlapped server between both read and write replicas to update the other read quorum servers with the latest information. We mandate that all members of the read quorum have to return the exact same value of the article to ensure that we always have the latest copy of the bulletin board articles for the client.

ServeChoose: The code for this function is very similar to the ServerRead function. Every server in the read quorum searches through their reference trees for their individual copy of the article whose ID is being requested, and return the article if all their read results match.

#### BulletinBoardClient

This class defines the UI for the client to make Join, Leave, Post, Reply, Read and Choose messages to the server. We maintain a list of the servers that the client is a part of and update it everytime we perform any of the aforementioned operations to ensure that any offline server objects are removed from the list. Unlike project 1, we decided to only ping servers when the client performs a Post, Reply, Read and Choose request. We noticed that if we did it repeatedly and tried to update the server list (called "joinedServers" in the file), then we would occasionally run into errors and conflicting changes to the list. Hence we decided to only Ping servers and update the server list only when its necessary. If the client tries to join or leave any offline servers, then the appropriate error message is printed in the client terminal. There is no restriction as to what servers the client can join. They can connect, isconnect and reconnect with any server of their choosing. We have also defined various validation functions that validate join, leave, post, reply, read and choose commands that the client enters in the terminal. We decided to keep our Article format as "Article Title;Article Contents" as it is easy to understand and simple to use. We have included various print statements that highlight any possible failures such as attempting to join/leave servers that are offline or attempting to reply to or read or choose articles that don't exist. We have also included print statements for acknowledging server responses in case the server failed or was successful in posting articles, replies, returning list of articles, or returning an article of the clients choosing.

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
