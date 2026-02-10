package DataBase;

import Character.Character;
import GamePanel.GamePanel;

import java.io.File;
import java.sql.*;

public class DataBase {
    private static final String DB_URL = "jdbc:sqlite:Joc.db";
    private static DataBase instance;
    private Connection connection;

    // Constructor privat pentru a preveni instantierea directa
    private DataBase() {
        connect();
        initializeDatabase();
        initializeLeaderboardTable();
    }

    // Metoda publica pentru a obtine instanta singleton
    public static DataBase getInstance() {
        if (instance == null) {
            synchronized (DataBase.class) {
                if (instance == null) {
                    instance = new DataBase();
                }
            }
        }
        return instance;
    }

    // Metoda privata pentru conectarea la baza de date
    private void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println(" Cale completă către Joc.db: " + new File("Joc.db").getAbsolutePath());

                // Încearcă să încarce driver-ul SQLite JDBC
                try {
                    Class.forName("org.sqlite.JDBC");
                    System.out.println(" SQLite JDBC driver încărcat cu succes.");
                } catch (ClassNotFoundException e) {
                    System.err.println(" SQLite JDBC driver nu a fost găsit!");
                    throw new RuntimeException("SQLite JDBC driver lipsește din classpath", e);
                }

                connection = DriverManager.getConnection(DB_URL);
                System.out.println(" Conexiune la baza de date realizată cu succes.");
            }
        } catch (SQLException e) {
            System.err.println(" Eroare SQL la conectarea la baza de date: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Nu s-a putut realiza conexiunea la baza de date.", e);
        } catch (Exception e) {
            System.err.println(" Eroare generală la conectarea la baza de date: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Nu s-a putut realiza conexiunea la baza de date.", e);
        }
    }

    // Metoda pentru a obtine conexiunea
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println(" Eroare la verificarea conexiunii: " + e.getMessage());
            connect();
        }
        return connection;
    }

    // Initializarea bazei de date
    private void initializeDatabase() {
        String createJucatori = """
        CREATE TABLE IF NOT EXISTS jucatori (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nume TEXT UNIQUE NOT NULL
        );
    """;

        String createProgresNivel = """
        CREATE TABLE IF NOT EXISTS progres_nivel (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            jucator_id INTEGER NOT NULL,
            nivel INTEGER NOT NULL,
            viata INTEGER DEFAULT 3,
            pozitie_x INTEGER,
            pozitie_y INTEGER,
            mere INTEGER DEFAULT 0,
            oi INTEGER DEFAULT 0,
            completat BOOLEAN DEFAULT 0,
            timp_salvare TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (jucator_id) REFERENCES jucatori(id)
        );
    """;

        String createLeaderboard = """
        CREATE TABLE IF NOT EXISTS leaderboard (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        jucator_id INTEGER NOT NULL,
        timp_completare INTEGER NOT NULL,
        data_completare TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (jucator_id) REFERENCES jucatori(id)
    );
    """;

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(createJucatori);
            stmt.execute(createProgresNivel);
            stmt.execute(createLeaderboard);

            // Adauga coloana 'oi' dacă nu exista
            try {
                String addOiColumn = "ALTER TABLE progres_nivel ADD COLUMN oi INTEGER DEFAULT 0";
                stmt.execute(addOiColumn);
                System.out.println(" Coloana 'oi' a fost adăugată în tabelul progres_nivel.");
            } catch (SQLException e) {
                System.out.println("️ Coloana 'oi' există deja sau nu a putut fi adăugată: " + e.getMessage());
            }

            try {
                String addTimpColumn = "ALTER TABLE progres_nivel ADD COLUMN timp_ramas INTEGER DEFAULT 120";
                stmt.execute(addTimpColumn);
                System.out.println(" Coloana 'timp_ramas' a fost adăugată în tabelul progres_nivel.");
            } catch (SQLException e) {
                System.out.println("️ Coloana 'timp_ramas' există deja sau nu a putut fi adăugată: " + e.getMessage());
            }

            stmt.close();
            System.out.println(" Tabelele au fost create/verificate cu succes.");
        } catch (SQLException e) {
            System.err.println(" Eroare la inițializarea bazei de date: " + e.getMessage());
        }
    }

    // Returnează ID-ul jucatorului sau il creeaza
    public int getOrCreatePlayerId(String numeJucator) {
        Connection conn = getConnection();

        try {
            String selectSQL = "SELECT id FROM jucatori WHERE nume = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
            selectStmt.setString(1, numeJucator);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                selectStmt.close();
                return id;
            }

            rs.close();
            selectStmt.close();

            String insertSQL = "INSERT INTO jucatori(nume) VALUES (?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
            insertStmt.setString(1, numeJucator);
            insertStmt.executeUpdate();
            insertStmt.close();

            // Cauta din nou pentru a lua id-ul
            PreparedStatement retryStmt = conn.prepareStatement(selectSQL);
            retryStmt.setString(1, numeJucator);
            ResultSet rs2 = retryStmt.executeQuery();

            if (rs2.next()) {
                int id = rs2.getInt("id");
                rs2.close();
                retryStmt.close();
                return id;
            }

            rs2.close();
            retryStmt.close();

        } catch (SQLException e) {
            System.err.println(" Eroare la getOrCreatePlayerId: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    // Incarca progresul jucatorului
    public boolean loadProgressForPlayer(String playerName, Character character, GamePanel gp) {
        int jucatorId = getOrCreatePlayerId(playerName);
        if (jucatorId == -1) return false;

        String sql = """
    SELECT * FROM progres_nivel
    WHERE jucator_id = ?
    ORDER BY timp_salvare DESC
    LIMIT 1
    """;

        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, jucatorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                character.setHealth(rs.getInt("viata"));
                character.setPosition(rs.getInt("pozitie_x"), rs.getInt("pozitie_y"));
                character.appleCount = rs.getInt("mere");

                // Incarca numarul de oi
                try {
                    character.sheeps_collected = rs.getInt("oi");
                } catch (SQLException e) {
                    character.sheeps_collected = 0;
                }

                // Incarca nivelul
                int savedLevel = rs.getInt("nivel");
                gp.currentMap.currentLevel = savedLevel;

                // Incarca timpul ramas
                try {
                    int timpRamas = rs.getInt("timp_ramas");
                    gp.setRemainingTime(timpRamas);
                } catch (SQLException e) {
                    gp.setRemainingTime(120); // default 2 minute
                }

                rs.close();
                stmt.close();
                System.out.println(" Progres încărcat cu succes pentru nivelul " + savedLevel);
                return true;
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(" Eroare la încărcarea progresului: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Salveaza progresul jucatorului
    public boolean saveProgressForPlayer(String playerName, Character character, GamePanel gp) {
        int jucatorId = getOrCreatePlayerId(playerName);
        if (jucatorId == -1) return false;

        String sql = """
    INSERT INTO progres_nivel (jucator_id, nivel, viata, pozitie_x, pozitie_y, mere, oi, completat, timp_ramas)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, jucatorId);
            stmt.setInt(2, gp.currentMap.currentLevel);
            stmt.setInt(3, character.getHealth());
            stmt.setInt(4, character.getX());
            stmt.setInt(5, character.getY());
            stmt.setInt(6, character.appleCount);
            stmt.setInt(7, character.sheeps_collected);
            stmt.setBoolean(8, gp.CheckLevelCompleted());
            stmt.setInt(9, gp.getRemainingTime());

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                System.out.println(" Progres salvat cu succes pentru nivelul " + gp.currentMap.currentLevel);
                return true;
            }
        } catch (SQLException e) {
            System.err.println(" Eroare la salvarea progresului: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Metoda pentru a preveni clonarea instantei singleton
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clonarea nu este permisă pentru clasa Singleton DataBase");
    }

    private void initializeLeaderboardTable() {
        String createLeaderboard = """
    CREATE TABLE IF NOT EXISTS leaderboard (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        jucator_id INTEGER NOT NULL,
        timp_completare INTEGER NOT NULL,
        data_completare TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (jucator_id) REFERENCES jucatori(id)
    );
    """;

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(createLeaderboard);
            stmt.close();
            System.out.println(" Tabelul leaderboard a fost creat/verificat cu succes.");
        } catch (SQLException e) {
            System.err.println(" Eroare la crearea tabelului leaderboard: " + e.getMessage());
        }
    }

    public boolean saveCompletionTime(String playerName, int completionTimeSeconds) {
        int jucatorId = getOrCreatePlayerId(playerName);
        if (jucatorId == -1) return false;

        String sql = """
    INSERT INTO leaderboard (jucator_id, timp_completare)
    VALUES (?, ?)
    """;

        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, jucatorId);
            stmt.setInt(2, completionTimeSeconds);

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                System.out.println(" Timp de completare salvat: " + completionTimeSeconds + " secunde pentru " + playerName);
                return true;
            }
        } catch (SQLException e) {
            System.err.println(" Eroare la salvarea timpului de completare: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public java.util.List<LeaderboardEntry> getTop3Players() {
        java.util.List<LeaderboardEntry> leaderboard = new java.util.ArrayList<>();

        String sql = """
    SELECT j.nume, l.timp_completare, l.data_completare
    FROM leaderboard l
    JOIN jucatori j ON l.jucator_id = j.id
    ORDER BY l.timp_completare ASC
    LIMIT 3
    """;

        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String nume = rs.getString("nume");
                int timpCompletare = rs.getInt("timp_completare");
                String dataCompletare = rs.getString("data_completare");

                leaderboard.add(new LeaderboardEntry(nume, timpCompletare, dataCompletare));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println(" Eroare la încărcarea leaderboard-ului: " + e.getMessage());
            e.printStackTrace();
        }

        return leaderboard;
    }


}