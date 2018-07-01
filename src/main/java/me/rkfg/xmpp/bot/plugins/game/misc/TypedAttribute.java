package me.rkfg.xmpp.bot.plugins.game.misc;

public class TypedAttribute<T> { // NOSONAR
    private String name;
    private String accusativeName;

    protected TypedAttribute(String name) {
        this.name = name;
        this.accusativeName = name;
    }

    protected TypedAttribute(String name, String accusativeName) {
        this.name = name;
        this.accusativeName = accusativeName;
    }

    public String getName() {
        return name;
    }

    public String getAccusativeName() {
        return accusativeName;
    }

    public static <T> TypedAttribute<T> of(String name) {
        return new TypedAttribute<>(name);
    }

    public static <T> TypedAttribute<T> of(String name, String accusativeName) {
        return new TypedAttribute<>(name, accusativeName);
    }
}
