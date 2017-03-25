package de.renoth.blposition.controller;

import de.renoth.blposition.domain.League;
import de.renoth.blposition.service.LeagueService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/league")
public class LeagueController {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LeagueController.class);

    @Autowired
    private LeagueService leagueService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<League> index() {
        LOG.debug("Getting current league");

        Optional<League> leagueOptional = leagueService.getCalculatedLeague();

        if (leagueOptional.isPresent()) {
            return ResponseEntity.ok(leagueOptional.get());
        }

        return ResponseEntity.noContent().build();
    }

}
