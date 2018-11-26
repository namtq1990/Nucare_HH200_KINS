package android.HH100.Structure;

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
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import com.moyosoft.exchange.Exchange;
import com.moyosoft.exchange.ExchangeServiceException;
import com.moyosoft.exchange.mail.ExchangeMail;

import NcLibrary.SpcAnalysis;

import android.HH100.CameraUtil.Camera2Activity;
import android.HH100.DB.DBMng;
import android.HH100.EventListActivity;
import android.HH100.IDspectrumActivity;
import android.HH100.MainActivity;
import android.HH100.MainActivity.Media;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.Identification.Isotope;
import android.HH100.R;
import android.HH100.ReachBackListActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.util.Xml;
import android.widget.TextView;
import android.widget.Toast;

public class NcLibrary {
	public static final double CS137_PEAK1 = 32.0;
	public static final double CS137_PEAK2 = 661.660;
	public static final double K40_PEAK = 1461;
	static String msg = "";
	public static boolean isPhoto = true;

	//Define for K40 finder: Improve GainStabilization
	//Hung:2018.01.31	
	public static int CHSIZE=1024;
	public static int K40Peak = 1461;
	public static int NoMaxK40=20;
	public static double SignalMin=0.5;
	public static double ThesholdPeak=0.9;
	public static int GAIN_THRESHOLD_CNT=200;
	public static int mFail_time=300;

	public static HashMap<String, Object> hashMap = new HashMap<String, Object>();
	static EventData m_EventData = new EventData(); //리치백 180827
	
