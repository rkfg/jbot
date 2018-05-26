package me.rkfg.xmpp.bot.domain;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import ru.ppsrk.gwt.dto.BasicDTO;

@SuppressWarnings("serial")
@Entity
@Table(indexes = { @Index(columnList = "word", unique = true) })
public class MarkovFirstWordCount extends BasicDTO {
    String word;
    Long count;

    public MarkovFirstWordCount() {
    }

    public MarkovFirstWordCount(String word) {
        super();
        this.word = word;
        count = 0L;
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
        setCount(getCount() + 1);
    }
}
