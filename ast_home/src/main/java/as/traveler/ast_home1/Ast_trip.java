package as.traveler.ast_home1;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import as.traveler.ast_home1.providers.ScheduleContentProvider;

public class Ast_trip extends AppCompatActivity implements View.OnClickListener {


    private FloatingActionButton trip_floating;
    private Dialog newDlg,updateDlg;
    private Button b01;
    private EditText e01;
    private Spinner s01;
    private int sdays;
    private String tripname,departuretime;
    private ArrayList<Map<String,Object>> mList= new ArrayList<>();
    private String enddaytext;
    private int syear;
    private int smonth;
    private int sdayOfMonth;
    private BottomNavigationView ast_Bottom;
    private String stime;
    private String now_time;

    private ContentResolver mContRes;
    private String[] MYCOLUMN=new String[]{"id","name","departuretime","days","email"};
    private int tcount;
    private String msg;

    //RecyclerView
    RecyclerView mRecyclerView;
    ScheduleListAdapter scheduleListAdapter;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    //    下拉刷新
    SwipeRefreshLayout swipeRefreshLayout;

    //抓取登入者email
    private int loginon;
    private String loginemail;

    //選擇時間
    private Calendar selectdate;
    private Calendar nowtime;

    private Handler mysql =new Handler();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_trip);
        setTitle("行程");


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



            setupViewcomponent();
