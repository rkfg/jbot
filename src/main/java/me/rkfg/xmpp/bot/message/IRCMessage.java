package me.rkfg.xmpp.bot.message;

import java.util.Optional;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import me.rkfg.xmpp.bot.IBot;

public class IRCMessage implements BotMessage {

    private Optional<Channel> channel;
    private User user;
    private String message;
    private Event event;

    public IRCMessage(MessageEvent event) {
        this.event = event;
        channel = Optional.ofNullable(event.getChannel());
        user = event.getUser();
        message = event.getMessage();
    }

    public IRCMessage(PrivateMessageEvent event) {
        this.event = event;
        channel = Optional.empty();
        user = event.getUser();
        message = event.getMessage();
    }

    @Override
    public boolean isFromUser() {
        return true;
    }

    @Override
    public String getNick() {
        return user.getNick();
    }

    @Override
    public String getAppeal(String target) {
        return "";
    }

    @Override
    public boolean isFromGroupchat() {
        return channel.isPresent();
    }

    @Override
    public String getFromRoom() {
        return channel.map(Channel::getName).orElse(user.getNick());
    }

    @Override
    public String getFrom() {
        return user.getNick();
    }

    @Override
    public String getBody() {
        return message;
    }

    @Override
    public IBot.Protocol getProtocol() {
        return IBot.Protocol.IRC;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Event getOriginalMessage() {
        return event;
    }

}
