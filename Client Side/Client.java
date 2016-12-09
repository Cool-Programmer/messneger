import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Client extends JFrame
{
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP; //IP of server, the person we are talking to
    private Socket connection;

    // Constructor
    public Client(String host)
    {
        super("Client's Instant Messenger");
        serverIP = host;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        sendMessage(event.getActionCommand()); // Stuff to send
                        userText.setText(""); // Clean
                    }
                }
        );
        add(userText, BorderLayout.NORTH);

        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(500, 300);
        setVisible(true);
    }

    // Start Running
    public void startRunning()
    {
        try {
            connectToServer(); // Specific server
            setupStreams();
            whileChatting();
        }catch (EOFException eofException){
            showMessage("\n The client terminated connection.");
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally {
            closeAll();
        }
    }


    // Connect to server
    private void connectToServer() throws IOException
    {
        showMessage("Attempting connection...");
        connection = new Socket(InetAddress.getByName(serverIP), 7777); // Ip and the port
        showMessage("Connected to: " + connection.getInetAddress().getHostName()); // Show
    }


    // Setup streams
    private void setupStreams() throws IOException
    {
        output = new ObjectOutputStream(connection.getOutputStream()); // Client-server stream
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are ready. \n");
    }


    // While chatting with server
    private void whileChatting() throws IOException
    {
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n"+message);
            }catch (ClassNotFoundException classNotFoundException){
                showMessage("\n Something went wrong... \n");
            }
        }while (!message.equals("SERVER: END"));
    }


    // Close everything
    private void closeAll()
    {
        showMessage("\n Closing connection... \n");
        ableToType(false);
        try {
            output.close();
            input.close();
            connection.close();
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }


    // Send client messages to server
    private void sendMessage(final String message){
        try {
            output.writeObject("CLIENT: " + message);
            output.flush();
            showMessage("\nCLIENT: " + message);
        }catch (IOException ioException){
            chatWindow.append("\n Something went wrong while sending the message.");
        }
    }


    // Show message. Update chatWindow
    private void showMessage(final String msg)
    {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        chatWindow.append(msg);
                    }
                }
        );
    }


    // Permission to type
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
