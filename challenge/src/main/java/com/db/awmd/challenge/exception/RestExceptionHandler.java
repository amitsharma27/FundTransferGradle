package com.db.awmd.challenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
//This class will throw exception at controller level 
	 @ExceptionHandler(value = AccountBalanceException.class)
	   public ResponseEntity<Object> exception(AccountBalanceException exception) {
	      return new ResponseEntity<>(exception.getMessage(), HttpStatus.OK);
	   }


}
