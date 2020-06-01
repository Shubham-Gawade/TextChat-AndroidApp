package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private Toolbar mytoolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabAccessorAdaptor myTabAccessorAdaptor;

    private FirebaseAuth myAuth;
    private DatabaseReference reference;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAuth=FirebaseAuth.getInstance();
        reference= FirebaseDatabase.getInstance().getReference();

        mytoolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mytoolbar);
        getSupportActionBar().setTitle("TextChat");

        myViewPager=findViewById(R.id.main_tabs_pager);
        myTabAccessorAdaptor=new TabAccessorAdaptor(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdaptor);

        myTabLayout=findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=myAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            Status obj=new Status();
            obj.updateUserStatus("online");
            verifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser=myAuth.getCurrentUser();

        if(currentUser != null)
        {
            Status obj=new Status();
            obj.updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser=myAuth.getCurrentUser();

        if(currentUser != null)
        {
            Status obj=new Status();
            obj.updateUserStatus("offline");
        }
    }

    private void verifyUserExistance()
    {
        String currentUserId=myAuth.getCurrentUser().getUid();

        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists()))
                {

                }
                else
                {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_logout_option)
        {
            Status obj=new Status();
            obj.updateUserStatus("offline");
            myAuth.signOut();
            SendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_setting_option)
        {
            SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option)
        {
            RequestNewGroup();
        }
        if(item.getItemId()==R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }
        return true;
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");
        final EditText groupNameField=new EditText(MainActivity.this);
        groupNameField.setHint("E.g   My Group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String groupName=groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this,"Please enter Group Name",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(String groupName)
    {
        reference.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this,"Group Created Successfully",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToSettingsActivity()
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SendUserToFindFriendsActivity() {
        Intent intent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(intent);
    }

//    private void updateUserStatus(String state)
//    {
//        String saveCurrentTime,saveCurrentDate;
//
//        Calendar calendar=Calendar.getInstance();
//        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
//        saveCurrentDate=currentDate.format(calendar.getTime());
//
//        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
//        saveCurrentTime=currentTime.format(calendar.getTime());
//
//        HashMap<String,Object> onlineStateMap=new HashMap<>();
//        onlineStateMap.put("time",saveCurrentTime);
//        onlineStateMap.put("date",saveCurrentDate);
//        onlineStateMap.put("state",state);
//
//        currentUserID=myAuth.getCurrentUser().getUid();
//
//        reference.child("Users").child(currentUserID).child("userState")
//                .updateChildren(onlineStateMap);
//    }
}
