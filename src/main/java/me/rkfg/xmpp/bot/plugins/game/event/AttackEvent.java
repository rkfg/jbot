package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class AttackEvent extends AbstractEvent {

    public static final String TYPE = "attack";

    public static final TypedAttribute<Boolean> SUCCESSFUL = TypedAttribute.of("successful");

    public AttackEvent(IGameObject source, IGameObject target) {
        super(TYPE, source);
        setTarget(target);
        try {
            IPlayer srcPlayer = source.asPlayer().orElseThrow(() -> new RuntimeException("Неверный источник атаки"));
            IPlayer tgtPlayer = target.asPlayer().orElseThrow(() -> new RuntimeException("Неверная цель атаки"));
            final int attack = srcPlayer.getStat(IPlayer.ATK) + Utils.drn();
            setAttribute(IPlayer.ATK, attack);
            final int defence = tgtPlayer.getStat(IPlayer.DEF) + Utils.drn();
            setAttribute(IPlayer.DEF, defence);
            final int strength = srcPlayer.getStat(IPlayer.STR) + Utils.drn();
            setAttribute(IPlayer.STR, strength);
            final int protection = tgtPlayer.getStat(IPlayer.PRT) + Utils.drn();
            setAttribute(IPlayer.PRT, protection);
            final int damage = Math.max(strength - protection, 0);
            setAttribute(SUCCESSFUL, attack > defence && damage > 0);
            setAttribute(IPlayer.HP, damage);
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
        }
    }

    public boolean isSuccessful() {
        return getAttribute(SUCCESSFUL).orElse(false);
    }

    public int getDamage() {
        return getAttribute(IPlayer.HP).orElse(0);
    }

}
