package Collectable;

import java.awt.Graphics2D;

public abstract class Collectable {
    protected int x, y;
    protected boolean canBeCollected;
    protected boolean isCollected;

    public Collectable(int x, int y) {
        this.x = x;
        this.y = y;
        this.canBeCollected = false;
        this.isCollected = false;
    }

    public abstract void draw(Graphics2D g2);
    public abstract void checkDistance(int playerX, int playerY);
    public abstract void loadImage();

    public boolean canBeCollected() {
        return canBeCollected;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        this.isCollected = collected;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}