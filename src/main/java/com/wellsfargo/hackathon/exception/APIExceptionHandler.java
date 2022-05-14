package com.wellsfargo.hackathon.exception;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.wellsfargo.hackathon.model.ErrorResponse;


@ControllerAdvice
public class APIExceptionHandler extends ResponseEntityExceptionHandler {
	
	@RequestMapping(produces = {"application/json", "application/xml"})
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleBadRequestException(BadRequestException ex) throws Exception {
        ErrorResponse errorResponse = new ErrorResponse();
    	errorResponse.setErrorCode(HtmlUtils.htmlEscape(ex.getErrorCode()));
        errorResponse.setMessage(HtmlUtils.htmlEscape(ex.getMessage()));
        return errorResponse;
    }
	
	@RequestMapping(produces = {"application/json", "application/xml"})
    @ExceptionHandler(ContentTypeException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleContentTypeException(ContentTypeException ex) throws Exception {
        ErrorResponse errorResponse = new ErrorResponse();
    	errorResponse.setErrorCode(HtmlUtils.htmlEscape(ex.getErrorCode()));
        errorResponse.setMessage(HtmlUtils.htmlEscape(ex.getMessage()));
        return errorResponse;
    }
	
	
	@RequestMapping(produces = {"application/json", "application/xml"})
    @ExceptionHandler(ExternalSystemException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleExternalSystemException(ExternalSystemException ex) throws Exception {
        ErrorResponse errorResponse = new ErrorResponse();
    	errorResponse.setErrorCode(HtmlUtils.htmlEscape(ex.getErrorCode()));
        errorResponse.setMessage(HtmlUtils.htmlEscape(ex.getMessage()));
        return errorResponse;
    }
	
	
	@RequestMapping(produces = {"application/json", "application/xml"})
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleException(Exception ex) throws Exception {
		ex.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse();
    	errorResponse.setErrorCode(HtmlUtils.htmlEscape("F-0002"));
        errorResponse.setMessage(HtmlUtils.htmlEscape("Server Error"));
        return errorResponse;
    }

}
