package com.inifiniti.repeatalarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;


public class MainActivity extends Activity {

    TextView alarmtime,repeattype,result;
    Button set;
    int ahr,amin,size=0,aid=1;
    String repeat;
    ArrayList<Integer> weekdays;

    SQLiteDatabase db;
    SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmtime=(TextView)findViewById(R.id.timePicker);
        repeattype=(TextView)findViewById(R.id.repeat);
        result=(TextView)findViewById(R.id.textView);
        set=(Button)findViewById(R.id.button);
        weekdays=new ArrayList<Integer>();

        db=getApplicationContext().openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);
        String createlogs="CREATE TABLE IF NOT EXISTS logs (Id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, Log VARCHAR NOT NULL)";
        db.execSQL(createlogs);
        String createalarm="CREATE TABLE IF NOT EXISTS alarms ( Id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, AId INTEGER NOT NULL , Time VARCHAR NOT NULL, Date VARCHAR NOT NULL , RepeatType VARCHAR NOT NULL)";
        db.execSQL(createalarm);

        /*String removealarm="DELETE FROM alarms WHERE RepeatType='No repeat'";                 //Dont Use....!!!
        db.execSQL(removealarm);*/

        String use="";
        sp=getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        use=sp.getString("Usage","No");

        if(!use.equals("First")){
            setParentAlarm();
            editor.putString("Usage","First");
            editor.commit();
        }

        final ArrayAdapter weekdayadapter=ArrayAdapter.createFromResource(this, R.array.repeat, android.R.layout.select_dialog_item);

        final Calendar c= Calendar.getInstance();

        alarmtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int shr, int smin) {
                        ahr = shr;
                        amin = smin;
                        alarmtime.setText(ahr + ":" + amin);
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });


        final AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("Repeat On:");
        adb.setAdapter(weekdayadapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                repeat = weekdayadapter.getItem(position).toString();
                repeattype.setText(repeat);

                if (repeat.equals("Selected week days")) {
                    AlertDialog.Builder adbd = new AlertDialog.Builder(MainActivity.this);
                    adbd.setTitle("Select Day:");
                    adbd.setMultiChoiceItems(R.array.weekdays, null, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int item, boolean ischecked) {
                            if (ischecked) {
                                weekdays.add(item);
                            } else if (weekdays.contains(item)) {
                                weekdays.remove(Integer.valueOf(item));
                            }
                        }
                    });
                    adbd.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            size = weekdays.size();
                        }
                    });
                    adbd.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            repeat = "No repeat";
                        }
                    });
                    AlertDialog wd = adbd.create();
                    wd.show();
                }
            }
        });

        repeattype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog repertd = adb.create();
                repertd.show();
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("Time:" + ahr + ":" + amin + " \nRepeat: " + repeat + " \nSize: " + size);

                setChildAlarms(ahr, amin, repeat, weekdays);
            }
        });
    }

    private void setChildAlarms(int ahr, int amin, String repeat, ArrayList<Integer> weekdays) {

        db=getApplicationContext().openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);

        Calendar cdate=Calendar.getInstance();
        int d=cdate.get(Calendar.DATE);
        int m=cdate.get(Calendar.MONTH)+1;
        String date=d+"/"+m;

        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,ahr);
        cal.set(Calendar.MINUTE, amin);
        String time=ahr+":"+amin;

        String countalarm="SELECT MAX(Id) FROM alarms";
        Cursor cur=db.rawQuery(countalarm, null);
        if(cur!=null){
            if(cur.moveToFirst()){
               aid = cur.getInt(0);
            }
            else{
                aid=1;
            }cur.close();
        }else {aid=1;}

        Intent intent=new Intent(getApplicationContext(),AlarmReciever.class);
        intent.putExtra("AId", aid);
        intent.putExtra("RepeatType",repeat);

        PendingIntent alarmintent=PendingIntent.getBroadcast(getApplicationContext(),aid,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm=(AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        if(repeat.equals("Selected week days")){
            Iterator it=weekdays.iterator();
            int wid=0;
            if(aid!=0){
                wid=aid*100;}
            else{
                wid=9900;
            }

            if(!weekdays.isEmpty()){
                repeat="Weekly";                                                                        //Are considered as repeating weekly only...
                while (it.hasNext()){
                    intent.putExtra("AId", wid);
                    PendingIntent wpendingIntent = PendingIntent.getBroadcast(getApplicationContext(), wid, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    int wd=Integer.parseInt(it.next().toString())+1;
                    Calendar calw = Calendar.getInstance();
                    calw.set(Calendar.DAY_OF_WEEK, wd);
                    calw.set(Calendar.HOUR_OF_DAY, ahr);
                    calw.set(Calendar.MINUTE, amin);

                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calw.getTimeInMillis(), 0, wpendingIntent);
                    Log.d("Child Alarm", "Child Alarm set at " + calw.getTime() + ", Id " + wid + ", Repeating " + repeat);
                    String log="Child Alarm set at " + calw.getTime() + ", Id " + wid + ", Repeating " + repeat;
                    String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
                    db.execSQL(insertlog);

                    d=calw.get(Calendar.DATE);
                    m=calw.get(Calendar.MONTH)+1;
                    date=d+"/"+m;
                    String insertalarm="INSERT INTO alarms ( AId, Time, Date, RepeatType) VALUES("+wid+",'"+time+"','"+date+"','"+repeat+"')";
                    db.execSQL(insertalarm);
                    wid++;
                }
            }
        }
        else {

            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),0,alarmintent);
            Log.d("Child Alarm", "Child Alarm set at " + cal.getTime() + ", Id " + aid+", Repeating "+repeat);
            String log="Child Alarm set at " + cal.getTime() + ", Id " + aid + ", Repeating " + repeat;
            String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
            db.execSQL(insertlog);

            String insertalarm="INSERT INTO alarms ( AId, Time, Date, RepeatType) VALUES("+aid+",'"+time+"','"+date+"','"+repeat+"')";
            db.execSQL(insertalarm);
        }
    }

    private void setParentAlarm() {

        db=getApplicationContext().openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);

        int parentid=978912;
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE, 0);

        Intent parentintent=new Intent(getApplicationContext(),AlarmSetter.class);
        parentintent.putExtra("ParentId", parentid);

        PendingIntent parentpendingintent=PendingIntent.getBroadcast(getApplicationContext(),parentid,parentintent,0);
        AlarmManager parentalarm=(AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        parentalarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, parentpendingintent);

        Log.d("Parent Alarm", "Parent Alarm set at " + cal.getTime() + ", Id " + parentid);
        String log="Parent Alarm set at " + cal.getTime() + ", Id " + parentid;
        String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
        db.execSQL(insertlog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_log) {
            Intent in= new Intent(this,LogsActivity.class);
            startActivity(in);
        }else if(id == R.id.action_alarms){
            Intent in= new Intent(this,AlarmsActivity.class);
            startActivity(in);
        }else if(id == R.id.action_test){
            Intent in= new Intent(this,TestActivity.class);
            startActivity(in);
        }

        return super.onOptionsItemSelected(item);
    }
}
