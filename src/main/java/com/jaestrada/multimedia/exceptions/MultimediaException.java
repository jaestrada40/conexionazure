package com.jaestrada.multimedia.exceptions;

public class MultimediaException extends Exception {
    
    public enum Type {
        INVALID_FILE_TYPE,
        FILE_TOO_LARGE,
        STORAGE_ERROR,
        DUPLICATE_GENRE,
        TITLE_NOT_FOUND,
        FILE_NOT_FOUND
    }
    
    private final Type type;
    
    public MultimediaException(Type type, String message) {
        super(message);
        this.type = type;
    }
    
    public MultimediaException(Type type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
}