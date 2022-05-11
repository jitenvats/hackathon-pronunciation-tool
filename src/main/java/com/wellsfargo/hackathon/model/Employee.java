package com.wellsfargo.hackathon.model;

import com.wellsfargo.hackathon.util.PronunciationType;

import lombok.Data;

@Data
public class Employee {
	
	
	private String employeeName;	
	private PronunciationType pronunciationType;
	private String language;
}
