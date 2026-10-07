package com.cinema.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class for managing scene navigation.
 */
public class SceneManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // Reuse existing scene if available, otherwise create new one
            if (primaryStage.getScene() != null) {
                primaryStage.getScene().setRoot(root);
            } else {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(SceneManager.class.getResource("/styles/style.css").toExternalForm());
                primaryStage.setScene(scene);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + fxmlPath);
        }
    }

    public static <T> T switchSceneAndGetController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // Reuse existing scene if available, otherwise create new one
            if (primaryStage.getScene() != null) {
                primaryStage.getScene().setRoot(root);
            } else {
                Scene scene = new Scene(root);
                scene.getStylesheets().add(SceneManager.class.getResource("/styles/style.css").toExternalForm());
                primaryStage.setScene(scene);
            }
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + fxmlPath);
            return null;
        }
    }

    public static FXMLLoader getLoader(String fxmlPath) {
        return new FXMLLoader(SceneManager.class.getResource(fxmlPath));
    }
}
