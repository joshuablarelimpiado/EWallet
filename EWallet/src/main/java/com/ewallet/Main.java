package com.ewallet;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        DataStore.load();

        primaryStage.setTitle("E-Wallet");
        SceneManager.init(primaryStage);
        SceneManager.showLogin();

        primaryStage.setOnCloseRequest(e -> DataStore.save());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
