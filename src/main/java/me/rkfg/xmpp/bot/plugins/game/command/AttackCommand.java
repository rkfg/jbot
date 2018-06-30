package me.rkfg.xmpp.bot.plugins.game.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.event.BattleBeginsEvent;

public class AttackCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("атака", "а");
    }

    @Override
    public Optional<String> exec(IPlayer player, Stream<String> args) {
        try {
            List<IPlayer> playersList = World.THIS.listPlayers();
            IPlayer target = args.findFirst().map(Integer::valueOf).filter(v -> v > 0 && v <= playersList.size())
                    .map(i -> playersList.get(i - 1)).orElseThrow(NumberFormatException::new);
            if (target == player) {
                return Optional.of("нельзя атаковать себя");
            }
            if (!player.enqueueEvent(new BattleBeginsEvent(player)) || !target.enqueueEvent(new BattleBeginsEvent(player))) {
                return Optional.of("Не удалось начать бой");
            }
        } catch (NumberFormatException e) {
            return getHelp();
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of("Атаковать противника из списка игроков по его номеру. Список можно посмотреть командой %гм игроки");
    }

}
