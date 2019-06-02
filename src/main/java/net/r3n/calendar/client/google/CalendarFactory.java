package net.r3n.calendar.client.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalendarFactory {
  private static final String APPLICATION_NAME =
    "Calendar integration challenge";

  @Autowired private final NetHttpTransport httpTransport;
  @Autowired private final JsonFactory jsonFactory;

  public Calendar makeCalendar(final Credential credential) {
    return new Calendar.Builder(httpTransport, jsonFactory, credential)
      .setApplicationName(APPLICATION_NAME)
      .build();
  }

}
