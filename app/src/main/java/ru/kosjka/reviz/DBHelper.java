package ru.kosjka.reviz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Артём on 10.08.2016.
 */
//public class DBHelper extends SQLiteOpenHelper {
//    private static final String NAME = "reviz.db";
//    private static final int version = 1;
//    public DBHelper(Context context) {
//        super(context, NAME, null, version);
//    }
//    private final String MYLOG = "MainActivity";
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        Log.d(MYLOG, "onCreate start");
//        //db.execSQL("drop table if exits goods");
//        //db.execSQL("drop table if exits barcodes");
//        db.execSQL("create table goods ( ngoods integer primary key autoincrement, fullname text, qty integer default 0)");
//        db.execSQL("create table barcodes (barcode text primary key, ngoods integer)");
//
////        db.execSQL("insert into goods (ngoods,fullname) values(10000,'Папка темно зеленая');");
////        db.execSQL("insert into goods (ngoods,fullname) values(10001,'Bantex папка зеленая');");
////        db.execSQL("insert into goods (ngoods,fullname) values(10002,'ErichKrause синий');");
////        db.execSQL("insert into goods (ngoods,fullname) values(10003,'EXPERT complete серая');");
//        db.execSQL("insert into goods (ngoods,fullname) values(10004,'EXPERT complete красная');");
//
////        db.execSQL("insert into barcodes (barcode,ngoods) values('46000025165',10000);");
////        db.execSQL("insert into barcodes (barcode,ngoods) values('5702231446156',10001);");
////        db.execSQL("insert into barcodes (barcode,ngoods) values('4601921006964',10002);");
////        db.execSQL("insert into barcodes (barcode,ngoods) values('4603515000518',10003);");
//        db.execSQL("insert into barcodes (barcode,ngoods) values('4600000251615',10004);");
//        Log.d(MYLOG, "onCreate stop");
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("drop table if exists goods");
//        db.execSQL("drop table if exists barcodes");
//        onCreate(db);
//    }
//}
public class DBHelper extends SQLiteOpenHelper {


    public static final String DBNAME = "reviz.db";
    //public static final String DBLOCATION = "/data/data/ru.kosjka.reviz/databases/";
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public DBHelper(Context context) {
        super(context, DBNAME, null, 1);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //db.execSQL("drop table if exits goods");
        //db.execSQL("drop table if exits barcodes");
        db.execSQL("create table goods ( ngoods integer primary key autoincrement, fullname text, qty integer default 0)");
        db.execSQL("create table barcodes (barcode text primary key, ngoods integer)");
//        db.execSQL("insert into goods (ngoods,fullname) values(10000,'Папка темно зеленая');");
//        db.execSQL("insert into goods (ngoods,fullname) values(10001,'Bantex папка зеленая');");
//        db.execSQL("insert into goods (ngoods,fullname) values(10002,'ErichKrause синий');");
//        db.execSQL("insert into goods (ngoods,fullname) values(10003,'EXPERT complete серая');");
//        db.execSQL("insert into goods (ngoods,fullname) values(10004,'EXPERT complete красная');");
//
//        db.execSQL("insert into barcodes (barcode,ngoods) values('46000025165',10000);");
//        db.execSQL("insert into barcodes (barcode,ngoods) values('5702231446156',10001);");
//        db.execSQL("insert into barcodes (barcode,ngoods) values('4601921006964',10002);");
//        db.execSQL("insert into barcodes (barcode,ngoods) values('4603515000518',10003);");
//        db.execSQL("insert into barcodes (barcode,ngoods) values('4600000251615',10004);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists goods");
        db.execSQL("drop table if exists barcodes");
        onCreate(db);
    }
    public void cleardb(SQLiteDatabase db) {
        db.execSQL("drop table if exists goods");
        db.execSQL("drop table if exists barcodes");
        onCreate(db);
    }

    public void openDatabase() {
        String dbPath = mContext.getDatabasePath(DBNAME).getPath();
        if (mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

}