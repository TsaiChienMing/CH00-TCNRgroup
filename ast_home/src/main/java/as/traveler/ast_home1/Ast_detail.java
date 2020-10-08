package as.traveler.ast_home1;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import as.traveler.ast_home1.providers.DetailContentProvider;
import as.traveler.ast_home1.providers.DetailattrContentProvider;
import as.traveler.ast_home1.providers.ScheduleContentProvider;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class Ast_detail extends AppCompatActivity {
    private TextView plan_name,plan_departuretime,plan_days;
    private String name;
    private String departuretime;
    private String sdays;
    private String id;
    private String whichday;

    private LinearLayout mLinear;
    private List<String> titleList;//放置標題的集合
    private List<Integer> ivList;//放logo的集合
    private int[] iv = {R.drawable.d1,R.drawable.d2,R.drawable.d3,R.drawable.d4,R.drawable.d5}; //圖片集合


//detailMYSQL
    private ContentResolver mContRes;
    private String[] MYCOLUMN=new String[]{"id","sch_id","day","att_id"};
    private String msg;
    private int tcount;
//    detail查詢出來的uid 再去比對抓取名字圖片



    //detailattrMYSQL
    private String[] attr_MYCOLUMN=new String[]{"id","name","sch_id","uid","thumburl","day"};
    //    private ContentResolver mContRes;

    //    recyclerview
    private ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DetailListAdapter myAdapter;
    private TextView trip_plan_selectday;

    //   新增行程
    private int selectday=0;            //給新增button判斷要加在哪天
    private Button trip_plan_new;
    private View btCancel;
    private View detail_from_collect;
    private View detail_from_attr;
    private BottomSheetDialog bottomSheetDialog;

//   序
    private Handler mysql =new Handler();
    private LinearLayout linear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ast_detail_plan);
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
        setupViewcomponent();

    }

    private void setupViewcomponent() {
        trip_plan_new=(Button)findViewById(R.id.trip_plan_new);
        trip_plan_new.setOnClickListener(newattr);

        plan_name=(TextView)findViewById(R.id.trip_plan_name);
        plan_departuretime=(TextView)findViewById(R.id.trip_plan_departuretime);
        plan_days=(TextView)findViewById(R.id.trip_plan_days);
//        ===========intent行程名稱 時間 天數==========================================
        Intent intent = getIntent();
        whichday=intent.getStringExtra("WHICHDAY");
         id = intent.getStringExtra("ID");
         name = intent.getStringExtra("NAME");
         departuretime = intent.getStringExtra("DEPARTURETIME");
         sdays = intent.getStringExtra("DAYS");


        setTitle(name);
        plan_name.setText(name);
        plan_departuretime.setText(departuretime);
        plan_days.setText(sdays+getString(R.string.day));

        /**
         * 處理資料,可以是伺服器請求過來的,也可以是本地的
         */
        //要新增view的容器
        mLinear = (LinearLayout) findViewById(R.id.trip_plan_linear);
        ivList = new ArrayList<>();
        titleList = new ArrayList<>();
        //設置陣列內容
        for (int i = 1; i < Integer.parseInt(sdays)+1; i++) {
            ivList.add(iv[i-1]);
            titleList.add("第" + i + "天");
        }
        //資料拿到之後去根據資料去動態新增View
        addView();

        //=============================================================================================

        //設置RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        myAdapter = new DetailListAdapter();
        recyclerView.setAdapter(myAdapter);

        recyclerViewAction(recyclerView, arrayList, myAdapter);//滑動動作

//==============================================================
//        新增選擇dialog
         bottomSheetDialog = new BottomSheetDialog(this);//初始化BottomSheet
        View view = LayoutInflater.from(this).inflate(R.layout.ast_detail_newitem,null);//連結的介面
         btCancel = view.findViewById(R.id.button_cancel);
         detail_from_collect = view.findViewById(R.id.detail_from_collect);
         detail_from_attr = view.findViewById(R.id.detail_from_attr);
        btCancel.setOnClickListener(selectfrom);
        detail_from_collect.setOnClickListener(selectfrom);
        detail_from_attr.setOnClickListener(selectfrom);
        bottomSheetDialog.setContentView(view);//將介面載入至BottomSheet內
        ViewGroup parent = (ViewGroup) view.getParent();//取得BottomSheet介面設定
        parent.setBackgroundResource(android.R.color.transparent);//將背景設為透明，否則預設白底
//        UPDATE_detail.attr_update("44","1","1146,1,");
        }//setupviewcomponent

    Button.OnClickListener selectfrom=new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.button_cancel:
                    bottomSheetDialog.dismiss();
                    break;
                case R.id.detail_from_collect:
                    Toast.makeText(getApplicationContext(),"施工中", Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
                    break;
                case R.id.detail_from_attr:

                    Intent it = new Intent();
                    //從這到
                    it.setClass(Ast_detail.this, Ast_act.class);
                    Bundle bundle = new Bundle();
                    //行程名稱,出發時間,儲存天數 傳值
                    bundle.putString("sch_id", id);
                    bundle.putString("day", String.valueOf(selectday));
                    it.putExtras(bundle);
                    startActivity(it);
                    bottomSheetDialog.dismiss();
                    break;

            }
        }
    };
    /**
     * 動態新增的具體實現
     */
    private void addView() {
        //ivList集合有幾個元素就新增幾個
        for (int i = 0; i < ivList.size(); i++) {
            //首先引入要新增的View
            View view = View.inflate(this, R.layout.ast_detail_plandays, null);
            //找到裡面需要動態改變的控制元件
            ImageView ivLogo = (ImageView) view.findViewById(R.id.trip_plandays_pic);

            //給控制元件賦值
            ivLogo.setImageResource(ivList.get(i));

            /*
            動態給每個View設定margin,也可以在xml裡面設定,xml裡面設定後每個view之間的間距都是一樣的
            動態設定可以給每個view之間的間距設定的不一樣 params.setMargins(int left, int top, int right, int bottom);
             */
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(params);
            params.setMargins(0,0,0,0);

            //           設定tag讓onClick可以抓取到是哪個按鈕
            view.setTag(i);
            //設定每個View的點選事件
            view.setOnClickListener(select_day);


            //把所有動態建立的view都新增到容器裡面
            mLinear.addView(view);
        }

    }
    Button.OnClickListener newattr=new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            bottomSheetDialog.show();//顯示BottomSheet
        }
    };


    View.OnClickListener select_day =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = (Integer) v.getTag();
            selectday=tag+1;
            whichday=String.valueOf(selectday);
            mysql.postDelayed(updatemysql, 1000);
            Snackbar.make(v, "您點選第"+(tag+1)+"天",Snackbar.LENGTH_SHORT).setAction("", null).show();

            trip_plan_selectday.setText("第"+(tag+1)+"天");
            arrayList.clear();
