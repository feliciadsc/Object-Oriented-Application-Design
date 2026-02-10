package Character;

import GamePanel.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Character {
    //pozitia personajului
    private int x;
    private int y;

    //viteza de miscare
    private int speed;

    //viata personajului
    private int lives = 5;

    //referinta catre panel-ul de joc
    GamePanel gp;

    //contoare pentru obiectele colectate
    public int sheeps_collected;
    public int appleCount = 0;

    //referinta catre handler-ul de taste
    private KeyHandler keyH;

    //variabile pentru imaginile si animatiile personajului
    private BufferedImage spriteSheet;
    private Animation walkUp, walkDown, walkLeft, walkRight;
    private Animation currentAnimation;
    private Animation attackAnimation;

    // dimensiunile zonei de detectie a coliziunilor
    private final int HITBOX_WIDTH = 16;
    private final int HITBOX_HEIGHT = 16;
    private final int HITBOX_OFFSET_X = 16;
    private final int HITBOX_OFFSET_Y = 21;

    //mecanismul de gravitatie
    private float yVelocity;                    //viteza verticala a personajului
    private final float GRAVITY = 0.5f;             //acceleratia gravitationala
    private final int TERMINAL_VELOCITY = 7;        //viteza maxima de cadere
    private final int JUMP_FORCE = -7;              //forta aplicata la saritura (cu minus pt ca Y creste in jos)

    //variabile de stare pentru sarituri
    private boolean isJumping;
    private boolean isGrounded;

    private boolean gameOver = false;

    private boolean isAttacking = false;
    private long attackStartTime;
    private final long ATTACK_DURATION = 400;

    //constructor
    public Character(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        loadSprites();

        setDefaultValues();
    }

    //setarea valorilor implicite in functie de nivel
    public void setDefaultValues() {
        switch (gp.currentMap.currentLevel) {
            case 1:
                x = 125;
                y = 725;
                speed = 2;
                sheeps_collected = 0;
                break;

            case 2:
                x = 25;
                y = 250;
                speed = 2;
                appleCount = 0;

                yVelocity = 0;
                isJumping = false;
                isGrounded = false;
                break;

            case 3:
                x = 100;
                y = 250;
                speed = 2;
                yVelocity = 0;
                isJumping = false;
                isGrounded = false;
                break;

            default:
                x = 25;
                y = 250;
                break;
        }
    }

    // setter pentru pozitiile personajului in functie de nivel
    public void setDefaultPosition() {
        setDefaultValues();
    }

    private BufferedImage flipImageHorizontally(BufferedImage image) {
        BufferedImage flipped = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flipped.createGraphics();
        g.drawImage(image,
                0, 0, image.getWidth(), image.getHeight(),
                image.getWidth(), 0, 0, image.getHeight(), // inversat pe orizontală
                null);
        g.dispose();
        return flipped;
    }

    private void loadSprites() {
        try {
            BufferedImage raw = ImageIO.read(getClass().getResource("/characters_animation/character_sprites.png"));
            spriteSheet = makeColorTransparent(raw, Color.WHITE);
            BufferedImage attackSheet = ImageIO.read(getClass().getResource("/characters_animation/character_attack.png"));
            attackSheet = makeColorTransparent(attackSheet, Color.WHITE);
            BufferedImage[] attackFrames = new BufferedImage[4];
            for (int i = 0; i < 4; i++) {
                attackFrames[i] = attackSheet.getSubimage(i * 32, 0, 32, 32);
            }
            attackAnimation = new Animation(attackFrames, 8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage[] rightFrames = new BufferedImage[6];
        BufferedImage[] leftFrames = new BufferedImage[6];

        for (int i = 0; i < 6; i++) {
            BufferedImage frame = spriteSheet.getSubimage(i * 32, 0, 32, 32);
            rightFrames[i] = frame;
            leftFrames[i] = flipImageHorizontally(frame);
        }

        walkRight = new Animation(rightFrames, 10);
        walkLeft = new Animation(leftFrames, 10);
        walkUp = new Animation(rightFrames, 10);
        walkDown = new Animation(rightFrames, 10);

        currentAnimation = walkDown;
    }

    //actualizarea starii personajului
    public void update() {
        boolean moving = false;

        //variabile temporare pentru calcularea pozitiei viitoare
        int xTemp = x;
        int yTemp = y;

        //tipul de miscare al personajului in functie de nivel(
        switch (gp.currentMap.currentLevel) {
            case 1:
            {
                if (keyH.upPressed) {
                    yTemp -= speed;
                    currentAnimation = walkUp;
                    moving = true;
                }
                if (keyH.downPressed) {
                    yTemp += speed;
                    currentAnimation = walkDown;
                    moving = true;
                }
                if (keyH.leftPressed) {
                    xTemp -= speed;
                    currentAnimation = walkLeft;
                    moving = true;
                }
                if (keyH.rightPressed) {
                    xTemp += speed;
                    currentAnimation = walkRight;
                    moving = true;
                }

                //daca nu exista coliziuni, personajul ramane in limitele hartii
                if (!checkTileCollision(xTemp, yTemp) && yTemp > 0 && yTemp <= (gp.currentMap.getHeight() - 2) * gp.tileHeight && xTemp > 0 && xTemp <= (gp.currentMap.getWidth() - 2) * gp.tileWidth) {
                    x = xTemp;
                    y = yTemp;
                }

                break;
            }

            case 2:
            case 3:
            {
                //se incrementeaza viteza verticala cu val gravitatiei, limitand-o la viteza terminala
                yVelocity += GRAVITY;
                if (yVelocity > TERMINAL_VELOCITY) {
                    yVelocity = TERMINAL_VELOCITY;
                }

                //se aplica viteza verticala la pozitia temporara
                yTemp += yVelocity;

                //verifica starea de salt, setam velocitatea cu forta de saritura, setam starea de saritura
                if (keyH.upPressed && isGrounded) {
                    yVelocity = JUMP_FORCE;
                    isGrounded = false;
                    isJumping = true;
                }

                //miscarea orizontala (la fel ca la nivelul 1)
                if (keyH.leftPressed) {
                    xTemp -= speed;
                    currentAnimation = walkLeft;
                    moving = true;
                }
                if (keyH.rightPressed) {
                    xTemp += speed;
                    currentAnimation = walkRight;
                    moving = true;
                }

                //verificam coliziunile orizontale, actualizam daca nu exista coliziuni
                if (!checkHorizontalCollision(xTemp, y)) {
                    x = xTemp;
                }

                //verificam coliziunile verticale, actualizam daca nu exista coliziuni
                if (!checkVerticalCollision(x, yTemp)) {
                    y = yTemp;
                    isGrounded = false;
                } else {

                    //daca exista coliziuni verticale, setam starea pe pamant
                    if (yVelocity > 0) {
                        isGrounded = true;
                        isJumping = false;
                    }

                    //resetam velocitatea
                    yVelocity = 0;
                }
                break;
            }
        }

        if (gp.keyH.attack && !isAttacking) {
            isAttacking = true;
            attackStartTime = System.currentTimeMillis();
            currentAnimation = attackAnimation;
        }

        if (isAttacking) {
            attackAnimation.update();
            if (System.currentTimeMillis() - attackStartTime > ATTACK_DURATION) {
                isAttacking = false;
            }
        } else if (moving) {
            currentAnimation.update();
        } else {
            currentAnimation.setFrame(0);
        }
    }

    private boolean checkHorizontalCollision(int nextX, int nextY) {
        int centerX = nextX + HITBOX_OFFSET_X + (HITBOX_WIDTH / 2);
        int centerY = nextY + HITBOX_OFFSET_Y + (HITBOX_HEIGHT / 2);

        int leftX = nextX + HITBOX_OFFSET_X;
        int rightX = nextX + HITBOX_OFFSET_X + HITBOX_WIDTH;

        int tileWidth = gp.tileWidth;
        int tileHeight = gp.tileHeight;

        int centerTileX = centerX / tileWidth;
        int centerTileY = centerY / tileHeight;
        int leftTileX = leftX / tileWidth;
        int rightTileX = rightX / tileWidth;

        if (isSolidTileAt(leftTileX, centerTileY) ||
                isSolidTileAt(centerTileX, centerTileY) ||
                isSolidTileAt(rightTileX, centerTileY)) {
            return true;
        }

        if (nextX < 0 || nextX + HITBOX_WIDTH > gp.currentMap.getWidth() * tileWidth) {
            return true;
        }

        return false;
    }

    //verifica coliziunile verticale pentru nivelurile 2 si 3
    private boolean checkVerticalCollision(int nextX, int nextY) {
        int bottomY = nextY + HITBOX_OFFSET_Y + HITBOX_HEIGHT;
        int topY = nextY + HITBOX_OFFSET_Y;

        int centerX = nextX + HITBOX_OFFSET_X + (HITBOX_WIDTH / 2);

        int tileWidth = gp.tileWidth;
        int tileHeight = gp.tileHeight;

        int centerTileX = centerX / tileWidth;
        int bottomTileY = bottomY / tileHeight;
        int topTileY = topY / tileHeight;

        if (yVelocity >= 0) {
            if (isSolidTileAt(centerTileX, bottomTileY)) {
                y = bottomTileY * tileHeight - HITBOX_OFFSET_Y - HITBOX_HEIGHT;
                return true;
            }
        }
        else{
            if (isSolidTileAt(centerTileX, topTileY)) {
                y = (topTileY + 1) * tileHeight - HITBOX_OFFSET_Y;
                return true;
            }
        }

        if (nextY < 0 || nextY + HITBOX_HEIGHT > gp.currentMap.getHeight() * tileHeight) {
            return true;
        }

        return false;
    }

    //verifica daca un tile este solid pe baza pozitiei jucatorului
    private boolean isSolidTileAt(int tileX, int tileY) {
        if (tileX >= 0 && tileX < gp.currentMap.getWidth() && tileY >= 0 && tileY < gp.currentMap.getHeight())
        {
            int tileNum = gp.currentMap.getTileAt(tileX, tileY);
            return gp.currentMap.tileManager.isSolidTile(tileNum);
        }
        return true;    //daca iese din harta se considera solide
    }

    //verificarea coliziunilor pentru nivelul 1
    private boolean checkTileCollision(int nextX, int nextY) {
        int centerX = nextX + HITBOX_OFFSET_X + (HITBOX_WIDTH / 2);
        int centerY = nextY + HITBOX_OFFSET_Y + (HITBOX_HEIGHT / 2);

        int tileWidth = gp.tileWidth;
        int tileHeight = gp.tileHeight;

        int centerTileX = centerX / tileWidth;
        int centerTileY = centerY / tileHeight;

        if (centerTileX >= 0 && centerTileX < gp.currentMap.getWidth() &&
                centerTileY >= 0 && centerTileY < gp.currentMap.getHeight() - 1) {

            int tileNum = gp.currentMap.getTileAt(centerTileX, centerTileY);

            return gp.currentMap.tileManager.isSolidTile(tileNum);
        }

        return true;
    }


    public void draw(Graphics2D g2) {
        double scale = 1.5;
        BufferedImage frame = currentAnimation.getCurrentFrame();

        int width = (int)(frame.getWidth() * scale);
        int height = (int)(frame.getHeight() * scale);

        g2.drawImage(frame, x, y, width, height, null);
    }

    private BufferedImage makeColorTransparent(BufferedImage image, Color color) {
        BufferedImage transparentImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                if (new Color(pixel, true).equals(color)) {
                    transparentImage.setRGB(x, y, 0x00000000); // Transparent
                } else {
                    transparentImage.setRGB(x, y, pixel);
                }
            }
        }
        return transparentImage;
    }

    //gettere pentru coordonate
    public int getX() { return x; }
    public int getY() { return y; }

    private void restoreHealth() {
        if (lives < 3) {
            lives++;
        }
    }

    public boolean useApple() {
        if (appleCount > 0) {
            appleCount--;
            restoreHealth();
            return true;
        } else {
            return false;
        }
    }

    public void applyKnockback(int dx, int dy, int strength) {
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) return;

        double nx = dx / distance;
        double ny = dy / distance;

        x += (int)(nx * strength);
        y += (int)(ny * strength);
    }

    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            System.out.println("Game Over!");
            gameOver = true;
        }
    }

    public int getAppleCount() {
        return appleCount;
    }

    public Rectangle getBounds() {
        BufferedImage currentFrame = currentAnimation.getCurrentFrame();
        return new Rectangle(x, y, currentFrame.getWidth(), currentFrame.getHeight());
    }

    public int getHealth() {
        return lives;
    }

    public void setHealth(int health) {
        this.lives = health;
    }

    public boolean getGameOver()
    {
        return gameOver;
    }

    public void setGameOver(boolean game)
    {
        this.gameOver = game;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

}