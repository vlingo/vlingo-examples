package io.vlingo.xoom.examples.petclinic;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.examples.petclinic.infrastructure.ClientData;
import io.vlingo.xoom.examples.petclinic.infrastructure.ContactInformationData;
import io.vlingo.xoom.examples.petclinic.infrastructure.FullnameData;
import io.vlingo.xoom.examples.petclinic.infrastructure.persistence.ClientQueries;
import io.vlingo.xoom.examples.petclinic.infrastructure.persistence.ClientQueriesActor;
import io.vlingo.xoom.examples.petclinic.model.PostalAddress;
import io.vlingo.xoom.examples.petclinic.model.Telephone;
import io.vlingo.xoom.lattice.model.stateful.StatefulTypeRegistry;
import io.vlingo.xoom.symbio.store.dispatch.NoOpDispatcher;
import io.vlingo.xoom.symbio.store.state.StateStore;
import io.vlingo.xoom.symbio.store.state.inmemory.InMemoryStateStoreActor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientQueryTests {

    private static final StateStore.WriteResultInterest NOOP_WRI = new NoopWriteResultInterest();

    private World world;
    private StateStore stateStore;
    private ClientQueries queries;

    @BeforeEach
    public void setUp(){
        world = World.startWithDefaults("test-state-store-query");
        stateStore = world.actorFor(StateStore.class, InMemoryStateStoreActor.class,
                                    Collections.singletonList(new NoOpDispatcher()));
        StatefulTypeRegistry.registerAll(world, stateStore, ClientData.class);
        queries = world.actorFor(ClientQueries.class, ClientQueriesActor.class, stateStore);
    }

    @Test
    public void clientOfEmptyResult(){
        ClientData item = queries.clientOf("1").await();
        assertEquals("", item.id);
    }

    @Test
    public void clientOf(){
        ContactInformationData contact = ContactInformationData.of(
                PostalAddress.of("St.", "City", "UK", "123"),
                Telephone.of("99110011")
        );
        stateStore.write("1", ClientData.from("1", FullnameData.of("Harry", "Potter"), contact), 1, NOOP_WRI);
        stateStore.write("2", ClientData.from("2", FullnameData.of("Draco", "Malfoy"), contact), 1, NOOP_WRI);
        ClientData item = queries.clientOf("1").await();
        assertEquals("1", item.id);
        assertEquals("Harry", item.name.first);
        assertEquals("Potter", item.name.last);
        assertEquals("123", item.contact.postalAddress.postalCode);
        assertEquals("UK", item.contact.postalAddress.stateProvince);
        assertEquals("St.", item.contact.postalAddress.streetAddress);
        assertEquals("99110011", item.contact.telephone.number);

        item = queries.clientOf("2").await();
        assertEquals("2", item.id);
        assertEquals("Draco", item.name.first);
        assertEquals("Malfoy", item.name.last);
    }

    @Test
    public void clientsEmptyResult(){
        Collection<ClientData> animalTypes = queries.clients().await();
        assertEquals(0, animalTypes.size());
    }

    @Test
    public void clients(){
        ContactInformationData contact = ContactInformationData.of(
                PostalAddress.of("St.", "City", "UK", "123"),
                Telephone.of("99110011")
        );
        stateStore.write("1", ClientData.from("1", FullnameData.of("Harry", "Potter"), contact), 1, NOOP_WRI);
        stateStore.write("2", ClientData.from("2", FullnameData.of("Draco", "Potter"), contact), 1, NOOP_WRI);
        Collection<ClientData> animalTypes = queries.clients().await();
        assertEquals(2, animalTypes.size());
    }

    @Test
    public void clients2(){
        ContactInformationData contact = ContactInformationData.of(
                PostalAddress.of("St.", "City", "UK", "123"),
                Telephone.of("99110011")
        );
        stateStore.write("1", ClientData.from("1", FullnameData.of("Harry", "Potter"), contact), 1, NOOP_WRI);
        Collection<ClientData> animalTypes = queries.clients().await();
        assertEquals(1, animalTypes.size());
        ClientData item = animalTypes.stream().findFirst().orElseThrow(RuntimeException::new);
        assertEquals("1", item.id);
        assertEquals("Harry", item.name.first);
        assertEquals("Potter", item.name.last);
        assertEquals("123", item.contact.postalAddress.postalCode);
        assertEquals("UK", item.contact.postalAddress.stateProvince);
        assertEquals("St.", item.contact.postalAddress.streetAddress);
        assertEquals("99110011", item.contact.telephone.number);
    }
}
