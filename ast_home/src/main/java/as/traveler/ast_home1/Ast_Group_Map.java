package as.traveler.ast_home1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import as.traveler.ast_home1.providers.GroupContentProvider;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class Ast_Group_Map extends AppCompatActivity implements
        View.OnClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        LocationListener{
    //=======範圍內的都需要==================
    //所需要申請的權限陣列
    private static final String[] permissionsArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private List<String> permissionsList = new ArrayList<String>();

    //申請權限後的返回碼
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    //==================================
    private LocationManager manager;
    private Location currentLocation;
    String TAG = "tcnr02=";
    private Button group_map_addfriend, group_map_point, group_map_alert;
    private Dialog group_Alert, group_Point, group_AddFriend;
    private long spentTime, hours, minute, second;  //時間的變數
    private long endTime;
    private int year_set, month_set, day_set, hour_set, min_set;
    private Handler handler = new Handler();
    //鬧鐘的物件
    private TimePicker group_alert_time;
    private Button group_alert_ok_btn, group_alert_calset_btn, group_alert_cal_btn;
    private TextView group_alert_show_time, group_check_text;

    //集合點的物件
    private GoogleMap group_map;
    private SupportMapFragment group_map_Fragment;
    private SearchView group_map_search;
    private String location;
    private TextView group_check_point;  //選擇的集合點
    private Button check_okbtn, check_calbtn, point_navigation, point_cancel, point_quit;
    private LocationManager locationManager;
    private String provider;
    private Marker markerMe;
    private long minTime = 2000; //ms
    private float minDist = 2.0f; //meter
    float mapzoom = 14;
    private TextView tmsg;
    static LatLng VGPS = new LatLng(24.172127, 120.610313);
    private List<Address> addressList;
    //-----------------------------
    private String Myid = "0";
    private String Myname = "02號蔡荐銘";
    private String Myaddress = "24.172127,120.610313";
    private String Mygroup = "1"; //群組
    //-----------------------------
    private double dLat, dLon;
    private float Anchor_x = 0.5f;
    private float Anchor_y = 0.9f;
    private String[] MYAddr = new String[]{"points"};
    private String key = "key=" + "";  //需使用 javascriptmap key;

    private Polyline mPolyline;

    //添加好友的物件
    private EditText friend_edit;
    private Button friend_okbtn, friend_calbtn;
    private String set_alert;

    //SLQ物件
    private static ContentResolver mContRes;
    private String[] MYCOLUMN = new String[]{"id", "grp_name", "creator_id", "member_id", "points", "alert_year", "alert_month", "alert_day", "alert_hour", "alert_min", "alert_state"};       //MySQL欄位
    String msg = null;
    int tcount;
    private int autotime = 30;   //多久更新一次     10*1000 = 10s
    private ContentValues newRow;
    private JSONObject jsonData;
    private JSONArray jsonArray;
    private String grp_name;
    private String ResetTime;
    private String Str_Year, Str_Month, Str_Day, Str_Hour, Str_Min;
    private int alert_Count = 0;
    private String alert_state;
    private Bundle bundle = new Bundle();
    private Vibrator myVibrator;
    private String creator_id;
    private LinearLayout group_point_set, group_point_features;
    private int routeon;
    private LatLng mlatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_group_map);
        //----------第一步------------抓取遠端資料庫設定執行續----------必備--------------------
        StrictMode.setThreadPolicy(new
                StrictMode.
                        ThreadPolicy.Builder().
                detectDiskReads().
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
//-------------------------------------------------------------------------------------------------
        bundle = this.getIntent().getExtras();
        alert_state = bundle.getString("alert_state");
        creator_id = bundle.getString("creator_id");
        grp_name = bundle.getString("name");
        int u = 5;
        checkRequiredPermission(this);//檢查SDK版本，確認是否獲得權限
        u_checkgps();//檢查GPS是否開啟
        setupViewComponent();
        //將更新資料做成一個緒
//        handler.postDelayed(updateSQL,1000);
//        sqliteupdate();


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
                            //沒有開啟GPS按下啟用之後，就跳到設定GPS畫面
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
    private void setupViewComponent() {
        group_map_addfriend = (Button) findViewById(R.id.group_map_addfriend);
        group_map_point = (Button) findViewById(R.id.group_map_point);
        group_map_alert = (Button) findViewById(R.id.group_map_alert);
        group_map_search = (SearchView) findViewById(R.id.group_map_search);
        //顯示剩餘時間的Textview
        group_alert_show_time = (TextView) findViewById(R.id.group_alert_show_time);
        group_map_alert.setOnClickListener(this);
        group_map_point.setOnClickListener(Map_Points);
        group_map_addfriend.setOnClickListener(Map_Addfriend);
        group_alert_show_time.setVisibility(View.INVISIBLE);
        myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
        tmsg = (TextView) findViewById(R.id.msg);
        //設定GoogleMap地圖
        group_map_Fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.group_map_fragment);

        group_map_Fragment.getMapAsync(this);

        group_map_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                location = group_map_search.getQuery().toString();      //查詢的點
                addressList = null;

                if (location != null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(Ast_Group_Map.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    mlatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mlatLngupload(mlatLng);
                    int f = 4;
                    group_map.addMarker(new MarkerOptions().position(mlatLng).title(location));
                    group_map.animateCamera(CameraUpdateFactory.newLatLngZoom(mlatLng, 10));

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        group_map_Fragment.getMapAsync(this);

        handler.postDelayed(updateSQL, 100);
//        sqliteupdate();//抓取SQLite資料

    }


    private Runnable updateSQL = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, autotime * 1000); // 真正延遲的時間
            // -------執行匯入MySQL -------------
            dbmysql();
        }
    };
    //GOOGLEMAP
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int i = 0; i < permissions.length; i++) {
                    //寫了陣列，有幾個就問幾個
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), permissions[i] + "權限申請成功!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "權限被拒絕： " + permissions[i], Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        group_map = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        map.addMarker(new MarkerOptions().position(VGPS).title(""));
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS,mapzoom));    //14倍GOOGLEMAP

        //        mUiSettings = map.getUiSettings();//
