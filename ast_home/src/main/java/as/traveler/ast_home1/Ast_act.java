package as.traveler.ast_home1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

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

public class Ast_act extends AppCompatActivity implements View.OnClickListener {

    private Spinner act_s001, act_s002;
    private Button act_b001,act_cancelbtn;
    private Uri uri;
    private Intent it;
    private String locationSelected, selected;
    private String url;
    private BottomNavigationView ast_Bottom;
    private Intent intent01= new Intent();//使用intent方法2,記得要宣告new
    private Handler handler = new Handler();
    private GridView act_gridView;
    private HashMap<String, Object> item;
    private JSONArray jsonArray;
    private PlaceDbHelper dbHper;
    private static final String DB_FILE = "friends.db";
    private static final String DB_TABLE = "place";
    private static final int DBversion = 1;
    private ArrayList<String> recSet,listSet,searchSet;
    private List<Map<String,Object>> mList = new ArrayList<>();; //建立一個可以新增內容的Array
    private ProgressBar act_progressbar;
    private Handler mHandler = new Handler();
    private TextView act_contentdes;
    private int count = 0;
    private String get_sch_id,get_day;
    private static int listmode=1;//預設值1 trip沒有傳值
    private EditText act_editText;
    private MenuItem act_update;
    private static int searchmode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableStrictMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_act);
        setupViewcomponent();

        //-----------建立BottomNavigationView物件
        ast_Bottom = (BottomNavigationView) findViewById(R.id.ast_Bottom);
        if(listmode==1){
            ast_Bottom.setVisibility(View.VISIBLE);
        }else{
            ast_Bottom.setVisibility(View.GONE);
        }
        ast_Bottom.setSelectedItemId(R.id.ast_act);
        BottomNavigationHelper.removeShiftMode(ast_Bottom);  // 生一個外部的class
        ast_Bottom.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ast_Bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ast_act:
                        Ast_act.this.finish();
                        return true;
                    case R.id.ast_trip:
                        startActivity(new Intent(getApplicationContext(), Ast_trip.class));
                        Ast_act.this.finish();//避免堆疊，本頁結束
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_group:
                        startActivity(new Intent(getApplicationContext(), Ast_Group.class));
                        Ast_act.this.finish();//避免堆疊，本頁結束
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_col:
                        startActivity(new Intent(getApplicationContext(), Ast_Col_File.class));
                        Ast_act.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.ast_more:
                        startActivity(new Intent(getApplicationContext(), Ast_More.class));
                        Ast_act.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    public static void enableStrictMode(Context context) {
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .penaltyLog()
                        .build());
    }

    private void setupViewcomponent() {
        act_s001 = (Spinner) findViewById(R.id.act_placeselect);
        act_s002 = (Spinner) findViewById(R.id.act_chgselect);
        act_editText = (EditText) findViewById(R.id.act_editText);
        act_b001 = (Button) findViewById(R.id.act_searchbtn);
        act_cancelbtn = (Button) findViewById(R.id.act_cancelbtn);
        act_progressbar = (ProgressBar) findViewById(R.id.act_progressBar);
        act_contentdes = (TextView) findViewById(R.id.act_contentdes);
        act_progressbar.setVisibility(View.INVISIBLE);//預設關閉進度條
        act_gridView = (GridView) findViewById(R.id.act_gridview);
        //--------連接SQLite------
        initDB();//注意順序跟位置，真的很重要
        //--------------顯示listview--------------
        if(dbHper.RecCount()!=0){//資料庫有資料的話
            if(searchmode==0){
                recSet = listSet;
                act_cancelbtn.setVisibility(View.GONE);
            }else{
                recSet = searchSet;
                act_cancelbtn.setVisibility(View.VISIBLE);
            }
            showdata();//抓取資料庫顯示資料
        }else{//資料庫沒資料就抓取opendata存入資料庫
            readOpenData();
        }
        //----------------------------------------------
        Intent intent = this.getIntent();
        get_sch_id = intent.getStringExtra("sch_id");
        get_day = intent.getStringExtra("day");
        if(get_sch_id!=null){
            listmode=0;//trip有傳值過來的話
            act_contentdes.setText(getString(R.string.act_t004));
            act_s001.setVisibility(View.GONE);//TODO 想改成限定區域範圍，難度有點高，先放著
            act_s002.setVisibility(View.GONE);
            act_editText.setVisibility(View.VISIBLE);
            this.setTitle(get_sch_id+":"+get_day);

        }else{
            listmode=1;//沒有傳值
            act_contentdes.setText(getString(R.string.act_t003));
            act_s001.setVisibility(View.VISIBLE);
            act_s002.setVisibility(View.VISIBLE);
            act_editText.setVisibility(View.GONE);
            this.setTitle(getString(R.string.item_title_search));//設定title

        }
        //----------------------------------------------
        act_s001.setOnItemSelectedListener(slcS001);
        act_s002.setOnItemSelectedListener(slcS002);
        act_b001.setOnClickListener(this);
        act_cancelbtn.setOnClickListener(this);
    }
