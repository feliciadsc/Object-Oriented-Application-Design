package Character;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import GamePanel.GamePanel;

public class Wolf
{
    private int x, y;
    private double speed = 2.0;
    private int health = 3;
    private boolean isAlive = true;
    private boolean attacking = false;
    long lastAttackTime = 0;

    private boolean faceRight = false;

    private Animation walkRight, walkLeft, walkUp, walkDown;
    private Animation currentAnimation, attackAnimation;

    private GamePanel gamePanel;

    // Gravity and jumping variables
    private float yVelocity;
    private boolean isJumping;
    private boolean isGrounded;

    public Wolf(int startX, int startY, GamePanel gp)
    {
        this.x = startX;
        this.y = startY;
        this.gamePanel = gp;
        loadSprites();
        initializePhysics();

        if (gp != null && gp.currentMap.currentLevel == 1) {
            this.speed = 1.5;
        }
    }

    private void initializePhysics() {
        yVelocity = 0;
        isJumping = false;
        isGrounded = false;
    }

    private void loadSprites() {
        try {
            BufferedImage walkRaw = ImageIO.read(getClass().getResource("/characters_animation/lup_sprite_walk.png"));
            BufferedImage attackRaw = ImageIO.read(getClass().getResource("/characters_animation/wolf_attack.png"));

            BufferedImage walkSpriteSheet = makeColorTransparent(walkRaw, Color.WHITE);
            BufferedImage attackSpriteSheet = makeColorTransparent(attackRaw, Color.WHITE);

            BufferedImage[] rightFrames = new BufferedImage[4];
            BufferedImage[] leftFrames = new BufferedImage[4];
            BufferedImage[] upFrames = new BufferedImage[4];
            BufferedImage[] downFrames = new BufferedImage[4];

            for (int i = 0; i < 4; i++)
            {
                BufferedImage frame = walkSpriteSheet.getSubimage(i * 40, 0, 39, 32);
                rightFrames[i] = frame;
                leftFrames[i] = frame;
                upFrames[i] = frame;
                downFrames[i] = frame;
            }

            walkRight = new Animation(rightFrames, 10);
            walkLeft = new Animation(leftFrames, 10);
            walkUp = new Animation(upFrames, 10);
            walkDown = new Animation(downFrames, 10);

            BufferedImage[] attackFrames = new BufferedImage[4];
            for (int i = 0; i < 4; i++)
            {
                attackFrames[i] = attackSpriteSheet.getSubimage(i * 48, 0, 48, 32);
            }
            attackAnimation = new Animation(attackFrames, 8);

            currentAnimation = walkLeft;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage makeColorTransparent(BufferedImage image, Color color) {
        BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                if (new Color(pixel, true).equals(color)) {
                    transparentImage.setRGB(x, y, 0x00000000);
                } else {
                    transparentImage.setRGB(x, y, pixel);
                }
            }
        }
        return transparentImage;
    }

    private boolean isSolidTileAt(int tileX, int tileY) {
        if (gamePanel == null) return false;

        if (tileX >= 0 && tileX < gamePanel.currentMap.getWidth() &&
                tileY >= 0 && tileY < gamePanel.currentMap.getHeight()) {
            int tileNum = gamePanel.currentMap.getTileAt(tileX, tileY);
            return gamePanel.currentMap.tileManager.isSolidTile(tileNum);
        }
        return true;
    }

    private boolean wouldCollide(int newX, int newY) {
        if (gamePanel == null) return false;

        int tileSize = gamePanel.tileWidth;

        // verificam fiecare colt al lupului
        int left = newX;
        int right = newX + 39; // latimea lupului
        int top = newY;
        int bottom = newY + 31; // inaltime lupului

        // convertire in coordonate de dale
        int leftTile = left / tileSize;
        int rightTile = right / tileSize;
        int topTile = top / tileSize;
        int bottomTile = bottom / tileSize;

        // verificam coliziunile
        return isSolidTileAt(leftTile, topTile) ||
                isSolidTileAt(rightTile, topTile) ||
                isSolidTileAt(leftTile, bottomTile) ||
                isSolidTileAt(rightTile, bottomTile);
    }

    //gravitatia si coliziunile
    private boolean checkVerticalCollision(int nextX, int nextY) {
        if (gamePanel == null) return false;

        int bottomY = nextY + 31;
        int topY = nextY;
        int centerX = nextX + 20;

        int tileWidth = gamePanel.tileWidth;
        int tileHeight = gamePanel.tileHeight;

        int centerTileX = centerX / tileWidth;
        int bottomTileY = bottomY / tileHeight;
        int topTileY = topY / tileHeight;

        if (yVelocity >= 0) {
            if (isSolidTileAt(centerTileX, bottomTileY)) {
                y = bottomTileY * tileHeight - 32;
                return true;
            }
        } else {
            if (isSolidTileAt(centerTileX, topTileY)) {
                y = (topTileY + 1) * tileHeight;
                return true;
            }
        }

        if (nextY < 0 || nextY + 32 > gamePanel.currentMap.getHeight() * tileHeight) {
            return true;
        }

        return false;
    }

