package me.rkfg.xmpp.bot.matrix;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StateManager {
    private Map<String, String> names = new HashMap<>();
    private Set<String> joinedRooms = new HashSet<>();
    
    public void addDisplayName(String mxid, String displayName) {
        names.put(mxid, displayName);
    }
    
    public String getName(String mxid) {
        return names.get(mxid);
    }
    
    public void removeDisplayName(String mxid) {
        names.remove(mxid);
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
