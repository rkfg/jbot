package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.item.IArmor;
import me.rkfg.xmpp.bot.plugins.game.item.IWeapon;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class BattleAttackEvent extends AbstractEvent {

    public static final String TYPE = "battleattack";

    public static final TypedAttribute<Boolean> SUCCESSFUL = TypedAttribute.of("successful");

    public BattleAttackEvent(IGameObject source, IGameObject target) {
        super(TYPE);
        setSource(source);
        setTarget(target);
        try {
            IPlayer srcPlayer = source.as(PLAYER_OBJ).orElseThrow(() -> new RuntimeException("Неверный источник атаки"));
            IPlayer tgtPlayer = target.as(PLAYER_OBJ).orElseThrow(() -> new RuntimeException("Неверная цель атаки"));
            final Optional<IWeapon> atkWeapon = srcPlayer.getWeapon();
            final Optional<IWeapon> defWeapon = tgtPlayer.getWeapon();
            final Optional<IArmor> defArmor = tgtPlayer.getArmor();
            Integer srcAtk = srcPlayer.getStat(ATK);
            Integer srcWeaponAtk = atkWeapon.map(IWeapon::getAttack).orElse(0);
            int srcDRN = Utils.drn();
            final int attack = srcAtk + srcWeaponAtk + srcDRN;
            setAttribute(ATK, attack);
            Integer tgtDef = tgtPlayer.getStat(DEF);
            Integer tgtWeaponDef = defWeapon.map(IWeapon::getDefence).orElse(0);
            Integer tgtArmorDef = defArmor.map(IArmor::getDefence).orElse(0);
            int tgtDRN = Utils.drn();
            final int defence = tgtDef + tgtWeaponDef + tgtArmorDef + tgtDRN;
            setAttribute(DEF, defence);
            Integer srcStr = srcPlayer.getStat(STR);
            Integer srcWeaponStr = atkWeapon.map(IWeapon::getStrength).orElse(0);
            int strDRN = Utils.drn();
            final int strength = srcStr + srcWeaponStr + strDRN;
            setAttribute(STR, strength);
            Integer tgtPrt = tgtPlayer.getStat(PRT);
            Integer tgtArmorPrt = defArmor.map(IArmor::getProtection).orElse(0);
            int prtDRN = Utils.drn();
            final int protection = tgtPrt + tgtArmorPrt + prtDRN;
            setAttribute(PRT, protection);
            final int damage = Math.max(strength - protection, 0);
            setAttribute(SUCCESSFUL, attack > defence && damage > 0);
            setAttribute(HP, damage);
            log.debug("Battle: {} ATK + {} WPNATK + {} DRN == {} vs {} DEF + {} WPNDEF + {} ARMDEF + {} DRN == {}", srcAtk, srcWeaponAtk,
                    srcDRN, attack, tgtDef, tgtWeaponDef, tgtArmorDef, tgtDRN, defence);
            log.debug("Battle success: {}, {} STR + {} WPNSTR + {} DRN == {} vs {} PRT + {} ARMPRT + {} DRN == {}, HP == {}",
                    getAttribute(SUCCESSFUL).orElse(false), srcStr, srcWeaponStr, strDRN, strength, tgtPrt, tgtArmorPrt, prtDRN, protection,
                    damage);
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
