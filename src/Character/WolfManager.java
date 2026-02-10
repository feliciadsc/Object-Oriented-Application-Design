package Character;

import GamePanel.GamePanel;

import java.awt.*;

public class WolfManager {
    private Wolf wolf1;
    public Wolf wolf2;
    private GamePanel gamePanel;

    public boolean wolf2Active = false;

    private boolean showMessage = false;
    private long messageTime = 0;

    private boolean canAttack = true;

    private int lastWolfX = 0;
    private int lastWolfY = 0;

    public WolfManager(GamePanel gp)
    {
        this.gamePanel = gp;
    }

    public void initializeWolf() {
        int GROUND_LEVEL_Y = 680;
        switch (gamePanel.currentMap.currentLevel) {
            case 1:
                wolf1 = new Wolf(600, 350, gamePanel);
                break;
            case 2:
                wolf1 = new Wolf(200, GROUND_LEVEL_Y, gamePanel);
                break;
        }

        wolf2 = null;
    }

    public void resetWolfForLevel() {
        wolf2Active = false;
        wolf2 = null;
        showMessage = false;
        initializeWolf();
    }

    public void draw(Graphics2D g2d)
    {
        if (wolf1 != null && wolf1.isAlive()) {
            wolf1.draw(g2d);
        }

        if (wolf2Active && wolf2 != null && wolf2.isAlive()) {
            wolf2.draw(g2d);
        }
    }

    public void showMessage(Graphics2D g2d)
    {
        if (showMessage) {
            long elapsed = System.currentTimeMillis() - messageTime;
            int MESSAGE_TIME = 15000;
            if (elapsed < MESSAGE_TIME) {
                String text = "I don't feel safe yet...";

                int screenX = (int) ((lastWolfX - gamePanel.camera.getX()) * gamePanel.camera.getScale());
                int screenY = (int) ((lastWolfY - gamePanel.getY()) * gamePanel.camera.getScale());

                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.setColor(Color.WHITE);

                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(text);

                int x = screenX - textWidth / 2 + 10;
                int y = screenY - 50;

                g2d.drawString(text, x, y);
            } else {
                showMessage = false;
            }
        }
    }

    public void positionWolfInBottomRightCorner()
    {
        int mapWidthPixels = gamePanel.currentMap.getWidth() * gamePanel.tileWidth;
        int mapHeightPixels = gamePanel.currentMap.getHeight() * gamePanel.tileHeight;

        int wolfX = mapWidthPixels - 80;
        int wolfY = mapHeightPixels - 150;

        wolf1.setPosition(wolfX, wolfY);
    }

    public void updateWolves() {
        if (wolf1 != null) {
            if (wolf1.isAlive()) {
                wolf1.update(gamePanel.character);
                lastWolfX = wolf1.getX();
                lastWolfY = wolf1.getY();
            } else {
                lastWolfX = wolf1.getX();
                lastWolfY = wolf1.getY();

                wolf1 = null;
                spawnWolf2AtCameraStart();

                showMessage = true;
                messageTime = System.currentTimeMillis();
            }
        }

        if (wolf2Active && wolf2 != null) {
            if (wolf2.isAlive()) {
                wolf2.update(gamePanel.character);
            } else {
                wolf2 = null;
                wolf2Active = false;
                showMessage = false;
            }
        }
    }

    private void spawnWolf2AtCameraStart() {
        if (!wolf2Active) {
            int mapHeightPixels = gamePanel.currentMap.getHeight() * gamePanel.tileHeight;
            int startX = 300;
            int startY = mapHeightPixels - 150;

            wolf2 = new Wolf(startX, startY, gamePanel);
            wolf2Active = true;
            System.out.println("Lupul 2 a apărut la (" + startX + ", " + startY + ")");
        }
    }

    private void attackWolfWithApple() {
        if (!canAttack || gamePanel.character.getAppleCount() <= 0) {
            return;
        }

        Rectangle playerBounds = gamePanel.character.getBounds();

        if (wolf1 != null && wolf1.isAlive()) {
            Rectangle wolfBounds = wolf1.getBounds();
            if (playerBounds.intersects(wolfBounds)) {
                gamePanel.character.useApple();
                wolf1.takeHit();
                System.out.println("Lupul 1 a fost lovit! Vieți rămase: " + wolf1.getHealth());

                if (!wolf1.isAlive()) {
                    System.out.println("Lupul 1 a fost înfrânt!");

                    lastWolfX = wolf1.getX();
                    lastWolfY = wolf1.getY();

                    showMessage = true;
                    messageTime = System.currentTimeMillis();

                    spawnWolf2AtCameraStart();

                    wolf1 = null;
                }
                canAttack = false;
                return;
            }
        }

        if (wolf2Active && wolf2 != null && wolf2.isAlive()) {
            Rectangle wolf2Bounds = wolf2.getBounds();
            if (playerBounds.intersects(wolf2Bounds)) {
                gamePanel.character.useApple();
                wolf2.takeHit();
                System.out.println("Lupul 2 a fost lovit! Vieți rămase: " + wolf2.getHealth());

                if (!wolf2.isAlive()) {
                    System.out.println("Lupul 2 a fost înfrânt!");
                    wolf2 = null;
                    wolf2Active = false;
                    showMessage = false;
                }
                canAttack = false;
            }
        }
    }

    public void update()
    {
        if (gamePanel.keyH.attack) {
            attackWolfWithApple();
        } else {
            canAttack = true;
        }
    }

    public boolean canFillBucket() {
        if (gamePanel.currentMap.currentLevel == 2) {
            boolean wolf1Dead = (wolf1 == null || !wolf1.isAlive());
            boolean wolf2Alive = (wolf2Active && wolf2 != null && wolf2.isAlive());

            if (wolf1Dead && (wolf2Alive || showMessage)) {
                return false;
            }

            return wolf1Dead && !wolf2Alive && !showMessage;
        }
        return true;
    }



}
