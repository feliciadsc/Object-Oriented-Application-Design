// Concrete Sheep class
package Collectable;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;

public class Sheep extends Collectable {
    private BufferedImage sheepImage;

    public Sheep(int x, int y) {
        super(x, y);
        loadImage();
    }

    @Override
    public void loadImage() {
        try {
            sheepImage = ImageIO.read(new File("resources/characters_animation/sheep.png"));
        } catch (IOException e) {
            System.err.println("Error loading sheep image: " + e.getMessage());
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!isCollected && sheepImage != null) {
            g2.drawImage(sheepImage, x, y, null);

            if (canBeCollected) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Book Antiqua", Font.BOLD, 7));
                g2.drawString("Press E to collect", x - 11, y - 8);
            }
        }
    }

    @Override
    public void checkDistance(int playerX, int playerY) {
        if (isCollected) return;

        int sheepCenterX = x + sheepImage.getWidth() / 2;
        int sheepCenterY = y + sheepImage.getHeight() / 2;

        double distance = Math.sqrt(Math.pow(playerX - sheepCenterX, 2) + Math.pow(playerY - sheepCenterY, 2));
        canBeCollected = distance <= 30;
    }
}