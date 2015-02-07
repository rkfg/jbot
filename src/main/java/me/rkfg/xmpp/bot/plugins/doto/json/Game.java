package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Game {

    @JsonProperty("players")
    private List<Player> players = new ArrayList<Player>();
    @JsonProperty("radiant_team")
    private TeamInfo radiantTeam;
    @JsonProperty("dire_team")
    private TeamInfo direTeam;
    @JsonProperty("lobby_id")
    private Long lobbyId;
    @JsonProperty("match_id")
    private Integer matchId;
    @JsonProperty("spectators")
    private Integer spectators;
    @JsonProperty("league_id")
    private Integer leagueId;
    @JsonProperty("stream_delay_s")
    private Integer streamDelayS;
    @JsonProperty("radiant_series_wins")
    private Integer radiantSeriesWins;
    @JsonProperty("dire_series_wins")
    private Integer direSeriesWins;
    @JsonProperty("series_type")
    private Integer seriesType;
    @JsonProperty("league_tier")
    private Integer leagueTier;
    @JsonProperty("scoreboard")
    private Scoreboard scoreboard;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("players")
    public List<Player> getPlayers() {
        return players;
    }

    @JsonProperty("players")
    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @JsonProperty("radiant_team")
    public TeamInfo getRadiantTeam() {
        return radiantTeam;
    }

    @JsonProperty("radiant_team")
    public void setRadiantTeam(TeamInfo radiantTeam) {
        this.radiantTeam = radiantTeam;
    }

    @JsonProperty("dire_team")
    public TeamInfo getDireTeam() {
        return direTeam;
    }

    @JsonProperty("dire_team")
    public void setDireTeam(TeamInfo direTeam) {
        this.direTeam = direTeam;
    }

    @JsonProperty("lobby_id")
    public Long getLobbyId() {
        return lobbyId;
    }

    @JsonProperty("lobby_id")
    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

    @JsonProperty("match_id")
    public Integer getMatchId() {
        return matchId;
    }

    @JsonProperty("match_id")
    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    @JsonProperty("spectators")
    public Integer getSpectators() {
        return spectators;
    }

    @JsonProperty("spectators")
    public void setSpectators(Integer spectators) {
        this.spectators = spectators;
    }

    @JsonProperty("league_id")
    public Integer getLeagueId() {
        return leagueId;
    }

    @JsonProperty("league_id")
    public void setLeagueId(Integer leagueId) {
        this.leagueId = leagueId;
    }

    @JsonProperty("stream_delay_s")
    public Integer getStreamDelayS() {
        return streamDelayS;
    }

    @JsonProperty("stream_delay_s")
    public void setStreamDelayS(Integer streamDelayS) {
        this.streamDelayS = streamDelayS;
    }

    @JsonProperty("radiant_series_wins")
    public Integer getRadiantSeriesWins() {
        return radiantSeriesWins;
    }

    @JsonProperty("radiant_series_wins")
    public void setRadiantSeriesWins(Integer radiantSeriesWins) {
        this.radiantSeriesWins = radiantSeriesWins;
    }

    @JsonProperty("dire_series_wins")
    public Integer getDireSeriesWins() {
        return direSeriesWins;
    }

    @JsonProperty("dire_series_wins")
    public void setDireSeriesWins(Integer direSeriesWins) {
        this.direSeriesWins = direSeriesWins;
    }

    @JsonProperty("series_type")
    public Integer getSeriesType() {
        return seriesType;
    }

    @JsonProperty("series_type")
    public void setSeriesType(Integer seriesType) {
        this.seriesType = seriesType;
    }

    @JsonProperty("league_tier")
    public Integer getLeagueTier() {
        return leagueTier;
    }

    @JsonProperty("league_tier")
    public void setLeagueTier(Integer leagueTier) {
        this.leagueTier = leagueTier;
    }

    @JsonProperty("scoreboard")
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @JsonProperty("scoreboard")
    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}