/**
 * Contender in Faggot of the Day game.
 * Copyright (C) 2017 Kona-chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.rkfg.xmpp.bot.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import ru.ppsrk.gwt.domain.BasicDomain;

/**
 * Contender in Faggot of the Day game.
 *
 * @author Kona-chan
 */
@SuppressWarnings("serial")
@Entity
public final class Contender extends BasicDomain {

    private String nick;
    private String jid;
    private String room;
    private Boolean loggedInYesterday;
    @OneToMany(mappedBy = "contender")
    private List<Winning> winnings;

    public Contender() {}

    public Contender(String nick, String jid, String room, Boolean loggedInYesterday) {
        this.nick = nick;
        this.jid = jid;
        this.room = room;
        this.loggedInYesterday = loggedInYesterday;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Contender [nick=")
                .append(nick)
                .append(", jid=")
                .append(jid)
                .append(", room=")
                .append(room)
                .append(", loggedInYesterday=")
                .append(loggedInYesterday)
                .append("]");
        return builder.toString();
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Boolean getLoggedInYesterday() {
        return loggedInYesterday;
    }

    public void setLoggedInYesterday(Boolean loggedInYesterday) {
        this.loggedInYesterday = loggedInYesterday;
    }

}
