package as.traveler.ast_home1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Ast_point_map extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap pointMap; //宣告map物件
    //TODO==================================

    //所需要申請的權限數組
    private static final String[] permissionsArray = new String[]{
            ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION   };

    private List<String> permissionsList = new ArrayList<String>();

    //申請權限後的返回碼
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private LocationManager manager;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private TextView output;
    private Location currentLocation;
    //TODO==================================

//-----路況spinner陣列------
    private static String[] mapType = {
            "街道圖",
            "衛星圖",
            "地形圖",
            "混合圖",
            "開啟路況",
            "關閉路況"};
    private LocationManager locationManager;
    private Spinner mSpnMapType;
    private int currentZoom=14; //倍率0~20
    private String provider;  //提供資料
    private GoogleMap map;
    private  ArrayList<LatLng>mytrace;//追蹤我的位置
    private String point_name;
    private float lat,lng;
    private BitmapDescriptor image_des;
    static LatLng VGPS= new LatLng(24.172127, 120.610313);
    private float Anchor_x=0.5f,Anchor_y=1.0f; //info-window的位置
    private Marker markerMe;
    private int routeon=0;
    private String key="key=" + "";  //需使用 javascriptmap key;
    private String TAG="tcnr11";
    private Polyline mPolyline;
    private long minTime=1000;
    private float minDist=5;
    private double dLat=0,dLon=0;
    float mapzoom = (float) 14.0; //0~20可以有小數點
    private String point_latitude,point_longitude,point_pic,point_address,point_phone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_point_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        u_checkgps();
        setupViewComponent();
        checkRequiredPermission(this);//TODO---詢問權限
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

    private void setupViewComponent() {
        mSpnMapType = (Spinner) this.findViewById(R.id.spnMapType);
        routeon = 1;


        // -----宣告地圖路況adpter----------
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        for (int i = 0; i < mapType.length; i++)
            adapter.add(mapType[i]);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnMapType.setAdapter(adapter);

        //------地圖路況-------
        mSpnMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        pointMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); //道路地圖
                        break;

                    case 1:
                        pointMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); //衛星地圖
                        break;

                    case 2:
                        pointMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN); //地形圖
                        break;

                    case 3:
                        pointMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); //道路地圖混和空照圖
                        break;

                    case 4:
                        pointMap.setTrafficEnabled(true);//開啟路況
                        break;

                    case 5:
                        pointMap.setTrafficEnabled(false); //關閉路況
                        break;

                }
                setMapLocation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    /** 位置變更狀態監視*/
    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }

        @Override
        public void onProviderEnabled(String provider) {
//            tmsg.setText("onProviderEnabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
//                    tmsg.setText("Out of Service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                    tmsg.setText("Temporarily Unavailable");
                    break;
                case LocationProvider.AVAILABLE:
//                    tmsg.setText("Available");
                    break;
            }
        }

    };

    private void setMapLocation() {
        dLat = lat; // 南北緯
        dLon = lng; // 東西經

        int a=0;
        double dLat = Double.parseDouble(point_latitude);    // 南北緯
        double dLon = Double.parseDouble(point_longitude);    // 東西經
        String vtitle = point_name;
        //--- 設定所選位置之當地圖示 ---//
        image_des = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN); //使用系統水滴

        VGPS = new LatLng(dLat, dLon);
        // --- 設定自訂義infowindow ---//
        pointMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        pointMap.setOnMarkerClickListener(this);
        // map.setOnInfoWindowClickListener(this);
        // map.setOnMarkerDragListener(this);
        // --- 根據所選位置項目顯示地圖/標示文字與圖片 ---//

        pointMap.addMarker(new MarkerOptions()
                .position(VGPS)
                .title(vtitle)
                .snippet("座標:" + dLat + "," + dLon)
                .infoWindowAnchor(Anchor_x, Anchor_y)
                .icon(image_des));// 顯示圖標文字


        pointMap.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, currentZoom));
        onCameraChange(pointMap.getCameraPosition());
