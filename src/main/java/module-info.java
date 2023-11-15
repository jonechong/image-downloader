module com.jone {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.jone to javafx.fxml;
    exports com.jone;
}
