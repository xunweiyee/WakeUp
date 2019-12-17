package com.wakeup.wakeup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.wakeup.wakeup.Games.GameMath;
import com.wakeup.wakeup.Games.GameShake;
import com.wakeup.wakeup.Games.GameTicTacToe;
import com.wakeup.wakeup.ObjectClass.Alarm;
import com.wakeup.wakeup.UserLogin.ChangePassword;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Calendar;

public class AlarmPopUp extends AppCompatActivity {
    private Context mContext;
    private static MediaPlayer mediaPlayer = new MediaPlayer();
    private static Vibrator vibrator;
    private Alarm alarm;
    private boolean isAllowToBack = false;
    private View snoozeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_alarm_pop_up);

        mContext = getApplicationContext();
        alarm = getIntent().getExtras().getParcelable("AlarmData");

        // TODO: Detect Swipe Up
        findViewById(R.id.img_swipeup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipeToContinue(view);
            }
        });

        findViewById(R.id.btn_Snooze).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snoozeView = view;
                snooze(view);
            }
        });

        //Call the alarm
        setAlarmRing();
        setVibration();
    }
    //

    public void swipeToContinue(View v) {
        //  gameOption = [NONE, TICTACTOE, MATH, SHAKER]
        switch (alarm.getGameOption()) {
            case 0:
                stopAlarm();
                // TODO: Close Application
                moveTaskToBack(true);
                break;
            case 1:
                navigateToMath(v);
                break;
            case 2:
                navigateToTicTacToe(v);
                break;
            case 3:
                navigateToShake(v);
                break;
            default:
                System.out.println("Opps pass default stopping alarm.");
                stopAlarm();
                break;
        }
    }

    private void navigateToTicTacToe(View v) {
        Intent gameTicTacToe = new Intent(this, GameTicTacToe.class);
        gameTicTacToe.putExtra("AlarmData", alarm);
        startActivity(gameTicTacToe);
    }

    private void navigateToMath(View v) {
        Intent goToMathPage = new Intent(this, GameMath.class);
        goToMathPage.putExtra("AlarmData", alarm);
        startActivity(goToMathPage);
    }

    private void navigateToShake(View v) {
        Intent goToShakePage = new Intent(this, GameShake.class);
        goToShakePage.putExtra("AlarmData", alarm);
        startActivity(goToShakePage);
    }

    private void snooze(View view) {

        Intent intent = new Intent(this, AlarmReceiver.class);
        //Change the alarm object to byte so that pass
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutputStream out = null;
//        try {
//            out = new ObjectOutputStream(bos);
//            out.writeObject(alarm);
//            out.flush();
//            byte[] data = bos.toByteArray();
//            intent.putExtra("alarm", data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                bos.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
        Bundle alarmBundle = new Bundle();
        alarmBundle.putParcelable("alarm", alarm);
        intent.putExtra("alarmBundle", alarmBundle);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                (alarm.getAlarmKey()).hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar c = Calendar.getInstance();
        long timeAfter10Minutes = c.getTimeInMillis() + 10 * 60 * 1000;
        Toast.makeText(mContext, "Snooze for 10 minutes.", Toast.LENGTH_SHORT).show();
        System.out.println("Snooze for 10 minutes" + timeAfter10Minutes);

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeAfter10Minutes, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeAfter10Minutes, pendingIntent);
        }

        // Stop Alarm
        stopAlarm();
        // TODO: Close Application
        moveTaskToBack(true);
    }

    // Ringing and Vibration Section
    private void setAlarmRing() {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        }
        mediaPlayer.setLooping(true);
        try {
            mediaPlayer.setDataSource(mContext, alarmUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    private void setVibration() {
        vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 500, 1000, 500};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        } else {
            //deprecated in API 26
            vibrator.vibrate(pattern, 0);
        }
    }

    private static void stopAlarm() {
        System.out.println("Stop Alarm");
        mediaPlayer.stop();
        vibrator.cancel();
    }

    @Override
    public void onBackPressed() {
        if (!isAllowToBack) {

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            System.out.println("[DEBUG] Home is pressed");
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_POWER) {
            System.out.println("[DEBUG] Power is pressed");
            return true;
//        }else{
//
//            snooze(snoozeView);
//            System.out.println("[DEBUG] Wish to let them snooze");
//            return super.onKeyDown(keyCode, event);
//        }
        }
        return true;
    }
}
