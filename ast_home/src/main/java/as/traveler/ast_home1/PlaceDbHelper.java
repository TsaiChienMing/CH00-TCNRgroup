package as.traveler.ast_home1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class PlaceDbHelper extends SQLiteOpenHelper {
    private static final DatabaseErrorHandler errorHandler = null;
    private static final Context context = null;
    String TAG = "TCNR01=>";
    public String sCreateTableCommand;    // 資料庫名稱
    private static final String DB_FILE = "friends.db";
    // 資料庫版本，資料結構改變的時候要更改這個數字，通常是加一
    public static final int VERSION = 1;    // 資料表名稱
    private static final String DB_TABLE = "attraction";    // 資料庫物件，固定的欄位變數
    private static final String DB_TABLE2 = "collection";    // 收藏列的TABLE名稱

    private static final String crTBsql = "CREATE TABLE " + DB_TABLE + " ( "
            + "id INTEGER PRIMARY KEY," + "name TEXT NOT NULL," + "lat DECIMAL,"
            + "lng DECIMAL," + "brief TEXT," + "area TEXT," + "address TEXT," + "phone TEXT," + "uid TEXT," + "imageurl TEXT," + "thumburl TEXT);";

    //=====================生成加入收藏的SQLite資料庫的TABLE==============================================
    private static final String collectDBsql = "CREATE TABLE " + DB_TABLE2 + " ( "
            + "id INTEGER PRIMARY KEY ," + "email TEXT ," + "att_name TEXT NOT NULL," + "att_brief TEXT,"+"att_img TEXT,"+"att_uid TEXT);";
    private static SQLiteDatabase database;

    //----------------------------------------------
    // 需要資料庫的元件呼叫這個方法，這個方法在一般的應用都不需要修改
//    public static SQLiteDatabase getDatabase(Context context){
//        if (database == null || !database.isOpen())
//        {
//            database = new PlaceDbHelper(context, DB_FILE, null, VERSION)
//                    .getWritableDatabase();
//        }
//        return database;
//    }

    public PlaceDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {

//傳入的參數說明
//		context: 用來開啟或建立資料庫的應用程式物件，如 Activity 物件
//		name: 資料庫檔案名稱，若傳入 null 表示將資料庫暫存在記憶體
//		factory: 用來建立指標物件的類別，若傳入 null 表示使用預設值
//		version: 即將要建立的資料庫版本 (版本編號從 1 開始)
//		        若資料庫檔案不存在，就會呼叫 onCreate() 方法
//		        若即將建立的資料庫版本比現存的資料庫版本新，就會呼叫 onUpgrade() 方法
//		        若即將建立的資料庫版本比現存的資料庫版本舊，就會呼叫 onDowngrade() 方法
//		errHandler: 當資料庫毀損時的處理程式，若傳入 null 表示使用預設的處理程式
//                super(context, name, factory, version);
        super(context, "friends.db", null, 1);
        sCreateTableCommand = "";

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public PlaceDbHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {db.execSQL(crTBsql);db.execSQL(collectDBsql);
    }


    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //修改Version之後就會執行這邊
        Log.d(TAG, "onUpgrade()");

        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
        //修改資料庫，有檔案不能改，要先移除原有的才能重新上傳資料
        onCreate(db);
    }
    public long insertRec(String b_name, String b_lat, String b_lng, String b_brief, String b_area, String b_address, String b_phone, String b_uid, String b_imageurl, String b_thumburl) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rec = new ContentValues();
        rec.put("name", b_name);
        rec.put("lat", b_lat);
        rec.put("lng", b_lng);
        rec.put("brief", b_brief);
        rec.put("area", b_area);
        rec.put("address", b_address);
        rec.put("phone", b_phone);
        rec.put("uid", b_uid);
        rec.put("imageurl", b_imageurl);
        rec.put("thumburl", b_thumburl);
        long rowID = db.insert(DB_TABLE, null, rec);
        db.close();
        return rowID;
    }


    public int RecCount() {
        SQLiteDatabase db = getWritableDatabase();
        //上面方法也有，重寫是一次是怕出錯，因為很多最後都有寫db.close()，要重連一次
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        //--------------------------------
//        recSet.close();
//        db.close();

        //------------方便debug用---------------
        int a = recSet.getCount();
        int bb = 0;
        recSet.close();//要放這位置，在上面就先關掉cursor的話會閃退
        db.close();//原先的寫法回傳還沒執行就關掉，會GG
        return a;
        //-------------跟return那行一樣--------------
//        return recSet.getCount();
    }

        public String FindUid(int tId) {
        SQLiteDatabase db = getReadableDatabase(); //讀資料
        String fldSet = "";
//        String sql = "SELECT * FROM "+DB_TABLE+" WHERE uid LIKE ? ORDER BY id ASC"; //貼上sql查詢語法，注意空格
        String sql = "SELECT uid FROM "+DB_TABLE+" WHERE id ="+tId; //貼上sql查詢語法，注意空格
//        String[] args = {Integer.toString(tId)}; //設定查詢內容
        Cursor recSet = db.rawQuery(sql,null); //資料游標
        recSet.moveToFirst();
        fldSet = recSet.getString(0) ;//因為我只查詢一筆資料，直接讀取出來
//        int columnCount = recSet.getColumnCount();
//        int a=recSet.getCount();
//        int x=0;

//        if(recSet.getCount()!=0){
//            recSet.moveToFirst();
//            fldSet = recSet.getString(0) + " "
//                    +recSet.getString(1) + " "
//                    +recSet.getString(2) + " "
//                    +recSet.getString(3) + "\n";
//            //上面列出第一列四欄位(id,name,lat,lng)
//            //下面要是還有下一列，就新增新的一列四欄位，直到沒有下一列為止
//            while (recSet.moveToNext()){
//                for(int i=0; i<columnCount; i++){
//                    fldSet = recSet.getString(i);
//                }
////                fldSet += "\n";
//            }
//        }
        recSet.close();
        db.close();
        return fldSet;
    }

    public ArrayList<String> getRecSet(){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        ArrayList<String> recAry = new ArrayList<String>();

        //----------------------------
        Log.d(TAG,"recSet="+recSet);
        int columnCount = recSet.getColumnCount();
        while(recSet.moveToNext()){
            String fldSet = "";
            for(int i=0; i<columnCount; i++)
                fldSet += recSet.getString(i) + "##";
            recAry.add(fldSet);
        }
        //------------------------
        recSet.close();
        db.close();

        Log.d(TAG,"recAry="+recAry);
        return recAry;
    }
    public ArrayList<String> getRecSet_col(String email) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE2 +" WHERE email ='"+email+"';";
        Cursor recSet_col = db.rawQuery(sql, null);
        ArrayList<String> recAry = new ArrayList<String>();

        //----------------------------
        Log.d(TAG,"recSet="+recSet_col);
        int columnCount = recSet_col.getColumnCount();
        while(recSet_col.moveToNext()){
            String fldSet = "";
            for(int i=0; i<columnCount; i++)
                fldSet += recSet_col.getString(i) + "##";
            recAry.add(fldSet);
        }
        //------------------------
        recSet_col.close();
        db.close();

        Log.d(TAG,"recAry="+recAry);
        return recAry;
    }
    public ArrayList<String> getRecSet_colCheckAll() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE2 +";";
        Cursor recSet_col = db.rawQuery(sql, null);
        ArrayList<String> recAry = new ArrayList<String>();

        //----------------------------
        Log.d(TAG,"recSet="+recSet_col);
        int columnCount = recSet_col.getColumnCount();
        while(recSet_col.moveToNext()){
            String fldSet = "";
            for(int i=0; i<columnCount; i++)
                fldSet += recSet_col.getString(i) + "##";
            recAry.add(fldSet);
        }
        //------------------------
        recSet_col.close();
        db.close();

        Log.d(TAG,"recAry="+recAry);
        return recAry;
    }

    public ArrayList<String> getRecSet_search(String editText){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM "+DB_TABLE+" WHERE name LIKE ? ORDER BY id ASC";
        String[] args = {"%"+editText+"%"};//設定查詢內容
        Cursor recSet = db.rawQuery(sql, args);
        ArrayList<String> recAry = new ArrayList<String>();

        //----------------------------
        Log.d(TAG,"recSet="+recSet);
        int columnCount = recSet.getColumnCount();
        while(recSet.moveToNext()){
            String fldSet = "";
            for(int i=0; i<columnCount; i++)
                fldSet += recSet.getString(i) + "##";
            recAry.add(fldSet);
        }
        //------------------------
        recSet.close();
        db.close();

        Log.d(TAG,"recAry="+recAry);
        return recAry;
    }
    //======================point加入收藏的SQLite欄位====================================================
    public long insertcollect(String email, String act_site, String act_introduction,String s,String uid) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues rec = new ContentValues();
        rec.put("email", email);
        rec.put("att_name", act_site);
        rec.put("att_brief", act_introduction);
        rec.put("att_img", s);
        rec.put("att_uid",uid);


        long collect = db.insert(DB_TABLE2, null, rec);
        db.close();
        return collect;
    }

