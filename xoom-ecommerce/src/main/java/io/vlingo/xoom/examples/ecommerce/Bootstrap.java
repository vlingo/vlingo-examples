package io.vlingo.xoom.examples.ecommerce;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.examples.ecommerce.infra.CartQueryProvider;
import io.vlingo.xoom.examples.ecommerce.infra.EventAdapter;
import io.vlingo.xoom.examples.ecommerce.infra.ProjectionDispatcherProvider;
import io.vlingo.xoom.examples.ecommerce.model.CartActor;
import io.vlingo.xoom.examples.ecommerce.model.CartEvents.AllItemsRemovedEvent;
import io.vlingo.xoom.examples.ecommerce.model.CartEvents.CreatedForUser;
import io.vlingo.xoom.examples.ecommerce.model.CartEvents.ProductQuantityChangeEvent;
import io.vlingo.xoom.examples.ecommerce.model.CartResource;
import io.vlingo.xoom.examples.ecommerce.model.OrderActor;
import io.vlingo.xoom.examples.ecommerce.model.OrderEvents;
import io.vlingo.xoom.examples.ecommerce.model.OrderResource;
import io.vlingo.xoom.examples.ecommerce.model.UserResource;
import io.vlingo.xoom.http.resource.Configuration;
import io.vlingo.xoom.http.resource.Resources;
import io.vlingo.xoom.http.resource.Server;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.xoom.lattice.model.sourcing.SourcedTypeRegistry.Info;
import io.vlingo.xoom.lattice.model.stateful.StatefulTypeRegistry;
import io.vlingo.xoom.symbio.store.dispatch.Dispatchable;
import io.vlingo.xoom.symbio.store.dispatch.Dispatcher;
import io.vlingo.xoom.symbio.store.dispatch.DispatcherControl;
import io.vlingo.xoom.symbio.store.journal.Journal;
import io.vlingo.xoom.symbio.store.journal.inmemory.InMemoryJournalActor;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.inmemory.InMemoryStateStoreActor;

public class Bootstrap {
    private static Bootstrap instance;
    private final Server server;
    private final World world;

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Bootstrap(final int portNumber) {
        world = World.startWithDefaults("cartservice");

        final StatefulTypeRegistry statefulTypeRegistry = new StatefulTypeRegistry(world);


        final StateStore keyValueStateStore = world.stage().actorFor(StateStore.class,
                InMemoryStateStoreActor.class,
                Arrays.asList(createNoOpDispatcher()));

        CartQueryProvider.using(world.stage(), statefulTypeRegistry, keyValueStateStore);
        ProjectionDispatcherProvider.using(world.stage());

        final Journal<String> journal = Journal.using(world.stage(),
                                                InMemoryJournalActor.class,
                                                Arrays.asList(ProjectionDispatcherProvider.instance().storeDispatcher));

        final SourcedTypeRegistry registry = new SourcedTypeRegistry(world);
        registry.register(new Info(journal, CartActor.class, CartActor.class.getSimpleName()));
        registry.register(new Info(journal, OrderActor.class, OrderActor.class.getSimpleName()));

        registry.info(OrderActor.class)
                .registerEntryAdapter(OrderEvents.Created.class, new EventAdapter<>(OrderEvents.Created.class))
                .registerEntryAdapter(OrderEvents.PaymentReceived.class, new EventAdapter<>(OrderEvents.PaymentReceived.class))
                .registerEntryAdapter(OrderEvents.OrderShipped.class, new EventAdapter<>(OrderEvents.OrderShipped.class));

        registry.info(CartActor.class)
                .registerEntryAdapter(CreatedForUser.class, new EventAdapter<>(CreatedForUser.class))
                .registerEntryAdapter(ProductQuantityChangeEvent.class, new EventAdapter<>(ProductQuantityChangeEvent.class))
                .registerEntryAdapter(AllItemsRemovedEvent.class, new EventAdapter<>(AllItemsRemovedEvent.class));

        final CartResource cartResource = new CartResource(world.stage(),
                                                           world.addressFactory(),
                                                           CartQueryProvider.instance().cartQuery);

        final OrderResource orderResource = new OrderResource(world);
        final UserResource userResource = new UserResource(CartQueryProvider.instance().cartQuery);
        final Resources resources = Resources.are(
                cartResource.routes(),
                orderResource.routes(),
                userResource.routes());

        this.server = Server.startWith(world.stage(),
                resources,
                portNumber,
                Configuration.Sizing.define(),
                Configuration.Timing.define());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.server.stop();

                System.out.println("\n");
                System.out.println("=======================");
                System.out.println("Stopping ecommerce-service.");
                System.out.println("=======================");
                pause();
            }
        }));
    }

    @NotNull
    @SuppressWarnings("rawtypes")
    private Dispatcher createNoOpDispatcher() {
        return new Dispatcher() {
                public void controlWith(final DispatcherControl control) { }
                public void dispatch(Dispatchable d) { }
            };
    }

    public static Bootstrap forTest(final int portNumber) {
    	return new Bootstrap(portNumber);
    }
    
    static Bootstrap instance(final int portNumber) {
        if (instance == null) {
            instance = new Bootstrap(portNumber);
        }
        return instance;
    }

    static Bootstrap instance() {
      return instance(8081);
    }

    public static void main(final String[] args) {
        System.out.println("=======================");
        System.out.println("service: ecommerce-started.");
        System.out.println("=======================");
        Bootstrap.instance();
    }

    Completes<Boolean> serverStartup() {
        return server.startUp();
    }

    void stopAndCleanup() {
    	if (instance != null) {
    		Bootstrap.instance.server.stop();
    	}

        CartQueryProvider.deleteInstance();
        ProjectionDispatcherProvider.deleteInstance();

        instance = null;
    }

    private void pause() {
        try {
            Thread.sleep(1000L);
        } catch (Exception e) {
            // ignore
        }
    }
}
