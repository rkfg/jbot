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
    private Integer teamId;
    @JsonProperty("team_logo")
    private Long teamLogo;
    @JsonProperty("complete")
    private Boolean complete;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
    public Integer getTeamId()
    {
        return teamId;
    }

    @JsonProperty("team_id")
    public void setTeamId(Integer teamId)
    {
        this.teamId = teamId;
    }

    @JsonProperty("team_logo")
    public Long getTeamLogo()
    {
        return teamLogo;
    }

    @JsonProperty("team_logo")
    public void setTeamLogo(Long teamLogo)
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
