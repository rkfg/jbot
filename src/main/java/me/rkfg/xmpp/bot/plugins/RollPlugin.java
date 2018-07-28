/*
 * Plugin for jbot that generates random numbers and rolls the dice.
 * Copyright (C) 2017 Kona-chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import me.rkfg.xmpp.bot.message.BotMessage;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

/**
 * Generates random numbers in a given interval and rolls the dice.
 *
 * @author Kona-chan
 * @version 0.1.0
 */
public final class RollPlugin extends CommandPlugin {

    private static final List<String> COMMANDS = Arrays.asList("roll", "ролл", "r", "р");

    private static final Pattern ROLL_NUMBER = Pattern.compile("^([1-9][0-9]{0,5})$");
    private static final Pattern ROLL_DICE = Pattern.compile("^([1-9][0-9]{0,5})d([1-9][0-9]{0,5})$");

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_DICE = 10;
    private static final int MAX_SIDES = 120;

    private static final String[] FUNNY_ERRORS = new String[] { "\uD83C\uDF1D", "чух.", "пук.", "вуф.", "хрюк.", "среньк.", "тупица.",
            "RTFM.", "ты понимаешь, что ты нулевой?", "do that again and I will kill you.", };

    @Override
    public List<String> getCommand() {
        return COMMANDS;
    }

    @Override
    public String getManual() {
        final String commands = COMMANDS.stream().collect(Collectors.joining("|"));
        final String command = PREFIX + COMMANDS.get(0);

        return "сгенерировать случайное число в диапазоне от 1 до " + Integer.toString(DEFAULT_LIMIT) + ".\n" + "Формат: <" + commands
                + ">\n" + "Пример: " + command + "\n" + "\n"
                + "Сгенерировать случайное число в диапазоне от 1 до заданной верхней границы.\n"
                + "Максимальная верхняя граница: 999999.\n" + "Формат: <" + commands + "> <верхняя граница>\n" + "Пример: " + command
                + " 20\n" + "\n" + "Бросить заданное количество костей с заданным числом граней.\n" + "Максимальное количество костей: "
                + Integer.toString(MAX_DICE) + ".\n" + "Максимальное число граней у кости: " + Integer.toString(MAX_SIDES) + ".\n"
                + "Формат: <" + commands + "> <количество костей>d<число граней>\n" + "Пример: " + command + " 5d6\n";
    }

    @Override
    public String processCommand(BotMessage message, Matcher matcher) throws LogicException, ClientAuthException {
        final String command = matcher.group(COMMAND_GROUP);
        if (command == null) {
            return rollNumber(DEFAULT_LIMIT);
        }

        Matcher m;

        m = ROLL_NUMBER.matcher(command);
        if (m.find()) {
            final int limit = Integer.parseInt(m.group(1));
            return rollNumber(limit);
        }

        m = ROLL_DICE.matcher(command);
        if (m.find()) {
            final int count = Integer.parseInt(m.group(1));
            final int sides = Integer.parseInt(m.group(2));
            return rollDice(count, sides);
        }

        return randomError();
    }

    private String rollNumber(int limit) {
        int number = ThreadLocalRandom.current().nextInt(1, limit + 1);
        return Integer.toString(number) + ".";
    }

    private String rollDice(int count, int sides) {
        if (count > MAX_DICE || sides > MAX_SIDES) {
            return randomError();
        }

        return IntStream.range(0, count).map(i -> ThreadLocalRandom.current().nextInt(1, sides + 1)).mapToObj(Integer::toString)
                .collect(Collectors.joining(", ")) + ".";
    }

    private String randomError() {
        final int index = ThreadLocalRandom.current().nextInt(FUNNY_ERRORS.length);
        return FUNNY_ERRORS[index];
    }

}
