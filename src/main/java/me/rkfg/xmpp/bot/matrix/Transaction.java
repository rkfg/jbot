package me.rkfg.xmpp.bot.matrix;

import me.rkfg.xmpp.bot.message.MatrixMessage;

public class Transaction {
    public String txid;
    public MatrixMessage msg;

    public Transaction(String txid, MatrixMessage msg) {
        this.txid = txid;
        this.msg = msg;
    }

}
