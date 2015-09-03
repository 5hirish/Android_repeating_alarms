package com.inifiniti.repeatalarm;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestActivity extends Activity {
    SQLiteDatabase db;
    TextView testv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        db=getApplicationContext().openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM");
        testv=(TextView)findViewById(R.id.testv);


        String getalarms="SELECT * FROM alarms";
        Cursor cur=db.rawQuery(getalarms, null);
        if(cur!=null){
            if(cur.moveToFirst()){
                do{
                    int aid=cur.getInt(cur.getColumnIndex("AId"));
                    String caltime=cur.getString(cur.getColumnIndex("Time"));
                    String date=cur.getString(cur.getColumnIndex("Date"));
                    String repeat=cur.getString(cur.getColumnIndex("RepeatType"));

                    Calendar cal = Calendar.getInstance();                                                          //Today date
                    String datetoday = df.format(cal.getTime());
                    Date tudate = new Date();
                    try {tudate = df.parse(datetoday);} catch (ParseException e) {}
                    cal.setTime(tudate);

                    Calendar caltaskdate=Calendar.getInstance();
                    Date tdate = new Date();
                    try {tdate = df.parse(date);} catch (ParseException e) {}
                    caltaskdate.setTime(tdate);

                    //testv.append(caltaskdate.getTime()+"=="+cal.getTime()+"\n");

                    if (cal.equals(caltaskdate)){
                        testv.append(aid+")"+caltime+" Repeating "+repeat+"\n");
                    }else {
                        testv.append("Not equal Id "+aid+")"+caltime+" Repeating "+repeat+"\n");
                    }

                }while (cur.moveToNext());
            }cur.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
