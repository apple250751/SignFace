package com.kuma.facesignteacher.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.kuma.facesignteacher.DB.DBController;
import com.kuma.facesignteacher.Facepp.MyFacepp;
import com.kuma.facesignteacher.GpsLocationListener;
import com.kuma.facesignteacher.R;

/**
 * Created by kuma on 2017/12/30.
 */

public class TeacherFragment extends Fragment {

    public static final String TAG = "TEACHERFRAGMENT";
    //private MyFacepp facepp;

    private ArrayAdapter<String> course_adapter = null;
    private ArrayAdapter<String> student_adapter = null;

    private ListView student;
    private Spinner course;
    //private DBController db;

    private String[] course_data = null;
    private String[] student_data = null;
    private boolean isremoved = false;
    //private GpsLocationListener locationListener = null;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_teacher, container, false);

        Log.e(TAG, "on create addFragment");
        //facepp = new MyFacepp();
        //locationListener = new GpsLocationListener(getContext());
        student = (ListView) view.findViewById(R.id.student_listView);
        course = (Spinner) view.findViewById(R.id.course_spinner);
        //讀取課程資訊
        view.findViewById(R.id.load_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DBController db = new DBController();
                            db.connectDB();
                            course_data = db.getCourse();
                            db.closeDB();
                        }
                    });
                    t.start();
                    t.join();
                    if (course_data != null) {
                        course_adapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_selectable_list_item, course_data);
                        course.setAdapter(course_adapter);
                    } else {
                        course_adapter.clear();
                        course_adapter.notifyDataSetChanged();
                        //course_adapter = new ArrayAdapter<String>(getActivity(),
                        //        android.R.layout.simple_selectable_list_item);
                        course.setAdapter(course_adapter);
                    }
                    Toast.makeText(getActivity(), "課程獲取成功！", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
                    Toast.makeText(getActivity(), "課程獲取失敗！", Toast.LENGTH_LONG).show();
                }
            }
        });

        //刷新簽到列表
        view.findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DBController db = new DBController();
                        db.connectDB();
                        try {
                            Log.e(TAG, "course_adapter_selected:" + course_adapter.getItem(course.getSelectedItemPosition()));
                            //locationListener.getLocation();
                            //student_data = db.getData(course_adapter.getItem(course.getSelectedItemPosition()),
                            //        locationListener.getLa(), locationListener.getLt());
                            student_data = db.getData(course_adapter.getItem(course.getSelectedItemPosition()),0,0);
                        } catch (Exception e) {
                            student_data = null;
                            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
                            //Toast.makeText(getActivity(), "刷新列表失敗！", Toast.LENGTH_LONG).show();
                        }
                        db.closeDB();
                    }
                });
                try {
                    t.start();
                    t.join();
                    if (student_data != null) {
                        student_adapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_selectable_list_item,
                                student_data);
                        student.setAdapter(student_adapter);
                        Toast.makeText(getActivity(), "學生名單獲取成功！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "學生名單獲取失敗！", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
                    Toast.makeText(getActivity(), "學生名單獲取失敗！", Toast.LENGTH_LONG).show();
                }
            }
        });

        //增加課程
        view.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

                ft.hide(getActivity().getSupportFragmentManager().findFragmentByTag(TAG));

                ft.add(R.id.fragment, new AddFragment(), AddFragment.TAG)
                        .addToBackStack(null)
                        .commit();
                /*
                Fragment fragment = getActivity().getSupportFragmentManager()
                        .findFragmentByTag(AddFragment.TAG);
                if (fragment == null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, new AddFragment(),AddFragment.TAG)
                            .addToBackStack(null)
                            .commit();
                } else {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, fragment)
                            .addToBackStack(null)
                            .commit();
                }
                */
            }
        });

        //移除課程
        view.findViewById(R.id.remove_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DBController db = new DBController();
                        db.connectDB();
                        try {
                            Log.e(TAG, "remove_course:" + course_adapter.getItem(course.getSelectedItemPosition()));
                            if(!course_adapter.getItem(course.getSelectedItemPosition()).isEmpty()) {
                                db.removeCourse(course_adapter.getItem(course.getSelectedItemPosition()));
                                new MyFacepp().deleteFaceSet(course_adapter.getItem(course.getSelectedItemPosition()));
                                isremoved = true;
                            }
                        } catch (Exception e) {
                            isremoved = false;
                            //student_data = null;
                            Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
                            //Toast.makeText(getActivity(), "刷新列表失敗！", Toast.LENGTH_LONG).show();
                        }
                        db.closeDB();
                    }
                });
                try{
                    t.start();
                    t.join();
                    //view.findViewById(R.id.load_button).callOnClick();
                    if(isremoved)
                        Toast.makeText(getActivity(), "課程移除成功！", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(), "課程移除失敗！", Toast.LENGTH_LONG).show();
                }catch (Exception e)
                {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
                    Toast.makeText(getActivity(), "課程移除失敗！", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }
}
