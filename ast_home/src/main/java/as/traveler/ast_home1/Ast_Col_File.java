package as.traveler.ast_home1;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

public class Ast_Col_File extends AppCompatActivity implements OnQueryTextListener{

    private static final String DB_FILE = "friends.db";
    private static final int DBversion = 1;
    private TextView file_filename,col_addbutton;
    private Intent it = new Intent();
    private GridView file_gridView;
    private Intent intent01= new Intent();
    private List<Map<String, Object>> mList;
    private ArrayList<String> recSet_col,recSet_colCheckAll;
    private HashMap<String, Object> item;
    private PlaceDbHelper dbHper;
    private Handler handler = new Handler();
    private Handler dhandler = new Handler();
    private JSONArray jsonArray;
    private JSONObject jsonData;
    private BottomNavigationView ast_Bottom;
    private static int loginon;
    private static String loginemail;
    private ProgressBar col_progressBar;
    private String email,att_img,att_name,att_brief,att_uid;
    private int checkdownloadflag =0;
    private ArrayList<NameValuePair> nameValuePairs;

    //-------------檢查登入------------------------
    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        loginon = login.getInt("flag", 0); //0 是未登入，1是有登入
        loginemail=login.getString("Email", "抓不到");
    }
    //==========create====================
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        enableStrictMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_col_file);
        u_loaddata();
        if (loginon != 1) {
            Toast.makeText(getApplicationContext(), "請先登入", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), Ast_Login.class));
            this.finish();
        }
        setupViewComponent();

        //-----------建立BottomNavigationView物件
        ast_Bottom = (BottomNavigationView) findViewById(R.id.ast_Bottom);

        ast_Bottom.setSelectedItemId(R.id.ast_col);
        BottomNavigationHelper.removeShiftMode(ast_Bottom);  // 生一個外部的class
        ast_Bottom.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ast_Bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ast_act:
