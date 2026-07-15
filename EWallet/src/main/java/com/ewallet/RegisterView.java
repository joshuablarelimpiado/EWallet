package com.ewallet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegisterView {

    private final VBox root = new VBox(14);

    public RegisterView() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root");

        Label title = new Label("Create Account");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Sign up to start using E-Wallet");
        subtitle.getStyleClass().add("subtitle-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.getStyleClass().add("field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.getStyleClass().add("field");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");
        confirmField.getStyleClass().add("field");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }
            if (!password.equals(confirm)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }
            if (password.length() < 4) {
                errorLabel.setText("Password must be at least 4 characters.");
                return;
            }

            boolean success = DataStore.registerUser(username, password);
            if (!success) {
                errorLabel.setText("That username is already taken.");
                return;
            }

            SceneManager.showLogin();
        });

        Button backBtn = new Button("Back to Login");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> SceneManager.showLogin());

        VBox form = new VBox(12, usernameField, passwordField, confirmField, errorLabel, registerBtn, backBtn);
        form.setMaxWidth(280);
        form.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, subtitle, form);
    }

    public VBox getView() {
        return root;
    }
}
