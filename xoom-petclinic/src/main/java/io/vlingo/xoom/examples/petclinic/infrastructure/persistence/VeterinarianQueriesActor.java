package io.vlingo.xoom.examples.petclinic.infrastructure.persistence;

import java.util.ArrayList;
import java.util.Collection;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.query.StateStoreQueryActor;
import io.vlingo.xoom.symbio.store.state.StateStore;

import io.vlingo.xoom.examples.petclinic.infrastructure.VeterinarianData;

/**
 * See <a href="https://docs.vlingo.io/xoom-lattice/entity-cqrs#querying-a-statestore">Querying a StateStore</a>
 */
public class VeterinarianQueriesActor extends StateStoreQueryActor implements VeterinarianQueries {

  public VeterinarianQueriesActor(StateStore store) {
    super(store);
  }

  @Override
  public Completes<VeterinarianData> veterinarianOf(String id) {
    return queryStateFor(id, VeterinarianData.class, VeterinarianData.empty());
  }

  @Override
  public Completes<Collection<VeterinarianData>> veterinarians() {
    return streamAllOf(VeterinarianData.class, new ArrayList<>());
  }

}