//
        //-----------建立BottomNavigationView物件
        ast_Bottom = (BottomNavigationView) findViewById(R.id.ast_Bottom);

        ast_Bottom.setSelectedItemId(R.id.ast_trip);
        BottomNavigationHelper.removeShiftMode(ast_Bottom);  // 生一個外部的class
        ast_Bottom.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ast_Bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ast_act:
                        Ast_trip.this.finish();
                        return true;
                    case R.id.ast_trip:

                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_group:
                        startActivity(new Intent(getApplicationContext(), Ast_Group.class));
                        Ast_trip.this.finish();//避免堆疊，本頁結束
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_col:
                        startActivity(new Intent(getApplicationContext(), Ast_Col_File.class));
                        Ast_trip.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.ast_more:
                        startActivity(new Intent(getApplicationContext(), Ast_More.class));
                        Ast_trip.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    private void setupViewcomponent() {
        progressBar=findViewById(R.id.progressBar);
        nowtime=Calendar.getInstance();  //抓取現在時間
        trip_floating=(FloatingActionButton)findViewById(R.id.trip_floating);
        trip_floating.setOnClickListener(this);

        u_loaddata();       //抓取email
        if(loginon!=1) {
            Intent log = new Intent();
            //從這到
            log.setClass(Ast_trip.this, Ast_Login.class);
            Toast.makeText(this, R.string.pleaselogin,Toast.LENGTH_SHORT).show();
            startActivity(log);

        }else{

            //更新
            mysql.postDelayed(updatemysql, 1000);

            //設置RecycleView
            mRecyclerView = findViewById(R.id.recycleview);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));//為RecyclerView每個item畫底線
            scheduleListAdapter = new ScheduleListAdapter();
            mRecyclerView.setAdapter(scheduleListAdapter);

            swipeRefreshLayout = findViewById(R.id.refreshLayout);
            //        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.blue_RURI));    下拉轉圈顏色


            //更新adapter
            swipeRefreshLayout.setOnRefreshListener(()->{

                arrayList.clear();          //清空
                mysql.postDelayed(updatemysql, 1000);                 //重讀一次mysql匯入資料

                scheduleListAdapter.notifyDataSetChanged();         //通知adapter更新
                swipeRefreshLayout.setRefreshing(false);                //關掉轉圈圈

            });
        }





    }//onCreate
    private Runnable updatemysql=new Runnable() {
        @Override
        public void run() {

            ast_Bottom.setVisibility(View.GONE);
            trip_floating.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.GONE);
            LinearLayout progressbarlinear = findViewById(R.id.progressbarlinear);
            progressbarlinear.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            dbmysql();

            progressbarlinear.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            ast_Bottom.setVisibility(View.VISIBLE);
            trip_floating.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    };
    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        loginon = login.getInt("flag", 0);
        loginemail = login.getString("Email", "抓取失敗");

    }



    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.trip_floating:

                    newDlg = new Dialog(Ast_trip.this);
                    newDlg.setTitle(getString(R.string.newdialog)); //此段會被後面layout蓋過
                    newDlg.setCancelable(false);
                    newDlg.setContentView(R.layout.ast_trip_new); //選擇layout
                    newDlg.show();

                    //在此宣告dialog裡的物件
                    Button OK_btn = (Button) newDlg.findViewById(R.id.trip_new_b04);
                    Button CANCEL_btn = (Button) newDlg.findViewById(R.id.trip_new_b03);

                    OK_btn.setOnClickListener(this);
                    CANCEL_btn.setOnClickListener(this);

                    e01 = (EditText) newDlg.findViewById(R.id.trip_new_e01);
                    b01 = (Button) newDlg.findViewById(R.id.trip_new_b01);
                    s01 = (Spinner) newDlg.findViewById(R.id.trip_new_s01);
                    b01.setOnClickListener(this);
                    //設定spinner內容製作adapter
                    //設定adapter名稱adapterselectday
                    ArrayAdapter<CharSequence> adapterselectday = ArrayAdapter
                            .createFromResource(this, R.array.selectday, android.R.layout.simple_spinner_item);
                    //選擇spinner的內容樣式
                    adapterselectday.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    s01.setAdapter(adapterselectday);
                    s01.setOnItemSelectedListener(s1on);


                break;
            case R.id.trip_new_b01:
                //DatePickerDialog=日期跳出視窗
                //--日期的對話盒--

                DatePickerDialog dateDlg= new DatePickerDialog(
                        this,    //在現在的layout跳出視窗
                        datelist,
                        nowtime.get(Calendar.YEAR),
                        nowtime.get(Calendar.MONTH),
                        nowtime.get(Calendar.DAY_OF_MONTH));

                dateDlg.setCancelable(false); //鎖定對話盒，不點無法離開
                dateDlg.show();

                break;

            case R.id.trip_new_b04:

                if(b01.getText().toString().equals("請選擇時間")){
                    Toast.makeText(getApplicationContext(), R.string.trip_new_hint01, Toast.LENGTH_SHORT).show();
                }else{
                    if (e01.getText().toString() != "") {         //行程名稱不得為空白
//                        if (Integer.parseInt(now_time) <= Integer.parseInt(stime)) {             //若選擇時間大於現在時間才可按確認

                        newDlg.cancel();        //關閉dialog

//                MySQL
                        mContRes = getContentResolver();
                        Cursor c_add = mContRes.query(ScheduleContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);

                        //======直接增加到MySQL============
                        mysql_insert();
                        mysql.postDelayed(updatemysql, 1000);
                        //==========================
//                            String msg = null;
                        // -------------------------
//                            msg = "新增記錄  成功 ! \n" + "目前資料表共有 " + (c_add.getCount() + 1) + " 筆記錄 !";
//                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        c_add.close();


                        arrayList.clear();//清空列表

                        scheduleListAdapter.notifyDataSetChanged();     //更新列表

//                        } else {
//                            Toast.makeText(this, R.string.seterror, Toast.LENGTH_SHORT).show(); //時間設定錯誤
//                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.seterrorname, Toast.LENGTH_SHORT).show();      //名稱不得為空白
                    }
                }






                break;
            case R.id.trip_new_b03:
                newDlg.cancel();
                break;
        }
    }

    private void dbmysql() {
        mContRes = getContentResolver();
        Cursor cur = mContRes.query(ScheduleContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur.moveToFirst(); // 一定要寫，不然會出錯
        // // ---------------------------
        try {
            String result = SchedualDBConnector.executeQuery(" SELECT *  FROM  schedule WHERE email = '"+loginemail+"' ");  //搜尋登入者帳號的行程
//        String r = result.toString().trim();
//==========================================
            //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
            //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)


            if (SchedualDBConnector.httpstate == 200) {
                Uri uri = ScheduleContentProvider.CONTENT_URI;
                mContRes.delete(uri, null, null);  //清空SQLite

            } else {
                int checkcode= SchedualDBConnector.httpstate/100;

                msg=getString(R.string.error_msg_internet);

                Toast.makeText(getBaseContext(), msg,  Toast.LENGTH_LONG).show();
            }
//======================================
            // 選擇讀取特定欄位
            // String result = DBConnector.executeQuery("SELECT id,name FROM
            // member");
            /*******************************************************************************************
             * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
             * jsonData = new JSONObject(result);
             *******************************************************************************************/
            JSONArray jsonArray = new JSONArray(result);

            // -------------------------------------------------------
            if (jsonArray.length() > 0) { // MySQL 連結成功有資料
                Uri uri = ScheduleContentProvider.CONTENT_URI;
                mContRes.delete(uri, null, null); // 匯入前,刪除所有SQLite資料

                // ----------------------------
                // 處理JASON 傳回來的每筆資料
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    //
                    ContentValues newRow = new ContentValues();
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

                    }

  // -------------------加入SQLite---------------------------------------
                    mContRes.insert(ScheduleContentProvider.CONTENT_URI, newRow);
                }

                // ---------------------------
            } else {
                Toast.makeText(Ast_trip.this, "主機資料庫無資料", Toast.LENGTH_LONG).show();
            }
            // --------------------------------------------------------

        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
        cur.close();

//        更新寫入SQLite
        sqliteSetList();
    }


    private void mysql_insert() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("name", tripname));
        nameValuePairs.add(new BasicNameValuePair("departuretime", departuretime));
        nameValuePairs.add(new BasicNameValuePair("days", Integer.toString(sdays+1)));
        nameValuePairs.add(new BasicNameValuePair("email", loginemail));
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = SchedualDBConnector.executeInsert("SELECT * FROM schedule", nameValuePairs);
//-----------------------------------------------
    }


    //===============設置spinner動作==============================================
    private  OnItemSelectedListener s1on=new OnItemSelectedListener(){
        @Override
        public void  onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //將選項抓出來存為String(抓值用)
            sdays=position;

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            sdays=0;

        }
    };
    //    ====================選取時間dialog==========================
    private DatePickerDialog.OnDateSetListener datelist=new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            s01.performClick();

            String date= Integer.toString(year)+"/"+
                    Integer.toString(month+1)+"/"+
                    Integer.toString(dayOfMonth);

