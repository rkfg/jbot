package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "hero_id"
})
public class Pick {

    @JsonProperty("hero_id")
    private Integer heroId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("hero_id")
    public Integer getHeroId() {
        return heroId;
    }

    @JsonProperty("hero_id")
    public void setHeroId(Integer heroId) {
        this.heroId = heroId;
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