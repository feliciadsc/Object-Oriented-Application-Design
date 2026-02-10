package Character;
import GamePanel.GamePanel;
import java.awt.AlphaComposite;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FinalBoss {
    private int x, y;
    private double speed = 1;
    private double health = 15.0; // vieti mai multe pentru dificultate crescuta
    private boolean isAlive = true;
    private boolean isAttacking = false;
    private boolean invincible = false;
    private long lastAttackTime = 0;
    private long invincibleStartTime = 0;

    private float yVelocity = 0.0f;
    private boolean isJumping = false;
    private boolean isGrounded = false;

    private Animation walkAnimation;
    private Animation attackAnimation;
    private Animation currentAnimation;

    private BufferedImage spriteSheetWalk;
    private BufferedImage spriteSheetAttack;

    private boolean isDying = false;
    private long deathStartTime = 0;
    private final int deathDuration = 1500; // în milisecunde

    private float deathAlpha = 1.0f; // transparenta initiala completa

    private GamePanel gp;

    private boolean faceRight = true;

    private final double SCALE_FACTOR = 1.5; // face sprite-ul de 1.5x mai mare

    public FinalBoss(int startX, int startY, GamePanel gp) {
        this.x = startX;
        this.y = startY;
        this.gp = gp;
        loadSprites();
    }

    private void loadSprites() {
        try {
            BufferedImage walkRaw = ImageIO.read(getClass().getResource("/characters_animation/inamic_sprite.png"));
            BufferedImage attackRaw = ImageIO.read(getClass().getResource("/characters_animation/inamic_atac.png"));

            spriteSheetWalk = makeColorTransparent(walkRaw, Color.WHITE);
            spriteSheetAttack = makeColorTransparent(attackRaw, Color.WHITE);

            BufferedImage[] walkFrames = new BufferedImage[4];
            BufferedImage[] attackFrames = new BufferedImage[4];

            for (int i = 0; i < 4; i++) {
                walkFrames[i] = spriteSheetWalk.getSubimage(i * 32, 0, 32, 32);
                attackFrames[i] = spriteSheetAttack.getSubimage(i * 32, 0, 32, 32);
            }

            walkAnimation = new Animation(walkFrames, 12);
            attackAnimation = new Animation(attackFrames, 10);
            currentAnimation = walkAnimation;

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
        if (gp == null) return false;

        if (tileX >= 0 && tileX < gp.currentMap.getWidth() &&
                tileY >= 0 && tileY < gp.currentMap.getHeight()) {
            int tileNum = gp.currentMap.getTileAt(tileX, tileY);
            return gp.currentMap.tileManager.isSolidTile(tileNum);
        }
        return true;
    }

    private boolean checkVerticalCollision(int nextX, int nextY) {
        if (gp == null) return false;

        int bottomY = nextY + (int)(32 * SCALE_FACTOR);
        int topY = nextY;
        int centerX = nextX + (int)(16 * SCALE_FACTOR);

        int tileWidth = gp.tileWidth;
        int tileHeight = gp.tileHeight;

        int centerTileX = centerX / tileWidth;
        int bottomTileY = bottomY / tileHeight;
        int topTileY = topY / tileHeight;

        if (yVelocity >= 0) {
            if (isSolidTileAt(centerTileX, bottomTileY)) {
                y = bottomTileY * tileHeight - (int)(32 * SCALE_FACTOR);
                return true;
            }
        } else {
            if (isSolidTileAt(centerTileX, topTileY)) {
                y = (topTileY + 1) * tileHeight;
                return true;
            }
        }

        if (nextY < 0 || nextY + (int)(32 * SCALE_FACTOR) > gp.currentMap.getHeight() * tileHeight) {
            return true;
        }

        return false;
    }

    private void applyGravity() {
        float GRAVITY = 0.5f;
        yVelocity += GRAVITY;
        int TERMINAL_VELOCITY = 7;
        if (yVelocity > TERMINAL_VELOCITY) {
            yVelocity = TERMINAL_VELOCITY;
        }

        int yTemp = y + (int)yVelocity;

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

    public void update(Character player) {

        if (isDying) {
            applyGravity();
            long now = System.currentTimeMillis();
            float progress = (now - deathStartTime) / (float) deathDuration;
            deathAlpha = Math.max(0f, 1f - progress); // scade alpha până la 0

            if (progress >= 1f) {
                isAlive = false;
                System.out.println(" Final Boss a fost eliminat complet!");
            }
            return;
        }

        if (!isAlive)
            return;

        applyGravity();

        // Dezactiveaza invincibilitatea dupa 1 sec
        if (invincible && System.currentTimeMillis() - invincibleStartTime > 1000) {
            invincible = false;
        }

        //  Boost de viteza daca boss-ul e rănit
        if (health <= 3.0 && speed < 2.0) {
            speed = 2.0;
            System.out.println("Boss-ul devine  mai rapid.");
        }

        int dx = player.getX() - x;
        faceRight = dx > 0; // se uita spre dreapta daca jucatorul e la dreapta

        int dy = player.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= 30) {
            isAttacking = true;
            currentAnimation = attackAnimation;
            attackAnimation.update();

            long now = System.currentTimeMillis();
            if (now - lastAttackTime > 5000) {
                player.takeDamage();

                // Knockback simplu
                int knockbackX = dx > 0 ? 2 : -2;
                int knockbackY = dy > 0 ? 2 : -2;
                int strength = 20;

                player.applyKnockback(knockbackX, knockbackY, strength);

                lastAttackTime = now;
            }

        } else {
            isAttacking = false;
            moveTowardPlayer(dx, dy);
            walkAnimation.update();
            currentAnimation = walkAnimation;
        }
    }

    private void moveTowardPlayer(int dx, int dy) {
        if (Math.abs(dx) > 5) x += (dx > 0 ? speed : -speed);
    }

    public void draw(Graphics2D g2, Character player) {
        if (!isAlive && !isDying)
            return;

        BufferedImage frame = currentAnimation.getCurrentFrame();

        Composite originalComposite = g2.getComposite();
        if (isDying) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, deathAlpha));
        }

        // calcul dimensiuni scalate
        int scaledWidth = (int) (frame.getWidth() * SCALE_FACTOR);
        int scaledHeight = (int) (frame.getHeight() * SCALE_FACTOR);

        // Desen inamic final
        if (faceRight) {
            Graphics2D g2Copy = (Graphics2D) g2.create();
            g2Copy.translate(x + scaledWidth, y);
            g2Copy.scale(-1, 1);
            g2Copy.drawImage(frame, 0, 0, scaledWidth, scaledHeight, null);
            g2Copy.dispose();
        } else {
            g2.drawImage(frame, x, y, scaledWidth, scaledHeight, null);
        }

        g2.setComposite(originalComposite);

        // Bara de viata
        int barWidth = 60; // mai mare pentru boss-ul marit
        int barHeight = 8;
        int offsetY = 15;
        double hpPercent = health / 15.0;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, y - offsetY, barWidth, barHeight);
        g2.setColor(Color.RED);
        g2.fillRect(x, y - offsetY, (int)(barWidth * hpPercent), barHeight);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y - offsetY, barWidth, barHeight);

        // Afiseaza mesajul daca jucatorul e aproape
        Rectangle bossBounds = getBounds();
        Rectangle playerBounds = player.getBounds();

        if (bossBounds.intersects(playerBounds)) {
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(Color.YELLOW);
            g2.drawString("Press K to attack the boss", x, y - 25);
        }
    }

    public Rectangle getBounds() {
        BufferedImage frame = currentAnimation.getCurrentFrame();
        return new Rectangle(x, y, (int) (frame.getWidth() * SCALE_FACTOR), (int) (frame.getHeight() * SCALE_FACTOR));
    }

    public void takeDamage(double dmg) {
        if (invincible || !isAlive) return;

        double actualDamage = dmg * 0.5;
        health -= actualDamage;
        invincible = true;
        invincibleStartTime = System.currentTimeMillis();

        System.out.println("Boss a fost lovit! HP rămas: " + health);

        if (health <= 0 && !isDying) {
            isDying = true;
            deathStartTime = System.currentTimeMillis();
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setHealth(int lives) {
        this.health = lives;
    }

    public void positionInBottomRightCorner() {
        if (gp != null && gp.currentMap != null) {
            int mapWidthInPixels = gp.currentMap.getWidth() * gp.tileWidth;
            int mapHeightInPixels = gp.currentMap.getHeight() * gp.tileHeight;

            int spriteWidth = (int) (32 * SCALE_FACTOR);
            int spriteHeight = (int) (32 * SCALE_FACTOR);

            this.x = mapWidthInPixels - spriteWidth - 100;
            this.y = mapHeightInPixels - spriteHeight - 100;

            // setam inamicul pe pamant
            isGrounded = true;
            yVelocity = 0;
        }
    }
}