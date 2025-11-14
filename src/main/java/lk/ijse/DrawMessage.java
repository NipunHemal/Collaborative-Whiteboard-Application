package lk.ijse;


import java.io.Serializable;

/**
 * This class represents a single message sent over the network.
 * It must implement Serializable to be sent through Object Streams.
 */
public class DrawMessage implements Serializable {

    // A version ID for serialization
    private static final long serialVersionUID = 1L;

    private String type; // "DRAW" or "CLEAR"
    private double x1, y1, x2, y2;
    private String color; // Stored as a string (e.g., "#RRGGBB")
    private double lineWidth;

    // Constructor for a DRAW message
    public DrawMessage(String type, double x1, double y1, double x2, double y2, String color, double lineWidth) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.lineWidth = lineWidth;
    }

    // Constructor for a CLEAR message
    public DrawMessage(String type) {
        this.type = type;
    }

    // --- Getters ---
    public String getType() { return type; }
    public double getX1() { return x1; }
    public double getY1() { return y1; }
    public double getX2() { return x2; }
    public double getY2() { return y2; }
    public String getColor() { return color; }
    public double getLineWidth() { return lineWidth; }
}