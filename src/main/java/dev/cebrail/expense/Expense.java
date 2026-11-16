package dev.cebrail.expense;

import java.time.LocalDate;

/**
 * One expense row. A record is an immutable data holder:
 * Java generates the constructor, getters, equals/hashCode and toString.
 */
public record Expense(
        Integer id,
        LocalDate date,
        String category,
        double amount,
        String description
) {}