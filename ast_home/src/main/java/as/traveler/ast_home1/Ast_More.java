package as.traveler.ast_home1;

import android.app.Dialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

public class Ast_More extends AppCompatActivity implements View.OnClickListener{

    private BottomNavigationView ast_Bottom;
    private Button btnAccount, ast_more_mapbtn;
    private Dialog ast_account_login;
    private AppCompatDialog mLoginDlg;


    public static final int LAUNCH_GAME= 0;
    private int logincheck;

    //SQLiteDataBase
    private static final String DB_FILE = "Account.db", DB_TABLE = "member";
    private static final int DBversion=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast__more);
        u_loaddata();//傳flag進來 1是登入 0是登出
        setupViewcomponent();

        //-----------建立BottomNavigationView物件
        ast_Bottom = (BottomNavigationView)findViewById(R.id.ast_Bottom);

        ast_Bottom.setSelectedItemId(R.id.ast_more);
        BottomNavigationHelper.removeShiftMode(ast_Bottom);  // 生一個外部的class
        ast_Bottom.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        ast_Bottom.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ast_act:
//                        startActivity(new Intent(getApplicationContext(), Ast_Home.class));
                        Ast_More.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.ast_trip:
                        startActivity(new Intent(getApplicationContext(), Ast_trip.class));
                        Ast_More.this.finish();//避免堆疊，本頁結束
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_group:
                        startActivity(new Intent(getApplicationContext(), Ast_Group.class));
                        Ast_More.this.finish();//避免堆疊，本頁結束
                        overridePendingTransition(0, 0);//轉場效果，目前無設定
                        return true;
                    case R.id.ast_col:
                        startActivity(new Intent(getApplicationContext(), Ast_Col_File.class));
                        Ast_More.this.finish();
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.ast_more:
//                        startActivity(new Intent(getApplicationContext(), Ast_More.class));
                        return true;
                }
                return false;
            }
        });
    }

    //登入狀態    接收傳回值
    public void u_loaddata() {
        SharedPreferences login =
                getSharedPreferences("as_member", 0);
        logincheck = login.getInt("flag",1 );
        int aaa=0;
    }

    private void setupViewcomponent(){
        //設定class標題
        this.setTitle(getString(R.string.item_title_more));//設定title
        //----------------------------------------------
        btnAccount = (Button)findViewById(R.id.ast_more_accountbtn);
        ast_more_mapbtn = (Button) findViewById(R.id.ast_more_mapbtn);
//        btnAccount.setOnClickListener(this);// TODO 還沒寫功能，先關掉監聽，改天再打開
        ast_more_mapbtn.setOnClickListener(this);
        btnAccount.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            //會員資料
            case R.id.ast_more_accountbtn:
                if (logincheck==1){
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), Ast_Member.class);
                    int aa = 0;
                    startActivity(intent);
                    this.finish();
                }else if(logincheck==0){
                    Intent it = new Intent();
                    it.setClass(getApplicationContext(), Ast_Login.class);
                    int aab = 0;
                    startActivity(it);
                    this.finish();
                }
                break;
            //路邊停車opendata
            case R.id.ast_more_mapbtn:
                Intent intent = new Intent();
                intent.putExtra("class_title",getString(R.string.item_title_parking));//傳遞title字串
                intent.setClass(getApplicationContext(), Ast_parking.class);
                startActivity(intent);
//                Ast_More.this.finish();//這一連結不能finish啦 !!!!!
                break;
        }
    }
    private Button.OnClickListener loginBtnOKon= new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.ast_logout_btnok:

                    break;
                case R.id.ast_logout_btncancle:    //登出

                    break;
            }
        }
    };
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
                Ast_More.this.finish();
                break;
        }
        return true;
    }
}
