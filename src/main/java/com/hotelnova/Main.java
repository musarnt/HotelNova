package com.hotelnova;

import com.hotelnova.ui.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point of the HotelNova application.
 * Extends JavaFX Application to launch the UI.
 * The first screen shown is always the LoginView.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Launch the login screen as the first view
        LoginView loginView = new LoginView(primaryStage);
        loginView.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}