package as.traveler.ast_home1;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Ast_Member extends AppCompatActivity implements View.OnClickListener {
    //登出Dialog
    private Dialog  mLogoutDlg;
    private Button lougout_agree_send,lougout_cnacle_send;

    //SQLiteDataBase
    private static final String DB_FILE = "Account.db", DB_TABLE = "member";
    private static final int DBversion=1;
    private int logincheck;
    private Button memberlist,logoutbtn,changepwd;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_more_member);
        setupViewComponent();
        u_loaddata();

    }

    private void setupViewComponent() {
        memberlist= (Button)findViewById(R.id.ast_member_data);
        logoutbtn = (Button)findViewById(R.id.ast_member_logout);
        changepwd= (Button)findViewById(R.id.ast_member_chpwd);
        memberlist.setOnClickListener(this);
        logoutbtn.setOnClickListener(this);
        changepwd.setOnClickListener(this);
    }


    //讀取登入資料
    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        logincheck = login.getInt("flag", 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.ast_member_data:
                Intent it = new Intent();
                it.setClass(getApplicationContext(), Ast_Memberlist.class);
                startActivity(it);
                break;
            case R.id.ast_member_logout:
                                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.logout))
                            .setMessage(getString(R.string.ast_more_logout_title))
                            .setCancelable(true)
                            .setIcon(R.drawable.logo)
                            .setPositiveButton(getString(R.string.ast_more_login_check),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            logincheck = 0;//登出
                                            SharedPreferences login =
                                                    getSharedPreferences("as_member",0);
                                            login
                                                    .edit()
                                                    .putInt("flag", logincheck)
                                                    .commit();
                                            refresh();

                                            Ast_Member.this.finish();
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
            case R.id.ast_member_chpwd:
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), Ast_Changepwd.class);
                    startActivity(intent);
                break;
        }
    }
    //刷新頁面
    private void refresh() {
        finish();
        Intent intent = new Intent(Ast_Member.this, Ast_Home.class);
        startActivity(intent);
    }
    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //--START_EXCLUDE--
                updateUI(null);
                // [END_EXCLUDE]
            }
        });
    }
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

            logincheck = 1;//帳密正確 進入登入

            SharedPreferences member =
                    getSharedPreferences("as_member", 0);
            member
                    .edit()
                    .putInt("flag", logincheck)
                    .putString("Email", g_Email)
                    .putString("DisplayName",g_DisplayName)
                    .putString("g_ID",g_ID)
                    .commit();
            refresh();

        }
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
                Ast_Member.this.finish();
                break;
        }
        return true;
    }
}
