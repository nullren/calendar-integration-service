package net.r3n.calendar.logic.types;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder
@Value
public class InternalEvents {
  private final List<InternalEventData> events;
  private final String nextToken;
}
