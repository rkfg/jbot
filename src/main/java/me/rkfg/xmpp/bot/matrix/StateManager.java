package me.rkfg.xmpp.bot.matrix;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class StateManager {
    private Map<String, String> names = new HashMap<>();
    private Map<String, String> mxids = new HashMap<>();
    private Set<String> joinedRooms = new HashSet<>();
    private Logger log = LoggerFactory.getLogger(getClass());

    public void addDisplayName(String mxid, String displayName) {
        log.debug("Adding display name {} for {}", displayName, mxid);
        names.put(mxid, displayName);
        mxids.put(displayName, mxid);
    }

    public String getName(String mxid) {
        return names.get(mxid);
    }

    public String getMXID(String name) {
        return mxids.get(name);
    }

    public void removeDisplayName(String mxid) {
        String displayName = names.remove(mxid);
        mxids.remove(displayName);
        log.debug("Removing display name {} for {}", displayName, mxid);
    }

    public void joinRoom(String roomName) {
        joinedRooms.add(roomName);
    }

    public boolean isInRoom(String roomName) {
        return joinedRooms.contains(roomName);
    }

    public void leaveRoom(String roomName) {
        joinedRooms.remove(roomName);
    }

    public Set<String> listRooms() {
        return Collections.unmodifiableSet(joinedRooms);
    }
}
