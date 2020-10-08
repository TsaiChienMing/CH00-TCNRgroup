package as.traveler.ast_home1;

import android.accounts.AccountManagerFuture;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import as.traveler.ast_home1.providers.MemberContentProvider;


public class Ast_Login extends AppCompatActivity implements View.OnClickListener {

    //登入主畫面
    private Button login_gohome, login_signin, login_signup, login_accountbtn, login_mapbtn, login_messagebtn, login_logout;


    //登入Dialog
    private Dialog mSigninDlg;
    private EditText signin_edtUserName, signin_edtPassword;
    private Button login_signin_signin, login_signin_forgot, login_signin_cancle;

    //註冊Dialog
    private Dialog mSignupDlg;
    private EditText signup_edtEmail, signup_edtPhone, signup_edtName, signup_edtPassword, signup_edtCPassword;
    private TextView signin_hint;
    private Button login_signup_create, login_signup_cancle;

    //忘記密碼Dialog
    private Dialog mForgotpwDlg;
    private EditText forgot_edtEmail;
    private Button signin_forgot_send;

    //登出Dialog
    private Dialog mLogoutDlg;
    private Button lougout_agree_send, lougout_cnacle_send;
    //--------------------------
    private static ContentResolver mContRes;
    private String[] MYCOLUMN = new String[]{"id", "uid", "name", "sex", "email", "password", "phone", "latitude", "longitude", "rank", "create_at", "login_at"};
    String trank = "brozen";
    String tname, temail, tpassword, tcpwd, tphone;
    // ------------------

    //註冊
    private int loginon;
    private String errMsg = null;
    private String msg;
    String TAG = "tcnr03=";
    private String s_id, s_uid, s_latitude, s_longitude, s_create, s_login;
    private Handler handler = new Handler();
    private int autotime = 10;
    //Google登入
    private String spwd, smail;
    private static final int RC_SIGN_IN = 9001;