//        開啟 Google Map 拖曳功能
        group_map.getUiSettings().setScrollGesturesEnabled(true);
//        右下角的導覽及開啟 Google Map功能
        group_map.getUiSettings().setMapToolbarEnabled(true);

//        左上角顯示指北針，要兩指旋轉才會出現
        group_map.getUiSettings().setCompassEnabled(true);

//        右下角顯示縮放按鈕的放大縮小功能
        group_map.getUiSettings().setZoomControlsEnabled(false);

// Add a marker in 中區職訓 and move the camera
        group_map.addMarker(new MarkerOptions().position(VGPS).title("中區職訓"));
        group_map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, mapzoom));
//        map.moveCamera(CameraUpdateFactory.newLatLng(VGPS));
        //------取得定位許可---------
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
//            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        } else {
            //----顯示我的位置ICO-------
            group_map.setMyLocationEnabled(true);//要先有定位才能看到"我的位置"的按鈕
            return;
        }
    }
    private void checkRequiredPermission(final Activity activity) {
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        if (permissionsList.size() != 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new
                    String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    //-----------------------鬧鐘
    //設定鬧鐘監聽
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.group_map_alert:
                Alert_DiaLog();
                break;
        }
    }


    //按下確定鬧鐘設定的按鈕
    private Button.OnClickListener Alert_Set_Btn = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //按下確定鬧鐘設定的按鈕
                case R.id.group_alert_okbtn:
                    group_alert_okset();
                    alert_mysql_insert();
                    group_alert_show_time.setVisibility(View.VISIBLE);
                    dbmysql();
                    break;

                //按下離開鬧鐘設定的按鈕
                case R.id.group_alert_calbtn:
                    group_Alert.cancel();
                    break;

                //按下重製鬧鐘設定的按鈕
                case R.id.group_alert_calset:
                    Alert_Resettime();
                    alert_mysql_reset();
                    alert_state_off();
                    group_alert_show_time.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };



    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (initLocationProvider()) {
            nowaddress();
        } else {
            Toast.makeText(getApplicationContext(),"GPS未開啟，請先開啟定位",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(updateSQL, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateSQL);
        this.finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        handler.postDelayed(updateSQL, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSQL);
        this.finish();
    }

    //計算剩餘時間
    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            try{

                spentTime = endTime - System.currentTimeMillis()  ; //花費的時間(鬧鐘)
                int d = 23;
                hours = (spentTime / 1000) / 60 / 60;
                minute = (spentTime / 1000) / 60 % 60;
                second = (spentTime / 1000) % 60;
                int dv = 3;
                if (spentTime < 0 || hours > 999) {    //時間設定不正確
                    group_alert_show_time.setText(getString(R.string.error_msg) + "\n" + "");
//                Toast.makeText(getApplicationContext(),getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                    handler.removeCallbacks(updateTimer);
                } else {     //時間設定正確
//                music_set();
                    group_alert_show_time.setVisibility(View.VISIBLE);
                    group_alert_show_time.setText(getString(R.string.group_alert_show_time) + "\n" + String.format("%02d", hours) + ":" +
                            String.format("%02d", minute) + ":" + String.format("%02d", second)); //顯示剩餘多少時間
                    handler.postDelayed(this, 1000);
                    alert_state = "true";
                    //時間到了鬧鐘響起
                    if (hours <= 0 && minute <= 0 && second <= 0) {
//                    startmusic.start();
//                    myVibrator.vibrate(100);        //鬧鐘
                        group_alert_show_time.setText("時間已經到了");
                        handler.removeCallbacks(updateTimer);   //結束緒
                    alert_mysql_reset();        //將MySQL的資料清空
                    alert_state_off();              //將MySQL的狀態改回false。
                    }
                    //不斷確認狀態是否有從off變更為on。
                    alert_state_on();
                }
            }catch (Exception e){

            }

        }
    };

    //設定鬧鐘的DIALOG
    private void Alert_DiaLog() {
        group_Alert = new Dialog(Ast_Group_Map.this);
        group_Alert.setTitle(getString(R.string.group_setalert));
        group_Alert.setCancelable(false);
        group_Alert.setContentView(R.layout.ast_group_alert_dialog);

        //日曆
        //時間
        group_alert_time = (TimePicker) group_Alert.findViewById(R.id.group_alert_time);
        ;

        //設定確認
        group_alert_ok_btn = (Button) group_Alert.findViewById(R.id.group_alert_okbtn);
        //設定取消
        group_alert_cal_btn = (Button) group_Alert.findViewById(R.id.group_alert_calbtn);
        //鬧鐘取消
        group_alert_calset_btn = (Button) group_Alert.findViewById(R.id.group_alert_calset);

        group_Alert.show();

        //設監聽
        group_alert_ok_btn.setOnClickListener(Alert_Set_Btn);               //確定鬧鐘
        group_alert_cal_btn.setOnClickListener(Alert_Set_Btn);              //取消鬧鐘
        group_alert_calset_btn.setOnClickListener(Alert_Set_Btn);       //重製鬧鐘
    }

    //完成鬧鐘的設定
    private void group_alert_okset() {
        //獲得設定的時間
        Calendar c = Calendar.getInstance();
        year_set = c.get(Calendar.YEAR);
        month_set = c.get(Calendar.MONTH);
        day_set = c.get(Calendar.DAY_OF_MONTH);
        hour_set = group_alert_time.getHour();
        min_set = group_alert_time.getMinute();
//----------------------------------------------------------------------------------------------
//        cg = Calendar.getInstance();    //設定日歷新物件
//        cg.set(year_set, month_set, day_set, hour_set, min_set);   //將日期及時間設定進去物件
//         endTime = cg.getTimeInMillis();    //設定好的結束時間
//        Str_endTime = String.valueOf(endTime);      //Long轉String     //會轉成一串數字 不是時間的格式

//設定鬧鐘時間格式化為String
        Str_Year = String.format("%d", year_set);
        Str_Month = String.format("%d", month_set);
        Str_Day = String.format("%d", day_set);
        Str_Hour = String.format("%d", hour_set);
        Str_Min = String.format("%d", min_set);
    int f = 3;
        group_Alert.cancel();
    }

    //重製鬧鐘的設定
    private void Alert_Resettime() {
        //取得目前時間
        Calendar c = Calendar.getInstance();
        year_set = c.get(Calendar.YEAR);
        month_set = c.get(Calendar.MONTH);
        day_set = c.get(Calendar.DAY_OF_MONTH);
        hour_set = c.get(Calendar.HOUR_OF_DAY);
        min_set = c.get(Calendar.MINUTE);
        group_alert_time.setHour(hour_set);
        group_alert_time.setMinute(min_set);

        ResetTime = "";
        //結束倒數計時
        handler.removeCallbacks(updateTimer);

        //重製剩餘時間
        group_alert_show_time.setText(getString(R.string.group_alert_show_time) + (""));
        group_Alert.cancel();
    }

    //新增鬧鐘到MySQL
    private void alert_mysql_insert() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        Bundle bundle1 = this.getIntent().getExtras();
        if (bundle1 != null) {
            String grp_name = bundle1.getString("name");
            nameValuePairs.add(new BasicNameValuePair("grp_name", grp_name));        //這裡是null 08/15  已解決
            nameValuePairs.add(new BasicNameValuePair("creator_id", creator_id));
            nameValuePairs.add(new BasicNameValuePair("alert_state", alert_state));

        }
        nameValuePairs.add(new BasicNameValuePair("alert_year", Str_Year));           //將年上傳到MySQL
        nameValuePairs.add(new BasicNameValuePair("alert_month", Str_Month));           //將月上傳到MySQL
        nameValuePairs.add(new BasicNameValuePair("alert_day", Str_Day));           //將日上傳到MySQL
        nameValuePairs.add(new BasicNameValuePair("alert_hour", Str_Hour));           //將時上傳到MySQL
        nameValuePairs.add(new BasicNameValuePair("alert_min", Str_Min));           //將分上傳到MySQL

        //擔心沒有將資料寫入，所以停一下再新增
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = Group_DBConnector.alertupdate("SELECT * FROM grp", nameValuePairs);
//-----------------------------------------------
    }

    //重設鬧鐘到MySQL
    private void alert_mysql_reset() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            String grp_name = bundle.getString("name");
            nameValuePairs.add(new BasicNameValuePair("grp_name", grp_name));        //這裡是null 08/15  已解決
            nameValuePairs.add(new BasicNameValuePair("creator_id", creator_id));
        }
        nameValuePairs.add(new BasicNameValuePair("alert_year", ""));           //將年上傳到MySQL
        nameValuePairs.add(new BasicNameValuePair("alert_month", ""));           //將月上傳到MySQL
        nameValuePairs.add(new BasicNameValuePair("alert_day", ""));           //將日上傳到MySQL
        nameValuePairs.add(new BasicNameValuePair("alert_hour", ""));           //將時上傳到MySQL
        nameValuePairs.add(new BasicNameValuePair("alert_min", ""));           //將分上傳到MySQL
        //擔心沒有將資料寫入，所以停一下再新增
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = Group_DBConnector.alertdelete("SELECT * FROM grp", nameValuePairs);

