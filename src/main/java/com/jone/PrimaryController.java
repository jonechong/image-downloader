package com.jone;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.net.URL;

public class PrimaryController implements Initializable {

    @FXML
    private VBox rootVBox;
    @FXML
    private Label selectedDirectoryLabel;
    @FXML
    private TextArea urlsTextArea;
    @FXML
    private Button downloadImagesButton;
    @FXML
    private Button downloadPdfButton;

    private File selectedDirectory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This will request focus on the root VBox when the scene is loaded
        Platform.runLater(() -> rootVBox.requestFocus());
    }

    @FXML
    private void handleSelectDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Download Directory");

        File chosenDirectory = directoryChooser.showDialog(null);

        if (chosenDirectory != null) {
            selectedDirectory = chosenDirectory;
            selectedDirectoryLabel.setText(selectedDirectory.getAbsolutePath());
            this.selectedDirectoryLabel.setText(selectedDirectory.getAbsolutePath());
            downloadImagesButton.setDisable(false);
            downloadPdfButton.setDisable(false);
        } else {
            return;
        }
    }

    private List<String> extractUrls(String text) {
        String[] splitText = text.split("\\s+|,|\n");
        return new ArrayList<>(Arrays.asList(splitText));
    }

    @FXML
    private void handleDownloadImages() {
        String text = urlsTextArea.getText();
        List<String> urls = extractUrls(text);
        // For now, just log the URLs
        for (String url : urls) {
            System.out.println(url);
        }
    }

}
