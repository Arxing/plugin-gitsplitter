package org.arxing.functions;

public interface ThrowsConsumer<T> {
    void apply(T data) throws Exception;
}
