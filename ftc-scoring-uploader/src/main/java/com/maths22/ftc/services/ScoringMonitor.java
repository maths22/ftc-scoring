package com.maths22.ftc.services;

import com.maths22.ftc.entities.*;
import com.maths22.ftc.enums.MatchEventType;
import com.maths22.ftc.enums.MatchType;
import com.maths22.ftc.repositories.*;
import com.maths22.ftc.season.SeasonManager;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by Jacob on 2/2/2017.
 */
@Component
public class ScoringMonitor {
    private static final Logger log = LoggerFactory.getLogger(ScoringMonitor.class);

    private static final String JAR_FILE = "FTCScoring.jar";
    private static final String DIVISIONS_FILE = "divisions.txt";
    private static final String MATCHES_FILE = "matches.txt";
    private static final String AWARDS_FILE = "awards.txt";
    private static final String TEAMS_FILE = "teams.txt";

    private String path;
    private String key;
    private Map<Integer, Division> divisionMap;
    private Map<Integer, TeamEventAssignment> teamMap;
    private Map<Triple<Integer, MatchType, Integer>, Match> matchMap;
    private Season season;

    private Thread watchThread;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamEventAssignmentRepository teamEventAssignmentRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private AllianceRepository allianceRepository;

    @Autowired
    private Map<String, SeasonManager<? extends Score>> seasonManagerMap;


    public void setEvent(String key, String path) {
        reset();
        this.key = key;
        this.path = path;
    }

    private void reset() {
        divisionMap = new HashMap<>();
        teamMap = new HashMap<>();
        matchMap = new HashMap<>();
    }

    public boolean start() {
        File jarFile = new File(path + File.separator + JAR_FILE);
        if(!jarFile.exists()) {
            log.error("The path {} does not appear to be a valid scoring system directory", path);
            return false;
        }

        new Thread(this::refresh).start();

        Path watchPath = new File(path).toPath();
        watchThread = new Thread(() -> {
            try(WatchService service = new File(path).toPath().getFileSystem().newWatchService()) {

                // We register the path to the service
                // We watch for creation events
                watchPath.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

                // Start the infinite polling loop
                WatchKey key;
                while(true) {
                    key = service.take();

                    // Dequeueing events
                    WatchEvent.Kind<?> kind;
                    for(WatchEvent<?> watchEvent : key.pollEvents()) {
                        // Get the type of the event
                        kind = watchEvent.kind();
                        if (OVERFLOW != kind) {
                            refresh();
                        }
                    }

                    if(!key.reset() || Thread.interrupted()) {
                        break; //loop
                    }
                }

            } catch(IOException ex) {
                //TOOD: this is not good error handling
                ex.printStackTrace();
            } catch (InterruptedException e) {
                log.info("File watcher interrupted");
            }
        });

        watchThread.start();

        return true;
    }

    public void stop() {
        watchThread.interrupt();
    }

    @Transactional
    private void refresh() {
        long start = System.currentTimeMillis();
        processDivisions();
        processTeams();
        processMatches();
        long end = System.currentTimeMillis();
        log.debug("Refreshed in {} seconds", (end-start)/1000.0);
    }

