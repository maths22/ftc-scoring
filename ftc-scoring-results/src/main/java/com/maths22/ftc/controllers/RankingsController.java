package com.maths22.ftc.controllers;

import com.maths22.ftc.entities.Division;
import com.maths22.ftc.entities.Event;
import com.maths22.ftc.entities.Team;
import com.maths22.ftc.entities.TeamEventAssignment;
import com.maths22.ftc.ranking.Ranking;
import com.maths22.ftc.ranking.RankingsCalculator;
import com.maths22.ftc.repositories.DivisionRepository;
import com.maths22.ftc.repositories.EventRepository;
import com.maths22.ftc.repositories.TeamEventAssignmentRepository;
import com.maths22.ftc.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jacob on 2/4/2017.
 */
@Controller
public class RankingsController {
    @Autowired
    private  EventRepository eventRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private RankingsCalculator rankingsCalculator;

    @Autowired
    private TeamEventAssignmentRepository teamEventAssignmentRepository;

    @RequestMapping("/{eventKey}/rankings")
    public String rankingsByEvent(@PathVariable String eventKey, Model model) {
        Optional<Event> event = eventRepository.findByKey(eventKey);

        if(!event.isPresent()) {
            throw new ResourceNotFoundException("Invalid eventKey " + eventKey);
        }

        Collection<Division> divisions = divisionRepository.getByEventId(event.get().getId());

        Map<Division, List<Ranking>> rankings = new HashMap<>();
        for(Division div : divisions) {
            rankings.put(div, rankingsCalculator.calculateRankings(div));
        }
        model.addAttribute("divisions", divisions);
        model.addAttribute("rankings", rankings);

        return "event/rankings";
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String msg) {
            super(msg);
        }
    }

}
