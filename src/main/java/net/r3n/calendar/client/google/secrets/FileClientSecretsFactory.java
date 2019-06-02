package net.r3n.calendar.client.google.secrets;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * Reads GoogleClientSecrets from resource file. This will throw a
 * RuntimeException if the resources are not available thus crashing the
 * server - which is intended since the Google API is required for this service
 * in any capacity.
 */
@Component
@Profile("local")
public class FileClientSecretsFactory implements FactoryBean<GoogleClientSecrets> {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  @Override
  public GoogleClientSecrets getObject() {
    InputStream in = FileClientSecretsFactory.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new RuntimeException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    try {
      return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse secrets", e);
    }
  }

  @Override
  public Class<?> getObjectType() {
    return GoogleClientSecrets.class;
  }
}
