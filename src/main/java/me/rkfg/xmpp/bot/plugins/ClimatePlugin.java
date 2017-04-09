package me.rkfg.xmpp.bot.plugins;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.packet.Message;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public class ClimatePlugin extends CommandPlugin {

    private static final int SOCKET_TIMEOUT = 5000;

    private static final InetSocketAddress CLASSIFICATION_SERVER_ADDRESS = new InetSocketAddress("127.0.0.1", 6000);

    private DatagramSocket socket;

    private static final int CLASSES_NUMBER = 4;

    private static final float THRESHOLD = 0.3f;

    private static final long TIMEOUT = TimeUnit.HOURS.toMillis(3);
    private byte[] rbuf = new byte[CLASSES_NUMBER * 4]; // 4 bytes per float
    private DatagramPacket rdp = new DatagramPacket(rbuf, rbuf.length);
    private ByteBuffer receiveBuffer = ByteBuffer.wrap(rbuf);
    private int[] climate = new int[CLASSES_NUMBER];
    private int total = 0;

    private class ClimateRecord {
        int score;
        long timestamp;
        int personId;

        public ClimateRecord(int score, long timestamp, int personId) {
            this.score = score;
            this.timestamp = timestamp;
            this.personId = personId;
        }

    }

    LinkedList<ClimateRecord> records = new LinkedList<>();

    private enum Person {
        SKFG("аскафажность"), TXB("тиэксбишность"), RKFG("шпиканутость"), STEAMLOCK("стимлочность");

        private String category;

        private Person(String category) {
            this.category = category;
        }

        public String getCategory() {
            return category;
        }
    }

    @Override
    public void init() {
        super.init();
        receiveBuffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (SocketException e) {
            log.warn("Classification socket creation failed: ", e);
        }
    }

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(.+)", Pattern.DOTALL);
    }

    @Override
    public String process(Message message, Matcher matcher) {
        if (!isMessageFromUser(message)) {
            // don't store system messages
            return null;
        }
        cleanup();
        Matcher cmdMatcher = super.getPattern().matcher(message.getBody());
        if (cmdMatcher.find()) {
            return super.process(message, cmdMatcher);
        }
        final String text = matcher.group(1).replace('\n', ' ');
        try {
            byte[] buf = text.getBytes(StandardCharsets.UTF_8);
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            dp.setSocketAddress(CLASSIFICATION_SERVER_ADDRESS);
            socket.send(dp);
            socket.receive(rdp);
            receiveBuffer.rewind();
            int i = 0;
            while (receiveBuffer.hasRemaining()) {
                float f = receiveBuffer.getFloat();
                log.debug("Received weight for {}: {}", i, f);
                if (f >= THRESHOLD) {
                    int score = (int) ((f - THRESHOLD) * 100);
                    climate[i] += score;
                    total += score;
                    records.add(new ClimateRecord(score, System.currentTimeMillis(), i));
                }
                ++i;
            }
        } catch (SocketTimeoutException e) {
            log.warn("Classification backend unavailable.");
        } catch (IOException e) {
            log.warn("Exception while processing a message: ", e);
        }
        return null;
    }

    private void cleanup() {
        boolean removed = true;
        while (removed) {
            if (records.isEmpty()) {
                return;
            }
            ClimateRecord first = records.getFirst();
            if (System.currentTimeMillis() - first.timestamp > TIMEOUT) {
                climate[first.personId] -= first.score;
                total -= first.score;
                records.removeFirst();
            } else {
                removed = false;
            }
        }
    }

    @Override
    public String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        StringBuilder sb = new StringBuilder("Климат в чате:\n");
        for (int i = 0; i < climate.length; ++i) {
            sb.append(Person.values()[i].getCategory()).append(": ").append(climate[i]).append(" [").append(climate[i] * 100 / (total > 0 ? total : 1))
                    .append("%]\n");
        }
        return sb.toString();
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("cl", "кл");
    }

    @Override
    public String getManual() {
        return "узнать климат в чате.\nФормат: " + PREFIX + "cl";
    }
    
}
