package me.rkfg.xmpp.bot.plugins.game.exception;

@SuppressWarnings("serial")
public class GameException extends RuntimeException {
    
    public GameException() {
    }

    public GameException(String message) {
        super(message);
    }
}
