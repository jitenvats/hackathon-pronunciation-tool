package com.wellsfargo.hackathon.handlers;

import com.wellsfargo.hackathon.controller.UserController;
import com.wellsfargo.hackathon.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Component
public class UserProfileHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileHandler.class);

    @Value("${okta.apis.baseUri}")
    private String oktaApiBaseUri;

    @Value("${okta.apis.userEndPoint}")
    private String oktaUserEndPoint;

    private RestTemplate restTemplate;

    @Autowired
    public UserProfileHandler(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public List<UserProfile> getProfileByName(String firstName, String lastName) throws URISyntaxException {

        String searchParam = "firstName sw "+firstName+ " and lastName sw "+lastName;
        URI uri = new URI(oktaApiBaseUri+oktaUserEndPoint+"?search="+searchParam);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        //TODO : Get it from Spring Security Context
        headers.set("Authorization", "SSWS 00UyT7qbp1Ak6Z2-UMjqxBO7owSIN5m2roynJJN3yD");

        HttpEntity requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<UserProfile[]> result = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, UserProfile[].class);
        UserProfile[] profiles = result.getBody();

        return Arrays.asList(profiles);
    }
}
