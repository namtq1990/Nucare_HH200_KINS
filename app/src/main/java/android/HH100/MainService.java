package android.HH100;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

import android.HH100.Control.*;
import android.HH100.R.string;
import android.HH100.Structure.Detector;
import android.HH100.Structure.GCData;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.util.Log;

public class MainService {
	// 濡쒓렇罹ｌ쓣 �쐞�븳
	private static final String TAG = "MainService";
	private static final String TAG_RECV_DATA = "MainService_recvData";
	public static final boolean D = MainActivity.D;

	private static final String NAME = "Kainac";
	private static final UUID MY_UUID2 = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // SPP

	private final BluetoothAdapter mAdapter;
	private final Handler mSuperHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	public static int mState;

	public static final int MESSAGE_READ_GAMMA = 21;
	public static final int MESSAGE_READ_NEUTRON = 22;
	public static final int MESSAGE_READ_GM = 23;
	public static final int MESSAGE_READ_LA = 24;
	public static final int MESSAGE_READ_BATTERY = 25;

	public static final int STATE_NONE = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;
	public static final int STATE_LOST = 4;

	public static final byte HEAD_PACKET_STANDARD = 'U';
	public static final byte TAIL_PACKET_STANDARD = (byte) 0x66;
	public static final byte PACKET0_BATTREY = (byte) 0x42;
	public static final byte PACKET0_BATTREY2 = (byte) 0x54;
	public static final byte PACKET1_STANDARD = (byte) 0x80;
	public static final byte PACKET2_STANDARD = (byte) 0x90;
	public static final byte PACKET3_STANDARD = (byte) 0xa0;
	public static final byte PACKET4_STANDARD = (byte) 0xb0;
	public static final byte PACKET5_STANDARD = (byte) 0xc0;
	public static final byte PACKET6_STANDARD = (byte) 0xd0; //181102추가
	public static final byte PACKET7_STANDARD = (byte) 0xe0;
	public static final byte PACKET8_STANDARD = (byte) 0xf0;
	public static final byte SERVER_PACKET_START = (byte) 0x80;
	public static final int STEP_ERROR = 500;
	public boolean PACKET_TRSF = false;

	public static final int NEUTRON_ACCUM_SEC = 30;
	public static boolean FIRST_PACKET = false;

	public int aad = 0;

	public int mBatteryCount = 50;
	
	Context mContext;

	// 하드웨어키 선언 부분

	public static final byte HARDWAREKEY_SHOTPRESS = (byte) 0x43;

	public static final byte HARDWAREKEY_LONGPRESS = (byte) 0x4c;

	public static final byte HARDWAREKEY_DOUBLECLICK = (byte) 0x44;

