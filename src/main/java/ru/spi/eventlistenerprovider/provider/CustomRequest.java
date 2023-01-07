package ru.spi.eventlistenerprovider.provider;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.crypto.AsymmetricSignatureSignerContext;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;

@Slf4j
public class CustomRequest {

  private static String endPointCreateUser = "/user/createUser";

  /**
   * Method to send request to springboot App end point with registered user credentials in DTO
   *
   * @param newUser {@code UserDTO} uset DTO
   * @param newRegisteredUser {@code UserModel} UserModel object
   * @param session {@code KeycloakSession} keycloak session object
   * @throws IOException if something went wrong with request
   * @throws InterruptedException if something went wrong with request
   * @author NVN
   * @since 2022.12.28
   */
  public static void sendRequest(
      UserDTO newUser, UserModel newRegisteredUser, KeycloakSession session)
      throws IOException, InterruptedException {

    HttpClient client = HttpClient.newHttpClient();
    String accessToken = getAccessToken(newRegisteredUser, session);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(appURL() + endPointCreateUser))
            .headers("token", accessToken, "Content-Type", "application/json")
            .POST(BodyPublishers.ofString(newUser.toString()))
            .build();

    String response =
        client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).join();
    log.info("-------response = " + response);
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
   * @param userModel {@code UserModel} UserModel object
   * @param keycloakSession {@code KeycloakSession} keycloak session object
   * @author NVN
   * @since 2022.12.28
   */
  public static String getAccessToken(UserModel userModel, KeycloakSession keycloakSession) {
    KeycloakContext keycloakContext = keycloakSession.getContext();

    AccessToken token = new AccessToken();
    token.setSubject(userModel.getId());
    token.issuer(
        Urls.realmIssuer(
            keycloakContext.getUri().getBaseUri(), keycloakContext.getRealm().getName()));
    token.issuedNow();
    token.expiration((int) (token.getIat() + 60L)); // Lifetime of 60 seconds

    KeyWrapper key =
        keycloakSession.keys().getActiveKey(keycloakContext.getRealm(), KeyUse.SIG, "RS256");
    return new JWSBuilder()
        .kid(key.getKid())
        .type("JWT")
        .jsonContent(token)
        .sign(new AsymmetricSignatureSignerContext(key));
  }
}
