module FoundationCode {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;
    
    opens application to javafx.base, javafx.graphics, javafx.controls;
}