package Collectable;

import GamePanel.GamePanel;
import GameWindow.GameWindow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Bucket {
    private int x;
    private int y;
    public boolean isFilled = false;  // initial galeata este goală
    private BufferedImage sprite;      // Sprite-ul folosit pentru desenare
    private final int width = 16, height = 16;  // Dimensiunile sprite-ului
    private GamePanel gamePanel;

    // Variabile pentru mesajul de avertizare
    private boolean showWarningMessage = false;
    private long warningMessageTime = 0;
    private final int WARNING_MESSAGE_DURATION = 3000; // 3 secunde

    public Bucket(int x, int y, GamePanel gp) {
        this.x = x;
        this.y = y;
        loadSprite();  //  imaginea sprite-ului
        this.gamePanel = gp;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    //  sprite-ul galetii (care contine ambele frame-uri)
    private void loadSprite() {
        try {
            BufferedImage rawImage = ImageIO.read(getClass().getResource("/characters_animation/galeata.png"));

            //  fundalul alb transparent
            sprite = makeColorTransparent(rawImage, Color.WHITE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage makeColorTransparent(BufferedImage image, Color color)
    {
        BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                if (new Color(pixel, true).equals(color)) {
                    transparentImage.setRGB(x, y, 0x00000000);  // Transparent
                } else {
                    transparentImage.setRGB(x, y, pixel);
                }
            }
        }
        return transparentImage;
    }

    //  starea galetii
    public void update()
    {
        // Actualizeaza mesajul de avertizare
        if (showWarningMessage) {
            long elapsed = System.currentTimeMillis() - warningMessageTime;
            if (elapsed > WARNING_MESSAGE_DURATION) {
                showWarningMessage = false;
            }
        }

        if (gamePanel.keyH.fillBucketPressed) {
            tryToFillBucket();
        }
    }

    // Incearcă sa umple galeata, verificand mai intai daca este permis
    public void tryToFillBucket()
    {
        // Verifica daca poate umple galeata (ambii lupi trebuie sa fie morti)
        if (!gamePanel.wolfManager.canFillBucket()) {
            // Afisează mesaj de avertizare
            showWarningMessage = true;
            warningMessageTime = System.currentTimeMillis();
            System.out.println("Nu poți umple galeata încă! Trebuie să învingi toți lupii!");
            return; // Nu permite umplerea
        }

        int playerX = gamePanel.character.getX();
        int playerY = gamePanel.character.getY();

        double distance = Math.sqrt(Math.pow(playerX - this.x, 2) + Math.pow(playerY - this.y, 2));

        if (distance > 50) {
            return;
        }

        // Daca poate umple galeata, continua cu logica normală
        fillBucket();
    }

    // Schimba starea galetii
    public void fillBucket()
    {
        if (!isFilled) {
            isFilled = true;
            System.out.println("Găleata a fost umplută!");
        }
    }

    // Deseneaza galeata pe ecran
    public void draw(Graphics2D g2d) {
        if(gamePanel.currentMap.currentLevel == 2)
        {
            // Dimensiunea fiecarui frame
            int frameWidth = 16;
            int frameHeight = 16;

            // Folosim primul frame pentru galeata goala (0) si al doilea frame pentru galeata umpluta (1)
            int frameIndex = isFilled ? 1 : 0;

            //  imaginea corespunzatoare starii galetii
            g2d.drawImage(sprite.getSubimage(frameIndex * frameWidth, 0, frameWidth, frameHeight),
                    x, y, width, height, null);

            // Deseneaza mesajul de avertizare daca este necesar
            if (showWarningMessage) {
                drawWarningMessage(g2d);
            }
        }
    }

    // Deseneaza mesajul de avertizare
    private void drawWarningMessage(Graphics2D g2d) {
        String message = "I don't feel safe yet...";

        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        g2d.setColor(Color.RED);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(message);

        // Pozitioneaza mesajul deasupra galetii
        int messageX = x - (textWidth - width) / 2;
        int messageY = y - 20;

        g2d.setColor(Color.RED);
        g2d.drawString(message, messageX, messageY);
    }

    public void positionBucketsInBottomRightCorner()
    {
        int mapWidthPixels = gamePanel.currentMap.getWidth() * gamePanel.tileWidth;
        int mapHeightPixels = gamePanel.currentMap.getHeight() * gamePanel.tileHeight;

        int bucketX = mapWidthPixels - 60;
        int bucketY = mapHeightPixels - 90;

        setPosition(bucketX, bucketY);
    }
}