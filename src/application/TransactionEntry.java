package application;

import java.time.LocalDate;

public class TransactionEntry {
    private String title;
    private double amount;
    private String category;
    private String type; // "Income" or "Expense"
    private LocalDate date;

    public TransactionEntry(String title, double amount, String category, String type, LocalDate date) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.date = date;
    }

    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getType() { return type; }
    public LocalDate getDate() { return date; }

    public void setTitle(String title) { this.title = title; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
    public void setType(String type) { this.type = type; }
    public void setDate(LocalDate date) { this.date = date; }
}
