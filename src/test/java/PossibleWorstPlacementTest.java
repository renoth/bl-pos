import de.renoth.blposition.BlPosition;
import de.renoth.blposition.service.LeagueService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;

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
        leagueService.possibleWorstPlacementForLeadingTeam();
    }

}
