package com.example.chatapp2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.format.DateFormat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;
    FloatingActionButton fab;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out){
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main, "You have signed out.", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST_CODE){
            Snackbar.make(activity_main, "Successfully Signed in! Welcome!", Snackbar.LENGTH_SHORT).show();
            displayChatMessage();
        }else{
            Snackbar.make(activity_main, "Sign in failed. Try again", Snackbar.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = (RelativeLayout) findViewById(R.id.activity_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText) findViewById(R.id.input);
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(input.getText().toString(),
                        FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                input.setText("");
                adapter.notifyDataSetChanged();

            }
        });

        //Check if not signed it, then navigate sign in page
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        }else {
            Snackbar.make(activity_main, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
            //Load Content on page
            displayChatMessage();
        }

    }

    private void displayChatMessage() {

        ListView listMessage = (ListView)activity_main.findViewById(R.id.list_of_messages);

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(FirebaseDatabase.getInstance().getReference(), ChatMessage.class).setLayout(R.layout.list_item)
                .build();


        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull ChatMessage model, int position) {
                //Get reference to the view of list_item.xml
                TextView messageText, messageUser, messageTime;
                messageText = (TextView) v.findViewById(R.id.message_text);
                messageTime = (TextView) v.findViewById(R.id.message_time);
                messageUser = (TextView) v.findViewById(R.id.message_user);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
            }
        };
        listMessage.setAdapter(adapter);

    }

}
