package as.traveler.ast_home1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
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
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import as.traveler.ast_home1.BuildConfig;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Ast_place extends AppCompatActivity implements OnMapReadyCallback {
    //=======範圍內的都需要==================
    //所需要申請的權限陣列
    private static final String[] permissionsArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private List<String> permissionsList = new ArrayList<String>();

    //申請權限後的返回碼
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    //==================================
    private LocationManager locationManager;
    private GoogleMap map;
    static LatLng VGPS = new LatLng(24.172127, 120.610313);
    float mapzoom = 17;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields;
    private TextView txtOutput;
    private String name;
    private BitmapDescriptor image_des;
    private LatLng latlng;
    private String address;
    private String uid;
    private HashMap<String, Object> item;
    private ArrayList<HashMap<String, Object>> mList = new ArrayList<>();
    private ListView place_listview;
    private ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
    private ArrayList<String> idArray =  new ArrayList<String>();
    private FindCurrentPlaceResponse response;
    private String TAG = "TCNR01=>";
    private ArrayList<String> nameArray = new ArrayList<String>();
    private ArrayList<LatLng> latlngArray = new ArrayList<LatLng>();
    private float Anchor_x = 0.5f;
    private float Anchor_y = 0.9f;
    private Marker markerMe;
    private int resID = 0;
    private MenuItem findCurrentPlace;
    private String provider;
    private long minTime = 2000; //ms
    private float minDist = 2.0f; //meter
    private Intent intent01= new Intent();;
    private String get_latlng;
    private ArrayList<String> addressArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_place);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        checkRequiredPermission(this);
        setupViewComponent();
    }

    private void setupViewComponent() {
        this.setTitle(getString(R.string.findCurrentPlace));
        //設定API_KEY
        final String apiKey = BuildConfig.PLACES_API_KEY;

        if (apiKey.equals("")) {
            Toast.makeText(this, getString(R.string.error_api_key), Toast.LENGTH_LONG).show();
            return;
        }
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
//            Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));//在value放KEY可以改用這方法
        }
        // Retrieve a PlacesClient
        placesClient = Places.createClient(this);

        // Set view objects 設定你想從Places SDK取得哪一些內容
        //注意詢問通信方式以及價位 評比之類的內容會額外增加收費基準
        //這個current place已經是最貴的，別再加了
         placeFields =
                Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.PHOTO_METADATAS,
                        Place.Field.ADDRESS,
                        Place.Field.LAT_LNG
                );
        place_listview = (ListView) findViewById(R.id.place_listview);
    }

    private void getPlaces() {
        //Build the request...
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();
        //studio自動要求我再加這個查詢權限...就加吧，反正checkRequiredPermission()已經事先處理過了
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //Start the place response task...
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);

        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Access the results..
                response = task.getResult();

                //Loop through each likely nearby place..
                if (response != null) {
                    for (PlaceLikelihood place : response.getPlaceLikelihoods()) {
                            item = new HashMap<String, Object>();
                        //Access place fields
                        //取得內容，設定在這
    //                        txtOutput.setText(place.getPlace().getName()+"#"+place.getPlace().getLatLng()+"#"+place.getPlace().getAddress()+"#"+place.getPlace().getId());
                            name = place.getPlace().getName();//取得地點名稱
                            latlng = place.getPlace().getLatLng();//取得經緯度座標
                            address = place.getPlace().getAddress();//取得地址
                            uid = place.getPlace().getId();//取得景點ID

                            item.put("name",name);
    //                        item.put("latlng",latlng);
                            item.put("address",address);
    //                        item.put("uid",uid);
                            mList.add(item);

                            idArray.add(uid);//寫入UID陣列
                            nameArray.add(name);
                            latlngArray.add(latlng);
                            addressArray.add(address);

                        image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);//使用不同顏色的系統水滴
                        map.addMarker(new MarkerOptions()
                                .position(latlng)
                                .title(name)
                                .alpha(0.9f)
                                .snippet("地址:" +address)
                                .infoWindowAnchor(0.5f,0.9f)//錨點對準的位置
    //                    .draggable(true)
                                .icon(image_des));//顯示圖標文字
                    }
                }
                showdata();//資料抓到了，就去顯示下方的listView
            }
        }).addOnFailureListener(e -> {
            //Something went wrong, handle the error...
//            txtOutput.setText(e.getMessage());//取得錯誤代碼跟訊息顯示出來
        });
    }

    private void showdata() {
        //==========設定listView============
        //自定義的adapter
        MyAdapter adapter = new MyAdapter(
                getApplicationContext(),
                mList,
                R.layout.place_list,
                new String[]{"name","address"},
                new int[]{R.id.place_name,R.id.place_address}
        );
        place_listview.setAdapter(adapter);//將抓取的資料設定到表格視窗
        place_listview.setOnItemClickListener(onClick);//建立表格視窗按鈕監聽
    }

    private final ListView.OnItemClickListener onClick = new ListView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            cameraFocusOnTarget(latlngArray.get(position));
