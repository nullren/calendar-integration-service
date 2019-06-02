package net.r3n.calendar.api;

import lombok.extern.slf4j.Slf4j;
import net.r3n.calendar.generated.api.EchoApi;
import net.r3n.calendar.generated.model.EchoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.OffsetDateTime;


@Slf4j
@RestController
public class EchoApiImpl implements EchoApi {
  @Override
  public ResponseEntity<EchoResponse> echo(@Valid String value) {
    log.info("Testing echo request for value: {}", value);
    return new ResponseEntity<>(
      new EchoResponse()
        .timestamp(OffsetDateTime.now())
        .value(value),
      HttpStatus.OK);
  }
}
