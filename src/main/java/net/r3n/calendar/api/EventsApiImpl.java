package net.r3n.calendar.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.r3n.calendar.errors.UnauthorizedUserException;
import net.r3n.calendar.generated.api.EventsApi;
import net.r3n.calendar.generated.model.Event;
import net.r3n.calendar.generated.model.ListEvents;
import net.r3n.calendar.logic.EventsLookup;
import net.r3n.calendar.logic.EventsLookupFactory;
import net.r3n.calendar.logic.types.InternalEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.r3n.calendar.api.AccessTokenUtils.getBearerToken;

@Slf4j
@RequiredArgsConstructor
@Controller
public class EventsApiImpl implements EventsApi {
  @Autowired private final HttpServletRequest request;
  @Autowired private final EventsLookupFactory eventsLookupFactory;

  private static Instant startOfDay(final LocalDate date, final ZoneId zone) {
    return Optional.ofNullable(date).orElse(LocalDate.now())
      .atStartOfDay(zone).toInstant();
  }

  private static Instant endOfDay(final LocalDate date, final ZoneId zone) {
    return Optional.ofNullable(date).orElse(LocalDate.now())
      .atTime(LocalTime.MIDNIGHT).atZone(zone).toInstant();
  }

  private static ListEvents transform(final InternalEvents events) {
    return new ListEvents()
      .nextToken(events.getNextToken())
      .events(events.getEvents()
        .stream()
        .map(e -> {
          final OffsetDateTime startTime = e.getStartTime().toOffsetDateTime();
          final OffsetDateTime endTime = e.getEndTime().toOffsetDateTime();
          return new Event()
            .startTime(startTime)
            .endTime(endTime)
            .description(e.getDescription())
            .location(e.getLocation())
            .internalAttendeesOnly(e.isInternalOnly())
            .attendees(e.getAttendeeEmails())
            .title(e.getTitle());
        }).collect(Collectors.toList()));
  }

  @Override
  public ResponseEntity<ListEvents> listEvents(
    @Valid LocalDate startDate,
    @Valid LocalDate endDate,
    @Valid String timezone,
    @Valid String nextToken)
  {
    log.info("loading events from calendar...");

    try {
      log.debug("fetching token");
      final String token = getBearerToken(request);
      final EventsLookup eventsLookup =
        eventsLookupFactory.makeEventsLookup(token);

      log.debug("fetching timezone");
      final ZoneId zone = timezone == null
        ? ZoneId.systemDefault()
        // throws DateTimeException
        : ZoneId.of(timezone);

      log.debug("setting times");
      final Instant start = startOfDay(startDate, zone);
      final Instant end = endOfDay(endDate, zone);

      log.debug("fetching events");
      final InternalEvents events =
        eventsLookup.getByDate(start, end, nextToken);

      log.debug("transforming output");
      final ListEvents response = transform(events);
      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (UnauthorizedUserException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    } catch (DateTimeException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("failed to fetch items", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
