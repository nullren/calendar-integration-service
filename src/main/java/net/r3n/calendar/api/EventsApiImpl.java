package net.r3n.calendar.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.r3n.calendar.client.google.CalendarFactory;
import net.r3n.calendar.filters.AccessTokenFilter;
import net.r3n.calendar.generated.api.EventsApi;
import net.r3n.calendar.generated.model.Event;
import net.r3n.calendar.generated.model.NextEvents;
import net.r3n.calendar.logic.CalendarQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Controller
public class EventsApiImpl implements EventsApi {
  @Autowired private final HttpServletRequest request;
  @Autowired private final CalendarFactory calendarFactory;

  private OffsetDateTime fromDateTime(final EventDateTime dateTime) {
    final String timezone = dateTime.getTimeZone();
    final ZoneId zone = timezone == null
      ? ZoneId.systemDefault()
      : ZoneId.of(timezone);
    final Instant instant =
      Instant.ofEpochMilli(dateTime.getDateTime().getValue());
    return OffsetDateTime.ofInstant(instant, zone);
  }

  @Override
  public ResponseEntity<NextEvents> nextEvents(
    @Valid LocalDate start,
    @Valid LocalDate end,
    @Valid String timezone,
    @Valid String nextToken)
  {
    log.info("loading events from calendar...");

    final Credential credential = AccessTokenFilter.getCredential(request);
    if (credential == null) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    try {
      // normalize input
      final ZoneId zone = timezone == null
        ? ZoneId.systemDefault()
        : ZoneId.of(timezone);
//      final LocalDate today = LocalDate.now(zone);
//      final LocalDate startDay = Optional.ofNullable(start).orElse(today);
//      final LocalDate endDay = Optional.ofNullable(end).orElse(today);

      final NextEvents response = new NextEvents();

      final Events events = CalendarQueries.of(calendarFactory.makeCalendar(credential))
        .getTodaysEvents(zone, nextToken);

      response.setNextToken(events.getNextPageToken());
      response.setEvents(events.getItems()
        .stream()
        .map(e -> {
          final OffsetDateTime startTime = fromDateTime(e.getStart());
          final OffsetDateTime endTime = fromDateTime(e.getEnd());
          return new Event()
            .startTime(startTime)
            .endTime(endTime)
            .description(e.getDescription())
            .location(e.getLocation())
            .title(e.getSummary());
        }).collect(Collectors.toList()));

      log.info("response set");

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (DateTimeException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (IOException e) {
      log.error("failed to fetch items", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
