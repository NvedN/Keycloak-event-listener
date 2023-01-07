package ru.spi.eventlistenerprovider.provider;

import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;

@Slf4j
public class RefactorEventListenerProvider implements EventListenerProvider {

  private final KeycloakSession session;
  private final RealmProvider model;

  public RefactorEventListenerProvider(KeycloakSession session) {
    this.session = session;
    this.model = session.realms();
  }

  // Override method to interact with different events more about this you can find on official
  // documentation keycloak SPI events
  @Override
  public void onEvent(Event event) {
    if (EventType.REGISTER.equals(event.getType())) {

      log.info("## NEW %s EVENT" + event.getType());
      log.info("-----------------------------------------------------------");

      RealmModel realm = this.model.getRealm(event.getRealmId());
      UserModel newRegisteredUser = this.session.users().getUserById(realm, event.getUserId());

      UserDTO newUser =
          new UserDTO(
              newRegisteredUser.getUsername(),
              newRegisteredUser.getEmail(),
              newRegisteredUser.getCreatedTimestamp(),
              newRegisteredUser.getFirstName(),
              newRegisteredUser.getLastName());
      try {
        CustomRequest.sendRequest(newUser, newRegisteredUser, session);

      } catch (RuntimeException e) {
        log.error("Failed to send http request" + e);
      } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
      }
      log.info("-----------------------------------------------------------");
    }
  }

  @Override
  public void onEvent(AdminEvent event, boolean includeRepresentation) {}

  private String toString(Event event) {
    StringBuilder sb = new StringBuilder();

    sb.append("{'type': '");
    sb.append(event.getType());
    sb.append("', 'realmId': '");
    sb.append(event.getRealmId());
    sb.append("', 'clientId': '");
    sb.append(event.getClientId());
    sb.append("', 'userId': '");
    sb.append(event.getUserId());
    sb.append("', 'ipAddress': '");
    sb.append(event.getIpAddress());
    sb.append("'");

    if (event.getError() != null) {
      sb.append(", 'error': '");
      sb.append(event.getError());
      sb.append("'");
    }
    sb.append(", 'details': {");
    if (event.getDetails() != null) {
      for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
        sb.append("'");
        sb.append(e.getKey());
        sb.append("': '");
        sb.append(e.getValue());
        sb.append("', ");
      }
    }
    sb.append("}}");

    return sb.toString();
  }

  private String toString(AdminEvent adminEvent) {
    StringBuilder sb = new StringBuilder();

    sb.append("{'type': '");
    sb.append(adminEvent.getOperationType());
    sb.append("', 'realmId': '");
    sb.append(adminEvent.getAuthDetails().getRealmId());
    sb.append("', 'clientId': '");
    sb.append(adminEvent.getAuthDetails().getClientId());
    sb.append("', 'userId': '");
    sb.append(adminEvent.getAuthDetails().getUserId());
    sb.append("', 'ipAddress': '");
    sb.append(adminEvent.getAuthDetails().getIpAddress());
    sb.append("', 'resourcePath': '");
    sb.append(adminEvent.getResourcePath());
    sb.append("'");

    if (adminEvent.getError() != null) {
      sb.append(", 'error': '");
      sb.append(adminEvent.getError());
      sb.append("'");
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public void close() {}
}
