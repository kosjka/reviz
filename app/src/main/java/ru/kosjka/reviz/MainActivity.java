package ru.kosjka.reviz;

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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;


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
                            //File file = new File("/sdcard/Download/reviz.db");
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
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/octet-stream");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_DBSELECT);
                break;
            case R.id.btnSend:
                if (copy(DBHelper.DBLOCATION + DBHelper.DBNAME, "/sdcard/send.db")) {
                    File filelocation = new File("/sdcard/send.db");
                    Uri path = Uri.fromFile(filelocation);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
// set the type to 'email'
                    emailIntent.setType("*/*");
                    String to[] = {"kosjka@ro.ru"};
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
                    emailIntent.putExtra(Intent.EXTRA_STREAM, path);
// the mail subject
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
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
        ContentValues contentValues = new ContentValues();
        Log.d(MYLOG, "start find ngoods");
        Cursor cursor = database.rawQuery("select g.ngoods, g.fullname, g.qty from barcodes b " +
                        "inner join goods g on b.ngoods = g.ngoods where b.barcode = ?",
                new String[]{tvBarcode.getText().toString()});
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


}