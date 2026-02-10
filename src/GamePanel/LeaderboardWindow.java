package GamePanel;

import DataBase.DataBase;
import DataBase.LeaderboardEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LeaderboardWindow extends JDialog {
    private DataBase database;              //baza de date
    private JPanel parentPanel;             //panel-ul de baza

    //constructor
    public LeaderboardWindow(JFrame parent, JPanel parentPanel) {
        super(parent, "Leaderboard - Top 3 Jucători", true);
        this.parentPanel = parentPanel;
        this.database = DataBase.getInstance();

        initializeWindow();
        loadLeaderboard();
    }

    //functie de initializare a clasamentului
    private void initializeWindow() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // seteaza layout-ul principal
        setLayout(new BorderLayout());      //imparte zona in 5 zone

        // fereastra principala
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setPreferredSize(new Dimension(500, 60));

        //eticheta de titlu
        JLabel titleLabel = new JLabel("TOP 3 JUCĂTORI", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);
    }

    //incarcarea clasamentului
    private void loadLeaderboard() {
        List<LeaderboardEntry> top3 = database.getTop3Players();

        // panel pentru leaderboard
        JPanel leaderboardPanel = new JPanel();
        leaderboardPanel.setLayout(new BoxLayout(leaderboardPanel, BoxLayout.Y_AXIS));
        leaderboardPanel.setBackground(Color.WHITE);
        leaderboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (top3.isEmpty()) {
            // mesaj cand nu exista inregistrari
            JLabel noDataLabel = new JLabel("Nu există încă înregistrări în leaderboard", JLabel.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            noDataLabel.setForeground(Color.GRAY);
            noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            leaderboardPanel.add(Box.createVerticalGlue());
            leaderboardPanel.add(noDataLabel);
            leaderboardPanel.add(Box.createVerticalGlue());
        } else {
            // adauga spatiu la inceput
            leaderboardPanel.add(Box.createVerticalStrut(20));

            // afisam top 3
            Color[] colors = {
                    new Color(255, 215, 0),  // Auriu
                    new Color(192, 192, 192), // Argintiu
                    new Color(205, 127, 50)   // Bronz
            };

            for (int i = 0; i < top3.size(); i++) {
                LeaderboardEntry entry = top3.get(i);

                // panel pentru fiecare intrare
                JPanel entryPanel = new JPanel();
                entryPanel.setLayout(new BorderLayout());
                entryPanel.setBackground(colors[i]);
                entryPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createRaisedBevelBorder(),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));

                // medalia si pozitie
                JLabel positionLabel = new JLabel(" #" + (i + 1));
                positionLabel.setFont(new Font("Arial", Font.BOLD, 20));
                positionLabel.setForeground(Color.BLACK);

                // numele jucatorului
                JLabel nameLabel = new JLabel(entry.getPlayerName());
                nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
                nameLabel.setForeground(Color.BLACK);
                nameLabel.setHorizontalAlignment(JLabel.CENTER);

                // timpul
                JLabel timeLabel = new JLabel(entry.getFormattedTime());
                timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
                timeLabel.setForeground(Color.BLACK);

                // Organizează elementele
                entryPanel.add(positionLabel, BorderLayout.WEST);
                entryPanel.add(nameLabel, BorderLayout.CENTER);
                entryPanel.add(timeLabel, BorderLayout.EAST);

                // seteaza dimensiunea maxima
                entryPanel.setMaximumSize(new Dimension(450, 60));

                leaderboardPanel.add(entryPanel);
                leaderboardPanel.add(Box.createVerticalStrut(10));
            }

            // adauga spatiu flexibil
            leaderboardPanel.add(Box.createVerticalGlue());
        }

        // Scroll pane pentru leaderboard
        JScrollPane scrollPane = new JScrollPane(leaderboardPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Panel pentru butonul de închidere
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.LIGHT_GRAY);
        buttonPanel.setPreferredSize(new Dimension(500, 60));

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setPreferredSize(new Dimension(120, 35));
        closeButton.setBackground(new Color(220, 53, 69));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}