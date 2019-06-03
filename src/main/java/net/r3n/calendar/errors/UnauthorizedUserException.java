package net.r3n.calendar.errors;

public class UnauthorizedUserException extends Exception {
  public UnauthorizedUserException(String message) {
    super(message);
  }
}
