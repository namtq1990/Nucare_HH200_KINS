package android.HH100;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import NcLibrary.Coefficients;
import NcLibrary.NcMath;

import android.HH100.CameraUtil.VideoActivity;
import android.HH100.DB.DBMng;
import android.HH100.LogActivity.LogEventPic;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.MainActivity.Focus;
import android.HH100.MainActivity.Tab_Name;
import android.HH100.Control.Analysis_TopInfor;
import android.HH100.Control.GpsInfo;
import android.HH100.Control.GpsInfo2;
import android.HH100.Control.ProgressBar;
import android.HH100.Control.ScView_Ad;
import android.HH100.Control.SpectrumView;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.Identification.FindPeaksM;
import android.HH100.Identification.Isotope;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.LogActivity.LogPhotoTab.Media;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Structure.Detector;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import static android.HH100.MainActivity.Media.FolderPath;
import static android.HH100.Structure.NcLibrary.Separate_EveryDash3;
import static android.HH100.Structure.NcLibrary.hashMap;

public class IDspectrumActivity extends Activity implements View.OnTouchListener {
	private static final boolean D = MainActivity.D;
	private static final String TAG = "IDSpectrum";
	private static final String TAG_SEQ = "IDSpectrum_seq";
	private static final String TAG_MANUALID = "IDSpectrum_manual";

	public static final int MS_SEQUENCE_MODE = 532842;
	public static final int MS_MANUAL_ID = 532843;
	public static final int LIST_DRAW = 532844;

	public static final int FOCUSBTN = 53284;

	public static final int TEST = 532845;

	public static final String EXTRA_SPECTRUM = "extra_spc";
	public static final String EXTRA_BG_SPECTRUM = "extra_bg_spc";
	public static final String EXTRA_DETECTED_SOURCE = "extra_detected_source";
	public static final String EXTRA_MANUAL_ID_GOAL_TIME = "extra_mID_goalTime";
	public static final String EXTRA_MANUAL_ID_ADJUST_TIME = "extra_adjust_goalTime";
	public static final String EXTRA_SEQ_ACQTIME = "extra_acq_time";
	public static final String EXTRA_SEQ_REPEAT = "extra_repeat";
	public static final String EXTRA_SEQ_PAUSE_TIME = "extra_pause_time";
	public static final String EXTRA_SV_UNIT = "extra_sv_unit";
	public static final String ACTIVTY = "actvity";

	public static boolean SOURCE_ID_RESULT_MODE = false;

	private SpectrumView mSpectrumView;
	private Analysis_TopInfor mTopAnal_Info;

	public Spectrum mSPC = new Spectrum();

	private LinearLayout m_MainLayout;
	private LinearLayout m_AnalysisLayout;
	private ScView_Ad mAnalysisView;

	/// --Manual ID
	private ImageButton mManualID_TimeUP;
	private ImageButton mManualID_TimeDown;
	private TextView mManualID_Time;

	// -- Acq Time
	private int mManualID_GoalTime = 60;
	private int mManualID_Adjust_sec = 10;

	private int mSequence_acqTime = 0;
	private int mSequence_repeat_Goal = 0;
	private int mSequence_repeat_count = 0;

	private int mPauseTime = 5;// sec
	private int mPauseTime_ElapsedTime = 0;
	// --
	public boolean mIsSequenceMode = false;
	private boolean mIsSvUnit = true;

	private boolean mIsEvent = false;
	/// --End Manual ID
	public static boolean mIsManualID_mode = false;
	private Vector<Integer> mClassColor = new Vector<Integer>();

	private EventData m_EventData = null;
	public int mBtnDownCount = 0;
	ImageView filpperImgView;
	ViewFlipper flipper;

	int swicthCount = 0, id_result_menu_b_count = Activity_Mode.UN_EXCUTE_MODE;

	private int mPreTouchPosX;

	boolean filperMove = false;

	View IDspectrum, Iso_analysis;

	ScrollView scrollview1;

	private ProgressBar mProgBar = null;

	TextView ResultLocationInfoTxt, userInfoTxt, IdAcqTime, cpsTxt, totalCountTxt, acqTimeTxt, acqTimeTxt2,
			avg_Doserate_txt;

	Button removeEventTxt;

	LinearLayout LayoutA, LayoutB, LayoutC;

	EditText CommentEdit;

	public static Vector<EventData> mAllLog;

	public static int mFileNumber = 0;

	// video, photo declear

	public static String EXTRA_PHOTO_FILE_NAME = "Photo";
	public static String EXTRA_VIDEO_FILE_NAME = "Video";
	public static String EXTRA_EVENT_NUMBER = "EventNumber";

	public boolean IS_TAKE_PHOTO_AND_VIDEO = true;

	public static ListView mGallery = null;
	Vector<Bitmap> mThumnail = new Vector<Bitmap>();
	Vector<String> mPhoto = null;
	Vector<String> mVideo = null;
	TextView mMediaCnt = null;
	public static int mEventNumber;

	ProgressDialog mPrgDlg;

	public ListView m_lv = null;
	View m_lv1 = null;

	public static int rootFocusCnt = Focus.ID_RESULT_MENU_C;

	public static int checkMediaModeCount = Activity_Mode.UN_EXCUTE_MODE;

	public static int DoubleClickRock = Activity_Mode.EXCUTE_MODE;

	public static int idBottomSwicthCount = Focus.ID_RESULT_MENU_C_REMOVE_BTN;

	public boolean dlgTemp = true; // 0122 추가 리치백 다이얼로그가 이중으로 뜨는경우가 있어서 막기위함
	public boolean dbTemp = false; // 0802 디비 저장확인

	public static String Path = "";

	public interface Check {

		int IdResult_Checked = 0;
		int IdResult_Not_Checked = 1;
		int Result_Ok = -1;
		int Result_Not_Ok = -2;
		String ListNumber = "ListNumber";
		String ListValue = "ListValue";

		public String Favorite_False = "false";

		public String Favorite_True = "true";
	}

	int mGalleryListViewCurrentPosition = 0;

	Vector<String> mPhotoName = new Vector<String>();
	Vector<String> mVideoName = new Vector<String>();
	Vector<String> mRecoderName = new Vector<String>();
	Vector<String> mTotalTxt = new Vector<String>();
	Vector<String> mRecoder = new Vector<String>();

	FrameLayout mFrameLayout;

	// video, photo end

	GpsInfo2 mGpsInfo2;
	int mFirstSpec[] = new int[1024];

	TimerTask mRecTask;
	Timer mRecTimer;

	TextView idAcqTimeTxt, dateTimeTxt, dateTxt, EventId, INDTxt;

	//180802 Dspectrum result
	ArrayList<LogEventPic.LogGallery> eventGallery = new ArrayList<LogEventPic.LogGallery>();
	ArrayList<String> arrPhoto = new ArrayList<String>(); //IDspectrum result photo, video, record
	ArrayList<String> arrVideo = new ArrayList<String>();
	ArrayList<String> arrRecord = new ArrayList<String>();
	int galleryType  = -1; //1 : photh, 2: video, 3:record
	IDspectrumResultAdapter galleryAdapter;
	String fileName = "";

	//180823 reachback인지 eventdb인지 판별
	boolean reachBack = true;

	boolean result = false;

	public class MainBCRReceiver extends MainBroadcastReceiver {

		@Override
		public void onReceive(Context context, android.content.Intent intent) {

			try {
				String action = intent.getAction();
				switch (action) {
				case MSG_RECV_SPECTRUM:
					if (D)
						Log.d(TAG, "Receive Broadcast- Spectrum");
					Spectrum spc = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);

					mSpectrumView.Show_Info(true);

					if (true) {
						mHandler.obtainMessage(MS_MANUAL_ID, 0, 0, spc).sendToTarget();
					} else if (mIsSequenceMode) {
						if (mPauseTime_ElapsedTime >= mPauseTime) {
							mHandler.obtainMessage(MS_SEQUENCE_MODE, 0, 0, spc).sendToTarget();
						} else {
							mPauseTime_ElapsedTime += 1;
							Set_Info_OnSpectrumView_OnAnalysisView(mPauseTime_ElapsedTime, 0);
							mSpectrumView.invalidate();
							mTopAnal_Info.invalidate();
						}
					} else {
						mSPC.Accumulate_Spectrum(spc);
						mSpectrumView.SetChArray(mSPC);
						if (mIsEvent) {
							Set_Info_OnSpectrumView_OnAnalysisView(mSPC.Get_AcqTime(), spc.Get_TotalCount());
						} else {
							Set_Info_OnSpectrumView_OnAnalysisView(0, spc.Get_TotalCount());
						}
						mSpectrumView.invalidate();
						mTopAnal_Info.invalidate();
					}

					break;
				case MSG_RECV_NEUTRON:
					// int event_status =
					// intent.getIntExtra(DATA_EVENT_STATUS,Detector.EVENT_NONE);
					break;
				case MSG_EVENT:
					if (D)
						Log.d(TAG, "Receive Broadcast- Event");
					int event_status = intent.getIntExtra(DATA_EVENT_STATUS, Detector.EVENT_NONE);
					EventData eventdb = (EventData) intent.getSerializableExtra(DATA_EVENT);
					if (eventdb != null) {
						if (eventdb.Event_Detector.matches(EventData.EVENT_NEUTRON))
							break;

					}
					if (event_status == Detector.EVENT_BEGIN) {
						mSPC.ClearSPC();
						mIsEvent = true;
					} else if (event_status == Detector.EVENT_ING) {

						@SuppressWarnings("unchecked")
						Vector<Isotope> isos = (Vector<Isotope>) intent.getSerializableExtra(DATA_SOURCE_ID);
						Set_IdResult_toViews(isos);
						mIsEvent = true;

					} else if (event_status == Detector.EVENT_FINISH) {
						mAnalysisView.RemoveAll_IsotopeData();
						mSpectrumView.Clear_Found_Isotopes();

						mSpectrumView.invalidate();
						mAnalysisView.invalidate();

						mSPC.ClearSPC();
						mIsEvent = false;
					}

					break;

				case MSG_EN_CALIBRATION:
					Coefficients En_Coeff = (Coefficients) intent.getSerializableExtra(DATA_COEFFCIENTS);
					Coefficients Ch_Coeff = (Coefficients) intent.getSerializableExtra(DATA_CALIBRATION_PEAKS);

					mSPC.Set_Coefficients(En_Coeff.get_Coefficients());
					mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());

					if (D)
						Log.d(TAG, "Receive Broadcast- Recalibration (" + En_Coeff.ToString() + " || "
								+ Ch_Coeff.ToString() + ")");
					break;
				case MSG_REMEASURE_BG:
					Spectrum bg = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);