//-----------------------------------------------
    }

    //設定鬧鐘關閉狀態到MySQL
    private void alert_state_on() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        Bundle bundle = this.getIntent().getExtras();
        //確認名稱是否正確。
        if (bundle != null) {
            String grp_name = bundle.getString("name");
            nameValuePairs.add(new BasicNameValuePair("grp_name", grp_name));        //這裡是null 08/15  已解決
            nameValuePairs.add(new BasicNameValuePair("member_id", creator_id));
            //鬧鐘新增成功時，將狀態傳至MySQL
            if (alert_state.equals("true")) {
                nameValuePairs.add(new BasicNameValuePair("alert_state", "true"));           //將狀態關閉上傳到MySQL
            }
        }

        //擔心沒有將資料寫入，所以停一下再新增
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = Group_DBConnector.alert_StateOn("SELECT * FROM grp", nameValuePairs);

//-----------------------------------------------
    }

    //設定鬧鐘關閉狀態到MySQL
    private void alert_state_off() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            String grp_name = bundle.getString("name");
            nameValuePairs.add(new BasicNameValuePair("grp_name", grp_name));        //這裡是null 08/15  已解決
            nameValuePairs.add(new BasicNameValuePair("member_id", creator_id));
        }
        //如果要將鬧鐘重製要完成條件
        if (alert_state.equals("true")) {
            nameValuePairs.add(new BasicNameValuePair("alert_state", "false"));           //將狀態關閉上傳到MySQL
            alert_state = "false";
        }


        //擔心沒有將資料寫入，所以停一下再新增
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = Group_DBConnector.alert_StateOff("SELECT * FROM grp", nameValuePairs);

