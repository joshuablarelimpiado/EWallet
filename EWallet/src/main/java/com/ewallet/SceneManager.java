package com.ewallet;

import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/** Small helper that swaps the root of the single primary Stage. */
public class SceneManager {

    private static Stage stage;
    private static Scene scene;

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void showLogin() {
        setRoot(new LoginView().getView(), "E-Wallet - Login", 420, 480);
    }

    public static void showRegister() {
        setRoot(new RegisterView().getView(), "E-Wallet - Register", 420, 520);
    }

    public static void showDashboard(User user) {
        setRoot(new DashboardView(user).getView(), "E-Wallet - " + user.getUsername(), 640, 560);
    }

    private static void setRoot(Region root, String title, double w, double h) {
        if (scene == null) {
            scene = new Scene(root, w, h);
            scene.getStylesheets().add(SceneManager.class.getResource("/style.css").toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            stage.setWidth(w);
            stage.setHeight(h);
        }
        stage.setTitle(title);
    }
}