//       先清空再讀之後再想放哪
            Uri uri = DetailattrContentProvider.CONTENT_URI;
            mContRes.delete(uri, "  sch_id= "+String.valueOf(id), null);  //清空當下sch_id當天SQLite
//        設定在按鈕按下去再搜尋該天
            detaildbmysql(tag+1);

        }
    };
    private void detaildbmysql(int whichday) {

//        讀入sqlite
        mContRes = getContentResolver();
        Cursor cur = mContRes.query(DetailContentProvider.CONTENT_URI, MYCOLUMN, null, null, null);
        cur.moveToFirst(); // 一定要寫，不然會出錯
        // // ---------------------------
        try {
            String result = DetailDBConnector.executeQuery(" SELECT *  FROM  detail WHERE  `sch_id` = "+" '"+id+"' "+" AND `day` = "+"'"+whichday+"'");  //搜尋登入者帳號的行程

//==========================================
            //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
            //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)

            if (DetailDBConnector.httpstate == 200) {
                Uri uri = DetailContentProvider.CONTENT_URI;
                mContRes.delete(uri, "day="+whichday+" and sch_id= "+String.valueOf(id), null);  //清空當天SQLite

            } else {


                msg=getString(R.string.error_msg_internet);

                Toast.makeText(getBaseContext(), msg,  Toast.LENGTH_LONG).show();
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
                Uri uri = DetailContentProvider.CONTENT_URI;
                mContRes.delete(uri, "day="+whichday+" and sch_id= "+String.valueOf(id), null); // 匯入前,刪除當天SQLite資料

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

                    }

                    // -------------------加入SQLite---------------------------------------
                    mContRes.insert(DetailContentProvider.CONTENT_URI, newRow);

                }

                // ---------------------------
            } else {
                Toast.makeText(Ast_detail.this, "主機資料庫無資料", Toast.LENGTH_LONG).show();
            }
            // --------------------------------------------------------

        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
        cur.close();
        //------------------------------------
        detailsqliteSetList(whichday);//放資料進SQLite更新
    }



    private void detailsqliteSetList(int whichday) {
        //抓取SQLite資料
        ArrayList<String> recAry = new ArrayList<String>();
        mContRes = getContentResolver();
        Cursor c = mContRes.query(DetailContentProvider.CONTENT_URI, MYCOLUMN, "day="+whichday+" and sch_id= "+String.valueOf(id), null, null);
        tcount = c.getCount();
        int columnCount = c.getColumnCount();
        while (c.moveToNext()) {
            String fldSet = "";
            for (int ii = 0; ii < columnCount; ii++) {
                fldSet += c.getString(ii) + "#";
            }
            recAry.add(fldSet);
        }

        c.close();
        if(recAry.isEmpty()) {
//            清空recyclerview
            arrayList.clear();
            myAdapter.notifyDataSetChanged();
        } else{

            //最後一筆寫進陣列(會讀到前面天數)
            String[] fld = recAry.get(recAry.size()-1).split("#");
            //whichday現在正在讀取的是第幾天的資料
            // uid放入String在做查詢

                String   uidlist=fld[3];
                String[] uidarray = uidlist.split(",");

                for(int k=0;k<uidarray.length;k++){
                    detailattrdbmysql(whichday,uidarray[k]);
                }



        }


        }

    private void detailattrdbmysql(int whichday,String uid) {

//        讀入sqlite
        mContRes = getContentResolver();
        Cursor cur1 = mContRes.query(DetailattrContentProvider.CONTENT_URI, attr_MYCOLUMN, null, null, null);
        cur1.moveToFirst(); // 一定要寫，不然會出錯
        // // ---------------------------
        try {
            String result = DetailDBConnector.executeQuery(" SELECT `name`,`uid`,`thumburl`  FROM  attraction WHERE  `uid` = "+" '"+uid+"' ");  //搜尋attraction 抓下景點uid跟name


//==========================================
            //以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
            //存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)


            if (DetailDBConnector.httpstate != 200) {
                msg=getString(R.string.error_msg_internet);
                Toast.makeText(getBaseContext(), msg,  Toast.LENGTH_SHORT).show();
            }
//======================================

            /*******************************************************************************************
             * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
             * jsonData = new JSONObject(result);
             *******************************************************************************************/
            JSONArray jsonArray = new JSONArray(result);

            // -------------------------------------------------------
            if (jsonArray.length() > 0) { // MySQL 連結成功有資料
                Uri uri = DetailattrContentProvider.CONTENT_URI;
//                mContRes.delete(uri, "day="+whichday+" and sch_id= "+String.valueOf(id), null); // 匯入前,刪除當天SQLite資料

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

                    }
//                    補齊SQLite欄位(mysql抓下來缺少的)
                    newRow.put("sch_id",id);
                    newRow.put("day",whichday);

                    // -------------------加入SQLite---------------------------------------
                    mContRes.insert(DetailattrContentProvider.CONTENT_URI, newRow);

                }

                // ---------------------------
            } else {
                Toast.makeText(Ast_detail.this, "主機資料庫無資料", Toast.LENGTH_LONG).show();
            }
            // --------------------------------------------------------

        } catch (Exception e) {
            // Log.e("log_tag", e.toString());
        }
        cur1.close();
        //------------------------------------
        detailattrsqliteSetList(whichday);//放資料進SQLite更新傳入第幾天的參數
    }



    private void detailattrsqliteSetList(int whichday) {

        //抓取SQLite資料
        ArrayList<String> recAry = new ArrayList<String>();
        mContRes = getContentResolver();
        Cursor c = mContRes.query(DetailattrContentProvider.CONTENT_URI, attr_MYCOLUMN, "day= "+whichday, null, null);
        tcount = c.getCount();
        int columnCount = c.getColumnCount();
        while (c.moveToNext()) {
            String fldSet = "";
            for (int ii = 0; ii < columnCount; ii++)
                fldSet += c.getString(ii) + "#";
            recAry.add(fldSet);
        }

        c.close();

//        寫進陣列
            try{
                String[] fld = recAry.get(recAry.size()-1).split("#");
                HashMap<String,String> hashMap = new HashMap<>();
//            fld[0-5]  id name  schid  uid  thuburl day
                hashMap.put("id",fld[0]);
                hashMap.put("name",fld[1]);
                hashMap.put("sch_id",fld[2]);
                hashMap.put("uid",fld[3]);
                hashMap.put("thuburl",fld[4]);
                hashMap.put("day",fld[5]);
                arrayList.add(hashMap);
                myAdapter.notifyDataSetChanged();
            }catch (Exception e){
                arrayList.clear();
                myAdapter.notifyDataSetChanged();
//                    新增行程text要出來
            }





    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ast_detail_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                Ast_detail.this.finish();
                break;
            case R.id.hint:

                AlertDialog.Builder hint = new AlertDialog.Builder(Ast_detail.this);

                hint.setTitle(getString(R.string.howtouse));
                hint.setMessage( getString(R.string.hintmsg));

                hint.show();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        this.finish();
        Toast.makeText(getApplicationContext(), R.string.error_back, Toast.LENGTH_LONG).show();
    }

