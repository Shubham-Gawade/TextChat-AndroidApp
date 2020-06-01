package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiver_user_id, senderUserID, currentState;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    private DatabaseReference userRef,chatReqRef,contactRef,notificationRef;
    private FirebaseAuth myAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiver_user_id=getIntent().getExtras().get("visit_user_id").toString();

        myAuth=FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");

        senderUserID=myAuth.getCurrentUser().getUid();

        userProfileImage=findViewById(R.id.users_profile_image);
        userProfileName=findViewById(R.id.visit_user_name);
        userProfileStatus=findViewById(R.id.visit_user_status);
        sendMessageRequestButton=findViewById(R.id.send_message_request_button);
        declineMessageRequestButton=findViewById(R.id.decline_message_request_button);
        currentState="new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        userRef.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")))
                {
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();
                    String userImage=dataSnapshot.child("image").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    //Picasso.with(ProfileActivity.this).load(userImage).into(userProfileImage);

                    ManageChatRequest();
                }
                else
                {
                    String userName=dataSnapshot.child("name").getValue().toString();
                    String userStatus=dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest()
    {
        chatReqRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(receiver_user_id))
                        {
                            String request_type=dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                            if(request_type.equals("sent"))
                            {
                                currentState="request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if(request_type.equals("received"))
                            {
                                currentState="request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");

                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);

                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.hasChild(receiver_user_id))
                                    {
                                        currentState="friends";
                                        sendMessageRequestButton.setText("Remove Friend");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(!senderUserID.equals(receiver_user_id))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    sendMessageRequestButton.setEnabled(false);

                    if(currentState.equals("new"))
                    {
                        SendChatRequest();
                    }
                    if(currentState.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if(currentState.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if(currentState.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else
        {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact()
    {
        contactRef.child(senderUserID).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            contactRef.child(receiver_user_id).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState="new";
                                                sendMessageRequestButton.setText("Send Message");
                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest()
    {
        contactRef.child(senderUserID).child(receiver_user_id).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            contactRef.child(receiver_user_id).child(senderUserID).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                chatReqRef.child(senderUserID).child(receiver_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    chatReqRef.child(receiver_user_id).child(senderUserID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        sendMessageRequestButton.setEnabled(true);
                                                                                        currentState="friends";
                                                                                        sendMessageRequestButton.setText("Remove Friend");

                                                                                        declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                        declineMessageRequestButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest()
    {
        chatReqRef.child(senderUserID).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                         if(task.isSuccessful())
                         {
                             chatReqRef.child(receiver_user_id).child(senderUserID).removeValue()
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task)
                                         {
                                             if(task.isSuccessful())
                                             {
                                                 sendMessageRequestButton.setEnabled(true);
                                                 currentState="new";
                                                 sendMessageRequestButton.setText("Send Message");
                                                 declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                 declineMessageRequestButton.setEnabled(false);
                                             }
                                         }
                                     });
                         }
                    }
                });
    }

    private void SendChatRequest()
    {
        chatReqRef.child(senderUserID).child(receiver_user_id).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            chatReqRef.child(receiver_user_id).child(senderUserID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                HashMap<String,String> chatNotificationMap=new HashMap<>();
                                                chatNotificationMap.put("from",senderUserID);
                                                chatNotificationMap.put("type","request");

                                                notificationRef.child(receiver_user_id).push()
                                                        .setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if(task.isSuccessful())
                                                        {
                                                            sendMessageRequestButton.setEnabled(true);
                                                            currentState="request_sent";
                                                            sendMessageRequestButton.setText("Cancel Chat Request");
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