    private AccountManagerFuture completedTask;
    private GoogleSignInClient mGoogleSignInClient;
    private LinearLayout linear01, linear02;
    private TextView no_account;
    private String DisplayName;
    private int update_time = 0;
    private Button button;
    private String photo;
    private EditText forgot_pwd,forgot_pwd2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_login);
        u_loaddata();
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

    }

    private void setupViewComponent() {
        login_signin = (Button) findViewById(R.id.ast_login_signin);
        login_signup = (Button) findViewById(R.id.ast_login_signup);
        login_gohome = (Button) findViewById(R.id.ast_login_gohome);
        no_account = (TextView) findViewById(R.id.no_account);


        login_accountbtn = (Button) findViewById(R.id.ast_more_accountbtn);
        login_mapbtn = (Button) findViewById(R.id.ast_more_mapbtn);
        login_messagebtn = (Button) findViewById(R.id.ast_more_messagebtn);
        login_logout = (Button) findViewById(R.id.ast_login_logout);

        login_signin.setOnClickListener(this);
        login_signup.setOnClickListener(this);
        login_gohome.setOnClickListener(goHome);
        login_accountbtn.setOnClickListener(this);
        login_mapbtn.setOnClickListener(this);
        login_logout.setOnClickListener(this);


        SignInButton signInButton = findViewById(R.id.ast_login_google);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        findViewById(R.id.ast_login_google).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (loginon == 1) {
            login_signin.setVisibility(View.INVISIBLE);
            login_signup.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
            no_account.setVisibility(View.INVISIBLE);
            login_gohome.setVisibility(View.INVISIBLE);

            login_accountbtn.setVisibility(View.VISIBLE);
            login_mapbtn.setVisibility(View.VISIBLE);
            login_messagebtn.setVisibility(View.VISIBLE);
            login_logout.setVisibility(View.VISIBLE);
        } else {
            login_signin.setVisibility(View.VISIBLE);
            login_signup.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            no_account.setVisibility(View.VISIBLE);
            login_gohome.setVisibility(View.VISIBLE);

            login_accountbtn.setVisibility(View.INVISIBLE);
            login_mapbtn.setVisibility(View.INVISIBLE);
            login_messagebtn.setVisibility(View.INVISIBLE);
            login_logout.setVisibility(View.INVISIBLE);
        }

    }

    //"以訪客身分繼續"按鈕
    private Button.OnClickListener goHome = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ast_login_gohome:
                    Intent it = new Intent();
                    it.setClass(Ast_Login.this, Ast_Home.class);
                    startActivity(it);
                    finish();
                    break;
            }
        }
    };

    //監聽登入或是註冊
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ast_login_signin://主畫面"登入"按鈕
                u_signinDlg();
                break;
            case R.id.ast_login_signup://主畫面"註冊"按鈕
                u_signupDlg();
                break;
            case R.id.ast_login_google://主畫面"google登入"按鈕
                signIn();
                break;
            case R.id.ast_more_accountbtn:
                Intent it = new Intent();
                it.setClass(getApplicationContext(), Ast_Member.class);
                startActivity(it);
                break;
            case R.id.ast_more_mapbtn:
                Intent it2 = new Intent();
                it2.setClass(getApplicationContext(), Ast_parking.class);
                startActivity(it2);
                break;
            case R.id.ast_more_messagebtn:
                break;
            case R.id.ast_login_logout:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.logout))
                        .setMessage(getString(R.string.ast_more_logout_title))
                        .setCancelable(true)
                        .setIcon(R.drawable.logo)
                        .setPositiveButton(getString(R.string.ast_more_login_check),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loginon = 0;//登出
                                        DisplayName="";
                                        SharedPreferences login =
                                                getSharedPreferences("as_member", 0);
                                        login
                                                .edit()
                                                .putInt("flag", loginon)
                                                .putString("DisplayName", "")
                                                .commit();
                                        refresh();
                                        signOut();
                                        Ast_Login.this.finish();
                                    }
                                })
                        .setNeutralButton(getString(R.string.ast_more_login_canclebtn),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .show();
                break;
            case R.id.forgot_send:
                String email=forgot_edtEmail.getText().toString().trim();
                String pwd=forgot_pwd.getText().toString().trim();
                String pwd2=forgot_pwd2.getText().toString().trim();
                if(pwd!=null&&pwd2!=null){
                    if (pwd.equals(pwd2)){
                        updatepwd(email,pwd);
                    }else {
                        Toast.makeText(getApplicationContext(), "密碼不一致", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "欄位有空缺", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void updatepwd(String u_email,String u_pwd) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("password", u_pwd));
        nameValuePairs.add(new BasicNameValuePair("email", u_email));
        String aa= DBConnector.executeUpdate("SELECT * FROM  as_member ", nameValuePairs);
        Toast.makeText(getApplicationContext(), "密碼修改成功", Toast.LENGTH_LONG).show();
        this.finish();
    }

    //主畫面"登入"按鈕----->dialog
    private void u_signinDlg() {
        mSigninDlg = new Dialog(Ast_Login.this);
        mSigninDlg.setTitle(getString(R.string.login));
        mSigninDlg.setCancelable(true);
        mSigninDlg.setContentView(R.layout.signin);

        signin_edtUserName = (EditText) mSigninDlg.findViewById(R.id.signin_username);
        signin_edtPassword = (EditText) mSigninDlg.findViewById(R.id.signin_password);

        login_signin_signin = (Button) mSigninDlg.findViewById(R.id.signin_signin);
        login_signin_forgot = (Button) mSigninDlg.findViewById(R.id.signin_forgot);
        login_signin_cancle = (Button) mSigninDlg.findViewById(R.id.signin_cancle);
        login_signin_signin.setOnClickListener(signinon);
        login_signin_forgot.setOnClickListener(signinon);
        login_signin_cancle.setOnClickListener(signinon);
        mSigninDlg.show();
    }

    //監聽"登入"按鈕
    private Button.OnClickListener signinon = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.signin_signin:
                    u_signinAccount();
                    break;
                case R.id.signin_cancle:
                    mSigninDlg.cancel();
                    break;
                case R.id.signin_forgot:
                    u_forgotPw();
                    break;
            }
        }
    };

    //主畫面"註冊"按鈕
    private void u_signupDlg() {
        mSignupDlg = new Dialog(Ast_Login.this);
        mSignupDlg.setTitle(getString(R.string.register));
        mSignupDlg.setCancelable(true);
        mSignupDlg.setContentView(R.layout.signup);

        signup_edtEmail = (EditText) mSignupDlg.findViewById(R.id.signup_email);
        signup_edtPhone = (EditText) mSignupDlg.findViewById(R.id.signup_phone);
        signup_edtName = (EditText) mSignupDlg.findViewById(R.id.signup_name);
        signup_edtPassword = (EditText) mSignupDlg.findViewById(R.id.signup_password);
        signup_edtCPassword = (EditText) mSignupDlg.findViewById(R.id.signup_cpassword);

        login_signup_create = (Button) mSignupDlg.findViewById(R.id.signup_create);
        login_signup_cancle = (Button) mSignupDlg.findViewById(R.id.signup_cancle);

        login_signup_create.setOnClickListener(signupon);
        login_signup_cancle.setOnClickListener(signupon);

        mSignupDlg.show();
    }

    //監聽"註冊"按鈕
    private Button.OnClickListener signupon = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.signup_create:
                    u_createAccount();
                    break;
                case R.id.signup_cancle:
                    mSignupDlg.cancel();
                    break;
            }
        }
    };

    //註冊method
    private void u_createAccount() {
        temail = signup_edtEmail.getText().toString().trim();
        tphone = signup_edtPhone.getText().toString().trim();
        tname = signup_edtName.getText().toString().trim();
        tpassword = signup_edtPassword.getText().toString().trim();
        tcpwd = signup_edtCPassword.getText().toString().trim();
        if (temail.equals("") || tphone.equals("") || tname.equals("") || tpassword.equals("") || tcpwd.equals("")) {
            errMsg = "內容不可為空";
            Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
        } else if (!signup_edtPassword.getText().toString().trim().equals(signup_edtCPassword.getText().toString().trim())) {
            errMsg = "密碼並不一致";
            Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
        } else if (isTelphoneValid(tphone) == false) {
            errMsg = "手機格式不正確";
            Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
        } else if (isPasswordValid(tpassword) == false) {
            errMsg = "密碼長度至少要6個字";
            Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
        } else if(checkEmail(temail)==false){
            errMsg = "信箱格式不正確";
            Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
        } else {
            String result = DBConnector.executeQuery("SELECT * FROM  `as_member`  WHERE  `email` = "+temail);
            String fldSet = null;
            try{
                JSONArray jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {

                    fldSet += "\n";
                }
            }catch (Exception e){

            }
            if(fldSet==null){
                mysql_insert(tpassword);
                Toast.makeText(getApplicationContext(), "註冊成功", Toast.LENGTH_SHORT).show();
                mSignupDlg.cancel();
            }else {
                Toast.makeText(getApplicationContext(), "註冊失敗，帳號重複", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //註冊寫入method
    private void mysql_insert(String tpassword) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String password = u_md5(tpassword);
        nameValuePairs.add(new BasicNameValuePair("uid", ""));
        nameValuePairs.add(new BasicNameValuePair("name", tname));
        nameValuePairs.add(new BasicNameValuePair("email", temail));
        nameValuePairs.add(new BasicNameValuePair("phone", tphone));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("latitude", ""));
        nameValuePairs.add(new BasicNameValuePair("longitude", ""));
        nameValuePairs.add(new BasicNameValuePair("login_at", ""));
        nameValuePairs.add(new BasicNameValuePair("rank", trank));

        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.executeInsert("SELECT * FROM  as_member ", nameValuePairs);
//-----------------------------------------------

    }

    //登入method
    private void u_signinAccount() {
        smail = signin_edtUserName.getText().toString().trim();
        spwd = signin_edtPassword.getText().toString().trim();
        String password = u_md5(spwd);
        if (smail == null || spwd == null) {
            Toast.makeText(getApplicationContext(), "請輸入使用者名稱和密碼 !", Toast.LENGTH_SHORT).show();
        } else {
            //拆解登入JSON
            String result = DBConnector.executeQuery("SELECT * FROM `as_member` WHERE `email` = "+"'"+smail+"'"+" AND `password` = "+"'"+password+"'");
//            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            String fldSet = null;
            try{
                JSONArray jsonArray = new JSONArray(result);

                for (int i = 0; i < jsonArray.length(); i++) {

                    fldSet += "\n";
                }
            }catch (Exception e){

            }
            if (fldSet != null) {
                Toast.makeText(getApplicationContext(), "登入成功", Toast.LENGTH_SHORT).show();
                senddata();
                dbmysql(smail);
            } else {
                Toast.makeText(getApplicationContext(), "登入失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //忘記密碼Dialog
    private void u_forgotPw() {//忘記密碼Dialog
        mForgotpwDlg = new Dialog(Ast_Login.this);
        mForgotpwDlg.setTitle(getString(R.string.forgotpw));
        mForgotpwDlg.setCancelable(true);
        mForgotpwDlg.setContentView(R.layout.forgotpw);
        forgot_edtEmail = (EditText) mForgotpwDlg.findViewById(R.id.emailAddress);
        forgot_pwd=(EditText) mForgotpwDlg.findViewById(R.id.forgetpwd);
        forgot_pwd2=(EditText) mForgotpwDlg.findViewById(R.id.forgetpwd2);
        signin_forgot_send = (Button) mForgotpwDlg.findViewById(R.id.forgot_send);
        signin_forgot_send.setOnClickListener(this);
        mForgotpwDlg.show();
    }

    //登出Dialog
    private void u_logoutAccount() {
        mLogoutDlg = new Dialog(Ast_Login.this);
        mLogoutDlg.setTitle(getString(R.string.logout));
        mLogoutDlg.setCancelable(true);

        mLogoutDlg.setContentView(R.layout.ast_logout);

        lougout_agree_send = (Button) mLogoutDlg.findViewById(R.id.ast_logout_btnok);
        lougout_cnacle_send = (Button) mLogoutDlg.findViewById(R.id.ast_logout_btncancle);
        lougout_agree_send.setOnClickListener(this);
        lougout_cnacle_send.setOnClickListener(this);
        mForgotpwDlg.show();

    }

    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        loginon = login.getInt("flag", 0);
        smail=login.getString("Email", "0");
    }

    //送出會員資料
    public void senddata() {
        loginon = 1;//帳密正確 進入登入
        SharedPreferences member =
                getSharedPreferences("as_member", 0);
        member
                .edit()
                .putInt("flag", loginon)
                .putString("Email", smail)
                .commit();
        refresh();

    }

    //刷新頁面
    private void refresh() {
        finish();
        Intent intent = new Intent(Ast_Login.this, Ast_Home.class);
        startActivity(intent);
    }
    //檢查Email格式
    public static boolean checkEmail(String email)
    {// 驗證郵箱的正規表示式
        String format = "^\\w{1,63}@[a-zA-Z0-9]{2,63}\\.[a-zA-Z]{2,63}(\\.[a-zA-Z]{2,63})?$";
//p{Alpha}:內容是必選的，和字母字元[\p{Lower}\p{Upper}]等價。如：200896@163.com不是合法的。
//w{2,15}: 2~15個[a-zA-Z_0-9]字元；w{}內容是必選的。 如：dyh@152.com是合法的。
//[a-z0-9]{3,}：至少三個[a-z0-9]字元,[]內的是必選的；如：dyh200896@16.com是不合法的。
//[.]:'.'號時必選的； 如：dyh200896@163com是不合法的。
//p{Lower}{2,}小寫字母，兩個以上。如：dyh200896@163.c是不合法的。
        if (email.matches(format))
        {
            return true;
        }
        else
        {
            return false;
        }
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

    // 檢查密碼要>5
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    // md5加密
    public static String u_md5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
    // md5加密

    //google登入
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //google登出
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // START_EXCLUDE
                        // END_EXCLUDE
                    }
                });
    }

    //google UI update
    private void updateUI(GoogleSignInAccount account) {
        GoogleSignInAccount aa = account;
        int bb=1;
        //確認帳號是否登入只要加下面這行確認
        if(account!=null){
//            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));
            String g_DisplayName=account.getDisplayName(); //暱稱
            String g_Email=account.getEmail();  //信箱
            String g_ID = account.getDisplayName();
//            String g_GivenName=account.getGivenName(); //Firstname
//            String g_FamilyName=account.getFamilyName(); //Last name
            String check_google = Findemail(g_Email);

            if(check_google==null){
//                tEmail=g_Email;
                mysql_insert_gmail(g_Email,g_DisplayName);
                SharedPreferences member =
                        getSharedPreferences("as_member", 0);
                member
                        .edit()
                        .putInt("flag", loginon)
                        .putString("Email", g_Email)
                        .putString("Password", spwd)
                        .putString("DisplayName",g_DisplayName)
                        .putString("g_ID",g_ID)
                        .commit();
                setupViewComponent();
            }
            loginon = 1;//帳密正確 進入登入

            SharedPreferences member =
                    getSharedPreferences("as_member", 0);
            member
                    .edit()
                    .putInt("flag", loginon)
                    .putString("Email", g_Email)
                    .putString("Password", spwd)
                    .putString("DisplayName",g_DisplayName)
                    .putString("g_ID",g_ID)
                    .commit();
            dbmysql(g_Email);
            setupViewComponent();
        }
    }

    private void mysql_insert_gmail(String g_email,String g_DisplayName) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

        nameValuePairs.add(new BasicNameValuePair("uid", ""));
        nameValuePairs.add(new BasicNameValuePair("name", g_DisplayName));
        nameValuePairs.add(new BasicNameValuePair("email", g_email));
        nameValuePairs.add(new BasicNameValuePair("phone", tphone));
        nameValuePairs.add(new BasicNameValuePair("password", tpassword));
        nameValuePairs.add(new BasicNameValuePair("latitude", ""));
        nameValuePairs.add(new BasicNameValuePair("longitude", ""));
        nameValuePairs.add(new BasicNameValuePair("login_at", ""));
        nameValuePairs.add(new BasicNameValuePair("rank", trank));

        try {
            Thread.sleep(500); //  延遲Thread 睡眠0.5秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//-----------------------------------------------
        String result = DBConnector.executeInsert("SELECT * FROM  as_member ", nameValuePairs);
//-----------------------------------------------
    }

    private String Findemail(String tusername) {
        mContRes = getContentResolver();
//        Cursor cur = mContRes.query(FriendsContentProvider.CONTENT_URI_f00000, MYCOLUMN, null, null, null);
//        cur.moveToFirst(); // 一定要寫，不然會出錯

        String fldSet = null;
        try {
            String sql = "SELECT * FROM as_member WHERE email LIKE " + "'" + tusername + "'" + " ORDER BY id ASC";

            String result = DBConnector.executeQuery(sql);
            JSONArray jsonArray = new JSONArray(result);

            if (result != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    fldSet = jsonData.get("id").toString();
                }
            }
        } catch (Exception e) {
            Log.e("log_tag", e.toString());
        }
        return fldSet;
    }

    private Bitmap getBitmapFromURL(String imageUrl) {
        try{
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        }  catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // --START on_start_sign_in--
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
        //--END on_start_sign_in--
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }
    private void dbmysql(String email) {
        mContRes = getContentResolver();
        Cursor cur = mContRes.query(MemberContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur.moveToFirst(); // 一定要寫，不然會出錯
        // // ---------------------------
        try {
//            String result = DBConnector.executeQuery("SELECT * FROM as_member");
            String result = DBConnector.executeQuery("SELECT * FROM  as_member  WHERE  email  =  "+"'"+smail+"'");
//        String r = result.toString().trim();
//==========================================
            //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
            //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
            Log.d(TAG, "httpstate=" + DBConnector.httpstate);

            if (DBConnector.httpstate == 200) {
                Uri uri = MemberContentProvider.CONTENT_URI;
                mContRes.delete(uri, null, null);  //清空SQLite
            } else {

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

            }
            // --------------------------------------------------------

        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
        cur.close();
        //------------------------------------
//        sqliteupdate();//抓取SQLite資料
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
                Ast_Login.this.finish();
                break;
        }
        return true;
    }
}