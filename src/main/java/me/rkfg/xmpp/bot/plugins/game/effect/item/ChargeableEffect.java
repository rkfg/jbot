package me.rkfg.xmpp.bot.plugins.game.effect.item;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.effect.AbstractEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IBattleEffect;
import me.rkfg.xmpp.bot.plugins.game.event.IEvent;
import me.rkfg.xmpp.bot.plugins.game.event.RechargeEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;

public class ChargeableEffect extends AbstractEffect implements IBattleEffect {

    public static final String TYPE = "chargeable";
    public static final TypedAttribute<Integer> CHARGES = TypedAttribute.of("charges");
    public static final TypedAttribute<Integer> MAXCHARGES = TypedAttribute.of("maxcharges");

    private static final List<String> KEYS = Arrays.asList("atk", "def", "str", "prt");
    private static final List<TypedAttribute<Integer>> EFFECT_ATTRS = Arrays.asList(ATK, DEF, STR, PRT);
    private static final List<String> MESSAGES = Arrays.asList("Способность атаковать", "Способность защищаться", "Сила", "Броня");
    public static final TypedAttribute<String> CHARGETYPE = TypedAttribute.of("chargetype");

    public ChargeableEffect() {
        super(TYPE, "заряжаемое");
    }

    @Override
    public Collection<IEvent> processEvent(IEvent event) {
        if (event.isOfType(RechargeEvent.TYPE)) {
            getAttribute(MAXCHARGES).ifPresent(mc -> {
                boolean charged = isCharged();
                setAttribute(CHARGES, mc);
                if (!charged) { // charged now, change the stats back
                    enqueueStatsEvent();
                }
            });
        }
        return IBattleEffect.super.processEvent(event);
    }

    @Override
    public void onBeforeAttach() {
        getIntParameter(0).ifPresent(ch -> {
            setAttribute(CHARGES, ch);
            setAttribute(MAXCHARGES, ch);
        });
        getParameterByKey("type").ifPresent(t -> setAttribute(CHARGETYPE, t));
        enqueueStatsEvent();
    }

    private void enqueueStatsEvent() {
        StatsEvent statsEvent = new StatsEvent();
        for (int i = 0; i < KEYS.size(); ++i) {
            int idx = i;
            getIntParameterByKey(KEYS.get(i)).map(stat -> isCharged() ? stat : -stat).ifPresent(stat -> {
                target.log("%s %s на %d.", MESSAGES.get(idx), stat > 0 ? "увеличивается при заряде" : "уменьшается от нехватки заряда",
                        stat);
                statsEvent.setAttribute(EFFECT_ATTRS.get(idx), stat);
            });
        }
        target.enqueueEvent(statsEvent);
    }

    public boolean isCharged() {
        return getAttribute(CHARGES).filter(c -> c > 0).isPresent();
    }

    @Override
    public Collection<IEvent> attackSuccess(IEvent event) {
        return target.as(WEAPON_OBJ).map(a -> discharge()).orElseGet(this::noEvent);
    }

    @Override
    public Collection<IEvent> defenceSuccess(IEvent event) {
        return target.as(ARMOR_OBJ).map(a -> discharge()).orElseGet(this::noEvent);
    }

    private Collection<IEvent> discharge() {
        return decAttribute(CHARGES, this::onDischarged).orElseGet(this::noEvent);
    }

    private Collection<IEvent> onDischarged() {
        target.getDescription().ifPresent(d -> target.log("Теперь %s менее эффективно использовать из-за разряда.", d));
        enqueueStatsEvent();
        return noEvent();
    }

    @Override
    public Optional<String> getDescription() {
        return super.getDescription()
                .map(d -> String.format("%s (%s)", d, isCharged() ? String.format("%d зар. осталось", getCharges()) : "разряжено"));
    }

    private Integer getCharges() {
        return getAttribute(CHARGES).orElse(0);
    }

}
