package me.rkfg.xmpp.bot.plugins.doto.json;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "player_slot",
        "account_id",
        "hero_id",
        "kills",
        "death",
        "assists",
        "last_hits",
        "denies",
        "gold",
        "level",
        "gold_per_min",
        "xp_per_min",
        "ultimate_state",
        "ultimate_cooldown",
        "item0",
        "item1",
        "item2",
        "item3",
        "item4",
        "item5",
        "respawn_timer",
        "position_x",
        "position_y",
        "net_worth"
})
public class Player2 {

    @JsonProperty("player_slot")
    private Integer playerSlot;
    @JsonProperty("account_id")
    private Integer accountId;
    @JsonProperty("hero_id")
    private Integer heroId;
    @JsonProperty("kills")
    private Integer kills;
    @JsonProperty("death")
    private Integer death;
    @JsonProperty("assists")
    private Integer assists;
    @JsonProperty("last_hits")
    private Integer lastHits;
    @JsonProperty("denies")
    private Integer denies;
    @JsonProperty("gold")
    private Integer gold;
    @JsonProperty("level")
    private Integer level;
    @JsonProperty("gold_per_min")
    private Integer goldPerMin;
    @JsonProperty("xp_per_min")
    private Integer xpPerMin;
    @JsonProperty("ultimate_state")
    private Integer ultimateState;
    @JsonProperty("ultimate_cooldown")
    private Integer ultimateCooldown;
    @JsonProperty("item0")
    private Integer item0;
    @JsonProperty("item1")
    private Integer item1;
    @JsonProperty("item2")
    private Integer item2;
    @JsonProperty("item3")
    private Integer item3;
    @JsonProperty("item4")
    private Integer item4;
    @JsonProperty("item5")
    private Integer item5;
    @JsonProperty("respawn_timer")
    private Integer respawnTimer;
    @JsonProperty("position_x")
    private Double positionX;
    @JsonProperty("position_y")
    private Double positionY;
    @JsonProperty("net_worth")
    private Integer netWorth;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("player_slot")
    public Integer getPlayerSlot() {
        return playerSlot;
    }

    @JsonProperty("player_slot")
    public void setPlayerSlot(Integer playerSlot) {
        this.playerSlot = playerSlot;
    }

    @JsonProperty("account_id")
    public Integer getAccountId() {
        return accountId;
    }

    @JsonProperty("account_id")
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("hero_id")
    public Integer getHeroId() {
        return heroId;
    }

    @JsonProperty("hero_id")
    public void setHeroId(Integer heroId) {
        this.heroId = heroId;
    }

    @JsonProperty("kills")
    public Integer getKills() {
        return kills;
    }

    @JsonProperty("kills")
    public void setKills(Integer kills) {
        this.kills = kills;
    }

    @JsonProperty("death")
    public Integer getDeath() {
        return death;
    }

    @JsonProperty("death")
    public void setDeath(Integer death) {
        this.death = death;
    }

    @JsonProperty("assists")
    public Integer getAssists() {
        return assists;
    }

    @JsonProperty("assists")
    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    @JsonProperty("last_hits")
    public Integer getLastHits() {
        return lastHits;
    }

    @JsonProperty("last_hits")
    public void setLastHits(Integer lastHits) {
        this.lastHits = lastHits;
    }

    @JsonProperty("denies")
    public Integer getDenies() {
        return denies;
    }

    @JsonProperty("denies")
    public void setDenies(Integer denies) {
        this.denies = denies;
    }

    @JsonProperty("gold")
    public Integer getGold() {
        return gold;
    }

    @JsonProperty("gold")
    public void setGold(Integer gold) {
        this.gold = gold;
    }

    @JsonProperty("level")
    public Integer getLevel() {
        return level;
    }

    @JsonProperty("level")
    public void setLevel(Integer level) {
        this.level = level;
    }

    @JsonProperty("gold_per_min")
    public Integer getGoldPerMin() {
        return goldPerMin;
    }

    @JsonProperty("gold_per_min")
    public void setGoldPerMin(Integer goldPerMin) {
        this.goldPerMin = goldPerMin;
    }

    @JsonProperty("xp_per_min")
    public Integer getXpPerMin() {
        return xpPerMin;
    }

    @JsonProperty("xp_per_min")
    public void setXpPerMin(Integer xpPerMin) {
        this.xpPerMin = xpPerMin;
    }

    @JsonProperty("ultimate_state")
    public Integer getUltimateState() {
        return ultimateState;
    }

    @JsonProperty("ultimate_state")
    public void setUltimateState(Integer ultimateState) {
        this.ultimateState = ultimateState;
    }

    @JsonProperty("ultimate_cooldown")
    public Integer getUltimateCooldown() {
        return ultimateCooldown;
    }

    @JsonProperty("ultimate_cooldown")
    public void setUltimateCooldown(Integer ultimateCooldown) {
        this.ultimateCooldown = ultimateCooldown;
    }

    @JsonProperty("item0")
    public Integer getItem0() {
        return item0;
    }

    @JsonProperty("item0")
    public void setItem0(Integer item0) {
        this.item0 = item0;
    }

    @JsonProperty("item1")
    public Integer getItem1() {
        return item1;
    }

    @JsonProperty("item1")
    public void setItem1(Integer item1) {
        this.item1 = item1;
    }

    @JsonProperty("item2")
    public Integer getItem2() {
        return item2;
    }

    @JsonProperty("item2")
    public void setItem2(Integer item2) {
        this.item2 = item2;
    }

    @JsonProperty("item3")
    public Integer getItem3() {
        return item3;
    }

    @JsonProperty("item3")
    public void setItem3(Integer item3) {
        this.item3 = item3;
    }

    @JsonProperty("item4")
    public Integer getItem4() {
        return item4;
    }

    @JsonProperty("item4")
    public void setItem4(Integer item4) {
        this.item4 = item4;
    }

    @JsonProperty("item5")
    public Integer getItem5() {
        return item5;
    }

    @JsonProperty("item5")
    public void setItem5(Integer item5) {
        this.item5 = item5;
    }

    @JsonProperty("respawn_timer")
    public Integer getRespawnTimer() {
        return respawnTimer;
    }

    @JsonProperty("respawn_timer")
    public void setRespawnTimer(Integer respawnTimer) {
        this.respawnTimer = respawnTimer;
    }

    @JsonProperty("position_x")
    public Double getPositionX() {
        return positionX;
    }

    @JsonProperty("position_x")
    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    @JsonProperty("position_y")
    public Double getPositionY() {
        return positionY;
    }

    @JsonProperty("position_y")
    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    @JsonProperty("net_worth")
    public Integer getNetWorth() {
        return netWorth;
    }

    @JsonProperty("net_worth")
    public void setNetWorth(Integer netWorth) {
        this.netWorth = netWorth;
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