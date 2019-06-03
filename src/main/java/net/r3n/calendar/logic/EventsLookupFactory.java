package net.r3n.calendar.logic;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.calendar.Calendar;
import lombok.RequiredArgsConstructor;
import net.r3n.calendar.client.google.CalendarFactory;
import net.r3n.calendar.client.google.CalendarQueries;
import net.r3n.calendar.errors.UnauthorizedUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class EventsLookupFactory {
  @Autowired private final GoogleAuthorizationCodeFlow flow;
  @Autowired private final CalendarFactory calendarFactory;

  public EventsLookup makeEventsLookup(final String token)
    throws UnauthorizedUserException
  {
    try {
      final Credential credential = flow.loadCredential(token);
      final Calendar calendar = calendarFactory.makeCalendar(credential);
      final CalendarQueries queries = CalendarQueries.of(calendar);
      return new EventsLookup(queries);
    } catch (IOException e) {
      throw new UnauthorizedUserException("Could not load credential for token " + token);
    }
  }
}
