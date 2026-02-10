package Collectable;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class SheepManager extends CollectableManager {

    public SheepManager() {
        super();
        this.factory = CollectableFactoryProvider.getFactory(CollectableType.SHEEP);
    }

    @Override
    public void setupCollectables(int tileWidth, int tileHeight, int currentLevel) {
        collectables.clear();

        if (!isValidLevel(currentLevel)) {
            return;
        }

        int[][] sheepPositions = getPositionsForLevel(currentLevel);
        createCollectablesAtPositions(sheepPositions, tileWidth, tileHeight);
    }

    @Override
    protected int[][] getPositionsForLevel(int level) {
        if (level == 1) {
            return new int[][]{
                    {6, 7}, {2, 30}, {17, 21}, {45, 47}, {37, 7},
                    {15, 2}, {24, 5}, {28, 2}, {41, 2}, {40, 23}
            };
        }
        return new int[0][0];
    }

    @Override
    protected boolean isValidLevel(int level) {
        return level == 1;
    }

    public void drawSheeps(Graphics2D g2) {
        drawCollectables(g2);
    }

    public ArrayList<Sheep> getSheeps() {
        ArrayList<Sheep> sheeps = new ArrayList<>();
        for (Collectable collectable : collectables) {
            if (collectable instanceof Sheep) {
                sheeps.add((Sheep) collectable);
            }
        }
        return sheeps;
    }
}