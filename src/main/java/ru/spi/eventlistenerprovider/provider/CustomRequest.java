package ru.spi.eventlistenerprovider.provider;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.crypto.AsymmetricSignatureSignerContext;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.keycloak.services.Urls;

@Slf4j
public class CustomRequest {

  /**
   * Endpoint to create user
   */
  private static String endPointCreateOrUpdateUser = "/user/createUser";

  /**
   * Endpoint to delete user
   */
  static String endPointDeleteUser = "/user/deleteUser";

  /**
   * Constant for register value
   */
  static String REGISTER = "Register";

  /**
   * Constant for Update value
   */
  static String UPDATE = "Update";

  /**
   * Constant for Delete value
   */
  static String DELETE = "Delete";

  /**
   * Method to send request to springboot App end point with registered user credentials in DTO
   *
   * @param userDTO {@code UserDTO} uset DTO
   * @param session {@code KeycloakSession} keycloak session object
   * @param type    {@code String} with event type
   * @author NVN
   * @since 2022.12.28
   */
  public static void sendRequest(UserDTO userDTO, KeycloakSession session, String type) {
    String endPoint = type.equals(DELETE) ? endPointDeleteUser : endPointCreateOrUpdateUser;
    HttpClient client = HttpClient.newHttpClient();
    String accessToken = getAccessToken(session);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(appURL() + endPoint))
            .headers("Authorization", "Bearer " + accessToken,
                "Content-Type", "application/json", "Accept", "*/*")
            .POST(BodyPublishers.ofString(userDTO.toString()))
            .build();
    client
        .sendAsync(request, BodyHandlers.ofString())
        .thenAccept(
            response ->
                log.info(
                    "Event type ="
                        + type
                        + "\n User DTO ="
                        + userDTO
                        + "\n  Response status code: "
                        + response.statusCode()))
        .join();
  }

  /**
   * Method to read url from keycloak config file
   *
   * @return url to backend app
   * @author NVN
   * @since 2022.12.29
   */
  public static String appURL() {
    String path = System.getProperty("jboss.server.config.dir");
    Properties prop = new Properties();
    String fileName = path + "/keycloak.conf";
    try (FileInputStream fis = new FileInputStream(fileName)) {
      prop.load(fis);
    } catch (IOException ignored) {
      log.error("can't read file", ignored);
    }
    return prop.getProperty("app-url");
  }

  /**
   * Method to generate valid JWT token with keycloak credentials
   *
   * @param keycloakSession {@code KeycloakSession} keycloak session object
   * @author NVN
   * @since 2022.12.28
   */
  public static String getAccessToken(KeycloakSession keycloakSession) {
    KeycloakContext keycloakContext = keycloakSession.getContext();
    AccessToken token = new AccessToken();
    token.issuer(
        Urls.realmIssuer(
            keycloakContext.getUri().getBaseUri(), keycloakContext.getRealm().getName()));
    token.issuedNow();
    token.expiration((int) (token.getIat() + 60L)); // Lifetime of 60 seconds
    token.type("Bearer");
    token.setSubject("admin-cli");
    token.addAccess("admin-api").addRole("admin-role");
    token.setRealmAccess(new Access().addRole("role_admin"));

    KeyWrapper key =
        keycloakSession.keys().getActiveKey(keycloakContext.getRealm(), KeyUse.SIG, "RS256");
    return new JWSBuilder()
        .kid(key.getKid())
        .type("JWT")
        .jsonContent(token)
        .sign(new AsymmetricSignatureSignerContext(key));
  }
}
