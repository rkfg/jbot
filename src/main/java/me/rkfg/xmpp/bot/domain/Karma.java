package me.rkfg.xmpp.bot.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import ru.ppsrk.gwt.domain.BasicDomain;

@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "jid") })
public class Karma extends BasicDomain {
    String jid;
    Long karma;
    @OneToMany(mappedBy = "karma")
    List<KarmaHistory> history;

    public Karma() {
    }

    public Karma(String jid) {
        this.jid = jid;
        karma = 0L;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public Long getKarma() {
        return karma;
    }

    public void setKarma(Long karma) {
        this.karma = karma;
    }

}
