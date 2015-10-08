package com.chernov.android.android_paralaxparse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class ParralaxDownloader<Token> extends HandlerThread {
    private static final String TAG = "ParallaxDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    // кэш в оперативной памяти
    private MemoryCache mMemoryCache;

    // приемник и обработчик сообщений
    Handler mHandler;
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    // для хранения экзэмпляра handler (имеет доступ к handler, связанному с экзэмпляром looper,
    // главного потока, через поле mResponseHandler)
    Handler mResponseHandler;
    Listener<Token> mListener;

    // интерфейс слушателя для передачи ответов
    public interface Listener<Token> {
        void onThumbnailDowloanded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    @SuppressLint("NewApi")
    public ParralaxDownloader(Handler responseHandler, MemoryCache memoryCache) {
        super(TAG);

        mResponseHandler = responseHandler;
        mMemoryCache = memoryCache;
    }

    @SuppressLint("HandlerLeak")
    @Override
    //Метод onLooperPrepared() вызывается до того, как Looper проверит очередь в первый раз
    //поэтому он хорошо подходит для создания реализации Handler
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressWarnings("unchecked")
                    Token token = (Token) msg.obj;
                    handleRequest(token);
                }
            }
        };
    }

    // метот ожидает получить token и string. Вызывается в реализации адаптера getView(...)
    public void queueThumbnail(Token token, String url) {
        // запись пары Фото-URL адрес
        requestMap.put(token, url);
        mHandler
                // получение объекта Message
                .obtainMessage(MESSAGE_DOWNLOAD, token)
                // отправка сообщения его обработчику на постановку в очередь сообщений
                .sendToTarget();
    }

    private void handleRequest(final Token token) {
        try {
            final String url = requestMap.get(token);
            if (url == null) return;
            //Получить объект Bitmap из кэша
            Bitmap bitmapTemp = mMemoryCache.getBitmapFromMemCache(url);
            if (bitmapTemp == null) {
                //Загрузка изображения из интернета
                byte[] bitmapBytes = new ParallaxConnect().getUrlBytes(url);
                // из byte получаем  bitmap
                bitmapTemp = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                // добавление объекта Bitmap в кэш. Вычисляем размеры и сжимаем
                mMemoryCache.addBitmapToMemoryCache(url, decodeSampledBitmapFromByte(
                        bitmapBytes,
                        bitmapTemp.getWidth() / 2,
                        bitmapTemp.getHeight() / 2));
                // освободим ресурс
                bitmapTemp.recycle();
            }

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url) return;

                    requestMap.remove(token);
                    mListener.onThumbnailDowloanded(token, mMemoryCache.getBitmapFromMemCache(url));
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }

    public static Bitmap decodeSampledBitmapFromByte(byte[] res, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(res, 0, res.length);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (((options.outHeight / 2) / inSampleSize)
                    > reqHeight && ((options.outWidth / 2) / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}