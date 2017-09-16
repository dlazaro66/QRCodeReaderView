package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * Created by Administrator on 2017/9/8.
 */

public class DataBaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "SensorDate1.db";
    public static final String TABLE_NAME = "sensordate1";

    public static final String COL_2 = "TIME";
    public static final String COL_1 = "VALUE";
    public static final String COL_3 = "DEVICE";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME,null,1);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //创建表的SQL语句，日期用text格式存储
        sqLiteDatabase.execSQL("create table "+TABLE_NAME+" ( ID INTEGER PRIMARY KEY AUTOINCREMENT,VALUE TEXT , TIME TEXT , DEVICE TEXT)");
    }




    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //更新数据表
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public boolean insertData(String time,String value,String device){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,time);
        contentValues.put(COL_1,value);
        contentValues.put(COL_3,device);
        long result = db.insert(TABLE_NAME,null,contentValues);
        if (result==-1)
            return false;
        else
            return true;

    }
    //获取表中所有数据信息
    public Cursor getAllData(){
        //
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    public Cursor getValue(String device,int numdate){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res2 = null;
        //读取前20个的value数据
        //res2 = db.rawQuery("SELECT VALUE FROM "+TABLE_NAME+"    ORDER BY ID DESC  LIMIT "+numdate ,null);
        //res2 = db.rawQuery(" SELECT * FROM "+TABLE_NAME +" where DEVICE  =  '"+device+"'  ",null);
        //res2 = db.rawQuery(" SELECT VALUE FROM ( SELECT * FROM  "+TABLE_NAME + " where DEVICE = ' "+device+" ' ) ORDER BY ID DESC LIMIT "+numdate,null);
        res2 = db.rawQuery("SELECT VALUE FROM  ( SELECT * FROM "+TABLE_NAME +" where DEVICE  =  '"+device+"' ) ORDER BY ID DESC  LIMIT "+numdate,null);
        //res2 = db.rawQuery("SELECT VALUE FROM (SELECT * FROM "+TABLE_NAME +"  ) ORDER BY ID DESC  LIMIT "+numdate,null);

        return res2;
    }

    public Cursor getTime(String device,int numdate){
        SQLiteDatabase db = this.getWritableDatabase();
        //device的名称单引号之间不要有空格
        //Cursor res = db.rawQuery("SELECT TIME FROM "+TABLE_NAME +"  ORDER BY ID DESC LIMIT "+numdate,null);
        //Cursor res = db.rawQuery(" SELECT * FROM "+TABLE_NAME +" where DEVICE = '"+ device+"'  ",null);
        //Cursor res = db.rawQuery(" SELECT TIME FROM ( SELECT * FROM "+TABLE_NAME +" where DEVICE = ' "+device+" ' ) ORDER BY ID DESC LIMIT "+numdate ,null);
        Cursor res = db.rawQuery("SELECT TIME FROM ( SELECT * FROM "+TABLE_NAME +" where DEVICE = '"+ device+"' ) ORDER BY ID DESC LIMIT "+numdate,null);
        //Cursor res = db.rawQuery("SELECT TIME FROM ( SELECT * FROM "+TABLE_NAME +"  ) ORDER BY ID DESC LIMIT "+numdate,null);
        return res;
    }

    public Cursor getAllTime(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT TIME FROM "+TABLE_NAME,null);
        return res;
    }

    //倒序排列
    public Cursor DESC(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT TIME FROM "+TABLE_NAME+" ORDER BY ID DESC LIMIT 20",null);

        return res;
    }

}
