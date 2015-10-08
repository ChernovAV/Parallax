package com.chernov.android.android_paralaxparse;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Android on 07.10.2015.
 */
public class ParallaxConnect {

    public static final String TAG = "myLog";
    private static final String ENDPOINT = "http://ellotv.bigdig.com.ua/api/home/video";

    // получает данные по URL и возвращает их в виде массива байтов
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        // создаем объект URL на базе строки urlSpec, например https://www.google.com
        URL url = new URL(urlSpec);
        // создаем объект подключения к заданному URL адресу
        // url.openConnection() - возвращает URLConnection (подключение по протоколу HTTP)
        // это откроет доступ для работы с методами запросов. HttpURLConnection - предоставляет подключение
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            // создаем пустой массив байтов
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // связь с конечной точкой
            InputStream in = connection.getInputStream();
            // если подключение с интернетом отсутствует (нет кода страницы)
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            // разбираем код по 1024 байта, пока не закончится информация
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            // чтение закончено, выдаем массив байтов
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    // преобразует результат (массив байтов) из getUrlBytes(String) в String
    String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<Item> search() {

        String xmlString = null;
        try {
            // получаем данные по URL и возвращаем их в виде массива байтов, затем делаем строку
            xmlString = getUrl(ENDPOINT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // парсим строку и получаем ArrayList<Item> parseItems
        return parseItems(xmlString);
    }

    // Парсинг полученной JSON-строки
    public ArrayList<Item> parseItems(String jsonStr) {

        Log.i(TAG, "get " + jsonStr);

        JSONObject dataJsonObj = null;

        ArrayList<Item> gItems = new ArrayList<>();
        try {

            dataJsonObj = new JSONObject(jsonStr);

            JSONObject data = dataJsonObj.getJSONObject("data");

            Log.i(TAG, "data " + data);

            JSONArray items = data.getJSONArray("items");

            // каждый элемент item
            for (int i = 0; i < items.length(); i++) {

                JSONObject item = items.getJSONObject(i);

                String clip = item.getString("title");

                String count = item.getString("view_count");

                String link = item.getString("picture");

                JSONArray art = item.getJSONArray("artists");

                JSONObject it = art.getJSONObject(0);

                String artist = it.getString("name");

                Log.i(TAG, "clip " + clip);
                Log.i(TAG, "artists " + artist);
                Log.i(TAG, "count " + count);
                Log.i(TAG, "link " + link);

                // заполняем Item и добавляем
                Item Data = new Item();
                Data.setClip(clip);
                Data.setName(artist);
                Data.setCount(count);
                Data.setLink(link);
                gItems.add(Data);
            }

        }
        catch (Exception e) {}

        return gItems;
    }
}