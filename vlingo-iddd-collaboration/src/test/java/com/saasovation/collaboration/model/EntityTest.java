// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package com.saasovation.collaboration.model;

import org.junit.After;
import org.junit.Before;

import com.saasovation.collaboration.infra.persistence.SourcedRegistration;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.actors.testkit.TestWorld;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.inmemory.InMemoryJournalActor;

public class EntityTest {
  protected Journal<String> journal;
  protected MockJournalListener journalListener;
  protected SourcedTypeRegistry registry;
  protected World world;
  protected TestWorld testWorld;
  
  public TestUntil until;

  protected EntityTest() { }

  public TestUntil until(final int times) {
    return TestUntil.happenings(times);
  }

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    testWorld = TestWorld.startWithDefaults("entity-test");
    world = testWorld.world();
    journalListener = new MockJournalListener();
    journal = world.actorFor(Journal.class, InMemoryJournalActor.class, journalListener);
    registry = new SourcedTypeRegistry(world);
    SourcedRegistration.registerAllWith(registry, journal);
  }

  @After
  public void tearDown() throws Exception {
    testWorld.terminate();
  }
}
