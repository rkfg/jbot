package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.effect.AmbushEffect;

public class AmbushCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "засада";
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        player.enqueueToggleEffect(new AmbushEffect());
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Устроить засаду, расходует " + Player.AMBUSH_FATIGUE_COST + " единиц энергии. "
                + "При определённой доле везения, атакующий вас не заметит, и вы сможете атаковать первым, "
                + "а также получите бонус к атаке и защите на время боя. "
                + "Если же противник заметит засаду, вы получите штраф к атаке и защите на время боя.");
    }
}