//   Recyclerview的adpter
    private class DetailListAdapter extends RecyclerView.Adapter<DetailListAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {

//            在此findviewbyid 範例layout物件
//###################################################################################################
             ImageView img01_pic;
            TextView t01_name;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                t01_name = itemView.findViewById(R.id.detail_sketch_t01);
                img01_pic = itemView.findViewById(R.id.detail_sketch_img01);

            }
        }
    //###################################################################################################

//======================================================================================
//      此方法放入範例layout
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ast_detail_sketch, parent, false);
            return new ViewHolder(view);
        }



//======================================================================================
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.t01_name.setText(arrayList.get(position).get("name"));
//            .placeholder(r.xxxxxx)圖片讀取完成前的佔位圖
//            .error(r.xxxxxx)圖片讀取失敗的佔位圖

            Picasso.get().load(arrayList.get(position).get("thuburl")).fit().error(R.drawable.d1).into(holder.img01_pic);

//            點擊事件
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String uid = arrayList.get(position).get("uid");
                    Intent intent=new Intent();
                    intent.setClass(Ast_detail.this, Ast_Point.class)
                            .putExtra("uid", uid);
                    //傳遞內容
                    startActivity(intent);

                }
            });
        }


        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }

//   操作上下滑以及左右滑
    private void recyclerViewAction(RecyclerView recyclerView, final ArrayList<HashMap<String,String>> choose, final DetailListAdapter DetailListAdapter){
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN , ItemTouchHelper.LEFT );
                //這裡是告訴RecyclerView你想開啟哪些操作
