package com.wellsfargo.hackathon.exception;

import java.io.Serializable;
import java.util.Objects;

public class BadRequestException extends Exception implements Serializable {

	private static final long serialVersionUID = -7651980454224721488L;
	private String message;
	private String errorCode;
	
	public BadRequestException() {
		super();
	}
	
	public BadRequestException(String message, String errorCode) {
		this.message = message;
		this.errorCode = errorCode;
	}
	
	public String getMessage() {
		return message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(errorCode, message);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BadRequestException other = (BadRequestException) obj;
		return Objects.equals(errorCode, other.errorCode) && Objects.equals(message, other.message);
	}

	@Override
	public String toString() {
		return "BadRequestException [message=" + message + ", errorCode=" + errorCode + "]";
	}
	
	
	

}
