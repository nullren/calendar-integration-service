package net.r3n.calendar.client.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.r3n.calendar.logic.types.InternalEventData;
import net.r3n.calendar.logic.types.InternalEvents;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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
  public InternalEvents getEvents(
    final Instant start,
    final Instant end,
    final String nextToken) throws IOException
  {
    final Events result = calendar.events()
      .list(PRIMARY_CALENDAR)
      .setPageToken(nextToken)
      .setMaxResults(MAX_PAGE_RESULTS)
      .setTimeMin(new DateTime(start.toEpochMilli()))
      .setTimeMax(new DateTime(end.toEpochMilli()))
      //.setTimeZone(String) // just transforms time zone
      .setOrderBy(START_TIME)
      .setSingleEvents(true)
      .execute();

    // transform to internal types
    return InternalEvents.builder()
      .nextToken(result.getNextPageToken())
      .events(result.getItems().stream().map(e -> {
        final List<String> attendeeEmails = e.getAttendees().stream()
          .map(EventAttendee::getEmail)
          .collect(Collectors.toList());
        return InternalEventData.builder()
          .id(e.getId())
          .title(e.getSummary())
          .location(e.getLocation())
          .description(e.getDescription())
          .startTime(DateUtils.fromEventDateTime(e.getStart()))
          .endTime(DateUtils.fromEventDateTime(e.getEnd()))
          .attendeeEmails(attendeeEmails)
          .isInternalOnly(allEmailsSameDomain(attendeeEmails))
          .build();
      }).collect(Collectors.toList()))
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

  /**
   * Gets a list of events that end or start today sorted by start time.
   * @param zone specify the timezone from which to calculate the current day
   * @param nextToken if null, fetches the first page of results
   * @throws IOException
   */
  public InternalEvents getTodaysEvents(final ZoneId zone, final String nextToken) throws IOException {
    final Instant today = LocalDate.now().atStartOfDay(zone).toInstant();
    final Instant tomorrow = LocalDate.now()
      .plus(1, ChronoUnit.DAYS)
      .atStartOfDay(zone)
      .toInstant();
    return getEvents(today, tomorrow, nextToken);
  }
}
