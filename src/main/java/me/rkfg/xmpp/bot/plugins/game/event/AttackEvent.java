package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class AttackEvent extends AbstractEvent {

    public static final String TYPE = "attack";

    public static final TypedAttribute<Boolean> SUCCESSFUL = TypedAttribute.of("successful");

    public AttackEvent(IGameObject source, IGameObject target) {
        super(TYPE, source);
        setTarget(target);
        try {
            IPlayer srcPlayer = source.as(PLAYER_OBJ).orElseThrow(() -> new RuntimeException("Неверный источник атаки"));
            IPlayer tgtPlayer = target.as(PLAYER_OBJ).orElseThrow(() -> new RuntimeException("Неверная цель атаки"));
            final Optional<IWeapon> atkWeapon = srcPlayer.getWeapon();
            final Optional<IWeapon> defWeapon = tgtPlayer.getWeapon();
            final Optional<IArmor> defArmor = tgtPlayer.getArmor();
            final int attack = srcPlayer.getStat(ATK) + atkWeapon.map(IWeapon::getAttack).orElse(0) + Utils.drn();
            setAttribute(ATK, attack);
            final int defence = tgtPlayer.getStat(DEF) + defWeapon.map(IWeapon::getDefence).orElse(0)
                    + defArmor.map(IArmor::getDefence).orElse(0) + Utils.drn();
            setAttribute(DEF, defence);
            final int strength = srcPlayer.getStat(STR) + atkWeapon.map(IWeapon::getStrength).orElse(0) + Utils.drn();
            setAttribute(STR, strength);
            final int protection = tgtPlayer.getStat(PRT) + defArmor.map(IArmor::getProtection).orElse(0) + Utils.drn();
            setAttribute(PRT, protection);
            final int damage = Math.max(strength - protection, 0);
            setAttribute(SUCCESSFUL, attack > defence && damage > 0);
            setAttribute(HP, damage);
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
        }
    }

    public boolean isSuccessful() {
        return getAttribute(SUCCESSFUL).orElse(false);
    }

    public int getDamage() {
        return getAttribute(HP).orElse(0);
    }

}