//-----------------------------------------------
    }


    //--------------------------集合
    //集合點設定的DiaLog
    private Button.OnClickListener Map_Points = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            Point_DiaLog();
        }
    };
    //集合點確定的按鈕
    private Button.OnClickListener Point_CheckBtn = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.point_okbtn:
                    mlatLngupload(mlatLng);
                    dbmysql();
                    Point_Set_Cancel();     //開啟集合功能
                    break;

                case R.id.point_calbtn:
                    group_Point.cancel();
                    break;
            }
        }
    };
    //集合點功能的按鈕
    private Button.OnClickListener Point_SetBtn = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.point_navigation://導航功能
                    routeon = 1;
                    group_Point.cancel();
                    break;

                case R.id.point_cancel:                     //重新設定集合地功能
                    Point_Set();
                    break;

                case R.id.point_quit:                     //離開畫面功能
                    group_Point.cancel();
                    break;
            }
        }
    };
    //檢查GPS是否開啟
    private boolean initLocationProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER; //這裡會出現GPS或wifi之類文字的訊息
            return true;
        }
        return false;
    }
    //設定集合點的DIALOG
    private void Point_DiaLog() {
        //設定集合點DIALOG
        group_Point = new Dialog(Ast_Group_Map.this);
//        group_Point.setTitle(getString(R.string.group_setpoint));
        group_Point.setCancelable(false);

        group_Point.setContentView(R.layout.ast_group_point_dialog);
        group_point_set = (LinearLayout)group_Point.findViewById(R.id.point_set);   //設定集合點的layout
        group_point_features = (LinearLayout)group_Point.findViewById(R.id.point_features);//集合功能的layout
        group_check_text = (TextView) group_Point.findViewById(R.id.point_text);//將此設為集合點
        group_check_point = (TextView) group_Point.findViewById(R.id.point_check);//顯示輸入的集合點名稱
        check_okbtn = (Button) group_Point.findViewById(R.id.point_okbtn); //確定設定集合點
        check_calbtn = (Button) group_Point.findViewById(R.id.point_calbtn);//取消設定集合點
        point_navigation = (Button) group_Point.findViewById(R.id.point_navigation);//導航按鈕
        point_cancel = (Button) group_Point.findViewById(R.id.point_cancel);//重新設定集合點
        point_quit = (Button) group_Point.findViewById(R.id.point_quit);

        group_check_point.setText(location);

        group_Point.show();
        check_okbtn.setOnClickListener(Point_CheckBtn);
        check_calbtn.setOnClickListener(Point_CheckBtn);
        point_navigation.setOnClickListener(Point_SetBtn);
        point_cancel.setOnClickListener(Point_SetBtn);
        point_quit.setOnClickListener(Point_SetBtn);
    }
    //集合點的功能顯示
    private void Point_Set() {
        group_point_set.setVisibility(View.VISIBLE);
        group_point_features.setVisibility(View.GONE);
    }
    //取消集合點的設定
    private void Point_Set_Cancel() {
        group_point_set.setVisibility(View.GONE);
        group_point_features.setVisibility(View.VISIBLE);
    }
    private void SelectMysql(String myname) {
        String selectMYSQL = "";
        String result = "";
        try {
            selectMYSQL = "SELECT * FROM  WHERE name = '" + myname + "' ORDER BY id";
            result = DBConnector.executeQuery(selectMYSQL);
            int cc = result.length();
            //注意Myid的預設值是0，資料庫沒有0這個ID
            //======尋到有資料的時候就直接抓ID======
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonData = jsonArray.getJSONObject(0);   //第幾欄位
            Myid = jsonData.getString("id").toString();     //欄位名稱
            //=======if沒有資料就去創建一個ID=====
            if (result.length() <= 12) {
                //執行InsertMySQL新增個人資料
                //也可以直接呼叫DBConnector.executeInsert(A,B,C);
//                InsertMySQL(myname, Mygroup, Myaddress);
//
//                selectMYSQL = "SELECT * FROM member WHERE name = '" + myname + "' ORDER BY id";
//                result = DBConnector.executeQuery(selectMYSQL);
//                jsonArray = new JSONArray(result);
//                jsonData = jsonArray.getJSONObject(0);
//                Myid = jsonData.getString("id").toString();
//                Myname = jsonData.getString("name").toString();
//                Mygroup = jsonData.getString("grp").toString();
//                Myaddress = jsonData.getString("address").toString();
//                Toast.makeText(getApplicationContext(), R.string.nothisview,Toast.LENGTH_LONG).show();
            }
            UpdateMysql(Myid, Myname, Mygroup, Myaddress);
        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
    }
    //新增到MySQL
    private void InsertMySQL(String insmyname, String insmygroup, String insmyaddress) {
        String result = DBConnector.executePointInsert("", insmyname, insmygroup, insmyaddress);
    }

    //更新MySQL
    private void UpdateMysql(String upmyid, String upmyname, String upmygroup, String upmyaddress) {
        String result = DBConnector.executePointUpdate("", upmyid, upmyname, upmygroup, upmyaddress);
    }
    private void nowaddress() {
// 取得上次已知的位置
        //確認精密定位，使用者是否有允許權限
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(provider);//包含經緯度，速度，高度
            updateWithNewLocation(location);
            return;
        }
        // 監聽 GPS Listener----------------------------------
// long minTime = 5000;// ms
// float minDist = 5.0f;// meter
//---網路和GPS來取得定位，因為GPS精準度比網路來的更好，所以先使用網路定位、
// 後續再用GPS定位，如果兩者皆無開啟，則跳無法定位的錯誤訊息
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            tmsg.setText("GPS 未開啟");
        else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                tmsg.setText("使用網路GPS");
            }
