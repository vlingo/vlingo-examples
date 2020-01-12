// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.eventjournal;

import io.vlingo.common.Outcome;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.journal.Journal.AppendResultInterest;

import java.util.List;
import java.util.Optional;

public class MockCounterAppendResultInterest implements AppendResultInterest {

  @Override
  public <S,ST> void appendResultedIn(Outcome<StorageException, Result> outcome, String streamName, int streamVersion,
          Source<S> source, Optional<ST> snapshot, Object object) {
    
  }

  @Override
  public <S, ST> void appendResultedIn(final Outcome<StorageException, Result> outcome, final String streamName, final int streamVersion,
          final Source<S> source, final Metadata metadata, final Optional<ST> snapshot, final Object object) {
    
  }

  @Override
  public <S,ST> void appendAllResultedIn(Outcome<StorageException, Result> outcome, String streamName, int streamVersion,
          List<Source<S>> sources, Optional<ST> snapshot, Object object) {
    
  }

  @Override
  public <S, ST> void appendAllResultedIn(final Outcome<StorageException, Result> outcome, final String streamName, final int streamVersion,
          final List<Source<S>> sources, final Metadata metadata, final Optional<ST> snapshot, final Object object) {

  }
}
