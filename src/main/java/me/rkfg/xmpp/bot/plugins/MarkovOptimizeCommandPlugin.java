package me.rkfg.xmpp.bot.plugins;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;

import me.rkfg.xmpp.bot.domain.Markov;
import me.rkfg.xmpp.bot.domain.MarkovFirstWord;
import me.rkfg.xmpp.bot.domain.MarkovFirstWordCount;

import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.jivesoftware.smack.packet.Message;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.HibernateCallback;
import ru.ppsrk.gwt.server.HibernateUtil;

public class MarkovOptimizeCommandPlugin extends CommandPlugin {

    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    volatile HashMap<String, ReentrantLock> wordsInProcess = new HashMap<String, ReentrantLock>();

    @Override
    public String processCommand(Message message, Matcher matcher) throws LogicException, ClientAuthException {
        Long[] minmax = HibernateUtil.exec(new HibernateCallback<Long[]>() {

            @Override
            public Long[] run(Session session) throws LogicException, ClientAuthException {
                session.createSQLQuery(
                        "SET REFERENTIAL_INTEGRITY FALSE; truncate table markovfirstword; truncate table markovfirstwordcount; SET REFERENTIAL_INTEGRITY TRUE;")
                        .executeUpdate();
                Object[] markovMinMax = (Object[]) session.createQuery("select MIN(id), MAX(id) from Markov").uniqueResult();
                return new Long[] { (Long) markovMinMax[0], (Long) markovMinMax[1] };
            }
        });
        Long min = minmax[0];
        Long max = minmax[1];
        log.info("Processing lines from {} to {}...", min, max);
        for (long i = min; i <= max; i++) {
            final long cnt = i;
            executor.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        HibernateUtil.exec(new HibernateCallback<Void>() {

                            @Override
                            public Void run(Session session) throws LogicException, ClientAuthException {
                                Markov markov = (Markov) session.get(Markov.class, cnt);
                                if (markov == null) {
                                    return null;
                                }
                                String firstWord = markov.getFirstWord();
                                ReentrantLock lock;
                                synchronized (wordsInProcess) {
                                    lock = wordsInProcess.get(firstWord);
                                    if (lock == null) {
                                        lock = new ReentrantLock();
                                        wordsInProcess.put(firstWord, lock);
                                    }
                                }
                                lock.lock();
                                try {
                                    MarkovFirstWordCount count = (MarkovFirstWordCount) session
                                            .createQuery("from MarkovFirstWordCount where word = :fw").setString("fw", firstWord)
                                            .uniqueResult();
                                    if (count == null) {
                                        count = (MarkovFirstWordCount) session.merge(new MarkovFirstWordCount(firstWord));
                                    }
                                    count.incCount();
                                    session.merge(new MarkovFirstWord(count, count.getCount() - 1, markov));
                                    session.getTransaction().commit();
                                } catch (NonUniqueResultException e) {
                                    log.warn("Non-unique word found: {}", firstWord);
                                }
                                if (cnt % 10000 == 0) {
                                    log.info("processed {} lines...", cnt);
                                }
                                synchronized (wordsInProcess) {
                                    if (!lock.hasQueuedThreads()) {
                                        wordsInProcess.remove(firstWord);
                                    }
                                }
                                lock.unlock();
                                return null;
                            }
                        });
                    } catch (Throwable e) {
                        log.warn("Exception happened: ", e);
                    }
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "done.";
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("mo");
    }

}
