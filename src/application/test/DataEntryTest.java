package application.test;

import static org.junit.jupiter.api.Assertions.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import application.TransactionEntry;

import java.time.LocalDate;

public class DataEntryTest {

    private ObservableList<TransactionEntry> transactionList;

    @BeforeEach
    void setup() {
        transactionList = FXCollections.observableArrayList();
    }

    // Adding a transaction
    @Test
    void testAddTransaction() {
        TransactionEntry entry = new TransactionEntry("Paycheck", 2000.0, "Income", "Income", LocalDate.now());
        transactionList.add(entry);

        assertEquals(1, transactionList.size());
        assertEquals("Paycheck", transactionList.get(0).getTitle());
        assertEquals(2000.0, transactionList.get(0).getAmount());
        assertEquals("Income", transactionList.get(0).getCategory());
        assertEquals("Income", transactionList.get(0).getType());
    }

    // Editing a transaction
    @Test
    void testEditTransaction() {
        TransactionEntry entry = new TransactionEntry("Groceries", 150.0, "Groceries", "Expense", LocalDate.now());
        transactionList.add(entry);

        entry.setTitle("Groceries");
        entry.setAmount(175.0);
        entry.setCategory("Dining Out");
        entry.setType("Expense");

        assertEquals("Groceries", entry.getTitle());
        assertEquals(175.0, entry.getAmount());
        assertEquals("Dining Out", entry.getCategory());
        assertEquals("Expense", entry.getType());
    }

    // Deleting a transaction
    @Test
    void testDeleteTransaction() {
        TransactionEntry entry1 = new TransactionEntry("Netflix", 15.99, "Entertainment", "Expense", LocalDate.now());
        TransactionEntry entry2 = new TransactionEntry("Salary", 3000.0, "Income", "Income", LocalDate.now());

        transactionList.addAll(entry1, entry2);
        assertEquals(2, transactionList.size());

        transactionList.remove(entry1);
        assertEquals(1, transactionList.size());
        assertEquals("Salary", transactionList.get(0).getTitle());
    }

    // Summary calculations (Income, Expense, Balance)
    @Test
    void testSummaryCalculations() {
        transactionList.add(new TransactionEntry("Salary", 1200.0, "Income", "Income", LocalDate.now()));
        transactionList.add(new TransactionEntry("Utilities", 180.0, "Bills", "Expense", LocalDate.now()));
        transactionList.add(new TransactionEntry("Dining", 90.0, "Dining Out", "Expense", LocalDate.now()));
        transactionList.add(new TransactionEntry("Bonus", 300.0, "Income", "Income", LocalDate.now()));

        double income = transactionList.stream()
                .filter(t -> "Income".equals(t.getType()))
                .mapToDouble(TransactionEntry::getAmount)
                .sum();

        double expenses = transactionList.stream()
                .filter(t -> "Expense".equals(t.getType()))
                .mapToDouble(TransactionEntry::getAmount)
                .sum();

        double balance = income - expenses;

        assertEquals(1500.0, income);
        assertEquals(270.0, expenses);
        assertEquals(1230.0, balance);
    }
}
