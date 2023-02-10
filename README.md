# CSCI 5105 Project 1: Publish-Subscribe RMI Service
### Authors: Jashwin Acharya (achar061), Al Yaqdhan Al Maawali (almaa009), William Stahl (stahl186)

## Run Tests
You will need 3 separate terminals. In one, run
````
rmiregistry &
````
In another, navigate to src from the project root and use the script that sompiles and starts several differently named servers.
````
./start-servers.sh
````
In the 3rd terminal, run the tests with
````
javac -cp ./../lib/junit-4.13.2.jar:. RunTestClasses.java
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClasses
````
