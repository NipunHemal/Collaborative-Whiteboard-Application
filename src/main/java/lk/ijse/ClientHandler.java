package lk.ijse;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {

    private Socket socket;
    private Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Set up streams
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Continuously listen for messages from the client
            while (true) {
                try {
                    // Read a message object
                    DrawMessage message = (DrawMessage) in.readObject();

                    // Log it and broadcast it
                    if (message.getType().equals("DRAW")) {
                        System.out.println("Received DRAW message");
                    } else if (message.getType().equals("CLEAR")) {
                        System.out.println("Received CLEAR message");
                    }

                    server.broadcast(message, this);

                } catch (ClassNotFoundException e) {
                    System.err.println("Unknown object received: " + e.getMessage());
                }
            }
        } catch (EOFException | SocketException e) {
            // Client disconnected (this is a normal way for the loop to end)
            System.out.println("Client connection lost.");
        } catch (IOException e) {
            System.err.println("Error in client handler: " + e.getMessage());
        } finally {
            // Clean up: remove client from server list and close resources
            server.removeClient(this);
            closeResources();
        }
    }

    /**
     * Sends a DrawMessage to this specific client.
     */
    public void sendMessage(DrawMessage message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            // This might happen if the client disconnected abruptly
            System.err.println("Error sending message to client: " + e.getMessage());
        }
    }

    /**
     * Helper method to close streams and socket.
     */
    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}