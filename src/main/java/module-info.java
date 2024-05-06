module com.javafx.onlinekaraokesongsmanager {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;

    opens com.javafx.onlinekaraokesongsmanager to javafx.fxml;
    exports com.javafx.onlinekaraokesongsmanager;
}