//  把選取時間的字顯示在畫面上
            b01.setText(date );
//  =================選取時間===============================
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            selectdate = Calendar.getInstance();
            try {
                selectdate.setTime(df.parse(date));
            } catch (Exception e) {
            }


            //設變數存取 輸入值
            tripname=e01.getText().toString();
            departuretime=b01.getText().toString();

        }
    };


    private void sqliteSetList() {
        //寫入sqLite
        ArrayList<String> recAry = new ArrayList<String>();
        mContRes = getContentResolver();
        Cursor c = mContRes.query(ScheduleContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        tcount = c.getCount();
        int columnCount = c.getColumnCount();
        while (c.moveToNext()) {
            String fldSet = "";
            for (int ii = 0; ii < columnCount; ii++)
                fldSet += c.getString(ii) + "#";
            recAry.add(fldSet);
        }

        c.close();
        //寫進陣列
        for (int i = 0; i < recAry.size(); i++) {
            String[] fld = recAry.get(i).split("#");
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("id",fld[0]);
            hashMap.put("tripname",fld[1]);
            hashMap.put("departuretime",fld[2]);
            hashMap.put("days",fld[3]);
            arrayList.add(hashMap);

        }

    }



    private class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ViewHolder>{

        private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
        private Button OK_btn_update;
        private Button CANCEL_btn_update;
        private EditText u_e01;
        private Button u_b01;
        private Spinner u_s01;

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView sketch_tripname,sketch_departuretime;
            private Button btDelete,btupdate,sketch_menu;
            private View mView;
            private SwipeRevealLayout swipeRevealLayout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                sketch_tripname = itemView.findViewById(R.id.sketch_t01);
                sketch_departuretime = itemView.findViewById(R.id.sketch_t02);
                btDelete = itemView.findViewById(R.id.button_Delete);
                btupdate= itemView.findViewById(R.id.button_Show);
                swipeRevealLayout = itemView.findViewById(R.id.swipeLayout);
                sketch_menu=itemView.findViewById(R.id.sketch_menu);

                mView= itemView.findViewById(R.id.sketch_linear);

            }
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ast_trip_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            holder.sketch_tripname.setText(arrayList.get(position).get("tripname"));
            holder.sketch_departuretime.setText(arrayList.get(position).get("departuretime"));
            viewBinderHelper.setOpenOnlyOne(true);//設置swipe只能有一個item被拉出
            viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(position));//綁定Layout

            holder.sketch_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.swipeRevealLayout.open(true);
                }
            });


            holder.btupdate.setOnClickListener((v -> {
                holder.swipeRevealLayout.close(true);//關閉已被拉出的視窗

                updateDlg=new Dialog(Ast_trip.this);
                updateDlg.setTitle(getString(R.string.newdialog)); //此段會被後面layout蓋過
                updateDlg.setCancelable(false);
                updateDlg.setContentView(R.layout.ast_trip_update); //選擇layout
                updateDlg.show();

//                //在此宣告dialog裡的物件
                OK_btn_update = (Button) updateDlg.findViewById(R.id.trip_update_b04);
                CANCEL_btn_update=(Button)updateDlg.findViewById(R.id.trip_update_b03);
                u_e01 = (EditText) updateDlg.findViewById(R.id.trip_update_e01);
                u_b01 = (Button) updateDlg.findViewById(R.id.trip_update_b01);
                u_s01 = (Spinner) updateDlg.findViewById(R.id.trip_update_s01);

                u_e01.setText(arrayList.get(position).get("tripname"));
                u_b01.setText(arrayList.get(position).get("departuretime"));
                u_b01.setOnClickListener(b01on);


                //設定spinner內容製作adapter
                //設定adapter名稱adapterselectday
                ArrayAdapter<CharSequence> adapterselectday_update = ArrayAdapter
                        .createFromResource(getApplicationContext(), R.array.selectday, android.R.layout.simple_spinner_item);
                //選擇spinner的內容樣式
                adapterselectday_update.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                u_s01.setAdapter(adapterselectday_update);
                u_s01.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        sdays=position+1;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        sdays=1;
                    }
                });

                OK_btn_update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                    MySQL新增
                        if(u_e01.getText().toString()!=""){         //行程名稱不得為空白

//                            if(Integer.parseInt(now_time)<=Integer.parseInt(stime)) {             //若選擇時間大於現在時間才可按確認

                                String update_name = u_e01.getText().toString().trim();
                                String update_departure = u_b01.getText().toString().trim();

                                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("id", arrayList.get(position).get("id")));
                                nameValuePairs.add(new BasicNameValuePair("name", update_name));
                                nameValuePairs.add(new BasicNameValuePair("departuretime", update_departure));
                                nameValuePairs.add(new BasicNameValuePair("days", String.valueOf(sdays)));
                                nameValuePairs.add(new BasicNameValuePair("email", loginemail));
                                String result = SchedualDBConnector.executeUpdate("SELECT * FROM schedule", nameValuePairs);

                                updateDlg.cancel();
                                arrayList.clear();//清空列表
                            mysql.postDelayed(updatemysql, 1000);


                                scheduleListAdapter.notifyDataSetChanged();     //更新列表

                            }else{
                                Toast.makeText(getApplicationContext(), R.string.seterror, Toast.LENGTH_SHORT).show();      //時間設定錯誤
                            }

