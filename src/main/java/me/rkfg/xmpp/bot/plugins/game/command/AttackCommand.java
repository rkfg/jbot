package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.event.BattleEvent;

public class AttackCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("атака");
    }

    @Override
    public Optional<String> exec(IPlayer attacker, Stream<String> args) {
        try {
            List<IPlayer> playersList = World.THIS.listPlayers();
            IPlayer defender = getFirstIntegerArg(args).filter(v -> v > 0 && v <= playersList.size()).map(i -> playersList.get(i - 1))
                    .orElseThrow(NumberFormatException::new);
            if (defender == attacker) {
                return Optional.of("нельзя атаковать себя.");
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
        return Optional.of(
                "Атаковать противника из списка игроков по его номеру (требует 5 единиц энергии). Список можно посмотреть командой %гм игроки");
    }

}
