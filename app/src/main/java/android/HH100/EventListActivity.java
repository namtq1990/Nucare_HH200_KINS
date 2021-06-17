
package android.HH100;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import android.HH100.DB.DBMng;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.DB.EventDBOper;
import android.HH100.Identification.Isotope;
import android.HH100.LogActivity.LogTabActivity;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.HH100.Structure.NcLibrary.Separate_EveryDash3;

public class EventListActivity extends Activity  implements AbsListView.OnScrollListener {
	private static final int CHOOSE_EVENT_FILE = 234124;

	private Context mContext;
	public static int mSelPositioin = 0;
	public static EventData EventLog = null;

	// 다이얼로그창
	AlertDialog alert1;

	//180731 EventList수정
	ArrayList<EventData> arr;
	EventList_Adapter adt;
	int clickIndex = 0;
	ListView eventList;
	EventDBOper mEventDB;
	int selectId = -1;
	String photoFile = "";

	//180803 listview 페이징 처리
	ProgressBar progressBar;
	int currentPage = 0; // 페이징변수. 초기 값은 1 이다.
	boolean lastItemVisibleFlag = false; // 리스트 스크롤이 마지막 셀(맨 바닥)로 이동했는지 체크할 변수
	boolean mLockListView = false; // 데이터 불러올때 중복안되게 하기위한 변수
	final int OFFSET = 50;                  // 한 페이지마다 로드할 데이터 갯수.
	int dbCount = 0; // eventdb 전체 count
	ActionBar actionBar; //상단 엑션바 ex:이벤트
	CheckBox chkReachBack; //리치백로그 호출
	TextView actionBarTitle;

	boolean clickChk = false; //핸들키로 누르면 2번눌리는 현상이있음..

	@Override
	protected void onResume() {

		MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;


		// 해당하는 인덱스로 스크롤 이동
		if (arr != null && arr.size() != 0) {
			eventList.smoothScrollToPosition(clickIndex);
		}

		super.onResume();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		setContentView(R.layout.database);

		MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
		MainActivity.ACTIVITY_STATE = Activity_Mode.EVENTLOG_LIST_MAIN;
		mContext = EventListActivity.this;
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		eventList = (ListView) findViewById(R.id.eventlist);
		eventList.setOnScrollListener(this);
		arr = new ArrayList<EventData>();

		LoadDBList(currentPage);

		//180803 actionbar custom
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true); //아이콘 사용할지 여부
		actionBar.setDisplayShowTitleEnabled(false); //타이블 사용할지여부
		actionBar.setDisplayShowCustomEnabled(true);

/*		LinearLayout actionBarLayout = new LinearLayout(EventListActivity.this);
		LinearLayout.LayoutParams paramLLayoutBG = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		actionBarLayout.setLayoutParams(paramLLayoutBG);
		TextView txt = new TextView(EventListActivity.this);
		txt.setTextSize(16);
		txt.setTextColor(Color.rgb(255, 255, 255));
		txt.setText(getResources().getString(R.string.event_log));
		actionBarLayout.addView(txt);
		actionBarLayout.setTag("actionBar");
		actionBarLayout.setOnClickListener(click);

		actionBar.setCustomView(actionBarLayout);*/

		LinearLayout actionBarLayout = new LinearLayout(EventListActivity.this);
		actionBarLayout.setGravity(Gravity.CENTER_VERTICAL);

		LinearLayout.LayoutParams paramLLayoutBG = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		actionBarLayout.setLayoutParams(paramLLayoutBG);

		LinearLayout.LayoutParams chk = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		chk.gravity = Gravity.CENTER_VERTICAL;
		chk.weight = 1;
		actionBarTitle = new TextView(EventListActivity.this);
		actionBarTitle.setTextSize(16);
		actionBarTitle.setTextColor(Color.rgb(255, 255, 255));
		actionBarTitle.setText(getResources().getString(R.string.event_log));
		actionBarTitle.setTag("event");
		actionBarTitle.setLayoutParams(chk);
		actionBarLayout.addView(actionBarTitle);

		chk = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		chk.gravity = Gravity.CENTER_VERTICAL;
		chk.setMargins(0, 0, 15, 0);

