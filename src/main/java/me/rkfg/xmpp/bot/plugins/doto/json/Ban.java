package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"hero_id"})
public class Ban
{

    @JsonProperty("hero_id")
    private String heroId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("hero_id")
    public String getHeroId()
    {
        return heroId;
    }

    @JsonProperty("hero_id")
    public void setHeroId(String heroId)
    {
        this.heroId = heroId;
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