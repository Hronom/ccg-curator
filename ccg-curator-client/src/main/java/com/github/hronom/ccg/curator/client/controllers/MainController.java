package com.github.hronom.ccg.curator.client.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

public class MainController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML
    private MenuBar mbMenuBar;

    @FXML
    private MenuItem miNew;

    @FXML
    private MenuItem miOpen;

    @FXML
    private MenuItem miSave;

    @FXML
    private MenuItem miPrint;

    @FXML
    private MenuItem miPrintSeparately;

    @FXML
    private MenuItem miClose;

    @FXML
    private MenuItem miUndo;

    @FXML
    private MenuItem miRedo;

    @FXML
    private MenuItem miAbout;

    @FXML
    private AnchorPane apProperties;

    private final ConcurrentHashMap<TreeItem<String>, Object>
        objectHashMap
        = new ConcurrentHashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apProperties.getChildren().clear();

        miNew.setOnAction(event -> {
        });

        miOpen.setOnAction(event -> {
        });

        miSave.setOnAction(event -> {
        });

        miPrint.setOnAction(event -> {
        });

        miPrintSeparately.setOnAction(event -> {
        });

        miClose.setOnAction(event -> Platform.exit());

        miUndo.setOnAction(event -> {});

        miAbout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("About");
                alert.setHeaderText("Information");
                WebView webView = new WebView();
                webView.getEngine().loadContent("<html>" + "Authors:" +
                                                "<p>Eugene Tenkaev <a href=\"mailto:hronom@gmail.com\" target=\"_top\">hronom@gmail.com</a></p>" +
                                                "</html>");
                webView.setPrefSize(450, 100);
                alert.getDialogPane().setContent(webView);
                alert.show();
            }
        });
    }
}
