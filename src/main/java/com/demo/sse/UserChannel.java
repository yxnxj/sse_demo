package com.demo.sse;

import org.slf4j.LoggerFactory;
import reactor.core.publisher.*;
import reactor.core.publisher.Sinks.Many;

import java.util.logging.Logger;

public class UserChannel {
//    private EmitterProcessor<String> processor;
    private final Flux<String> flux;
    private final Many<String> sink;
//    private FluxSink<String> sink;
    private Runnable closeCallback;

    public UserChannel() {

        this.sink = Sinks.many().unicast().onBackpressureBuffer();

        Flux<String> fluxWithCancelSupport = sink.asFlux()
                .doOnCancel(() -> {
                    System.out.println("canceled on flux");
                    if (sink.currentSubscriberCount() == 1) {
                        System.out.println("closing..");
                        close();
                    }
                })
                .doOnTerminate(() -> {
                    System.out.println("terminated on flux");
                });

        this.flux = fluxWithCancelSupport.log();
//        processor = EmitterProcessor.create();
//        this.sink = processor.sink();
//        this.flux = processor
//                .doOnCancel(() -> {
//                    System.out.println("canceled on flux");
////                    logger.info("doOnCancel, downstream " + processor.downstreamCount());
//                    if (processor.downstreamCount() == 1){
//                        close();
//                        System.out.println("closing..");
//                    }
//                })
//                .doOnTerminate(() -> {
//                    System.out.println("terminated on flux");
////                    logger.info("doOnTerminate, downstream " + processor.downstreamCount());
//                });
    }

    private boolean printFailureLog(SignalType signalType, Sinks.EmitResult emitResult) {
        System.out.println("send failed : " + signalType + emitResult);
        return false;
    }
//    public void send(String message) {
//        sink.next(message);
//    }

    public void send(String message) {
        sink.emitNext(message, this::printFailureLog);
    }


    public Flux<String> toFlux() {
        return this.flux;
    }

//    private void close() {
//        if (closeCallback != null) closeCallback.run();
//        sink.complete();
//    }
    private void close() {
        if (closeCallback != null) closeCallback.run();
        sink.emitComplete(this::printFailureLog);
    }

    public UserChannel onClose(Runnable closeCallback) {
        this.closeCallback = closeCallback;
        return this;
    }
}