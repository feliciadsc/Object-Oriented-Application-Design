package Maps;

import Tile.Tiles;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Map {
    private static String MAPS_FOLDER = "resources/maps/";          //calea directorului hartilor
    private static String FILE_EXTENSION = ".txt";                  //extensia fisierului cautat

    private int width;                                              //latimea hartii
    private int height;                                             //inaltimea hartii
    private int[][] tiles;                                          //variabila pentru id-ul tile-urilor(harta)
    public Tiles tileManager;

    public int currentLevel = 1;                                    //nivelul current

    //constructor
    public Map(int level) {
        this.currentLevel = level;
        try {
            //setam tileset-ul folosit, in functie de nivel
            tileManager = new Tiles(currentLevel);
            loadWorld();
        } catch (IOException e) {
            System.err.println("Error loading map for level " + currentLevel + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    //functie pentru importarea hartilor din fisier
    public void loadMapFromFile(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        height = lines.size();

        if (height > 0) {
            String[] values = lines.get(0).trim().split("\\s+");
            width = values.length;
        } else {
            width = 0;
        }

        //harta descrisa prin numere intregi
        tiles = new int[width][height];

        for (int y = 0; y < height; y++) {
            String[] values = lines.get(y).trim().split("\\s+");
            for (int x = 0; x < values.length && x < width; x++) {
                try {
                    tiles[x][y] = Integer.parseInt(values[x]);
                } catch (NumberFormatException e) {
                    tiles[x][y] = 0;
                }
            }
        }
    }

    //getter pentru un tile de la o pozitie data
    public int getTileAt(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return -1; // valoare invalida pentru tile
    }

    //incarcarea efectiva a hartii
    public void loadWorld() {
        try {
            String mapPath = MAPS_FOLDER + "level" + currentLevel + FILE_EXTENSION;
            loadMapFromFile(mapPath);
        } catch (IOException e) {
            System.err.println("Error loading map for level " + currentLevel + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    //desenarea hartii
    public void draw(Graphics g, int tileWidth, int tileHeight) {
        if (tileManager == null) {
            System.err.println("Tile manager is not initialized!");
            return;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tileCode = tiles[x][y];
                tileManager.drawTile(g, tileCode, x * tileWidth, y * tileHeight, tileWidth, tileHeight);
            }
        }
    }

    //getter latime
    public int getWidth() {
        return width;
    }

    //getter inaltime
    public int getHeight() {
        return height;
    }

    //functie de actualizare a nivelului
    public void nextLevel() {
        if (currentLevel >= 0 && currentLevel < 3) {
            currentLevel++;
            try {
                // actualizam setul de dale
                tileManager.updateLevel(currentLevel);

                // incarcam noua harta
                loadWorld();
                System.out.println("Moving to level " + currentLevel);
            } catch (IOException e) {
                System.err.println("Error updating to level " + currentLevel + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}