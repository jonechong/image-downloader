package com.jone;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimaryController {
    @FXML
    private Label selectedDirectoryLabel;
    @FXML
    private TextArea urlsTextArea;
    @FXML
    private Button downloadImagesButton;
    @FXML
    private Button downloadPdfButton;

    private File selectedDirectory;

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
        String[] splitText = text.split(",|\n");
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
