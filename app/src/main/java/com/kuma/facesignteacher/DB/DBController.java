package com.kuma.facesignteacher.DB;

import android.util.Log;

import com.kuma.facesignteacher.Global.Global;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

/**
 * Created by kuma on 2017/12/28.
 */

public class DBController {

    private static final String TAG = "DBCONTROLLER";
    private static MongoDB mongodb;
    //private String account_email = "";

    static {
        if (mongodb == null) {
            mongodb = new MongoDB();
            mongodb.connect();
            Log.e(TAG, "connect mongodb");
        }
    }

    /*
     * 如果在途中發現數據庫不能連接，請呼叫該方法再次連接  **
     * 可以在try catch裡面使用  **
     * 在new的時候就會和資料庫進行連接，一般時候不用呼叫！！ **
     */
    public boolean connectDB() {
        return mongodb.connect();
    }

    public void inseert(String name, String data) {
        Document d = new Document();
        d.put("a", data);
        mongodb.insertOne(name, d);
    }

    //不一定要關閉，在析構函數中已經會自動呼叫關閉連接，不過最好是在app關閉的時候呼叫
    public boolean closeDB() {
        return mongodb.close();
    }

    @Override
    protected void finalize() throws Throwable {
        mongodb.close();
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
    }

    /*
     * 獲得課程名字
     * 數據庫為 course
     */
    public String[] getCourse() {
        List<Document> course = mongodb.getAllData("course");
        if (course == null || course.size() == 0)
            return null;

        String[] c = new String[course.size()];
        for (int i = 0; i < course.size(); i++) {
            c[i] = course.get(i).getString("name") + "_"
                    + course.get(i).getString("teacher");
        }
        return c;
    }

    /*
     *  在課程course中加入學生
     *  數據庫為 course_teacher_student
     */
    public boolean addStudent(String course, String teacher, String id, String name, String face_token) {
        Document document = new Document();
        document.append("id", id);
        document.append("name", name);
        document.append("face_token", face_token);
        document.append("time", Global.getTime());
        /*
        Object obj = mongodb.find(course + teacher + "_student",
                Filters.and(Filters.eq("id", id),
                        Filters.eq("name", name),
                        Filters.eq("face_token", face_token)));

        if (obj == null)
            return mongodb.insertOne(course + "_" + teacher + "_student", document);
        else
            return false;
            */
        return mongodb.insertOne(course + "_" + teacher + "_student", document);
    }

    public boolean inCourse(String course,String id, String name, String face_token)
    {
        List<Document> obj = mongodb.find(course + "_student",
                Filters.and(Filters.eq("id", id),
                        Filters.eq("name", name),
                        Filters.eq("face_token", face_token)));
        if (obj == null || obj.size() == 0)
            return false;
        Log.e(TAG, obj.toString());
        return true;
    }

    public boolean isSigned(String course,String id, String name, String face_token)
    {
        if(!inCourse(course,id,name,face_token))
            return false;
        List<Document> obj = mongodb.find(course + "_sign",
                Filters.and(Filters.eq("id", id),
                        Filters.eq("name", name),
                        Filters.eq("face_token", face_token)));
        if (obj == null || obj.size() == 0)
            return false;
        Log.e(TAG, obj.toString());
        return true;
    }

    public boolean studentSign(String id, String name, String course, String face_token, String la, String lt) {
        if (!inCourse(course,id,name,face_token) && !isSigned(course,id,name,face_token))
            return false;

        Document document = new Document();
        document.append("course", course);
        document.append("id", id);
        document.append("name", name);
        document.append("face_token", face_token);
        document.append("time", Global.getTime());
        document.append("la", la);
        document.append("lt", lt);
        return mongodb.insertOne(course + "_sign", document);
    }

    /*
     * 在數據庫 course 中加入課程
     */
    public boolean addCourse(String name, String teacher, String la, String lt) {
        Document document = new Document();
        document.append("course", name + "_" + teacher);
        document.append("name", name);
        document.append("teacher", teacher);
        document.append("time", Global.getTime());
        document.append("la", la);
        document.append("lt", lt);
        return mongodb.insertOne("course", document);
        /*
        Object obj = mongodb.find("course",
                Filters.and(Filters.eq("name", name),
                        Filters.eq("teacher", teacher)));
        if (obj == null)
            return mongodb.insertOne("course", document);
        else
            return false;
            */
    }

    /*
     * 課程學生人數
     * 數據庫為 course_teacher_student
     */
    public int getStudentSize(String course) {
        List<Document> data = mongodb.getAllData(course + "_student");
        if (data.size() == 0)
            return 0;
        return data.size();
    }

    /*
     * 簽到的人
     * 數據庫為 course_teacher_sign
     */
    public String[] getSignedStudent(String course) {
        List<Document> data = mongodb.getAllData(course + "_sign");
        if (data == null || data.size() == 0)
            return null;
        String[] r = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            String tamp = data.get(i).getString("id") + " - " +
                    data.get(i).getString("name");
            r[i] = tamp;
        }
        return r;
    }

    public String[] getCourseStudent(String course) {
        List<Document> data = mongodb.getAllData(course + "_student");
        if (data == null || data.size() == 0)
            return null;
        String[] r = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            String tamp = data.get(i).getString("id") + " - " +
                    data.get(i).getString("name");
            r[i] = tamp;
        }
        return r;
    }

    //獲得簽到列表，包括簽到的和沒簽到的
    //string[0] 為簽到人數 / 總人數
    //string[1 ~ size()] 為簽到的人
    public String[] getData(String course, double la, double lt) {
        List<Document> sign = mongodb.getAllData(course + "_sign");
        List<Document> all = mongodb.getAllData(course + "_student");
        if (all == null || all.size() == 0)
            return null;
        String[] r = new String[all.size() + 1];
        int count = 0;
        for (int i = 0; i < all.size(); i++) {
            boolean finded = false;
            for (int j = 0; j < sign.size(); j++) {
                if ((all.get(i).getString("id").equals(sign.get(j).getString("id"))) &&
                        (all.get(i).getString("name").equals(sign.get(j).getString("name"))) &&
                        (all.get(i).getString("face_token").equals(sign.get(j).getString("face_token")))){ //&&
                        //next(la, lt, sign.get(j).getDouble("la"), sign.get(j).getDouble("lt")) < 500) {
                        r[i + 1] = " √√√ " + String.valueOf(i) + ". " +
                                all.get(i).getString("id") + " - " +
                                all.get(i).getString("name");
                        finded = true;
                        count++;
                        break;
                }
            }
            if (!finded) {
                r[i + 1] = " ××× " + String.valueOf(i) + ". " +
                        all.get(i).getString("id") + " - " +
                        all.get(i).getString("name");
            }
            Log.e(TAG, r[i + 1]);
        }
        r[0] = "簽到人數 / 總人數：" + String.valueOf(count) + " / " + String.valueOf(all.size());
        return r;
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    private double next(double la1, double lt1, double la2, double lt2) {
        double radla1 = rad(la1);
        double radla2 = rad(la2);
        double a = radla1 - radla2;
        double b = rad(lt1) - rad(lt2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radla1) * Math.cos(radla2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378.137;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    public boolean removeCourse(String course) {
        String[] sep = course.split("_");
        boolean c = mongodb.deleteOne("course",
                Filters.and(Filters.eq("name", sep[0]),
                        Filters.eq("teacher", sep[1])));
        boolean sign = mongodb.removeCollection(course + "_sign");
        boolean all = mongodb.removeCollection(course + "_student");
        return (sign && all && c);
    }
}
