package com.mozu.base.security;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mozu.api.MozuConfig;
import com.mozu.api.contracts.appdev.AppAuthInfo;
import com.mozu.api.security.AppAuthenticator;
import com.mozu.api.utils.MozuHttpClientPool;
import com.mozu.encryptor.PropertyEncryptionUtil;

@Component
public class MozuAppAuthenticator {
	private static final Logger logger = LoggerFactory.getLogger(MozuAppAuthenticator.class);
	
	@Value("${ApplicationId}")
	String applicationId;
    @Value("${SharedSecret}")
    String sharerdSecret;
    @Value("${BaseAuthAppUrl}")
    String baseAppAuthUrl;
    @Value("${spice}")
    String spice;
	
	@PostConstruct
	public void appAuthentication() {
		
		logger.info("Authenticating Application in Mozu...");
		try {
			String realSharedSecret = PropertyEncryptionUtil.decryptProperty(spice,  sharerdSecret);
            AppAuthInfo appAuthInfo = new AppAuthInfo();
            appAuthInfo.setApplicationId(applicationId);
            appAuthInfo.setSharedSecret(realSharedSecret);
            if (!StringUtils.isEmpty(baseAppAuthUrl))
            	MozuConfig.setBaseUrl(baseAppAuthUrl);
            AppAuthenticator.initialize(appAuthInfo);
            logger.info("Auth ticket : "+AppAuthenticator.getInstance().getAppAuthTicket().getAccessToken());
            logger.info("Application authenticated");
            realSharedSecret = "";
		} catch(Exception exc) {
			logger.error(exc.getMessage(), exc);
		}
		
	}

    @PreDestroy
    public void cleanup () {
        logger.debug("Shutdown HttpClient connection manager.");
        MozuHttpClientPool.getInstance().shutdown();
    }
}
