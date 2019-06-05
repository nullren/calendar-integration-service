package net.r3n.calendar.logic;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.r3n.calendar.client.google.CalendarQueries;
import net.r3n.calendar.client.google.DateUtils;
import net.r3n.calendar.logic.types.InternalEventData;
import net.r3n.calendar.logic.types.InternalEvents;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class EventsLookup {
  private final CalendarQueries queries;

  public InternalEvents getByDate(
    final Instant start,
    final Instant end,
    final String nextToken) throws IOException
  {
    final Events queryResult = queries.getEvents(start, end, nextToken);
    final InternalEvents events = transform(queryResult);

    // filter out events with no attendees
    final List<InternalEventData> filteredEvents = events.getEvents().stream()
      .filter(e -> e.getAttendeeEmails() != null && e.getAttendeeEmails().size() > 0)
      .collect(Collectors.toList());

    return InternalEvents.builder()
      .events(filteredEvents)
      .nextToken(events.getNextToken())
      .build();
  }

  public InternalEventData updateEvent(final InternalEventData event)
    throws IOException
  {
    // because we're sending a patch update, only include the fields that are
    // not null
    final Event request = new Event().setId(event.getId());
    if (event.getStartTime() != null) {
      log.debug("setting start time: {}", event.getStartTime());
      request.setStart(DateUtils.toEventDateTime(event.getStartTime()));
    }
    if (event.getEndTime() != null) {
      log.debug("setting end time: {}", event.getEndTime());
      request.setEnd(DateUtils.toEventDateTime(event.getEndTime()));
    }
    if (event.getTitle() != null) {
      log.debug("setting summary: {}", event.getTitle());
      request.setSummary(event.getTitle());
    }
    if (event.getDescription() != null) {
      log.debug("setting description: {}", event.getDescription());
      request.setDescription(event.getDescription());
    }
    if (event.getLocation() != null) {
      log.debug("setting location: {}", event.getLocation());
      request.setLocation(event.getLocation());
    }

    // do not change attendees

    final Event patchResult = queries.patchEvent(request);
    return transform(patchResult);
  }

  private static String getEmailDomain(final String email) {
    return email.substring(email.indexOf("@") + 1);
  }

  private static boolean allEmailsSameDomain(final List<String> emails) {
    if (emails.size() < 2) {
      return true;
    }
    final String domain = getEmailDomain(emails.get(0));
    return emails.stream().allMatch(e -> domain.equals(getEmailDomain(e)));
  }

  private static InternalEventData transform(final Event event) {
    // no attendee emails if null
    final var maybeAttendeeEmails =
      Optional.ofNullable(event.getAttendees())
        .map(attendees -> attendees.stream()
          .map(EventAttendee::getEmail)
          .collect(Collectors.toList()));
    final var attendeeEmails = maybeAttendeeEmails.orElse(null);
    final var isInternalOnly = maybeAttendeeEmails
      .map(EventsLookup::allEmailsSameDomain)
      .orElse(true);
    return InternalEventData.builder()
      .id(event.getId())
      .title(event.getSummary())
      .location(event.getLocation())
      .description(event.getDescription())
      .startTime(DateUtils.fromEventDateTime(event.getStart()))
      .endTime(DateUtils.fromEventDateTime(event.getEnd()))
      .attendeeEmails(attendeeEmails)
      .isInternalOnly(isInternalOnly)
      .build();
  }

  private static InternalEvents transform(final Events events) {
    // transform to internal types
    return InternalEvents.builder()
      .nextToken(events.getNextPageToken())
      .events(events.getItems().stream()
        .map(EventsLookup::transform)
        .collect(Collectors.toList()))
      .build();
  }
}