//                        }else{
//                            Toast.makeText(getApplicationContext(), R.string.seterrorname, Toast.LENGTH_SHORT).show();      //時間設定錯誤
//                        }


                    }
                });
                CANCEL_btn_update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDlg.cancel();
                    }
                });


            }));//holder.btGetInfo

            holder.btDelete.setOnClickListener((v -> {
                holder.swipeRevealLayout.close(true);
                // 刪除資料
                new AlertDialog.Builder(Ast_trip.this)
                        .setTitle("確定要刪除行程?")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("id", arrayList.get(position).get("id")));
                                SchedualDBConnector.executeDelet("DELETE From schedule ", nameValuePairs);//真正執行刪除
                                SchedualDBConnector.executeQuery("DELETE FROM detail where sch_id="+arrayList.get(position).get("id")+" ");//真正執行刪除

                                arrayList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position,arrayList.size());
                                setupViewcomponent();

                            }
                        }).setNegativeButton("取消",null).create()
                        .show();


            }));//holder.btDelete

            holder.mView.setOnClickListener((v)->{
                //傳值

                Intent it = new Intent();
                //從這到
                it.setClass(Ast_trip.this, Ast_detail.class);

                Bundle bundle = new Bundle();
                //行程名稱,出發時間,儲存天數 傳值
                bundle.putString("ID", arrayList.get(position).get("id"));
                bundle.putString("NAME", arrayList.get(position).get("tripname"));
                bundle.putString("DEPARTURETIME", arrayList.get(position).get("departuretime"));
                bundle.putString("DAYS",arrayList.get(position).get("days"));

                it.putExtras(bundle);

                startActivity(it);


            });

        }
        private Button.OnClickListener b01on =new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Calendar nowtime=Calendar.getInstance();  //抓取現在時間

                //DatePickerDialog=日期跳出視窗
                //--日期的對話盒--

                DatePickerDialog dateDlg= new DatePickerDialog(
                        Ast_trip.this,    //在現在的layout跳出視窗
                        update_datelist,
                        nowtime.get(Calendar.YEAR),
                        nowtime.get(Calendar.MONTH),
                        nowtime.get(Calendar.DAY_OF_MONTH));
