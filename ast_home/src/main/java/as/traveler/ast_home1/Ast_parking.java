package as.traveler.ast_home1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Ast_parking extends AppCompatActivity{

    private ListView listView;
    private TableRow tab01;
    private JSONArray jsonArray;
    private String check_t=" ";
    private JSONObject jsonData;
    private HashMap<String, Object> item;
    private String Range,Charge,ChargePeriod,Memo;
    private Handler handler = new Handler();
    private SearchView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_parking);
        setupViewComponent();
    }

    private void setupViewComponent() {
        //設定class標題
        this.setTitle(getString(R.string.item_title_parking));//設定title
        //----------------------------------------------
        //根據裝置尺寸動態調整高度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int newscrollheight = displayMetrics.heightPixels * 70 / 100; // 設定ScrollView使用尺寸的4/5

        listView = (ListView) findViewById(R.id.listView1);
        listView.getLayoutParams().height = newscrollheight;
        listView.setLayoutParams(listView.getLayoutParams()); // 重定ScrollView大小
        listView.setTextFilterEnabled(true);//設為可被過濾
        //XML直接從網路上下載，網路操作一定要在新的執行序


        tab01 =(TableRow)findViewById(R.id.ast_parking_tab01);//Table

        search = (SearchView)findViewById(R.id.ast_parking_s001);//搜尋欄
        search.setIconifiedByDefault(false);//設定searchview縮小為圖示
        search.setOnQueryTextListener(searchOn);//設定監聽
        search.setSubmitButtonEnabled(true);
        search.setQueryHint(getString(R.string.ast_parking_hint));

        //------------------------------------------------------------------------------------------------------------------------------------
        handler.postDelayed(updateTimer, 1000);
    }

    private  SearchView.OnQueryTextListener searchOn = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {

            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (TextUtils.isEmpty(newText))
            {
                listView.clearTextFilter();
            }else {
                listView.setFilterText(newText);
            }
            return true;
        }
    };

    private Runnable updateTimer = new Runnable(){

        @Override
        public void run() {
            try {
                String Task_Opendata
                        = new TransTask().execute("https://quality.data.gov.tw/dq_download_json.php?nid=127805&md5_url=4f3c762e8e1894c99a7452102c4fe558").get();
//                {
//                         "Name": "光復路",
//                        "Range": "市府路~三民路",
//                        "Charge": "小型車每小時20元",
//                        "ChargePeriod": "每日08:00~18:00",
//                        "Memo": "大型車加倍"
//                }
                List<Map<String,Object>> mList;
                mList = new ArrayList<>();
                //解析json   json包含array  array中含object
                JSONArray jsonArray = new JSONArray(Task_Opendata);
                int xx= jsonArray.length();//讀到幾筆資料

                //開始逐筆轉換
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    Map<String, Object> item = new HashMap<String, Object>();
                    String Range = jsonData.getString("Range");
                    String Charge = jsonData.getString("Charge");
                    String ChargePeriod = jsonData.getString("ChargePeriod");
                    String Memo = jsonData.getString("Memo");

                    item.put("Range", Range);
                    item.put("Charge", Charge);
                    item.put("ChargePeriod", ChargePeriod);
                    item.put("Memo", Memo);
                    mList.add(item);
                }


                //===============設定ListView====================
                SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), mList, R.layout.list,
                        new String[]{"Range","Charge","ChargePeriod","Memo"},
                        new int[]{R.id.t001,R.id.t002,R.id.t003,R.id.t004});
                listView.setAdapter(adapter);
            }catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

    };

    //將JSON中資料一個一個抓下來放進新的Array中
    private JSONArray sortJsonArray(JSONArray jsonArray) {
        //Range自定義的排序Method
        final  ArrayList<JSONObject> json = new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){  //將資料存入ArrayList json中
            try {
                json.add(jsonArray.getJSONObject(i));
            }catch (JSONException jsone){
                jsone.printStackTrace();
            }
        }
        //------------------------------------------------
        Collections.sort(json, new Comparator<JSONObject>(){

            @Override
            public int compare(JSONObject jsonOb1, JSONObject jsonOb2) {
                //用多重key排序
                String lidRange = "", ridRange = "";                     //  L是排序前 R是排序後
//                String lidMemo="",ridMemo="";
//                String lidPM25="",ridPM25="";
                try {
                    lidRange = jsonOb1.getString("Range");
                    ridRange = jsonOb2.getString("Range");
//                    lidMemo = jsonOb1.getString("Memo");
//                    ridMemo = jsonOb2.getString("Memo");
//                    整數判斷方法
//                    if(!jsonOb1.getString("PM2.5").isEmpty()&&!jsonOb2.getString("PM2.5").isEmpty()
//                            &&!jsonOb1.getString("PM2.5").equals("ND")&&!jsonOb2.getString("PM2.5").equals("ND")){
//                        lidPM25=String.format("%02d",Integer.parseInt(jsonOb1.getString("PM2.5")));
//                        ridPM25=String.format("%02d",Integer.parseInt(jsonOb2.getString("PM2.5")));
//                    }else{
//                        lidPM25="0";
//                        ridPM25="0";
//                    }
                }catch (JSONException jsone){
                    jsone.printStackTrace();  }
                return lidRange.compareTo(ridRange);
            }
        });
        return new JSONArray(json);
    }

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

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateTimer);//關閉這個APP後台運行
        this.finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(updateTimer, 1000);//返回的時候更新時間設為1秒
    }

    //====================================================
    //打開一個網址並把該網址資料一行一行全部讀進來，
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.onBackPressed), Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                Ast_parking.this.finish();
                break;
        }
        return true;
    }
}

