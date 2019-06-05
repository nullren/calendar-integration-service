package net.r3n.calendar.client.google;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class CalendarQueries {
  private static final int MAX_PAGE_RESULTS = 200;
  private static final String PRIMARY_CALENDAR = "primary";
  private static final String START_TIME = "startTime";

  @NonNull private final Calendar calendar;

  /**
   * Gets a list of single event items from the primary calendar sorted by
   * event start time.
   *
   * @param start filters events ending after the start date
   * @param end filters events starting before the end date
   * @param nextToken if null, fetches the first page of results
   * @throws IOException
   */
  public Events getEvents(
    final Instant start,
    final Instant end,
    final String nextToken) throws IOException
  {
    return calendar.events()
      .list(PRIMARY_CALENDAR)
      .setPageToken(nextToken)
      .setMaxResults(MAX_PAGE_RESULTS)
      .setTimeMin(DateUtils.toDateTime(start))
      .setTimeMax(DateUtils.toDateTime(end))
      //.setTimeZone(String) // just transforms time zone
      .setOrderBy(START_TIME)
      .setSingleEvents(true)
      .execute();
  }

  public Event patchEvent(final Event event) throws IOException {
     return calendar.events()
       .patch(PRIMARY_CALENDAR, event.getId(), event)
       .execute();
  }

}
