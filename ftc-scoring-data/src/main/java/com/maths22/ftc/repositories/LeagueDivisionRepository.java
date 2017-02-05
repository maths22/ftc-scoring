package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.LeagueDivision;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface LeagueDivisionRepository extends CrudRepository<LeagueDivision, UUID> {
    Collection<LeagueDivision> getByLeagueId(UUID league);

}