//------------------------
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                tmsg.setText("使用精確GPS");
            }
        }
    }
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
            Log.d(TAG, "locationListener->onLocationChanged:" + group_map.getCameraPosition().zoom + " currentZoom:"
                    + mapzoom);
            tmsg.setText("目前Zoom:" + group_map.getCameraPosition().zoom);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.v(TAG, "Status Changed: Out of Service");
                    tmsg.setText("Out of Service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.v(TAG, "Status Changed: Temporarily Unavailable");
                    tmsg.setText("Temporarily Unavailable");
                    break;
                case LocationProvider.AVAILABLE:
                    Log.v(TAG, "Status Changed: Available");
                    tmsg.setText("Available");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            tmsg.setText("onProviderEnabled");
            Log.d(TAG, "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
            Log.d(TAG, "onProviderDisabled");
        }
    };
    //不斷更新現在位置
    private void updateWithNewLocation(Location location) {
//        String where = "";
        if (location != null) {
            double lng = location.getLongitude();// 經度
            double lat = location.getLatitude();// 緯度
//            float speed = location.getSpeed();// 速度
//            long time = location.getTime();// 時間
//            String timeString = getTimeString(time);
//            where = "經度: " + lng + "\n緯度: " + lat + "\n速度: " + speed + "\n時間: " + timeString + "\nProvider: "
//                    + provider;
            Myaddress = lat + "," + lng;
//            SelectMysql(Myname);
            // 標記"我的位置"
            showMarkerMe(lat, lng);
            cameraFocusOnMe(lat, lng);
            //---------------------------
//            trackMe(lat, lng);//從這開始畫軌跡圖
            //---------------------------
            //是否使用導航
            if (routeon == 1) {
                u_routeuse(lat, lng);
            }

        } else {
//            where = "*位置訊號消失*";
        }
        // 位置改變顯示
//        txtOutput.setText(where);
    }
    private void showMarkerMe(double lat, double lng) {
        if (markerMe != null) {
            markerMe.remove();
        }
        int resID = getResources().getIdentifier("z00", "drawable", getPackageName());
//------------------
//        if (icosel != 0) {
//            image_des = BitmapDescriptorFactory.fromResource(resID);//使用照片
//        } else {
//            image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);//使用系統水滴
//        }
//-------------------------
        dLat = lat; // 南北緯
        dLon = lng; // 東西經
        String vtitle = "GPS位置:" + "#" + resID;
        String vsnippet = "座標:" + String.valueOf(dLat) + "," + String.valueOf(dLon);
        VGPS = new LatLng(lat, lng);// 更新成欲顯示的地圖座標
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title(vtitle);
        markerOpt.snippet(vsnippet);
        markerOpt.infoWindowAnchor(Anchor_x, Anchor_y);
        markerOpt.draggable(true);
//        markerOpt.icon(image_des);
        markerMe = group_map.addMarker(markerOpt);
    }
    private void cameraFocusOnMe(double lat, double lng) {
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(group_map.getCameraPosition().zoom)
                .build();
        /* 移動地圖鏡頭 */
        group_map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
        tmsg.setText("目前Zoom:" + group_map.getCameraPosition().zoom);
    }
    //導航功能
    private void u_routeuse(double lat, double lng) {
        //抓下選取的位址
        String toWhere = "24.136829,120.685011";
//-----
        //取得SQLite的points欄位
        Cursor cur_routeuse = mContRes.query(GroupContentProvider.CONTENT_URI, MYCOLUMN, null,null,null);
        //抓到2筆以上的相同資料，則進行第一筆。
        cur_routeuse.moveToFirst();
        int bb = 0;
        //SQLite名稱欄位與toWHere必須名稱一致
        String TGPS = cur_routeuse.getString(4);        //SQLite的points欄位

        int aa = 0;

        cur_routeuse.close();
        String[] sLocationb = TGPS.substring(10,TGPS.length()-1).split(",");

        double dLat1 = Double.parseDouble(sLocationb[0]);    // 南北緯
        double dLon1 = Double.parseDouble(sLocationb[1]);    // 東西經
        int k = 4;

////-----

//        /** 起始及終點位置符號顏色      */
        LatLng origin = new LatLng(lat, lng);
        LatLng dest = new LatLng(dLat1, dLon1);
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);//起始點到目的地

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions
        // API
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Key
        // Building the parameters to the web service
        // Travelling Mode-------------------------
