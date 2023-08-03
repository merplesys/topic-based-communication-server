import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;

public class Client {
    private Socket clientSocket;
    private JTextArea messageTextArea;
    private JTextField inputField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            client.connect("localhost", 1234); // Connect to the server at the specified host and port
        });
    }

    public void connect(String host, int port){
        try{
            clientSocket = new Socket(host, port); //establish a connection by providing a host and port number
            System.out.println("Connected to server: " + host + ":" + port);

            JFrame frame = new JFrame("Client"); //Creation of GUI elements
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JTextArea tutorial = new JTextArea();
            tutorial.setFont(new Font("Arial", Font.PLAIN, 20));
            tutorial.setEditable(false);
            tutorial.append("Subscribe to topics using the following message format: [subscribe:(topic)] \n");
            tutorial.append("Unsubscribe to topics using the following message format: [unsubscribe:(topic)] \n");
            tutorial.append("If you include a space after the colon, the space will be included as part of the topic \n"); // Informing the user of the message formats so the server can receive them properly
            tutorial.append("\n");
            tutorial.append("Type your messages in the following format: [(topic):(message)] \n");
            JScrollPane tutorialPane = new JScrollPane(tutorial);

            messageTextArea = new JTextArea();
            messageTextArea.setFont(new Font("Arial", Font.PLAIN, 20));
            messageTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(messageTextArea);

            inputField = new JTextField();
            inputField.addActionListener(new SendMessageListener());
            inputField.setFont(new Font("Arial", Font.PLAIN, 20));

            frame.setLayout(new BorderLayout());
            frame.add(tutorialPane, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(inputField, BorderLayout.SOUTH);
            frame.setVisible(true);

            Thread receiverThread = new Thread(new MessageReceiver()); // Start a new thread to receive messages from the server
            receiverThread.start();
            }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private class SendMessageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = inputField.getText();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            message = message + "-" + timestamp.getTime();
            sendMessage(message);
            inputField.setText("");
        }
    }

    public void sendMessage(String message) {
        try {
            OutputStream outputStream = clientSocket.getOutputStream(); // Retrieve the output stream of the client socket
            outputStream.write(message.getBytes()); // Write the message content to the output stream
            outputStream.flush(); // Flush the output stream to ensure the message is sent
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MessageReceiver implements Runnable{
        @Override
        public void run(){
            try {
                InputStream inputStream = clientSocket.getInputStream(); // Retrieve the input stream of the client socket
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    String content;
                    String time;
                    Long diff = null;
                    String message = new String(buffer, 0, bytesRead); // Convert the received bytes into a string message
                    //System.out.println("Received message: " + message);
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    String[] parts = message.split("-", 2);
                    if (parts.length == 2){
                        content = parts[0];
                        time = parts[1];
                        //System.out.println(time);
                        diff = Long.parseLong(String.valueOf(timestamp.getTime())) - Long.parseLong(time);
                    } else{
                        content = message;
                    }
                    if (message.contains("You are now subscribed to the topic")){
                        messageTextArea.append("Received: " + content + " \n");
                    } else{
                        messageTextArea.append("Received: " + content + "  - " + diff + " ms \n");
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}