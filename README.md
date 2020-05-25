# xfs
The aim of the project was to build a “serverless” file system based on xFS paper, 
wherein the peer node can share their files directly with other peers without involving the server.

The project constists of two parts, the clients(which also acts as a server for other peers) and the tracking server. 

##
### Client

* The Client-side consists of 4 major files: **Client.java, ClientHelper.java, ClientThread.java, ClientToClientResponder.java**. 
All these files help in managing the connection between Client and TrackingServer, and Client to other Clients.
* Client.java has the functionality for the client to connect with the Server/Client to carry out several functionalities that include, 
1. Find(ing) a file, which creates a connection to the Server to get the list of peers that have the file,
2. Download(ing) a file, which connects to the ideal peer using the load and latency algorithm.
* We are assuming that there are no updates to a file once it enters a peer, that is there is no update to the contents of the file nor is there deletion of the file. 

### Tracking Server 

* The Server side code consists of **TrackingServer.java, ServerHelper.java, ServerHealth.java, ServerToClientResponder.java**. 
* The server does not have a lot of functionality apart from managing the list of files each peer has, which will be presented when the client tries to find a file using the ServerToClientResponder class for effective communication between the Server and Client.

##
### How to find the ideal peer
* Latency.properties file has the latency values in milliseconds, which is the expected latency between two clients when they are communicating with each other to download a file.
* peerList.properties file has the list of peers that are present which is used by the server to populate the list of peers for gathering file information.
* Load is the number of requests processed by the peer.
* ***Min(Load x Latency) is used to obtain the ideal peer***. 

##
### How to run
    1. make all 
      a) Creates the java classes required to run the project
    2. java TrackingServer
      a) Runs the TrackingServer in port 8000 of the running system
    3. java Client 8001
    4. java Client 8002
    5. java Client 8003
    6. java Client 8004
    7. java Client 8005
    8. java Client 8006
      a) Runs 6 Clients at the port provided by the argument. 
      b) By default, use these ports. Different ports can be used to run the clients, 
         but in order to do so, subsequent changes are to be made in peerList.properties and latency.properties file. 
      c) If more clients are wished to be added, add the latency of each pair in latency.properties. 
         This is used while contacting the peer to download a file. 
    9. make clean
    
##
### Group members
* Bhaargav Sriraman (srira048)
* Soumya Agrawal (agraw184)
* Garima Mehta (mehta250)

##
Please refer to the project document if more information is needed. 

