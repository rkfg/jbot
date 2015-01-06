package me.rkfg.xmpp.bot.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import ru.ppsrk.gwt.domain.BasicDomain;

@SuppressWarnings("serial")
@Entity
public class Karma extends BasicDomain {
    String jid;
    Long karma;
    @OneToMany(mappedBy = "karma")
    List<KarmaHistory> history;

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
