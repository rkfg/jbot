package me.rkfg.xmpp.bot.plugins.game.misc;

public class TypedAttribute<T> {
    private String name;

    protected TypedAttribute(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public static <T> TypedAttribute<T> of(String name) {
        return new TypedAttribute<>(name);
    }
}
