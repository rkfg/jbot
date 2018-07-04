package me.rkfg.xmpp.bot.plugins.game.repository;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.effect.IAttachDetachEffect;
import me.rkfg.xmpp.bot.plugins.game.effect.IEffect;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public interface IHasEffects extends IContentRepository {

    default void processEffects(TypedAttributeMap content, String effectsDesc) {
        content.put(EFFECTS, Stream.of(effectsDesc.split(",")).map(String::trim).map(effectDesc -> {
            String[] params = effectDesc.split(":");
            Optional<IEffect> effect = World.THIS.getEffectRepository().getObjectById(params[0]);
            if (!effect.isPresent()) {
                LoggerFactory.getLogger(getClass()).warn("Effect {} for item {} not found", params[0], content.get(CONTENT_ID));
            }
            if (params.length > 1) {
                effect.ifPresent(e -> processEffectParams(params, e));
            }
            return effect;
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet()));

    }

    default void processEffectParams(String[] params, IEffect e) {
        List<String> simpleEffectParams = new ArrayList<>();
        Map<String, String> kvEffectParams = new HashMap<>();
        for (int i = 1; i < params.length; ++i) {
            String param = params[i];
            if (param.contains("=")) {
                kvEffectParams.put(param.substring(0, param.indexOf('=')), param.substring(param.indexOf('=') + 1));
            } else {
                simpleEffectParams.add(param);
            }
        }
        if (!simpleEffectParams.isEmpty()) {
            e.setAttribute(EFFECT_PARAMS, simpleEffectParams);
        }
        if (!kvEffectParams.isEmpty()) {
            e.setAttribute(EFFECT_PARAMS_KV, kvEffectParams);
        }
    }

    default <T extends IAttachDetachEffect> T attachEffects(T item, TypedAttributeMap content) {
        content.get(EFFECTS).ifPresent(fx -> fx.forEach(item::attachEffect));
        return item;
    }

}
