package ru.infoenergo.mis;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import static ru.infoenergo.mis.DlgConfirmationActSignKt.SMS_DELIVERED;
import static ru.infoenergo.mis.DlgConfirmationActSignKt.SMS_SENT;

public class SmsReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(action)) {
        Bundle extras = intent.getExtras();
        Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
        int statusCode = status.getStatusCode();

        switch (statusCode) {
            case CommonStatusCodes.SUCCESS:
                // Get SMS message contents
                String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                System.out.println("LOG ERR: " + message);
                // Extract one-time code from the message and complete verification
                // by sending the code back to your server.
                break;
            case CommonStatusCodes.TIMEOUT:
                System.out.println("LOG ERR sms TIMEOUT");
                // Waiting for SMS timed out (5 minutes)
                // Handle the error ...
                break;
        }
        // }

        int resultCode = getResultCode();
        if (action.equals(SMS_SENT))
            System.out.println("LOG ERR SMS_SENT status:");
        else if (action.equals(SMS_DELIVERED))
            System.out.println("LOG ERR SMS_DELIVERED status: ");
        else System.out.println("LOG ERR " + action + " status:");

        switch (resultCode) {
            case Activity.RESULT_OK:
                System.out.print("SMS sent");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                System.out.print("Generic failure");
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                System.out.print("No service");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                System.out.print("Null PDU");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                System.out.print("Radio off");
                break;
        }
    }


}

