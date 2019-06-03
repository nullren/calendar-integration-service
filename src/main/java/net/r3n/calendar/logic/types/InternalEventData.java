package net.r3n.calendar.logic.types;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Value
public class InternalEventData {
  private final String id;
  private final String title;
  private final String location;
  private final String description;
  private final ZonedDateTime startTime;
  private final ZonedDateTime endTime;
  private final boolean isInternalOnly;
  private final List<String> attendeeEmails;
}