					if (D)
						Log.d(TAG, "Receive Broadcast- Remeasured background (" + bg.ToString() + ")");
					// mBG = bg;
					break;
				case MSG_DISCONNECTED_BLUETOOTH:
					mSpectrumView.Clear_Found_Isotopes();
					mSPC.ClearSPC();
					mSpectrumView.SetChArray(mSPC);
					mAnalysisView.RemoveAll_IsotopeData();

					if (D)
						Log.d(TAG, "bluetooth disconnected");
					break;

				case MSG_TAB_SIZE_MODIFY_FINISH:

					result = true;
					SourceIdResultInfo();
					End_ManualID();
					MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;

					break;

				case MSG_SOURCE_ID_RUNNING_START:

					MainActivity.tabHost.setCurrentTab(Tab_Name.MenualID);
					SetSourceIdRunningActivityMode();
					// MainActivity.ACTIVITY_STATE =
					// Activity_Mode.SOURCE_ID_RUNNING;
					onCreatePart();

					// DropDownAnimation();
					break;

				case MSG_SPEC_VIEWFILPPER:

					View_Filpper();

					break;
				case MSG_SOURCE_ID_TIMEDOWN:

					if (mBtnDownCount == 0) {

						TimeDown();
					}
					break;

				}

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	private MainBCRReceiver mMainBCR = new MainBCRReceiver();

	// 인플레이터 선언 부분

	LayoutInflater inflater;
	LinearLayout linearLayout, linearLayout1;

	Button startBtn;
	Context mContext;

	PreferenceDB mPrefDB;

	TextView IDAcqTimeTxt = null;

	RelativeLayout filperTouch = null;

	// SourceIdResultInfo

	CheckBox Favorite_Checkbox;

	Bitmap RecBitmap;

	ImageButton ReachBackBtn, CameraBtn, VideoBtn, VoiceBtn;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub5
		super.onCreate(savedInstanceState);

		NcLibrary.Write_ExceptionLog("IDSpectrum.java");

		mContext = this;

		mGpsInfo2 = new GpsInfo2(mContext);

		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

		IntentFilter filter = new IntentFilter();

		filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_START);

		filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

		filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_BACK);

		filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

	}

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			try {

				Spectrum spc = (Spectrum) msg.obj;
				PreferenceDB prefDB = new PreferenceDB(getApplicationContext());
				FindPeaksM FPM = new FindPeaksM();
				IsotopesLibrary IsoLib = new IsotopesLibrary(getApplicationContext());

				switch (msg.what) {
				case MS_SEQUENCE_MODE:

					if (m_EventData == null) {
						Log.i(TAG_SEQ, "SEQ: Start Sequence mode");
						m_EventData = new EventData();
						m_EventData.mInstrument_Name = MainActivity.mDetector.InstrumentModel_Name;
						m_EventData.Doserate_unit = (MainActivity.mDetector.IsSvUnit) ? Detector.DR_UNIT_SV
								: Detector.DR_UNIT_R;
						m_EventData.Event_Detector = "SM" + (mSequence_repeat_count + 1);
						m_EventData.MS.Set_Coefficients(spc.Get_Coefficients());
						m_EventData.MS.Set_StartSystemTime();
						m_EventData.mUser = MainActivity.mDetector.User;
						m_EventData.mLocation = MainActivity.mDetector.Location;

						/////////////////////////////////////////////////////////
						// 2018.02.14  Background spectrum adjustment using current calibration info.
						//double []FWHM_NaI2_2=new double [] {1.2707811254,-1.5464537062};
						double []FWHM_NaI2_2=MainActivity.mDetector.MS.getFWHM();
						double []mCoefficient= prefDB.Get_Cali_ABC_From_pref();

						// reload background spectrum
						MainActivity.mDetector.Real_BG.Set_MeasurementDate(prefDB.Get_BG_Date_From_pref());
						//MainActivity.mDetector.Real_BG.Set_Spectrum(prefDB.Get_BG_From_pref(), prefDB.Get_BG_MeasuredAcqTime_From_pref());

						int K40_Ch=NcLibrary.PeakAna(MainActivity.mDetector.Real_BG.ToInteger(), FWHM_NaI2_2,mCoefficient);

						int K40_New=(int)NcMath.ToPolynomial_FittingValue_AxisX1(1461.0, mCoefficient);

						//MainActivity.mDetector.Background_GainStabilization(K40_Ch, K40_New);
						if(K40_Ch>10)
						{
							double [] mSpc=new double [MainActivity.CHANNEL_ARRAY_SIZE];
							mSpc=NcMath.Background_GainStabilization(MainActivity.mDetector.Real_BG.ToDouble(),K40_Ch,K40_New);
							MainActivity.mDetector.Real_BG.Set_Spectrum(mSpc, MainActivity.mDetector.Real_BG.Get_AcqTime());
						}
						////////////////////////////////////////////////////////////

						m_EventData.BG.Set_Spectrum(MainActivity.mDetector.Real_BG);
						m_EventData.Set_StartTime();
						m_EventData.IsManualID = false;

						EventDBOper mEventDBOper = new EventDBOper();
						mEventDBOper.Set_Crytal_Info(prefDB.Get_CryStal_Type_Number_pref());
						m_EventData.MS.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);

						m_EventData.MS.setFWHM(mEventDBOper.Cry_Info.FWHM);
						m_EventData.MS.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
					}

					m_EventData.MS.Accumulate_Spectrum(spc);
					m_EventData.mNeutron.Set_CPS(MainActivity.mDetector.mNeutron.Get_CPS());
					if (m_EventData.Doserate_MAX < MainActivity.mDetector.Get_Gamma_DoseRate_nSV())
						m_EventData.Doserate_MAX = MainActivity.mDetector.Get_Gamma_DoseRate_nSV();
					m_EventData.Doserate_AVG += MainActivity.mDetector.Get_Gamma_DoseRate_nSV();

					// --

					prefDB = new PreferenceDB(getApplicationContext());
					//FPM = new FindPeaksM();
					IsoLib = new IsotopesLibrary(getApplicationContext());
					IsoLib.Set_LibraryName(prefDB.Get_Selected_IsoLibName());

					/*
					Vector<NcPeak> Peaks = FPM.Find_Peak(m_EventData.MS, m_EventData.BG);

					m_EventData.Detected_Isotope = IsoLib.Find_Isotopes_with_Energy(m_EventData.MS, m_EventData.BG,
							Peaks);
					 */
					
					/*..........................
					 * Hung.18.03.05
					 * Added Code to new algorithm
					 * 
					 */

					long starttime=System.currentTimeMillis();
					m_EventData.Detected_Isotope = IsoLib.Find_Isotopes_with_Energy(m_EventData.MS, m_EventData.BG);

					long endtime=System.currentTimeMillis();

				//	NcLibrary.SaveText("start,"+starttime+",end,"+endtime+"\n");

					if (m_EventData.MS.Get_AcqTime() <= 5 & m_EventData.Detected_Isotope.size() > 2) {
						Isotope iso1 = m_EventData.Detected_Isotope.get(0);
						Isotope iso2 = m_EventData.Detected_Isotope.get(1);

						m_EventData.Detected_Isotope.clear();
						m_EventData.Detected_Isotope.add(iso1);
						m_EventData.Detected_Isotope.add(iso2);
					}

					Log.i(TAG_SEQ, "SEQ: Measured, Acq.time - " + m_EventData.MS.Get_AcqTime());
					///////////////// �젙�웾 遺꾩꽍

					if (m_EventData.Detected_Isotope.isEmpty() == false)
						m_EventData.Detected_Isotope = NcLibrary.Quantitative_analysis(MainActivity.mDetector.MS,
								m_EventData.BG, m_EventData.Detected_Isotope, MainActivity.mDetector.IsSvUnit,
								MainActivity.mDetector.mPmtSurface, MainActivity.mDetector.mCrystal);

					Set_IdResult_toViews(m_EventData.Detected_Isotope);
					Set_Info_OnSpectrumView_OnAnalysisView(m_EventData.MS.Get_AcqTime(), spc.Get_TotalCount());
					mSpectrumView.SetChArray(m_EventData.MS);
					mSpectrumView.invalidate();
					mTopAnal_Info.invalidate();

					if (mSequence_acqTime <= m_EventData.MS.Get_AcqTime()) {

						Log.i(TAG_SEQ, "SEQ: wirte to DB, repeat cnt - " + (mSequence_repeat_count + 1));

						m_EventData.Set_EndEventTime();
						m_EventData.Doserate_AVG = m_EventData.Doserate_AVG / m_EventData.MS.Get_AcqTime();
						m_EventData.Neutron_AVG = m_EventData.mNeutron.Get_AvgCps();
						m_EventData.Neutron_MAX = m_EventData.mNeutron.Get_MaxCount();

						m_EventData.Doserate_AVGs = Double.toString(m_EventData.Doserate_AVG);
						m_EventData.Doserate_MAXs = Double.toString(m_EventData.Doserate_MAX);

						if (WriteEvent_toDB(m_EventData))
							Toast.makeText(getApplicationContext(), "#" + (mSequence_repeat_count + 1) + " saved in DB",
									Toast.LENGTH_SHORT).show();

						mAnalysisView.RemoveAll_IsotopeData();
						mSpectrumView.Clear_Found_Isotopes();

						mSPC.ClearSPC();
						mSpectrumView.SetChArray(mSPC);
						mSpectrumView.invalidate();
						mTopAnal_Info.invalidate();

						m_EventData = null;

						mSequence_repeat_count += 1;
						mPauseTime_ElapsedTime = 0;
						if (mSequence_repeat_count >= mSequence_repeat_Goal) {
							End_SequenceMode();
							// --===--
							Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
							intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_ON);
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
							// --===--
							Toast.makeText(getApplicationContext(), "Sequential Mode Completed", Toast.LENGTH_SHORT)
									.show();

							Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);
							intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_ON);
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
							Log.i(TAG_SEQ, "SEQ:  Success");
						} else {
							Set_SeqModeInfo(mSequence_repeat_count + 1);
						}

						Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);
						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
					}
					break;

				case MS_MANUAL_ID:

					if (m_EventData == null) {
						Log.i(TAG_MANUALID, "Manual ID: Start Manual ID");
						// Toast.makeText(getApplicationContext(),
						// "#"+(mSequence_repeat_count+1)+"
						// Start",Toast.LENGTH_SHORT).show();

						m_EventData = new EventData();
						m_EventData.mInstrument_Name = MainActivity.mDetector.InstrumentModel_Name;
						m_EventData.Doserate_unit = (MainActivity.mDetector.IsSvUnit) ? Detector.DR_UNIT_SV
								: Detector.DR_UNIT_R;
						m_EventData.Event_Detector = "Manual ID";
						m_EventData.MS.Set_Coefficients(spc.Get_Coefficients());
						m_EventData.MS.Set_StartSystemTime();
						m_EventData.mUser = MainActivity.mDetector.User;
						m_EventData.mLocation = MainActivity.mDetector.Location;
						
						
						
						/*..........................
						 * Hung.18.03.05
						 * Added Code to new algorithm
						 */

						/////////////////////////////////////////////////////////
						// 2018.02.14  Background spectrum adjustment using current calibration info.
						//double []FWHM_NaI2_2=new double [] {1.2707811254,-1.5464537062};
						/*
						m_EventData.BG.Set_Spectrum(MainActivity.mDetector.Real_BG);
						m_EventData.Set_StartTime();
						m_EventData.IsManualID = true;
						*/

						double []FWHM=MainActivity.mDetector.MS.getFWHM();
						double []mCoefficient= prefDB.Get_Cali_ABC_From_pref();

						MainActivity.mDetector.Real_BG.Set_MeasurementDate(prefDB.Get_BG_Date_From_pref());

						int K40_Ch=NcLibrary.PeakAna(MainActivity.mDetector.Real_BG.ToInteger(), FWHM, mCoefficient);

						int K40_New=(int)NcMath.ToPolynomial_FittingValue_AxisX1(1461.0, mCoefficient);

						//MainActivity.mDetector.Background_GainStabilization(K40_Ch, K40_New);
						if(K40_Ch>10)
						{
							double [] mSpc=new double [MainActivity.CHANNEL_ARRAY_SIZE];
							mSpc=NcMath.Background_GainStabilization(MainActivity.mDetector.Real_BG.ToDouble(),K40_Ch,K40_New);
							MainActivity.mDetector.Real_BG.Set_Spectrum(mSpc, MainActivity.mDetector.Real_BG.Get_AcqTime());
						}

						m_EventData.BG = MainActivity.mDetector.Real_BG;
						m_EventData.Set_StartTime();
						m_EventData.IsManualID = true;

						EventDBOper mEventDBOper = new EventDBOper();
						mEventDBOper.Set_Crytal_Info(prefDB.Get_CryStal_Type_Number_pref());
						m_EventData.MS.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);

						m_EventData.MS.setFWHM(mEventDBOper.Cry_Info.FWHM);
						m_EventData.MS.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
					}

					m_EventData.MS.Accumulate_Spectrum(spc);
					m_EventData.mNeutron.Set_CPS(MainActivity.mDetector.mNeutron.Get_CPS());
					if (m_EventData.Doserate_MAX < MainActivity.mDetector.Get_Gamma_DoseRate_nSV())
						m_EventData.Doserate_MAX = MainActivity.mDetector.Get_Gamma_DoseRate_nSV();
					m_EventData.Doserate_AVG += MainActivity.mDetector.Get_Gamma_DoseRate_nSV();
					// --
					prefDB = new PreferenceDB(getApplicationContext());

					IsoLib = new IsotopesLibrary(getApplicationContext());
					IsoLib.Set_LibraryName(prefDB.Get_Selected_IsoLibName());

					/*
					
					FPM = new FindPeaksM();
					Peaks = FPM.Find_Peak(m_EventData.MS, m_EventData.BG);
					m_EventData.Detected_Isotope = IsoLib.Find_Isotopes_with_Energy(m_EventData.MS, m_EventData.BG,
							Peaks);
					 */
					/*..........................
					 * Hung.18.03.05
					 * Added Code to new algorithm
					 */


					long starttime1=System.currentTimeMillis();
					m_EventData.Detected_Isotope = IsoLib.Find_Isotopes_with_Energy(m_EventData.MS, m_EventData.BG);
					long endtime1=System.currentTimeMillis();

					//NcLibrary.SaveText("start,"+starttime1+",end,"+endtime1+"\n");

					Log.i("asdasd", "remove iso c" + m_EventData.Detected_Isotope.size());
					if (m_EventData.MS.Get_AcqTime() <= 5 & m_EventData.Detected_Isotope.size() > 2) {
						Isotope iso1 = m_EventData.Detected_Isotope.get(0);
						Isotope iso2 = m_EventData.Detected_Isotope.get(1);

						m_EventData.Detected_Isotope.clear();
						m_EventData.Detected_Isotope.add(iso1);
						m_EventData.Detected_Isotope.add(iso2);
					}
					Log.i("asdasd", "remove iso after" + m_EventData.Detected_Isotope.size());
					///////////////// �젙�웾 遺꾩꽍

					if (m_EventData.Detected_Isotope.isEmpty() == false)
					{
//						m_EventData.Detected_Isotope = NcLibrary.Quantitative_analysis(MainActivity.mDetector.MS,
//								m_EventData.BG, m_EventData.Detected_Isotope, MainActivity.mDetector.IsSvUnit,
//								MainActivity.mDetector.mPmtSurface, MainActivity.mDetector.mCrystal);
						m_EventData.Detected_Isotope = NcLibrary.Quantitative_analysis(m_EventData.MS,
							m_EventData.BG, m_EventData.Detected_Isotope, MainActivity.mDetector.IsSvUnit,
							MainActivity.mDetector.mPmtSurface, MainActivity.mDetector.mCrystal);
					}
					Set_IdResult_toViews(m_EventData.Detected_Isotope);
					Set_Info_OnSpectrumView_OnAnalysisView(m_EventData.MS.Get_AcqTime(), spc.Get_TotalCount());
					mSpectrumView.SetChArray(m_EventData.MS);
					mSpectrumView.invalidate();
					mTopAnal_Info.invalidate();

					mManualID_TimeDown.setVisibility(View.VISIBLE);
					mManualID_TimeUP.setVisibility(View.VISIBLE);

					double Percent = ((double) m_EventData.MS.Get_AcqTime() / (double) mManualID_GoalTime) * 100.0;
					mProgBar.Set_Value(Percent);
					mProgBar.invalidate();

					if (m_EventData.MS.Get_AcqTime() >= mManualID_GoalTime) {

						Class<? extends EventData> a = m_EventData.getClass();

						String ac = m_EventData.toString();

						m_EventData.Set_EndEventTime();
						m_EventData.Doserate_AVG = m_EventData.Doserate_AVG / m_EventData.MS.Get_AcqTime();
						m_EventData.Neutron_AVG = m_EventData.mNeutron.Get_AvgCps();
						m_EventData.Neutron_MAX = m_EventData.mNeutron.Get_MaxCount();

						m_EventData.Doserate_AVGs = Double.toString(m_EventData.Doserate_AVG);
						m_EventData.Doserate_MAXs = Double.toString(m_EventData.Doserate_MAX);

						m_EventData.GPS_Latitude = mGpsInfo2.GetLat();
						m_EventData.GPS_Longitude = mGpsInfo2.GetLon();

						mAnalysisView.RemoveAll_IsotopeData();
						mSpectrumView.Clear_Found_Isotopes();

						mSPC.ClearSPC();
						mSpectrumView.SetChArray(mSPC);
						mSpectrumView.invalidate();
						mTopAnal_Info.invalidate();

						// m_EventData = null;

						if(WriteEvent_toDB(m_EventData))
						{
							dbTemp = true;
							Toast.makeText(getApplicationContext(), "이벤트를 기록했습니다", Toast.LENGTH_LONG).show();
						}

						Set_Invisible_ManualID_Contol(true);

						Log.i(TAG_MANUALID, "Manual ID: End Manual ID");

						mProgBar.Set_Value(0);

						mProgBar.invalidate();

						Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT);

						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

						LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

						IntentFilter filter = new IntentFilter();

						filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_HW_BACK);

						filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);

						filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

						filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_START);

						LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

					}

					break;

				case LIST_DRAW:
					dlgTemp = true;
					ListViewSizeChange();

					break;

				case FOCUSBTN:
					Log.d("time:", "Log2");
					CameraBtn = (ImageButton) findViewById(R.id.CameraBtn);
					CameraBtn.setFocusable(true);
					CameraBtn.setFocusableInTouchMode(true);
					CameraBtn.requestFocus();

					break;

				case TEST:

					Toast.makeText(getApplicationContext(), "제거", Toast.LENGTH_LONG).show();

					break;
				}
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	};

	private boolean WriteEvent_toDB(EventData event) {
		// if(event.MS.Get_AcqTime() <=3) return false;

		event.GPS_Latitude = mGpsInfo2.GetLat();
		event.GPS_Longitude = mGpsInfo2.GetLon();

		event.mColumn_Version = EventDBOper.DB_verion;

		EventDBOper eventDB = new EventDBOper();

		int aaa3 = eventDB.GetEventCount();

		if (eventDB.WriteEvent_OnDatabase(event))
		{

/*			try {
				File eventFile = new File(Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER + "/"
						+ EventDBOper.DB_FILE_NAME + ".sql");
				if (eventFile.isFile())
					new SingleMediaScanner(getApplicationContext(), eventFile);
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}*/
			return true;
		}
		else
		{
			Toast.makeText(getApplicationContext(), "이벤트 기록에 실패했습니다", Toast.LENGTH_LONG).show();
			return false;
		}

		// Start_MediaScan();

	}

	private void End_ManualID() {
		try {
			// mManualID_Adjust_sec = mPrefDB.Get_ManualID_AdjustTime();

			mSPC.ClearSPC();
			mIsManualID_mode = false;
			mManualID_GoalTime = getIntent().getIntExtra(EXTRA_MANUAL_ID_GOAL_TIME, mManualID_GoalTime);
			mSpectrumView.Set_DataColor(getResources().getColor(R.color.Gray));
			mSpectrumView.Clear_Found_Isotopes();

			mAnalysisView.RemoveAll_IsotopeData();

			// --===--
			Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
			intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_ON);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
			// --===--
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	private void End_SequenceMode() {
		try {
			Set_Invisible_Sequence_Contol(true);
			mSequence_repeat_count = 0;
			mPauseTime_ElapsedTime = mPauseTime;
			mIsSequenceMode = false;
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public void Set_Info_OnSpectrumView_OnAnalysisView(int acqtime, int cps) {

		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();

		// mSpectrumView.Set_inform(getResources().getString(R.string.cps),
		// NcLibrary.Comma_Format(cps));
		/*
		 * mSpectrumView.Set_SoureId_inform(getResources().getString(R.string. cps),
		 * Cut_Decimal_Point(cps));
		 * 
		 * mSpectrumView.Set_SoureId_inform2(getResources().getString(R.string.
		 * total_count), Cut_Decimal_Point(m_EventData.MS.Get_TotalCount()));
		 * mSpectrumView.Set_SoureId_inform3(getResources().getString(R.string.
		 * acq_time), String.valueOf(acqtime) + " / " + mManualID_GoalTime + "s");
		 */

		cpsTxt.setText(Cut_Decimal_Point(cps));

		Log.d("CPS", Cut_Decimal_Point(cps) + ";");

		avg_Doserate_txt.setText(Cut_Decimal_Point_Doserate_double(MainActivity.mDetector.Get_Gamma_DoseRate_nSV()));

		totalCountTxt.setText(Cut_Decimal_Point(m_EventData.MS.Get_TotalCount()));

		acqTimeTxt.setText(String.valueOf(acqtime));

		acqTimeTxt2.setText(String.valueOf(mManualID_GoalTime));

		// mSpectrumView.Set_inform4(getResources().getString(R.string.cps),
		// NcLibrary.Comma_Format(cps));

		// --------------------------------------------------------------------------------------------

		mTopAnal_Info.Set_infor1(getResources().getString(R.string.date), calendar.get(Calendar.DAY_OF_MONTH) + "."
				+ (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR));
		mTopAnal_Info.Set_infor2(getResources().getString(R.string.time),
				date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
		mTopAnal_Info.Set_infor3(getResources().getString(R.string.acq_time), String.valueOf(acqtime) + "sec");
		mTopAnal_Info.Set_infor4(getResources().getString(R.string.cps), NcLibrary.Prefix(cps, true));

	}

	public Location Get_Location() {

		GpsInfo gps = new GpsInfo(IDspectrumActivity.this);
		if (gps.isGetLocation()) {
			return gps.getLocation();
		}
		return new Location(LocationManager.GPS_PROVIDER);
	}

	private void Set_IdResult_toViews(Vector<Isotope> result) {

		if (result == null)
			return;

		Isotope IsotopeClass = new Isotope();

		mSpectrumView.Clear_Found_Isotopes();
		mAnalysisView.RemoveAll_IsotopeData();
		for (int i = 0; i < result.size(); i++) {
			mSpectrumView.Add_Found_Isotope(result.get(i));
			if (!result.get(i).Class.matches("UNK"))
				mAnalysisView.Add_IsotopeData(result.get(i));
		}
		// mSpectrumView.invalidate();
		mAnalysisView.invalidate();
		mTopAnal_Info.Set_Log_GridCount(mAnalysisView.Get_Grid_Count());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		/*
		 * MenuInflater inflater = getMenuInflater(); inflater.inflate(R.menu.id_spc,
		 * menu);
		 * 
		 * 
		 */

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (SOURCE_ID_RESULT_MODE == false) {
			// menu.removeItem(menu.getItem(1).getItemId());
			// menu.removeItem(menu.getItem(2).getItemId());

		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (mIsManualID_mode == false) {
				// mReDrawTimer.cancel();
				LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
				finish();
			}
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.manualID_Cancel:

			if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {

				Source_Id_Result_Cancel();

			} else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

				Source_Id_Running_Cancel();
			}

			break;

		}
		return true;
	}

	public void Start_ManualID() {

		mIsManualID_mode = true;

	}

	public void Start_sequenceMode() {
		mSPC.ClearSPC();
		mIsSequenceMode = true;
		mPauseTime_ElapsedTime = mPauseTime;
		mSequence_repeat_count = 0;
		Set_Invisible_Sequence_Contol(false);
	}

	private void Set_SeqModeInfo(int repeatCnt) {
		TextView repeatCntTV = (TextView) m_MainLayout.findViewById(R.id.tv_seq_repeat);
		repeatCntTV.setText(repeatCnt + "/" + mSequence_repeat_Goal);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// m_MainLayout.onTouchEvent(event);
		// mSpectrumView.onTouchEvent(event);

		return super.onTouchEvent(event);
	}

	/////////////////////////////////////////////////////////////////////
	public void Set_Invisible_ManualID_Contol(boolean IsInvisible) {

		/*
		 * int Weight = 0; if (IsInvisible) Weight = 0; else Weight = 1;
		 * 
		 * FrameLayout SpcLayout = (FrameLayout) findViewById(R.id.frameLayout1);
		 * LinearLayout.LayoutParams Param = (LinearLayout.LayoutParams)
		 * SpcLayout.getLayoutParams(); Param.weight = Weight;
		 * SpcLayout.setLayoutParams(Param);
		 * 
		 * if (Weight == 0) { // LinearLayout control_lyaout = (LinearLayout) //
		 * m_MainLayout.findViewById(R.id.frameLayout2); //
		 * control_lyaout.setVisibility(LinearLayout.GONE); } else { LinearLayout
		 * control_lyaout = (LinearLayout) m_MainLayout.findViewById(R.id.frameLayout2);
		 * control_lyaout.setVisibility(LinearLayout.VISIBLE);
		 * 
		 * LinearLayout seqInfo_Layout = (LinearLayout)
		 * m_MainLayout.findViewById(R.id.layout_sequenceInfo);
		 * seqInfo_Layout.setVisibility(LinearLayout.GONE); }
		 * 
		 * m_MainLayout.invalidate();
		 */
	}

	private void Set_Invisible_Sequence_Contol(boolean IsVisible) {
		/*
		 * int Weight = 0; if (IsVisible) Weight = 0; else Weight = 1;
		 * 
		 * FrameLayout SpcLayout = (FrameLayout)
		 * m_MainLayout.findViewById(R.id.frameLayout1); LinearLayout.LayoutParams Param
		 * = (LinearLayout.LayoutParams) SpcLayout.getLayoutParams(); Param.weight =
		 * Weight; SpcLayout.setLayoutParams(Param);
		 * 
		 * if (Weight == 0) { // LinearLayout control_lyaout = (LinearLayout) //
		 * m_MainLayout.findViewById(R.id.layout_sequenceInfo); //
		 * control_lyaout.setVisibility(LinearLayout.GONE); } else { LinearLayout
		 * control_lyaout = (LinearLayout)
		 * m_MainLayout.findViewById(R.id.layout_sequenceInfo);
		 * control_lyaout.setVisibility(LinearLayout.VISIBLE);
		 * 
		 * LinearLayout seqInfo_Layout = (LinearLayout)
		 * m_MainLayout.findViewById(R.id.frameLayout2);
		 * seqInfo_Layout.setVisibility(LinearLayout.GONE); } m_MainLayout.invalidate();
		 */}

	private void Set_Spectrum_Y_toEnergy() {
		mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
	}

	@Override
	public void onBackPressed()
	{
		Log.e("ahn","backpressed");
		int mGain_restTime_over2=10;
		// Toast.makeText(getApplicationContext(), "백키 눌림", 1).show();
		if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT)
		{
			MainActivity.mDetector.mGain_restTime=mGain_restTime_over2;
			Source_Id_Result_Cancel();
		}
		else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING)
		{
			MainActivity.mDetector.mGain_restTime=mGain_restTime_over2;
			Source_Id_Running_Cancel();
		}
		return;
	}

	public void onCreatePart() {

		try {

			mBtnDownCount = 1;

			TimerTask mTask5 = new TimerTask() {

				@Override
				public void run() {

					mBtnDownCount = 0;
				}
			};

			Timer mTimer5 = new Timer();
			mTimer5.schedule(mTask5, 1000);
			Spectrum spcdata = (MainActivity.mDetector.mGamma_Event == null) ? MainActivity.mDetector.MS.ToSpectrum()
					: MainActivity.mDetector.mGamma_Event.MS.ToSpectrum();

			if (spcdata != null)

				for (int i = 0; i < 1024; i++) {

					mFirstSpec[i] = 0;

				}

			spcdata.Set_Spectrum(mFirstSpec);
			mSPC.Set_Spectrum(spcdata);

			mManualID_GoalTime = MainActivity.mPrefDB.Get_ManualID_DefaultTime();
			mManualID_Adjust_sec = MainActivity.mPrefDB.Get_ManualID_AdjustTime();

			mIsSvUnit = MainActivity.mDetector.IsSvUnit;

			mSequence_acqTime = MainActivity.mPrefDB.Get_SequenceMode_acqTime_From_pref();
			mSequence_repeat_Goal = MainActivity.mPrefDB.Get_SequenceMode_Repeat_From_pref();
			mPauseTime = MainActivity.mPrefDB.Get_SequenceMode_PauseTime_From_pref();

			// String mPauseTime1;
			// mPauseTime1 = getIntent().getStringExtra(ACTIVTY);

			int a;
			a = 0;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		// ---------------------

		mClassColor.clear();
		mClassColor.add(Color.rgb(150, 24, 150));
		mClassColor.add(Color.rgb(27, 23, 151));
		mClassColor.add(Color.rgb(44, 192, 185));
		mClassColor.add(Color.rgb(10, 150, 20));
		mClassColor.add(Color.RED);

		IntentFilter filter = new IntentFilter();
		filter.addAction(MainBroadcastReceiver.MSG_EVENT);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_EN_CALIBRATION);
		filter.addAction(MainBroadcastReceiver.MSG_REMEASURE_BG);
		filter.addAction(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_EVENT_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_NEUTRON);
		filter.addAction(MainBroadcastReceiver.MSG_TAB_SOURCE_ID);
		filter.addAction(MainBroadcastReceiver.MSG_SPEC_VIEWFILPPER);
		filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_TIMEDOWN);

		filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

		/*
		 * @SuppressWarnings("unchecked") Vector<Isotope> id_result = (Vector<Isotope>)
		 * getIntent().getSerializableExtra(EXTRA_DETECTED_SOURCE); if(id_result !=
		 * null) { Log.i(TAG, "asdf"); Set_IdResult_toViews(id_result); }
		 */
		//////// �씠�븯 �옄�룞 �뒳由쎈え�뱶 �빐�젣
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_MainLayout = (LinearLayout) inflater.inflate(R.layout.id_spectrum, null);

		///////////////

		m_AnalysisLayout = (LinearLayout) inflater.inflate(R.layout.iso_analysis, null);
		setContentView(m_AnalysisLayout);
		/////////

		LinearLayout layout = (LinearLayout) m_MainLayout.findViewById(R.id.AdLayout);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mAnalysisView = new ScView_Ad(this);
		layout.addView(mAnalysisView, p);
		mAnalysisView.Set_IsSv_Unit(mIsSvUnit);
		////

		mManualID_Time = (TextView) m_MainLayout.findViewById(R.id.ManualID_time);

		mTopAnal_Info = (Analysis_TopInfor) m_MainLayout.findViewById(R.id.Iso_analysis);
		mTopAnal_Info.Set_Log_GridCount(mAnalysisView.Get_Grid_Count());
		mTopAnal_Info.Set_Class_Color(mClassColor);
		mTopAnal_Info.Set_Doserate_Unit(mIsSvUnit);

		///////////////// Manual ID Picker
		mManualID_TimeUP = (ImageButton) m_MainLayout.findViewById(R.id.button_up);
		mManualID_TimeUP.setOnTouchListener((new Button.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					mManualID_TimeUP.setBackgroundResource(R.drawable.time_up_focus);

					mManualID_GoalTime += mManualID_Adjust_sec;
					acqTimeTxt2.setText(Integer.toString(mManualID_GoalTime));

					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mManualID_TimeUP.setBackgroundResource(R.drawable.time_up);
					// mManualID_sec =
					// Integer.valueOf(String.valueOf(mManualID_Time.getText()));

				}
				return false;
			}
		}));
		mManualID_TimeDown = (ImageButton) m_MainLayout.findViewById(R.id.button_down);
		mManualID_TimeDown.setOnTouchListener((new Button.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {

				try {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (m_EventData.MS != null) {
							if (m_EventData.MS.Get_AcqTime() < mManualID_GoalTime - mManualID_Adjust_sec) {

								mManualID_TimeDown.setBackgroundResource(R.drawable.time_down_focus);

								mManualID_GoalTime -= mManualID_Adjust_sec;

								acqTimeTxt2.setText(Integer.toString(mManualID_GoalTime));

							}
						}
						return false;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						mManualID_TimeDown.setBackgroundResource(R.drawable.time_down);

					}
					return false;
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
					return false;
				}
			}
		}));

		LinearLayout control_lyaout = (LinearLayout) m_MainLayout.findViewById(R.id.frameLayout2);
		control_lyaout.setVisibility(LinearLayout.VISIBLE);

		Button seq_cancel = (Button) m_MainLayout.findViewById(R.id.btn_seq_Cancel);
		seq_cancel.setOnTouchListener((new Button.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					End_SequenceMode();

					mAnalysisView.RemoveAll_IsotopeData();
					mSpectrumView.Clear_Found_Isotopes();

					mSpectrumView.invalidate();
					mAnalysisView.invalidate();

					m_EventData = null;
					mSPC.ClearSPC();

					// --===--
					Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
					intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_ON);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
					// --===--

					Toast.makeText(getApplicationContext(), getResources().getString(R.string.cancel),
							Toast.LENGTH_LONG).show();
					return true;
				}

				return false;
			}
		}));

		// -- End Manual Id Picker
		mSpectrumView = (SpectrumView) m_MainLayout.findViewById(R.id.IDspectrum);
		mSpectrumView.setChArraySize(mSPC.Get_Ch_Size());
		mSpectrumView.SetChArray(mSPC);
		mSpectrumView.LogMode(true);
		mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
		mSpectrumView.invalidate();

		setContentView(m_MainLayout);

		filpperImgView = (ImageView) findViewById(R.id.filperImgView);

		filperTouch = (RelativeLayout) findViewById(R.id.filperTouch);
		flipper = (ViewFlipper) findViewById(R.id.flipper);

		IDspectrum = (View) findViewById(R.id.IDspectrum);

		Iso_analysis = (View) findViewById(R.id.Iso_analysis);

		filpperImgView.setOnTouchListener(mTouchEvent);

		mProgBar = (ProgressBar) m_MainLayout.findViewById(R.id.SetupSpcSrc_ProgressBar);

		mFrameLayout = (FrameLayout) m_MainLayout.findViewById(R.id.frameLayout1);

		cpsTxt = (TextView) m_MainLayout.findViewById(R.id.cpsTxt);

		totalCountTxt = (TextView) m_MainLayout.findViewById(R.id.totalCountTxt);

		avg_Doserate_txt = (TextView) m_MainLayout.findViewById(R.id.avg_Doserate_txt);

		acqTimeTxt = (TextView) m_MainLayout.findViewById(R.id.Acq_TimeTxt);

		acqTimeTxt2 = (TextView) m_MainLayout.findViewById(R.id.Acq_TimeTxt2);

		acqTimeTxt2.setText(Integer.toString(mManualID_GoalTime));
		// mFrameLayout.setOnTouchListener(this);

		Manual_Nuclide_Analysis();

		//180802
		linearLayout = (LinearLayout) inflater.inflate(R.layout.id_result, null);
		ReachBackBtn = (ImageButton) linearLayout.findViewById(R.id.ReachBackBtn);
		CameraBtn = (ImageButton) linearLayout.findViewById(R.id.CameraBtn);
		VideoBtn = (ImageButton) linearLayout.findViewById(R.id.VideoBtn);
		VoiceBtn = (ImageButton) linearLayout.findViewById(R.id.VoiceBtn);
		ResultLocationInfoTxt = (TextView) linearLayout.findViewById(R.id.s_location_info);
		userInfoTxt = (TextView) linearLayout.findViewById(R.id.s_user_info);
		removeEventTxt = (Button) linearLayout.findViewById(R.id.removeEventTxt);
		idAcqTimeTxt = (TextView) linearLayout.findViewById(R.id.alarm_duration_info);
		dateTimeTxt = (TextView) linearLayout.findViewById(R.id.time_info);
		dateTxt = (TextView) linearLayout.findViewById(R.id.date_info);
		EventId = (TextView) linearLayout.findViewById(R.id.event_info);
		LayoutA = (LinearLayout) linearLayout.findViewById(R.id.FocusA);
		LayoutB = (LinearLayout) linearLayout.findViewById(R.id.FocusB);
		LayoutC = (LinearLayout) linearLayout.findViewById(R.id.FocusC);

		/*
		 * 180119 최종결과창에 비고란 완료누르면 키보드 내려가게
		 */
		CommentEdit = (EditText) linearLayout.findViewById(R.id.editT_Comment1);
		CommentEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
		CommentEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

		linearLayout.findViewById(R.id.removeEventTxt).setOnClickListener(mClickListener);
		linearLayout.findViewById(R.id.RadresponderBtn).setOnClickListener(mClickListener);
		linearLayout.findViewById(R.id.ReachBackBtn).setOnClickListener(mClickListener);

		linearLayout.findViewById(R.id.CameraBtn).setOnClickListener(mClickListener);
		linearLayout.findViewById(R.id.VideoBtn).setOnClickListener(mClickListener);
		linearLayout.findViewById(R.id.VoiceBtn).setOnClickListener(mClickListener);
		Favorite_Checkbox = (CheckBox) linearLayout.findViewById(R.id.Favorite_Checkbox);
		mGallery = (ListView) linearLayout.findViewById(R.id.Gallery_List);


	}



	public void Manual_Nuclide_Analysis()
	{
		SetSourceIdRunningActivityMode();

		Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
		intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_OFF);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

		mManualID_Time.setText(String.valueOf(mManualID_GoalTime));
		Set_Invisible_ManualID_Contol(false);
		Start_ManualID();

		Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_DISABLE);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

	}

	@Override
	protected void onResume() {

		// DoubleClickRock = Activity_Mode.EXCUTE_MODE;

		/*
		 * if(MainActivity.ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY){
		 * 
		 * MainActivity.ACTIVITY_STATE }
		 */

		MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;

		MainActivity.tabHost.getTabWidget().getChildAt(0).setOnTouchListener(this);
		MainActivity.tabHost.getTabWidget().getChildAt(1).setOnTouchListener(this);
		MainActivity.tabHost.getTabWidget().getChildAt(2).setOnTouchListener(this);

		super.onResume();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private OnTouchListener mTouchEvent = new OnTouchListener()

	{
		public boolean onTouch(View v, MotionEvent event)

		{

			/*
			 * if (v.getContext() ==
			 * MainActivity.tabHost.getTabWidget().getChildAt(1).getContext()) {
			 */
			if (event.getAction() == MotionEvent.ACTION_DOWN) {

			}
			if (event.getAction() == MotionEvent.ACTION_UP) {

				switch (swicthCount) {
				case 0:
					LeftMove();
					// filperMove = true;
					// IDspectrum.setVisibility(View.INVISIBLE);
					// Iso_analysis.setVisibility(View.VISIBLE);
					filpperImgView.setImageResource(R.drawable.left);
					swicthCount = 1;
					break;

				case 1:
					RightMove();

					// filpperImgView.set
					// filperMove = false;
					// IDspectrum.setVisibility(View.VISIBLE);
					// Iso_analysis.setVisibility(View.INVISIBLE);
					filpperImgView.setImageResource(R.drawable.right);

					swicthCount = 0;
					break;

				default:

					break;
				}

			}
			/* } */

			return true;
		}
	};

	public void RightMove() {

		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_left));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_right));
		flipper.showPrevious();

	}

	public void LeftMove() {

		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_right));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_left));
		flipper.showNext();

	}

	public void DropDownAnimation()
	{
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.drop_down);
		linearLayout.startAnimation(anim);
	}

	//180802 수정
	public void SourceIdResultInfo()
	{
		setContentView(linearLayout);

		new Thread(new Runnable() {
			@Override
			public void run() {
				mHandler.obtainMessage(LIST_DRAW).sendToTarget();
			}

		}).start();

		MainActivity.ACTIVITY_HW_KEY_ROOT_CHECK = Activity_Mode.NOT_FIRST_ACTIVITY;
		MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;
		Id_Result_Reset();

		ReachBackBtn.setFocusable(true);
		ReachBackBtn.setFocusableInTouchMode(true);
		ReachBackBtn.requestFocus();

		mPhotoName = new Vector<String>();
		mVideoName = new Vector<String>();
		mRecoderName = new Vector<String>();
		mTotalTxt = new Vector<String>();
		mRecoder = new Vector<String>();

		//ReachBackBtn.requestFocus();
		RecBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rec);

		userInfoTxt.setText(MainActivity.mPrefDB.Get_User_Name());
		ResultLocationInfoTxt.setText(MainActivity.mPrefDB.Get_Location_Info());
		idAcqTimeTxt.setText(String.valueOf(mManualID_GoalTime) + " " + getResources().getString(R.string.sec));
		EventDBOper eventDB = new EventDBOper();
		int eventNumber = eventDB.GetEventCount();
		EventId.setText("#" + String.valueOf(eventNumber));
		mEventNumber = eventNumber;
		if (mEventNumber == -1) {
			IS_TAKE_PHOTO_AND_VIDEO = false;
			return;
		}
		dateTimeTxt.setText(m_EventData.Get_EndEventTime());
		dateTxt.setText(m_EventData.Get_Date());

		mThumnail.clear();
		//Init_GalleryView();

		nuclideAdapter adapter = new nuclideAdapter(this, R.layout.id_result_row,m_EventData.Detected_Isotope);

		m_lv = (ListView) findViewById(R.id.ListView);

		m_lv.setDivider(null);
		m_lv.setAdapter(adapter);


		Log.e("ahn", dlgTemp + "");
		if (dlgTemp && dbTemp)
		{
			NcLibrary.showReachBackDlg(mContext,mEventNumber,arrPhoto.size(),false);
			dlgTemp = false;
			dbTemp = false;
		}

		CreateCsvFile();

	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//EventDBOper DB = new EventDBOper();
	//	String updateFile = "";

	/*	if (resultCode == RESULT_OK)
		{
			*/
			MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;
			switch (requestCode)
			{
			case 1:

/*				if(saveGallery(1))
				{*/
				ReachBackBtn.setFocusable(true);
				ReachBackBtn.setFocusableInTouchMode(true);
				ReachBackBtn.requestFocus();

					//180827 수정

					//NcLibrary.SendEmail( true,IDspectrumActivity.this,  mEventNumber);

					//NcLibrary의 SendEmail 안에있던 인터넷 체크 맨앞으로 뺌
					//기존에는 인터넷 연결이 안되어있으면 바로 오류메세지 팝업 -> 이벤트 로그 저장
					//현재는 리치백 로그에도 저장이 되어야하기때문에 변경
					if (!NcLibrary.isNetworkOnline(mContext))
					{
						EventDBOper mEventDB = new EventDBOper();
						mEventDB.OpenDB();
						EventData eventdata = mEventDB.LoadEventDB(mEventNumber);
						mEventDB.EndDB();
						eventdata.reachBackSuccess = false;
						eventdata.Event_Number = mEventNumber;
						File f = new File(MainActivity.Media.reachbackFolderPath + "/" + "EventP" + eventdata.Event_Number + "_1" + ".png");
						{
							if(f.exists())
							{
								eventdata.reachBackPic = MainActivity.Media.reachbackFolderPath + "/" + "EventP" + eventdata.Event_Number + "_1" + ".png";
							}
						}

						int idx = DBMng.GetInst(IDspectrumActivity.this).loadReahBackDB(eventdata.Event_Date, eventdata.StartTime);
						if (idx <= 0)
						{
							DBMng.GetInst(IDspectrumActivity.this).writeReachBackDB(eventdata);
						}
						else
						{
							DBMng.GetInst(IDspectrumActivity.this).updateReachBack(idx, eventdata.reachBackPic, eventdata.reachBackXml, eventdata.reachBackSuccess+"");
						}


						NcLibrary.Show_Dlg1(IDspectrumActivity.this.getResources().getString(R.string.internet_not).toString(), IDspectrumActivity.this);
					}
					else
					{
						EventData reachBack = NcLibrary.SendEmail( true,IDspectrumActivity.this,  mEventNumber,true);

						int idx = DBMng.GetInst(IDspectrumActivity.this).loadReahBackDB(reachBack.Event_Date, reachBack.StartTime);
						if (idx == -1)
						{
							DBMng.GetInst(IDspectrumActivity.this).writeReachBackDB(reachBack);
						}
						else
						{
							DBMng.GetInst(IDspectrumActivity.this).updateReachBack(idx, reachBack.reachBackPic, reachBack.reachBackXml, reachBack.reachBackSuccess+"");
						}

/*						if (reachBack != null)
						{
							DBMng.GetInst(IDspectrumActivity.this).writeReachBackDB(reachBack);
						}
						else
						{
							NcLibrary.Show_Dlg1(IDspectrumActivity.this.getResources().getString(R.string.email_transmit_error).toString(), IDspectrumActivity.this);
						}*/
					}


				break;
			case 2:
                VideoBtn.setFocusable(true);
                VideoBtn.setFocusableInTouchMode(true);
                VideoBtn.requestFocus();
				saveGallery(2);

				break;
			case 3:
                VoiceBtn.setFocusable(true);
                VoiceBtn.setFocusableInTouchMode(true);
                VoiceBtn.requestFocus();
				saveGallery(3);
				break;

			case 4:

				if(saveGallery(1))
				{
					CameraBtn.setFocusable(true);
					CameraBtn.setFocusableInTouchMode(true);
					CameraBtn.requestFocus();
				}
				else
				{
					NcLibrary.Show_Dlg1(IDspectrumActivity.this.getResources().getString(R.string.email_transmit_error).toString(), IDspectrumActivity.this);
				}

				break;

			default:
				break;
			}

			// timetask 중지 관련

	//	}

