
package android.HH100;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import Debug.Debug;
import NcLibrary.Coefficients;
import NcLibrary.NewNcAnalsys;
import android.HH100.CcswService.MappingData;
import android.HH100.Control.BatteryView;
import android.HH100.Control.GpsInfo;
import android.HH100.Control.GuageView;
import android.HH100.Control.RealActivitySpectrumView;
import android.HH100.DB.DBMng;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.NormalDB;
import android.HH100.DB.PreferenceDB;
import android.HH100.Dialog.DeviceListActivity;
import android.HH100.Dialog.LoginDlg;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.Service.Guide;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Service.SourceSample;
import android.HH100.Structure.Detector;
import android.HH100.Structure.EventData;
import android.HH100.Structure.GCData;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.ReadDetectorData;
import android.HH100.Structure.SingleMediaScanner;
import android.HH100.Structure.Spectrum;
import android.HH100.Structure.Detector.HwPmtProperty_Code;
import android.HH100.config.Config;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

//VISIBLESTARTBUTTON+++++++++++++++++++++++++++++++++++++++++++++++++++
//MESSAGE_READ_DETECTOR_DATA
//뷰플리퍼
//UIPart1
//UIPart2
//핸들러부분
//메뉴부분
//리스너부분
//dddddd

////////////////////////////
//YKIM, 2018.02.26
// Modification list
// 1. Gain stabilization method
//    Method1: Re-calibration unsing HW 
// 2. keep original Background spectrum
// 3. add K40 finder
// Modified class and function
// MainActivity.java: Handler(Gain_stabilization), Send_GC_ToHW, 
// Detector.java: Gain_stabilization, Background_stabilization
// IDSPectrumActivity.java: Handler(ID)
// SequentialActivity.java: Handler(ID)
// NcMath.java
// NcLibrary.java
////////////////////////////
//////////////////
// 2018.03.25 PA III KINS V1.0.3
// Gainstabilization modification 
// GS using HW information for calibration
// Save HW information in CurrHWCali.txt
// Save data calibration info. in HWCali.txt when Calibration
// Auto calibration have 4times for stabilization
////////////////////////
// 2018.03.26 PA III KINS V1.0.4
// Dose rage for each ID isotope using accmulation spectrum (mEventData.MS)
// XML modification
////////////////////////////////////////////////////////////////
// 2018.3.30 PA v1.0.5
// when id Cs-137, reject U-235, 238
// Factory restore -> Calibration configuration update
////////////////////////////////////////////////////////////////

public class MainActivity extends TabActivity
		implements TabHost.TabContentFactory, OnTabChangeListener, View.OnTouchListener {
	/**
	 * Called when the activity is first created.
	 */

	//180823 reachback db add
	public static DBMng reachbackDB;
	public static EventDBOper mEventDBOper;

	public static final int CHANNEL_ARRAY_SIZE = 1024;
	public static final String DEVICE_NAME = "SAM";

	public static final boolean MAPPING_VERSION = false;
	public static final boolean D = false;
	public static final boolean E = false;
	private static final String TAG_RecvData = "RecvData";
	private static final String TAG = "Main Activity";

	public static int mAUTO_GAIN_result = 0;
	static final int MESSAGE_STATE_CHANGE = 1;

	public static final int MESSAGE_READ_GAMMA = 21;
	public static final int MESSAGE_READ_NEUTRON = 22;
	public static final int MESSAGE_READ_GM = 23;
	public static final int MESSAGE_READ_LA = 24;
	public static final int MESSAGE_READ_BATTERY = 25;
	public static final int MESSAGE_READ_DETECTOR_DATA = 26;
	public static final int MESSAGE_READ_DETECTOR_DATA1 = 55;
	public static final int MESSAGE_SAVE_EVENT = 27;
	public static final int MESSAGE_MEDIA_SCAN = 28;
	public static final int MESSAGE_READ_GC = 29;
	public static final int MESSAGE_USB_READ_GC = 30;

	public static final int MESSAGE_NEUTRON_RECV = 31;
	public static final int MESSAGE_READ_SERIAL_DATA = 33; //181102 추가
	public static final  int MESSAGE_READ_DETECTOR_DATA_J3 = 34; //190123 j3 데이터 (400바이트*7 ) + 시간데이터 처리

	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_CONNECTED_DEVICE_INFO = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_SHUTDOWN = 6;

	static final int REQUEST_CONNECT_DEVICE = 1;

	private static final int REQUEST_ENABLE_BT = 3;
	private static final int AUTO_CALIB_FINISH = 55;
	private static final int FINISH_CALIB_BG = 56;
	static final int FINISH_SETUP_PREF = 57;
	public static final int RESULT_LOGIN = 42;

	public static final int MESSAGE_MAIL_STATE = 61; //180827 추가

	public static final byte[] MESSAGE_END_HW = { 'U', '4', 'A', 'A' };
	public static final byte[] MESSAGE_START_HW = { 'U', '2', 'A', 'A' };
	public static final byte[] MESSAGE_GS_HW = { 'G', 'S' };

	private final static int DEAFALUT_GAIN_SEC = 10;// 0;
	private final int GAIN_THRESHOLD = 150;
	private final int GAIN_EVERY_SEC = 10; //
	private static int mGain_Sec = DEAFALUT_GAIN_SEC; // 기본 gain stabilization
	public static MainService mService = null;
	public static MainUsbService mMainUsbService = null;
	public static final String TOAST = "toast";

	private final static float TAB_TEXT_SIZE = 15.5f;

	private final static String TAB_ENABLE_TEXT_COLOR = "#ffffff";

	public static boolean press = false;

	boolean IsDebugMode = true;

	public int mGain_restTime_under1=10; // stabilization rest time if K40diff <= 1%
	public int mGain_restTime_under2=10; // stabilization rest time if K40diff <= 2%
	public int mGain_restTime_over2=10; // stabilization rest time if K40diff > 2%


	private BluetoothAdapter mBTAdapter;

	public static boolean MANUAL_ID_STATUS = false;

	public static boolean CONNECT_CHECK = false;

	// public static boolean TAB_ENABLE_CHECK = false;

	GuageView m_GammaGuage_Panel;
	/////////////////////////////////////////////////////////////////////////////// �대깽���곹깭
	// private LinearLayout m_MainLayout;
	public static int mLogin = LoginDlg.LOGIN_USER;

	/////////////////////////////////////////////////////////////////////////////////
	public static Detector mDetector = new Detector();
	int count = 0;
	public static String NewGC;

	//
	public static PreferenceDB mPrefDB = null;

	int SpecCnt = 0;

	// private GuageView m_GammaGuage_Panel; // gamma��洹몃옒�쏀뙣��
	// private TextView m_Neutron_Pannel;
	private RealActivitySpectrumView mFinder;

	private ImageView m_Bluetooth_Status;


	public static final String FilenameCaliInfo="HWCali.txt";	// New calibration info
	public static final String FilenameCurCaliInfo="CurrentHWCali.txt";	// from HH200 HW

	// private ViewFlipper mMainFlipper;
	private int mPreTouchPosX;

	private int[] mStbChannel = new int[1024];

	public boolean AUTO_FAIL_CODE_10 = false;
	///////////////////////////////////////////////////////////////////////
	// private Button m_startBtn; // 釉붾（�ъ뒪 �곌구��寃뚯씠吏�痢≪젙 �쒖옉 踰꾪듉

	private NormalDB mNormalDB;
	public static EventDBOper mEventDB;

	public static MediaPlayer mAlarmSound;

	private ProgressDialog mProgressDialog = null;
	private static Context mContext;
	public static int Logcount = 0;

	int TabMoveCount = 0;

	int AutoCalibrationCnt = Activity_Mode.EXCUTE_MODE;

	CcswService mCCSW_Service = null;

	int mBluetoothImage_flag = 0;

	private boolean mVibrating = false;
	AudioManager audio;
	int tabswitch = 0;

	public static int eventId;

	public static int ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;

	public static int DoubleClickRock = Activity_Mode.EXCUTE_MODE;

	public static int DoubleClickRock2 = Activity_Mode.EXCUTE_MODE;

	ArrayList<String> mGainValue = new ArrayList<>();

	public static Boolean mCurrentConnectMode = true;
	public static Boolean mChangeConnectMode = true;

	public static int mBattary = 0;

	public static Debug mDebug;

	// 0122 비정상 종료 (죽을때) 캐치하는 핸들러.
    private Thread.UncaughtExceptionHandler mHnderUncaughException;

	// public static boolean mDoubleConnectCheck = false;
	public interface Media {
		/*
		 * String FolderPath = Environment.getExternalStorageDirectory() + "/" +
		 * EventDBOper.DB_FOLDER + "/" + EventDBOper.MIDEA_FOLDER;
		 */

		String FolderPath = Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER;
		String reachbackFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + EventDBOper.REACHBACK_FOLDER;

	}

	private enum ConnectType {

		Bluetooth, USB
	}

	;

	//180717 추가
	public static boolean openMenu = false; //180712 메뉴키 활성화 해제 안되는 현상 방지

	private ConnectType mCnnctMode = ConnectType.USB;
	// ------------

	public LocationManager mLocationManager;
	// Location m_nowLocation = null;

	// Usb And Blutooth

	// Tab

	// 상단바 선언

	TextView Paired, Library, Alarm, Battery, GainstabilizattonTxt;

	public static String PairedStr;

	public static String LibraryStr;

	public static String AlarmStr;

	public static String BatteryStr;

	public static Intent intent;
	public static TabHost.TabSpec spec;
	public static TabHost tabHost;

	public static TabWidget TabWidget;

	private BatteryView mBatteryProgBar = null;

	public static int mCount = 0;
	public static int mSaveCount = 0;
	int mToastCount = 0;
	public static ArrayList<String> StrArraylist;

	// 하드웨어키 테스트 선언

	public static final int INPUT_HARDWARE_KEY = 100;

	public static final int HW_KEY_SHORT = 101;
	public static final int HW_KEY_LONG = 102;
	public static final int HW_KEY_DOUBLE = 103;

	public static final int TAB_END_NUMBER = 3;
	public static final int TAB_FIRST_NUMBER = 0;

	public static String strMsg = "Real Time";

	public static int FirstActivityCurrentTab = 0;

	public static boolean isHWUsbUnConnect = false;

	TimerTask ShutDownTimeTask;
	Timer ShutDownTimer;

	int sendGcValue = 33000;
	boolean IsSpcSaved = false;
	boolean temp = false;
	public int mTimeSpcCollect=0;
	public int mTimeSpcCollect1=0;
	TimerTask mSendGSTask;
	int mTimeTaskcount = 0;;
	Timer mSendGSTimer;
	String saveSpc = "";

	public interface Tab_Name {

		public static int Reatime = 0;
		public static int MenualID = 1;

		public static int SequentialMode = 2;
		public static int EnCalibration = 3;

		public static String RealTime_Str = "Real Time";
		public static String ManualID_Str = "Manual ID.";

		public static String SequentialMode_Str = "Sequential Mode";

		public static String En_Calibration_Str = "En.Calibration";
		public static String Background_Str = "Background";
	}

	;

	public interface HW_Key_Type {

		public static int SHORTPRESS = 101;
		public static int LONGPRESS = 102;
		public static int DOUBLECLICK = 103;

	}

	;

	public interface HW_Key {

		public int Left = 76;
		public int Right = 82;
		public int Up = 85;
		public int Down = 68;
		public int Enter = 77;
		public int Back = 66;

	}

	;

	public interface HW_Key_State {
		public int ActivityResetTrue = 1000;
		public int ActivityResetFalse = 1001;
		public int FocusA = 1002;
		public int FocusB = 1003;
		public int FocusC = 1004;
		public int FocusA_Down = 1005;
		public int FocusA_Up = 1006;
		public int FocusB_Down = 1007;
		public int FocusB_Up = 1008;
		public int FocusC_Left = 1009;
		public int FocusC_Right = 1010;

	}

	public interface Activity_Mode {

		public int SOURCE_ID_MAIN = 1200;

		public int SOURCE_ID_RUNNING = 1201;
		public int SOURCE_ID_RESULT = 1202;
		public int SOURCE_ID_RESULT_CAMERA = 1203;
		public int SEQUENTAL_MODE_RUNNING = 1204;

		public int BACKGROUND_MAIN = 1303;
		public int BACKGROUND_RUNNING = 1304;
		public int CALIBRATION_MAIN = 1405;
		public int CALIBRATION_RUNNING = 1406;

		public int REALTIME_MAIN = 1507;
		public int FIRST_ACTIVITY = 1608;
		public int NOT_FIRST_ACTIVITY = 1609;

		public int EVENTLOG_LIST_MAIN = 1711;

		public int SETUP_MAIN = 1812;

		public int AUTO_CALIBRATION = 1900;

		public int ID_RESULT_UN_CHECK_SELECT_MODE = 0;

		public int UN_EXCUTE_MODE = 1;

		public int EXCUTE_MODE = 0;

	}

	public interface Focus {

		public int ID_RESULT_MENU_A = 2200;
		public int ID_RESULT_MENU_B = 2201;
		public int ID_RESULT_MENU_C = 2202;

		public int ID_RESULT_MENU_A_ENTER = 2210;
		public int ID_RESULT_MENU_B_ENTER = 2211;
		public int ID_RESULT_MENU_C_SUB = 2212;

		public int ID_RESULT_MENU_C_PHOTO = 2220;
		public int ID_RESULT_MENU_C_VIDEO = 2221;
		public int ID_RESULT_MENU_C_VOICE = 2222;
		public int ID_RESULT_MENU_C_REMOVE_BTN = 2223;

	}

	interface GainState {

		int mIncrease = 0;
		int mDecrease = 1;
		int mNormal = 2;
		int mInrange = 3;
		int mNullValue = 4;

	}

	;

	public static int ACTIVITY_HW_KEY_ROOT_CHECK = Activity_Mode.FIRST_ACTIVITY;

	public static int ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;

	int realTimeSwitch = 0;

	// Activity 구분 선언

	public String ActivityCheck = "";

	int HW_Key_Double_Rock = 0;

	String specstr = SourceSample.CPS_30000;
	String specstr1;

    private WindowManager.LayoutParams params;
    private float brightness; // 밝기값은 float형으로 저장


	// 리스너부분

	LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {

			// m_nowLocation = location;
			if (MAPPING_VERSION & mCCSW_Service != null) {
				if (mCCSW_Service.Is_Connected()) {// set
					// location
					Location loc = location;
					MappingData data = new MappingData();
					data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
					data.InstrumentName = mDetector.InstrumentModel_Name;
					data.InstrumentMacAddress = mDetector.InstrumentModel_MacAddress;
					data.Doserate = mDetector.Get_Gamma_DoseRate_nSV();
					data.CPS = mDetector.MS.Get_TotalCount();

					mCCSW_Service.Set_Data(null, data);
				}
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	// ----------------------------------------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------------------------------------

	public class MainBCRReceiver extends MainBroadcastReceiver {

		@Override
		public void onReceive(Context context, android.content.Intent intent) {

			try {
				PreferenceDB prefDB = new PreferenceDB(getApplicationContext());

				String action = intent.getAction();
				switch (action) {

				case MSG_MANUAL_ID:
					int status = intent.getIntExtra(DATA_MANUAL_ID_STASTUS, 0);
					if (status == DATA_START) {
						mDetector.IsManualID = true;

						if (mDetector.IsSigmaThreshold)
							mDetector.Gamma_SigmaThreshold = 0;
						else
							mDetector.Gamma_Threshold = 0;

					} else if (status == DATA_END) {
						mDetector.IsManualID = false;

						mDetector.Finish_GammaEvent();
						if (mDetector.IsSigmaThreshold)
							mDetector.Gamma_SigmaThreshold = prefDB.Get_GammaThreshold_Sigma_From_pref();
						else
							mDetector.Gamma_Threshold = prefDB.Get_GammaThreshold_From_pref();

					} else if (status == DATA_CANCEL) {
						mDetector.IsManualID = false;

						mDetector.Cancel_Event();
						if (mDetector.IsSigmaThreshold)
							mDetector.Gamma_SigmaThreshold = prefDB.Get_GammaThreshold_Sigma_From_pref();
						else
							mDetector.Gamma_Threshold = prefDB.Get_GammaThreshold_From_pref();
					}

					break;

				case MSG_EN_CALIBRATION:
					Coefficients En_Coeff = (Coefficients) intent.getSerializableExtra(DATA_COEFFCIENTS);
					Coefficients Ch_Coeff = (Coefficients) intent.getSerializableExtra(DATA_CALIBRATION_PEAKS);

					if (En_Coeff.get_Coefficients()[0] == 0 || En_Coeff.get_Coefficients()[1] == 0)
						break;

					// if(!Send_GC_ToHW((int)Ch_Coeff.Get_Coefficients()[2])){
					// //out of bound :
					mDetector.Set_EnergyFittingArgument(En_Coeff.get_Coefficients());
					mPrefDB.Set_Calibration_Result(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());
					mPrefDB.Set_HW_ABC_From_pref(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());

					prefDB.Set_Calibration_Result(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());
					if (D)
						Log.d(TAG,
								"Receive Broadcast - Recalibration, DR: "
										+ NcLibrary.Channel_to_Energy(1024, En_Coeff.get_Coefficients()) + " ("
										+ En_Coeff.ToString() + " || " + Ch_Coeff.ToString() + ")");

					break;

				case MSG_REMEASURE_BG:
					Spectrum bg = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);
					mDetector.Real_BG = bg;

					if (mEventDBOper != null) {
						mDetector.Real_BG.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
						mDetector.Real_BG.setFWHM(mEventDBOper.Cry_Info.FWHM);

						mDetector.Real_BG.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);

						Vector<NcPeak> peakInfo_bg = new Vector<NcPeak>();
						peakInfo_bg = NewNcAnalsys.GetPPSpectrum_H(mDetector.Real_BG);
						mDetector.Real_BG.SetPeakInfo(peakInfo_bg);
					}

					prefDB.Set_BG_MeasuredRealAcqTime_From_pref(bg.Get_SystemElapsedTime().getTime() - 1000);
					prefDB.Set_BG_Date_From_pref(bg.Get_MesurementDate());
					prefDB.Set_BG_MeasuredAcqTime_From_pref(bg.Get_AcqTime());
					prefDB.Set_BG_On_pref(bg.ToInteger(), bg.Get_Ch_Size());

					if (D)
						Log.d(TAG, "Receive Broadcast - Remeasured background (" + bg.Get_SystemElapsedTime().getTime()
								+ "__" + bg.ToString() + ")");
					break;

				case MAIN_DATA_SEND1:
					// Battery = (TextView) findViewById(R.id.BatteryTxt);

					// String abc4 =
					// intent.getStringExtra(MainBCRReceiver.DATA_BATTERY);

					// Toast.makeText(mContext, abc, 1).show();

					// Battery.setText(abc4);

					break;

				case MSG_HEALTH_EVENT:

					if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

						int Hevent_status = intent.getIntExtra(DATA_EVENT_STATUS, Detector.EVENT_NONE);
						if (Hevent_status == Detector.EVENT_BEGIN) {
							// Input_BlutoothStatusIcon();
							Start_HealthAlarm();

						} else if (Hevent_status == Detector.EVENT_FINISH) {
							// Input_BlutoothStatusIcon();
							Stop_Alarm();
							if (mDetector.EVENT_STATUS != Detector.EVENT_NONE)
								Start_Alarm(false);
						}
					}
					break;

				case MSG_EVENT:
					if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
						int event_status = intent.getIntExtra(DATA_EVENT_STATUS, Detector.EVENT_NONE);
						EventData eventdb = (EventData) intent.getSerializableExtra(DATA_EVENT);

						if (event_status == Detector.EVENT_OFF) {
							mDetector.Init_Measure_Data();
							mDetector.EVENT_STATUS = Detector.EVENT_OFF;
							mDetector.EVENT_STATUS_N = Detector.EVENT_OFF;
							break;
						} else if (event_status == Detector.EVENT_ON) {
							mDetector.EVENT_STATUS = Detector.EVENT_NONE;
							mDetector.EVENT_STATUS_N = Detector.EVENT_NONE;
							break;
						}

						if (event_status == Detector.EVENT_BEGIN) {
							if (tabHost.getCurrentTab() == 0)
								Start_Alarm(false);

							if (eventdb.Event_Detector.matches(EventData.EVENT_GAMMA)) {
								GainstabilizattonTxt.setText("");

								if (mDetector.EVENT_STATUS_N == Detector.EVENT_NONE)
									Start_Alarm(false);
							} else if (eventdb.Event_Detector.matches(EventData.EVENT_NEUTRON)) {

								if (mDetector.EVENT_STATUS == Detector.EVENT_NONE)
									Start_Alarm(false);
							} else if (eventdb.Event_Detector.matches(EventData.EVENT_MANUAL_ID)) {
								GainstabilizattonTxt.setText("");
							}

						} else if (event_status == Detector.EVENT_ING) {
							if (tabHost.getCurrentTab() == 0)
								Start_Alarm(false);
						} else if (event_status == Detector.EVENT_FINISH) {
							Stop_Alarm();
						}

					}
					break;

				// Peak About III Method
				// YKIM 2018.2.26
				case MSG_GAIN_STABILIZATION:

					if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY
							|| ACTIVITY_STATE == Activity_Mode.AUTO_CALIBRATION) {

						if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_START) {

							GainstabilizattonTxt.setText("Stabilization in progress..");

						} else if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_END) {
							// mt.setText("");
							GainstabilizattonTxt.setText("");
							double K40Peak = (double) intent.getIntExtra(DATA_K40_PEAK, 0);

						double Old_calib_peaks[] = { prefDB.Get_CaliPeak1_From_pref(),
									prefDB.Get_CaliPeak2_From_pref(), prefDB.Get_CaliPeak3_From_pref() };

							if (K40Peak < 0)
								break;

							int resultGc = Send_GC_ToHW((int) K40Peak);
							double New_Peak1=0;
							double New_Peak2=0;
							double newK40Peak=0;
							String str="";
							Coefficients En_coeff;
							Coefficients Ch_coeff;
							Intent intent41;
							double[] FitParam = new double[3];

							switch (resultGc) {
							case 0: // <1%
								str = ("under1%  Old ch," + Old_calib_peaks[0] + "," + Old_calib_peaks[1] + "," + Old_calib_peaks[2] + ",findNewK40," + K40Peak + ",GCValueNew," + mDetector.mHW_GC + ",HV,"+mDetector.hv+"\n");
								//str = ("under1%  Old ch," + Old_calib_peaks[0] + "," + Old_calib_peaks[1] + "," + Old_calib_peaks[2] + ",findNewK40," + K40Peak + ",GCValueNew," + mDetector.mHW_GC +",HighVoltage,"+ "\n");
								NcLibrary.SaveText(str);
								//NcLibrary.SaveText_HNM(str);

								break;

							case 1: // 1% < < 2%
								
								
							/*	180725 bnc와 동일하게 수정
							 * double Old_K40_Ch = Old_calib_peaks[2];
								double Ratio = ((K40Peak - Old_K40_Ch) / Old_K40_Ch);
								New_Peak1 = Old_calib_peaks[0] + (Old_calib_peaks[0] * Ratio);
								New_Peak2 = Old_calib_peaks[1] + (Old_calib_peaks[1] * Ratio);*/


//								newK40Peak=K40Peak;	//current K40 peak
//								K40Peak = (double) mDetector.mHW_K40_FxiedCh;	// HW fixed K40 peak
//								New_Peak1 = mDetector.mHW_Cs137_FxiedCh1 ;
//								New_Peak2 = mDetector.mHW_Cs137_FxiedCh2;


						//		NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak, NcLibrary.CS137_PEAK1,
						//				NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);


								//180725 bnc와 동일하게 수정
								newK40Peak=K40Peak;	//current K40 peak
								K40Peak = (double) mDetector.mHW_K40_FxiedCh;	// HW fixed K40 peak
								New_Peak1 = mDetector.mHW_Cs137_FxiedCh1 ;
								New_Peak2 = mDetector.mHW_Cs137_FxiedCh2;


								/////////////////////////////////////////////
								 //파일안에 문자열 쓰기
								str=("over1% Old ch," + Old_calib_peaks[0]+","+Old_calib_peaks[1]+","+Old_calib_peaks[2]+","+"findNewK40,"+newK40Peak+ ",GCValueNew," + mDetector.mHW_GC + ",HV,"+mDetector.hv+"\n");
								 NcLibrary.SaveText(str);
								//NcLibrary.SaveText_HNM(str);
								 //////////////////////////////////////////

								FitParam = new double[3];

								NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak, NcLibrary.CS137_PEAK1,
										NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);


								En_coeff = new Coefficients(FitParam);
								Ch_coeff = new Coefficients(new double[] { New_Peak1, New_Peak2, K40Peak });

								mDetector.Set_EnergyFittingArgument(En_coeff.get_Coefficients());
								prefDB.Set_Calibration_Result(En_coeff.get_Coefficients(), Ch_coeff.get_Coefficients());

								//intent41 = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
								//intent41.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
								//intent41.put Extra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
								//LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent41);

								//MainActivity.mDetector.Background_GainStabilization(Old_K40_Ch, K40Peak);
								mDetector.IsGainStb = true;
								break;
							case 2: // >2% Send GC
								
								/*
								double Old_K40_Ch = Old_calib_peaks[2];
								double Ratio = ((K40Peak - Old_K40_Ch) / Old_K40_Ch);
								double New_Peak1 = Old_calib_peaks[0] + (Old_calib_peaks[0] * Ratio);
								double New_Peak2 = Old_calib_peaks[1] + (Old_calib_peaks[1] * Ratio);
								 */
								newK40Peak=K40Peak;	//current K40 peak
								K40Peak = (double) mDetector.mHW_K40_FxiedCh;	// HW fixed K40 peak
								New_Peak1 = mDetector.mHW_Cs137_FxiedCh1 ;
								New_Peak2 = mDetector.mHW_Cs137_FxiedCh2;

								FitParam = new double[3];

								NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak, NcLibrary.CS137_PEAK1,
										NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);

								/////////////////////////////////////////////
								// 파일안에 문자열 쓰기
								str=("over2%, Old ch,"+Old_calib_peaks[0]+","+Old_calib_peaks[1]+","+Old_calib_peaks[2]+","+"findNewK40,"+newK40Peak+ ",GCValueNew," + mDetector.mHW_GC + ",HV,"+mDetector.hv+"\n");
								NcLibrary.SaveText(str);
								//NcLibrary.SaveText_HNM(str);
								 //////////////////////////////////////////

								En_coeff = new Coefficients(FitParam);

								//Coefficients Ch_coeff = new Coefficients(	new double[] { New_Peak1, New_Peak2, K40Peak });
								Ch_coeff = new Coefficients(new double[] { New_Peak1, New_Peak2, K40Peak });

								mDetector.Set_EnergyFittingArgument(En_coeff.get_Coefficients());
								prefDB.Set_Calibration_Result(En_coeff.get_Coefficients(), Ch_coeff.get_Coefficients());

								//intent41 = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
								//intent41.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
								//intent41.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
								//LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent41);

								//MainActivity.mDetector.Background_GainStabilization(Old_K40_Ch, K40Peak);
								mDetector.IsGainStb = true;
								if (D)
									Log.d(TAG, "Receive Broadcast - Gain stabilization ( To Fixed K40 Ch )");

								break;
							}

						} else if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_CANCEL) {

							GainstabilizattonTxt.setText("");
						}
					}
					break;
				
				/* Peak About II Method 
				// YKIM 2018.2.26
				case MSG_GAIN_STABILIZATION:

					if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY
							|| ACTIVITY_STATE == Activity_Mode.AUTO_CALIBRATION) {

						if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_START) {

							GainstabilizattonTxt.setText("Stabilization in progress..");

						} else if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_END) {
							// mt.setText("");
							GainstabilizattonTxt.setText("");
							double K40Peak = (double) intent.getIntExtra(DATA_K40_PEAK, 0);

							double Old_calib_peaks[] = { prefDB.Get_CaliPeak1_From_pref(),
									prefDB.Get_CaliPeak2_From_pref(), prefDB.Get_CaliPeak3_From_pref() };

							if (K40Peak < 0)
								break;

							int resultGc = Send_GC_ToHW((int) K40Peak);
							double New_Peak1=0;
							double New_Peak2=0;
							String str="";
							Coefficients En_coeff;
							Coefficients Ch_coeff;
							Intent intent41;
							double[] FitParam = new double[3];
							
							switch (resultGc) {
							case 0: // <1%
								break;

							case 1: // 1% < < 2%
								
								double Old_K40_Ch = Old_calib_peaks[2];

								double Ratio = ((K40Peak - Old_K40_Ch) / Old_K40_Ch);
								New_Peak1 = Old_calib_peaks[0] + (Old_calib_peaks[0] * Ratio);
								New_Peak2 = Old_calib_peaks[1] + (Old_calib_peaks[1] * Ratio);
								
								NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak, NcLibrary.CS137_PEAK1,
										NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);

								
								/////////////////////////////////////////////
								// 파일안에 문자열 쓰기
								str=("under2, Old ch,"+Old_calib_peaks[0]+","+Old_calib_peaks[1]+","+Old_calib_peaks[2]+","+"newK40,"+K40Peak+",Ch1 Ch2 Ch3,"+New_Peak1+","+New_Peak2+","+K40Peak+"\n");
								NcLibrary.SaveText(str);
								 //////////////////////////////////////////
					
								
								En_coeff = new Coefficients(FitParam);
								Ch_coeff = new Coefficients(
										new double[] { New_Peak1, New_Peak2, K40Peak });
								
								intent41 = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
								intent41.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
								intent41.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
								LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent41);

								//MainActivity.mDetector.Background_GainStabilization(Old_K40_Ch, K40Peak);
								mDetector.IsGainStb = true;
								
							case 2: // 1% < < 2%
								
								
								//double Old_K40_Ch = Old_calib_peaks[2];
								//double Ratio = ((K40Peak - Old_K40_Ch) / Old_K40_Ch);
								//double New_Peak1 = Old_calib_peaks[0] + (Old_calib_peaks[0] * Ratio);
								//double New_Peak2 = Old_calib_peaks[1] + (Old_calib_peaks[1] * Ratio);
							
								double newK40Peak=K40Peak;	//current K40 peak
								K40Peak = (double) mDetector.mHW_K40_FxiedCh;	// HW fixed K40 peak
								New_Peak1 = mDetector.mHW_Cs137_FxiedCh1 ;
								New_Peak2 = mDetector.mHW_Cs137_FxiedCh2;
								
								
								NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak, NcLibrary.CS137_PEAK1,
										NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);

								/////////////////////////////////////////////
								// 파일안에 문자열 쓰기
								str=("over2, Old ch,"+Old_calib_peaks[0]+","+Old_calib_peaks[1]+","+Old_calib_peaks[2]+","+"newK40,"+newK40Peak+",Ch1 Ch2 Ch3,"+New_Peak1+","+New_Peak2+","+K40Peak+"\n");
								NcLibrary.SaveText(str);
								 //////////////////////////////////////////
								
								En_coeff = new Coefficients(FitParam);
								
								//Coefficients Ch_coeff = new Coefficients(	new double[] { New_Peak1, New_Peak2, K40Peak });
								Ch_coeff = new Coefficients(new double[] { New_Peak1, New_Peak2, newK40Peak });
								
								intent41 = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
								intent41.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
								intent41.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
								LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent41);

								//MainActivity.mDetector.Background_GainStabilization(Old_K40_Ch, K40Peak);
								mDetector.IsGainStb = true;
								if (D)
									Log.d(TAG, "Receive Broadcast - Gain stabilization ( To Fixed K40 Ch )");
								
								
							}
							
						} else if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_CANCEL) {

							GainstabilizattonTxt.setText("");
						}
					}
					break;
					*/
				case START_SETUP_MODE:
					// mMainUsbService.write(MESSAGE_END_HW);
					if (mDetector.Is_Event())
						mDetector.Finish_GammaEvent();
					mDetector.Finish_NeutronEvent();
					InIt_SPC_Data();

					if (MAPPING_VERSION & mCCSW_Service != null) {
						if (mCCSW_Service.Is_Connected()) {// set location

							Location loc = Get_Location();
							MappingData data = new MappingData();
							data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
							data.InstrumentName = "Setup mode";
							data.InstrumentMacAddress = mDetector.InstrumentModel_MacAddress;
							data.Doserate = 0;

							mCCSW_Service.Set_Data(null, data);
						}
					}

					break;

				case START_ID_MODE:

					InIt_SPC_Data();
					// mDetector.mGain_elapsedTime = Detector.GAIN_START_IN_SEC;
					// mService.write(MESSAGE_START_HW);
					Update_StatusBar();
					break;

				case MSG_TAB_ENABLE:
					// TAB_ENABLE_CHECK = true;
					tabEnable();
					break;

				case MSG_TAB_DISABLE:
					// TAB_ENABLE_CHECK = false;
					tabDisable();
					break;

				case MSG_USB_CONNECTED:

					try {
						// Toast.makeText(getApplicationContext(),
						// "MSG_USB_CONNECTED1", Toast.LENGTH_SHORT).show();
						// mMainUsbService = new MainUsbService(mContext,
						// mHandler);

						// mMainUsbService.usbStop();

						ConnectUsb();
						// Toast.makeText(getApplicationContext(),
						// "MSG_USB_CONNECTED2", Toast.LENGTH_SHORT).show();
						// mIsUsbConnect = true;

						if(MainActivity.ACTIVITY_STATE  != Activity_Mode.SOURCE_ID_RESULT)
						{
							Update_StatusBar();
						}

						// Update_All_DetectorInfo();

						// Intent send_gs2 = new
						// Intent(MainBroadcastReceiver.MSG_START_ID_MODE);

						// LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs2);
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);

					}

					// mHandler.obtainMessage(USB_START).sendToTarget();

					break;

				case MSG_SOURCE_ID_RESULT:

					sourceIdResult();

					IDspectrumActivity.SOURCE_ID_RESULT_MODE = true;

					break;

				case MSG_SOURCE_ID_RESULT_CANCEL:

					sourceIdResultCancel();
					tabEnable();

					SetFristActiviyMode();
					break;

				case MSG_USB_DISCONNECT:

					Stop_Alarm();
					Stop_Vibrate();
					break;
				case MSG_FIXED_GC_SEND:

