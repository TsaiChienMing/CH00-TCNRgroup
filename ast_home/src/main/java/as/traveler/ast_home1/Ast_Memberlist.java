package as.traveler.ast_home1;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import as.traveler.ast_home1.providers.MemberContentProvider;

public class Ast_Memberlist extends AppCompatActivity implements View.OnClickListener {

    //SQLiteDataBase
    private static ContentResolver mContRes;
    private String[] MYCOLUMN = new String[]{"id", "uid", "name", "sex","birth", "email", "password", "phone", "latitude", "longitude", "rank", "create_at", "login_at"};
    private int logincheck;
    //SQLiteDataBase

    private TextView accountname,nickname,birth,sex,phone;
    private Button btnok,btncancle;
    private String account ;
    private int tcount;
    private String msg;
    private int loginon;
    private String errMsg = null;
    String TAG = "tcnr03=";;
    private Dialog checkname,checkphone;
    private TextView chname_input;
    private RadioButton radioman,radiowoman;
    private String d;
    private String s;
    private TextView chphone_input;
    private String sexvalue;
    private RadioGroup radiosex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_memberlist);
        //----------抓取遠端資料庫設定執行續----------------
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
//----------------------------------------------------------------
        setupViewComponent();
        u_loaddata();
        dbmysql(accountname.getText().toString());
//        sqliteupdate();
    }

    private void setupViewComponent() {
        accountname = (TextView)findViewById(R.id.ast_memberlist_e001);
        nickname = (TextView)findViewById(R.id.ast_memberlist_e003);
        sex = (TextView)findViewById(R.id.ast_memberlist_e004);
        birth = (TextView)findViewById(R.id.ast_memberlist_e005);
        phone=(TextView)findViewById(R.id.ast_memberlist_e008);
        radiosex=(RadioGroup)findViewById(R.id.radiosex);
        radioman=(RadioButton)findViewById(R.id.radioman);
        radiowoman=(RadioButton)findViewById(R.id.radiowoman);
        btnok =(Button)findViewById(R.id.ast_memberlist_btnOK);
        btncancle = (Button)findViewById(R.id.ast_memberlist_btnCancel);


        nickname.setOnClickListener(this);
        sex.setOnClickListener(this);
        birth.setOnClickListener(this);
        phone.setOnClickListener(this);
        btnok.setOnClickListener(this);
        btncancle.setOnClickListener(this);

    }


    //讀取登入資料
    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        logincheck = login.getInt("flag", 0);
        accountname.setText(login.getString("Email", "0"));
    }

    @Override
    public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.ast_memberlist_e003:
                    checkname = new Dialog(Ast_Memberlist.this);
                    checkname.setTitle(getString(R.string.ast_memberlist_nametitle));
                    checkname.setCancelable(false);
                    checkname.setContentView(R.layout.ch_nickname);
                    chname_input =(TextView)checkname.findViewById(R.id.chname_input);
                    Button checknameBtnOK = (Button) checkname.findViewById(R.id.chname_okbtn);
                    Button checkBtnCancel = (Button) checkname.findViewById(R.id.chname_cancle);
                    checknameBtnOK.setOnClickListener(chechnameon);
                    checkBtnCancel.setOnClickListener(chechnameon);
                    checkname.show();
                    break;
                case R.id.ast_memberlist_e004:

                    break;
                case R.id.ast_memberlist_e005:
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog datePicDlg = new DatePickerDialog(
                            this,
                            datePicDigOnDateSelLis,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    datePicDlg.setTitle(getString(R.string.ast_memberlist_birthtitle));
                    datePicDlg.setIcon(android.R.drawable.ic_dialog_info);
                    datePicDlg.setCancelable(false);
                    datePicDlg.show();
                    break;
                case R.id.ast_memberlist_e008:
                    checkphone = new Dialog(Ast_Memberlist.this);
                    checkphone.setTitle(getString(R.string.ast_memberlist_phonetitle));
                    checkphone.setCancelable(false);
                    checkphone.setContentView(R.layout.ch_phone);
                    chphone_input =(TextView)checkphone.findViewById(R.id.chphone_input);
                    Button chphoneBtnOK = (Button) checkphone.findViewById(R.id.chphone_okbtn);
                    Button chphoneBtnCancel = (Button) checkphone.findViewById(R.id.chphone_cancle);
                    chphoneBtnOK.setOnClickListener(chphonenameon);
                    chphoneBtnCancel.setOnClickListener(chphonenameon);
                    checkphone.show();
                    break;
                case R.id.ast_memberlist_btnOK:
                    mysql_update_memeberlist();
                    Ast_Memberlist.this.finish();
                    break;
                case R.id.ast_memberlist_btnCancel:
                    this.finish();
                    break;
            }
    }



    private  Button.OnClickListener chechnameon = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.chname_okbtn:
                    String newname =chname_input.getText().toString().trim();
                    nickname.setText(newname);
                    checkname.cancel();
                    break;
                case R.id.chname_cancle:
                    checkname.cancel();
                    break;
            }
        }
    };
    private DatePickerDialog.OnDateSetListener datePicDigOnDateSelLis = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//            d = (year + getString(R.string.n_yy) +
