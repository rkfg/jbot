package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.GamePlugin;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.Player;
import me.rkfg.xmpp.bot.plugins.game.event.BattleEvent;

public class AttackCommand extends AbstractCommand {

    @Override
    public String getCommand() {
        return "атака";
    }

    @Override
    public Optional<String> exec(IPlayer attacker, Stream<String> args) {
        try {
            IPlayer defender = getPlayer(args);
            if (defender == attacker) {
                return Optional.of("Нельзя атаковать себя.");
            }
            if (!defender.isAlive()) {
                return Optional.of(defender.getName() + " мёртв.");
            }
            attacker.enqueueEvent(new BattleEvent(attacker, defender));
        } catch (NumberFormatException e) {
            return getHelp();
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Атаковать противника из списка игроков по его номеру (требует " + Player.BATTLE_FATIGUE_COST
                + " единиц энергии). Список можно посмотреть командой " + GamePlugin.CMD + "игроки");
    }

}
