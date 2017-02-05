package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.Score;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface ScoreRepository extends CrudRepository<Score, UUID> {

}
