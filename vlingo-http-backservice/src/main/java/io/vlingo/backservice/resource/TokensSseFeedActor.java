// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.backservice.resource;

import io.vlingo.actors.Actor;
import io.vlingo.backservice.infra.persistence.EventJournal;
import io.vlingo.backservice.infra.persistence.EventJournal.Sink;
import io.vlingo.backservice.resource.model.PrivateTokenGernerated;
import io.vlingo.http.resource.sse.SseEvent;
import io.vlingo.http.resource.sse.SseFeed;
import io.vlingo.http.resource.sse.SseSubscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokensSseFeedActor extends Actor implements SseFeed, Sink {
  private final String EventType = PrivateTokenGernerated.class.getSimpleName();
  private final int RetryThreshold = 3000;

  private final SseEvent.Builder builder;
  private final int currentStreamId;
  private final int defaultId;
  private final EventJournal eventJournal;
  private final int feedPayload;
  private final Map<String,SseSubscriber> pending;
  private final Sink sink;
  private final String streamName;

  public TokensSseFeedActor(final String streamName, final int feedPayload, final String feedDefaultId) {
    this.streamName = streamName;
    this.feedPayload = feedPayload;
    this.currentStreamId = 1;
    this.defaultId = defaultId(feedDefaultId, currentStreamId);
    this.builder = SseEvent.Builder.instance();
    this.eventJournal = EventJournal.provider.instance();
    this.pending = new HashMap<>();
    this.sink = selfAs(Sink.class);
    logger().debug("SseFeed started for stream: " + this.streamName);
  }

  //=====================================
  // SseFeed
  //=====================================

  @Override
  public void to(final Collection<SseSubscriber> subscribers) {
    for (final SseSubscriber subscriber : subscribers) {
      final SseSubscriber presentSubscriber = pending.putIfAbsent(subscriber.id(), subscriber);
      if (presentSubscriber == null) {
        final boolean fresh = subscriber.currentEventId().isEmpty();
        final int startId = fresh ? defaultId : Integer.parseInt(subscriber.currentEventId());
        eventJournal.streamTo(sink, subscriber, startId, feedPayload);
      }
    }
  }

  //=====================================
  // Sink
  //=====================================

  @Override
  public void consume(final List<Object> events, final int startId, final Object referencing) {
    final SseSubscriber subscriber = (SseSubscriber) referencing;
    final boolean fresh = subscriber.currentEventId().isEmpty();
    final int retry = fresh ? RetryThreshold : SseEvent.NoRetry;
    if (!events.isEmpty()) logger().debug("SENDING " + events.size() + " MESSAGES FOR " + subscriber.correlationId());
    subscriber.client().send(subStream(events, startId, retry));
    subscriber.currentEventId(String.valueOf(startId + events.size()));
    pending.remove(subscriber.id());
  }

  //=====================================
  // internal implementation
  //=====================================

  private int defaultId(final String feedDefaultId, final int defaultDefaultId) {
    final int maybeDefaultId = Integer.parseInt(feedDefaultId);
    return maybeDefaultId <= 0 ? defaultDefaultId : maybeDefaultId;
  }

  private List<SseEvent> subStream(final List<Object> events, final int startId, final int retry) {
    int currentId = startId;
    final List<SseEvent> substream = new ArrayList<>(events.size());
    for (final Object eventObject : events) {
      final PrivateTokenGernerated event = (PrivateTokenGernerated) eventObject;
      substream.add(builder.clear().event(EventType).id(currentId++).data(event.id).data(event.hash).retry(retry).toEvent());
    }
    return substream;
  }
}
