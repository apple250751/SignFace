package com.kuma.facesignteacher.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kuma.facesignteacher.Fragment.StudentFragment;
import com.kuma.facesignteacher.R;


/**
 * Created by kuma on 2017/12/28.
 */

public class StudentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment, new StudentFragment(), StudentFragment.TAG)
                .addToBackStack(null)
                .commit();
    }
}

