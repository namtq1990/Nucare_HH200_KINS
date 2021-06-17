package android.HH100.LogActivity;

import android.HH100.DB.EventDBOper;
import android.HH100.R;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TabHost;

import java.io.Serializable;
import java.util.ArrayList;

public class LogTabActivity extends FragmentActivity implements Serializable
{
	public static String EXTRA_EVENT_DATA = "EventLogData";
	EventData mEventLog = new EventData();
	TabHost tabs;
	ArrayList<EventData> arr;
	public static Activity LogTabActivity;
	EventData Item = new EventData();

	//////
	static LogEventID tabEventID;
	static LogEventInfo tabEventInfo;
	static LogEventPic tabEventPic;
	TabLayout mTablayout;
	public static CustomViewPager mPagerEvent;
	static  pagerAdapter adtPager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//NcLibrary.SaveText("onCreate\n");
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//NcLibrary.SaveText("onCreate \n");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.eventlog_tab);
		
		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		mTablayout = (TabLayout) findViewById(R.id.IDL_TAB);
		mPagerEvent = (CustomViewPager) findViewById(R.id.IDFLIPPER_DETECTORS);


		int id  = getIntent().getIntExtra("_id",0);
		//NcLibrary.SaveText("_id\n");

		arr = new ArrayList<EventData>();
		try
		{
			//NcLibrary.SaveText("EventDBOper\n");
		EventDBOper mEventDB = new EventDBOper();
			mEventDB.OpenDB();
			Item = mEventDB.LoadEventDB(id);
			if (Item != null)
			{
				tabEventID = new LogEventID(android.HH100.LogActivity.LogTabActivity.this, Item);
				tabEventInfo = new LogEventInfo(android.HH100.LogActivity.LogTabActivity.this, Item);
				tabEventPic = new LogEventPic(android.HH100.LogActivity.LogTabActivity.this, Item);

				adtPager = new pagerAdapter(getSupportFragmentManager());
				mPagerEvent.setAdapter(adtPager);
				mPagerEvent.setCurrentItem(0);
				mPagerEvent.addOnPageChangeListener(pageChangeListener);
				mTablayout.addOnTabSelectedListener(tabSelectedListener);
			}
			mEventDB.EndDB();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			NcLibrary.Write_ExceptionLog(e);
		}

	}

/*
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();

	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		//NcLibrary.SaveText("Log dispatchKeyEvent : "+event.getKeyCode()+"\n");
		switch (event.getKeyCode())
		{
			case KeyEvent.KEYCODE_BACK:
				onBackPressed();
				return false;
		}
		return super.dispatchKeyEvent(event);
	}
*/


		ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener()
	{
		@Override
		public void onPageSelected(int position)
		{
			super.onPageSelected(position);
			mTablayout.setScrollPosition(position,0f,true);
		}
	};


	TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener()
	{
		@Override
		public void onTabSelected(TabLayout.Tab tab)
		{
			mPagerEvent.setCurrentItem(tab.getPosition());
		}
		@Override
		public void onTabUnselected(TabLayout.Tab tab) {}
		@Override
		public void onTabReselected(TabLayout.Tab tab) {}
	};

	private class pagerAdapter extends FragmentStatePagerAdapter
	{
		public pagerAdapter(FragmentManager fm)
		{
			super(fm);
		}
		@Override
		public Fragment getItem(int position)
		{
			switch(position)
			{
				case 0:
					return  tabEventID.newInstance(position);
				case 1:
					return tabEventInfo.newInstance(position);
				case 2:
					return tabEventPic.newInstance(position);
				default:
					return  tabEventPic.newInstance(position);
			}
		}
		@Override
		public int getCount()
		{
			return 3;
		}

		@Override
		public int getItemPosition(Object object)
		{
			return POSITION_NONE;
		}


		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			super.destroyItem(container, position, object);
			if(object instanceof Fragment)
			{
				Fragment fragment    = (Fragment)object;
				FragmentManager fm    = fragment.getFragmentManager();
				FragmentTransaction ft    = fm.beginTransaction();
			//	ft.commitAllowingStateLoss();
			//	ft.remove(fragment);
				ft.commitAllowingStateLoss();
			}
		}
	}
}
