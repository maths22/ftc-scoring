package com.maths22.ftc.services;

import org.apache.commons.dbcp.BasicDataSource;
import org.jumpmind.security.ISecurityService;
import org.jumpmind.security.SecurityService;
import org.jumpmind.security.SecurityServiceFactory;
import org.jumpmind.symmetric.ClientSymmetricEngine;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.jumpmind.symmetric.service.IRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;

/**
 * Created by Jacob on 2/3/2017.
 */
@RestController
public class RegistrationManager {
    private static final Logger log = LoggerFactory.getLogger(RegistrationManager.class);

    private ISymmetricEngine engine;

    @Value("classpath:/master.properties")
    private Resource propertiesFile;

    ISecurityService securityService = SecurityServiceFactory.create();

    @RequestMapping(value = "/activateRegistration", method = RequestMethod.POST)
    public String activateRegistration(@RequestParam("eventKey") String eventKey, @RequestParam("eventKey") String password) {
        IRegistrationService registrationService = this.getSymmetricEngine(true).getRegistrationService();

        String externalId = eventKey + "$" + securityService.nextSecureHexString(10);

        String registeredNode = registrationService.openRegistration("event", externalId);
        log.info("Opened registration for node '{}'", registeredNode);
        return externalId;
    }

    //Adapted from AbstractCommandLauncher
    protected ISymmetricEngine getSymmetricEngine(boolean testConnection) {
        if(this.engine == null) {
            if(testConnection) {
                this.testConnection();
            }

            try {
                this.engine = new ClientSymmetricEngine(propertiesFile.getFile());
            } catch (IOException e) {
                log.error("Invalid master.properties file");
            }
        }

        return this.engine;
    }

    protected void testConnection() {
        try {
            BasicDataSource e = ClientSymmetricEngine.createBasicDataSource(this.propertiesFile.getFile());
            Connection conn = e.getConnection();
            conn.close();
            e.close();
        } catch (Exception var3) {
            throw new RuntimeException(var3);
        }
    }
}
