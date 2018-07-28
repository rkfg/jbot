package me.rkfg.xmpp.bot.message;

import java.util.Optional;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;

public class IRCMessage implements BotMessage {

    private Optional<Channel> channel;
    private User user;
    private String message;
    private MessageEvent event;

    public IRCMessage(MessageEvent event) {
        this.event = event;
        channel = Optional.ofNullable(event.getChannel());
        user = event.getUser();
        message = event.getMessage();
    }

    @Override
    public boolean isFromUser() {
        return !channel.isPresent();
    }

    @Override
    public String getNick() {
        return user.getNick();
    }

    @Override
    public String getAppeal(String target) {
        return target + ": ";
    }

    @Override
    public boolean isFromGroupchat() {
        return channel.isPresent();
    }

    @Override
    public String getFromRoom() {
        return channel.map(Channel::getName).orElse(null);
    }

    @Override
    public String getFrom() {
        return user.getLogin();
    }

    @Override
    public String getBody() {
        return message;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.IRC;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Event getOriginalMessage() {
        return event;
    }

}
