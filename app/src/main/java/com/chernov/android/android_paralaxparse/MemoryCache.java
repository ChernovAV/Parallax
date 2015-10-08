package com.chernov.android.android_paralaxparse;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class MemoryCache extends LruCache<String, Bitmap> {

    public MemoryCache(int maxSize) {
        super(maxSize);
    }

    @SuppressLint("NewApi")
    @Override
    protected int sizeOf(String key, Bitmap value) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return value.getByteCount() / 1024;
    }

    // добавление объекта Bitmap в кэш
    synchronized public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            put(key, bitmap);
        }
    }
    // загрузка объекта Bitmap из кэша
    synchronized public Bitmap getBitmapFromMemCache(String key) {
        return get(key);
    }
}
