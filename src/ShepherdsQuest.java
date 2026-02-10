import GameWindow.GameWindow;

import javax.swing.*;

public class ShepherdsQuest {
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            GameWindow gameWindow = new GameWindow("Shepherd's Quest", 700, 700);
            gameWindow.BuildGameWindow();


        });
    }
}
