package me.rkfg.xmpp.bot.plugins.game.event;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttribute;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class RenameEvent extends AbstractEvent {

    public static final String TYPE = "rename";
    public static final TypedAttribute<String> NAME = TypedAttribute.of("name");

    public RenameEvent(String newName) {
        super(TYPE);
        setAttribute(NAME, newName);
    }

    @Override
    public void apply() {
        target.as(MUTABLEPLAYER_OBJ).ifPresent(p -> p.setName(getAttribute(NAME).orElse(p.getName())));
        target.log("Персонажа теперь зовут <i>%s</i>.", Utils.getPlayerName(target));
    }

}
