package android.HH100.LogActivity;

import android.HH100.Control.SpectrumView;
import android.HH100.DB.EventDBOper;
import android.HH100.EventListActivity;
import android.HH100.IDspectrumActivity;
import android.HH100.MainActivity;
import android.HH100.R;
import android.HH100.RecActivity;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcPeak;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Vector;

import static android.HH100.Structure.NcLibrary.SendEmail;

/**
 * Created by inseon.ahn on 2018-06-29.
 */

public class LogEventID extends Fragment
{

    static Activity mAtvt;
    private SpectrumView mSpectrum;
    static EventData mEventData  =new EventData();
    ListView mID_ListView;
    TextView textTemp;
    Adt_Isotope adt;
    public LogEventID() {
    }

    @SuppressLint("ValidFragment")
    public LogEventID(Activity atvt, EventData item )
    {
        mAtvt = atvt;
        mEventData = item;
    }

    public static LogEventID newInstance(int index) {
        LogEventID f = new LogEventID();
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

    private Adt_Isotope.CAdt_Isotope onListCellClick = new Adt_Isotope.CAdt_Isotope()
    {
        @Override
        public void onCellClick(String type, int index)

        {
            if(type.equals("click"))
            {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog));
                dialogBuilder.setTitle(mEventData.Detected_Isotope.get(index).isotopes);
                dialogBuilder.setMessage(mEventData.Detected_Isotope.get(index).Comment);
                final String ss = mEventData.Detected_Isotope.get(index).isotopes;// .replace("-", "");
                final File video = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + EventDBOper.DB_LIB_FOLDER + "/" + ss + ".mp4");
                if (ss.matches("") == false & video.isFile()) {
                    dialogBuilder.setPositiveButton(getResources().getString(R.string.video), new DialogInterface.OnClickListener()
                    {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri.fromFile(video);
                                    intent.setDataAndType(uri, "video/*");
                                    startActivity(intent);
                                }
                            });
                }
                dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
                dialogBuilder.setCancelable(false);
                dialogBuilder.show();
            }
        }
    } ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.log_spectrum, null);
        mSpectrum = (SpectrumView) view.findViewById(R.id.LogSpectrum);
        mID_ListView = (ListView) view.findViewById(R.id.EventLog_ID_List);
        textTemp = (TextView) view.findViewById(R.id.ID_Title);
        adt = new Adt_Isotope(mAtvt, mEventData);
        mID_ListView.setAdapter(adt);
        adt.setOnListener(onListCellClick);

        mSpectrum.setChArraySize(MainActivity.CHANNEL_ARRAY_SIZE);
        mSpectrum.LogMode(true);
        mSpectrum.Change_X_to_Energy(mEventData.MS.Get_Coefficients().get_Coefficients());
        mSpectrum.Set_inform(getResources().getString(R.string.acq_time), String.valueOf(mEventData.MS.Get_AcqTime() + getResources().getString(R.string.sec)));
        mSpectrum.Set_inform2(getResources().getString(R.string.avg_cps), String.valueOf((int) mEventData.MS.Get_AvgCPS() + mEventData.AvgFillCps));
        mSpectrum.Set_inform3(getResources().getString(R.string.total_count), String.valueOf( mEventData.MS.Get_TotalCount()));
        mSpectrum.SetChArray(mEventData.MS.ToInteger());
