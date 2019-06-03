package net.r3n.calendar.filters;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.CheckForNull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class AccessTokenFilter extends OncePerRequestFilter {
  private static final String AUTH_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String REQUEST_CRED_ATTR = "credential";

  @Autowired private final GoogleAuthorizationCodeFlow flow;

  @CheckForNull
  public static Credential getCredential(final HttpServletRequest request) {
      return (Credential) request.getAttribute(REQUEST_CRED_ATTR);
  }

  private static void setCredential(
    final HttpServletRequest request,
    final Credential credential)
  {
    request.setAttribute(REQUEST_CRED_ATTR, credential);
  }

  @CheckForNull
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
      setCredential(request, credential);
    } catch (IOException e) {
      log.error("credential failed to load for token: {}", token, e);
    } finally {
      chain.doFilter(request, response);
    }
  }
}