//        String mode = "mode=driving";
//           String mode = "mode=bicycling";
//        String mode = "mode=walking";
        //------------------------
//        // Building the parameters to the web service
//        String parameters = str_origin+"&"+str_dest+"&"+key+"&"+mode;
        String parameters = str_origin + "&" + str_dest + "&" + key;

        // Output format
        String output = "json";
//--------
//
//--------
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
//        String aa="test";
        return url;
    }
    //新增集合點到MySQL
    private void mlatLngupload(LatLng mlatLng) {
        String points = mlatLng.toString();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        Bundle bundle1 = this.getIntent().getExtras();
        if (bundle1 != null) {
            String grp_name = bundle1.getString("name");
            nameValuePairs.add(new BasicNameValuePair("grp_name", grp_name));
            nameValuePairs.add(new BasicNameValuePair("creator_id", creator_id));
        }
        nameValuePairs.add(new BasicNameValuePair("points",points));           //將地點上傳到MySQL
        int i = 3;
        //擔心沒有將資料寫入，所以停一下再新增
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = Group_DBConnector.pointsupdate("SELECT * FROM grp", nameValuePairs);
//-----------------------------------------------
    }
    //---------------------------好友
    //新增好友設定的監聽
    private Button.OnClickListener Map_Addfriend = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            AddFriend_DiaLog();
        }
    };
    //按下新增好友的按鈕監聽
    private Button.OnClickListener AddFirend_Btn = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.friend_okbtn:
                    String member_email = friend_edit.getText().toString().trim();
                    String result = DBConnector.executeQuery("SELECT `email` FROM  `as_member` WHERE `email`='"+member_email+"'");
                    if(result.length()>9){

                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("grp_name", grp_name));
                        nameValuePairs.add(new BasicNameValuePair("creator_id", creator_id));
                        nameValuePairs.add(new BasicNameValuePair("alert_state", "false"));
                        nameValuePairs.add(new BasicNameValuePair("member_id",member_email));
                        int i = 1;

                        String result2 = Group_DBConnector.executeInsertFriends(nameValuePairs);
