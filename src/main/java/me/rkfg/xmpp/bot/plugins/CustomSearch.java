/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.rkfg.xmpp.bot.plugins;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;
import static me.rkfg.xmpp.bot.plugins.CommandPlugin.COMMAND_GROUP;
import org.jivesoftware.smack.packet.Message;
import org.jsoup.Jsoup;
import static org.jsoup.helper.HttpConnection.DEFAULT_UA;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author skfg
 */

public class CustomSearch extends CommandPlugin {
       
    @Override
    public String processCommand(Message message, Matcher matcher) {
        return searchString(matcher.group(COMMAND_GROUP));
    }
    
    protected String searchString(String str) {
        String s = null;
        
        try {
            s = getResultHTML(str);

                    } catch (IOException | ParserConfigurationException ex) {
            Logger.getLogger(CustomSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }
     
    protected String getResultHTML(String str) throws IOException, ParserConfigurationException
    {
        String urlStr = String.format("https://duckduckgo.com/html/?q=%s", str);
        log.info("DuckDuckGo search string: {}", urlStr);
        
        Document html = Jsoup.connect(urlStr).userAgent(DEFAULT_UA).get();
        Document doc = Jsoup.parse(html.toString(), "UTF-8");

        Element title = doc.select("a.result__a").first();
        Element text = doc.select("a.result__snippet").first();
        Element link = doc.select("a.result__url").first();
                
        log.info(title.text());
        log.info(text.text());
        log.info(link.text());
        
        String res = String.format("%s\n%s\nhttp://%s\n", title.text(), text.text(), link.text()); // possible failure with https links (obviously)

        return res;
    }
    
    @Override
    public List<String> getCommand() {
        return Arrays.asList("d", "в");
    }
    
    @Override
    public String getManual() {
        return "выдать первый результат из DuckDuckGo.\n" + "Формат: <текст запроса>\n" + "Пример: " + PREFIX + "d печеньки";
    }
}
