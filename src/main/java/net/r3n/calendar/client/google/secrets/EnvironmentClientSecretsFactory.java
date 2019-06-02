package net.r3n.calendar.client.google.secrets;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.FactoryBean;
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
@Profile("heroku")
public class EnvironmentClientSecretsFactory implements FactoryBean<GoogleClientSecrets> {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String GOOGLE_CLIENT_SECRETS_JSON =
    "GOOGLE_CLIENT_SECRETS";

  @Override
  public GoogleClientSecrets getObject() {
    String secretsJson = System.getenv(GOOGLE_CLIENT_SECRETS_JSON);
    if (secretsJson == null) {
      throw new RuntimeException("Failed to read secrets from env " + GOOGLE_CLIENT_SECRETS_JSON);
    }
    try {
      return GoogleClientSecrets.load(JSON_FACTORY,
        new StringReader(secretsJson));
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse secrets", e);
    }
  }

  @Override
  public Class<?> getObjectType() {
    return GoogleClientSecrets.class;
  }
}
