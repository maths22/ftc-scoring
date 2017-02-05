package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.Team;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface TeamRepository extends CrudRepository<Team, Integer> {
    Optional<Team> findByNumber(int number);
}
