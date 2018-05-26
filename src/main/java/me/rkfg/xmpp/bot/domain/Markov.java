package me.rkfg.xmpp.bot.domain;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import ru.ppsrk.gwt.dto.BasicDTO;

@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "firstWord"), @Index(columnList = "lastWord"), @Index(columnList = "position") })
public class Markov extends BasicDTO {
    String text;
    String firstWord;
    String lastWord;
    Integer position;

    public Markov() {
    }

    public Markov(String text, Integer position, String firstWord, String lastWord) {
        super();
        this.text = text;
        this.position = position;
        this.firstWord = firstWord;
        this.lastWord = lastWord;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFirstWord() {
        return firstWord;
    }

    public void setFirstWord(String firstWord) {
        this.firstWord = firstWord;
    }

    public String getLastWord() {
        return lastWord;
    }

    public void setLastWord(String lastWord) {
        this.lastWord = lastWord;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

}
