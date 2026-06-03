module com.ghads {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Open packages to JavaFX FXML reflection
    opens com.ghads            to javafx.fxml;
    opens com.ghads.controller to javafx.fxml;
    opens com.ghads.model      to javafx.base;

    // Export main packages
    exports com.ghads;
    exports com.ghads.controller;
    exports com.ghads.model;
    exports com.ghads.dao;
    exports com.ghads.dao.impl;
    exports com.ghads.service;
    exports com.ghads.util;
    exports com.ghads.config;
}
