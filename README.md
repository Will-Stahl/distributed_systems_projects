# CSCI 5105 Project 1: Publish-Subscribe RMI Service
### Authors: Jashwin Acharya (achar061), Al Yaqdhan Al Maawali (almaa009), William Stahl (stahl186)

## Run Tests
You will need 3 separate terminals. In one navigate to src from the project root
````
cd src
````
and then run
````
rmiregistry &
````
In another, navigate to src from the project root
````
cd src
````
and use the script that compiles and starts several differently named servers.
````
./start-servers.sh
````
In the 3rd terminal, navigate to the tests directory from the root
````
cd tests
````
and run the tests with
````
javac -cp ./../lib/junit-4.13.2.jar:. RunTestClasses.java
java -cp ./../lib/junit-4.13.2.jar:./../lib/hamcrest-core-1.3.jar:. RunTestClasses
````
In order to re-run the tests, you must first kill the process started by start-servers. Use
````
pkill -15 java
````
This will also kill any other running java processes you have started.
