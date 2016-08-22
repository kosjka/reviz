package ru.kosjka.reviz;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnScan, btnOk, btnOpen, btnSend;
    public TextView tvBarcode, tvFullName, tvQty;
    private DBHelper dbHelper;
    private EditText etQty;
    private String ngoods;
    private final String MYLOG = "MainActivity";
    final Activity activity = this;
    private final int REQUEST_SCAN = 1;
    private final int REQUEST_DBSELECT = 2;
    private final int REQUEST_SEND = 3;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        btnScan = (Button) this.findViewById(R.id.btnScan);
        btnOk = (Button) this.findViewById(R.id.btnOk);
        btnOpen = (Button) this.findViewById(R.id.btnOpen);
        btnSend = (Button) this.findViewById(R.id.btnSend);

        btnOk.setOnClickListener(this);
        btnOpen.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        tvBarcode = (TextView) this.findViewById(R.id.tvBarcode);
        tvFullName = (TextView) this.findViewById(R.id.tvFullName);
        tvQty = (TextView) this.findViewById(R.id.tvQty);

        etQty = (EditText) this.findViewById(R.id.etQty);
        //Log.d(MYLOG,"-----"+tvBarcode.getText().toString() );

        dbHelper = new DBHelper(this);

        //Check exists database
        File database = getApplicationContext().getDatabasePath(DBHelper.DBNAME);
        if (!database.exists()) {
            dbHelper.getReadableDatabase();
            //Copy db
            if (copyDatabase(this)) {
                Toast.makeText(this, "Copy database succes", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Copy data error", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private boolean copyDatabase(Context context) {
        try {

            InputStream inputStream = context.getAssets().open(DBHelper.DBNAME);
            String outFileName = DBHelper.DBLOCATION + DBHelper.DBNAME;
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            Log.w("MainActivity", "DB copied");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(MYLOG, "Cancelled scan");

                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d(MYLOG, "Scanned");
                tvBarcode.setText(result.getContents());
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                if (tvBarcode.getText().length() != 0) findGoods();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
//                                case REQUEST_SCAN:
//                                    break;
                    case REQUEST_DBSELECT:
                        try {
                            //File file = new File("/sdcard/goods");
                            //InputStream inputStream = new FileInputStream(file);
                            ContentResolver cr = getContentResolver();
                            InputStream inputStream = cr.openInputStream(data.getData());
                            String outFileName = DBHelper.DBLOCATION + DBHelper.DBNAME;
                            OutputStream outputStream = new FileOutputStream(outFileName);
                            byte[] buff = new byte[1024];
                            int length = 0;
                            while ((length = inputStream.read(buff)) > 0) {
                                outputStream.write(buff, 0, length);
                            }
                            outputStream.flush();
                            outputStream.close();
                            Log.w("MainActivity", "DB copied");
                            Toast.makeText(this, "Ура", Toast.LENGTH_LONG).show();//return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            ///return false;
                        }

                        break;

                }
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tvBarcode", tvBarcode.getText().toString());
        outState.putString("tvFullName", tvFullName.getText().toString());
        outState.putString("tvQty", tvQty.getText().toString());
        outState.putString("ngoods", ngoods);
        Log.d(MYLOG, "saved");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvBarcode.setText(savedInstanceState.getString("tvBarcode"));
        tvFullName.setText(savedInstanceState.getString("tvFullName"));
        tvQty.setText(savedInstanceState.getString("tvQty"));
        this.ngoods = savedInstanceState.getString("ngoods");
        Log.d(MYLOG, "restore");

    }

    @SuppressLint("SdCardPath")
    @Override
    public void onClick(View view) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        switch (view.getId()) {
            case R.id.btnOk:
                if (etQty.getText().toString().trim().length() != 0) {
                    Log.d(MYLOG, "--" + etQty.getText().toString() + "--");
                    contentValues.put("qty", Integer.parseInt(etQty.getText().toString()));
                    database.update("goods", contentValues, "ngoods = ?", new String[]{ngoods});
                    findGoods();
                    etQty.setText("");
                }
                break;
            case R.id.btnOpen:
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("application/octet-stream");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent, REQUEST_DBSELECT);
                dbHelper.getReadableDatabase();
                //Copy db
                if (copyDatabase(this)) {
                    Toast.makeText(this, "Copy database succes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Copy data error", Toast.LENGTH_SHORT).show();
                }
                RandomAccessFile in = null;
                database.beginTransaction();
                try {
                    in = new RandomAccessFile("/sdcard/goods", "r");
                    for (int i=0;i<in.length()/140;i++) {
                            byte[] byteNgoods = new byte[4];
                            in.read(byteNgoods, 0, 4);
                            int ngoods = java.nio.ByteBuffer.wrap(byteNgoods).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                            byte xbyte = in.readByte();
                            byte[] tempId = new byte[xbyte];
                            byte[] temp = new byte[131];
                            in.read(tempId, 0, xbyte);
                            in.read(temp, 0, 131-xbyte);
                            String fullgoods = new String(tempId);
                            byte[] bytePrice = new byte[4];
                            in.read(bytePrice, 0, 4);
                            //int price = java.nio.ByteBuffer.wrap(bytePrice).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                          contentValues.put("ngoods",ngoods);
                          contentValues.put("fullname",fullgoods);
                          contentValues.put("qty",0);
                          //contentValues.put("");
                           database.insert("goods",null,contentValues);
                    }
                    in.close();
                    contentValues.clear();
                    in = new RandomAccessFile("/sdcard/codes", "r");
                    for (int i=0;i<in.length()/24;i++) {
                        byte[] byteNgoods = new byte[4];
                        in.read(byteNgoods, 0, 4);
                        int ngoods = java.nio.ByteBuffer.wrap(byteNgoods).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                        byte xbyte = in.readByte();
                        byte[] tempId = new byte[xbyte];
                        byte[] temp = new byte[15];
                        in.read(tempId, 0, xbyte);
                        in.read(temp, 0, 15-xbyte);
                        String barcode = new String(tempId);
                        byte[] byteQty = new byte[4];
                        in.read(byteQty, 0, 4);
                        int qty = java.nio.ByteBuffer.wrap(byteQty).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                       // System.out.printf("Код: %d, Длина строки: %d, Штрих-код: %s, количество шт в упаковке: %d шт.\n", ngoods, xbyte, barcode, qty);
                        contentValues.put("ngoods",ngoods);
                        contentValues.put("barcode",barcode);
                        database.insert("barcodes",null,contentValues);
                    }
                    database.setTransactionSuccessful();
                    contentValues.clear();
                    in.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    database.endTransaction();
                    Toast.makeText(this, "База прогружена", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnSend:
//                if (copy(DBHelper.DBLOCATION + DBHelper.DBNAME, "/sdcard/send.db")) {
//                    File filelocation = new File("/sdcard/send.db");
//                    Uri path = Uri.fromFile(filelocation);
//                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
//                    emailIntent.setType("*/*");
//                    String to[] = {"kosjka@ro.ru"};
//                    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
//                    emailIntent.putExtra(Intent.EXTRA_STREAM, path);
//                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
//                }
                Cursor cursor = database.rawQuery("select g.ngoods,  g.qty from goods g where g.qty <> ? ",
                        new String[]{"0"});
                if (cursor != null) {
                    cursor.moveToFirst();
                   RandomAccessFile out = null;
                    try {
                        //OutputStream in = new FileOutputStream("/sdcard/reviz");
                        out = new RandomAccessFile("/sdcard/reviz", "rw");
                        do{
                            byte[] bngoods = ByteBuffer.allocate(4).putInt(cursor.getInt(cursor.getColumnIndex("ngoods"))).array();
                            out.writeInt( ByteBuffer.wrap(bngoods).order(ByteOrder.LITTLE_ENDIAN).getInt());
                            out.writeInt(0);
                            byte[] btempqty = ByteBuffer.allocate(8).putDouble(cursor.getInt(cursor.getColumnIndex("qty"))).array();
                            byte[] bqty = new byte [8];
                            for(int j= 0;j<8;j++){
                                bqty[j]=btempqty[7-j];
                            }
                            out.write(bqty);
                        } while (cursor.moveToNext());
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    cursor.close();
                    File filelocation = new File("/sdcard/reviz");
                    Uri path = Uri.fromFile(filelocation);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("*/*");
                    String to[] = {"kosjka@ro.ru"};
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }else {cursor.close();}


                break;
        }
    }

    void logCursor(Cursor cursor) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : cursor.getColumnNames()) {
                        str = str.concat(cn + " = " + cursor.getString(cursor.getColumnIndex(cn)) + "; ");
                    }
                    Log.d(MYLOG, str);
                } while (cursor.moveToNext());
            }
        } else Log.d(MYLOG, "Cursor is null");
    }

    void findGoods() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        //ContentValues contentValues = new ContentValues();
        Log.d(MYLOG, "start find ngoods");
        String str = null;
        Log.d(MYLOG, String.valueOf(tvBarcode.getText().toString().length()));
        //if (tvBarcode.getText().toString().length()==13){

            str = tvBarcode.getText().toString().substring(0,2);
        //}
        Cursor cursor;
        int intngoods;
        String str3;
        switch (str){
        //if (str=="98" || str=="00") {
            case "98":
                Log.d(MYLOG, str+" str");
                str3 = tvBarcode.getText().toString().substring(5, 12);
                Log.d(MYLOG, str3+" str3");
                intngoods = Integer.valueOf(str3);
                Log.d(MYLOG, String.valueOf(ngoods)+" ngoods");
                cursor = database.rawQuery("select g.ngoods, g.fullname, g.qty " +
                                " from goods g where g.ngoods = ?",
                    new String[]{String.valueOf(intngoods)});
                break;
            case "00":
                Log.d(MYLOG, str+" str");
                str3 = tvBarcode.getText().toString().substring(5, 11);
                Log.d(MYLOG, str3+" str3");
                intngoods = Integer.valueOf(str3);
                Log.d(MYLOG, String.valueOf(intngoods)+" ngoods");
                cursor = database.rawQuery("select g.ngoods, g.fullname, g.qty " +
                                " from goods g where g.ngoods = ?",
                        new String[]{String.valueOf(intngoods)});
                break;
            default:
                cursor = database.rawQuery("select g.ngoods, g.fullname, g.qty from barcodes b " +
                                "inner join goods g on b.ngoods = g.ngoods where b.barcode = ?",
                        new String[]{tvBarcode.getText().toString()});
                break;

        }
            Log.d(MYLOG, "finish find ngoods");
        if (cursor != null && cursor.getCount() == 1) {
            cursor.moveToFirst();
            Log.d(MYLOG, "cursor out start");
            tvFullName.setText(cursor.getString(cursor.getColumnIndex("fullname")));
            Log.d(MYLOG, "cursor out fullname finish");
            tvQty.setText(cursor.getString(cursor.getColumnIndex("qty")).toString());
            Log.d(MYLOG, "1");
            ngoods = cursor.getString(cursor.getColumnIndex("ngoods"));
            Log.d(MYLOG, "2");

        } else {
            tvFullName.setText("Товар не найден!");
            tvQty.setText("0");
        }
        cursor.close();
    }

    void updateQty() {

    }

    public static boolean copy(String from, String to) {
        try {
            File file = new File(from);
            InputStream inputStream = new FileInputStream(file);
//            ContentResolver cr = getContentResolver();
//            InputStream inputStream = cr.openInputStream(data.getData());
            String outFileName = to;
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            Log.w("MainActivity", "DB copied");
            //Toast.makeText(this, "Ура", Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://ru.kosjka.reviz/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://ru.kosjka.reviz/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}