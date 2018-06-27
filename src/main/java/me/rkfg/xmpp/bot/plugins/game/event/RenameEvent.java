package me.rkfg.xmpp.bot.plugins.game.event;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class RenameEvent extends AbstractEvent {

    public static final String TYPE = "rename";
    public static final TypedAttribute<String> NAME = TypedAttribute.of("name");

    public RenameEvent(IGameObject source, String newName) {
        super(TYPE, source);
        setAttribute(NAME, newName);
    }

    @Override
    public void apply() {
        target.asMutablePlayer().ifPresent(p -> p.setName(getAttribute(NAME).orElse(p.getName())));
        target.log("Персонажа теперь зовут " + Utils.getPlayerName(target));
    }

}
