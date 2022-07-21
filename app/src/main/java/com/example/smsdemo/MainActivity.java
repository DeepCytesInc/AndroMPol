package com.example.smsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText textPhone,textMsg;
    Button buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textPhone = findViewById(R.id.text_phone);
        textMsg = findViewById(R.id.text_msg);
        buttonSend = findViewById(R.id.button_send);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    sendMessage();
                } else {
                    //when permission not granted, request here
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},100);
                }
            }
        });

        //Receive Sms code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},111);
        }



    }

    private void sendMessage() {
        String sPhone = textPhone.getText().toString().trim();
        String sMsg = textMsg.getText().toString().trim();

        if(!sPhone.equals("") && !sMsg.equals("")){
            //when both fields empty
            SmsManager smsManager = SmsManager.getDefault();
            //Send Text msg here
            smsManager.sendTextMessage(sPhone, null, sMsg,null,null);

            Toast.makeText(getApplicationContext(), "SMS sent successfully!", Toast.LENGTH_SHORT).show();
        } else {
            //When edit text is Blank
            //Display Toast
            Toast.makeText(getApplicationContext(), "Enter value first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check condition
        if (requestCode ==100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            sendMessage();
        } else if (requestCode ==111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(),"Permission Granted!",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"Permission Denied!",Toast.LENGTH_SHORT).show();
        }

        //Receive SMS
        if (requestCode ==111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ } else { }
    }


    public static class ReceiveSms extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
            {
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                String msg_from;
                if(bundle !=null){
                    try{
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];
                        for(int i =0; i < msgs.length; i++){
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            msg_from = msgs[i].getOriginatingAddress();
                            String msgbody = msgs[i].getMessageBody();

                            Toast.makeText(context, "From:  " + msg_from + ", Body: " + msgbody, Toast.LENGTH_SHORT).show();
                    //Error on below code, impplement later
                        //    TextView txtview = (TextView) findViewById(R.id.text_receive);
                        //    txtview.setText("From:  " + msg_from + ", Body: " + msgbody);

                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}