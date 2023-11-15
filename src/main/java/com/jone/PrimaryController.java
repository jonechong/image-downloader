package com.jone;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import java.io.File;

public class PrimaryController {
    @FXML
    private Label selectedDirectoryLabel;
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
        } else {
            return;
        }
    }
}
