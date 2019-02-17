package com.kuma.facesignteacher.Fragment;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kuma.facesignteacher.DB.DBController;
import com.kuma.facesignteacher.Facepp.MyFacepp;
import com.kuma.facesignteacher.R;

import java.util.ArrayList;
import java.util.List;


public class AddFragment extends Fragment {

    public static final String TAG = "ADDFRAGMENT";
    private static int RESULT_LOAD_IMAGE = 1;

    //private DBController db;
    private MyFacepp facepp;
    private ArrayAdapter<String> adapter = null;
    private ListView student = null;
    private EditText course_input;
    private EditText teacher_input;

    private List<String> students_id = null;
    private List<String> students_name = null;
    private List<String> students_token = null;
    private String token = "";
    private boolean isok = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add, container, false);
        // Inflate the layout for this fragment

        //DBController db = new DBController();
        facepp = new MyFacepp();

        students_id = new ArrayList<>();
        students_name = new ArrayList<>();
        students_token = new ArrayList<>();
        student = (ListView) view.findViewById(R.id.student_list);

        course_input = (EditText) view.findViewById(R.id.input_course_name);
        teacher_input = (EditText) view.findViewById(R.id.input_teacher);

        //添加學生
        view.findViewById(R.id.add_student_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStudentDialog();
            }
        });

        //移除學生
        view.findViewById(R.id.remove_student_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null && student.isSelected()) {
                    adapter.remove(adapter.getItem(student.getSelectedItemPosition()));
                    student.setAdapter(adapter);
                    Toast.makeText(getActivity(), "移除成功！", Toast.LENGTH_LONG).show();
                }
            }
        });

        //確實增加課程並且加入到數據庫中
        view.findViewById(R.id.create_course_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String course = course_input.getText().toString() + "_" +
                        teacher_input.getText().toString() + "_student";
                if (course_input.getText().length() == 0) {
                    Toast.makeText(getActivity(), "請輸入課程名！", Toast.LENGTH_LONG).show();
                } else if (teacher_input.getText().length() == 0) {
                    Toast.makeText(getActivity(), "請輸入教師名！", Toast.LENGTH_LONG).show();
                }// else if (db.getCourseStudent(course) != null) {
                //   Toast.makeText(getActivity(), "課程已存在！", Toast.LENGTH_LONG).show();
                //}
                else if (students_id == null || students_id.size() == 0)
                    Toast.makeText(getActivity(), "請添加學生！", Toast.LENGTH_LONG).show();
                else {
                    try {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    isok = true;
                                    StringBuffer tokens = new StringBuffer();
                                    for (int i = 0; i < students_id.size(); i++) {
                                        if(i == 0)
                                            tokens.append(students_token.get(i));
                                        else
                                            tokens.append("," + students_token.get(i));
                                    }
                                    if(facepp.createFaceSet(course_input.getText().toString() + "_" +
                                            teacher_input.getText().toString(), tokens.toString()) == null)
                                        isok = false;
                                    if (isok) {
                                        DBController db = new DBController();
                                        db.connectDB();
                                        db.addCourse(course_input.getText().toString(),
                                                teacher_input.getText().toString(),
                                                "0.0",
                                                "0.0");
                                        for (int i = 0; i < students_id.size(); i++) {
                                            Log.e(TAG, "add to db,id:" + students_id.get(i));
                                            db.addStudent(course_input.getText().toString(),
                                                    teacher_input.getText().toString(),
                                                    students_id.get(i),
                                                    students_name.get(i),
                                                    students_token.get(i));
                                        }
                                        db.closeDB();
                                    }
                                } catch (Exception e) {
                                    isok = false;
                                    Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
                                }
                            }
                        });
                        t.start();
                        t.join();
                        if (isok) {
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .show(getActivity().getSupportFragmentManager().findFragmentByTag(AddFragment.TAG));
                            getActivity().getSupportFragmentManager().popBackStack();
                            Toast.makeText(getActivity(), "課程添加成功！", Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(getActivity(), "課程添加失敗！", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
                        Toast.makeText(getActivity(), "課程添加失敗！", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .show(getActivity().getSupportFragmentManager().findFragmentByTag(AddFragment.TAG));
        super.onPause();
    }

    private boolean isExist(String id, String name, String token) {
        if (!students_id.isEmpty()) {
            for (int i = 0; i < students_id.size(); i++) {
                if (students_id.get(i).equals(id))
                    return true;
            }
            for (int i = 0; i < students_name.size(); i++) {
                if (students_name.get(i).equals(name))
                    return true;
            }
            for (int i = 0; i < students_token.size(); i++) {
                if (students_token.get(i).equals(token))
                    return true;
            }
        }
        return false;
    }

    private boolean add(String id, String name, String token) {
        Log.e(TAG, "id:" + id + ", name:" + name + " ,token:" + token);
        if (token.contains("CONCURRENCY_LIMIT_EXCEEDED")) {
            Log.e(TAG, "error!! token:" + token);
            return false;
        }
        students_id.add(id);
        students_name.add(name);
        students_token.add(token);
        return true;
    }

    private String[] getStudent() {
        if (students_id.isEmpty())
            return null;
        String[] r = new String[students_id.size()];
        for (int i = 0; i < students_id.size(); i++) {
            String tamp = String.valueOf(i) + ". " +
                    students_id.get(i) + " - " +
                    students_name.get(i);
            r[i] = tamp;
            Log.e(TAG, tamp);
        }
        return r;
    }

    private boolean remove(String id, String name, String token) {
        for (int i = 0; i < students_id.size(); i++) {
            if (students_id.get(i).equals(id)) {
                students_id.remove(i);
                students_name.remove(i);
                students_token.remove(i);
                return true;
            }
        }
        for (int i = 0; i < students_name.size(); i++) {
            if (students_name.get(i).equals(name)) {
                students_id.remove(i);
                students_name.remove(i);
                students_token.remove(i);
                return true;
            }
        }
        for (int i = 0; i < students_token.size(); i++) {
            if (students_token.get(i).equals(token)) {
                students_id.remove(i);
                students_name.remove(i);
                students_token.remove(i);
                return true;
            }
        }
        return false;
    }

    private void addStudentDialog() {

        token = "";
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_add_detail, null);

        dialog.setTitle("請輸入學生信息");
        dialog.setView(v);
        dialog.setNeutralButton("添加人臉", null);
        dialog.setNegativeButton("確定", null);
        dialog.setPositiveButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });


        //確定
        final AlertDialog alertDialog = dialog.create();
        alertDialog.show();
        if (alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText id = (EditText) v.findViewById(R.id.add_student_id_input);
                    EditText name = (EditText) v.findViewById(R.id.add_student_name_input);
                    if (id.getText().length() == 0) {
                        Toast.makeText(getActivity(), "請輸入學號！", Toast.LENGTH_LONG).show();
                        return;
                    } else if (name.getText().length() == 0) {
                        Toast.makeText(getActivity(), "請輸入名字！", Toast.LENGTH_LONG).show();
                        return;
                    } else if (token.equals("")) {
                        Toast.makeText(getActivity(), "請添加人臉！", Toast.LENGTH_LONG).show();
                        return;
                    } else if (isExist(id.getText().toString(), name.getText().toString(), token)) {
                        Toast.makeText(getActivity(), "該學生已存在！", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        add(id.getText().toString(), name.getText().toString(), token);
                        String[] r = getStudent();
                        if (r != null && r.length != 0) {
                            adapter = new ArrayAdapter<String>(getActivity(),
                                    android.R.layout.simple_selectable_list_item,
                                    r);
                            student.setAdapter(adapter);
                        }
                        Toast.makeText(getActivity(), "學生添加成功！", Toast.LENGTH_LONG).show();
                        alertDialog.cancel();
                    }
                }
            });
        }
        //添加人臉
        if (alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL) != null) {
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE &&
                resultCode == getActivity().RESULT_OK &&
                data != null) {
            Uri uri = data.getData();
            Log.e(TAG, uri.toString());
            try {
                //File path = new File(data.getData().toString());
                ContentResolver contentResolver = getActivity().getContentResolver();
                final Bitmap bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                Log.e(TAG, "bitmap  h:" + String.valueOf(bitmap.getHeight()) + ", w:" + String.valueOf(bitmap.getWidth()));
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        token = facepp.GetFaceTokenBase64(bitmap);
                        Log.e(TAG, token);
                    }
                });
                t.start();
                t.join();

                if (token.contains("CONCURRENCY_LIMIT_EXCEEDED"))
                    Toast.makeText(getActivity(), "上傳失敗,请重新添加人脸！", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getActivity(), "上傳成功！", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "上傳失敗！", Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == getActivity().RESULT_CANCELED) {
            Toast.makeText(getActivity(), "取消", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "獲取失敗！", Toast.LENGTH_LONG).show();
        }
        return;
    }
}
