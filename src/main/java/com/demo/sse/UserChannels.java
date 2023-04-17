package com.demo.sse;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserChannels {
    private ConcurrentHashMap<Long, UserChannel> map = new ConcurrentHashMap<>();

    public UserChannel connect(Long userId) {
        return map.computeIfAbsent(userId, key -> new UserChannel().onClose(() ->
                map.remove(userId)));
    }

    public void post(Long userId, String message) {
        Optional.ofNullable(map.get(userId)).ifPresentOrElse(ch -> ch.send(message), () -> {
        });
    }
}