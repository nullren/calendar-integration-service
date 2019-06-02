package net.r3n.calendar.logging;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
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

  private final String mdcTokenKey = DEFAULT_MDC_UUID_TOKEN_KEY;
  private final String requestHeader = DEFAULT_RESPONSE_TOKEN_HEADER;
  private final String responseHeader = DEFAULT_RESPONSE_TOKEN_HEADER;

  @Override
  protected void doFilterInternal(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final FilterChain filterChain) throws ServletException, IOException
  {
    final String token;
    if (!StringUtils.isEmpty(requestHeader) && !StringUtils.isEmpty(request.getHeader(requestHeader))) {
      token = request.getHeader(requestHeader);
    } else {
      token = UUID.randomUUID().toString().toLowerCase();
      log.info("created request_id: {}", token);
    }
    if (!StringUtils.isEmpty(responseHeader)) {
      response.addHeader(responseHeader, token);
    }
    try(MDC.MDCCloseable mdc = MDC.putCloseable(mdcTokenKey, token)) {
      filterChain.doFilter(request, response);
    }
  }
}
