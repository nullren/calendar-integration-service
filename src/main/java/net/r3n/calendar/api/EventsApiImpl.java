package net.r3n.calendar.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.r3n.calendar.client.google.CalendarFactory;
import net.r3n.calendar.generated.api.EventsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Controller
public class EventsApiImpl implements EventsApi {
  @Autowired private final HttpServletRequest request;
  @Autowired private final CalendarFactory calendarFactory;

  private List<Event> getRecentEvents(final Credential credential)
    throws IOException
  {
    final Calendar calendar = calendarFactory.makeCalendar(credential);
    // List the next 10 events from the primary calendar.
    final DateTime now = new DateTime(System.currentTimeMillis());
    final Events events = calendar.events()
      .list("primary")
      .setMaxResults(10)
      .setTimeMin(now)
      .setOrderBy("startTime")
      .setSingleEvents(true)
      .execute();
    return events.getItems();
  }

  @Override
  public ResponseEntity<List<net.r3n.calendar.generated.model.Event>> nextEvents() {
    final Credential credential =
      (Credential) request.getAttribute("credential");

    if (credential == null) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    try {
      List<Event> items = getRecentEvents(credential);
      return new ResponseEntity<>(
        items.stream().map(e -> {
          Instant instant = new Date(e.getCreated().getValue()).toInstant();
          return new net.r3n.calendar.generated.model.Event()
            .date(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()))
            .title(e.getSummary());
        }).collect(Collectors.toList()),
        HttpStatus.OK);
    } catch (IOException e) {
      log.error("failed to fetch items", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
