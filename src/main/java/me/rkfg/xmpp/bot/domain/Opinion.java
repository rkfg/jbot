package me.rkfg.xmpp.bot.domain;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import ru.ppsrk.gwt.dto.BasicDTO;

@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "author"), @Index(columnList = "name"), @Index(columnList = "opinion") })
public class Opinion extends BasicDTO {
    String author;
    String name;
    String opinion;

    public Opinion() {
    }

    public Opinion(String author, String name, String opinion) {
        super();
        this.author = author;
        this.name = name;
        this.opinion = opinion;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

}
