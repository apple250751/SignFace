package com.kuma.facesignteacher.Global;

import com.kuma.facesignteacher.DB.MongoDB;
import com.kuma.facesignteacher.Facepp.MyFacepp;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by kuma on 2017/12/27.
 */

public class Global {

    private static final String FACEPP_KEY = "KTVmukrFlH_tJ_xVOO_A7qy_E1JdbbsY";
    private static final String FACEPP_SECRET = "M1lMbgUpe2aa5cQJEGQLooGPT76RkCMO";

    //用於連接mongodb的相關信息
    private static final String MONGO_DB_IP = "172.20.10.12";
    private static final int MONGO_DB_PORT = 27017;
    private static final String MONGO_DB_USER = "";
    private static final String MONGO_DB_PASSWD = "";

    private static final String MMONGO_DB_NAME_COURSE = "course";

    public static String getMmongoDbNameCourse() {
        return MMONGO_DB_NAME_COURSE;
    }

    public static String getFaceppKey() {
        return FACEPP_KEY;
    }

    public static String getFaceppSecret() {
        return FACEPP_SECRET;
    }


    public static String getMONGO_DB_IP() {
        return MONGO_DB_IP;
    }

    public static int getMONGO_DB_PORT() {
        return MONGO_DB_PORT;
    }

    public static String getMONGO_DB_USER() {
        return MONGO_DB_USER;
    }

    public static String getMONGO_DB_PASSWD() {
        return MONGO_DB_PASSWD;
    }

    //獲取當前時間
    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String time = String.valueOf(String.valueOf(calendar.get(Calendar.YEAR)) + "-"
                + String.valueOf(calendar.get(Calendar.MONTH)) + "-"
                + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":"
                + String.valueOf(calendar.get(Calendar.MINUTE)) + ":"
                + String.valueOf(calendar.get(Calendar.SECOND)));
        return time;
    }
}
