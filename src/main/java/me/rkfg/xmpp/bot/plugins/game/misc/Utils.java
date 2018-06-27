package me.rkfg.xmpp.bot.plugins.game.misc;

import java.security.SecureRandom;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class Utils {
    private Utils() {
    }

    private static final String UNKNOWN = "<неизвестный>";
    private static SecureRandom rnd = new SecureRandom();

    public static int drn() {
        int sum = 0;
        for (int i = 0; i < 2; ++i) {
            int roll = 0;
            while ((roll = rnd.nextInt(6) + 1) == 6) {
                sum += 5;
            }
            sum += roll;
        }
        return sum;
    }
    
    public static String getPlayerName(IGameObject obj) {
        return obj.asPlayer().map(IPlayer::getName).orElse(UNKNOWN);
    }
}
