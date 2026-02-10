package GamePanel;

import Collectable.*;
import Camera.Camera;
import GameWindow.GameWindow;
import Maps.Map;
import Character.Character;
import Character.FinalBoss;
import DataBase.DataBase;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

import Character.KeyHandler;
import Character.WolfManager;

public class GamePanel extends JPanel implements Runnable, MouseListener {
    public Map currentMap;
    public int tileWidth;
    public int tileHeight;
    private final int windowWidth;
    private final int windowHeight;

    public Camera camera;

    int FPS = 60;

    public KeyHandler keyH = new KeyHandler();
    Thread gameThread;
    public Character character;

    private long start;
    private long pausedTime = 0;
    private long pauseStartTime = 0;
    private int remainingTimeSeconds = 120;
    private int totalGameTimeSeconds;

    private final AppleManager appleManager;
    private final SheepManager sheepManager;
    public final WolfManager wolfManager;
    public FinalBoss finalBoss;
    private Bucket bucket;

    private DataBase database;
    private String currentPlayerName;

    private BufferedImage backgroundImage;
    private BufferedImage pauseMenuImage;
    private BufferedImage gameOverImage;
    private BufferedImage gameCompletedImage;

    private boolean gamePaused = false;
    private boolean gameOver = false;
    private boolean gameCompleted = false;

    private long bossDefeatedTime =  -1;

    private long level3StartTime = 0;
    private final int warningDuration = 3000; // 3 secunde
    private boolean warningShown = false;

    private int plant35 = 0;
    private int plant36 = 0;
    private int plant37 = 0;

    private boolean collectLastFrame = false;
    private final int plantGoal = 2;

    private final ConvolveOp blurFilter;

    //butoane meniul de pauza
    private Rectangle restartButtonArea;
    private Rectangle leaderboardButtonArea;
    private Rectangle saveExitButtonArea;

    // butonul de la finalizarea jocului
    private Rectangle mainMenuButtonArea;

    // pozitionarea meniului
    private int menuX;
    private int menuY;
    private final int menuWidth = 400;
    private final int menuHeight = 400;

    private GameWindow gameWindow;

    public GamePanel(Map map, int tileWidth, int tileHeight, int windowWidth, int windowHeight, GameWindow gamewind) {
        this.currentMap = map;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.totalGameTimeSeconds = 0;
        this.addKeyListener(keyH);
        this.addMouseListener(this);
        this.setFocusable(true);

        this.setPreferredSize(new Dimension(windowWidth, windowHeight));
        this.setSize(new Dimension(windowWidth, windowHeight));
        this.setMinimumSize(new Dimension(windowWidth, windowHeight));
        this.setMaximumSize(new Dimension(windowWidth, windowHeight));

        int mapPixelWidth = map.getWidth() * tileWidth;
        int mapPixelHeight = map.getHeight() * tileHeight;

        this.camera = new Camera(windowWidth, windowHeight, mapPixelWidth, mapPixelHeight);

        character = new Character(this, keyH);

        CollectableFactory appleFactory = CollectableFactoryProvider.getFactory(CollectableType.APPLE);
        appleManager = (AppleManager) appleFactory.createManager();
        appleManager.setGamePanel(this);

        CollectableFactory sheepFactory = CollectableFactoryProvider.getFactory(CollectableType.SHEEP);
        sheepManager = (SheepManager) sheepFactory.createManager();

        appleManager.setupCollectables(tileWidth, tileHeight, currentMap.currentLevel);
        sheepManager.setupCollectables(tileWidth, tileHeight, currentMap.currentLevel);

        wolfManager = new WolfManager(this);
        wolfManager.initializeWolf();

        finalBoss = new FinalBoss(0, 0, this);
        if (currentMap.currentLevel == 3) {
            finalBoss.positionInBottomRightCorner();
        }

        level3StartTime = System.currentTimeMillis();
        warningShown = true;

        bucket = new Bucket(windowWidth - 250, windowHeight -150 , this);
        bucket.positionBucketsInBottomRightCorner();

        this.start = System.currentTimeMillis();

        initializeMenuAreas();

        try {
            database = DataBase.getInstance();
            System.out.println(" Database connection established in GamePanel");


            backgroundImage = ImageIO.read(new File("resources/images/level_trans.png"));
            pauseMenuImage = ImageIO.read(new File("resources/images/pause_menu.png"));
            gameOverImage = ImageIO.read(new File("resources/images/game_over.png"));
            gameCompletedImage = ImageIO.read(new File("resources/images/game_complete.png"));
            System.out.println("Images loaded successfully.");
        } catch (IOException e) {
            System.err.println("Something failed: " + e.getMessage());
        }

        float[] blurKernel = {
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f
        };
        blurFilter = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, null);

