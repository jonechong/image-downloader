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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

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

    private void mergeImagesIntoPDF(List<String> imageUrls, Path targetDirectory) throws IOException {
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new IllegalArgumentException("Image URLs list cannot be null or empty.");
        }

        PDDocument document = new PDDocument();
        PDRectangle pageSize = PDRectangle.A4; // Default page size

        // Extract the name from the first image URL and sanitize it
        String firstImageUrl = imageUrls.get(0);
        String pdfName = firstImageUrl.substring(firstImageUrl.lastIndexOf('/') + 1);
        pdfName = sanitizeFileName(pdfName);
        if (!pdfName.toLowerCase().endsWith(".pdf")) {
            pdfName += ".pdf"; // Append .pdf extension if not present
        }
        for (String imageUrl : imageUrls) {
            File tempImageFile = downloadImageToTempFile(imageUrl);
            PDImageXObject pdImage = PDImageXObject.createFromFile(tempImageFile.getAbsolutePath(), document);

            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Scale image to fit the page
            float scale = Math.min(pageSize.getWidth() / pdImage.getWidth(),
                    pageSize.getHeight() / pdImage.getHeight());
            float scaledWidth = pdImage.getWidth() * scale;
            float scaledHeight = pdImage.getHeight() * scale;
            float x = (pageSize.getWidth() - scaledWidth) / 2;
            float y = (pageSize.getHeight() - scaledHeight) / 2;

            contentStream.drawImage(pdImage, x, y, scaledWidth, scaledHeight);
            contentStream.close();

            tempImageFile.delete();
        }

        Path pdfPath = targetDirectory.resolve(pdfName);
        document.save(pdfPath.toFile());
        document.close();
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private File downloadImageToTempFile(String imageUrl) throws IOException {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check if the response code indicates a successful download
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to download: " + imageUrl);
            }

            // Create a temporary file to store the image
            File tempFile = File.createTempFile("image", ".jpg");
            tempFile.deleteOnExit();

            // Copy the downloaded image to the temporary file
            try (InputStream inputStream = connection.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            throw new IOException("Invalid URL: " + imageUrl, e);
        }
    }

    @FXML
    private void handleDownloadPdf() {
        try {
            List<String> urls = extractUrls(urlsTextArea.getText());
            mergeImagesIntoPDF(urls, selectedDirectory.toPath());
        } catch (IOException e) {
            showAlert("Error", "Failed to merge images into PDF", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

}
