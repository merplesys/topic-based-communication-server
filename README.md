# topic-based-communication-server
Everything needed to run the application is in the [Project Server/src](https://github.com/merplesys/topic-based-communication-server/tree/main/Project%20Server/src) folder. The included files are the actual server and multiple copies of the client that I used for testing. Technically only Server and Client are necessary to run the program but I included the others as well. Clients 2 and 3 are exact copies of the first client just with a number at the end to differentiate them. SClient is a copy of the main client but it sends its messages several times instead of once to simulate more activity.
## Running Instructions:
1) Run Server.Java
2) Run the Clients that you want to use
3) Application is ready to use, type in the Client to send messages to the server

To subscribe to a topic, type [subscribe:topic]

To unsubscribe from a topic, type [unsubscribe:topic]

Type messages in the following format [topic:message]
