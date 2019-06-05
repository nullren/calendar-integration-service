package net.r3n.calendar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.format.Formatter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Slf4j
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
    return "http://localhost:8080/authorize/google";
  }

  @Bean
  @Qualifier("redirectUri")
  @Profile("heroku")
  public String getHerokuRedirectUri() {
    return "https://shrouded-fjord-98511.herokuapp.com/authorize/google";
  }

  @Bean
  public Formatter<LocalDate> localDateFormatter() {
    return new Formatter<>() {
      @Override
      public LocalDate parse(String text, Locale locale) {
        try {
          return LocalDate.parse(text, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
          log.debug("date not ISO_DATE format");
        }

        try {
          return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
          log.debug("date not ISO_LOCAL_DATE_TIME format");
        }

        return null;
      }

      @Override
      public String print(LocalDate object, Locale locale) {
        return DateTimeFormatter.ISO_DATE.format(object);
      }
    };
  }
}
