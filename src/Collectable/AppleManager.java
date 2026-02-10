package Collectable;

import java.awt.Graphics2D;
import java.util.ArrayList;

import Character.WolfManager;
import Character.Wolf;
import GamePanel.GamePanel;

public class AppleManager extends CollectableManager {
    private GamePanel gamePanel;

    public AppleManager() {
        super();
        this.factory = CollectableFactoryProvider.getFactory(CollectableType.APPLE);
    }

    @Override
    public void setupCollectables(int tileWidth, int tileHeight, int currentLevel) {
        collectables.clear();

        if (!isValidLevel(currentLevel)) {
            return;
        }

        int[][] applePositions = getPositionsForLevel(currentLevel);
        System.out.println("Placing " + applePositions.length + " apples for level " + currentLevel);

        createCollectablesAtPositions(applePositions, tileWidth, tileHeight);
    }

    //getter pentru pozitiile merelor
    @Override
    protected int[][] getPositionsForLevel(int level) {
        if (level == 2) {
            return new int[][]{
                    {6, 22}, {9, 23}, {17, 21}, {27, 23},
                    {34, 22}, {51, 23}, {54, 24}, {62, 23}
            };
        }
        return new int[0][0];
    }

    //conditionare inn functie de nivel
    @Override
    protected boolean isValidLevel(int level) {
        return level == 2;
    }

    //desenare mere
    public void drawApples(Graphics2D g2) {
        drawCollectables(g2);
    }

    //getter pentru mere
    public ArrayList<Apple> getApples() {
        ArrayList<Apple> apples = new ArrayList<>();
        for (Collectable collectable : collectables) {
            if (collectable instanceof Apple) {
                apples.add((Apple) collectable);
            }
        }
        return apples;
    }

    //setter pentru GamePanel
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
}