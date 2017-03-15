package de.renoth.blposition.domain.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import de.renoth.blposition.domain.Team;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamDeserializer extends JsonDeserializer<Team> {

    public static Map<Long, Team> TEAMS;

    @Override
    public Team deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        final Long id = node.get("TeamId").asLong();

        return TEAMS.get(id);
    }

    public static void initialize(List<Team> teamsInput) {
        if (TEAMS == null) {
            TEAMS = new HashMap<>();
        }

        TEAMS.clear();

        teamsInput.stream().forEach(team -> {
            TEAMS.put(team.getId(), team);
        });
    }
}
