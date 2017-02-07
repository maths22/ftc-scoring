package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.TeamEventAssignment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface TeamEventAssignmentRepository extends CrudRepository<TeamEventAssignment, UUID> {
    Optional<TeamEventAssignment> findByDivisionIdAndTeamNumber(UUID divisionId, int team);


    List<TeamEventAssignment> findByDivisionId(UUID divisionId);
}
