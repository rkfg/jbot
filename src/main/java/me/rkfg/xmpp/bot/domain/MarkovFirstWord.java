package me.rkfg.xmpp.bot.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Index;

@Entity
public class MarkovFirstWord {
    @Id
    @GeneratedValue
    Long id;
    @ManyToOne
    MarkovFirstWordCount word;
    @Index(name = "number_index")
    Long number;
    @OneToOne
    Markov markov;

    public MarkovFirstWord() {
    }

    public MarkovFirstWord(MarkovFirstWordCount word, Long number, Markov markov) {
        super();
        this.word = word;
        this.number = number;
        this.markov = markov;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MarkovFirstWordCount getWord() {
        return word;
    }

    public void setWord(MarkovFirstWordCount word) {
        this.word = word;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Markov getMarkov() {
        return markov;
    }

    public void setMarkov(Markov markov) {
        this.markov = markov;
    }

}
