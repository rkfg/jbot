package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
public class TeamInfo
{

    @JsonProperty("team_name")
    private String teamName;
    @JsonProperty("team_id")
    private String teamId;
    @JsonProperty("team_logo")
    private String teamLogo;
    @JsonProperty("complete")
    private Boolean complete;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("team_name")
    public String getTeamName()
    {
        return teamName;
    }

    @JsonProperty("team_name")
    public void setTeamName(String teamName) {
        if (null!= teamName)
            this.teamName = teamName;
        else
        {
            this.teamName = "Noname";
        }
    }

    @JsonProperty("team_id")
    public String getTeamId()
    {
        return teamId;
    }

    @JsonProperty("team_id")
    public void setTeamId(String teamId)
    {
        this.teamId = teamId;
    }

    @JsonProperty("team_logo")
    public String getTeamLogo()
    {
        return teamLogo;
    }

    @JsonProperty("team_logo")
    public void setTeamLogo(String teamLogo)
    {
        this.teamLogo = teamLogo;
    }

    @JsonProperty("complete")
    public Boolean getComplete()
    {
        return complete;
    }

    @JsonProperty("complete")
    public void setComplete(Boolean complete)
    {
        this.complete = complete;
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
