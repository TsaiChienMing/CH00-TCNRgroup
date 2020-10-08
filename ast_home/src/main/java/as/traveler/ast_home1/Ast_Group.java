package as.traveler.ast_home1;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import as.traveler.ast_home1.providers.GroupContentProvider;


public class Ast_Group extends AppCompatActivity implements View.OnClickListener {

    String TAG = "tcnr02=>";
    //主layout的物件
    private Button group_addfolder, group_calfolder, group_listview_b001, group_listview_b002, group_listview_b003,
            group_listview_b004, group_listview_b005;
    private BottomNavigationView ast_Bottom;
    private Dialog group_Dialog;
    private Intent group_intent = new Intent();
    private int num = 0;
    private EditText input_foldername;
    private String group_name;

    //MySQL的物件
    private static ContentResolver mContRes;
    String msg = null;
    private Handler handler = new Handler();
    private int autotime = 20;
    private String[] MYCOLUMN = new String[]{"id", "grp_name", "creator_id", "member_id","points","alert_year","alert_month","alert_day","alert_hour","alert_min","alert_state"};       //MySQL欄位
    private int logincheck;         //會員登入狀態
    private String email;           //會員信箱
    private int tcount;
    private String creator;
    private Cursor c;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_group);
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
        setupViewComponent();
        close_all();
        open_btn1();
        open_btn2();
        open_btn3();
        open_btn4();
        open_btn5();

        //隱藏全部的Button
        group_listview_b001.setVisibility(View.INVISIBLE);
        group_listview_b002.setVisibility(View.INVISIBLE);
        group_listview_b003.setVisibility(View.INVISIBLE);
        group_listview_b004.setVisibility(View.INVISIBLE);
        group_listview_b005.setVisibility(View.INVISIBLE);

        //-----------建立BottomNavigationView物件
        ast_Bottom = (BottomNavigationView) findViewById(R.id.ast_Bottom);

        ast_Bottom.setSelectedItemId(R.id.ast_group);
        BottomNavigationHelper.removeShiftMode(ast_Bottom);  // 生一個外部的class
        ast_Bottom.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ast_Bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ast_act:
//                        startActivity(new Intent(getApplicationContext(), Ast_Home.class));
                        Ast_Group.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.ast_trip:
//                        startActivity(new Intent(getApplicationContext(), Ast_stroke.class));
                        Ast_Group.this.finish();//避免堆疊，本頁結束
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_group:
//                        startActivity(new Intent(getApplicationContext(), Ast_Group.class));

                        return true;
                    case R.id.ast_col:
//                        startActivity(new Intent(getApplicationContext(), Ast_Col.class));
                        Ast_Group.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.ast_more:
//                        startActivity(new Intent(getApplicationContext(), Ast_More.class));
                        Ast_Group.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
        //確認會員是否有登入
        u_loaddata();
        if(logincheck == 0){
            Toast.makeText(getApplicationContext(),"請先登入",Toast.LENGTH_LONG).show();
            this.finish();
        }else  if(logincheck == 1){
            setupViewComponent();
        }
        handler.postDelayed(updateSQL, 100);
    }
    private Runnable updateSQL = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, autotime * 1000); // 真正延遲的時間
            // -------執行匯入MySQL -------------
            dbmysql();
        }

    };
    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        logincheck = login.getInt("flag", 0);
        email= login.getString("Email","0");        //email的值
    }
    private void setupViewComponent() {
        //設定class標題
        this.setTitle(getString(R.string.item_title_group));//設定title
        //----------------------------------------------
        group_addfolder = (Button) findViewById(R.id.group_addfolder);   //新增群組
        group_calfolder = (Button) findViewById(R.id.group_calfolder);   //刪除群組

        group_addfolder.setOnClickListener(this);   //按下跳出新增群組名稱的DiaLog
        group_calfolder.setOnClickListener(this);

        group_listview_b001 = (Button) findViewById(R.id.group_listview_b001);
        group_listview_b002 = (Button) findViewById(R.id.group_listview_b002);
        group_listview_b003 = (Button) findViewById(R.id.group_listview_b003);
        group_listview_b004 = (Button) findViewById(R.id.group_listview_b004);
        group_listview_b005 = (Button) findViewById(R.id.group_listview_b005);
    }

    //按下新增群組的監聽
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.group_addfolder:
                group_Dialog = new Dialog(Ast_Group.this);
                group_Dialog.setTitle(getString(R.string.group_addfolder));
