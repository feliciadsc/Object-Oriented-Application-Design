package GameWindow;

import  Maps.Map;
import GamePanel.GamePanel;
import DataBase.DataBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.JOptionPane;


public class GameWindow
{
    public JFrame wndFrame;                                     // fereastra principala a jocului
    public JFrame lvlFrame;                                     // fereastra de selectare a nivelurilor
    private String wndTitle;                                    // titlul ferestrei
    private int wndWidth;                                       // latimea ferestrei in pixeli
    private int wndHeight;                                      // inaltimea ferestrei in pixeli

    private Map currentMap;                                     //harta curenta
    private int currentPlayerLevel = 1;                         //nivelul curent
    private String currentPlayerName;                           //numele jucatorului
    private GamePanel gamePanel;                                //panel-ul de joc

    private DataBase database;                                  //baza de date
    private int maxUnlockedLevel = 1;                           //nivelul maxim pe care il poate juca la revenirea in progres

    private Image backgroundImage;                              //imaginea de fundal a meniului

    private Rectangle rectPlay;                                 //zona de click pentru Play
    private Rectangle rectContinue;                             //zona de click pentru Continue
    private Rectangle rectExit;                                 //zona de click pentru Exit

    //constructorul
    public GameWindow(String title, int width, int height)
    {
        wndTitle = title;
        wndWidth = width;
        wndHeight = height;
        wndFrame = null;

        try {
            //stabilim conectiunea catre baza de date
            database = DataBase.getInstance();
            System.out.println(" Database connection established in GameWindow");
        } catch (Exception e) {
            System.err.println(" Failed to initialize database: " + e.getMessage());
        }
    }

    public void BuildGameWindow()
    {
        //in cazul in care fereastra este deja creata, nu o mai cream
        if (wndFrame != null) {
            return;
        }

        // crearea propriu-zisa a ferestrei
        wndFrame = new JFrame(wndTitle);                                    //setarea titlului
        wndFrame.setSize(wndWidth, wndHeight);                              //dimensiunea ferestrei
        wndFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            //setam operatia default de inchidere a ferestrei
        wndFrame.setResizable(false);                                       //interzicem redimensionarea ferestrei
        wndFrame.setLocationRelativeTo(null);                               //centram fereastra

        // solicitam numele jucatorului
        requestPlayerName();
    }

    // functia de solicitare a numelui jucatorului
    private void requestPlayerName() {
        String playerName = JOptionPane.showInputDialog(
                null,
                "Introduceți numele jucătorului:",
                "Nume Jucător",
                JOptionPane.QUESTION_MESSAGE
        );

        // verificam daca jucatorul si-a introdus numele
        if (playerName != null && !playerName.trim().isEmpty()) {
            currentPlayerName = playerName.trim();

            // verificam progresul existent pentru acest jucator
            checkPlayerProgress();

            // afisam meniul principal dupa ce avem numele
            showMainMenu();
        } else {
            // daca jucatorul nu introduce niciun nume, programul se inchide
            System.exit(0);
        }
    }

    //functie de verificare a progresului unui jucator in baza de date
    private void checkPlayerProgress() {

        if (database != null && currentPlayerName != null) {

            // cream un GamePanel temporar pentru a verifica progresul
            Map tempMap = new Map(1);
            GamePanel tempGamePanel = new GamePanel(tempMap, 16, 16, wndWidth, wndHeight, this);
            tempGamePanel.setCurrentPlayerName(currentPlayerName);

            //folosim functia existenta in clasa GamePanel
            boolean hasProgress = tempGamePanel.loadPlayerProgress();

            //daca gasim progresul, ii acordam libertatea de a continua de unde a ramas, sau sa joace un nivel la alegere(dintre cele deja completate)
            if (hasProgress) {
                currentPlayerLevel = tempMap.currentLevel;
                maxUnlockedLevel = Math.max(currentPlayerLevel, maxUnlockedLevel);

                // calculeaza nivelele deblocate bazat pe progres
                for (int i = 1; i < currentPlayerLevel; i++) {
                    maxUnlockedLevel = Math.max(maxUnlockedLevel, i);
                }
                System.out.println("Player " + currentPlayerName + " - current level: " + currentPlayerLevel + ", max unlocked: " + maxUnlockedLevel);
            } else {

                //altfel il obligam sa inceapa de la primul nivel
                maxUnlockedLevel = 1;
                currentPlayerLevel = 1;
                System.out.println("Player " + currentPlayerName + " is new - starting from level 1");
            }
        }
    }