//                        Toast.makeText(getApplicationContext(), "成功加入", Toast.LENGTH_LONG).show();
                        group_AddFriend.cancel();
                    }else{
                        Toast.makeText(getApplicationContext(), "查無這個使用者", Toast.LENGTH_LONG).show();
                    }
                    break;

                case R.id.friend_calbtn:
                    group_AddFriend.cancel();
                    break;
            }
        }
    };
    //新增好友的DIALOG
    private void AddFriend_DiaLog() {
        //設定添加好友DIALOG
        group_AddFriend = new Dialog(Ast_Group_Map.this);
        group_AddFriend.setTitle(getString(R.string.group_map_addfriend));
        group_AddFriend.setCancelable(false);

        group_AddFriend.setContentView(R.layout.ast_group_addfriend);

        friend_edit = (EditText) group_AddFriend.findViewById(R.id.friend_edit);
        friend_okbtn = (Button) group_AddFriend.findViewById(R.id.friend_okbtn);
        friend_calbtn = (Button) group_AddFriend.findViewById(R.id.friend_calbtn);

        group_AddFriend.show();
        friend_okbtn.setOnClickListener(AddFirend_Btn);
        friend_calbtn.setOnClickListener(AddFirend_Btn);
    }

    //----------------------------------------------------------
    //確認是否有連紹網路，若有則將MySQL資料寫入SQLite
    private void dbmysql() {
        mContRes = getContentResolver();
        Cursor cur = mContRes.query(GroupContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur.moveToFirst(); // 一定要寫，不然會出錯
        // // ---------------------------
        try {
            String result = Group_DBConnector.executeQuery("SELECT  *  FROM  `grp`  WHERE  grp_name  =  "  + " '" + grp_name + "'  AND creator_id = " + " '" + creator_id + "'");
//        String r = result.toString().trim();
//==========================================
            //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
            //存取類別成員 Group_DBConnector.httpstate 判定是否回應 200(連線要求成功)
            Log.d(TAG, "httpstate=" + Group_DBConnector.httpstate);
            int aa = Group_DBConnector.httpstate;
            if (Group_DBConnector.httpstate == 200) {
                Uri uri = GroupContentProvider.CONTENT_URI;
                mContRes.delete(uri, null, null);  //清空SQLite
//                Toast.makeText(getBaseContext(), "已經完成由伺服器會入資料",
//                        Toast.LENGTH_LONG).show();
            } else {
                int checkcode = Group_DBConnector.httpstate / 100;
                switch (checkcode) {
                    case 1:
                        msg = "資訊回應(code:" + Group_DBConnector.httpstate + ")";
                        break;
                    case 2:
                        msg = "已經完成由伺服器會入資料(code:" + Group_DBConnector.httpstate + ")";
                        break;
                    case 3:
                        msg = "伺服器重定向訊息，請稍後在試(code:" + Group_DBConnector.httpstate + ")";
                        break;
                    case 4:
                        msg = "用戶端錯誤回應，請稍後在試(code:" + Group_DBConnector.httpstate + ")";
                        break;
                    case 5:
                        msg = "伺服器error responses，請稍後在試(code:" + Group_DBConnector.httpstate + ")";
                        break;
                }
//                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
            }
//======================================
            // 選擇讀取特定欄位
            // String result = Group_DBConnector.executeQuery("SELECT id,name FROM
            // member");
            /*******************************************************************************************
             * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
             * jsonData = new JSONObject(result);
             *******************************************************************************************/
            jsonArray = new JSONArray(result);

            // -------------------------------------------------------
            if (jsonArray.length() > 0) { // MySQL 連結成功有資料
                Uri uri = GroupContentProvider.CONTENT_URI;
                mContRes.delete(uri, null, null); // 匯入前,刪除所有SQLite資料

                // ----------------------------
                // 處理JASON 傳回來的每筆資料
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonData = jsonArray.getJSONObject(i);
                    //
                    newRow = new ContentValues();
                    // --(1) 自動取的欄位
                    // --取出 jsonObject
                    // 每個欄位("key","value")-----------------------
                    Iterator itt = jsonData.keys();
                    while (itt.hasNext()) {
                        String key = itt.next().toString();
                        String value = jsonData.getString(key); // 取出欄位的值
                        if (value == null) {
                            continue;
                        } else if ("".equals(value.trim())) {
                            continue;
                        } else {
                            jsonData.put(key, value.trim());
                        }
                        // ------------------------------------------------------------------
                        newRow.put(key, value.toString()); // 動態找出有幾個欄位
                        // -------------------------------------------------------------------
                        Log.d(TAG, "第" + i + "個欄位 key:" + key + " value:" + value);

                    }
                    // ---(2) 使用固定已知欄位---------------------------
                    // newRow.put("id", jsonData.getString("id").toString());
                    // newRow.put("name",
                    // jsonData.getString("name").toString());
                    // newRow.put("grp", jsonData.getString("grp").toString());
                    // newRow.put("address", jsonData.getString("address")
                    // .toString());
                    // -------------------加入SQLite---------------------------------------
                    mContRes.insert(GroupContentProvider.CONTENT_URI, newRow);
                    int ixcv = 3;
                }
                // ---------------------------
            } else {
//                Toast.makeText(Ast_Group_Map.this, "主機資料庫無資料", Toast.LENGTH_LONG).show();
            }

            int dfa = 3234;
            // --------------------------------------------------------

        } catch (Exception e) {
            Log.e("log_tag", e.toString());
        }
        cur.close();
        //--------------------------------
        sqliteupdate(); // TODO  閃退 暫時先關掉，debug
        //-----------------------------------
    }

    //把SQLite的資料更新
    private void sqliteupdate() {
        Cursor c = mContRes.query(GroupContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        tcount = c.getCount();
        int columnCount = c.getColumnCount();
        // ---------------------------
        String fidSet = "";
        for (int i = 0; i < tcount; i++) {
            c.moveToPosition(i);

            for (int j = 0; j < columnCount; j++) {
                fidSet += c.getString(j) + ", ";
            }
        }
        try {
            String points = c.getString(4); //擷取points欄位
             int year =    c.getInt(5);  //擷取year欄位
             int month =   c.getInt(6); //擷取month欄位
            int day = c.getInt(7);  //擷取day欄位
            int hour = c.getInt(8);  //擷取hour欄位
            int min = c.getInt(9);  //擷取min欄位
            //不斷進迴圈確定是否有設定時間。
            if (alert_state.equals("false") && year != 0) {
                //獲得設定的時間 FROM SQLite

//----------------------------------------------------------------------------------------------
                Calendar cg = Calendar.getInstance();
                cg.set(year, month, day, hour, min);
                endTime = cg.getTimeInMillis();
                //開始跑緒
                handler.postDelayed(updateTimer, 100);       //倒數計時的緒  這裡會一直跑
                //如果有設定時間到MySQL並成功更新至SQLite
            }
            if(points.equals("")){
                Point_Set_Cancel();
            }else{
                Point_Set();
            }
            //若取消鬧鐘或鬧鐘完成則將狀態改為false
//            if (c.isNull(5) && alert_state.equals("true")) {
//                alert_state_off();
//            }
        } catch (Exception e) {

        }

        c.close();

//        MyAlertDialog dialog = new MyAlertDialog(Ast_Group_Map.this);
//        dialog.setTitle("測試用dialog");
//        dialog.setMessage(fidSet);
//        dialog.show();
    }

    //-------------------指定menu選單的layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    //------------------------選單的指令
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //*** 增加 Marker 監聽 使用Animation動畫*/
    @Override
    public boolean onMarkerClick(final Marker marker_Animation) {
        if (!marker_Animation.getTitle().substring(0, 4).equals("Move")) {
            //非GPS移動位置;設定動畫
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500; //連續時間
            final Interpolator interpolator = new BounceInterpolator();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker_Animation.setAnchor(Anchor_x, Anchor_y + 2 * t); //設定標的位置
                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        } else {//GPS移動位置,不使用動畫
            Ast_Group_Map.this.markerMe.hideInfoWindow();
        }
        return false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(getApplicationContext(), "返回GPS目前位置", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }
    /*** 從URL下載JSON資料的方法   **/
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * 解析JSON格式
     **/
    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8); //導航路徑寬度
                lineOptions.color(Color.RED); //導航路徑顏色
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = group_map.addPolyline(lineOptions);

            } else
                Toast.makeText(getApplicationContext(), "找不到路徑", Toast.LENGTH_LONG).show();
        }

    }
}


