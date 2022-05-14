package com.wellsfargo.hackathon.service;

import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;
import com.wellsfargo.hackathon.exception.BadRequestException;
import com.wellsfargo.hackathon.exception.ExternalSystemException;
import com.wellsfargo.hackathon.model.EmployeeEntity;
import com.wellsfargo.hackathon.model.EmployeeResponse;
import com.wellsfargo.hackathon.model.repository.EmployeeRepository;
import com.wellsfargo.hackathon.util.PronunciationType;

@Service
public class EmployeeServiceImpl implements EmployeeService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);


	private EmployeeRepository employeeRepository;
	private TranslationService translationService; 

	@Autowired
	public EmployeeServiceImpl(EmployeeRepository employeeRepository, TranslationService translationService) {
		this.employeeRepository = employeeRepository;
		this.translationService = translationService;
		
	}

	@Override
	public EmployeeResponse saveEmployee(EmployeeEntity employee, PronunciationType pronunciationType, String language, boolean translate, long speed) throws ExternalSystemException {

		if (translate) {
			
			ByteString response = translationService.translateEmployeeName(employee.getEmployeeName(), pronunciationType, language, speed);
			try {
				SpeechToTextService.toText(response,language);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Binary pronaunciation = new Binary(BsonBinarySubType.BINARY, response.toByteArray());
			String translation = SpeechToTextService.toText(response,language);
			LOGGER.info("Name Translation : {}", translation);
			employee.setPronunciation(pronaunciation);
			employee.setPhonetic(translation);
		}

		EmployeeEntity savedEmployee = employeeRepository.save(employee);
		EmployeeResponse emp = new EmployeeResponse();

		try {
			BeanUtils.copyProperties(emp, savedEmployee);
		} catch (IllegalAccessException |InvocationTargetException e ) {
			e.printStackTrace();
		} 
		return emp;
	}

	@Override
	public void deleteEmployee(String empId) throws BadRequestException {
		if (employeeRepository.findById(empId).isPresent()) {
			employeeRepository.deleteById(empId);
		} else {
			throw new BadRequestException("Not a Valid Employee Id", "E-0001");
		}

	}

	@Override
	public EmployeeEntity getEmployeeDetails(String employeeId) throws BadRequestException {
		Optional<EmployeeEntity> result = employeeRepository.findById(employeeId);
		
		if (result.isPresent()) {
			return employeeRepository.findById(employeeId).get();
		} else {
			throw new BadRequestException("Not a Valid Employee Id", "E-0001");
		}
	}

	@Override
	public EmployeeEntity getEmployeeDetailsWilNull(String employeeId) {
		Optional<EmployeeEntity> result = employeeRepository.findById(employeeId);
		
		if (result.isPresent()) {
			return employeeRepository.findById(employeeId).get();
		} else {
			return null;
		}
	}

}
