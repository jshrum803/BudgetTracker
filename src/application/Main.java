package application;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

import java.time.LocalDate;

public class Main extends Application {

    private ObservableList<TransactionEntry> transactionList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        // Transaction entry screen
        Tab entryTab = new Tab("Add Entry");
        entryTab.setContent(buildEntryForm(transactionList));
        entryTab.setClosable(false);

        // Table view screen
        Tab tableTab = new Tab("All Transactions");
        tableTab.setContent(buildTransactionTable(transactionList));
        tableTab.setClosable(false);

        // Summary screen
        Tab summaryTab = new Tab("Summary");
        summaryTab.setContent(buildSummaryView(transactionList));
        summaryTab.setClosable(false);

        // TabPane configuration
        TabPane tabPane = new TabPane(entryTab, tableTab, summaryTab);
        Scene scene = new Scene(tabPane, 900, 500);
        primaryStage.setTitle("Personal Budget Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Data entry fields for the transaction entry form
    private VBox buildEntryForm(ObservableList<TransactionEntry> transactionList) {
        TextField titleField = new TextField();
        titleField.setPromptText("Title/Description");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Income", "Dining Out", "Bills", "Entertainment", "Gas", "Groceries", "Shopping", "Other");
        categoryBox.setPromptText("Category");

        DatePicker datePicker = new DatePicker();

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton incomeBtn = new RadioButton("Income");
        RadioButton expenseBtn = new RadioButton("Expense");
        incomeBtn.setToggleGroup(typeGroup);
        expenseBtn.setToggleGroup(typeGroup);

        Button addButton = new Button("Add Entry");

        addButton.setOnAction(e -> {
            String title = titleField.getText();
            String amountText = amountField.getText();
            String category = categoryBox.getValue();
            RadioButton selectedType = (RadioButton) typeGroup.getSelectedToggle();
            LocalDate date = datePicker.getValue();

            // Error thrown if any fields are left blank
            if (title.isEmpty() || amountText.isEmpty() || category == null || selectedType == null || date == null) {
                showAlert("Missing input", "Please fill in all fields.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
            	// Error thrown if the input isn't completely numeric
                showAlert("Invalid input", "Amount must be numeric.");
                return;
            }

            String type = selectedType.getText();
            TransactionEntry entry = new TransactionEntry(title, amount, category, type, date);
            transactionList.add(entry);

            // Clear inputs after submission
            titleField.clear();
            amountField.clear();
            categoryBox.getSelectionModel().clearSelection();
            typeGroup.selectToggle(null);
            datePicker.setValue(null);
        });

        VBox form = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Amount:"), amountField,
                new Label("Category:"), categoryBox,
                new Label("Date:"), datePicker,
                new Label("Type:"), new HBox(10, incomeBtn, expenseBtn),
                addButton);
        form.setPadding(new Insets(15));
        return form;
    }

    // Displays all the data gathered from the entry form in a table
    private VBox buildTransactionTable(ObservableList<TransactionEntry> transactionList) {
        TableView<TransactionEntry> table = new TableView<>(transactionList);

        TableColumn<TransactionEntry, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        titleCol.setCellFactory(col -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label link = new Label(item);
                        link.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-cursor: hand;");
                        link.setOnMouseClicked(e -> {
                            TransactionEntry selected = getTableView().getItems().get(getIndex());
                            showEditTransaction(selected, transactionList);
                        });
                        setGraphic(link);
                    }
                }
            };
        });

        TableColumn<TransactionEntry, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getAmount()).asObject());

        TableColumn<TransactionEntry, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));

        TableColumn<TransactionEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));

        TableColumn<TransactionEntry, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDate()));

        TableColumn<TransactionEntry, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("x");

            {
                deleteBtn.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                deleteBtn.setOnAction(e -> {
                    TransactionEntry selected = getTableView().getItems().get(getIndex());

                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Confirm Delete");
                    confirmDialog.setHeaderText(null);
                    confirmDialog.setContentText("Are you sure you want to delete this transaction?");

                    ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                    ButtonType noBtn = new ButtonType("No", ButtonBar.ButtonData.NO);
                    confirmDialog.getButtonTypes().setAll(yesBtn, noBtn);

                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == yesBtn) {
                            transactionList.remove(selected);
                        }
                    });
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });

        table.getColumns().setAll(List.of(titleCol, amountCol, categoryCol, typeCol, dateCol, deleteCol));
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);


        VBox layout = new VBox(10, table);
        layout.setPadding(new Insets(15));
        return layout;
    }

    //Code for the "Edit Transaction" screen
    private void showEditTransaction(TransactionEntry selected, ObservableList<TransactionEntry> transactionList) {
        Dialog<TransactionEntry> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");

        TextField titleField = new TextField(selected.getTitle());
        TextField amountField = new TextField(String.valueOf(selected.getAmount()));
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Income", "Dining Out", "Bills", "Entertainment", "Gas", "Groceries", "Shopping", "Other");
        categoryBox.setValue(selected.getCategory());

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton incomeBtn = new RadioButton("Income");
        RadioButton expenseBtn = new RadioButton("Expense");
        incomeBtn.setToggleGroup(typeGroup);
        expenseBtn.setToggleGroup(typeGroup);
        if (selected.getType().equals("Income")) incomeBtn.setSelected(true);
        else expenseBtn.setSelected(true);

        DatePicker datePicker = new DatePicker(selected.getDate());

        VBox editForm = new VBox(10,
                new Label("Title:"), titleField,
                new Label("Amount:"), amountField,
                new Label("Category:"), categoryBox,
                new Label("Date:"), datePicker,
                new Label("Type:"), new HBox(10, incomeBtn, expenseBtn));
        dialog.getDialogPane().setContent(editForm);

        ButtonType saveBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                try {
                    String title = titleField.getText();
                    double amount = Double.parseDouble(amountField.getText());
                    String category = categoryBox.getValue();
                    RadioButton selectedType = (RadioButton) typeGroup.getSelectedToggle();
                    String type = selectedType.getText();
                    LocalDate date = datePicker.getValue();

                    if (title.isEmpty() || category == null || type == null || date == null) {
                        showAlert("Invalid Input", "All fields must be filled.");
                        return null;
                    }

                    return new TransactionEntry(title, amount, category, type, date);
                } catch (Exception ex) {
                    showAlert("Invalid Input", "Amount must be numeric.");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            int index = transactionList.indexOf(selected);
            transactionList.set(index, updated);
        });
    }

    
    // Summary screen
    private VBox buildSummaryView(ObservableList<TransactionEntry> transactionList) {
        Label incomeLabel = new Label("Total Income: $0.00");
        Label expenseLabel = new Label("Total Expenses: $0.00");
        Label balanceLabel = new Label("Net Balance: $0.00");

        Button refreshBtn = new Button("Update Summary");

        // Refreshes the screen taking into account any new transaction entries
        refreshBtn.setOnAction(e -> {
            double income = transactionList.stream()
                    .filter(t -> t.getType().equalsIgnoreCase("Income"))
                    .mapToDouble(TransactionEntry::getAmount).sum();

            double expenses = transactionList.stream()
                    .filter(t -> t.getType().equalsIgnoreCase("Expense"))
                    .mapToDouble(TransactionEntry::getAmount).sum();

            double balance = income - expenses;

            incomeLabel.setText(String.format("Total Income: $%.2f", income));
            expenseLabel.setText(String.format("Total Expenses: $%.2f", expenses));
            balanceLabel.setText(String.format("Net Balance: $%.2f", balance));

            // Show alert if balance is negative
            if (balance < 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Negative Balance");
                alert.setHeaderText(null);
                alert.setContentText("Your net balance is negative. You may be overspending.");
                alert.showAndWait();
            }
        });


        VBox summary = new VBox(10, incomeLabel, expenseLabel, balanceLabel, refreshBtn);
        summary.setPadding(new Insets(15));
        return summary;
    }

    // Called to display a warning or error
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
