package com.wellsfargo.hackathon.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellsfargo.hackathon.exception.BadRequestException;
import com.wellsfargo.hackathon.exception.ContentTypeException;
import com.wellsfargo.hackathon.exception.ExternalSystemException;
import com.wellsfargo.hackathon.model.Employee;
import com.wellsfargo.hackathon.model.EmployeeEntity;
import com.wellsfargo.hackathon.model.EmployeeResponse;
import com.wellsfargo.hackathon.model.UserProfile;
import com.wellsfargo.hackathon.service.EmployeeService;
import com.wellsfargo.hackathon.service.TranslationService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.beanutils.BeanUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.*;
import javax.websocket.server.PathParam;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class UserController {


	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@Value("${okta.apis.baseUri}")
	private String oktaApiBaseUri;

	@Value("${okta.apis.userEndPoint}")
	private String oktaUserEndPoint;

	private RestTemplate restTemplate;

	@Autowired
	public UserController(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	@GetMapping(value = "/users",  produces = {MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "List of Users based on search param", response = EmployeeResponse.class)
	public ResponseEntity<UserProfile[]> users(@RequestParam("search") String search) throws ExternalSystemException, BadRequestException, URISyntaxException, JsonProcessingException {
		LOGGER.info("GOOGLE_APPLICATION_CREDENTIALS :" + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

		URI uri = new URI(oktaApiBaseUri+oktaUserEndPoint+"?search="+search);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");
		//TODO : Get it from Spring Security Context
		headers.set("Authorization", "SSWS 00UyT7qbp1Ak6Z2-UMjqxBO7owSIN5m2roynJJN3yD");

		HttpEntity requestEntity = new HttpEntity<>(null, headers);
		ResponseEntity<UserProfile[]> result = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, UserProfile[].class);
		UserProfile[] profiles = result.getBody();


		return new ResponseEntity<UserProfile[] >(profiles, HttpStatus.OK);
	}


	@GetMapping(value = "/users/{logIn}",  produces = {MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "Get user by login", response = UserProfile.class)
	public ResponseEntity<UserProfile> userById(@PathVariable("logIn") String logIn) throws ExternalSystemException, BadRequestException, URISyntaxException, JsonProcessingException {
		LOGGER.info("GOOGLE_APPLICATION_CREDENTIALS :" + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

		String endPoint = oktaApiBaseUri + oktaUserEndPoint + "/" + logIn;
		URI uri = new URI(endPoint);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Accept", "application/json");
		//TODO : Get it from Spring Security Context
		headers.set("Authorization", "SSWS 00UyT7qbp1Ak6Z2-UMjqxBO7owSIN5m2roynJJN3yD");

		HttpEntity requestEntity = new HttpEntity<>(null, headers);
		ResponseEntity<UserProfile> result = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, UserProfile.class);
		UserProfile profile = result.getBody();

		return new ResponseEntity<UserProfile >(profile, HttpStatus.OK);
	}
	
	@GetMapping(value = "/health", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> healthCheck() throws Exception {
		return ResponseEntity.ok().body("OK");

	}

}
