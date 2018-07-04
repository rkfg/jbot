package me.rkfg.xmpp.bot.plugins.game.command;

import static me.rkfg.xmpp.bot.plugins.game.misc.Attrs.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import me.rkfg.xmpp.bot.plugins.game.IGameObject;
import me.rkfg.xmpp.bot.plugins.game.IPlayer;
import me.rkfg.xmpp.bot.plugins.game.World;
import me.rkfg.xmpp.bot.plugins.game.event.AttackEvent;
import me.rkfg.xmpp.bot.plugins.game.event.BattleBeginsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.BattleEndsEvent;
import me.rkfg.xmpp.bot.plugins.game.event.StatsEvent;
import me.rkfg.xmpp.bot.plugins.game.misc.Utils;

public class AttackCommand implements ICommandHandler {

    @Override
    public Collection<String> getCommand() {
        return Arrays.asList("атака", "а");
    }

    @Override
    public Optional<String> exec(IPlayer attacker, Stream<String> args) {
        try {
            List<IPlayer> playersList = World.THIS.listPlayers();
            IPlayer defender = getFirstIntegerArg(args).filter(v -> v > 0 && v <= playersList.size()).map(i -> playersList.get(i - 1))
                    .orElseThrow(NumberFormatException::new);
            if (defender == attacker) {
                return Optional.of("нельзя атаковать себя");
            }
            if (!attacker.enqueueEvent(new BattleBeginsEvent(attacker, defender))
                    || !defender.enqueueEvent(new BattleBeginsEvent(attacker, defender))) {
                attacker.log("Не удалось начать бой");
                return Optional.empty();
            }
            attacker.log(String.format("Вы нападаете на %s!", Utils.getPlayerName(defender)));
            defender.log(String.format("%s нападает на вас!", Utils.getPlayerName(attacker)));
            battleTurn(attacker, defender);
            battleTurn(defender, attacker);
            attacker.enqueueEvent(new BattleEndsEvent(attacker, defender));
            defender.enqueueEvent(new BattleEndsEvent(attacker, defender));
            attacker.log("Бой между %s и %s завершён.", Utils.getPlayerName(attacker), Utils.getPlayerName(defender));
            defender.log("Бой между %s и %s завершён.", Utils.getPlayerName(attacker), Utils.getPlayerName(defender));
        } catch (NumberFormatException e) {
            return getHelp();
        }
        return Optional.empty();
    }

    private void battleTurn(IGameObject srcPlayer, IGameObject tgtPlayer) {
        AttackEvent attackEvent = new AttackEvent(srcPlayer, tgtPlayer);
        if (tgtPlayer.enqueueEvent(attackEvent)
                && tgtPlayer.as(PLAYER_OBJ).flatMap(IPlayer::getArmor).map(a -> a.enqueueEvent(attackEvent)).orElse(true)
                && srcPlayer.as(PLAYER_OBJ).flatMap(IPlayer::getWeapon).map(a -> a.enqueueEvent(attackEvent)).orElse(true)
                && srcPlayer.enqueueEvent(attackEvent)) {
            if (attackEvent.isSuccessful()) {
                srcPlayer.log("Атака достигает цели и наносит " + attackEvent.getDamage() + " урона!");
                tgtPlayer.log("Соперник наносит вам " + attackEvent.getDamage() + " урона!");
                final StatsEvent statsEvent = new StatsEvent();
                statsEvent.setSource(srcPlayer);
                tgtPlayer.enqueueEvent(statsEvent.setAttributeChain(HP, -attackEvent.getDamage()));
            } else {
                srcPlayer.log("Вы пытаетесь поразить соперника, но промахиваетесь!");
                tgtPlayer.log("Соперник пытается нанести удар, но промахивается!");
            }
        }
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.of(
                "Атаковать противника из списка игроков по его номеру (требует 5 единиц энергии). Список можно посмотреть командой %гм игроки");
    }

}
