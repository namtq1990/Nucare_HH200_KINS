package android.HH100;

import java.io.*;
import java.text.*;
import java.util.*;

import NcLibrary.Coefficients;
import android.HH100.R;
import android.R.bool;
import android.HH100.AutoCalibActivity.MainBCRReceiver;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.Control.*;
import android.HH100.Control.ProgressBar;
import android.HH100.DB.PreferenceDB;
import android.HH100.Dialog.LoginDlg;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.*;
import android.graphics.*;
import android.hardware.Camera;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.*;
import android.preference.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.*;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class SetupSpectrumActivity extends Activity implements View.OnTouchListener {
	private final boolean D = MainActivity.D;
	private final String TAG = "SetupSpectrum";

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ_GAMMA = 21;
	public static final int VIEW_BG_CALI = 1;
	public static final int VIEW_ID = 2;

	public static final String CALIB_ENDCNT = "calib.endcnt";
	public static final String BG_GOALTIME = "bg.goaltime";

	private final int GOAL_COUNT = 200000;

	private final int MSG_MEASURE_BG = 151235;
	private final int MSG_MEASURE_CALIB = 151237;

	// -----------------------------------------------
	private SpectrumView mSpectrumView;
	// private Handler mHandler;

	private int LANDSCAPE = 2;
	private int PORTRAIT = 1;

	private Spectrum mSPC = new Spectrum();

	private int mBG_GoalTime = 0;
	private boolean mIsBackGrounding = false;
	public static boolean mIsCaling = false;
	private int mCalib_EndCnt = GOAL_COUNT;

	private PreferenceDB mPrefDB = null;
	private ProgressBar mProgBar = null;

	SpecturmSurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	private Camera camera;
	TextView firstword, lastword, acqtimeTxt, cpsTxt, totalCountTxt, bottombarTxt, setup_ModeTxt1, setup_ModeTxt2;

	TimerTask mTask;

	TextView Paired, Library, Alarm, Battery;

	int count = 0, count1 = 0;
	private BatteryView mBatteryProgBar = null;
	public static String AcqTimeStr = null, AcqCountStr = null;

	private final int MSG_CALIBRATION = 301248;

	// 액티비티 모드 정의

	public static final String MEASUREMENT_MODE = "activity_mode";
	public static final String MEASUREMENT_BACKGROUND = "background_mode";
	public static final String MEASUREMENT_EN_CALIBRATION = "en_calibration_mode";

	// HW START정의

	// 각각 모드 택스트뷰 선언

	private class MainBCRReceiver extends MainBroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			// Action �젙蹂대�� 媛�吏�怨� �삩�떎.
			String action = intent.getAction();

			switch (action) {

			case MSG_RECV_SPECTRUM:
				Spectrum recv_data = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);
				L("Read Spectrum");
				// ------------------------------------------------------------------------
				mSPC.Accumulate_Spectrum(recv_data);

				Set_SpcInfo_date();
				Set_SpcInfo_cps(recv_data.Get_TotalCount());

				mSpectrumView.SetChArray(mSPC);
				mSpectrumView.invalidate();

				if (mIsBackGrounding)
					mHandler.obtainMessage(MSG_MEASURE_BG, 0, 0, recv_data).sendToTarget();
				else if (mIsCaling)
					mHandler.obtainMessage(MSG_MEASURE_CALIB, 0, 0, recv_data).sendToTarget();

				break;

			case MSG_DISCONNECTED_BLUETOOTH:

				mSPC.ClearSPC();
				mSpectrumView.SetChArray(mSPC);
				mSpectrumView.invalidate();
				break;

			case MSG_START_BACKGROUND:
				onCreatPart();
				break;

			case MAIN_DATA_SEND1:
				Battery = (TextView) findViewById(R.id.BatteryTxt);

				String abc4 = intent.getStringExtra(MainBCRReceiver.DATA_BATTERY);

			
				Battery.setText(Integer.toString(MainActivity.mBattary)+ " %");
				mBatteryProgBar.Set_Value((double)MainActivity.mBattary);
				mBatteryProgBar.invalidate();

				break;
			case MSG_START_CALIBRATION:
				onCreatPart();

				break;
			case MSG_BACKGROUND_CANCEL:
				mProgBar.Set_Value(0);
				mProgBar.invalidate();

				mIsBackGrounding = false;
				mIsCaling = false;
				mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
				mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
				mSpectrumView.invalidate();
				Set_SpcInfo_AcqTime(0);

				Toast.makeText(getApplicationContext(), getResources().getString(R.string.cancel), Toast.LENGTH_LONG)
						.show();

				mSPC.ClearSPC();
				Intent send_gs1 = new Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);

				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs1);

				MainActivity.tabHost.setCurrentTab(0);
				break;

			case MSG_CALIBRATION_CANCEL:

				mIsBackGrounding = false;
				mIsCaling = false;

				break;

			default:
				break;
			}
		}
	}

	private MainBCRReceiver mMainBCR = new MainBCRReceiver();

	// -----------------------
	public SetupSpectrumActivity() {

	}

	protected boolean inProgress;
	LayoutInflater inflater;
	LinearLayout linearLayout, linearLayout1;

	Button startBtn;

	Context mContext;

	FrameLayout mFrameLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		super.onCreate(savedInstanceState);

		// Regist brc receiver
		mContext = this;

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

		layoutParams.screenBrightness = (float) 1.0;

		getWindow().setAttributes(layoutParams);

		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// StartBtn
		IntentFilter filter1 = new IntentFilter();

		filter1.addAction(MainBroadcastReceiver.MSG_TAB_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_TAB_EN_CALIBRATION);
		filter1.addAction(MainBroadcastReceiver.MSG_BACKGROUND_CANCEL);
		filter1.addAction(MainBroadcastReceiver.MSG_CALIBRATION_CANCEL);

		filter1.addAction(MainBroadcastReceiver.MSG_START_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_START_CALIBRATION);

		filter1.addAction(MainBroadcastReceiver.MAIN_DATA_SEND1);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter1);

		String activity = null;
		activity = getIntent().getStringExtra(MEASUREMENT_MODE);

		onCreatPart();

	}

	private void L(String log) {
		if (D)
			Log.i(TAG, log);
	}

	/////// --------------------------------------------
	public String Prefix_CPS(int CPS) {
		DecimalFormat format = new DecimalFormat();
		String Result = null;

		char Pref = 0;
		int ConversionFactor = 1;
		if (CPS > 100000) {
			ConversionFactor = 1000;
			Pref = 'K';
		} // count媛� 留롮븘吏� 寃쎌슦 SI Prefix�궗�슜
		if (CPS > 100000000) {
			ConversionFactor = 1000000;
			Pref = 'M';
		}

		if (ConversionFactor == 1)
			Result = String.valueOf(CPS);
		else
			format.applyLocalizedPattern("0.#");
		Result = format.format(CPS / (double) ConversionFactor) + Pref;

		return Result;
	}

	@Override
	public void onBackPressed() {

		mProgBar.Set_Value(0);
		mProgBar.invalidate();
		mIsCaling = false;
		mIsBackGrounding = false;
		mIsCaling = false;
		mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
		mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
		mSpectrumView.invalidate();
		Set_SpcInfo_AcqTime(0);

		/*
		 * Toast.makeText(getApplicationContext(),
		 * getResources().getString(R.string.cancel), Toast.LENGTH_LONG)
		 * .show();
		 */

		mSPC.ClearSPC();

		finish();
		return;
	}

	private void Set_Spectrum_X_toEnergy() {

		mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients()[0],
				mSPC.Get_Coefficients().get_Coefficients()[1], mSPC.Get_Coefficients().get_Coefficients()[2]);
	}

	@Override

	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.spectrum_menu, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		/*
		 * if (MainActivity.mLogin == LoginDlg.LOGIN_USER) {
		 * 
		 * if (menu.size() > 1) menu.removeItem(menu.getItem(1).getItemId()); }
		 */

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.Cancel_Menu:

			mProgBar.Set_Value(0);
			mProgBar.invalidate();
			mIsCaling = false;
			mIsBackGrounding = false;
			mIsCaling = false;
			mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
			mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
			mSpectrumView.invalidate();
			Set_SpcInfo_AcqTime(0);

			Toast.makeText(getApplicationContext(), getResources().getString(R.string.cancel), Toast.LENGTH_LONG)
					.show();

			mSPC.ClearSPC();

			finish();

			break;
		}

		return true;
	}

	@Override
	protected void onDestroy() {

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
		L("--- ON DESTROY ---");
		super.onDestroy();
	}

	private void Write_Calibration_Result(double A, double B, double C, double PeakCh1, double PeakCh2,

			double PeakCh3) {
		mPrefDB.Set_Calibration_Result(A, B, C, PeakCh1, PeakCh2, PeakCh3);
	}

	private Spectrum Background_GainStabilization(double Before_K40, double Now_K40) {

		Spectrum result = null;

		if (Before_K40 == 0)
			return result;
		try {
			if (Before_K40 == Now_K40)
				return result;
			int[] BG = new int[MainActivity.CHANNEL_ARRAY_SIZE];
			int[] NewBG = new int[MainActivity.CHANNEL_ARRAY_SIZE];
			BG = mPrefDB.Get_BG_From_pref();

			// background adjustment
			int tempindex = 0;
			float diffgap = 0;

			float temp = 0;
			if (Now_K40 == 0)
				diffgap = 1;
			else
				diffgap = (float) Now_K40 / (float) Before_K40;

			for (int i = 0; i < MainActivity.CHANNEL_ARRAY_SIZE; i++) // 梨꾨꼸
																		// �씠�룞
			{
				tempindex = NcLibrary.Auto_floor(((float) i * diffgap));
				if (tempindex >= MainActivity.CHANNEL_ARRAY_SIZE)
					break;
				NewBG[tempindex] = BG[i];
			}

			for (int i = 0; i < MainActivity.CHANNEL_ARRAY_SIZE - 1; i++) // �씠鍮좎쭊怨�
																			// 蹂댁젙
			{
				temp = NewBG[i];
				if (temp <= 0 && (i > 0 && i < MainActivity.CHANNEL_ARRAY_SIZE - 1)) {
					if (NewBG[i - 1] > 0 & NewBG[i + 1] > 0) {
						NewBG[i] = (NewBG[i - 1] + NewBG[i + 1]) / 2;
					}
				}
			}
			result = new Spectrum();
			result.Set_Spectrum(BG, mPrefDB.Get_BG_MeasuredAcqTime_From_pref());
			result.Save_DateNow();
			result.Set_Coefficients(mSPC.Get_Coefficients());
			// mPrefDB.Set_BG_On_pref(NewBG,MainActivity.CHANNEL_ARRAY_SIZE);
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return result;
		}
	}

	private void WriteOnDB_Background_Data(Spectrum SPC) {

		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		String bg_date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DAY_OF_MONTH) + "T" + date.getHours() + ":" + date.getMinutes() + ":"
				+ date.getSeconds() + "." + MSec + NcLibrary.Get_GMT();
		////

		mPrefDB.Set_BG_On_pref(SPC.ToInteger(), SPC.Get_Ch_Size());
		mPrefDB.Set_BG_Date_From_pref(bg_date);
		mPrefDB.Set_BG_MeasuredAcqTime_From_pref(SPC.Get_AcqTime());
	}

	private void Set_SpcInfo_cps(int CPS) {

		// mSpectrumView.Set_inform4(getResources().getString(R.string.cps),
		// String.valueOf(CPS));

		cpsTxt.setText(NcLibrary.Cut_Decimal_Point(CPS));

		totalCountTxt.setText(NcLibrary.Cut_Decimal_Point(mSPC.Get_TotalCount()));
	}

	private void Set_SpcInfo_AcqTime(int AcqTime) {
		acqtimeTxt.setText(String.valueOf(AcqTime) + " sec");
		// mSpectrumView.Set_inform3(getResources().getString(R.string.acq_time),
		// String.valueOf(AcqTime) + " sec");
	}

	private void Set_SpcInfo_date() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		/*
		 * mSpectrumView.Set_inform(getResources().getString(R.string.date),
		 * calendar.get(Calendar.DAY_OF_MONTH) + "." +
		 * (calendar.get(Calendar.MONTH) + 1) + "." +
		 * calendar.get(Calendar.YEAR));
		 * mSpectrumView.Set_inform2(getResources().getString(R.string.time),
		 * date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
		 */
	}

	private void Set_SpcInfo(int AcqTime, int CPS) {
		/*
		 * Calendar calendar = Calendar.getInstance(); Date date =
		 * calendar.getTime();
		 * mSpectrumView.Set_inform(getResources().getString(R.string.date),
		 * calendar.get(Calendar.DAY_OF_MONTH) + "." +
		 * (calendar.get(Calendar.MONTH) + 1) + "." +
		 * calendar.get(Calendar.YEAR));
		 * mSpectrumView.Set_inform2(getResources().getString(R.string.time),
		 * date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
		 * mSpectrumView.Set_inform3(getResources().getString(R.string.acq_time)
		 * , String.valueOf(AcqTime) + " sec");
		 * mSpectrumView.Set_inform4(getResources().getString(R.string.cps),
		 * String.valueOf(CPS));
		 */

	}

	private SurfaceHolder.Callback sufaceListener = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

			camera.release();
			camera = null;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			// TODO Auto-generated method stub
			camera = Camera.open();
			try {
				camera.setPreviewDisplay(surfaceHolder);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			Camera.Parameters param = camera.getParameters();
			param.setPreviewSize(width, height);
			camera.startPreview();

		}
	};

	private android.hardware.Camera.PictureCallback takePicture = new android.hardware.Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			camera.startPreview();
			inProgress = false;
		}
	};

	public void onCreatPart() {

		////////// �젅�씠�븘�썐 �벑濡�

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout m_MainLayout = (LinearLayout) inflater.inflate(R.layout.spectrum, null);

		/////////
		mPrefDB = new PreferenceDB(this);
		mBG_GoalTime = mPrefDB.Get_BG_AcqTime_SetValue_From_pref();
		mSPC.Set_Coefficients(mPrefDB.Get_Cali_ABC_From_pref());
		//
		mProgBar = (ProgressBar) m_MainLayout.findViewById(R.id.SetupSpcSrc_ProgressBar);
		//
		mSpectrumView = (SpectrumView) m_MainLayout.findViewById(R.id.Spectrum);

		acqtimeTxt = (TextView) m_MainLayout.findViewById(R.id.Acq_TimeTxt);

		cpsTxt = (TextView) m_MainLayout.findViewById(R.id.cpsTxt);

		totalCountTxt = (TextView) m_MainLayout.findViewById(R.id.totalCountTxt);

		// bottombarTxt = (TextView)
		// m_MainLayout.findViewById(R.id.bottombarTxt);

		setup_ModeTxt1 = (TextView) m_MainLayout.findViewById(R.id.setup_ModeTxt1);
		setup_ModeTxt2 = (TextView) m_MainLayout.findViewById(R.id.setup_ModeTxt2);

		Paired = (TextView) m_MainLayout.findViewById(R.id.Paired);
		Library = (TextView) m_MainLayout.findViewById(R.id.Library);
		Alarm = (TextView) m_MainLayout.findViewById(R.id.Alarm);

		
		mBatteryProgBar = (BatteryView) m_MainLayout.findViewById(R.id.betterView_ProgressBar);

		

		mBatteryProgBar.Set_Value((double)MainActivity.mBattary);
		mBatteryProgBar.invalidate();
		
		
		Battery = (TextView) m_MainLayout.findViewById(R.id.BatteryTxt);

		Paired.setText(getResources().getString(R.string.HardwareName));

		Library.setText(MainActivity.LibraryStr);

		Alarm.setText(MainActivity.AlarmStr);
		
		Battery.setText(Integer.toString(MainActivity.mBattary)+ " %");

		mSpectrumView.setChArraySize(MainActivity.CHANNEL_ARRAY_SIZE);
		mSpectrumView.LogMode(true);
		Set_Spectrum_X_toEnergy();
		Set_SpcInfo(0, 0);

		mCalib_EndCnt = MainActivity.mPrefDB.Get_Calibration_AcqCnt();
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);
		IntentFilter filter = new IntentFilter();
		filter.addAction(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
		filter.addAction(MainBroadcastReceiver.MSG_BACKGROUND_CANCEL);

		LocalBroadcastManager.getInstance(mContext).registerReceiver(mMainBCR, filter);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(m_MainLayout);

		mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout1);

		mFrameLayout.setOnTouchListener(this);

		String activity = getIntent().getStringExtra(MEASUREMENT_MODE);

		if (activity.equals(MEASUREMENT_BACKGROUND)) {
			// bottombarTxt.setText(getString(R.string.background));

			setup_ModeTxt1.setText(getString(R.string.b));
			setup_ModeTxt2.setText(getString(R.string.ackground));
			StartBackground();
			// openOptionsMenu();
		} else if (activity.equals(MEASUREMENT_EN_CALIBRATION)) {

			setup_ModeTxt1.setText(getString(R.string.e));
			setup_ModeTxt2.setText(getString(R.string.nergy_calibration));

			// bottombarTxt.setText(getString(R.string.energy_calibration));

			StartCalibration();

		}

	}

	public boolean onTouch(View v, MotionEvent event) {

		if (v.getContext() == mFrameLayout.getContext()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// mPreTouchPosX = (int) event.getX();
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				int nTouchPosX = (int) event.getX();

				int nTouchPosY = (int) event.getY();

				String str = Integer.toString(nTouchPosX);

				String str2 = Integer.toString(nTouchPosY);

				// Toast.makeText(getApplicationContext(), "X: " + nTouchPosX +
				// ", Y:" + nTouchPosY, 1).show();

			}
		}
		return true;
	};

	public void StartBackground() {

		MainActivity.ACTIVITY_HW_KEY_ROOT_CHECK = Activity_Mode.NOT_FIRST_ACTIVITY;
		MainActivity.ACTIVITY_STATE = Activity_Mode.BACKGROUND_RUNNING;

		// MainActivity.mService.write(MainActivity.MESSAGE_START_HW);
		Set_SpcInfo(0, 0);

		mSPC.ClearSPC();

		mIsCaling = false;
		mIsBackGrounding = false;

		Set_SpcInfo_AcqTime(0);

		mSPC.ClearSPC();
		mIsBackGrounding = true;
		mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));

		mSPC.Set_StartSystemTime();

	}

	public void StartCalibration() {
		MainActivity.ACTIVITY_HW_KEY_ROOT_CHECK = Activity_Mode.NOT_FIRST_ACTIVITY;
		MainActivity.ACTIVITY_STATE = Activity_Mode.CALIBRATION_RUNNING;

		// MainActivity.mService.write(MainActivity.MESSAGE_START_HW);
		mIsCaling = false;
		mIsBackGrounding = false;

		mSPC.ClearSPC();
		mIsCaling = true;
		mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
		mSpectrumView.Change_X_to_Channel();

	}

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_MEASURE_BG:
				try {

					double Percent = ((double) mSPC.Get_AcqTime() / (double) mBG_GoalTime) * 100.0;
					mProgBar.Set_Value(Percent);
					mProgBar.invalidate();
					Set_SpcInfo_AcqTime(mSPC.Get_AcqTime());
					mSpectrumView.invalidate();

					L("Mesurement BG: AcqTime- " + mSPC.Get_AcqTime() + " / " + mBG_GoalTime + " sec");
					// ---

					if (mBG_GoalTime <= mSPC.Get_AcqTime()) {

						mProgBar.Set_Value(0);
						mProgBar.invalidate();
						mIsBackGrounding = false;

						mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
						Set_SpcInfo_AcqTime(0);

						double[] before_Coeffcient = mPrefDB.Get_Cali_ABC_From_pref();
						if (before_Coeffcient[0] != 0) {
							double[] SmthedBG = NcLibrary.ft_smooth(mSPC.ToInteger(), 0.015, 2.96474);
							double ROI_Start = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 0.8, before_Coeffcient);
							double ROI_End = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 1.2, before_Coeffcient);
							int K40PeakCh = NcLibrary.ROIAnalysis(SmthedBG, (int) ROI_Start, (int) ROI_End);

							/*180321
							 * if (K40PeakCh != 0) {
								double Old_CaliCh1 = mPrefDB.Get_CaliPeak1_From_pref();
								double Old_CaliCh2 = mPrefDB.Get_CaliPeak2_From_pref();
								double Old_K40CH = mPrefDB.Get_CaliPeak3_From_pref();
								double Ratio = (((double) K40PeakCh - Old_K40CH) / Old_K40CH);
								double New_Peak1 = (double) Old_CaliCh1 + ((double) Old_CaliCh1 * Ratio);
								double New_Peak2 = (double) Old_CaliCh2 + ((double) Old_CaliCh2 * Ratio);

								double[] FitParam = new double[3];
								NcLibrary.QuadraticCal(New_Peak1, New_Peak2, (double) K40PeakCh, NcLibrary.CS137_PEAK1,
										NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);

								Coefficients En_coeff = new Coefficients(FitParam);
								Coefficients Ch_coeff = new Coefficients(
										new double[] { New_Peak1, New_Peak2, K40PeakCh });
								Intent intent = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
								intent.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
								intent.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
								LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

								mSPC.Set_Coefficients(FitParam);
								mSpectrumView.Change_X_to_Energy(FitParam);
								L("Recalibration: found K40- " + K40PeakCh + "ch, DR- "
										+ NcLibrary.Channel_to_Energy(1024, FitParam) + " kev");
							}*/
						}
						L("Measured BG- " + mSPC.ToString());
						L("Mesurement BG Success");
						mSPC.Save_DateNow();
						// WriteOnDB_Background_Data(mSPC);

						Intent intent = new Intent(MainBroadcastReceiver.MSG_REMEASURE_BG);
						intent.putExtra(MainBroadcastReceiver.DATA_SPECTRUM, mSPC.ToSpectrum());
						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

						mSPC.ClearSPC();

						Toast.makeText(getApplicationContext(), getResources().getString(R.string.success),
								Toast.LENGTH_LONG).show();

						finish();

					}
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

			case MSG_MEASURE_CALIB:
				try {
					double Percents = ((double) mSPC.Get_TotalCount() / (double) mCalib_EndCnt) * 100.0;
					mProgBar.Set_Value(Percents);
					mProgBar.invalidate();
					Set_SpcInfo_AcqTime(mSPC.Get_AcqTime());
					mSpectrumView.invalidate();

					L("Energy Calibration: AcqTime- " + mSPC.Get_TotalCount() + " / " + mCalib_EndCnt + " cnt");
					// -----------------

					if (mCalib_EndCnt < mSPC.Get_TotalCount()) { // end measure
						mProgBar.Set_Value(0);
						mProgBar.invalidate();
						Set_SpcInfo_AcqTime(0);
						mIsCaling = false;
						mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));

						// ----
						double[] Peaks = new double[2];
						Peaks[0] = (double) NcLibrary.FindPeak(mSPC.ToInteger())[0];

						double[] temp = new double[1024];
						temp = NcLibrary.ft_smooth(mSPC.ToInteger(), 0.015, 2.96474);

						Peaks[1] = (double) NcLibrary.FindPeak(mSPC.ToInteger())[1];

						if (Peaks[0] == 0 || Peaks[1] == 0) {
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed),
									Toast.LENGTH_LONG).show();
						} else {
							double resultA = ((float) (Math.max(NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2)
									- Math.min(NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2))
									/ (Math.max(Peaks[0], Peaks[1]) - Math.min(Peaks[0], Peaks[1])));
							double resultB = (float) (NcLibrary.CS137_PEAK1 - Peaks[0] * resultA);
							int ch1 = (int) NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 0.9, resultA, resultB, 0);
							int ch2 = (int) NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 1.1, resultA, resultB, 0);
							int K40_Ch = NcLibrary.ROIAnalysis(temp, ch1, ch2);
							double[] FitParam = new double[3];
							double[] PeakCh= {Peaks[0], Peaks[1], K40_Ch};
							NcLibrary.QuadraticCal(Peaks[0], Peaks[1], K40_Ch, NcLibrary.CS137_PEAK1,
									NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);

