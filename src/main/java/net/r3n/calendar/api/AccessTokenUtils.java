package net.r3n.calendar.api;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletRequest;

@Slf4j
public class AccessTokenUtils {
  private static final String AUTH_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  @CheckForNull
  public static String getBearerToken(final HttpServletRequest request) {
    final String bearer = request.getHeader(AUTH_HEADER);
    return bearer != null && bearer.startsWith(BEARER_PREFIX)
      ? bearer.substring(BEARER_PREFIX.length())
      : null;
  }
}
