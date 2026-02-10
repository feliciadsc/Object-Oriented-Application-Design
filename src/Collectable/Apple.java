package Collectable;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;

public class Apple extends Collectable {
    private BufferedImage appleImage;

    public Apple(int x, int y) {
        super(x, y);
        loadImage();
    }


    //functie de incarcare a imaginii
    @Override
    public void loadImage() {
        try {
            appleImage = ImageIO.read(new File("resources/characters_animation/apple.png"));
            if (appleImage == null) {
                System.err.println("Warning: Apple image could not be loaded");
            }
        } catch (IOException e) {
            System.err.println("Error loading apple image: " + e.getMessage());
            // Create fallback image
            appleImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = appleImage.createGraphics();
            g.setColor(Color.RED);
            g.fillOval(0, 0, 16, 16);
            g.dispose();
        }
    }

    //functie de desenare
    @Override
    public void draw(Graphics2D g2) {
        if (!isCollected && appleImage != null) {
            g2.drawImage(appleImage, x, y, null);

            if (canBeCollected) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Book Antiqua", Font.BOLD, 7));
                g2.drawString("Press E to collect", x - 11, y - 8);
            }
        }
    }

    //functie de verificare daca se poate colecta
    @Override
    public void checkDistance(int playerX, int playerY) {
        if (isCollected) return;

        int appleCenterX = x + (appleImage.getWidth()) / 2;
        int appleCenterY = y + (appleImage.getHeight()) / 2;

        double distance = Math.sqrt(Math.pow(playerX - appleCenterX, 2) + Math.pow(playerY - appleCenterY, 2));
        canBeCollected = distance <= 30;
    }
}