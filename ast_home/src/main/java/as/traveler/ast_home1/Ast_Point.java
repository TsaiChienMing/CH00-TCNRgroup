package as.traveler.ast_home1;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import as.traveler.ast_home1.providers.DetailContentProvider;
import as.traveler.ast_home1.providers.FriendsContentProvider;
import as.traveler.ast_home1.providers.ScheduleContentProvider;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Ast_Point extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener, AdapterView.OnItemSelectedListener  {

    private TextView pointTitle,pointContent;
    private ImageView pointImg;


    private String act_site,act_introduction,act_latitude,act_longitude,act_uid, act_phone, act_area, act_address;
    private BottomNavigationView ast_Bottom;
    private TextView ast_point_title , ast_point_content;
    private Dialog collectDlg;
    private Dialog addDlg;
    private Uri uri;

    private PlaceDbHelper dbHper;
    private static final String DB_FILE = "friends.db";
    private static final int DBversion = 1;
    private ArrayList<String> recSet;
    private String get_uid,get_name,get_lat,get_lng,get_address;
    private String url;
    private int get_index;
    private ContentResolver mContRes;
    private String[] MYCOLUMN= new String[]{"id", "email", "att_img", "att_name","att_brief","att_uid"};
    private int tcount,index;
    String TAG = "tcnr11=";
    private String msg;

    private GoogleMap pointMap; //宣告map物件
    //TODO==================================

    //申請權限後的返回碼
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private Location currentLocation;
    private LocationManager locationManager;
    private int currentZoom=14; //倍率0~20
    private String provider;  //提供資料
    private TextToSpeech tts;
    private Context context;
    private LatLng point_site;
    private static int loginon;
    private static String loginemail;
    private BottomNavigationItemView ast_point_TTS;
    private Cursor c_add;
    private ArrayList<NameValuePair> nameValuePairs;
    private Handler ihandler  = new Handler();
    private ContentResolver SSpinmContRes;
    private Spinner dladdplan_sp01,dldays_sp01;
    private ArrayList<String> spinrecSet;
    private static ContentResolver spinmContRes;
    private static String myselection="";
    public static String myargs[] = new String[]{};
    public static String myorder = "id ASC"; // 排序欄位

    private String[] SPINMYCOLUMN=new String[]{"id","name","departuretime","days","email"};
    private int up_item=0;
    private Dialog AddTripDlg,DaysDlg;

    private ArrayList<String> days=new ArrayList<>();
    private ArrayAdapter<String> daysadapter;
    private String[] daysArray;
    private String sel_day;

    //TODO==================================
    //-------------檢查登入------------------------
    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        loginon = login.getInt("flag", 0); //0 是為登入，1是有登入
        loginemail=login.getString("Email", "抓不到");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //==========================加入行程抓取遠端資料庫設定執行續============================

        StrictMode.setThreadPolicy(new
                StrictMode.
                        ThreadPolicy.Builder().

                detectDiskWrites().
                detectNetwork().
                penaltyLog().
                build());
        StrictMode.setVmPolicy(
                new
                        StrictMode.
                                VmPolicy.
                                Builder().
                        detectLeakedSqlLiteObjects().
                        penaltyLog().
                        penaltyDeath().
                        build());
        SSpinmContRes = getContentResolver();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast__point);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        u_checkgps();
        u_loaddata();
        setupViewcomponent();
        checkRequiredPermission(this);//TODO---詢問權限
        initDB();

    }
    private void setupViewcomponent() {
        pointTitle=(TextView)findViewById(R.id.ast_point_title);    //景點名稱
        pointContent=(TextView)findViewById(R.id.ast_point_content);    //景點介紹內文


        //-----------建立BottomNavigationView物件
        ast_Bottom = (BottomNavigationView) findViewById(R.id.ast_Bottom);
        ast_point_TTS = (BottomNavigationItemView) findViewById(R.id.ast_point_TTS);

        ast_Bottom.setSelectedItemId(R.id.ast_act);
        BottomNavigationHelper.removeShiftMode(ast_Bottom);  // 生一個外部的class
        ast_Bottom.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ast_Bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = new Intent();
                switch (item.getItemId()) {

                    //-----跳出外部網頁搜尋景點-----
                    case R.id.ast_point_googlesearch:
                        uri = Uri.parse("https://www.google.com/search?q=" + act_site);  // ?q= 後加上欲搜尋名稱
                        intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        if (tts != null) {
                            //stop : 停止朗讀。
                            //shutdown : 關閉語音引擎。
                            tts.stop();
                            tts.shutdown();
                        }
                        break;
                    //-----景點導航-------------------
                    case R.id.ast_googlemap:
                        intent.setClass(Ast_Point.this, Ast_point_map.class);
                        //建立一個bundle
                        Bundle bundle = new Bundle();
                        //打包資料傳送
                        bundle.putString("point_latitude", act_latitude);
                        bundle.putString("point_longitude", act_longitude);
                        bundle.putString("point_name", act_site);
                        bundle.putString("point_pic", url);
                        bundle.putString("point_address", act_address);
                        bundle.putString("point_phone", act_phone);
                        intent.putExtras(bundle);

                        startActivity(intent);
                        if (tts != null) {
                            //stop : 停止朗讀。
                            //shutdown : 關閉語音引擎。
                            tts.stop();
                            tts.shutdown();
                        }
                        break;
                    //----------------加入收藏對話框-----------------
                    case R.id.ast_point_collect:
                        //先檢查是否登入
                        if (loginon != 1) {
                            Toast.makeText(getApplicationContext(), "請先登入", Toast.LENGTH_LONG).show();
                            intent.setClass(getApplicationContext(), Ast_Login.class);
                            startActivity(intent);
                        } else {
                            int chkFav = dbHper.checkUid(act_uid, loginemail);//先確認當前顯示的UID是否有重複
                            if (chkFav != 0) {//回傳值-1為有重複的資料, 0為沒有資料，可以新增
                                Toast.makeText(getApplicationContext(), "這筆資料已經加入我的最愛", Toast.LENGTH_SHORT).show();// TODO 之後記得改這裡的文字到values
                            } else {
                                collectDlg = new Dialog(Ast_Point.this);
                                collectDlg.setTitle(getString(R.string.ast_point_dladdclt_t001));  //對話框標題
                                collectDlg.setCancelable(false);   //是否可點擊對話框外跳出
                                collectDlg.setContentView(R.layout.ast_point_dladdclt);  //選擇的layout
                                collectDlg.show();
                                Button dladdclt_b001 = (Button) collectDlg.findViewById(R.id.ast_point_dladdclt_b001);  //宣告取消按鈕
                                Button dladdclt_b002 = (Button) collectDlg.findViewById(R.id.ast_point_dladdclt_b002);  //宣告確認按鈕
                                dladdclt_b001.setOnClickListener(boo1on);
                                dladdclt_b002.setOnClickListener(boo1on);
                            }
                            if (tts != null) {
                                //stop : 停止朗讀。
                                //shutdown : 關閉語音引擎。
                                tts.stop();
                                tts.shutdown();
                            }
                        }
                        break;

                    //----------------加入行程對話框-----------------
                    case R.id.ast_point_addplan:
                        //先檢查是否登入
                        if (loginon != 1) {
                            Toast.makeText(getApplicationContext(), "請先登入", Toast.LENGTH_LONG).show();
                            intent.setClass(getApplicationContext(), Ast_Login.class);
                            startActivity(intent);
                        } else {
                            if (tts != null) {
                                //stop : 停止朗讀。
                                //shutdown : 關閉語音引擎。
                                tts.stop();
                                tts.shutdown();
                            }
                            //-------------------------------宣告另支layout加入行程DIALOG----------------------------------
                            AddTripDlg=new Dialog(Ast_Point.this);
                            AddTripDlg.setTitle(getString(R.string.ast_point_dladdclt_t002)); //此段會被後面layout蓋過
                            AddTripDlg.setCancelable(true);
                            AddTripDlg.setContentView(R.layout.ast_point_dladdplan); //選擇layout
                            AddTripDlg.show();
                            Button dladdplan_b001 = (Button) AddTripDlg.findViewById(R.id.ast_point_dladdplan_b001);  //宣告取消按鈕
                            Button dladdplan_b002 = (Button) AddTripDlg.findViewById(R.id.ast_point_dladdplan_b002);  //宣告確認按鈕
                            dladdplan_b001.setOnClickListener(boo1on);
                            dladdplan_b002.setOnClickListener(boo1on);

                            dladdplan_sp01 = (Spinner)AddTripDlg.findViewById(R.id.ast_point_dladdplan_sp01); //行程列表
                            //--------------------------------------------------
                            spinrecSet = u_selectdb(myselection, myargs, myorder);
                            u_setspinner();
                            // -------------------------
                            dladdplan_sp01.setOnItemSelectedListener(Ast_Point.this);


                        }
                        break;
                    //----------------朗讀功能-----------------
                    case R.id.ast_point_TTS:

                        if(tts.isSpeaking()){
                            tts.stop();
                            ast_point_TTS.setTitle(getString(R.string.ast_point_TTS));
                            ast_point_TTS.setIcon(getResources().getDrawable(R.mipmap.tts_icon));
                        }else{
                            tts.stop();
                            String talkString = act_introduction;
//        tts.speak(CharSequence text,   int queueMode,  android.os.Bundle params,   String utteranceId);
                            tts.speak(talkString, TextToSpeech.QUEUE_FLUSH, null);
                            ast_point_TTS.setTitle(getString(R.string.ast_point_noTTS));
                            ast_point_TTS.setIcon(getResources().getDrawable(R.mipmap.tts_cancel_icon));
                        }
                        break;
                }
                return false;
            }
        });