	public MainService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mSuperHandler = handler;
		mContext = context;
		StartReceiver();
	}
	
	public void StartReceiver() 
	{
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		mContext.registerReceiver(myUsbReceiver, intentFilter);
	}
	
	private BroadcastReceiver myUsbReceiver = new BroadcastReceiver() {

		@Override

		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) 
			{
				mSuperHandler.obtainMessage(MainActivity.MESSAGE_SHUTDOWN).sendToTarget();

			}
		}
	};

	private final Handler mServiceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_READ_GAMMA:

				break;

			}
		}
	};
	private Timer sender;

	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		mSuperHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	public synchronized int getState() {
		return mState;
	}

	public synchronized void start() {

		if (D)
			Log.d(TAG, "start");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(STATE_LISTEN);
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}

	}

	public synchronized void connect(BluetoothDevice device) {
		if (D) {
			/*
			 * Log.d(TAG, "connect to: " + device); mState = STATE_CONNECTED;
			 * //setState(STATE_CONNECTED);
			 * mSuperHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,
			 * STATE_CONNECTING, -1).sendToTarget();
			 * mSuperHandler.obtainMessage(MainActivity.
			 * MESSAGE_CONNECTED_DEVICE_INFO, 0, -1, device).sendToTarget();
			 * mSuperHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,
			 * STATE_CONNECTED, -1).sendToTarget();
			 * 
			 * sender = new Timer(true); sender.schedule( new TimerTask(){
			 * 
			 * @Override public void run(){ mServiceHandler.post(new Runnable(){
			 * public void run(){ int[] sdw = new int[1024]; for(int i=0;
			 * i<300;i++){ sdw[i] = 1; }
			 * mSuperHandler.obtainMessage(MainActivity.
			 * MESSAGE_READ_DETECTOR_DATA,0,0,sdw).sendToTarget(); } }); } },
			 * 1000, 1000 // 1占쎈／�뜝�룞�삕占쎈걠�굢占� 1占쎈／占쎈쐞壤쏅쪋�삕�뜝�띁爾몌옙裕뉑틦占� );
			 * return;
			 */
		}
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		mSuperHandler.obtainMessage(MainActivity.MESSAGE_CONNECTED_DEVICE_INFO, 0, -1, device).sendToTarget();
		setState(STATE_CONNECTED);
	}

	public synchronized void stop() {

		// MainActivity.SetDoubleConnectCheck(false);
		if (D)
			Log.d(TAG, "stop");
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		setState(STATE_NONE);
	}

	public boolean write(byte[] out) {

		ConnectedThread r;

		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return false;
			r = mConnectedThread;
		}
		return r.write(out);

	}

	private void connectionFailed() {

		setState(STATE_LISTEN);

		Message msg = mSuperHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.TOAST, "Unable to connect device");
		msg.setData(bundle);
		mSuperHandler.sendMessage(msg);
		mSuperHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, 3530, 3530).sendToTarget();
		// mHandler.obtainMessage(KainacActivity.MESSAGE_STATE_CHANGE,-1, -1,
		// pdata).sendToTarget();

	}

	private void connectionLost() {

		setState(STATE_LOST);

		PACKET_TRSF = false;

		Message msg = mSuperHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.TOAST, "Device connection was lost");
		msg.setData(bundle);
		mSuperHandler.sendMessage(msg);
	}

	private class AcceptThread extends Thread {

		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;

			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);

			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			mmServerSocket = tmp;
		}

		@Override
		public void run() {
			if (D)
				Log.d(TAG, "BEGIN mAcceptThread" + this);
			setName("AcceptThread");
			BluetoothSocket socket = null;

			while (mState != STATE_CONNECTED) {

				try {
					if (mmServerSocket == null)
						throw new IOException();
					socket = mmServerSocket.accept();

				} catch (IOException e) {
					NcLibrary.Write_ExceptionLog(e);
					break;
				}

				if (socket != null) {
					synchronized (MainService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							try {
								socket.close();
							} catch (IOException e) {
								NcLibrary.Write_ExceptionLog(e);
							}
							break;
						}
					}
				}
			}
			if (D)
				Log.i(TAG, "END mAcceptThread");
		}

		public void cancel() {
			if (D)
				Log.d(TAG, "cancel " + this);
			try {
				if (mmServerSocket != null)
					mmServerSocket.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	private class ConnectThread extends Thread {

		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;

			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			mmSocket = tmp;
		}

		@Override
		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			mAdapter.cancelDiscovery();

			try {
				mmSocket.connect();
			} catch (IOException e) {
				connectionFailed();
				NcLibrary.Write_ExceptionLog(e);
				try {
					mmSocket.close();
				} catch (IOException e2) {
					NcLibrary.Write_ExceptionLog(e2);
				}

				MainService.this.start();
				return;
			}

			synchronized (MainService.this) {
				mConnectThread = null;
			}

			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	private class ConnectedThread extends Thread {

		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		private Vector<int[]> mTemp_for_PacketData = new Vector<int[]>();
		byte[] m_CompletedRealData = new byte[3087];
		private Vector<Integer> mNeutron = new Vector<Integer>();
		private Vector<Integer> mGM = new Vector<Integer>();

		private Vector<Byte> mPacketHead = new Vector<Byte>();

		ArrayList<Integer> IsThereNeutronBit = new ArrayList<Integer>();

		public Spectrum MS = new Spectrum();

		public ConnectedThread(BluetoothSocket socket) {

			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			mmInStream.mark(200);

		}

		private int IsThereNeutron;
		private boolean misCheckedGC = true;

		@Override
		public void run() {

			Log.i(TAG, "BEGIN mConnectedThread");
			byte[] InputtedPacket = new byte[1];
			int Step = 0;


/*
			try {
				sleep(20);
			} catch (InterruptedException e1) {
				NcLibrary.Write_ExceptionLog(e1);
			}
*/
			int sleep = 20;
			int GC_Cnt = 0;
			int GcCheck_count = 0;
			while (true)
			{ // GC 諛쏄린
				try {


					mmInStream.read(InputtedPacket, 0, 1);
					GC_Cnt = (InputtedPacket[0] == HEAD_PACKET_STANDARD) ? GC_Cnt + 1 : 0;
					if (GC_Cnt == 2)
					{
						GC_Cnt = 0;
						GcCheck_count += 1;   
						byte[] GC_Head = new byte[2];
						mmInStream.read(GC_Head);
						if (GC_Head[0] == 'G' & GC_Head[1] == 'K') {
							
							GCData mGCData = new GCData();
							
							mmInStream.read(GC_Head);
							int temp1 = GC_Head[0] & 0xff; // unsigned 蹂��솚 諛⑸쾿
							int temp2 = GC_Head[1] & 0xff;
							int s = (char) temp1 * 0x100 + ((char) temp2);

							mGCData.GC = (char) temp1 * 0x100 + ((char) temp2);
							
							String logd = "U U G K";
							logd = logd.format("%s %02x %02x", logd, GC_Head[0], GC_Head[1]);

							mmInStream.read(GC_Head);
							temp1 = GC_Head[0] & 0xff; // unsigned 蹂��솚 諛⑸쾿
							temp2 = GC_Head[1] & 0xff;
							int s2 = (char) temp1 * 0x100 + ((char) temp2);
							
							mGCData.K40_Ch = (char) temp1 * 0x100 + ((char) temp2);

							logd = logd.format("%s %02x %02x", logd, GC_Head[0], GC_Head[1]);

							
							byte[] DetType = new byte[1];
							mmInStream.read(DetType);

							int DetType2 = (DetType[0] == HEAD_PACKET_STANDARD) ? Detector.HwPmtProperty_Code.NaI_3x3
									: (int) DetType[0]; // unsigned 蹂��솚 諛⑸쾿
							logd = logd.format("%s , Det Type : %c", logd, (char) DetType2);

							mGCData.DetType = (DetType[0] == HEAD_PACKET_STANDARD) ? Detector.HwPmtProperty_Code.NaI_3x3: (int) DetType[0];

							mmInStream.read(GC_Head);
							temp1 = GC_Head[0] & 0xff; // unsigned 蹂��솚 諛⑸쾿
							temp2 = GC_Head[1] & 0xff;
							mGCData.Cs137_Ch1 = (char) temp1 * 0x100 + ((char) temp2);

							mmInStream.read(GC_Head);
							temp1 = GC_Head[0] & 0xff; // unsigned 蹂��솚 諛⑸쾿
							temp2 = GC_Head[1] & 0xff;
							mGCData.Cs137_Ch2 = (char) temp1 * 0x100 + ((char) temp2);
							
							
							mSuperHandler.obtainMessage(MainActivity.MESSAGE_READ_GC, s, s2, mGCData).sendToTarget();

							
							if (D)
								Log.d("GC", logd);
							break;
						} else
						{
							if (GcCheck_count > 2)
							{
								sleep(sleep);
								GCData mGCData = new GCData();
								mSuperHandler.obtainMessage(MainActivity.MESSAGE_READ_GC, 0, 0, mGCData).sendToTarget(); // 2

								break;
							}
						}
					}
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
			}

			while (true) {
				try {

					byte[] order = new byte[1];
					byte[] RealData = new byte[1000];

					Log.i(TAG, "BEGIN Packet Head Check");
					int i = 0;

					while (true)
					{
						try {
							sleep(sleep);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mmInStream.read(InputtedPacket, 0, 1);
						i = (InputtedPacket[0] == HEAD_PACKET_STANDARD) ? i + 1 : 0;
						if (i == 3)
						{
							i = 0;
							mmInStream.read(order);
							mPacketHead.add(order[0]);
							Log.i("Packet", "Head " + String.valueOf(order[0]));
							if (order[0] == PACKET5_STANDARD) {// log

								Log.i("Packet",
										mPacketHead.toString() + "  Milli Second : " + System.currentTimeMillis());
								mPacketHead.removeAllElements();
							}
							break;

						}
					}
					if (order[0] == PACKET0_BATTREY) {
						int Battery = 0;
						byte[] batt = new byte[1];

						mmInStream.read(order, 0, 1); // 怨듬갚

						if (order[0] == 0x20) {

							mmInStream.read(batt, 0, 1);
							int temp1 = Integer.valueOf(String.valueOf((char) batt[0]));
							mmInStream.read(batt, 0, 1);
							int temp2 = Integer.valueOf((String.valueOf((char) batt[0])));
							mmInStream.read(batt, 0, 1);
							int temp3 = Integer.valueOf((String.valueOf((char) batt[0])));

							Battery = ((temp1 * 100) + (temp2 * 10) + (temp3));
						} else
						{

							int temp4 = Integer.valueOf((String.valueOf((char) order[0])));
							mmInStream.read(batt, 0, 1);
							int temp1 = Integer.valueOf((String.valueOf((char) batt[0])));
							mmInStream.read(batt, 0, 1);
							int temp2 = Integer.valueOf((String.valueOf((char) batt[0])));
							mmInStream.read(batt, 0, 1);
							int temp3 = Integer.valueOf((String.valueOf((char) batt[0])));

							Battery = ((temp4 * 1000) + (temp1 * 100) + (temp2 * 10) + (temp3));
						}

						mmInStream.read(batt, 0, 1); // 留덈Т由� �뙣�궥 'f'
						if (batt[0] != TAIL_PACKET_STANDARD) {
							int temp4 = 0;// ;Integer.valueOf((String.valueOf((char)batt[0])));
							mmInStream.read(batt, 0, 1);
							int temp1 = Integer.valueOf((String.valueOf((char) batt[0])));
							mmInStream.read(batt, 0, 1);
							int temp2 = Integer.valueOf((String.valueOf((char) batt[0])));
							mmInStream.read(batt, 0, 1);
							int temp3 = Integer.valueOf((String.valueOf((char) batt[0])));

							int HV = ((temp4 * 1000) + (temp1 * 100) + (temp2 * 10) + (temp3));
							if (D)
								Log.d(TAG_RECV_DATA, "Battery- " + Battery + ",   HV - " + HV);
						} else {
							if (D)
								Log.d(TAG_RECV_DATA, "Battery- " + Battery);
						}

						mSuperHandler.obtainMessage(MainActivity.MESSAGE_READ_BATTERY,
								AcumulBattery_and_getNowPercent(Battery), -1, AcumulBattery_and_getNowPercent(Battery))
								.sendToTarget();

						// mBattery = Battery;
						Step = 0;
						continue;
					}

					if (order[0] == PACKET0_BATTREY2) {

						int Battery = 0;
						byte[] batt = new byte[1];

						// mmInStream.read(order, 0, 1); // 怨듬갚

						if (true) {

							mmInStream.read(batt, 0, 1);

							// int temp1 = Integer.valueOf(String.valueOf((char)
							// batt[0]));

							int temp1 = batt[0] & 0xff;

							mmInStream.read(batt, 0, 1);
							// int temp2 =
							// Integer.valueOf((String.valueOf((char)
							// batt[0])));

							int temp2 = batt[0] & 0xff;
							mmInStream.read(batt, 0, 1);
							// int temp3 =
							// Integer.valueOf((String.valueOf((char)
							// batt[0])));

							int temp3 = batt[0] & 0xff;

							// Battery = ((temp1 * 100) + (temp2 * 10) +
							// (temp3));

							Battery = ((char) temp1 * 0x100 + ((char) temp2));

						} else {

							int temp4 = Integer.valueOf((String.valueOf((char) order[0])));
							mmInStream.read(batt, 0, 1);
							int temp1 = Integer.valueOf((String.valueOf((char) batt[0])));
							mmInStream.read(batt, 0, 1);
							int temp2 = Integer.valueOf((String.valueOf((char) batt[0])));
							mmInStream.read(batt, 0, 1);
							int temp3 = Integer.valueOf((String.valueOf((char) batt[0])));

							Battery = ((temp4 * 1000) + (temp1 * 100) + (temp2 * 10) + (temp3));
						}
						// if(D)Log.d(TAG, "Battery- "+Battery);
						mmInStream.read(batt, 0, 1); // 留덈Т由� �뙣�궥 'f'
						
						if (batt[0] != TAIL_PACKET_STANDARD) {
							/*
							 * int temp4 = 0;//
							 * ;Integer.valueOf((String.valueOf((char)batt[0])))
							 * ; mmInStream.read(batt, 0, 1); int temp1 =
							 * Integer.valueOf((String.valueOf((char)
							 * batt[0]))); mmInStream.read(batt, 0, 1); int
							 * temp2 = Integer.valueOf((String.valueOf((char)
							 * batt[0]))); mmInStream.read(batt, 0, 1); int
							 * temp3 = Integer.valueOf((String.valueOf((char)
							 * batt[0])));
							 * 
							 * int HV = ((temp4 * 1000) + (temp1 * 100) + (temp2
							 * * 10) + (temp3)); if (D) Log.d(TAG_RECV_DATA,
							 * "Battery- " + Battery + ",   HV - " + HV);
							 */} else {
							if (D)
								Log.d(TAG_RECV_DATA, "Battery- " + Battery);
						}
						mBatteryCount++;

						if (mBatteryCount == 50 || mBatteryCount > 5) {
							mBatteryCount = 0;
							mSuperHandler.obtainMessage(MainActivity.MESSAGE_READ_BATTERY,
									AcumulBattery_and_getNowPercent(Battery), -1,
									AcumulBattery_and_getNowPercent(Battery)).sendToTarget();
							// mBattery = Battery;
						}
						Step = 0;
						continue;
					}
					// /////////////////////////////////////// �뜲�씠�꽣 �쁺�뿭 寃��궗
					Log.i(TAG, "BEGIN Packet Middle(data) Check");
					if (order[0] == PACKET5_STANDARD & Step != 4) {
						Step = 0;
						continue;
					}
					if (Step == 4) {
						byte[] temp = new byte[1];
						for (int j = 0; j < 207; j++) {// 207 packet
							mmInStream.read(temp, 0, 1);
							RealData[j] = temp[0];
						}

					} else if (Step != 4) {
						byte[] temp = new byte[1];

						for (int j = 0; j < 720; j++) {
							
							mmInStream.read(temp, 0, 1);
							RealData[j] = temp[0];
						}

					}


					Step = OrderPacket(order[0], Step, RealData);
					if (Step == STEP_ERROR) {
						Step = 0;
						continue;
					}
				} catch (IOException e) {
					NcLibrary.Write_ExceptionLog(e);
					connectionLost();
					break;
				}
			}
		}

		public boolean write(byte[] buffer) {

			try {
				// mmObjectOutStream = new ObjectOutputStream(mmOutStream);
				if (PACKET_TRSF == true & buffer == MainActivity.MESSAGE_END_HW) {
					MainService.FIRST_PACKET = true;
					PACKET_TRSF = false;
					mNeutron.clear();
					mGM.clear();
					mTemp_for_PacketData.clear();
					mmOutStream.write(buffer);
				} else if (PACKET_TRSF == false & buffer == MainActivity.MESSAGE_START_HW) {
					PACKET_TRSF = true;
					MainService.FIRST_PACKET = true;
					mmOutStream.write(buffer);
				}

				if (buffer != MainActivity.MESSAGE_START_HW & buffer != MainActivity.MESSAGE_END_HW) {
					mmOutStream.write(buffer);
				}
				// mHandler.obtainMessage(KainacActivity.MESSAGE_WRITE, -1,
				// -1,buffer).sendToTarget();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
				return false;
			}
			return true;
		}

		public boolean Restart() {
			try {
				// mmOutStream.write(KainacActivity.MESSAGE_END_HW);
				mmOutStream.write(MainActivity.MESSAGE_START_HW);

			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
				return false;
			}

			FIRST_PACKET = true;
			return true;
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}

		/////////////////////////////////////////////////// 寃��궗�맂 �뙣�궥�뱾 �젙由�
		private void SetPacketData(int[] ChArray, int Neutron, int GM) {
			int[] temp = ChArray;
			mTemp_for_PacketData.add(temp);
			while (true) {
				if (mTemp_for_PacketData.size() > 3)
					mTemp_for_PacketData.remove(0);
				else
					break;
			}

			int Neu = Neutron;
			mNeutron.add(Neu);
			while (true) {
				if (mNeutron.size() > NEUTRON_ACCUM_SEC)
					mNeutron.remove(0);
				else
					break;
			}

			mGM.add(GM);
			while (true) {
				if (mGM.size() > 20)
					mGM.remove(0);
				else
					break;
			}
		}

		private int[] GetGammaPacketData() {
			// mCheck = true;
			int[] TempData = null;

			if (mTemp_for_PacketData == null | mTemp_for_PacketData.size() == 0)
				return TempData;

			// Log.i("Packet", "Get Gamma channel");
			TempData = mTemp_for_PacketData.get(0);
			mTemp_for_PacketData.remove(0);
			// mCheck=false;
			return TempData;
		}

		private double GetAVGNeutron() {
			double Avg = 0;
			for (int i = 0; i < mNeutron.size(); i++) {
				Avg += mNeutron.get(i);
			}
			Avg = Avg / NEUTRON_ACCUM_SEC;
			return Avg;
		}

		private int AcumulBattery_and_getNowPercent(int BattValue) {

			int BATTERY_MIN = 830;
			int BATTERY_MAX = 960;// 985;

			double result = 0;
			double tt = BattValue;

			if (BattValue < BATTERY_MIN)
				return 0;

			tt -= BATTERY_MIN;
			tt = (tt / (BATTERY_MAX - BATTERY_MIN)) * 100.0;
			if (tt > 100)
				tt = 100;
			else if (tt < 0)
				tt = 0;

			return NcLibrary.Auto_floor(tt);
		}

		private int GetAvgGmValue() {

			double Avg = 0;
			for (int i = 0; i < mGM.size(); i++) {
				Avg += mGM.get(i);
			}
			Avg = Avg / mGM.size();
			return NcLibrary.Auto_floor(Avg);

		}

		public int OrderPacket(byte order, int NowStep, byte[] InputtedPacket) {

			/*
			 * if(order == PACKET1_STANDARD) aad+=1;// Write Packet File sdcard
			 * = Environment.getExternalStorageDirectory();
			 * 
			 * File dbpath = new File(sdcard.getAbsolutePath() + File.separator
			 * + "SAM_Packet"); if(!dbpath.exists()){ if(D) Log.d(TAG,
			 * "Create DB directory. " + dbpath.getAbsolutePath());
			 * dbpath.mkdirs(); }
			 * 
			 * String dbfile = dbpath.getAbsolutePath() + File.separator +
			 * "asd"+aad+".txt";
			 * 
			 * try { FileOutputStream fos = new FileOutputStream(dbfile,true);
			 * fos.write(String.format("\n").getBytes()); fos.write(order);
			 * fos.write(String.format(" 55 55 55 %x ",order).getBytes());
			 * 
			 * int ii =720; if(order == 0xc0) ii = 207;
			 * 
			 * for(int i=0; i< ii; i+=3){ fos.write(String.format(
			 * "%02x%02x%02x_ ",
			 * InputtedPacket[i],InputtedPacket[i+1],InputtedPacket[i+2]).
			 * getBytes()); } fos.close(); } catch (FileNotFoundException e) {
			 * // TODO Auto-generated catch block e.printStackTrace(); } catch
			 * (IOException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 * 
			 */
			///////////////////////////////////////////////////////////////
			switch (order) {
			case PACKET1_STANDARD:
				if (NowStep == 0) {
					for (int i = 3; i < 720; i++) {
						m_CompletedRealData[i - 3] = InputtedPacket[i];
					}
					NowStep = 1;
					if (D)
						Log.i("Packet", "0x80 completed");
				} else
					return STEP_ERROR;

				break;

			// /////////////
			case PACKET2_STANDARD:
				if (NowStep == 1) {
					for (int i = 0; i < 720; i++) {
						m_CompletedRealData[i + 717] = InputtedPacket[i];
					}
					NowStep = 2;
					if (D)
						Log.i("Packet", "0x90 completed");
				} else
					return STEP_ERROR;

				break;
			// ///////////////
			case PACKET3_STANDARD:
				if (NowStep == 2) {
					for (int i = 0; i < 720; i++) {
						m_CompletedRealData[i + 1437] = InputtedPacket[i];
					}
					NowStep = 3;
					if (D)
						Log.i("Packet", "0xa0 completed");
				} else
					return STEP_ERROR;

				break;
			// //////////////
			case PACKET4_STANDARD:
				if (NowStep == 3) {
					for (int i = 0; i < 720; i++) {
						m_CompletedRealData[i + 2157] = InputtedPacket[i];
					}
					NowStep = 4;
					if (D)
						Log.i("Packet", "0xb0 completed");
				} else
					return STEP_ERROR;

				break;
			// ////////////////
			case PACKET5_STANDARD:
				if (NowStep == 4) {
					for (int i = 0; i < 207; i++) {
						m_CompletedRealData[i + 2877] = InputtedPacket[i];
					}
					NowStep = 0;
					if (D)
						Log.i("Packet", "0xc0 completed");

					int pdata[] = new int[1024];
					long CPS = 0;

					for (int i = 0; i < 1024; i++) {
						int temp1 = m_CompletedRealData[3 * i] & 0xff; // unsigned
																		// 蹂��솚
																		// 諛⑸쾿
						int temp2 = m_CompletedRealData[3 * i + 1] & 0xff;
						int temp3 = m_CompletedRealData[3 * i + 2] & 0xff;

						pdata[i] = ((char) temp1 * 0x10000 + ((char) temp2 * 0x100 + ((char) temp3)));
						if (pdata[i] > 1500000)
							pdata[i] = 0;
						CPS += pdata[i];
					}

					int Neutron = 0;
					int GM = 0;
					int temp1 = m_CompletedRealData[3072] & 0xff; // unsigned
																	// 蹂��솚 諛⑸쾿
					int temp2 = m_CompletedRealData[3073] & 0xff;
					int temp3 = m_CompletedRealData[3074] & 0xff;
					GM = ((char) temp1 * 0x10000 + ((char) temp2 * 0x100 + ((char) temp3)));
					// if(D)Log.i(TAG_RECV_DATA, "GM read : "+GM);
					/*
					 * temp1 = m_CompletedRealData[3078]&0xff; //unsigned 蹂��솚
					 * 諛⑸쾿 temp2 = m_CompletedRealData[3079]&0xff; temp3 =
					 * m_CompletedRealData[3080]&0xff;
					 */
					temp1 = m_CompletedRealData[3075] & 0xff; // unsigned 蹂��솚
																// 諛⑸쾿
					temp2 = m_CompletedRealData[3076] & 0xff;
					temp3 = m_CompletedRealData[3077] & 0xff;
					Neutron = ((char) temp1 * 0x10000 + ((char) temp2 * 0x100 + ((char) temp3)));
					if (D)
						Log.i(TAG_RECV_DATA, "GM read : " + GM + ",   Neutron read : " + Neutron);

					temp1 = m_CompletedRealData[3078] & 0xff; // unsigned 蹂��솚
																// 諛⑸쾿
					temp2 = m_CompletedRealData[3079] & 0xff;
					temp3 = m_CompletedRealData[3080] & 0xff;

					IsThereNeutronBit = byteToIntergerArray(m_CompletedRealData[3078]);

					IsThereNeutron = (int) Byte.parseByte("0000000" + Integer.toString(IsThereNeutronBit.get(0)), 2);

					if(IsThereNeutron==1)
						mSuperHandler.obtainMessage(MainActivity.MESSAGE_NEUTRON_RECV, 0, 0, GetAVGNeutron()).sendToTarget();

					temp1 = (int) Byte.parseByte("0" + Integer.toString(IsThereNeutronBit.get(1))
							+ Integer.toString(IsThereNeutronBit.get(2)) + Integer.toString(IsThereNeutronBit.get(3))
							+ Integer.toString(IsThereNeutronBit.get(4)) + Integer.toString(IsThereNeutronBit.get(5))
							+ Integer.toString(IsThereNeutronBit.get(6)) + Integer.toString(IsThereNeutronBit.get(7)),
							2);

					int FillCps;
					FillCps = ((char) temp1 * 0x10000 + ((char) temp2 * 0x100 + ((char) temp3)));

					// if (FillCps == 0) {

					String pakit = "";
					for (int i = 3072; i < 3081; i++) {
						pakit += Integer.toString(m_CompletedRealData[i] & 0xff) + ";";

					}

					Log.d("time:", "Fill_CPS_0 : " + pakit);
					Log.d("time:", "Fill_CPS_0 : " + Integer.toString(temp1) + ";" + Integer.toString(temp2) + ";"
							+ Integer.toString(temp3));
					// }
					MS.SetFillCps(FillCps);

					if (IsThereNeutron > 10) {

						int a;

						a = 1;

					}

					/////
					Spectrum spc = new Spectrum(pdata);
					if (D)
						Log.i(TAG_RECV_DATA, "CPS : " + CPS + ",   SPC : " + spc.ToString());

					for (int i = 0; i < m_CompletedRealData.length; i++) {
						m_CompletedRealData[i] = 0;
					}

					/////
					if (MainService.FIRST_PACKET == true) {
						MainService.FIRST_PACKET = false;
					} else {
						// SetPacketData(pdata,(int)(Math.random()*10)+1,GM);
						SetPacketData(pdata, Neutron, GM);
						// mSuperHandler.obtainMessage(MainActivity.MESSAGE_READ_BATTERY,
						// AcumulBattery_and_getNowPercent(mBattery), -1,
						// AcumulBattery_and_getNowPercent(mBattery)).sendToTarget();

						SystemClock.sleep( 500 );

						if (IsThereNeutron == 0) {// None Neutron
							mSuperHandler
									.obtainMessage(MainActivity.MESSAGE_READ_DETECTOR_DATA, GetAvgGmValue(), -1, pdata)
									.sendToTarget();

						} else { // Neutron
							MainActivity.mDetector.mNeutron.Set_CPS((GetAVGNeutron() <= 0.08) ? 0 : GetAVGNeutron());// HW瑜�
																														// �쐞�븳
																														// 泥섎갑

							mSuperHandler
									.obtainMessage(MainActivity.MESSAGE_READ_DETECTOR_DATA, GetAvgGmValue(), 0, pdata)
									.sendToTarget();
						}
					}
				} else
					return STEP_ERROR;

				break;
			}
			///////////////////
			return NowStep;
		}

		public ArrayList<Integer> byteToIntergerArray(byte b) {
			ArrayList<Integer> BitArray = new ArrayList<Integer>();
			byte[] masks = { -128, 64, 32, 16, 8, 4, 2, 1 };
			// StringBuilder builder = new StringBuilder();
			for (byte m : masks) {
				if ((b & m) == m) {
					// builder.append('1');
					BitArray.add(1);
				} else {
					// builder.append('0');
					BitArray.add(0);
				}
			}
			return BitArray;
		}

	}

}