//準備在這邊寫連接到景點介紹的頁面 TODO
            get_latlng = String.valueOf(latlngArray.get(position));
            String[] lat_lng= get_latlng.substring(10,get_latlng.length()-1).split(",");
            int aa = 0;
            intent01.setClass(Ast_place.this, Ast_Point.class)
                    .putExtra("uid", idArray.get(position))
                    .putExtra("name", nameArray.get(position))
                    .putExtra("lat", lat_lng[0])
                    .putExtra("lng", lat_lng[1])
                    .putExtra("address", addressArray.get(position));
            startActivity(intent01);
        }
    };

    private void cameraFocusOnTarget(LatLng latlng) {
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(latlng)
                .zoom(map.getCameraPosition().zoom)
                .build();
        /* 移動地圖鏡頭 */
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
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
        map = googleMap;
//        mUiSettings = map.getUiSettings();//
//        開啟 Google Map 拖曳功能
        map.getUiSettings().setScrollGesturesEnabled(true);

//        右下角的導覽及開啟 Google Map功能
        map.getUiSettings().setMapToolbarEnabled(true);

//        左上角顯示指北針，要兩指旋轉才會出現
        map.getUiSettings().setCompassEnabled(true);

//        右下角顯示縮放按鈕的放大縮小功能
        map.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker in 中區職訓 and move the camera
        map.addMarker(new MarkerOptions().position(VGPS).title("中區職訓"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS,mapzoom));
//        map.moveCamera(CameraUpdateFactory.newLatLng(VGPS));
        //------取得定位許可---------
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        } else {
            //----顯示我的位置ICO-------
            map.setMyLocationEnabled(true);//要先有定位才能看到"我的位置"的按鈕
            return;
        }
    }

    private void checkRequiredPermission(final Activity activity) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int i=0; i<permissions.length; i++) {
                    //寫了陣列，有幾個就問幾個
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

    //檢查GPS是否開啟
    private boolean initLocationProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER; //這裡會出現GPS或wifi之類文字的訊息
            return true;
        }
        return false;
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
        if (!(isGPSEnabled || isNetworkEnabled)){
//            txtOutput.setText(getString(R.string.place_no_GPS));
        }else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                txtOutput.setText(getString(R.string.place_use_NETWORK));
            }
//------------------------
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                txtOutput.setText(getString(R.string.place_useFine_GPS));
            }
        }
    }

    LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.v(TAG, "Status Changed: Out of Service");
//                    txtOutput.setText("Out of Service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.v(TAG, "Status Changed: Temporarily Unavailable");
//                    txtOutput.setText("Temporarily Unavailable");
                    break;
                case LocationProvider.AVAILABLE:
                    Log.v(TAG, "Status Changed: Available");
//                    txtOutput.setText("Available");
                    break;
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onProviderEnabled(String provider) {
//            txtOutput.setText("onProviderEnabled");
            Log.d(TAG, "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
            Log.d(TAG, "onProviderDisabled");
        }
    };

    private void updateWithNewLocation(Location location) {
        String where = "";
        if (location != null) {
            double lng = location.getLongitude();// 經度
            double lat = location.getLatitude();// 緯度

            cameraFocusOnMe(lat, lng);

        } else {
            where = "*位置訊號消失*";
        }
        // 位置改變顯示
//        txtOutput.setText(where);
    }

    private void cameraFocusOnMe(double lat, double lng) {
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(map.getCameraPosition().zoom)
                .build();
        /* 移動地圖鏡頭 */
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.onBackPressed), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(initLocationProvider()){
            nowaddress();
        }else{
//            txtOutput.setText(getString(R.string.place_no_location));
            Toast.makeText(getApplicationContext(), getString(R.string.place_no_location), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        findCurrentPlace = menu.findItem(R.id.findCurrentPlace);
        findCurrentPlace.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it = new Intent();
        switch (item.getItemId())
        {
            case R.id.action_settings:
                this.finish();
                findCurrentPlace.setVisible(false);
                break;
            case R.id.findCurrentPlace:
                new AlertDialog.Builder(this)//這裡要用this
                        .setTitle(R.string.findCurrentPlace)
                        .setMessage(R.string.findCurrentPlace_message)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.btn_star_big_on)
                        .setNeutralButton(R.string.findCurrentPlace_chk, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getPlaces();
                            }
                        })
                        .setPositiveButton(R.string.findCurrentPlace_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    public class MyAdapter extends SimpleAdapter {

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
                img = (ImageView) v.findViewById(R.id.place_photo);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                v.setTag(img); // <<< THIS LINE !!!!
            }
//---------------google提供的呼叫照片的方式------------------
// Define a Place ID.
            final String placeId = idArray.get(position);//這邊填入景點UID

// Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
            final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

// Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
            final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

            ImageView finalImg = img;//另外定義img給finalimg  或者要考慮把final拔掉
            placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                final Place place = response.getPlace();

                // Get the photo metadata.
                final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                if (metadata == null || metadata.isEmpty()) {
                    Log.w(TAG, "No photo metadata.");
                    return;
                }
                final PhotoMetadata photoMetadata = metadata.get(0);

                // Get the attribution text.
                final String attributions = photoMetadata.getAttributions();

                // Create a FetchPhotoRequest.
                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500) // Optional.
                        .setMaxHeight(300) // Optional.
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    finalImg.setImageBitmap(bitmap);// 在這裡設定照片
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        final ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + exception.getMessage());
                        final int statusCode = apiException.getStatusCode();
                        // TODO: Handle error with given status code.
                    }
                });
            });
//---------------------------------
            return v;
        }
    }

}
