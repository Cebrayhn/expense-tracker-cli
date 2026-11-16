package dev.cebrail.expense;

public class Main {
    public static void main(String[] args) throws Exception {
        Database db = new Database("expenses.db");
        db.init();
        System.out.println("Database ready: expenses.db");
    }
}