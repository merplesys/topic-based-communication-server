import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Server {
    private ServerSocket server;
    private Map<String, List<Socket>> topicSubscriptions;

    public static void main(String[] args) {
        Server server = new Server(1234);
        server.start();
    }

    public Server (int port) {
        try {
            server = new ServerSocket(port); //server is listening on the port
            topicSubscriptions = new HashMap<>(); // Initialize the map to store topic subscriptions
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Server started.");
        while(true){  //running infinite loop for getting client request
            try{
                Socket clientSocket = server.accept(); //socket object to receive incoming client requests
                System.out.println("New client connected " + clientSocket.getInetAddress().getHostAddress()); //displaying that new client is connected to server
                Thread clientThread = new Thread (new ClientHandler(clientSocket)); // Start a new thread to handle the client communication
                clientThread.start();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribeToTopic(String topic, Socket clientSocket){
        List<Socket> subscribers = topicSubscriptions.getOrDefault(topic, new ArrayList<>()); // Retrieve the list of subscribers for the topic or create a new list
        subscribers.add(clientSocket); // Add the client socket to the list of subscribers
        topicSubscriptions.put(topic, subscribers); // Update the topic subscriptions in the map
        System.out.println("Client subscribed to topic '" + topic + "'.");
        sendAcknowledgmentMessage(clientSocket, "You are now subscribed to the topic '" + topic + "'.");
    }

    public void unsubscribeFromTopic(String topic, Socket clientSocket) {
        List<Socket> subscribers = topicSubscriptions.getOrDefault(topic, new ArrayList<>()); // Retrieve the list of subscribers for the topic or create a new list
        subscribers.remove(clientSocket); // Remove the client socket from the list of subscribers
        topicSubscriptions.put(topic, subscribers); // Update the topic subscriptions in the map
        System.out.println("Client unsubscribed from topic '" + topic + "'.");
        sendAcknowledgmentMessage(clientSocket, "You are now unsubscribed from the topic '" + topic + "'.");
    }

    public void sendMessage(String topic, String message) {
        List<Socket> subscribers = topicSubscriptions.getOrDefault(topic, new ArrayList<>()); // Retrieve the list of subscribers for the topic or create a new list
        for (Socket subscriber : subscribers) {
            try {
                OutputStream outputStream = subscriber.getOutputStream(); // Retrieve the output stream of the subscriber's socket
                outputStream.write(message.getBytes()); // Write the message content to the output stream
                outputStream.flush(); // Flush the output stream to ensure the message is sent immediately
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Message sent to topic '" + topic + "': " + message);
    }

    private void sendAcknowledgmentMessage(Socket clientSocket, String message) { //method to send confirmation messages after subscription or unsubscription
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(message.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;

        //Constructor
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try{
                InputStream inputStream = clientSocket.getInputStream(); // Retrieve the input stream of the client's socket
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    String timestamp = null;
                    String message = new String(buffer, 0, bytesRead); // Convert the received bytes into a string message
                    System.out.println("Received message from client: " + message);
                    String[] remove = message.split("-", 2);
                    if (remove.length == 2){ //Split the timestamp from the message so subscribing doesn't break
                        message = remove[0];
                        timestamp = remove[1];
                    }
                    String[] parts = message.split(":", 2); // Split the message into topic and content
                    if (parts.length == 2) {
                        String topic = parts[0];
                        String content = parts[1];

                        if (topic.equals("subscribe")) {
                            subscribeToTopic(content, clientSocket); // Subscribe the client to a topic
                        } else if (topic.equals("unsubscribe")) {
                            unsubscribeFromTopic(content, clientSocket); // Unsubscribe the client from a topic
                        } else {
                            if (timestamp != "null"){
                                message = message + "-" + timestamp;
                            }
                            sendMessage(topic, message); // Send the message to all subscribers of the specified topic
                        }
                    }
                }

                System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}