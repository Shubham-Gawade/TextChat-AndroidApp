package com.example.whatsapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Status
{
    private FirebaseAuth myAuth;
    private DatabaseReference reference;
    private String currentUserID;

    public void updateUserStatus(String state)
    {
        myAuth=FirebaseAuth.getInstance();
        reference= FirebaseDatabase.getInstance().getReference();

        String saveCurrentTime,saveCurrentDate;

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserID=myAuth.getCurrentUser().getUid();

        reference.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);
    }
}
