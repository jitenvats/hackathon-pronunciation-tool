package com.wellsfargo.hackathon.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.wellsfargo.hackathon.exception.BadRequestException;
import com.wellsfargo.hackathon.exception.ContentTypeException;
import com.wellsfargo.hackathon.exception.ExternalSystemException;
import com.wellsfargo.hackathon.handlers.UserProfileHandler;
import com.wellsfargo.hackathon.model.Employee;
import com.wellsfargo.hackathon.model.EmployeeEntity;
import com.wellsfargo.hackathon.model.EmployeeResponse;
import com.wellsfargo.hackathon.model.UserProfile;
import com.wellsfargo.hackathon.service.EmployeeService;
import com.wellsfargo.hackathon.service.TranslationService;
import com.wellsfargo.hackathon.util.PronunciationType;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
public class PronunciationAPIController {

	private final String AUDIO_CONTENT_TYPE = "audio/";
	private static final Logger LOGGER = LoggerFactory.getLogger(PronunciationAPIController.class);
	private EmployeeService employeeService;
	private TranslationService translationService;
	private UserProfileHandler userProfileHandler;

	@Autowired
	public PronunciationAPIController(EmployeeService employeeService, TranslationService translationService,
			UserProfileHandler userProfileHandler) {
		this.employeeService = employeeService;
		this.translationService = translationService;
		this.userProfileHandler = userProfileHandler;
	}

	@PostMapping(value = "/translate", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "Translate Employee Name Based on Pronunciation Type and Language", response = EmployeeResponse.class)
	public ResponseEntity<EmployeeResponse> translateName(@Valid @RequestBody Employee employee)
			throws ExternalSystemException, BadRequestException {
		LOGGER.info("GOOGLE_APPLICATION_CREDENTIALS :" + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
		LOGGER.info("Requested Employee : {}", employee);

		EmployeeEntity entity = new EmployeeEntity();
		try {
			BeanUtils.copyProperties(entity, employee);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		EmployeeResponse savedEmployee = employeeService.saveEmployee(entity, employee.getPronunciationType(),
				employee.getLanguage(), true, employee.getSpeed());
		return new ResponseEntity<EmployeeResponse>(savedEmployee, HttpStatus.CREATED);
	}

	@GetMapping(value = "/pronunce/{employeeId}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	@ApiOperation(value = "Get Translated Employee Name Based on Employee ID", response = StreamingResponseBody.class)
	public ResponseEntity<StreamingResponseBody> getPronunciation(@PathVariable("employeeId") String employeeId,
			Authentication auth) throws Exception {
		EmployeeEntity employee = employeeService.getEmployeeDetailsWilNull(employeeId);
		LOGGER.info("Employee : {}", employee);
		StreamingResponseBody responseBody = null;
		if (employee == null) {
			LOGGER.info("Employee Details not found in DB");
			OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) auth;
			String employeeName = (String) token.getPrincipal().getAttributes().get("name");
			LOGGER.info("Employee Name from  Authentication : {}", employeeName);
			responseBody = response -> {
				try {
					response.write(translationService
							.translateEmployeeName(employeeName, PronunciationType.MALE, null, 1).toByteArray());
				} catch (ExternalSystemException e) {
					e.printStackTrace();
				}
			};
			return ResponseEntity.ok().body(responseBody);
		} else {
			LOGGER.info("Employee : {}", employee);
			responseBody = response -> {
				response.write(employee.getPronunciation().getData());
			};
			return ResponseEntity.ok().body(responseBody);
		}
	}

	@GetMapping(value = "/pronunce/{firstName}/{lastName}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	@ApiOperation(value = "Get Translated Employee Name Based on firstName and lastName ", response = StreamingResponseBody.class)
	public ResponseEntity<StreamingResponseBody> getPronunciation(@PathVariable("firstName") String firstName,
			@PathVariable("lastName") String lastName) throws Exception {

		List<UserProfile> userProfileList = userProfileHandler.getProfileByName(firstName, lastName);

		if (userProfileList.size() > 1 || userProfileList.size() == 0) {
			return ResponseEntity.notFound().build();
		}
		UserProfile userProfile = userProfileList.get(0);
		EmployeeEntity employee = employeeService.getEmployeeDetails(userProfile.getId());

		LOGGER.info("Employee : {}", employee);
		StreamingResponseBody responseBody = response -> {
			response.write(employee.getPronunciation().getData());
		};

		return ResponseEntity.ok().body(responseBody);

	}

	@PutMapping(value = "/pronunce/{employeeId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "Update Custom Employee Name Pronunciation Based on Employee ID", response = EmployeeResponse.class)
	public ResponseEntity<EmployeeResponse> updatePronunciation(@PathVariable("employeeId") String employeeId,
			@RequestParam("file") MultipartFile document) throws Exception {

		LOGGER.info("File Type : {}", document.getContentType());

		if (!document.getContentType().startsWith(AUDIO_CONTENT_TYPE)) {
			throw new ContentTypeException("Not a Valid Audio File", "E-0002");
		}

		EmployeeEntity employee = employeeService.getEmployeeDetails(employeeId);
		LOGGER.info("Employee : {}", employee);

		employee.setPronunciation(new Binary(BsonBinarySubType.BINARY, document.getBytes()));
		EmployeeResponse updatedEmployee = employeeService.saveEmployee(employee, null, null, false, 1);
		return new ResponseEntity<EmployeeResponse>(updatedEmployee, HttpStatus.ACCEPTED);

	}

	@DeleteMapping(value = "/pronunce/{employeeId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "Delete Employee and Name Pronunciation Based on Employee ID", response = Void.class)
	public ResponseEntity<Void> deletePronunciation(@PathVariable("employeeId") String employeeId) throws Exception {
		LOGGER.info("Deleting Employee : {}", employeeId);
		employeeService.deleteEmployee(employeeId);
		return ResponseEntity.noContent().build();

	}

	@GetMapping(value = "/language", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "Get Supported Language for Translation", response = StreamingResponseBody.class)
	public ResponseEntity<List<String>> getSupportedLanguage() throws Exception {
		return ResponseEntity.ok().body(translationService.listAllSupportedVoices());

	}

	@GetMapping(value = "/hello", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getHello() throws Exception {
		return ResponseEntity.ok().body("Hello World");

	}

	@GetMapping(value = "/hello2", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getHello2(Authentication auth) throws Exception {

		OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) auth;

		System.out.println("principle:" + auth.toString());
		return ResponseEntity.ok().body("After Authentication :" + token.getPrincipal().getAttributes().get("name"));

	}

}
