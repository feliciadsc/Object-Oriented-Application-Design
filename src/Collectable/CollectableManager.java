package Collectable;

import java.awt.Graphics2D;
import java.util.ArrayList;

public abstract class CollectableManager {
    protected ArrayList<Collectable> collectables;
    protected CollectableFactory factory;

    public CollectableManager() {
        collectables = new ArrayList<>();
    }

    public abstract void setupCollectables(int tileWidth, int tileHeight, int currentLevel);
    protected abstract int[][] getPositionsForLevel(int level);
    protected abstract boolean isValidLevel(int level);

    public void drawCollectables(Graphics2D g2) {
        for (Collectable collectable : collectables) {
            collectable.draw(g2);
        }
    }

    protected void createCollectablesAtPositions(int[][] positions, int tileWidth, int tileHeight) {
        for (int[] pos : positions) {
            int x = pos[0] * tileWidth;
            int y = pos[1] * tileHeight;
            collectables.add(factory.createCollectable(x, y));
        }
    }
}