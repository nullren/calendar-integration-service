package net.r3n.calendar.logic;

import lombok.RequiredArgsConstructor;
import net.r3n.calendar.client.google.CalendarQueries;
import net.r3n.calendar.logic.types.InternalEventData;
import net.r3n.calendar.logic.types.InternalEvents;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EventsLookup {
  private final CalendarQueries queries;

  public InternalEvents getByDate(
    final Instant start,
    final Instant end,
    final String nextToken) throws IOException
  {
    final InternalEvents events = queries.getEvents(start, end, nextToken);

    // filter out events with no attendees
    final List<InternalEventData> filteredEvents = events.getEvents().stream()
      .filter(e -> e.getAttendeeEmails().size() > 0)
      .collect(Collectors.toList());

    return InternalEvents.builder()
      .events(filteredEvents)
      .nextToken(events.getNextToken())
      .build();
  }
}
