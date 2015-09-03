package com.inifiniti.repeatalarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AlarmReciever extends BroadcastReceiver {
    SQLiteDatabase db;
    @Override
    public void onReceive(Context context, Intent intent) {
        String repeat="";
        int aid=intent.getIntExtra("AId", 0);
        repeat=intent.getStringExtra("RepeatType");

        NotificationCompat.Builder nb= new NotificationCompat.Builder(context);
        nb.setAutoCancel(true);
        nb.setSmallIcon(R.drawable.ic_alarm);
        nb.setContentTitle("Repeat Alarm");
        nb.setContentText("Test Alarm Id " + aid+ " Repeating "+repeat);
        nb.setPriority(Notification.PRIORITY_HIGH);

        Intent incancel=new Intent(context,CanelReciever.class);
        incancel.putExtra("AIdcan", aid);
        PendingIntent cancel=PendingIntent.getBroadcast(context,0,incancel,PendingIntent.FLAG_UPDATE_CURRENT);
        nb.addAction(R.drawable.ic_cancel, "Cancel", cancel);

        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        nb.setSound(alert);
        nb.setCategory(Notification.CATEGORY_ALARM);

        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, nb.build());

        db=context.openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);

        Log.d("Child Alarm", "Alarm Broadcasted...!! Id " + aid);
        String log="Alarm Broadcasted...!! Id "+aid;
        String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
        db.execSQL(insertlog);
        log="Alarm removed Id "+aid;
        insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";

        String removealarm="DELETE FROM alarms WHERE AId="+aid;

        Intent aintent=new Intent(context,AlarmReciever.class);
        aintent.putExtra("AId", aid);

        PendingIntent alarmintent=PendingIntent.getBroadcast(context,aid,aintent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        alarm.cancel(alarmintent);                                                                                  //Test ... ... ... ... time for which alarm is ringing..

        Log.d("Child Alarm", "Alarm canceled Id " + aid);
        String logcancel="Alarm canceled Id "+aid;
        String insertcancellog="INSERT INTO logs (Log) VALUES ('"+logcancel+"')";
        db.execSQL(insertcancellog);

        if(repeat.equals("No repeat")){
            db.execSQL(insertlog);
            db.execSQL(removealarm);
        }
    }
}
