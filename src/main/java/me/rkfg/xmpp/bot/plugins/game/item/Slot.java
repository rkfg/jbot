package me.rkfg.xmpp.bot.plugins.game.item;

import java.util.Optional;

import me.rkfg.xmpp.bot.plugins.game.misc.TypedAttributeMap;

public class Slot implements IMutableSlot {

    private TypedAttributeMap attrs = new TypedAttributeMap();
    private IItem item;

    public Slot(String description) {
        setDescription(description);
    }

    @Override
    public Optional<IItem> getItem() {
        return Optional.ofNullable(item);
    }

    @Override
    public void setItem(IItem item) {
        this.item = item;
    }

    @Override
    public TypedAttributeMap getAttrs() {
        return attrs;
    }

}
