package com.wellsfargo.hackathon.exception;

import java.io.Serializable;
import java.util.Objects;

public class ContentTypeException extends Exception implements Serializable{

	
	private static final long serialVersionUID = 8911326279611194084L;
	private String message;
	private String errorCode;
	
	public ContentTypeException() {
		super();
	}
	
	public ContentTypeException(String message, String errorCode) {
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
		ContentTypeException other = (ContentTypeException) obj;
		return Objects.equals(errorCode, other.errorCode) && Objects.equals(message, other.message);
	}

	@Override
	public String toString() {
		return "ContentTypeException [message=" + message + ", errorCode=" + errorCode + "]";
	}
	
	
}
