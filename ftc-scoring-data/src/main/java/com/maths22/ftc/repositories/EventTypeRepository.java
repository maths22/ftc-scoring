package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.EventType;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public interface EventTypeRepository extends Repository<EventType, UUID> {
    Collection<EventType> findAll();

    EventType findOne(UUID id);

    EventType findByName(String name);
}
