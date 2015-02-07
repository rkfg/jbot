package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"ability_id", "ability_level"})
public class Ability
{

    @JsonProperty("ability_id")
    private Integer abilityId;
    @JsonProperty("ability_level")
    private Integer abilityLevel;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ability_id")
    public Integer getAbilityId()
    {
        return abilityId;
    }

    @JsonProperty("ability_id")
    public void setAbilityId(Integer abilityId)
    {
        this.abilityId = abilityId;
    }

    @JsonProperty("ability_level")
    public Integer getAbilityLevel()
    {
        return abilityLevel;
    }

    @JsonProperty("ability_level")
    public void setAbilityLevel(Integer abilityLevel)
    {
        this.abilityLevel = abilityLevel;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

}