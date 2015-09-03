package com.inifiniti.repeatalarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmSetter extends BroadcastReceiver{
    SQLiteDatabase db;

    @Override
    public void onReceive(Context context, Intent intent) {
        int pid=intent.getIntExtra("ParentId",0);
        if(pid==0){
            Log.d("ParentId","Parent Alarm pending intent failed!");
        }

        parentAlarmNotify(context,pid);

        setAlarms(context);



        /*
        *
        * Set All Alarms here....
        *
        * */


    }

    private void parentAlarmNotify(Context context, int pid) {
        db=context.openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);

        NotificationCompat.Builder nb= new NotificationCompat.Builder(context);
        nb.setAutoCancel(true);
        nb.setSmallIcon(R.drawable.ic_alarm);
        nb.setContentTitle("Parent Alarm Set");
        nb.setContentText("Parent Alarm Id " + pid);

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        nb.setSound(alert);
        nb.setCategory(Notification.CATEGORY_STATUS);

        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, nb.build());
        String log="Parent Alarm Broadcasted!";
        String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
        db.execSQL(insertlog);

    }

    public void setAlarms(Context context) {
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

                    Calendar calcomp=Calendar.getInstance();

                    if (cal.equals(caltaskdate)){
                        Intent intent=new Intent(context,AlarmReciever.class);
                        intent.putExtra("AId", aid);
                        intent.putExtra("RepeatType",repeat);

                        PendingIntent alarmintent=PendingIntent.getBroadcast(context,aid,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

                        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,setalarm.getTimeInMillis(),0,alarmintent);
                        Log.d("Child Alarm", "Child Alarm set by Parent at " + setalarm.getTime() + ", Id " + aid + ", Repeating " + repeat);
                        String log="Child Alarm set by Parent at " + setalarm.getTime() + ", Id " + aid + ", Repeating " + repeat;
                        String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
                        db.execSQL(insertlog);
                    }

                    switch (repeat){
                        case "Daily":
                            if (cal.after(caltaskdate)) {                                             // || cal.equals(caltaskdate)
                                date = datetoday;                                                     //daily tasks just set task date as today's date every day
                                caltaskdate.set(Calendar.DATE, cal.get(Calendar.DATE));               //Required to set alarm here...

                                String updaterepeattaskdate = "UPDATE alarms SET Date='"+date+"' WHERE AId=" + aid + "";
                                db.execSQL(updaterepeattaskdate);

                                Intent intent=new Intent(context,AlarmReciever.class);
                                intent.putExtra("AId", aid);
                                intent.putExtra("RepeatType", repeat);

                                PendingIntent alarmintent=PendingIntent.getBroadcast(context,aid,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                                AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);   //Alarm being set twice after booting...

                                alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,setalarm.getTimeInMillis(),0,alarmintent);
                                Log.d("Child Alarm", "Child Alarm set by Parent at " + setalarm.getTime() + ", Id " + aid + ", Repeating " + repeat);
                                String log="Child Alarm set by Parent at " + setalarm.getTime() + ", Id " + aid + ", Repeating " + repeat;
                                String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
                                db.execSQL(insertlog);
                            }
                            break;
                        case "Weekly":
                            if (cal.after(caltaskdate)) {
                                calcomp.setTime(tdate);
                                calcomp.add(Calendar.DATE, 7);
                                date = df.format(calcomp.getTime());

                                String updaterepeattaskdate = "UPDATE alarms SET Date='"+date+"' WHERE AId=" + aid + "";
                                db.execSQL(updaterepeattaskdate);
                            }
                            break;
                        case "Monthly":
                            if (cal.after(caltaskdate)) {
                                calcomp.setTime(tdate);
                                calcomp.add(Calendar.MONTH, 1);
                                date = df.format(calcomp.getTime());

                                String updaterepeattaskdate = "UPDATE alarms SET Date='"+date+"' WHERE AId=" + aid + "";
                                db.execSQL(updaterepeattaskdate);
                            }
                            break;
                    }

                }while (cur.moveToNext());
            }cur.close();
        }
    }
}
