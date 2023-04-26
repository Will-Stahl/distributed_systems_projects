# Project 3: Simple xFS

## Authors: Jashwin Acharya (`achar061`), William Stahl (`stahl186`)

## Running Server and clients

Navigate to the src folder.

Open one terminal window in this folder and compile Tracker.java using the following command:

```
javac Tracker.java
```

You can start the server using the following command:

```
java Tracker
```

Example image below:

<img src="images/run_server.png"  width="60%" height="60%">

## Running clients and using client UI

Open a separate terminal window (apart from the server one) and compile PeerNode.java using the command:

```
javac PeerNode.java
```

In the same terminal window, use the following command:

```
java PeerNode localhost 0
```

Example image below:

<img src="images/run_client.png"  width="60%" height="60%">

The above command has the format `java PeerNode <server host name> <machID>`. You can only assign a value of 0, 1, 2, 3 or 4 to the machID. Any other machID produces an error and you will have to run the command again using a valid machID.

Example image below of using an incorrect machID:

<img src="images/run_client_error.png"  width="60%" height="60%">

### Joining the tracker server

The peers automatically join the tracking server when they are first launched, but in the event that the server goes down and the client wants to join again, they can simply enter `join` in the client UI.

Example image below:

<img src="images/join_tracker.png"  width="60%" height="60%">

A peer cannot join the tracker server if it's already part of it.

Example image below:

<img src="images/join_again_error.png"  width="60%" height="60%">

Every peer needs to have a different machID before attempting to join the server. Launching two peers with the same machID causes the peer that was launched at a later time to exit gracefully.

Example image below:

<img src="images/join_machID_error.png"  width="60%" height="60%">

### Leaving the tracker server

A client can leave the tracker server at any time by entering `leave` in their respective client UI terminal.

Example image below:

<img src="images/leave_success.png"  width="60%" height="60%">

A client cannot leave the tracker server if it was never part of it.

Example image below:

<img src="images/leave_error.png"  width="60%" height="60%">

## Running Tests
Navigate to the test directory from root with
```
cd test
```
Compile and then run with
````
javac -cp ./../lib/junit-4.13.2.jar:. RunTests.java
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTests
````
Note that the tests manipulate the `files` directory, so anything done in the command line interface may be changed. It is also assumed that files with names corresponding to their respective directories are always present, so do not delete them.
