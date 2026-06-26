package com.chat.exception;

import java.util.UUID;

public class InactiveSessionException extends RuntimeException {

    public InactiveSessionException(UUID id) {
        super("Sessão inativa: " + id);
    }
}
