package android.HH100;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.HH100.DB.PreferenceDB;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Element;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.widget.Toast;

//PostDataRun
//onPageFinished1
//FindAccessToken
public class RadresponderActivity extends Activity {

	public final static String CLIENT_ID = "25c7f2b4-ea90-4177-b333-73524920da3e";
	public final static String CLIENT_SECRET = "Ix7kf9aCMIyvqPnfw2uuN408GJnF9Pc4aR7anhwH";
	public final static String REQUEST_TOKEN_URL = "https://api.radresponder.net/oauth/request_token";
	public final static String AUTHORIZE_URL = "https://api.radresponder.net/oauth/authorize";
	public final static String ACCESS_TOKEN_URL = "https://api.radresponder.net/oauth/token";
	public final static String REDIRECT_URI = "http://localhost:51029/implicitgrant/callback";
	public final static String SCOPE = "create_field_survey get_accessible_events create_spectrum";
	public final static String STATE = "my-nonce";

	// ����Ʈ�����ͺκм���

	public final static String POST_DATA_URL = "https://api.radresponder.net/api/v1/fieldsurvey";
	public final static String POST_DATA_HEADER = "Authorization";
	public final static String POST_DATA_METHOD = "Bearer ";
	public final static String COMMUNICATIONS_METHOD = "application/json";
	public final static String COMMUNICATIONS_ENCODE = "UTF-8";

	public static final String url = AUTHORIZE_URL + "?scope=" + SCOPE + "&state=" + STATE + "&redirect_uri="
			+ REDIRECT_URI + "&response_type=code&client_id=" + CLIENT_ID;
	public final static String EVENTID_TITLE = "eventId";

	public static final String GPS_LAT = "GPS_Latitude";
	public static final String GPS_LONG = "GPS_Longitude";
	public static final String DOSERATE_TYPE = "Doserate_AVGs";
	public static final String COLLECTION_DATE = "Date";
	public static final String COLLECTDATE_TITLE = "collectionDate";
	public static final String LATITUDE_TITLE = "latitude";
	public static final String LONGITUDE_TITLE = "longitude";
	public static final String COORDINATE_TITLE = "coordinate";
	public static final String RADIATIONTYPE_TITLE = "radiationType";
	public static final String RADIATIONUNIT_TITLE = "radiationUnit";
	public static final String VALUE_TITLE = "value";
	public static final String HEIGHTUNIT_TITLE = "heightUnit";
	public static final String HEIGHT_TITLE = "height";
	public static final String ORIENTATION_TITLE = "orientation";
	public static final String ISWINDOWOPEN_TITLE = "isWindowOpen";
	public static final String COMMENT_TITLE = "comment";
	public static final String FIELDSURVEYS_TITLE = "fieldSurveys";

	public static final String RADIATIONTYPE_SUBSTANCE = "gamma";
	public static final String RADIATIONUNIT_SUBSTANCE = "uSv/h";
	public static final String HEIGHTUNIT_SUBSTANCE = "meter";
	public static final String ORIENTATION_SUBSTANCE = "Up";
	public static final String ISWINDOWOPEN_SUBSTANCE = "yes";
	public static String COMMENT_SUBSTANCE = "Test Survey";

	public static final String STARTTIME_TITLE = "startTime";
	public static final String STOPTIME_TITLE = "stopTime";
	public static final String DWELLTIME_TITLE = "dwellTime";
	public static final String ISBACKGROUND_TITLE = "isBackground";

	public static final String SERIALNUMBER_TITLE = "serialNumber";

	public static final String EQUIPMENT_TITLE = "equipment";

	public static final String NUCLIDETYPE_TITLE = "nuclideType";

	public static final String CONFIDENCE_TITLE = "confidence";

	public static final String ISOTOPES_TITLE = "isotopes";

	public static final String START_TIME = "Start_Time";

	public static final String START_TIME_NOT_UTC = "Start_Time_not_utc";