//                上下左右都操作的狀況就在return的部分打上
//                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
//                如果不要上下只要左右則是
//                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
//                反之則是
//                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN ,0);
//                如果只要左不要右就是
//                return makeMovementFlags(0,temTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                        上下滑更新array
//                        更新資料庫寫在此
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(choose, position_dragged, position_target);
//               前兩行是定義一起始位置以及最終變換位置
//                第三行是利用java內的集合函式處理陣列位置修改
                String update_uid="";
                for(int i=0;i<choose.size();i++){
                    update_uid+=choose.get(i).get("uid")+",";
                }

                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("sch_id",id));
                nameValuePairs.add(new BasicNameValuePair("day", choose.get(0).get("day")));
                nameValuePairs.add(new BasicNameValuePair("att_id",update_uid ));

                try{
                    DetailDBConnector.executeattrUpdate("Update attr_id From detail ", nameValuePairs);//更新移除

                }catch(Exception e){

                }

                update_uid="";
                myAdapter.notifyItemMoved(position_dragged, position_target);
//                通知Adapter元素有成功變換位置

                return false;//管理上下拖曳
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //管理滑動情形
                int position = viewHolder.getAdapterPosition();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
//                        左右滑刪除array
//                        ****************************************************撤銷未完成*****************************************
//                        刪除資料庫寫在此

                    //去sqlite把這筆資料抓出來修改

                        ArrayList<String> recAry = new ArrayList<String>();
                        mContRes = getContentResolver();
                        Cursor c = mContRes.query(DetailContentProvider.CONTENT_URI, MYCOLUMN, "day="+arrayList.get(position).get("day")+" and sch_id= "+arrayList.get(position).get("sch_id"), null, null);
                        tcount = c.getCount();
                        int columnCount = c.getColumnCount();
                        while (c.moveToNext()) {
                            String fldSet = "";
                            for (int ii = 0; ii < columnCount; ii++) {
                                fldSet += c.getString(ii) + "#";
                            }
                            recAry.add(fldSet);
                        }
                        ArrayList<String> a = recAry;
                        //抓取第0筆(只有一筆)
                        String[] fld = recAry.get(0).split("#");

                        //替換掉uid,為""
                        fld[3]=fld[3].replace(arrayList.get(position).get("uid")+",","" );

                        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("id", fld[0]));
                        nameValuePairs.add(new BasicNameValuePair("sch_id",fld[1] ));
                        nameValuePairs.add(new BasicNameValuePair("day", fld[2]));
                        nameValuePairs.add(new BasicNameValuePair("att_id",fld[3] ));
                        try{
                            DetailDBConnector.executeUpdate("Update attr_id From detail ", nameValuePairs);//更新移除

                        }catch(Exception e){

                        }
                            choose.remove(position);
                            myAdapter.notifyItemRemoved(position);
                        if(arrayList.isEmpty()){
                                        arrayList.clear();
                                          myAdapter.notifyDataSetChanged();
                        }

                        break;
                }

            }
