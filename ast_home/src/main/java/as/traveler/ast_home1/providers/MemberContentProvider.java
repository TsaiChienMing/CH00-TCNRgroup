package as.traveler.ast_home1.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MemberContentProvider extends ContentProvider {
    private static final String AUTHORITY = "as.traveler.ast_login1001";      //packgename
    private static final String DB_FILE = "member.db", DB_TABLE = "as_member";
    private static final int URI_ROOT = 0;
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TABLE);

    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, DB_TABLE, CONTACTS);
        sUriMatcher.addURI(AUTHORITY, DB_TABLE + "/#", CONTACT_ID);
    }

    private SQLiteDatabase mFriendDb;
    String TAG = "tcnr03=>";
    private static final String crTBsql = " CREATE TABLE   " + DB_TABLE +  " ( "
            + "id INTEGER PRIMARY KEY,"
            + "uid TEXT ,"
            + "name TEXT ,"
            + "sex TEXT ,"
            + "birth TEXT ,"
            + "email TEXT ,"
            + "password TEXT ,"
            + "phone TEXT ,"
            + "latitude TEXT ,"
            + "longitude TEXT ,"
            + "rank TEXT ,"
            + "create_at TEXT ,"
            + "login_at TEXT   );";
//private static final String crTBsql= "CREATE     TABLE   " + DB_TABLE + "   ( "
//        + "id    INTEGER   PRIMARY KEY," + "grp_name TEXT NOT NULL," + "creator_id TEXT,"
//        + "member_id TEXT," + "alert TEXT," + "points TEXT);" ;

    @Override
    public boolean onCreate() {
        // ---宣告 使用Class DbOpenHelper.java 作為處理SQLite介面
        // Content Provider 就是 data Server, 負責儲存及提供資料, 他允許任何不同的APP使用
        // 共同的資料(不同的APP用同一個SQLite).

        DbOpenHelper MemberContentProvider = new DbOpenHelper(getContext(), DB_FILE, null, 1);

        mFriendDb = MemberContentProvider.getWritableDatabase();
        // 檢查資料表是否已經存在，如果不存在，就建立一個。
        Cursor cursor = mFriendDb
                .rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + DB_TABLE + "'", null);
        if (cursor != null) {
            if (cursor.getCount() == 0) // 沒有資料表，要建立一個資料表。
                mFriendDb.execSQL(crTBsql);
            cursor.close();
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (sUriMatcher.match(uri) != CONTACTS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
//  Cursor c = mFriendDb.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
        Cursor c = mFriendDb.query(true, DB_TABLE, projection, selection, selectionArgs, null, null, sortOrder, null); //"ASC DESC"
        c.setNotificationUri(getContext().getContentResolver(), uri);
        int f = 46;
        return c;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (sUriMatcher.match(uri) != CONTACTS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        long rowId = mFriendDb.insert(DB_TABLE, null, values);
        if (rowId > 0) {
            // 在已有的 Uri的後面追加ID數據
            Uri insertedRowUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            ;
            // 通知數據已經改變
            getContext().getContentResolver().notifyChange(insertedRowUri, null);
            return insertedRowUri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (sUriMatcher.match(uri) != CONTACTS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        int rowsAffected = mFriendDb.delete(DB_TABLE, selection, null);
        return rowsAffected;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (sUriMatcher.match(uri) != CONTACTS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        int rowsAffected = mFriendDb.update(DB_TABLE, values, selection, null);
        return rowsAffected;
    }
    // ------------------------
}

//}
