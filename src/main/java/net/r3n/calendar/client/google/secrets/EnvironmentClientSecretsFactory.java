package net.r3n.calendar.client.google.secrets;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;

/**
 * Reads GoogleClientSecrets from environment variable. This will throw a
 * RuntimeException if the resources are not available thus crashing the
 * server - which is intended since the Google API is required for this service
 * in any capacity.
 */
@Component
@RequiredArgsConstructor
@Profile("heroku")
public class EnvironmentClientSecretsFactory implements FactoryBean<GoogleClientSecrets> {
  private static final String JSON_ENV_NAME = "GOOGLE_CLIENT_SECRETS";

  @Autowired private final JsonFactory jsonFactory;

  @Override
  public GoogleClientSecrets getObject() {
    String secretsJson = System.getenv(JSON_ENV_NAME);
    if (secretsJson == null) {
      throw new RuntimeException("Failed to read secrets from env " + JSON_ENV_NAME);
    }
    try {
      return GoogleClientSecrets.load(jsonFactory, new StringReader(secretsJson));
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse secrets", e);
    }
  }

  @Override
  public Class<?> getObjectType() {
    return GoogleClientSecrets.class;
  }
}
