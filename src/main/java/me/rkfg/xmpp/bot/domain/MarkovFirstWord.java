package me.rkfg.xmpp.bot.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ru.ppsrk.gwt.dto.BasicDTO;

@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "number") })
public class MarkovFirstWord extends BasicDTO {
    @ManyToOne(cascade = { CascadeType.DETACH })
    MarkovFirstWordCount word;
    Long number;
    @OneToOne(cascade = { CascadeType.DETACH })
    Markov markov;

    public MarkovFirstWord() {
    }

    public MarkovFirstWord(MarkovFirstWordCount word, Long number, Markov markov) {
        super();
        this.word = word;
        this.number = number;
        this.markov = markov;
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
