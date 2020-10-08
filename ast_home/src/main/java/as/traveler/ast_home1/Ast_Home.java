package as.traveler.ast_home1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class Ast_Home extends AppCompatActivity implements View.OnClickListener {

    private ImageButton home_account;
    private Button home_search,home_whereToGo,home_check;
    private ImageButton home_nearby;
    private ImageButton trips01, trips02, trips03, trips04, home_tripsadd;
    private Button home_rct_more;
    private Uri uri;
    private Intent it;
    private BottomNavigationView ast_Bottom;
    final private int LOGOUT_LOGIN = 0;

    //SQLiteDataBase
    private static final String DB_FILE = "Account.db", DB_TABLE = "member";
    private static final int DBversion=1;
    private int logincheck;
    private ListView hotelListView;

    //--------------------------
    private static ContentResolver mContRes;
    private String[] MYCOLUMN = new String[]{"id", "name", "grp", "address"};
    String tname, tgrp, taddr, s_id, taddress;
    int tcount;
    // ------------------

    //Opendata
    private Handler handler =new Handler();
    private ConstraintLayout ast__home;
    private MediaPlayer startMusic;
    private static int aa=0;
    private ImageButton hotel_imgBtn;

    //取得定位
    private GoogleMap mMap;
    private static final String[] permissionsArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private List<String> permissionsList = new ArrayList<String>();
    //==========================================
    //申請權限後的返回碼
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private LocationManager manager;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=2;
    private TextView output;
    private Location currentLocation;
    private String TAG="tcnr03=>";


    // 更新位置頻率的條件
    int minTime = 2000; // 毫秒
    float minDistance = 3; // 公尺
    private String bestgps;
    private String email;
    private int flag=0;
    private MenuItem gps_open,gps_close;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ast__home);
        //-------------抓取遠端資料庫設定執行續------------------------------
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
        mContRes = getContentResolver();
