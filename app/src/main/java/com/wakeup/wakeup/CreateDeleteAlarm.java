package com.wakeup.wakeup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.wakeup.wakeup.GroupTab.GroupAlarmDetailsFragment;
import com.wakeup.wakeup.PersonalAlarmTab.PersonalAlarmDetailsFragment;
import com.wakeup.wakeup.ObjectClass.Alarm;

import java.util.Calendar;

public class CreateDeleteAlarm extends AppCompatActivity {

    private String viewTitle, buttonName;
    private Alarm alarmData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_delete_alarm);

        viewTitle = getIntent().getExtras().getString("ViewTitle");
        buttonName = getIntent().getExtras().getString("ButtonName");
        updateViewDetails();

        Fragment fragment = null;
        if (viewTitle.contains("Edit")) {
            alarmData = getIntent().getExtras().getParcelable("AlarmData");
            if (alarmData.isGroup()) {
                // isGroup
                System.out.println("[DEBUG] Group Details Fragment");
                fragment = new GroupAlarmDetailsFragment(alarmData.getAlarmName());
            } else {
                System.out.println("[DEBUG] Personal Details Fragment");
                //isPersonal
                fragment = new PersonalAlarmDetailsFragment(alarmData.getAlarmName());
            }
            ((TextView) findViewById(R.id.tv_time_display)).setText(alarmData.getTime());
            //TODO: Update Spinner value
//        (Spinner) findViewById(R.id.input_spinner); // Update value of Spinner
        }
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_alarm_details, fragment);
        transaction.commit();


        Spinner spinner = (Spinner) findViewById(R.id.input_spinner);
//        spinner.setOnItemSelectedListener(this);
//                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.game_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!viewTitle.equals("New Alarm")) {
            getMenuInflater().inflate(R.menu.alarm_edit_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.menu.alarm_edit_menu) {
            deleteAlarm();
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void updateViewDetails() {
        getSupportActionBar().setTitle(viewTitle);
        ((Button) findViewById(R.id.btn_create_save_alarm)).setText(buttonName);
    }

    public void deleteAlarm(){
        //TODO: perform delete alarm
    }

    public void submitButtonOnClick(View view) {
        //TODO: Save details in database
        if(viewTitle.contains("Edit")){
            //update existing alarm details
        }else{
            //create new alarm
        }
        finish();
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) { // Use the current time as the
            // default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        }
    }
}