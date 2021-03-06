package io.vlingo.xoom.examples.petclinic.model.pet;

import io.vlingo.xoom.common.version.SemanticVersion;
import io.vlingo.xoom.lattice.model.IdentifiedDomainEvent;

import io.vlingo.xoom.examples.petclinic.model.*;

/**
 * See
 * <a href="https://docs.vlingo.io/xoom-lattice/entity-cqrs#commands-domain-events-and-identified-domain-events">
 *   Commands, Domain Events, and Identified Domain Events
 * </a>
 */
public final class PetDeathRecorded extends IdentifiedDomainEvent {

  public final String id;
  public final Date death;

  public PetDeathRecorded(final PetState state) {
    super(SemanticVersion.from("1.0.0").toValue());
    this.id = state.id;
    this.death = state.death;
  }

  @Override
  public String identity() {
    return id;
  }
}
