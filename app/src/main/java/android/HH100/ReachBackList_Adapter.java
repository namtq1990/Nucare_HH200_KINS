package android.HH100;

import android.HH100.Structure.EventData;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by inseon.ahn on 2018-6-28.
 */
public class ReachBackList_Adapter extends BaseAdapter
{

    public static class ListHolder
	{
        public TextView EventNum;
        public TextView ManualID;
        public TextView incharge;
        public TextView value;
        public TextView date;
        public TextView date_time;
        public TextView identification;
        public TextView reachBackSuccess;
		LinearLayout layout;

		int idx;
	}

	public static ListHolder mHolder;
    ArrayList<EventData> mEventID;
	private Context mCtxt;
   public ReachBackList_Adapter(Context ctxt, ArrayList<EventData> mData)
    {
		mCtxt = ctxt;
        mEventID = mData;
    }
    public void setOnListener(clickListener listener)
    {
        mListener = listener;
    }
    public interface clickListener
    {
       void onCellClick(String type, int id, int index, String xml, String pic, String date);
    }
    private clickListener mListener;
    @Override
    public int getCount()
    {
        return mEventID.size();
    }

    @Override
    public Object getItem(int i)
    {
        return null;
    }

    @Override
    public long getItemId(int i)
    {
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


	@Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        if(convertView == null)
        {
            convertView = LayoutInflater.from(mCtxt).inflate(R.layout.database_row2, null);
            mHolder = new ListHolder();

            mHolder.layout = (LinearLayout) convertView.findViewById(R.id.EVENT_LAYOUT);
            mHolder.EventNum = (TextView) convertView.findViewById(R.id.Event_Num);
            mHolder.ManualID = (TextView) convertView.findViewById(R.id.Manual_ID);
            mHolder.incharge = (TextView) convertView.findViewById(R.id.location);
            mHolder.value = (TextView) convertView.findViewById(R.id.value);
            mHolder.date = (TextView) convertView.findViewById(R.id.date);
            mHolder.date_time = (TextView) convertView.findViewById(R.id.date_time);
            mHolder.identification = (TextView) convertView.findViewById(R.id.incharge);
            mHolder.reachBackSuccess = (TextView) convertView.findViewById(R.id.Add_Favorite_Txt);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ListHolder) convertView.getTag();
        }

        mHolder.idx = position;
        mHolder.layout.setOnClickListener(listClick);
        mHolder.layout.setOnLongClickListener(listLongClick);
        mHolder.EventNum.setText("#"+mEventID.get(position).Event_Number+"");
        mHolder.ManualID.setText(mEventID.get(position).Event_Detector);
        mHolder.incharge.setText(mCtxt.getResources().getString(R.string.alarm_duration) + " : " + mEventID.get(position).AcqTime + " "+ mCtxt.getResources().getString(R.string.sec) + "   ");
        mHolder.value.setText(mCtxt.getResources().getString(R.string.avg_doserate) + " : " + mEventID.get(position).Doserate_AVGs + "   ");

        String identification = "None";
        if(mEventID.get(position).Identification.size() != 0)
        {
            identification = mEventID.get(position).Identification.toString().replace("[","");
            identification = identification.replace("]","");
        }
        mHolder.identification.setText(mCtxt.getResources().getString(R.string.radionuclide_id) + " : " + identification+ "   ");

        String[] mDateArray = mEventID.get(position).Event_Date.split("-");
        mHolder.date.setText(mDateArray[0] + " - " + mDateArray[1] + " - " + mDateArray[2]);

        String[] mTimerArray = mEventID.get(position).StartTime.split(":");
        int mTimerInt = Integer.parseInt(mTimerArray[0]);
        if (mTimerInt < 12)
        {
            mHolder.date_time.setText("    " + (mTimerInt>=10 ? mTimerInt : ("0"+ mTimerInt  ))+ " : " + mTimerArray[1] + " : " + mTimerArray[2] + " AM");

        } else if (mTimerInt == 12)
        {
            mHolder.date_time.setText("    " + mTimerInt + " : " + mTimerArray[1] + " : " + mTimerArray[2] + " PM");

        } else if (mTimerInt > 12)
        {
            mTimerInt = mTimerInt - 12;
            mHolder.date_time.setText("    " + (mTimerInt>=10 ? mTimerInt : ("0"+ mTimerInt  )) + " : " + mTimerArray[1] + " : " + mTimerArray[2] + " PM");

        }

      mHolder.reachBackSuccess.setText(mEventID.get(position).reachBackSuccess?"성공" : "실패");
      mHolder.EventNum.setTextColor(mEventID.get(position).reachBackSuccess? Color.parseColor("#35b5e5") : Color.parseColor("#029E97"));
      mHolder.reachBackSuccess.setTextColor(mEventID.get(position).reachBackSuccess? Color.parseColor("#35b5e5") : Color.parseColor("#029E97"));
        //mHolder.FavoriteCheck.setVisibility(mEventID.get(position).reachBackSuccess.equals(IDspectrumActivity.Check.Favorite_True )? View.VISIBLE : View.INVISIBLE);


        return convertView;

    }

    // click Listener
    private View.OnClickListener listClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            ListHolder holder = (ListHolder) view.getTag();
            mListener.onCellClick("click" , mEventID.get(holder.idx).Event_Number, mEventID.get(holder.idx).idx,mEventID.get(holder.idx).reachBackXml,mEventID.get(holder.idx).reachBackPic,mEventID.get(holder.idx).Event_Date);

        }
    };

    // long click Listener
    private View.OnLongClickListener listLongClick = new View.OnLongClickListener()
    {
        @Override
        public boolean onLongClick(View view)
        {
            ListHolder holder = (ListHolder) view.getTag();
            mListener.onCellClick("longclick" , mEventID.get(holder.idx).Event_Number, mEventID.get(holder.idx).idx,mEventID.get(holder.idx).reachBackXml,mEventID.get(holder.idx).reachBackPic,mEventID.get(holder.idx).Event_Date);
            return  true;
        }
    };


}
