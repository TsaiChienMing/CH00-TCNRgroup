package as.traveler.ast_home1;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public  class FriendDbOpenHelper extends SQLiteOpenHelper {
    private  String sCreateTableCommand;

    public FriendDbOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        sCreateTableCommand="";    //去掉final

    }
    //傳入的參數說明
//  context: 用來開啟或建立資料庫的應用程式物件，如 Activity 物件
//  name: 資料庫檔案名稱，若傳入 null 表示將資料庫暫存在記憶體
//  factory: 用來建立指標物件的類別，若傳入 null 表示使用預設值
//  version: 即將要建立的資料庫版本 (版本編號從 1 開始)
//          若資料庫檔案不存在，就會呼叫 onCreate() 方法
//          若即將建立的資料庫版本比現存的資料庫版本新，就會呼叫 onUpgrade() 方法
//          若即將建立的資料庫版本比現存的資料庫版本舊，就會呼叫 onDowngrade() 方法
//  errHandler: 當資料庫毀損時的處理程式，若傳入 null 表示使用預設的處理程式


    public FriendDbOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);


    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public FriendDbOpenHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (sCreateTableCommand.isEmpty())
        {
            return;
        }
        db.execSQL(sCreateTableCommand);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
