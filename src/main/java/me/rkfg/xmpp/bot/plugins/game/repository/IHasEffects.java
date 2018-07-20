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
            TypedAttributeMap result = new TypedAttributeMap();
            String[] params = effectDesc.split(":");
            String type = params[0];
            result.put(CONTENT_ID, type);
            if (params.length > 1) {
                processEffectParams(params, result);
            }
            return result;
        }).collect(Collectors.toSet()));

    }

    default void processEffectParams(String[] params, TypedAttributeMap result) {
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
            result.put(EFFECT_PARAMS, simpleEffectParams);
        }
        if (!kvEffectParams.isEmpty()) {
            result.put(EFFECT_PARAMS_KV, kvEffectParams);
        }
    }

    default Optional<IEffect> instantiateEffect(TypedAttributeMap content) {
        return content.get(CONTENT_ID).flatMap(type -> {
            Optional<IEffect> effect = World.THIS.getEffectRepository().getObjectById(type);
            effect.ifPresent(eff -> {
                content.get(EFFECT_PARAMS).ifPresent(p -> eff.setAttribute(EFFECT_PARAMS, p));
                content.get(EFFECT_PARAMS_KV).ifPresent(p -> eff.setAttribute(EFFECT_PARAMS_KV, p));
            });
            if (!effect.isPresent()) {
                LoggerFactory.getLogger(getClass()).warn("Effect {} for item {} not found", type, content.getDef(CONTENT_ID, "<noid>"));
            }
            return effect;
        });
    }

    default <T extends IAttachDetachEffect> T attachEffects(T item, TypedAttributeMap content) {
        content.get(EFFECTS).ifPresent(fx -> fx.stream().map(this::instantiateEffect).forEach(e -> e.ifPresent(item::attachEffect)));
        return item;
    }

}
