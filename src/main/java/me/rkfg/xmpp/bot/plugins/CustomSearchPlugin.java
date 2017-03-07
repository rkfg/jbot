/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.rkfg.xmpp.bot.plugins;

import static org.jsoup.helper.HttpConnection.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;

import org.jivesoftware.smack.packet.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author skfg (xmpp:asukafag@behind.computer)
 */

public class CustomSearchPlugin extends CommandPlugin {
    
    static boolean DEBUG = false;    
    
    @Override
    public String processCommand(Message message, Matcher matcher) {
        return searchString(matcher.group(COMMAND_GROUP));
    }
    
    protected String searchString(String str) {
        
        String s = null;
        
        try 
        {
            s = getResultHTML(str);
        } 
        catch (IOException | ParserConfigurationException ex)
        {
            Logger.getLogger(CustomSearchPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }
     
    protected String getResultHTML(String str) throws IOException, ParserConfigurationException, NullPointerException
    {
        String urlStr = String.format("https://duckduckgo.com/html/?q=%s", str);
        /* some debug info */
        if (DEBUG)
        {
            log.info("DuckDuckGo search string: {}", urlStr);
        }
        Document html = Jsoup.connect(urlStr).userAgent(DEFAULT_UA).get();
        Document doc = Jsoup.parse(html.toString(), "UTF-8");
        
        String res;
        
        try
        {
            if (str == null)
            {
                res = "чё?";
                return res;
            }
            else
            {
                Element divAd = doc.select("div.result--ad").first();
                if (divAd != null) {
                    divAd.remove();
                }
                Element title = doc.select("a.result__a").first();
                Element text = doc.select("a.result__snippet").first();
                Element link = doc.select("a.result__url").first();

                /* some debug info */
                if (DEBUG)
                {
                    log.info(title.text());
                    log.info(text.text());
                    log.info(link.attr("href"));
                } 
                res = String.format("%s\n%s\n%s", title.text(), text.text(), URLDecoder.decode(link.attr("href"),"UTF-8"));

                return res;
            }
        } catch (NullPointerException ex)
        {
            log.error(ex.getMessage());
            res = "ничего не найдено!";
            return res;
        }
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
