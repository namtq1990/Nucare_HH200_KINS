package android.HH100;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import Debug.Version;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.DB.PreferenceDB;
import android.HH100.Dialog.EmailSetupActivity;
import android.HH100.Dialog.LoginDlg;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.Structure.NcLibrary;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.content.LocalBroadcastManager;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PreferenceActivity extends android.preference.PreferenceActivity implements OnPreferenceChangeListener {

	private EditTextPreference mPre_inCharge;
	private EditTextPreference mPre_Location;
	private EditTextPreference mPre_GammaThre;
	private EditTextPreference mPre_NeutronThre;
	private EditTextPreference mPre_A;
	private EditTextPreference mPre_B;
	private EditTextPreference menuEnableKey;

	private ListPreference mIsoLib_List;
	private EditTextPreference medit_GammaThre;

	private PreferenceCategory mPreferenceCategory;

	private Preference mLastUser;
	// private Preference mLastDetector;
	private Preference mLastTime;
	private PreferenceScreen mSWinformScreen, mBackGroundMesurement, mCalibrationMesurement, mMesurement, mAdmin,
			SystemLog,mResetCalibration,reachBack;

	private Context mContext;
	public static Activity PreferenceActivity;

	boolean DebugginMode = false;

	interface Isotope {

		int ANSI_NAI = 0;
		int ANSI_Cebr = 1;
		int IND_NaI = 2;
		int MED_NaI = 3;
		int ANSI_Nai_KINS = 4;

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		addPreferencesFromResource(R.xml.setting);

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		MainActivity.ACTIVITY_STATE = Activity_Mode.SETUP_MAIN;

		mContext = this;

		reachBack = (PreferenceScreen) findPreference("reachback");
		reachBack.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(PreferenceActivity.this, ReachBackListActivity.class));
				return false;
			}
		});

		mSWinformScreen = (PreferenceScreen) findPreference("p_inform");
		mLastUser = mSWinformScreen.findPreference("p_lastUser");
		mLastTime = mSWinformScreen.findPreference("p_lastTime");
		// mLastDetector = mSWinformScreen.findPreference("p_lastDetector");

		// Adminrock();
		mAdmin = (PreferenceScreen) findPreference(getString(R.string.p_admin));
		final PreferenceScreen menuEnableKey = (PreferenceScreen) findPreference(getString(R.string.menu_enable_key));
		menuEnableKey.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				LinearLayout layout = new LinearLayout(PreferenceActivity.this);
				layout.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