	public static final String STOP_TIME_NOT_UTC = "Stop_Time_not_utc";

	public static final String LEVEL_S = "Level_S";
	public static final String SOURCE_NAME_S = "Source_Name_S";
	public static final String DOSERATE_S = "DoseRate_S";

	public static final int HEIGHT_SUBSTANCE = 1;

	public static final String METER_TITLE = "meter";
	public static final String PROBE_TITLE = "probe";
	public static final String EXPOSURES_TITLE = "exposures";
	public static final String SPECTRA_TITLE = "spectra";
	Thread thread;
	WebView mWebView;

	String access_TokenKey, GUrl, receiveData;

	int switchInt = 0;

	PostDataThread PostDataThread;
	GetDataThread GetDataThread;

	Context mContext;
	String response;
	ProgressDialog mPrgDlg;
	boolean submitBtnClick = true, TransportSuccess = false, runOneClick = false;

	ArrayList<String> IDArray = new ArrayList<String>();
	ArrayList<String> nameArray = new ArrayList<String>();

	String eventID = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.radresponder_auth);

		setLayout();

		mContext = this;
		boolean wificheck1 = wificheck();
		if (wificheck1 == false) {

			Toast.makeText(mContext, getString(R.string.rad_response_wifi_disconnect), 0).show();

			finish();

		}

		PostDataThread = new PostDataThread();

		GetDataThread = new GetDataThread();

		mPrgDlg = new ProgressDialog(mContext);
		mPrgDlg.setIndeterminate(true);
		mPrgDlg.setCancelable(false);

		PreferenceDB prefDB = new PreferenceDB(getApplicationContext());

		String abcd = prefDB.Get_String_From_pref(getString(R.string.rad_response_used_not_key));
		String abcde = getString(R.string.rad_response_eventlist_key);

		mWebView.setVisibility(View.VISIBLE);

		tumblrWebView(AUTHORIZE_URL + "?scope=" + SCOPE + "&state=" + STATE + "&redirect_uri=" + REDIRECT_URI
				+ "&response_type=token&client_id=" + CLIENT_ID);

	}

	private class WebViewClientClass extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			view.loadUrl(url);

			return true;
		}

		public void onPageFinished(WebView view, String url) {

			boolean wificheck1 = wificheck();
			if (wificheck1 == false) {

				view.setVisibility(View.GONE);
				Toast.makeText(mContext, getString(R.string.rad_response_wifi_disconnect), 0).show();

				finish();

			}

			Uri uri = Uri.parse(url);
			GUrl = url;

			String abc = null;

			System.out.println(view);
			// FindAccessToken
			if (url.contains("#")) {
				String[] aa = url.split("#");

				String[] ab = aa[1].split("=");

				String[] ac = ab[1].split("&");
				abc = ac[0];
				access_TokenKey = abc;
			}

			if (GUrl.contains("access_token")) {

				mWebView.setVisibility(View.GONE);

				mPrgDlg.setMessage(getString(R.string.rad_response_data_transmitting));
				mPrgDlg.show();
				decreaseBar();
				if (submitBtnClick == true) {

					// PostDataThread.start();
					GetDataThread.start();
					submitBtnClick = false;
				}

			}

			thread = new Thread(mScrollDown);

			thread.start();

			TimerTask mTask = new TimerTask() {

				@Override
				public void run() {
					thread.interrupted();
					mWebView.removeCallbacks(mScrollDown);
				}
			};

			Timer mTimer = new Timer();
			mTimer.schedule(mTask, 1000);

			// ScrollDonw();
			super.onPageFinished(view, url);
		}

	}

	private void setLayout() {

		mWebView = (WebView) findViewById(R.id.webview);
	}

	public void tumblrWebView(String authUrl) {

		try {
			URLEncoder.encode(authUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ���信�� �ڹٽ�ũ��Ʈ���డ��
		mWebView.getSettings().setJavaScriptEnabled(true);
		// ����Ȩ������ ����
		mWebView.loadUrl(authUrl);
		// WebViewClient ����
		mWebView.setWebViewClient(new WebViewClientClass());

	}

	public class PostDataThread extends Thread {

		public void run() {

			PUT("");

		}

		@SuppressWarnings("deprecation")
		public String PUT(String url) {

			int EVENTID_SUBSTANCE = Integer.parseInt(eventID);

			Intent intent = getIntent();

			String GPS_Latitude = intent.getStringExtra(GPS_LAT);
			String GPS_Longitude = intent.getStringExtra(GPS_LONG);
			String Doserate_AVGs = intent.getStringExtra(DOSERATE_TYPE);
			String Date = intent.getStringExtra(COLLECTION_DATE) + " -0500";

			String Start_Time = intent.getStringExtra(START_TIME) + " -0500";

			String End_Time = intent.getStringExtra(COLLECTION_DATE) + " -0500";;

			COMMENT_SUBSTANCE = intent.getStringExtra(COMMENT_TITLE);

			String[] Source_Names = intent.getStringExtra(SOURCE_NAME_S).split(",");

			String[] Doserate_S = intent.getStringExtra(DOSERATE_S).split(",");

			String[] Level_S = intent.getStringExtra(LEVEL_S).split(",");

			System.out.println("Doserate_AVGs :" + Doserate_AVGs);

			// final Map<String, String> mParams1 = new HashMap<String,
			// String>();

			final Map<String, String> mParams1 = new HashMap<String, String>();

			JSONObject root = new JSONObject();
			JSONObject root2 = new JSONObject();
			JSONObject address_sub = new JSONObject();
			JSONObject coordinate_sub = new JSONObject();

			JSONObject equipment_sub = new JSONObject();

			JSONObject list5 = new JSONObject();

			JSONArray root_sup = new JSONArray();

			JSONArray isotopes_sup = new JSONArray();

			JSONArray exposures_sup = new JSONArray();

			try {

				root2.put(EVENTID_TITLE, EVENTID_SUBSTANCE);
				root2.put(COLLECTDATE_TITLE, Date);

				// start_root.put("address", null);

				coordinate_sub.put(LATITUDE_TITLE, GPS_Latitude);
				coordinate_sub.put(LONGITUDE_TITLE, GPS_Longitude);

				root2.put(COORDINATE_TITLE, coordinate_sub);

				root2.put(STARTTIME_TITLE, Start_Time);

				root2.put(STOPTIME_TITLE, End_Time);

				root2.put(HEIGHTUNIT_TITLE, "meter");

				root2.put(HEIGHT_TITLE, 1);

				root2.put(DWELLTIME_TITLE, 460);

				root2.put(ISBACKGROUND_TITLE, false);

				root2.put(COMMENT_TITLE, COMMENT_SUBSTANCE);

				equipment_sub.put(SERIALNUMBER_TITLE, "21001");

				root2.put(EQUIPMENT_TITLE, equipment_sub);

				if (!Source_Names[0].equals("None")) {

					for (int i = 0; i < Source_Names.length; i++) {
						if (!Source_Names[i].equals("Unknown")) {

							JSONObject isotopes_sub = new JSONObject();
							isotopes_sub.put(NUCLIDETYPE_TITLE, Source_Names[i]);
							isotopes_sub.put(CONFIDENCE_TITLE, Level_S[i]);
							isotopes_sup.put(isotopes_sub);
						}

					}
				}
				root2.put(ISOTOPES_TITLE, isotopes_sup);
				if (!Source_Names[0].equals("None")) {
					for (int i = 0; i < Source_Names.length; i++) {
						if (!Source_Names[i].equals("Unknown")) {
							JSONObject exposures_sub = new JSONObject();
							JSONObject meter_sub = new JSONObject();
							JSONObject probe_sub = new JSONObject();
							exposures_sub.put(RADIATIONTYPE_TITLE, RADIATIONTYPE_SUBSTANCE);
							exposures_sub.put(RADIATIONUNIT_TITLE, RADIATIONUNIT_SUBSTANCE);
							exposures_sub.put(HEIGHT_TITLE, 3);
							exposures_sub.put(HEIGHTUNIT_TITLE, "in");
							exposures_sub.put(VALUE_TITLE, Double.valueOf(Doserate_S[i]).doubleValue());
							exposures_sub.put(ORIENTATION_TITLE, ORIENTATION_SUBSTANCE);
							exposures_sub.put(COMMENT_TITLE, "");
							meter_sub.put(SERIALNUMBER_TITLE, "297954");
							exposures_sub.put(METER_TITLE, meter_sub);
							probe_sub.put(SERIALNUMBER_TITLE, "PR345824");
							exposures_sub.put(PROBE_TITLE, probe_sub);
							exposures_sup.put(exposures_sub);
						}
					}
				}
				root2.put(EXPOSURES_TITLE, exposures_sup);

				root_sup.put(root2);

				root.put(SPECTRA_TITLE, root_sup);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			InputStream inputStream = null;
			String result = "";

			StringRequestEntity requestEntity = null;
			try {
				requestEntity = new StringRequestEntity(root.toString(), "application/json", "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			HttpClient client = new HttpClient();

			HttpPost request = new HttpPost("https://api.radresponder.net/api/v1/fieldsurvey");

			// Create a method instance.
			PostMethod method = new PostMethod("https://api.radresponder.net/api/v1/spectra");
			method.setRequestHeader("Authorization", "Bearer " + access_TokenKey);

			method.setRequestEntity(requestEntity);

			String body12 = "abc";
			System.out.println(root.toString());

			// method.setRequestHeader("content-type", "application/json");

			// method.equals(params);

			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));

			System.out.println(method);

			// Provide custom retry handler is necessary

			try {

				// Execute the method.
				int statusCode = client.executeMethod(method);

				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: " + method.getStatusLine());
				}

				// Read the response body.
				byte[] responseBody = method.getResponseBody();

				response = new String(responseBody);

				System.out.println("�����ȣ: " + new String(responseBody));

			} catch (HttpException e) {
				System.err.println("Fatal protocol violation: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Fatal transport error: " + e.getMessage());
				e.printStackTrace();
			} finally {
				// Release the connection.
				method.releaseConnection();
				handler.obtainMessage(12, response).sendToTarget();

			}

			return result;
		}

		// convert inputstream to String
		private String convertInputStreamToString(InputStream inputStream) throws IOException {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = "";
			String result = "";
			while ((line = bufferedReader.readLine()) != null)
				result += line;

			inputStream.close();
			return result;

		}

		private Handler handler = new Handler() {

			public void handleMessage(Message msg) {

				String a = (String) msg.obj;

				mPrgDlg.dismiss();
				if (a.equals("")) {
					Toast.makeText(mContext, getString(R.string.rad_response_submit_success), 2).show();

					finish();

				} else if (!a.equals("")) {

					Toast.makeText(mContext, a, 2).show();

					finish();

				}

				super.handleMessage(msg);

			}

		};

	}

	public class GetDataThread extends Thread {

		private static final String TAG = "ExampleThread2";
		private int n1 = 0;
		private int n2 = 0;

		String abcd;

		public void run() {

			GET("");

		}

		public String GET(String url) {

			InputStream inputStream = null;
			String result = "";

			HttpClient client = new HttpClient();

			// Create a method instance.
			GetMethod method = new GetMethod("https://api.radresponder.net/api/v1/event");

			System.out.println(method);
			method.setRequestHeader("Authorization", "Bearer " + access_TokenKey);
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));

			System.out.println(method);

			// Provide custom retry handler is necessary

			try {
				// Execute the method.
				int statusCode = client.executeMethod(method);

				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: " + method.getStatusLine());
				}

				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				String mResponseBodyStr;
				System.out.println(new String(responseBody));
				receiveData = new String(responseBody);

			} catch (HttpException e) {
				System.err.println("Fatal protocol violation: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Fatal transport error: " + e.getMessage());
				e.printStackTrace();
			} finally {
				// Release the connection.
				method.releaseConnection();
				if (receiveData.contains("name") || receiveData.contains("sponsorName") || receiveData.contains("id")
						|| receiveData.contains("endDate") || receiveData.contains("startDate")) {
					TransportSuccess = true;
					handler.sendEmptyMessage(0);

				} else {
					TransportSuccess = false;
					handler.sendEmptyMessage(0);
				}
				handler.sendEmptyMessage(0);

			}

			return result;
		}

		// convert inputstream to String
		private String convertInputStreamToString(InputStream inputStream) throws IOException {

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = "";
			String result = "";
			while ((line = bufferedReader.readLine()) != null)
				result += line;

			inputStream.close();
			return result;

		}

		private Handler handler = new Handler() {

			public void handleMessage(Message msg) {

				// mPrgDlg.dismiss();

				GetDataThread.interrupt();

				PreferenceDB prefDB = new PreferenceDB(getApplicationContext());
				String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventlist_key));

				if (abc.equals("0")) {
					eventID = split(receiveData, getString(R.string.rad_response_eventlist11));
				} else if (abc.equals("1")) {
					eventID = split(receiveData, getString(R.string.rad_response_eventlist22));
				} else if (abc.equals("2")) {
					eventID = split(receiveData, getString(R.string.rad_response_eventlist33));

				}

				/*
				 * try { GetDataThread.sleep(1500); PostDataThread.sleep(1500);
				 * } catch (InterruptedException e) { // TODO Auto-generated
				 * catch block e.printStackTrace(); }
				 */

				TimerTask task = new TimerTask() {

					public void run() {

						try {

							PostDataThread.start();
							// Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				if (TransportSuccess == true) {
					// Toast.makeText(mContext,
					// getString(R.string.rad_response_receive_success),
					// 2).show();
					PostDataThread = new PostDataThread();
					Timer mTimer = new Timer();
					mTimer.schedule(task, 1500);
					/* PostDataThread.start(); */

				} else if (TransportSuccess == false) {

					// Toast.makeText(mContext,
					// getString(R.string.rad_response_receive_failure),
					// 2).show();

					finish();

				}

				super.handleMessage(msg);

			}

		};
	}

	private String split(String data, String word) {

		String result = "";
		String[] TotalLengthArray;
		String[] firstSplitArray;
		String[] firstSplitArray2;

		firstSplitArray = data.split(word);
		String[] IDSplit = null;

		IDSplit = firstSplitArray[0].split("id");
		String IDSplit2 = null;
		IDSplit2 = IDSplit[IDSplit.length - 1];

		String[] IDSplit3 = null;
		IDSplit3 = IDSplit2.split(":");

		String[] IDSplit4 = null;

		IDSplit4 = IDSplit3[1].split(",");

		String IDSplit5 = null;

		IDSplit5 = IDSplit4[0];

		return IDSplit5;
	}

	public void decreaseBar() {
		runOnUiThread( // progressBar�� ui�� �ش��ϹǷ� runOnUiThread�� ��Ʈ���ؾ��Ѵ�
				new Runnable() { // thread������ ���������� Runnable�� ���ְ�

					@Override
					public void run() {

					}
				});
	}

	private boolean wificheck() {
		boolean bIsWiFiConnect = false;
		ConnectivityManager oManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo oInfo = oManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (oInfo != null && oInfo.isAvailable() && oInfo.isConnected())
			return true;
		return false;

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void ScrollDonw() {

	}

	// ����
	private Runnable mScrollDown = new Runnable()

	{
		public void run()

		{

			mWebView.scrollBy(0, 500);
			mWebView.postDelayed(this, 200);

		}
	};

}
