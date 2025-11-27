module com.maxi.albumartsetter_v2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.maxi.albumartsetter_v2 to javafx.fxml;
    exports com.maxi.albumartsetter_v2;
}