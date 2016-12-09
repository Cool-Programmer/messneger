import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame
{
    private JTextField userText; // Area where user types message
    private JTextArea chatWindow; // Displays the whole conversation

    // Whenever you connect to someone else's computer in java, the connection is called stream
    // There are 2 main streams - input stream and output stream. Output from me to someone else's.
    // Output goes from you, input comes to you.

    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;

    // GUI's constructor
    public Server()
    {
        super("Instant Messenger");
        userText = new JTextField();
        userText.setEditable(false); // Cannot type unless connected
        userText.addActionListener( // When user types something and hits enter
                new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        sendMessage(event.getActionCommand()); // Send what the user typed into input
                        userText.setText(""); // Clear the field
                    }
                }
        );
        add(userText, BorderLayout.NORTH);

        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow)); // add
        setSize(500, 300);
        setVisible(true);
    }

    // Set up and run the server
    public void startRunning()
    {
        try {
            // Port numbers.
            // Program's port number is 7777. Backlog.
            // <= 100 people can wait
            server = new ServerSocket(7777, 100); // Setting up the server
            while (true){
                try {
                    waitForConnection(); // Wait to connect
                    setupStream(); // Setup input-output stream
                    whileChatting(); // Do the main
                }catch (EOFException eofException){ // End of the stream.
                    showMessage("\n Server ended the connection.");
                }finally {
                    closeAll();
                }
            }
        }catch (IOException ioeException){
            ioeException.printStackTrace();
        }
    }


    // Wait for connection, then display connection info
    private void waitForConnection() throws IOException
    {
        showMessage("Waiting for someone to connection... \n");
        connection = server.accept(); // Once someone asked to connect, accepts
        showMessage("Now connected to " + connection.getInetAddress().getHostName()); // Returns the sockets address ip.
    }


    // Setup the streams - pathways
    private void setupStream() throws IOException
    {
        output = new ObjectOutputStream(connection.getOutputStream()); // Creating the pathway to connect to other computer
        output.flush(); // Clean up the lefover bytes
        input = new ObjectInputStream(connection.getInputStream()); // Greating the patway to receive messages
        showMessage("\n Stream setup done. \n");
    }


    // While chatting method
    private void whileChatting() throws IOException
    {
        String message = "You are now connected and can start the conversation";
        sendMessage(message);
        ableToType(true);
        do {
            try {
                message = (String) input.readObject(); // Read what was typed. Force String
                showMessage("\n" + message);
            }catch (ClassNotFoundException classNotFoundException){
                showMessage("\n Something went wrong. \n");
            }
        }while (!message.equals("CLIENT - END"));
    }


    // Close all method
    private void closeAll()
    {
        showMessage("\n Closing connection...");
        ableToType(false);
        try {
            output.close(); // Close output stream
            input.close(); // Close input stream
            connection.close(); // Close overall connection
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }


    // Send message to client
    private void sendMessage(String message)
    {
        try {
            output.writeObject("SERVER: " + message); // Create the write object
            output.flush(); // Clear
            showMessage("\n Server: " + message); // Display
        }catch (IOException ioException){
            chatWindow.append("\n An error occured while sending message");
        }
    }


    // Show messages method
    private void showMessage(final String text)
    {
        SwingUtilities.invokeLater( // Tread to update the GUI
            new Runnable() {
                @Override
                public void run() {
                    chatWindow.append(text);
                }
            }
        );
    }


    // Able to type method
    private void ableToType(final boolean tof)
    {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userText.setEditable(tof);
                    }
                }
        );
    }


}
