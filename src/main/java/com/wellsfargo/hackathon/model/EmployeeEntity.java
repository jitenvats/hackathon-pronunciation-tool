package com.wellsfargo.hackathon.model;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.wellsfargo.hackathon.util.PronunciationType;

import lombok.Data;

@Data
@Document(collection = "employee")
public class EmployeeEntity {
	
	@Id
	private String employeeId;
	
	private String employeeName;
		
	private Binary pronunciation;

}
