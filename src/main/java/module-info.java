module com.jone {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.pdfbox;

    opens com.jone to javafx.fxml;

    exports com.jone;
}
