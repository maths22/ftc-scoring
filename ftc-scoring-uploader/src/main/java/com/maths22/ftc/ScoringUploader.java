package com.maths22.ftc;

import com.maths22.ftc.ui.ApplicationController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by Jacob on 2/1/2017.
 */
@SpringBootApplication
public class ScoringUploader extends Application {
    private static String[] args;

    public static void main(String[] args) {
        ScoringUploader.args = args;
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {

        // Bootstrap Spring context here.
        ConfigurableApplicationContext context = SpringApplication.run(ScoringUploader.class, args);

        // Create a Scene
        ApplicationController mainPaneController = context.getBean(ApplicationController.class);
        Scene scene = new Scene((Parent) mainPaneController.getRoot());

        // Set the scene on the primary stage
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            try {
                context.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        primaryStage.show();


    }
}
