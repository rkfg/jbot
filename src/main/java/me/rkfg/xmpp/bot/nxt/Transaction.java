package me.rkfg.xmpp.bot.nxt;

public class Transaction {
    String txid;
    Long height;
    Long amount;

    public Transaction(String txid, Long height, Long amount) {
        super();
        this.txid = txid;
        this.height = height;
        this.amount = amount;
    }

    public String getTxid() {
        return txid;
    }

    public Long getTimestamp() {
        return height;
    }

    public Long getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("txid=%s, height=%d, amount=%d", txid, height, amount);
    }

}