//-----------------------從資料庫接收線上圖片----------------------------------
        //-------連結資料庫-----------------
        if (dbHper == null)
            dbHper = new PlaceDbHelper(this, DB_FILE, null, DBversion);
        recSet = dbHper.getRecSet();
        //-----------------------接收資料用----------------------------------
        Intent intent = this.getIntent();
        get_uid = intent.getStringExtra("uid");//getIntExtra後面要給個預設值避免取不到值
        get_name = intent.getStringExtra("name");
        get_lat = intent.getStringExtra("lat");
        get_lng = intent.getStringExtra("lng");
        get_address =intent.getStringExtra("address");
        if(get_uid.length()<5){
            //-------------------取得資料庫資料-------------------------------------------
            //這串資料是資料庫來的
            get_index = dbHper.findIndex(get_uid);//由uid取得id，再-1換成index值
            String[] fld = recSet.get(get_index-1).split("##");
            act_site = fld[1];//名稱
            act_latitude = fld[2];//緯度
            act_longitude = fld[3];//經度
            act_introduction = fld[4];//簡介
            act_area = fld[5];//區域
            act_address = fld[6];//地址
            act_phone = fld[7];//電話
            act_uid = fld[8];//觀光局給的景點ID
            url = fld[9];//圖片
        }else{
            //這串資料是google來的
            if(dbHper.checkUid(get_uid, loginemail)==0){//SQLite沒有存這筆資料的話，就讀取intent過來的內容
                act_uid = get_uid;
                act_site = get_name;
                act_latitude = get_lat;
                act_longitude = get_lng;
            }else{
                //SQLite有存這筆收藏資料的話，就讀取資料庫
                //寫這個的原因是為了收藏頁面跳轉到這裡使用
                get_index = dbHper.findIndex(get_uid);//由uid取得id，再-1換成index值
                String[] fld = recSet.get(get_index-1).split("##");
                act_site = fld[1];//名稱
                act_latitude = fld[2];//緯度
                act_longitude = fld[3];//經度
                act_introduction = fld[4];//簡介
                act_area = fld[5];//區域
                act_address = fld[6];//地址
                act_phone = fld[7];//電話
                act_uid = fld[8];//觀光局給的景點ID
                url = fld[9];//圖片
            }
        }

        //設定class標題
        this.setTitle(act_site);//設定title
        //------------將接收資料傳到layout-----------------------------------------
        pointTitle.setText(act_site);
        pointContent.setText(act_introduction);

        //----------TTS語音朗讀----------------
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // 設定語系
                    //setLanguage：設置語言。
                    //英語為Locale.ENGEN;
                    //法語為Locale.FRENCH;
                    //德語為Locale.GERMAN;
                    //意大利語為Locale.ITALIAN;
                    //漢語普通話為Locale.CHINA（需安裝中文引擎，如科大訊飛+）
                    int result = tts.setLanguage(Locale.CHINESE);

                    //setPitch：設置音調.1.0正常音調;低於1.0的為低音;高於1.0的為高音
                    tts.setPitch(1.0f); // 設定語音間距

                    //setSpeechRate：設置語速.1.0正常語速; 0.5慢一半的語速; 2.0;快一倍的語速
                    tts.setSpeechRate(1.0f); // 設定語音速率
                    //------------------------------------------
                    //speak : 開始對指定文本進行語音朗讀。
                    //synthesizeToFile : 把指定文本的朗讀語音輸出到文件。
                    //stop : 停止朗讀。
                    //shutdown : 關閉語音引擎。
                    //isSpeaking : 判斷是否在語音朗讀。
                    //getLanguage : 獲取當前的語言。
                    //getCurrentEngine : 獲取當前的語音引擎。
                    //getEngines : 獲取系統支持的所有語音引擎。
                    //------------------------------------------
                    // 確認手機所設定的TTS引擎是否支援該語系的語音
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        Toast.makeText(context, getString(R.string.msg1), Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(context, getString(R.string.msg2), Toast.LENGTH_SHORT).show();
//                        talkBtn.setEnabled(true);
//                        Drawable top = getResources().getDrawable(R.drawable.speaker_on);
//                        talkBtn.setCompoundDrawablesWithIntrinsicBounds(
//                                null, top, null, null);
                    }
                } else {
//                    Toast.makeText(context, getString(R.string.msg3), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void u_checkgps() {
        // 取得系統服務的LocationManager物件
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 檢查是否有啟用GPS
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 顯示對話方塊啟用GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("定位管理")
                    .setMessage("GPS目前狀態是尚未啟用.\n"
                            + "請問你是否現在就設定啟用GPS?")
                    .setPositiveButton("啟用", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 使用Intent物件啟動設定程式來更改GPS設定
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("不啟用", null).create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void checkRequiredPermission(Ast_Point ast_point) {

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //map的基本元件
        pointMap = googleMap;

        // 開啟 Google Map 拖曳功能
        pointMap.getUiSettings().setScrollGesturesEnabled(true);

        // 右下角的導覽及開啟 Google Map功能
        pointMap.getUiSettings().setMapToolbarEnabled(true);

        //左上角顯示指北針，要兩指旋轉才會出現
        pointMap.getUiSettings().setCompassEnabled(true);

        //右下角顯示縮放按鈕的放大縮小功能
        pointMap.getUiSettings().setZoomControlsEnabled(true);

        //將接收到的緯經度從string轉換成float型態
        float lat =Float.parseFloat(act_latitude);
        float log =Float.parseFloat(act_longitude);

        // Add a marker in Sydney and move the camera
        point_site = new LatLng(lat,log);
        pointMap.addMarker(new MarkerOptions().position(point_site).title(act_site));
        pointMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point_site,currentZoom)); //中心點   moveCamera就是鏡頭移動的地方
        //----------取得定位許可-----------------------
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        } else {
            //----顯示我的位置ICO-------
            pointMap.setMyLocationEnabled(true);
            return;
        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }



    @Override
    public void onClick(View v) {

    }
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.onBackPressed), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            //stop : 停止朗讀。
            //shutdown : 關閉語音引擎。
            tts.stop();
            tts.shutdown();
        }
    }

    private void initDB() {

    }
    //    ======================================加入行程相關==========================================
    //----------讀取行程------------
    private ArrayList<String> u_selectdb(String myselection, String[] myargs, String myorder) {
        ArrayList<String> recAry = new ArrayList<String>();
        spinmContRes = getContentResolver();


        Cursor add_c = spinmContRes.query(ScheduleContentProvider.CONTENT_URI, SPINMYCOLUMN, null, null, null);
        tcount = add_c.getCount();
        int columnCount = add_c.getColumnCount();
        while (add_c.moveToNext()) {
            String fldSet = "";
            for (int ii = 0; ii < columnCount; ii++)
                fldSet += add_c.getString(ii) + "#";
            recAry.add(fldSet);
        }
        add_c.close();
        return recAry;
    }
    private void u_setspinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);


        for (int i = 0; i < spinrecSet.size(); i++) {
            String[] fld = spinrecSet.get(i).split("#");
            adapter.add("行程:" + fld[1] + "，" + fld[3]+ "日旅程" ); //spinner顯示格式
//          adapter.add(fld[0] + " " + fld[1] + " " + fld[2] + " " + fld[3]); 資料庫id、行程名稱、出發日期、幾日
            days.add( fld[0]+","+fld[3]);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dladdplan_sp01.setAdapter(adapter);

        dladdplan_sp01.setOnItemSelectedListener(this);
        //        mSpnName.setSelection(index, true); //spinner 小窗跳到第幾筆
    }