		chkReachBack = new CheckBox(EventListActivity.this);
		chkReachBack.setText(getResources().getString(R.string.reachback_log));
		chkReachBack.setTextColor(Color.rgb(51, 181, 229));
		chkReachBack.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));

		chkReachBack.setLayoutParams(chk);
		actionBarLayout.addView(chkReachBack);
		actionBar.setCustomView(actionBarLayout);
		chkReachBack.setOnCheckedChangeListener(chkClick);
		chkReachBack.setTag("ReachBack_Cheak");
	}

	// reachback failed  checkbox click Listener
	private CompoundButton.OnCheckedChangeListener chkClick = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				currentPage = 0;
				arr = new ArrayList<EventData>();
				actionBarTitle.setText(getResources().getString(R.string.reachback_log));
				LoadDBList(true, currentPage);
			} else {
				currentPage = 0;
				arr = new ArrayList<EventData>();
				actionBarTitle.setText(getResources().getString(R.string.event_log));
				LoadDBList(0);
			}
		}
	};

	// click Listener
	private View.OnClickListener click = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch ((String) view.getTag()) {
				case "actionBar":
					eventList.setSelection(0); //최상단으로 이동
					break;
			}

		}
	};

	//reachback log 호출
	public void LoadDBList(boolean success, int page) {
		Log.e("ahn", "LoadDBList");
		//List에 사용될 DB내용들 저장
		arr = new ArrayList<EventData>();
		eventList.setTag("reachback");
		try {
			Cursor cu = DBMng.GetInst(EventListActivity.this).loadAllReachBackDB(success, (page * OFFSET), OFFSET);

			if (cu.getCount() != 0) {

				while (cu.moveToNext()) {
					EventData Item = new EventData();
					Item.Event_Number = Integer.parseInt(cu.getString(cu.getColumnIndex("_id")));
					Item.Event_Date = cu.getString(cu.getColumnIndex("Date"));
					Item.AcqTime = cu.getString(cu.getColumnIndex("AcqTime"));
					Item.Doserate_AVGs = cu.getString(cu.getColumnIndex("Avg_Gamma"));
					Item.Event_Detector = cu.getString(cu.getColumnIndex("Event_Detector"));
					Item.StartTime = cu.getString(cu.getColumnIndex("begin"));
					Item.reachBackXml = cu.getString(cu.getColumnIndex("xml"));
					Item.reachBackSuccess = Boolean.parseBoolean(cu.getString(cu.getColumnIndex("success")));
					Item.reachBackPic = cu.getString(cu.getColumnIndex("Photo"));
					String temp = cu.getString(cu.getColumnIndex("Identification"));
					Item.idx = Integer.parseInt(cu.getString(cu.getColumnIndex("idx")));
					Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
					ArrayList<Isotope> tempIdentification = new ArrayList<Isotope>();

					String temp1 = "None";

					for (int i = 0; i < IsoTemp.size(); i++) {
						Isotope iso = new Isotope();
						iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
						tempIdentification.add(iso);
						Item.Identification = new ArrayList<String>();

						if (tempIdentification != null && tempIdentification.size() != 0) {
							for (int k = 0; k < tempIdentification.size(); k++) {
								temp1 = "";
								if (k == tempIdentification.size() - 1) {
									temp1 = tempIdentification.get(k).isotopes;
									break;
								} else {
									temp1 = temp1 + tempIdentification.get(k).isotopes + ", ";
								}
							}
						}
						Item.Identification.add(temp1);
					}
					arr.add(Item);
				}

				cu.close();

				ReachBackList_Adapter rAdt = new ReachBackList_Adapter(EventListActivity.this, arr);
				eventList.setAdapter(rAdt);
				rAdt.setOnListener(reachBackListClick);

			} else {
				if (currentPage != 0) {
					Toast.makeText(EventListActivity.this, getResources().getString(R.string.eventlog_load_failed), Toast.LENGTH_SHORT).show();
					currentPage--;
					eventList.setSelection(adt.getCount() - 1);

				} else {
					eventList.setAdapter(null);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private ReachBackList_Adapter.clickListener reachBackListClick = new ReachBackList_Adapter.clickListener() {
		@Override
		public void onCellClick(String type, int id, int index, String xml, String pic, String date) {
			clickIndex = index;
			Log.e("ahn", "clickIndex : " + clickIndex);
			if (type.equals("click")) {
				reSendReachBackDlg(index, id);
				return;
			}
			if (type.equals("longclick")) {
				re_deleteDlg(index, pic, xml, date);
				return;
			}

		}

	};

	//리치백 리스트 삭제
	public void re_deleteDlg(final int _id, final String pic, final String xml, final String date) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity.this);
		dialogBuilder.setTitle("리치백 로그");
		dialogBuilder.setMessage("리치백 로그를 삭제하시겠습니까");
		dialogBuilder.setPositiveButton("삭제",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						if (DBMng.GetInst(mContext).deleteReachBack(_id)) {
							if (!pic.equals("") || pic != null) {
								File f2d = new File(pic);
								f2d.delete();
							}

							if (!xml.equals("") || xml != null) {
								File f2d = new File(xml);
								f2d.delete();
							}

							if (chkReachBack.isChecked()) {
								LoadDBList(true, currentPage);
							} else {
								LoadDBList(0);
							}
						}

					}
				});
		dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();


	}

	//리치백 재전송 dlg
	public void reSendReachBackDlg(final int index, final int id) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity.this);
		dialogBuilder.setTitle(getResources().getString(R.string.transmit_N42));
		dialogBuilder.setMessage(getResources().getString(R.string.send_toRCBCenter_event1) + "\n" + getResources().getString(R.string.event_log) + "(#" + id + ")" + getResources().getString(R.string.send_toRCBCenter_event3) + "\n"
				+ getResources().getString(R.string.receive_email_address) + " " + MainActivity.mPrefDB.Get_recv_email());
		dialogBuilder.setPositiveButton(getResources().getString(R.string.transmit),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						if (!NcLibrary.isNetworkOnline(mContext)) {
							EventDBOper mEventDB = new EventDBOper();
							mEventDB.OpenDB();
							EventData m_EventData = DBMng.GetInst(mContext).loadReachBackDB(index);
							m_EventData.reachBackSuccess = false;
							mEventDB.EndDB();

							DBMng.GetInst(mContext).updateReachBack(m_EventData.idx, m_EventData.reachBackPic, m_EventData.reachBackXml, m_EventData.reachBackSuccess + "");

							arr = new ArrayList<EventData>();
							LoadDBList(true, currentPage);

							NcLibrary.Show_Dlg1(EventListActivity.this.getResources().getString(R.string.internet_not).toString(), EventListActivity.this);
						} else {

							EventData reachBack = null;
							reachBack = NcLibrary.reSendEmail(true, mContext, index, true);
							if (reachBack != null) {
								if (DBMng.GetInst(mContext).updateReachBack(reachBack.idx, reachBack.reachBackPic, reachBack.reachBackXml, reachBack.reachBackSuccess + "")) {
									if (chkReachBack.isChecked()) {
										LoadDBList(true, currentPage);
									} else {
										LoadDBList(0);
									}

								}

							}
						}


					}
				});
		dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), null);
		dialogBuilder.setCancelable(false);
		//dialogBuilder.show();
		AlertDialog dialog = dialogBuilder.show();
		TextView msgView = (TextView) dialog.findViewById(android.R.id.message);
		//msgView.setTextSize(16);

	}


	public void LoadDBList(int page) {
		//List에 사용될 DB내용들 저장
		try {
			mEventDB = new EventDBOper();
			mEventDB.OpenDB();
			dbCount = (int) mEventDB.getEventDBCount();
			eventList.setTag("event");

			Cursor cu = mEventDB.LoadEventList(page * OFFSET, OFFSET);

			if (cu.getCount() != 0) {

				while (cu.moveToNext()) {
					EventData Item = new EventData();
					Item.Event_Number = Integer.parseInt(cu.getString(cu.getColumnIndex("_id")));
					Item.Event_Date = cu.getString(cu.getColumnIndex("Date"));
					Item.AcqTime = cu.getString(cu.getColumnIndex("AcqTime"));
					Item.Doserate_AVGs = cu.getString(cu.getColumnIndex("Avg_Gamma"));
					Item.Favorite_Checked = cu.getString(cu.getColumnIndex("Favorite"));
					Item.Event_Detector = cu.getString(cu.getColumnIndex("Event_Detector"));
					Item.StartTime = cu.getString(cu.getColumnIndex("begin"));
					Item.PhotoFileName1 = Separate_EveryDash3(cu.getString(cu.getColumnIndex("Photo")));

					String temp = cu.getString(cu.getColumnIndex("Identification"));
					Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');

					String temp1 = "None";
					for (int i = 0; i < IsoTemp.size(); i++) {
						Isotope iso = new Isotope();
						iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
						ArrayList<Isotope> tempIdentification = new ArrayList<Isotope>();
						tempIdentification.add(iso);

						if (tempIdentification != null && tempIdentification.size() != 0) {
							temp1 = "";
							for (int k = 0; k < tempIdentification.size(); k++) {
								if (k == tempIdentification.size() - 1) {
									temp1 = tempIdentification.get(k).isotopes;
									Item.Identification.add(temp1);
									break;
								} else {
									temp1 = temp1 + tempIdentification.get(k).isotopes + ", ";
									Item.Identification.add(temp1);
								}

							}
						}

					}
					arr.add(Item);
				}

				cu.close();
				mEventDB.EndDB();

				adt = new EventList_Adapter(EventListActivity.this, arr);
				eventList.setAdapter(adt);
				adt.setOnListener(onListCellClick);
				eventList.setSelection((currentPage * OFFSET) - 1);
			} else {
				if (currentPage != 0) {
					Toast.makeText(EventListActivity.this, getResources().getString(R.string.eventlog_load_failed), Toast.LENGTH_SHORT).show();
					currentPage--;
					eventList.setSelection(adt.getCount() - 1);
				} else {
					eventList.setAdapter(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}


/*	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		NcLibrary.SaveText("onKeyDown"+keyCode+" \n");
	switch (keyCode)
		{


			case KeyEvent.KEYCODE_ENTER:
				NcLibrary.SaveText("KEYCODE_ENTER \n");
				Intent intent = new Intent(EventListActivity.this, LogTabActivity.class);
				intent.putExtra("_id", 1);
				startActivity(intent);
				return false;

				//break;


		}
		return super.onKeyDown(keyCode, event);
	}*/

@Override
public boolean dispatchKeyEvent(KeyEvent event)
{
	int[] coordinates = new int[2];
	long downTime = 0;
	long eventTime = 0;
	MotionEvent down_event, up_event;
	try
	{
		getCurrentFocus().getLocationOnScreen(coordinates);
	}
	catch (NullPointerException e)
	{
		return super.dispatchKeyEvent(event);
	}

	//getCurrentFocus().getLocationOnScreen(coordinates);
	if(coordinates[1]<10) //상단 리치백로그 인지 판단
	{
		return super.dispatchKeyEvent(event);
	}
	else
	{
		if(!clickChk)
		{
			clickChk = true;
			switch (event.getKeyCode())
			{
				case KeyEvent.KEYCODE_ENTER :
					eventList.getSelectedView().getLocationOnScreen(coordinates);

					downTime = SystemClock.uptimeMillis();
					eventTime = SystemClock.uptimeMillis();
					down_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, coordinates[0],coordinates[1], 0);
					up_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,  coordinates[0],coordinates[1], 0);
					eventList.dispatchTouchEvent(down_event);
					eventList.dispatchTouchEvent(up_event);
					return false;

				case KeyEvent.KEYCODE_POWER: //longclick 일때

					eventList.getSelectedView().getLocationOnScreen(coordinates);

					downTime = SystemClock.uptimeMillis();
					eventTime = SystemClock.uptimeMillis();
					down_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, coordinates[0],coordinates[1], 0);
					eventList.dispatchTouchEvent(down_event);
					return false;
			}

		}
		else
		{
			clickChk = false;
		}

		return super.dispatchKeyEvent(event);
	}

/*	if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
	{
		int[] coordinates = new int[2];
		eventList.getSelectedView().getLocationOnScreen(coordinates);

		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();
		MotionEvent down_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, coordinates[0],coordinates[1], 0);
		MotionEvent up_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,  coordinates[0],coordinates[1], 0);
		eventList.dispatchTouchEvent(down_event);
		eventList.dispatchTouchEvent(up_event);

		return false;
	}

	if (event.getKeyCode() == KeyEvent.KEYCODE_POWER)
	{
		int[] coordinates = new int[2];
		eventList.getSelectedView().getLocationOnScreen(coordinates);

		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis()+1000;
		MotionEvent down_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, coordinates[0],coordinates[1], 0);
		eventList.dispatchTouchEvent(down_event);

		return false;
	}*/

	//return super.dispatchKeyEvent(event);
}


	//cView.dispatchTouchEvent(event);

	//eventList.performClick();
/*		switch (event.getKeyCode())
		{

			case KeyEvent.KEYCODE_ENTER:
				NcLibrary.SaveText("KEYCODE_ENTER \n");
				Intent intent = new Intent(EventListActivity.this, LogTabActivity.class);
				intent.putExtra("_id", 1);
				startActivity(intent);
				return false;

			//break;


		}*/

		//return super.dispatchKeyEvent(event);

/*		NcLibrary.SaveText("dispatchKeyEventdispatchKeyEvent\n");
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			// keydown logic
			NcLibrary.SaveText("dispatchKeyEvent\n");
			NcLibrary.SaveText("event  "+event+"\n");


		}
		if(event.getAction() == KeyEvent.KEYCODE_ENTER)
		{
			NcLibrary.SaveText("dispatchKeyEvent KEYCODE_ENTER\n");
			Toast.makeText(EventListActivity.this,"dispatchKeyEvent KEYCODE_ENTER",Toast.LENGTH_LONG).show();
			return false;
		}

		if(event.getAction() == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			NcLibrary.SaveText("dispatchKeyEvent KEYCODE_ENTERR\n");
			return false;
		}*/


/*
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		NcLibrary.SaveText("dispatchKeyEventdispatchKeyEvent\n");
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			// keydown logic
			NcLibrary.SaveText("dispatchKeyEvent\n");
			NcLibrary.SaveText("event  "+event+"\n");


		}
		if(event.getAction() == KeyEvent.KEYCODE_ENTER)
		{
			NcLibrary.SaveText("dispatchKeyEvent KEYCODE_ENTER\n");
			Toast.makeText(EventListActivity.this,"dispatchKeyEvent KEYCODE_ENTER",Toast.LENGTH_LONG).show();
			return false;
		}

		if(event.getAction() == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			NcLibrary.SaveText("dispatchKeyEvent KEYCODE_ENTERR\n");
			return false;
		}
		return super.dispatchKeyEvent(event);
	}*/


	//clickListener
	private EventList_Adapter.clickListener onListCellClick = new EventList_Adapter.clickListener()
	{
		@Override
		public void onCellClick(String type, final int id, int index, final int photoSize)
		{
			clickIndex = index;
			Log.e("ahn", "clickIndex : " + clickIndex);
			if (type.equals("click"))
			{
				try
				{
					//NcLibrary.SaveText("clickIndex  "+id+"\n");
					Intent intent = new Intent(EventListActivity.this, LogTabActivity.class);
					intent.putExtra("_id", id);
					startActivity(intent);

				//	return;
				}catch (Exception e)
				{
					//NcLibrary.SaveText("click Exception\n");
					NcLibrary.Write_ExceptionLog(e);
				}

			}
			if (type.equals("longclick"))
			{
				photoFile = "";
				selectId = id;

				if (!NcLibrary.isNetworkOnline(mContext))
				{
					EventDBOper mEventDB = new EventDBOper();
					mEventDB.OpenDB();
					EventData m_EventData = mEventDB.LoadEventDB(selectId);
					mEventDB.EndDB();
					int idx = DBMng.GetInst(EventListActivity.this).loadReahBackDB(m_EventData.Event_Date, m_EventData.StartTime);
					if (idx <= 0)
					{
						m_EventData.reachBackSuccess = false;
						DBMng.GetInst(mContext).writeReachBackDB(m_EventData);
					}
					else
					{
						mEventDB.OpenDB();
						EventData eventdata = DBMng.GetInst(mContext).loadReachBackDB(idx);
						mEventDB.EndDB();
						eventdata.reachBackSuccess = false;

						DBMng.GetInst(mContext).updateReachBack(idx, m_EventData.reachBackPic, m_EventData.reachBackXml, m_EventData.reachBackSuccess+"");
					}

					NcLibrary.Show_Dlg1(EventListActivity.this.getResources().getString(R.string.internet_not).toString(), EventListActivity.this);
				}
				else
				{
					NcLibrary.showReachBackDlg(mContext,selectId,photoSize, true);
				}

			}

				return;
			}

	};


		private void Show_Dlg(String Message) {
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity.this);
			// dialogBuilder.setTitle(Title);
			dialogBuilder.setMessage(Message);
			dialogBuilder.setNegativeButton("OK", null);
			dialogBuilder.setCancelable(false);
			dialogBuilder.show();
		}

		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data)
		{
			// TODO Auto-generated method stub

	/*		if (resultCode == RESULT_OK)
			{*/
				if (requestCode == 1)
				{
/*
					String photoFile = "";
					if(NcLibrary.hashMap.get("photo")!=null)
					{
						photoFile  = (String) NcLibrary.hashMap.get("photo");
						NcLibrary.hashMap.remove("photo");
					}
					EventDBOper mEventDB = new EventDBOper();
					mEventDB.OpenDB();
					String updatePhoto = "";
				 	Cursor cu = mEventDB.LoadGallery(1,selectId);
				  	ArrayList<String> photoFileName = new ArrayList<String>();
				  	if (cu.moveToFirst())
				  	{
					  if (cu.getCount() > 0)
					  {
						  updatePhoto = cu.getString(cu.getColumnIndex("Photo"));
					  }
				  }

				  if(!updatePhoto.equals(""))
				  {
					  updatePhoto = updatePhoto+ photoFile+";" ;
				  }
				  else
				  {
					  updatePhoto = photoFile + ";";
				  }

					mEventDB.updateGallery(1,  selectId, updatePhoto); //1 : photo
					mEventDB.EndDB();
*/

                    NcLibrary.SendEmail( false,EventListActivity.this,  selectId,true);
			/*	EventData reachBack = NcLibrary.SendEmail( false,EventListActivity.this,  selectId,true);
					if(reachBack!=null)
					{
						int idx = DBMng.GetInst(EventListActivity.this).loadReahBackDB(reachBack.Event_Date, reachBack.StartTime);
						if (idx <= 0)
						{
							DBMng.GetInst(EventListActivity.this).writeReachBackDB(reachBack);
						}
						else
						{
							DBMng.GetInst(EventListActivity.this).updateReachBack(idx, reachBack.reachBackPic, reachBack.reachBackXml, reachBack.reachBackSuccess+"");
						}
					}*/
				}
		//	}
			//super.onActivityResult(requestCode, resultCode, data);
		}


		@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
			super.onBackPressed();

		}

		@Override
		protected void onDestroy() {
			MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;

			super.onDestroy();
		}


	//180803 페이징 추가
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		// 1. OnScrollListener.SCROLL_STATE_IDLE : 스크롤이 이동하지 않을때의 이벤트(즉 스크롤이 멈추었을때).
		// 2. lastItemVisibleFlag : 리스트뷰의 마지막 셀의 끝에 스크롤이 이동했을때.
		// 3. mLockListView == false : 데이터 리스트에 다음 데이터를 불러오는 작업이 끝났을때.
		// 1, 2, 3 모두가 true일때 다음 데이터를 불러온다.
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag && mLockListView == false)
		{
			// 화면이 바닦에 닿을때 처리
			// 로딩중을 알리는 프로그레스바를 보인다.
		//	progressBar.setVisibility(View.VISIBLE);

			// 다음 데이터를 불러온다.

			getItem();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// firstVisibleItem : 화면에 보이는 첫번째 리스트의 아이템 번호.
		// visibleItemCount : 화면에 보이는 리스트 아이템의 갯수
		// totalItemCount : 리스트 전체의 총 갯수
		// 리스트의 갯수가 0개 이상이고, 화면에 보이는 맨 하단까지의 아이템 갯수가 총 갯수보다 크거나 같을때.. 즉 리스트의 끝일때. true
		lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);

	}

	private void getItem()
	{
		// 리스트에 다음 데이터를 입력할 동안에 이 메소드가 또 호출되지 않도록 mLockListView 를 true로 설정한다.
		mLockListView = true;

		// 다음 20개의 데이터를 불러와서 리스트에 저장한다.
		currentPage++;
		mLockListView = false;
		if(!chkReachBack.isChecked())
		{
			LoadDBList(currentPage);
		}
		else
		{
			LoadDBList(true,currentPage);
		}


/*			// 1초 뒤 프로그레스바를 감추고 데이터를 갱신하고, 중복 로딩 체크하는 Lock을 했던 mLockListView변수를 풀어준다.
			new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					adt.notifyDataSetChanged();
					eventList.setSelection( (currentPage*OFFSET)-1);
					progressBar.setVisibility(View.GONE);
					mLockListView = false;
				}
			},200);*/

		}




	}
