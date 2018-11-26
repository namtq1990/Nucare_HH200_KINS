
package android.HH100;

import android.HH100.DB.DBMng;
import android.HH100.DB.EventDBOper;
import android.HH100.Identification.Isotope;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class ReachBackListActivity extends Activity  implements AbsListView.OnScrollListener
{

	private Context mContext;


	public static int mSelPositioin = 0;
	public static int Pos = 0;

	public static EventData EventLog = null;
	public static Vector<EventData> mAllLog = null;

	static ListView mEventList = null;

	//180725
	ArrayList<EventData> arr;
	ReachBackList_Adapter adt;
	int clickIndex = 0;
	ListView eventList;
	CheckBox chkFailedDB;
	ActionBar actionBar; //상단 엑션바 ex:이벤트

	//180803 listview 페이징 처리
	ProgressBar progressBar;
	int currentPage = 0; // 페이징변수. 초기 값은 1 이다.
	boolean lastItemVisibleFlag  = false; // 리스트 스크롤이 마지막 셀(맨 바닥)로 이동했는지 체크할 변수
	boolean mLockListView = false; // 데이터 불러올때 중복안되게 하기위한 변수
	final int OFFSET = 50;                  // 한 페이지마다 로드할 데이터 갯수.

	@Override
	protected void onResume()
	{
		Log.e("ahn","onResume");


/*		// 해당하는 인덱스로 스크롤 이동
		if(arr !=null && arr.size() != 0)
		{
			Log.e("ahn","clickIndex1 : "+clickIndex);
			eventList.smoothScrollToPosition(clickIndex);
		}*/

		super.onResume();

	}


	public void LoadDBList(boolean success, int page)
	{
		Log.e("ahn","LoadDBList");

		try
		{
			Cursor cu = DBMng.GetInst(ReachBackListActivity.this).loadAllReachBackDB(success,(page*OFFSET),OFFSET);

			if (cu.getCount() != 0)
			{

				while (cu.moveToNext())
				{
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

					for (int i = 0; i < IsoTemp.size(); i++)
					{
						Isotope iso = new Isotope();
						iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
						tempIdentification.add(iso);
						Item.Identification = new ArrayList<String>();

						if (tempIdentification  != null && tempIdentification.size() != 0)
						{
							for (int k = 0; k <tempIdentification.size(); k++)
							{
								temp1 = "";
								if (k == tempIdentification.size() - 1)
								{
									temp1 = tempIdentification.get(k).isotopes;
									break;
								}
								else
								{
									temp1 =temp1+ tempIdentification.get(k).isotopes + ", ";
								}
							}
						}
						Item.Identification.add(temp1);
					}
					arr.add(Item);
				}

				cu.close();

				ReachBackList_Adapter rAdt = new ReachBackList_Adapter(ReachBackListActivity.this, arr);
				eventList.setAdapter(rAdt);
				eventList.setSelection( (currentPage*OFFSET)-1);
				rAdt.setOnListener(onListCellClick);

			}
			else
			{
				if(currentPage != 0)
				{
					Toast.makeText(ReachBackListActivity.this, getResources().getString(R.string.eventlog_load_failed),Toast.LENGTH_SHORT).show();
					currentPage--;
					eventList.setSelection(adt.getCount() - 1);

				}
				else
				{
					eventList.setAdapter(null);
				}



			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}


	}


	private ReachBackList_Adapter.clickListener onListCellClick = new ReachBackList_Adapter.clickListener()
	{
		@Override
		public void onCellClick(String type, int id, int index, String xml, String pic, String date)
		{
			clickIndex = index;
			Log.e("ahn","clickIndex : "+clickIndex);
			if (type.equals("click"))
			{
				reSendReachBackDlg(index,id);

				return;
			}
			if (type.equals("longclick"))
			{
				deleteDlg(index,pic,xml,date);
				return;
			}

		}

	};



	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.database_re);

		MainActivity.ACTIVITY_STATE = MainActivity.Activity_Mode.EVENTLOG_LIST_MAIN;

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		mContext = this;
		eventList = (ListView)findViewById(R.id.eventlist);
		//List에 사용될 DB내용들 저장
		arr = new ArrayList<EventData>();
		eventList.setOnScrollListener(this);

		//180803 actionbar custom
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true); //아이콘 사용할지 여부
		actionBar.setDisplayShowTitleEnabled(true); //타이틀 사용할지여부
		actionBar.setDisplayShowCustomEnabled(true);

		LinearLayout actionBarLayout = new LinearLayout(ReachBackListActivity.this);
		actionBarLayout.setGravity(Gravity.CENTER_VERTICAL );

		LinearLayout.LayoutParams paramLLayoutBG = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		actionBarLayout.setLayoutParams(paramLLayoutBG);

		LinearLayout.LayoutParams chk = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		chk.gravity = Gravity.CENTER_VERTICAL ;
		chk.weight = 1;
		TextView txt = new TextView(ReachBackListActivity.this);
		txt.setTextSize(16);
		txt.setTextColor(Color.rgb(255, 255, 255));
		txt.setText(getResources().getString(R.string.reachback_log));
		txt.setTag("actionBar");
		txt.setLayoutParams(chk);
		actionBarLayout.addView(txt);

        chk = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		chk.gravity = Gravity.CENTER_VERTICAL ;
		chk.setMargins(0,0,15,0);

		chkFailedDB = new CheckBox(ReachBackListActivity.this);
		chkFailedDB.setText(getResources().getString(R.string.reachback_failed));
		chkFailedDB.setTextColor(Color.rgb(51,	181,  229));
		chkFailedDB.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));

		chkFailedDB.setLayoutParams(chk);
		actionBarLayout.addView(chkFailedDB);
		actionBar.setCustomView(actionBarLayout);

		LoadDBList(true,currentPage);
		chkFailedDB.setOnCheckedChangeListener(chkClick);

	}

	// reachback failed  checkbox click Listener
	private CompoundButton.OnCheckedChangeListener chkClick = new CompoundButton.OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked)
			{
				// checked
				currentPage = 0;
				arr = new ArrayList<EventData>();
				LoadDBList(false,currentPage);
			}
			else
			{
				// not checked
				currentPage = 0;
				arr = new ArrayList<EventData>();
				LoadDBList(true,currentPage);
			}
		}
	};

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
		super.onBackPressed();

	}

	public void reSendReachBackDlg(final int index, final int id)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ReachBackListActivity.this);
		dialogBuilder.setTitle(getResources().getString(R.string.transmit_N42));
		dialogBuilder.setMessage(getResources().getString(R.string.send_toRCBCenter_event1)+"\n"+ getResources().getString(R.string.event_log)+"(#"+id+")"+getResources().getString(R.string.send_toRCBCenter_event3)+"\n"
				+getResources().getString(R.string.receive_email_address)+" "+MainActivity.mPrefDB.Get_recv_email());
		dialogBuilder.setPositiveButton(getResources().getString(R.string.transmit),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton)
					{

						if (!NcLibrary.isNetworkOnline(mContext))
						{
							EventDBOper mEventDB = new EventDBOper();
							mEventDB.OpenDB();
							EventData eventdata = DBMng.GetInst(mContext).loadReachBackDB(index);
							mEventDB.EndDB();
							eventdata.reachBackSuccess = false;

							if(DBMng.GetInst(mContext).updateReachBack(eventdata.idx, eventdata.reachBackPic, eventdata.reachBackXml, eventdata.reachBackSuccess+""))
							{
								if(chkFailedDB.isChecked())
								{
									arr = new ArrayList<EventData>();
									LoadDBList(false,currentPage);
								}
								else
								{
									arr = new ArrayList<EventData>();
									LoadDBList(true,currentPage);
								}
							}
							NcLibrary.Show_Dlg1(ReachBackListActivity.this.getResources().getString(R.string.internet_not).toString(), ReachBackListActivity.this);

						}
						else
						{
							EventData reachBack = null;
							reachBack = NcLibrary.reSendEmail( true, mContext,index, true);
							if(reachBack!=null)
							{
								if(DBMng.GetInst(mContext).updateReachBack(reachBack.idx, reachBack.reachBackPic, reachBack.reachBackXml, reachBack.reachBackSuccess+""))
								{
									if(chkFailedDB.isChecked())
									{
										arr = new ArrayList<EventData>();
										LoadDBList(false,currentPage);
									}
									else
									{
										arr = new ArrayList<EventData>();
										LoadDBList(true,currentPage);
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
		//msgView.setTextSize(15);


	}

	public void deleteDlg(final int _id, final String pic, final String xml, final String date)
	{

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ReachBackListActivity.this);
		dialogBuilder.setTitle("리치백 로그");
		dialogBuilder.setMessage("리치백 로그를 삭제하시겠습니까");
		dialogBuilder.setPositiveButton("삭제",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton)
					{

						if(DBMng.GetInst(mContext).deleteReachBack(_id))
						{
							if(!pic.equals("") || pic !=null)
							{
								File f2d = new File(pic);
								f2d.delete();
							}

							if(!xml.equals("") || xml !=null)
							{
								File f2d = new File(xml);
								f2d.delete();
							}

							if(chkFailedDB.isChecked())
							{
								LoadDBList(false,currentPage);
							}
							else
							{
								LoadDBList(true,currentPage);
							}
						}

					}
				});
		dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();


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
		if(!chkFailedDB.isChecked())
		{
			LoadDBList(true,currentPage);
		}

		if(chkFailedDB.isChecked() && arr.size()!=0)
		{
			Toast.makeText(ReachBackListActivity.this, getResources().getString(R.string.eventlog_load_failed),Toast.LENGTH_SHORT).show();
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
