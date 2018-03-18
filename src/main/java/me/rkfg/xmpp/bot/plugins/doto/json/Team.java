package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Team
{

    @JsonProperty("score")
    private Integer score;
    @JsonProperty("tower_state")
    private Integer towerState;
    @JsonProperty("barracks_state")
    private Integer barracksState;
    @JsonProperty("picks")
    private List<Pick> picks = new ArrayList<>();
    @JsonProperty("bans")
    private List<Ban> bans = new ArrayList<>();
    @JsonProperty("players")
    private List<Player2> players = new ArrayList<>();
    @JsonProperty("abilities")
    private List<Ability> abilities = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("score")
    public Integer getScore() {
        return score;
    }

    @JsonProperty("score")
    public void setScore(Integer score) {
        this.score = score;
    }

    @JsonProperty("tower_state")
    public Integer getTowerState() {
        return towerState;
    }

    @JsonProperty("tower_state")
    public void setTowerState(Integer towerState) {
        this.towerState = towerState;
    }

    @JsonProperty("barracks_state")
    public Integer getBarracksState() {
        return barracksState;
    }

    @JsonProperty("barracks_state")
    public void setBarracksState(Integer barracksState) {
        this.barracksState = barracksState;
    }

    @JsonProperty("picks")
    public List<Pick> getPicks() {
        return picks;
    }

    @JsonProperty("picks")
    public void setPicks(List<Pick> picks) {
        this.picks = picks;
    }

    @JsonProperty("bans")
    public List<Ban> getBans() {
        return bans;
    }

    @JsonProperty("bans")
    public void setBans(List<Ban> bans) {
        this.bans = bans;
    }

    @JsonProperty("players")
    public List<Player2> getPlayers() {
        return players;
    }

    @JsonProperty("players")
    public void setPlayers(List<Player2> players) {
        this.players = players;
    }

    @JsonProperty("abilities")
    public List<Ability> getAbilities() {
        return abilities;
    }

    @JsonProperty("abilities")
    public void setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
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
