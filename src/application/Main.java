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

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.scene.chart.PieChart;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;


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
        // Title
        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter title");

        // Amount
        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");

        // Category
        Label categoryLabel = new Label("Category:");
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Select category");

        // Type
        Label typeLabel = new Label("Type:");
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton incomeBtn = new RadioButton("Income");
        incomeBtn.setToggleGroup(typeGroup);
        RadioButton expenseBtn = new RadioButton("Expense");
        expenseBtn.setToggleGroup(typeGroup);
        expenseBtn.setSelected(true); // Default to Expense

        // Set default categories for Expense
        categoryBox.getItems().setAll("Dining Out", "Bills", "Entertainment", "Gas", "Groceries", "Shopping", "Other");
        categoryBox.setValue("Dining Out");

        // Adjust category options when type changes
        typeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                RadioButton selected = (RadioButton) newVal;
                String selectedType = selected.getText();

                categoryBox.getItems().clear();
                if (selectedType.equals("Income")) {
                    categoryBox.getItems().add("Income");
                    categoryBox.setValue("Income");
                } else {
                    categoryBox.getItems().addAll("Dining Out", "Bills", "Entertainment", "Gas", "Groceries", "Shopping", "Other");
                    categoryBox.setValue("Dining Out");
                }
            }
        });

        // Date
        Label dateLabel = new Label("Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        // Submit
        Button submitBtn = new Button("Add Transaction");
        submitBtn.setOnAction(e -> {
            String title = titleField.getText();
            String amountText = amountField.getText();
            String category = categoryBox.getValue();
            String type = ((RadioButton) typeGroup.getSelectedToggle()).getText();
            LocalDate date = datePicker.getValue();

            if (title.isEmpty() || amountText.isEmpty() || category == null || type == null || date == null) {
                showAlert("Validation Error", "All fields must be filled out.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                showAlert("Validation Error", "Amount must be a number.");
                return;
            }

            transactionList.add(new TransactionEntry(title, amount, category, type, date));

            titleField.clear();
            amountField.clear();
            if (expenseBtn.isSelected()) {
                categoryBox.setValue("Dining Out");
            } else {
                categoryBox.setValue("Income");
            }
            datePicker.setValue(LocalDate.now());
        });

        // Layout
        HBox typeBox = new HBox(10, incomeBtn, expenseBtn);
        VBox layout = new VBox(10,
            titleLabel, titleField,
            amountLabel, amountField,
            categoryLabel, categoryBox,
            typeLabel, typeBox,
            dateLabel, datePicker,
            submitBtn
        );
        layout.setPadding(new Insets(15));
        return layout;
    }



    // Displays all the data gathered from the entry form in a table
    private VBox buildTransactionTable(ObservableList<TransactionEntry> transactionList) {
        // Filtered list for dynamic filtering
        FilteredList<TransactionEntry> filteredData = new FilteredList<>(transactionList, p -> true);

        // ComboBox for Type filter
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All", "Income", "Expense");
        typeFilter.setValue("All");

        // ComboBox for Category filter
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All", "Dining Out", "Bills", "Entertainment", "Gas", "Groceries", "Shopping", "Other", "Income");
        categoryFilter.setValue("All");

        // Apply filters on selection change
        typeFilter.setOnAction(e -> applyFilter(typeFilter, categoryFilter, filteredData));
        categoryFilter.setOnAction(e -> applyFilter(typeFilter, categoryFilter, filteredData));

        TableView<TransactionEntry> table = new TableView<>();
        table.setColumnResizePolicy((param) -> true);


        // Table columns
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
        amountCol.setSortable(true);

        TableColumn<TransactionEntry, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));

        TableColumn<TransactionEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getType()));

        TableColumn<TransactionEntry, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDate()));
        dateCol.setSortable(true);

        TableColumn<TransactionEntry, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("x");

            {
                deleteBtn.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                deleteBtn.setOnAction(e -> {
                    TransactionEntry selected = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this transaction?", ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            transactionList.remove(selected);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        
        table.getColumns().addAll(titleCol, amountCol, categoryCol, typeCol, dateCol, deleteCol);

        // Sorted list wraps the filtered list
        SortedList<TransactionEntry> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        HBox filters = new HBox(10, new Label("Type:"), typeFilter, new Label("Category:"), categoryFilter);
        VBox layout = new VBox(10, filters, table);
        layout.setPadding(new Insets(15));
        return layout;
    }

    private void applyFilter(ComboBox<String> typeFilter, ComboBox<String> categoryFilter, FilteredList<TransactionEntry> filteredData) {
        filteredData.setPredicate(entry -> {
            boolean typeMatches = typeFilter.getValue().equals("All") || entry.getType().equalsIgnoreCase(typeFilter.getValue());
            boolean categoryMatches = categoryFilter.getValue().equals("All") || entry.getCategory().equalsIgnoreCase(categoryFilter.getValue());
            return typeMatches && categoryMatches;
        });
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

        // Refresh button
        Button refreshBtn = new Button("Update Summary");

        // Pie chart setup
        PieChart categoryChart = new PieChart();
        categoryChart.setTitle("Spending by Category");
        categoryChart.setLabelsVisible(false);
        categoryChart.setLegendVisible(false);


        // Legend setup (built after pie chart is populated)
        Label legendTitle = new Label("Legend:");
        VBox legend = new VBox(5);

        // Refresh Button: refreshes summary and pie chart
        refreshBtn.setOnAction(e -> {
            double income = transactionList.stream()
                .filter(t -> t.getType().equalsIgnoreCase("Income"))
                .mapToDouble(TransactionEntry::getAmount)
                .sum();

            double expenses = transactionList.stream()
                .filter(t -> t.getType().equalsIgnoreCase("Expense"))
                .mapToDouble(TransactionEntry::getAmount)
                .sum();

            double balance = income - expenses;

            incomeLabel.setText(String.format("Total Income: $%.2f", income));
            expenseLabel.setText(String.format("Total Expenses: $%.2f", expenses));
            balanceLabel.setText(String.format("Net Balance: $%.2f", balance));

            if (balance < 0) {
                showAlert("Warning", "Your balance is negative!");
            }

            // Update chart and legend
            updatePieChart(categoryChart, legend, transactionList);
        });

        
        refreshBtn.fire();

        VBox layout = new VBox(15, incomeLabel, expenseLabel, balanceLabel, refreshBtn, categoryChart, legendTitle, legend);
        layout.setPadding(new Insets(15));
        return layout;
    }

    private void updatePieChart(PieChart chart, VBox legend, ObservableList<TransactionEntry> transactionList) {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (TransactionEntry entry : transactionList) {
            if (entry.getType().equalsIgnoreCase("Expense")) {
                categoryTotals.merge(entry.getCategory(), entry.getAmount(), Double::sum);
            }
        }

        chart.getData().clear();
        legend.getChildren().clear();

        String[] colors = {
            "#e6194b", "#3cb44b", "#ffe119", "#4363d8", "#f58231",
            "#911eb4", "#46f0f0", "#f032e6", "#bcf60c", "#fabebe"
        };

        int i = 0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            chart.getData().add(slice);

            // Assign color to slice
            final String color = colors[i % colors.length];
            slice.getNode().setStyle("-fx-pie-color: " + color + ";");

            // Add to legend
            Label legendItem = new Label(entry.getKey());
            legendItem.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 2 6 2 6;");
            legend.getChildren().add(legendItem);

            i++;
        }
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
