package io.vlingo.xoom.examples.petclinic.model.client;

import io.vlingo.xoom.common.version.SemanticVersion;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

import io.vlingo.xoom.examples.petclinic.model.*;

/**
 * See
 * <a href="https://docs.vlingo.io/xoom-lattice/entity-cqrs#commands-domain-events-and-identified-domain-events">
 *   Commands, Domain Events, and Identified Domain Events
 * </a>
 */
public final class ClientRegistered extends IdentifiedDomainEvent {

  public final String id;
  public final FullName name;
  public final ContactInformation contactInformation;

  public ClientRegistered(final ClientState state) {
    super(SemanticVersion.from("1.0.0").toValue());
    this.id = state.id;
    this.name = state.name;
    this.contactInformation = state.contactInformation;
  }

  @Override
  public String identity() {
    return id;
  }
}
