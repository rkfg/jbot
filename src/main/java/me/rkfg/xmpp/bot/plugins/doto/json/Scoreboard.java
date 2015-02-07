package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "duration",
        "roshan_respawn_timer",
        "radiant",
        "dire"
})
public class Scoreboard {

    @JsonProperty("duration")
    private Double duration;
    @JsonProperty("roshan_respawn_timer")
    private Integer roshanRespawnTimer;
    @JsonProperty("radiant")
    private Team radiant;
    @JsonProperty("dire")
    private Team dire;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("duration")
    public Double getDuration() {
        return duration;
    }


    @JsonProperty("duration")
    public void setDuration(Double duration) {
        this.duration = duration;
    }

    @JsonProperty("roshan_respawn_timer")
    public Integer getRoshanRespawnTimer() {
        return roshanRespawnTimer;
    }

    @JsonProperty("roshan_respawn_timer")
    public void setRoshanRespawnTimer(Integer roshanRespawnTimer) {
        this.roshanRespawnTimer = roshanRespawnTimer;
    }

    @JsonProperty("radiant")
    public Team getRadiant() {
        return radiant;
    }

    @JsonProperty("radiant")
    public void setRadiant(Team radiant) {
        this.radiant = radiant;
    }

    @JsonProperty("dire")
    public Team getDire() {
        return dire;
    }

    @JsonProperty("dire")
    public void setDire(Team dire) {
        this.dire = dire;
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

