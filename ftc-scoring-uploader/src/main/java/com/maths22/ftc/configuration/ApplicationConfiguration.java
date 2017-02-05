package com.maths22.ftc.configuration;

import com.maths22.ftc.ui.ApplicationController;
import javafx.fxml.FXMLLoader;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jacob on 2/1/2017.
 */
@Configuration
@EnableTransactionManagement
public class ApplicationConfiguration {

    @Bean
    public ApplicationController mainPaneController() throws IOException {
        return loadController(ApplicationController.VIEW);
    }

    private <T> T loadController(String url) throws IOException {
        try (InputStream fxmlStream = getClass().getResourceAsStream(url)) {
            FXMLLoader loader = new FXMLLoader();
            loader.load(fxmlStream);
            return loader.getController();
        }
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties springDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource springDataSource() {
        return springDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @ConfigurationProperties("symmetric.datasource")
    public DataSourceProperties symmetricDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("symmetric.datasource")
    public DataSource symmetricDataSource() {
        return symmetricDataSourceProperties().initializeDataSourceBuilder().build();
    }

}