//               dateDlg.setTitle(getString(R.string.app_name));
//               dateDlg.setMessage(getString(R.string.trip_new_select));
//               dateDlg.setIcon(android.R.drawable.ic_dialog_info);
                dateDlg.setCancelable(false); //鎖定對話盒，不點無法離開
                dateDlg.show();

                now_time =Integer.toString(nowtime.get(Calendar.YEAR))
                        +Integer.toString(nowtime.get(Calendar.MONTH))
                        +Integer.toString(nowtime.get(Calendar.DAY_OF_MONTH)) ;
            }
        };
        private DatePickerDialog.OnDateSetListener update_datelist=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                u_b01.setText(
                        Integer.toString(year)+"/"+
                                Integer.toString(month+1)+"/"+
                                Integer.toString(dayOfMonth)
                );

                syear=year;         //把選擇的年月日設為公共變數  給結束日取用
                smonth=month;
                sdayOfMonth=dayOfMonth;
                stime=String.valueOf(year)+String.valueOf(month)+String.valueOf(dayOfMonth);   //用來判斷選擇天數是不是早於今天
                tripname=u_e01.getText().toString();
                departuretime=u_b01.getText().toString();

            }
        };
        @Override
        public int getItemCount() {
            return arrayList.size();
        }
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
                Ast_trip.this.finish();
                break;

        }
        return true;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        this.finish();
        Toast.makeText(getApplicationContext(), R.string.error_back, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mysql.removeCallbacks(updatemysql);
    }
}