//        map.setOnMyLocationButtononClickListener(this);
        pointMap.setOnMyLocationButtonClickListener(this);

    }

    private void onCameraChange(CameraPosition cameraPosition) {
//        tmsg.setText("目前Zoom"+map.getCameraPosition().zoom);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //從ast_point收到的值
        Bundle bundle = getIntent().getExtras();
         point_latitude = bundle.getString("point_latitude"); //緯度
         point_longitude = bundle.getString("point_longitude"); //經度
         point_name=bundle.getString("point_name"); //景點名稱
        point_pic=bundle.getString("point_pic"); //景點圖片
        point_address=bundle.getString("point_address"); //景點名稱
        point_phone=bundle.getString("point_phone"); //景點圖片

        //將接收到的緯經度從string轉換成float型態
        lat =Float.parseFloat(point_latitude);
        lng =Float.parseFloat(point_longitude);

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

        // Add a marker in Sydney and move the camera
        LatLng point_site = new LatLng(lat,lng);
        pointMap.addMarker(new MarkerOptions().position(point_site).title(point_name));
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
    private void checkRequiredPermission(Ast_point_map ast_point_map) {
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(ast_point_map, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        if (permissionsList.size()!=0) {
            ActivityCompat.requestPermissions(ast_point_map, permissionsList.toArray(new
                    String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int i=0; i<permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), permissions[i]+"權限申請成功!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "權限被拒絕： "+permissions[i], Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        //checkRequiredPermission(this)   //檢查SDK版本，確認是否獲得權限
        if (initLocationProvider()){
            nowaddress();
        }else{
            Toast.makeText(getApplicationContext(), "GPS未開啟，請先開啟定位!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean initLocationProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        }
        return false;
    }

    private void nowaddress() {
        // 取得上次已知的位置
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(provider);
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
            Toast.makeText(getApplicationContext(), "GPS未開啟", Toast.LENGTH_LONG).show();
        else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                Toast.makeText(getApplicationContext(), "使用網路gps", Toast.LENGTH_LONG).show();
            }
//------------------------
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                Toast.makeText(getApplicationContext(), "使用精確gps", Toast.LENGTH_LONG).show();
            }
        }
    }
    // -------- 地圖縮放 -------------------------------------------
    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) return;
        pointMap.getUiSettings().setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    // ---------------設定指北針----------------------------------------------
    public void setCompassEnabled(View v) {
        if (!checkReady()) return;
        pointMap.getUiSettings().setCompassEnabled(((CheckBox) v).isChecked());
    }

    // -----顯示 我的位置座標圖示
    public void setMyLocationLayerEnabled(View v) {
        if (!checkReady()) return;

        //----------取得定位許可-----------------------
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            pointMap.setMyLocationEnabled(((CheckBox) v).isChecked());
        } else {
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        }
    }

    // ---- 可用捲動手勢操控,用手指平移或捲動來拖曳地圖
    public void setScrollGesturesEnabled(View v) {
        if (!checkReady()) return;
        pointMap.getUiSettings().setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    // ---- 縮放手勢 按兩下 按一下 或兩指拉大拉小----
    public void setZoomGesturesEnabled(View v) {
        if (!checkReady()) return;
        pointMap.getUiSettings().setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }

    // ---- 傾斜手勢 改變地圖的傾斜角度 兩指上下拖曳來增加/減少傾斜角度----
    public void setTiltGesturesEnabled(View v) {
        if (!checkReady()) return;
        pointMap.getUiSettings().setTiltGesturesEnabled(((CheckBox) v).isChecked());
    }

    // ---- 旋轉手勢 兩指旋轉地圖  ----
    public void setRotateGesturesEnabled(View v) {
        if (!checkReady()) return;
        pointMap.getUiSettings().setRotateGesturesEnabled(((CheckBox) v).isChecked());
    }

    private boolean checkReady() {
        if (pointMap == null) {
            Toast.makeText(this, "地圖設置尚未完成", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateWithNewLocation(Location location) {
        String where = "";
        if (location != null) {
            double lng = location.getLongitude();// 經度
            double lat = location.getLatitude();// 緯度
            float speed = location.getSpeed();// 速度
            long time = location.getTime();// 時間
            String timeString = getTimeString(time);
            where = "經度: " + lng + "\n緯度: " + lat + "\n速度: " + speed + "\n時間: " + timeString + "\nProvider: "
                    + provider;
            // 標記"我的位置"
            showMarkerMe(lat, lng);
            cameraFocusOnMe(lat, lng);

            trackMe(lat,lng);  //畫軌跡圖
            //是否使用導航
            if (routeon == 1){
                u_routeuse(lng,lat);
            }
        } else {
            where = "*位置訊號消失*";
        }
    }

    private String getTimeString(long timeInMilliseconds) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);
    }

    private void showMarkerMe(double lat, double lng) {
        if (markerMe != null) {
            markerMe.remove();
        }

//------------------

//            image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);//使用系統水滴

//-------------------------
        dLat = lat; // 南北緯
        dLon = lng; // 東西經
        String vtitle = "我的位置" ;
        String vsnippet = "座標:" + String.valueOf(dLat) + "," + String.valueOf(dLon);
        VGPS = new LatLng(lat, lng);// 更新成欲顯示的地圖座標
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title(vtitle);
        markerOpt.snippet(vsnippet);
        markerOpt.infoWindowAnchor(Anchor_x, Anchor_y);
        markerOpt.draggable(true);
        markerOpt.icon(image_des);

//        markerMe = pointMap.addMarker(markerOpt);  //我的位置也加入系統水滴
    }


    private void cameraFocusOnMe(double lat, double lng) {
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(pointMap.getCameraPosition().zoom)
                .build();
        /* 移動地圖鏡頭 */
        pointMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));

    }

    private void trackMe(double lat, double lng) {
        //加軌跡圖
        if (mytrace == null) {
            mytrace = new ArrayList<LatLng>();
        }
        mytrace.add(new LatLng(lat, lng));
        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : mytrace) {
            polylineOpt.add(latlng);
        }
        polylineOpt.color(Color.BLUE); // 軌跡顏色
        Polyline line = pointMap.addPolyline(polylineOpt);
        line.setWidth(15); // 軌跡寬度      數字越大越寬越小越細
//---
        line.setPoints(mytrace);
        //    ----虛線-----
//    private void trackMe(double lat, double lng) {
//        if (mytrace == null) {
//            mytrace = new ArrayList<LatLng>();
//        }
//        mytrace.add(new LatLng(lat, lng));
//        PolylineOptions polylineOpt = new PolylineOptions()
//                .geodesic(true)
//                .color(Color.CYAN)
//                .width(10)
//                .pattern(PATTERN_POLYGON_ALPHA);
//
////        polylineOpt.addAll(Polyline.getPoints(mytrace));
////        polylinePaths.add(mGoogleMap.addPolyline(polylineOpt));
//
////        for (LatLng latlng : mytrace) {
////            polylineOpt.add(latlng);
////        }
//        // -----***軌跡顏色***-----
//        polylineOpt.color(Color.rgb(188 ,143,143));
//        Polyline line = map.addPolyline(polylineOpt);
//        line.setWidth(10); // 軌跡寬度
//        line.equals(10);
//        line.setPoints(mytrace);
//
//    }
    }

    private void u_routeuse(double lng, double lat) {


        double dLat1 = Double.parseDouble(point_latitude);    // 南北緯
        double dLon1 = Double.parseDouble(point_longitude);    // 東西經


//        /** 起始及終點位置符號顏色      */
        LatLng origin = new LatLng(lat, lng); //起始點
        LatLng dest = new LatLng(dLat1, dLon1); //目的地
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(origin, dest);

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
        String parameters = str_origin+"&"+str_dest+"&"+key;

        // Output format
        String output = "json";
//--------
//
//--------
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
//        String aa="test";
        return url;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getApplicationContext(), "返回GPS目前位置", Toast.LENGTH_SHORT).show();
        return true;
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
                Ast_point_map.this.finish();
                break;
        }
        return true;
    }

    //*** 增加 Marker 監聽 使用Animation動畫*/
    @Override
    public boolean onMarkerClick(final Marker marker_Animation) {
        if (!marker_Animation.getTitle().substring(0, 4).equals("Move")) {
            //非GPS移動位置;設定動畫
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500; //連續時間
            final BounceInterpolator interpolator = new BounceInterpolator();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker_Animation.setAnchor(Anchor_x, Anchor_y + 2 * t); //設定標的位置
                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }} });
        } else {//GPS移動位置,不使用動畫
            Ast_point_map.this.markerMe.hideInfoWindow();
        }
        return false;
    }
    //=====================副程式===========================

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override

        public View getInfoWindow(Marker marker) {
            // 依指定layout檔，建立地標訊息視窗View物件
            // --------------------------------------------------------------------------------------
            // 單一框
            // View infoWindow=
            // getLayoutInflater().inflate(R.layout.custom_info_window,
            // null);
            // 有指示的外框
            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_content, null);
            infoWindow.setAlpha(0.8f); //透明度
            // ----------------------------------------------
            // 顯示地標title
            TextView title = ((TextView) infoWindow.findViewById(R.id.title));
            String[] ss = marker.getTitle().split("#");
            title.setText(ss[0]);
            // 顯示地標snippet
            TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
            snippet.setText(marker.getSnippet());

            //--測試--
            TextView tel = ((TextView) infoWindow.findViewById(R.id.tel));
            TextView addr = ((TextView) infoWindow.findViewById(R.id.addr));
            tel.setText(point_phone);
            addr.setText(point_address);

            // 顯示圖片
            ImageView imageview = ((ImageView) infoWindow.findViewById(R.id.content_ico));
//            imageview.setImageResource(Integer.parseInt(ss[1]));
            Picasso.get().load(point_pic).into(imageview); //將bundle帶來的網路圖片網址放入



            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {

            //按下去會做的事情，ex:導航等
            Toast.makeText(getApplicationContext(), "getInfoContents", Toast.LENGTH_LONG).show();
            return null;
        }
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

    /** 解析JSON格式     **/
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
                //========改這裡就可以了=========//
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = pointMap.addPolyline(lineOptions);

            } else
                Toast.makeText(getApplicationContext(), "找不到路徑", Toast.LENGTH_LONG).show();
        }

    }
}
