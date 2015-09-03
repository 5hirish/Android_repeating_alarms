package com.inifiniti.repeatalarm;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class LogsActivity extends Activity {
    SQLiteDatabase db;
    TextView logv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        logv=(TextView)findViewById(R.id.logv);

        db=getApplicationContext().openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);
        String viewlogs="SELECT * FROM logs ORDER BY Id DESC";
        Cursor c=db.rawQuery(viewlogs, null);
        if(c!=null){
            if (c.moveToFirst()){
                do{
                    int id=c.getInt(c.getColumnIndex("Id"));
                    String log=c.getString(c.getColumnIndex("Log"));

                    logv.append(id+") "+log+"\n");
                }while (c.moveToNext());
            }c.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logs, menu);
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
