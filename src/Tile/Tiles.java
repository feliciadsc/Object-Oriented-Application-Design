package Tile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Tiles {
    public static final int TILE_SIZE_IN_SHEET = 16;            //dimensiunea dalelor
    private BufferedImage tilesetImage;                         //setul de dale
    private int[] solidTileIds;                                 //dale solide
    private int currentLevel;                                   //nivelul curent

    //constructor
    public Tiles(int level) throws IOException {
        this.currentLevel = level;
        setSolidTilesForLevel(level);
        loadTileset();
    }

    //setter pentru dalele solide in functie de nivel
    private void setSolidTilesForLevel(int level) {
        switch (level) {
            case 1:
                solidTileIds = new int[]{41, 42, 43, 26, 28, 11, 13, 14, 56, 57, 58, 71, 73, 86, 87, 88, 120, 121, 122, 123,
                        124, 125, 126, 127, 128, 129, 130, 135, 144, 145, 150, 143, 158, 159, 160, 165, 173, 180, 188, 195, 196, 197,
                        198, 199, 200, 201, 202, 203, 64, 63, 48, 49, 50, 51, 65, 66};
                break;
            case 2:
                solidTileIds = new int[]{0, 1, 2, 5, 6, 7, 18, 19, 25, 26, 27, 65, 78, 64, 79, 66};
                break;
            default:
                solidTileIds = new int[]{3};
                break;
        }
    }

    //incarcarea setului de date in functie de nivel
    public void loadTileset() throws IOException {
        try {
            String TILESET_PATH = "resources/images/tileset" + currentLevel + ".png";
            tilesetImage = ImageIO.read(new File(TILESET_PATH));
            System.out.println("Tileset " + currentLevel + " loaded successfully");
        } catch (IOException e) {
            System.err.println("Could not load tileset: " + e.getMessage());
            throw e;
        }
    }

    //functie de actualizare a setului de date
    public void updateLevel(int newLevel) throws IOException {
        this.currentLevel = newLevel;
        setSolidTilesForLevel(newLevel);
        loadTileset();
    }

    //functie de verificare a soliditatii
    public boolean isSolidTile(int tileId) {
        for (int id : solidTileIds) {
            if (tileId == id) {
                return true;
            }
        }
        return false;
    }

    //functie de desenare a fiecarei dale, pe coordonate
    public void drawTile(Graphics g, int tileCode, int x, int y, int tileWidth, int tileHeight) {
        if (tilesetImage == null) {
            System.err.println("Tileset is not loaded!");
            return;
        }

        int[] tileCoords = getTileCoordinatesFromSheet(tileCode);

        g.drawImage(
                tilesetImage,
                x, y, x + tileWidth, y + tileHeight,
                tileCoords[0], tileCoords[1], tileCoords[0] + TILE_SIZE_IN_SHEET, tileCoords[1] + TILE_SIZE_IN_SHEET,
                null
        );
    }

    //getter pentru coordonatele unei dale
    private int[] getTileCoordinatesFromSheet(int tileCode) {
        int tilesetWidth = tilesetImage.getWidth() / TILE_SIZE_IN_SHEET;
        int tileX = tileCode % tilesetWidth;
        int tileY = tileCode / tilesetWidth;
        return new int[]{tileX * TILE_SIZE_IN_SHEET, tileY * TILE_SIZE_IN_SHEET};
    }
}