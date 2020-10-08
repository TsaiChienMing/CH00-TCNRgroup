package as.traveler.ast_home1;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public  class Ast_Changepwd extends AppCompatActivity implements View.OnClickListener {
    //SQLiteDataBase
    private int logincheck;
    private EditText epassword1,epassword2;
    private Button okbtn,canclebtn;
    private String pwd1,pwd2;
    private String account;
    private int rowsAffected;
    private String msg;
    private static ContentResolver mContRes;
    private String[] MYCOLUMN = new String[]{"id", "uid", "name", "email", "password", "phone", "latitude", "longitude", "rank", "create_at", "login_at" };
    private String c_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_changepwd);
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
        u_loaddata();
    }

    private void setupViewComponent() {
        epassword1 = (EditText)findViewById(R.id.ast_pwd_e001);
        epassword2= (EditText)findViewById(R.id.ast_pwd_e002);
        okbtn = (Button)findViewById(R.id.ast_pwd_btnOK);
        canclebtn = (Button)findViewById(R.id.ast_pwd_cancel);

        okbtn.setOnClickListener(this);
        canclebtn.setOnClickListener(this);
    }


    //讀取登入資料
    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        logincheck = login.getInt("flag", 0);
        account = login.getString("Email","");
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.ast_pwd_btnOK:
                updatepwd();
                break;
            case R.id.ast_pwd_cancel:
                this.finish();
                break;
        }
    }
    public void updatepwd(){
        c_pwd=epassword1.getText().toString().trim();
        String password = u_md5(c_pwd);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("email", account));
        String aa= DBConnector.executeUpdate("SELECT * FROM  as_member ", nameValuePairs);
        this.finish();
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
}
