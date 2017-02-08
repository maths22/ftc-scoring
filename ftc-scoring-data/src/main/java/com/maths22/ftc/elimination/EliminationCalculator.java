package com.maths22.ftc.elimination;

import com.maths22.ftc.entities.Alliance;
import com.maths22.ftc.entities.Division;
import com.maths22.ftc.enums.MatchType;
import com.maths22.ftc.repositories.AllianceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Jacob on 2/7/2017.
 */
@Component
public class EliminationCalculator {
    @Autowired
    public AllianceRepository allianceRepository;

    //TODO: generify?
    public Optional<EliminationRound> calculateEliminationRounds(Division division) {
        Collection<Alliance> allianceList = allianceRepository.getByDivisionIdAndElimination(division.getId(), true);
        if(allianceList.size() != 4) {
            return Optional.empty();
        }
        EliminationRound finalRound = EliminationRound.fromAlliances(allianceList);
        finalRound.setMatchList(division.getMatches().stream()
                .filter(m -> m.getType().equals(MatchType.FINAL))
                .collect(Collectors.toList()));
        assert finalRound.getRedRound().isPresent();
        finalRound.getRedRound().get().setMatchList(division.getMatches().stream()
                .filter(m -> m.getType().equals(MatchType.SEMIFINAL) && m.getNumber() / 10 == 1)
                .collect(Collectors.toList()));
        assert finalRound.getBlueRound().isPresent();
        finalRound.getBlueRound().get().setMatchList(division.getMatches().stream()
                .filter(m -> m.getType().equals(MatchType.SEMIFINAL)  && m.getNumber() / 10 == 2)
                .collect(Collectors.toList()));

        return Optional.of(finalRound);
    }
}
