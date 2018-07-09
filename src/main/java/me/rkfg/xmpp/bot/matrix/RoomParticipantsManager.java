package me.rkfg.xmpp.bot.matrix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomParticipantsManager {
    private Map<String, Set<String>> roomUsers = new HashMap<>();
    private String mxid;

    public RoomParticipantsManager(String mxid) {
        this.mxid = mxid;
    }

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
        Set<String> users = getUsers(roomId);
        return users.size() == 1 && users.contains(mxid); // only I left
    }

    public Set<String> getEmptyRooms() {
        return roomUsers.keySet().stream().filter(this::isRoomEmpty).collect(Collectors.toSet());
    }

    public void removeRoom(String roomId) {
        roomUsers.remove(roomId);
    }
}
