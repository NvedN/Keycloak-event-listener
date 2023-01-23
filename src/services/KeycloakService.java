package ru.spi.eventlistenerprovider.provider;

import java.util.List;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;

public class KeycloakService {

  private String SERVER_URL;

  private String REALM;

  private String USERNAME;

  private String PASSWORD;

  private String CLIENT_ID;

  private String MASTER_REALM;

  private Keycloak getInstance() {
    return KeycloakBuilder.builder()
        .serverUrl(SERVER_URL)
        .realm(MASTER_REALM)
        .username(USERNAME)
        .password(PASSWORD)
        .clientId(CLIENT_ID)
        .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build())
        .build();
  }

  /**
   * Method to get all users from keycloak DB
   *
   * @return List with keycloak users
   * @author NVN
   * @since 2023.01.11
   */
  public List<UserRepresentation> getAllUsers() {
    return getInstance().realm(REALM).users().list();
  }
}
