package net.r3n.calendar.client.google.secrets;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads GoogleClientSecrets from resource file. This will throw a
 * RuntimeException if the resources are not available thus crashing the
 * server - which is intended since the Google API is required for this service
 * in any capacity.
 */
@Component
@RequiredArgsConstructor
@Profile("local")
public class FileClientSecretsFactory implements FactoryBean<GoogleClientSecrets> {
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  @Autowired private final JsonFactory jsonFactory;

  @Override
  public GoogleClientSecrets getObject() {
    InputStream in = FileClientSecretsFactory.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new RuntimeException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    try {
      return GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse secrets", e);
    }
  }

  @Override
  public Class<?> getObjectType() {
    return GoogleClientSecrets.class;
  }
}
