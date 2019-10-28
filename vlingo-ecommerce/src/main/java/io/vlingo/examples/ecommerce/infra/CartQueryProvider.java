// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.examples.ecommerce.infra;

import io.vlingo.actors.Stage;
import io.vlingo.examples.ecommerce.model.CartQuery;
import io.vlingo.examples.ecommerce.model.CartQueryActor;
import io.vlingo.examples.ecommerce.model.CartUserSummaryData;
import io.vlingo.examples.ecommerce.model.UserId;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry;
import io.vlingo.lattice.model.stateful.StatefulTypeRegistry.Info;
import io.vlingo.symbio.EntryAdapterProvider;
import io.vlingo.symbio.StateAdapterProvider;
import io.vlingo.symbio.store.state.StateStore;

public class CartQueryProvider {
  private static CartQueryProvider instance;

  public final CartQuery cartQuery;
  public final StateStore store;

  public static CartQueryProvider instance() {
    return instance;
  }

  @SuppressWarnings("rawtypes")
  public static CartQueryProvider using(final Stage stage,
                                        final StatefulTypeRegistry registry,
                                        final StateStore stateStore) {
    if (instance == null) {
      registerStateAdapter(stage);
      registerStatefulTypes(stateStore, registry);
      final CartQuery queries = stage.actorFor(CartQuery.class, CartQueryActor.class, stateStore);

      instance = new CartQueryProvider(stateStore, queries);
    }

    return instance;
  }

  public static void deleteInstance() {
      instance = null;
  }


  private static void registerStateAdapter(Stage stage) {
    final StateAdapterProvider stateAdapterProvider = new StateAdapterProvider(stage.world());
    stateAdapterProvider.registerAdapter(CartUserSummaryData.class, new CartStateAdapter());
    stateAdapterProvider.registerAdapter(UserId.class, new UserIdStateAdapter() );
    new EntryAdapterProvider(stage.world()); // future?
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static void registerStatefulTypes(StateStore stateStore, StatefulTypeRegistry registry) {
    registry
            .register(new Info(stateStore, CartUserSummaryData.class, CartUserSummaryData.class.getSimpleName()))
            .register(new Info(stateStore, UserId.class, UserId.class.getSimpleName()));
  }


  private CartQueryProvider(final StateStore store, final CartQuery queries) {
    this.store = store;
    this.cartQuery = queries;
  }
}
