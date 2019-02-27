package io.vlingo.examples.ecommerce.infra.cart;

import io.vlingo.common.serialization.JsonSerialization;
import io.vlingo.examples.ecommerce.model.CartEvents;
import io.vlingo.examples.ecommerce.model.CartEvents.ProductQuantityChangeEvent;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.Entry.TextEntry;
import io.vlingo.symbio.EntryAdapter;
import io.vlingo.symbio.Metadata;

public class CartProductQuantityChangedEventAdapter implements EntryAdapter<CartEvents.ProductQuantityChangeEvent,Entry.TextEntry> {

    @Override
    public CartEvents.ProductQuantityChangeEvent fromEntry(final Entry.TextEntry entry) {
        return JsonSerialization.deserialized(entry.entryData, CartEvents.ProductQuantityChangeEvent.class);
    }

    @Override
    public Entry.TextEntry toEntry(final CartEvents.ProductQuantityChangeEvent source) {
      return toEntry(source, source.cartId);
    }

    @Override
    public TextEntry toEntry(ProductQuantityChangeEvent source, String id) {
        final String serialization = JsonSerialization.serialized(source);
        return new Entry.TextEntry(id, CartEvents.ProductQuantityChangeEvent.class, 1, serialization, Metadata.nullMetadata());
    }
}