    //functie de afisare a meniului principal
    public void showMainMenu() {
        // incarcam imaginea de fundal
        ImageIcon icon = new ImageIcon("resources/images/meniu_principal.jpg");
        backgroundImage = icon.getImage();

        // folosim un JLabel pentru fundal
        JLabel backgroundLabel = new JLabel(icon);
        backgroundLabel.setLayout(null);

        // coordonatele butoanelor
        rectPlay = new Rectangle(265, 377, 130, 30);      // Coordonatele pentru butonul "Play"
        rectContinue = new Rectangle(265, 452, 130, 30);  // Coordonatele pentru butonul "Continue"
        rectExit = new Rectangle(265, 535, 130, 30);      // Coordonatele pentru butonul "Exit"

        // mouse listener pe background
        backgroundLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point clickPoint = e.getPoint();

                if (rectPlay.contains(clickPoint))
                {
                    System.out.println("Play!");
                    try {
                        startGame();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else if (rectContinue.contains(clickPoint))
                {
                    System.out.println("Continue!");
                    continueGame();
                } else if (rectExit.contains(clickPoint))
                {
                    System.out.println("Exit!");
                    System.exit(0);
                }
            }
        });

        // afisam numele jucatorului în coltul din stanga sus
        JLabel playerNameLabel = new JLabel("Jucător: " + currentPlayerName);
        playerNameLabel.setForeground(Color.WHITE);
        playerNameLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        playerNameLabel.setBounds(10, 10, 300, 30);
        backgroundLabel.add(playerNameLabel);

