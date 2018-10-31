// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.frontservice.infra.projection;

import java.util.function.Consumer;
import java.util.function.Function;

import io.vlingo.actors.Actor;
import io.vlingo.frontservice.data.UserData;
import io.vlingo.frontservice.infra.persistence.QueryModelStoreProvider;
import io.vlingo.frontservice.infra.persistence.UserDataStateAdapter;
import io.vlingo.frontservice.model.User;
import io.vlingo.lattice.model.projection.Projectable;
import io.vlingo.lattice.model.projection.Projection;
import io.vlingo.lattice.model.projection.ProjectionControl;
import io.vlingo.symbio.State;
import io.vlingo.symbio.State.TextState;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;
import io.vlingo.symbio.store.state.TextStateStore;

public class UserProjectionActor extends Actor
    implements Projection, ReadResultInterest<String>, WriteResultInterest<String> {

  private final UserDataStateAdapter adapter;
  private final ReadResultInterest<String> readInterest;
  private final WriteResultInterest<String> writeInterest;
  private final TextStateStore store;

  @SuppressWarnings("unchecked")
  public UserProjectionActor() {
    this.store = QueryModelStoreProvider.instance().store;
    this.adapter = new UserDataStateAdapter();
    this.readInterest = selfAs(ReadResultInterest.class);
    this.writeInterest = selfAs(WriteResultInterest.class);
  }

  @Override
  public void projectWith(final Projectable projectable, final ProjectionControl control) {
    final User.UserState state = projectable.object();
    final UserData data = UserData.from(state);
    final Confirmer confirmation = () -> control.confirmProjected(projectable.projectionId());

    switch (projectable.becauseOf()) {
      case "User:new": {
        final State<String> projection = new TextState(data.id, UserData.class, 1, adapter.to(data, 1, 1), state.version);
        store.write(projection, writeInterest, confirmation);
        break;
      }
      case "User:contact": {
        final Consumer<State<String>> updater = readState -> {
          updateWith(readState, data, state.version,
            (writeData) -> UserData.from(writeData.id, writeData.nameData, data.contactData, writeData.publicSecurityToken),
            confirmation
          );
        };
        store.read(data.id, UserData.class, readInterest, updater);
        break;
      }
      case "User:name": {
        final Consumer<State<String>> updater = readState -> {
          updateWith(readState, data, state.version,
            (writeData) -> UserData.from(writeData.id, data.nameData, writeData.contactData, writeData.publicSecurityToken),
            confirmation
          );
        };
        store.read(data.id, UserData.class, readInterest, updater);
        break;
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void readResultedIn(final Result result, final String id, final State<String> state, final Object object) {
    ((Consumer<State<String>>) object).accept(state);
  }

  @Override
  public void readResultedIn(Result result, Exception cause, String id, State<String> state, Object object) {
    // log but don't retry, allowing re-delivery of Projectable
    logger().log("Query state not read for update because: " + cause.getMessage(), cause);
  }

  @Override
  public void writeResultedIn(final Result result, final String id, final State<String> state, final Object object) {
    ((Confirmer) object).confirm();
  }

  @Override
  public void writeResultedIn(final Result result, final Exception cause, final String id, final State<String> state, final Object object) {
    // log but don't retry, allowing re-delivery of Projectable
    logger().log("Query state not written for update because: " + cause.getMessage(), cause);
  }

  private void updateWith(final State<String> state, final UserData data, final int version, final Function<UserData,UserData> updater, final Confirmer confirmer) {
    final UserData read = adapter.from(state.data, state.dataVersion, state.typeVersion);
    final UserData write = updater.apply(read);
    final State<String> projection = new TextState(write.id, UserData.class, 1, adapter.to(write, 1, 1), version);
    store.write(projection, writeInterest, confirmer);
  }

  @FunctionalInterface
  private static interface Confirmer {
    void confirm();
  }
}
