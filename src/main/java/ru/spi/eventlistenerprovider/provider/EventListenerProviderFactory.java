package ru.spi.eventlistenerprovider.provider;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;


public class EventListenerProviderFactory implements
    org.keycloak.events.EventListenerProviderFactory {

  @Override
  public org.keycloak.events.EventListenerProvider create(KeycloakSession session) {

    return new EventListenerProvider(session);
  }

  @Override
  public void init(Config.Scope scope) {
  }

  @Override
  public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

  }

  @Override
  public void close() {

  }

  @Override
  public String getId() {
    return "event_listener";
  }
}
