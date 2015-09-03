package com.inifiniti.repeatalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class CanelReciever extends BroadcastReceiver {
    SQLiteDatabase db;
    @Override
    public void onReceive(Context context, Intent intent) {
        db=context.openOrCreateDatabase("RepeatAlarm", Context.MODE_PRIVATE, null);

        int aid=intent.getIntExtra("AIdcan",0);

        Intent aintent=new Intent(context,AlarmReciever.class);
        aintent.putExtra("AId", aid);

        PendingIntent alarmintent=PendingIntent.getBroadcast(context,aid,aintent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        if(alarm!=null){
            alarm.cancel(alarmintent);
            Log.d("Child Alarm","Alarm canceled and removed Id "+aid);
            String log="Alarm canceled and removed Id "+aid;
            String insertlog="INSERT INTO logs (Log) VALUES ('"+log+"')";
            String deletealarm="DELETE FROM alarms WHERE AId="+aid;                                         //
            db.execSQL(insertlog);
            db.execSQL(deletealarm);
        }

    }
}
