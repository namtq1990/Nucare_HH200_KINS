package android.HH100.Service;

import android.HH100.MainActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import Debug.*;
import android.util.Log;

public class StartReceiver extends BroadcastReceiver {

	Debug mDebug = new Debug();
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals("android.intent.action.BOOT_COMPLETED")) {
			//Log.i("Check", "Screen went ON");
			if(mDebug.IsDebugMode){
			Intent i = new Intent(context, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			}
		}
	}

}
