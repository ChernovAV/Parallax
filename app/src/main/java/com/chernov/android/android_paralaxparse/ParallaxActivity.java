package com.chernov.android.android_paralaxparse;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class ParallaxActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (savedInstanceState != null) {
            ft.replace(R.id.fragmentContainer, getSupportFragmentManager()
                    .findFragmentById(R.id.fragmentContainer));
            ft.commit();
        } else {
            ParallaxFragment mContentFragment = new ParallaxFragment();
            ft.add(R.id.fragmentContainer, mContentFragment);
            ft.commit();
        }
    }
}
