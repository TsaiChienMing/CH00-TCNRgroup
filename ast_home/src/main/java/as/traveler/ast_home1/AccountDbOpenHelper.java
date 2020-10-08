package as.traveler.ast_home1;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AccountDbOpenHelper extends SQLiteOpenHelper {
    String TAG = "tcnr03=>";
    public String sCreateTableCommand;    // 資料庫名稱
    private static final String DB_FILE = "Account.db";
    // 資料庫版本，資料結構改變的時候要更改這個數字，通常是加一
    public static final int VERSION = 1;    // 資料表名稱
    private static final String DB_TABLE = "member";    // 資料庫物件，固定的欄位變數

    private static final String crTBsql = "CREATE     TABLE   " + DB_TABLE + "   ( "
            + "id    INTEGER   PRIMARY KEY," + "account TEXT NOT NULL," + "password TEXT NOT NULL,"
            + "name TEXT NOT NULL," + "mail TEXT NOT NULL);";       ///crTBsql  控制SQL變數   SQL語法貼到此
    private static SQLiteDatabase database;
    private String fldSet;

    //----------------------------------------------
    // 需要資料庫的元件呼叫這個方法，這個方法在一般的應用都不需要修改
    public static SQLiteDatabase getDatabase(Context context){
        if (database == null || !database.isOpen())  {
            database = new AccountDbOpenHelper(context, DB_FILE, null, VERSION)
                    .getWritableDatabase();
        }
        return database;
    }

    public AccountDbOpenHelper(@Nullable Context context,
                               @Nullable String name,
                               @Nullable SQLiteDatabase.CursorFactory factory,
                               int version) {
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


    @Override
    public void onCreate(SQLiteDatabase db) {
//        if (sCreateTableCommand.isEmpty())
//        {
//            return;
//        }
//        db.execSQL(sCreateTableCommand);
        db.execSQL(crTBsql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade()"); //跟著版本再跑

        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);  //如果這table存在，則刪除    sqlite清空沒關係 再從mysql重新匯入

        onCreate(db);      //然後再創一個新的table
    }

    public long insertRec(String b_name, String b_account, String b_password, String b_email) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rec = new ContentValues();
        rec.put("name", b_name);
        rec.put("account", b_account);
        rec.put("password", b_password);
        rec.put("mail", b_email);
        long rowID = db.insert(DB_TABLE, null, rec);
        int aa = 0;
        return rowID;
    }

    public int RecCount() {
        SQLiteDatabase db = getWritableDatabase();
        String sql ="SELECT * FROM "+DB_TABLE+" member ";   //後面可以加where來過濾篩選條件
        Cursor recSet = db.rawQuery(sql, null);
        db.close();
        recSet.close();
        return recSet.getCount();
    }




    public long FindRec(String tname) {
        SQLiteDatabase db = getReadableDatabase();
        String fldSet = "ans=";
        String sql  = "SELECT * \n" +
                "FROM  "+DB_TABLE+
                " WHERE  `account`  LIKE   ?  ORDER BY  `id` ASC ";
        String[] args = {tname };
        Cursor recSet =db.rawQuery(sql, args);   //SQL命令句,條件
        long columnCount = recSet.getColumnCount();//查詢得到的答案共幾筆
        //=============================
        if (recSet.getCount()!=0)
        {
            recSet.moveToFirst();
            fldSet = recSet.getString(0) + " "
                    +recSet.getString(1) + " "
                    +recSet.getString(2) + " "
                    +recSet.getString(3) + " "
                    +recSet.getString(4) + "\n";

            while (recSet.moveToNext()){
                for (int i = 0 ;i<columnCount;i++)
                {
                    fldSet+= recSet.getString(i)+" ";
                }
                fldSet+="\n";
            }
        }
        int aa= recSet.getCount();
        recSet.close();
        db.close();
        return recSet.getCount();
    }

    public ArrayList<String> getRecset() {
        SQLiteDatabase db = getReadableDatabase();
        String sql ="SELECT * FROM   "+DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);  //指標指向整個table
        ArrayList<String> recAry = new ArrayList<String>();
        //==================================
        int columnCount =recSet.getColumnCount();//宣告一個INT 裡面存table欄位數
        while (recSet.moveToNext()){  //當開始搜尋到下一筆的時候
            String fldSet = "";
            for (int i=0;i<columnCount;i++)
                fldSet+=recSet.getString(i)+ "#"; //把全部欄位寫成String寫進去  後面加#當作分隔
            recAry.add(fldSet);
        }
        //=============================
        recSet.close();
        db.close();
        return recAry;   //回傳SQLite的結果
    }


    public int clearRec() {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0) {
            //	String whereClause = "id < 0";
            int rowsAffected = db.delete(DB_TABLE, "1", null); //
            // From the documentation of SQLiteDatabase delete method:
            // To remove all rows and get a count pass "1" as the whereClause.
            db.close();
            return rowsAffected;
        } else {
            db.close();
            return -1;
        }
    }

    public int delectRec(String b_id) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0){
            String whereClause = "id= '" + b_id + "'";   //b_id要被""包起來
            int rowsAffected = db.delete(DB_TABLE, whereClause, null);  //SQLite寫法
            db.close();
            return rowsAffected;
        } else  {
            db.close();
            return -1;
        }
    }

    public int updateRec(String b_id, String b_name, String b_grp, String b_address) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0){
            ContentValues rec =new ContentValues();  //一堆變數的加值
            rec.put("name", b_name);
            rec.put("grp", b_grp);
            rec.put("address", b_address);
            String whereClause = "id=  '" + b_id + "'";
            int rowAffected =db.update(DB_TABLE, rec,whereClause,null);
//            String whereClause = "id= ?";
//            int rowAffected =db.update(DB_TABLE, rec,whereClause,b_id);
//            db.execSQL("DELETE  member  WHERE  id = ' " + b_id + "  '   ");
//            db.execSQL("DELETE  '"+DB_TABLE+"' + WHERE  id =  ' " + b_id + "  ' ");
            db.close();
            return rowAffected;
        }else {
            db.close();
            return -1;
        }
    }

    public String GetRec(String b_account, String b_password) {
        SQLiteDatabase db = getReadableDatabase();
        String fldSet = null;
        String sql = "SELECT * FROM " + DB_TABLE + " WHERE account LIKE ? AND password LIKE ? ORDER BY id ASC";
        String[] args = {b_account , b_password };
        Cursor recSet = db.rawQuery(sql, args);
        int columnCount = recSet.getColumnCount();
        if (recSet.getCount() != 0) {
            recSet.moveToFirst();
            fldSet = recSet.getString(0) + " "
                    + recSet.getString(1) + " "
                    + recSet.getString(2) + " "
                    + recSet.getString(3) + " "
                    + recSet.getString(4) + "\n";
            while (recSet.moveToNext()) {
                for (int i = 0; i < columnCount; i++) {
                    fldSet += recSet.getString(i) + " ";
                }
                fldSet+="\n";
            }
        }
        recSet.close();
        db.close();
        return fldSet;
    }

    public String Findid(String account) {
        SQLiteDatabase db = getReadableDatabase();
        String fldSet = null;
        String sql = "SELECT * FROM " + DB_TABLE + " WHERE account LIKE ?";
        String[] args = {account};
        Cursor recSet = db.rawQuery(sql, args);

        int columnCount = recSet.getColumnCount();
        if (recSet.getCount() != 0) {
            recSet.moveToFirst();
            fldSet = recSet.getString(0);
            while (recSet.moveToNext()) {
                for (int i = 0; i < columnCount; i++) {
                    fldSet += recSet.getString(i) + " ";
                }
                fldSet+="\n";
            }
        }
        recSet.close();
        db.close();
        return fldSet;
    }

    public int updatePwd(String u_id, String pwd1) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql,  null);

        if (recSet.getCount() != 0) {
            ContentValues rec = new ContentValues();
            rec.put("password", pwd1);
            String whereClause = "id = '" + u_id + "'";
            int rowsAffected = db.update(DB_TABLE, rec, whereClause, null);
            db.close();
            return rowsAffected;
        } else {
            db.close();
            return -1;
        }
    }
}


