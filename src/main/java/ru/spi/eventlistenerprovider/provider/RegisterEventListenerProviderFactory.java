package ru.spi.eventlistenerprovider.provider;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;


public class RegisterEventListenerProviderFactory implements EventListenerProviderFactory {

  @Override
  public EventListenerProvider create(KeycloakSession session) {

    return new RefactorEventListenerProvider(session);
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
    return "alfaBi_register_event_listener";
  }
}
