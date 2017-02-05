package com.maths22.ftc.services;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jumpmind.symmetric.ClientSymmetricEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Jacob on 2/3/2017.
 */
@Component
public class SyncManager {
    private static final Logger log = LoggerFactory.getLogger(SyncManager.class);
    private ClientSymmetricEngine engine;

    @Autowired
    @Qualifier("symmetricDataSource")
    private DataSource symmetricDataSource;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext springContext;

    @Value("${symmetric.azure.registration}")
    private String registrationUrl;

    @Value("${symmetric.azure.sync}")
    private String syncUrl;
    
    public void start(String eventKey, String password) {

        boolean mustAuth = true;
        String externalId = "";
        try {
            //This statement is necessary to fix a shortcoming in H2 multi-schema support (bug workaround)
            dataSource.getConnection().prepareStatement("CREATE ALIAS IF NOT EXISTS sym_BASE64_ENCODE for\n" +
                    "\"org.jumpmind.symmetric.db.EmbeddedDbFunctions.encodeBase64\";").execute();

            ResultSet result = dataSource.getConnection().prepareStatement("SELECT NODE.NODE_GROUP_ID as \"group_id\", NODE.EXTERNAL_ID as \"external_id\" FROM SYM.SYM_NODE_IDENTITY AS NODE_ID\n" +
                    "  INNER JOIN SYM.SYM_NODE AS NODE\n" +
                    "    ON NODE_ID.NODE_ID = NODE.NODE_ID").executeQuery();
            if(result.first()) {
                String retGroupId = result.getString("group_id");
                externalId = result.getString("external_id");
                String retExternalId = externalId.split("\\$")[0];

                if (!retGroupId.equals("event") || !retExternalId.equals(eventKey)) {
                    log.info("Dropping existing SymmetricDS config");
                    dataSource.getConnection().prepareStatement("DROP SCHEMA SYM").execute();
                } else {
                    mustAuth = false;
                }
            }
        } catch (SQLException e) {
            log.info("Unable to verify current node id");
        }

        if(mustAuth) {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(registrationUrl);

            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("eventKey", eventKey));
            params.add(new BasicNameValuePair("password", password));
            try {
                httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("UTF-8 is unsupported!?", e);
            }

            HttpResponse response = null;
            try {
                response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    try (InputStream instream = entity.getContent()) {
                        externalId = IOUtils.toString(instream, "UTF-8").trim();
                    }
                }
            } catch (IOException e) {
                log.error("Unable to active registration", e);
            }

        }

        Properties props = new Properties();
        props.put("group.id", "event");
        props.put("external.id", externalId);
        props.put("registration.url", syncUrl);


        try {
            dataSource.getConnection().prepareStatement("CREATE SCHEMA IF NOT EXISTS SYM").execute();
        } catch (SQLException e) {
            log.warn("Could not create schema");
        }

        engine = new ClientSymmetricEngine(symmetricDataSource, springContext, props, false);
        engine.setup();
        engine.start();
        engine.getRegistrationService().registerWithServer();
    }

    public void stop() {
        engine.stop();
    }

    //TODO implement status monitoring
}
