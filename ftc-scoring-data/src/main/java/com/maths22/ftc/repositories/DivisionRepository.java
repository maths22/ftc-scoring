package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.Division;
import com.maths22.ftc.entities.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface DivisionRepository extends CrudRepository<Division, UUID> {
    Collection<Division> getByEventId(UUID event);

    Optional<Division> findByEventAndNumber(Event event, int number);
}
