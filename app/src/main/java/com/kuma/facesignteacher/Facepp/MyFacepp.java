package com.kuma.facesignteacher.Facepp;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.kuma.facesignteacher.Global.Global;
import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.FaceSetOperate;
import com.megvii.cloud.http.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;


/**
 * Created by kuma on 2017/12/27.
 */

public class MyFacepp {

    private static final String TAG = "MYFACEPP";
    private static CommonOperate commonOperate;
    private static FaceSetOperate FaceSet;

    static
    {
        if(commonOperate == null) {
            commonOperate = new CommonOperate(Global.getFaceppKey(), Global.getFaceppSecret(), false);
            Log.e(TAG, "connect commonOperate");
        }
        if(FaceSet == null) {
            FaceSet = new FaceSetOperate(Global.getFaceppKey(), Global.getFaceppSecret(), false);
            Log.e(TAG, "connect FaceSet");
        }
    }

    public String GetFaceTokenByByte(Bitmap bitmap)
    {
        try
        {
            Response response = commonOperate.detectByte(bitmap2Byte(bitmap, 50), 0, null);
            String faceToken = getFaceToken(response);
            Log.e("getFaceToken", faceToken);
            return faceToken;
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String GetFaceTokenBase64(Bitmap bitmap)
    {
        try
        {
            String base64 = Base64.encodeToString(bitmap2Byte(bitmap, 50), Base64.NO_WRAP);
            Response response = commonOperate.detectBase64(base64, 0, null);
            String faceToken = getFaceToken(response);
            Log.e("getFaceToken", faceToken);
            return faceToken;
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private String getFaceToken( Response response) throws JSONException {
        if(response.getStatus() != 200){
            return new String(response.getContent());
        }
        String res = new String(response.getContent());
        Log.e("getFaceToken", res);
        JSONObject json = new JSONObject(res);
        String faceToken = json.optJSONArray("faces").optJSONObject(0).optString("face_token");
        return faceToken;
    }

    private byte[] bitmap2Byte(Bitmap bitmap){
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, o);
        return o.toByteArray();
    }

    private byte[] bitmap2Byte(Bitmap bitmap, int quality){
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, o);
        return o.toByteArray();
    }


    public String createFaceSet(String set_name, String tokens){
        try{
            Response response =  FaceSet.createFaceSet(set_name,set_name,set_name,tokens,null, 1);
            String res = new String(response.getContent());
            Log.e("createFaceSet", res);
            if(res.contains("error_message"))
                return null;
            else
                return res;
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public Response addFace(String face_token, String set_name)
    {
        try{
            Response response = FaceSet.addFaceByOuterId(face_token,set_name);
            String res = new String(response.getContent());
            Log.e("addFace", res);
            if(res.contains("error_message"))
                return null;
            else
                return response;
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String searchFace(String face_token, String set_name)
    {
        try{
            Response response = commonOperate.searchByOuterId(face_token,
                    null,
                    null,
                    set_name,
                    1);
            if(response.getStatus() != 200){
                return null;
            }
            String res = new String(response.getContent());
            Log.e("searchFace", res);
            //最相似的臉
            JSONObject json = new JSONObject(res);
            String faceToken = json.optJSONArray("results")
                    .optJSONObject(0)
                    .optString("face_token");
            //相似度
            double confidence = json.optJSONArray("results")
                    .optJSONObject(0)
                    .getDouble("confidence");

            if(confidence < 60)
                return "<bad>";
            else
                return faceToken;
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public Response removeFace(String face_token, String set_name)
    {
        try{
            Response response = FaceSet.removeFaceFromFaceSetByOuterId(set_name,face_token);
            String res = new String(response.getContent());
            Log.e("removeFace", res);
            return response;
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public Response deleteFaceSet(String set_name)
    {
        try{
            Response response = FaceSet.deleteFaceSetByOuterId(set_name, 0);
            String res = new String(response.getContent());
            Log.e("deleteFaceSet", res);
            if(res.contains("error_message"))
                return null;
            else
                return response;
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
