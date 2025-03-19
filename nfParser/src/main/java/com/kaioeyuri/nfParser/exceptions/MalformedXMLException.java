package com.kaioeyuri.nfParser.exceptions;

public class MalformedXMLException extends RuntimeException {
    public MalformedXMLException() {
        super();
    }

    public MalformedXMLException(String message) {
        super(message);
    }

    public MalformedXMLException(String message, Throwable cause) {
        super(message, cause);
    }
}
