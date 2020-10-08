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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Ast_Hotel extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ListView hotelListView;
    private Handler handler =new Handler();
    private SearchView hotel_search;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_hotel);
        setupViewComponent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(updateTimer,2000);
    }

    private void setupViewComponent() {
        //==============找飯店==============//
        //根據裝置尺寸動態調整高度
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int newscrollheight = displayMetrics.heightPixels * 90 / 100; // 設定ScrollView使用尺寸的4/5

        hotelListView = (ListView) findViewById(R.id.hotelListView);
        hotelListView.getLayoutParams().height = newscrollheight;
        hotelListView.setLayoutParams(hotelListView.getLayoutParams()); // 重定ScrollView大小

        hotel_search=(SearchView)findViewById(R.id.hotel_search);

        hotelListView.setTextFilterEnabled(true);
        hotel_search.setIconifiedByDefault(false);
        hotel_search.setOnQueryTextListener(this);
        hotel_search.setSubmitButtonEnabled(true);
        hotel_search.setQueryHint("搜尋飯店");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (TextUtils.isEmpty(newText)) {
            hotelListView.clearTextFilter();
        } else {
            hotelListView.setFilterText(newText);
        }
        return true;
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
            //------------//
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

    private JSONArray sortJsonArray(JSONArray jsonArray) {
        //County自定義的排序Method
        final ArrayList<JSONObject> json = new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){  //將資料存入ArrayList json中
            try {
                json.add(jsonArray.getJSONObject(i));
            }catch (JSONException jsone){
                jsone.printStackTrace();
            }
        }
        return new JSONArray(json);
    }

    private Runnable updateTimer = new Runnable() {

        @Override
        public void run() {
            //XML直接從網路上下載，網路操作一定要在新的執行序
            try {
                String Task_Opendata
                        = new TransTask().execute("https://datacenter.taichung.gov.tw/swagger/OpenData/5776de16-8814-4361-b7f3-ea1862a61103").get();
                List<Map<String,Object>> mList;
                mList = new ArrayList<>();
                //解析json   json包含array  array中含object
                JSONArray jsonArray = new JSONArray(Task_Opendata);
                int xx= jsonArray.length();//讀到幾筆資料
                int yy=1;
                //-----------json排序------------------------
                jsonArray=sortJsonArray( jsonArray);
                int aa= jsonArray.length();

                //開始逐筆轉換
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    Map<String, Object> item = new HashMap<String, Object>();
                    String site = jsonData.getString("旅館名稱");
                    String address = jsonData.getString("地址");

                    item.put("旅館名稱", site);
                    item.put("地址", address);
                    mList.add(item);
                }
                //===============設定ListView====================
                SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), mList, R.layout.hotel_list,
                        new String[]{"旅館名稱","地址"},
                        new int[]{R.id.site,R.id.address});
                hotelListView.setAdapter(adapter);
                int a=0;
            }catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
//runnable方法裡加這個，會不停重複run，造成無限迴圈，只有開頭run一次的，就別加
//            handler.postDelayed(updateTimer,5000 );
        }
    };

    //--------------------------SQLite--------------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            //menu
            case R.id.action_settings:
                Ast_Hotel.this.finish();
                break;
        }
        return true;
    }
}