	// 로그메세지 메일로 전송 180103수정
	public static void SendSystemLog(final Context mContext) {
		final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SystemLog.txt";
		msg = "";
		if (!isNetworkOnline(mContext)) {
			Toast.makeText(mContext, "네트워크 연결을 확인해주세요", Toast.LENGTH_LONG).show();
			return;
		}

		else 
		{
			try 
			{
				final ProgressDialog mPrgDlg;
				mPrgDlg = new ProgressDialog(mContext);
				mPrgDlg.setIndeterminate(true);
				mPrgDlg.setCancelable(false);
				mPrgDlg.setTitle(mContext.getResources().getString(R.string.System_Log_Transfer_content));
				mPrgDlg.setMessage("오류내역을 전송중입니다 \n잠시만 기다려주세요");
				mPrgDlg.show();

				Thread thread = new Thread() 
				{
					@Override
					public void run() {
						super.run();

						String sender = "inseon.ahn@nucaremed.com";
						String sender_pw = "dksdlstjs233";
						String sender_server = "mail.nucaremed.com";
						String sender_port = "587";
						String recv_mail = "inseon.ahn@nucaremed.com";

						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
						String mLastUser = pref.getString(mContext.getResources().getString(R.string.last_user),"None");
						String location = pref.getString(mContext.getResources().getString(R.string.location), "None");
						String time = pref.getString(mContext.getResources().getString(R.string.last_time), "None");

						Mail m = new Mail(sender, sender_pw, sender_server, sender_port);

						// Array of emailIds where you want to
						// sent
						String[] toArr = new String[1];
						toArr[0] = recv_mail;
						m.setTo(toArr);

						// Your emailid(from)
						m.setFrom(sender);
						m.setSubject("SAM III PeakAbout - System Log Data ( PeakAbout III " + Get_AppVersion(mContext) + ")");
						m.setBody("From System Log" + "\n사용자 : " + mLastUser + "\n측정장소 : " + location + "\n최근 사용 일시 : "+ time);
						try 
						{
							final File file = new File(path);
							if (file.exists()) 
							{
								//Toast.makeText(mContext, "저장된 오류내역이 없습니다!", Toast.LENGTH_LONG).show();
								m.addAttachment(path, "SystemLog.txt");
							}
							
							String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SAM/DB_Version.txt";
							File file1 = new File(path1);
							if (file1.exists()) 
							{
								m.addAttachment(path1, "DB_Version.txt");
							}			
							file1 = null;
							path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SAM/DeviceName.txt";
							file1 = new File(path1);
							if (file1.exists()) 
							{
								m.addAttachment(path1, "DeviceName.txt");
							}	
							file1 = null;
							path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SAM/NormalDB.sql";
							file1 = new File(path1);
							if (file1.exists()) 
							{
								m.addAttachment(path1, "NormalDB.sql");
							}
							file1 = null;
							path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SAM/EventDB.sql";
							file1 = new File(path1);
							if (file1.exists()) 
							{
								m.addAttachment(path1, "EventDB.sql");
							}			
																	
							if (m.send()) {
								// Toast.makeText(mContext,, Toast.LENGTH_LONG).show();

								mPrgDlg.dismiss();
								msg = "전송완료되었습니다";
								Log.v("Forgot Password mail", "Success");

								if (file.exists()) 
								{
									file.delete();
								}
							} else {
								mPrgDlg.dismiss();
								msg = "전송이 실패하였습니다";
								Log.v("Forgot Password mail", "Not Success");
							}

							((Activity) mContext).runOnUiThread(new Runnable() {
								public void run() {
									Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
									toast.show();
								}
							});
						} catch (Exception e) {
							NcLibrary.Write_ExceptionLog(e);
							mPrgDlg.dismiss();
							Log.e("MailApp", "Could not send email", e);
						}
					}
				};

				thread.start();

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	///****************************************************FINDING K40 PEAK: NEW ALGORITHM*************************************************************
		//Hung
		// DATE: 2018.01.31

		public static int PeakAna(int[] Spec, double []FWHM, double [] Coeff)
		{
			//Define Peak K40
			int PeakK40 = 0;

			//Step 0: Get Spectrum information
			double []ChSpec = new double[CHSIZE];
			double []ChSpecSmoo = new double[CHSIZE];
			double []WChSpec = new double[CHSIZE];
			double []WChSpecSmoo = new double[CHSIZE];


			for (int  i = 0; i < CHSIZE; i++)
			{
				ChSpec[i] = Spec[i];

				ChSpecSmoo[i] = 0;
				WChSpec[i] = 0;
				WChSpecSmoo[i] = 0;
			}

			// Define ROI for K40
			double En_theshold1 = K40Peak * 0.8;
			double En_thshold2 = K40Peak * 1.2;

			double theshold1 = Math.round(EntoCh_Cali(En_theshold1, Coeff));
			double theshold2 = Math.round(EntoCh_Cali(En_thshold2, Coeff));

			int[] ListK40 = new int[NoMaxK40];

			for (int i = 0; i < NoMaxK40; i++) {
				ListK40[i] = 0;
			}


			// Step 1: Adaptive Filtering for orignal Spectrum	
			ChSpecSmoo = AdaptFilter(ChSpec, ChSpecSmoo, FWHM, 3);

			int indexSpc=FindIndMax(ChSpecSmoo, theshold1, theshold2);

			double MaxSpec = ChSpecSmoo[indexSpc];

			if (MaxSpec > SignalMin)
	    	{
				// Step 2: Weighted Spectrum	
	            WChSpec = WeightSpc(ChSpecSmoo, WChSpec, 1);
	            
				
	          //Step 3: Adapvtive Filteringfor Weighted Spectrum	
	        	WChSpecSmoo = AdaptFilter(WChSpec, WChSpecSmoo, FWHM, 3);
						
	        	
	        	//Step 4: Find Peak
	        	
	        	        	
	        	//Step 4.2: Get Max valude of  Weighted signal inside K40 ROI
	        	int indexWSpc= FindIndMax(WChSpecSmoo, theshold1, theshold2);
	        	
	        	double MaxWSpc = WChSpecSmoo[indexWSpc];

	        	// Step 4.3: Condition for finding K40 peak
	        	
	        	//Step 4.3.1: 1st Condition
	    	
	    		//ListK40 = PeakSearch(WChSpecSmoo, ListK40, FWHM,ThesholdPeak, theshold1, theshold2);
	    		ListK40 = PeakSearch_New(WChSpecSmoo, ListK40, FWHM,ThesholdPeak, theshold1, theshold2);
	    		
	    		int CntPeak = 0;
	    		for (int  i = 0; i < NoMaxK40; i++)
	    		{
	    			if (ListK40[i] > 0)
	    			{
	    				CntPeak = CntPeak + 1;
	    			}
	    		}


	    		//Step 4.3.2: 2nd Condition, finding only 1 peak

	    		if (CntPeak == 1)
	    		{
	    			int PeakK40_tmp = ListK40[0];

	    			if (WChSpecSmoo[PeakK40_tmp] > MaxWSpc*ThesholdPeak)
	    			//if (WChSpecSmoo[PeakK40] == MaxWSpc)
	    			{
	    				PeakK40 = PeakK40_tmp;
	    			}	
	    		}
	    	}
			else
			{
				//Finish(AutoCalbration.RESULT_TIMEOUT_AND_COUNT);
			}
	    	
	    	
	    	
			return PeakK40;

		}


		public static double CalStdDevSpc(double []ChSpec, double ROI_Start, double ROI_End)
		{
			double MAX = 0;
			double AVG = 0;
			double SUM = 0;
			
			int cnt = 0;
			
			for (int i = (int)ROI_Start; i <= (int)ROI_End; i++)
			{	
				SUM = SUM + ChSpec[i];
				
				if (ChSpec[i] > MAX)
				{
					MAX = ChSpec[i];
				}

				cnt = cnt + 1;
			}

			if (cnt == 0) cnt = 1;


			AVG = SUM / (double)(cnt);

			//%%calculate varian
			double m_devia=0, std_dev=0;
			for (int i = (int)ROI_Start; i <=(int) ROI_End; i++)
			{
				m_devia = (AVG - ChSpec[i]) / MAX * 100.0;

				std_dev = std_dev + m_devia * m_devia;
			}


			AVG = std_dev / (double)cnt;

			std_dev = Math.sqrt(AVG);

			return std_dev;

		}
		public static int []PeakSearch(double []ChSpec, int []ListK40, double []FWHM, double thshold, double thshold1_k40, double thshold2_k40, double theshold1, double theshold2)
		{
			int  [] peaklist = new int[NoMaxK40];
			int  [] templist = new int[NoMaxK40];

			for (int i = 0; i < NoMaxK40; i++)
			{
				//reset
				peaklist[i] = 0;
				templist[i] = 0;
				ListK40[i] = 0;
			}
			
			//1st Step: Calculate Standard deviation
			double Std_dev = CalStdDevSpc(ChSpec,  thshold1_k40, thshold2_k40);

			
			if (Std_dev > 17)
			{
				
				// Finding Max value
				int IndexMax = FindIndMax(ChSpec, theshold1, theshold2);
				
				double MAX = ChSpec[IndexMax];		

				double PeakAvg = (theshold1 + theshold2)/2.0;
				
				double FWHM_Peak = FWHM[0]*Math.sqrt(PeakAvg) + FWHM[1]; //%% fwhm = 2.355 sigma

				//%% To >95 % density, then - 2sigma to 2 sigma.
				//Reference link: https://www.mathsisfun.com/definitions/standard-normal-distribution.html

				double PeakMinWidth = 4.0 / 2.355*FWHM_Peak;  //%% To >95 % density, then - 2sigma to 2 sigma.		
				
															  //double PeakMinWidth = 40;

				double halfWidth = (PeakMinWidth / 2.0)*0.8;
				double quarterWidth = halfWidth / 2.0;

				int ch1 =(int) Math.round(halfWidth);
				int ch2 =(int) Math.round(halfWidth - quarterWidth / 2.0);
				int ch3 = (int)Math.round(quarterWidth);
				int ch4 = (int)Math.round(quarterWidth - quarterWidth / 2.0);
				
				int peakCount = 0;

				double thshold_val = thshold*MAX;

				for (int  i = (int)theshold1; i <=(int) theshold2; i++)
				{
					if (ChSpec[i] > thshold_val)
					{
						if (ChSpec[i - ch2] < ChSpec[i - ch3] & ChSpec[i + ch2] < ChSpec[i + ch3])
						{
							if (ChSpec[i - ch3] < ChSpec[i - ch4] & ChSpec[i + ch3] < ChSpec[i + ch4])
							{
								if (ChSpec[i - ch4] < ChSpec[i] & ChSpec[i + ch4] < ChSpec[i])
								{
									if (peakCount < NoMaxK40)
									{
										templist[peakCount] = i;
										peakCount = peakCount + 1;
									}
								}
							}
						}
					}
				}

			

				int realPeakCount=0;

				int ind = 0;

				double maxval = 0;
				int maxch = 0;
				double tmp = 0;
				int index0=0;
				for (int i = 0; i < peakCount-1; i++)
				{
					maxval = 0;
					maxch = 0;

					if (i <= ind) continue;

					index0 = templist[i];			

					if (Math.abs((long)(templist[i]- templist[i + 1])) <= 1)
					{
						index0 = templist[i];
						maxval = ChSpec[index0];

						maxch = i;
						
						int j = 0;
						for (j = i+1; j < peakCount-1; j++)
						{
							if (Math.abs((long)(templist[j] - templist[j + 1])) <= 1)
							{
								index0 = templist[j];

								if (maxval < ChSpec[index0])
								{
									maxval = ChSpec[index0];
									maxch = j;
								}
							}
							else
							{
								break;
							}
						}

						ind = j + 1;
					}

					

					peaklist[realPeakCount] = templist[maxch];
					realPeakCount = realPeakCount + 1;
					
					if (ind == peakCount-1)
					{
						break;
					}
					
				}

				//
				if (realPeakCount > 0)
				{
					for (int i = 0; i < realPeakCount; i++)
					{
						ListK40[i] = peaklist[i];
					}
				
				}

			}

			return ListK40;
		}
		public static int []PeakSearch_New(double []ChSpec, int []ListK40, double []FWHM, double thshold, double theshold1, double theshold2)
		{

			double PeakAvg = (theshold1 + theshold2) / 2.0;

			double FWHM_K40 = FWHM[0]*Math.sqrt(PeakAvg) + FWHM[1];

			double Width_2Sig = FWHM_K40 * 4.0 / 2.355;
			double Width_3Sig = FWHM_K40 * 6.0 / 2.355;

			double threshold1_2sig = Math.round(PeakAvg - Width_2Sig / 2.0);
			double threshold2_2sig = Math.round(PeakAvg + Width_2Sig / 2.0);

			double threshold1_3sig = Math.round(PeakAvg - Width_3Sig / 2.0);
			double threshold2_3sig = Math.round(PeakAvg + Width_3Sig / 2.0);

			ListK40 = PeakSearch(ChSpec, ListK40, FWHM, thshold, theshold1, theshold2, threshold1_2sig, threshold2_2sig);

			if (ListK40[0] == 0)
			{
				ListK40 = PeakSearch(ChSpec, ListK40, FWHM, thshold, theshold1, theshold2, threshold1_3sig, threshold2_3sig);

				if (ListK40[0] == 0)
				{
					ListK40 = PeakSearch(ChSpec, ListK40, FWHM, thshold, theshold1, theshold2, theshold1, theshold2);
				}
			}

			return ListK40;
		}
		
		
		public static double []WeightSpc(double []ChSpec, double []ChSpecSmoo, int WF)
		{

			for (int i = 0; i < CHSIZE; i++)
			{
				ChSpecSmoo[i] = 0;
				ChSpecSmoo[i] = ChSpec[i] * Math.pow((double)(i), (double)(WF));
			}
			
			return ChSpecSmoo;
			
		}
		
		
		public static int FindIndMax(double []ChSpec, double ROI_Start, double ROI_End)
		{
			
			//Result[0]<--- Index of Max value
			// Result[1] <----- Max value

			double Max = 0;
			int Index=0;

			for (int  i = (int) ROI_Start; i <= (int) ROI_End; i++)
			{
				if (ChSpec[i] > Max)
				{
					Max = ChSpec[i];
					Index = i;
				}
			}
			
			return Index;
		}
		

		public static double EntoCh_Cali(double Erg, double [] coeff)
		{
			double a = coeff[0];
			double b = coeff[1];
			double c = coeff[2]-Erg;		

			double Ch=(-b+Math.sqrt(b*b-4*a*c))/(2*a);

			return Ch;
		}


		public static double []AdaptFilter(double []ChSpec, double []ChSpecSmoo,double []FWHM, int NoRepeat)
		{
			double []Data = new double[CHSIZE];

			double aval = FWHM[0];
			double bval = FWHM[1];

			if (aval == 0 && bval == 0)
			{
				//This is FWHM of NaI 3x3 form Theory
				 aval = 1.51088;
				 bval = -6.25751;
			}
			
			int NoSmooth = NoRepeat;

			double Ratio = 1; //=1: meaning: Peak-FWHM:Peak+FWHM

			double temp_wind0 = aval*Math.sqrt((double)(CHSIZE)) + bval;

			double temp_wind = Math.round(temp_wind0*Ratio);

			double sum1 = 0, wnd0;
			int wnd = 0;
			int wnd_half = 0;

			for (int j = 0; j < CHSIZE; j++)
			{
				Data[j] = ChSpec[j];
				ChSpecSmoo[j] = 0;
			}

			for (int nosmoo = 1; nosmoo <= NoSmooth; nosmoo++)
			{
				if (nosmoo > 1)
				{
					for (int j = 0; j < CHSIZE; j++)
					{
						Data[j] = ChSpecSmoo[j];
					}
				}
				for (int j = 3; j < CHSIZE - temp_wind; j++)
				{

					sum1 = 0;
					wnd0= aval*Math.sqrt((double)(j+1)) + bval;

					wnd = (int)(wnd0*Ratio);

					//wnd = (int)(floor(aval * (j + 1) + bval));

					if (wnd < 0)
					{
						ChSpecSmoo[j] = Data[j];
					}
					if (wnd % 2 == 0)
					{
						wnd = wnd + 1;
					}
					wnd_half = (int)(Math.floor(wnd / 2.0));

					for (int k = j - wnd_half; k <= j + wnd_half; k++)
					{
						if (k >= 0 && k < CHSIZE)
							sum1 = sum1 + Data[k];
					}

					ChSpecSmoo[j] = sum1 / (double)wnd;
				}
			}

			
			return ChSpecSmoo;
		}
		
		
	public static String Get_AppVersion(Context context) {
		String version;
		try {
			PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = i.versionName;
			return version;
		} catch (NameNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
			return null;
		}

	}

	public static boolean CheckedHH200Launcher(Context mContext, String AppName) {

		try {

			PackageInfo info = mContext.getPackageManager().getPackageInfo("ah.hathi.simplelauncher", 0);
			String version = "";
			if (info != null) {

				version = info.versionName;
			}

			String mAppVersion = "";
			mAppVersion = AppName.replace("_", ".");

			mAppVersion = mAppVersion.replace("Launcher.", "");

			if (version.equals(mAppVersion)) 
			{
				return true;
			} else {

				NcLibrary.RestoreHH200Launhcer(mContext, AppName);
				return false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			NcLibrary.RestoreHH200Launhcer(mContext, AppName);
			return false;
		}

	}

	public static void RestoreHH200Launhcer(Context mContext, final String AppName) {

		String sdRootPath = Environment.getExternalStorageDirectory() + "/download/" + AppName + ".apk";

		File file;
		file = new File(sdRootPath);
		if (file.exists()) {

			file.delete();

		}

		CreateLauncherApk(R.raw.launcher, mContext, AppName);

		// TimerTask mTask = new TimerTask() {
		// @Override
		// public void run() {
		// try {
		//
		// Process proc = Runtime.getRuntime()
		// .exec(new String[] { "su", "-c", "pm install -r /sdcard/download/" + AppName
		// + ".apk" });
		// proc.waitFor();
		// } catch (Exception e) {
		// Write_ExceptionLog(e);
		// }
		//
		// }
		// };
		//
		// Timer mTimer = new Timer();
		// mTimer.schedule(mTask, 1000);

	}

	public static void CreateLauncherApk(int rawId, Context mContext, String AppName) {
		File file = new File(getLauncher_FilePath(AppName + ".apk"));
		if (file.isFile() == false) {
			byte[] buffer = new byte[8 * 1024];

			int length = 0;
			InputStream is = mContext.getResources().openRawResource(rawId);
			BufferedInputStream bis = new BufferedInputStream(is);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(getLauncher_FilePath(AppName + ".apk"));
			} catch (FileNotFoundException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			try {
				while ((length = bis.read(buffer)) >= 0)
					fos.write(buffer, 0, length);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			try {
				fos.flush();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			try {
				fos.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	public static String getLauncher_FilePath(String FileName) {
		try {
			File sdcard = Environment.getExternalStorageDirectory();

			File dbpath = new File(sdcard.getAbsolutePath() + File.separator + "Download");
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

	public static int pow(int x, int y) { // �젣怨� 怨꾩궛
		int result = 1;
		try {
			for (int i = 0; i < y; i++) {
				result *= x;
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 1;
		}

		return result;
	}

	public static int Get_fontSize(double width) {
		return (int) ((width * 0.01) + 8);
	}

	public static String Prefix(int Value) {
		try {
			if (Value > 1000 && Value <= 1000000)
				return (int) (Value * 0.001) + "k";
			if (Value > 1000000)
				return (int) (Value * 0.00001) + "m";
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return "";
		}

		return String.valueOf(Value);
	}

	public static String Prefix(int Value, boolean Float) {
		try {
			if (Float == true) {
				if (Value > 1000 && Value <= 1000000)
					return Math_round(Value * 0.001, 2) + "k";
				if (Value > 1000000)
					return Math_round(Value * 0.00001, 2) + "m";

				return String.valueOf(Value);
			} else {
				if (Value > 1000 && Value <= 1000000)
					return (int) (Value * 0.001) + "k";
				if (Value > 1000000)
					return (int) (Value * 0.00001) + "m";

				return String.valueOf(Value);
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return "";
		}

	}

	static double Math_round(double d, int n) {

		return Math.round(d * Math.pow(10, n)) / Math.pow(10, n);

	}

	public static String Comma_Format(int Value) {
		String Result = "";

		try {
			String temp = String.valueOf(Value);
			int count = 0;
			for (int i = temp.length() - 1; i >= 0; i--) {
				count += 1;
				if (count % 3 == 0) {

					Result = temp.charAt(i) + Result;
					if (i == 0)
						break;
					Result = "," + Result;
				} else {
					Result = temp.charAt(i) + Result;
				}

			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return "";
		}
		return Result;
	}

	public static double Channel_to_Energy(double Channel, double A, double B, double C) // 梨꾨꼸媛믪쓣
	// A,B媛믪뿉
	// �쓽�븳
	// �뿉�꼫吏�媛믪쑝濡�
	// 諛붽씔�떎.
	{
		double Result = 0;

		try {
			if (C == 0) {
				Result = (A * Channel) + B;
			} else {
				Result = (A * (Channel * Channel)) + (B * Channel) + C;
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}

		return Result;
	}

	public static double Channel_to_Energy(double Channel, double[] ABC) // 梨꾨꼸媛믪쓣
	// A,B媛믪뿉
	// �쓽�븳
	// �뿉�꼫吏�媛믪쑝濡�
	// 諛붽씔�떎.
	{
		if (ABC.length <= 1)
			return 0;

		double Param[] = ABC;
		double Result = 0;

		try {
			if (Param.length == 2) {
				Result = (Param[0] * Channel) + Param[1];
			} else {
				Result = (Param[0] * (Channel * Channel)) + (Param[1] * Channel) + Param[2];
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			Result = 0;
		}

		if (Double.isNaN(Result))
			Result = 0;
		return Result;
	}

	public static double Energy_to_Channel(double Energy, double A, double B, double C) // 梨꾨꼸媛믪쓣
	// A,B媛믪뿉
	// �쓽�븳
	// �뿉�꼫吏�媛믪쑝濡�
	// 諛붽씔�떎.
	{
		double Result = 0;
		// int[] sdfsdf = new int[23];
		try {
			if (C == 0) {
				Result = (Energy - B) / A;
			} else {
				Result = 2 * A;
				Result = (-(B) + Math.sqrt((B * B) - ((4 * A) * (C - Energy)))) / Result;
			}
			// sdfsdf[-1] = 0;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			Result = 0;
		}

		if (Result < 0)
			Result = 0;
		if (Double.isNaN(Result))
			Result = 0;
		return Result;
	}

	public static double Energy_to_Channel(double Energy, double[] EnergyFittingArg) // 梨꾨꼸媛믪쓣
	// A,B媛믪뿉
	// �쓽�븳
	// �뿉�꼫吏�媛믪쑝濡�
	// 諛붽씔�떎.
	{
		if (EnergyFittingArg.length <= 1)
			return 0;

		int argSize = EnergyFittingArg.length;
		double ABC[] = EnergyFittingArg;
		double Result = 0;

		try {
			if (argSize == 2) {
				Result = (Energy - ABC[1]) / ABC[0];

			} else if (argSize == 3) {

				Result = 2 * ABC[0];
				Result = (-(ABC[1]) + Math.sqrt((ABC[1] * ABC[1]) - ((4 * ABC[0]) * (ABC[2] - Energy)))) / Result;

			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			Result = 0;
		}

		if (Result < 0)
			Result = 0;
		return Result;
	}

	public static double[] ft_smooth(int[] ihist, double aval, double bval) {

		int temp_wind = (int) Math.ceil(aval * ihist.length + bval);
		double temp;
		int window = 0;
		int window_half = 0;
		double temp_sum = 0;
		double temphist1[] = new double[ihist.length];
		double temphist2[] = new double[ihist.length];
		double[] result = new double[ihist.length];

		try {

			for (int i = 4; i < 1024 - temp_wind; i++) {

				temp_sum = 0;
				temp = 0;
				window = (int) Math.ceil((aval * i) + bval);

				if (window <= 0)
					continue;

				if (window % 2 == 0)
					window = window + 1;

				window_half = (window / 2);

				for (int j = i - window_half; j <= i + window_half; j++)
					temp_sum = temp_sum + ihist[j];

				temp = temp_sum / window;
				temphist1[i] = temp;
			}

			for (int i = 4; i < ihist.length - temp_wind; i++) {

				temp_sum = 0;
				temp = 0;
				window = (int) Math.ceil(aval * i + bval);

				if (window <= 0)
					continue;

				if (window % 2 == 0)
					window = window + 1;

				window_half = (window / 2);

				for (int j = i - window_half; j <= i + window_half; j++)
					temp_sum = temp_sum + temphist1[j];

				temp = temp_sum / window;
				temphist2[i] = temp;
			}

			for (int i = 0; i < ihist.length; i++)
				result[i] = temphist2[i];

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return result;
	}

	public static double[] ft_smooth(double[] ihist, double aval, double bval) {

		int temp_wind = (int) Math.ceil(aval * ihist.length + bval);
		double temp;
		int window = 0;
		int window_half = 0;
		double temp_sum = 0;
		double temphist1[] = new double[ihist.length];
		double temphist2[] = new double[ihist.length];
		double[] result = new double[ihist.length];

		try {
			for (int i = 0; i < ihist.length; i++) {
				temphist1[i] = 0;
				temphist2[i] = 0;
			}

			for (int i = 4; i < ihist.length - temp_wind; i++) {

				temp_sum = 0;
				temp = 0;
				window = NcLibrary.Auto_floor(Math.ceil(aval * i + bval));

				if (window <= 0)
					continue;

				if (window % 2 == 0)
					window = window + 1;

				window_half = window / 2;

				for (int j = i - window_half; j <= i + window_half; j++)
					temp_sum = temp_sum + ihist[j];

				temp = temp_sum / window;
				temphist1[i] = temp;
			}

			for (int i = 4; i < ihist.length - temp_wind; i++) {

				temp_sum = 0;
				temp = 0;
				window = NcLibrary.Auto_floor(Math.ceil(aval * i + bval));

				if (window <= 0)
					continue;

				if (window % 2 == 0)
					window = window + 1;

				window_half = window / 2;

				for (int j = i - window_half; j <= i + window_half; j++)
					temp_sum = temp_sum + temphist1[j];

				temp = temp_sum / window;
				temphist2[i] = temp;
			}

			for (int i = 0; i < ihist.length; i++)
				result[i] = temphist2[i];

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return result;
	}

	public static double[] Smooth(int[] ChArray, int ChArray_Size, int WindowSize, int repeat) {
		double[] result = new double[ChArray_Size];
		try {

			WindowSize = (WindowSize % 2 == 0) ? WindowSize + 1 : WindowSize; // Window
			// Size媛�
			// ���닔
			// 吏앹닔
			// �씤吏�
			// �뙋蹂꾪썑
			// ���닔濡�
			int WindowHalfSize = (WindowSize - 1) / 2; // Window Size�쓽 �젅諛�
			int Smooth_Start = ((WindowSize - 1) / 2); // 梨꾨꼸 �뜲�씠�꽣�뿉�꽌 �뒪臾대뵫�씠
			// �떆�옉�릺�뒗 吏��젏
			int Soomth_End = ChArray_Size - ((WindowSize - 1) / 2); // 梨꾨꼸
			// �뜲�씠�꽣�뿉�꽌
			// �뒪臾대뵫�씠
			// 醫낅즺�릺�뒗
			// 吏��젏

			double[] Smoothed_ChArray = new double[ChArray_Size];

			for (int i = Smooth_Start; i < Soomth_End; i++) {
				Smoothed_ChArray[i] = 0;

				for (int k = 0; k < WindowSize; k++) {
					Smoothed_ChArray[i] += (ChArray[i - WindowHalfSize + k]);
				}
				Smoothed_ChArray[i] = (Smoothed_ChArray[i] / (double) WindowSize);

			}

			if (repeat > 1) // 諛섎났
			{
				for (int j = 0; j < repeat - 1; j++) {// 諛섎났 �슏�닔

					double[] Smoothed_TempArray = new double[ChArray_Size];

					for (int i = Smooth_Start; i < Soomth_End; i++)// �뒪臾대뵫
					{
						Smoothed_TempArray[i] = 0;
						for (int k = 0; k < WindowSize; k++) {
							Smoothed_TempArray[i] += (Smoothed_ChArray[i - WindowHalfSize + k]);
						}
						Smoothed_TempArray[i] = (Smoothed_TempArray[i] / (double) WindowSize);

					}

					for (int i = 0; i < ChArray_Size; i++) {// �뒪臾대뵫 寃곌낵 由ы꽩
						Smoothed_ChArray[i] = Smoothed_TempArray[i];
					}

				}

			}

			for (int i = 0; i < ChArray_Size; i++) // 理쒖쥌 寃곌낵 由ы꽩
			{
				result[i] = (Smoothed_ChArray[i] <= 0) ? 0 : Smoothed_ChArray[i];
			}

			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return result;
		}

	}

	public static double[] LinearFitting(int Point1_X, int point1_Y, int Point2_X, int Point2_Y) {
		double[] result = new double[2];
		try {
			double resultA = ((float) (Math.max(Point2_X, Point2_Y) - Math.min(Point2_X, Point2_Y))
					/ (Math.max(Point1_X, point1_Y) - Math.min(Point1_X, point1_Y)));
			double resultB = (float) (Point2_X - Point1_X * resultA);

			result = new double[] { resultA, resultB };
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return result;
	}

	public static int QuadraticCal(int ch1, int ch2, int ch3, double en1, double en2, double en3, double result[]) // 2李⑤갑�젙�떇
	// 移쇰━釉뚮젅�씠�뀡
	{
		//////////////////////
		// Quadratic calibration
		// y=mBluetoothImage_flag*x^2 + b*x + c ;
		// input channel(x): ch1, ch2, ch3
		// input energy(y): en1, en2, en3
		// output: result, result[0] = mBluetoothImage_flag, result[1] = b,
		////////////////////// result[2] = c
		//
		////////////////////
		try {
			int resultflag = 1;
			result[0] = 0;
			result[1] = 0;
			result[2] = 0;
			if (ch1 == 0 || ch2 == 0 || ch3 == 0 || en1 == 0 || en2 == 0 || en3 == 0) {
				return 0;
			}
			///////////////////////////////////////////////////
			//// 3 point calibration
			double[] calChArr = new double[3];
			calChArr[0] = ch1;
			calChArr[1] = ch2;
			calChArr[2] = ch3;

			double[] calEnArr = new double[3];
			calEnArr[0] = en1;
			calEnArr[1] = en2;
			calEnArr[2] = en3;

			double[][] chMatrix = new double[3][3];
			chMatrix[0][0] = ch1 * ch1;
			chMatrix[0][1] = ch1;
			chMatrix[0][2] = 1;

			chMatrix[1][0] = ch2 * ch2;
			chMatrix[1][1] = ch2;
			chMatrix[1][2] = 1;

			chMatrix[2][0] = ch3 * ch3;
			chMatrix[2][1] = ch3;
			chMatrix[2][2] = 1;

			double revD = chMatrix[0][0] * chMatrix[1][1] * chMatrix[2][2]
					+ chMatrix[2][0] * chMatrix[0][1] * chMatrix[1][2]
					+ chMatrix[1][0] * chMatrix[2][1] * chMatrix[0][2]
					- chMatrix[0][2] * chMatrix[1][1] * chMatrix[2][0]
					- chMatrix[0][0] * chMatrix[1][2] * chMatrix[2][1]
					- chMatrix[1][0] * chMatrix[0][1] * chMatrix[2][2];

			double[][] revMatrix = new double[3][3];
			revMatrix[0][0] = (chMatrix[1][1] * chMatrix[2][2] - chMatrix[1][2] * chMatrix[2][1]) / revD;
			revMatrix[0][1] = -(chMatrix[0][1] * chMatrix[2][2] - chMatrix[0][2] * chMatrix[2][1]) / revD;
			revMatrix[0][2] = (chMatrix[0][1] * chMatrix[1][2] - chMatrix[0][2] * chMatrix[1][1]) / revD;

			revMatrix[1][0] = -(chMatrix[1][0] * chMatrix[2][2] - chMatrix[1][2] * chMatrix[2][0]) / revD;
			revMatrix[1][1] = (chMatrix[0][0] * chMatrix[2][2] - chMatrix[0][2] * chMatrix[2][0]) / revD;
			revMatrix[1][2] = -(chMatrix[0][0] * chMatrix[1][2] - chMatrix[0][2] * chMatrix[1][0]) / revD;

			revMatrix[2][0] = (chMatrix[1][0] * chMatrix[2][1] - chMatrix[1][1] * chMatrix[2][0]) / revD;
			revMatrix[2][1] = -(chMatrix[0][0] * chMatrix[2][1] - chMatrix[0][1] * chMatrix[2][0]) / revD;
			revMatrix[2][2] = (chMatrix[0][0] * chMatrix[1][1] - chMatrix[0][1] * chMatrix[1][0]) / revD;

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					result[i] = result[i] + revMatrix[i][j] * calEnArr[j];
				}
			}
			return 1;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static int QuadraticCal(double ch1, double ch2, double ch3, double en1, double en2, double en3,
			double result[]) // 2李⑤갑�젙�떇 移쇰━釉뚮젅�씠�뀡
	{
		//////////////////////
		// Quadratic calibration
		// y=mBluetoothImage_flag*x^2 + b*x + c;
		// input channel(x): ch1, ch2, ch3
		// input energy(y): en1, en2, en3
		// output: result, result[0] = mBluetoothImage_flag, result[1] = b,
		////////////////////// result[2] = c
		//
		////////////////////

		try {
			int resultflag = 1;
			result[0] = 0;
			result[1] = 0;
			result[2] = 0;
			if (ch1 == 0 || ch2 == 0 || ch3 == 0 || en1 == 0 || en2 == 0 || en3 == 0) {
				return 0;
			}
			///////////////////////////////////////////////////
			//// 3 point calibration
			double[] calChArr = new double[3];
			calChArr[0] = ch1;
			calChArr[1] = ch2;
			calChArr[2] = ch3;

			double[] calEnArr = new double[3];
			calEnArr[0] = en1;
			calEnArr[1] = en2;
			calEnArr[2] = en3;

			double[][] chMatrix = new double[3][3];
			chMatrix[0][0] = ch1 * ch1;
			chMatrix[0][1] = ch1;
			chMatrix[0][2] = 1;

			chMatrix[1][0] = ch2 * ch2;
			chMatrix[1][1] = ch2;
			chMatrix[1][2] = 1;

			chMatrix[2][0] = ch3 * ch3;
			chMatrix[2][1] = ch3;
			chMatrix[2][2] = 1;

			double revD = chMatrix[0][0] * chMatrix[1][1] * chMatrix[2][2]
					+ chMatrix[2][0] * chMatrix[0][1] * chMatrix[1][2]
					+ chMatrix[1][0] * chMatrix[2][1] * chMatrix[0][2]
					- chMatrix[0][2] * chMatrix[1][1] * chMatrix[2][0]
					- chMatrix[0][0] * chMatrix[1][2] * chMatrix[2][1]
					- chMatrix[1][0] * chMatrix[0][1] * chMatrix[2][2];

			double[][] revMatrix = new double[3][3];
			revMatrix[0][0] = (chMatrix[1][1] * chMatrix[2][2] - chMatrix[1][2] * chMatrix[2][1]) / revD;
			revMatrix[0][1] = -(chMatrix[0][1] * chMatrix[2][2] - chMatrix[0][2] * chMatrix[2][1]) / revD;
			revMatrix[0][2] = (chMatrix[0][1] * chMatrix[1][2] - chMatrix[0][2] * chMatrix[1][1]) / revD;

			revMatrix[1][0] = -(chMatrix[1][0] * chMatrix[2][2] - chMatrix[1][2] * chMatrix[2][0]) / revD;
			revMatrix[1][1] = (chMatrix[0][0] * chMatrix[2][2] - chMatrix[0][2] * chMatrix[2][0]) / revD;
			revMatrix[1][2] = -(chMatrix[0][0] * chMatrix[1][2] - chMatrix[0][2] * chMatrix[1][0]) / revD;

			revMatrix[2][0] = (chMatrix[1][0] * chMatrix[2][1] - chMatrix[1][1] * chMatrix[2][0]) / revD;
			revMatrix[2][1] = -(chMatrix[0][0] * chMatrix[2][1] - chMatrix[0][1] * chMatrix[2][0]) / revD;
			revMatrix[2][2] = (chMatrix[0][0] * chMatrix[1][1] - chMatrix[0][1] * chMatrix[1][0]) / revD;

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					result[i] = result[i] + revMatrix[i][j] * calEnArr[j];
				}
			}
			return 1;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static double DoseRateCalculate(int ChanelArray[], double[] EnergyFittingArg, double Surface,
			Detector.CrystalType crystal) {

		if (crystal == crystal.LaBr) {
			double DR = Math.pow(2.1716 * DoseRate(ChanelArray, EnergyFittingArg, Surface), 0.8614) - 150;
			return (DR < 0) ? 0 : DR;
		} else {
			return DoseRate(ChanelArray, EnergyFittingArg, Surface);
		}
	}

	public static double DoseRate(int ChanelArray[], double[] EnergyFittingArg, double Det_Surface) {

		try {
			double Cali_A = EnergyFittingArg[0];
			double Cali_B = EnergyFittingArg[1];
			double Cali_C = EnergyFittingArg[2];

			double surface = Det_Surface;

			double[] MeV = new double[29];
			double[] Factor = new double[29];

			MeV[0] = 0;
			Factor[1] = 3.96 * Math.pow(10, -6);
			MeV[1] = 0.01;
			Factor[2] = 5.82 * Math.pow(10, -7);
			MeV[2] = 0.03;
			Factor[3] = 2.90 * Math.pow(10, -7);
			MeV[3] = 0.05;
			Factor[4] = 2.58 * Math.pow(10, -7);
			MeV[4] = 0.07;
			Factor[5] = 2.83 * Math.pow(10, -7);
			MeV[5] = 0.1;
			Factor[6] = 3.79 * Math.pow(10, -7);
			MeV[6] = 0.15;
			Factor[7] = 5.01 * Math.pow(10, -7);
			MeV[7] = 0.2;
			Factor[8] = 6.31 * Math.pow(10, -7);
			MeV[8] = 0.25;
			Factor[9] = 7.59 * Math.pow(10, -7);
			MeV[9] = 0.3;
			Factor[10] = 8.78 * Math.pow(10, -7);
			MeV[10] = 0.35;
			Factor[11] = 9.85 * Math.pow(10, -7);
			MeV[11] = 0.4;
			Factor[12] = 1.08 * Math.pow(10, -6);
			MeV[12] = 0.45;
			Factor[13] = 1.17 * Math.pow(10, -6);
			MeV[13] = 0.5;
			Factor[14] = 1.27 * Math.pow(10, -6);
			MeV[14] = 0.55;
			Factor[15] = 1.36 * Math.pow(10, -6);
			MeV[15] = 0.6;
			Factor[16] = 1.44 * Math.pow(10, -6);
			MeV[16] = 0.65;
			Factor[17] = 1.52 * Math.pow(10, -6);
			MeV[17] = 0.7;
			Factor[18] = 1.68 * Math.pow(10, -6);
			MeV[18] = 0.8;
			Factor[19] = 1.98 * Math.pow(10, -6);
			MeV[19] = 1.0;
			Factor[20] = 2.51 * Math.pow(10, -6);
			MeV[20] = 1.4;
			Factor[21] = 2.99 * Math.pow(10, -6);
			MeV[21] = 1.8;
			Factor[22] = 3.42 * Math.pow(10, -6);
			MeV[22] = 2.2;
			Factor[23] = 3.83 * Math.pow(10, -6);
			MeV[23] = 2.6;
			Factor[24] = 4.01 * Math.pow(10, -6);
			MeV[24] = 2.8;
			Factor[25] = 4.41 * Math.pow(10, -6);
			MeV[25] = 3.25;
			Factor[26] = 4.83 * Math.pow(10, -6);
			MeV[26] = 3.75;
			Factor[27] = 5.23 * Math.pow(10, -6);
			MeV[27] = 4.25;
			Factor[28] = 5.60 * Math.pow(10, -6);
			MeV[28] = 4.75;

			double Result = 0;

			for (int K = 0; K < 28; K++) {
				double Count = 0;
				for (int i = 0; i < MainActivity.CHANNEL_ARRAY_SIZE; i++) {
					double En = NcLibrary.Channel_to_Energy(i, Cali_A, Cali_B, Cali_C);
					En = En * 0.001;
					if (MeV[K] < En & En <= MeV[K + 1]) {
						Count += ChanelArray[i];
					}
				}
				double TEn = MeV[K + 1] * 1000;
				double factor = (-7 * Math.pow(10, -16) * (TEn * TEn * TEn)) + (3 * Math.pow(10, -12) * (TEn * TEn))
						+ (2 * Math.pow(10, -9) * TEn) + (-6 * Math.pow(10, -8));
				// if( k == )
				double Sv = (Count / surface) * factor;// NcLibrary.Channel_to_Energy(MeV[K+1]*1000,
				// (-1*Math.pow(10,
				// -12)), 7*Math.pow(10,
				// -9), -1*Math.pow(10,
				// -06));//*Factor[K+1];
				Result += Sv;
			}

			return (Result * Math.pow(10, 9)) / 100;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}

	}

	public static int ROIAnalysis(double[] Smoothed_ChArray, int ROI_Start, int ROI_End) {

		try {
			double MAX = 0;
			double AVG = 0;
			double SUM = 0;
			double Base_Pyuncha = 0;
			Vector<Double> roiSPC = new Vector<Double>();
			Vector<Double> Pyuncha = new Vector<Double>();
			for (int i = ROI_Start; i <= ROI_End; i++) {
				roiSPC.add(Smoothed_ChArray[i]);
				SUM += Smoothed_ChArray[i];
				if (Smoothed_ChArray[i] > MAX) {
					MAX = Smoothed_ChArray[i];
				}
			}
			AVG = SUM / roiSPC.size();

			for (int i = ROI_Start; i <= ROI_End; i++) {
				double bunsan_per = ((AVG - Smoothed_ChArray[i]) / MAX) * 100;
				Pyuncha.add(bunsan_per * bunsan_per);

				Base_Pyuncha += bunsan_per * bunsan_per;
			}
			AVG = Base_Pyuncha / roiSPC.size();
			Base_Pyuncha = Math.sqrt(AVG);

			if (Base_Pyuncha < 17)
				return 0;
			/////////
			double[] sst = new double[1024];
			for (int i = 0; i < 1024; i++) {
				sst[i] = Smoothed_ChArray[i];
			}

			double y2max = 0;
			double xmax = 0;
			double temp_y2max = 0;
			double x2max = 0;

			for (int n = ROI_Start; n < ROI_End; n++) {
				if (sst[n] > y2max) {
					if (sst[n - 10] < sst[n] && sst[n + 10] < sst[n]) {
						if (sst[n - 30] < 0.8 * sst[n] && sst[n + 30] < 0.8 * sst[n]) {
							if (Math.abs(xmax - n) > 100) {
								if (sst[n] > temp_y2max) {
									y2max = sst[n];
									x2max = n;
									temp_y2max = sst[n];
								}
							}
						}
					}
				}
			}

			ROI_End = NcLibrary.Auto_floor(Base_Pyuncha);
			return NcLibrary.Auto_floor(x2max);

		} catch (Exception e) {

			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static double ROIAnalysis(double[] Smoothed_ChArray, int ROI_Start, int ROI_End, int tor)// �몴以��렪李⑤쭔
	// 由ы꽩
	{
		try {
			double MAX = 0;
			double AVG = 0;
			double SUM = 0;
			double Base_Pyuncha = 0;
			Vector<Double> roiSPC = new Vector<Double>();
			Vector<Double> Pyuncha = new Vector<Double>();
			for (int i = ROI_Start; i <= ROI_End; i++) {
				roiSPC.add(Smoothed_ChArray[i]);
				SUM += Smoothed_ChArray[i];
				if (Smoothed_ChArray[i] > MAX) {
					MAX = Smoothed_ChArray[i];
				}
			}
			AVG = SUM / roiSPC.size();

			for (int i = ROI_Start; i <= ROI_End; i++) {
				double bunsan_per = ((AVG - Smoothed_ChArray[i]) / MAX) * 100;
				Pyuncha.add(bunsan_per * bunsan_per);

				Base_Pyuncha += bunsan_per * bunsan_per;
			}
			AVG = Base_Pyuncha / roiSPC.size();
			Base_Pyuncha = Math.sqrt(AVG);

			return Base_Pyuncha;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static int ROIAnalysis(double[] Smoothed_ChArray, int ROI_Start, int ROI_End, boolean isK40) {
		try {
			double AxisY_max = 0;
			Vector<Integer> Peak_Ch = new Vector<Integer>();
			int[] result = new int[2];
			double Total_count = 0;

			for (int n = ROI_Start; n < ROI_End; n++) // �뵾�겕瑜� 李얜뒗�떎.
			{
				Total_count += Smoothed_ChArray[n];
				if (Smoothed_ChArray[n - 5] < Smoothed_ChArray[n] && Smoothed_ChArray[n + 5] < Smoothed_ChArray[n]) {
					Peak_Ch.add(n);
				}
			}

			for (int i = 0; i < Peak_Ch.size(); i++) // �뵾�겕�뱾以� �젣�씪 �겙 媛믪쓣
			// 李얜뒗�떎.
			{
				if (Smoothed_ChArray[Peak_Ch.get(i)] > AxisY_max) {
					AxisY_max = Smoothed_ChArray[Peak_Ch.get(i)];
					result[0] = Peak_Ch.get(i);
				}

			}
			for (int i = 0; i < Peak_Ch.size(); i++) {
				if (result[0] == Peak_Ch.get(i)) {
					Peak_Ch.remove(i);
					break;
				}
			}
			for (int i = 0; i < Peak_Ch.size(); i++) // �뵾�겕�뱾以� �젣�씪 �겙 媛믪쓣
			// 李얜뒗�떎.
			{
				if (Smoothed_ChArray[Peak_Ch.get(i)] > AxisY_max) {
					AxisY_max = Smoothed_ChArray[Peak_Ch.get(i)];
					result[1] = Peak_Ch.get(i);
				}

			}

			return (result[0] < result[1]) ? result[1] : result[0];
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static double ROIAnalysis_GetTotCnt(double[] Smoothed_ChArray, int ROI_Start, int ROI_End) {
		try {
			double result = 0;
			for (int i = ROI_Start; i < ROI_End; i++) {
				result += Smoothed_ChArray[i];
			}
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static int ROIAnalysis_GetTotCnt(int[] Smoothed_ChArray, int ROI_Start, int ROI_End) {
		try {
			int result = 0;
			for (int i = ROI_Start; i < ROI_End; i++) {
				result += Smoothed_ChArray[i];
			}
			return result;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static int Auto_floor(double value) {
		return (int) Math.floor(value + 0.5);
	}

	public static double Get_Roi_window_by_energy(double Energy) {

		try {
			if (Energy > 2200) {
				// return (72*Math.pow(Energy, -0.338));//*1.7;
				return 6.5;
			} else
				return (72 * Math.pow(Energy, -0.43));// *1.7;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
		// return 45*Math.pow(Energy, -0.309);
	}

	public static double Get_Roi_window_by_energy_VAllY(double Energy) {
		return 750 * Math.pow(Energy, -0.79) + 7;
	}

	public static int[] Import_spectrum_data(String Path, int Channel_size) {
		int[] result = new int[Channel_size];

		/////////// �뒪�럺�듃�읆 �뀓�뒪�듃�뙆�씪 �깮�꽦

		String temp = "0";
		int k = 0;

		try {
			FileInputStream fos = new FileInputStream(Path);
			DataInputStream dow = new DataInputStream(fos);
			for (int i = 0; i < 5000; i++) {
				int sdq = fos.read();
				if (sdq != 10 & sdq != 32) {
					sdq = sdq - 48;
					temp = temp + sdq;
				} else {
					result[k] = Integer.valueOf(temp);
					temp = "0";
					k += 1;
					if (k == 1024)
						break;
				}

				// Result[i] = Integer.valueOf(fos.read());
			}
			fos.close();
			dow.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			NcLibrary.Write_ExceptionLog(e);

		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);

		}
		return result;
	}

	public static boolean Write_ExceptionLog(String data) {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		String Today = (new SimpleDateFormat("(yyyy-MM-dd_HH:mm:ss)").format(date));

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());
		String dbfile = dbpath.getAbsolutePath() + File.separator + "SystemLog.txt";

		Today = "\n"+data + "  " + Today;
		try {
			FileOutputStream fos = new FileOutputStream(dbfile, true);
			fos.write(Today.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		return true;
	}


	public static boolean Write_ExceptionLog2(String data) {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		String Today = (new SimpleDateFormat("(yyyy-MM-dd_HH:mm:ss)").format(date));

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());
		String dbfile = dbpath.getAbsolutePath() + File.separator + "SystemLog.txt";

		Today = data + "," ;
		try {
			FileOutputStream fos = new FileOutputStream(dbfile, true);
			fos.write(Today.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		return true;
	}


	public static void Write_ExceptionLog(Exception ex) {

		// if (Debug.IsDebugMode) {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		String Today = (new SimpleDateFormat("(yyyy-MM-dd_HH:mm:ss)").format(date));

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());
		String dbfile = dbpath.getAbsolutePath() + File.separator + "SystemLog.txt";

		String mClassName = "\n\n ClassName : " + ex.getStackTrace()[0].getClassName();
		String mMethodName = "\n MethodName : " + ex.getStackTrace()[0].getMethodName();
		String mLineNumber = "\n LineNumber : " + Integer.toString(ex.getStackTrace()[0].getLineNumber());
		String ErrorMsg = "\n ErrorMsg : " + ex.getMessage();

		String data = mClassName + mMethodName + mLineNumber + ErrorMsg;
		Today = data + "  \n" + Today;
		try 
		{
			FileOutputStream fos = new FileOutputStream(dbfile, true);
			fos.write(Today.getBytes());
			fos.close();
		}
		catch (FileNotFoundException e) 
		{
			NcLibrary.Write_ExceptionLog(e);
		}
		catch (IOException e) 
		{
			NcLibrary.Write_ExceptionLog(e);
		}
		// }

	}

	public static boolean Export_ToTextFile(String FileName, String data) {

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());

		String dbfile = dbpath.getAbsolutePath() + File.separator + FileName + ".txt";

		try {
			FileOutputStream fos = new FileOutputStream(dbfile, true);

			fos.write(data.getBytes());

			fos.close();
		} catch (FileNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return true;
	}

	public static boolean Export_spectrum_data(String FileName, int[] SPC, int Channel_size) {

		/////////// �뒪�럺�듃�읆 �뀓�뒪�듃�뙆�씪 �깮�꽦
		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());

		String dbfile = dbpath.getAbsolutePath() + File.separator + FileName + ".txt";

		try {
			FileOutputStream fos = new FileOutputStream(dbfile, false);

			for (int i = 0; i < Channel_size; i++) {

				fos.write(String.valueOf(SPC[i] + " ").getBytes());
			}
			fos.close();
		} catch (FileNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return true;
	}

	public static boolean Export_spectrum_data(double[] SPC, int Channel_size) {

		/////////// �뒪�럺�듃�읆 �뀓�뒪�듃�뙆�씪 �깮�꽦
		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());

		String dbfile = dbpath.getAbsolutePath() + File.separator + "SPC.txt";

		try {
			FileOutputStream fos = new FileOutputStream(dbfile, true);

			for (int i = 0; i < Channel_size; i++) {

				fos.write(String.valueOf("\n " + SPC[i] + " ").getBytes());
			}
			fos.close();
		} catch (FileNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return true;
	}

	public static boolean Export_spectrum_data(String FileName, double[] SPC, int Channel_size) {

		/////////// �뒪�럺�듃�읆 �뀓�뒪�듃�뙆�씪 �깮�꽦
		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());

		String dbfile = dbpath.getAbsolutePath() + File.separator + FileName + ".txt";

		try {
			FileOutputStream fos = new FileOutputStream(dbfile, true);

			for (int i = 0; i < Channel_size; i++) {

				fos.write(String.valueOf("\n " + SPC[i] + " ").getBytes());
			}
			fos.close();
		} catch (FileNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return true;
	}

	public static boolean Wrtie_SW_RealTime_Log(String log) {

		/////////// �뒪�럺�듃�읆 �뀓�뒪�듃�뙆�씪 �깮�꽦
		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());

		String dbfile = dbpath.getAbsolutePath() + File.separator + "SAM_Log.txt";

		try {
			FileOutputStream fos = new FileOutputStream(dbfile);

			fos.write(log.getBytes());

			fos.close();
		} catch (FileNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return true;
	}

	public static String SvToString(double nSv, boolean point, boolean IsSvUnit) { // �닽�옄�삎
		// �떆蹂댄듃
		// 媛믪쓣
		// string�쑝濡�
		try {
			if (IsSvUnit == false)
				nSv = nSv * 100;

			DecimalFormat format = new DecimalFormat();
			String unit = " Sv/h";
			double value = 1;
			int checker = 0;
			if (point == true) {
				if (nSv < Math.pow(10, 3)) {
					value = nSv * 0.001;
					unit = " uSv/h";
					checker = 1;
				} else if (nSv >= Math.pow(10, 3) & nSv < Math.pow(10, 6)) {
					value = (nSv * 0.001);
					unit = " uSv/h";
				} else if (nSv >= Math.pow(10, 6) & nSv < Math.pow(10, 9)) {
					value = (nSv * 0.000001);
					unit = " mSv/h";
				} else if (nSv > Math.pow(10, 9)) {
					value = (nSv * 0.000000001);
					unit = " Sv/h";
				}
			} else {
				if (nSv < Math.pow(10, 3)) {
					value = nSv;
					unit = " nSv/h";
				} else if (nSv >= Math.pow(10, 3) & nSv < Math.pow(10, 6)) {
					value = (nSv * 0.001);
					unit = " uSv/h";
				} else if (nSv >= Math.pow(10, 6) & nSv < Math.pow(10, 9)) {
					value = nSv * 0.000001;
					unit = " mSv/h";
				} else if (nSv > Math.pow(10, 9)) {
					value = nSv * 0.000000001;
					unit = " Sv/h";
				}
			}

			if (checker == 1)
				format.applyLocalizedPattern("0.###");
			else
				format.applyLocalizedPattern("0.##");

			if (IsSvUnit == false) {
				unit = unit.replace("Sv/h", "rem/h");
			}
			return format.format(value) + unit;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return "error";
		}
	}

	public static Vector<Isotope> Quantitative_analysis(Spectrum spc, Spectrum bg, Vector<Isotope> ID_Result,
			boolean IsSvUnit, double PmtSurface, Detector.CrystalType Crystal) {
		Vector<Isotope> result = ID_Result;

		try {
			if (result.isEmpty() == false) {
				for (int i = 0; i < result.size(); i++) {
					result.get(i).Set_Class(ID_Result.get(i).Class);

					double Doserate = NcLibrary.Get_Isotope_Doserate(spc, bg, result.get(i), PmtSurface, Crystal);

					Doserate = Doserate / spc.Get_AcqTime();
					result.get(i).DoseRate = Doserate;
					result.get(i).DoseRate_S = NcLibrary.SvToString(Doserate, true, IsSvUnit);
				}
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		return result;
	}

	public static int[] FindPeak(int[] ChCount) {
		try {
			long xmax = 0, x2max = 0;
			long ymax = 0, y2max = 0;

			double[] x = new double[1024];
			double[] y = new double[1024];
			double[] y2 = new double[1024];
			double temp_xmax = 0;
			double temp_x2max = 0;

			double temp_ymax = 0;
			double temp_y2max = 0;

			double[] smoo = new double[11];
			for (int i = 0; i < 11; i++) // x[i]�뿉 諛깆뾽�빐�몢怨�
			{
				smoo[i] = 1;
			}
			double[] smoo2 = new double[21];
			for (int i = 0; i < 21; i++) // x[i]�뿉 諛깆뾽�빐�몢怨�
			{
				smoo2[i] = 1;
			}

			double[] smoo3 = new double[5];
			for (int i = 0; i < 5; i++) // x[i]�뿉 諛깆뾽�빐�몢怨�
			{
				smoo3[i] = 1;
			}

			int P = 11;
			int P2 = 21;
			int P3 = 5;
			int P3_h = 2;

			// 11point averaging �쑝濡� Smoothing�븯�뒗 猷⑦떞

			for (int i = 0; i < 1024; i++) // x[i]�뿉 諛깆뾽�빐�몢怨�
			{
				x[i] = ChCount[i];
				y[i] = ChCount[i];
			}

			/*
			 * for (int n=0;n<1021;n++) { if(n<=3) { y[n]=ChCount[n]; }else{ y[n]=0; for(int
			 * k=0;k<P3;k++) { y[n] += (smoo[k]*ChCount[n-P3_h+k]); // y2[n]�뿉 smoo2濡�
			 * �뒪臾대뵫�맂媛� ���옣 } y[n] = (int)(y[n]/P3); } }
			 */

			for (int n = 3; n < 24; n++) {
				if (y[n] > ymax && y[n] > 50) // 5ch 50counts �씠�긽�씠硫댁꽌 �겙媛�
				{
					if (y[n - 3] < y[n] && y[n + 5] < y[n]) {
						if (n > 50) {
							if (y[n - 3] < 0.85 * y[n] && y[n + 3] < 0.85 * y[n]) // 洹쇱쿂�쓽
							// 媛믩뱾�씠
							// �뵾�겕濡�
							// 遺��꽣
							// 湲됯꺽�엳
							// 媛먯냼�븯�뒗吏�
							// �뙋�떒
							{

								{
									// y2max=ymax;x2max=xmax;
									ymax = (int) y[n];
									xmax = n;
									// break;
								}
							}
						} else {
							if (y[n + 5] < 0.9 * y[n]) // 洹쇱쿂�쓽 媛믩뱾�씠 �뵾�겕濡�
							// 遺��꽣 湲됯꺽�엳 媛먯냼�븯�뒗吏�
							// �뙋�떒
							{
								if (y[n] > temp_ymax) {
									// y2max=ymax;x2max=xmax;
									ymax = (int) y[n];
									xmax = n;
									temp_ymax = y[n];
									// break;
								}
							}
						}
					}
				}
			}

			for (int n = 150; n < 300; n++) {
				if (n > 50 && y[n] > y2max && y[n] > 50) {
					if (y[n - 10] < y[n] && y[n + 10] < y[n]) {
						if (n < 50 || y[n - 30] < 0.8 * y[n] && y[n + 30] < 0.8 * y[n]) // 洹쇱쿂�쓽
						// 媛믩뱾�씠
						// �뵾�겕濡�
						// 遺��꽣
						// 湲됯꺽�엳
						// 媛먯냼�븯�뒗吏�
						// �뙋�떒
						{
							if (Math.abs(xmax - n) > 100) // 50ch 洹쇱쿂�쓽 �뵾�겕�뱾��
							// �씤�젙�븯吏� �븡�쓬
							{
								if (y[n] > temp_y2max) {
									y2max = (long) y[n];
									x2max = n;
									temp_y2max = y[n];
								}
							}
						}
					}
				}
			}

			int xtemp;
			long ytemp;

			ymax = ChCount[(int) xmax];
			y2max = ChCount[(int) x2max];

			int[] peck = new int[2];
			peck[0] = (int) xmax;
			peck[1] = (int) x2max;

			return peck;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return new int[2];
		}

	}

	public static double Sv_To_Rem(double Sv) {
		try {
			return Sv * 100;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static double Rem_To_Sv(double Rem) {
		try {
			return Rem * 0.01;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static double[] Nomalization(int[] SPC, int spcSize, int AcqTime, int[] BG, int BgAcqTime) {
		double Result[] = new double[spcSize];

		try {
			for (int i = 0; i < spcSize; i++) {
				//	Result[i] = SPC[i] - ((BG[i] / BgAcqTime) * AcqTime);
					if(BgAcqTime<=0)
						Result[i] = (double)SPC[i];
					else
						Result[i] = (double)SPC[i] - (((double)BG[i] / (double)BgAcqTime) * (double)AcqTime);
				}
				return Result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return Result;
		}
	}

	public static double GM_to_uSV(int GM_count) {
		double result = 0;

		try {
			if (GM_count != 0)
				result = GM_count / 0.171;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		return result;
	}

	public static int[] Init_ChannelArray(int[] Array, int ArraySize) {
		int[] result = new int[ArraySize];

		try {
			for (int i = 0; i < ArraySize; i++) {
				Array[i] = 0;
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		return result;
	}

	public static String Get_GMT() {

		try {
			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();

			int gmt = calendar.get(Calendar.ZONE_OFFSET);
			gmt = (int) ((gmt * 0.001) / 3600);

			String tte;
			if (gmt > 0)
				tte = "+";
			else
				tte = "";
			return String.format("%s%d:00", tte, gmt);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return "";
		}

	}

	public static Vector<Integer> Separate_EveryDash(String Data) {
		Vector<Integer> result = new Vector<Integer>();

		try {
			String OneData = "";
			for (int i = 0; i < Data.length(); i++) {
				if (Data.charAt(i) == ';') {

					result.add(Integer.valueOf(OneData));
					OneData = "";
				} else {
					OneData = OneData + Data.charAt(i);
				}

			}
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return result;
		}
	}

	public static double[] Separate_EveryDash(String Data, boolean isIntegerArray) {
		double[] result = new double[1024];

		try {
			String OneData = "";
			int cnt = 0;
			for (int i = 0; i < Data.length(); i++) {
				if (Data.charAt(i) == ';') {

					result[cnt] = Double.valueOf(OneData);
					cnt += 1;
					OneData = "";
				} else {
					OneData = OneData + Data.charAt(i);
				}

			}
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return new double[1024];
		}

	}

	//180731 inseon add return arrayList
	public static ArrayList<String> Separate_EveryDash3(String Data)
	{
		if (Data == null)
			return null;

		try {
			ArrayList<String> result = new ArrayList<String>();

			String OneData = "";
			for (int i = 0; i < Data.length(); i++) {
				if (Data.charAt(i) == ';') {

					result.add(OneData);
					OneData = "";
				} else {
					OneData = OneData + Data.charAt(i);
				}

			}
			if (OneData != "")
				result.add(OneData);
			return result;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return new ArrayList<String>();
		}
	}


	public static int[] Separate_EveryIndex(String Data, char index) {
		int[] result = new int[1024];

		try {
			String OneData = "";
			int cnt = 0;
			for (int i = 0; i < Data.length(); i++) {
				if (Data.charAt(i) == index) {

					result[cnt] = Integer.valueOf(OneData);
					cnt += 1;
					OneData = "";
				} else {
					OneData = OneData + Data.charAt(i);
				}

			}
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return new int[1024];
		}

	}

	public static double[] Separate_EveryIndexD(String Data, char index) {
		double[] result = new double[1024];

		try {
			String OneData = "";
			int cnt = 0;
			for (int i = 0; i < Data.length(); i++) {
				if (Data.charAt(i) == index) {

					result[cnt] = Double.valueOf(OneData);
					cnt += 1;
					OneData = "";
				} else {
					OneData = OneData + Data.charAt(i);
				}

			}
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return new double[1024];
		}

	}

	public static Vector<String> Separate_EveryDash2(String Data) {
		if (Data == null)
			return null;

		try {
			Vector<String> result = new Vector<String>();

			String OneData = "";
			for (int i = 0; i < Data.length(); i++) {
				if (Data.charAt(i) == ';') {

					result.add(OneData);
					OneData = "";
				} else {
					OneData = OneData + Data.charAt(i);
				}

			}
			return result;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return new Vector<String>();
		}
	}

	public static Vector<String> Separate_EveryDash2(String Data, char Index) {

		try {
			Vector<String> result = new Vector<String>();

			String OneData = "";
			for (int i = 0; i < Data.length(); i++) {
				if (Data.charAt(i) == Index) {

					result.add(OneData);
					OneData = "";
				} else {
					OneData = OneData + Data.charAt(i);
				}

			}
			if (OneData != "")
				result.add(OneData);
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return new Vector<String>();
		}
	}

	public static double Get_Isotope_Doserate(Spectrum SPC_Data, Spectrum BG_Data, Isotope Target, double Surface,
			Detector.CrystalType crystal) {

		double result = 0;
		int[] SPC = SPC_Data.ToInteger();
		int[] temp = new int[SPC_Data.Get_Ch_Size()];
		double[] tempSPC = new double[SPC_Data.Get_Ch_Size()];

		tempSPC = NcLibrary.Nomalization(SPC, 1024, SPC_Data.Get_AcqTime(), BG_Data.ToInteger(), BG_Data.Get_AcqTime());

		try {
			for (int EnergyCnt = 0; EnergyCnt < Target.FoundPeaks.size(); EnergyCnt++) {
				//int Vally_Start = Target.FoundPeaks.get(EnergyCnt).Vally_CH.x;
				//int Vally_End = Target.FoundPeaks.get(EnergyCnt).Vally_CH.y;
				
				int Vally_Start = Target.FoundPeaks.get(EnergyCnt).ROI_Left;
				int Vally_End = Target.FoundPeaks.get(EnergyCnt).ROI_Right;

				for (int i = Vally_Start; i <= Vally_End; i++) {
					if (i < 0)
						continue;
					if (Vally_Start == 0 & Vally_End == 0)
						break;
					// temp[i] = NcLibrary.Auto_floor(
					// (SPC[i]-NcLibrary.Channel_to_Energy(i,
					// Target.Channel2_AB.x, Target.Channel2_AB.y, 0)));
					try {
						double resulta = (float) ((SPC[Vally_Start] - SPC[Vally_End]) / (Vally_Start - Vally_End));
						double resultb = (float) (SPC[Vally_Start] - (resulta * Vally_Start));
						temp[i] = NcLibrary
								.Auto_floor((tempSPC[i] - NcLibrary.Channel_to_Energy(i, resulta, resultb, 0)));
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);// _
						// MS:"+SPC_Data.ToString()+"
						// _
						// BG:"+BG_Data.ToString());
						return 0;
					}

				}
			}

			for (int i = 0; i < temp.length; i++) {
				if (temp[i] < 0)
					temp[i] = 0;
			}

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);// _
			// MS:"+SPC_Data.ToString()+"
			// _
			// BG:"+BG_Data.ToString());
			return 0;
		}

		result = NcLibrary.DoseRateCalculate(temp, SPC_Data.Get_Coefficients().get_Coefficients(), Surface, crystal);
		return (result <= 0) ? 1 : result;

	}

	public static double SvToR(double Sv) {
		try {
			return Sv * 100;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}
	}

	public static double FWHM(double[] Smoothed_ChArray, int ChArray_size, int Peak_Channel, double Cali_A,
			double Cali_B, boolean Energy) {

		try {
			int LeftMax = Peak_Channel;
			int RightMax = Peak_Channel;

			int OriMax = Peak_Channel;
			int HV = (int) (Smoothed_ChArray[Peak_Channel] / 2);

			int count = 0;

			int tempy0 = 0;

			int tempy1 = 0;

			double tempdev1 = 0.0;
			double tempdev2 = 0.0;

			while (Smoothed_ChArray[LeftMax] >= HV) {
				tempy0 = (int) Smoothed_ChArray[LeftMax];
				tempy1 = (int) Smoothed_ChArray[LeftMax + 1];
				if (LeftMax == 0)
					return 0;
				LeftMax--;
				count++;
			}
			if ((tempy0 - tempy1) != 0)
				tempdev1 = LeftMax + Math.abs(HV - tempy0) / Math.abs(tempy0 - tempy1);
			else
				tempdev1 = LeftMax;

			while (Smoothed_ChArray[RightMax] >= HV) {
				tempy0 = (int) Smoothed_ChArray[RightMax];
				tempy1 = (int) Smoothed_ChArray[RightMax - 1];

				if (RightMax == ChArray_size)
					return 0;
				RightMax++;
				count++;
			}
			if ((tempy0 - tempy1) != 0)
				tempdev2 = (RightMax - 1) + Math.abs(HV - tempy1) / Math.abs(tempy0 - tempy1);
			else
				tempdev2 = RightMax;

			if (Energy == true)
				return (NcLibrary.Channel_to_Energy(tempdev2, Cali_A, Cali_B, 0)
						- Channel_to_Energy(tempdev1, Cali_A, Cali_B, 0) / Channel_to_Energy(OriMax, Cali_A, Cali_B, 0))
						* 100.0;
			else
				return ((tempdev2 - tempdev1) / OriMax) * 100;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return 0;
		}

	}

	 public static int Get_RoiCount(int[] channel, int start, int end) {
		  int result = 0;

		  for (int i = start; i < end; i++) {
		   
		   if(i>=0&&i<1024)
		   {
		    result += channel[i];
		   }
		  }
		  return result;
		 }
	 
	public static class Event_XML 
	{
		public static String WriteXML_toANSI42( EventData event, final  String ToName, final  Boolean IsIDspectrumMode, String UserName) 
		{
			String path = "";

			if (event.Event_Date == null)
				event.Event_Date = "";
			if (event.StartTime == null)
				event.StartTime = "";
			if (event.EndTime == null)
				event.EndTime = "";

			File newxmlfile;
			if (ToName != null) 
			{
				newxmlfile = new File(Media.reachbackFolderPath + "/" + ToName);
			} else 
			{
				newxmlfile = new File(Media.reachbackFolderPath + "/Event.xml");
			}
			// create a new file called "new.xml" in the SD card

			try 
			{
				newxmlfile.createNewFile();
			} 
			catch (IOException e) 
			{
				NcLibrary.Write_ExceptionLog(e);
			}
			// we have to bind the new file with a FileOutputStream
			FileOutputStream fileos = null;
			try 
			{
				fileos = new FileOutputStream(newxmlfile);
			} 
			catch (FileNotFoundException e) 
			{
				NcLibrary.Write_ExceptionLog(e);
			}
			// we create a XmlSerializer in order to write xml data
			XmlSerializer serializer = Xml.newSerializer();
			try 
			{
				// we set the FileOutputStream as output for the serializer,
				// using UTF-8 encoding
				serializer.setOutput(fileos, "UTF-8");
				// Write <?xml declaration with encoding (if encoding not null)
				// and standalone flag (if standalone not null)
				serializer.startDocument(null, Boolean.valueOf(true));
				// set indentation option
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				// start a tag called "root"
				serializer.startTag(null, "N42InstrumentData");
				serializer.attribute(null, "xmlns","http://physics.nist.gov/Divisions/Div846/Gp4/ANSIN4242/2005/ANSIN4242");
				serializer.attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				serializer.attribute(null, "xsi:schemaLocation","http://physics.nist.gov/Divisions/Div846/Gp4/ANSIN4242/2005/ANSIN4242 http://physics.nist.gov/Divisions/Div846/Gp4/ANSIN4242/2005/ANSIN4242.xsd");
				/// ----------------------------------------------------
				serializer.startTag(null, "Measurement");
				serializer.attribute(null, "UUID", "54531d28-402b-11d8-af12-0002a5094c23");
				serializer.startTag(null, "InstrumentInformation");
				serializer.startTag(null, "InstrumentType");
				serializer.text("Spectrometer");
				serializer.endTag(null, "InstrumentType");

				serializer.startTag(null, "Manufacture");
				serializer.text("Nucare");
				serializer.endTag(null, "Manufacture");

				serializer.startTag(null, "InstrumentModel");
				serializer.text("HH200");
				serializer.endTag(null, "InstrumentModel");

				serializer.startTag(null, "InstrumentID");

				serializer.text((event.mInstrument_Name == null) ? "" : event.mInstrument_Name);
				serializer.endTag(null, "InstrumentID");
				serializer.endTag(null, "InstrumentInformation");
				//
				
			    //180426 추가
			    serializer.startTag(null, "UserName");
			    serializer.text(UserName!=null ? UserName : "");
			    serializer.endTag(null, "UserName");
			    
			    
				serializer.startTag(null, "MeasuredItemInformation");
				serializer.startTag(null, "ItemDescription");
				serializer.text("Cal standard 1");
				serializer.endTag(null, "ItemDescription");

				serializer.startTag(null, "ItemQuantity");
				serializer.attribute(null, "Units", "Kg");
				serializer.text("1.0.0");
				serializer.endTag(null, "ItemQuantity");

				serializer.startTag(null, "MeasurementLocation");
				serializer.startTag(null, "MeasurementLocationName");
				serializer.text((event.mLocation == null ? "" : event.mLocation));
				serializer.endTag(null, "MeasurementLocationName");

				serializer.startTag(null, "Coordinates");
				serializer.text( (event.GPS_Latitude == 0  ? "" : ( Double.toString(event.GPS_Latitude) + " " + Double.toString(event.GPS_Longitude))));
				serializer.endTag(null, "Coordinates");
				serializer.endTag(null, "MeasurementLocation");

				serializer.startTag(null, "ItemReferenceDate");
				serializer.text((event.Event_Date !=null && event.StartTime !=null ? event.Event_Date + "T" + event.StartTime : ""));				
				serializer.endTag(null, "ItemReferenceDate");
				serializer.endTag(null, "MeasuredItemInformation");

				// Mesurement spectrum
				serializer.startTag(null, "Spectrum");
				serializer.attribute(null, "CalibrationIDs", "en");
				serializer.startTag(null, "StartTime");
				serializer.text((event.Event_Date !=null && event.StartTime !=null ? event.Event_Date + "T" + event.StartTime : ""));	
				serializer.endTag(null, "StartTime");

				serializer.startTag(null, "RealTime");
				serializer.text("PT" + (event.MS.Get_AcqTime() != 0 ? event.MS.Get_AcqTime() + ".0S" : "0.0S"));
				serializer.endTag(null, "RealTime");

				serializer.startTag(null, "LiveTime");
				serializer.text("PT" + (event.MS.Get_AcqTime() != 0 ? event.MS.Get_AcqTime() + ".0S" : "0.0S"));
				serializer.endTag(null, "LiveTime");

				serializer.startTag(null, "SourceType");
				serializer.text("Item");
				serializer.endTag(null, "SourceType");

				serializer.startTag(null, "ChannelData");
				serializer.text(event.MS != null ? event.MS.ToString_() : "");
				serializer.endTag(null, "ChannelData");
				String Doserate_AVGs = "";
				String MaxDoserate = "";
				if(event.Doserate_AVGs!=null && event.Doserate_MAXs !=null )
				{
				/*
					if (IsIDspectrumMode) 
					{
						Doserate_AVGs = NcLibrary.SvToString(event.Doserate_AVG, true,(event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false);
						MaxDoserate = NcLibrary.SvToString(event.Doserate_MAX, true, (event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false);
					} else {
						Doserate_AVGs = event.Doserate_AVGs
						MaxDoserate = event.Doserate_MAXs;
					}*/
						Doserate_AVGs = event.Doserate_AVGs;
						MaxDoserate = event.Doserate_MAXs;
				}
			
				serializer.startTag(null, "AvgDoserate"); // KINS
				serializer.text(Doserate_AVGs);
				serializer.endTag(null, "AvgDoserate");

				serializer.startTag(null, "MaxDoserate"); // KINS
				serializer.text(MaxDoserate);
				serializer.endTag(null, "MaxDoserate");
				serializer.endTag(null, "Spectrum");
				//

				// Mesurement spectrum
				serializer.startTag(null, "Spectrum");
				serializer.attribute(null, "CalibrationIDs", "en");
				serializer.attribute(null, "Type", "Background");
				serializer.startTag(null, "StartTime");
				serializer.text((event.BG.Get_MesurementDate() == null) ? "" : event.BG.Get_MesurementDate());
				serializer.endTag(null, "StartTime");

				serializer.startTag(null, "RealTime");
				serializer.text("PT" + (event.BG.Get_AcqTime() != 0 ? event.MS.Get_AcqTime() + ".0S" : "0.0S"));
				serializer.endTag(null, "RealTime");

				serializer.startTag(null, "LiveTime");
				serializer.text("PT" + (event.BG.Get_AcqTime() != 0 ? event.MS.Get_AcqTime() + ".0S" : "0.0S"));
				serializer.endTag(null, "LiveTime");

				serializer.startTag(null, "SourceType");
				serializer.text("Item");
				serializer.endTag(null, "SourceType");

				serializer.startTag(null, "ChannelData");
				serializer.text((event.BG !=null ? event.BG.ToString_() : ""));
				serializer.endTag(null, "ChannelData");

				serializer.endTag(null, "Spectrum");
				//
				serializer.endTag(null, "Measurement");
				// -----------
				serializer.startTag(null, "AnalysisResults");
				serializer.startTag(null, "ThreatDescription");
				serializer.text("None");
				serializer.endTag(null, "ThreatDescription");

				serializer.startTag(null, "NuclideAnalysis");
				serializer.attribute(null, "ActivityUnits", "Bq");

				if(event.Detected_Isotope!=null)
				{
					for (int i = 0; i < event.Detected_Isotope.size(); i++) 
					{
						serializer.startTag(null, "Nuclide");
						serializer.startTag(null, "NuclideName");
						serializer.text(event.Detected_Isotope.get(i).isotopes);
						serializer.endTag(null, "NuclideName");

						serializer.startTag(null, "NuclideType");
						serializer.text(event.Detected_Isotope.get(i).Class);
						serializer.endTag(null, "NuclideType");

						int RoiCnt = 0;
						for (int q = 0; q < event.Detected_Isotope.get(i).FoundPeaks.size(); q++) 
						{
							RoiCnt += Get_RoiCount(event.MS.ToInteger(),(int) (SpcAnalysis.ToChannel( event.Detected_Isotope.get(i).FoundPeaks.get(q).Peak_Energy * 0.94, event.MS.Get_Coefficients())),
									(int) (SpcAnalysis.ToChannel(event.Detected_Isotope.get(i).FoundPeaks.get(q).Peak_Energy * 1.6,event.MS.Get_Coefficients())));
						}
						serializer.startTag(null, "NuclideActivity");
						serializer.text(String.valueOf(RoiCnt) + " 0");
						serializer.endTag(null, "NuclideActivity");

						serializer.startTag(null, "NuclideDoserate"); // KINS
						serializer.text(event.Detected_Isotope.get(i).DoseRate_S);
						serializer.endTag(null, "NuclideDoserate");

						serializer.startTag(null, "NuclideIDConfidenceIndication");
						serializer.text(String.valueOf((int) event.Detected_Isotope.get(i).Confidence_Level));
						serializer.endTag(null, "NuclideIDConfidenceIndication");


						serializer.startTag(null, "NuclideIDConfidenceDescription");
						serializer.text("Medium");
						serializer.endTag(null, "NuclideIDConfidenceDescription");
						serializer.endTag(null, "Nuclide");

					}
				}
				else
				{
					serializer.startTag(null, "Nuclide");
					serializer.startTag(null, "NuclideName");
					serializer.text("");
					serializer.endTag(null, "NuclideName");

					serializer.startTag(null, "NuclideType");
					serializer.text("");
					serializer.endTag(null, "NuclideType");

					serializer.startTag(null, "NuclideActivity");
					serializer.text("");
					serializer.endTag(null, "NuclideActivity");

					serializer.startTag(null, "NuclideDoserate"); // KINS
					serializer.text("");
					serializer.endTag(null, "NuclideDoserate");

					serializer.startTag(null, "NuclideIDConfidenceIndication");
					serializer.text("");
					serializer.endTag(null, "NuclideIDConfidenceIndication");
					
					serializer.startTag(null, "NuclideIDConfidenceDescription");
					serializer.text("Medium");
					serializer.endTag(null, "NuclideIDConfidenceDescription");
					serializer.endTag(null, "Nuclide");
				}

				serializer.endTag(null, "NuclideAnalysis");
				serializer.endTag(null, "AnalysisResults");
				// -----------
				serializer.startTag(null, "Calibration");
				serializer.attribute(null, "EnergyUnits", "keV");
				serializer.attribute(null, "ID", "en");
				serializer.attribute(null, "Type", "Energy");

				serializer.startTag(null, "Equation");
				serializer.attribute(null, "Form", "Term0+(Term1*Ch)+(Term2*(Ch^2))");
				serializer.attribute(null, "Model", "Polynomial");

				serializer.startTag(null, "Coefficients");
				serializer.text((event.MS.Get_Coefficients().get_Coefficients() != null ? (event.MS.Get_Coefficients().At(2) + " " + event.MS.Get_Coefficients().At(1) + " "+ event.MS.Get_Coefficients().At(0)) : ""));
				serializer.endTag(null, "Coefficients");

				serializer.startTag(null, "CovarianceMatrix");
				serializer.text((event.MS.Get_Coefficients().get_Coefficients() !=null ? (event.MS.Get_Coefficients().At(2) + " " + event.MS.Get_Coefficients().At(1) + " "+ event.MS.Get_Coefficients().At(0)) : ""));
				serializer.endTag(null, "CovarianceMatrix");
				serializer.endTag(null, "Equation");
				serializer.endTag(null, "Calibration");
				/// ------------------------

				serializer.endTag(null, "N42InstrumentData");
				serializer.endDocument();
				// write xml data into the FileOutputStream
				serializer.flush();
				// finally we close the file stream
				fileos.close();
				path = newxmlfile.getAbsolutePath();

			} 
			catch (Exception e) 
			{
				NcLibrary.Write_ExceptionLog(e);
				path = "";
				return path;
			}
			return path;
		}

	/*	public static String WriteXML_toANSI42(EventData event, String ToName, Boolean IsIDspectrumMode) {

			if (event.Event_Date == null)
				event.Event_Date = "";
			if (event.StartTime == null)
				event.StartTime = "";
			if (event.EndTime == null)
				event.EndTime = "";

			File newxmlfile;
			if (ToName != null) {
				newxmlfile = new File(Environment.getExternalStorageDirectory() + "/" + ToName);
			} else {
				newxmlfile = new File(Environment.getExternalStorageDirectory() + "/Event.xml");
			}
			// create a new file called "new.xml" in the SD card

			try {
				newxmlfile.createNewFile();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			// we have to bind the new file with a FileOutputStream
			FileOutputStream fileos = null;
			try {
				fileos = new FileOutputStream(newxmlfile);
			} catch (FileNotFoundException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			// we create a XmlSerializer in order to write xml data
			XmlSerializer serializer = Xml.newSerializer();
			try {
				// we set the FileOutputStream as output for the serializer,
				// using UTF-8 encoding
				serializer.setOutput(fileos, "UTF-8");
				// Write <?xml declaration with encoding (if encoding not null)
				// and standalone flag (if standalone not null)
				serializer.startDocument(null, Boolean.valueOf(true));
				// set indentation option
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				// start a tag called "root"
				serializer.startTag(null, "N42InstrumentData");
				serializer.attribute(null, "xmlns",
						"http://physics.nist.gov/Divisions/Div846/Gp4/ANSIN4242/2005/ANSIN4242");
				serializer.attribute(null, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
				serializer.attribute(null, "xsi:schemaLocation",
						"http://physics.nist.gov/Divisions/Div846/Gp4/ANSIN4242/2005/ANSIN4242 http://physics.nist.gov/Divisions/Div846/Gp4/ANSIN4242/2005/ANSIN4242.xsd");
				/// ----------------------------------------------------
				serializer.startTag(null, "Measurement");
				serializer.attribute(null, "UUID", "54531d28-402b-11d8-af12-0002a5094c23");
				serializer.startTag(null, "InstrumentInformation");
				serializer.startTag(null, "InstrumentType");
				serializer.text("Spectrometer");
				serializer.endTag(null, "InstrumentType");

				serializer.startTag(null, "Manufacture");
				serializer.text("Nucare medical systems");
				serializer.endTag(null, "Manufacture");

				serializer.startTag(null, "InstrumentModel");
				serializer.text("SAM 950");
				serializer.endTag(null, "InstrumentModel");

				serializer.startTag(null, "InstrumentID");
				serializer.text((event.mInstrument_Name == null) ? "" : event.mInstrument_Name);
				serializer.endTag(null, "InstrumentID");
				serializer.endTag(null, "InstrumentInformation");
				//
				serializer.startTag(null, "MeasuredItemInformation");
				serializer.startTag(null, "ItemDescription");
				serializer.text("Cal standard 1");
				serializer.endTag(null, "ItemDescription");

				serializer.startTag(null, "ItemQuantity");
				serializer.attribute(null, "Units", "Kg");
				serializer.text("1.0.0");
				serializer.endTag(null, "ItemQuantity");

				serializer.startTag(null, "MeasurementLocation");
				serializer.startTag(null, "MeasurementLocationName");
				serializer.text(event.mLocation !=null ? event.mLocation : "");
				serializer.endTag(null, "MeasurementLocationName");

				serializer.startTag(null, "Coordinates");
				serializer.text(Double.toString(event.GPS_Latitude) + " " + Double.toString(event.GPS_Longitude));
				serializer.endTag(null, "Coordinates");
				serializer.endTag(null, "MeasurementLocation");

				serializer.startTag(null, "ItemReferenceDate");
				serializer.text(event.Event_Date + "T" + event.StartTime);
				serializer.endTag(null, "ItemReferenceDate");
				serializer.endTag(null, "MeasuredItemInformation");

				// Mesurement spectrum
				serializer.startTag(null, "Spectrum");
				serializer.attribute(null, "CalibrationIDs", "en");
				serializer.startTag(null, "StartTime");
				serializer.text(event.Event_Date + "T" + event.StartTime);
				serializer.endTag(null, "StartTime");

				serializer.startTag(null, "RealTime");
				serializer.text("PT" + event.MS.Get_AcqTime() + ".0S");
				serializer.endTag(null, "RealTime");

				serializer.startTag(null, "LiveTime");
				serializer.text("PT" + event.MS.Get_AcqTime() + ".0S");
				serializer.endTag(null, "LiveTime");

				serializer.startTag(null, "SourceType");
				serializer.text("Item");
				serializer.endTag(null, "SourceType");

				serializer.startTag(null, "ChannelData");
				serializer.text(event.MS.ToString_());
				serializer.endTag(null, "ChannelData");
				String Doserate_AVGs;
				String MaxDoserate;
				if (IsIDspectrumMode) {
					Doserate_AVGs = NcLibrary.SvToString(event.Doserate_AVG, true,
							(event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false);

					MaxDoserate = NcLibrary.SvToString(event.Doserate_MAX, true,
							(event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false);
				} else {
					Doserate_AVGs = event.Doserate_AVGs;
					MaxDoserate = event.Doserate_MAXs;
				}

				serializer.startTag(null, "AvgDoserate"); // KINS
				serializer.text(Doserate_AVGs);
				serializer.endTag(null, "AvgDoserate");

				serializer.startTag(null, "MaxDoserate"); // KINS
				serializer.text(MaxDoserate);
				serializer.endTag(null, "MaxDoserate");
				serializer.endTag(null, "Spectrum");
				//

				// Mesurement spectrum
				serializer.startTag(null, "Spectrum");
				serializer.attribute(null, "CalibrationIDs", "en");
				serializer.attribute(null, "Type", "Background");
				serializer.startTag(null, "StartTime");
				serializer.text((event.BG.Get_MesurementDate() == null) ? "" : event.BG.Get_MesurementDate());
				serializer.endTag(null, "StartTime");

				serializer.startTag(null, "RealTime");
				serializer.text("PT" + event.BG.Get_AcqTime() + ".0S");
				serializer.endTag(null, "RealTime");

				serializer.startTag(null, "LiveTime");
				serializer.text("PT" + event.BG.Get_AcqTime() + ".0S");
				serializer.endTag(null, "LiveTime");

				serializer.startTag(null, "SourceType");
				serializer.text("Item");
				serializer.endTag(null, "SourceType");

				serializer.startTag(null, "ChannelData");
				serializer.text(event.BG.ToString_());
				serializer.endTag(null, "ChannelData");

				serializer.endTag(null, "Spectrum");
				//
				serializer.endTag(null, "Measurement");
				// -----------
				serializer.startTag(null, "AnalysisResults");
				serializer.startTag(null, "ThreatDescription");
				serializer.text("None");
				serializer.endTag(null, "ThreatDescription");

				serializer.startTag(null, "NuclideAnalysis");
				serializer.attribute(null, "ActivityUnits", "Bq");
				if (event.Detected_Isotope != null) {
					for (int i = 0; i < event.Detected_Isotope.size(); i++) {
						serializer.startTag(null, "Nuclide");
						serializer.startTag(null, "NuclideName");
						serializer.text(event.Detected_Isotope.get(i).isotopes);
						serializer.endTag(null, "NuclideName");

						serializer.startTag(null, "NuclideType");
						serializer.text(event.Detected_Isotope.get(i).Class);
						serializer.endTag(null, "NuclideType");

						int RoiCnt = 0;
						for (int q = 0; q < event.Detected_Isotope.get(i).FoundPeaks.size(); q++) {
							RoiCnt += Get_RoiCount(event.MS.ToInteger(),
									(int) (SpcAnalysis.ToChannel(
											event.Detected_Isotope.get(i).FoundPeaks.get(q).Energy * 0.94,
											event.MS.Get_Coefficients())),
									(int) (SpcAnalysis.ToChannel(
											event.Detected_Isotope.get(i).FoundPeaks.get(q).Energy * 1.6,
											event.MS.Get_Coefficients())));
						}
						serializer.startTag(null, "NuclideActivity");
						serializer.text(String.valueOf(RoiCnt) + " 0");
						serializer.endTag(null, "NuclideActivity");

						serializer.startTag(null, "NuclideDoserate"); // KINS
						serializer.text(event.Detected_Isotope.get(i).DoseRate_S);
						serializer.endTag(null, "NuclideDoserate");

						serializer.startTag(null, "NuclideIDConfidenceIndication");
						serializer.text(String.valueOf((int) event.Detected_Isotope.get(i).Confidence_Level));
						serializer.endTag(null, "NuclideIDConfidenceIndication");

						serializer.startTag(null, "NuclideIDConfidenceDescription");
						serializer.text("Medium");
						serializer.endTag(null, "NuclideIDConfidenceDescription");
						serializer.endTag(null, "Nuclide");
					}
				}
				serializer.endTag(null, "NuclideAnalysis");
				serializer.endTag(null, "AnalysisResults");

				// -----------
				serializer.startTag(null, "Calibration");
				serializer.attribute(null, "EnergyUnits", "keV");
				serializer.attribute(null, "ID", "en");
				serializer.attribute(null, "Type", "Energy");

				serializer.startTag(null, "Equation");
				serializer.attribute(null, "Form", "Term0+(Term1*Ch)+(Term2*(Ch^2))");
				serializer.attribute(null, "Model", "Polynomial");

				serializer.startTag(null, "Coefficients");
				serializer.text(event.MS.Get_Coefficients().At(2) + " " + event.MS.Get_Coefficients().At(1) + " "
						+ event.MS.Get_Coefficients().At(0));
				serializer.endTag(null, "Coefficients");

				serializer.startTag(null, "CovarianceMatrix");
				serializer.text(event.MS.Get_Coefficients().At(2) + " " + event.MS.Get_Coefficients().At(1) + " "
						+ event.MS.Get_Coefficients().At(0));
				serializer.endTag(null, "CovarianceMatrix");
				serializer.endTag(null, "Equation");
				serializer.endTag(null, "Calibration");
				/// ------------------------

				serializer.endTag(null, "N42InstrumentData");
				serializer.endDocument();
				// write xml data into the FileOutputStream
				serializer.flush();
				// finally we close the file stream
				fileos.close();

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			return newxmlfile.getAbsolutePath();
		}*/

		public static EventData ReadXML_ANSI42(String FilePath)
				throws SAXException, IOException, ParserConfigurationException {

			EventData Result = new EventData();

			File xml_file = null;
			xml_file = new File(FilePath);
			if (xml_file.isFile() == false)
				return Result;

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document doc = builder.parse(xml_file);

			NodeList node = doc.getElementsByTagName("InstrumentID");
			if (node.getLength() == 0)
				throw new SAXException();

			Result.mInstrument_Name = node.item(0).getTextContent();

			node = doc.getElementsByTagName("Coordinates");
			if (node.item(0).getTextContent() != "") {
				Result.GPS_Latitude = Double
						.valueOf(NcLibrary.Separate_EveryDash2(node.item(0).getTextContent(), ' ').get(0));
				Result.GPS_Longitude = Double
						.valueOf(NcLibrary.Separate_EveryDash2(node.item(0).getTextContent(), ' ').get(1));
			}

			node = doc.getElementsByTagName("ItemReferenceDate");
			Result.Event_Date = NcLibrary.Separate_EveryDash2(node.item(0).getTextContent(), 'T').get(0);
			Result.StartTime = NcLibrary.Separate_EveryDash2(node.item(0).getTextContent(), 'T').get(1);

			// -------------------- Spectrum

			node = doc.getElementsByTagName("RealTime");
			String temp = node.item(0).getTextContent();
			temp = temp.replace("PT", "");
			temp = temp.replace(".0S", "");
			int acqtime = Integer.valueOf(temp);

			temp = node.item(1).getTextContent();
			temp = temp.replace("PT", "");
			temp = temp.replace(".0S", "");
			int bg_acqtime = Integer.valueOf(temp);

			node = doc.getElementsByTagName("ChannelData");
			int[] ch = NcLibrary.Separate_EveryIndex(node.item(0).getTextContent(), ' ');
			Result.MS.Set_Spectrum(ch, acqtime);

			ch = NcLibrary.Separate_EveryIndex(node.item(1).getTextContent(), ' ');
			Result.BG.Set_Spectrum(ch, bg_acqtime);

			node = doc.getElementsByTagName("StartTime");
			Result.BG.Set_MeasurementDate(node.item(1).getTextContent());

			// -------------------------------------

			Vector<Isotope> tempIsoDB = new Vector<Isotope>();

			node = doc.getElementsByTagName("NuclideName");
			for (int i = 0; i < node.getLength(); i++) {
				Isotope tempIso = new Isotope();
				tempIso.isotopes = node.item(i).getTextContent();
				tempIsoDB.add(tempIso);
			}

			node = doc.getElementsByTagName("NuclideType");
			for (int i = 0; i < node.getLength(); i++) {
				tempIsoDB.get(i).Class = node.item(i).getTextContent();
			}

			node = doc.getElementsByTagName("NuclideIDConfidenceIndication");
			for (int i = 0; i < node.getLength(); i++) {
				tempIsoDB.get(i).Confidence_Level = Double.valueOf(node.item(i).getTextContent());
			}

			node = doc.getElementsByTagName("NuclideActivity");
			for (int i = 0; i < node.getLength(); i++) {
				int[] dr = NcLibrary.Separate_EveryIndex(node.item(i).getTextContent(), ' ');

				tempIsoDB.get(i).DoseRate = dr[0] / acqtime;
				tempIsoDB.get(i).DoseRate_S = String.valueOf(dr[0] / acqtime) + " Bq";

			}

			Result.Detected_Isotope = tempIsoDB;
			Result.Doserate_unit = -1;
			/// ----------------
			node = doc.getElementsByTagName("Coefficients");
			Vector<String> coeff = NcLibrary.Separate_EveryDash2(node.item(0).getTextContent(), ' ');

			Result.MS.Set_Coefficients(new double[] { Double.valueOf(coeff.get(2)), Double.valueOf(coeff.get(1)),
					Double.valueOf(coeff.get(0)) });
			Result.BG.Set_Coefficients(new double[] { Double.valueOf(coeff.get(2)), Double.valueOf(coeff.get(1)),
					Double.valueOf(coeff.get(0)) });

			return Result;

		}
	}

	public static String Get_AppVersion(Context context, String PackageName) {
		String version;
		try {
			PackageInfo i = context.getPackageManager().getPackageInfo(PackageName, 0);
			version = i.versionName;
			return version;
		} catch (NameNotFoundException e) {
			NcLibrary.Write_ExceptionLog(e);
			return null;
		}

	}

	public static boolean IsWifiAvailable(Context context) {
		ConnectivityManager m_NetConnectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean bConnect = false;
		try {
			if (m_NetConnectMgr == null)
				return false;

			NetworkInfo info = m_NetConnectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			bConnect = (info.isAvailable() && info.isConnected());

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}

		return bConnect;
	}

	public static boolean Is3GAvailable(Context context) {
		ConnectivityManager m_NetConnectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean bConnect = false;
		try {
			if (m_NetConnectMgr == null)
				return false;
			NetworkInfo info = m_NetConnectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			bConnect = (info.isAvailable() && info.isConnected());
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}

		return bConnect;
	}

	public static String Cut_Decimal_Point(int value) {

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

/*	public static void SendEmail(final EventData m_EventData, final Context mContext, final Handler mHandler) {
		try {

			final ProgressDialog mPrgDlg;
			mPrgDlg = new ProgressDialog(mContext);
			mPrgDlg.setIndeterminate(true);
			mPrgDlg.setCancelable(false);

			mPrgDlg.setTitle(mContext.getResources().getString(R.string.transmit_N42));
			mPrgDlg.setMessage("Sending...");
			mPrgDlg.show();

			Thread thread = new Thread() {

				@Override
				public void run() {

					super.run();

					
					 * if (isNetworkOnline(mContext) == false) { mHandler.sendEmptyMessage(3);
					 * mPrgDlg.dismiss(); return; }
					 * 
					 * PreferenceDB pref = new PreferenceDB(mContext); if (pref.Get_sender_email()
					 * == "" | pref.Get_sender_email() == null) { mHandler.sendEmptyMessage(2);
					 * mPrgDlg.dismiss(); return; }
					 * 
					 * String sender = pref.Get_sender_email(); String sender_pw =
					 * pref.Get_sender_pw(); String sender_server = pref.Get_sender_Server(); String
					 * sender_port = pref.Get_sender_Port(); String recv_mail =
					 * pref.Get_recv_email();
					 

					
					 * KINS200 유심사용가능 wifi, 유심 network 둘다 체크 180103
					 
					if (isNetworkOnline(mContext) == false) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(3);
						return;
					}

					PreferenceDB pref = new PreferenceDB(mContext);
					
					 * if (pref.Get_sender_email() == "" | pref.Get_sender_email() == null) {
					 * //mHandler.sendEmptyMessage(2); //mPrgDlg.dismiss(); // return;
					 * 
					 * }
					 

					String sender = pref.Get_sender_email();
					String sender_pw = pref.Get_sender_pw();
					String recv_mail = pref.Get_recv_email();
					String server = pref.Get_sender_Server();
					String sender_port = pref.Get_sender_Port();
					String sender_server = pref.Get_sender_Server();
					// Email 형식으로 소스코드 변경 시작

					if ((sender == null || sender.length() <= 1) || (sender_pw == null || sender_pw.length() <= 1)
							|| (recv_mail == null || recv_mail.length() <= 1)
							|| (server == null || server.length() <= 1)
							|| (sender_port == null || sender_port.length() <= 1)
							|| (sender_server == null || sender_server.length() <= 1)) {
						
						 * pref.Set_sender_Server("mail.kins.re.kr"); pref.Set_sender_Port("587");
						 * pref.Set_sender_pw("rpm0356##"); pref.Set_sender_email("kinsrpm@kins.re.kr");
						 * pref.Set_recv_address("kinsrpm@kins.re.kr");
						 

						sender = "kinsrpm@kins.re.kr";
						sender_pw = "rpm0356##";
						recv_mail = "kinsrpm@kins.re.kr";
						server = "mail.kins.re.kr";
						sender_port = "587";
						sender_server = "mail.kins.re.kr";

						pref.Set_sender_Server("mail.kins.re.kr");
						pref.Set_sender_Port("587");
						pref.Set_sender_pw("rpm0356##");
						pref.Set_sender_email("kinsrpm@kins.re.kr");
						pref.Set_recv_address("kinsrpm@kins.re.kr");

					}

					Mail m = new Mail(sender, sender_pw, sender_server, sender_port);

					// Array of emailIds where you want to
					// sent
					String[] toArr = new String[1];
					toArr[0] = recv_mail;
					m.setTo(toArr);

					// Your emailid(from)
					m.setFrom(sender);
					m.setSubject("SAM III PeakAbout - Event Data");
					m.setBody("From " + m_EventData.mInstrument_Name);
					try {
						m.addAttachment(GetCsvPath(), "SAM950 (" + CurrentDate() + " ).csv");
						m.addAttachment(NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData,m_EventData.Event_Date + "_" + m_EventData.StartTime + "("+ m_EventData.mInstrument_Name + ").xml",true),
								m_EventData.Event_Date + "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml");

					} catch (Exception e1) {
						NcLibrary.Write_ExceptionLog(e1);
					}

					try {
						if (m.send()) {
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(0);
							Log.v("Forgot Password mail", "Success");

						} else {
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(1);
							Log.v("Forgot Password mail", "Not Success");
						}
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(2);
						Log.e("MailApp", "Could not send email", e);
					}

				}

			};

			thread.start();

		} catch (Exception e) {

			NcLibrary.Write_ExceptionLog(e);
		}
	}

	*/

	
	/////180328 kins
	public static void SendEmail1_image(final Context mContext, final Handler mHandler,  boolean photo, boolean id, final String Path)
	{
		final boolean IsPhotoSave = photo;
		final boolean IDSpectrumMode = id;

		msg = "";
		if (!isNetworkOnline(mContext)) {
			Toast.makeText(mContext, "네트워크 연결을 확인해주세요", Toast.LENGTH_LONG).show();
			return;
		}
		
		else 
		{
			try 
			{
				final ProgressDialog mPrgDlg;
				mPrgDlg = new ProgressDialog(mContext);
				mPrgDlg.setTitle(mContext.getResources().getString(R.string.transmit_N42));
				mPrgDlg.setMessage("Sending...");
				mPrgDlg.setIndeterminate(true);
				mPrgDlg.setCancelable(false);
				final String appver = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),0).versionName;
				final EventDBOper db = new EventDBOper();
				mPrgDlg.show();

				Thread thread = new Thread() 
				{
					@Override
					public void run() {
						super.run();

/*				
						if ((sender == null || sender.length() <= 1) || (sender_pw == null || sender_pw.length() <= 1)
								|| (recv_mail == null || recv_mail.length() <= 1)
								|| (server == null || server.length() <= 1)) {
							sender = "kinsrpm@kins.re.kr";
							sender_pw = "rpm0356##";
							recv_mail = "kinsrpm@kins.re.kr";
							server = "mail.kins.re.kr";

							pref.Set_sender_Server("mail.kins.re.kr");
							pref.Set_sender_Port("587");
							pref.Set_sender_pw("rpm0356##");
							pref.Set_sender_email("kinsrpm@kins.re.kr");
							pref.Set_recv_address("kinsrpm@kins.re.kr");

						}
						
						*/

						PreferenceDB pref = new PreferenceDB(mContext);
						String sender = pref.Get_sender_email();
						String sender_pw = pref.Get_sender_pw();
						String recv_mail = pref.Get_recv_email();
						String sender_server = pref.Get_sender_Server();
						String sender_port = pref.Get_sender_Port();
						
						final EventDBOper db = new EventDBOper();
						EventData m_EventData = db.Load_Event(db.GetEventCount() - 1);
				
						//db.OpenDB();
						final Mail m = new Mail(sender, sender_pw, sender_server, sender_port);
						File n42 = null;
						File n43 = null;

						// Array of emailIds where you want to
						// sent
						String[] toArr = new String[1];
						toArr[0] = recv_mail;
						m.setTo(toArr);

						// Your emailid(from)
						m.setFrom(sender);
						m.setSubject("SAM III PeakAbout - Event Data (" + m_EventData.mLocation + ")");
						m.setBody("From " + m_EventData.mInstrument_Name + "(" + m_EventData.mLocation+ ")" + "\nAppVersion : PeakAbout III " + appver);

					
						
						try 
						{
							boolean n42FileTemp = true;
							String path = "";
									
								if (m_EventData != null) 
								{
									String name = pref.Get_Last_Cntd_User();
									path = NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml", IDSpectrumMode,name);
										if(!path.equals(""))
										{
											n42 = new File(path);
											if (n42 != null && n42.exists())
											{
												Long size = n42.length();
												if(size !=0)
												{
													m.addAttachment(path, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml" );
													n42FileTemp = false;
													Log.e("ahn", "File Size :"+ size);
												}
													
											}
												
										}
								}
								
								/*
								 *  n42FileTemp 기본값 true
								 *  n42 파일이 정상적으로 생성됐을대 false로 변함
								 *  파일이 없거나 size가 0일때 메일전송안되게 처리
								 */
								if(n42FileTemp)
								{
									NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3414 file == null");
									mPrgDlg.dismiss();
									mHandler.sendEmptyMessage(4);
									return;
								}
								
								if (IsPhotoSave) 
								{
									isPhoto = false;
//									String FileName2 = Media.FolderPath + "/" + m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png";
									String FileName2 = Media.FolderPath + "/" + Path + ".png";
									n43 = new File(FileName2);
									if (n43.exists()) 
									{
										//m.addAttachment(FileName2 ,Path + ".png" );				
										db.Update_PhotoFileNames(Path,  m_EventData.Event_Number);

										isPhoto = true;
									} 
									else 
									{
										isPhoto = false;
									}

								}
								
								db.OpenDB();
								Vector<String> mPhoto = db.Get_PhotoFileName(m_EventData.Event_Number - 1);
								Vector<String> mPhotoName = new Vector<String>();
								if(mPhoto!=null && mPhoto.size() != 0)
								{
									for (int i = 0; i < mPhoto.size(); i++) {

										mPhotoName.add(mPhoto.get(i));
										mPhoto.set(i, Media.FolderPath + "/" + mPhoto.get(i) + ".png");

										File file;
										file = new File(mPhoto.get(i));
										if (file.exists()) 
										{

											m.addAttachment(mPhoto.get(i) , mPhotoName.get(i) + ".png" );
											isPhoto = true;
										}
									}
								}
								
								
								db.EndDB();
			
																	
							if (m.send()) {
								// Toast.makeText(mContext,, Toast.LENGTH_LONG).show();

								mPrgDlg.dismiss();
								msg = "전송완료되었습니다";
								Log.v("Forgot Password mail", "Success");

								if (isPhoto)
									mHandler.sendEmptyMessage(0);
								else
									mHandler.sendEmptyMessage(5);
								
								n42.delete();
						/*		if (n43 != null && n43.exists())
									n43.delete();*/
								
							} else {
								mPrgDlg.dismiss();
								msg = "전송이 실패하였습니다";
								Log.v("Forgot Password mail", "Not Success");
							}

						} catch (Exception e) {
							NcLibrary.Write_ExceptionLog(e);
							mPrgDlg.dismiss();
							Log.e("MailApp", "Could not send email", e);
						}
					}
				};

				thread.start();

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	
	

	/*
	 * IDspectrum에서 전송
	 * 0115 1. eventdata db에서 불러와서 실행 2. 사진파일, eventdata null체크
	 * 0329 2. Mail 클래스로 변경
	 */
	public static void SendEmail1(final Context mContext, final Handler mHandler,  boolean photo, boolean id)
	{
		final boolean IsPhotoSave = photo;
		final boolean IDSpectrumMode = id;
		final PreferenceDB pref = new PreferenceDB(mContext);
		
		msg = "";
		if (!isNetworkOnline(mContext)) {
			Toast.makeText(mContext, "네트워크 연결을 확인해주세요", Toast.LENGTH_LONG).show();
			return;
		}
		
		else 
		{
			try 
			{
				final ProgressDialog mPrgDlg;
				mPrgDlg = new ProgressDialog(mContext);
				mPrgDlg.setTitle(mContext.getResources().getString(R.string.transmit_N42));
				mPrgDlg.setMessage("Sending...");
				mPrgDlg.setIndeterminate(true);
				mPrgDlg.setCancelable(false);
				final String appver = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),0).versionName;
				final EventDBOper db = new EventDBOper();
				mPrgDlg.show();

				Thread thread = new Thread() 
				{
					@Override
					public void run() {
						super.run();
				
						String sender = pref.Get_sender_email();
						String sender_pw = pref.Get_sender_pw();
						String recv_mail = pref.Get_recv_email();
						String sender_server = pref.Get_sender_Server();
						String sender_port = pref.Get_sender_Port();
						
						if ((sender == null || sender.length() <= 1) || (sender_pw == null || sender_pw.length() <= 1)
								|| (recv_mail == null || recv_mail.length() <= 1)
								|| (sender_server == null || sender_server.length() <= 1) || (sender_port == null || sender_port.length() <= 1)) {
							sender = "inseon.ahn@nucaremed.com";
							sender_pw = "dksdlstjs233";
							recv_mail = "kinsrpm@kins.re.kr";
							sender_server = "mail.nucaremed.com";
							sender_port = "587";

							
							pref.Set_sender_Server("mail.nucaremed.com");
							pref.Set_sender_Port("587");
							pref.Set_sender_pw("dksdlstjs233");
							pref.Set_sender_email("inseon.ahn@nucaremed.com");
							pref.Set_recv_address("kinsrpm@kins.re.kr");
							

						}
						

			
						
						final EventDBOper db = new EventDBOper();
						EventData m_EventData = db.Load_Event(db.GetEventCount() - 1);
				
						//db.OpenDB();
						final Mail m = new Mail(sender, sender_pw, sender_server, sender_port);
						File n42 = null;
						File n43 = null;

						// Array of emailIds where you want to
						// sent
						String[] toArr = new String[1];
						toArr[0] = recv_mail;
						m.setTo(toArr);

						// Your emailid(from)
						m.setFrom(sender);
						m.setSubject("SAM III PeakAbout - Event Data (" + m_EventData.mLocation + ")");
						m.setBody("From " + m_EventData.mInstrument_Name + "(" + m_EventData.mLocation+ ")" + "\nAppVersion : PeakAbout III " + appver);

					
						
						try 
						{
							boolean n42FileTemp = true;
							String path = "";
									
								if (m_EventData != null) 
								{
									String name = pref.Get_Last_Cntd_User();
									path = NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml", IDSpectrumMode, name);
										if(!path.equals(""))
										{
											n42 = new File(path);
											if (n42 != null && n42.exists())
											{
												Long size = n42.length();
												if(size !=0)
												{
													m.addAttachment(path, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml" );
													n42FileTemp = false;
													Log.e("ahn", "File Size :"+ size);
												}
													
											}
												
										}
								}
								
								/*
								 *  n42FileTemp 기본값 true
								 *  n42 파일이 정상적으로 생성됐을대 false로 변함
								 *  파일이 없거나 size가 0일때 메일전송안되게 처리
								 */
								if(n42FileTemp)
								{
									NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3414 file == null");
									mPrgDlg.dismiss();
									mHandler.sendEmptyMessage(4);
									return;
								}
								
								if (IsPhotoSave) 
								{
									isPhoto = false;
									String FileName2 = Media.FolderPath + "/" + m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png";
								
									n43 = new File(FileName2);
									if (n43.exists()) 
									{
										m.addAttachment(FileName2 ,m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png" );				
										isPhoto = true;
									} 
									else 
									{
										isPhoto = false;
									}

								}
								
											
							if (m.send()) {
								// Toast.makeText(mContext,, Toast.LENGTH_LONG).show();

								mPrgDlg.dismiss();
								msg = "전송완료되었습니다";
								Log.v("Forgot Password mail", "Success");

								if (isPhoto)
									mHandler.sendEmptyMessage(0);
								else
									mHandler.sendEmptyMessage(5);
								
								n42.delete();
						/*		if (n43 != null && n43.exists())
									n43.delete();*/
								
							} else {
								mPrgDlg.dismiss();
								msg = "전송이 실패하였습니다";
								Log.v("Forgot Password mail", "Not Success");
							}

						} catch (Exception e) {
							NcLibrary.Write_ExceptionLog(e);
							mPrgDlg.dismiss();
							Log.e("MailApp", "Could not send email", e);
						}
					}
				};

				thread.start();

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}


	//EventList에서 전송 180731수정
	//180827 return value 수정 (void->EventData)
	public static EventData SendEmail( final boolean IDSpectrumMode,final Context context , final  int _id,final boolean IsPhotoSave)
	{
		m_EventData = null;
		final ProgressDialog mPrgDlg;
		mPrgDlg = new ProgressDialog((new ContextThemeWrapper(context, android.R.style.Theme_Holo_Dialog)));
		mPrgDlg.setTitle(context.getResources().getString(R.string.transmit_N42));
		mPrgDlg.setMessage("Sending...");
		mPrgDlg.setIndeterminate(true);
		mPrgDlg.setCancelable(false);

		final Handler mHandler = new Handler(Looper.getMainLooper())
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what)
				{
					case 0:
						m_EventData.reachBackSuccess = true;
						Show_Dlg1(context.getResources().getString(R.string.email_transmit_success).toString(), context);
						break;
					case 1:
						Show_Dlg1(context.getResources().getString(R.string.email_transmit_failed).toString(), context);
						break;
					case 2:
						Show_Dlg1(context.getResources().getString(R.string.email_info_fail).toString(), context);
						break;
					case 3:
						Show_Dlg1(context.getResources().getString(R.string.internet_not).toString(), context);
						break;
					case 4:
						Show_Dlg1(context.getResources().getString(R.string.email_transmit_error).toString(), context);
						break;
					case 5:
						Show_Dlg1(context.getResources().getString(R.string.email_transmit_pic_error).toString(), context);
						break;
					case 7:
						mPrgDlg.show();
						break;
					default:
						break;
				}
			}
		};


		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				super.run();



				mHandler.sendEmptyMessage(7); //progressbar show

				EventDBOper mEventDB = new EventDBOper();
				mEventDB.OpenDB();
				m_EventData = mEventDB.LoadEventDB(_id);
				mEventDB.EndDB();

				if (m_EventData != null) {
					PreferenceDB pref = new PreferenceDB(context);
					String sender = pref.Get_sender_email();
					String sender_pw = pref.Get_sender_pw();
					String recv_mail = pref.Get_recv_email();
					String sender_server = pref.Get_sender_Server();
					String sender_port = pref.Get_sender_Port();

					if ((sender == null || sender.length() <= 1) || (sender_pw == null || sender_pw.length() <= 1) || (recv_mail == null || recv_mail.length() <= 1)
							|| (sender_server == null || sender_server.length() <= 1) || (sender_port == null || sender_port.length() <= 1)) {
						sender = "inseon.ahn@nucaremed.com";
						sender_pw = "dksdlstjs233";
						recv_mail = "kinsrpm@kins.re.kr";
						sender_server = "mail.nucaremed.com";
						sender_port = "587";

						pref.Set_sender_Server("mail.nucaremed.com");
						pref.Set_sender_Port("587");
						pref.Set_sender_pw("dksdlstjs233");
						pref.Set_sender_email("inseon.ahn@nucaremed.com");
						pref.Set_recv_address("kinsrpm@kins.re.kr");
					}

					final Mail m = new Mail(sender, sender_pw, sender_server, sender_port);
					try {
						File n42 = null;
						File n43 = null;

						String[] toArr = new String[1];
						toArr[0] = recv_mail;
						m.setTo(toArr);

						m.setFrom(sender);

						//m.setSubject("test");
						m.setSubject("SAM III PeakAbout - Event Data (" + m_EventData.mLocation + ")");
					/*m.setBody("From " + m_EventData.mInstrument_Name + "(" + m_EventData.mLocation + ")" + "\nAppVersion : PeakAbout III " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName
							+ "\n"+context.getResources().getString(R.string.email_addAttachment).toString() + (m_EventData.PhotoFileName1 != null ? m_EventData.PhotoFileName1.size() : "0"));*/
						m.setBody("From " + m_EventData.mInstrument_Name + "(" + m_EventData.mLocation + ")" + "\nAppVersion : PeakAbout III " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);

						boolean n42FileTemp = true;
						String name = pref.Get_Last_Cntd_User();
						//	String path = NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData, m_EventData.Event_Date + "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml", IDSpectrumMode, name);
						m_EventData.reachBackXml = NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData, m_EventData.Event_Date + "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml", IDSpectrumMode, name);
						if (!m_EventData.reachBackXml.equals("")) {
							n42 = new File(m_EventData.reachBackXml);
							if (n42 != null && n42.exists()) {
								Long size = n42.length();
								if (size != 0) {
									m.addAttachment(m_EventData.reachBackXml, m_EventData.Event_Date + "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml");
									n42FileTemp = false;
									//Log.e("ahn", "File Size :" + size);
								}

							}

						}
					/*
					 *  n42FileTemp 기본값 true
					 *  n42 파일이 정상적으로 생성됐을대 false로 변함
					 *  파일이 없거나 size가 0일때 메일전송안되게 처리
					 */
						if (n42FileTemp) {
							NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3809 file == null");
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(4);
							return;
						}

						if (IsPhotoSave) {
							isPhoto = false;

							m_EventData.reachBackPic = Media.reachbackFolderPath + "/" + "EventP" + _id + "_1" + ".png";
							n43 = new File(m_EventData.reachBackPic);
							if (n43.exists()) {
								m.addAttachment(m_EventData.reachBackPic, m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png");
								isPhoto = true;
							} else {
								n43 = new File(Media.reachbackFolderPath + "/" + "EventP" + _id + "_1_" + ".png");
								if (n43.exists()) {
									m_EventData.reachBackPic = Media.reachbackFolderPath + "/" + "EventP" + _id + "_1_" + ".png";
									m.addAttachment(m_EventData.reachBackPic, m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png");
									isPhoto = true;
								} else {
									isPhoto = false;
								}
							}
						}

/*
					//EventDB안에 있는 Photo
					if(m_EventData.PhotoFileName1!=null && m_EventData.PhotoFileName1.size() != 0)
					{
						for (int i = 0; i < m_EventData.PhotoFileName1.size(); i++)
						{
							File file;
							file = new File(Media.FolderPath + "/" + m_EventData.PhotoFileName1.get(i)+".png");
							if (file.exists())
							{

								m.addAttachment(Media.FolderPath + "/" + m_EventData.PhotoFileName1.get(i)+".png" ,  m_EventData.Event_Date + "_" + m_EventData.StartTime+ "_" +m_EventData.PhotoFileName1.get(i)+".png");
							}
						}
					}*/

					} catch (IOException e) {
						NcLibrary.Write_ExceptionLog(e);
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(4);
						e.printStackTrace();
						return;
					} catch (Exception e) {
						e.printStackTrace();
						NcLibrary.Write_ExceptionLog(e);
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(4);
						e.printStackTrace();
						return;
					}
					try {
						if (m.send()) {
							m_EventData.reachBackSuccess = true;
							mPrgDlg.dismiss();
							Log.v("Forgot Password mail", "Success");
							mHandler.sendEmptyMessage(0);
							Thread.currentThread().interrupt();

						} else {
							mPrgDlg.dismiss();
							Log.v("Forgot Password mail", "Not Success");
							Thread.currentThread().interrupt();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3213 Exception");
						NcLibrary.Write_ExceptionLog(e1);
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(1);
					}


				}

				}};

		thread.start();

		try {
			// 해당 쓰레드가 멈출때까지 멈춤
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			return m_EventData;



	}


	//180828 리치백 재전송
	public static EventData reSendEmail( final boolean IDSpectrumMode,final Context context , final  int _id,final boolean IsPhotoSave)
	{
		m_EventData = null;
		final ProgressDialog mPrgDlg;
		mPrgDlg = new ProgressDialog((new ContextThemeWrapper(context, android.R.style.Theme_Holo_Dialog)));
		mPrgDlg.setTitle(context.getResources().getString(R.string.transmit_N42));
		mPrgDlg.setMessage("Sending...");
		mPrgDlg.setIndeterminate(true);
		mPrgDlg.setCancelable(false);

		final Handler mHandler = new Handler(Looper.getMainLooper())
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case 0:
						m_EventData.reachBackSuccess = true;
						Show_Dlg1(context.getResources().getString(R.string.email_transmit_success).toString(), context);
						break;
					case 1:
						Show_Dlg1(context.getResources().getString(R.string.email_transmit_failed).toString(), context);
						break;
					case 2:
						Show_Dlg1(context.getResources().getString(R.string.email_info_fail).toString(), context);
						break;
					case 3:
						Show_Dlg1(context.getResources().getString(R.string.internet_not).toString(), context);
						break;
					case 4:
						Show_Dlg1(context.getResources().getString(R.string.email_transmit_error).toString(), context);
						break;
					case 5:
						Show_Dlg1(context.getResources().getString(R.string.email_transmit_pic_error).toString(), context);
						break;
					case 7:
						mPrgDlg.show();
						break;
					default:
						break;
				}
			}
		};


		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				super.run();

					/*
					 * KINS200 유심사용가능 wifi, 유심 network 둘다 체크 180103
					*/
				if (isNetworkOnline(context) == false)
				{
					mPrgDlg.dismiss();
					mHandler.sendEmptyMessage(3);
					return;
				}

				mHandler.sendEmptyMessage(7); //progressbar show
				m_EventData = DBMng.GetInst(context).loadReachBackDB(_id);

				if (m_EventData != null)
				{
					PreferenceDB pref = new PreferenceDB(context);
					String sender = pref.Get_sender_email();
					String sender_pw = pref.Get_sender_pw();
					String recv_mail = pref.Get_recv_email();
					String sender_server = pref.Get_sender_Server();
					String sender_port = pref.Get_sender_Port();

					if ((sender == null || sender.length() <= 1) || (sender_pw == null || sender_pw.length() <= 1) || (recv_mail == null || recv_mail.length() <= 1)
							|| (sender_server == null || sender_server.length() <= 1) || (sender_port == null || sender_port.length() <= 1)) {
						sender = "inseon.ahn@nucaremed.com";
						sender_pw = "dksdlstjs233";
						recv_mail = "kinsrpm@kins.re.kr";
						sender_server = "mail.nucaremed.com";
						sender_port = "587";

						pref.Set_sender_Server("mail.nucaremed.com");
						pref.Set_sender_Port("587");
						pref.Set_sender_pw("dksdlstjs233");
						pref.Set_sender_email("inseon.ahn@nucaremed.com");
						pref.Set_recv_address("kinsrpm@kins.re.kr");
					}

					final Mail m = new Mail(sender, sender_pw, sender_server, sender_port);
					try {
						File n42 = null;
						File n43 = null;

						String[] toArr = new String[1];
						toArr[0] = recv_mail;
						m.setTo(toArr);

						m.setFrom(sender);
						//m.setSubject("resend_test");
						m.setSubject("SAM III PeakAbout - Event Data (" + m_EventData.mLocation + ")");
					/*m.setBody("From " + m_EventData.mInstrument_Name + "(" + m_EventData.mLocation + ")" + "\nAppVersion : PeakAbout III " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName
							+ "\n"+context.getResources().getString(R.string.email_addAttachment).toString() + (m_EventData.PhotoFileName1 != null ? m_EventData.PhotoFileName1.size() : "0"));*/
						m.setBody("From " + m_EventData.mInstrument_Name + "(" + m_EventData.mLocation + ")" + "\nAppVersion : PeakAbout III " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);

						boolean n42FileTemp = true;
						String name = pref.Get_Last_Cntd_User();
						if(name == null || name.equals("null"))
						{
							name = "None";
						}

						//이전에 만든 파일이 있는지 확인
						if (m_EventData.reachBackXml == null || m_EventData.reachBackXml.equals(""))
						{
							m_EventData.reachBackXml = NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData, m_EventData.Event_Date + "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml", IDSpectrumMode, name);
						}

						if (!m_EventData.reachBackXml.equals(""))
						{
							n42 = new File(m_EventData.reachBackXml);
							if (n42 != null && n42.exists()) {
								Long size = n42.length();
								if (size != 0) {
									m.addAttachment(m_EventData.reachBackXml, m_EventData.Event_Date + "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml");
									n42FileTemp = false;
									//Log.e("ahn", "File Size :" + size);
								}

							}

						}
					/*
					 *  n42FileTemp 기본값 true
					 *  n42 파일이 정상적으로 생성됐을대 false로 변함
					 *  파일이 없거나 size가 0일때 메일전송안되게 처리
					 */
						if (n42FileTemp) {
							NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3809 file == null");
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(4);
							return;
						}

						if (IsPhotoSave)
						{
							isPhoto = false;
							//m_EventData.reachBackPic = Media.reachbackFolderPath + "/" + m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png";
							if (!m_EventData.reachBackPic.equals("") && m_EventData.reachBackPic != null)
							{
								n43 = new File(m_EventData.reachBackPic);
								if (n43.exists())
								{
									m.addAttachment(m_EventData.reachBackPic, m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png");
									isPhoto = true;
								}
								else
								{
									isPhoto = false;
								}
							}
						}

/*
					//EventDB안에 있는 Photo
					if(m_EventData.PhotoFileName1!=null && m_EventData.PhotoFileName1.size() != 0)
					{
						for (int i = 0; i < m_EventData.PhotoFileName1.size(); i++)
						{
							File file;
							file = new File(Media.FolderPath + "/" + m_EventData.PhotoFileName1.get(i)+".png");
							if (file.exists())
							{

								m.addAttachment(Media.FolderPath + "/" + m_EventData.PhotoFileName1.get(i)+".png" ,  m_EventData.Event_Date + "_" + m_EventData.StartTime+ "_" +m_EventData.PhotoFileName1.get(i)+".png");
							}
						}
					}*/

					} catch (IOException e) {
						NcLibrary.Write_ExceptionLog(e);
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(4);
						e.printStackTrace();
						return;
					} catch (Exception e) {
						e.printStackTrace();
						NcLibrary.Write_ExceptionLog(e);
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(4);
						e.printStackTrace();
						return;
					}
					try {
						if (m.send()) {
							m_EventData.reachBackSuccess = true;
							mPrgDlg.dismiss();
							Log.v("Forgot Password mail", "Success");
							mHandler.sendEmptyMessage(0);
							Thread.currentThread().interrupt();

						} else {
							mPrgDlg.dismiss();
							Log.v("Forgot Password mail", "Not Success");
							Thread.currentThread().interrupt();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3213 Exception");
						NcLibrary.Write_ExceptionLog(e1);
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(1);
					}


				}
				else
				{
					NcLibrary.Write_ExceptionLog("\nNcLibrary Line 4243 Exception");
					mPrgDlg.dismiss();
					mHandler.sendEmptyMessage(1);
					return;
				}

			}};

		thread.start();

		try {
			// 해당 쓰레드가 멈출때까지 멈춤
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return m_EventData;



	}


	//EventList에서 전송
	public static void SendEmail_ListOld(final EventData m_EventData, final Context mContext, final Handler mHandler,
			final boolean IsPhotoSave, final boolean IDSpectrumMode) {
		try {

			final ProgressDialog mPrgDlg;
			mPrgDlg = new ProgressDialog(mContext);
			mPrgDlg.setTitle(mContext.getResources().getString(R.string.transmit_N42));
			mPrgDlg.setMessage("Sending...");
			mPrgDlg.setIndeterminate(true);
			mPrgDlg.setCancelable(false);
			final String appver = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
			final EventDBOper db = new EventDBOper();
			mPrgDlg.show();

			try {
				Thread thread = new Thread() {
					@Override
					public void run() {

						super.run();

						/*
						 * KINS200 유심사용가능 wifi, 유심 network 둘다 체크 180103
						 */
						if (isNetworkOnline(mContext) == false) {
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(3);
							return;
						}

						PreferenceDB pref = new PreferenceDB(mContext);
						
						String sender = pref.Get_sender_email();
						String sender_pw = pref.Get_sender_pw();
						String recv_mail = pref.Get_recv_email();
						String sender_server = pref.Get_sender_Server();
						String sender_port = pref.Get_sender_Port();
						
						if ((sender == null || sender.length() <= 1) || (sender_pw == null || sender_pw.length() <= 1)
								|| (recv_mail == null || recv_mail.length() <= 1)
								|| (sender_server == null || sender_server.length() <= 1) || (sender_port == null || sender_port.length() <= 1)) {
							sender = "inseon.ahn@nucaremed.com";
							sender_pw = "dksdlstjs233";
							recv_mail = "kinsrpm@kins.re.kr";
							sender_server = "mail.nucaremed.com";
							sender_port = "587";

							pref.Set_sender_Server("mail.nucaremed.com");
							pref.Set_sender_Port("587");
							pref.Set_sender_pw("dksdlstjs233");
							pref.Set_sender_email("inseon.ahn@nucaremed.com");
							pref.Set_recv_address("kinsrpm@kins.re.kr");
							

						}
					
						//db.OpenDB();
						final Mail m = new Mail(sender, sender_pw, sender_server, sender_port);
						File n42 = null;
						File n43 = null;

						// Array of emailIds where you want to
						// sent
						String[] toArr = new String[1];
						toArr[0] = recv_mail;
						m.setTo(toArr);

						// Your emailid(from)
						m.setFrom(sender);
						m.setSubject("SAM III PeakAbout - Event Data (" + m_EventData.mLocation + ")");
						m.setBody("From " + m_EventData.mInstrument_Name + "(" + m_EventData.mLocation+ ")" + "\nAppVersion : PeakAbout III " + appver);
						
						/*
						 * try { exchange = new Exchange(server, sender, sender_pw, null, false); }
						 * catch (Exception e) { NcLibrary.Write_ExceptionLog(e); mPrgDlg.dismiss();
						 * mHandler.sendEmptyMessage(2); return; }
						 */
						try {
							boolean n42FileTemp = true;
							if (m_EventData != null) 
							{
								String name = pref.Get_Last_Cntd_User();
							//	String path = NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml", IDSpectrumMode,name);
								m_EventData.reachBackXml  = NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml", IDSpectrumMode,name);
								if(!m_EventData.reachBackXml.equals(""))
								{
									n42 = new File(m_EventData.reachBackXml);
									if (n42 != null && n42.exists())
									{
										Long size = n42.length();
										if(size !=0)
										{
											m.addAttachment(m_EventData.reachBackXml, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml" );
											n42FileTemp = false;
											Log.e("ahn", "File Size :"+ size);
										}
											
									}
										
								}
								/*
								 *  n42FileTemp 기본값 true
								 *  n42 파일이 정상적으로 생성됐을대 false로 변함
								 *  파일이 없거나 size가 0일때 메일전송안되게 처리
								 */
								if(n42FileTemp)
								{
									NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3809 file == null");
									mPrgDlg.dismiss();
									mHandler.sendEmptyMessage(4);
									return;
								}
								

								if (IsPhotoSave) 
								{
									isPhoto = false;
								//	String FileName2 = Media.FolderPath + "/" + m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png";
									m_EventData.reachBackPic = Media.FolderPath + "/" + m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png";
									n43 = new File(m_EventData.reachBackPic);
									if (n43.exists()) 
									{
										m.addAttachment(m_EventData.reachBackPic ,m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png" );
										//db.Update_PhotoFileNames(Path,  m_EventData.Event_Number);

										isPhoto = true;
									} 
									else 
									{
										isPhoto = false;
									}

								}

							} else {
								NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3837 m_EventData == null");
								mPrgDlg.dismiss();
								mHandler.sendEmptyMessage(4);
								return;
							}

						} catch (IOException e) {
							NcLibrary.Write_ExceptionLog(e);
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(4);
							e.printStackTrace();
							return;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
						if (m.send()) {
							// Toast.makeText(mContext,, Toast.LENGTH_LONG).show();

							mPrgDlg.dismiss();
							msg = "전송완료되었습니다";
							Log.v("Forgot Password mail", "Success");

							if (isPhoto)
								mHandler.sendEmptyMessage(0);
							else
								mHandler.sendEmptyMessage(5);
							
							n42.delete();
					/*		if (n43 != null && n43.exists())
								n43.delete();*/
							
						} 
						else 
						{
							mPrgDlg.dismiss();
							msg = "전송이 실패하였습니다";
							Log.v("Forgot Password mail", "Not Success");
						}
						}
						catch (Exception e) {
							NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3213 Exception");
							NcLibrary.Write_ExceptionLog(e);
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(1);
						}

						
					
					}

				};

				thread.start();

				// Email 형식으로 소스코드 변경 종료

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}} catch (Exception e) {

			NcLibrary.Write_ExceptionLog(e);
		}
	}


	//EventList에서 전송
		public static void SendEmail_image(final EventData m_EventData, final Context mContext, final Handler mHandler,
				final boolean IsPhotoSave, final boolean IDSpectrumMode, final String Path) {
			try {

				final ProgressDialog mPrgDlg;
				mPrgDlg = new ProgressDialog(mContext);
				mPrgDlg.setTitle(mContext.getResources().getString(R.string.transmit_N42));
				mPrgDlg.setMessage("Sending...");
				mPrgDlg.setIndeterminate(true);
				mPrgDlg.setCancelable(false);
				final String appver = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
				final EventDBOper db = new EventDBOper();
				mPrgDlg.show();

				try {
					Thread thread = new Thread() {
						@Override
						public void run() {

							super.run();

							/*
							 * KINS200 유심사용가능 wifi, 유심 network 둘다 체크 180103
							 */
							if (isNetworkOnline(mContext) == false) {
								mPrgDlg.dismiss();
								mHandler.sendEmptyMessage(3);
								return;
							}

							PreferenceDB pref = new PreferenceDB(mContext);
							/*
							 * if (pref.Get_sender_email() == "" | pref.Get_sender_email() == null) {
							 * //mHandler.sendEmptyMessage(2); //mPrgDlg.dismiss(); // return;
							 * 
							 * }
							 */

						/*	
							// Email 형식으로 소스코드 변경 시작

							if ((sender == null || sender.length() <= 1) || (sender_pw == null || sender_pw.length() <= 1)
									|| (recv_mail == null || recv_mail.length() <= 1)
									|| (server == null || server.length() <= 1)) {
								sender = "kinsrpm@kins.re.kr";
								sender_pw = "rpm0356##";
								recv_mail = "kinsrpm@kins.re.kr";
								server = "mail.kins.re.kr";

								pref.Set_sender_Server("mail.kins.re.kr");
								pref.Set_sender_Port("587");
								pref.Set_sender_pw("rpm0356##");
								pref.Set_sender_email("kinsrpm@kins.re.kr");
								pref.Set_recv_address("kinsrpm@kins.re.kr");

							}*/
							
							String sender = pref.Get_sender_email();
							String sender_pw = pref.Get_sender_pw();
							String recv_mail = pref.Get_recv_email();
							String sender_server = pref.Get_sender_Server();
							String sender_port = pref.Get_sender_Port();
							
							//db.OpenDB();
							final Mail m = new Mail(sender, sender_pw, sender_server, sender_port);
							File n42 = null;
							File n43 = null;

							// Array of emailIds where you want to
							// sent
							String[] toArr = new String[1];
							toArr[0] = recv_mail;
							m.setTo(toArr);

							// Your emailid(from)
							m.setFrom(sender);
							m.setSubject("SAM III PeakAbout - Event Data (" + m_EventData.mLocation + ")");
							m.setBody("From " + m_EventData.mInstrument_Name + "(" + m_EventData.mLocation+ ")" + "\nAppVersion : PeakAbout III " + appver);
							
							/*
							 * try { exchange = new Exchange(server, sender, sender_pw, null, false); }
							 * catch (Exception e) { NcLibrary.Write_ExceptionLog(e); mPrgDlg.dismiss();
							 * mHandler.sendEmptyMessage(2); return; }
							 */
							try {
								boolean n42FileTemp = true;
								if (m_EventData != null) 
								{
									String name = pref.Get_Last_Cntd_User();
									String path = NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml", IDSpectrumMode,name);
									if(!path.equals(""))
									{
										n42 = new File(path);
										if (n42 != null && n42.exists())
										{
											Long size = n42.length();
											if(size !=0)
											{
												m.addAttachment(path, m_EventData.Event_Date+ "_" + m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml" );
												n42FileTemp = false;
												Log.e("ahn", "File Size :"+ size);
											}
												
										}
											
									}
									/*
									 *  n42FileTemp 기본값 true
									 *  n42 파일이 정상적으로 생성됐을대 false로 변함
									 *  파일이 없거나 size가 0일때 메일전송안되게 처리
									 */
									if(n42FileTemp)
									{
										NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3809 file == null");
										mPrgDlg.dismiss();
										mHandler.sendEmptyMessage(4);
										return;
									}
									

									if (IsPhotoSave) 
									{
										isPhoto = false;
										//String FileName2 = Media.FolderPath + "/" + m_EventData.Event_Date + "_" + m_EventData.StartTime + ".png";
										String FileName2 = Media.FolderPath + "/" + Path + ".png";
										n43 = new File(FileName2);
										if (n43.exists()) 
										{
											//m.addAttachment(FileName2 ,Path + ".png" );
											db.Update_PhotoFileNames(Path,  m_EventData.Event_Number);

											isPhoto = true;
										} 
										else 
										{
											isPhoto = false;
										}

									}
									
									db.OpenDB();
									Vector<String> mPhoto = db.Get_PhotoFileName(m_EventData.Event_Number - 1);
									Vector<String> mPhotoName = new Vector<String>();
									if(mPhoto!=null && mPhoto.size() != 0)
									{
										for (int i = 0; i < mPhoto.size(); i++) {

											mPhotoName.add(mPhoto.get(i));
											mPhoto.set(i, Media.FolderPath + "/" + mPhoto.get(i) + ".png");

											File file;
											file = new File(mPhoto.get(i));
											if (file.exists()) 
											{

												m.addAttachment(mPhoto.get(i) , mPhotoName.get(i) + ".png" );
											}
										}
									}
									
									
									db.EndDB();
										

								} else {
									NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3837 m_EventData == null");
									mPrgDlg.dismiss();
									mHandler.sendEmptyMessage(4);
									return;
								}

							} catch (IOException e) {
								NcLibrary.Write_ExceptionLog(e);
								mPrgDlg.dismiss();
								mHandler.sendEmptyMessage(4);
								e.printStackTrace();
								return;
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							try {
							if (m.send()) {
								// Toast.makeText(mContext,, Toast.LENGTH_LONG).show();

								mPrgDlg.dismiss();
								msg = "전송완료되었습니다";
								Log.v("Forgot Password mail", "Success");

								if (isPhoto)
									mHandler.sendEmptyMessage(0);
								else
									mHandler.sendEmptyMessage(5);
								
								n42.delete();
						/*		if (n43 != null && n43.exists())
									n43.delete();*/
								
							} 
							else 
							{
								mPrgDlg.dismiss();
								msg = "전송이 실패하였습니다";
								Log.v("Forgot Password mail", "Not Success");
							}
							}
							catch (Exception e) {
								NcLibrary.Write_ExceptionLog("\nNcLibrary Line 3213 Exception");
								NcLibrary.Write_ExceptionLog(e);
								mPrgDlg.dismiss();
								mHandler.sendEmptyMessage(1);
							}

							
						
						}

					};

					thread.start();

					// Email 형식으로 소스코드 변경 종료

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}} catch (Exception e) {

				NcLibrary.Write_ExceptionLog(e);
			}
		}

	

	

	private static String GetCsvPath() {

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EventDB.csv";

		return path;
	}

	public static String CurrentDate() {
		Date date = new Date();
		Format formatter;
		formatter = new SimpleDateFormat("dd-MM-yyyy"); // d는 day 이런식
		String mDateStr = formatter.format(date);

		return mDateStr;
	}

	public static void Set_DebugModeEmail(PreferenceDB mPrefDB) {

		mPrefDB.Set_sender_Server("mail.kins.re.kr");
		mPrefDB.Set_sender_Port("587");
		mPrefDB.Set_sender_pw("rpm0356##");
		mPrefDB.Set_sender_email("kinsrpm@kins.re.kr");
		mPrefDB.Set_recv_address("inseon.ahn@nucaremed.com");

	}

	// 와이파이체크
	public static boolean isNetworkOnline(Context context) {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
		return status;

	}

	public static void DebugModeSettingMail(PreferenceDB mPrefDB) {
		// String abcde = mPrefDB.Get_sender_email();
		// if (abcde == null) {
		mPrefDB.Set_sender_Server("mail.nucaremed.com");
		mPrefDB.Set_sender_Port("587");
		mPrefDB.Set_sender_pw("dksdlstjs233");
		mPrefDB.Set_sender_email("inseon.ahn@nucaremed.com");
		mPrefDB.Set_recv_address("inseon.ahn@nucaremed.com");
		// }
	}


	public static void SaveText(String data) 
	{

			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String Today = (new SimpleDateFormat("MM-dd_HH:mm:ss").format(date));

			File sdcard = Environment.getExternalStorageDirectory();
			File dbpath = new File(sdcard.getAbsolutePath());
			String dbfile = dbpath.getAbsolutePath() + File.separator + "GS200.txt";

			Today =Today+"  "+ data + "  ";
			try {
				FileOutputStream fos = new FileOutputStream(dbfile, true);
				fos.write(Today.getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


	//	return true;
	}

	public static void SaveText1(String fileName, String data)
	{

		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());
		String dbfile = dbpath.getAbsolutePath() + File.separator + fileName;

		try {
			FileOutputStream fos = new FileOutputStream(dbfile, true);
			fos.write(data.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public static double CalcROIK40(int[] Spec, double []FWHM, double [] Coeff)
	{
		//Define Peak K40
		int PeakK40 = -1;

		//Step 0: Get Spectrum information
		double []ChSpec = new double[CHSIZE];
		double []ChSpecSmoo = new double[CHSIZE];
	
		for (int  i = 0; i < CHSIZE; i++)
		{
			ChSpec[i] = Spec[i];
			ChSpecSmoo[i] = 0;			
		}

		// Define ROI for K40
		double En_theshold1 = K40Peak * 0.8;
		double En_thshold2 = K40Peak * 1.2;

		double theshold1 = Math.round(EntoCh_Cali(En_theshold1, Coeff));
		double theshold2 = Math.round(EntoCh_Cali(En_thshold2, Coeff));


		// Step 1: Adaptive Filtering for orignal Spectrum	
		ChSpecSmoo = AdaptFilter(ChSpec, ChSpecSmoo, FWHM, 3);
		
		double SumCnt=0;
		
		for (int  i = (int) theshold1; i <=(int) theshold2; i++)
		{
			SumCnt=SumCnt+ChSpecSmoo[i];
		}

		return SumCnt;

	}
	
	/*..........................
	 * Hung.18.03.05
	 * Added Code to new algorithm
	 */
	
	public static int FindMaxValue(double[] Smoothed_ChArray, int ROI_Start, int ROI_End) {

		double mTemp = 0;
		double mMax = 0;
		int mCh = 0;
		// double[] mTemp = new double[ROI_End - ROI_Start];

		for (int i = ROI_Start; i < ROI_End; i++) {

			mTemp = Smoothed_ChArray[i];

			if (mMax < mTemp) {

				mCh = i;
				mMax = mTemp;
			}

		}

		return mCh;

	}
	
	public static String get_LIB_FilePath(String FileName) {
		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_LIB_FOLDER);
		if (!dbpath.exists()) {
			dbpath.mkdirs();
		}

		String dbfile = dbpath.getAbsolutePath() + File.separator + FileName;
		return dbfile;

	}


	// save calibration infromation
	public static void SaveTextCali1(String data, String filename)
	{

		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		//String Today = (new SimpleDateFormat("(yyyy-MM-dd_HH:mm:ss)").format(date));

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath());
		String dbfile = dbpath.getAbsolutePath() + File.separator + filename;

		//Today = data + "  ";
		try
		{

			FileWriter fileWriter=new FileWriter(dbfile);

			BufferedWriter bufWriter= new BufferedWriter(fileWriter);
			if(bufWriter != null)
			{
				bufWriter.write(data+"\n");
			}
			bufWriter.close();
			fileWriter.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}




	// save calibration infromation
	public static void SaveTextCali(int[] data, String filename, int NumData) 
	{

			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String Today = (new SimpleDateFormat("(yyyy-MM-dd_HH:mm:ss)").format(date));

			File sdcard = Environment.getExternalStorageDirectory();
			File dbpath = new File(sdcard.getAbsolutePath());
			String dbfile = dbpath.getAbsolutePath() + File.separator + filename;

			Today = data + "  ";
			try {
//				FileOutputStream fos = new FileOutputStream(dbfile, false);
//				fos.write(Today.getBytes());
//				
//				fos.close();
				
				if(data[0]<0)
					return;
				
				FileWriter fileWriter=new FileWriter(dbfile);
				
				BufferedWriter bufWriter= new BufferedWriter(fileWriter);
				if(bufWriter != null)
				{
					for(int i=0;i<NumData;i++)
					{	bufWriter.write(String.valueOf(data[i])+"\n");
					
					}
				}
				bufWriter.close();
				fileWriter.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


	}
	
	// get clibration information from file
	public static int[] GetTextCli(String filename, int NumData ) 
	{

			Calendar calendar = Calendar.getInstance();
			Date date = calendar.getTime();
			String Today = (new SimpleDateFormat("(yyyy-MM-dd_HH:mm:ss)").format(date));

			File sdcard = Environment.getExternalStorageDirectory();
			File dbpath = new File(sdcard.getAbsolutePath());
			String dbfile = dbpath.getAbsolutePath() + File.separator + filename;

			String line="";
			//Today = data + "  ";
			
			int[] caliCh= new int[NumData];
			for(int i=0;i<NumData;i++)
				caliCh[i]=0;
			try {
				//FileInputStream fos = new FileInputStream(dbfile);
				FileReader fileReader=new FileReader(dbfile);
				
				BufferedReader bufReader= new BufferedReader(fileReader);
				
				if(bufReader != null)
				{
					int count=0;
					while((line = bufReader.readLine())!= null || count<3)
					{	
						caliCh[count]=Integer.parseInt(line);
						count ++;
					}
						
				}
				
				fileReader.close();
				bufReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				caliCh[0]=0;
			} catch (IOException e) {
				e.printStackTrace();
				caliCh[0]=0;
			}

		return caliCh;
	}

	public static void Show_Dlg1(String Message, Context context)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Dialog));
		dialogBuilder.setMessage(Message);
		dialogBuilder.setNegativeButton(context.getResources().getString(R.string.check), null);
		dialogBuilder.setCancelable(false);
		//dialogBuilder.show();

		//	dialogBuilder.show();
		AlertDialog dialog = dialogBuilder.show();
		TextView msgView = (TextView) dialog.findViewById(android.R.id.message);
		msgView.setTextSize(15);
	}

	//N42 파일 전송
	public static void showReachBackDlg(final Context context, final int id, final int photoSize, final boolean evetList)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(context.getResources().getString(R.string.transmit_N42));
		dialogBuilder.setMessage(context.getResources().getString(R.string.send_toRCBCenter_event1)+"\n"+ context.getResources().getString(R.string.event_log)+"(#"+id+")"+context.getResources().getString(R.string.send_toRCBCenter_event2)+"\n"
				+context.getResources().getString(R.string.receive_email_address)+" "+MainActivity.mPrefDB.Get_recv_email());
		dialogBuilder.setPositiveButton(context.getResources().getString(R.string.transmit), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
				dialogBuilder.setTitle(context.getResources().getString(R.string.transmit_N42));
				dialogBuilder.setMessage(context.getResources().getString(R.string.msg_send_attach_pictures));
				dialogBuilder.setPositiveButton(context.getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton)
							{
								PhotoExcute((Activity)context,id,photoSize,true);
							}
						});
				dialogBuilder.setNegativeButton(context.getResources().getString(R.string.no),new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						EventData reachBack =  NcLibrary.SendEmail( false,context,  id,false);
						if(evetList)
						{
							if(reachBack!=null)
							{
								int idx = DBMng.GetInst(context).loadReahBackDB(reachBack.Event_Date, reachBack.StartTime);
								if (idx == -1)
								{
									DBMng.GetInst(context).writeReachBackDB(reachBack);
								}
								else
								{
									DBMng.GetInst(context).updateReachBack(idx, reachBack.reachBackPic, reachBack.reachBackXml, reachBack.reachBackSuccess+"");
								}
							}
						}
						else
						{
							//public static void SendEmail( final boolean IDSpectrumMode,final Context context , final  int _id)
							//180827 수정

							if (reachBack != null)
							{
								DBMng.GetInst(context).writeReachBackDB(reachBack);
							}
						}


					}
				});
				dialogBuilder.setCancelable(false);
				dialogBuilder.show();

			}

		});

		dialogBuilder.setNegativeButton(context.getResources().getString(R.string.cancel), null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();

	}

	public static void PhotoExcute(Activity activity, int _id, int photoSize, boolean reachBack)
	{

		String photoFile = "";
		//리치백->사진전송인지 IDspectrum -> 사진저장인지 판별
		if(reachBack)
		{
			photoFile =  "EventP"+_id + "_1";
/*			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File file = new File(Media.reachbackFolderPath, photoFile + ".png");
			if(file.exists())
			{
				//photoFile = photoFile+"_" + ".png";
				file.delete();
				file = new File(photoFile);
			}*/
			if(hashMap.get("photo")!=null)
			{
				hashMap.remove("photo");
			}
			hashMap.put("photo", photoFile);
			//cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));


			Intent intent = null;
			intent = new Intent(activity, Camera2Activity.class);
			intent.putExtra("file", photoFile);
			intent.putExtra("hint", activity.getResources().getString(R.string.camera_area));
			intent.putExtra("email", "T");
			//프레이밍 영역 (전체 밝은 영역)으로 전체 화면 사용 여부
			intent.putExtra("hideBounds", true);
			//최대 허용 카메라 크기 (픽셀 수)
			intent.putExtra("maxPicturePixels", 3840 * 2160);
			activity.startActivityForResult(intent, 1);
		}
		else
		{
			photoFile =  "EventP"+_id + "_1";
			if(photoSize  != 0)
			{
				photoFile = "EventP"+_id + "_" + (photoSize+1);
			}

			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File file = new File(Media.FolderPath, photoFile + ".png");
			if(file.exists())
			{
				photoFile = photoFile+"_" + ".png";
				file = new File(photoFile);
			}
			if(hashMap.get("photo")!=null)
			{
				hashMap.remove("photo");
			}
			hashMap.put("photo", photoFile);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			//activity.startActivityForResult(cameraIntent, 4);
			Intent intent = null;
			intent = new Intent(activity, Camera2Activity.class);
			intent.putExtra("file", file.getAbsolutePath());
			intent.putExtra("hint", activity.getResources().getString(R.string.camera_area));
			intent.putExtra("email", "F");
			//프레이밍 영역 (전체 밝은 영역)으로 전체 화면 사용 여부
			intent.putExtra("hideBounds", true);
			//최대 허용 카메라 크기 (픽셀 수)
			intent.putExtra("maxPicturePixels", 3840 * 2160);
			activity.startActivityForResult(intent, 4);
		}

	}


}