/*				//비밀번호를 입력해주세요
				TextView msg1= new TextView(PreferenceActivity.this);
				msg1.setGravity(Gravity.CENTER);
				msg1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
				msg1.setText(getString(R.string.password));
				msg1.setPadding(0,20,0,0);
				layout.addView(msg1);*/

				//password
				final EditText password = new EditText(PreferenceActivity.this);
				password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
				//password.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				//password.setPrivateImeOptions("defaultInputmode=numeric;"); //영어로 기본 자판 설정
				password.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
				password.setTransformationMethod(new PasswordTransformationMethod());
				password.setGravity(Gravity.CENTER_HORIZONTAL);
				lp.setMargins(20, 20, 20, 30);
				password.setLayoutParams(lp);
				layout.addView(password);

				AlertDialog.Builder security = new AlertDialog.Builder(new ContextThemeWrapper(PreferenceActivity.this, android.R.style.Theme_Holo_Dialog));
				security.setCancelable(true);
				security.setTitle(getString(R.string.admin));
				security.setView(layout);
				security.setPositiveButton(getResources().getString(R.string.check), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{

						try {
							PreferenceDB mPrefDB = new PreferenceDB(PreferenceActivity.this);
							mPrefDB.Get_AdminPW_From_pref();
							if (password.getText().toString().equals(mPrefDB.Get_AdminPW_From_pref())|| password.getText().toString().equals(getResources().getString(R.string.master_PW)))
							{
								//menuEnableKey.setText("");
								mAdmin.setEnabled(true);
								mAdmin.setEnabled(true);
								menuEnableKey.setEnabled(false);

							}
							else
							{
								//menuEnableKey.setText("");
								mAdmin.setEnabled(false);
								mAdmin.setEnabled(false);
							}
						} catch (Exception e) {
							NcLibrary.Write_ExceptionLog(e);
						}
					}
				});
				security.setNegativeButton(getResources().getString(R.string.cancel), null);
				AlertDialog dlg =  security.create();
				dlg.show();

				return false;
			}
		});

		final PreferenceScreen setPw = (PreferenceScreen) findPreference(getString(R.string.Admin_Password));
		setPw.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				PreferenceDB pre = new PreferenceDB(PreferenceActivity.this);
				LinearLayout layout = new LinearLayout(PreferenceActivity.this);
				layout.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

				//password
				final EditText password = new EditText(PreferenceActivity.this);
				//password.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD );
				//password.setPrivateImeOptions("defaultInputmode=numeric;"); //영어로 기본 자판 설정
				password.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
				password.setTransformationMethod(new PasswordTransformationMethod());
				password.setGravity(Gravity.CENTER_HORIZONTAL);
				password.setHint(pre.Get_AdminPW_From_pref());
				lp.setMargins(20, 20, 20, 30);
				password.setLayoutParams(lp);
				layout.addView(password);

				AlertDialog.Builder security = new AlertDialog.Builder(new ContextThemeWrapper(PreferenceActivity.this, android.R.style.Theme_Holo_Dialog));
				security.setCancelable(true);
				security.setTitle(getString(R.string.admin));
				security.setView(layout);
				security.setPositiveButton(getResources().getString(R.string.check), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{

						try {

							pre.Set_String_on_pref(PreferenceActivity.this.getString(R.string.Admin_Password),password.getText().toString() );

/*							if (password.getText().toString().equals(mPrefDB.Get_AdminPW_From_pref())|| password.getText().toString().equals(getResources().getString(R.string.master_PW)))
							{
								//menuEnableKey.setText("");
								mAdmin.setEnabled(true);
								mAdmin.setEnabled(true);
								menuEnableKey.setEnabled(false);

							}
							else
							{
								//menuEnableKey.setText("");
								mAdmin.setEnabled(false);
								mAdmin.setEnabled(false);
							}*/
						} catch (Exception e) {
							NcLibrary.Write_ExceptionLog(e);
						}
					}
				});
				security.setNegativeButton(getResources().getString(R.string.cancel), null);
				AlertDialog dlg =  security.create();
				dlg.show();

				return false;
			}
		});


		if (DebugginMode == false) {

			mAdmin.setEnabled(false);
		}


