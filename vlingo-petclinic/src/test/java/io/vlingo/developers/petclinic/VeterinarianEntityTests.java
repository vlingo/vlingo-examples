package io.vlingo.developers.petclinic;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.actors.testkit.TestWorld;
import io.vlingo.developers.petclinic.infrastructure.persistence.VeterinarianNameChangedAdapter;
import io.vlingo.developers.petclinic.infrastructure.persistence.VeterinarianRegisteredAdapter;
import io.vlingo.developers.petclinic.model.ContactInformation;
import io.vlingo.developers.petclinic.model.Fullname;
import io.vlingo.developers.petclinic.model.PostalAddress;
import io.vlingo.developers.petclinic.model.Telephone;
import io.vlingo.developers.petclinic.model.client.Specialty;
import io.vlingo.developers.petclinic.model.veterinarian.*;
import io.vlingo.lattice.model.sourcing.SourcedTypeRegistry;
import io.vlingo.symbio.BaseEntry;
import io.vlingo.symbio.EntryAdapterProvider;
import io.vlingo.symbio.store.journal.Journal;
import io.vlingo.symbio.store.journal.inmemory.InMemoryJournalActor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VeterinarianEntityTests {

    private World world;
    private TestWorld testWorld;

    private Journal<String> journal;
    private MockJournalDispatcher dispatcher;
    private SourcedTypeRegistry registry;

    private Veterinarian vet;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setUp(){
        testWorld = TestWorld.startWithDefaults("test-es");

        world = testWorld.world();

        dispatcher = new MockJournalDispatcher();

        EntryAdapterProvider entryAdapterProvider = EntryAdapterProvider.instance(world);

        entryAdapterProvider.registerAdapter(VeterinarianRegistered.class, new VeterinarianRegisteredAdapter());
        entryAdapterProvider.registerAdapter(VeterinarianNameChanged.class, new VeterinarianNameChangedAdapter());

        journal = world.actorFor(Journal.class, InMemoryJournalActor.class, Collections.singletonList(dispatcher));

        registry = new SourcedTypeRegistry(world);
        registry.register(new SourcedTypeRegistry.Info(journal, VeterinarianEntity.class, VeterinarianEntity.class.getSimpleName()));

        vet = world.actorFor(Veterinarian.class, VeterinarianEntity.class, "#1");
    }

    @Test
    public void offer(){
        final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

        final ContactInformation contact = ContactInformation.of(
                PostalAddress.of("Ave.", "New-York", "US", "321"),
                Telephone.of("991100")
        );
        VeterinarianState vetState = vet.register(Fullname.of("Rubeus", "Hagrid"), contact, Specialty.of("Behaviour")).await();

        assertNotNull(vetState);
        assertNotNull(vetState.name);
        assertNotNull(vetState.contact);
        assertNotNull(vetState.contact.postalAddress);
        assertNotNull(vetState.contact.telephone);
        assertNotNull(vetState.specialty);
        assertEquals("#1", vetState.id);
        assertEquals("Rubeus", vetState.name.first);
        assertEquals("Hagrid", vetState.name.last);

        assertEquals("Ave.", vetState.contact.postalAddress.streetAddress);
        assertEquals("New-York", vetState.contact.postalAddress.city);
        assertEquals("US", vetState.contact.postalAddress.stateProvince);
        assertEquals("321", vetState.contact.postalAddress.postalCode);
        assertEquals("991100", vetState.contact.telephone.number);

        assertEquals("Behaviour", vetState.specialty.specialtyTypeId);

        // this will block until the first event is persisted in the Journal
        assertEquals(1, (int) dispatcherAccess.readFrom("entriesCount"));
        BaseEntry<String> appendedAt0 = dispatcherAccess.readFrom("appendedAt", 0);
        assertNotNull(appendedAt0);
        assertEquals(VeterinarianRegistered.class.getName(), appendedAt0.typeName());
    }

    @Test
    public void rename(){
        offer();

        final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

        VeterinarianState vetState = vet.changeName(Fullname.of("Albus", "Dumbledore")).await();

        assertEquals("#1", vetState.id);
        assertEquals("Albus", vetState.name.first);
        assertEquals("Dumbledore", vetState.name.last);

        // this will block until the first event is persisted in the Journal
        assertEquals(2, (int) dispatcherAccess.readFrom("entriesCount"));
        BaseEntry<String> appendedAt1 = dispatcherAccess.readFrom("appendedAt", 1);
        assertNotNull(appendedAt1);
        assertEquals(VeterinarianNameChanged.class.getName(), appendedAt1.typeName());
    }

    @Test
    public void specialize(){
        offer();

        final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

        VeterinarianState vetState = vet.specializesIn(Specialty.of("Ophthalmology")).await();

        assertEquals("#1", vetState.id);
        assertEquals("Ophthalmology", vetState.specialty.specialtyTypeId);

        // this will block until the first event is persisted in the Journal
        assertEquals(2, (int) dispatcherAccess.readFrom("entriesCount"));
        BaseEntry<String> appendedAt1 = dispatcherAccess.readFrom("appendedAt", 1);
        assertNotNull(appendedAt1);
        assertEquals(VeterinarianSpecialtyChosen.class.getName(), appendedAt1.typeName());
    }

    @Test
    public void contactUpdate(){
        offer();

        final AccessSafely dispatcherAccess = dispatcher.afterCompleting(1);

        final ContactInformation contact = ContactInformation.of(
                PostalAddress.of("Ave.", "New-York", "US", "321"),
                Telephone.of("991100")
        );
        VeterinarianState vetState = vet.changeContactInformation(contact).await();

        assertEquals("#1", vetState.id);
        assertEquals("Ave.", vetState.contact.postalAddress.streetAddress);
        assertEquals("New-York", vetState.contact.postalAddress.city);
        assertEquals("US", vetState.contact.postalAddress.stateProvince);
        assertEquals("321", vetState.contact.postalAddress.postalCode);
        assertEquals("991100", vetState.contact.telephone.number);

        // this will block until the first event is persisted in the Journal
        assertEquals(2, (int) dispatcherAccess.readFrom("entriesCount"));
        BaseEntry<String> appendedAt1 = dispatcherAccess.readFrom("appendedAt", 1);
        assertNotNull(appendedAt1);
        assertEquals(VeterinarianContactInformationChanged.class.getName(), appendedAt1.typeName());
    }
}
