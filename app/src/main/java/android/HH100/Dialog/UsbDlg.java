package android.HH100.Dialog;

import java.util.regex.Pattern;

import android.HH100.Service.MainBroadcastReceiver;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UsbDlg extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stubeditText1

		Intent send_gs = new Intent(MainBroadcastReceiver.MSG_USB_CONNECTED);

		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

	/*	Toast.makeText(getApplicationContext(),

				"Usb�����",

				Toast.LENGTH_LONG).show();*/

		finish();

		super.onCreate(savedInstanceState);

	}

}
