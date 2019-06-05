package net.r3n.calendar.client.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
public class DateUtils {
  public static ZonedDateTime fromEventDateTime(final EventDateTime dateTime) {
    final ZoneId zone = dateTime.getTimeZone() == null
      ? ZoneId.systemDefault()
      : ZoneId.of(dateTime.getTimeZone());

    final DateTime time = dateTime.getDateTime() != null
      ? dateTime.getDateTime()
      : dateTime.getDate();

    if (time == null) {
      log.warn("EventDateTime from google has neither Date nor DateTime");
      return null;
    }

    final Instant instant = Instant.ofEpochMilli(time.getValue());
    return instant.atZone(zone);
  }
}
