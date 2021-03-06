package com.wakeup.wakeup.ui.main;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.wakeup.wakeup.AlarmReceiver;
import com.wakeup.wakeup.ObjectClass.Alarm;
import com.wakeup.wakeup.ObjectClass.FirebaseHelper;
import com.wakeup.wakeup.ObjectClass.GroupMember;
import com.wakeup.wakeup.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    // firebase
    DatabaseReference dbAlarms;
    DatabaseReference dbUserGroups;
    DatabaseReference dbGroupAlarms;
    private List<Alarm> allAlarms;
    private List<Alarm> personalAlarms;
    private List<Alarm> groupAlarms;

    // adapter
    private RecyclerView.Adapter homeAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rvAlarm;
    private Context context;
    private ArrayList<GroupMember> allContacts;

    public HomeFragment(ArrayList<GroupMember> allContacts) {
        // Required empty public constructor
        allAlarms = new ArrayList<>();
        personalAlarms = new ArrayList<>();
        groupAlarms = new ArrayList<>();
        this.allContacts = allContacts;

        // firebase
        dbAlarms = new FirebaseHelper().getDbUserAlarms();
        dbGroupAlarms = new FirebaseHelper().getDbUserGroupAlarms();
        // createDummyData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvAlarm = view.findViewById(R.id.rv_home);
        layoutManager = new LinearLayoutManager(getContext());
        rvAlarm.setLayoutManager(layoutManager);
//        homeAdapter = new HomeFragmentAdapter(allAlarms, allContacts);
//        rvAlarm.setAdapter(homeAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // personal alarms
        dbAlarms.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clear previous list
                personalAlarms.clear();

                // iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Alarm alarm = postSnapshot.getValue(Alarm.class);
                    String alarmKey = postSnapshot.getKey(); //alarm key
                    alarm.setAlarmKey(alarmKey);
                    if (alarm.isOn()) {
                        startAlarm(alarm);
                    } else {
                        cancelAlarm(alarm);
                    }
                    personalAlarms.add(alarm);
                }

                // create adapter
                createAlarmAdaptor();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // group alarms
        dbGroupAlarms.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // clear previous list
                groupAlarms.clear();

                // iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Alarm alarm = postSnapshot.getValue(Alarm.class);
                    String alarmKey = postSnapshot.getKey(); //alarm key
                    alarm.setAlarmKey(alarmKey);
                    if (alarm.isOn()) {
                        startAlarm(alarm);
                    } else {
                        cancelAlarm(alarm);
                    }
                    groupAlarms.add(alarm);
                }

                // create adapter
                createAlarmAdaptor();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void createAlarmAdaptor() {
        allAlarms.clear();
        allAlarms.addAll(personalAlarms);
        allAlarms.addAll(groupAlarms);

        homeAdapter = new HomeFragmentAdapter(allAlarms, allContacts);
        rvAlarm.setAdapter(homeAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void startAlarm(Alarm alarm) {
        Intent intent = new Intent(context, AlarmReceiver.class);

        Bundle alarmBundle = new Bundle();
        alarmBundle.putParcelable("alarm", alarm);
        intent.putExtra("alarmBundle", alarmBundle);
        System.out.println("Data is store in byte:" + alarmBundle);
        FragmentActivity activity = getActivity();
        if(activity != null) {
            AlarmManager alarmManager =
                    (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    (alarm.getAlarmKey()).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar c = alarm.getTimeInCalender();

            if (c.before(Calendar.getInstance())) {
                c.add(Calendar.DATE, 1);
            }

            if (android.os.Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            }
        }else{
            System.out.println("[DEBUG] Activity null?");
        }
    }

    // Cancel if already created
    private void cancelAlarm(Alarm alarm) {
        AlarmManager alarmManager =
                (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                (alarm.getAlarmKey()).hashCode(), intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            System.out.println("[DEBUG] Alarm is cancel");
//            Toast.makeText(context, "Alarm is Cancel.", Toast.LENGTH_SHORT).show();
        }
    }

}
