module TempSocket {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens lk.ijse to javafx.fxml;
    exports lk.ijse;
    exports lk.ijse.temp;
    opens lk.ijse.temp to javafx.fxml;
}