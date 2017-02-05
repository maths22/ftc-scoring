package com.maths22.ftc.season;

import com.maths22.ftc.entities.Score;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

/**
 * Created by Jacob on 9/6/2016.
 */
public interface SeasonManager<T extends Score> {
    String BEAN_NAME_PREFIX = "seasonManager-";

    T createNewScore();
    T convertLegacyArray (Score score, String[] description, int offset);
    CrudRepository<T, UUID> getScoreRepository();
    String getSlug();
}
