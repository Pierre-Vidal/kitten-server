package com.kitten.kitten_server.config;

import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SessionIdInterceptor implements ChannelInterceptor {

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECTED.equals(accessor.getCommand())) {
            String sessionId = accessor.getSessionId();
            if (sessionId != null) {
                StompHeaderAccessor mutable = StompHeaderAccessor.create(StompCommand.CONNECTED);
                mutable.copyHeaders(accessor.toNativeHeaderMap());
                mutable.setSessionId(sessionId);
                mutable.setNativeHeader("user-name", sessionId);
                return MessageBuilder.createMessage(new byte[0], mutable.getMessageHeaders());
            }
        }
        return message;
    }
}
