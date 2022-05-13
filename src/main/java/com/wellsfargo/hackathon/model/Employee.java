package com.wellsfargo.hackathon.model;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.wellsfargo.hackathon.util.PronunciationType;

import lombok.Data;

@Data
@Valid
public class Employee {
	
	@NotEmpty(message = "Name Cannot be empty")
	@NotNull(message = "Name Cannot be null")
	private String employeeName;	
	
	@NotEmpty(message = "Pronunciation Type Cannot be empty")
	@NotNull(message = "Pronunciation Type Cannot be null")
	private PronunciationType pronunciationType;
	
	@NotEmpty(message = "Language Type Cannot be empty")
	private String language;
	
	@Positive
	@Max(4)
	private long speed;
}
