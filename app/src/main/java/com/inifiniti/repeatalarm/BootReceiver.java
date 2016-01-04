package com.inifiniti.repeatalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BootReceiver extends BroadcastReceiver {
    SQLiteDatabase db;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
                || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON")
                || intent.getAction().equals("android.intent.action.REBOOT")){
            // Set the alarm here.

            setParentAlarm(context);

            //setChildAlarms(context);
        }

    }

    private void setParentAlarm(Context context) {
        db=context.openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);

        int parentid=978912;
        Calendar cal=Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE, 0);

        Intent parentintent=new Intent(context,AlarmSetter.class);
        parentintent.putExtra("ParentId", parentid);

        PendingIntent parentpendingintent=PendingIntent.getBroadcast(context,parentid,parentintent,0);
        AlarmManager parentalarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        parentalarm.setRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_DAY,parentpendingintent);

        Log.d("Parent Alarm", "Parent Alarm set at " + cal.getTime() + ", Id " + parentid + " after Booting");
        String log="Parent Alarm set at " + cal.getTime() + ", Id " + parentid + " after Booting";
        String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
        db.execSQL(insertlog);
    }

    public void setChildAlarms(Context context) {
        db=context.openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM");
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm");

        String getalarms="SELECT * FROM alarms";
        Cursor cur=db.rawQuery(getalarms, null);
        if(cur!=null){
            if(cur.moveToFirst()){
                do{
                    int aid=cur.getInt(cur.getColumnIndex("AId"));
                    String caltime=cur.getString(cur.getColumnIndex("Time"));
                    String date=cur.getString(cur.getColumnIndex("Date"));
                    String repeat=cur.getString(cur.getColumnIndex("RepeatType"));

                    Calendar alarmtime=Calendar.getInstance();
                    Date atime = new Date();
                    try {atime=tf.parse(caltime);} catch (ParseException e) {e.printStackTrace();}
                    alarmtime.setTime(atime);                                                           //Issue...!!! Date being set at Jan 01 1970...
                    int hr=alarmtime.get(Calendar.HOUR_OF_DAY);
                    int min=alarmtime.get(Calendar.MINUTE);

                    Calendar setalarm=Calendar.getInstance();
                    setalarm.set(Calendar.HOUR_OF_DAY,hr);
                    setalarm.set(Calendar.MINUTE, min);

                    Calendar cal = Calendar.getInstance();                                                          //Today date
                    String datetoday = df.format(cal.getTime());
                    Date tudate = new Date();
                    try {tudate = df.parse(datetoday);} catch (ParseException e) {}
                    cal.setTime(tudate);

                    Calendar caltaskdate=Calendar.getInstance();
                    Date tdate = new Date();
                    try {tdate = df.parse(date);} catch (ParseException e) {}
                    caltaskdate.setTime(tdate);

                    if (cal.equals(caltaskdate)){
                        Intent intent=new Intent(context,AlarmReciever.class);
                        intent.putExtra("AId", aid);

                        PendingIntent alarmintent=PendingIntent.getBroadcast(context,aid,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

                        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,alarmtime.getTimeInMillis(),0,alarmintent);
                        Log.d("Child Alarm", "Child Alarm set at " + alarmtime.getTime() + ", Id " + aid + ", Repeating " + repeat +", after Boot");
                        String log="Child Alarm set at " + alarmtime.getTime() + ", Id " + aid + ", Repeating " + repeat +", after Boot";
                        String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
                        db.execSQL(insertlog);
                    }

                }while (cur.moveToNext());
            }cur.close();
        }

    }
}
