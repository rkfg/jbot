package me.rkfg.xmpp.bot.plugins.game.command;

import static java.util.Arrays.*;
import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.GamePlugin;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.effect.ExpiringBonusPointsEffect;

public class SpendPointsCommand implements ICommandHandler {

    private static final List<String> STAT_CHARS = asList("д", "э", "а", "з", "с", "б", "у");

    @Override
    public Collection<String> getCommand() {
        return asList("бонус");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        Integer points = player.getAttribute(BONUS_POINTS).orElse(0);
        if (points < 1) {
            return Optional.of("у вас нет бонусных очков для распределения");
        }
        String stats = args.map(String::toLowerCase).reduce((a, s) -> a + s).orElse("");
        if (stats.isEmpty()) {
            return Optional.of(String.format("имеется бонусных очков: %d.", points));
        }
        if (stats.length() > points) {
            return Optional.of(String.format("недостаточно бонусных очков для распределения (есть только %d).", points));
        }
        int used = 0;
        for (int i = 0; i < stats.length(); ++i) {
            int statNum = STAT_CHARS.indexOf(stats.substring(i, i + 1));
            if (statNum >= 0) {
                player.changeAttribute(STATS.get(statNum), 1);
                ++used;
            }
        }
        player.log("Распределено %d/%d очков. Осталось %d.", used, points, points - used);
        player.changeAttribute(BONUS_POINTS, -used);
        int fused = used;
        player.getEffect(ExpiringBonusPointsEffect.TYPE)
                .ifPresent(e -> e.changeAttribute(ExpiringBonusPointsEffect.EXPIRING_POINTS, -fused));
        player.dumpStats();
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional
                .of("Распределить очки по статам. Без аргумента покажет количество имеющихся очков. Используйте столько букв подряд, "
                        + "сколько очков желаете потратить на стату, отдельные статы можно разделять пробелами по желанию. "
                        + "Буквы стат: [а]така, [з]ащита, [с]ила, [б]роня, [у]дача, [э]нергия, з[д]оровье. " + "Например, '" + GamePlugin.CMD
                        + "бонус аа з сс' добавит 2 атаки, 1 защиту и 2 силы.");
    }

}
