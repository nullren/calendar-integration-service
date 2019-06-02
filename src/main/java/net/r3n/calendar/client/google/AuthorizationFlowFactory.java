package net.r3n.calendar.client.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AuthorizationFlowFactory implements FactoryBean<GoogleAuthorizationCodeFlow> {
  private static final List<String> SCOPES =
    Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

  @Autowired private final NetHttpTransport netHttpTransport;
  @Autowired private final JsonFactory jsonFactory;
  @Autowired private final GoogleClientSecrets clientSecrets;
  @Autowired private final DataStoreFactory dataStoreFactory;

  @Override
  public GoogleAuthorizationCodeFlow getObject() throws Exception {
    return new GoogleAuthorizationCodeFlow.Builder(
      netHttpTransport, jsonFactory, clientSecrets, SCOPES)
      .setDataStoreFactory(dataStoreFactory)
      .setAccessType("offline")
      .build();
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public Class<?> getObjectType() {
    return GoogleAuthorizationCodeFlow.class;
  }
}