//                group_Dialog.setCancelable(false);

                group_Dialog.setContentView(R.layout.ast_group_dialog);
                Button group_folder_ok_btn = (Button) group_Dialog.findViewById(R.id.group_florder_okbtn);
                Button group_folder_cal_btn = (Button) group_Dialog.findViewById(R.id.group_florder_calbtn);


                group_folder_ok_btn.setOnClickListener(this);
                group_folder_cal_btn.setOnClickListener(this);
                group_Dialog.show();
                break;
            case R.id.group_florder_okbtn:
                input_foldername = (EditText) group_Dialog.findViewById(R.id.group_input_username);
                group_name = input_foldername.getText().toString().trim();      //群組名稱

                while(group_name != "" && num <= 4){
                    group_show(num);
                    break;
                }
                //若名稱空白
                if (group_name.equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.group_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                //若新增超過5個
                    if(num > 4){
                        Toast.makeText(getApplicationContext(), R.string.list_error, Toast.LENGTH_LONG).show();
                        return;
                    }
                if (num >=5) {        //防止Layout出現5個 但資料庫卻有5個以上
                    msg = "新增記錄失敗! ";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                }
                group_Dialog.cancel();
                break;
            case R.id.group_florder_calbtn:
                group_Dialog.cancel();
                break;

            case R.id.group_listview_b001:
                group_intent.setClass(Ast_Group.this, Ast_Group_Map.class);
                String name = group_listview_b001.getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("alert_state", "false");
                bundle.putString("creator_id", email);
                group_intent.putExtras(bundle);
                startActivity(group_intent);

                break;
            case R.id.group_listview_b002:
                group_intent.setClass(Ast_Group.this, Ast_Group_Map.class);
                String name2 = group_listview_b002.getText().toString();
                Bundle bundle2 = new Bundle();
                bundle2.putString("name", name2);
                bundle2.putString("alert_state","false");
                bundle2.putString("creator_id", email);
                group_intent.putExtras(bundle2);
                startActivity(group_intent);
                break;
            case R.id.group_listview_b003:
                group_intent.setClass(Ast_Group.this, Ast_Group_Map.class);
                String name3 = group_listview_b003.getText().toString();
                Bundle bundle3 = new Bundle();
                bundle3.putString("name", name3);
                bundle3.putString("alert_state","false");
                bundle3.putString("creator_id", email);
                group_intent.putExtras(bundle3);
                startActivity(group_intent);
                break;
            case R.id.group_listview_b004:
                group_intent.setClass(Ast_Group.this, Ast_Group_Map.class);
                String name4 = group_listview_b004.getText().toString();
                Bundle bundle4 = new Bundle();
                bundle4.putString("name", name4);
                bundle4.putString("alert_state","false");
                bundle4.putString("creator_id", email);
                group_intent.putExtras(bundle4);
                startActivity(group_intent);
                break;
            case R.id.group_listview_b005:
                group_intent.setClass(Ast_Group.this, Ast_Group_Map.class);
                String name5 = group_listview_b005.getText().toString();
                Bundle bundle5 = new Bundle();
                bundle5.putString("name", name5);
                bundle5.putString("alert_state","false");
                bundle5.putString("creator_id", email);
                group_intent.putExtras(bundle5);
                startActivity(group_intent);
                break;
            case R.id.group_calfolder:
                MyAlertDialog delete = new MyAlertDialog(this);
                delete.setTitle(getString(R.string.delete_group_title));
                delete.setMessage(getString(R.string.delete_group));
                delete.setIcon(android.R.drawable.ic_dialog_info);
                delete.setCancelable(false);
                delete.setButton(DialogInterface.BUTTON_POSITIVE,getString(R.string.m0902_positive),altDlgOnClkPosiBtnLis);
                delete.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.m0902_negative), altDlgOnClkNegaBtnLis);
                delete.show();
                break;
        }
    }
    //---------------------------------------------------------------------------------------
    private DialogInterface.OnClickListener altDlgOnClkPosiBtnLis = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("creator_id", email));
            //擔心沒有將資料寫入，所以停一下再新增
            try {
                Thread.sleep(500); //  延遲Thread 睡眠0.5秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//-----------------------------------------------
            String result = Group_DBConnector.groupdelete("SELECT * FROM grp", nameValuePairs);
            int aa =5;
//-----------------------------------------------
            close_all();
            dialog.cancel();
        }
    };
    private DialogInterface.OnClickListener altDlgOnClkNegaBtnLis = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };
    private void group_show(int count) {
        switch (count){
            case 0:
                open_btn1();
                group_listview_b001.setText(group_name);
                group_listview_b001.setOnClickListener(this);
                //------------------直接新增至MySQL
                insert_group();
                dbmysql();
                num = 1;
                break;
            case  1:
                if (group_name.equals(group_listview_b001.getText().toString())) {
                    Toast.makeText(getApplicationContext(),"名稱不可重複",Toast.LENGTH_LONG).show();
                    return;
                }else {
                    open_btn2();
                    group_listview_b002.setText(group_name);
                    group_listview_b002.setOnClickListener(this);
                    //------------------直接新增至MySQL
                    insert_group();
                    dbmysql();
                }
                break;
            case 2:
                if (group_name.equals(group_listview_b001.getText().toString())
                        && group_name.equals(group_listview_b002.getText().toString())) {
                    Toast.makeText(getApplicationContext(),"名稱不可重複",Toast.LENGTH_LONG).show();
                    return;
                } else {
                    open_btn3();
                    group_listview_b003.setText(group_name);
                    group_listview_b003.setOnClickListener(this);
                    //------------------直接新增至MySQL
                    insert_group();
                    dbmysql();
                    num = 3;
                }
                break;

            case 3:
                if (group_name.equals(group_listview_b001.getText().toString())
                        && group_name.equals(group_listview_b002.getText().toString())
                && group_name.equals(group_listview_b003.getText().toString())) {
                    Toast.makeText(getApplicationContext(),"名稱不可重複",Toast.LENGTH_LONG).show();
                    return;
                } else {
                    open_btn4();
                    group_listview_b004.setText(group_name);
                    group_listview_b004.setOnClickListener(this);
                    //------------------直接新增至MySQL
                    insert_group();
                    dbmysql();
                    num = 4;
                }
                break;
            case 4:
                if (group_name.equals(group_listview_b001.getText().toString())
                    && group_name.equals(group_listview_b002.getText().toString())
                    && group_name.equals(group_listview_b003.getText().toString())
                        && group_name.equals(group_listview_b004.getText().toString())) {
                    Toast.makeText(getApplicationContext(),"名稱不可重複",Toast.LENGTH_LONG).show();
                    return;
                } else {
                    open_btn5();
                    group_listview_b005.setText(group_name);
                    group_listview_b005.setOnClickListener(this);
                    //------------------直接新增至MySQL
                    insert_group();
                    dbmysql();
                    num = 5;
                }
                break;
        }
    }
