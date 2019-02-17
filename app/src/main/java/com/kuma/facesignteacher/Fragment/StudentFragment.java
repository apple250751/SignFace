package com.kuma.facesignteacher.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.kuma.facesignteacher.DB.DBController;
import com.kuma.facesignteacher.Facepp.MyBitmapUtil;
import com.kuma.facesignteacher.Facepp.MyFacepp;
import com.kuma.facesignteacher.Global.Global;
import com.kuma.facesignteacher.GpsLocationListener;
import com.kuma.facesignteacher.R;
import com.megvii.cloud.http.Response;

import java.io.File;

/**
 * Created by kuma on 2017/12/31.
 */

public class StudentFragment extends Fragment {

    public static final String TAG = "STUDENTFRAGMENT";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;

    private Uri fileUri = null;
    private MyFacepp facepp;
    private ArrayAdapter<String> course_adapter = null;
    private String face_token = null;
    private String res_token = null;
    private EditText id_input;
    private EditText name_input;
    private Spinner course;
    //private GpsLocationListener locationListener;
    private String[] course_data = null;
    private int state = -1;
    //private DBController db;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student, container, false);

        //db = new DBController();
        //db.connectDB();
        facepp = new MyFacepp();
        //locationListener = new GpsLocationListener(getContext());
        id_input = (EditText) view.findViewById(R.id.id_input);
        name_input = (EditText) view.findViewById(R.id.name_input);
        course = (Spinner) view.findViewById(R.id.couse);

        //讀取課程資訊按鈕
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
                        //course.setBackgroundColor(Color.BLACK);
                        course.setAdapter(course_adapter);
                    }
                    Toast.makeText(getActivity(), "課程獲取成功！", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
                    Toast.makeText(getActivity(), "課程獲取失敗！", Toast.LENGTH_LONG).show();
                }
            }
        });

        //拍攝人臉
        view.findViewById(R.id.take_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                face_token = null;
                res_token = null;
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = Uri.fromFile(getOutputPhotoFile());
                //Log.e(TAG, fileUri.toString());
                i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ);
            }
        });

        //解析人臉並且簽到
        view.findViewById(R.id.set_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (course_data == null) {
                    Toast.makeText(getActivity(), "請加載課程！", Toast.LENGTH_LONG).show();
                } else if (id_input.getText().length() == 0 || name_input.getText().length() == 0) {
                    Toast.makeText(getActivity(), "請輸入學號和名字！", Toast.LENGTH_LONG).show();
                } else if (fileUri == null) {
                    Toast.makeText(getActivity(), "請拍攝人臉！", Toast.LENGTH_LONG).show();
                } else {
                    state = -1;
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.e(TAG, fileUri.toString());
                            String c = course_adapter.getItem(course.getSelectedItemPosition());
                            if(face_token == null)
                                loadBitmap(fileUri.getPath());
                            if (face_token != null) {
                                res_token = facepp.searchFace(face_token,
                                        c);
                                if (res_token == null)
                                    Log.e(TAG, "search face error!!");
                                else if(res_token.equals("<bad>"))
                                    state = 1;
                                else {
                                    Log.e(TAG, res_token);
                                    DBController db = new DBController();
                                    db.connectDB();
                                    if (!db.inCourse(c, id_input.getText().toString(),
                                            name_input.getText().toString(), res_token))
                                        state = 1;
                                    else if (db.isSigned(c, id_input.getText().toString(),
                                            name_input.getText().toString(), res_token))
                                        state = 2;
                                    else {
                                        //locationListener.getLocation();
                                        db.studentSign(id_input.getText().toString(),
                                                name_input.getText().toString(),
                                                c,
                                                res_token,
                                                "0",
                                                "0");
                                        //String.valueOf(locationListener.getLa()),
                                        //String.valueOf(locationListener.getLt()));
                                        db.closeDB();
                                        state = 0;
                                    }
                                }
                            }
                        }
                    });
                    try {
                        t.start();
                        t.join();
                        if (res_token != null && face_token != null) {
                            if (state == 0)
                                Toast.makeText(getActivity(), "上傳成功！", Toast.LENGTH_LONG).show();
                            else if (state == 1)
                                Toast.makeText(getActivity(), "不是本課程的學生！", Toast.LENGTH_LONG).show();
                            else if (state == 2)
                                Toast.makeText(getActivity(), "該學生已經簽到過了！", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getActivity(), "上傳失敗！", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(getActivity(), "上傳失敗請重新上傳！", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "上傳失敗！", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        return view;
    }

    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getActivity().getPackageName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "無法創建目錄！");
                return null;
            }
        }
        String timeStamp = Global.getTime();
        Log.e(TAG, directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
        return new File(directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ &&
                resultCode == getActivity().RESULT_OK) {
            //fileUri = data.getData();
            Toast.makeText(getActivity(), "照片拍攝成功！", Toast.LENGTH_LONG).show();
        } else if (resultCode == getActivity().RESULT_CANCELED) {
            Toast.makeText(getActivity(), "取消", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "拍攝失敗！", Toast.LENGTH_LONG).show();
        }
        */
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
            if (resultCode == getActivity().RESULT_OK) {
                Uri photoUri = null;
                if (data == null) {
                    Toast.makeText(getActivity(), "照片拍攝成功！",
                            Toast.LENGTH_LONG).show();
                    //photoUri = fileUri;
                } else {
                    fileUri = data.getData();
                    //photoUri = data.getData();
                    Toast.makeText(getActivity(), "照片拍攝成功並且保存在" + data.getData(),
                            Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Toast.makeText(getActivity(), "取消", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "拍攝失敗！",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadBitmap(String photoUri) {
        File imageFile = new File(photoUri);
        if (imageFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Matrix matrix = new Matrix();
            matrix.postScale(0.2f, 0.2f); //长和宽放大缩小的比例
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = MyBitmapUtil.rotateBitmap(bitmap, 90, true);
            //BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmap);
            //photoImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //photoImage.setImageDrawable(drawable);
            try {
                face_token = facepp.GetFaceTokenBase64(bitmap);
                if (face_token.contains("error_message"))
                    face_token = null;
            } catch (Exception e) {
                face_token = null;
                Log.e(TAG, e.getClass().getName() + ": " + e.getMessage());
            }
            //mTextView.setText(token);
        }
    }
}
