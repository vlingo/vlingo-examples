package io.vlingo.xoom.examples.petclinic.model.pet;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.examples.petclinic.model.*;
import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Completes;

public interface Pet {

  Completes<PetState> register(final Name name, final Date birth, final Date death, final Kind kind, final Owner owner);

  static Completes<PetState> register(final Stage stage, final Name name, final Date birth, final Date death, final Kind kind, final Owner owner) {
    final io.vlingo.xoom.actors.Address _address = stage.addressFactory().uniquePrefixedWith("g-");
    final Pet _pet = stage.actorFor(Pet.class, Definition.has(PetEntity.class, Definition.parameters(_address.idString())), _address);
    return _pet.register(name, birth, death, kind, owner);
  }

  Completes<PetState> changeName(final Name name);

  Completes<PetState> recordBirth(final Date birth);

  Completes<PetState> recordDeath(final Date death);

  Completes<PetState> correctKind(final Kind kind);

  Completes<PetState> changeOwner(final Owner owner);

}