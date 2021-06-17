package android.HH100.LogActivity;

import android.HH100.Control.SpectrumView;
import android.HH100.DB.EventDBOper;
import android.HH100.R;
import android.HH100.Structure.EventData;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import static android.HH100.Structure.NcLibrary.SendEmail;

/**
 * Created by inseon.ahn on 2018-06-29.
 */

public class LogEventInfo extends Fragment
{

    static Activity mAtvt;
    private SpectrumView mSpectrum;
    static android.HH100.Structure.EventData EventData = new EventData();
    ListView mID_ListView;
    TextView tvEventId, tvData, tvStartTime, tvAcqTime, tvUser, tvLocation, tvDoserate_AVGs,
                    tvNeutron_AVGs, tvDoserate_MAXs, tvNeutron_MAXs,
                    tvGPS_Latitude, tvGPS_Longitude, tvEvent_Detector;
    EditText editCommnet;
    public LogEventInfo() {}

    @SuppressLint("ValidFragment")
    public LogEventInfo(Activity atvt, android.HH100.Structure.EventData item )
    {
        mAtvt = atvt;
        EventData = item;
    }

    public static LogEventInfo newInstance(int index)
    {
        LogEventInfo f = new LogEventInfo();
        Bundle args = new Bundle(2);
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LogTabActivity.mPagerEvent.setPageScrollEnabled(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.event_log_info, null);
        tvEventId = (TextView)view.findViewById(R.id.LogInfoTab_n1st_row_2);
        tvData = (TextView)view.findViewById(R.id.LogInfoTab_n1st_row_4);
        tvStartTime = (TextView)view.findViewById(R.id.LogInfoTab_n2nd_row_2);
        tvAcqTime = (TextView)view.findViewById(R.id.LogInfoTab_n2nd_row_4);
        tvUser = (TextView)view.findViewById(R.id.LogInfoTab_n3rd_row_2);
        tvLocation = (TextView)view.findViewById(R.id.LogInfoTab_n3rd_row_4);
        tvDoserate_AVGs = (TextView)view.findViewById(R.id.LogInfoTab_n4th_row_2);
        tvNeutron_AVGs = (TextView)view.findViewById(R.id.LogInfoTab_n4th_row_4);
        tvDoserate_MAXs = (TextView)view.findViewById(R.id.LogInfoTab_n5th_row_2);
        tvNeutron_MAXs = (TextView)view.findViewById(R.id.LogInfoTab_n5th_row_4);
        tvGPS_Latitude = (TextView)view.findViewById(R.id.LogInfoTab_n6th_row_2);
        tvGPS_Longitude = (TextView)view.findViewById(R.id.LogInfoTab_n6th_row_4);
        editCommnet = (EditText) view.findViewById(R.id.editT_Comment);
        tvEvent_Detector = (TextView)view.findViewById(R.id.LogInfoTab_ManualID);
        view.setOnTouchListener(click);
        editCommnet.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2)
            {
                switch (arg1)
                {
                    case EditorInfo.IME_ACTION_DONE:
                        if (EventData == null)
                            break;
                        EventDBOper mEventDB;
                        mEventDB = new EventDBOper();

                        if (editCommnet.getText().toString() == null || editCommnet.getText().toString().length() == 0)
                        {
                            mEventDB.UpdateCommnet(EventData.Event_Number, "");
                        }
                        else
                        {
                            String tem = editCommnet.getText().toString();
                            tem = tem.replace("\"", "'");
                            tem = tem.replace("'", "''");
                            mEventDB.UpdateCommnet(EventData.Event_Number, tem);
                        }

                        break;

                    default:
                        return false;

                }
                return false;
            }

            });


        tvEventId.setText("#" + EventData.Event_Number);
        tvData.setText(EventData.Event_Date);
       tvStartTime.setText(EventData.StartTime);
        tvAcqTime.setText(String.valueOf(EventData.MS.Get_AcqTime()) + " Sec");
        tvUser.setText(EventData.mUser);
        tvLocation.setText(EventData.mLocation);
        tvDoserate_AVGs.setText(EventData.Doserate_AVGs);
        tvNeutron_AVGs.setText(EventData.Neutron_AVGs);
        tvDoserate_MAXs.setText(EventData.Doserate_MAXs);
        tvNeutron_MAXs.setText(EventData.Neutron_MAXs);
        tvGPS_Latitude.setText(String.format("%.5f", EventData.GPS_Latitude));
        tvGPS_Longitude.setText(String.format("%.5f", EventData.GPS_Longitude));
        editCommnet.setText(EventData.Comment);
       tvEvent_Detector.setText(EventData.Event_Detector);

        return view;
    }

    //touch Listener
    public View.OnTouchListener click = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            LogTabActivity.mPagerEvent.setPageScrollEnabled(true);
            return false;
        }

    };


/*    //리치백 재전송
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.log_reachback, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
           case R.id.reachback:
                SendEmail( false,mAtvt,  EventData.Event_Number);
                break;

        }
        return true;
    }*/

}


