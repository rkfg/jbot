package me.rkfg.xmpp.bot.plugins.game.misc;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.function.BinaryOperator;

import org.apache.commons.lang3.StringUtils;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;

public class Utils {
    private Utils() {
    }

    private static final String UNKNOWN = "<неизвестный>";
    private static Random rnd = new SecureRandom();
    public static final BinaryOperator<String> pipeReducer = (acc, v) -> acc + " | " + v;
    public static final BinaryOperator<String> commaReducer = (acc, v) -> acc + ", " + v;

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

    public static int dice(String desc) {
        try {
            String[] vals = desc.toLowerCase().split("d", 2);
            int count = Integer.parseInt(vals[0]);
            int faces = Integer.parseInt(vals[1]);
            int sum = 0;
            for (int i = 0; i < count; ++i) {
                sum += rnd.nextInt(faces) + 1;
            }
            return sum;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getPlayerName(IGameObject obj) {
        return obj.as(PLAYER_OBJ).map(IPlayer::getName).orElse(UNKNOWN);
    }

    public static <T> T unboxOptional(Optional<T> opt, T def) {
        return opt.orElse(def);
    }

    public static String unboxString(Optional<String> opt) {
        return unboxOptional(opt, "<неизвестно>");
    }
    
    public static String capitalize(String s) {
        return StringUtils.capitalize(s);
    }
}
