package com.kuma.facesignteacher.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kuma.facesignteacher.Fragment.TeacherFragment;
import com.kuma.facesignteacher.R;

public class TeacherActivity extends AppCompatActivity {

    private static final String TAG = "TEACHERACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment, new TeacherFragment(), TeacherFragment.TAG)
                .addToBackStack(null)
                .commit();
    }
}
