package me.rkfg.xmpp.bot.plugins.game.effect;

/**
 * Changes stats on attach, used for traits
 *
 */
public class TraitStatsEffect extends AttrStatsEffect {

    public static final String TYPE = "tstat";

    public TraitStatsEffect() {
        super(TYPE, "изменение стат от трейта");
    }
    
    @Override
    public void onBeforeAttach() {
        applyEffectToTarget(target);
    }
    
    @Override
    public boolean isVisible() {
        return false;
    }

}
