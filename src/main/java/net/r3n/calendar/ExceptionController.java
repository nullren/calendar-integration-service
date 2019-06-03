package net.r3n.calendar;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ExceptionController extends ResponseEntityExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleException(final Exception e) {
    log.error("Uh oh...", e);
    return new ResponseEntity<>(
      ApiError.builder().message(e.getLocalizedMessage()).build(),
      HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Builder
  static class ApiError {
    private final String message;
  }

}