        setParentWindow(gamewind);
        this.currentPlayerName = gamewind.getCurrentPlayerName();
    }

    public void setParentWindow(GameWindow parentWindow) {
        this.gameWindow = parentWindow;
    }

    private void initializeMenuAreas() {
        menuX = (windowWidth - menuWidth) / 2;
        menuY = (windowHeight - menuHeight) / 2;

        int buttonWidth = 250;
        int buttonHeight = 60;
        int buttonStartX = menuX + 80;

        // butonul de restart
        int restartY = menuY + 110;
        restartButtonArea = new Rectangle(buttonStartX, restartY, buttonWidth, buttonHeight);

        //butonul de clasament
        int leaderboardY = menuY + 180;
        leaderboardButtonArea = new Rectangle(buttonStartX, leaderboardY, buttonWidth, buttonHeight);

        // butonul de salvare si iesire
        int saveExitY = menuY + 260;
        saveExitButtonArea = new Rectangle(buttonStartX, saveExitY, buttonWidth, buttonHeight);

        // butonul de meniu principal
        int mainMenuButtonY = menuY + 320;
        mainMenuButtonArea = new Rectangle(buttonStartX, mainMenuButtonY, buttonWidth, buttonHeight);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        if (gameCompleted) {
            BufferedImage tempImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2dTemp = tempImage.createGraphics();

            drawBackground(g2dTemp);

            AffineTransform originalTransform = g2dTemp.getTransform();

            camera.apply(g2dTemp);

            if (currentMap != null) {
                currentMap.draw(g2dTemp, tileWidth, tileHeight);
            }

            assert currentMap != null;
            if(currentMap.currentLevel == 2) {
                appleManager.drawApples(g2dTemp);
            } else if(currentMap.currentLevel == 1) {
                sheepManager.drawSheeps(g2dTemp);
            } else if(currentMap.currentLevel == 3 && finalBoss != null)
            {
                finalBoss.draw(g2dTemp, character);
            }

            character.draw(g2dTemp);

            wolfManager.draw(g2dTemp);

            bucket.draw(g2dTemp);

            g2dTemp.setTransform(originalTransform);

            drawUI(g2dTemp);

            g2dTemp.dispose();

            BufferedImage blurredImage = blurFilter.filter(tempImage, null);
            g2d.drawImage(blurredImage, 0, 0, null);

            savePlayerProgress();
            if (gameCompletedImage != null) {
                g2d.drawImage(gameCompletedImage, menuX, menuY, menuWidth, menuHeight, null);
            }

            return;
        }

        if (gamePaused || gameOver) {
            BufferedImage tempImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2dTemp = tempImage.createGraphics();

            drawBackground(g2dTemp);

            AffineTransform originalTransform = g2dTemp.getTransform();

            camera.apply(g2dTemp);

            if (currentMap != null) {
                currentMap.draw(g2dTemp, tileWidth, tileHeight);
            }

            assert currentMap != null;
            if(currentMap.currentLevel == 2) {
                appleManager.drawApples(g2dTemp);
            } else if(currentMap.currentLevel == 1) {
                sheepManager.drawSheeps(g2dTemp);
            } else if(currentMap.currentLevel == 3 && finalBoss != null)
            {
                finalBoss.draw(g2d, character);
            }

            character.draw(g2dTemp);

            wolfManager.draw(g2dTemp);

            bucket.draw(g2dTemp);

            g2dTemp.setTransform(originalTransform);

            drawUI(g2dTemp);

            g2dTemp.dispose();

            BufferedImage blurredImage = blurFilter.filter(tempImage, null);

            g2d.drawImage(blurredImage, 0, 0, null);

            if (pauseMenuImage != null && !gameOver) {
                g2d.drawImage(pauseMenuImage, menuX, menuY, menuWidth, menuHeight, null);
            }

            if(gameOverImage != null && gameOver)
            {
                g2d.drawImage(gameOverImage, menuX, menuY, menuWidth, menuHeight, null);
            }


        } else {

            drawBackground(g2d);

            AffineTransform originalTransform = g2d.getTransform();

            camera.apply(g2d);

            if (currentMap != null) {
                currentMap.draw(g2d, tileWidth, tileHeight);
            }

            if(currentMap.currentLevel == 2)
            {
                appleManager.drawApples(g2d);
            }
            else if(currentMap.currentLevel == 1)
            {
                sheepManager.drawSheeps(g2d);
            } else if (currentMap.currentLevel == 3 && warningShown) {
                long now = System.currentTimeMillis();
                if (now - level3StartTime <= warningDuration) {
                    g2d.setColor(Color.RED);
                    g2d.setFont(new Font("Arial", Font.BOLD, 10));

                    int msgX = character.getX() - 15;
                    int msgY = character.getY() + 5;

                    g2d.drawString("Be careful!", msgX, msgY);
                } else {
                    warningShown = false;
                }
            }

            if (currentMap.currentLevel == 3 && finalBoss != null) {
                finalBoss.draw(g2d, character);
            }

            character.draw(g2d);
            wolfManager.draw(g2d);
            bucket.draw(g2d);

            wolfManager.showMessage(g2d);

            g2d.setTransform(originalTransform);

            drawUI(g2d);
        }
    }

    private void drawBackground(Graphics2D g2d) {
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, windowWidth, windowHeight, null);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, windowWidth, windowHeight);
        }
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Times New Roman", Font.BOLD, 20));

        if(currentMap.currentLevel == 2) {
            g2d.drawString("Apples: " + character.appleCount, 20, 30);
        } else if(currentMap.currentLevel == 1) {
            g2d.drawString("Sheeps: " + character.sheeps_collected + "/10", 20, 30);
        }

        g2d.drawString("Lives: " + character.getHealth(), 20, 60);
        drawTimer(g2d);

        if (currentMap.currentLevel == 3 && finalBoss == null) {
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.setColor(Color.WHITE);

            boolean shown35 = false, shown36 = false, shown37 = false;

            int offsetX = tileWidth * 14;
            int offsetY = 230;

            for (int y = 0; y < currentMap.getHeight(); y++) {
                for (int x = 0; x < currentMap.getWidth(); x++) {
                    int tileId = currentMap.getTileAt(x, y);
                    int screenX = x * tileWidth - (int) camera.getX();
                    int screenY = y * tileHeight - (int) camera.getY();

                    if (tileId == 35 && !shown35) {
                        g2d.drawString(plant35 + "/" + plantGoal, screenX + offsetX, screenY + offsetY);
                        shown35 = true;
                    } else if (tileId == 36 && !shown36) {
                        g2d.drawString(plant36 + "/" + plantGoal, screenX + offsetX, screenY + offsetY);
                        shown36 = true;
                    } else if (tileId == 37 && !shown37) {
                        g2d.drawString(plant37 + "/" + plantGoal, screenX + offsetX, screenY + offsetY);
                        shown37 = true;
                    }

                    if (shown35 && shown36 && shown37) break;
                }
                if (shown35 && shown36 && shown37) break;
            }

            // Afisare mesaj dupa infrangerea boss-ului timp de 4 secunde
            if (bossDefeatedTime > 0 && System.currentTimeMillis() - bossDefeatedTime <= 4000) {
                String message = "Find the vegetables and press E to collect";
                g2d.setFont(new Font("Arial", Font.PLAIN, 14)); // font mai mic
                g2d.setColor(Color.YELLOW);
                FontMetrics metrics = g2d.getFontMetrics();
                int textWidth = metrics.stringWidth(message);

                int x = (windowWidth - textWidth) / 2;
                int y = windowHeight - 100;

                g2d.drawString(message, x, y);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(windowWidth, windowHeight);
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        //nu mai actualizam daca jocul este finalizat
        if (gameCompleted) {
            return;
        }

        if (keyH.pausePressed != gamePaused) {
            if (keyH.pausePressed && !gamePaused) {
                pauseStartTime = System.currentTimeMillis();
            } else if (!keyH.pausePressed && gamePaused) {
                pausedTime += System.currentTimeMillis() - pauseStartTime;
            }
            gamePaused = keyH.pausePressed;
        }

        boolean newGameOver = character.getGameOver();
        if (newGameOver != gameOver) {
            if (newGameOver && !gameOver) {
                pauseStartTime = System.currentTimeMillis();
            } else if (!newGameOver && gameOver) {
                pausedTime += System.currentTimeMillis() - pauseStartTime;
            }
            gameOver = newGameOver;
        }

        if (gamePaused || gameOver) {
            return;
        }

        character.update();

        boolean wasEPressed = keyH.interact;

        switch (currentMap.currentLevel) {
            case 1: {
                for (Sheep sheep : sheepManager.getSheeps()) {
                    sheep.checkDistance(
                            character.getX() + 24,
                            character.getY() + 24
                    );

                    if (wasEPressed && sheep.canBeCollected() && !sheep.isCollected()) {
                        sheep.setCollected(true);
                        character.sheeps_collected++;
                        if (CheckLevelCompleted()) {
                            handleLevelCompletion();
                        }
                    }
                }
                break;
            }

            case 2: {
                boolean bucketWasEmpty = !bucket.isFilled;
                for (Apple apple : appleManager.getApples()) {
                    apple.checkDistance(
                            character.getX() + 24,
                            character.getY() + 24
                    );

                    if (wasEPressed && apple.canBeCollected() && !apple.isCollected()) {
                        apple.setCollected(true);
                        character.appleCount++;
                        if (CheckLevelCompleted()) {
                            handleLevelCompletion();
                        }
                    }
                }

                wolfManager.update();

                bucket.update();

                if (bucketWasEmpty && bucket.isFilled) {
                    if (CheckLevelCompleted()) {
                        handleLevelCompletion();
                    }
                }

                break;
            }
            case 3:
                //logica inamicului final
                if (finalBoss != null) {
                    if (finalBoss.isAlive()) {
                        finalBoss.update(character);
                        if (keyH.attack) {
                            Rectangle playerBounds = character.getBounds();
                            Rectangle bossBounds = finalBoss.getBounds();
                            if (playerBounds.intersects(bossBounds)) {
                                finalBoss.takeDamage(1.5);
                                System.out.println("Boss lovit!");
                            }
                        }
                    } else {
                        finalBoss = null;
                        bossDefeatedTime = System.currentTimeMillis(); // <-- setează timpul când boss-ul moare
                        System.out.println("Boss eliminat complet din joc.");
                    }
                } else {
                    // colectarea plantelor dupa moartea inamicului
                    int tileX = (character.getX() + 16) / tileWidth;
                    int tileY = (character.getY() + 16) / tileHeight;
                    int tileId = currentMap.getTileAt(tileX, tileY);
                    if (keyH.interact && !collectLastFrame ) {
                        switch (tileId) {
                            case 35:
                                if (plant35 < plantGoal) plant35++;
                                break;
                            case 36:
                                if (plant36 < plantGoal) plant36++;
                                break;
                            case 37:
                                if (plant37 < plantGoal) plant37++;
                                break;
                        }

                        // Verifica daca jocul a fost completat
                        if (CheckLevelCompleted()) {
                            handleGameCompletion();
                        }
                    }
                    collectLastFrame = keyH.interact;
                }
                break;
        }

        camera.update(character);

        wolfManager.updateWolves();
    }

    private void drawTimer(Graphics2D g2d) {
        g2d.setFont(new Font("Times New Roman", Font.BOLD, 20));

        long remainingSeconds = getRemainingTime();

        if (remainingSeconds <= 0) {
            remainingSeconds = 0;
            System.out.println("Game Over!!");
            if (!gameOver) {
                character.setGameOver(true);
            }
        }

        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        String timeText = String.format("Timp rămas: %02d:%02d", minutes, seconds);

        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(timeText);
        int textHeight = metrics.getHeight();

        int width = 200;
        int height = 165;

        g2d.setColor(new Color(0, 0, 0, 150));

        int textX = (width - textWidth) / 2;
        int textY = (height - textHeight) / 2 + metrics.getAscent();

        g2d.setColor(Color.BLACK);
        g2d.drawString(timeText, textX, textY);
    }


    public void startGameThread()
    {
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void handleLevelCompletion() {
        int oldLevel = currentMap.currentLevel;

        currentMap.nextLevel();

        if (currentMap.currentLevel != oldLevel) {

            if (gameWindow != null) {
                gameWindow.updatePlayerProgress(oldLevel);
            }

            if (currentMap.currentLevel == 2) {
                gameOver = false;
                character.appleCount = 0;
                character.setHealth(5);
                appleManager.setupCollectables(tileWidth, tileHeight, currentMap.currentLevel);

                // reinitializeaza galeata pentru nivelul 2
                bucket = new Bucket(windowWidth - 250, windowHeight - 150, this);
                bucket.positionBucketsInBottomRightCorner();
            }
            else if(currentMap.currentLevel == 3)
            {
                gameOver=false;
                character.setHealth(5);
                finalBoss.setHealth(15);
            }

            character.setDefaultPosition();
            camera.updateLoc(currentMap);

            bucket.update();

            wolfManager.resetWolfForLevel();

            // pozitioneaza lupul in coltul din dreapta jos pentru nivelul 2
            if (currentMap.currentLevel == 2) {
                wolfManager.positionWolfInBottomRightCorner();
            }

            if (currentMap.currentLevel == 3) {
                finalBoss = new FinalBoss(0, 0, this);
                finalBoss.positionInBottomRightCorner();
                finalBoss.setHealth(15);
                character.setHealth(5);

                //pornim timer-ul pentru avertisment
                level3StartTime = System.currentTimeMillis();
                warningShown = true;
            }

            // resetarea timer-ului pentru urmatorul nivel
            this.start = System.currentTimeMillis();
            pausedTime = 0;
            pauseStartTime = 0;
        }
    }

    // metoda pentru finalizarea jocului
    private void handleGameCompletion() {
        System.out.println("Jocul a fost completat cu succes!");

        totalGameTimeSeconds = 360 - getRemainingTime();

        if (database != null && currentPlayerName != null) {
            database.saveCompletionTime(currentPlayerName, totalGameTimeSeconds);
            System.out.println(" Timp de completare salvat: " + totalGameTimeSeconds + " secunde");
        }

        if (gameWindow != null) {
            gameWindow.updatePlayerProgress(3); // Nivelul 3 a fost completat
        }

        gameCompleted = true;

        // opreste thread-ul jocului
        gameThread = null;
    }

    //functie pentru verificarea completarii unui nivel
    public boolean CheckLevelCompleted()
    {
        boolean completed = false;

        switch(currentMap.currentLevel)
        {
            case 1:
                if(character.sheeps_collected == 10)
                {
                    completed = true;
                    System.out.println("Ati finalizat nivelul 1!!");
                }
                break;

            case 2:
                if(bucket.isFilled)
                {
                    completed = true;
                    System.out.println("Ati finalizat nivelul 2!!");
                }
                break;

            case 3:
                if(plant35 == 2 && plant36 == 2 && plant37 == 2)
                {
                    completed = true;
                    System.out.println("Ati finalizat nivelul 3!!");
                }
                break;

            default:
                break;
        }

        return completed;
    }

    // handlere pentru actiunile mouse-ului
    @Override
    public void mouseClicked(MouseEvent e) {
        Point clickPoint = e.getPoint();

        // apasarea butonului de meniu principal in fereastra de finalizare joc
        if (gameCompleted) {
            if (mainMenuButtonArea.contains(clickPoint)) {
                handleMainMenuButton();
            }
            return;
        }

        if (!gamePaused && !gameOver) {
            return;
        }

        if (restartButtonArea.contains(clickPoint)) {
            handleRestartButton();
        } else if (leaderboardButtonArea.contains(clickPoint)) {
            handleLeaderboardButton();
        } else if (saveExitButtonArea.contains(clickPoint)) {
            handleSaveExitButton();
        }
    }

    private void handleMainMenuButton() {
        System.out.println("Main Menu button clicked from game completed screen!");
        returnToMainMenu();
    }

    private void handleRestartButton() {
        System.out.println("Restart button clicked!");

        // resetam nivelul curent
        character.setDefaultPosition();
        character.setHealth(5);
        character.setGameOver(false);

        // resetam obiectele colectabile si inamicii
        if (currentMap.currentLevel == 1) {
            character.sheeps_collected = 0;
            sheepManager.setupCollectables(tileWidth, tileHeight, currentMap.currentLevel);
        } else if (currentMap.currentLevel == 2) {
            character.appleCount = 0;
            appleManager.setupCollectables(tileWidth, tileHeight, currentMap.currentLevel);
            bucket = new Bucket(windowWidth - 250, windowHeight - 150, this);
            bucket.positionBucketsInBottomRightCorner();
        } else if (currentMap.currentLevel == 3) {
            plant35 = 0;
            plant36 = 0;
            plant37 = 0;
            bossDefeatedTime = -1;

            if (finalBoss == null) {
                finalBoss = new FinalBoss(0, 0, this);
                finalBoss.positionInBottomRightCorner();
            }
            finalBoss.setHealth(15);
        }

        wolfManager.resetWolfForLevel();
        if (currentMap.currentLevel == 2) {
            wolfManager.positionWolfInBottomRightCorner();
        }

        setRemainingTime(120); // Reseteaza la 2 minute

        // revenim la joc
        gamePaused = false;
        keyH.pausePressed = false;
        gameOver = false;
        gameCompleted = false;

        if (gameThread == null) {
            startGameThread();
        }
    }

    private void handleLeaderboardButton() {
        System.out.println("Leaderboard button clicked!");

        // Creeaza si afiseaza fereastra de leaderboard
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        LeaderboardWindow leaderboardWindow = new LeaderboardWindow(parentFrame, this);
        leaderboardWindow.setVisible(true);
    }

    private void handleSaveExitButton() {
        System.out.println("Save & Exit button clicked!");

        if (savePlayerProgress()) {

            if (gameWindow != null) {
                gameWindow.updatePlayerProgress(currentMap.currentLevel - 1);
            }

            JOptionPane.showMessageDialog(
                    this,
                    " Progress saved successfully!",
                    "Successfully Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );
            System.out.println(" Progress saved successfully");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save progress!",
                    "Failed Save",
                    JOptionPane.ERROR_MESSAGE
            );
            System.out.println(" Failed to save progress");
        }

        // oprim game thread-ul
        gameThread = null;

        // revenim la meniul principal
        returnToMainMenu();
    }

    private void returnToMainMenu() {
        if (gameWindow != null) {
            try {
                gameWindow.showMainMenu();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void setCurrentPlayerName(String playerName) {
        this.currentPlayerName = playerName;
    }

    public boolean loadPlayerProgress() {
        if (database != null && currentPlayerName != null) {
            boolean loaded = database.loadProgressForPlayer(currentPlayerName, character, this);

            if (loaded) {
                // actualizeaza harta si componentele pentru nivelul încarcat
                updateMapForCurrentLevel();
            }

            return loaded;
        }
        return false;
    }

    private void updateMapForCurrentLevel() {
        int level = currentMap.currentLevel;

        // Reseteaza componentele pentru nivelul curent
        switch (level) {
            case 1:
                // Setup pentru nivelul 1 - oi
                sheepManager.setupCollectables(tileWidth, tileHeight, level);
                break;

            case 2:
                // Setup pentru nivelul 2 - mere si bucket
                appleManager.setupCollectables(tileWidth, tileHeight, level);
                bucket = new Bucket(windowWidth - 250, windowHeight - 150, this);
                bucket.positionBucketsInBottomRightCorner();
                wolfManager.resetWolfForLevel();
                wolfManager.positionWolfInBottomRightCorner();
                break;

            case 3:
                // Setup pentru nivelul 3 - boss si plante
                if (finalBoss == null) {
                    finalBoss = new FinalBoss(0, 0, this);
                    finalBoss.positionInBottomRightCorner();
                }
                level3StartTime = System.currentTimeMillis();
                warningShown = true;
                break;
        }

        // actualizeaza camera pentru nivelul curent
        camera.updateLoc(currentMap);

        System.out.println(" Harta actualizată pentru nivelul " + level);
    }

    public boolean savePlayerProgress() {
        if (database != null && currentPlayerName != null) {
            return database.saveProgressForPlayer(currentPlayerName, character, this);
        }
        return false;
    }

    public int getRemainingTime() {
        // Calculeaza timpul ramas pe baza timpului curent
        long currentTime = System.currentTimeMillis();
        long totalPausedTime = pausedTime;

        if (gamePaused || gameOver) {
            totalPausedTime += currentTime - pauseStartTime;
        }

        long elapsedSeconds = (currentTime - start - totalPausedTime) / 1000;
        long remaining = remainingTimeSeconds - elapsedSeconds;

        return (int) Math.max(0, remaining);
    }

    public void setRemainingTime(int timeInSeconds) {
        this.remainingTimeSeconds = timeInSeconds;
        // Reseteaza timpul de start pentru a reflecta timpul ramas
        this.start = System.currentTimeMillis();
        this.pausedTime = 0;
        this.pauseStartTime = 0;
    }
}