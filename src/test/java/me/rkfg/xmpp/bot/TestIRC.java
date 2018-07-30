package me.rkfg.xmpp.bot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import me.rkfg.xmpp.bot.irc.MessageParser;

public class TestIRC {

    private int maxLen = 10;

    @BeforeAll
    public static void init() {
        MessageParser.init();
    }

    @Test
    public void testLongSplit() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; ++i) {
            sb.append('A');
        }
        assertEquals(20, sb.length());
        List<String> list = new MessageParser(sb.toString(), maxLen).process();
        assertEquals(2, list.size());
        assertEquals(10, list.get(0).length());
        assertEquals(10, list.get(1).length());
    }

    @Test
    public void testWordsSplit() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; ++i) {
            sb.append("AAA ");
        }
        assertEquals(20, sb.length());
        List<String> list = new MessageParser(sb.toString(), maxLen).process();
        assertEquals(3, list.size());
        assertEquals(7, list.get(0).length());
        assertEquals(7, list.get(1).length());
        assertEquals(3, list.get(2).length());
    }

    @Test
    public void testColorsSplit() {
        String msg = "<teal><_brown>AAA AAA AAA AAA AAA</_brown></teal>";
        List<String> list = new MessageParser(msg, 15).process();
        assertEquals(3, list.size());
        assertEquals("\u000310\u000310,05AAA", list.get(0));
        assertEquals("\u000310,05AAA AAA", list.get(1));
        assertEquals("\u000310,05AAA AAA", list.get(2));
    }

    @Test
    public void testRestore() {
        String msg = "<_brown>AAA <teal>AAA</teal> AAA AAA AAA</_brown>";
        List<String> list = new MessageParser(msg, 20).process();
        assertEquals(3, list.size());
        assertEquals("\u000399,05AAA", list.get(0));
        assertEquals("\u000399,05\u000310AAA\u000f\u000399,05", list.get(1));
        assertEquals("\u000399,05AAA AAA AAA", list.get(2));
    }

    @Test
    public void testDecors() {
        String msg = "<b>AAA</b> <i>AAA</i> <u>AAA</u> <b>AAA</b> <i>AAA</i> <u>AAA</u>";
        List<String> list = new MessageParser(msg, 20).process();
        assertEquals(2, list.size());
        assertEquals("\u0002AAA\u000f \u001dAAA\u000f \u001fAAA\u000f", list.get(0));
        assertEquals("\u0002AAA\u000f \u001dAAA\u000f \u001fAAA", list.get(1));
    }
}