//							Spectrum New_bg = Background_GainStabilization(mPrefDB.Get_CaliPeak3_From_pref(), K40_Ch);
							Write_Calibration_Result(FitParam[0], FitParam[1], FitParam[2], Peaks[0], Peaks[1], K40_Ch);
							
							// --===--
							Coefficients En_coeff = new Coefficients(FitParam);
							Coefficients Ch_coeff = new Coefficients(new double[] { Peaks[0], Peaks[1], K40_Ch });
							
							int[] caliInfo= {(int)Peaks[0], (int)Peaks[1], K40_Ch, MainActivity.mDetector.mHW_GC };
					NcLibrary.SaveTextCali(caliInfo,MainActivity.FilenameCaliInfo, 4);

							// mHW_GC, Channal info update
							MainActivity.mDetector.mHW_Cs137_FxiedCh1=(int)Peaks[0];
							MainActivity.mDetector.mHW_Cs137_FxiedCh2=(int)Peaks[1];
							MainActivity.mDetector.mHW_K40_FxiedCh=K40_Ch;
							MainActivity.mDetector.mHW_FixedGC=MainActivity.mDetector.mHW_GC;
							
							
							
							
							mSPC.Set_Coefficients(FitParam); 
							mSpectrumView.Change_X_to_Energy(FitParam);
							
							Intent intent = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
							intent.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
							intent.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

