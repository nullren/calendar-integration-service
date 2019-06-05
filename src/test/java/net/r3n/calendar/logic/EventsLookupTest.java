package net.r3n.calendar.logic;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import net.r3n.calendar.client.google.CalendarQueries;
import net.r3n.calendar.logic.types.InternalEvents;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class EventsLookupTest {
  @Mock
  CalendarQueries queries;

  @InjectMocks
  EventsLookup lookupUnderTest;

  @Test
  public void filtersEventsWithAttendees() throws Exception {
    final Events mockResponse = new Events()
      .setNextPageToken(null)
      .setItems(List.of(
        new Event()
          .setSummary("title1")
          .setDescription("description")
          .setLocation("location")
          .setStart(new EventDateTime().setDateTime(new DateTime(Instant.now().toEpochMilli())))
          .setEnd(new EventDateTime().setDateTime(new DateTime(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))),
        new Event()
          .setSummary("title2")
          .setDescription("description")
          .setLocation("location")
          .setStart(new EventDateTime().setDateTime(new DateTime(Instant.now().toEpochMilli())))
          .setEnd(new EventDateTime().setDateTime(new DateTime(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli())))
          .setAttendees(List.of(
            new EventAttendee().setEmail("user3@example.com"),
            new EventAttendee().setEmail("user2@example.com"),
            new EventAttendee().setEmail("user1@example.com")
          ))
      ));

    final Instant start = Instant.now();
    final Instant end = Instant.now().plus(1, ChronoUnit.DAYS);

    when(queries.getEvents(any(), any(), any())).thenReturn(mockResponse);

    final InternalEvents results = lookupUnderTest.getByDate(start, end, null);
    assertEquals("only 1 event", 1, results.getEvents().size());
  }

  @Test
  public void sameDomainsAreInternal() throws Exception {
    final Events mockResponse = new Events()
      .setNextPageToken(null)
      .setItems(List.of(
        new Event()
          .setSummary("internal event")
          .setDescription("description")
          .setLocation("location")
          .setStart(new EventDateTime().setDateTime(new DateTime(Instant.now().toEpochMilli())))
          .setEnd(new EventDateTime().setDateTime(new DateTime(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli())))
          .setAttendees(List.of(
            new EventAttendee().setEmail("user3@example.com"),
            new EventAttendee().setEmail("user2@example.com"),
            new EventAttendee().setEmail("user1@example.com")
          ))
      ));

    final Instant start = Instant.now();
    final Instant end = Instant.now().plus(1, ChronoUnit.DAYS);

    when(queries.getEvents(any(), any(), any())).thenReturn(mockResponse);

    final InternalEvents results = lookupUnderTest.getByDate(start, end, null);
    assertTrue("should be internal",
      results.getEvents().get(0).isInternalOnly());
  }

  @Test
  public void differentDomainsAreExternal() throws Exception {
    final Events mockResponse = new Events()
      .setNextPageToken(null)
      .setItems(List.of(
        new Event()
          .setSummary("external event")
          .setDescription("description")
          .setLocation("location")
          .setStart(new EventDateTime().setDateTime(new DateTime(Instant.now().toEpochMilli())))
          .setEnd(new EventDateTime().setDateTime(new DateTime(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli())))
          .setAttendees(List.of(
            new EventAttendee().setEmail("user2@example.com"),
            new EventAttendee().setEmail("user1@different.com")
          ))
      ));

    final Instant start = Instant.now();
    final Instant end = Instant.now().plus(1, ChronoUnit.DAYS);

    when(queries.getEvents(any(), any(), any())).thenReturn(mockResponse);

    final InternalEvents results = lookupUnderTest.getByDate(start, end, null);
    assertFalse("should be external",
      results.getEvents().get(0).isInternalOnly());
  }

}
