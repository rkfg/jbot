package me.rkfg.xmpp.bot.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.ppsrk.gwt.domain.BasicDomain;

@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "blockTimestamp") })
public class KarmaHistory extends BasicDomain {
    @Temporal(TemporalType.TIMESTAMP)
    Date date;
    Long blockTimestamp;
    @ManyToOne
    Karma karma;
    Long change;

    public KarmaHistory() {
    }

    public KarmaHistory(Date date, Long blockTimestamp, Karma karma, Long change) {
        this.date = date;
        this.blockTimestamp = blockTimestamp;
        this.karma = karma;
        this.change = change;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getBlockTimestamp() {
        return blockTimestamp;
    }

    public void setBlockTimestamp(Long blockTimestamp) {
        this.blockTimestamp = blockTimestamp;
    }

    public Karma getKarma() {
        return karma;
    }

    public void setKarma(Karma karma) {
        this.karma = karma;
    }

    public Long getChange() {
        return change;
    }

    public void setChange(Long change) {
        this.change = change;
    }

}
