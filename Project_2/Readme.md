## Running servers

Navigate to src folder.

Compile server using the command: 
`javac BulletinBoardServer.java`

Run server using the command: 
`java BulletinBoardServer 2000 <consistency name>`

## Running clients
Compile client using the command: 
`javac BulletinBoardClient.java`

Run client using the command: 
`java BulletinBoardServer localhost`

When prompted to enter a command, type "join: 2000" for example to join a server running on port 2000. 

The port number used above can be 2000, 2001, 2002, 2003 and 2004. Any other port number will result in the program terminating gracefully.
