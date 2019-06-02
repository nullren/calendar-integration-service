package net.r3n.calendar.filters;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class AccessTokenFilter extends OncePerRequestFilter {
  private static final String AUTH_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  @Autowired private final GoogleAuthorizationCodeFlow flow;

  private String getBearerToken(final HttpServletRequest request) {
    final String bearer = request.getHeader(AUTH_HEADER);
    return bearer != null && bearer.startsWith(BEARER_PREFIX)
      ? bearer.substring(BEARER_PREFIX.length())
      : null;
  }

  @Override
  protected void doFilterInternal(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final FilterChain chain) throws ServletException, IOException
  {

    final String token = getBearerToken(request);

    try {
      if (token == null) {
        log.info("No authorization bearer token on request");
        return; // finish chain
      }
      final Credential credential = flow.loadCredential(token);
      request.setAttribute("credential", credential);
    } catch (IOException e) {
      log.error("credential failed to load for token: {}", token, e);
    } finally {
      chain.doFilter(request, response);
    }
  }
}
