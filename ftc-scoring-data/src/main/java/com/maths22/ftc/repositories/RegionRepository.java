package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.Region;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface RegionRepository extends Repository<Region, UUID> {
    Collection<Region> findAll();

    Region findOne(UUID id);

    Region findByName(String name);
}
