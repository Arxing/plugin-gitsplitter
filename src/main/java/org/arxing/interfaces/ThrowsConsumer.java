package org.arxing.interfaces;

public interface ThrowsConsumer<T> {
    void apply(T data) throws Exception;
}
