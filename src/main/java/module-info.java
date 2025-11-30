module com.maxi.albumartsetter_v2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires jaudiotagger;
    requires javafx.graphics;
    requires java.net.http;
    requires com.google.gson;
    requires java.logging;


    opens com.maxi.albumartsetter_v2 to javafx.fxml;
    exports com.maxi.albumartsetter_v2;
}