package net.r3n.calendar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class CISConfiguration {
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  @Bean
  public JsonFactory getJsonFactory() {
    return JacksonFactory.getDefaultInstance();
  }

  @Bean
  public NetHttpTransport getNetHttpTransport()
    throws GeneralSecurityException, IOException
  {
    return GoogleNetHttpTransport.newTrustedTransport();
  }

  @Bean
  public DataStoreFactory getDataStoreFactory() throws IOException {
    return new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
  }

  @Bean
  @Qualifier("redirectUri")
  @Profile("local")
  public String getLocalRedirectUri() {
    return "http://localhost:5000/authorize/google";
  }

  @Bean
  @Qualifier("redirectUri")
  @Profile("heroku")
  public String getHerokuRedirectUri() {
    return "https://shrouded-fjord-98511.herokuapp.com/authorize/google";
  }
}
