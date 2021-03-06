package io.vlingo.xoom.examples.ecommerce.infra;

import io.vlingo.xoom.common.serialization.JsonSerialization;
import io.vlingo.xoom.symbio.BaseEntry.TextEntry;
import io.vlingo.xoom.symbio.EntryAdapter;
import io.vlingo.xoom.symbio.Metadata;
import io.vlingo.xoom.symbio.Source;

public class EventAdapter<T extends Source<?>> implements EntryAdapter<T, TextEntry> {

    private final Class<T> type;
    private final int eventVersion;

    public EventAdapter(final Class<T> type) {
        this.type = type;
        this.eventVersion = 1;
    }

    @Override
    public T fromEntry(final TextEntry entry) {
        return JsonSerialization.deserialized(entry.entryData(), type);
    }

    @Override
    public TextEntry toEntry(final T object) {
        final String serialization = JsonSerialization.serialized(object);
        return new TextEntry(type, eventVersion, serialization, Metadata.nullMetadata());
    }

    @Override
    public TextEntry toEntry(T object, Metadata metadata) {
        final String serialization = JsonSerialization.serialized(object);
        return new TextEntry(type, eventVersion, serialization, metadata);
    }

    @Override
    public TextEntry toEntry(T object, int version, String id, Metadata metadata) {
        final String serialization = JsonSerialization.serialized(object);
        return new TextEntry(id, type, eventVersion, serialization, version, Metadata.nullMetadata());
    }
}
