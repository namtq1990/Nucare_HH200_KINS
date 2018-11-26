
package android.HH100;

import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.IDspectrumActivity.Check;
import android.HH100.Identification.Isotope;
import android.HH100.LogActivity.LogTabActivity;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.MainActivity.Media;
import android.HH100.Structure.EventData;
import android.HH100.Structure.MediaScanner;
import android.HH100.Structure.NcLibrary;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.moyosoft.exchange.Exchange;
import com.moyosoft.exchange.ExchangeServiceException;
import com.moyosoft.exchange.mail.ExchangeMail;

import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import Debug.Version;

public class EventListActivity_1 extends ListActivity {
	private static final int CHOOSE_EVENT_FILE = 234124;

	private Context mContext;
	private ArrayList<String> mEventID,mDate,mDate1,mAcqTime,mId,mGamma,mTimer,mTimer1,mManual_ID,mFavorite_Checked,mEndTime,mStartTime,
										mSourceName,mLatitude,mLongitude,mDoserate_S,mConfidence_Level,mComment;
/*	private ArrayList<String> mEventID = new ArrayList<String>();
	private ArrayList<String> mDate = new ArrayList<String>();
	private ArrayList<String> mDate1 = new ArrayList<String>();

	private ArrayList<String> mAcqTime = new ArrayList<String>();
	private ArrayList<String> mId = new ArrayList<String>();
	private ArrayList<String> mGamma = new ArrayList<String>();
	private ArrayList<String> mTimer = new ArrayList<String>();

	private ArrayList<String> mTimer1 = new ArrayList<String>();
	private ArrayList<String> mManual_ID = new ArrayList<String>();

	private ArrayList<String> mFavorite_Checked = new ArrayList<String>();

	private ArrayList<String> mEndTime = new ArrayList<String>();
	private ArrayList<String> mStartTime = new ArrayList<String>();
	private ArrayList<String> mSourceName = new ArrayList<String>();

	private ArrayList<String> mLatitude = new ArrayList<String>();
	private ArrayList<String> mLongitude = new ArrayList<String>();

	private ArrayList<String> mDoserate_S = new ArrayList<String>();
	private ArrayList<String> mConfidence_Level = new ArrayList<String>();

	private ArrayList<String> mComment = new ArrayList<String>();
*/
	public static int mSelPositioin = 0;

	public static EventData EventLog = null;
	public static Vector<EventData> mAllLog = null;
	MyArrayAdapter mEventArray;
	static ListView mEventList = null;
	ProgressDialog mPrgDlg;

	boolean menuBtnClick = false;

	public static Activity EventListActivity;
	public static String Path = "";