/*		menuEnableKey.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				try {

					PreferenceDB mPrefDB = new PreferenceDB(PreferenceActivity.this);
					mPrefDB.Get_AdminPW_From_pref();
					if (newValue.toString().equals(mPrefDB.Get_AdminPW_From_pref())
							|| newValue.toString().equals(getResources().getString(R.string.master_PW))) {

						//menuEnableKey.setText("");
						mAdmin.setEnabled(true);
						mAdmin.setEnabled(true);
						menuEnableKey.setEnabled(false);

						return false;
					} else {

						//menuEnableKey.setText("");
						mAdmin.setEnabled(false);
						mAdmin.setEnabled(false);

					}
					return true;
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
					return false;
				}
			}
		});*/

		mAdmin.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				return false;
			}
		});

		
		mResetCalibration = (PreferenceScreen) findPreference(getString(R.string.reset_Calibartion_key));

		mResetCalibration.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference) {

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
				dialogBuilder.setTitle(getResources().getString(R.string.reset_Calibartion_sub_title));
				dialogBuilder.setMessage(getResources().getString(R.string.reset_Calibartion_Dlg));
				dialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) {

								PreferenceDB mPreb = new PreferenceDB(getApplicationContext());

								// mPreb.Set_Initialization();

								if (mPreb.Get_HW_CaliPeakCh1_From_pref() == 0
										|| mPreb.Get_HW_CaliPeakCh2_From_pref() == 0) {

									Toast.makeText(getApplicationContext(), "data does not exist", 1).show();
								} else {
									Toast.makeText(getApplicationContext(), "success", 1).show();

									double Cal_A, Cal_B, Cal_C, Cal_Ch1, Cal_Ch2, Cal_Ch3;

									Cal_A = mPreb.Get_HW_CaliPeak1_From_pref();
									Cal_B = mPreb.Get_HW_CaliPeak2_From_pref();
									Cal_C = mPreb.Get_HW_CaliPeak3_From_pref();

									Cal_Ch1 = mPreb.Get_HW_CaliPeakCh1_From_pref();
									Cal_Ch2 = mPreb.Get_HW_CaliPeakCh2_From_pref();
									Cal_Ch3 = mPreb.Get_HW_CaliPeakCh3_From_pref();

									mPreb.Set_Calibration_Result(Cal_A, Cal_B, Cal_C, Cal_Ch1, Cal_Ch2, Cal_Ch3);

									mPreb.Get_Cali_A_From_pref();
									mPreb.Get_Cali_B_From_pref();
									mPreb.Get_Cali_C_From_pref();

									Intent send_gs = new Intent(MainBroadcastReceiver.MSG_FIXED_GC_SEND);
									LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

								}

								// OptionDefaultData mDefault = new OptionDefaultData(getApplicationContext());
								// SetIsotopeList(Integer.parseInt(mDefault.IsoLib_list));
								//
								// finish();
								// startActivity(getIntent());
							}
						});
				dialogBuilder.setNegativeButton("Cancel", null);
				dialogBuilder.setCancelable(false);
				dialogBuilder.show();

				return false;
			}
		});
		
		
		
		mBackGroundMesurement = (PreferenceScreen) findPreference("SetBackgroundMeasurementMode");

		mBackGroundMesurement.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				Intent intent = new Intent(PreferenceActivity.this, SetupSpectrumActivity.class);
				intent.putExtra(SetupSpectrumActivity.MEASUREMENT_MODE, SetupSpectrumActivity.MEASUREMENT_BACKGROUND);
				intent.putExtra(SetupSpectrumActivity.BG_GOALTIME,
						MainActivity.mPrefDB.Get_BG_AcqTime_SetValue_From_pref());
				startActivity(intent);

				return false;
			}
		});

		mCalibrationMesurement = (PreferenceScreen) findPreference("SetCalibrationMode");

		mCalibrationMesurement.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				Intent intent = new Intent(PreferenceActivity.this, SetupSpectrumActivity.class);
				intent.putExtra(SetupSpectrumActivity.MEASUREMENT_MODE,
						SetupSpectrumActivity.MEASUREMENT_EN_CALIBRATION);
				intent.putExtra(SetupSpectrumActivity.CALIB_ENDCNT, MainActivity.mPrefDB.Get_Calibration_AcqCnt());
				startActivity(intent);

				return false;
			}
		});

		// mPreferenceCategory = (PreferenceCategory)
		// findPreference("p_alarm_title");
		/*
		 * String s= "<font color=\"green\">"+"hello" + "</font>";
		 * 
		 * mPreferenceCategory.setTitle(Html.fromHtml(s));
		 * 
		 */

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		mLastUser.setSummary(pref.getString(getResources().getString(R.string.last_user), "None"));
		mLastTime.setSummary(pref.getString(getResources().getString(R.string.last_time), "None"));
		// mLastDetector.setTitle(pref.getString(getResources().getString(R.string.last_detector),
		// "None"));
		// mLastDetector.setSummary(pref.getString(getResources().getString(R.string.last_detectorMac),
		// "None"));

		Preference temp2 = findPreference("IsoLib_list");
		ListPreference listpr = (ListPreference) temp2;

		temp2.setDefaultValue(3);

		IsotopesLibrary Isolib = new IsotopesLibrary(this);
		Vector<String> IsoLibList = Isolib.get_IsotopeLibrary_List();

		String[] temp = new String[IsoLibList.size()];
		for (int i = 0; i < IsoLibList.size(); i++) {
			temp[i] = IsoLibList.get(i);
		}

		listpr.setEntries(temp);
		listpr.setEntryValues(temp);

		// =------
		Preference preAppver = (Preference) findPreference(getResources().getString(R.string.app_version));
		try 
		{
			preAppver.setSummary(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) 
		{
			// TODO Auto-generated catch block
			NcLibrary.Write_ExceptionLog(e);
		}
	
		Preference Sw_Update = (Preference) findPreference(getResources().getString(R.string.p_sw_update));
		Sw_Update.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri u = null;
				
				if (Version.IsKinsVersion) {
					
					u = Uri.parse("http://nucaremed.com/PeakAboutUpdateK200.html");
					
				} else {
					
					u = Uri.parse("http://nucaremed.com/PeakAboutUpdateK2.html");
					
				}
				i.setData(u);
				startActivity(i);

				return false;
			}
		});

		PreferenceScreen setup_email = (PreferenceScreen) findPreference(
				getResources().getString(R.string.p_export_event));
		setup_email.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(PreferenceActivity.this, EmailSetupActivity.class));
				return false;
			}
		});

		EditTextPreference upper_disc = (EditTextPreference) findPreference(
				getResources().getString(R.string.p_upper_discri));
		upper_disc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {

				try {
					if (Integer.valueOf(newValue.toString()) > MainActivity.CHANNEL_ARRAY_SIZE) {
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
						dialogBuilder.setTitle("Warning");
						dialogBuilder.setMessage("< 1024");

						dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
						dialogBuilder.setCancelable(false);
						dialogBuilder.show();

						return false;
					} else if (Integer.valueOf(newValue.toString()) < 0) {
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
						dialogBuilder.setTitle("Warning");
						dialogBuilder.setMessage("> 0");

						dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
						dialogBuilder.setCancelable(false);
						dialogBuilder.show();

						return false;
					}
					return true;
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
					return false;
				}
			}
		});
		EditTextPreference low_disc = (EditTextPreference) findPreference(
				getResources().getString(R.string.p_low_discri));
		low_disc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					if (Integer.valueOf(newValue.toString()) > MainActivity.CHANNEL_ARRAY_SIZE) {
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
						dialogBuilder.setTitle("Warning");
						dialogBuilder.setMessage("< 1024");

						dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
						dialogBuilder.setCancelable(false);
						dialogBuilder.show();

						return false;
					} else if (Integer.valueOf(newValue.toString()) < 0) {
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
						dialogBuilder.setTitle("Warning");
						dialogBuilder.setMessage("> 0");

						dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
						dialogBuilder.setCancelable(false);
						dialogBuilder.show();

						return false;
					}
					return true;
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
					return false;
				}
			}
		});

		medit_GammaThre = (EditTextPreference) findPreference("p_gamma_threshold");

		medit_GammaThre.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Set_String_on_pref("p_IsSigma", "0");

				Set_String_on_pref("p_gamma_threshold", String.valueOf(newValue));

				return true;
			}
		});
		EditTextPreference edit_SigmaValue = (EditTextPreference) findPreference("p_gamma_Sigma");
		edit_SigmaValue.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Set_String_on_pref("p_IsSigma", "1");

				Set_String_on_pref("p_gamma_Sigma", String.valueOf(newValue));
				return true;
			}
		});

		// -----
		EditTextPreference edit_User = (EditTextPreference) findPreference(getResources().getString(R.string.user));
		edit_User.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				mLastUser.setSummary(String.valueOf(newValue));
				return true;
			}
		});

		SystemLog = (PreferenceScreen) findPreference(getString(R.string.System_Log_Transfer_Key));

		SystemLog.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				NcLibrary.SendSystemLog(mContext);

				return false;
			}
		});

		ListPreference DoseUnit = (ListPreference) findPreference("p_DoseUnit");
		if (Get_String_From_pref("p_DoseUnit").matches("1")) {
			EditTextPreference HealthThre = (EditTextPreference) findPreference(
					getResources().getString(R.string.healthy_threshold));
			String sum = HealthThre.getSummary().toString().replace("Sv/h", "rem/h");
			HealthThre.setSummary(sum);
		}
		DoseUnit.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String s = Get_String_From_pref("p_DoseUnit");
				if (s.matches(newValue.toString()))
					return true;
				Change_HealthAlarm_Unit((Integer.valueOf(newValue.toString()) == 0) ? true : false);
				return true;
			}
		});
		//

		//DefaultIsotopeList(Isotope.ANSI_Nai_KINS);
		DefaultIsotopeList(0);

		///////////// User mode
		/*
		 * if (MainActivity.mLogin == LoginDlg.LOGIN_USER) { PreferenceScreen email_src
		 * = (PreferenceScreen) findPreference(
		 * getResources().getString(R.string.p_export_event));
		 * email_src.setEnabled(false);
		 * 
		 * EditTextPreference edit_CaliTime = (EditTextPreference)
		 * findPreference("Calibration measurement time");
		 * edit_CaliTime.setEnabled(false); EditTextPreference edit_BgSec =
		 * (EditTextPreference) findPreference("p_second");
		 * edit_BgSec.setEnabled(false); //ListPreference edit_AlarmSound =
		 * (ListPreference) findPreference("IsoLib_list");
		 * //edit_AlarmSound.setEnabled(false);
		 * 
		 * ListPreference edit_DoseUnit = (ListPreference) findPreference("p_DoseUnit");
		 * edit_DoseUnit.setEnabled(false); EditTextPreference Admin_Pass =
		 * (EditTextPreference) findPreference("Admin_Password");
		 * Admin_Pass.setEnabled(false); EditTextPreference NeutronThre =
		 * (EditTextPreference) findPreference(
		 * getResources().getString(R.string.neutron_threshold));
		 * NeutronThre.setEnabled(false); EditTextPreference HealthThre =
		 * (EditTextPreference) findPreference(
		 * getResources().getString(R.string.healthy_threshold));
		 * HealthThre.setEnabled(false); PreferenceScreen PrefSrc_SetAlarmMode =
		 * (PreferenceScreen) findPreference("SetAlarmMode");
		 * PrefSrc_SetAlarmMode.setEnabled(false); PreferenceScreen disc =
		 * (PreferenceScreen) findPreference(getResources().getString(R.string.p_disc));
		 * disc.setEnabled(false); EditTextPreference manualID_defaultTime =
		 * (EditTextPreference) findPreference(
		 * getResources().getString(R.string.p_manual_id_defalut));
		 * manualID_defaultTime.setEnabled(false); EditTextPreference
		 * manualID_adjustTime = (EditTextPreference) findPreference(
		 * getResources().getString(R.string.p_manual_id_adjust));
		 * manualID_adjustTime.setEnabled(false); PreferenceScreen seqMode =
		 * (PreferenceScreen) findPreference(
		 * getResources().getString(R.string.p_psrc_sequential));
		 * seqMode.setEnabled(false);
		 * 
		 * PreferenceScreen admin_pw = (PreferenceScreen)
		 * findPreference(getResources().getString(R.string.admin_pw));
		 * admin_pw.setEnabled(false);
		 * 
		 * PreferenceScreen SetBackgroundMeasurementMode = (PreferenceScreen)
		 * findPreference(
		 * getResources().getString(R.string.SetBackgroundMeasurementMode));
		 * SetBackgroundMeasurementMode.setEnabled(false);
		 * 
		 * PreferenceScreen SetCalibrationMode = (PreferenceScreen) findPreference(
		 * getResources().getString(R.string.SetCalibrationMode));
		 * SetCalibrationMode.setEnabled(false);
		 * 
		 * } else { // EditTextPreference Admin_Pass = (EditTextPreference) //
		 * findPreference("Admin_Password"); //
		 * Admin_Pass.setDefaultValue(String.valueOf(pref.get)) }
		 */
		PreferenceActivity = this;
	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	@Override
	public void onContentChanged() {
		// TODO Auto-generated method stub
		super.onContentChanged();
	}

	private void Change_HealthAlarm_Unit(boolean IsSv) {

		if (IsSv) {
			EditTextPreference HealthThre = (EditTextPreference) findPreference(
					getResources().getString(R.string.healthy_threshold));
			String summary = HealthThre.getSummary().toString();
			summary = summary.replace("rem/h", "Sv/h");
			HealthThre.setSummary(summary);

			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			int Value = Integer.valueOf(pref.getString(getResources().getString(R.string.healthy_threshold), "0"));
			Value = (int) NcLibrary.Rem_To_Sv(Value);
			HealthThre.setText(String.valueOf(Value));

		} else {
			EditTextPreference HealthThre = (EditTextPreference) findPreference(
					getResources().getString(R.string.healthy_threshold));
			String summary = HealthThre.getSummary().toString();
			summary = summary.replace("Sv/h", "rem/h");
			HealthThre.setSummary(summary);

			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			int Value = Integer.valueOf(pref.getString(getResources().getString(R.string.healthy_threshold), "0"));
			Value = (int) NcLibrary.Sv_To_Rem(Value);
			HealthThre.setText(String.valueOf(Value));
		}
	}

	public String Get_String_From_pref(String key) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		return pref.getString(key, "0");
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		// TODO Auto-generated method stub
		return false;
	}

	public void Set_String_on_pref(String key, String Value) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, Value);
		editor.commit();
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		// loadHeadersFromResource(R.xml.setting, target);
	}

	@Override
	protected void onDestroy() {

		MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;

		super.onDestroy();
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(PreferenceActivity.this);
		// Get the layout inflater
		LayoutInflater inflater = PreferenceActivity.this.getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflater.inflate(R.layout.dialog_signin, null))
				// Add action buttons
				.setPositiveButton("ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// sign in the user ...
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// LoginDialogFragment.this.getDialog().cancel();
					}
				});
		return builder.create();
	}

	public void Adminrock() {

		PreferenceScreen email_src = (PreferenceScreen) findPreference(
				getResources().getString(R.string.p_export_event));
		email_src.setEnabled(false);

		EditTextPreference edit_CaliTime = (EditTextPreference) findPreference("Calibration measurement time");
		edit_CaliTime.setEnabled(false);
		EditTextPreference edit_BgSec = (EditTextPreference) findPreference("p_second");
		edit_BgSec.setEnabled(false);
		ListPreference edit_AlarmSound = (ListPreference) findPreference("IsoLib_list");
		edit_AlarmSound.setEnabled(false);

		ListPreference edit_DoseUnit = (ListPreference) findPreference("p_DoseUnit");
		edit_DoseUnit.setEnabled(false);
		//admin password표시 180130
		PreferenceScreen Admin_Pass = (PreferenceScreen) findPreference("Admin_Password");
		Admin_Pass.setEnabled(false);
/*		EditTextPreference Admin_Pass = (EditTextPreference) findPreference("Admin_Password");
		Admin_Pass.setEnabled(false);*/
		EditTextPreference NeutronThre = (EditTextPreference) findPreference(
				getResources().getString(R.string.neutron_threshold));
		NeutronThre.setEnabled(false);
		EditTextPreference HealthThre = (EditTextPreference) findPreference(
				getResources().getString(R.string.healthy_threshold));
		HealthThre.setEnabled(false);
		PreferenceScreen PrefSrc_SetAlarmMode = (PreferenceScreen) findPreference("SetAlarmMode");
		PrefSrc_SetAlarmMode.setEnabled(false);
		PreferenceScreen disc = (PreferenceScreen) findPreference(getResources().getString(R.string.p_disc));
		disc.setEnabled(false);
		EditTextPreference manualID_defaultTime = (EditTextPreference) findPreference(
				getResources().getString(R.string.p_manual_id_defalut));
		manualID_defaultTime.setEnabled(false);
		EditTextPreference manualID_adjustTime = (EditTextPreference) findPreference(
				getResources().getString(R.string.p_manual_id_adjust));
		manualID_adjustTime.setEnabled(false);
		PreferenceScreen seqMode = (PreferenceScreen) findPreference(
				getResources().getString(R.string.p_psrc_sequential));
		seqMode.setEnabled(false);

/*		EditTextPreference admin_pw = (EditTextPreference) findPreference(
				getResources().getString(R.string.Admin_Password));
		admin_pw.setEnabled(false);*/
		PreferenceScreen admin_pw = (PreferenceScreen) findPreference("Admin_Password");
		admin_pw.setEnabled(false);

		PreferenceScreen SetBackgroundMeasurementMode = (PreferenceScreen) findPreference(
				getResources().getString(R.string.SetBackgroundMeasurementMode));
		SetBackgroundMeasurementMode.setEnabled(false);

		PreferenceScreen SetCalibrationMode = (PreferenceScreen) findPreference(
				getResources().getString(R.string.SetCalibrationMode));
		SetCalibrationMode.setEnabled(false);

		PreferenceScreen rad_response_option = (PreferenceScreen) findPreference(
				getResources().getString(R.string.rad_response_option_key));
		SetCalibrationMode.setEnabled(false);

	}

	public void AdminUnrock() {

		PreferenceScreen email_src = (PreferenceScreen) findPreference(
				getResources().getString(R.string.p_export_event));
		email_src.setEnabled(true);

		EditTextPreference edit_CaliTime = (EditTextPreference) findPreference("Calibration measurement time");
		edit_CaliTime.setEnabled(true);
		EditTextPreference edit_BgSec = (EditTextPreference) findPreference("p_second");
		edit_BgSec.setEnabled(false);
		ListPreference edit_AlarmSound = (ListPreference) findPreference("IsoLib_list");
		edit_AlarmSound.setEnabled(true);

		ListPreference edit_DoseUnit = (ListPreference) findPreference("p_DoseUnit");
		edit_DoseUnit.setEnabled(false);
/*		EditTextPreference Admin_Pass = (EditTextPreference) findPreference("Admin_Password");
		Admin_Pass.setEnabled(true);*/
		PreferenceScreen Admin_Pass = (PreferenceScreen) findPreference("Admin_Password");
		Admin_Pass.setEnabled(false);
		EditTextPreference NeutronThre = (EditTextPreference) findPreference(
				getResources().getString(R.string.neutron_threshold));
		NeutronThre.setEnabled(true);
		EditTextPreference HealthThre = (EditTextPreference) findPreference(
				getResources().getString(R.string.healthy_threshold));
		HealthThre.setEnabled(true);
		PreferenceScreen PrefSrc_SetAlarmMode = (PreferenceScreen) findPreference("SetAlarmMode");
		PrefSrc_SetAlarmMode.setEnabled(true);
		PreferenceScreen disc = (PreferenceScreen) findPreference(getResources().getString(R.string.p_disc));
		disc.setEnabled(true);
		EditTextPreference manualID_defaultTime = (EditTextPreference) findPreference(
				getResources().getString(R.string.p_manual_id_defalut));
		manualID_defaultTime.setEnabled(true);
		EditTextPreference manualID_adjustTime = (EditTextPreference) findPreference(
				getResources().getString(R.string.p_manual_id_adjust));
		manualID_adjustTime.setEnabled(true);
		PreferenceScreen seqMode = (PreferenceScreen) findPreference(
				getResources().getString(R.string.p_psrc_sequential));
		seqMode.setEnabled(true);

/*		EditTextPreference admin_pw = (EditTextPreference) findPreference(
				getResources().getString(R.string.Admin_Password));
		admin_pw.setEnabled(true);*/

		PreferenceScreen admin_pw = (PreferenceScreen) findPreference("Admin_Password");
		admin_pw.setEnabled(false);

		PreferenceScreen SetBackgroundMeasurementMode = (PreferenceScreen) findPreference(
				getResources().getString(R.string.SetBackgroundMeasurementMode));
		SetBackgroundMeasurementMode.setEnabled(true);

		PreferenceScreen SetCalibrationMode = (PreferenceScreen) findPreference(
				getResources().getString(R.string.SetCalibrationMode));
		SetCalibrationMode.setEnabled(true);

		PreferenceScreen rad_response_option = (PreferenceScreen) findPreference(
				getResources().getString(R.string.rad_response_option_key));
		SetCalibrationMode.setEnabled(true);

	}

	public void DefaultIsotopeList(int index) {
		PreferenceDB mPreDB = new PreferenceDB(this);
		Boolean FirstInstallState = mPreDB.Get_First_Install_State();

		if (FirstInstallState == false) {

			ListPreference edit_AlarmSound = (ListPreference) findPreference("IsoLib_list");
			edit_AlarmSound.setValueIndex(index);
			mPreDB.Set_Fisrt_Install_State(true);

		}

	}

}