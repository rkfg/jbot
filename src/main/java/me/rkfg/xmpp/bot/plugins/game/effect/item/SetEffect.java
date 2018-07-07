package me.rkfg.xmpp.bot.plugins.game.effect.item;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;

public class SetEffect extends AbstractEffect implements ISetEffect {

    public static final String TYPE = "set";

    public SetEffect() {
        super(TYPE, "меняет статы в тандеме с другим предметом");
    }

    @Override
    public void onAfterAttach() {
        getParameter(0).ifPresent(this::initSet);
    }

    @Override
    public boolean isVisible() {
        return false;
    }

}
