package me.rkfg.xmpp.bot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import ru.ppsrk.gwt.server.HibernateUtil;
import ru.ppsrk.gwt.server.SettingsManager;

public abstract class BotBase implements IBot {
    private static final String PLUGINS_PACKAGE_NAME = "me.rkfg.xmpp.bot.plugins.";
    private Logger log = LoggerFactory.getLogger(getClass());
    protected SettingsManager sm = SettingsManager.getInstance();
    protected List<MessagePlugin> plugins = new LinkedList<>();
    protected String nick;

    protected void init() {
        log.info("Starting up...");
        sm.setFilename("settings.ini");
        try {
            sm.loadSettings();
        } catch (FileNotFoundException e) {
            log.warn("settings.ini not found!", e);
            return;
        } catch (IOException e) {
            log.warn("settings.ini can't be read!", e);
            return;
        }
        sm.setDefault("nick", "Talho-san");
        sm.setDefault("login", "talho");
        sm.setDefault("resource", "jbot");
        sm.setDefault("usedb", "0");
        if (sm.getIntegerSetting("usedb") != 0) {
            HibernateUtil.initSessionFactory("hibernate.cfg.xml");
        }

        nick = sm.getStringSetting("nick");
        String pluginClasses = sm.getStringSetting("plugins");
        loadPlugins(pluginClasses);
        log.info("Plugins loaded, initializing...");
        for (MessagePlugin plugin : plugins) {
            plugin.init();
        }
        log.info("Plugins initializion complete.");
    }

    @Override
    public SettingsManager getSettingsManager() {
        return sm;
    }

    @Override
    public List<MessagePlugin> getPlugins() {
        return plugins;
    }

    public String getBotNick() {
        return nick;
    }

    protected void loadPlugins(String pluginClassesNamesStr) {
        String[] pluginClassesNames = pluginClassesNamesStr.split(",\\s?");
        log.debug("Plugins found: {}", (Object) pluginClassesNames);
        for (String pluginName : pluginClassesNames) {
            try {
                Class<? extends MessagePlugin> clazz = Class.forName(PLUGINS_PACKAGE_NAME + pluginName).asSubclass(MessagePlugin.class);
                plugins.add(clazz.getDeclaredConstructor().newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                log.warn("Couldn't load plugin {}: {}", pluginName, e);
            }
        }
    }

}
