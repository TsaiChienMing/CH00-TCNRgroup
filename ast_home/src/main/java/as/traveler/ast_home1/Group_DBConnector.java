package as.traveler.ast_home1;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class Group_DBConnector {
    //宣告類別變數以方便存取，並判斷是否連線成功
    public static int httpstate = 0;
    static String result = null;
    static String TAG = "tcnr02=>";
    //---------------------------
    static InputStream is = null;
    static String line = null;
    static int code;
    static String mysql_code = null;


    static String connect_ip ="http://";
    //--------------------------------------
    public static String executeQuery(String query_string) {

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(connect_ip + "group_connect_db_all.php");


            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            // selefunc_string -> 給php 使用的參數	query:選擇 insert:新增 update:更新 delete:刪除
            params.add(new BasicNameValuePair("selefunc_string", "query"));
            params.add(new BasicNameValuePair("query_string", query_string));
            int f = 3;
            // query_string -> 給php 使用的參數
            Log.d(TAG, "Value=" + params.toString());

            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            // ===========================================
            // 使用httpResponse的方法取得http 狀態碼設定給httpstate變數
            httpstate = httpResponse.getStatusLine().getStatusCode();
            // ===========================================
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();

            Log.d(TAG, "inputStream=" + inputStream.toString());

            BufferedReader bufReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                builder.append(line + "\n");
            }
            inputStream.close();
            result = builder.toString();
            Log.d(TAG, "result" + result);
        } catch (Exception e) {
            Log.d("TAG", "Exception e" + e.toString());
        }

        return result;
    }

    public static String executeInsert(String string, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //---- 連結MySQL-------------------
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(connect_ip + "group_connect_db_all.php");
int j = 3;
            // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
            nameValuePairs.add(new BasicNameValuePair("selefunc_string", "insert_group"));
int g = 3;

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));

            HttpResponse response = httpclient.execute(httppost);  //執行
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.d(TAG, "pass 1:" + "connection success ");
        } catch (Exception e) {
            Log.d(TAG, "Fail 1" + e.toString());
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.d(TAG, "pass 2:" + "connection success ");
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
        //--------真正新增資料-----
        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));

            if (code == 1) {
                Log.d(TAG, "pass 3:" + "Inserted Successfully");
            } else {
                Log.d(TAG, "pass 3:" + "Sorry, Try Again");
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return result;
    }

    public static String executeUpdate(String string, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        String update_code = null;
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "group_connect_db_all.php"); //

        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "update"));
//--------------------------
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost); //
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent(); // InputStream is = null;

            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
//------------------------------------------------------------------------------
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
//----------------------------------------
        try {
            JSONObject json_data = new JSONObject(result);      //result轉成JSON
            code = (json_data.getInt("code"));
            if (code == 1) {
                update_code = "updata Successfully";
            } else {
                update_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return update_code;
    }
    //更新鬧鐘
    public static String alertupdate(String s, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        String update_code = null;
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "group_connect_db_all.php"); //
        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "update_alert"));

//--------------------------
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost); //
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent(); // InputStream is = null;

            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
//------------------------------------------------------------------------------
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
//----------------------------------------
        try {
            JSONObject json_data = new JSONObject(result);      //result轉成JSON
            code = (json_data.getInt("code"));
            if (code == 1) {
                update_code = "updata Successfully";
            } else {
                update_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return update_code;
    }
    //鬧鐘狀態變更為ON
    public static String alert_StateOn(String s, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        String update_code = null;
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "group_connect_db_all.php"); //
        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "alert_stateon"));

//--------------------------
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost); //
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent(); // InputStream is = null;

            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
//------------------------------------------------------------------------------
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
//----------------------------------------
        try {
            JSONObject json_data = new JSONObject(result);      //result轉成JSON
            code = (json_data.getInt("code"));
            if (code == 1) {
                update_code = "updata Successfully";
            } else {
                update_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return update_code;
    }
//鬧鐘狀態變更為OFF
    public static String alert_StateOff(String s, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        String update_code = null;
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "group_connect_db_all.php"); //
        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "alert_stateoff"));

//--------------------------
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost); //
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent(); // InputStream is = null;

            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
//------------------------------------------------------------------------------
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
//----------------------------------------
        try {
            JSONObject json_data = new JSONObject(result);      //result轉成JSON
            code = (json_data.getInt("code"));
            if (code == 1) {
                update_code = "updata Successfully";
            } else {
                update_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return update_code;
    }
//重製鬧鐘傳至MySQL
    public static String alertdelete(String s, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        String update_code = null;
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "group_connect_db_all.php"); //
        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "delete_alert"));

//--------------------------
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost); //
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent(); // InputStream is = null;

            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
//------------------------------------------------------------------------------
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
//----------------------------------------
        try {
            JSONObject json_data = new JSONObject(result);      //result轉成JSON
            code = (json_data.getInt("code"));
            if (code == 1) {
                update_code = "alert_delete Successfully";
            } else {
                update_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return update_code;
    }

    public static String executeInsertFriends(ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //---- 連結MySQL-------------------
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(connect_ip + "group_connect_db_all.php");
            int j = 3;
            // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
            nameValuePairs.add(new BasicNameValuePair("selefunc_string", "insert_friends"));
            int g = 3;

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));

            HttpResponse response = httpclient.execute(httppost);  //執行
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.d(TAG, "pass 1:" + "connection success ");
        } catch (Exception e) {
            Log.d(TAG, "Fail 1" + e.toString());
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.d(TAG, "pass 2:" + "connection success ");
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
        //--------真正新增資料-----
        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));

            if (code == 1) {
                Log.d(TAG, "pass 3:" + "Inserted Successfully");
            } else {
                Log.d(TAG, "pass 3:" + "Sorry, Try Again");
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return result;
    }
//更新集合點
    public static String pointsupdate(String s, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        String update_code = null;
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "group_connect_db_all.php"); //
        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "update_points"));

//--------------------------
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost); //
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent(); // InputStream is = null;

            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
//------------------------------------------------------------------------------
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
//----------------------------------------
        try {
            JSONObject json_data = new JSONObject(result);      //result轉成JSON
            code = (json_data.getInt("code"));
            if (code == 1) {
                update_code = "updata Successfully";
            } else {
                update_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return update_code;
    }

    public static String groupdelete(String s, ArrayList<NameValuePair> nameValuePairs) {
        is = null;
        result = null;
        line = null;
        mysql_code = null;
        Log.d(TAG, "value=" + nameValuePairs);
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
//--------------------------------------------------------------------------------------
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(connect_ip + "group_connect_db_all.php");

        // selefunc_string -> 給php 使用的參數 query:選擇 insert:新增 update:更新 delete:刪除
        nameValuePairs.add(new BasicNameValuePair("selefunc_string", "group_delete"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
                    HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            HttpResponse response;
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            try {
                is = entity.getContent();
            } catch (IllegalStateException e1) {
                e1.printStackTrace();
            } catch (ClientProtocolException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        Log.d(TAG, "pass 1:" + "connection success");
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "utf8"), 8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.d(TAG, "pass 2:" + "connection success");
        } catch (Exception e) {
            Log.d(TAG, "Fail 2:" + e.toString());
        }
        try {
            JSONObject json_data = new JSONObject(result);
            code = (json_data.getInt("code"));
            if (code == 1) {
                mysql_code = "delete Successfully";
            } else {
                mysql_code = "Sorry,Try Again";
            }
        } catch (Exception e) {
            Log.d(TAG, "Fail 3:" + e.toString());
        }
        return mysql_code;
    }
}