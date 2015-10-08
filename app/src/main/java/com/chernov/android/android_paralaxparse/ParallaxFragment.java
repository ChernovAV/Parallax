package com.chernov.android.android_paralaxparse;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ParallaxFragment extends ListFragment {

    // ArrayList, содержащий элементы Item
    static ArrayList<Item> mItems;
    // адаптер, принимает ArrayList mItems
    Adapter adapter;
    // экзэмпляр класса, принимает и обрабатывает handler,
    ParralaxDownloader<ImageView> mThumbnailThread;
    // контроль размера, работа с кэш (добавление и загрузка bitmap)
    MemoryCache mMemoryCache;

    public static final String TAG = "myLog";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // при смене ориентации экрана этот фрагмент сохраняет свое состояние. onDestroy не вызывается
        setRetainInstance(true);
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;
        // объект принимает размер кэш, можно регулировать размер в поле cacheSize
        mMemoryCache = new MemoryCache(cacheSize);
        // экзэмпляр элементов Item
        mItems = new ArrayList<>();
        // отправляем экзэмпляр handler и mMemoryCache
        mThumbnailThread = new ParralaxDownloader<>(new Handler(), mMemoryCache);
        mThumbnailThread.setListener(new ParralaxDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDowloanded(ImageView imageView, Bitmap thumbnail) {
            // предотвращаем назначение изображения устаревшему виджиту ImageView при помощи isVisible
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        if(mItems.size()==0) {
            adapter = new Adapter( mItems);
            new AsyncTaskRun().execute();
        }
        // присоединяем адаптер к listview
        setListAdapter(adapter);
        return v;
    }

    // обработка информации в потоке, на выходе ArrayList с элементами Item
    private class AsyncTaskRun extends AsyncTask<Void, Void, ArrayList<Item>> {
        @Override
        // запуск потока
        protected ArrayList<Item> doInBackground(Void... params) {

            return new ParallaxConnect().search();
        }

        @Override
        // метод, выполняемый после doInBackground, принимает массив items
        protected void onPostExecute(ArrayList<Item> items) {
            if(mItems==null) {
                mItems = items;
                // если массив не пуст
            } else {
                mItems.addAll(items);
                Log.i(TAG, "mItems " + mItems.size());
            }
            adapter.notifyDataSetChanged();
        }
    }

    // адаптер принимает ArrayList с нашими items
    public class Adapter extends ArrayAdapter<Item> {

        public Adapter(ArrayList<Item> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.item, parent, false);
            }

            final Item item = getItem(position);

            ImageView imageView = (ImageView)convertView.findViewById(R.id.picture);

            ((TextView)convertView.findViewById(R.id.clip)).setText(item.getClip());
            ((TextView)convertView.findViewById(R.id.name)).setText(item.getName());
            ((TextView)convertView.findViewById(R.id.count)).setText(item.getCount());

            // если в коллекции уже существует картинка, загружаем ее
            if(mMemoryCache.getBitmapFromMemCache(item.getLink()) == null) {
                imageView.setImageResource(R.drawable.loading);
                // отправляем imageView и ссылку на картинку для обработки и добавление в кэш
                mThumbnailThread.queueThumbnail(imageView, item.getLink());
            } else {
                imageView.setImageBitmap(mMemoryCache.getBitmapFromMemCache(item.getLink()));
            }

            return convertView;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailThread.quit();
    }
}