//    ================================================================================================

    private Button.OnClickListener boo1on= new Button.OnClickListener() {
        Intent it = new Intent();
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                //--------加入收藏對話框之按鈕----------------------
                case R.id.ast_point_dladdclt_b001: //取消
                    collectDlg.cancel();
                    break;

                case R.id.ast_point_dladdclt_b002: //確認
                    if(url==null){//google點過來的都走這
                        //TODO app要加也是能加，但是網頁瀏覽行程或收藏會出事，所以還是先關起來
                        Toast.makeText(getApplicationContext(), "資料庫尚未整理好，不能加google資料", Toast.LENGTH_LONG).show();
//                        String area =null;
//                        String phone =null;
//                        //這裡的url要填充一個預設值
//                        url = "https://live.staticflickr.com/65535/50173649227_650a19f741_o.jpg";
//                        String thumburl ="https://live.staticflickr.com/65535/50173649227_87ba6d8ce4_q.jpg";
////                        long collect = dbHper.insertcollect(loginemail, act_site, act_introduction,url,act_uid);//真正執行SQL
//                        dbHper.insertRec(act_site, act_latitude, act_longitude, act_introduction, area, get_address, phone, act_uid, url, thumburl);
//                        mContRes = getContentResolver();
//                        ContentValues newRow = new ContentValues();
//                        newRow.put("email", loginemail);
//                        newRow.put("att_name", act_site);
//                        newRow.put("att_brief", act_introduction);
//                        newRow.put("att_img", url);
//                        newRow.put("att_uid", act_uid);
//                        mContRes.insert(FriendsContentProvider.CONTENT_URI, newRow);
                    }else{//資料庫的走這
//                        long collect = dbHper.insertcollect(loginemail, act_site, act_introduction,url,act_uid);//真正執行SQL
                        mContRes = getContentResolver();
                        ContentValues newRow = new ContentValues();
                        newRow.put("email", loginemail);
                        newRow.put("att_name", act_site);
                        newRow.put("att_brief", act_introduction);
                        newRow.put("att_img", url);
                        newRow.put("att_uid", act_uid);
                        mContRes.insert(FriendsContentProvider.CONTENT_URI, newRow);
                    }
//                    c_add = mContRes.query(FriendsContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
//                    c_add.moveToFirst();
                    //======直接增加到MySQL============
                    mysql_insert();
//                    dbmysql();
                    //==========================
                    String msg = null;
                    // -------------------------
//                    ContentValues newRow = new ContentValues();
//                    newRow.put("email", loginemail);
//                    newRow.put("att_name", act_site);
//                    newRow.put("att_brief", act_introduction);
//                    newRow.put("att_img", url);
//                    newRow.put("att_uid", act_uid);
//                    mContRes.insert(FriendsContentProvider.CONTENT_URI, newRow);
                    // -------------------------
//                    msg = "新增記錄  成功 ! \n" + "目前資料表共有 " + (c_add.getCount() + 1) + " 筆記錄 !";
//                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

//                    if (c_add == null) {
//                        tcount = 0;
//                        index = 0;
//                        return;
//                    };
//
//                    c_add.close();
//                    setupViewcomponent(); //似乎沒必要 先關起來
                    Toast.makeText(getApplicationContext(), "已加入收藏列!", Toast.LENGTH_SHORT).show();
                    collectDlg.cancel();
                    break;

                //--------加入行程對話框之按鈕----------------------

                case R.id.ast_point_dladdplan_b001: //取消
                    AddTripDlg.cancel();
                    break;

                case R.id.ast_point_dladdplan_b002: //確認
                    //先判斷是否有行程，無的話跳至行程新增
                    if (daysArray != null){
                        AddTripDlg.cancel(); //先關掉選成行程
                        //-------------------------------選擇天數DIALOG----------------------------------
                        DaysDlg=new Dialog(Ast_Point.this);
                        DaysDlg.setTitle(getString(R.string.ast_point_dladdclt_t003)); //此段會被後面layout蓋過
                        DaysDlg.setCancelable(true);
                        DaysDlg.setContentView(R.layout.ast_point_dldays); //選擇layout
                        DaysDlg.show();
                        Button dldays_b001 = (Button) DaysDlg.findViewById(R.id.ast_point_dldays_b001);  //宣告取消按鈕
                        Button dldays_b002 = (Button) DaysDlg.findViewById(R.id.ast_point_dldays_b002);  //宣告確認按鈕
                        dldays_b001.setOnClickListener(boo1on);
                        dldays_b002.setOnClickListener(boo1on);


                        dldays_sp01 = (Spinner)DaysDlg.findViewById(R.id.ast_point_dldays_sp01); //行程列表
                        dladdplan_sp01.setOnItemSelectedListener(Ast_Point.this);
                        dldays_sp01.setOnItemSelectedListener(SelectDay);
                        dldays_sp01.setAdapter(daysadapter);
//
                    }else{
                        Toast.makeText(Ast_Point.this,"請先新增行程",Toast.LENGTH_LONG).show();
                        Intent it = new Intent();
                        it.setClass(Ast_Point.this,Ast_trip.class);
                        startActivity(it);
                        Ast_Point.this.finish();
                    }

                    break;
                //--------加入行程選擇日期之按鈕----------------------
                case R.id.ast_point_dldays_b001: //取消
                    DaysDlg.cancel();
                    break;

                case R.id.ast_point_dldays_b002: //確認
                    UPDATE_detail.attr_update( daysArray[0],sel_day,act_uid);
                    Toast.makeText(getApplicationContext(), "已加入行程", Toast.LENGTH_LONG).show();
                    DaysDlg.cancel();

                    break;
            }
        }
    };

    static class UPDATE_detail {
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
                    nameValuePairs.add(new BasicNameValuePair("att_id", uid));
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
        }
    }

    //------------------------------------------------------------------------------------

    private void mysql_insert() {
        nameValuePairs = new ArrayList<NameValuePair>();
        if(url==null)
            url = "https://live.staticflickr.com/65535/50173649227_650a19f741_o.jpg";
        nameValuePairs.add(new BasicNameValuePair("att_img", url ));
        nameValuePairs.add(new BasicNameValuePair("att_name", act_site ));
        nameValuePairs.add(new BasicNameValuePair("att_brief", act_introduction ));
        nameValuePairs.add(new BasicNameValuePair("email", loginemail ));
        nameValuePairs.add(new BasicNameValuePair("att_uid", act_uid ));
        ihandler.postDelayed(insertDB, 500);
    }
    private Runnable insertDB =new Runnable(){
        @Override
        public void run() {
            String result = Act_DBConnector.executeInsert("INSERT", nameValuePairs);
        }
    };

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
                Ast_Point.this.finish();
                if (tts != null) {
                    //stop : 停止朗讀。
                    //shutdown : 關閉語音引擎。
                    tts.stop();
                    tts.shutdown();
                }
                break;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            int iSelect = dladdplan_sp01.getSelectedItemPosition(); // 找到按何項

            // -----------------------------------

            daysArray = days.get(iSelect).split(",");

            daysadapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item);

            for(int i=0;i<  Integer.parseInt(daysArray[1] );i++){

                daysadapter.add(String.valueOf(i+1)) ;
            }





            Cursor c = spinmContRes.query(ScheduleContentProvider.CONTENT_URI, SPINMYCOLUMN, null, null, null);
            c.moveToFirst(); // 一定要寫，不然會出錯
            c.moveToPosition(iSelect);
            // -------目前所選的item---
            up_item = iSelect;

            c.close();



    }

    Spinner.OnItemSelectedListener SelectDay =new Spinner.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int dSelect = dldays_sp01.getSelectedItemPosition(); // 找到按何項
            sel_day = String.valueOf(dSelect+1);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
