package ru.spi.eventlistenerprovider.provider;

import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;

@Slf4j
public class EventListenerProvider implements org.keycloak.events.EventListenerProvider {

  private final KeycloakSession session;
  private final RealmProvider model;

  public EventListenerProvider(KeycloakSession session) {
    this.session = session;
    this.model = session.realms();
  }

  // Override method to interact with different events more about this you can find on official
  // documentation keycloak SPI events
  @Override
  public void onEvent(Event event) {
    if (EventType.REGISTER.equals(event.getType())
        || EventType.UPDATE_PROFILE.equals(event.getType())
        || EventType.DELETE_ACCOUNT.equals(event.getType())) {
      prepareDataAndSendRequest(event.getRealmId(), event.getUserId(), null, event);
    }
  }

  @Override
  public void onEvent(AdminEvent event, boolean includeRepresentation) {

    if (ResourceType.USER.equals(event.getResourceType())
        && (OperationType.DELETE.equals(event.getOperationType())
        || OperationType.CREATE.equals(event.getOperationType()))
        || OperationType.UPDATE.equals(event.getOperationType())) {
      String resourcePath = event.getResourcePath();
      if (resourcePath.startsWith("users/")) {
        prepareDataAndSendRequest(
            event.getRealmId(), resourcePath.substring("users/".length()), event, null);
      }
    }
  }


  /**
   * Method to convert EventType or OperationType object to constant string with event type
   *
   * @param adminEvent {@code AdminEvent} AdminEvent object
   * @param event {@code Event} Event object
   * @return string type of event
   * @author NVN
   * @since 2023.01.09
   */
  public String defineEventType(AdminEvent adminEvent, Event event) {
    String returnValue = null;
    if (event != null) {
      if (EventType.REGISTER.equals(event.getType())) {
        returnValue = CustomRequest.REGISTER;
      } else if (EventType.UPDATE_PROFILE.equals(event.getType())) {
        returnValue = CustomRequest.UPDATE;
      } else if (EventType.DELETE_ACCOUNT.equals(event.getType())) {
        returnValue = CustomRequest.DELETE;
      }
    } else {
      if (OperationType.CREATE.equals(adminEvent.getOperationType())) {
        returnValue = CustomRequest.REGISTER;
      } else if (OperationType.UPDATE.equals(adminEvent.getOperationType())) {
        returnValue = CustomRequest.UPDATE;
      } else if ((OperationType.DELETE.equals(adminEvent.getOperationType()))) {
        returnValue = CustomRequest.DELETE;
      }
    }
    return returnValue;
  }

  /**
   * Method to prepare data and send request
   *
   * @param realmId {@code String}
   * @param userId {@code String}
   * @param adminEvent {@code AdminEvent}
   * @param event {@code Event}
   * @author NVN
   * @since 2023.01.09
   */
  private void prepareDataAndSendRequest(
      String realmId, String userId, AdminEvent adminEvent, Event event){
    RealmModel realm = session.realms().getRealm(realmId);
    UserModel userModel = session.users().getUserById(realm, userId);
    String eventType;
    if (adminEvent != null) {
      eventType = defineEventType(adminEvent, null);
    } else {
      eventType = defineEventType(null, event);
    }
    UserDTO user;
    if (CustomRequest.DELETE.equals(eventType)) {
      user = new UserDTO(UUID.fromString(userId));
    } else {
      user =
          new UserDTO(
              UUID.fromString(userId),
              userModel.getUsername(),
              userModel.getEmail(),
              userModel.getCreatedTimestamp(),
              userModel.getFirstName(),
              userModel.getLastName());
    }
    try {
      CustomRequest.sendRequest(user, session, eventType);
    } catch (Exception e) {
      log.error("Failed to send http request" + e);
    }
  }

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