/*		TimerTask mTask = new TimerTask() {
			@Override
			public void run() {
				MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;
			}
		};

		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 1500);*/

		// MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;

		MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;
	}


	//180802 추가
	public boolean saveGallery(final int gallery)
	{

		try
		{
			String newFile = "";
			String column = "";
			switch (gallery)
			{
				case 1 :
					column = "Photo";
					if (hashMap.get("photo") != null)
					{
						newFile = (String) hashMap.get("photo");
						hashMap.remove("photo");
					}
					break;
				case 2 :
					if (hashMap.get("video") != null)
					{
						newFile = (String) hashMap.get("video");
						hashMap.remove("video");
					}
					column = "Video";
					//newFile = fileName;
					break;
				case 3 :
					column = "Recode";
					if (hashMap.get("record") != null)
					{
						newFile = (String) hashMap.get("record");
						hashMap.remove("record");
					}
					break;
			}

			if(!newFile.equals(""))
			{
				//db에 저장
				EventDBOper mEventDB = new EventDBOper();
				mEventDB.OpenDB();
				String updateFile = "";
				Cursor cu = mEventDB.LoadGallery(gallery, mEventNumber);
				if (cu.moveToFirst())
				{
					if (cu.getCount() > 0) {
						updateFile = cu.getString(cu.getColumnIndex(column));
					}
				}
				if (!updateFile.equals("")) {
					updateFile = updateFile + newFile + ";";
				} else
				{
					updateFile = newFile + ";";
				}

				setGallery(gallery, updateFile);

				mEventDB.updateGallery(gallery, mEventNumber, updateFile); //1 : photo
				mEventDB.EndDB();
			}


			return true;
		}
		catch (Exception e)
		{
			return false;
		}

	}

	//gallery adapter 갱신
	public void setGallery(final int gallery, String updateFile )
	{

			switch (gallery)
			{
				case 1 :
					arrPhoto = Separate_EveryDash3(updateFile);
					break;
				case 2 :
					arrVideo = Separate_EveryDash3(updateFile);
					break;
				case 3 :
					arrRecord = Separate_EveryDash3(updateFile);
					break;
				case 4 :
					break;
			}


		eventGallery = new ArrayList<LogEventPic.LogGallery>();
		if (arrPhoto != null)
		{
			for (int i = 0; i < arrPhoto.size(); i++)
			{
				LogEventPic.LogGallery item = new LogEventPic.LogGallery();
				item.file = Media.FolderPath +"/"+ arrPhoto.get(i) + ".png";
				item.fileName = arrPhoto.get(i);
				eventGallery.add(item);
			}
		}
		if (arrVideo != null)
		{
			for (int i = 0; i < arrVideo.size(); i++)
			{
				LogEventPic.LogGallery item = new LogEventPic.LogGallery();
				item.file = Media.FolderPath +"/"+ arrVideo.get(i) + ".mp4";
				item.fileName = arrVideo.get(i);
				eventGallery.add(item);
			}
		}

		if (arrRecord != null)
		{
			for (int i = 0; i < arrRecord.size(); i++)
			{
				LogEventPic.LogGallery item = new LogEventPic.LogGallery();
				item.file = Media.FolderPath +"/"+ arrRecord.get(i) + ".amr";
				item.fileName = arrRecord.get(i);
				eventGallery.add(item);
			}
		}

		galleryAdapter= new IDspectrumResultAdapter(IDspectrumActivity.this, eventGallery);
		mGallery.setAdapter(galleryAdapter);
		galleryAdapter.setOnListener(onGalleryClick);

	}

	private IDspectrumResultAdapter.clickListener onGalleryClick = new IDspectrumResultAdapter.clickListener()
	{
		@Override
		public void onCellClick(String type, final int index, String file, final String name, final ArrayList<LogEventPic.LogGallery> mArrFileUrl)
		{

			Log.e("ahn",file);
			galleryType = -1;
			if(file.contains("EventP"))
			{
				galleryType = 1;
			}
			else if(file.contains("EventV"))
			{
				galleryType = 2;
			}
			else
			{
				galleryType = 3;
			}

			if (type.equals("click"))
			{
				MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				Uri uri = Uri.fromFile(new File(file));
				switch (galleryType)
				{
					case 1 :
						intent.setDataAndType(uri, "image/*");
						break;
					case 2 :
						intent.setDataAndType(uri, "video/*");
						break;
					case 3 :
						intent.setDataAndType(uri, "audio/*");
						break;
				}
				startActivity(intent);
				return;
			}

			if (type.equals("longClick"))
			{
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(IDspectrumActivity.this, android.R.style.Theme_Holo_Dialog));
				dialogBuilder.setTitle(getResources().getString(R.string.delete));
				dialogBuilder.setMessage("'"+name+"'"+getResources().getString(R.string.delete_message));
				dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						EventDBOper DB = new EventDBOper();
						DB.OpenDB();
						File f2d = null;
						String updateFile = "";

						switch (galleryType)
						{
							case 1 :
								for(int i = 0; i<mArrFileUrl.size(); i++)
								{
									if(mArrFileUrl.get(i).fileName.contains("EventP") && !mArrFileUrl.get(i).fileName.equals(name))
									{
										updateFile += mArrFileUrl.get(i).fileName+";";
									}
								}

								f2d = new File(FolderPath+"/"+arrPhoto.get(index)+".png");
								f2d.delete();

								arrPhoto.remove(index);
								DB.updateGallery(1,  mEventNumber, updateFile);

								break;
							case 2 :
								for(int i = 0; i<mArrFileUrl.size(); i++)
								{
									if(mArrFileUrl.get(i).fileName.contains("EventV") && !mArrFileUrl.get(i).fileName.equals(name))
										updateFile += mArrFileUrl.get(i).fileName+";";
								}
								f2d = new File(FolderPath+"/"+arrVideo.get(index)+".mp4");
								f2d.delete();


								DB.updateGallery(2,  mEventNumber, updateFile);
								arrRecord.remove(index);
								break;
							case 3 :
								for(int i = 0; i<mArrFileUrl.size(); i++)
								{
									if(mArrFileUrl.get(i).fileName.contains("EventR") && !mArrFileUrl.get(i).fileName.equals(name))
										updateFile += mArrFileUrl.get(i).fileName+";";
								}
								f2d = new File(FolderPath+"/"+arrRecord.get(index)+".amr");
								f2d.delete();

								DB.updateGallery(3,  mEventNumber, updateFile);
								arrRecord.remove(index);
								break;
						}

						DB.EndDB();

						galleryAdapter.miSelIndex = -1;
						setGallery(4,"");
					}
				});
				dialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						galleryAdapter.miSelIndex = -1;
						galleryAdapter.notifyDataSetChanged();

					}
				});
				dialogBuilder.setCancelable(false);
				dialogBuilder.show();
				return;
			}

		}
	};



	//IDspectrum result  하단 버튼
	Button.OnClickListener mClickListener = new View.OnClickListener()
	{
		//180802 수정
		public void onClick(View v)
		{
			File file;
			fileName = "";
			Intent intent = null;
			switch (v.getId())
			{
				case R.id.CameraBtn :
					MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
					reachBack = false;
					NcLibrary.PhotoExcute(IDspectrumActivity.this, mEventNumber, arrPhoto.size(),reachBack);
					break;
				case R.id.VideoBtn :
					MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
					//Intent Intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					fileName = "EventV"+mEventNumber + "_" + (arrVideo.size()+1);
					file = new File(FolderPath+"."+fileName + ".mp4");
					if (file.exists())
					{
						fileName = "EventV"+mEventNumber + "_" + (arrVideo.size()+1)+"_";
					}
					file = new File(FolderPath, fileName + ".mp4");

					if(hashMap.get("video")!=null)
					{
						hashMap.remove("video");
					}
					hashMap.put("video", fileName);

				/*	Intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
					startActivityForResult(Intent, 2);*/

					///
					intent = new Intent(IDspectrumActivity.this, VideoActivity.class);
					intent.putExtra("path",fileName + ".mp4");
					startActivityForResult(intent, 2);

					break;
				case R.id.VoiceBtn :
					intent = new Intent(IDspectrumActivity.this, RecActivity.class);
					intent.putExtra(Check.ListNumber, mEventNumber);
					startActivityForResult(intent, 3);
					break;
				case R.id.removeEventTxt :
					RemoveExucute();
					break;
				case R.id.ReachBackBtn :

					if (!NcLibrary.isNetworkOnline(mContext))
					{
						ReachBackBtn.setFocusable(true);
						ReachBackBtn.setFocusableInTouchMode(true);
						ReachBackBtn.requestFocus();

						EventDBOper mEventDB = new EventDBOper();
						mEventDB.OpenDB();
						EventData eventdata = mEventDB.LoadEventDB(mEventNumber);
						mEventDB.EndDB();
						eventdata.reachBackSuccess = false;
						eventdata.Event_Number = mEventNumber;
						File f = new File(MainActivity.Media.reachbackFolderPath + "/" + "EventP" + eventdata.Event_Number + "_1" + ".png");
						{
							if(f.exists())
							{
								eventdata.reachBackPic = MainActivity.Media.reachbackFolderPath + "/" + "EventP" + eventdata.Event_Number + "_1" + ".png";
							}
							else
							{
								eventdata.reachBackPic = "";
							}
						}
                      //  DBMng.GetInst(IDspectrumActivity.this).writeReachBackDB(eventdata);

						int idx = DBMng.GetInst(IDspectrumActivity.this).loadReahBackDB(eventdata.Event_Date, eventdata.StartTime);
						if (idx <= 0)
						{
							DBMng.GetInst(IDspectrumActivity.this).writeReachBackDB(eventdata);
						}
						else
						{
							DBMng.GetInst(IDspectrumActivity.this).updateReachBack(idx, eventdata.reachBackPic, eventdata.reachBackXml, eventdata.reachBackSuccess+"");
						}

						NcLibrary.Show_Dlg1(IDspectrumActivity.this.getResources().getString(R.string.internet_not).toString(), IDspectrumActivity.this);
					}
					else
					{
						NcLibrary.showReachBackDlg(mContext,mEventNumber,arrPhoto.size(),false);
					}
					break;
			}
		}
	};

	//180802 수정

	public class GalleryListAdapter extends BaseAdapter {

		LayoutInflater inflater;

		int GalItemBg;
		private Context cont;
		TextView text1;

		public GalleryListAdapter(Context c) {
			cont = c;

			TypedArray typArray = obtainStyledAttributes(R.styleable.GalleryTheme);

			GalItemBg = typArray.getResourceId(R.styleable.GalleryTheme_android_galleryItemBackground, 0);

			typArray.recycle();

		}

		public int getCount() {

			return mThumnail.size();

		}

		public Object getItem(int position) {

			return position;

		}

		public long getItemId(int position) {

			return position;

		}

		public View getView(int position, View convertView, ViewGroup parent) {

			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// ImageView imgView = new ImageView(cont);

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.id_result_addview, null);
			}
			text1 = (TextView) convertView.findViewById(R.id.txt01);
			ImageView imgView = (ImageView) convertView.findViewById(R.id.imgView1);
			LinearLayout mLinearLayout = (LinearLayout) convertView.findViewById(R.id.linearlayout1);

			imgView.setLayoutParams(new LinearLayout.LayoutParams(250, 187));
			text1.setText(mTotalTxt.get(position));
			imgView.setImageBitmap(mThumnail.get(position));

			imgView.setScaleType(ImageView.ScaleType.FIT_XY);

			return convertView;

		}

	}

	//180802 수정
	class nuclideAdapter extends ArrayAdapter
	{
		Vector<Isotope> arrIsotope = new Vector<Isotope>();

		public class ListHolder
		{
			public int idx;
			TextView nuclide1, nuclide2, doserate, level;
		}
		ListHolder mHolder;
		Isotope IsoClass = new Isotope();

		public nuclideAdapter(Context context, int resource, Vector<Isotope> arr)
		{
			super(context, resource);
			// TODO Auto-generated constructor stub

			mContext = context;
			arrIsotope = arr;
		}

		@Override
		public int getCount()
		{
			return arrIsotope.size();
		}
		@Override
		public Object getItem(int position)
		{
			return position;
		}
		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.id_result_row, parent, false);

				mHolder = new ListHolder();

				mHolder.nuclide1= (TextView) convertView.findViewById(R.id.NuclideTxt);
				mHolder.nuclide2= (TextView) convertView.findViewById(R.id.NuclideTxt2);
				mHolder.doserate= (TextView) convertView.findViewById(R.id.DoserateTxt);
				mHolder.level= (TextView) convertView.findViewById(R.id.LevelTxt);
				convertView.setTag(mHolder);
			}
			else
			{
				mHolder = (ListHolder) convertView.getTag();
			}

			mHolder.idx = position;
			mHolder.nuclide1.setText(arrIsotope.get(mHolder.idx).Class);
			if (arrIsotope.get(mHolder.idx).Class.equals(IsoClass.CLASS_IND))
			{
				mHolder.nuclide1.setTextColor(Color.rgb(157, 207, 255));
			}
			else if (arrIsotope.get(mHolder.idx).Class.equals(IsoClass.CLASS_MED)) {
				mHolder.nuclide1.setTextColor(Color.rgb(44, 192, 185));
			} else if (arrIsotope.get(mHolder.idx).Class.equals(IsoClass.CLASS_NORM)) {
				mHolder.nuclide1.setTextColor(Color.rgb(0, 150, 20));
				} else if (arrIsotope.get(mHolder.idx).Class.equals(IsoClass.CLASS_SNM)) {
				mHolder.nuclide1.setTextColor(Color.rgb(150, 24, 150));
				} else if (arrIsotope.get(mHolder.idx).Class.equals(IsoClass.CLASS_UNK)) {
				mHolder.nuclide1.setTextColor(Color.rgb(206, 28, 32));
				}

			mHolder.nuclide2.setText(arrIsotope.get(mHolder.idx).isotopes);
			mHolder.doserate.setText(arrIsotope.get(mHolder.idx).DoseRate_S);

			int level = (int)Math.floor(arrIsotope.get(mHolder.idx).Confidence_Level)*10/10;
			mHolder.level.setText(level+ "%");
				//mHolder.level.setText(String.format("%.1f",level)+ "%");
				//mHolder.level.setText(String.format("%.1f",Math.floor(arrIsotope.get(mHolder.idx).Confidence_Level))+ "%");
				//mHolder.level.setText(Math.floor(arrIsotope.get(mHolder.idx).Confidence_Level)+ "%");


			mHolder.nuclide2.setTextColor(Color.rgb(230, 220, 0));
			mHolder.doserate.setTextColor(Color.rgb(230, 220, 0));
			mHolder.level.setTextColor(Color.rgb(230, 220, 0));

				return convertView;
			}

	};

	public void ListViewSizeChange() {

		m_lv = (ListView) findViewById(R.id.ListView);

		View m_lv1 = (View) this.findViewById(R.id.ListView);
		int tabBodyWidth = m_lv1.getWidth();
		int tabBodyHeight = m_lv1.getHeight();

		String str = Integer.toString(m_EventData.Detected_Isotope.size());
		String str1 = Integer.toString(tabBodyWidth);
		String str2 = Integer.toString(tabBodyHeight);

		// Toast.makeText(getApplicationContext(), "isotope 사이즈: " + str + "가로크기
		// : " + str1 + "세로크기 :" + str2, 1).show();

		if (m_EventData.Detected_Isotope.size() > 2) {
			m_lv.setLayoutParams(
					new LinearLayout.LayoutParams(tabBodyWidth, tabBodyHeight * m_EventData.Detected_Isotope.size()));

		} else {
			m_lv.setLayoutParams(new LinearLayout.LayoutParams(tabBodyWidth, tabBodyHeight * 2));
		}
	}

	public void TimeUp() {
		mManualID_GoalTime += mManualID_Adjust_sec;
		acqTimeTxt2.setText(String.valueOf(mManualID_GoalTime));

	}

	public void TimeDown() {

		if (m_EventData.MS.Get_AcqTime() < mManualID_GoalTime - mManualID_Adjust_sec) {
			mManualID_GoalTime -= mManualID_Adjust_sec;
			acqTimeTxt2.setText(String.valueOf(mManualID_GoalTime));
		}

	}

	public void View_Filpper() {

		switch (swicthCount) {
		case 0:
			LeftMove();
			// filperMove = true;
			// IDspectrum.setVisibility(View.INVISIBLE);
			// Iso_analysis.setVisibility(View.VISIBLE);
			filpperImgView.setImageResource(R.drawable.left);
			swicthCount = 1;
			break;

		case 1:
			RightMove();

			// filpperImgView.set
			// filperMove = false;
			// IDspectrum.setVisibility(View.VISIBLE);
			// Iso_analysis.setVisibility(View.INVISIBLE);
			filpperImgView.setImageResource(R.drawable.right);

			swicthCount = 0;
			break;

		default:

			break;
		}

	}

	public void Id_Result_Reset() {

		checkMediaModeCount = Activity_Mode.UN_EXCUTE_MODE;
		rootFocusCnt = Focus.ID_RESULT_MENU_C;
		idBottomSwicthCount = Focus.ID_RESULT_MENU_C_REMOVE_BTN;
		id_result_menu_b_count = Activity_Mode.UN_EXCUTE_MODE;
	}



	public void VideoExcute() {

		File file;
		if (mVideo == null)
			mVideo = new Vector<String>();

		for (int i = 1; i < 1000; i++) {

			File mFile;
			mFile = new File(Media.FolderPath + "/EventV" + mEventNumber + "_" + i + ".mp4");
			if (!mFile.exists()) {

				mFileNumber = i;
				break;
			}

		}

		Intent Intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		String File = "EventV" + mEventNumber + "_" + mFileNumber + ".mp4";
		file = new File(Media.FolderPath, File);
		Intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
		startActivityForResult(Intent, 2);
	}


	public void RemoveExucute() {
		MainActivity.tabHost.setCurrentTab(0);
		Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT_CANCEL);

		EventDBOper DB = new EventDBOper();
		EventData mEventData = DB.Load_Event(DB.GetEventCount() - 1);
		final int number = DB.GetEventCount() - 2;
		DB.OpenDB();

		DB.DeleteDB(mEventData.Event_Number);
		DB.EndDB();

		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
		SOURCE_ID_RESULT_MODE = false;
		m_EventData = null;
		setContentView(R.layout.black_background);

	}



	public void Source_id_Hw_Key_Back() {

		if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

			Source_Id_Running_Cancel();

		} else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {

			Source_Id_Result_Cancel();

		} else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT_CAMERA) {

			new Thread(new Runnable() {

				public void run() {

					new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				}
			}).start();

		}
	}

	public void Source_Id_Running_Cancel() {

		MainActivity.tabHost.setCurrentTab(0);
		Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);

		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

		Set_Invisible_ManualID_Contol(true);
		mProgBar.Set_Value(0);
		mProgBar.invalidate();
		End_ManualID();
		m_EventData = null;
		mSPC.ClearSPC();

		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

		IntentFilter filter = new IntentFilter();

		filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_BACK);

		filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);

		filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_START);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);
		setContentView(R.layout.black_background);

	}

	public void Source_Id_Result_Cancel() {

		MainActivity.tabHost.setCurrentTab(0);

		if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {
			EventDBOper DB = new EventDBOper();
			EventData mEventData = DB.Load_Event(DB.GetEventCount() - 1);
			final int number = DB.GetEventCount() - 2;
			DB.OpenDB();

			DB.Update_Favorite(m_EventData.Favorite_Checked, mEventData.Event_Number);

			if (CommentEdit.getText().toString().length() != 0) {
				String tem = CommentEdit.getText().toString();
				tem = tem.replace("\"", "'");
				tem = tem.replace("'", "\"");

				DB.Set_Comment(tem, mEventData.Event_Number);
			}


/*
			m_EventData.PhotoFileName = mPhotoName;
			m_EventData.VedioFileName = mVideoName;
			m_EventData.RecodeFileName = mRecoderName;

			if (Favorite_Checkbox.isChecked()) {
				m_EventData.Favorite_Checked = Check.Favorite_True;
			} else {
				m_EventData.Favorite_Checked = Check.Favorite_False;
			}



			if (m_EventData.PhotoFileName.size() != 0) {
				DB.Update_PhotoFileNames("EventP" + mEventData.Event_Number + '_' + mFileNumber,
						mEventData.Event_Number);

			}
			if (m_EventData.VedioFileName.size() != 0) {
				DB.Update_VideoFileNames("EventV" + mEventData.Event_Number + '_' + mFileNumber,
						mEventData.Event_Number);
			}
			if (m_EventData.RecodeFileName.size() != 0) {
				for (int i = 0; i < m_EventData.RecodeFileName.size(); i++) {

					DB.Update_RecoderFileNames(m_EventData.RecodeFileName.get(i), mEventData.Event_Number);
				}
			}
*/


			// WriteEvent_toDB(m_EventData);
			Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT_CANCEL);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
			SOURCE_ID_RESULT_MODE = false;
			m_EventData = null;

			DB.EndDB();

			Id_Result_Reset();
			setContentView(R.layout.black_background);

		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

			case KeyEvent.KEYCODE_BACK:
			{
				Log.e("ahn","KEYCODE_BACK");
				int mGain_restTime_over2=10;
				// Toast.makeText(getApplicationContext(), "백키 눌림", 1).show();

				if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {
					MainActivity.mDetector.mGain_restTime=mGain_restTime_over2;
					Source_Id_Result_Cancel();

				} else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {
					MainActivity.mDetector.mGain_restTime=mGain_restTime_over2;
					Source_Id_Running_Cancel();
				}
				return true;
			}
		case KeyEvent.KEYCODE_DPAD_LEFT: {
			// event.startTracking();

			if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {
				View_Filpper();
				return true;
			} else {
				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

					KeyExecute(KeyEvent.KEYCODE_DPAD_LEFT);
					return false;
				}
			}

			// Toast.makeText(getApplicationContext(), "key right", 1).show();

			return true;
		}
		case KeyEvent.KEYCODE_DPAD_RIGHT: {
			// event.startTracking();
			if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {
				View_Filpper();

				return true;
			} else {
				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

					KeyExecute(KeyEvent.KEYCODE_DPAD_RIGHT);
					return false;
				}

			}
			return true;
		}
		case KeyEvent.KEYCODE_DPAD_UP: {
			if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {
				TimeUp();

				return true;
			} else if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

				KeyExecute(KeyEvent.KEYCODE_DPAD_UP);
				return false;
			}

			return true;
		}
		case KeyEvent.KEYCODE_DPAD_DOWN: {
			if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

				if (mBtnDownCount == 0) {
					// TimeDown();

				}

				return true;
			} else if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

				KeyExecute(KeyEvent.KEYCODE_DPAD_DOWN);
				return false;
			}

			return true;
		}

		}
		return super.onKeyDown(keyCode, event);
	}

	public void KeyExecute(final int keyvalue) {

		new Thread(new Runnable() {

			public void run() {

				new Instrumentation().sendKeyDownUpSync(keyvalue);

			}
		}).start();

		DoubleClickRock();

	}

	public String Cut_Decimal_Point(int value) {

		float sum = 0;
		String sumStr = "";
		if (value > 1000) {

			sum = (float) value / 1000;
			sumStr = String.format("%.2f", sum);

			sumStr = sumStr + "k";
		} else {

			sumStr = Integer.toString(value);
		}

		return sumStr;
	}

	public String Cut_Decimal_Point_Doserate_double(double value) {

		value = value / 1000;

		String sumStr = "";

		sumStr = String.format("%.3f", value);

		return sumStr;
	}

	public boolean onTouch(View v, MotionEvent event) {
		Log.d("time:", "touch : idspectrum");

		if (v.getContext() == MainActivity.tabHost.getTabWidget().getChildAt(1).getContext()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mPreTouchPosX = (int) event.getX();
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {

				switch (swicthCount) {
				case 0:
					LeftMove();
					// filperMove = true;
					// IDspectrum.setVisibility(View.INVISIBLE);
					// Iso_analysis.setVisibility(View.VISIBLE);
					filpperImgView.setImageResource(R.drawable.left);
					swicthCount = 1;
					break;

				case 1:
					RightMove();

					// filpperImgView.set
					// filperMove = false;
					// IDspectrum.setVisibility(View.VISIBLE);
					// Iso_analysis.setVisibility(View.INVISIBLE);
					filpperImgView.setImageResource(R.drawable.right);

					swicthCount = 0;
					break;

				default:

					break;

				}

				/*
				 * int nTouchPosY = (int) event.getY();
				 * 
				 * String str = Integer.toString(nTouchPosX);
				 * 
				 * String str2 = Integer.toString(nTouchPosY);
				 * 
				 * Toast.makeText(getApplicationContext(), "X: " + nTouchPosX + ", Y:" +
				 * nTouchPosY, 1).show();
				 */

				return true;
			}
		}
		return false;
	};

	public void DoubleClickRock() {

		DoubleClickRock = Activity_Mode.UN_EXCUTE_MODE;
		TimerTask mTask = new TimerTask() {
			@Override
			public void run() {
				DoubleClickRock = Activity_Mode.EXCUTE_MODE;
			}
		};

		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 200);

	}

	@Override
	protected void onDestroy() {
		//MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
		if (mRecTimer != null && mRecTask != null)
		{
			mRecTask.cancel();
			mRecTimer.cancel();
		}
		if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {

			Source_Id_Result_Cancel();

		} else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

			Source_Id_Running_Cancel();
		}

		super.onDestroy();
	}

	public void SetSourceIdRunningActivityMode() {

		MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RUNNING;

	}

	public void SetSourceIdResultActivityMode() {

		MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;

	}

	public void CreateCsvFile() {

		String Doserate_AVGs = NcLibrary.SvToString(m_EventData.Doserate_AVG, true,
				(m_EventData.Doserate_unit == Detector.DR_UNIT_SV) ? true : false);

		String[] mGamma1 = Doserate_AVGs.split(" ");

		// event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false

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

		String mGPS_Latitude = String.format("%.6f", m_EventData.GPS_Latitude);
		String mGPS_Longitude = String.format("%.6f", m_EventData.GPS_Longitude);
		String mDate = DateChange();

		ArrayList<String> mId = new ArrayList<String>();

		Vector<Isotope> Id = m_EventData.Detected_Isotope;
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

		String[] Isotope = mId.get(0).split(",");

		String mIsotope = "";
		for (int i = 0; i < Isotope.length; i++) {

			mIsotope += Isotope[i] + " ";
		}

		String mCPM = String.valueOf((int) ((m_EventData.MS.Get_AvgCPS() + m_EventData.MS.GetAvgFillCps()) * 60));

		String mComment = m_EventData.Comment;

		String enc = new java.io.OutputStreamWriter(System.out).getEncoding();

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
			NcLibrary.Write_ExceptionLog(e);
			// TODO: handle exception
		}

	}

	private String GetCsvPath() {

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EventDB.csv";

		return path;
	}

	private String DateChange() {

		String mDateStr = m_EventData.Event_Date;

		// mAllLog.get(i).EventData
		String[] mDateArray = mDateStr.split("-");

		String mDateSub = mDateArray[2] + "/" + mDateArray[1] + "/" + mDateArray[0];

		return mDateSub;
	}


}
