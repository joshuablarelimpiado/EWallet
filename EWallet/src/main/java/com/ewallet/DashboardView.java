package com.ewallet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class DashboardView {

    private final BorderPane root = new BorderPane();
    private final User user;

    private Label balanceAmount;
    private ListView<String> historyList;
    private Label statusLabel;

    public DashboardView(User user) {
        this.user = user;
        root.getStyleClass().add("root");
        root.setPadding(new Insets(24));

        root.setTop(buildHeader());
        root.setCenter(user.isAdmin() ? buildAdminCenter() : buildCenter());

        BorderPane.setMargin(root.getCenter(), new Insets(20, 0, 0, 0));
    }

    private HBox buildHeader() {
        String label = user.isAdmin() ? "Admin: " + user.getUsername() : "Hi, " + user.getUsername();
        Label usernameLabel = new Label(label);
        usernameLabel.getStyleClass().add("username-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label logout = new Label("Log out");
        logout.getStyleClass().add("logout-label");
        logout.setOnMouseClicked(e -> {
            DataStore.save();
            SceneManager.showLogin();
        });

        HBox header = new HBox(10, usernameLabel, spacer, logout);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox buildAdminCenter() {
        Label sectionLabel = new Label("All Users");
        sectionLabel.getStyleClass().add("section-label");

        ListView<String> userList = new ListView<>();
        userList.getStyleClass().add("history-list");
        userList.setPlaceholder(new Label("No registered users yet."));

        for (User u : DataStore.getUsers().values()) {
            if (u.isAdmin()) continue; // don't list the admin account itself
            userList.getItems().add(String.format("%-20s Balance: $%,.2f    (%d transactions)",
                    u.getUsername(), u.getBalance(), u.getHistory().size()));
        }

        VBox.setVgrow(userList, Priority.ALWAYS);

        Label note = new Label("Read-only view of all wallet accounts.");
        note.getStyleClass().add("subtitle-label");

        VBox center = new VBox(10, sectionLabel, note, userList);
        VBox.setVgrow(userList, Priority.ALWAYS);
        return center;
    }

    private VBox buildCenter() {
        VBox balanceCard = buildBalanceCard();
        HBox actions = buildActions();
        VBox historySection = buildHistorySection();

        VBox center = new VBox(20, balanceCard, actions, historySection);
        VBox.setVgrow(historySection, Priority.ALWAYS);
        return center;
    }

    private VBox buildBalanceCard() {
        Label label = new Label("Available Balance");
        label.getStyleClass().add("balance-label");

        balanceAmount = new Label(formatMoney(user.getBalance()));
        balanceAmount.getStyleClass().add("balance-amount");

        VBox card = new VBox(6, label, balanceAmount);
        card.getStyleClass().add("balance-card");
        return card;
    }

    private HBox buildActions() {
        Button depositBtn = new Button("Deposit");
        depositBtn.getStyleClass().add("btn-primary");
        depositBtn.setOnAction(e -> showAmountDialog("Deposit Funds", "Amount to deposit:", amount -> {
            user.deposit(amount);
            refresh("Deposited " + formatMoney(amount) + ".");
        }));

        Button withdrawBtn = new Button("Withdraw");
        withdrawBtn.getStyleClass().add("btn-secondary");
        withdrawBtn.setOnAction(e -> showAmountDialog("Withdraw Funds", "Amount to withdraw:", amount -> {
            if (user.withdraw(amount)) {
                refresh("Withdrew " + formatMoney(amount) + ".");
            } else {
                statusLabel.getStyleClass().setAll("error-label");
                statusLabel.setText("Insufficient balance.");
            }
        }));

        Button transferBtn = new Button("Transfer");
        transferBtn.getStyleClass().add("btn-secondary");
        transferBtn.setOnAction(e -> showTransferDialog());

        HBox actions = new HBox(12, depositBtn, withdrawBtn, transferBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        return actions;
    }

    private VBox buildHistorySection() {
        Label sectionLabel = new Label("Transaction History");
        sectionLabel.getStyleClass().add("section-label");

        statusLabel = new Label();
        statusLabel.getStyleClass().add("success-label");

        historyList = new ListView<>();
        historyList.getStyleClass().add("history-list");
        historyList.setPlaceholder(new Label("No transactions yet."));
        loadHistory();
        VBox.setVgrow(historyList, Priority.ALWAYS);

        VBox section = new VBox(8, sectionLabel, statusLabel, historyList);
        return section;
    }

    private void loadHistory() {
        historyList.getItems().clear();
        for (Transaction t : user.getHistory()) {
            historyList.getItems().add(t.getDisplay());
        }
    }

    private void refresh(String message) {
        balanceAmount.setText(formatMoney(user.getBalance()));
        loadHistory();
        statusLabel.getStyleClass().setAll("success-label");
        statusLabel.setText(message);
        DataStore.save();
    }

    private void showAmountDialog(String title, String prompt, java.util.function.DoubleConsumer onConfirm) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);
        dialog.showAndWait().ifPresent(input -> {
            try {
                double amount = Double.parseDouble(input.trim());
                if (amount <= 0) {
                    statusLabel.getStyleClass().setAll("error-label");
                    statusLabel.setText("Enter an amount greater than zero.");
                    return;
                }
                onConfirm.accept(amount);
            } catch (NumberFormatException ex) {
                statusLabel.getStyleClass().setAll("error-label");
                statusLabel.setText("Please enter a valid number.");
            }
        });
    }

    private void showTransferDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Transfer Funds");
        dialog.setHeaderText(null);

        ButtonType sendButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        TextField recipientField = new TextField();
        recipientField.setPromptText("Recipient username");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        VBox content = new VBox(10, recipientField, amountField);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(bt -> {
            if (bt == sendButtonType) {
                String recipientName = recipientField.getText().trim();
                String amountText = amountField.getText().trim();

                if (recipientName.isEmpty() || amountText.isEmpty()) {
                    statusLabel.getStyleClass().setAll("error-label");
                    statusLabel.setText("Please fill in recipient and amount.");
                    return null;
                }
                if (recipientName.equalsIgnoreCase(user.getUsername())) {
                    statusLabel.getStyleClass().setAll("error-label");
                    statusLabel.setText("You can't transfer to yourself.");
                    return null;
                }

                User recipient = DataStore.findUser(recipientName);
                if (recipient == null) {
                    statusLabel.getStyleClass().setAll("error-label");
                    statusLabel.setText("Recipient not found.");
                    return null;
                }

                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        statusLabel.getStyleClass().setAll("error-label");
                        statusLabel.setText("Enter an amount greater than zero.");
                        return null;
                    }
                    if (user.transferOut(amount, recipient.getUsername())) {
                        recipient.transferIn(amount, user.getUsername());
                        refresh("Sent " + formatMoney(amount) + " to " + recipient.getUsername() + ".");
                    } else {
                        statusLabel.getStyleClass().setAll("error-label");
                        statusLabel.setText("Insufficient balance.");
                    }
                } catch (NumberFormatException ex) {
                    statusLabel.getStyleClass().setAll("error-label");
                    statusLabel.setText("Please enter a valid number.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private String formatMoney(double amount) {
        return String.format("$%,.2f", amount);
    }

    public BorderPane getView() {
        return root;
    }
}