	// 다이얼로그창
	AlertDialog alert1;

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 0:
				Show_Dlg(getResources().getString(R.string.email_transmit_success).toString());
				break;
			case 1:
				Show_Dlg(getResources().getString(R.string.email_transmit_failed).toString());
				break;
			case 2:
				Show_Dlg(getResources().getString(R.string.email_info_fail).toString());
				break;
			case 3:
				Show_Dlg(getResources().getString(R.string.internet_not).toString());
				break;
			case 4:
				Show_Dlg("데이터 저장중에 오류가 발생하였습니다.\n 설정-이벤트로그 메뉴에서 전송 부탁드립니다.");
				break;
			case 5:
				Show_Dlg("사진파일 저장중 오류가 발생하여 \n사진파일없이 리치백서비스 실행되었습니다.\n사진파일 첨부 원할 시 : 설정-이벤트로그 메뉴에서 전송 부탁드립니다.");
				break;
			default:
				break;
			}
		};
	};

	// Pos Global

	int pos1, pos2;


	@Override
	protected void onResume()
	{

		MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
		LoadDBList();
		super.onResume();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.database);
		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		mContext = this;
		mPrgDlg = new ProgressDialog(mContext);
		mPrgDlg.setIndeterminate(true);
		mPrgDlg.setCancelable(false);

		MainActivity.ACTIVITY_STATE = Activity_Mode.EVENTLOG_LIST_MAIN;

		// Hold Screen rotate

		ListView lv = getListView();
		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
				pos1 = pos;
				pos2 = Integer.valueOf(mEventID.get(pos)) - 1;
				if(Version.IsKinsVersion)
				{

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity_1.this);
				dialogBuilder.setTitle(getResources().getString(R.string.transmit_N42));
				dialogBuilder.setMessage(getResources().getString(R.string.send_toRCBCenter_event));
				dialogBuilder.setPositiveButton(getResources().getString(R.string.transmit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton)
							{

								if (!NcLibrary.isNetworkOnline(mContext))
								{
									mHandler.sendEmptyMessage(3);
									dialog.dismiss();
									return;
								}
								else
								{

								AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity_1.this);
								dialogBuilder.setTitle(getResources().getString(R.string.transmit_N42));
								dialogBuilder.setMessage(getResources().getString(R.string.msg_send_attach_pictures));
								dialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int whichButton) {
												PhotoExcute();

											}
										});
								dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int whichButton) {

												// ReachBackNotPhotoExcute(pos2);
												if (Version.IsKinsVersion) {
													//NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler, false,false,"");
													//NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler, false,false);
												}

											}
										});
								dialogBuilder.setCancelable(false);
								dialogBuilder.show();

								}
							}
						});

				dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), null);
				dialogBuilder.setCancelable(false);
				dialogBuilder.show();
				}

				//bnc
				else {
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity_1.this);
					dialogBuilder.setTitle(getResources().getString(R.string.transmit_N42));
					dialogBuilder.setMessage(getResources().getString(R.string.send_toRCBCenter_event));
					dialogBuilder.setPositiveButton(getResources().getString(R.string.transmit),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {

									//NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler, false,false);
								//	NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler, false,false,"");
								}
							});
					dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), null);
					dialogBuilder.setCancelable(false);
					dialogBuilder.show();
				}

				return true;
			}

		});

	}

	public void LoadDBList()
	{
		mEventID = new ArrayList<String>();
		mDate = new ArrayList<String>();
		mDate1 = new ArrayList<String>();
		mAcqTime = new ArrayList<String>();
		mId = new ArrayList<String>();
		mGamma = new ArrayList<String>();
		mTimer = new ArrayList<String>();
		mTimer1 = new ArrayList<String>();
		mManual_ID = new ArrayList<String>();
		mFavorite_Checked = new ArrayList<String>();
		mEndTime = new ArrayList<String>();
		mStartTime = new ArrayList<String>();
		mSourceName = new ArrayList<String>();
		mLatitude = new ArrayList<String>();
		mLongitude = new ArrayList<String>();
		mDoserate_S = new ArrayList<String>();
		mConfidence_Level = new ArrayList<String>();
		mComment = new ArrayList<String>();

		EventDBOper mEventDB = new EventDBOper();
		mEventDB.OpenDB();
		mAllLog = mEventDB.Load_ALL_Event();
		mEventDB.EndDB();
		mEventDB = null;

		if (mAllLog != null) {
			for (int i = mAllLog.size() - 1; i >= 0; i--) {
				mDate.add(mAllLog.get(i).Event_Date);
				mAcqTime.add(String.valueOf(mAllLog.get(i).MS.Get_AcqTime()));
				mGamma.add(mAllLog.get(i).Doserate_AVGs);
				mTimer.add(mAllLog.get(i).StartTime);
				mManual_ID.add((mAllLog.get(i).Event_Detector));// .IsManualID==true)?"MANUAL
																// ID":"");

				mEventID.add(String.valueOf(i + 1));

				// radreponder value Add Part

				Vector<Isotope> Id = mAllLog.get(i).Detected_Isotope;
				String temp = "";
				String temp2 = "";
				String temp3 = "";
				String temp4 = "";

				if (Id == null || Id.size() == 0) {
					temp = "None";
					temp2 = "None";
					temp3 = "None";
					temp4 = "None";
				} else {
					for (int k = 0; k < Id.size(); k++) {
						String[] DoseRate_S = Id.get(k).DoseRate_S.split(" ");
						if (k == Id.size() - 1) {

							temp += Id.get(k).isotopes;
							temp2 += DoseRate_S[0];
							temp3 += Double.toString(Id.get(k).Confidence_Level);
							temp4 += Id.get(k).isotopes;
							break;

						} else {
							temp += Id.get(k).isotopes + ", ";
							temp2 += DoseRate_S[0] + ",";
							temp3 += Double.toString(Id.get(k).Confidence_Level) + ",";
							temp4 += Id.get(k).isotopes + ",";
						}

					}
				}
				mId.add(temp);
				mDoserate_S.add(temp2);
				mConfidence_Level.add(temp3);
				mSourceName.add(temp4);

				mComment.add(mAllLog.get(i).Comment);

				mStartTime.add(mAllLog.get(i).Event_Date + " " + mAllLog.get(i).StartTime);

				mEndTime.add(mAllLog.get(i).Event_Date + " " + mAllLog.get(i).EndTime);

				mLatitude.add(Double.toString(mAllLog.get(i).GPS_Latitude));
				mLongitude.add(Double.toString(mAllLog.get(i).GPS_Longitude));

				try {
					if ((mAllLog.get(i).Favorite_Checked).equals("null") || mAllLog.get(i).Favorite_Checked == null  || mAllLog.get(i).Favorite_Checked.equals("false;")|| mAllLog.get(i).Favorite_Checked.equals("false"))
					{
						mFavorite_Checked.add(Check.Favorite_False);
					} else
					{
						mFavorite_Checked.add(Check.Favorite_True);
					//	mFavorite_Checked.add((mAllLog.get(i).Favorite_Checked));
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		}

		for (int i = 0; i < mDate.size(); i++) {
			String mDateStr = mDate.get(i);
			String[] mDateArray = mDateStr.split("-");

			mDate1.add(mDateArray[0] + " - " + mDateArray[1] + " - " + mDateArray[2]);
		}

		for (int i = 0; i < mTimer.size(); i++) {

			String[] mTimerArray = mTimer.get(i).split(":");

			int mTimerInt = Integer.parseInt(mTimerArray[0]);
			if (mTimerInt < 12) {

				mTimer1.add("    " + mTimerArray[0] + " : " + mTimerArray[1] + " : " + mTimerArray[2] + " AM");

			} else if (mTimerInt == 12) {

				mTimer1.add("    " + mTimerArray[0] + " : " + mTimerArray[1] + " : " + mTimerArray[2] + " PM");

			} else if (mTimerInt > 12) {

				mTimerInt = mTimerInt - 12;

				mTimer1.add(
						"    " + Integer.toString(mTimerInt) + " : " + mTimerArray[1] + " : " + mTimerArray[2] + " PM");

			}

		}
		new MediaScanner(this).Start_MediaScan();
		mEventArray = new MyArrayAdapter(this);
		setListAdapter(mEventArray);
		EventListActivity = this;

		// MainActivity.SendU4AA();
	}

	private void Show_Dlg(String Message) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity_1.this);
		// dialogBuilder.setTitle(Title);
		dialogBuilder.setMessage(Message);
		dialogBuilder.setNegativeButton("OK", null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (resultCode == RESULT_OK) {

			// timetask 중지 관련

			if (requestCode == 1) {

				// ReachBackPhotoExcute(pos2);
				if (Version.IsKinsVersion)
				{
					//NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler, true, false, Path);
					//NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler, true, false);
					Path = "";
				}/* else {
					NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler);
				}*/
			}

		}

		if (data == null) {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}

		String str = data.getDataString();
		str = str.replace("file://", "");
		str = str.replace("%3A", ":");

		try {
			if (str.matches(".*.xml") == false)
				throw new SAXException();

			EventLog = NcLibrary.Event_XML.ReadXML_ANSI42(str);

			Intent intent = new Intent(mContext, LogTabActivity.class);
			startActivity(intent);
		} catch (SAXException e1) {
			Show_Dlg(getResources().getString(R.string.not_event_file));
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// --
		super.onActivityResult(requestCode, resultCode, data);
	}

	public boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return status;

	}

	public int pow(int x, int y) { // �젣怨� 怨꾩궛
		int result = 1;
		for (int i = 0; i < y; i++) {
			result *= x;
		}
		return result;
	}

	public String SvToString(double nSv, boolean point) { // �닽�옄�삎 �떆蹂댄듃 媛믪쓣
															// string�쑝濡�

		DecimalFormat format = new DecimalFormat();
		String unit = "Sv";
		double value = 1;

		if (point == true) {
			if (nSv < pow(10, 3)) {
				value = nSv;
				unit = "nSv";
			} else if (nSv >= pow(10, 3) & nSv < pow(10, 6)) {
				value = (nSv * 0.001);
				unit = "uSv";
			} else if (nSv >= pow(10, 6) & nSv < pow(10, 9)) {
				value = (nSv * 0.000001);
				unit = "mSv";
			} else if (nSv > pow(10, 9)) {
				value = (nSv * 0.000000001);
				unit = "Sv";
			}
		} else {
			if (nSv < pow(10, 3))
				return (long) nSv + "nSv";
			else if (nSv >= pow(10, 3) & nSv < pow(10, 6))
				return (long) (nSv * 0.001) + "uSv";
			else if (nSv >= pow(10, 6) & nSv < pow(10, 9))
				return (long) (nSv * 0.000001) + "mSv";
			else if (nSv > pow(10, 9))
				return (long) (nSv * 0.000000001) + "Sv";
		}

		format.applyLocalizedPattern("0.##");

		return format.format(value) + unit;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		mSelPositioin = Integer.valueOf(mEventID.get(position)) - 1;

		EventLog = mAllLog.get(mSelPositioin);

		Intent intent = new Intent(mContext, LogTabActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();

	}
	// ArrayAdapter�뿉�꽌 �긽�냽諛쏅뒗 而ㅼ뒪�� ArrayAdapter

	private Account Get_Gmail_account() {
		Account result = null;

		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(mContext).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				result = account;

			}
		}

		return result;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	class MyArrayAdapter extends ArrayAdapter {

		Context context;

		MyArrayAdapter(Context context) {

			super(context, R.layout.database_row, mDate);
			this.context = context;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = ((Activity) context).getLayoutInflater();

			View row = inflater.inflate(R.layout.database_row, null);

			TextView EventNum = (TextView) row.findViewById(R.id.Event_Num);
			EventNum.setText("  #" + mEventID.get(position));

			TextView ManualID = (TextView) row.findViewById(R.id.Manual_ID);
			ManualID.setText(mManual_ID.get(position));

			TextView incharge = (TextView) row.findViewById(R.id.location);
			incharge.setText(getResources().getString(R.string.alarm_duration) + " : " + mAcqTime.get(position) + " "
					+ getResources().getString(R.string.sec) + "   ");

			TextView value = (TextView) row.findViewById(R.id.value);
			value.setText(getResources().getString(R.string.avg_doserate) + " : " + mGamma.get(position) + "   ");

			/*
			 * String mDateStr = mDate.get(position); String[] mDateArray =
			 * mDateStr.split("-");
			 */

			TextView date = (TextView) row.findViewById(R.id.date);
			date.setText(mDate1.get(position));

			TextView date_time = (TextView) row.findViewById(R.id.date_time);
			date_time.setText(mTimer1.get(position));

			TextView location = (TextView) row.findViewById(R.id.incharge);
			location.setText(getResources().getString(R.string.radionuclide_id) + " : " + mId.get(position) + "   ");

			TextView FavoriteCheck = (TextView) row.findViewById(R.id.Add_Favorite_Txt);
			if (mFavorite_Checked.get(position).equals(Check.Favorite_True))
			{
				FavoriteCheck.setVisibility(View.VISIBLE);
			}
			else
			{
				FavoriteCheck.setVisibility(View.INVISIBLE);
			}

/*			try {
				if (mFavorite_Checked.get(position).equals(Check.Favorite_True)) {
					FavoriteCheck.setVisibility(View.VISIBLE);

				} else {

				}
			} catch (Exception e) {
				// TODO: handle exception
			}
*/
			// TextView Detector = (TextView)row.findViewById(R.id.Detector);
			// Detector.setText(mTimer.get(position));

			return row;
		}

	};

	@Override

	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {

			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity_1.this);
			dialogBuilder.setTitle(getResources().getString(R.string.transmit_N42));
			dialogBuilder.setMessage(getResources().getString(R.string.send_toRCBCenter_event));
			dialogBuilder.setPositiveButton(getResources().getString(R.string.transmit),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							// ReachBackNotPhotoExcute(pos2);
							if (Version.IsKinsVersion) {
								//NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler, false, false,"");
								//NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler, false, false);
							} /*else {
								NcLibrary.SendEmail(mAllLog.get(pos2), mContext, mHandler);
							}*/

						}
					});
			dialogBuilder.setNegativeButton("Cancel", null);
			dialogBuilder.setCancelable(false);
			dialogBuilder.show();
			return true;
		}

		// Disable Radresponder Function

		/*
		 * if (id == R.id.action_settings2) {
		 *
		 * Intent intent = new Intent(EventListActivity.this,
		 * RadresponderActivity.class); intent.putExtra("GPS_Latitude",
		 * mLatitude.get(pos1)); intent.putExtra("GPS_Longitude", mLongitude.get(pos1));
		 *
		 * String[] mGamma1 = mGamma.get(pos1).split(" ");
		 *
		 * intent.putExtra("Doserate_AVGs", mGamma1[0]); intent.putExtra("Date",
		 * mEndTime.get(pos1)); startActivity(intent);
		 *
		 * return true; }
		 */

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;

		super.onDestroy();
	}

	public void CreateCsvFile() {

		String[] mGamma1 = mGamma.get(pos1).split(" ");
		String mDoserate = "";
		if (mGamma1[1].equals("uSv/h")) {
			double d = Double.valueOf(mGamma1[0]).doubleValue() / 100;
			mDoserate = String.format("%.6f", d);
		}
		if (mGamma1[1].equals("urem/h")) {
			double d = Double.valueOf(mGamma1[0]).doubleValue() / 10000;
			mDoserate = String.format("%.6f", d);
		}

		if (mGamma1[1].equals("mSv/h")) {
			double d = Double.valueOf(mGamma1[0]).doubleValue() * 10;
			mDoserate = String.format("%.6f", d);
		}

		if (mGamma1[1].equals("Sv/h")) {
			double d = Double.valueOf(mGamma1[0]).doubleValue() * 10000;
			mDoserate = String.format("%.6f", d);
		}
		String mGPS_Latitude = String.format("%.6f", mAllLog.get((mAllLog.size() - 1) - pos1).GPS_Latitude);
		String mGPS_Longitude = String.format("%.6f", mAllLog.get((mAllLog.size() - 1) - pos1).GPS_Longitude);
		String mDate = DateChange();

		String[] Isotope = mId.get(pos1).split(",");
		String mIsotope = "";
		for (int i = 0; i < Isotope.length; i++) {

			mIsotope += Isotope[i] + " ";
		}

		String mCPM = String.valueOf(((int) mAllLog.get((mAllLog.size() - 1) - pos1).MS.Get_AvgCPS()
				+ (int) mAllLog.get((mAllLog.size() - 1) - pos1).AvgFillCps) * 60);

		String mComment = mAllLog.get((mAllLog.size() - 1) - pos1).Comment;

		String enc = new OutputStreamWriter(System.out).getEncoding();
		try {

			String head = " , Comment, DATE, Background_CPM, Latitude , Longitude, ISOTOPE, mR/hr. w/c \r\n";

			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(GetCsvPath()), "MS949"));
			writer.write(head);

			String Row = "\r\n" + " " + "," + mComment + "," + mDate + ", " + mCPM + ", " + mGPS_Latitude + ","
					+ mGPS_Longitude + "," + mIsotope + "," + mDoserate;
			// String secondRow = "괴물, 스릴있는 공포 가족 영화\r\n";
			writer.write(Row);

			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

	}

	private String GetCsvPath() {

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EventDB.csv";

		return path;
	}

	private String DateChange() {

		String mDateStr = mDate.get(pos1);
		String[] mDateArray = mDateStr.split("-");

		String mDateSub = mDateArray[2] + "/" + mDateArray[1] + "/" + mDateArray[0];

		return mDateSub;
	}

	public String CurrentDate() {
		Date date = new Date();
		Format formatter;
		formatter = new SimpleDateFormat("dd-MM-yyyy"); // d는 day 이런식
		String mDateStr = formatter.format(date);

		return mDateStr;
	}

	public void ReachBackPhotoExcute(final int Pos) 
	{

		mPrgDlg.setTitle(getResources().getString(R.string.transmit_N42));
		mPrgDlg.setMessage("Sending...");
		mPrgDlg.show();
		try {
			Thread thread = new Thread() {

				@Override
				public void run() {

					super.run();

					if (isNetworkOnline() == false) {
						mHandler.sendEmptyMessage(3);
						mPrgDlg.dismiss();
						return;
					}

					PreferenceDB pref = new PreferenceDB(EventListActivity_1.this.mContext);
					if (pref.Get_sender_email() == "" | pref.Get_sender_email() == null) {
						mHandler.sendEmptyMessage(2);
						mPrgDlg.dismiss();
						return;
					}

					/*
					 * String sender = pref.Get_sender_email(); String sender_pw =
					 * pref.Get_sender_pw(); String sender_server = pref.Get_sender_Server(); String
					 * sender_port = pref.Get_sender_Port(); String recv_mail =
					 * pref.Get_recv_email();
					 */

					String sender = pref.Get_sender_email();
					String sender_pw = pref.Get_sender_pw();
					String recv_mail = pref.Get_recv_email();
					String server = pref.Get_sender_Server();
					// Email 형식으로 소스코드 변경 시작

					/*
					 * Mail m = new Mail(sender, sender_pw, sender_server, sender_port);
					 * 
					 * // Array of emailIds where you want to // sent String[] toArr = new
					 * String[1]; toArr[0] = recv_mail; m.setTo(toArr);
					 * 
					 * // Your emailid(from) m.setFrom(sender); m.setSubject(
					 * "SAM III PeakAbout - Event Data"); m.setBody( "From SAM III PeakAbout");
					 */

					Exchange exchange;
					ExchangeMail exMail = null;
					try {
						exchange = new Exchange(server, sender, sender_pw, null, false);
					} catch (Exception e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(2);
						return;
					}
					File n42 = null;
					try {

						exMail = exchange.createMail();
						exMail.setToRecipient(recv_mail);
						exMail.setSubject("SAM III PeakAbout - Event Data");
						exMail.setBody("From " + mAllLog.get(Pos).mInstrument_Name);

						// String FileName1 = mAllLog.get(Pos).Event_Date + "_"
						// + mAllLog.get(Pos).StartTime + ".png";
						// String FileName2 = Media.FolderPath + "/" +
						// FileName1;

						/*
						 * m.addAttachment(FileName2, mAllLog.get(Pos).Event_Date + "_" +
						 * mAllLog.get(Pos).StartTime + ".png"); m.addAttachment( NcLibrary.Event_XML
						 * .WriteXML_toANSI42(mAllLog.get(Pos), mAllLog.get(Pos).Event_Date + "_" +
						 * mAllLog.get(Pos).StartTime + "(" + mAllLog.get(Pos).Instrument_SerialNumber +
						 * ").xml"), "EventData.xml");
						 */
						String name = pref.Get_Last_Cntd_User();
						n42 = new File(NcLibrary.Event_XML.WriteXML_toANSI42(mAllLog.get(Pos),
								mAllLog.get(Pos).Event_Date + "_" + mAllLog.get(Pos).StartTime + "("
										+ mAllLog.get(Pos).mInstrument_Name + ").xml",
								false, name));

						String FileName1 = mAllLog.get(Pos).Event_Date + "_" + mAllLog.get(Pos).StartTime + ".png";
						String FileName2 = Media.FolderPath + "/" + FileName1;

						exMail.getAttachments().add(n42);
						n42 = new File(FileName2);

						exMail.getAttachments().add(n42);

					} catch (ExchangeServiceException e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(1);
						e.printStackTrace();
						return;
					} catch (IOException e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(1);
						e.printStackTrace();
						return;
					}

					try {
						if (exMail != null)
							exMail.send();
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(0);

					} catch (Exception e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(1);
					}

					n42.delete();
					/*
					 * catch (Exception e1) { // TODO Auto-generated catch block
					 * e1.printStackTrace(); }
					 * 
					 * try { if (m.send()) {
					 * 
					 * mPrgDlg.dismiss(); mHandler.sendEmptyMessage(0);
					 * Log.v("Forgot Password mail", "Success");
					 * 
					 * } else { mPrgDlg.dismiss(); mHandler.sendEmptyMessage(1);
					 * Log.v("Forgot Password mail", "Not Success"); } } catch (Exception e) {
					 * mPrgDlg.dismiss(); mHandler.sendEmptyMessage(2); Log.e("MailApp",
					 * "Could not send email", e); }
					 */

				}

			};

			thread.start();

			// Email 형식으로 소스코드 변경 종료
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void ReachBackNotPhotoExcute(final int Pos) {

		mPrgDlg.setTitle(getResources().getString(R.string.transmit_N42));
		mPrgDlg.setMessage("Sending...");
		mPrgDlg.show();
		try {
			Thread thread = new Thread() {
				@Override
				public void run() {

					super.run();

					if (isNetworkOnline() == false) {
						mHandler.sendEmptyMessage(3);
						mPrgDlg.dismiss();
						return;
					}

					PreferenceDB pref = new PreferenceDB(EventListActivity_1.this.mContext);
					if (pref.Get_sender_email() == "" | pref.Get_sender_email() == null) 
					{
						mHandler.sendEmptyMessage(2);
						mPrgDlg.dismiss();
						return;
					}

					/*
					 * String sender = pref.Get_sender_email(); String sender_pw =
					 * pref.Get_sender_pw(); String sender_server = pref.Get_sender_Server(); String
					 * sender_port = pref.Get_sender_Port(); String recv_mail =
					 * pref.Get_recv_email();
					 */

					String sender = pref.Get_sender_email();
					String sender_pw = pref.Get_sender_pw();
					String recv_mail = pref.Get_recv_email();
					String server = pref.Get_sender_Server();
					// Email 형식으로 소스코드 변경 시작

					/*
					 * Mail m = new Mail(sender, sender_pw, sender_server, sender_port);
					 * 
					 * // Array of emailIds where you want to // sent String[] toArr = new
					 * String[1]; toArr[0] = recv_mail; m.setTo(toArr);
					 * 
					 * // Your emailid(from) m.setFrom(sender); m.setSubject(
					 * "SAM III PeakAbout - Event Data"); m.setBody( "From SAM III PeakAbout");
					 */

					Exchange exchange;
					ExchangeMail exMail = null;
					try {
						exchange = new Exchange(server, sender, sender_pw, null, false);
					} catch (Exception e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(2);
						return;
					}
					File n42 = null;
					try {

						exMail = exchange.createMail();
						exMail.setToRecipient(recv_mail);
						exMail.setSubject("SAM III PeakAbout - Event Data");
						exMail.setBody("From " + mAllLog.get(Pos).mInstrument_Name);
						String name = pref.Get_Last_Cntd_User();
						n42 = new File(NcLibrary.Event_XML.WriteXML_toANSI42(mAllLog.get(Pos),
								mAllLog.get(Pos).Event_Date + "_" + mAllLog.get(Pos).StartTime + "("
										+ mAllLog.get(Pos).mInstrument_Name + ").xml",
								false,name));

						exMail.getAttachments().add(n42);

					} catch (ExchangeServiceException e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(1);
						e.printStackTrace();
						return;
					} catch (IOException e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(1);
						e.printStackTrace();
						return;
					}

					try {
						if (exMail != null)
							exMail.send();
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(0);

					} catch (Exception e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(1);
					}

					n42.delete();
					/*
					 * catch (Exception e1) { // TODO Auto-generated catch block
					 * e1.printStackTrace(); }
					 * 
					 * try { if (m.send()) {
					 * 
					 * mPrgDlg.dismiss(); mHandler.sendEmptyMessage(0);
					 * Log.v("Forgot Password mail", "Success");
					 * 
					 * } else { mPrgDlg.dismiss(); mHandler.sendEmptyMessage(1);
					 * Log.v("Forgot Password mail", "Not Success"); } } catch (Exception e) {
					 * mPrgDlg.dismiss(); mHandler.sendEmptyMessage(2); Log.e("MailApp",
					 * "Could not send email", e); }
					 */

				}

			};

			thread.start();

			// Email 형식으로 소스코드 변경 종료
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

/*	public void PhotoExcute() {
		MainActivity.ActionViewExcuteCheck = Activity_Mode.EXCUTE_MODE;
		File file;
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String FileName = getString(R.string.reach_back_filename);
		
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		String Today = (new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(date));
		//file = new File(Media.FolderPath, mAllLog.get(pos2).Event_Date + "_" + mAllLog.get(pos2).StartTime + ".png");
		file = new File(Media.FolderPath, Today + ".png");
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		Path = Today;
		// cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,600*600);
		startActivityForResult(cameraIntent, 1);

	}
*/
	public void PhotoExcute() {
		MainActivity.ActionViewExcuteCheck = Activity_Mode.EXCUTE_MODE;
		File file;
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String FileName = getString(R.string.reach_back_filename);
		file = new File(Media.FolderPath, mAllLog.get(pos2).Event_Date + "_" + mAllLog.get(pos2).StartTime + ".png");
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		// cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,600*600);
		startActivityForResult(cameraIntent, 1);

	}


}
