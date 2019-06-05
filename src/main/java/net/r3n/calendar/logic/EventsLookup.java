package net.r3n.calendar.logic;

import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import net.r3n.calendar.client.google.CalendarQueries;
import net.r3n.calendar.client.google.DateUtils;
import net.r3n.calendar.logic.types.InternalEventData;
import net.r3n.calendar.logic.types.InternalEvents;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  private static InternalEvents transform(final Events events) {
    // transform to internal types
    return InternalEvents.builder()
      .nextToken(events.getNextPageToken())
      .events(events.getItems().stream().map(e -> {
        // no attendee emails if null
        final var maybeAttendeeEmails =
          Optional.ofNullable(e.getAttendees())
            .map(attendees -> attendees.stream()
              .map(EventAttendee::getEmail)
              .collect(Collectors.toList()));
        final var attendeeEmails = maybeAttendeeEmails.orElse(null);
        final var isInternalOnly = maybeAttendeeEmails
          .map(EventsLookup::allEmailsSameDomain)
          .orElse(true);

        return InternalEventData.builder()
          .id(e.getId())
          .title(e.getSummary())
          .location(e.getLocation())
          .description(e.getDescription())
          .startTime(DateUtils.fromEventDateTime(e.getStart()))
          .endTime(DateUtils.fromEventDateTime(e.getEnd()))
          .attendeeEmails(attendeeEmails)
          .isInternalOnly(isInternalOnly)
          .build();
      }).collect(Collectors.toList()))
      .build();
  }
}
