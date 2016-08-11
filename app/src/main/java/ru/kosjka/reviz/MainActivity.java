package ru.kosjka.reviz;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnScan,btnOk,btnTest;
    public TextView tvBarcode, tvFullName, tvQty;
    private DBHelper dbHelper;
    private EditText etQty;
    private String ngoods;
    private final String MYLOG = "MainActivity";
    final Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        btnScan = (Button) this.findViewById(R.id.btnScan);
        btnOk = (Button) this.findViewById(R.id.btnOk);
        btnTest = (Button) this.findViewById(R.id.btnTest);
        btnOk.setOnClickListener( this);
        btnTest.setOnClickListener( this);


        tvBarcode =(TextView) this.findViewById(R.id.tvBarcode);
        tvFullName =(TextView) this.findViewById(R.id.tvFullName);
        tvQty =(TextView) this.findViewById(R.id.tvQty);

        etQty = (EditText) this.findViewById(R.id.etQty);
        //Log.d(MYLOG,"-----"+tvBarcode.getText().toString() );



        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d(MYLOG, "Cancelled scan");

                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d(MYLOG, "Scanned");
                tvBarcode.setText(result.getContents());
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                if(tvBarcode.getText().toString() != "") findGoods();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tvBarcode",tvBarcode.getText().toString());
        outState.putString("tvFullName",tvFullName.getText().toString());
        outState.putString("tvQty",tvQty.getText().toString());
        Log.d(MYLOG, "saved");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvBarcode.setText(savedInstanceState.getString("tvBarcode"));
        tvFullName.setText(savedInstanceState.getString("tvFullName"));
        tvQty.setText(savedInstanceState.getString("tvQty"));
        Log.d(MYLOG, "restore");

    }

    @Override
    public void onClick(View view) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        switch (view.getId()){
            case R.id.btnOk:
                   if (etQty.getText().toString().trim().length()!=0){
                       Log.d(MYLOG,"--"+etQty.getText().toString()+"--");
                       contentValues.put("qty",Integer.parseInt(etQty.getText().toString()));
                       database.update("goods",contentValues,"ngoods = ?",new String[] {ngoods});
                       findGoods();
                       etQty.setText("");
                   }
                break;
            case R.id.btnTest:
                Cursor cursor = database.rawQuery("select * from goods",null);
                logCursor(cursor);
                cursor.close();
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
    void findGoods(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        Log.d(MYLOG, "start find ngoods");
        Cursor cursor = database.rawQuery("select g.ngoods, g.fullname, g.qty from barcodes b " +
                        "inner join goods g on b.ngoods = g.ngoods where b.barcode = ?",
                new String[] {tvBarcode.getText().toString()});
        Log.d(MYLOG, "finish find ngoods");
        if(cursor != null && cursor.getCount() == 1 ) {
            cursor.moveToFirst();
            Log.d(MYLOG, "cursor out start");
            tvFullName.setText(cursor.getString(cursor.getColumnIndex("fullname")));
            Log.d(MYLOG, "cursor out fullname finish");
            tvQty.setText(cursor.getString(cursor.getColumnIndex("qty")).toString());
            Log.d(MYLOG, "1");
            ngoods = cursor.getString(cursor.getColumnIndex("ngoods"));
            Log.d(MYLOG, "2");

        }else{
            //Toast.makeText(this, "Не найдено!!!", Toast.LENGTH_LONG).show();
            tvFullName.setText("Товар не найден!");
            tvQty.setText("0");
        }
        cursor.close();
    }
    void updateQty(){

    }
}
