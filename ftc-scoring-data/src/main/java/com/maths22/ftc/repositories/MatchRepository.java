package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.Division;
import com.maths22.ftc.entities.Match;
import com.maths22.ftc.enums.MatchType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface MatchRepository extends CrudRepository<Match, UUID> {
//    @EntityGraph(value = "Match.scoresAndAlliances", type = EntityGraph.EntityGraphType.LOAD)
    List<Match> getByDivisionId(UUID event);

    Optional<Match> findByDivisionAndTypeAndNumber(Division division, MatchType type, int number);

}
