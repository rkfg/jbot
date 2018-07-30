package me.rkfg.xmpp.bot.irc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.pircbotx.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageParser {

    private static final Map<String, String> REPLACES = new HashMap<>();
    private StringBuilder sb = new StringBuilder();
    private List<String> state = new LinkedList<>();
    private List<String> result = new LinkedList<>();
    private String message = null;
    private int i = 0;
    private int maxLength;
    private boolean codeFound = false;
    private boolean needRestore = false;
    private int lastSpaceIdx = 0;
    private Logger log = LoggerFactory.getLogger(getClass());

    public static void init() {
        REPLACES.put("b", Colors.BOLD);
        REPLACES.put("i", Colors.ITALICS);
        REPLACES.put("u", Colors.UNDERLINE);
        REPLACES.put("white", Colors.WHITE);
        REPLACES.put("black", Colors.BLACK);
        REPLACES.put("red", Colors.RED);
        REPLACES.put("brown", Colors.BROWN);
        REPLACES.put("magenta", Colors.MAGENTA);
        REPLACES.put("purple", Colors.PURPLE);
        REPLACES.put("olive", Colors.OLIVE);
        REPLACES.put("yellow", Colors.YELLOW);
        REPLACES.put("green", Colors.GREEN);
        REPLACES.put("dgreen", Colors.DARK_GREEN);
        REPLACES.put("gray", Colors.LIGHT_GRAY);
        REPLACES.put("dgray", Colors.DARK_GRAY);
        REPLACES.put("blue", Colors.BLUE);
        REPLACES.put("dblue", Colors.DARK_BLUE);
        REPLACES.put("teal", Colors.TEAL);
        REPLACES.put("cyan", Colors.CYAN);
    }

    public MessageParser(String message, int maxLength) {
        this.maxLength = maxLength;
        this.message = message.replace('\n', ' ').trim();
    }

    public List<String> process() {
        int lastCharIdx = 0;
        while (i < message.length()) {
            final char ch = message.charAt(i);
            codeFound = false;
            if (ch == '<') {
                for (Entry<String, String> r : REPLACES.entrySet()) {
                    parseOpenTag(r);
                    parseOpenBGTag(r);
                    parseCloseTag(r);
                    parseCloseBGTag(r);
                    if (codeFound) {
                        break;
                    }
                }
            }
            if (!codeFound) {
                if (needRestore) {
                    restoreState(true);
                    needRestore = false;
                }
                sb.append(ch);
                lastCharIdx = sb.length();
                if (sb.length() > maxLength) {
                    int skip = 1;
                    if (lastSpaceIdx == 0) {
                        lastSpaceIdx = lastCharIdx - 1;
                        skip = 0;
                    }
                    log.debug("sblen: {}, maxlen: {}, lastSpace: {}", sb.length(), maxLength, lastSpaceIdx);
                    result.add(sb.substring(0, lastSpaceIdx));
                    String tail = "";
                    tail = sb.substring(lastSpaceIdx + skip);
                    sb = new StringBuilder();
                    restoreState(false);
                    // don't duplicate formatting if it was moved to the new line
                    if (tail.startsWith(sb.toString())) {
                        sb.setLength(0);
                    }
                    sb.append(tail);
                    lastSpaceIdx = 0;
                }
                if (ch == ' ') {
                    lastSpaceIdx = sb.length() - 1;
                }
                ++i;
            }
        }
        result.add(sb.toString());
        return result;
    }

    public void parseCloseBGTag(Entry<String, String> r) {
        if (message.length() > i + r.getKey().length() + 3 && message.substring(i + 1).toLowerCase().startsWith("/_" + r.getKey() + ">")) {
            i += r.getKey().length() + 4;
            removeState(r.getValue(), false);
            needRestore = true;
            codeFound = true;
        }
    }

    public void parseCloseTag(Entry<String, String> r) {
        if (message.length() > i + r.getKey().length() + 2 && message.substring(i + 1).toLowerCase().startsWith("/" + r.getKey() + ">")) {
            i += r.getKey().length() + 3;
            removeState(r.getValue(), true);
            needRestore = true;
            codeFound = true;
        }
    }

    public void parseOpenBGTag(Entry<String, String> r) {
        if (message.length() > i + r.getKey().length() + 2 && message.substring(i + 1).toLowerCase().startsWith("_" + r.getKey() + ">")) {
            i += r.getKey().length() + 3;
            sb.append("\u0003" + state.stream().filter(s -> s.startsWith("F_")).map(s -> s.substring(2)).findFirst().orElse("99") + ","
                    + r.getValue().substring(1));
            setState(r.getValue(), false);
            codeFound = true;
        }
    }

    public void parseOpenTag(Entry<String, String> r) {
        if (message.length() > i + r.getKey().length() + 1 && message.substring(i + 1).toLowerCase().startsWith(r.getKey() + ">")) {
            i += r.getKey().length() + 2;
            sb.append(r.getValue());
            setState(r.getValue(), true);
            codeFound = true;
        }
    }

    private void removeState(String value, boolean foreground) {
        if (Colors.BOLD.equals(value) || Colors.ITALICS.equals(value) || Colors.UNDERLINE.equals(value)) {
            state.remove(value);
        } else {
            state.remove((foreground ? "F_" : "B_") + value.substring(1));
        }
    }

    private void restoreState(boolean reset) {
        if (reset) {
            sb.append(Colors.NORMAL);
        }
        Optional<String> fgColor = Optional.empty();
        Optional<String> bgColor = Optional.empty();
        for (String s : state) {
            if (!fgColor.isPresent() && s.startsWith("F_")) {
                fgColor = Optional.of(s.substring(2));
            } else if (!bgColor.isPresent() && s.startsWith("B_")) {
                bgColor = Optional.of(s.substring(2));
            } else {
                sb.append(s);
            }
        }
        if (fgColor.isPresent() || bgColor.isPresent()) {
            sb.append("\u0003");
            sb.append(fgColor.orElse("99"));
            bgColor.ifPresent(bgc -> sb.append("," + bgc));
        }
    }

    private void setState(String value, boolean foreground) {
        if (Colors.BOLD.equals(value) || Colors.ITALICS.equals(value) || Colors.UNDERLINE.equals(value)) {
            state.add(0, value);
        } else {
            state.add(0, (foreground ? "F_" : "B_") + value.substring(1));
        }
    }

}
