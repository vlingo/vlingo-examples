package io.vlingo.xoom.examples.petclinic.infrastructure.persistence;

import java.util.ArrayList;
import java.util.Collection;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.lattice.query.StateStoreQueryActor;
import io.vlingo.xoom.symbio.store.state.StateStore;

import io.vlingo.xoom.examples.petclinic.infrastructure.PetData;

/**
 * See <a href="https://docs.vlingo.io/xoom-lattice/entity-cqrs#querying-a-statestore">Querying a StateStore</a>
 */
public class PetQueriesActor extends StateStoreQueryActor implements PetQueries {

  public PetQueriesActor(StateStore store) {
    super(store);
  }

  @Override
  public Completes<PetData> petOf(String id) {
    return queryStateFor(id, PetData.class, PetData.empty());
  }

  @Override
  public Completes<Collection<PetData>> pets() {
    return streamAllOf(PetData.class, new ArrayList<>());
  }

}
