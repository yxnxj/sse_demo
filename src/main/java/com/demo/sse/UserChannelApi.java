package com.demo.sse;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class UserChannelApi {
    private UserChannels channels = new UserChannels();
    private AtomicInteger id = new AtomicInteger();

    @GetMapping("/channels/users/{userId}/messages")
    public Flux<ServerSentEvent<String>> connect(@PathVariable("userId") Long userId) {
//        int no = id.getAndAdd(1);
        Flux<String> userStream = channels.connect(userId).toFlux();
        Flux<String> tickStream = Flux.interval(Duration.ofSeconds(3))
                .map(tick -> "HEARTBEAT " + userId);
        return Flux.merge(userStream, tickStream)
                .map(str -> ServerSentEvent.builder(str).build());
    }

    @PostMapping(path = "/channels/users/{userId}/messages",
            consumes = MediaType.TEXT_PLAIN_VALUE)
    public void send(@PathVariable("userId") Long userId,
                     @RequestBody String message) {
        channels.post(userId, message);

        if(message.equals("STOP")) channels.close(userId);

    }
}
