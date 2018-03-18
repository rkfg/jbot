package me.rkfg.xmpp.bot.matrix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoomParticipantsManager {
    private Map<String, Set<String>> roomUsers = new HashMap<>();

    public void addUser(String roomId, String userId) {
        getUsers(roomId).add(userId);
    }

    public void removeUser(String roomId, String userId) {
        getUsers(roomId).remove(userId);
    }

    public Set<String> getUsers(String roomId) {
        return roomUsers.computeIfAbsent(roomId, k -> new HashSet<>());
    }

    public boolean isRoomEmpty(String roomId) {
        return getUsers(roomId).size() == 1; // only I left
    }
}
