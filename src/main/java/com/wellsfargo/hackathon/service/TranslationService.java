package com.wellsfargo.hackathon.service;

import java.util.List;

import com.google.cloud.texttospeech.v1.Voice;
import com.google.protobuf.ByteString;
import com.wellsfargo.hackathon.exception.ExternalSystemException;
import com.wellsfargo.hackathon.model.Employee;
import com.wellsfargo.hackathon.model.EmployeeEntity;
import com.wellsfargo.hackathon.util.PronunciationType;

public interface TranslationService {
	public ByteString translateEmployeeName(String employeeName, PronunciationType pronunciationType, String language) throws ExternalSystemException;
	public  List<String> listAllSupportedVoices() throws Exception;
}
