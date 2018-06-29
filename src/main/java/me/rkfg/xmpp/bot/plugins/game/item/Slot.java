package me.rkfg.xmpp.bot.plugins.game.item;

import java.util.Optional;

public class Slot implements IMutableSlot {

    private String description;
    private IItem item;

    public Slot(String description) {
        this.description = description;
    }
    
    @Override
    public Optional<String> getDescription() {
        return Optional.of(description);
    }

    @Override
    public Optional<IItem> getItem() {
        return Optional.ofNullable(item);
    }

    @Override
    public void setItem(IItem item) {
        this.item = item;
    }
    
}
