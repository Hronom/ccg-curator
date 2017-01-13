package com.github.hronom.ccg.curator.client;

import com.github.hronom.ccg.curator.client.controllers.MainController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CcgOrganizerClientApp extends Application {
    private static final Logger logger = LogManager.getLogger();

    private final String appTitle = "CCG organizer";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Error", throwable);
        });

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(this.getClass().getResource("main.fxml"));
        GridPane gridPane = fxmlLoader.load();
        MainController mainController = fxmlLoader.getController();

        Scene scene = new Scene(gridPane, 1024, 768);

        primaryStage.setTitle(appTitle);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String args[]) {
        Application.launch(args);
    }
}


