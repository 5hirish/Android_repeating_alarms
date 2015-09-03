package com.inifiniti.repeatalarm;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class AlarmsActivity extends Activity {
    SQLiteDatabase db;
    TextView alarmv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);
        alarmv=(TextView)findViewById(R.id.alarmv);

        db=getApplicationContext().openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);
        String viewalarms="SELECT * FROM alarms";
        Cursor cur=db.rawQuery(viewalarms, null);
        if(cur!=null){
            if (cur.moveToFirst()){
                do{
                    int aid=cur.getInt(cur.getColumnIndex("AId"));
                    String caltime=cur.getString(cur.getColumnIndex("Time"));
                    String date=cur.getString(cur.getColumnIndex("Date"));
                    String repeat=cur.getString(cur.getColumnIndex("RepeatType"));

                    alarmv.append(aid+") "+caltime+" : "+date+" : "+repeat+"\n");
                }while (cur.moveToNext());
            }cur.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarms, menu);
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
