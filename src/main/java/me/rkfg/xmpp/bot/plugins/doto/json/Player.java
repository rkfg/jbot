package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Player {

    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("hero_id")
    private String heroId;
    @JsonProperty("team")
    private Integer team;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("account_id")
    public String getAccountId() {
        return accountId;
    }

    @JsonProperty("account_id")
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("hero_id")
    public String getHeroId() {
        return heroId;
    }

    @JsonProperty("hero_id")
    public void setHeroId(String heroId) {
        this.heroId = heroId;
    }

    @JsonProperty("team")
    public Integer getTeam() {
        return team;
    }

    @JsonProperty("team")
    public void setTeam(Integer team) {
        this.team = team;
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