//================================================================================
    public int clearRec() {//清除資料
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0) {
            //			String whereClause = "id < 0";
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
    public int clearRec_col() {//清除收藏資料
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE2;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0) {
            //			String whereClause = "id < 0";
            int rowsAffected = db.delete(DB_TABLE2, "1", null); //
            // From the documentation of SQLiteDatabase delete method:
            // To remove all rows and get a count pass "1" as the whereClause.
            recSet.close();
            db.close();
            return rowsAffected;
        } else {
            recSet.close();
            db.close();
            return -1;
        }
    }

    public int delectRec(String att_uid,String email){//給收藏刪除資料用
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + DB_TABLE2;
        Cursor recSet = db.rawQuery(sql, null);
        if (recSet.getCount() != 0) {
            //			String whereClause = "id < 0";
            int rowsAffected = db.delete(DB_TABLE2, "att_uid = '"+att_uid+"' AND email ='"+email+"'", null); //
            // From the documentation of SQLiteDatabase delete method:
            // To remove all rows and get a count pass "1" as the whereClause.
            db.close();
            return rowsAffected;
        } else {
            db.close();
            return -1;
        }
    }
//point頁面使用，根據uid取得id
    public int findIndex(String act_uid) {
        SQLiteDatabase db = getReadableDatabase(); //讀資料
        String fIndexSet = "";
        String sql = "SELECT id FROM "+DB_TABLE+" WHERE uid ='"+act_uid+"'"; //貼上sql查詢語法，注意空格
        Cursor recSet = db.rawQuery(sql,null); //資料游標
        recSet.moveToFirst();
        fIndexSet = recSet.getString(0) ;
        int returnId = Integer.parseInt(fIndexSet);
        recSet.close();
        db.close();
        return returnId;
    }
