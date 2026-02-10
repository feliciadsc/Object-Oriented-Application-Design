package Camera;

import java.awt.*;
import java.awt.geom.AffineTransform;
import Character.Character;
import Maps.Map;

public class Camera {
    public double x;            // X coordinate of camera
    public double y;            // Y coordinate of camera
    private double scale;

    private int windowWidth;
    private int windowHeight;
    private int mapWidth;
    private int mapHeight;

    // Constructor
    public Camera(int windowWidth, int windowHeight, int mapWidth, int mapHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        // Initializare colt jos-stanga
        this.x = 0;
        this.scale = 2.5;
        this.y = mapHeight - windowHeight / scale;
    }

    // Camera trebuie sa urmeze jucatorul
    public void update(Character player) {
        // Pozitia tinta pe baza pozitiei jucatorului
        double targetX = player.getX() - windowWidth / (2 * scale);
        double targetY = player.getY() - windowHeight / (2 * scale);

        // Camera ramane in limitele ferestrei
        double effectiveWindowWidth = windowWidth / 2;
        double effectiveWindowHeight = windowHeight / 2;

        // Limitam miscarea pe axa X
        if (targetX < 0) {
            targetX = 0;
        }
        if (targetX > mapWidth - effectiveWindowWidth) {
            targetX = mapWidth - effectiveWindowWidth;
        }

        // Limitam miscarea pe axa Y
        if (targetY < 0) {
            targetY = 0;
        }
        if (targetY > mapHeight - effectiveWindowHeight) {
            targetY = mapHeight - effectiveWindowHeight;
        }

        // Actualizam pozitia camerei
        this.x = targetX;
        this.y = targetY;
    }

    public void updateLoc(Map currentMap)
    {
        this.mapWidth = currentMap.getWidth() * 16;
        this.mapHeight = currentMap.getHeight() * 16;

        this.x = 0;
        this.scale = 2.5;
        this.y = mapHeight - windowHeight / scale;
    }

    // Aplica transformarea camerei asupra contextului grafic (zoom + deplasare)
    public void apply(Graphics2D g2d) {
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);       // Aplica zoom-ul
        transform.translate(-x, -y);         // Aplica deplasarea pe axele X și Y
        g2d.setTransform(transform);
    }



    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getScale() {
        return scale;
    }
}
