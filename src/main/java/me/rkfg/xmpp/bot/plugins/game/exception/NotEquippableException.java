package me.rkfg.xmpp.bot.plugins.game.exception;

@SuppressWarnings("serial")
public class NotEquippableException extends GameException {
    
    public NotEquippableException() {
        super("невозможно надеть этот предмет");
    }
    
    public NotEquippableException(String message) {
        super(message);
    }
}