    private void processDivisions() {
        File divisionsFile = new File(path + File.separator + DIVISIONS_FILE);
        if(!divisionsFile.exists()) {
            log.info("'{}' not found in directory '{}'", DIVISIONS_FILE, path);
            return;
        }

        List<String> divisionLines;
        try {
            divisionLines = Files.readAllLines(divisionsFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not read file {}", divisionsFile);
            return;
        }

        assert divisionLines.size() > 6;

        //NOTE: events have regions, not divisions
        String[] regionDateLine = divisionLines.get(7).split("\\|");
        Region region = regionRepository.findByName(regionDateLine[6]);
        int eventMonth = Integer.parseInt(regionDateLine[3]);
        int eventDay = Integer.parseInt(regionDateLine[4]);
        int eventYear = Integer.parseInt(regionDateLine[5]);
        LocalDate eventDate = LocalDate.of(eventYear, eventMonth, eventDay);

        Event event = eventRepository.findByKey(key).orElseGet(Event::new);
        event.setKey(key);
        event.setStartDate(eventDate);
        event.setEndDate(eventDate);
        event.setRegion(region);
        event.setSeason(season);
        event.setName(divisionLines.get(1));
        //TODO replace manual mapping
        EventType type = null;
        switch(divisionLines.get(3)) {
            case "SCRIMMAGE":
                type = eventTypeRepository.findByName("Scrimmage");
                break;
            case "LEAGUE":
                type = eventTypeRepository.findByName("League Meet");
                break;
            case "LEAGUE_CHAMPIONSHIP":
                type = eventTypeRepository.findByName("League Championship");
                break;
            case "QUALIFIER":
                type = eventTypeRepository.findByName("Qualifier");
                break;
            case "REGIONAL_CHAMPIONSHIP":
                type = eventTypeRepository.findByName("Regional Championship");
                break;
            case "SUPERREGIONAL":
                type = eventTypeRepository.findByName("Super-Regional");
                break;
            case "WORLDS":
                type = eventTypeRepository.findByName("World Championship");
                break;
            case "OTHER":
                type = eventTypeRepository.findByName("Other");
                break;
        }
        event.setType(type);
        event = eventRepository.save(event);

        for(int i = 7; i < divisionLines.size(); i++) {
            if(divisionLines.get(i).length() == 0) {
                continue;
            }
            String[] line = divisionLines.get(i).split("\\|");
            int number = Integer.parseInt(line[0]);
            Division division = divisionRepository.findByEventAndNumber(event, number).orElseGet(Division::new);
            division.setEvent(event);
            division.setName(line[1]);
            division.setNumber(number);
            division = divisionRepository.save(division);
            divisionMap.put(number, division);
        }

    }

    private void processTeams() {
        File teamsFile = new File(path + File.separator + TEAMS_FILE);
        if(!teamsFile.exists()) {
            log.info("'{}' not found in directory '{}'", TEAMS_FILE, path);
            return;
        }

        List<String> teamLines;
        try {
            teamLines = Files.readAllLines(teamsFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not read file {}", teamsFile);
            return;
        }

        /* teams.txt:
           0 division
           1 number
           2 name
           3 school
           4 city
           5 state
           6 country
           7 alreadyAdvanced
           8 participating
        */

        Map<Integer, TeamEventAssignment> localTeamMap = new HashMap<>();
        for (String teamLine : teamLines) {
            if (teamLine.length() == 0) {
                continue;
            }
            String[] line = teamLine.split("\\|");

            int divNumber = Integer.parseInt(line[0]);
            int teamNumber = Integer.parseInt(line[1]);

            TeamEventAssignment assignment;

            if((assignment = teamMap.get(teamNumber)) == null) {
                assignment = teamEventAssignmentRepository
                        .findByDivisionIdAndTeamNumber(divisionMap.get(divNumber).getId(), teamNumber)
                        .orElseGet(TeamEventAssignment::new);
            }

            Team team = assignment.getTeam() == null ? new Team() : assignment.getTeam();

            team.setNumber(teamNumber);
            team.setName(line[2]);
            team.setSchool(line[3]);
            team.setCity(line[4]);
            team.setState(line[5]);
            team.setCountry(line[6]);
            team = teamRepository.save(team);

            assignment.setDivision(divisionMap.get(divNumber));
            assignment.setTeam(team);
            assignment = teamEventAssignmentRepository.save(assignment);

            localTeamMap.put(team.getNumber(), assignment);
        }

        teamMap = localTeamMap;
    }

    private void processMatches() {
        File matchesFile = new File(path + File.separator + MATCHES_FILE);
        if(!matchesFile.exists()) {
            log.info("'{}' not found in directory '{}'", MATCHES_FILE, path);
            return;
        }

        List<String> matchLines;
        try {
            matchLines = Files.readAllLines(matchesFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not read file {}", matchesFile);
            return;
        }

        Map<String, Alliance> eliminationAlliances = new HashMap<>();

        for (String matchLine : matchLines) {
            if (matchLine.length() == 0) {
                continue;
            }
            String[] line = matchLine.split("\\|");

            int divisionNumber = Integer.parseInt(line[0]);
            Division division = divisionMap.get(divisionNumber);
            MatchType type = null;
            /*
            0: PRACTICE
            1: QUALIFICATION
            2: QUARTERFINAL
            3: SEMIFINAL
            4: FINAL
             */
            switch (Integer.parseInt(line[1])) {
                case 0:
                    type = MatchType.PRACTICE;
                    break;
                case 1:
                    type = MatchType.QUALIFICATION;
                    break;
                case 3:
                    type = MatchType.SEMIFINAL;
                    break;
                case 4:
                    type = MatchType.FINAL;
                    break;
            }

            int matchNumber = Integer.parseInt(line[2]);

            Triple<Integer, MatchType, Integer> description = Triple.of(division.getNumber(), type, matchNumber);

            Match match;

            if((match = matchMap.get(description)) == null) {
                match = matchRepository.findByDivisionAndTypeAndNumber(division,type,matchNumber).orElseGet(Match::new);
            }

            match.setDivision(division);
            match.setType(type);
            match.setNumber(Integer.parseInt(line[2]));


            Alliance redAlliance;
            Alliance blueAlliance;

            //note that matches must be processed in order for this to work
            //TODO this is inelegant
            if(match.getType().equals(MatchType.SEMIFINAL) || match.getType().equals(MatchType.FINAL)) {
                //First match of Semis
                if(match.getType().equals(MatchType.SEMIFINAL) && match.getNumber() % 10 == 1) {
                    redAlliance = Optional.ofNullable(match.getRedAlliance()).orElseGet(Alliance::new);
                    blueAlliance = Optional.ofNullable(match.getBlueAlliance()).orElseGet(Alliance::new);

                    redAlliance.setElimination(true);
                    blueAlliance.setElimination(true);

                    if(match.getNumber() / 10 == 1) {
                        redAlliance.setSeed(1);
                        blueAlliance.setSeed(4);
                    } else {
                        redAlliance.setSeed(2);
                        blueAlliance.setSeed(3);
                    }


                    redAlliance.setDivision(division);
                    for (int j = 3; j < 6; j++) {
                        int teamNo = Integer.parseInt(line[j]);
                        if (teamNo > 0) {
                            if(j - 3 >= redAlliance.getTeams().size()) {
                                redAlliance.getTeams().add(teamMap.get(teamNo));
                            } else {
                                redAlliance.getTeams().set(j - 3, teamMap.get(teamNo));
                            }
                        }
                    }

                    blueAlliance.setDivision(division);
                    for (int j = 6; j < 9; j++) {
                        int teamNo = Integer.parseInt(line[j]);
                        if (teamNo > 0) {
                            if(j - 6 >= blueAlliance.getTeams().size()) {
                                blueAlliance.getTeams().add(teamMap.get(teamNo));
                            } else {
                                blueAlliance.getTeams().set(j - 6, teamMap.get(teamNo));
                            }
                        }
                    }

                    eliminationAlliances.put(line[3], redAlliance);
                    eliminationAlliances.put(line[6], blueAlliance);
                } else {
                    redAlliance = eliminationAlliances.get(line[3]);
                    blueAlliance = eliminationAlliances.get(line[6]);
                }
            } else {
                redAlliance = Optional.ofNullable(match.getRedAlliance()).orElseGet(Alliance::new);
                blueAlliance = Optional.ofNullable(match.getBlueAlliance()).orElseGet(Alliance::new);
                redAlliance.setDivision(division);
                for (int j = 3; j < 6; j++) {
                    int teamNo = Integer.parseInt(line[j]);
                    if (teamNo > 0) {
                        if(j - 3 >= redAlliance.getTeams().size()) {
                            redAlliance.getTeams().add(teamMap.get(teamNo));
                        } else {
                            redAlliance.getTeams().set(j - 3, teamMap.get(teamNo));
                        }
                    }
                }

                blueAlliance.setDivision(division);
                for (int j = 6; j < 9; j++) {
                    int teamNo = Integer.parseInt(line[j]);
                    if (teamNo > 0) {
                        if(j - 6 >= blueAlliance.getTeams().size()) {
                            blueAlliance.getTeams().add(teamMap.get(teamNo));
                        } else {
                            blueAlliance.getTeams().set(j - 6, teamMap.get(teamNo));
                        }
                    }
                }
            }


            for (int j = 0; j < redAlliance.getTeams().size(); j++) {
                int result = Integer.parseInt(line[j + 9]);
                TeamEventAssignment team = redAlliance.getTeams().get(j);
                if (result == 1) {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.RED_CARD)))
                                    ).collect(Collectors.toList()));
                    MatchEvent matchEvent = match.getMatchEvents().stream()
                            .filter(me -> (me.getTeam().equals(team) && (me.getType().equals(MatchEventType.NO_SHOW))))
                            .findFirst().orElseGet(MatchEvent::new);
                    matchEvent.setMatch(match);
                    matchEvent.setTeam(team);
                    matchEvent.setType(MatchEventType.NO_SHOW);
                    match.getMatchEvents().add(matchEvent);
                } else if (result == 2) {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.NO_SHOW)))
                                    ).collect(Collectors.toList()));
                    MatchEvent matchEvent = match.getMatchEvents().stream()
                            .filter(me -> (me.getTeam().equals(team) && (me.getType().equals(MatchEventType.RED_CARD))))
                            .findFirst().orElseGet(MatchEvent::new);
                    matchEvent.setMatch(match);
                    matchEvent.setTeam(team);
                    matchEvent.setType(MatchEventType.RED_CARD);
                    match.getMatchEvents().add(matchEvent);
                } else {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                            .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.RED_CARD) ||
                                    me.getType().equals(MatchEventType.NO_SHOW))))
                            .collect(Collectors.toList()));
                }
            }

            for (int j = 0; j < redAlliance.getTeams().size(); j++) {
                boolean result = Boolean.parseBoolean(line[j + 12]);
                TeamEventAssignment team = redAlliance.getTeams().get(j);
                if (result) {
                    MatchEvent matchEvent = match.getMatchEvents().stream()
                            .filter(me -> (me.getTeam().equals(team) && (me.getType().equals(MatchEventType.YELLOW_CARD))))
                            .findFirst().orElseGet(MatchEvent::new);
                    matchEvent.setMatch(match);
                    matchEvent.setTeam(team);
                    matchEvent.setType(MatchEventType.YELLOW_CARD);
                    match.getMatchEvents().add(matchEvent);
                } else {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.YELLOW_CARD))))
                                    .collect(Collectors.toList()));
                }
            }

            for (int j = 0; j < blueAlliance.getTeams().size(); j++) {
                int result = Integer.parseInt(line[j + 15]);
                TeamEventAssignment team = blueAlliance.getTeams().get(j);
                if (result == 1) {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.RED_CARD)))
                                    ).collect(Collectors.toList()));
                    MatchEvent matchEvent = match.getMatchEvents().stream()
                            .filter(me -> (me.getTeam().equals(team) && (me.getType().equals(MatchEventType.NO_SHOW))))
                            .findFirst().orElseGet(MatchEvent::new);
                    matchEvent.setMatch(match);
                    matchEvent.setTeam(team);
                    matchEvent.setType(MatchEventType.NO_SHOW);
                    match.getMatchEvents().add(matchEvent);
                } else if (result == 2) {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.NO_SHOW)))
                                    ).collect(Collectors.toList()));
                    MatchEvent matchEvent = match.getMatchEvents().stream()
                            .filter(me -> (me.getTeam().equals(team) && (me.getType().equals(MatchEventType.RED_CARD))))
                            .findFirst().orElseGet(MatchEvent::new);
                    matchEvent.setMatch(match);
                    matchEvent.setTeam(team);
                    matchEvent.setType(MatchEventType.RED_CARD);
                    match.getMatchEvents().add(matchEvent);
                } else {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.RED_CARD) ||
                                            me.getType().equals(MatchEventType.NO_SHOW))))
                                    .collect(Collectors.toList()));
                }
            }

            for (int j = 0; j < blueAlliance.getTeams().size(); j++) {
                boolean result = Boolean.parseBoolean(line[j + 18]);
                TeamEventAssignment team = blueAlliance.getTeams().get(j);
                if (result) {
                    MatchEvent matchEvent = match.getMatchEvents().stream()
                            .filter(me -> (me.getTeam().equals(team) && (me.getType().equals(MatchEventType.YELLOW_CARD))))
                            .findFirst().orElseGet(MatchEvent::new);
                    matchEvent.setMatch(match);
                    matchEvent.setTeam(team);
                    matchEvent.setType(MatchEventType.YELLOW_CARD);
                    match.getMatchEvents().add(matchEvent);
                } else {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.YELLOW_CARD))))
                                    .collect(Collectors.toList()));
                }
            }

            for (int j = 0; j < redAlliance.getTeams().size(); j++) {
                int result = Integer.parseInt(line[j + 21]);
                TeamEventAssignment team = redAlliance.getTeams().get(j);
                if (result == 1) {
                    MatchEvent matchEvent = match.getMatchEvents().stream()
                            .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.SURROGATE))))
                            .findFirst().orElseGet(MatchEvent::new);
                    matchEvent.setMatch(match);
                    matchEvent.setTeam(redAlliance.getTeams().get(j));
                    matchEvent.setType(MatchEventType.SURROGATE);
                    match.getMatchEvents().add(matchEvent);
                } else {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.SURROGATE))))
                                    .collect(Collectors.toList()));
                }
            }

            for (int j = 0; j < blueAlliance.getTeams().size(); j++) {
                int result = Integer.parseInt(line[j + 24]);
                TeamEventAssignment team = blueAlliance.getTeams().get(j);
                if (result == 1) {
                    MatchEvent matchEvent = match.getMatchEvents().stream()
                            .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.SURROGATE))))
                            .findFirst().orElseGet(MatchEvent::new);
                    matchEvent.setMatch(match);
                    matchEvent.setTeam(redAlliance.getTeams().get(j));
                    matchEvent.setType(MatchEventType.SURROGATE);
                    match.getMatchEvents().add(matchEvent);
                } else {
                    match.setMatchEvents(
                            match.getMatchEvents().stream()
                                    .filter(me -> !(me.getTeam().equals(team) && (me.getType().equals(MatchEventType.SURROGATE))))
                                    .collect(Collectors.toList()));
                }
            }

            match.setScored(Integer.parseInt(line[27]) == 1);
            match.setRedAlliance(redAlliance);
            match.setBlueAlliance(blueAlliance);

            SeasonManager<? extends Score> seasonManager =
                    seasonManagerMap.get(SeasonManager.BEAN_NAME_PREFIX + division.getEvent().getSeason().getSlug());
            Score redScore = Optional.ofNullable(match.getRedScore()).orElseGet(seasonManager::createNewScore);
            seasonManager.convertLegacyArray(redScore, line, 28);
            match.setRedScore(redScore);
            Score blueScore = Optional.ofNullable(match.getBlueScore()).orElseGet(seasonManager::createNewScore);
            seasonManager.convertLegacyArray(blueScore, line, 28 + (line.length - 28) / 2);
            match.setBlueScore(blueScore);

            allianceRepository.save(redAlliance);
            allianceRepository.save(blueAlliance);
            match = matchRepository.save(match);

            matchMap.put(description, match);
        }

    }

    public void setSeason(Season season) {
        this.season = season;
    }
}
