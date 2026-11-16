package dev.cebrail.expense;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens connections to the SQLite file and creates the schema.
 */
public class Database {
    private final String url;

    public Database(String filePath) {
        // SQLite needs no server: the whole database is one file on disk.
        this.url = "jdbc:sqlite:" + filePath;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public void init() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS expenses (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                date        TEXT    NOT NULL,
                category    TEXT    NOT NULL,
                amount      REAL    NOT NULL,
                description TEXT
            )
            """;
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute(sql);
        }
    }
}