        //afisare fereastra
        wndFrame.setContentPane(backgroundLabel);
        wndFrame.setVisible(true);
        wndFrame.revalidate();
        wndFrame.repaint();
    }

    //functie de incepere a jocului (la apasarea butonului Play)
    public void startGame() throws IOException {
        System.out.println("Jocul a început!");

        //crarea ferestrei de selectare a nivelului
        lvlFrame = new JFrame(wndTitle);
        lvlFrame.setSize(wndWidth, wndHeight);
        lvlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lvlFrame.setResizable(false);
        lvlFrame.setLocationRelativeTo(null);

        // incarcarea imaginii de fundal
        ImageIcon icon = new ImageIcon("resources/images/level_trans.png");
        backgroundImage = icon.getImage();

        //folosim un JLabel pentru fundal
        JLabel backgroundLabel = new JLabel(icon);
        backgroundLabel.setLayout(null);

        // coordonatele butoanelor
        Rectangle rectLvl1 = new Rectangle(228, 151, 271, 95);
        Rectangle rectLvl2 = new Rectangle(228, 259, 271, 95);
        Rectangle rectLvl3 = new Rectangle(228, 366, 271, 95);
        Rectangle rectRet = new Rectangle(228, 474, 271, 95);

        // mouse listener pe background
        backgroundLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point clickPoint = e.getPoint();

                if (rectLvl1.contains(clickPoint) && maxUnlockedLevel >= 1)
                {
                    loadLevel(1);
                    lvlFrame.dispose();
                } else if (rectLvl2.contains(clickPoint) && maxUnlockedLevel >= 2)
                {
                    loadLevel(2);
                    lvlFrame.dispose();
                } else if (rectLvl3.contains(clickPoint) && maxUnlockedLevel >= 3)
                {
                    loadLevel(3);
                    lvlFrame.dispose();
                } else if (rectRet.contains(clickPoint))
                {
                    lvlFrame.dispose();
                    System.out.println("Return!");
                } else {
                    // nivel blocat
                    JOptionPane.showMessageDialog(
                            lvlFrame,
                            "Acest nivel este blocat! Completați nivelurile anterioare pentru a-l debloca.",
                            "Nivel Blocat",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        lvlFrame.setContentPane(backgroundLabel);
        lvlFrame.setVisible(true);
    }

    //functie pentru continuarea progresului luat din baza de data a jucatorului
    private void continueGame() {
        System.out.println("Continuare joc...");

        if (currentPlayerLevel == 1 && maxUnlockedLevel == 1) {
            JOptionPane.showMessageDialog(
                    wndFrame,
                    "Nu aveți progres salvat. Jocul va începe de la nivelul 1.",
                    "Fără Progres",
                    JOptionPane.INFORMATION_MESSAGE
            );
            loadLevel(1);
        } else {
            // Continuă exact de unde a rămas jucătorul
            loadLevelWithProgress(currentPlayerName);
        }
    }

    //functie pentru incarcarea unui nivel(harta, panelul corespunzator)
    private void loadLevel(int level)
    {
        // cream harta in functie de nivelul dat
        currentMap = new Map(level);

        // cream un nou panel pentru joc
        gamePanel = new GamePanel(currentMap, 16, 16, wndWidth, wndHeight, this);
        gamePanel.setCurrentPlayerName(currentPlayerName);

        System.out.println("Level " + level + " started for player: " + currentPlayerName);
        gamePanel.setPreferredSize(new Dimension(wndWidth, wndHeight));
        gamePanel.setLayout(null);
        gamePanel.setFocusable(true);

        // inlocuim continutul ferestrei cu noul panel de joc
        wndFrame.setContentPane(gamePanel);
        gamePanel.startGameThread();
        wndFrame.revalidate();
        wndFrame.repaint();

        // asiguram focusul pe fereastra jocului
        gamePanel.requestFocusInWindow();
    }

    private void loadLevelWithProgress(String playerName) {
        //cream o harta temporara pentru verificarea progresului
        Map tempMap = new Map(1);
        GamePanel tempGamePanel = new GamePanel(tempMap, 16, 16, wndWidth, wndHeight, this);
        tempGamePanel.setCurrentPlayerName(playerName);

        //verificam daca exista progres in baza de date
        boolean progressLoaded = tempGamePanel.loadPlayerProgress();

        int targetLevel = 1;                //nivelul daca nu exista progres
        if (progressLoaded) {
            //daca exista progres, actualizam nivelul
            targetLevel = tempMap.currentLevel;
            System.out.println("Progress loaded - detected level: " + targetLevel);
        } else {
            System.out.println("No saved progress found, starting from level 1");
        }

        // acum cream harta si GamePanel-ul corespunzator nivelului
        currentMap = new Map(targetLevel);
        gamePanel = new GamePanel(currentMap, 16, 16, wndWidth, wndHeight, this);
        gamePanel.setCurrentPlayerName(playerName);

        //afisari pentru informarea jucatorului
        if (progressLoaded) {
            boolean finalLoad = gamePanel.loadPlayerProgress();
            if (finalLoad) {
                JOptionPane.showMessageDialog(
                        wndFrame,
                        "Progress loaded for " + playerName + "!\nCurrent level: " + targetLevel,
                        "Progress Loaded",
                        JOptionPane.INFORMATION_MESSAGE
                );
                System.out.println("Progress loaded successfully for " + playerName + " at level " + targetLevel);
            }
        } else {
            JOptionPane.showMessageDialog(
                    wndFrame,
                    " No saved progress found for " + playerName + ".\nStarting from level 1...",
                    "New Progress",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        gamePanel.setPreferredSize(new Dimension(wndWidth, wndHeight));
        gamePanel.setLayout(null);
        gamePanel.setFocusable(true);

        // inlocuim continutul ferestrei cu noul panel de joc
        wndFrame.setContentPane(gamePanel);
        gamePanel.startGameThread();
        wndFrame.revalidate();
        wndFrame.repaint();

        // asiguram focusul pe fereastra jocului
        gamePanel.requestFocusInWindow();
    }

    // metoda pentru a actualiza progresul cand se completeaza un nivel
    public void updatePlayerProgress(int completedLevel) {
        if (completedLevel >= currentPlayerLevel) {
            currentPlayerLevel = completedLevel + 1;            // actualizam nivelul curent
            maxUnlockedLevel = Math.max(maxUnlockedLevel, completedLevel + 1);
            System.out.println("Level " + completedLevel + " completed. Current level: " + currentPlayerLevel);
        }
    }

    //getter pentru numele jucatorului
    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

}