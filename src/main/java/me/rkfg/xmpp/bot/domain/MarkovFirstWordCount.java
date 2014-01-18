package me.rkfg.xmpp.bot.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.Index;

@Entity
public class MarkovFirstWordCount {
    @Id
    @GeneratedValue
    Long id;
    @Index(name = "word_index")
    String word;
    Long count;

    public MarkovFirstWordCount() {
    }

    public MarkovFirstWordCount(String word) {
        super();
        this.word = word;
        count = 0L;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public void incCount() {
        count++;
    }
}
