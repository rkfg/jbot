package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.IAttachDetachEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public interface IHasEffects extends IContentRepository {
    default void processEffects(TypedAttributeMap content, String effectsDesc) {
        content.put(EFFECTS, Stream.of(effectsDesc.split(",")).map(String::trim).map(effectDesc -> {
            String[] params = effectDesc.split(":");
            Optional<IEffect> effect = World.THIS.getEffectRepository().getObjectById(params[0]);
            if (params.length > 1) {
                effect.ifPresent(e -> {
                    List<String> effectParams = Arrays.asList(Arrays.copyOfRange(params, 1, params.length));
                    if (!effectParams.isEmpty()) {
                        e.setAttribute(EFFECT_PARAMS, effectParams);
                    }
                });
            }
            return effect;
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet()));

    }

    default <T extends IAttachDetachEffect> T attachEffects(T item, TypedAttributeMap content) {
        content.get(EFFECTS).ifPresent(fx -> fx.forEach(item::attachEffect));
        return item;
    }

}