////                    (month + 1) + getString(R.string.n_mm) +
////                    dayOfMonth + getString(R.string.n_dd)
////            );
            d = (year +"-" +(month/10)+
                    (month%10+ 1) + "-" +(dayOfMonth/10)+
                    dayOfMonth%10);
            birth.setText(d + "\n");
        }
    };
    private  Button.OnClickListener chphonenameon = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.chphone_okbtn:
                    String newphone =chphone_input.getText().toString().trim();
                    if(isTelphoneValid(newphone)==false){
                        Toast.makeText(getApplicationContext(), "手機格式錯誤", Toast.LENGTH_LONG).show();
                    }else {
                        phone.setText(newphone);
                        checkphone.cancel();
                    }
                    break;
                case R.id.chphone_cancle:
                    checkphone.cancel();
                    break;
            }
        }
    };
    // 取得會員資料，寫入MySQL
    private void mysql_update_memeberlist() {
        String mail =accountname.getText().toString().trim();
        String name = nickname.getText().toString().trim();
        if(radioman.isChecked()){
            sexvalue="男";
        }else {
            sexvalue="女";
        }
        String birthday = birth.getText().toString().trim();
        String phonenume = phone.getText().toString().trim();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("email", mail));
        nameValuePairs.add(new BasicNameValuePair("name", name));
        nameValuePairs.add(new BasicNameValuePair("sex", sexvalue));
        nameValuePairs.add(new BasicNameValuePair("birth", birthday));
        nameValuePairs.add(new BasicNameValuePair("phone", phonenume));
        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.mysql_update_memeberlist("SELECT * FROM  as_member ", nameValuePairs);
//-----------------------------------------------
    }

//    //把SQLite的資料更新
    private void sqliteupdate() {
        Cursor c = mContRes.query(MemberContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        c.moveToFirst(); // 一定要寫，不然會出錯
        tcount = c.getCount();
        int aa=0;
        // ---------------------------
        String fidSet = "";
            for (int i = 0;  i< tcount; i++) {
                fidSet += c.getString(i)+", ";
            }
            try {
                if (fidSet!=null){
//                    new String[]{"id", "uid", "name", "sex", birth","email", ""password", "phone", "latitude", "longitude", "rank", "create_at", "login_at"};
                    nickname.setText(c.getString(2));
                    birth.setText(c.getString(4));
                    phone.setText(c.getString(7));
                    if(c.getString(3).equals("男")){
                        radiosex.check(R.id.radioman);
                    }else {
                        radiosex.check(R.id.radiowoman);
                    }
                }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "失敗啦", Toast.LENGTH_LONG).show();
            }
        c.close();
    }
    private void dbmysql(String email) {
        mContRes = getContentResolver();
        Cursor cur = mContRes.query(MemberContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur.moveToFirst(); // 一定要寫，不然會出錯
        // // ---------------------------
        try {
//            String result = DBConnector.executeQuery("SELECT * FROM as_member");
            String result = DBConnector.executeQuery("SELECT * FROM  as_member  WHERE  email  =  "+"'"+email+"'");
//==========================================
            //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
            //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
            Log.d(TAG, "httpstate=" + DBConnector.httpstate);

            if (DBConnector.httpstate == 200) {
                Uri uri = MemberContentProvider.CONTENT_URI;
                mContRes.delete(uri, null, null);  //清空SQLite
                Toast.makeText(getBaseContext(), "已經完成由伺服器匯入資料",
                        Toast.LENGTH_LONG).show();
            } else {
                int checkcode = DBConnector.httpstate / 100;
                switch (checkcode) {
                    case 1:
                        msg = "資訊回應(code:" + DBConnector.httpstate + ")";
                        break;
                    case 2:
                        msg = "已經完成由伺服器匯入資料(code:" + DBConnector.httpstate + ")";
                        break;
                    case 3:
                        msg = "伺服器重定向訊息，請稍後再試(code:" + DBConnector.httpstate + ")";
                        break;
                    case 4:
                        msg = "用戶端錯誤回應，請稍後再試(code:" + DBConnector.httpstate + ")";
                        break;
                    case 5:
                        msg = "伺服器error responses，請稍後再試(code:" + DBConnector.httpstate + ")";
                        break;
                }
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
            }
//======================================
            // 選擇讀取特定欄位
            // String result = DBConnector.executeQuery("SELECT id,name FROM
            // member");
            /*******************************************************************************************
             * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
             * jsonData = new JSONObject(result);
             *******************************************************************************************/
            // -------------------------------------------------------
            JSONArray jsonArray = new JSONArray(result);
            if (jsonArray.length() > 0) { // MySQL 連結成功有資料
                Uri uri = MemberContentProvider.CONTENT_URI;
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
                    mContRes.insert(MemberContentProvider.CONTENT_URI, newRow);

                }
                // ---------------------------
            } else {
                Toast.makeText(getApplicationContext(), "主機資料庫無資料", Toast.LENGTH_LONG).show();
            }
            // --------------------------------------------------------

        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
        cur.close();
        //------------------------------------
        sqliteupdate();//抓取SQLite資料
    }
//檢查手機格式
private boolean isTelphoneValid(String num) {
    if (num == null) {
        return false;
    }
    // 首位0, 第二位是9, 剩下七位0-9, 共10位数字
    // []: 括號內的任何字元
    //[^]: 不在括號內的任何字元
    //[-]: 範圍
    //^:字串開頭
    //$:字串結尾或字串結尾的 \n 之前
    //\b:比對必須發生在 \w (英數) 和 \W (非英數) 字元之間的界限上
    String pattern = "^09[0-9]{8}$";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(num);
    return m.matches();
}
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Toast.makeText(getApplicationContext(),getString(R.string.onBackPressed), Toast.LENGTH_SHORT).show();
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
                Ast_Memberlist.this.finish();
                break;
        }
        return true;
    }
}
