package net.r3n.calendar.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.r3n.calendar.generated.api.AuthorizeApi;
import net.r3n.calendar.generated.model.NewAccessTokenResponse;
import net.r3n.calendar.generated.model.NewAuthorizationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthorizeApiImpl implements AuthorizeApi {

  @Autowired
  @Qualifier("redirectUri")
  private final String redirectUri;

  @Autowired private final GoogleAuthorizationCodeFlow flow;

  @Override
  public ResponseEntity<NewAccessTokenResponse> googleOauthCallback(
    @Valid String code,
    @Valid String state)
  {
    try {
      flow.createAndStoreCredential(
        flow.newTokenRequest(code)
          .setRedirectUri(redirectUri)
          .execute(),
        state);
    } catch (IOException e) {
      log.error("Failed to create Auth URI", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(
      new NewAccessTokenResponse().accessToken(UUID.fromString(state)),
      HttpStatus.OK);
  }

  private String createAuthorizationUri(final String uuid) {
    return flow.newAuthorizationUrl()
      .setRedirectUri(redirectUri)
      .setState(uuid)
      .build();
  }

  @Override
  public ResponseEntity<NewAuthorizationResponse> newAuthorization() {
    // TODO: ensure unique
    final String uuid = UUID.randomUUID().toString();
    final String uri = createAuthorizationUri(uuid);
    return new ResponseEntity<>(
      new NewAuthorizationResponse().googleOauthUri(uri),
      HttpStatus.OK);
  }
}
