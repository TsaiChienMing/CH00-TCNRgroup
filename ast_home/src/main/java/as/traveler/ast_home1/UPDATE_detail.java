package as.traveler.ast_home1;

import android.content.ContentValues;
import android.net.Uri;
import android.widget.Toast;

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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import as.traveler.ast_home1.providers.DetailContentProvider;

class UPDATE_detail {
     static InputStream is;
    static String result;
     static String line;
     static String connect_ip ="http://";
     static int code;
     public static void attr_update(String sch_id, String days, String uid){
         try {
             String result = DetailDBConnector.executeQuery(" SELECT *  FROM  detail WHERE  `sch_id` = " + " '" + sch_id + "' " + " AND `day` = " + "'" + days + "'");  //搜尋登入者帳號的行程
             //有資料insertattr
             if(result.equals("0 results\n")){
            //沒資料updateattr
                 ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                 nameValuePairs.add(new BasicNameValuePair("sch_id", sch_id));
                 nameValuePairs.add(new BasicNameValuePair("day", days));
                 nameValuePairs.add(new BasicNameValuePair("att_id", uid+","));
                 try {
                     Thread.sleep(500); //  延遲Thread 睡眠0.5秒
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
//-----------------------------------------------
                 String result_insert = DetailDBConnector.executeInsert("SELECT * FROM schedule", nameValuePairs);

             }else{
            //把讀下來的資料分解
                 JSONArray jsonArray = new JSONArray(result);
                     JSONObject jsonData = jsonArray.getJSONObject(0);
                    String attrString=jsonData.getString("att_id");

                     attrString+=uid+",";

                     ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                     nameValuePairs.add(new BasicNameValuePair("sch_id",sch_id));
                     nameValuePairs.add(new BasicNameValuePair("day", days));
                     nameValuePairs.add(new BasicNameValuePair("att_id",attrString ));
                     DetailDBConnector.executeattrUpdate("Update attr_id From detail ", nameValuePairs);


             }
         }catch(Exception e){

         }
//==========================================

     }

}
