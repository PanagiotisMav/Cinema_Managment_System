package com.cinema.controller;

import com.cinema.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for a generic success screen that can be reused.
 */
public class GenericSuccessController {

    @FXML
    private Label messageLabel;

    @FXML
    private Label additionalInfoLabel;

    private String returnPath = "/fxml/login.fxml";
    
    // Static fields for setting message before navigation
    private static String staticMessage;
    private static String staticAdditionalInfo;
    private static String staticReturnPath = "/fxml/login.fxml";

    @FXML
    public void initialize() {
        // Load static message if set
        if (staticMessage != null) {
            messageLabel.setText(staticMessage);
        }
        if (staticAdditionalInfo != null && !staticAdditionalInfo.isEmpty()) {
            additionalInfoLabel.setText(staticAdditionalInfo);
            additionalInfoLabel.setVisible(true);
            additionalInfoLabel.setManaged(true);
        }
        if (staticReturnPath != null) {
            returnPath = staticReturnPath;
        }
    }

    /**
     * Sets the success message statically before navigation.
     */
    public static void setSuccessMessage(String message, String additionalInfo) {
        staticMessage = message;
        staticAdditionalInfo = additionalInfo;
    }

    /**
     * Sets the return scene path statically before navigation.
     */
    public static void setReturnScene(String path) {
        staticReturnPath = path;
    }

    public void setMessage(String message, String returnPath) {
        this.messageLabel.setText(message);
        this.returnPath = returnPath;
    }

    public void setMessage(String message, String additionalInfo, String returnPath) {
        this.messageLabel.setText(message);
        this.returnPath = returnPath;
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            this.additionalInfoLabel.setText(additionalInfo);
            this.additionalInfoLabel.setVisible(true);
            this.additionalInfoLabel.setManaged(true);
        }
    }

    @FXML
    private void handleReturn() {
        SceneManager.switchScene(returnPath);
    }
}
