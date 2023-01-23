package ru.spi.eventlistenerprovider.provider;

import java.util.List;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakService {

  @Value("${keycloak.auth-server-url}")
  private String SERVER_URL;

  @Value("${keycloak.realm}")
  private String REALM;

  @Value("${kc.user.username}")
  private String USERNAME;

  @Value("${kc.user.password}")
  private String PASSWORD;

  @Value("${keycloak.resource}")
  private String CLIENT_ID;

  @Value("${kc.admin.realm}")
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
