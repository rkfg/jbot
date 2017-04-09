package me.rkfg.xmpp.bot.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(columnList = "author"), @Index(columnList = "name"), @Index(columnList = "opinion") })
public class Opinion {
    @Id
    @GeneratedValue
    Long id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
