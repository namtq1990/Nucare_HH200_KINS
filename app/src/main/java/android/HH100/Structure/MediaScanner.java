package android.HH100.Structure;

import java.io.File;
import java.io.FilenameFilter;

import android.HH100.DB.EventDBOper;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class MediaScanner {

	private String TAG = "Media Scanner";
	private boolean mWasMediaScaned = false;
	private Context mSuper;
	private MediaScannerConnection msc = null;

	public MediaScanner(Context context) {
		mSuper = context;
		msc = new MediaScannerConnection(mSuper, mScanClient);
	}

	private MediaScannerConnectionClient mScanClient = new MediaScannerConnectionClient() {

		public void onMediaScannerConnected() {
			Log.i(TAG, "onMediaScannerConnected");
			File file = new File(
					Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + EventDBOper.DB_FOLDER); // �쇅�옣
																												// �뵒�젆�넗由�
																												// 媛��졇�샂

			File[] fileNames = file.listFiles(new FilenameFilter() { // �듅�젙
																		// �솗�옣�옄留�
																		// 媛�吏�
																		// �뙆�씪�뱾�쓣
																		// �븘�꽣留곹븿
				public boolean accept(File dir, String name) {
					return true;
				}
			});

			if (fileNames != null) {
				for (int i = 0; i < fileNames.length; i++) // �뙆�씪 媛��닔 留뚰겮
															// scanFile�쓣 �샇異쒗븿
				{
					msc.scanFile(fileNames[i].getAbsolutePath(), null);
				}
			}

			//// isotope library
			File file2 = new File(
					Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + EventDBOper.DB_LIB_FOLDER); // �쇅�옣
																													// �뵒�젆�넗由�
																													// 媛��졇�샂

			File[] fileNames2 = file2.listFiles(new FilenameFilter() { // �듅�젙
																		// �솗�옣�옄留�
																		// 媛�吏�
																		// �뙆�씪�뱾�쓣
																		// �븘�꽣留곹븿
				public boolean accept(File dir, String name) {
					return true;
				}
			});

			if (fileNames2 != null) {
				for (int i = 0; i < fileNames2.length; i++) // �뙆�씪 媛��닔 留뚰겮
															// scanFile�쓣 �샇異쒗븿
				{
					msc.scanFile(fileNames2[i].getAbsolutePath(), null);
				}
			}
			mWasMediaScaned = true;
		}

		public void onScanCompleted(String path, Uri uri) {
			// Log.i(TAG, "onScanCompleted(" + path + ", " + uri.toString() +
			// ")"); // �뒪罹먮떇�븳 �젙蹂대�� 異쒕젰�빐遊�

			msc.disconnect();
		}
	};

	public boolean Start_MediaScan() {
		if (msc != null) {
			if (msc.isConnected()) {
				msc.disconnect();
				return false;
			} else {
				msc.connect();
				return true;
			}
		}

		return false;
	}

}