//---------------------------------------------------------------------
        setupViewComponent();
        u_loaddata();//傳flag進來 1是登入 0是登出
        u_checkgps();

        //-----------建立BottomNavigationView物件
        ast_Bottom = (BottomNavigationView) findViewById(R.id.ast_Bottom);

        ast_Bottom.setSelectedItemId(R.id.ast_act);
        BottomNavigationHelper.removeShiftMode(ast_Bottom);  // 生一個外部的class
        ast_Bottom.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ast_Bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ast_act:

                        return true;
                    case R.id.ast_trip:
                        startActivity(new Intent(getApplicationContext(), Ast_trip.class));
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_group:
                        startActivity(new Intent(getApplicationContext(), Ast_Group.class));
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_col:
                        startActivity(new Intent(getApplicationContext(), Ast_Col_File.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.ast_more:
                        startActivity(new Intent(getApplicationContext(), Ast_More.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    private void setupViewComponent() {
        home_account = (ImageButton)findViewById(R.id.account);
        home_account.setOnClickListener(logIn);

        home_whereToGo = (Button) findViewById(R.id.whereToGo);
        home_check = (Button) findViewById(R.id.check);

        home_whereToGo.setOnClickListener(this);
        home_check.setOnClickListener(this);
        home_check.getBackground().setAlpha(175);//設定按鈕底色透明度

        //============探索鄰近地區============//
        home_nearby = (ImageButton) findViewById(R.id.nearby_imgBtn);
        home_nearby.setOnClickListener(this);

        //==============找飯店==============//
        hotel_imgBtn=(ImageButton)findViewById(R.id.hotel_imgBtn);
        hotel_imgBtn.setOnClickListener(this);

        if(aa==0) {
            //==============開機動畫==============//
//            ast__home = (ConstraintLayout) findViewById(R.id.ast__home);
//            ast__home.setAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_all_in));
            //==============開機音樂==============//
            startMusic = MediaPlayer.create(getApplication(), R.raw.strange_bell);
            startMusic.start();
            aa++;
        }
        //==========連結到行程的按鈕==========//
        trips01 = (ImageButton) findViewById(R.id.trips01);
        trips02 = (ImageButton) findViewById(R.id.trips02);
        trips03 = (ImageButton) findViewById(R.id.trips03);
        trips04 = (ImageButton) findViewById(R.id.trips04);
        home_tripsadd = (ImageButton) findViewById(R.id.trips_add);

        trips01.setOnClickListener(this);
        trips02.setOnClickListener(this);
        trips03.setOnClickListener(this);
        trips04.setOnClickListener(this);
        home_tripsadd.setOnClickListener(this);

        //============查看所有活動============//
        home_rct_more = (Button) findViewById(R.id.rct_more);
        home_rct_more.setOnClickListener(this);

    }

    private Button.OnClickListener logIn= new Button.OnClickListener(){

        @Override
        public void onClick(View v) {//登入帳號
            switch (v.getId()){
                case R.id.account:
                    if(logincheck==1){
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), Ast_Member.class);
                        startActivity(intent);
                    }else{
                        Intent it =new Intent();
                        it.setClass(getApplicationContext(), Ast_Login.class);
                        startActivity(it);
                        //告訴下一個class要回傳值
                    }
                    break;
            }
        }
    };



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check://"查看鄰近地點"按鈕
                startActivity(new Intent(getApplicationContext(), Ast_place.class));
                break;
            case R.id.nearby_imgBtn://"探索鄰近地點和觀光"按鈕
                startActivity(new Intent(getApplicationContext(), Ast_place.class));
                break;
            case R.id.whereToGo:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), Ast_act.class);
                startActivity(intent);
                break;
            case R.id.hotel_imgBtn:
                Intent goToHotel = new Intent();
                goToHotel.setClass(getApplicationContext(), Ast_Hotel.class);
                startActivity(goToHotel);
                break;
        }
    }



    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        logincheck = login.getInt("flag", 0);
        email = login.getString("Email", "0");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.onBackPressed), Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ast_home_menu, menu);
        gps_open = menu.findItem(R.id.gps_open);
        gps_close = menu.findItem(R.id.gps_close);
        gps_open.setVisible(true);
        gps_close.setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_setting:
                Ast_Home.this.finish();
                break;
            case R.id.gps_open:
                if (logincheck==1){
                    flag=1;
                    uploaddata(flag);
                    gps_open.setVisible(false);
                    gps_close.setVisible(true);
                }else {
                    Toast.makeText(getApplicationContext(), "請先登入會員", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.gps_close:
                flag=0;
                uploaddata(flag);
                gps_open.setVisible(true);
                gps_close.setVisible(false);
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkRequiredPermission(this);
    }

    private void checkRequiredPermission(final Activity activity ) {
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        if (permissionsList.size()!=0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new
                    String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    private void u_checkgps() {
        //取得系統服務的LocationManager物件
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 檢查是否有啟用GPS
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("定位管理")
                    .setMessage("GPS目前狀態尚未啟用\n"
                            +"請問你是否現在就設定啟用GPS")
                    .setPositiveButton("啟用", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("不啟用", null).create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // 建立定位服務的傾聽者物件
    private LocationListener listener = new LocationListener() {
        //手機回傳現在位置
        @Override
        public void onLocationChanged(Location location) {
            currentLocation =location;
            updatePosition();
        }
        //信號消息時 保持現在位置
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    private void updatePosition() {
        if (currentLocation == null) {
            Toast.makeText(getApplicationContext(), "取得定位資訊中...", Toast.LENGTH_LONG).show();
        } else {
            getLocationInfo(currentLocation);//自訂義函數
            Toast.makeText(getApplicationContext(), "上傳定位資訊中...", Toast.LENGTH_LONG).show();
        }
    }
    // 取得定位資訊
    public String getLocationInfo(Location location) {
        u_loaddata();
        StringBuffer str = new StringBuffer();
        str.append("定位方式(Provider): " + location.getProvider());
        str.append("\n緯度(Latitude): " + Double.toString(location.getLatitude()));
        str.append("\n經度(Longitude): " + Double.toString(location.getLongitude()));
        str.append("\n高度(Altitude): " + Double.toString(location.getAltitude()));
        str.append("\n速度(Speed): " + Double.toString(location.getSpeed()));
        String la=Double.toString(location.getLatitude());
        String lon=Double.toString(location.getLongitude());
        mysql_insert_location(email,la,lon);
        return str.toString();
    }
    //寫入MySQL
    private void mysql_insert_location(String mail,String g_la,String g_lon) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("email", mail));
        nameValuePairs.add(new BasicNameValuePair("latitude", g_la));
        nameValuePairs.add(new BasicNameValuePair("longitude", g_lon));
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.executeUpdateLocation("SELECT * FROM  as_member ", nameValuePairs);
//-----------------------------------------------
    }
    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override
    protected void onResume() {
        super.onResume();

    }
    private void uploaddata(int a){
        if (a == 1) {
            Criteria criteria = new Criteria();
            bestgps = manager.getBestProvider(criteria, true);

            try {
                if (bestgps != null) { // 取得快取的最後位置,如果有的話
                    currentLocation = manager.getLastKnownLocation(bestgps);
                    manager.requestLocationUpdates(bestgps, minTime, minDistance, listener);
                } else { // 取得快取的最後位置,如果有的話
                    currentLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            minTime, minDistance, listener);
                }
            } catch (SecurityException e) {
                Log.e(TAG, "GPS權限失敗..." + e.getMessage());
            }
            updatePosition(); // 更新位置
        }else {
        Toast.makeText(getApplicationContext(), "已關閉定位",Toast.LENGTH_LONG).show();
        }
    }
}