private void insert_group(){
    group_mysql_insert();
    String msg = null;
    msg = "新增記錄  成功 ! ";
    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
}
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(), getString(R.string.onBackPressed), Toast.LENGTH_SHORT).show();
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
                Ast_Group.this.finish();
                break;
        }
        return true;
    }

    //按下刪除群組的按鈕
    private void close_all() {
        group_listview_b001.setVisibility(View.INVISIBLE);
        group_listview_b002.setVisibility(View.INVISIBLE);
        group_listview_b003.setVisibility(View.INVISIBLE);
        group_listview_b004.setVisibility(View.INVISIBLE);
        group_listview_b005.setVisibility(View.INVISIBLE);

        group_listview_b001.setText("");
        group_listview_b002.setText("");
        group_listview_b003.setText("");
        group_listview_b004.setText("");
        group_listview_b005.setText("");

        num = 0;
    }

    //開啟一個按鈕的顯示
    private void open_btn1() {
        group_listview_b001.setVisibility(View.VISIBLE);

    }

    //開啟兩個按鈕的顯示
    private void open_btn2() {
        group_listview_b001.setVisibility(View.VISIBLE);
        group_listview_b002.setVisibility(View.VISIBLE);
    }

    //開啟三個按鈕的顯示
    private void open_btn3() {
        group_listview_b001.setVisibility(View.VISIBLE);
        group_listview_b002.setVisibility(View.VISIBLE);
        group_listview_b003.setVisibility(View.VISIBLE);

    }

    //開啟四個按鈕的顯示
    private void open_btn4() {
        group_listview_b001.setVisibility(View.VISIBLE);
        group_listview_b002.setVisibility(View.VISIBLE);
        group_listview_b003.setVisibility(View.VISIBLE);
        group_listview_b004.setVisibility(View.VISIBLE);

    }

    //開啟五個按鈕的顯示
    private void open_btn5() {
        group_listview_b001.setVisibility(View.VISIBLE);
        group_listview_b002.setVisibility(View.VISIBLE);
        group_listview_b003.setVisibility(View.VISIBLE);
        group_listview_b004.setVisibility(View.VISIBLE);
        group_listview_b005.setVisibility(View.VISIBLE);
    }

    private void dbmysql() {
        mContRes = getContentResolver();
        Cursor cur = mContRes.query(GroupContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        Uri url = GroupContentProvider.CONTENT_URI;
        mContRes.delete(url,null,null); //清空SQLite
        cur.moveToFirst(); // 一定要寫，不然會出錯
        // // ---------------------------
        try {
            String result = Group_DBConnector.executeQuery("SELECT  *  FROM  `grp`  WHERE  member_id  =  "  + " '" + email + "' " );
//        String r = result.toString().trim();
//==========================================
            //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
            //存取類別成員 Group_Group_DBConnector.httpstate 判定是否回應 200(連線要求成功)
            Log.d(TAG, "httpstate=" + Group_DBConnector.httpstate);

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
//            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

//======================================
            // 選擇讀取特定欄位
            // String result = Group_Group_DBConnector.executeQuery("SELECT id,name FROM
            // member");
            /*******************************************************************************************
             * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
             * jsonData = new JSONObject(result);
             *******************************************************************************************/
            JSONArray jsonArray = new JSONArray(result);

            // -------------------------------------------------------
            if (jsonArray.length() > 0) { // MySQL 連結成功有資料
                Uri uri = GroupContentProvider.CONTENT_URI;
//                mContRes.delete(uri, null, null); // 匯入前,刪除所有SQLite資料

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
////                    tvTitle.setTextColor(Color.BLUE);
////                    tvTitle.setText("顯示資料： 共加入" + Integer.toString(jsonArray.length()) + " 筆");
//                    Toast.makeText(Ast_Group.this, "新增至MySQL", Toast.LENGTH_LONG).show();
                }
                // ---------------------------
            } else {
                Toast.makeText(Ast_Group.this, "主機資料庫無資料", Toast.LENGTH_LONG).show();
            }
            // --------------------------------------------------------

        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
        cur.close();
        //--------------------------------
        sqliteupdate();
        //-----------------------------------
    }
    //把SQLite的資料更新
    private void sqliteupdate() {
        c = mContRes.query(GroupContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        tcount = c.getCount();
        int columnCount = c.getColumnCount();
        int k = 1;
        // ---------------------------
        String fidSet = "";
        for (int i = 0; i < tcount; i++) {
            c.moveToPosition(i);
                creator = c.getString(1);
                if(creator.equals(null)){
                    Toast.makeText(getApplicationContext(),"資料庫無資料",Toast.LENGTH_LONG).show();
                }else{
                        if(k == 1){
                            open_btn1();
                            group_listview_b001.setText(creator);
                            group_listview_b001.setOnClickListener(this);
                            k++;
                            continue;
                        }if(k == 2){
                            open_btn2();
                            group_listview_b002.setText(creator);
                            group_listview_b002.setOnClickListener(this);
                            k++;
                        continue;
                        }if(k == 3){
                            open_btn3();
                            group_listview_b003.setText(creator);
                            group_listview_b003.setOnClickListener(this);
                            k++;
                        continue;
                        }if(k == 4){
                            open_btn4();
                            group_listview_b004.setText(creator);
                            group_listview_b004.setOnClickListener(this);
                            k++;
                        continue;
                        }if(k == 5){
                            open_btn5();
                            group_listview_b005.setText(creator);
                            group_listview_b005.setOnClickListener(this);
                            k++;
                        continue;
                        }if(k > 5){
                            Toast.makeText(getApplicationContext(), R.string.more_group,Toast.LENGTH_LONG).show();
                        continue;
                        }
                }
            for (int j = 0; j < columnCount; j++) {
                fidSet += c.getString(j) + ", ";
            }
        }
        c.close();

//        MyAlertDialog dialog = new MyAlertDialog(Ast_Group_Map.this);
//        dialog.setTitle("測試用dialog");
//        dialog.setMessage(fidSet);
//        dialog.show();
    }
    //新增群組名稱到MySQL
    private void group_mysql_insert() {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("grp_name", group_name));
        nameValuePairs.add(new BasicNameValuePair("creator_id", email));
        nameValuePairs.add(new BasicNameValuePair("alert_state", "false"));
        int i = 1;


        //擔心沒有將資料寫入，所以停一下再新增
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = Group_DBConnector.executeInsert("SELECT * FROM member", nameValuePairs);

//-----------------------------------------------
    }
}

