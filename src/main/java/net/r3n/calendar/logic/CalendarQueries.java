package net.r3n.calendar.logic;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class CalendarQueries {
  private static final int MAX_PAGE_RESULTS = 200;
  private static final String PRIMARY_CALENDAR = "primary";
  private static final String START_TIME = "startTime";

  @NonNull private final Calendar calendar;

  private static DateTime toDateTime(
    final LocalDate localDate,
    final ZoneId zone)
  {
    return new DateTime(localDate.atStartOfDay(zone).toInstant().toEpochMilli());
  }

  public Events getTodaysEvents(final ZoneId zone, final String nextToken) throws IOException {
    final DateTime today = toDateTime(LocalDate.now(), zone);
    final DateTime tomorrow = toDateTime(
      LocalDate.now().plus(1, ChronoUnit.DAYS),
      zone);
    return calendar.events()
      .list(PRIMARY_CALENDAR)
      .setPageToken(nextToken)
      .setMaxResults(MAX_PAGE_RESULTS)
      .setTimeMin(today)
      .setTimeMax(tomorrow)
      .setOrderBy(START_TIME)
      .setSingleEvents(true)
      .execute();
  }
}
