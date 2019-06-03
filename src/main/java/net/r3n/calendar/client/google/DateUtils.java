package net.r3n.calendar.client.google;

import com.google.api.services.calendar.model.EventDateTime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtils {
  public static ZonedDateTime fromEventDateTime(final EventDateTime dateTime) {
    final ZoneId zone = dateTime.getTimeZone() == null
      ? ZoneId.systemDefault()
      : ZoneId.of(dateTime.getTimeZone());

    final Instant instant =
      Instant.ofEpochMilli(dateTime.getDateTime().getValue());

    return instant.atZone(zone);
  }
}
