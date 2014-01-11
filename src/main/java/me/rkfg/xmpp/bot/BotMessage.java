package me.rkfg.xmpp.bot;

public class BotMessage {
    private ChatAdapter chat;
    private String message;

    public BotMessage() {
    }

    public BotMessage(ChatAdapter chat, String message) {
        super();
        this.chat = chat;
        this.message = message;
    }

    public ChatAdapter getChat() {
        return chat;
    }

    public void setChat(ChatAdapter chat) {
        this.chat = chat;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