//            刪除加上圖標
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(Ast_detail.this,android.R.color.holo_red_dark))//底色
                        .addActionIcon(R.drawable.ic_baseline_delete_sweep_24)//垃圾桶圖
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
//        把滑動設定丟回recyclerview
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //寫一個序
        //讀取1-5天獲得陣列
//       先清空再讀之後再想放哪
//        Uri uri = DetailattrContentProvider.CONTENT_URI;
//        mContRes.delete(uri, "  sch_id= "+String.valueOf(id), null);  //清空當下sch_id當天SQLite
        trip_plan_selectday=(TextView) findViewById(R.id.trip_plan_selectday);

        linear=(LinearLayout)findViewById(R.id.progressbarlinear);
        mysql.postDelayed(updatemysql, 1000);

//================================================================
    }
    private Runnable updatemysql=new Runnable() {
        @Override
        public void run() {

//有傳入第幾天的值則跑哪天沒有則跑1
            if(whichday!=null){
                linear.setVisibility(View.VISIBLE);


//        設定在按鈕按下去再搜尋該天
//       一進來預設讀第一天
                arrayList.clear();
                detaildbmysql(Integer.valueOf(whichday));
                selectday=Integer.valueOf(whichday);
                trip_plan_selectday.setText("第"+whichday+"天");
                linear.setVisibility(View.GONE);


            }else{
                linear.setVisibility(View.VISIBLE);


                arrayList.clear();
                detaildbmysql(1);
                selectday=1;
                trip_plan_selectday.setText("第"+1+"天");
                linear.setVisibility(View.GONE);

            }





        }
    } ;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mysql.removeCallbacks(updatemysql);
    }
}

//recyclerview參考資料https://thumbb13555.pixnet.net/blog/post/316420566-recyclerview-swipe