//                        startActivity(new Intent(getApplicationContext(), Ast_Home.class));
                        //因為本來就是從首頁轉到這頁，所以直接finish，就能回到首頁，避免堆疊
                        Ast_Col_File.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.ast_trip:
                        startActivity(new Intent(getApplicationContext(), Ast_trip.class));
                        Ast_Col_File.this.finish();//避免堆疊，本頁結束
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_group:
                        startActivity(new Intent(getApplicationContext(), Ast_Group.class));
                        Ast_Col_File.this.finish();//避免堆疊，本頁結束
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_col:

                        return true;
                    case R.id.ast_more:
                        startActivity(new Intent(getApplicationContext(), Ast_More.class));
                        Ast_Col_File.this.finish();
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

    //==========normal====================
    private void setupViewComponent() {
        this.setTitle(getString(R.string.item_title_collection));//設定title
        //----------景點收藏頁物件----------
        file_filename = (TextView) findViewById(R.id.col_file_filename);
        file_gridView = (GridView) findViewById(R.id.file_gridview2);
        col_progressBar = (ProgressBar) findViewById(R.id.col_progressBar);
//        col_search =(SearchView)findViewById(R.id.col_search);
        col_addbutton = (TextView) findViewById(R.id.col_addbutton);
        col_addbutton.setVisibility(View.VISIBLE);
        //----------SQLite--------------------
        initDB();//呼叫SQLite
        if (dbHper.RecCount() != 0) {//景點資料庫有資料的話
            if(checkdownloadflag==0 && loginon==1){
                //跑MySQL檢查資料筆數同步，必須要登入
                col_progressBar.setVisibility(View.VISIBLE);
                col_addbutton.setText(getString(R.string.col_downloadSQL));
                handler.postDelayed(downloadDB, 500);
            }
            showdata();//抓取資料庫顯示資料
        } else {//資料庫沒資料就抓取opendata存入資料庫
//            file_filename.setText(getString(R.string.col_list_filename));
            Toast.makeText(getApplicationContext(), "資料庫沒有資料，跳轉頁面下載", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();//因為沒有資料，先跳去景點頁面下載
            intent.setClass(getApplicationContext(), Ast_act.class);
            startActivity(intent);
            this.finish();
        }
        if(recSet_col.size()>=1){//下載後這個email帳號有資料
            col_addbutton.setVisibility(View.GONE);//讓新增按鈕消失
        }
//        file_gridView.setTextFilterEnabled(true);
//        col_search = (SearchView)
//                findViewById(R.id.col_search);
//        col_search.setIconifiedByDefault(false);//查詢功能拔掉
//        col_search.setOnQueryTextListener(this);
//        col_search.setSubmitButtonEnabled(true);
//        col_search.setQueryHint("查詢");
    }

    private void initDB() {
        if (dbHper == null)//沒有連線的話，就開啟連線
            dbHper = new PlaceDbHelper(this, DB_FILE, null, DBversion);
        recSet_col = dbHper.getRecSet_col(loginemail); //抓取收藏的table
        recSet_colCheckAll = dbHper.getRecSet_colCheckAll();//檢查是否table有資料
    }

    private Runnable downloadDB =new Runnable(){
        @Override
        public void run() {
            String result = Act_DBConnector.executeQuery("SELECT * FROM `collection`");//TODO
            if(recSet_colCheckAll.size()!=result.length()){//要是SQLite資料筆數與MySQL不同
                /*TODO 這寫法有缺點，要是收藏時斷線，沒有傳到MySQL  導致兩者資料數不同，會以MySQL為主，
                   而且所有人的收藏都會下載，沒有帳號分開來下載
                 */
                //先清空SQLite再載入MySQL
                dbHper.clearRec_col();
                try {

                    jsonArray = new JSONArray(result);
                    if(result.length()>12){
                        for(int i=0;i<jsonArray.length();i++){
                            jsonData = jsonArray.getJSONObject(i);
                            email = jsonData.getString("email");
                            att_img = jsonData.getString("att_img");
                            att_name = jsonData.getString("att_name");
                            att_brief = jsonData.getString("att_brief");
                            att_uid = jsonData.getString("att_uid");
                            dbHper.insertcollect(email, att_name, att_brief,att_img,att_uid);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            checkdownloadflag = 1;//只嘗試一次下載，避免沒資料就無限循環
            col_addbutton.setText(getString(R.string.col_addbutton));
            col_progressBar.setVisibility(View.GONE);
            setupViewComponent();
        }
    };

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            file_gridView.clearTextFilter();
        } else {
            file_gridView.setFilterText(newText);
        }
        return true;
    }

    private void showdata() {
        mList = new ArrayList<>();
        for (int i = 0; i < recSet_col.size(); i++) {
            item = new HashMap<String,Object>();
            String[] fld = recSet_col.get(i).split("##");
            item.put("pointTitle", fld[2]);
            mList.add(item);
        }
//        //==========設定listView============
        //SimpleAdapter寫法
//        MyAdapter adapter = new MyAdapter(
//                getApplicationContext(),
//                mList,
//                R.layout.ast_col_view,
//                new String[]{"pointTitle"},
//                new int[]{R.id.col_view_placename}
//        );
        //BaseAdapter寫法。
        MyBaseAdapter adapter = new MyBaseAdapter(getApplicationContext(),mList);
        //可開啟上行，關閉SimpleAdapter，去最下面副class區觀察兩者不同
        file_gridView.setAdapter(adapter);//將抓取的資料設定到表格視窗
        file_gridView.setOnItemClickListener(onClickGridView);//建立表格視窗按鈕監聽
    }

    private GridView.OnItemClickListener onClickGridView = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            intent01.setClass(Ast_Col_File.this, Ast_Point.class)
                    .putExtra("uid", dbHper.FindUid_col(position,loginemail));
            startActivity(intent01);
        }
    };

    public void col_addbutton(View view) {
        Intent intent = new Intent();//因為沒有資料，先跳去景點頁面
        intent.setClass(getApplicationContext(), Ast_act.class);
        startActivity(intent);
        this.finish();
    }

    private class TransTask extends AsyncTask<String, Void, String> {
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
            String ans = sb.toString();
            //------------
            return ans;
        }
    }

    private void delsql(String uid) {//刪除MySQL的資料
        nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("email", loginemail ));
        nameValuePairs.add(new BasicNameValuePair("att_uid", uid ));
        dhandler.postDelayed(deleteDB, 500);
    }
    private Runnable deleteDB =new Runnable(){
        @Override
        public void run() {
            String result = Act_DBConnector.executeDelet("DELETE", nameValuePairs);
        }
    };

    //    ==========生命週期====================
    @Override
    public void onBackPressed() {
//    super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //    ==========main menu====================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Ast_Col_File.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//==========副class==========================
    public class MyAdapter extends SimpleAdapter {
        private ContentResolver mContRes;

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int     resource, String[] from, int[] to){
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            // here you let SimpleAdapter built the view normally.
            View v = super.getView(position, convertView, parent);

            // Then we get reference for Picasso
            ImageView img = (ImageView) v.getTag();
            if(img == null){
                img = (ImageView) v.findViewById(R.id.col_view_img1);
                v.setTag(img); // <<< THIS LINE !!!!
            }
            TextView delBtn = (TextView) v.findViewById(R.id.col_view_action);

            String[] fld = recSet_col.get(position).split("##");
            String url = fld[4];

            int imageWidth =300;
            Picasso.get().load(url).resize(imageWidth, imageWidth).into(img);
            //刪除按鈕監聽
            delBtn.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
//                    Toast.makeText(parent.getContext(), fld[5], Toast.LENGTH_LONG).show();//測試抓取UID值
                    new AlertDialog.Builder(parent.getContext())
                            .setTitle(R.string.col_dialog_delete)
                            .setMessage(getString(R.string.col_dialog_delplace)+fld[2])
                            .setCancelable(false)
                            .setIcon(android.R.drawable.btn_star_big_on)
                            .setNeutralButton(R.string.col_dialog_btnok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    mContRes = getContentResolver();
//                                    Uri uri = FriendsContentProvider.CONTENT_URI;
//                                    mContRes.delete(uri, "att_uid = '"+fld[5]+"'", null);//尚未修正，不能用
                                    dbHper.delectRec(fld[5],loginemail);
                                    delsql(fld[5]);
                                    setupViewComponent();
                                }
                            })
                            .setPositiveButton(R.string.col_dialog_btncancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
            });
            // return the view
            return v;
        }
    }
    //另一種Adapter寫法
    public class MyBaseAdapter extends BaseAdapter {

        private LayoutInflater mLayInf;
        List<Map<String, Object>> mItemList;
        private ContentResolver mContRes;

        public MyBaseAdapter(Context context, List<Map<String, Object>> itemList){
            mLayInf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mItemList = itemList;
        }

        @Override
        public int getCount() {
            //取得 ListView 列表 Item 的數量
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            //取得 ListView 列表於 position 位置上的 Item
            return position;
        }

        @Override
        public long getItemId(int position) {
            //取得 ListView 列表於 position 位置上的 Item 的 ID
            //可利用position讀取檔案在資料庫的ID
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//設定與回傳 convertView 作為顯示在這個 position 位置的 Item 的 View。
            View v = mLayInf.inflate(R.layout.ast_col_view, parent, false);

            ImageView imgView = (ImageView) v.findViewById(R.id.col_view_img1);
            TextView txtView = (TextView) v.findViewById(R.id.col_view_placename);
            TextView delBtn = (TextView) v.findViewById(R.id.col_view_action);

            txtView.setText(mItemList.get(position).get("pointTitle").toString());

            String[] fld = recSet_col.get(position).split("##");
            String url = fld[4];

            int imageWidth =300;
            Picasso.get().load(url).resize(imageWidth, imageWidth).into(imgView);
            //刪除按鈕監聽
            delBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
//                    Toast.makeText(parent.getContext(), fld[5], Toast.LENGTH_LONG).show();//測試抓取UID值
                    new AlertDialog.Builder(parent.getContext())
                            .setTitle(R.string.col_dialog_delete)
                            .setMessage(getString(R.string.col_dialog_delplace)+fld[2])
                            .setCancelable(false)
                            .setIcon(android.R.drawable.btn_star_big_on)
                            .setNeutralButton(R.string.col_dialog_btnok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //刪除方法1 尚未修正，不能用
//                                    mContRes = getContentResolver();
//                                    Uri uri = FriendsContentProvider.CONTENT_URI;
//                                    mContRes.delete(uri, "att_uid = '"+fld[5]+"'", null);
                                    //刪除方法2
                                    dbHper.delectRec(fld[5],loginemail);
                                    delsql(fld[5]);
                                    //更新listview畫面方法1,缺點:圖片沒刷新
//                                    mItemList.remove(position);
//                                    notifyDataSetChanged();
                                    //更新listview方法2...
                                    setupViewComponent();
                                }
                            })
                            .setPositiveButton(R.string.col_dialog_btncancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
            });
            return v;
        }

    }

}
