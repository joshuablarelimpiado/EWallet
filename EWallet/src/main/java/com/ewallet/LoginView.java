package com.ewallet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView {

    private final VBox root = new VBox(14);

    public LoginView() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("root");

        Label title = new Label("E-Wallet");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Log in to manage your balance");
        subtitle.getStyleClass().add("subtitle-label");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("field");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        Button loginBtn = new Button("Log In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both username and password.");
                return;
            }

            User user = DataStore.findUser(username);
            if (user == null || !user.getPassword().equals(password)) {
                errorLabel.setText("Invalid username or password.");
                return;
            }

            SceneManager.showDashboard(user);
        });

        Label registerLink = new Label("Don't have an account? Register here");
        registerLink.getStyleClass().add("link-label");
        registerLink.setOnMouseClicked(e -> SceneManager.showRegister());

        VBox form = new VBox(12, usernameField, passwordField, errorLabel, loginBtn);
        form.setMaxWidth(280);
        form.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, subtitle, form, registerLink);
    }

    public VBox getView() {
        return root;
    }
}
