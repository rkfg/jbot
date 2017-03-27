/**
 * Winning in Faggot of the Day game.
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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import ru.ppsrk.gwt.domain.BasicDomain;

/**
 * Winning in Faggot of the Day game.
 *
 * @author Kona-chan
 */
@SuppressWarnings("serial")
@Entity
public final class Winning extends BasicDomain {

    private Date date;
    @ManyToOne
    private Contender contender;

    public Winning() {}

    public Winning(Date date, Contender contender) {
        super();
        this.date = date;
        this.contender = contender;
    }

    @Override
    public String toString() {
        return "Winning [date=" + date + ", contender=" + contender + "]";
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Contender getContender() {
        return contender;
    }

    public void setContender(Contender contender) {
        this.contender = contender;
    }

}