/*
        mSpectrum.SetSpectrum(mEventData.MS);

        EventDBOper mEventDBOper = new EventDBOper();
        mEventDBOper.Set_Crytal_Info(MainActivity.mPrefDB.Get_CryStal_Type_Number_pref());

        mEventData.MS.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
        mEventData.MS.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
        mEventData.MS.setFWHM(mEventDBOper.Cry_Info.FWHM);

        mEventData.BG.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
        mEventData.BG.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
        mEventData.BG.setFWHM(mEventDBOper.Cry_Info.FWHM);

        Vector<NcPeak> peakInfo_bg = new Vector<NcPeak>();
        peakInfo_bg = FindPeaksN.GetPPSpectrum_H(mEventData.BG);
        mEventData.BG.SetPeakInfo(peakInfo_bg);

        FindPeaksN mFPM = new FindPeaksN();
        Vector<NcPeak> Peaks = mFPM.Find_Peak(mEventData.MS, mEventData.BG);
        mSpectrum.SetPeakInfo(Peaks);
*/


        return view;
    }


    /**
     * Isotope List Adapter
     * Created by inseon.ahn on 2018-03-15.
     */
    public static class Adt_Isotope extends BaseAdapter {

        public class ListHolder {
            TextView EventNum;
            TextView incharge;
            TextView value;
            LinearLayout main;

            int idx;
        }

        ListHolder mHolder;
        Activity mCtxt;
        EventData data = new EventData();

        public Adt_Isotope(Activity ctxt, EventData data)
        {
            mCtxt = ctxt;
            this.data = data;
        }

        @Override
        public int getCount() {
            //return mArrEvent.size();
            if(data.Detected_Isotope==null)
            {
                return 0;
            }
            else
                return data.Detected_Isotope.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }


        @Override
        public int getViewTypeCount() {
            // menu type count
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            // current menu type
            return 1;
        }

        public interface CAdt_Isotope
        {
            void onCellClick(String type, int index);
        }
        private CAdt_Isotope mListener;
        public void setOnListener(CAdt_Isotope listener)
        {
            mListener = listener;
        }


        @Override
        public View getView(int position, View curview, ViewGroup viewGroup) {
            if (curview == null)
            {
                curview = LayoutInflater.from(mAtvt).inflate(R.layout.id_list_row, null);
                mHolder = new ListHolder();
                mHolder.EventNum = (TextView) curview.findViewById(R.id.Id_list_Name);
                mHolder.incharge = (TextView) curview.findViewById(R.id.Id_list_Confidence);
                mHolder.value = (TextView) curview.findViewById(R.id.Id_list_Doserate);
               mHolder.main = (LinearLayout) curview.findViewById(R.id.layout);

                curview.setTag(mHolder);

            }
            else
            {
                mHolder = (ListHolder) curview.getTag();
            }

            mHolder.idx = position;
            mHolder.main.setTag(mHolder);
            mHolder.main.setOnClickListener(listClick);

            //0109 추가 Screening_Process ==0 이면 red로 표시
            if(mEventData.Detected_Isotope.get(position).Screening_Process == 1)
            {
                mHolder.EventNum.setTextColor(Color.rgb(230, 220, 0));
            }
            else
            {
                //EventNum.setTextColor(Color.rgb(255, 0, 0));
                mHolder.EventNum.setTextColor(Color.rgb(230, 220, 0));
            }

            mHolder.EventNum.setText(data.Detected_Isotope.get(position).isotopes);

            mHolder.incharge.setText(mCtxt.getResources().getString(R.string.confidence_level) + ": " + data.Detected_Isotope.get(position).Get_ConfidenceLevel() );

            if (data.Doserate_unit == -1)
            {
                mHolder.value.setText(mCtxt.getResources().getString(R.string.activity) + ": "+ data.Detected_Isotope.get(position).DoseRate_S+" ");
            }
            else
            {
                mHolder.value.setText(mCtxt.getResources().getString(R.string.dose_rate) + ": "+ data.Detected_Isotope.get(position).DoseRate_S+" ");
            }

            return curview;
        }

        // click Listener
        private View.OnClickListener listClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ListHolder holder = (ListHolder) view.getTag();
                int listIndex = holder.idx;
                mListener.onCellClick("click" , listIndex);

            }
        };

    }


    //리치백 재전송
/*    @Override
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
                SendEmail( false,mAtvt,  mEventData.Event_Number);
                break;

        }
        return true;
    }*/

}


