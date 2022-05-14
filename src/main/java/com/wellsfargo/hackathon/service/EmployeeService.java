package com.wellsfargo.hackathon.service;

import com.wellsfargo.hackathon.exception.BadRequestException;
import com.wellsfargo.hackathon.exception.ExternalSystemException;
import com.wellsfargo.hackathon.model.EmployeeEntity;
import com.wellsfargo.hackathon.model.EmployeeResponse;
import com.wellsfargo.hackathon.util.PronunciationType;

public interface EmployeeService {
	
	public EmployeeResponse saveEmployee(EmployeeEntity employeeEntity, PronunciationType pronunciationType, String language, boolean translate, long speed)  throws ExternalSystemException;
	public void deleteEmployee(String empId) throws BadRequestException;
	public EmployeeEntity getEmployeeDetails(String employeeId) throws BadRequestException;
	public EmployeeEntity getEmployeeDetailsWilNull(String employeeId);

}
