package com.expenseflow.core.storage;

import java.io.InputStream;

public interface StorageService {
    void store(String key, InputStream content, String contentType, long size);
    InputStream load(String key);
    void delete(String key);
    boolean exists(String key);
}
