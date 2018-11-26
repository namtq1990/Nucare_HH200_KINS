package android.HH100.Service;


import android.HH100.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class Guide extends Activity {

    ImageView mGuide;
    boolean mFinishCheck = true;
    TimerTask mTask;
    Timer mTimer;
    public static int LauncherUpdateCnt = 0;


    public interface GuideMode {
        public String GetGuideModeTitle = "GetGuideModeTitle";
        public String GetAppFileName = "GetAppFileName";
        public String GetModeNull = "GetModeNull";
        public String GetRoot = "Getting Administrator Authorization Guide";
        public String SetLauncher = "Setting Launcher Guide";
        public String UpdateLauncher = "UpdateLauncher";
        public int UpdateLauncherInt = 45678;
        public int SetLauncherint = 12345;
    }
public String mAppFileName;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.guide);
        mFinishCheck = true;
        LauncherUpdateCnt = 0;
        mGuide = (ImageView) findViewById(R.id.ImgView);

        Intent intent = getIntent();
        String mGuideMode = intent.getStringExtra(GuideMode.GetGuideModeTitle);
        mAppFileName = intent.getStringExtra(GuideMode.GetAppFileName);

        if (mGuideMode.equals(GuideMode.UpdateLauncher)) {

            LauncherUpdateCnt = 3;


            mSuperHandler.obtainMessage(GuideMode.UpdateLauncherInt, LauncherUpdateCnt).sendToTarget();

            Toast.makeText(getApplicationContext(), getString(R.string.Launcher_Update_GuideMsg), 1).show();




        } else if (mGuideMode.equals(GuideMode.SetLauncher)) {

            LauncherUpdateCnt = 2;

            mSuperHandler.obtainMessage(GuideMode.SetLauncherint, LauncherUpdateCnt).sendToTarget();

            Toast.makeText(getApplicationContext(), getString(R.string.Launcher_Setting_GuideMsg), 1).show();



        }
        //ImgView

    }

    @Override
    protected void onDestroy() {

        mFinishCheck = false;
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mTask != null) {
            mTask.cancel();
        }
        super.onDestroy();
    }

    private Handler mSuperHandler = new Handler() {

        @SuppressWarnings("deprecation")
        public void handleMessage(Message msg) {
            if (msg.what == GuideMode.UpdateLauncherInt) {
                switch ((int) msg.obj) {
                    case 0:
                        mGuide.setBackgroundResource(R.drawable.launcherupdateguide1);
                        break;
                    case 1:
                        mGuide.setBackgroundResource(R.drawable.launcherupdateguide2);
                        break;
                    case 2:
                        mGuide.setBackgroundResource(R.drawable.launcherupdateguide3);
                        break;
                    case 3:
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + mAppFileName+".apk")), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                        if (mTask != null) {
                            mTask.cancel();
                        }
                        finish();
                        break;
                }
            }


            if (msg.what == GuideMode.SetLauncherint) {
                switch ((int) msg.obj) {
                    case 0:
                        mGuide.setBackgroundResource(R.drawable.launchersettingguide1);
                        break;
                    case 1:

                        mGuide.setBackgroundResource(R.drawable.launchersettingguide2);
                        break;
                    case 2:
                        startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));

                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                        if (mTask != null) {
                            mTask.cancel();
                        }
                        finish();
                        break;
                }
            }

            super.handleMessage(msg);

        }

    };

}
