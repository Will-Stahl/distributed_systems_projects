javac BulletinBoardServer.java
javac BulletinBoardClient.java
cp ServerToServerInterface.class ../test
cp BulletinBoardServerInterface.class ../test
cp ReferencedTree.class ../test
cd ../test
javac -cp ./../lib/junit-4.13.2.jar:. RunTestClass.java
javac StartSystem.java
javac -cp ./../lib/junit-4.13.2.jar:. RunClientTestCases.java