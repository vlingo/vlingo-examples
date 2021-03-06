package io.vlingo.xoom.examples.petclinic.infrastructure;

import io.vlingo.xoom.examples.petclinic.model.Owner;

public class OwnerData {

  public final String clientId;

  public static OwnerData from(final Owner owner) {
    return from(owner.clientId);
  }

  public static OwnerData from(final String clientId) {
    return new OwnerData(clientId);
  }

  private OwnerData (final String clientId) {
    this.clientId = clientId;
  }

}