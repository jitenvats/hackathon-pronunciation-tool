package com.wellsfargo.hackathon.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.wellsfargo.hackathon.exception.BadRequestException;
import com.wellsfargo.hackathon.exception.ContentTypeException;
import com.wellsfargo.hackathon.exception.ExternalSystemException;
import com.wellsfargo.hackathon.model.Employee;
import com.wellsfargo.hackathon.model.EmployeeEntity;
import com.wellsfargo.hackathon.model.EmployeeResponse;
import com.wellsfargo.hackathon.service.EmployeeService;
import com.wellsfargo.hackathon.service.TranslationService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
public class PronunciationAPIController {

	private final String AUDIO_CONTENT_TYPE = "audio/";
	private static final Logger LOGGER = LoggerFactory.getLogger(PronunciationAPIController.class);
	private EmployeeService employeeService;
	private TranslationService translationService;

	@Autowired
	public PronunciationAPIController(EmployeeService employeeService, TranslationService translationService) {
		this.employeeService = employeeService;
		this.translationService = translationService; 
	}

	@PostMapping(value = "/translate", consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = {MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "Translate Employee Name Based on Pronunciation Type and Language", response = EmployeeResponse.class)
	public ResponseEntity<EmployeeResponse> translateName(@Valid @RequestBody Employee employee) throws ExternalSystemException, BadRequestException {
		LOGGER.info("GOOGLE_APPLICATION_CREDENTIALS :" + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
		LOGGER.info("Requested Employee : {}", employee);
		
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		
		Set<ConstraintViolation<Object>> validationResult = validator.validate(employee);
		
		if(!validationResult.isEmpty()) {
			throw new BadRequestException(validationResult.iterator().next().getMessage(), "E-0004");
		}

		EmployeeEntity entity = new EmployeeEntity();
		try {BeanUtils.copyProperties(entity,employee);} 
		catch (IllegalAccessException | InvocationTargetException e) {e.printStackTrace();}
		
		EmployeeResponse savedEmployee = employeeService.saveEmployee(entity, employee.getPronunciationType(), employee.getLanguage(), true, employee.getSpeed());
		return new ResponseEntity<EmployeeResponse>(savedEmployee, HttpStatus.CREATED);
	}

	@GetMapping(value = "/pronunce/{employeeId}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE })
	@ApiOperation(value = "Get Translated Employee Name Based on Employee ID", response = StreamingResponseBody.class)
	public ResponseEntity<StreamingResponseBody> getPronunciation(@PathVariable("employeeId") String employeeId) throws Exception {
		EmployeeEntity employee = employeeService.getEmployeeDetails(employeeId);
		LOGGER.info("Employee : {}", employee);
		StreamingResponseBody responseBody = response -> {response.write(employee.getPronunciation().getData());};

		return ResponseEntity.ok().body(responseBody);

	}

	@PutMapping(value = "/pronunce/{employeeId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(value = "Update Custom Employee Name Pronunciation Based on Employee ID", response = EmployeeResponse.class)
	public ResponseEntity<EmployeeResponse> updatePronunciation(@PathVariable("employeeId") String employeeId, @RequestPart MultipartFile document) throws Exception {

		LOGGER.info("File Type : {}" , document.getContentType());

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
		LOGGER.info("Deleting Employee : {}" , employeeId);
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
	public ResponseEntity<String> getHello2() throws Exception {
		return ResponseEntity.ok().body("After Authentication");

	}

}
