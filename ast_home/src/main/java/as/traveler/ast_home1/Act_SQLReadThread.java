package as.traveler.ast_home1;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Act_SQLReadThread extends Thread {
    private final Ast_act activity;
    private PlaceDbHelper dbHper;
    private Handler mHandler;
    private ProgressBar mProBar;
    private JSONArray jsonArray;
    private JSONObject jsonData;
    private String site,address,brief,area,longitude,latitude,phone,uid,imageurl,thumburl;
    private int count = 0;
    private ContentResolver mContRes;
    private String result;

    public Act_SQLReadThread(PlaceDbHelper dbHper, Ast_act activity) {
        this.dbHper = dbHper;
        this.activity = activity;
    }

    @Override
    public void run() {
        super.run();
        try{
            mProBar.setSecondaryProgress(30);
            URL url =new URL("http://as-traveler.com/");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(3000);
            urlc.connect();
            if(urlc.getResponseCode() ==200) {
                result = Act_DBConnector.executeQuery("SELECT * FROM `attraction`");
                mProBar.setSecondaryProgress(60);
            }else{
                activity.tHandler.sendEmptyMessage(201);
            }
            //解析JSON,裡面只有JSON Array跟JSON Object
            jsonArray = new JSONArray(result);
            mProBar.setSecondaryProgress(100);
            
            activity.tHandler.sendEmptyMessage(1);
            //-----開始逐筆轉換
            for(int i=0;i<jsonArray.length();i++){
                jsonData = jsonArray.getJSONObject(i);
                uid = jsonData.getString("uid");
                imageurl = jsonData.getString("imageurl");
                thumburl = jsonData.getString("thumburl");
                site = jsonData.getString("name");
                brief = jsonData.getString("brief");
                area = jsonData.getString("area");
                address = jsonData.getString("address");
                latitude = jsonData.getString("lat");
                longitude = jsonData.getString("lng");
                phone = jsonData.getString("phone");
                //------資料傳入SQLite----------------
                long rowID = dbHper.insertRec(site, latitude, longitude, brief, area, address, phone, uid, imageurl, thumburl);

//                mContRes = activity.getContentResolver();
//                ContentValues newRow = new ContentValues();
//                newRow.put("name", site);
//                newRow.put("lat", latitude);
//                newRow.put("lng", longitude);
//                newRow.put("brief", brief);
//                newRow.put("area", area);
//                newRow.put("address", address);
//                newRow.put("phone", phone);
//                newRow.put("uid", uid);
//                newRow.put("imageurl", imageurl);
//                newRow.put("thumburl", thumburl);
//                mContRes.insert(AttractionContentProvider.CONTENT_URI, newRow);

                //=========跑讀取條的關鍵在這裡==========
                count++;
                mHandler.post(new Runnable() {
                    public void run() {
                        mProBar.setProgress(count*100/jsonArray.length());
                        //每次讀取資料count++，乘以100除以全部資料量，最後跑完接近100
                    }
                });
                //=========跑讀取條的內容就這麼少=========
            }
            activity.tHandler.sendEmptyMessage(101);//跑完傳遞一個值回主程式，通知刷新頁面
        }catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
    //跑讀取條記得要傳一個工人來幫忙
    public void setHandler(Handler h) {
        mHandler = h;
    }
    //跑讀取條記得要傳讀取條過來
    public void setProgressBar(ProgressBar bar01) {
        mProBar = bar01;
    }

    //開這個class副程式=開一個網址，把資料一行一行讀進sb
    private class TransTask extends AsyncTask<String, Void, String> {
        String ans;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String line = in.readLine();
                while (line != null) {
                    Log.d("HTTP", line);
                    sb.append(line);
                    line = in.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ans = sb.toString();
            //------------
            return ans;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("s", "s:" + s);
            parseJson(s);
        }

        private void parseJson(String s) {
        }
    }
}
