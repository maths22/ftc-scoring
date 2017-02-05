package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.Season;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface SeasonRepository extends CrudRepository<Season, UUID> {


}
