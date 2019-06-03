package net.r3n.calendar.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
@Component
@Order(2)
public class Slf4jMDCFilter extends OncePerRequestFilter {
  private static final String DEFAULT_RESPONSE_TOKEN_HEADER = "X-Request-ID";
  private static final String DEFAULT_MDC_UUID_TOKEN_KEY = "request_id";

  @Override
  protected void doFilterInternal(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final FilterChain filterChain) throws ServletException, IOException
  {
    final String token;
    if (request.getHeader(DEFAULT_RESPONSE_TOKEN_HEADER) != null) {
      token = request.getHeader(DEFAULT_RESPONSE_TOKEN_HEADER);
    } else {
      token = UUID.randomUUID().toString().toLowerCase();
      log.info("created request_id: {}", token);
    }
    // add request-id to response header
    response.addHeader(DEFAULT_RESPONSE_TOKEN_HEADER, token);
    try(MDC.MDCCloseable mdc = MDC.putCloseable(DEFAULT_MDC_UUID_TOKEN_KEY, token)) {
      filterChain.doFilter(request, response);
    }
  }
}