//Col收藏頁面使用，根據index取得uid，不同使用者的情況下 email也不同
    public String FindUid_col(int i, String email) {
        SQLiteDatabase db = getReadableDatabase(); //讀資料
        String fldSet_col = "";
        String sql = "SELECT att_uid FROM "+DB_TABLE2 +" WHERE email ='"+email+"'"; //貼上sql查詢語法，注意空格
        Cursor recSet = db.rawQuery(sql,null); //資料游標
        recSet.moveToPosition(i);//關鍵在這
        fldSet_col = recSet.getString(0) ;
        recSet.close();
        db.close();
        return fldSet_col;
    }
//point頁面使用，加入最愛時，去確認是否有重複的uid，避免重複加入
    public int checkUid(String act_uid, String email) {
        SQLiteDatabase db = getReadableDatabase(); //讀資料
        String fIndexSet = "";
        String sql = "SELECT email FROM "+DB_TABLE2+" WHERE att_uid ='"+act_uid+"'"; //貼上sql查詢語法，注意空格
        Cursor recSet = db.rawQuery(sql,null); //資料游標
        if(recSet.getCount()==0){//假如在UID條件下搜不到任何email帳號，就回傳0
            recSet.close();
            db.close();
            return 0;
        }else{//假如有搜到任何UID加過收藏資料
            recSet.moveToFirst();//先檢查第一筆
            fIndexSet = recSet.getString(0) ;
            if(fIndexSet.equals(email)){//比對是否有相同的email登錄過
                recSet.close();
                db.close();
                return -1;//要是有就回傳-1
            }
            while(recSet.moveToNext()){//第二筆之後都run一次
                fIndexSet = recSet.getString(0) ;
                if(fIndexSet.equals(email)){//比對是否有相同的email登錄過
                    recSet.close();
                    db.close();
                    return -1;//要是有就回傳-1
                }
            }
            recSet.close();
            db.close();
            return 0;//比對不到相同的email，回傳0
        }
    }
}




