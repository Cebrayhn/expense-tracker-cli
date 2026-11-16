package dev.cebrail.expense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * All SQL for the expenses table lives here, so the rest of the app
 * never builds SQL strings by hand.
 */
public class ExpenseRepository {
    private final Database db;

    public ExpenseRepository(Database db) {
        this.db = db;
    }

    /** Inserts the expense and returns a copy with the generated id. */
    public Expense save(Expense e) throws SQLException {
        String sql = "INSERT INTO expenses (date, category, amount, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.date().toString());
            ps.setString(2, e.category());
            ps.setDouble(3, e.amount());
            ps.setString(4, e.description());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;
                return new Expense(id, e.date(), e.category(), e.amount(), e.description());
            }
        }
    }

    /** Returns every expense, newest date first. */
    public List<Expense> findAll() throws SQLException {
        String sql = "SELECT id, date, category, amount, description FROM expenses ORDER BY date DESC, id DESC";
        List<Expense> result = new ArrayList<>();
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Expense(
                        rs.getInt("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                ));
            }
        }
        return result;
    }
    /** Total amount per category, biggest first. */
    public Map<String, Double> totalsByCategory() throws SQLException {
        String sql = "SELECT category, SUM(amount) AS total FROM expenses GROUP BY category ORDER BY total DESC";
        return runTotalsQuery(sql, "category");
    }

    /** Total amount per month (yyyy-mm), newest first. */
    public Map<String, Double> totalsByMonth() throws SQLException {
        String sql = "SELECT substr(date, 1, 7) AS month, SUM(amount) AS total FROM expenses GROUP BY month ORDER BY month DESC";
        return runTotalsQuery(sql, "month");
    }

    /** Shared helper: runs a "label, total" query and keeps the row order. */
    private Map<String, Double> runTotalsQuery(String sql, String labelColumn) throws SQLException {
        Map<String, Double> totals = new LinkedHashMap<>();
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                totals.put(rs.getString(labelColumn), rs.getDouble("total"));
            }
        }
        return totals;
    }
}