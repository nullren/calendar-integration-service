package net.r3n.calendar.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.r3n.calendar.errors.UnauthorizedUserException;
import net.r3n.calendar.generated.api.EventApi;
import net.r3n.calendar.generated.model.Event;
import net.r3n.calendar.generated.model.ListEvents;
import net.r3n.calendar.logic.EventsLookup;
import net.r3n.calendar.logic.EventsLookupFactory;
import net.r3n.calendar.logic.types.InternalEventData;
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
public class EventApiImpl implements EventApi {
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

  private static Event toApiEvent(
    final InternalEventData e,
    final ZoneId outputZone)
  {
    final OffsetDateTime startTime;
    final OffsetDateTime endTime;
    if (outputZone == null) {
      startTime = e.getStartTime().toOffsetDateTime();
      endTime = e.getStartTime().toOffsetDateTime();
    } else {
      startTime = e.getStartTime()
        .withZoneSameInstant(outputZone)
        .toOffsetDateTime();
      endTime = e.getEndTime()
        .withZoneSameInstant(outputZone)
        .toOffsetDateTime();
    }

    return new Event()
      .id(e.getId())
      .startTime(startTime)
      .endTime(endTime)
      .description(e.getDescription())
      .location(e.getLocation())
      .internalAttendeesOnly(e.isInternalOnly())
      .attendees(e.getAttendeeEmails())
      .title(e.getTitle());
  }

  private static ListEvents toApiEvents(
    final InternalEvents events,
    final ZoneId outputZone)
  {
    return new ListEvents()
      .nextToken(events.getNextToken())
      .events(events.getEvents()
        .stream()
        .map(e -> toApiEvent(e, outputZone))
        .collect(Collectors.toList()));
  }

  private static InternalEventData toInternalEvent(final Event event) {
    return InternalEventData.builder()
      .id(event.getId())
      .startTime(event.getStartTime().toZonedDateTime())
      .endTime(event.getEndTime().toZonedDateTime())
      .description(event.getDescription())
      .location(event.getLocation())
      .isInternalOnly(event.getInternalAttendeesOnly())
      .attendeeEmails(event.getAttendees())
      .title(event.getTitle())
      .build();
  }

  private static ZoneId getTimezone(final String timezone) {
    if (timezone == null) {
      return ZoneId.systemDefault();
    }

    try {
      return ZoneId.of(timezone);
    } catch (DateTimeException e) {
      log.info("failed to load timezone {}", timezone);
      return ZoneId.systemDefault();
    }
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

      log.debug("setting timezone");
      final ZoneId zone = getTimezone(timezone);

      log.debug("setting times");
      // defaults to start of today or end of today, resp.
      final Instant start = startOfDay(startDate, zone);
      final Instant end = endOfDay(endDate, zone);

      log.debug("fetching events");
      final InternalEvents events =
        eventsLookup.getByDate(start, end, nextToken);

      log.debug("transforming output");
      final ListEvents response = toApiEvents(events, zone);
      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (UnauthorizedUserException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    } catch (DateTimeException e) {
      log.error("failed to parse time or timezone", e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error("failed to fetch items", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public ResponseEntity<Event> updateEvent(@Valid Event event) {
    log.info("updating event in calendar...");
    try {
      log.debug("fetching token");
      final String token = getBearerToken(request);
      final EventsLookup eventsLookup =
        eventsLookupFactory.makeEventsLookup(token);

      final InternalEventData internalEventData = toInternalEvent(event);
      final InternalEventData result =
        eventsLookup.updateEvent(internalEventData);

      return new ResponseEntity<>(
        toApiEvent(result, null),
        HttpStatus.OK);
    } catch (UnauthorizedUserException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    } catch (Exception e) {
      log.error("failed to fetch items", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
