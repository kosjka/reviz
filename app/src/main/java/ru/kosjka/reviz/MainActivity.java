package ru.kosjka.reviz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnScan, btnOk, btnOpen, btnSend, btnTest;
    public TextView tvBarcode, tvFullName, tvQty;
    private DBHelper dbHelper;
    private EditText etQty;
    private String ngoods;
    private String sdcard;
    private boolean work;
    public final String MYLOG = "MainActivity";

    final Activity activity = this;
    //private final int REQUEST_SCAN = 1;
    private final int REQUEST_DBSELECT = 2;
    //private final int REQUEST_SEND = 3;
    final int DIAlOG_ACCEPT = 1;
    final int DIAlOG_INTERNET = 2;
    //final int BUFFER_SIZE = 1024;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public static long getTime() throws Exception {
        Request request = new Request.Builder()
                .url("http://www.timeapi.org/utc/now")
                .header("Accept", "*/*")
                .header("User-agent", "Particle HttpClient")
                .build();
        Response responses = new OkHttpClient().newCall(request).execute();
        String stringValue = responses.body().string();
        //responses.toString(); // returned String value
        Log.d("MainActivity", "-" + responses.toString() + "-");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date mDate = sdf.parse(stringValue);

        return mDate.getTime() / 1000;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            showDialog(DIAlOG_INTERNET);
            Log.d(MYLOG, "запустился хендлер");

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sdcard = Environment.getExternalStorageDirectory().getPath()+"/";
        dbHelper = new DBHelper(this);
        btnScan = (Button) this.findViewById(R.id.btnScan);
        btnOk = (Button) this.findViewById(R.id.btnOk);
        btnOpen = (Button) this.findViewById(R.id.btnOpen);
        btnSend = (Button) this.findViewById(R.id.btnSend);
        btnTest = (Button) this.findViewById(R.id.btnTest);

        btnOk.setOnClickListener(this);
        btnOpen.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnTest.setOnClickListener(this);

        tvBarcode = (TextView) this.findViewById(R.id.tvBarcode);
        tvFullName = (TextView) this.findViewById(R.id.tvFullName);
        tvQty = (TextView) this.findViewById(R.id.tvQty);

        etQty = (EditText) this.findViewById(R.id.etQty);

        Log.d(MYLOG,Environment.getExternalStorageDirectory().getPath());
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
        if (savedInstanceState != null) work = savedInstanceState.getBoolean("work");
        Log.d(MYLOG, "перед запуском иф " + String.valueOf(work));
        if (!work) {
            Runnable runnable = new Runnable() {
                long time;

                public void run() {

                    try {
                        time = getTime();
                        long timeend = 1475251138;
                        Log.d(MYLOG, String.valueOf(time));
                        if (time > timeend) {
                            android.os.Process.killProcess(android.os.Process.myPid());

                        } else {
                            work = true;
                            //Log.d(MYLOG, "записали в ворк" + String.valueOf(work));
                        }

                    } catch (Exception e) {
                        if (time == 0) {
                            Log.d(MYLOG, "нет соединения");
                            handler.sendEmptyMessage(0);
                        }
                        e.printStackTrace();


                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
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
                    case REQUEST_DBSELECT:
                        SQLiteDatabase database = dbHelper.getWritableDatabase();
                        ContentValues contentValues = new ContentValues();
                        dbHelper.getReadableDatabase();
                        dbHelper.cleardb(database);
                        try {
                            unzip(data.getData(), Environment.getExternalStorageDirectory().getPath()+"/");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d(MYLOG, "start insert");
                        RandomAccessFile in = null;
                        database.beginTransaction();
                        try {
                            in = new RandomAccessFile(sdcard+"goods", "r");
                            for (int i = 0; i < in.length() / 140; i++) {
                                byte[] byteNgoods = new byte[4];
                                in.read(byteNgoods, 0, 4);
                                int ngoods = java.nio.ByteBuffer.wrap(byteNgoods).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                                byte xbyte = in.readByte();
                                byte[] tempId = new byte[xbyte];
                                byte[] temp = new byte[131];
                                in.read(tempId, 0, xbyte);
                                in.read(temp, 0, 131 - xbyte);
                                String fullgoods = new String(tempId);
                                byte[] bytePrice = new byte[4];
                                in.read(bytePrice, 0, 4);
                                //int price = java.nio.ByteBuffer.wrap(bytePrice).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                                contentValues.put("ngoods", ngoods);
                                contentValues.put("fullname", fullgoods);
                                contentValues.put("qty", 0);
                                //contentValues.put("");
                                database.insert("goods", null, contentValues);
                            }
                            in.close();
                            contentValues.clear();
                            in = new RandomAccessFile(sdcard+"codes", "r");
                            for (int i = 0; i < in.length() / 24; i++) {
                                byte[] byteNgoods = new byte[4];
                                in.read(byteNgoods, 0, 4);
                                int ngoods = java.nio.ByteBuffer.wrap(byteNgoods).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                                byte xbyte = in.readByte();
                                byte[] tempId = new byte[xbyte];
                                byte[] temp = new byte[15];
                                in.read(tempId, 0, xbyte);
                                in.read(temp, 0, 15 - xbyte);
                                String barcode = new String(tempId);
                                byte[] byteQty = new byte[4];
                                in.read(byteQty, 0, 4);
                                int qty = java.nio.ByteBuffer.wrap(byteQty).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                                contentValues.put("ngoods", ngoods);
                                contentValues.put("barcode", barcode);
                                database.insert("barcodes", null, contentValues);
                            }
                            database.setTransactionSuccessful();
                            contentValues.clear();
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            database.endTransaction();
                            Toast.makeText(this, R.string.dbLoaded, Toast.LENGTH_SHORT).show();
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
        outState.putBoolean("work", work);
        Log.d(MYLOG, "saved " + String.valueOf(work));
        Log.d(MYLOG, "saved");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvBarcode.setText(savedInstanceState.getString("tvBarcode"));
        tvFullName.setText(savedInstanceState.getString("tvFullName"));
        tvQty.setText(savedInstanceState.getString("tvQty"));
        ngoods = savedInstanceState.getString("ngoods");
        work = savedInstanceState.getBoolean("work");
        Log.d(MYLOG, "restore " + String.valueOf(work));
        Log.d(MYLOG, "restore");

    }

    @Override
    public void onClick(View view) {
        //Log.d(MYLOG, "----------------------------------1");
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Log.d("MainActivity", "----------------------------------2");
        switch (view.getId()) {
            case R.id.btnOk:
                if (etQty.getText().toString().trim().length() != 0) {
                    if (ngoods == null) {
                        Toast.makeText(this, R.string.notSelectGood, Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(MYLOG, "--" + etQty.getText().toString() + "--");
                        contentValues.put("qty", Integer.parseInt(etQty.getText().toString()));
                        database.update("goods", contentValues, "ngoods = ?", new String[]{ngoods});
                        findGoods();
                        etQty.setText("");
                    }
                } else {
                    Toast.makeText(this, R.string.notSetedQty, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btnOpen:
                showDialog(DIAlOG_ACCEPT);
                Toast.makeText(this, "Ура", Toast.LENGTH_LONG).show();
                break;
            case R.id.btnTest:
                tvBarcode.setText(etQty.getText().toString());
                findGoods();
                break;
            case R.id.btnSend:
                Cursor cursor = database.rawQuery("select g.ngoods,  g.qty from goods g where g.qty <> ? ",
                        new String[]{"0"});
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToFirst();
                    RandomAccessFile out = null;
                    try {
                        File file = new File(sdcard + "reviz");
                        file.delete();
                        out = new RandomAccessFile(sdcard + "reviz", "rw");
                        do {
                            byte[] bngoods = ByteBuffer.allocate(4).putInt(cursor.getInt(cursor.getColumnIndex("ngoods"))).array();
                            out.writeInt(ByteBuffer.wrap(bngoods).order(ByteOrder.LITTLE_ENDIAN).getInt());
                            out.writeInt(0);
                            byte[] btempqty = ByteBuffer.allocate(8).putDouble(cursor.getInt(cursor.getColumnIndex("qty"))).array();
                            byte[] bqty = new byte[8];
                            for (int j = 0; j < 8; j++) {
                                bqty[j] = btempqty[7 - j];
                            }
                            out.write(bqty);
                        } while (cursor.moveToNext());
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    cursor.close();
                    try {

                        zip(new String[]{sdcard + "reviz"}, sdcard + "reviz.zip");
                        File file = new File(sdcard + "reviz");
                        boolean delete = file.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    File filelocation = new File(sdcard + "reviz.zip");
                    Uri path = Uri.fromFile(filelocation);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("*/*");
                    String to[] = {getString(R.string.email)};
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }

                if (cursor != null) {
                    cursor.close();
                }


                break;
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder adb;
        switch (id) {
            case DIAlOG_ACCEPT:
                adb = new AlertDialog.Builder(this);
                // заголовок
                adb.setTitle(R.string.title);
                // сообщение
                adb.setMessage(R.string.dialogacceptmessage);
                // иконка
                adb.setIcon(android.R.drawable.ic_dialog_info);
                // кнопка положительного ответа
                adb.setPositiveButton(R.string.yes, dialogClickListener);
                // кнопка отрицательного ответа
                //adb.setNegativeButton(R.string.no, dialogClickListener);
                // кнопка нейтрального ответа
                adb.setNeutralButton(R.string.cancel, dialogClickListener);
                // создаем диалог
                dialog = adb.create();
                break;
            case DIAlOG_INTERNET:
                adb = new AlertDialog.Builder(this);
                adb.setTitle(R.string.title);
                // сообщение
                adb.setMessage(R.string.dialoginternetmessage);
                // иконка
                adb.setIcon(android.R.drawable.ic_dialog_info);
                adb.setPositiveButton(R.string.yes, dialogInternetClickListener);
                adb.setCancelable(false);
                dialog = adb.create();
                break;
            default:
                dialog = null;
                break;

        }
        return dialog;
    }

    DialogInterface.OnClickListener dialogInternetClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Log.d(MYLOG, "Dialog onClick");
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    android.os.Process.killProcess(android.os.Process.myPid());

                    break;

            }
        }
    };
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Log.d(MYLOG, "Dialog onClick");
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/zip");//
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, REQUEST_DBSELECT);

                    break;
                // негативная кнопка
                case Dialog.BUTTON_NEGATIVE:
                    //finish();
                    break;
                // нейтральная кнопка
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };

    void logCursor(Cursor cursor) {
        if (cursor != null && cursor.getCount() != 0) {
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

    void printGoods(Cursor cursor) {
        cursor.moveToFirst();
        Log.d(MYLOG, "cursor out start");
        tvFullName.setText(cursor.getString(cursor.getColumnIndex("fullname")));
        Log.d(MYLOG, "cursor out fullname finish");
        tvQty.setText(cursor.getString(cursor.getColumnIndex("qty")));
        Log.d(MYLOG, "1");
        ngoods = cursor.getString(cursor.getColumnIndex("ngoods"));
        Log.d(MYLOG, "2");
    }

    void findGoods() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Log.d(MYLOG, "start find ngoods");
        String str = null;
        int intngoods;
        String str3;
        Log.d(MYLOG, String.valueOf(tvBarcode.getText().toString().length()));
        Cursor cursor;
        cursor = database.rawQuery("select g.ngoods, g.fullname, g.qty from barcodes b " +
                        "inner join goods g on b.ngoods = g.ngoods where b.barcode = ?",
                new String[]{tvBarcode.getText().toString()});

        if (cursor != null && cursor.getCount() != 0) {
            printGoods(cursor);
        } else {
            str = tvBarcode.getText().toString().substring(0, 2);
            switch (str) {
                case "98":
                    Log.d(MYLOG, str + " str");
                    str3 = tvBarcode.getText().toString().substring(5, 12);
                    Log.d(MYLOG, str3 + " str3");
                    intngoods = Integer.valueOf(str3);
                    Log.d(MYLOG, String.valueOf(ngoods) + " ngoods");
                    cursor = database.rawQuery("select g.ngoods, g.fullname, g.qty " +
                                    " from goods g where g.ngoods = ?",
                            new String[]{String.valueOf(intngoods)});
                    break;
                case "00":
                    Log.d(MYLOG, str + " str");
                    str3 = tvBarcode.getText().toString().substring(5, 11);
                    Log.d(MYLOG, str3 + " str3");
                    intngoods = Integer.valueOf(str3);
                    Log.d(MYLOG, String.valueOf(intngoods) + " ngoods");
                    cursor = database.rawQuery("select g.ngoods, g.fullname, g.qty " +
                                    " from goods g where g.ngoods = ?",
                            new String[]{String.valueOf(intngoods)});
                    break;


            }
            if (cursor != null && cursor.getCount() != 0) {
                printGoods(cursor);
            } else {
                tvFullName.setText("Товар не найден!");
                tvQty.setText("0");
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    void updateQty() {

    }

    public static boolean copy(String from, String to) {
        try {
            File file = new File(from);
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = new FileOutputStream(to);
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

    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
            byte data[] = new byte[1024];

            for (String file : files) {
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, 1024);
                try {
                    ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, 1024)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        }
    }

    public void unzip(Uri zipFile, String location) throws IOException {
        String TAG = "MainActivity";
        try {
            ContentResolver cr = getContentResolver();
            InputStream inputStream = cr.openInputStream(zipFile);
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            try (ZipInputStream zin = new ZipInputStream(inputStream)) {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        try (FileOutputStream fout = new FileOutputStream(path, false)) {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }
                            zin.closeEntry();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Unzip exception", e);
        }
    }

}