//創一個方法 把讀取opendata的設定丟來這裡，之後更新資料也要呼叫，可以節省些空間
    private void readOpenData() {
        act_contentdes.setText(getString(R.string.act_download));
        act_progressbar.setVisibility(View.VISIBLE);//設定進度條可見
        Act_SQLReadThread writethread = new Act_SQLReadThread(dbHper,this);
        //挖個坑，可以呼叫執行緒的方法並且傳遞引數過去，
        // dbHper可以讓執行緒開啟SQLite，activity的this可以讓執行緒透過我設定的tHandler丟回傳值
        //回傳值接收在下面handleMessage方法
        writethread.setHandler(mHandler);//設定一個工人去執行緒跑進度條
        writethread.setProgressBar(act_progressbar);//傳遞進度條去執行緒
        writethread.start();//這個一定要有
    }

    //修改為確認更新內容使用 目前用不到
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            try{
                String Task_opendata
                        = new TransTask().execute("https://").get();
                jsonArray = new JSONArray(Task_opendata);
                if(dbHper.RecCount()==jsonArray.length()){
                    //假如資料庫資料筆數 和 JSON資料筆數一致的話，就不更新內容，跳TOAST
                    Toast.makeText(getApplicationContext(), getString(R.string.update_message_cancel), Toast.LENGTH_LONG).show();
                }else{
                    //反之就清空資料庫重新寫入
                    dbHper.clearRec();
                    readOpenData();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    };

    Handler tHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    act_contentdes.setText(getString(R.string.act_progress));
                    break;
                case 101:
                    //執行緒跑完進度條之後，回傳值通知主程式，資料OK了，要重刷頁面才能顯示資料
                    setupViewcomponent();
                    break;
                case 201:
                    Toast.makeText(getApplicationContext(), getString(R.string.checkINTERNET), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void showdata() {
        act_progressbar.setVisibility(View.INVISIBLE);
        act_contentdes.setText(getString(R.string.act_t003));
        for(int i=0;i<recSet.size();i++){
            item = new HashMap<String, Object>();// TODO 要注意物件new在for迴圈內, 不然會出錯變重複
            String[] fld = recSet.get(i).split("##");
            item.put("name", fld[1]);
            item.put("area", fld[5]);
            mList.add(item);
        }
        //==========設定listView============
        //自定義的adapter
        MyAdapter adapter = new MyAdapter(
                getApplicationContext(),
                mList,
                R.layout.ast_act_list,
                new String[]{"name","area"},
                new int[]{R.id.act_gv_name,R.id.act_gv_area}
        );
        act_gridView.setAdapter(adapter);//將抓取的資料設定到表格視窗
        act_gridView.setOnItemClickListener(onClickGridView);//建立表格視窗按鈕監聽
    }

    //縣市選擇spinner的事件
    Spinner.OnItemSelectedListener slcS001 = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //取得選擇內容轉換字串
            locationSelected = parent.getSelectedItem().toString();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
    //搜尋標的spinner的事件
    Spinner.OnItemSelectedListener slcS002 = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selected = parent.getSelectedItem().toString();
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.act_searchbtn:
                if(listmode==1){
                    //跳出至googleMap的搜尋按鈕的事件
                    //注意Manifest要加    <uses-permission android:name="android.permission.INTERNET"/>
                    uri = Uri.parse("geo:0,0?q=" + locationSelected + "+" + selected);
                    it = new Intent(Intent.ACTION_VIEW, uri);
                    it.setPackage("com.google.android.apps.maps");//直接指定用google map開啟
                    startActivity(it);
                }else{
                    //TODO
                    //搜尋條件的新列表
                    searchSet = dbHper.getRecSet_search(act_editText.getText().toString().trim());
                    searchmode = 1;
                    mList.clear();//之前列表的內容要清空，不然會顯示錯誤
                    setupViewcomponent();
                }
                break;
            case R.id.act_cancelbtn:
                searchmode = 0;
                mList.clear();//之前搜尋的內容要清空，不然會顯示錯誤
                setupViewcomponent();
                break;
        }
    }

    private void initDB() {//連接SQLite
        if (dbHper == null)//沒有連線的話，就開啟連線
            dbHper = new PlaceDbHelper(this, DB_FILE, null, DBversion);
        listSet = dbHper.getRecSet();
    }

    //====================================================
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

    private final GridView.OnItemClickListener onClickGridView = new GridView.OnItemClickListener() {
        //建立表格視窗按鈕監聽方法
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(listmode==1){
                intent01.setClass(Ast_act.this, Ast_Point.class)
                    .putExtra("uid", dbHper.FindUid(position+1));
                //傳遞內容
                startActivity(intent01);
                //注意傳過去的index從0開始，但是id從1開始算，因此要加1，不然第一筆沒資料
            }else{
                //TODO

                UPDATE_detail.attr_update(get_sch_id,get_day,dbHper.FindUid(position+1));
               Ast_act.this.finish();
            }
        }
    };

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.onBackPressed), Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ast_act_menu, menu);
        act_update = menu.findItem(R.id.act_update);
        if(listmode==1){
            act_update.setVisible(true);
        }else{
            act_update.setVisible(false);//手動更新要關起來，不然會直接閃退，一定不是我懶的debug
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                if(listmode==0) //在選行程景點模式下
                    setResult(RESULT_CANCELED);//返回鍵回傳值為取消
                Ast_act.this.finish();
                break;
            case R.id.act_update:
                new AlertDialog.Builder(this)//這裡要用this
                        .setTitle(R.string.act_update)
                        .setMessage(R.string.update_message)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.btn_star_big_on)
                        .setNeutralButton(R.string.update_force, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //不說廢話直接清空資料庫，然後重讀
                                dbHper.clearRec();
                                readOpenData();
                            }
                        })
                        .setPositiveButton(R.string.update_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
//                        .setNegativeButton(R.string.update_check, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //設定一秒之後跑執行緒檢查資料庫內容
//                                handler.postDelayed(updateTimer, 1000);
//                            }
//                        })
                        .show();
                break;
            case R.id.act_about:
                new AlertDialog.Builder(this)//這裡要用this
                        .setTitle(R.string.act_about)
                        .setMessage(R.string.about_message)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.btn_star_big_on)
                        .setPositiveButton(R.string.about_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setNegativeButton(R.string.about_site, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse("http://");
                                Intent it = new Intent(Intent.ACTION_VIEW,uri);
                                startActivity(it);
                            }
                        })
                        .setNeutralButton(R.string.about_link, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse("https://");
                                Intent it = new Intent(Intent.ACTION_VIEW,uri);
                                startActivity(it);
                            }
                        })
                        .show();
                break;
        }
        return true;
    }

    //自定義的Adapter，用意在於把Picasso的library帶入，否則picasso無法使用於listview
    public class MyAdapter extends SimpleAdapter{

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int     resource, String[] from, int[] to){
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            // here you let SimpleAdapter built the view normally.
            View v = super.getView(position, convertView, parent);

            Context context = parent.getContext();
            // Then we get reference for Picasso
            ImageView img = (ImageView) v.getTag();
            if(img == null){
                img = (ImageView) v.findViewById(R.id.act_gv_img);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                v.setTag(img); // <<< THIS LINE !!!!

            }
            // get the url from the data you passed to the `Map`
//            String url = ((Map)getItem(position)).get(TAG_IMAGE);
//            String url = "https://live.staticflickr.com/65535/50173649227_87ba6d8ce4_q.jpg";
            String[] fld = recSet.get(position).split("##");
            String url = fld[10];
            // do Picasso
            // maybe you could do that by using many ways to start

            int imageWidth =300;//設定長寬
            Picasso.get().load(url).resize(imageWidth, imageWidth).into(img);

            // return the view
            return v;
        }
    }
}