//							intent = new Intent(MainBroadcastReceiver.MSG_REMEASURE_BG);
//							intent.putExtra(MainBroadcastReceiver.DATA_SPECTRUM, New_bg);
//							if (New_bg != null)
//								LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//							// --===--

							Toast.makeText(getApplicationContext(), getResources().getString(R.string.success),
									Toast.LENGTH_LONG).show();
							L("Measured BG- " + mSPC.ToString());
							L("Calibration Success: Peaks- " + Peaks[0] + ", " + Peaks[1] + ", " + K40_Ch + "ch, DR- "
									+ NcLibrary.Channel_to_Energy(1024, FitParam) + " kev");

						}

						/*
						 * NcLibrary.Export_spectrum_data( "Calibration",mSPC
						 * .ToInteger(), mSPC.Get_Ch_Size());
						 * NcLibrary.Export_spectrum_data(
						 * "Calibration_smoothed",temp, mSPC.Get_Ch_Size());
						 * NcLibrary.Export_spectrum_data(
						 * "Calibration_Background",mPrefDB. Get_BG_From_pref
						 * (), 1024);
						 */

						/*
						 * Intent send_gs = new
						 * Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);
						 * 
						 * LocalBroadcastManager.getInstance(
						 * getApplicationContext()).sendBroadcast(send_gs);
						 */
						finish();
						mSPC.ClearSPC();
					}
				} catch (Exception e) {
					
					NcLibrary.Write_ExceptionLog(e);

				}
				break;

			case MSG_CALIBRATION:

				break;

			default:
				break;
			}

		}
	};

	@Override

	protected void onResume() {

		// Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);

		// LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
		/*
		 * String activity = null; activity =
		 * getIntent().getStringExtra(MEASUREMENT_MODE);
		 * 
		 * if (activity.equals(MEASUREMENT_BACKGROUND)) {
		 * 
		 * Start_Measurement_BG();
		 * 
		 * } else if (activity.equals(MEASUREMENT_EN_CALIBRATION)) {
		 * 
		 * Start_EnCalibration();
		 * 
		 * }
		 */
		super.onResume();
	}

	public void Start_EnCalibration() {

		IntentFilter filter1 = new IntentFilter();

		filter1.addAction(MainBroadcastReceiver.MSG_TAB_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_TAB_EN_CALIBRATION);
		filter1.addAction(MainBroadcastReceiver.MSG_BACKGROUND_CANCEL);
		filter1.addAction(MainBroadcastReceiver.MSG_CALIBRATION_CANCEL);

		filter1.addAction(MainBroadcastReceiver.MSG_START_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_START_CALIBRATION);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter1);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		linearLayout = (LinearLayout) inflater.inflate(R.layout.start_calibration, null);

		setContentView(linearLayout);

		AcqCountStr = String.valueOf(MainActivity.mPrefDB.Get_Calibration_AcqCnt());

		// StartBtn

		startBtn = (Button) findViewById(R.id.StartBtn);

		startBtn.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {

				onCreatPart();

			}

		});

	}

	public void Start_Measurement_BG() {

		IntentFilter filter1 = new IntentFilter();

		filter1.addAction(MainBroadcastReceiver.MSG_TAB_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_TAB_EN_CALIBRATION);
		filter1.addAction(MainBroadcastReceiver.MSG_BACKGROUND_CANCEL);
		filter1.addAction(MainBroadcastReceiver.MSG_CALIBRATION_CANCEL);

		filter1.addAction(MainBroadcastReceiver.MSG_START_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_START_CALIBRATION);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter1);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		linearLayout = (LinearLayout) inflater.inflate(R.layout.start_background, null);

		setContentView(linearLayout);

		IntentFilter filter2 = new IntentFilter();

		AcqTimeStr = String.valueOf(MainActivity.mPrefDB.Get_BG_AcqTime_SetValue_From_pref());

		// StartBtn

		startBtn = (Button) findViewById(R.id.StartBtn);

		startBtn.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {

				onCreatPart();

			}

		});

	}

}
