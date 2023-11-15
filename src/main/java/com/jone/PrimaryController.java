package com.jone;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

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

    private void downloadImage(String imageUrl, Path targetDirectory) throws IOException {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check if the response code indicates a successful download
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to download: " + imageUrl);
            }

            // Get the file name from the URL and sanitize it
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_"); // Replace illegal characters with underscores

            // Ensure the file name ends with ".jpg"
            if (!fileName.toLowerCase().endsWith(".jpg")) {
                fileName += ".jpg";
            }

            Path filePath = targetDirectory.resolve(fileName);

            // Check if the file already exists, and if so, append a number to the file name
            int count = 1;
            while (Files.exists(filePath)) {
                String baseName = fileName.substring(0, fileName.lastIndexOf(".jpg"));
                String extension = ".jpg";
                fileName = baseName + " (" + count + ")" + extension;
                filePath = targetDirectory.resolve(fileName);
                count++;
            }

            // Copy the downloaded image to the selected directory
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IOException("Invalid URL: " + imageUrl, e);
        }
    }

    @FXML
    private void handleDownloadImages() {
        String text = urlsTextArea.getText();
        List<String> urls = extractUrls(text);
        for (String url : urls) {
            try {
                downloadImage(url, selectedDirectory.toPath());
            } catch (IOException e) {
                // Handle the error by displaying an alert to the user
                showAlert("Error", "Failed to download image", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String headerText, String contentText, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setGraphic(null); // Because the X is ugly as hell
        alert.showAndWait();
    }

}