    private boolean checkHorizontalCollision(int nextX, int nextY) {
        if (gamePanel == null) return false;

        int centerX = nextX + 20;
        int centerY = nextY + 16;

        int leftX = nextX;
        int rightX = nextX + 39;

        int tileWidth = gamePanel.tileWidth;
        int tileHeight = gamePanel.tileHeight;

        int centerTileX = centerX / tileWidth;
        int centerTileY = centerY / tileHeight;
        int leftTileX = leftX / tileWidth;
        int rightTileX = rightX / tileWidth;

        if (isSolidTileAt(leftTileX, centerTileY) ||
                isSolidTileAt(centerTileX, centerTileY) ||
                isSolidTileAt(rightTileX, centerTileY)) {
            return true;
        }

        if (nextX < 0 || nextX + 39 > gamePanel.currentMap.getWidth() * tileWidth) {
            return true;
        }

        return false;
    }

    public void update(Character player) {
        if (!isAlive) return;

        int dx = player.getX() - x;
        int dy = player.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // aplicam gravitatia
        if (gamePanel != null && gamePanel.currentMap.currentLevel == 2) {
            float GRAVITY = 0.5f;
            yVelocity += GRAVITY;
            int TERMINAL_VELOCITY = 7;
            if (yVelocity > TERMINAL_VELOCITY) {
                yVelocity = TERMINAL_VELOCITY;
            }

            int yTemp = y + (int)yVelocity;

            // verificam coliziunile verticale
            if (!checkVerticalCollision(x, yTemp)) {
                y = yTemp;
                isGrounded = false;
            } else {
                if (yVelocity > 0) {
                    isGrounded = true;
                    isJumping = false;
                }
                yVelocity = 0;
            }
        }

        // ataca daca distanta e mai mica de 10 pixeli
        int ATTACK_DISTANCE = 10;
        if (distance <= ATTACK_DISTANCE) {
            currentAnimation = attackAnimation;
            attackAnimation.update();

            int ATTACK_COOLDOWN = 3000;
            if (!attacking && System.currentTimeMillis() - lastAttackTime > ATTACK_COOLDOWN) {
                attackPlayer(player);
            }
            return;
        }

        // urmareste player
        if (distance < 400) {
            // actualizare pozitie
            faceRight = dx > 0;

            // calcul miscare orizontala
            int moveSpeedX = (int) (dx > 0 ? speed : -speed);
            int newX = x + moveSpeedX;

            // adaugam abilitatea de salt
            if (gamePanel != null && gamePanel.currentMap.currentLevel == 2) {
                // sare daca are obstacol in fata
                if (Math.abs(dx) > 50 && isGrounded && wouldCollide(newX, y)) {
                    int JUMP_FORCE = -7;
                    yVelocity = JUMP_FORCE;
                    isGrounded = false;
                    isJumping = true;
                }

                if (!checkHorizontalCollision(newX, y)) {
                    x = newX;
                }
            } else {
                int moveSpeedY = (int) (dy > 0 ? speed : -speed);
                int newY = y + moveSpeedY;

                boolean canMoveToNew = !wouldCollide(newX, newY);

                if (canMoveToNew) {
                    x = newX;
                    y = newY;
                } else {
                    boolean canMoveX = !wouldCollide(newX, y);
                    boolean canMoveY = !wouldCollide(x, newY);

                    if (canMoveX) {
                        x = newX;
                    }
                    if (canMoveY) {
                        y = newY;
                    }
                }
            }

            // setam animatiile corespunzatoare
            if (Math.abs(dx) > Math.abs(dy)) {
                currentAnimation = (dx > 0) ? walkRight : walkLeft;
            } else {
                currentAnimation = (dy > 0) ? walkDown : walkUp;
            }

            currentAnimation.update();
        }
    }

    private void attackPlayer(Character player) {
        attacking = true;
        lastAttackTime = System.currentTimeMillis();
        player.takeDamage();
        attacking = false;
    }

    public void draw(Graphics2D g2) {
        if (!isAlive) return;

        BufferedImage frame = currentAnimation.getCurrentFrame();
        int width = frame.getWidth();
        int height = frame.getHeight();

        if (faceRight) {
            AffineTransform original = g2.getTransform();
            g2.translate(x + width, y);
            g2.scale(-1, 1);
            g2.drawImage(frame, 0, 0, width, height, null);
            g2.setTransform(original);
        } else {
            g2.drawImage(frame, x, y, width, height, null);
        }

        // bara de viata pentru nivelul 2
        if (gamePanel != null && gamePanel.currentMap.currentLevel == 2) {
            int healthBarWidth = width;
            int healthBarHeight = 6;
            g2.setColor(Color.RED);
            g2.fillRect(x, y - 10, healthBarWidth, healthBarHeight);

            g2.setColor(Color.GREEN);
            g2.fillRect(x, y - 10, (int) (healthBarWidth * (health / 3.0)), healthBarHeight);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 32);
    }

    public void takeHit() {
        health--;
        if (health <= 0) {
            isAlive = false;
            System.out.println("Lupul a murit.");
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getHealth() {
        return health;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;

        if (gamePanel != null && gamePanel.currentMap.currentLevel == 2) {
            yVelocity = 0;
            isGrounded = false;
            isJumping = false;
        }
    }

}