package com.maths22.ftc.controllers;

import com.maths22.ftc.entities.Division;
import com.maths22.ftc.entities.Event;
import com.maths22.ftc.repositories.DivisionRepository;
import com.maths22.ftc.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.*;

/**
 * Created by Jacob on 2/4/2017.
 */
@Controller
public class MatchResultsController {
    @Autowired
    private  EventRepository eventRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @RequestMapping("/matchResults/{eventKey}")
    public String rankingsByEvent(@PathVariable String eventKey, Model model) {
        Optional<Event> event = eventRepository.findByKey(eventKey);

        if(!event.isPresent()) {
            throw new ResourceNotFoundException("Invalid eventKey " + eventKey);
        }

        Collection<Division> divisions = divisionRepository.getByEventId(event.get().getId());

        model.addAttribute("divisions", divisions);

        return "matches/results";
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String msg) {
            super(msg);
        }
    }

}
