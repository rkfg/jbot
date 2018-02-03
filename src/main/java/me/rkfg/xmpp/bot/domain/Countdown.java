package me.rkfg.xmpp.bot.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.ppsrk.gwt.domain.BasicDomain;

@SuppressWarnings("serial")
@Entity
public class Countdown extends BasicDomain {
    String name;
    @Temporal(TemporalType.TIMESTAMP)
    Date date;
    String creator;
    String room;
    Boolean groupchat;
    Boolean notified;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Boolean getGroupchat() {
        return groupchat;
    }

    public void setGroupchat(Boolean groupchat) {
        this.groupchat = groupchat;
    }

    public Boolean getNotified() {
        return notified;
    }

    public void setNotified(Boolean notified) {
        this.notified = notified;
    }

}