//					NewGC = Integer.toString(mDetector.mHW_GC);
//
//					byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();
//
//					byte[] ss = new byte[5];
//					ss[0] = 'G';
//					ss[1] = 'C';
//					//
//					if (GcBytes.length == 1) {
//						ss[2] = 0;
//						ss[3] = GcBytes[2];
//					} else if (GcBytes.length == 3) {
//						ss[2] = GcBytes[1];
//						ss[3] = GcBytes[2];
//					} else if (GcBytes.length == 2) {
//						ss[2] = GcBytes[0];
//						ss[3] = GcBytes[1];
//					}
//
//					///
//					ss[4] = (byte) Byte.valueOf((byte) 1);
//
//					try {
//
//						if (mMainUsbService != null) {
//							mMainUsbService.write(ss);
//						}
//
//						if (mService != null) {
//							mService.write(ss);
//						}
//					} catch (Exception e) {
//						NcLibrary.Write_ExceptionLog(e);
//					}
//					//////////////////////////////////
//					Thread.sleep(1000);
//					
					int[] caliChInfo= {0,0,0,0};

					caliChInfo=NcLibrary.GetTextCli(MainActivity.FilenameCaliInfo, 4);
					if(caliChInfo[0] >0)
					{	byte[] ss2 = new byte[10];
						ss2[0] = 'C';
						ss2[1] = 'S';
						String str="";
						//Cs137 32kev
						str=Integer.toString(caliChInfo[2]);
						byte[] GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
						if(GcBytes2.length==1)
						{	ss2[2] = GcBytes2[0];
							ss2[3] =0;
						}else
						{ 	ss2[2] =GcBytes2[1];
							ss2[3] =  GcBytes2[0];
						}
						//Cs137 662kev
						str=Integer.toString(caliChInfo[0]);
						GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
						if(GcBytes2.length==1)
						{	ss2[4] = GcBytes2[0];
							ss2[5] = 0;
						}else
						{ 	ss2[4] = GcBytes2[1];
							ss2[5] = GcBytes2[0];
						}
						//K40
						str=Integer.toString(caliChInfo[1]);
						GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
						if(GcBytes2.length==1)
						{	ss2[6] = GcBytes2[0];
							ss2[7] = 0;
						}else
						{ 	ss2[6] =GcBytes2[1];
							ss2[7] = GcBytes2[0];
						}
						//GC
						str=Integer.toString(caliChInfo[3]);
						GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();

						/////////////////
						if (GcBytes2.length == 1) {
							ss2[8] = GcBytes2[2];
							ss2[9] = 0;
						} else if (GcBytes2.length == 3) {
							ss2[8] = GcBytes2[2];
							ss2[9] = GcBytes2[1];
						} else if (GcBytes2.length == 2) {
							ss2[8] = GcBytes2[1];
							ss2[9] = GcBytes2[0];
						}





						try {

							if (mMainUsbService != null) {
								mMainUsbService.write(ss2);
							}

							if (mService != null) {
								mService.write(ss2);
							}
						} catch (Exception e) {
							NcLibrary.Write_ExceptionLog(e);
						}
					}

					break;
					////////////////////////////////
					case MSG_FIXED_GC_SEND1:

						if(!temp)
						{
						//	temp = true;
							int[] caliChInfo1= {0,0,0,0};

							caliChInfo1=NcLibrary.GetTextCli(MainActivity.FilenameCaliInfo, 4);
							if(caliChInfo1[0] >0)
							{	byte[] ss2 = new byte[10];
								ss2[0] = 'C';
								ss2[1] = 'S';
								String str="";
								//Cs137 32kev
								str=Integer.toString(caliChInfo1[2]);
								byte[] GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
								if(GcBytes2.length==1)
								{	ss2[2] = GcBytes2[0];
									ss2[3] =0;
								}else
								{ 	ss2[2] =GcBytes2[1];
									ss2[3] =  GcBytes2[0];
								}
								//Cs137 662kev
								str=Integer.toString(caliChInfo1[0]);
								GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
								if(GcBytes2.length==1)
								{	ss2[4] = GcBytes2[0];
									ss2[5] = 0;
								}else
								{ 	ss2[4] = GcBytes2[1];
									ss2[5] = GcBytes2[0];
								}
								//K40
								str=Integer.toString(caliChInfo1[1]);
								GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
								if(GcBytes2.length==1)
								{	ss2[6] = GcBytes2[0];
									ss2[7] = 0;
								}else
								{ 	ss2[6] =GcBytes2[1];
									ss2[7] = GcBytes2[0];
								}
								//GC
								sendGcValue=40000;
								str=Integer.toString(sendGcValue);
								GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();

								/////////////////
								if (GcBytes2.length == 1) {
									ss2[8] = GcBytes2[2];
									ss2[9] = 0;
								} else if (GcBytes2.length == 3) {
									ss2[8] = GcBytes2[2];
									ss2[9] = GcBytes2[1];
								} else if (GcBytes2.length == 2) {
									ss2[8] = GcBytes2[1];
									ss2[9] = GcBytes2[0];
								}
								else
								{




								if(sendGcValue <=40050)
								{
									try {

										if (mMainUsbService != null) {
											mMainUsbService.write(ss2);
											//String str2=("send, "+ ", gc,"+ sendGcValue+"\n");
											//sendGcValue = sendGcValue +50;
											//IsSpcSaved = false;
											//NcLibrary.SaveText_HNM(str2);

											mSendGSTask = new TimerTask() {

												@Override
												public void run()
												{

													if (mTimeTaskcount > 3)
													{
														if (mSendGSTask != null)
														{
															mSendGSTask.cancel();
														}
													}
													mMainUsbService.write(MainActivity.MESSAGE_GS_HW);
													mTimeTaskcount++;

												}
											};
											mSendGSTimer = new Timer();
											mSendGSTimer.schedule(mSendGSTask, 0, 1000);


										}

										if (mService != null) {
											mService.write(ss2);
										}
									} catch (Exception e) {
										NcLibrary.Write_ExceptionLog(e);
									}
								}
					/*		else
							{
								String str2=("gc 30000 reset, \n");
								sendGcValue = 30000;
							}*/

							}
						}
						byte[] ss1 = new byte[5];

							////////////////////////////////
							// HH200
						sendGcValue = 33000;
							NewGC = Integer.toString(sendGcValue);
							byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();

							ss1[0] = 'G';
							ss1[1] = 'C';
							if (GcBytes.length == 1) {
								ss1[2] = 0;
								ss1[3] = GcBytes[2];
							} else if (GcBytes.length == 3) {
								ss1[2] = GcBytes[1];
								ss1[3] = GcBytes[2];
							} else if (GcBytes.length == 2) {
								ss1[2] = GcBytes[0];
								ss1[3] = GcBytes[1];
							}
							ss1[4] = (byte) Byte.valueOf((byte) 1);


						if(sendGcValue <=40050)
						{
							try {

								if (mMainUsbService != null) {
									mMainUsbService.write(ss1);
									//sendGcValue = sendGcValue +50;
									//IsSpcSaved = false;
									//String str2=("send, "+ ", gc,"+ sendGcValue+"\n");
									//NcLibrary.SaveText_HNM(str2);
									mTimeTaskcount = 0;

									mSendGSTask = new TimerTask() {

										@Override
										public void run()
										{

											if (mTimeTaskcount > 3)
											{
												if (mSendGSTask != null)
												{
													mSendGSTask.cancel();
												}
											}
											mMainUsbService.write(MainActivity.MESSAGE_GS_HW);
											mTimeTaskcount++;

										}
									};
									mSendGSTimer = new Timer();
									mSendGSTimer.schedule(mSendGSTask, 0, 1000);


								}


							} catch (Exception e) {
								NcLibrary.Write_ExceptionLog(e);
							}

						}
						}
						break;

				}

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}

		}
	}

	MainBCRReceiver mMainBCR = new MainBCRReceiver();

	public interface CHECK {

		public boolean ACTIVITY_RESET = true;

	}

	TabWidget hello;
	int tabBodyWidth = 0;

	int tabBodyHeight = 0;

	LinearLayout tabBottomLayout, tabBodyLayout;

	public void requestPermission()
	{



		if(!mDebug.IsDebugMode)
		{
			String AppName = "Launcher_1_2_1"; //180723 1.1.6로 런처업데이트
			if (!NcLibrary.CheckedHH200Launcher(mContext, AppName))
			{
				Intent Intent = new Intent(MainActivity.this, Guide.class);
				Intent.putExtra(Guide.GuideMode.GetGuideModeTitle, Guide.GuideMode.UpdateLauncher);
				Intent.putExtra(Guide.GuideMode.GetAppFileName, AppName);
				startActivityForResult(Intent, 2);
				finish();
			}
			else
			{
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				ResolveInfo defaultLauncher = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

				String nameOfLauncherPkg = defaultLauncher.activityInfo.packageName;

				if (!"ah.hathi.simplelauncher".equals(nameOfLauncherPkg))
				{
					Intent Intent = new Intent(MainActivity.this, Guide.class);
					Intent.putExtra(Guide.GuideMode.GetGuideModeTitle, Guide.GuideMode.SetLauncher);
					startActivityForResult(Intent, 2);
					finish();
				}
			}

		}




		File mIsEnablesdcard = new File("/sdcard");
		if (!mIsEnablesdcard.exists())
		{
			try {
				Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "am start -a android.intent.action.ACTION_REQUEST_SHUTDOWN --ez KEY_CONFIRM true --activity-clear-task"});
				proc.waitFor();
			} catch (Exception ex) {
				NcLibrary.Write_ExceptionLog(ex);
			}
			finish();
		}

		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		mAlarmSound = MediaPlayer.create(this, R.raw.beep1);


		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			turnGPSOn();
		}
		// Check Bluetooth Module
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBTAdapter == null)
		{
			Toast.makeText(this, getResources().getString(R.string.bt_not_enabled_leaving), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		Battery = (TextView) findViewById(R.id.BatteryTxt);
		mBatteryProgBar = (BatteryView) findViewById(R.id.betterView_ProgressBar);
		GainstabilizattonTxt = (TextView) findViewById(R.id.Gainstabilizatton);
		Battery.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				openOptionsMenu();
			}
		});

		mBatteryProgBar.Set_Value(12);
		mBatteryProgBar.invalidate();
		Battery.setText("0%");

		CreateMediaFile();

        //180823 reachback db
        // DB 쓸 준비
        reachbackDB= new DBMng(MainActivity.this);
        reachbackDB.GetInst(MainActivity.this).Open();

		mNormalDB = new NormalDB();
		mNormalDB.start();
		mEventDB = new EventDBOper();
		mEventDB.setHandler(mHandler);

        if (!mEventDB.Get_WroteDB_version().matches(mEventDB.DB_verion))
        {
            Check_AndMake_EventDB_VersionFile();
        }

        Check_AndMake_IsoLibraryFile();
        Check_AndMake_DeviceNameFile(true);

        Update_StatusBar();
		mCurrentConnectMode = mPrefDB.Get_IsConnect_UsbMode_From_pref();
		mChangeConnectMode = mCurrentConnectMode;

		if (mPrefDB.Get_IsConnect_UsbMode_From_pref() == false)
		{
			BluetoothListExcute();
		}
		else
		{
			mMainUsbService = new MainUsbService(mContext, mHandler);
		}

		if (mDebug.IsDebugMode)
		{
			StartVisualConnect();
			VolumeDown();
			DefaultSettingCalAndMail(mPrefDB);

			if (mDebug.IsMailDefaultSetting)
			{
				NcLibrary.DebugModeSettingMail(mPrefDB);
			}

		} else
		{
			VolumeUp();
			DefaultSettingCalAndMail(mPrefDB);
		}

		mEventDBOper = new EventDBOper();
		mEventDBOper.Set_Crytal_Info(Integer.toString(HwPmtProperty_Code.NaI_3x3));
		mDetector.Set_PmtProperty(Detector.HwPmtProperty_Code.NaI_3x3);

		Update_All_DetectorInfo();
		BrodcastDeclare();




		mPrefDB.Set_sender_Server(Config.Mail.MAIL_SERVER);
		mPrefDB.Set_sender_Port(Config.Mail.MAIL_PORT);
		mPrefDB.Set_sender_pw(Config.Mail.MAIL_PASSWD);
		mPrefDB.Set_sender_email(Config.Mail.MAIL_ACCOUNT);

		//맨처음 깔았을때 이메일 변경
		if (mPrefDB.Get_EmailFirst().equals("Y"))
		{
			mPrefDB.Set_recv_address(Config.Mail.MAIL_RECEIVER);

			mPrefDB.Set_EmailFirst("N");
		}




	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Settings.System.canWrite(this))
			{
				//Toast.makeText(this, "onCreate: Already Granted", Toast.LENGTH_SHORT).show();
			}
			else
			{
				//Toast.makeText(this, "onCreate: Not Granted. Permission Requested", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + this.getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}


		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		mHnderUncaughException = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new CUncaughtExceptionHandlerApp()); //오류 종료시 로그 기록
		mContext = this;
		isHWUsbUnConnect = false;

        // 화면 정보 불러오기
        params = getWindow().getAttributes();
		setContentView(R.layout.maintab);

		mPrefDB = new PreferenceDB(this);
		mDetector = new Detector(mContext);

		// 탭호스트에 붙일 각각의 탭스펙을 선언 ; 각 탭의 메뉴와 컨텐츠를 위한 객체
		// 각탭에서 사용할 인텐트 선언
		Resources res = getResources(); // 리소스 객체 생성
		tabHost = getTabHost(); // 탭을 붙이기위한 탭호스객체선언
		TabWidget = getTabWidget();
		tabHost.clearAllTabs();

		// 인텐트 생성
		intent = new Intent().setClass(this, RealTimeActivity.class);
		// 각 탭의 메뉴와 컨텐츠를 위한 객체 생성
		spec = tabHost.newTabSpec(Tab_Name.RealTime_Str).setIndicator(Tab_Name.RealTime_Str).setContent(intent);

		tabHost.addTab(spec);

		Spectrum input_spc = (mDetector.mGamma_Event == null) ? mDetector.MS.ToSpectrum(): mDetector.mGamma_Event.MS.ToSpectrum();
		tabHost.addTab(tabHost.newTabSpec(Tab_Name.ManualID_Str).setIndicator(Tab_Name.ManualID_Str)
				.setContent(new Intent(this, IDspectrumActivity.class)
						.putExtra(IDspectrumActivity.EXTRA_SPECTRUM, input_spc)
						.putExtra(IDspectrumActivity.EXTRA_MANUAL_ID_GOAL_TIME, mPrefDB.Get_ManualID_DefaultTime())
						.putExtra(IDspectrumActivity.EXTRA_MANUAL_ID_ADJUST_TIME, mPrefDB.Get_ManualID_AdjustTime())
						.putExtra(IDspectrumActivity.EXTRA_SEQ_ACQTIME, mPrefDB.Get_SequenceMode_acqTime_From_pref())
						.putExtra(IDspectrumActivity.EXTRA_SEQ_REPEAT, mPrefDB.Get_SequenceMode_Repeat_From_pref())
						.putExtra(IDspectrumActivity.EXTRA_SEQ_PAUSE_TIME,mPrefDB.Get_SequenceMode_PauseTime_From_pref())
						.putExtra(IDspectrumActivity.EXTRA_SV_UNIT, mDetector.IsSvUnit)
						.putExtra(IDspectrumActivity.ACTIVTY, "hellohello")));

		tabHost.addTab(tabHost.newTabSpec(Tab_Name.SequentialMode_Str).setIndicator(Tab_Name.SequentialMode_Str)
				.setContent(new Intent(this, SequentialActivity.class)
						.putExtra(IDspectrumActivity.EXTRA_SPECTRUM, input_spc)
						.putExtra(IDspectrumActivity.EXTRA_MANUAL_ID_GOAL_TIME, mPrefDB.Get_ManualID_DefaultTime())
						.putExtra(IDspectrumActivity.EXTRA_MANUAL_ID_ADJUST_TIME, mPrefDB.Get_ManualID_AdjustTime())
						.putExtra(IDspectrumActivity.EXTRA_SEQ_ACQTIME, mPrefDB.Get_SequenceMode_acqTime_From_pref())
						.putExtra(IDspectrumActivity.EXTRA_SEQ_REPEAT, mPrefDB.Get_SequenceMode_Repeat_From_pref())
						.putExtra(IDspectrumActivity.EXTRA_SEQ_PAUSE_TIME,mPrefDB.Get_SequenceMode_PauseTime_From_pref())
						.putExtra(IDspectrumActivity.EXTRA_SV_UNIT, mDetector.IsSvUnit)
						.putExtra(IDspectrumActivity.ACTIVTY, "hellohello")));

		tabHost.setCurrentTab(0); // 현재화면에 보여질 탭의 위치를 결정
		tabHost.setEnabled(true);
		tabHost.setOnTabChangedListener(this);
		//tabEnable();

		if (mPrefDB.Get_SequenceMode_From_pref())
		{
			tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
		} else {

			tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
		}

		tabHost.getTabWidget().getChildAt(1).setOnTouchListener(this);



		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{

			// Here, thisActivity is the current activity
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {

				if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
					Log.e("Main", "퍼미션 수락 거절");
					Toast.makeText(getApplicationContext(), "권한을 설정해 주셔야 앱이 원할하게 돌아갑니다.", Toast.LENGTH_LONG).show();
					ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_NETWORK_STATE,
							android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO}, 0);
				} else {
					//권한 요청 dlg 띄움
					ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_NETWORK_STATE,
							android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO}, 0);

					// 필요한 권한과 요청코드 넣고, 요청에 대한 결과 받아야 함.
					Log.e("Main", "퍼미션 요청");


				}
			} else
			{
				requestPermission();
			}
		}
		else
		{
			requestPermission();
		}

	}

	public void VolumeDown() {

		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		audio.setStreamVolume(AudioManager.STREAM_RING, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0),
				AudioManager.FLAG_PLAY_SOUND);

		audio.setStreamVolume(AudioManager.STREAM_SYSTEM,
				(int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);

		audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0),
				AudioManager.FLAG_PLAY_SOUND);

		audio.setStreamVolume(AudioManager.STREAM_ALARM, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0),
				AudioManager.FLAG_PLAY_SOUND);

		audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
				(int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);

	}

	@Override
	public void onRequestPermissionsResult(int code, String per[], int[] res) {
		switch (code) {
			case 0:
				if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED &&
						res[1] == PackageManager.PERMISSION_GRANTED &&
						res[2] == PackageManager.PERMISSION_GRANTED  &&
						res[3] == PackageManager.PERMISSION_GRANTED  &&
						res[4] == PackageManager.PERMISSION_GRANTED  &&
						res[5] == PackageManager.PERMISSION_GRANTED  )
				{
					// 권한 허가 완료. 해당 작업 진행
					requestPermission();
				}


				break;
		}
	}

	// 핸들러부분

	final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {

				case MainService.STATE_CONNECTED:
					try {
						byte[] GetGC = { 'G', 'Q' };

						mService.write(GetGC);

						Intent send_gs = new Intent(MainBroadcastReceiver.MSG_BLUETOOTH_CONNECTED);

						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;

				case MainUsbService.USB_DISCONNECTED:

					// SetDoubleConnectCheck(false);

					// SetDoubleConnectCheck(false);

					CONNECT_CHECK = false;
					// End_Animation_StatusIcon();

					// m_GammaGuage_Panel.Stop();

					Reset_Detector();

					// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					// m_startBtn.setVisibility(View.INVISIBLE);
					// m_Bluetooth_Status.setImageResource(0);
					// mt.setText("");

					Intent send_gs1 = new Intent(MainBroadcastReceiver.UPDATE_NEUTRONCPS);
					send_gs1.putExtra(MainBroadcastReceiver.UPDATE_NEUTRONCPS_TEXT, -1);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs1);

					// Update_StatusBar();
					Init_stabilization();

					// --===--
					Intent intent1 = new Intent(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
					LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent1);
					// --===--

					try {

						mMainUsbService.usbStop();
						mMainUsbService.write(MESSAGE_END_HW);
						if (mDetector.Is_Event())
							mDetector.Finish_GammaEvent();
						mDetector.Finish_NeutronEvent();
						InIt_SPC_Data();

						if (MAPPING_VERSION & mCCSW_Service != null) {
							if (mCCSW_Service.Is_Connected()) {// set
								// location

								Location loc = Get_Location();
								MappingData data = new MappingData();
								data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
								data.InstrumentName = "Setup mode";
								data.InstrumentMacAddress = mDetector.InstrumentModel_MacAddress;
								data.Doserate = 0;

								mCCSW_Service.Set_Data(null, data);
							}
						}
						// mMainUsbService.usbStart();

						// Update_All_DetectorInfo();
						Intent send_gs2 = new Intent(MainBroadcastReceiver.START_SETUP_MODE);

						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs2);

						// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

						// ShutDown();

					} catch (NullPointerException e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;
				case MainUsbService.USB_CONNECTED:
					try {

						// SetDoubleConnectCheck(true);

						CONNECT_CHECK = true;
						// mMainUsbService.usbStop();

						mMainUsbService.usbStart();

						Update_All_DetectorInfo();
						Intent send_gs2 = new Intent(MainBroadcastReceiver.MSG_START_ID_MODE);

						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs2);

						// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;

				case MainService.STATE_CONNECTING:
					// 1

					// SetDoubleConnectCheck(true);
					try {

						Show_ProgressDlg(getResources().getString(R.string.wait_while_connect));
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}

					break;
				case MainService.STATE_LISTEN:
					try {

						// m_startBtn.setVisibility(View.INVISIBLE);
						// m_Bluetooth_Status.setImageResource(mDevice_Image_off);
						Dismiss_ProgressDlg();

						// Toast.makeText(getApplicationContext(), "블루투스에 접근",
						// 1).show();
						// 1
						// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;
				case MainService.STATE_NONE:
					try {

						// SetDoubleConnectCheck(false);

						Intent send_gs = new Intent(MainBroadcastReceiver.MSG_BLUETOOTH_DISCONNECT);

						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
						CONNECT_CHECK = false;
						// 1
						// m_startBtn.setVisibility(View.INVISIBLE);
						// m_Bluetooth_Status.setImageResource(mDevice_Image_off);
						Dismiss_ProgressDlg();
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;
				case MainService.STATE_LOST:
					try {
						// SetDoubleConnectCheck(false);

						CONNECT_CHECK = false;
						// End_Animation_StatusIcon();

						// m_GammaGuage_Panel.Stop();

						Reset_Detector();

						// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						// m_startBtn.setVisibility(View.INVISIBLE);
						// m_Bluetooth_Status.setImageResource(0);
						// mt.setText("");

						Intent send_gs = new Intent(MainBroadcastReceiver.UPDATE_NEUTRONCPS);
						send_gs.putExtra(MainBroadcastReceiver.UPDATE_NEUTRONCPS_TEXT, -1);
						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

						// Update_StatusBar();
						Init_stabilization();

						// --===--
						Intent intent = new Intent(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
						LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
						// --===--
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;
				}
				break;
			/// -------------------------------------------
			case MESSAGE_READ_GC:
				try {

					/*
					 * mDetector.mHW_GC = msg.arg1; mDetector.mHW_K40_FxiedCh = msg.arg2;
					 * mDetector.mDetType = Integer.valueOf((String) msg.obj);
					 */
					GCData mGCData = new GCData();
					mGCData = (GCData) msg.obj;

					mDetector.mHW_GC = mGCData.GC;
					mDetector.mHW_FixedGC=mGCData.GC;
					mDetector.mHW_K40_FxiedCh = mGCData.K40_Ch;
					mDetector.mHW_Cs137_FxiedCh1 = mGCData.Cs137_Ch1;
					mDetector.mHW_Cs137_FxiedCh2 = mGCData.Cs137_Ch2;
					mDetector.mCrtstalType =  mGCData.DetType;
					int[] HWinfo= {mDetector.mHW_Cs137_FxiedCh1,mDetector.mHW_Cs137_FxiedCh2,mDetector.mHW_K40_FxiedCh ,mDetector.mHW_GC,mGCData.DetType};
					//NcLibrary.SaveTextCali(HWinfo,MainActivity.FilenameCurCaliInfo, 5);
					
					/*..........................
					 * Hung.18.03.05
					 * Added Code to new algorithm
					 */
					//////////////////
					//for test to apply calirbation when get the GCdata
					double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,
							(double) mGCData.K40_Ch };
					double[] FitParam = new double[3];

					NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2,
							NcLibrary.K40_PEAK, FitParam);
					mPrefDB.Set_Calibration_Result(FitParam, PeakCh);
					//////////////////////////////////////////////

                    String str = "ch : "+PeakCh[0]+", "+PeakCh[1]+ ", "+PeakCh[2]+" a :"+FitParam[0]+" b : "+FitParam[1]+" c : "+FitParam[2]+" GC : "+mDetector.mHW_GC+ " DetType : "+mGCData.DetType +"\n";
                    //NcLibrary.SaveText1(MainActivity.FilenameCurCaliInfo, str);
                    NcLibrary.SaveTextCali1(str,MainActivity.FilenameCurCaliInfo);


					mEventDBOper = new EventDBOper();
					mEventDBOper.Set_Crytal_Info(Integer.toString(mGCData.DetType));
					Write_HW_Calibration_Result(mGCData);
					
					/*
					mDetector.mHW_GC = msg.arg1;
					mDetector.mHW_K40_FxiedCh = msg.arg2;
					*/
					mDetector.Set_PmtProperty(mGCData.DetType);

					// mDetector.Set_PmtProperty(67);
					if (!mDebug.IsDebugMode) {

						mService.write(MESSAGE_START_HW);
					}

					if (D)
						Log.e("GC", "Recv GC - " + mDetector.mHW_GC + ", K40 Fixed Channel - "
								+ mDetector.mHW_K40_FxiedCh + " Ch, Det Type - " + mDetector.mCrystal);

					Dismiss_ProgressDlg();

					if (mDebug.IsDebugMode) {

						// Start_AutoCalib();
					} else {

						if (IsThere_CalibrationInfo()) {
							Init_stabilization();
							for (int i = 0; i < 10; i++) {
								mService.write(MESSAGE_START_HW); // �섎뱶�⑥뼱�먭쾶
								// �쒖옉
								// 硫붿꽭吏��
								// 蹂대궦��
								Thread.sleep(100);
								mService.write(MESSAGE_END_HW); // �섎뱶�⑥뼱�먭쾶 �쒖옉
								// 硫붿꽭吏�� 蹂대궦��
								Thread.sleep(100);
							}

							mService.write(MESSAGE_START_HW); // �섎뱶�⑥뼱�먭쾶 �쒖옉
							// 硫붿꽭吏�� 蹂대궦��
							Start_AutoCalib();
						} else {
							Toast.makeText(getApplicationContext(),
									getResources().getString(R.string.not_found_calibration), Toast.LENGTH_LONG).show();
							// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
						}

					}
					// VISIBLESTARTBUTTON
					// m_startBtn.setVisibility(View.VISIBLE);

					// Start_Animation_StatusIcon();
					/*
					 * for (int i = 0; i < 10; i++) { // hw mService.write(MESSAGE_START_HW); //
					 * �섎뱶�⑥뼱�먭쾶 // �쒖옉 // // Thread.sleep(100); mService.write(MESSAGE_END_HW); //
					 * �섎뱶�⑥뼱�먭쾶 �쒖옉 // Thread.sleep(100); }
					 *
					 * mService.write(MESSAGE_START_HW); // �섎뱶�⑥뼱�먭쾶 �쒖옉 //
					 */ /*
						 * if (IsThere_CalibrationInfo()) { Init_stabilization(); for (int i = 0; i <
						 * 10; i++) { // hw mService.write(MESSAGE_START_HW); // �섎뱶�⑥뼱�먭쾶 // �쒖옉 // //
						 * Thread.sleep(100); mService.write(MESSAGE_END_HW); // �섎뱶�⑥뼱�먭쾶 �쒖옉 //
						 * Thread.sleep(100); }
						 *
						 * mService.write(MESSAGE_START_HW); // �섎뱶�⑥뼱�먭쾶 �쒖옉 // Start_AutoCalib(); }
						 * else { Toast.makeText(getApplicationContext(),
						 * getResources().getString(R.string. not_found_calibration),
						 * Toast.LENGTH_LONG).show(); setRequestedOrientation(ActivityInfo.
						 * SCREEN_ORIENTATION_UNSPECIFIED); }
						 */

				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

			case MESSAGE_USB_READ_GC:
				// if(IsThere_CalibrationInfo())

				try {

					/*
					mDetector.mHW_GC = msg.arg1;
					mDetector.mHW_K40_FxiedCh = msg.arg2;
					 */

//					GCData mGCData = new GCData();
//					mGCData = (GCData) msg.obj;
//					
//					mDetector.mHW_GC = mGCData.GC;
//					mDetector.mHW_K40_FxiedCh = mGCData.K40_Ch;
//					mDetector.mHW_Cs137_FxiedCh1 = mGCData.Cs137_Ch1;
//					mDetector.mHW_Cs137_FxiedCh2 = mGCData.Cs137_Ch2;
//					mDetector.mCrtstalType = mGCData.DetType;
					GCData mGCData = new GCData();
					mGCData = (GCData) msg.obj;

					mDetector.mHW_GC = mGCData.GC;
					mDetector.mHW_FixedGC=mGCData.GC;
					mDetector.mHW_K40_FxiedCh = mGCData.K40_Ch;
					mDetector.mHW_Cs137_FxiedCh1 = mGCData.Cs137_Ch1;
					mDetector.mHW_Cs137_FxiedCh2 = mGCData.Cs137_Ch2;
					mDetector.mCrtstalType =  mGCData.DetType;
					int[] HWinfo= {mDetector.mHW_Cs137_FxiedCh1,mDetector.mHW_Cs137_FxiedCh2,mDetector.mHW_K40_FxiedCh ,mDetector.mHW_GC,mGCData.DetType};

					//String str2=("receive, "+ ", gc,"+  mGCData.GC+"\n");
					//NcLibrary.SaveText_HNM(str2);

					//NcLibrary.SaveTextCali(HWinfo,MainActivity.FilenameCurCaliInfo, 5);
					
					/*..........................
					 * Hung.18.03.05
					 * Added Code to new algorithm
					 */
					//////////////////
					//for test to apply calirbation when get the GCdata
					double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,(double) mGCData.K40_Ch };
					double[] FitParam = new double[3];

					NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2,NcLibrary.K40_PEAK, FitParam);
					mPrefDB.Set_Calibration_Result(FitParam, PeakCh);


					String str = "ch : "+PeakCh[0]+", "+PeakCh[1]+ ", "+PeakCh[2]+" a :"+FitParam[0]+" b : "+FitParam[1]+" c : "+FitParam[2]+" GC : "+mDetector.mHW_GC+ " DetType : "+mGCData.DetType;
					//NcLibrary.SaveText1(MainActivity.FilenameCurCaliInfo, str);

					NcLibrary.SaveTextCali1(str,MainActivity.FilenameCurCaliInfo);




					mEventDBOper = new EventDBOper();
					mEventDBOper.Set_Crytal_Info(Integer.toString(mGCData.DetType));
					Write_HW_Calibration_Result(mGCData);

					// Log.d("time:", "cystal : " + Integer.toString((int)
					// msg.obj));

					if (AutoCalibrationCnt == Activity_Mode.EXCUTE_MODE) {
						AutoCalibrationCnt = Activity_Mode.UN_EXCUTE_MODE;
						if (IsThere_CalibrationInfo()) {
							Init_stabilization();
							mDetector.IsGainStb = false;
							Start_AutoCalib();

						}

					}
					//mDetector.Set_PmtProperty((int) msg.obj);
					mDetector.Set_PmtProperty(mGCData.DetType);
					SendU2AA();

				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

			case MESSAGE_MEDIA_SCAN:
				Start_MediaScan_AllDBFile();
				break;
			case MESSAGE_READ_BATTERY:
				try {

					Log.e(TAG_RecvData, "Battery read - " + msg.obj.toString());

					mBattary = (int) msg.arg1;
					// mBattary = 100;

					mBatteryProgBar.Set_Value(mBattary);

					mBatteryProgBar.invalidate();

					/*
					 * Toast.makeText(getApplicationContext(), String.valueOf(tt) , 1).show();
					 */

					Battery.setText(String.valueOf((int) mBattary) + " %");

					Intent send_gs = new Intent(MainBroadcastReceiver.MAIN_DATA_SEND1);
					send_gs.putExtra(MainBCRReceiver.DATA_BATTERY,
							String.valueOf(NcLibrary.Auto_floor(mBattary)) + "%");

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

			case MESSAGE_READ_DETECTOR_DATA:
				try
				{
					mDetector.GM_Cnt = msg.arg1;
					if (msg.arg2 == -1)
					{
						Intent send_gs = new Intent(MainBroadcastReceiver.MSG_NOT_RECV_USB_NEUTRON);
						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
					}
					else
					{
						mDetector.mIsNeutronModel = true;
						mDetector.mNeutron.Reset_Acummul_data();
					}
					if ((int[]) msg.obj != null)

						// mDetector.MS.SetFillCps(mMainUsbService.FillCps);
					mDetector.Set_Spectrum((int[]) msg.obj);
					mDetector.Discrimination();
					int[] aaa = (int[]) msg.obj;



					// String a = Integer.toString(aaa.length);

					if (mNormalDB != null)
					{
						mNormalDB.addValue(mDetector.User, mDetector.Location, mDetector.Get_Gamma_DoseRate_nSV(),mDetector.mNeutron.Get_CPS());
					}


				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

				//j3 및 시간데이터 처리
				case MainActivity.MESSAGE_READ_DETECTOR_DATA_J3:

					try
					{
						ReadDetectorData mReadData = new ReadDetectorData();
						mReadData = (ReadDetectorData) msg.obj;
						mDetector.GM_Cnt = mReadData.GM;
						mDetector.hv = mReadData.mHighVoltage;



						if (mReadData.IsThereNeutron == false)
						{
							Intent send_gs = new Intent(MainBroadcastReceiver.MSG_NOT_RECV_USB_NEUTRON);
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
						}
						else
						{
							mDetector.mIsNeutronModel = true;
							//MainActivity.mDetector.mNeutron.Set_CPS((mReadData.GetAVGNeutron <= 0.08) ? 0 : mReadData.GetAVGNeutron);
							//테스트 MainActivity.mDetector.mNeutron.Set_CPS(mReadData.GetAVGNeutron);
							//181129 0.08이하 0으로 표시 삭제
							MainActivity.mDetector.mNeutron.Set_CPS(mReadData.GetAVGNeutron);
							mHandler.obtainMessage(MainActivity.MESSAGE_NEUTRON_RECV, 0, 0, mReadData.GetAVGNeutron).sendToTarget();

						}
						if ((ReadDetectorData) msg.obj != null) {
							mDetector.Set_Spectrum(mReadData.pdata, mReadData.mRealTime);
							mDetector.Discrimination();
						}

						if (mNormalDB != null)
						{
							mNormalDB.addValue(mDetector.User, mDetector.Location, mDetector.Get_Gamma_DoseRate_nSV(),mDetector.mNeutron.Get_CPS());
						}

                        if (mDetector.Get_GammaCPS() == 0) {


                            // 0104 SendU4AA();

                            SendU2AA();
                            Timer mTimer = new Timer();
                            TimerTask mTask = new TimerTask() {
                                @Override
                                public void run() {
                                    SendU2AA();
                                }
                            };
                            mTimer.schedule(mTask, 5000);
                            //190111  타이머 주석처리
	/*						Timer mTimer = new Timer();

							TimerTask mTask2 = new TimerTask() {
								@Override
								public void run() {
									if (mDetector.Get_GammaCPS() == 0) {

										NcLibrary.Show_Dlg(getString(R.string.CPS_ZeroMsg), mContext);
									}

								}
							};

							Timer mTimer2 = new Timer();
							mTimer2.schedule(mTask2, 5000);*/

	/*						if (mDetector.Get_GammaCPS() == 0) {
								((Activity) mContext).runOnUiThread(new Runnable() {
									public void run() {
										NcLibrary.Show_Dlg(getString(R.string.CPS_ZeroMsg), mContext);
									}
								});
							}*/

                        }

			/*			if ((ReadDetectorData) msg.obj != null)
						{
							mDetector.Set_Spectrum(mReadData.pdata);
							//mDetector.Set_Spectrum(mReadData.pdata, mReadData.mRealTime);
							//NcLibrary.SaveText1("RealTime "+mReadData.time +"(time:"+mReadData.mRealTime+") cps : "+mDetector.MS.Get_AvgCPS1()+"("+mDetector.MS.Get_AvgCPS()+") cnt : "+mDetector.MS.Get_TotalCount()+"\n","test");

							mTimeSpcCollect1+=1;
							mDetector.MS.sumSpectrum(mDetector.MS.get_Channel());

							if(mTimeSpcCollect1==30)
							{
								mTimeSpcCollect1 = 0;
								mTimeSpcCollect+=1;

								String str2=("30sec, "+"read, "+ mDetector.MS.getSumSpectrum());
								NcLibrary.SaveText_HNM(str2);
								mDetector.MS.clearSpectrum();
								//saveSpc = "";

								if(mTimeSpcCollect==5)
								{
									// 파일안에 문자열 쓰기
									mTimeSpcCollect = 0;
									mDetector.MS.clearSpectrum();
									Intent intent = new Intent(MainBroadcastReceiver.MSG_FIXED_GC_SEND1);
									LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
								}
							}
						}*/

					//	if ((ReadDetectorData) msg.obj != null) {
					//		mDetector.Set_Spectrum(mReadData.pdata);
							//mDetector.Set_Spectrum(mReadData.pdata, mReadData.mRealTime);
							//NcLibrary.SaveText1("RealTime "+mReadData.time +"(time:"+mReadData.mRealTime+") cps : "+mDetector.MS.Get_AvgCPS1()+"("+mDetector.MS.Get_AvgCPS()+") cnt : "+mDetector.MS.Get_TotalCount()+"\n","test");

							//
							// HungNM: Starting getting Spectrum
		/*					mTimeSpcCollect1 += 1;
							mDetector.MS.sumSpectrum(mDetector.MS.get_Channel());

							if (mTimeSpcCollect1 == 30) {
								mDetector.MS.clearSpectrum();
								//saveSpc = "";
							} else {
								mTimeSpcCollect1 = 0;
								mTimeSpcCollect += 1;

								if (mTimeSpcCollect == 5) {
									// 파일안에 문자열 쓰기
									mTimeSpcCollect = 0;
									mDetector.MS.clearSpectrum();
									Intent intent = new Intent(MainBroadcastReceiver.MSG_FIXED_GC_SEND1);
									LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
								}
							}*/
					//	}




						if (mNormalDB != null)
							mNormalDB.addValue(mDetector.User, mDetector.Location, mDetector.Get_Gamma_DoseRate_nSV(),
									mDetector.mNeutron.Get_CPS());



					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
						;
					}
					break;

			case MESSAGE_NEUTRON_RECV:

				Intent send_gs = new Intent(MainBroadcastReceiver.MSG_RECV_USB_NEUTRON);
				send_gs.putExtra(MainBroadcastReceiver.DATA_NEUTRON, (double) msg.obj);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

				break;

			case MESSAGE_READ_GM:

				Message_Read_Gm(msg.arg1);

				break;
			case MESSAGE_CONNECTED_DEVICE_INFO:
				try {

					BluetoothDevice device = (BluetoothDevice) msg.obj;
					mDetector.InstrumentModel_Name = device.getName();
					mDetector.InstrumentModel_MacAddress = device.getAddress();

					mEventDB.Save_DeviceName(device.getName());
					// mPrefDB.Set_Last_Cntd_Detector(mDetector.InstrumentModel_Name);
					// mPrefDB.Set_Last_Cntd_DetectorMac(mDetector.InstrumentModel_MacAddress);

					Update_StatusBar();

					// Set_DeviceImage(false);
					// Check_AndMake_DeviceNameFile(false);
					Toast.makeText(getApplicationContext(), "Connected to " + mDetector.InstrumentModel_Name,
							Toast.LENGTH_SHORT).show();

				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

			case MESSAGE_TOAST:

				int Index1 = (int) msg.arg1;

				if (Index1 == 0) {

					// SetDoubleConnectCheck(false);

					Toast.makeText(getApplicationContext(), getString(R.string.bt_not_enabled_leaving),
							Toast.LENGTH_SHORT).show();
					DeleteDlg();
					try {

					} catch (NullPointerException e) {

						NcLibrary.Write_ExceptionLog(e);
					}

				}

				break;

			case MESSAGE_SHUTDOWN:

				try {

					// finish();
					// System.runFinalizersOnExit(true);
					// android.os.Process.killProcess(android.os.Process.myPid());

					// moveTaskToBack(true);

					// android.os.Process.killProcess(android.os.Process.myPid());

					// finish();
					// android.os.Process.killProcess(android.os.Process.myPid());
					// ActivityManager am =
					// (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);

					// am.killBackgroundProcesses(getPackageName());
					isHWUsbUnConnect = true;
					if (mDebug.IsDebugMode) {
						if (mDebug.IsShutdown) {
							finish();
						} else {

						}

					} else {

						Intent intent = new Intent(getApplicationContext(), MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);

						finish();
					}

				} catch (NullPointerException e) {
					NcLibrary.Write_ExceptionLog(e);
				}

				break;

			/*
			 * case CcswService.SERVER_STATE_CONNECTED:
			 *
			 * Start_locationUpdates(); // Set_DeviceImage(true);
			 * Toast.makeText(getApplicationContext(), "서버와 연결 성공",
			 * Toast.LENGTH_LONG).show(); break; case CcswService.SERVER_STATE_CONNECT_FAIL:
			 * mLocationManager.removeUpdates(locationListener); // Set_DeviceImage(false);
			 *
			 * Toast.makeText(getApplicationContext(), "서버와 연결 실패",
			 * Toast.LENGTH_LONG).show(); mCCSW_Service = null; break; case
			 * CcswService.SERVER_STATE_LOST:
			 * mLocationManager.removeUpdates(locationListener); // Set_DeviceImage(true);
			 *
			 * Toast.makeText(getApplicationContext(), "서버와 연결이 해제되었습니다.",
			 * Toast.LENGTH_LONG).show(); mCCSW_Service = null; break;
			 */

			case INPUT_HARDWARE_KEY:

				int Index = (int) msg.arg1;

				int KeyValue = Integer.valueOf((String) msg.obj);

				if (ActionViewExcuteCheck == Activity_Mode.UN_EXCUTE_MODE) {

					HWKey(Index, KeyValue);

				}
				break;

			}
		}
	};



	public void onTabChanged(String tabId) {

		strMsg = tabId;

		ActivityCheck = tabId;
		TabChangeDraw(tabId);

		SendU2AA();

		// ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
		if (strMsg.equals(Tab_Name.RealTime_Str)) {
			MainActivity.mDetector.mGain_elapsedTime = Detector.GAIN_START_IN_SEC;
			mDetector.IsGainStb = true;
			// UsbConnect();
			// tabHost.getTabWidget().getChildAt(1).setOnTouchListener(this);
		} else {
			mDetector.IsGainStb = false;
			GainstabilizattonTxt.setText("");
			Stop_Alarm();
			Stop_Vibrate();

		}

		if (strMsg.equals("Background")) {

		}

		if (strMsg.equals("En.Calibration")) {

		}

		if (strMsg.equals(Tab_Name.ManualID_Str)) {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
				FirstActivityCurrentTab = Tab_Name.MenualID;

				Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_START);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

			}

		}

		if (strMsg.equals("Sequential Mode")) {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
				FirstActivityCurrentTab = Tab_Name.SequentialMode;

				Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SEQUENTAL_MODE_RUNNING_START);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

			}

		}

	}

	@Override
	public View createTabContent(String tag) {

		// TODO Auto-generated method stub
		return null;
	}

	void turnGPSOn() {
		String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (!provider.contains("gps")) { // if gps is disabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
		}
	}

	public void DownloadFiles() {

		try {
			URL u = new URL("http://nucare.cafe24.com/SAM III PeakAbout II Kins Launcher (v1.0.1).apk");
			InputStream is = u.openStream();

			DataInputStream dis = new DataInputStream(is);

			byte[] buffer = new byte[1024];
			int length;

			FileOutputStream fos = new FileOutputStream(
					new File(Environment.getExternalStorageDirectory() + "/" + "download/hh100.apk"));
			while ((length = dis.read(buffer)) > 0) {
				fos.write(buffer, 0, length);
			}

		} catch (MalformedURLException mue) {
			Log.e("SYNC getUpdate", "malformed url error", mue);
		} catch (IOException ioe) {
			Log.e("SYNC getUpdate", "io error", ioe);
		} catch (SecurityException se) {
			Log.e("SYNC getUpdate", "security error", se);
		}
	}

	/*
	 * void Start_Login_Dlg() {
	 *
	 * Intent intent = null; intent = new Intent(MainActivity.this, LoginDlg.class);
	 * intent.putExtra(LoginDlg.EXTRA_ADMIN_PW, mPrefDB.Get_AdminPW_From_pref());
	 * startActivityForResult(intent, RESULT_LOGIN); }
	 */

	void Check_AndMake_EventDB_VersionFile() {

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_FOLDER);
		if (!dbpath.exists()) {
			if (D)
				Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdirs();
		}
		//
		////////////////////////////////
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + EventDBOper.DB_VERSION_FILE + ".txt"); // 디바이스

		// 네임
		// 폴더를
		// 만든다.
		if (nameFilePath.isFile()) {
			FileInputStream fis = null;
			try {

				fis = new FileInputStream(nameFilePath);
			} catch (FileNotFoundException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			byte[] buf = new byte[3];
			try {
				fis.read(buf);
				fis.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog("1");
			}

			if (buf[0] == EventDBOper.DB_verion.getBytes()[0] && buf[2] == EventDBOper.DB_verion.getBytes()[2]) {

			} else { // 버젼이 다르다.
				if (mEventDB != null) {
					try {
						if (buf[2] == 51) { // if DB version is 1.3
							mEventDB.Remove_EventFile();
							mEventDB.OpenDB();
							mEventDB.EndDB();

							mNormalDB.OpenDB();
						} else {
							Vector<EventData> temp = mEventDB.Load_ALL_Event();
							mEventDB.Remove_EventFile();
							mEventDB.OpenDB();
							mEventDB.EndDB();

							if (temp != null) {
								for (int i = 0; i < temp.size(); i++) {
									mEventDB.WriteEvent_OnDatabase(temp.get(i));
								}

							}
						}
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}

				}
			}
		} else {
			if (mEventDB != null) {
				FileOutputStream Fos = null;
				try {

					Fos = new FileOutputStream(nameFilePath);
				} catch (FileNotFoundException e) {
					NcLibrary.Write_ExceptionLog(e);
				}

				byte[] buf = new byte[3];
				try {
					Fos.write(EventDBOper.DB_verion.getBytes());
				} catch (IOException e) {

					NcLibrary.Write_ExceptionLog(e);
				}
				try {
					Fos.close();
				} catch (IOException e) {
					NcLibrary.Write_ExceptionLog(e);
				}
			}
		}
	}

	void Check_AndMake_IsoLibraryFile()
	{
		String IsoLib_Path = getIsoDB_FilePath("SwLibrary.sql");
		File dbpath = new File(getIsoDB_FilePath(EventDBOper.DB_VERSION_FILE + ".txt"));
		String readVersion ="";

		if (!(dbpath.isFile()))
		{
			File isoFile = new File(IsoLib_Path);
			if (isoFile.isFile())
			{
				isoFile.delete();
			}
			exdbfile(R.raw.iso_library);

			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(dbpath);
				for(int i = 0; i<IsotopesLibrary.DB_VERSION.length(); i++)
				{
					fos.write(IsotopesLibrary.DB_VERSION.getBytes()[i]);
				}
				fos.close();
			}
			catch (IOException e)
			{
				NcLibrary.Write_ExceptionLog(e);
			}
		}
		else
		{
			FileReader fileReader= null;
			try
			{
				fileReader = new FileReader(dbpath.getAbsoluteFile().getPath());
				BufferedReader bufReader= new BufferedReader(fileReader);

				if(bufReader != null)
				{
					String readText = "";
					try
					{
						if((readText = bufReader.readLine())!= null)
						{
							readVersion = readText.replace("\n", "");
						}
						String version = IsotopesLibrary.DB_VERSION.replace("\n", "");
						if(!readVersion.equals(version))
						{
							dbpath.delete();
							File isoFile = new File(IsoLib_Path);
							if (isoFile.isFile())
							{
								isoFile.delete();
							}
							FileOutputStream fos = null;
							try
							{
								fos = new FileOutputStream(dbpath);
								fos.write(IsotopesLibrary.DB_VERSION.getBytes());
								fos.close();
							}
							catch (FileNotFoundException e1)
							{
								NcLibrary.Write_ExceptionLog(e1);
							}
							catch (IOException e)
							{
								NcLibrary.Write_ExceptionLog(e);
							}

							exdbfile(R.raw.iso_library);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
					fileReader.close();
					bufReader.close();

				}
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}




			/*byte[] buf = new byte[5];
			try {
				fis.read(buf);
				fis.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog("2");
			}

			if (buf[0] == IsotopesLibrary.DB_VERSION.getBytes()[0]&& buf[2] == IsotopesLibrary.DB_VERSION.getBytes()[2]&&buf[4] == IsotopesLibrary.DB_VERSION.getBytes()[4]) {

			} else {
				dbpath.delete();
				File isoFile = new File(IsoLib_Path);
				if (isoFile.isFile())
					isoFile.delete();

				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(dbpath);
				} catch (FileNotFoundException e1) {
					NcLibrary.Write_ExceptionLog(e1);
				}
				try {
					fos.write(IsotopesLibrary.DB_VERSION.getBytes());
					fos.close();
				} catch (IOException e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				exdbfile(R.raw.iso_library);
			}*/
		}

	}

	void Check_AndMake_IsoLibraryFile_old() {
		String IsoLib_Path = getIsoDB_FilePath("SwLibrary.sql");
		File dbpath = new File(getIsoDB_FilePath(EventDBOper.DB_VERSION_FILE + ".txt"));
		////////////////////////////////
		if (!(dbpath.isFile())) {
			File isoFile = new File(IsoLib_Path);
			if (isoFile.isFile())
				isoFile.delete();
			exdbfile(R.raw.iso_library);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(dbpath);
			} catch (FileNotFoundException e1) {
				NcLibrary.Write_ExceptionLog(e1);
			}
			try {
/*				fos.write(IsotopesLibrary.DB_VERSION.getBytes()[0]);
				fos.write(IsotopesLibrary.DB_VERSION.getBytes()[1]);
				fos.write(IsotopesLibrary.DB_VERSION.getBytes()[2]);*/
				for(int i = 0; i<IsotopesLibrary.DB_VERSION.length(); i++)
				{
					fos.write(IsotopesLibrary.DB_VERSION.getBytes()[i]);
				}

				fos.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		} else {
			FileInputStream fis = null;
			try {

				fis = new FileInputStream(dbpath);
			} catch (FileNotFoundException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			byte[] buf = new byte[5];
			try {
				fis.read(buf);
				fis.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog("2");
			}

			if (buf[0] == IsotopesLibrary.DB_VERSION.getBytes()[0]&& buf[2] == IsotopesLibrary.DB_VERSION.getBytes()[2]&&buf[4] == IsotopesLibrary.DB_VERSION.getBytes()[4]) {

			} else {
				dbpath.delete();
				File isoFile = new File(IsoLib_Path);
				if (isoFile.isFile())
					isoFile.delete();

				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(dbpath);
				} catch (FileNotFoundException e1) {
					NcLibrary.Write_ExceptionLog(e1);
				}
				try {
					fos.write(IsotopesLibrary.DB_VERSION.getBytes());
					fos.close();
				} catch (IOException e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				exdbfile(R.raw.iso_library);
			}
		}

	}

	public String getIsoDB_FilePath(String FileName) {
		try {
			File sdcard = Environment.getExternalStorageDirectory();

			File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_LIB_FOLDER);
			if (!dbpath.exists()) {
				dbpath.mkdirs();
			}

			String dbfile = dbpath.getAbsolutePath() + File.separator + FileName;
			return dbfile;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return "";
		}
	}

	public void exdbfile(int rawId) {
		File file = new File(getIsoDB_FilePath("SwLibrary.sql"));
		if (file.isFile() == false) {
			byte[] buffer = new byte[8 * 1024];

			int length = 0;
			InputStream is = getResources().openRawResource(rawId);
			BufferedInputStream bis = new BufferedInputStream(is);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(getIsoDB_FilePath("SwLibrary.sql"));
			} catch (FileNotFoundException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			try {
				while ((length = bis.read(buffer)) >= 0)
					fos.write(buffer, 0, length);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog("3");
			}

			try {
				fos.flush();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog("4");
			}

			try {
				fos.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog("5");
			}
		}
	}

	void Check_AndMake_DeviceNameFile(boolean IsEdit) {

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_FOLDER);
		if (!dbpath.exists()) {
			if (D)
				Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdirs();
		}
		////////////////////////////////
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + EventDBOper.DEVICE_FILE + ".txt"); // 디바이스
		// 네임
		// 폴더를
		// 만든다.
		if (nameFilePath.isFile() & IsEdit == false) {
			nameFilePath.delete();
		}

		if (!nameFilePath.isFile()) {
			try {
				nameFilePath.createNewFile();
			} catch (IOException e2) {
				NcLibrary.Write_ExceptionLog(e2);
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(nameFilePath);
			} catch (FileNotFoundException e1) {
				NcLibrary.Write_ExceptionLog(e1);
			}
			try {
				String Last_Dev;
				if (mDetector.InstrumentModel_Name == "" | mDetector.InstrumentModel_Name == null) {
					Last_Dev = DEVICE_NAME;
				} else {
					Last_Dev = mDetector.InstrumentModel_Name;
				}
				fos.write(String.valueOf(Last_Dev).getBytes());
				fos.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	public void onStart() {

		try {
			super.onStart();
			if (D)
				Log.e(TAG, "++ ON START ++");

			Start_MediaScan_AllDBFile();

			Intent send_gs = new Intent(MainBroadcastReceiver.MSG_USB_CONNECTED);

			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public boolean Start_MediaScan_AllDBFile() {
		try {
			// File f = new
			// File(Environment.getExternalStorageDirectory().getAbsolutePath());
			// new SingleMediaScanner(getApplicationContext(), f);

			File file = new File(
					Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + EventDBOper.DB_FOLDER); // 외장
			// 디렉토리
			// 가져옴
			File[] fileNames = file.listFiles(new FilenameFilter() { // 특정 확장자만
				// 가진
				// 파일들을
				// 필터링함
				public boolean accept(File dir, String name) {
					return true;
				}
			});

			if (fileNames != null) {
				for (int i = 0; i < fileNames.length; i++) // 파일 갯수 만큼 scanFile을
				// 호출함
				{
					new SingleMediaScanner(mContext.getApplicationContext(), fileNames[i]);
				}
			}

			File file2 = new File(
					Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + EventDBOper.DB_LIB_FOLDER); // 외장
			// 디렉토리
			// 가져옴
			File[] fileNames2 = file2.listFiles(new FilenameFilter() { // 특정
				// 확장자만
				// 가진
				// 파일들을
				// 필터링함
				public boolean accept(File dir, String name) {
					return true;
				}
			});

			if (fileNames2 != null) {
				for (int i = 0; i < fileNames2.length; i++) // 파일 갯수 만큼
				// scanFile을 호출함
				{
					new SingleMediaScanner(mContext.getApplicationContext(), fileNames2[i]);
				}
			}
			return true;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
	}

	void Start_AutoCalib() {

		if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY)

			try {
				Intent intent = null;
				intent = new Intent(MainActivity.this, AutoCalibActivity.class);
				intent.putExtra(AutoCalibActivity.EXTRA_THRESHOLD_CNT, mDetector.get_K40_ID_Threshold());
				intent.putExtra(AutoCalibActivity.EXTRA_FAIL_CNT, (mDetector.mCrystal != Detector.CrystalType.NaI) ? 1.5 : 4.0);
				startActivityForResult(intent, AUTO_CALIB_FINISH);

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
	}

	void Manual_Identification() {

	}

	boolean IsThere_CalibrationInfo() {
		try {
			if (mPrefDB.Get_Cali_A_From_pref() == 0)
				return false;
			else
				return true;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		SetFristActiviyMode();
		// ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
		switch (requestCode) {
		case AUTO_CALIB_FINISH:
			try {
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
				switch (resultCode)
				{
				case AutoCalibActivity.RESULT_SUCCESS:
					mDetector.Set_EnergyFittingArgument(mPrefDB.Get_Cali_ABC_From_pref());
					mDetector.Real_BG.Set_MeasurementDate(mPrefDB.Get_BG_Date_From_pref());
					mDetector.Real_BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(),
							mPrefDB.Get_BG_MeasuredAcqTime_From_pref());

					AUTO_FAIL_CODE_10 = false;
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.auto_cali_success),
							Toast.LENGTH_LONG).show();
					// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					Intent send_gs1 = new Intent(MainBroadcastReceiver.MSG_START_ID_MODE);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs1);
					Update_All_DetectorInfo();
					mDetector.IsGainStb = true;

					// if (mDebug.IsDebugMode) {
					// ShutDownTimeTask = new TimerTask() {
					//
					// @Override
					// public void run() {
					//
					// try {
					// // try {
					// //
					// // ShutDownTimer.cancel();
					// // ShutDownTimeTask.cancel();
					// //
					// // Process proc = Runtime.getRuntime()
					// // .exec(new String[] { "su", "-c", "reboot
					// // -p" });
					// // proc.waitFor();
					// // } catch (Exception ex) {
					// // ex.printStackTrace();
					// // }
					//
					// try {
					// ShutDownTimer.cancel();
					// ShutDownTimeTask.cancel();
					// Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c",
					// "am start -a android.intent.action.ACTION_REQUEST_SHUTDOWN --ez KEY_CONFIRM
					// true --activity-clear-task"});
					// proc.waitFor();
					// } catch (Exception ex) {
					// ex.printStackTrace();
					// }
					//
					// } catch (Exception ex) {
					// ex.printStackTrace();
					// }
					//
					// }
					// };
					// ShutDownTimer = new Timer();
					// ShutDownTimer.schedule(ShutDownTimeTask, 8000);
					//
					// finish();
					// }
					break;

				case AutoCalibActivity.RESULT_CANCEL:
					// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					Update_All_DetectorInfo();
					Intent send_gs2 = new Intent(MainBroadcastReceiver.MSG_START_ID_MODE);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs2);
					mDetector.IsGainStb = true;
					// Update_StatusBar();

					break;

				case AutoCalibActivity.RESULT_OUT_OF_BOUND:
					if (mPrefDB.Get_IsConnect_UsbMode_From_pref() == false) {
						// mService.write(MESSAGE_END_HW);
					}
					dialogBuilder.setTitle(getResources().getString(R.string.auto_cali_fail));
					dialogBuilder.setMessage(getResources().getString(R.string.auto_cali_gain_shift));
					dialogBuilder.setPositiveButton("Yes", null);
					dialogBuilder.setCancelable(false);
					dialogBuilder.show();
					// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

					break;

				case AutoCalibActivity.RESULT_CALIB_ERR:
					if (mPrefDB.Get_IsConnect_UsbMode_From_pref() == false) {
						// mService.write(MESSAGE_END_HW);
					}
					dialogBuilder.setTitle(getResources().getString(R.string.auto_cali_fail));
					dialogBuilder.setMessage(getResources().getString(R.string.not_found_calibration));
					dialogBuilder.setPositiveButton("Yes", null);
					dialogBuilder.setCancelable(false);
					dialogBuilder.show();
					// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					break;
				case AutoCalibActivity.RESULT_TIMEOUT_AND_COUNT:
					if (AUTO_FAIL_CODE_10) {
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.auto_cali_2nd_fail),
								Toast.LENGTH_LONG).show();
						mAUTO_GAIN_result = 0;
						AUTO_FAIL_CODE_10 = false;
						if (mPrefDB.Get_IsConnect_UsbMode_From_pref() == false) {
							mService.write(MESSAGE_END_HW);
						}
						// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
						break;
					} else {
						AUTO_FAIL_CODE_10 = true;
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.auto_cali_k40_insufficient), Toast.LENGTH_LONG)
								.show();
						Start_AutoCalib();
					}
				}
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}

		case FINISH_SETUP_PREF:
			try {

				Update_All_DetectorInfo();
				mPrefDB.Set_Last_Cntd_User(mDetector.User);
				Update_StatusBar();

				//////
				if (mAlarmSound != null) {
					if (mAlarmSound.isPlaying()) {
						if (mDetector.AlarmSound == R.raw.beep1) {
							Start_Alarm(true);
							Stop_Alarm();
						} else
							Start_Alarm(false);
					}
				}
				///////////////////

				mChangeConnectMode = mPrefDB.Get_IsConnect_UsbMode_From_pref();
				if (mCurrentConnectMode != mChangeConnectMode) {

					Toast.makeText(getApplicationContext(), getString(R.string.Appalication_Restart_Msg), Toast.LENGTH_LONG).show();
				}
				InIt_SPC_Data();
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			break;

		case RESULT_LOGIN:

			// TextView Login = (TextView)
			// m_MainLayout.findViewById(R.id.n3rd_row_3);
			if (resultCode == LoginDlg.LOGIN_USER) {
				mLogin = LoginDlg.LOGIN_USER;

				// Login.setText(getResources().getString(R.string.login_user));
			} else {
				mLogin = LoginDlg.LOGIN_ADMIN;
			}

			Update_StatusBar();

			// LOGIN_MODE

			// Conntect_With_LastDevice();

			break;

		case FINISH_CALIB_BG:
			/*
			 * try{ mDetector.Set_EnergyFittingArgument(mPrefDB. Get_Cali_ABC_From_pref());
			 * mDetector.BG.Set_MeasurementDate(mPrefDB.Get_BG_Date_From_pref()) ;
			 * mDetector.BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(),mPrefDB.
			 * Get_BG_MeasuredAcqTime_From_pref()); }catch(Exception e) {
			 * NcLibrary.Write_ExceptionLog("\nKainacActivity - FINISH_CALIB_BG" ); } break;
			 */

			////////////////////////////////// K40 roi �곸뿭��移댁슫�멸� 異⑸텇�섏�
			////////////////////////////////// 紐삵븷��

			/////////////////////////////////////////////////

			break;
		case REQUEST_ENABLE_BT:
			try {
				if (resultCode == Activity.RESULT_OK) {
					if (mService == null)
						mService = new MainService(this, mHandler);
				} else {
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, "not enabled leaving", Toast.LENGTH_SHORT).show();
					// finish();
				}
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			break;

		case MainActivity.REQUEST_CONNECT_DEVICE:
			try {
				if (resultCode == Activity.RESULT_OK) {
					String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

					mService.connect(device);
				}
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void Update_StatusBar() {

		try {

			Paired = (TextView) findViewById(R.id.Paired);
			Library = (TextView) findViewById(R.id.Library);
			Alarm = (TextView) findViewById(R.id.Alarm);

			Paired.setText(getResources().getString(R.string.HardwareName));

			IsotopesLibrary mIsoLib2 = new IsotopesLibrary(this);
			Vector<String> temp = mIsoLib2.get_IsotopeLibrary_List();
			if (temp.isEmpty() == true) {
				Library.setText("None");

				LibraryStr = "None";
			} else {
				String temp22 = mPrefDB.Get_Selected_IsoLibName();
				if (temp22.matches("null")) {
					mPrefDB.Set_String_on_pref(getResources().getString(R.string.IsoLib_list), temp.get(0));
					Library.setText(temp.get(0));
					LibraryStr = temp.get(0);

				} else {
					Library.setText(temp22);
					LibraryStr = temp22;
				}
			}
			if (mDetector.IsSigmaThreshold) {

				Alarm.setText(getResources().getString(R.string.variable));
				AlarmStr = getResources().getString(R.string.variable);

			} else {

				Alarm.setText(getResources().getString(R.string.fixed));
				AlarmStr = getResources().getString(R.string.fixed);
			}

			tabEnable();

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	void Start_HealthAlarm() {

		if (mAlarmSound.isPlaying()) {
			mAlarmSound.stop();
			mAlarmSound.reset();
		}
		mAlarmSound = MediaPlayer.create(this, R.raw.danger_alarm);

		if (mAlarmSound != null) {
			mAlarmSound.setVolume(100, 100);
			mAlarmSound.start();
			mAlarmSound.setLooping(true);
		}
		return;

	}

    void Start_Alarm(boolean IsBeep)
    {

        try {

            if (IsBeep)
                return;
            AudioManager ssq = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);

            if (ssq.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                Start_Vibrate();
            }

            if (mDetector.Is_HealthEvent())
                return;

            if (mDetector.AlarmSound == R.raw.beep1) {
                if (mAlarmSound == null)
                    mAlarmSound = MediaPlayer.create(this, mDetector.AlarmSound);

                if (mAlarmSound.isPlaying() == false) {
                    if (mDetector.MS.Get_TotalCount() > mDetector.Get_GammaThreshold() * 3.5) {
                        mAlarmSound = MediaPlayer.create(this, R.raw.beep3);
                        mAlarmSound.setVolume(100, 100);
                        mAlarmSound.start();
                    } else if (mDetector.MS.Get_TotalCount() > mDetector.Get_GammaThreshold() * 1.5) {
                        mAlarmSound = MediaPlayer.create(this, R.raw.beep2);
                        mAlarmSound.setVolume(100, 100);
                        mAlarmSound.start();
                    } else {
                        mAlarmSound = MediaPlayer.create(this, mDetector.AlarmSound);
                        mAlarmSound.setVolume(100, 100);
                        mAlarmSound.start();

                    }
                }
                return;
            }

            if (mAlarmSound != null) {
                if (mAlarmSound.isPlaying()) {
                    mAlarmSound.stop();
                    mAlarmSound.reset();
                }
            }
            mAlarmSound = MediaPlayer.create(this, mDetector.AlarmSound);

            if (mAlarmSound != null) {
                // if(mAlarmSound.isPlaying() == false){
                mAlarmSound.setVolume(80, 80);
                mAlarmSound.start();
                mAlarmSound.setLooping(true);
                // }
            }

        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

	void Stop_Alarm() {

		try {
			if (mAlarmSound != null) {
				if (mAlarmSound.isPlaying())
					mAlarmSound.stop();
			}
			Stop_Vibrate();
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	private void Stop_Vibrate() {

		if (mVibrating) {
			Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibe.cancel();
			mVibrating = false;
		}
	}

	private void Start_Vibrate() {
		if (mVibrating == false) {
			Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibe.vibrate(new long[] { 500, 500, 500, 500, 1000, 1000 }, 0);
			mVibrating = true;

		}
	}

	public Location Get_Location() {

		Location result = null;

		GpsInfo gps = new GpsInfo(this);
		if (gps.isGetLocation()) {
			result = gps.getLocation();
		}
		gps.stopUsingGPS();

		if (result != null)
			return result;
		else
			return new Location(LocationManager.GPS_PROVIDER);

	}

	void WriteEvent_toDB(EventData event) {

		if (event.MS.Get_AcqTime() <= 3)
			return;

		Location gps = Get_Location();
		event.GPS_Latitude = gps.getLatitude();
		event.GPS_Longitude = gps.getLongitude();
		event.mColumn_Version = EventDBOper.DB_verion;

		Check_AndMake_DeviceNameFile(true);
		Check_AndMake_EventDB_VersionFile();

		EventDBOper eventDB = new EventDBOper();
		if (eventDB.WriteEvent_OnDatabase(event)) {
			int cnt = eventDB.GetEventCount();
			String str_event = getResources().getString(R.string.event);

			Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
			Bundle bundle = new Bundle();
			bundle.putString(TOAST, getResources().getString(R.string.write_db) + " (" + str_event + " #" + cnt + ")");
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

		// Start_MediaScan();
		File eventFile = new File(Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER + "/"
				+ EventDBOper.DB_FILE_NAME + ".sql");
		if (eventFile.isFile())
			new SingleMediaScanner(getApplicationContext(), eventFile);
	}

	void Update_All_DetectorInfo() {

		try {
			mDetector.Set_EnergyFittingArgument(mPrefDB.Get_Cali_ABC_From_pref());
			mDetector.Real_BG.Set_MeasurementDate(mPrefDB.Get_BG_Date_From_pref());
			mDetector.Real_BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(), mPrefDB.Get_BG_MeasuredAcqTime_From_pref());
			mDetector.Real_BG.Set_StartSystemTime(new Date(0));
			mDetector.Real_BG.Set_EndSystemTime(new Date(mPrefDB.Get_BG_MeasuredRealAcqTime_From_pref()));

			mDetector.DB_BG.Set_MeasurementDate(mPrefDB.Get_BG_Date_From_pref());
			mDetector.DB_BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(), mPrefDB.Get_BG_MeasuredAcqTime_From_pref());
			mDetector.DB_BG.Set_StartSystemTime(new Date(0));
			mDetector.DB_BG.Set_EndSystemTime(new Date(mPrefDB.Get_BG_MeasuredRealAcqTime_From_pref()));

			if (mEventDBOper != null)
			{

				mDetector.MS.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
				mDetector.MS.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
				mDetector.DB_BG.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
				mDetector.Real_BG.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);

				mDetector.DB_BG.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
				mDetector.Real_BG.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);

				mDetector.MS.setFWHM(mEventDBOper.Cry_Info.FWHM);
				mDetector.DB_BG.setFWHM(mEventDBOper.Cry_Info.FWHM);
				mDetector.Real_BG.setFWHM(mEventDBOper.Cry_Info.FWHM);

				Vector<NcPeak> peakInfo_bg = new Vector<NcPeak>();
				peakInfo_bg = NewNcAnalsys.GetPPSpectrum_H(mDetector.Real_BG);

				mDetector.DB_BG.SetPeakInfo(peakInfo_bg);
				mDetector.Real_BG.SetPeakInfo(peakInfo_bg);
			}

			mDetector.User = mPrefDB.Get_User_From_pref();
			mDetector.Location = mPrefDB.Get_Location_From_pref();
			mDetector.AlarmSound = Get_AlarmResID(mPrefDB.Get_AlarmSound_From_pref());
			///////////////////
			mDetector.Gamma_Threshold = mPrefDB.Get_GammaThreshold_From_pref();
			mDetector.Gamma_SigmaThreshold = mPrefDB.Get_GammaThreshold_Sigma_From_pref();

			mDetector.Neutron_ThresholdCnt = mPrefDB.Get_NeutronThreshold_From_pref();
			mDetector.HealthSafety_Threshold = mPrefDB.Get_HealthyThreshold_From_pref();

			mDetector.IsSvUnit = mPrefDB.Get_IsSvUnit_From_pref();
			mDetector.IsSigmaThreshold = mPrefDB.Get_IsSigma_From_pref();

			mChangeConnectMode = mPrefDB.Get_IsConnect_UsbMode_From_pref();
			if (mCurrentConnectMode != mChangeConnectMode) {

			} else {
				mDetector.InstrumentModel_Name = mPrefDB.Get_equipment_From_pref();
			}

			mDetector.GMT = NcLibrary.Get_GMT();

			if (mPrefDB.Get_IsSvUnit_From_pref() == false) {

				mDetector.HealthSafety_Threshold = (int) NcLibrary
						.Rem_To_Sv((double) mPrefDB.Get_HealthyThreshold_From_pref());
			}

			Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SET_TOSVUNIT);
			send_gs.putExtra(MainBCRReceiver.DATA_SET_TOSVUNIT, mDetector.IsSvUnit);

			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

			mDetector.Low_discrimination = mPrefDB.Get_Low_Discrimination_From_pref();
			mDetector.Upper_discrimination = mPrefDB.Get_Upper_Discrimination_From_pref();
			// -----

			Detector.MeasurementInfo info = new Detector.MeasurementInfo();
			info.Set_Info("", "", mPrefDB.Get_Location_From_pref(), mPrefDB.Get_User_From_pref(),
					Get_AlarmResID(mPrefDB.Get_AlarmSound_From_pref()), "", mPrefDB.Get_IsSvUnit_From_pref(), false);

			mDetector.IsSvUnit = mPrefDB.Get_IsSvUnit_From_pref();
			if (m_GammaGuage_Panel != null)
				m_GammaGuage_Panel.Set_toSvUnit(mDetector.IsSvUnit);

			if (mPrefDB.Get_SequenceMode_From_pref()) {

				tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
			} else {

				tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
			}

			/*
			 * if (IsThere_CalibrationInfo()) {
			 *
			 * tabEnable(); } else { tabEnable(); NoCalDataTabDisable();
			 *
			 * }
			 */

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public int Get_AlarmResID(int ListNumber) {
		if (ListNumber == 0)
			return R.raw.warning;
		else if (ListNumber == 1)
			return R.raw.clock;
		else if (ListNumber == 2)
			return R.raw.charmingbell;
		else if (ListNumber == 3)
			return R.raw.trumpet;
		else if (ListNumber == 4)
			return R.raw.bell;
		else if (ListNumber == 5)
			return R.raw.beep1;
		else
			return 0;
	}

	private void Gain_Stabilizatioin() {

		try {
			if (D)
				Log.i("Stabilization", "Acq Time - " + mGain_Sec + " sec / " + GAIN_EVERY_SEC + " sec");
			if (MainActivity.mDetector.EVENT_STATUS == MainActivity.mDetector.EVENT_NONE) {
				mGain_Sec += 1;
				if (MANUAL_ID_STATUS == false) { // Gain stabilization
					if (mGain_Sec > GAIN_EVERY_SEC) {
						/*
						 * if (mt_Check == false) { mt.setText( "Stabilization in progress");// //
						 * +mGain_Sec+" // "+STB_Checker+"%"); mt_Check = true; }
						 */
						for (int i = 0; i < CHANNEL_ARRAY_SIZE; i++) {
							mStbChannel[i] += MainActivity.mDetector.MS.at(i);
						}
						Accumul_Channel_forGain(mStbChannel);
					} else {

					}
					// mt.setText("");

				} else {
					if (mGain_Sec > GAIN_EVERY_SEC) {
						Init_stabilization();
					}
				}
			} else {
				/*
				 * if (mt_Check == true) { mt.setText(""); mt_Check = false; }
				 */
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	boolean Accumul_Channel_forGain(int[] channel) {
		try {
			int THRESHOLD = GAIN_THRESHOLD;

			double[] mPeck = new double[2];
			mPeck[0] = mPrefDB.Get_CaliPeak1_From_pref();
			mPeck[1] = mPrefDB.Get_CaliPeak2_From_pref();
			double Be_K40_Ch = mPrefDB.Get_CaliPeak3_From_pref();

			double Be_A = mPrefDB.Get_Cali_A_From_pref();
			double Be_B = mPrefDB.Get_Cali_B_From_pref();
			double Be_C = mPrefDB.Get_Cali_C_From_pref();
			if (Be_A == 0)
				return false;

			int mROI_Ch_start = (int) NcLibrary
					.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 0.94, Be_A, Be_B, Be_C));
			int mROI_Ch_end = (int) NcLibrary
					.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 1.06, Be_A, Be_B, Be_C));

			double[] temp = new double[1024];
			temp = NcLibrary.Smooth(channel, 1024, 10, 2);

			int mStbWinCnt = (int) NcLibrary.ROIAnalysis_GetTotCnt(temp, mROI_Ch_start, mROI_Ch_end);
			if (D)
				Log.i("Stabilization", "K40 Cnt - " + mStbWinCnt + " Cnt  /  " + THRESHOLD + " Cnt");
			if (mStbWinCnt > THRESHOLD) {
				int K40_Ch = NcLibrary.ROIAnalysis(temp, mROI_Ch_start, mROI_Ch_end);
				if (D)
					Log.i("Stabilization", "Found K40 - " + K40_Ch + " cnt");
				if (K40_Ch != 0) {
					Background_GainStabilization(Be_K40_Ch, K40_Ch);
					double Ratio = (((double) K40_Ch - (double) Be_K40_Ch) / (double) Be_K40_Ch);

					double New_Peak1 = (double) mPeck[0] + ((double) mPeck[0] * Ratio);
					double New_Peak2 = (double) mPeck[1] + ((double) mPeck[1] * Ratio);
					double[] FitParam = new double[3];
					NcLibrary.QuadraticCal(New_Peak1, New_Peak2, (double) K40_Ch, NcLibrary.CS137_PEAK1,
							NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
					mPrefDB.Set_Calibration_Result(FitParam[0], FitParam[1], FitParam[2], New_Peak1, New_Peak2,
							(double) K40_Ch);
					MainActivity.mDetector.Set_EnergyFittingArgument(FitParam);

					/*
					 * Calendar calendar = Calendar.getInstance(); Date date = calendar.getTime();
					 * Spectrum Spc = new Spectrum(); Spectrum BG = new Spectrum();
					 * Spc.Set_Spectrum(channel); BG.Set_Spectrum(mPrefDB.Get_BG_From_pref());
					 * String LogData = "\n"+date.getHours()+":"+date.getMinutes()+":"+date.
					 * getSeconds() +" _Old 3Peak Ch: "+ mPeck[0]+", "+ mPeck[1] +",  "+ Be_K40_Ch+
					 * "Ch _New 3Peak Ch: "+ New_Peak1+", "+ New_Peak2+", "+ K40_Ch +
					 * "Ch _Old ABC : "+Be_A+", " + Be_B+", "+ Be_C+ " _New ABC : "+FitParam[0]+", "
					 * + FitParam[1]+", "+ FitParam[2]+ " _Spectrum:"
					 * +Spc.ToString()+" _BG:"+BG.ToString();
					 *
					 * NcLibrary.Export_ToTextFile("Gain Stabilization", LogData);
					 */
				} else {
					if (D)
						Log.i("Stabilization", "Fail - Gain Stabilization");
				}

				mGain_Sec = 0;
				// mt_Check = false;
				for (int i = 0; i < 1024; i++) {
					mStbChannel[i] = 0;
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
	}

	void Init_stabilization() {
		try {

			mGain_Sec = DEAFALUT_GAIN_SEC;
			// mt.setText("");
			// mt_Check = false;
			mStbChannel = NcLibrary.Init_ChannelArray(mStbChannel, CHANNEL_ARRAY_SIZE);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

	}

	void Background_GainStabilization(double Before_K40, double Now_K40) {

		try {
			if (Before_K40 == Now_K40)
				return;
			int[] BG = new int[CHANNEL_ARRAY_SIZE];
			int[] NewBG = new int[CHANNEL_ARRAY_SIZE];
			BG = mPrefDB.Get_BG_From_pref();

			// background adjustment
			int tempindex = 0;
			float diffgap = 0;

			float temp = 0;
			if (Now_K40 == 0)
				diffgap = 1;
			else
				diffgap = (float) Now_K40 / (float) Before_K40;

			for (int i = 0; i < CHANNEL_ARRAY_SIZE; i++) // 채널
			// 이동
			{
				tempindex = NcLibrary.Auto_floor(((float) i * diffgap));
				if (tempindex >= CHANNEL_ARRAY_SIZE)
					break;
				NewBG[tempindex] = BG[i];
			}

			for (int i = 0; i < CHANNEL_ARRAY_SIZE - 1; i++) // 이빠진곳
			// 보정
			{
				temp = NewBG[i];
				if (temp <= 0 && (i > 0 && i < CHANNEL_ARRAY_SIZE - 1)) {
					if (NewBG[i - 1] > 0 & NewBG[i + 1] > 0) {
						NewBG[i] = (NewBG[i - 1] + NewBG[i + 1]) / 2;
					}
				}
			}

			mPrefDB.Set_BG_On_pref(NewBG, CHANNEL_ARRAY_SIZE);
			MainActivity.mDetector.Real_BG.Set_Spectrum(NewBG, mPrefDB.Get_BG_MeasuredAcqTime_From_pref());
			// Save_BackGround_Data();

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public void Test() {

		mDetector.mHW_GC = 32000;
		double FixedK40 = 511;
		int NewK40Ch = 575;
		double mChangeK40 = (double) FixedK40 - NewK40Ch;

		mChangeK40 = mChangeK40 * 6;

		// Toast.makeText(getApplicationContext(), "count :" +
		// Integer.toString(mToastCount), 1).show();
		double y, x;

		NewGC = Integer.toString((int) ((double) mDetector.mHW_GC + mChangeK40));

		mDetector.mHW_GC = (int) ((double) mDetector.mHW_GC + mChangeK40);

		byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();

		byte[] ss = new byte[5];
		ss[0] = 'G';
		ss[1] = 'C';
		//
		if (GcBytes.length == 1) {
			ss[2] = 0;
			ss[3] = GcBytes[2];
		} else if (GcBytes.length == 3) {
			ss[2] = GcBytes[1];
			ss[3] = GcBytes[2];
		} else if (GcBytes.length == 2) {
			ss[2] = GcBytes[0];
			ss[3] = GcBytes[1];
		}

		///
		ss[4] = (byte) Byte.valueOf((byte) 1);
	}


	public int Send_GC_ToHW(int NewK40Ch) {

		//if (mDebug.IsDebugMode && mDebug.IsSendtoGCMode == false) {
		if (mDebug.IsDebugMode) {

			return 0;
		}

		if (mDetector.mHW_K40_FxiedCh == 0 || NewK40Ch <=0 ){
			return 0;
		}

		/*
		 * mSaveCount++;
		 * 
		 * Log.d("time", "FoundK40 : " + Integer.toString(NewK40Ch) + " GcValue : " +
		 * NewGC);
		 * 
		 * String mTxtBody = ""; StrArraylist.add("FoundK40 : " +
		 * Integer.toString(NewK40Ch) + " GcValue : " + NewGC); for (int i = 0; i <
		 * StrArraylist.size(); i++) { mTxtBody += StrArraylist.get(i) + "\n"; }
		 * 
		 * onTextWriting("Check-Gain-GC-Temp", mTxtBody);
		 */

		double FixedK40 = (double) mDetector.mHW_K40_FxiedCh;

		double mChangeK40 = (double) FixedK40 - (double)NewK40Ch;
		double mChangeK40ratio=Math.abs(mChangeK40/(double)FixedK40);

		int status=0;
		//YKIM, 2018.2.9, change GC factor value for each detector type
		// setup function: HwPmtProperty_Code() in Detector.java (instance: mDetector, value: mDetector.mGCFactor)
		mChangeK40 = mChangeK40 * mDetector.mGCFactor;


		if (mChangeK40ratio <=0.01) {
			mDetector.mGain_restTime=mGain_restTime_under2; // YKIM, 2018.2.19, rest time setup
			return status;	// return 0, Not thing to do, rest time for GS 150sec
		}else if(mChangeK40ratio <=0.02)
		{	mDetector.mGain_restTime=mGain_restTime_under2;
			status = 1;	// rest time for GS 60sec
			//return status;
		}else
		{	mDetector.mGain_restTime=mGain_restTime_over2;
			status=2;	// rest time for GS 10sec
		}


		// Toast.makeText(getApplicationContext(), "count :" +
		// Integer.toString(mToastCount), 1).show();
		double y, x;

		NewGC = Integer.toString((int) ((double) mDetector.mHW_GC + mChangeK40));

		mDetector.mHW_GC = (int) ((double) mDetector.mHW_GC + mChangeK40);

		//

		byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();

		//

		byte[] ss = new byte[5];
		ss[0] = 'G';
		ss[1] = 'C';
		//
		if (GcBytes.length == 1) {
			ss[2] = 0;
			ss[3] = GcBytes[2];
		} else if (GcBytes.length == 3) {
			ss[2] = GcBytes[1];
			ss[3] = GcBytes[2];
		} else if (GcBytes.length == 2) {
			ss[2] = GcBytes[0];
			ss[3] = GcBytes[1];
		}

		///
		ss[4] = (byte) Byte.valueOf((byte) 1);

		try {
			if (mMainUsbService != null) {
					mMainUsbService.write(ss);
			}
			if (mService != null) {
				mService.write(ss);
			}

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		//spectrum collect
		//30 sec/5 time


		//text file save

		return status;
	}

	boolean Send_GC_ToHW_old(int NewK40Ch) {

		if (mDebug.IsDebugMode) {

			return false;
		}

		if (mDetector.mHW_K40_FxiedCh == 0) {
			return false;
		}

		double FixedK40 = (double) mDetector.mHW_K40_FxiedCh;

		double mChangeK40 = (double) FixedK40 - NewK40Ch;

		mChangeK40 = mChangeK40 * 6;

		Log.d("time", "FoundK40 : " + Integer.toString(NewK40Ch) + " GcValue : " + NewGC);

		if (FixedK40 * 0.98 < NewK40Ch && FixedK40 * 1.02 > NewK40Ch) {
			/*
			 * Toast.makeText(getApplicationContext(), "count :" +
			 * Integer.toString(mToastCount), 1).show();
			 * StrArraylist.add(Integer.toString(mToastCount) + " " + "FoundK40 : " +
			 * Integer.toString(NewK40Ch) + " GcValue : " + NewGC); for (int i = 0; i <
			 * StrArraylist.size(); i++) { mTxtBody += StrArraylist.get(i) + "\n"; }
			 * onTextWriting("Check-Gain-GC-Temp", mTxtBody);
			 */
			if (D)
				Log.i("GC", "Found K40 Channel In 2%");
			return false;
		}
		// Toast.makeText(getApplicationContext(), "count :" +
		// Integer.toString(mToastCount), 1).show();
		double y, x;

		NewGC = Integer.toString((int) ((double) mDetector.mHW_GC + mChangeK40));

		mDetector.mHW_GC = (int) ((double) mDetector.mHW_GC + mChangeK40);

		byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();

		byte[] ss = new byte[5];
		ss[0] = 'G';
		ss[1] = 'C';
		//
		if (GcBytes.length == 1) {
			ss[2] = 0;
			ss[3] = GcBytes[2];
		} else if (GcBytes.length == 3) {
			ss[2] = GcBytes[1];
			ss[3] = GcBytes[2];
		} else if (GcBytes.length == 2) {
			ss[2] = GcBytes[0];
			ss[3] = GcBytes[1];
		}

		///
		ss[4] = (byte) Byte.valueOf((byte) 1);

		try {
			mMainUsbService.write(ss);

			// mService.write(ss);

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		return true;
	}

	void Change_Gc_Cal(double mChangeK40) {

		NewGC = Integer.toString((int) ((double) mDetector.mHW_GC + mChangeK40));

		mDetector.mHW_GC = (int) ((double) mDetector.mHW_GC + mChangeK40);

		byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();

		byte[] ss = new byte[5];
		ss[0] = 'G';
		ss[1] = 'C';
		//
		if (GcBytes.length == 1) {
			ss[2] = 0;
			ss[3] = GcBytes[2];
		} else {
			ss[2] = GcBytes[1];
			ss[3] = GcBytes[2];
		}

		ss[4] = (byte) Byte.valueOf((byte) 1);

		try {
			mMainUsbService.write(ss);

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	void Reset_Detector() {
		mDetector.Finish_GammaEvent();
		mDetector.Finish_NeutronEvent();

		mDetector.MS.ClearSPC();
		mDetector.Set_Mode(Detector.ID_MODE);
		mDetector.mIsNeutronModel = false;

		mDetector.InstrumentModel_Name = "None";
		mDetector.InstrumentModel_MacAddress = "None";

		mDetector.mHW_GC = 0;
		mDetector.mHW_K40_FxiedCh = 0;
		mDetector.Set_PmtProperty(Detector.HwPmtProperty_Code.NaI_2x2);

		InIt_SPC_Data();

	}

	public void InIt_SPC_Data() {

		try {
			mDetector.Init_Measure_Data();
			// m_GammaGuage_Panel.SETnSv(0, 0);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	@SuppressLint("NewApi")
	void ensureDiscoverable() {

		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBTAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	void Dismiss_ProgressDlg() {

		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}

	boolean Conntect_With_LastDevice() {

		try {
			String MacAdd = mPrefDB.Get_Last_Cntd_DetectorMac();
			if (MacAdd == null)
				return false;

			BluetoothDevice device = mBTAdapter.getRemoteDevice(MacAdd);
			mService.connect(device);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
		return true;
	}

	void Start_Login_Dlg() {

		Intent intent = null;
		intent = new Intent(MainActivity.this, LoginDlg.class);
		intent.putExtra(LoginDlg.EXTRA_ADMIN_PW, mPrefDB.Get_AdminPW_From_pref());
		startActivityForResult(intent, RESULT_LOGIN);
	}

	public static byte[] hexToBytes(String hex) {
		byte[] result = null;
		if (hex != null) {
			result = new byte[hex.length() / 2];
			for (int i = 0; i < result.length; i++) {
				result[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
			}
		}
		return result;
	}

/*180808
public void Start_locationUpdates() {

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
		else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
	}*/

	void Message_Read_Gm(int Gm_Cnt) {

		// Log.e("GM Read", msg.obj.toString());
		MainActivity.mDetector.GM_Cnt = Gm_Cnt;
		// TextView Tv_GM = (TextView) m_MainLayout.findViewById(R.id.tv_GM);

		// Tv_GM.setText("GM: " + String.valueOf(MainActivity.mDetector.GM_Cnt)
		// + " cps "
		// + NcLibrary.GM_to_uSV(MainActivity.mDetector.GM_Cnt) + " uSv/h");

	}

	@Override
	public void onDestroy() {

		try {
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int date = calendar.get(Calendar.DAY_OF_MONTH);
			int hour = calendar.get(Calendar.HOUR);
			int minute = calendar.get(Calendar.MINUTE);
			
			int iAMPM = calendar.get(Calendar.AM_PM);
			String ampm="";
			if (iAMPM == Calendar.AM)
				ampm = "AM";
			else
				ampm = "PM";

			mPrefDB.Set_Last_Cntd_Date(year + "/" + (month+1) + "/" + (date>=10? date:"0"+date)+ "  " +ampm+  " " + (hour>=10? hour:"0"+hour) + ":" + (minute>=10? minute:"0"+minute));

			ActivityManager AM = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			AM.restartPackage(getPackageName());
			// BrodcastStop();
			// mMainUsbService.StartReceiver();
			// mMainUsbService.usbStop();
			if (D)
				Log.e(TAG, "--- ON DESTROY ---");

			// mMainUsbService.write(MESSAGE_END_HW);
			// mMainUsbService.usbStop();

			// UsbConnect();
			if (!isHWUsbUnConnect) {
				SendU4AA();
				StopUsb();
				UsbBrodcastStop();
			}
			
			if (mService != null) {
				mService.write(MESSAGE_END_HW);
				mService.stop();
			}

			// System.exit(0);
		} catch (NullPointerException e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		super.onDestroy();
	}

	@Override
	public void onBackPressed() {

		return;
	}

	@Override
	public synchronized void onResume() {

		super.onResume();

/*		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (Settings.System.canWrite(this)) {
				Toast.makeText(this, "onResume: Granted", Toast.LENGTH_SHORT).show();
			}
		}*/

		// mDetector.Set_PmtProperty(Detector.HwPmtProperty_Code.NaI_2x2);
		if (ActivityCheck == Tab_Name.RealTime_Str) {

			ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;

		}
		if (mService != null)
		{
			if (mService.getState() == MainService.STATE_NONE)
			{
				mService.start();
			}
		}

/*		180806
		super.onNewIntent(intent);
		boolean isKill = intent.getBooleanExtra("KILL_APP", false);
		if (isKill) {
			moveTaskToBack(true);
			finish();
		}*/
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
/*
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

		}
		else
		{
			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 1000);
		}*/

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness= (float) (lp.screenBrightness*1.25);
		getWindow().setAttributes(lp);
/*
        // 기존 밝기 저장
        brightness = params.screenBrightness;
        // 최대 밝기로 설정
        params.screenBrightness = 1f;
        // 밝기 설정 적용
        getWindow().setAttributes(params);*/

		mDetector.MS.clearSpectrum();
		mDetector.Init_Measure_Data();

	}

	void Show_ProgressDlg(String messeage) {

		mProgressDialog = null;
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setTitle(getResources().getString(R.string.pleaseWait));
		mProgressDialog.setMessage(messeage);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (MAPPING_VERSION) {
			if (mService.mState == MainService.STATE_CONNECTED)
				menu.getItem(0).setTitle(getResources().getString(R.string.main_menu1_1));
			else
				menu.getItem(0).setTitle(getResources().getString(R.string.main_menu1));

		}
		openMenu = true;
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onOptionsMenuClosed(Menu menu) {
	/**
	 * OptionMenu가 강제로 Open될 때 호출 된다.
	 * 
	 */
		openMenu = false;
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		return true;
	}

	@Override

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		/*
		 * case R.id.bluetooth_Menu1: try {
		 *
		 *
		 * if(mCCSW_Service == null){ String ip = mPrefDB.Get_MappingServer_IP();
		 * if(ip==null) { Toast.makeText(getApplicationContext(), "IP가 설정 되어있지 않습니다.",
		 * Toast.LENGTH_SHORT).show(); break;} mCCSW_Service = new
		 * CcswService(mHandler); mCCSW_Service.connect(ip); } else{ Location loc =
		 * Get_Location(); MappingData data = new MappingData();
		 * data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
		 * data.InstrumentName = "sadf"; data.InstrumentMacAddress = "123";
		 * data.Doserate = 124512; mCCSW_Service.Set_Data(null,data); }
		 *
		 *
		 * if (GetDoubleConnectCheck() == false) {
		 *
		 * if (mService == null) mService = new MainService(this, mHandler);
		 *
		 * if (mService.mState == MainService.STATE_CONNECTED & mCCSW_Service == null &
		 * MAPPING_VERSION == true) { // try // to // connect // server String ip =
		 * mPrefDB.Get_MappingServer_IP();
		 *
		 * if (ip == null) { Toast.makeText(getApplicationContext(),
		 * "IP가 설정 되어있지 않습니다.", Toast.LENGTH_SHORT).show(); break; }
		 *
		 * mCCSW_Service = new CcswService(mHandler); mCCSW_Service.connect(ip);
		 *
		 * Toast.makeText(getApplicationContext(), "서버와 연결 시도 중",
		 * Toast.LENGTH_SHORT).show(); } else { if (mDetector.isIdMode()) { if (false) {
		 * Toast.makeText(getApplicationContext(),
		 * getResources().getString(R.string.now_running), Toast.LENGTH_LONG).show();
		 * break; } Intent serverIntent = null; serverIntent = new
		 * Intent(MainActivity.this, DeviceListActivity.class);
		 * startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); } } } catch
		 * (Exception e) {
		 * NcLibrary.Write_ExceptionLog("\nKainacActivity - R.id.bluetooth_Menu" ); }
		 * break;
		 */
		case R.id.db_Menu:
			try {

				startActivity(new Intent(MainActivity.this, EventListActivity.class));
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			break;

			case R.id.rdb_Menu:
				try {

					startActivity(new Intent(MainActivity.this, ReachBackListActivity.class));
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

		case R.id.setup_Menu:

			Intent Intent1 = null;
			Intent1 = new Intent(MainActivity.this, PreferenceActivity.class);
			startActivityForResult(Intent1, MainActivity.FINISH_SETUP_PREF);

			break;
		case R.id.Finish_Menu:
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.DAY_OF_MONTH);
			int date = calendar.get(Calendar.DATE);
			int hour = calendar.get(Calendar.HOUR);
			int minute = calendar.get(Calendar.MINUTE);
			
			int iAMPM = calendar.get(Calendar.AM_PM);
			String ampm="";
			if (iAMPM == Calendar.AM)
				ampm = "AM";
			else
				ampm = "PM";

			mPrefDB.Set_Last_Cntd_Date(year + "/" + (month+1) + "/" + (date>=10? date:"0"+date)+ "  " +ampm+  " " + (hour>=10? hour:"0"+hour) + ":" + (minute>=10? minute:"0"+minute));
			finish();

			// System.exit(0);
			break;

		}
		return true;
	}

	public Handler GetHandler() {
		return mHandler;
	}

	public void tabEnable() {

		SetFristActiviyMode();
		tabHost.getTabWidget().getChildAt(1).setOnTouchListener(this);

		tabHost.getTabWidget().getChildAt(2).setOnTouchListener(this);

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) relLayout.getChildAt(1);
			tv.setTextSize(TAB_TEXT_SIZE);
			tv.setTextColor(Color.parseColor(TAB_ENABLE_TEXT_COLOR));
			tv.setTypeface(Typeface.SANS_SERIF);

			relLayout.setBackgroundColor(Color.parseColor("#000000"));

			relLayout.setScaleY((float) 0.8);
			tv.setScaleY((float) 1.2);
		}

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(true);
		}

		LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(0);
		TextView tv = (TextView) relLayout.getChildAt(1);

		tv.setTextSize(TAB_TEXT_SIZE);
		tv.setTextColor(Color.parseColor(TAB_ENABLE_TEXT_COLOR));

		tv.setTypeface(Typeface.SANS_SERIF);

		relLayout.setBackgroundResource(R.drawable.tab_line);

		// relLayout.

		// relLayout.setScaleY((float)0.80);
		relLayout.setScaleX((float) 0.93);
		tv.setScaleX((float) 1.07);
		tv.setScaleY((float) 1.2);
		// tabHost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.line1);
	}

	public void tabDisable() {

		Context mContext;
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) relLayout.getChildAt(1);
			tv.setTextSize(TAB_TEXT_SIZE);
			tv.setTextColor(Color.parseColor("#747474"));

			tv.setTypeface(Typeface.SANS_SERIF);

		}

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(true);

		}

	}

	public void NoCalDataTabDisable() {

		Context mContext;
		for (int i = 1; i < tabHost.getTabWidget().getChildCount(); i++) {
			LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) relLayout.getChildAt(1);
			tv.setTextSize(TAB_TEXT_SIZE);
			tv.setTextColor(Color.parseColor("#747474"));

			tv.setTypeface(Typeface.SANS_SERIF);

		}

		for (int i = 1; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(false);

		}

	}

	public void tabRefresh() {

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		/*
		 * // TODO Auto-generated method stub // 여기서 width를 찍어보면 값이 제대로 출력된다.
		 *
		 *
		 * switch (tabswitch) { case 0: tabBottomLayout = (LinearLayout)
		 * findViewById(R.id.tabBottomLayout);
		 *
		 * tabBottomLayout.setVisibility(View.GONE);
		 *
		 * tabBodyLayout = (LinearLayout) findViewById(R.id.tabBodyLayout); View
		 * tabBodyLayoutView = (View) this.findViewById(R.id.tabBodyLayout);
		 *
		 * // TODO Auto-generated method stub tabBodyWidth =
		 * tabBodyLayoutView.getWidth(); tabBodyHeight = tabBodyLayoutView.getHeight();
		 *
		 * tabBodyLayout.setLayoutParams(new LinearLayout.LayoutParams(tabBodyWidth,
		 * tabBodyHeight + 68));
		 *
		 * tabswitch = 1; break;
		 *
		 * case 1: tabBottomLayout = (LinearLayout) findViewById(R.id.tabBottomLayout);
		 *
		 * tabBottomLayout.setVisibility(View.GONE);
		 *
		 * tabBodyLayout = (LinearLayout) findViewById(R.id.tabBodyLayout); View
		 * tabBodyLayoutView1 = (View) this.findViewById(R.id.tabBodyLayout);
		 *
		 * // TODO Auto-generated method stub tabBodyWidth =
		 * tabBodyLayoutView1.getWidth(); tabBodyHeight =
		 * tabBodyLayoutView1.getHeight();
		 *
		 * tabBodyLayout.setLayoutParams(new LinearLayout.LayoutParams(tabBodyWidth,
		 * tabBodyHeight - 68));
		 *
		 *
		 * tabswitch=0; break;
		 *
		 *
		 * default: break; }
		 *
		 *
		 *
		 */
	}

	public void sourceIdResult() {

		Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

		tabBottomLayout = (LinearLayout) findViewById(R.id.tabBottomLayout);

		tabBottomLayout.setVisibility(View.GONE);

		tabBodyLayout = (LinearLayout) findViewById(R.id.tabBodyLayout);
		View tabBodyLayoutView = (View) this.findViewById(R.id.tabBodyLayout);

		// TODO Auto-generated method stub
		tabBodyWidth = tabBodyLayoutView.getWidth();
		tabBodyHeight = tabBodyLayoutView.getHeight();

		// tabBodyLayout.set

		tabBodyLayout.setLayoutParams(new FrameLayout.LayoutParams(tabBodyWidth, tabBodyHeight + 72));

	}

	public void sourceIdResultCancel() {

		tabBottomLayout = (LinearLayout) findViewById(R.id.tabBottomLayout);

		tabBottomLayout.setVisibility(View.VISIBLE);

		tabBodyLayout = (LinearLayout) findViewById(R.id.tabBodyLayout);
		View tabBodyLayoutView1 = (View) this.findViewById(R.id.tabBodyLayout);

		// TODO Auto-generated method stub
		tabBodyWidth = tabBodyLayoutView1.getWidth();
		tabBodyHeight = tabBodyLayoutView1.getHeight();

		tabBodyLayout.setLayoutParams(new FrameLayout.LayoutParams(tabBodyWidth, tabBodyHeight - 72));

	}

	public void CreateMediaFile()
	{
		String sdRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+ EventDBOper.DB_FOLDER;
		File file;
		file = new File(sdRootPath);
		if (!file.exists())
		{
			file.mkdir();
		}
	}

	public void TabChangeDraw(String TabName) {

		int count = 0;
		if (TabName.equals(Tab_Name.RealTime_Str))
			count = 0;
		if (TabName.equals(Tab_Name.ManualID_Str))
			count = 1;
		if (TabName.equals(Tab_Name.SequentialMode_Str))
			count = 2;
		if (TabName.equals(Tab_Name.En_Calibration_Str))
			count = 3;

		String str = Integer.toString(count);

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);

			relLayout.setBackgroundColor(Color.parseColor("#000000"));

		}

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(true);
		}

		LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(count);
		TextView tv = (TextView) relLayout.getChildAt(1);
		tv.setTypeface(Typeface.SANS_SERIF);
		tv.setTextSize(TAB_TEXT_SIZE);
		tv.setTextColor(Color.parseColor(TAB_ENABLE_TEXT_COLOR));

		relLayout.setBackgroundResource(R.drawable.tab_line);

		relLayout.setScaleX((float) 0.93);
		tv.setScaleX((float) 1.07);
		tv.setScaleY((float) 1.2);
		tabHost.requestFocus();

		FirstActivityCurrentTab = count;

	}

	// KeyEvent

	public void HWKey(int Index, int KeyValue) {

		Log.d("time:", "hwkey : " + Integer.toString(Index) + ":" + Integer.toString(KeyValue));
		if (Index == HW_Key_Type.SHORTPRESS) {

			DoubleClickRock2 = Activity_Mode.UN_EXCUTE_MODE;
			TimerTask mTask = new TimerTask() {
				@Override
				public void run() {
					DoubleClickRock2 = Activity_Mode.EXCUTE_MODE;
				}
			};

			Timer mTimer = new Timer();
			mTimer.schedule(mTask, 300);
		}

		switch (Index) {

		case HW_Key_Type.SHORTPRESS:

			switch (KeyValue) {

			case HW_Key.Left:

				if (ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING
						|| ACTIVITY_STATE == Activity_Mode.SEQUENTAL_MODE_RUNNING) {

					Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SPEC_VIEWFILPPER);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
				} else {

					new Thread(new Runnable() {

						public void run() {

							new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
						}
					}).start();
				}
				break;

			case HW_Key.Right:

				if (ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING
						|| ACTIVITY_STATE == Activity_Mode.SEQUENTAL_MODE_RUNNING) {

					Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SPEC_VIEWFILPPER);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

				} else {
					new Thread(new Runnable() {

						public void run() {

							new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
						}
					}).start();
				}
				break;
			case HW_Key.Back:
				if(openMenu)
				{
					new Thread(new Runnable() {

						public void run() {

							new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
						}
					}).start();
					//menuTemp = false;
				}

				if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
					/*
					 * new Thread(new Runnable() {
					 *
					 * public void run() {
					 *
					 * new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU ); }
					 * }).start();
					 */
				}
				else
				{
					new Thread(new Runnable() {

						public void run() {

							new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
						}
					}).start();

				}
				break;
			case HW_Key.Up:

				new Thread(new Runnable() {

					public void run() {

						new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
					}
				}).start();

				break;
			case HW_Key.Down:

				if (ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

					Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_TIMEDOWN);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

				}

				new Thread(new Runnable() {

					public void run() {

						new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
					}
				}).start();

				break;
			case HW_Key.Enter:
				count++;
				Log.d("time:", "DoubleTest : ShotpressEnter 작동");
				new Thread(new Runnable() {

					public void run() {

						new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
					}
				}).start();

				break;

			default:
				break;
			}
		case HW_Key_Type.LONGPRESS:
			if (DoubleClickRock2 == Activity_Mode.EXCUTE_MODE)

			{
				switch (KeyValue) {
				case HW_Key.Left:

					// Toast.makeText(getApplicationContext(), "LongLeft",
					// 1).show();

					// KeyValue = "Left";
					break;
				case HW_Key.Back:

					new Thread(new Runnable() {

						public void run() {

							new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
						}
					}).start();
					// KeyValue = "Back";
					break;
				case HW_Key.Right:
					// KeyValue = "Right";
					break;
				case HW_Key.Up:

					// KeyValue = "Up";
					break;
				case HW_Key.Down:
					// KeyValue = "Down";
					break;
				case HW_Key.Enter:
					// 180712 메뉴키가 열려있으면 강제적으로 백버튼 눌른효과 준 후 화면 이동
					if(openMenu)
					{
						new Thread(new Runnable() {

							public void run() {

								new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
							}
						}).start();
						//menuTemp = false;
					}
					
					Log.d("time:", "DoubleTest : LongpressEnter 작동");
					if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY && IsThere_CalibrationInfo()) {
						Log.d("time:", "NotLong");
						tabHost.setCurrentTab(Tab_Name.MenualID);

					} else if ((ACTIVITY_STATE != Activity_Mode.FIRST_ACTIVITY)) {
						Log.d("time:", "LongClick");
						longPress(KeyEvent.KEYCODE_ENTER);

					}
					// longPress(KeyEvent.KEYCODE_ENTER);
					// KeyValue = "Menu";

				}
			}
			break;

		default:
			break;

		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		Log.d("time:", "LongClick1");
		switch (keyCode) {

		case KeyEvent.KEYCODE_DPAD_RIGHT: {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

				++FirstActivityCurrentTab;

				if (mPrefDB.Get_SequenceMode_From_pref() && IsThere_CalibrationInfo()) {

					if (FirstActivityCurrentTab > Tab_Name.SequentialMode) {
						FirstActivityCurrentTab = Tab_Name.SequentialMode;
					}

					switch (FirstActivityCurrentTab) {

					case Tab_Name.Reatime:

						TabChangeDraw(Tab_Name.RealTime_Str);

						break;
					case Tab_Name.MenualID:
						TabChangeDraw(Tab_Name.ManualID_Str);

						break;

					case Tab_Name.SequentialMode:
						TabChangeDraw(Tab_Name.SequentialMode_Str);

						break;

					default:
						break;
					}

				} else if (IsThere_CalibrationInfo() && mPrefDB.Get_SequenceMode_From_pref() == false) {

					if (FirstActivityCurrentTab > Tab_Name.MenualID) {
						FirstActivityCurrentTab = Tab_Name.MenualID;
					}

					switch (FirstActivityCurrentTab) {

					case Tab_Name.Reatime:

						TabChangeDraw(Tab_Name.RealTime_Str);

						break;
					case Tab_Name.MenualID:
						TabChangeDraw(Tab_Name.ManualID_Str);

						break;

					default:
						break;
					}

				} else if (IsThere_CalibrationInfo() == false) {

					if (FirstActivityCurrentTab > Tab_Name.Reatime) {
						FirstActivityCurrentTab = Tab_Name.Reatime;
					}

				}

				return true;
			} else {

				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
					KeyExecute(KeyEvent.KEYCODE_DPAD_RIGHT);
					return false;
				}

			}

			return true;
		}

		case KeyEvent.KEYCODE_DPAD_LEFT: {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
				--FirstActivityCurrentTab;

				if (mPrefDB.Get_SequenceMode_From_pref()) {
					if (FirstActivityCurrentTab < Tab_Name.Reatime) {
						FirstActivityCurrentTab = Tab_Name.Reatime;
					}

					switch (FirstActivityCurrentTab) {

					case Tab_Name.Reatime:

						TabChangeDraw(Tab_Name.RealTime_Str);

						break;
					case Tab_Name.MenualID:
						TabChangeDraw(Tab_Name.ManualID_Str);

						break;
					case Tab_Name.SequentialMode:
						TabChangeDraw(Tab_Name.SequentialMode_Str);

						break;

					default:
						break;
					}
				} else {
					if (FirstActivityCurrentTab < Tab_Name.Reatime) {
						FirstActivityCurrentTab = Tab_Name.Reatime;
					}

					switch (FirstActivityCurrentTab) {

					case Tab_Name.Reatime:

						TabChangeDraw(Tab_Name.RealTime_Str);

						break;
					case Tab_Name.MenualID:
						TabChangeDraw(Tab_Name.ManualID_Str);

						break;

					default:
						break;
					}

				}

			} else {

				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
					KeyExecute(KeyEvent.KEYCODE_DPAD_LEFT);
					return false;
				}

			}

			return true;
		}

		case KeyEvent.KEYCODE_DPAD_UP: {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

				switch (realTimeSwitch) {
				case 0:
					Intent send_gs = new Intent(MainBroadcastReceiver.MSG_MOVE_MAIN_NEXTFLIPPER);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

					realTimeSwitch = 1;
					break;
				case 1:
					Intent send_gs1 = new Intent(MainBroadcastReceiver.MSG_MOVE_MAIN_PREFLIPPER);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs1);

					realTimeSwitch = 0;
					break;

				default:
					break;
				}

			} else {

				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
					KeyExecute(KeyEvent.KEYCODE_DPAD_UP);
					return false;
				}

			}

			return true;
		}

		case KeyEvent.KEYCODE_DPAD_DOWN: {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

				switch (realTimeSwitch) {
				case 0:
					Intent send_gs = new Intent(MainBroadcastReceiver.MSG_MOVE_MAIN_NEXTFLIPPER);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

					realTimeSwitch = 1;
					break;
				case 1:
					Intent send_gs1 = new Intent(MainBroadcastReceiver.MSG_MOVE_MAIN_PREFLIPPER);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs1);

					realTimeSwitch = 0;
					break;

				default:
					break;
				}

			} else {

				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
					KeyExecute(KeyEvent.KEYCODE_DPAD_DOWN);
					return false;
				}

			}

			return true;
		}

		case KeyEvent.KEYCODE_ENTER: {

			Log.d("time:", "enterclickkeyevent : " + Integer.toString(count));
			if (FirstActivityCurrentTab != Tab_Name.Reatime) {
				tabHost.setCurrentTab(FirstActivityCurrentTab);

				return true;
			} else {

				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
					count++;
					Log.d("time:", "enterclick : " + Integer.toString(count));

					KeyExecute(KeyEvent.KEYCODE_ENTER);
					return false;
				}

			}

			return true;
		}

		case KeyEvent.KEYCODE_MENU: {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
					KeyExecute(KeyEvent.KEYCODE_MENU);
					return false;
				}

			} else if (ACTIVITY_STATE != Activity_Mode.FIRST_ACTIVITY) {

				if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
					KeyExecute(KeyEvent.KEYCODE_BACK);
					return false;
				}

			}

			return true;
		}

		case KeyEvent.KEYCODE_VOLUME_UP: {

			if (MainActivity.mDetector.Is_Event()) {
				AudioManager ssq = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
				if (ssq.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
					// Stop_Vibrate();
				}
			}

			return true;
		}
		case KeyEvent.KEYCODE_VOLUME_DOWN: {

			if (MainActivity.mDetector.Is_Event()) {
				AudioManager ssq = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
				if (ssq.getStreamVolume(AudioManager.STREAM_MUSIC) == 1) {
					// Start_Vibrate();
				}
			}

			return true;
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void longPress(final int key) {

		new Thread(new Runnable() {

			public void run() {

				long downTime = SystemClock.uptimeMillis();
				long eventTime = SystemClock.uptimeMillis();

				KeyEvent event1 = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER, 0);

				KeyEvent event2 = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER, 1);

				new Instrumentation().sendKeySync(event1);

				new Instrumentation().sendKeySync(event2);

			}
		}).start();

	}

	public void KeyExecute(final int keyvalue) {
		Log.d("time:", "DoubleTest : KeyExecute 함수 작동");
		new Thread(new Runnable() {

			public void run() {

				new Instrumentation().sendKeyDownUpSync(keyvalue);

			}
		}).start();

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
	public boolean onTouch(View v, MotionEvent event) {

		/*
		 * if (v.getContext() == tabHost.getTabWidget().getChildAt(1).getContext()) { if
		 * (event.getAction() == MotionEvent.ACTION_DOWN) { // mPreTouchPosX = (int)
		 * event.getX(); } if (event.getAction() == MotionEvent.ACTION_UP) { int
		 * nTouchPosX = (int) event.getX();
		 *
		 * int nTouchPosY = (int) event.getY();
		 *
		 * String str = Integer.toString(nTouchPosX);
		 *
		 * String str2 = Integer.toString(nTouchPosY);
		 *
		 *
		 *
		 * //Toast.makeText(getApplicationContext(), "X: " + nTouchPosX + ", Y:" +
		 * nTouchPosY, 1).show();
		 *
		 * } }
		 */
		return false;
	}

	@Override
	protected void onPause() {

		Stop_Vibrate();
		Stop_Alarm();

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

		layoutParams.screenBrightness = (float) 1.0;

		getWindow().setAttributes(layoutParams);
		/*
		 * TimerTask mTask = new TimerTask() {
		 *
		 * @Override public void run() {
		 *
		 * } };
		 *
		 * Timer mTimer = new Timer(); mTimer.schedule(mTask, 2000);
		 */

		super.onPause();
	}

	private void onTextWriting(String title, ArrayList<String> body) {
		File file;

		String path = Environment.getExternalStorageDirectory().getAbsolutePath();

		/*
		 * file = new File(path); if (!file.exists()) { file.mkdirs(); } file = new
		 * File(path + File.separator + title + ".txt"); try { FileOutputStream fos =
		 * new FileOutputStream(file); BufferedWriter buw = new BufferedWriter(new
		 * OutputStreamWriter(fos, "UTF8"));
		 *
		 * buw.write(body);
		 *
		 * buw.close(); fos.close();
		 *
		 *
		 * } catch (IOException e) {
		 *
		 * }
		 */

		try {

			// 파일 객체 생성
			File file1 = new File(path + File.separator + title + ".txt");

			// true 지정시 파일의 기존 내용에 이어서 작성
			FileWriter fw = new FileWriter(file1, true);

			// 파일안에 문자열 쓰기
			for (int i = 0; i < body.size(); i++) {

				fw.write(body.get(i) + "\n");
			}
			fw.flush();

			// 객체 닫기
			fw.close();

			Toast.makeText(getApplicationContext(), "내용이 txt파일로 저장되었습니다", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

	}

	/*
	 * private void ShutDown() { try {
	 *
	 * Runtime.getRuntime().exec("adb shell"); Runtime.getRuntime().exec(
	 * "/system/bin/reboot -p");
	 *
	 * } catch (Exception ex) {
	 *
	 * } }
	 */

	public void BrodcastDeclare() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(MainBroadcastReceiver.MSG_EN_CALIBRATION);
		filter.addAction(MainBroadcastReceiver.MSG_REMEASURE_BG);
		filter.addAction(MainBroadcastReceiver.MSG_MANUAL_ID);
		filter.addAction(MainBroadcastReceiver.MAIN_DATA_SEND1);
		filter.addAction(MainBroadcastReceiver.MSG_EVENT);
		filter.addAction(MainBroadcastReceiver.MSG_HEALTH_EVENT);
		filter.addAction(MainBroadcastReceiver.MSG_MANUAL_ID);
		filter.addAction(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
		filter.addAction(MainBroadcastReceiver.START_ID_MODE);
		filter.addAction(MainBroadcastReceiver.START_SETUP_MODE);
		filter.addAction(MainBroadcastReceiver.MSG_SETTIONG_TAB_BACKGROUND);
		filter.addAction(MainBroadcastReceiver.MSG_SETTION_TAB_CALIBRATION);
		filter.addAction(MainBroadcastReceiver.MSG_TAB_ENABLE);
		filter.addAction(MainBroadcastReceiver.MSG_TAB_DISABLE);
		filter.addAction(MainBroadcastReceiver.MSG_USB_CONNECTED);
		filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT);
		filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT_CANCEL);
		filter.addAction(MainBroadcastReceiver.MSG_STOP_HEALTH_ALARM);
		filter.addAction(MainBroadcastReceiver.MSG_POWER_DISCONNECT);
		filter.addAction(MainBroadcastReceiver.MSG_USB_DISCONNECT);
		filter.addAction(MainBroadcastReceiver.MSG_FIXED_GC_SEND);
		filter.addAction(MainBroadcastReceiver.MSG_FIXED_GC_SEND1);
		LocalBroadcastManager.getInstance(mContext).registerReceiver(mMainBCR, filter);
	}

	public void BrodcastStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
	}

	public void ConnectUsb() {
		mMainUsbService.usbStart();
	}

	public static void SendU2AA() {
		try {
			mMainUsbService.SendU2AA();
		} catch (NullPointerException e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public static void SendU4AA() {
		try {
			mMainUsbService.SendU4AA();
		} catch (NullPointerException e) {
			NcLibrary.Write_ExceptionLog(e);
		}

	}

	public void StopUsb() {
		mMainUsbService.usbStop();
	}

	// Add1

	public void UsbBrodcastStop() {
		mMainUsbService.UsbBrodcastStop();
	}

	public void DoubleClickRock() {
		DoubleClickRock = Activity_Mode.UN_EXCUTE_MODE;
		TimerTask mTask = new TimerTask() {
			@Override
			public void run() {
				DoubleClickRock = Activity_Mode.EXCUTE_MODE;
			}
		};

		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 500);

	}

    public void VolumeUp() {

        audio.setStreamVolume(AudioManager.STREAM_RING, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0),
                AudioManager.FLAG_PLAY_SOUND);

        audio.setStreamVolume(AudioManager.STREAM_SYSTEM,
                (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);

        audio.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0.65), AudioManager.FLAG_PLAY_SOUND);

        audio.setStreamVolume(AudioManager.STREAM_ALARM, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0),
                AudioManager.FLAG_PLAY_SOUND);

        audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);

    }

	public void BluetoothListExcute() {
		try {
			/*
			 * if(mCCSW_Service == null){ String ip = mPrefDB.Get_MappingServer_IP();
			 * if(ip==null) { Toast.makeText(getApplicationContext(), "IP가 설정 되어있지 않습니다.",
			 * Toast.LENGTH_SHORT).show(); break;} mCCSW_Service = new
			 * CcswService(mHandler); mCCSW_Service.connect(ip); } else{ Location loc =
			 * Get_Location(); MappingData data = new MappingData();
			 * data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
			 * data.InstrumentName = "sadf"; data.InstrumentMacAddress = "123";
			 * data.Doserate = 124512; mCCSW_Service.Set_Data(null,data); }
			 */

			/* if (GetDoubleConnectCheck() == false) { */

			if (mService == null)
				mService = new MainService(this, mHandler);

			if (mService.mState == MainService.STATE_CONNECTED & mCCSW_Service == null & MAPPING_VERSION == true) { // try
				// to
				// connect
				// server
				String ip = mPrefDB.Get_MappingServer_IP();

				if (ip == null) {
					Toast.makeText(getApplicationContext(), "IP가 설정 되어있지 않습니다.", Toast.LENGTH_SHORT).show();
					// break;
				}

				mCCSW_Service = new CcswService(mHandler);
				mCCSW_Service.connect(ip);

				Toast.makeText(getApplicationContext(), "서버와 연결 시도 중", Toast.LENGTH_SHORT).show();
			} else {
				/* if (mDetector.isIdMode()) { */
				if (false) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.now_running),
							Toast.LENGTH_LONG).show();
					// break;
				}
				Intent serverIntent = null;
				serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
			/* } */
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

	}

	private void DeleteDlg() {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
		dialogBuilder.setTitle("연결 실패");
		dialogBuilder.setMessage("연결에 실패하였습니다 다시 시도하시겠습니까?");
		dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

				BluetoothListExcute();

			}
		});
		dialogBuilder.setNegativeButton("Cancel", null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();

	}

	public void SetFristActiviyMode() {

		ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;

	}

	private void onTextWriting(String title, String body) {
		File file;

		String path = Environment.getExternalStorageDirectory().getAbsolutePath();

		file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(path + File.separator + title + ".txt");
		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter buw = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
			buw.write(body);
			buw.close();
			fos.close();

			Toast.makeText(getApplicationContext(), "내용이 txt파일로 저장되었습니다", Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public void DefaultSettingCalAndMail(PreferenceDB mPrefDB) {

		try {

			String mEmailName = mPrefDB.Get_recv_email();

			if (mEmailName == null || mEmailName.equals(""))
			{
				mPrefDB.Set_sender_Server(Config.Mail.MAIL_SERVER);
				mPrefDB.Set_sender_Port(Config.Mail.MAIL_PORT);
				mPrefDB.Set_sender_pw(Config.Mail.MAIL_PASSWD);
				mPrefDB.Set_sender_email(Config.Mail.MAIL_ACCOUNT);
				mPrefDB.Set_recv_address(Config.Mail.MAIL_RECEIVER);
			}

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		if (IsThere_CalibrationInfo()) {

		} else {
			try {
				GainK40Reset();

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);

				double A = 1.8930099519510156E-4;
				double B = 2.783258244516379;
				double C = 4.2034586689394295;

				double[] avg = new double[] { A, B, C };
				double cs137, cs137_2, K1462;
				cs137 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK1, avg);
				cs137_2 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK2, avg);
				K1462 = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, avg);

				mPrefDB.Set_Calibration_Result(A, B, C, (int) cs137, (int) cs137_2, (int) K1462);
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_found_calibration),
						Toast.LENGTH_LONG).show();

				// NoCalDataTabDisable();

			}

		}

	}

	public void GainK40Reset() {
		double DefaultK40 = 500;

		final double PEACK1e = 32.0;
		final double PEACK2e = 661.660;
		double K40e = 1461;

		Vector<EventData> mAllOne = new Vector<EventData>();
		EventDBOper mEventDB = new EventDBOper();
		mEventDB.OpenDB();
		mAllOne = mEventDB.Load_One_Event();
		Coefficients mAvg = mAllOne.get(0).MS.Get_Coefficients();

		double[] avg = mAvg.get_Coefficients();

		mEventDB.EndDB();

		double cs137, cs137_2, K1462;
		cs137 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK1, avg);
		cs137_2 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK2, avg);
		K1462 = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, avg);

		double Ratio = (((double) DefaultK40 - (double) K1462) / (double) K1462);
		double New_Peak1 = (double) cs137 + ((double) cs137 * Ratio);
		double New_Peak2 = (double) cs137_2 + ((double) cs137_2 * Ratio);

		double[] FitParam = new double[3];
		NcLibrary.QuadraticCal(New_Peak1, New_Peak2, (double) DefaultK40, PEACK1e, PEACK2e, K40e, FitParam);
		mPrefDB.Set_Calibration_Result(FitParam[0], FitParam[1], FitParam[2], (int) New_Peak1, (int) New_Peak2,
				(int) DefaultK40);

	}

	public void StartVisualConnect() {
		if (mDebug.IsSetSpectrumExcute) {
			Setting();
			StartVirsutal();
			StartGCVirsutal();
		}

	}

	public void Setting() {

		if (mDebug.IsDebugMode) {
			if (mDebug.IsBattEnalbe) {
				mBatteryProgBar.Set_Value(100);

				mBatteryProgBar.invalidate();
				Battery.setText(String.valueOf((int) 100) + " %");

			}
		}

		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		String bg_date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DAY_OF_MONTH) + "T" + date.getHours() + ":" + date.getMinutes() + ":"
				+ date.getSeconds() + "." + MSec + NcLibrary.Get_GMT();

		String mBgSpectrum = "";
		mBgSpectrum = mDebug.getSpecInfo().GetBackground();
		specstr = "0;0;0;81;140;157;131;168;187;180;164;172;209;210;231;240;260;234;282;314;347;395;444;413;481;648;739;939;1033;1009;932;836;794;734;699;659;588;492;443;365;357;324;311;300;288;286;283;271;299;288;306;317;274;281;248;235;210;225;240;217;216;217;239;220;207;194;218;206;208;211;216;187;214;204;219;244;236;238;233;257;236;281;274;281;299;320;357;449;550;603;709;803;769;753;723;632;508;436;374;315;287;209;185;181;189;170;141;138;159;152;156;165;151;154;149;145;113;130;160;132;166;138;156;172;180;177;212;218;202;214;211;223;218;216;196;163;134;151;144;116;101;85;86;68;81;81;67;63;69;72;77;84;82;73;77;69;82;71;66;69;71;70;74;78;67;87;51;68;69;72;85;77;76;79;71;80;94;77;95;93;74;89;86;92;84;61;72;66;83;77;86;63;71;102;87;101;94;95;97;84;87;93;82;78;69;77;75;70;54;59;59;57;88;80;83;94;102;109;111;119;120;142;156;162;160;156;180;160;160;177;153;143;125;117;107;87;72;62;67;51;52;46;40;38;41;31;30;45;31;40;42;51;40;32;37;34;33;44;28;54;40;35;42;42;36;47;33;41;41;42;40;41;39;42;58;48;65;45;39;54;69;52;63;50;51;61;45;49;45;37;50;56;39;36;51;40;44;37;29;54;52;49;43;45;47;38;45;25;43;34;63;36;25;28;30;29;33;36;29;39;26;36;34;43;28;30;48;37;34;32;33;43;31;40;44;44;45;54;47;57;58;65;82;79;79;81;95;87;94;94;102;99;87;75;86;87;72;80;82;80;79;74;63;59;73;86;74;94;70;71;74;71;62;76;70;59;61;49;51;31;54;34;36;22;28;28;23;30;26;20;16;13;9;14;19;14;21;8;11;21;14;6;12;9;12;13;9;18;17;15;21;25;18;12;12;21;15;17;20;15;11;18;20;21;10;11;21;16;14;11;14;15;15;10;14;16;8;8;12;12;14;11;8;9;13;10;14;8;11;8;12;4;7;10;12;9;8;7;10;11;11;19;9;12;11;10;5;10;10;10;17;13;5;14;11;7;12;15;5;12;7;14;10;6;12;11;8;9;9;13;12;10;7;6;4;6;7;7;11;4;7;6;9;6;13;4;6;9;7;7;5;8;7;9;10;8;10;5;7;4;4;9;7;11;9;15;11;6;7;7;7;8;10;7;10;12;8;9;14;7;10;8;5;12;11;10;9;18;12;9;11;22;22;11;12;16;8;11;14;8;11;14;11;12;13;4;10;13;7;9;6;6;7;8;11;14;10;5;8;8;6;6;13;8;5;16;12;16;10;14;12;12;5;11;10;16;14;11;10;8;20;14;13;20;14;14;17;11;13;6;10;10;10;20;15;6;15;9;9;10;7;10;10;13;9;7;12;13;5;6;15;13;2;8;14;5;11;4;2;4;5;3;4;2;4;2;4;7;8;12;5;6;1;3;5;3;4;5;5;10;6;5;2;3;5;4;5;6;3;3;5;3;3;5;5;5;4;6;6;7;5;4;1;4;6;3;4;7;4;4;4;5;5;9;6;4;4;3;6;5;4;5;9;9;3;2;6;4;5;4;4;4;6;4;6;7;5;5;4;8;4;4;1;2;4;4;1;7;2;7;5;5;3;4;5;5;4;5;3;2;10;6;7;5;10;3;2;3;2;6;3;5;1;9;7;7;5;7;1;4;2;5;3;5;1;5;8;6;8;6;10;6;5;6;7;8;6;11;6;5;5;15;6;7;10;9;12;8;13;11;5;14;8;12;11;15;12;10;12;8;8;11;8;8;10;11;6;6;3;4;8;5;6;7;10;6;12;12;10;5;8;10;2;4;5;9;8;8;9;8;5;10;7;4;5;9;6;5;2;6;2;3;6;12;5;7;4;10;3;8;2;9;5;6;6;6;14;6;5;4;6;3;4;10;6;6;5;6;2;7;4;3;3;6;4;5;7;8;4;4;4;1;2;4;2;5;6;5;4;2;2;3;2;5;1;1;4;2;1;0;1;1;3;2;3;0;3;3;0;4;3;1;2;3;4;1;1;0;1;3;1;2;4;5;1;2;4;0;2;5;3;2;6;6;8;6;5;4;10;14;8;6;13;13;5;8;17;16;15;13;19;20;14;21;25;20;26;20;22;24;18;19;9;18;23;18;16;15;21;14;14;12;9;13;12;9;9;11;7;7;8;7;9;6;7;3;9;6;7;9;1;7;3;3;2;3;1;2;1;1;2;3;1;3;4;0;2;1;2;0;0;2;1;1;2;1;0;2;2;0;1;2;5;2";

		specstr = SourceSample.CPS_30000;
		specstr1 = "0;0;0;0;0;1;7;38;60;46;57;49;61;76;90;88;86;99;133;146;134;139;151;176;172;169;207;169;160;164;192;167;165;150;161;141;172;159;154;130;142;145;147;124;138;147;129;111;116;100;121;105;118;101;101;98;87;99;89;95;114;97;82;82;79;66;79;68;79;71;93;63;77;69;79;65;62;67;72;66;83;66;55;56;48;71;37;48;44;49;41;58;43;43;29;39;45;38;41;32;36;41;32;38;51;36;35;24;23;23;28;29;35;33;35;28;36;28;38;31;30;27;36;27;18;24;22;27;16;22;21;18;9;15;25;21;16;27;18;16;14;15;12;15;11;13;20;19;10;16;14;17;15;16;11;12;15;11;19;17;11;12;22;12;12;16;21;18;20;11;11;12;9;14;18;11;7;16;15;6;5;9;14;14;8;9;10;14;13;9;12;14;12;18;11;18;6;25;17;13;9;11;14;14;12;14;15;21;25;6;15;14;18;14;14;12;8;7;7;12;11;8;16;9;10;8;12;4;6;8;8;10;9;8;13;2;10;6;11;6;5;8;6;5;10;6;4;9;2;6;6;8;9;5;3;8;6;4;7;7;9;9;4;4;4;7;8;7;6;13;10;15;5;5;13;8;9;5;5;8;6;1;5;6;7;2;10;7;2;13;4;6;5;4;6;8;8;6;5;9;4;5;4;8;7;6;5;8;8;4;6;4;4;9;2;7;3;11;3;10;7;5;7;6;7;5;4;4;2;6;6;8;4;3;9;5;4;6;8;6;5;2;6;4;5;3;3;3;7;6;5;7;3;3;4;4;3;3;6;4;1;8;4;4;1;7;2;1;6;4;3;1;4;3;5;6;5;6;6;6;2;6;6;8;5;5;1;1;2;6;6;4;4;11;2;5;5;4;4;5;5;5;5;1;5;5;1;5;0;0;7;4;4;4;2;5;1;2;2;3;2;4;3;2;3;2;1;4;3;3;4;3;3;4;3;2;3;2;1;0;0;3;2;1;1;3;2;2;4;3;2;2;1;1;3;1;3;2;2;2;4;5;1;3;3;1;1;2;2;5;1;4;2;1;2;3;1;2;5;3;3;3;2;5;2;10;13;3;9;7;14;9;11;18;14;10;11;12;10;6;11;10;10;15;11;8;9;14;12;12;10;9;7;6;3;4;6;2;5;1;5;2;6;2;3;2;3;2;1;1;2;2;1;1;2;2;1;1;1;0;0;0;2;2;3;2;1;1;1;1;1;1;1;0;0;1;1;1;4;1;1;1;1;2;1;1;1;0;0;1;1;2;1;1;1;1;2;3;2;1;1;2;1;1;2;0;0;2;3;2;2;1;1;1;1;1;2;1;2;1;3;0;0;0;1;0;0;1;0;0;1;1;1;1;1;1;1;1;2;2;3;1;1;2;0;0;1;1;1;0;0;0;2;0;0;0;0;0;0;1;1;2;0;0;0;1;2;1;1;1;1;2;1;0;0;0;1;0;0;1;1;0;0;0;0;0;0;3;2;1;1;1;0;0;0;0;0;1;1;0;0;1;0;0;0;1;1;1;0;0;1;1;1;0;0;0;0;1;0;0;0;0;0;1;1;2;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;1;1;1;0;0;0;1;1;0;0;0;0;0;0;1;1;1;0;0;0;1;1;1;1;1;0;0;0;0;0;0;0;0;0;0;1;1;1;1;0;0;0;1;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;1;1;0;0;2;0;0;1;1;0;0;0;0;0;0;1;0;0;0;1;0;0;0;0;0;0;0;0;1;2;1;1;1;1;0;0;0;0;0;0;0;0;0;0;0;0;2;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;1;1;1;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;2;2;1;0;0;0;1;1;1;0;0;0;0;0;0;3;1;0;0;0;0;0;1;0;0;1;1;0;0;0;0;2;1;2;0;0;0;0;1;1;1;1;1;0;0;0;0;1;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0";

		// testspectrum(specstr);

		// mDivideint = 1;

		double[] avg = new double[] { mDebug.getSpecInfo().Coefficients()[0], mDebug.getSpecInfo().Coefficients()[1],
				mDebug.getSpecInfo().Coefficients()[2] };
		double cs137, cs137_2, K1462;
		cs137 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK1, avg);
		cs137_2 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK2, avg);
		K1462 = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, avg);

		mPrefDB.Set_BG_MeasuredAcqTime_From_pref(60);
		mPrefDB.Set_BG_On_pref(testspectrum_background(mBgSpectrum), 1024);
		mPrefDB.Set_BG_Date_From_pref(bg_date);

		mPrefDB.Set_ABC_From_pref(avg);

		mPrefDB.Set_Calibration_Result(avg[0], avg[1], avg[2], (int) cs137, (int) cs137_2, (int) K1462);

		mLogin = LoginDlg.LOGIN_ADMIN;

		Update_StatusBar();

	}

	public int[] testspectrum_background(String mSpec) {

		String[] mSpecSplit = mSpec.split(",");

		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {

			mSpecInt[i] = (int) Double.parseDouble(mSpecSplit[i]);

		}

		return mSpecInt;
	}

	public int[] testspectrum2(String mSpec) {

		String[] mSpecSplit = mSpec.split(", ");

		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {

			mSpecInt[i] = (int) Double.parseDouble(mSpecSplit[i]);

		}

		return mSpecInt;
	}

	public int[] testspectrum1(String mSpec) {

		String[] mSpecSplit = mSpec.split(" ");

		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {

			mSpecInt[i] = Integer.parseInt(mSpecSplit[i]);

		}

		return mSpecInt;
	}

	public void StartGCVirsutal() {

		TimerTask mTask = new TimerTask() {

			@Override
			public void run() {

				mHandler.obtainMessage(MESSAGE_READ_GC, 1024, 500, String.valueOf(Detector.HwPmtProperty_Code.NaI_3x3))
						.sendToTarget();

			}
		};

		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 2000);

	}

	public void StartVirsutal() {
		TimerTask mTask = new TimerTask() {

			@Override
			public void run() {

				SpecCnt++;
				if (SpecCnt >= mDebug.getSpecInfo().GetSource().length - 1) {
					SpecCnt = 0;

				}

				specstr = mDebug.getSpecInfo().GetSource()[SpecCnt];

				mHandler.obtainMessage(MESSAGE_READ_DETECTOR_DATA, 1, -1, testspectrum(specstr, ",")).sendToTarget();
			}
		};

		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 1000, 1000);
	}

	public static int[] testspectrum(String mSpec, String SplitUnit) {

		String[] mSpecSplit = mSpec.split(SplitUnit);
		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {
			double a = Double.valueOf(mSpecSplit[i]).doubleValue();

			mSpecInt[i] = (int) a;

		}

		return mSpecInt;
	}
	
	
	
    private String GetStackTrace(Throwable th)
    {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        Throwable cause = th;
        while (cause != null)
        {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }


	 class CUncaughtExceptionHandlerApp implements Thread.UncaughtExceptionHandler
	    {
	        @Override
	        public void uncaughtException(Thread trd, Throwable ex)
	        {
	            // 예외 상황 처리
	            //Logger.log("APP CRASH!!", GetStackTrace(ex));
	            try
	            {
	                Log.e("MainActivity","App Down!!....Exceprion!!");
	                NcLibrary.Write_ExceptionLog(GetStackTrace(ex));
	            }
	            catch (Exception e)
	            {
	                e.printStackTrace();
	                NcLibrary.Write_ExceptionLog(e);
	            }
	            // 예외 처리 않고 default로 넘김
	            mHnderUncaughException.uncaughtException(trd, ex);
	        }
	    }
	 
	 
	 public void Write_HW_Calibration_Result(GCData mGCData)
	 {

			double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,
					(double) mGCData.K40_Ch };
			double[] FitParam = new double[3];

			NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2,
					NcLibrary.K40_PEAK, FitParam);

			mPrefDB.Set_CryStalType_Name_pref(mEventDBOper.Cry_Info.Crystal_Name);

			mPrefDB.Set_CryStalType_Number_pref(Integer.toString(mGCData.DetType));

			mPrefDB.Set_HW_ABC_From_pref(FitParam, PeakCh);
			mPrefDB.Get_HW_CaliPeakCh1_From_pref();
			mPrefDB.Get_HW_CaliPeakCh3_From_pref();
			mPrefDB.Get_HW_CaliPeakCh2_From_pref();

			mPrefDB.Get_HW_CaliPeak1_From_pref();
			mPrefDB.Get_HW_CaliPeak2_From_pref();
			mPrefDB.Get_HW_CaliPeak3_From_pref();

			//Write_Calibration_Result(FitParam[0], FitParam[1], FitParam[2], Peaks[0], Peaks[1], K40_Ch);
			mPrefDB.Set_Calibration_Result(FitParam[0], FitParam[1], FitParam[2], PeakCh[0], PeakCh[1], PeakCh[2]);
			mDetector.Set_EnergyFittingArgument(FitParam);
			
//			
//			mSPC.Set_Coefficients(FitParam);
//			mSpectrumView.Change_X_to_Energy(FitParam);
//			mDetector.Set_EnergyFittingArgument(FitParam);
		}
	 
	 
}
