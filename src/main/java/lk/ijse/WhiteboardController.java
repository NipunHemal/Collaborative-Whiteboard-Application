package lk.ijse;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class WhiteboardController {

    @FXML private Canvas canvas;
    @FXML private ColorPicker colorPicker;
    @FXML private Slider brushSizeSlider;
    @FXML private TextField usernameField;
    @FXML private Label statusLabel;

    private GraphicsContext gc;
    private double prevX, prevY;

    // Network fields
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private Thread networkListenerThread;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();

        // Set default values
        gc.setLineWidth(3);
        gc.setStroke(Color.BLACK);
        colorPicker.setValue(Color.BLACK);
        brushSizeSlider.setValue(3);

        // Connect to the server
        connectToServer("localhost", 5100);
    }

    private void connectToServer(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            setStatus("Status: Connected");

            // Start the background thread to listen for messages
            networkListenerThread = new Thread(this::listenForServerMessages);
            networkListenerThread.setDaemon(true); // Close with app
            networkListenerThread.start();

        } catch (IOException e) {
            setStatus("Status: Failed to connect");
            e.printStackTrace();
        }
    }

    private void listenForServerMessages() {
        try {
            while (true) {
                // Wait for and read a message from the server
                DrawMessage message = (DrawMessage) in.readObject();

                // Check the message type
                if (message.getType().equals("DRAW")) {
                    drawRemoteStroke(message.getX1(), message.getY1(), message.getX2(), message.getY2(),
                            message.getColor(), message.getLineWidth());
                } else if (message.getType().equals("CLEAR")) {
                    clearRemoteCanvas();
                }
            }
        } catch (EOFException | SocketException e) {
            setStatus("Status: Disconnected from server");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            setStatus("Status: Connection error");
        }
    }

    @FXML
    private void onCanvasMousePressed(MouseEvent e) {
        prevX = e.getX();
        prevY = e.getY();

        // Draw a dot locally
        double size = brushSizeSlider.getValue();
        Color color = colorPicker.getValue();
        gc.setLineWidth(size);
        gc.setStroke(color);
        gc.beginPath();
        gc.moveTo(prevX, prevY);
        gc.lineTo(prevX, prevY); // A line of zero length is a dot
        gc.stroke();

        // Send a "dot" draw message
        sendDrawMessage(prevX, prevY, prevX, prevY);
    }

    @FXML
    private void onCanvasMouseDragged(MouseEvent e) {
        double currentX = e.getX();
        double currentY = e.getY();

        // Draw the line locally
        double size = brushSizeSlider.getValue();
        Color color = colorPicker.getValue();
        gc.setLineWidth(size);
        gc.setStroke(color);
        gc.beginPath();
        gc.moveTo(prevX, prevY);
        gc.lineTo(currentX, currentY);
        gc.stroke();

        // Send the line segment to the server
        sendDrawMessage(prevX, prevY, currentX, currentY);

        // Update previous coordinates
        prevX = currentX;
        prevY = currentY;
    }

    @FXML
    private void onClearButtonAction() {
        // Clear the local canvas
        clearLocalCanvas();

        // Send a "CLEAR" command to the server
        try {
            if (out != null) {
                out.writeObject(new DrawMessage("CLEAR"));
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to send a draw message
    private void sendDrawMessage(double x1, double y1, double x2, double y2) {
        try {
            if (out != null) {
                // Convert Color to a web-format string (e.g., "#RRGGBB")
                String colorString = colorPicker.getValue().toString();
                double lineWidth = brushSizeSlider.getValue();

                DrawMessage msg = new DrawMessage("DRAW", x1, y1, x2, y2, colorString, lineWidth);
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Methods called by the network thread ---

    public void drawRemoteStroke(double x1, double y1, double x2, double y2, String color, double lineWidth) {
        // IMPORTANT: UI updates MUST happen on the JavaFX Application Thread.
        Platform.runLater(() -> {
            gc.setStroke(Color.web(color)); // Use Color.web() to parse color string
            gc.setLineWidth(lineWidth);
            gc.beginPath();
            gc.moveTo(x1, y1);
            gc.lineTo(x2, y2);
            gc.stroke();
        });
    }

    public void clearRemoteCanvas() {
        Platform.runLater(this::clearLocalCanvas);
    }

    public void setStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    // --- Local Helper Methods ---

    private void clearLocalCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void shutdown() {
        if (networkListenerThread != null) {
            networkListenerThread.interrupt(); // Stop the listener thread
        }
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}