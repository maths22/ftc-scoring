package com.maths22.ftc.ui;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by Jacob on 2/8/2017.
 */
public class ApplicationPreloader extends Preloader {
    Label label;
    ProgressBar bar;
    Stage stage;
    VBox vbox;

    private Scene createPreloaderScene() {
        vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        label = new Label();
        label.setText("Loading...");
        label.setFont(Font.font(24));
        bar = new ProgressBar();
        bar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        vbox.getChildren().add(label);
        vbox.getChildren().add(bar);
        BorderPane p = new BorderPane();
        p.setCenter(vbox);
        return new Scene(p, 300, 150);
    }

    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(createPreloaderScene());
        stage.show();
    }

//    @Override
//    public void handleProgressNotification(ProgressNotification pn) {
//        bar.setProgress(pn.getProgress());
//    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }
}