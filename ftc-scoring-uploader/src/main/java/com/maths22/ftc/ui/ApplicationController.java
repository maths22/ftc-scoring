package com.maths22.ftc.ui;

import com.maths22.ftc.entities.Season;
import com.maths22.ftc.repositories.SeasonRepository;
import com.maths22.ftc.services.ScoringMonitor;
import com.maths22.ftc.services.SyncManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Created by Jacob on 2/1/2017.
 */
public class ApplicationController {
    public static final String VIEW = "/ui/uploader.fxml";

    @Autowired
    private ScoringMonitor scoringMonitor;

    @Autowired
    private SyncManager syncManager;

    @Autowired
    private SeasonRepository seasonRepository;

    @FXML
    private Node root;

    @FXML
    private Button myButton;

    @FXML
    private TextField scoringSystemPathField;

    @FXML
    private TextField eventKeyField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public ChoiceBox<Season> seasonSelector;


    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    @PostConstruct
    public void init() {
        ObservableList<Season> observableList = FXCollections.observableArrayList();
        for(Season season : seasonRepository.findAll()) {
            observableList.add(season);
        }
        seasonSelector.setItems(observableList);
        seasonSelector.setConverter(new StringConverter<Season>() {
            @Override
            public String toString(Season object) {
                return object.getName() + " (" + object.getYear() + ")";
            }

            @Override
            public Season fromString(String string) {
                return null;
            }
        });
        if(observableList.size() > 0) {
            seasonSelector.setValue(observableList.get(0));
        }
    }

    public Node getRoot() {
        return root;
    }

    @FXML
    private void onButtonPress(ActionEvent event)
    {
        File result = directoryChooser.showDialog(root.getScene().getWindow());
        if(result != null) {
            scoringSystemPathField.setText(result.getAbsolutePath());
        }
    }

    @FXML
    private void startImporting(ActionEvent event)
    {
        String key = eventKeyField.getText();
        String password = passwordField.getText();
        if(!key.matches("[a-zA-Z0-9-]{5,}") || password.length() == 0) {
            //TODO use real key validation, etc
            return;
        }

        syncManager.start(key, password);

        myButton.setDisable(true);
        scoringSystemPathField.setDisable(true);
        String path = scoringSystemPathField.getText();
        scoringMonitor.setEvent(key, path);
        scoringMonitor.setSeason(seasonSelector.getValue());
        scoringMonitor.start();
    }

    @FXML
    private void stopImporting(ActionEvent event)
    {
        myButton.setDisable(false);
        scoringSystemPathField.setDisable(false);
        scoringMonitor.stop();
        syncManager.stop();
    }
}
