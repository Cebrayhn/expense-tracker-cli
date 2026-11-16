package dev.cebrail.expense;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {

    private static final String USAGE = """
            Usage:
              add <yyyy-mm-dd> <category> <amount> [description...]
              list
            """;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        if (args.length == 0) {
            System.out.print(USAGE);
            return;
        }

        Database db = new Database("expenses.db");
        db.init();
        ExpenseRepository repo = new ExpenseRepository(db);

        switch (args[0]) {
            case "add"  -> handleAdd(repo, args);
            case "list" -> handleList(repo);
            default -> {
                System.out.println("Unknown command: " + args[0]);
                System.out.print(USAGE);
            }
        }
    }

    private static void handleAdd(ExpenseRepository repo, String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("add needs at least: date, category, amount");
            System.out.print(USAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(args[1]);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date '" + args[1] + "'. Use yyyy-mm-dd, e.g. 2026-11-16");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount '" + args[3] + "'. Use a number like 120.50");
            return;
        }
        if (amount <= 0) {
            System.out.println("Amount must be greater than 0");
            return;
        }

        // Everything after the amount is the description (may be empty).
        String description = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

        Expense saved = repo.save(new Expense(null, date, args[2], amount, description));
        System.out.printf("Added #%d: %s  %-10s %10.2f  %s%n",
                saved.id(), saved.date(), saved.category(), saved.amount(), saved.description());
    }

    private static void handleList(ExpenseRepository repo) throws Exception {
        List<Expense> expenses = repo.findAll();
        if (expenses.isEmpty()) {
            System.out.println("No expenses yet. Add one with: add <date> <category> <amount>");
            return;
        }

        System.out.printf("%-4s %-10s %-12s %10s  %s%n", "ID", "DATE", "CATEGORY", "AMOUNT", "DESCRIPTION");
        double total = 0;
        for (Expense e : expenses) {
            System.out.printf("%-4d %-10s %-12s %10.2f  %s%n",
                    e.id(), e.date(), e.category(), e.amount(), e.description());
            total += e.amount();
        }
        System.out.printf("%-28s %10.2f%n", "TOTAL", total);
    }
}