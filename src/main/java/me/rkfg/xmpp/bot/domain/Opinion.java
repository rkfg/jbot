package me.rkfg.xmpp.bot.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Index;

@Entity
public class Opinion {
    @Id
    @GeneratedValue
    Long id;
    @Index(name = "author_index")
    String author;
    @Index(name = "name_index")
    String name;
    @Index(name = "opinion_index")
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
