import de.renoth.blposition.BlPosition;
import de.renoth.blposition.domain.League;
import de.renoth.blposition.domain.Team;
import de.renoth.blposition.service.LeagueService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = BlPosition.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
public class PossibleWorstPlacementTest {
    @Autowired
    private LeagueService leagueService;

    @Test
    public void testWorstPlacement() throws IOException {
        leagueService.calculatePossiblePlacements();
    }

    @Test
    public void updateRelevantTeamsTest() throws IOException {
        League league = leagueService.getCurrentLeague();

        Team team = league.getTable().first();

        final int[] currentMatchday = {25};

        List<Team> relevantTeams = new ArrayList<>();

        leagueService.updateRelevantTeams(league, relevantTeams, team, currentMatchday[0], false);

        Assert.assertThat(relevantTeams.size(), is(11));

        currentMatchday[0] = 26;

        leagueService.updateRelevantTeams(league, relevantTeams, team, currentMatchday[0], false);

        Assert.assertThat(relevantTeams.size(), is(8));

        team.setPoints(49);
        currentMatchday[0] = 25;

        leagueService.updateRelevantTeams(league, relevantTeams, team, currentMatchday[0], false);

        Assert.assertThat(relevantTeams.size(), is(15));
    }
}
