package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.Alliance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface AllianceRepository extends CrudRepository<Alliance, UUID> {
    Collection<Alliance> getByDivisionIdAndElimination(UUID event, boolean elimination);

}
