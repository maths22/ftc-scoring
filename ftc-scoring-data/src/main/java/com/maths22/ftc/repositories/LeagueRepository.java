package com.maths22.ftc.repositories;

import com.maths22.ftc.entities.League;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by jburroughs on 7/6/16.
 */
@Component
public interface LeagueRepository extends CrudRepository<League, UUID> {
    Collection<League> getByRegionIdAndSeasonSlug(UUID region, String season);
    Collection<League> getByRegionIdAndSeasonId(UUID region, UUID season);
}
