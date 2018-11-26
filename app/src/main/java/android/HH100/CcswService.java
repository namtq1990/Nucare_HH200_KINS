package android.HH100;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
//import java.util.Timer;
import java.util.Vector;
import android.HH100.Identification.Isotope;
import android.HH100.R.string;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.os.Handler;




public class CcswService {
	private Socket mServerSocket = null;
	private ConnectThread mConnectThread = null;
	private ConnectedThread mConnectedThread = null;
	private Handler mSuper_Handler = null;
	
	public static int mState;
	
	public static final int SERVER_STATE_NONE = 30; // �븘臾닿쾬�룄 �옟���엳吏��븡�쓬
	public static final int SERVER_STATE_LISTEN = 31; // �깉濡쒖슫 而ㅻ꽖�뀡�쓣 �씫�뒗以�
	public static final int SERVER_STATE_CONNECTING = 32; // �뿰寃고븯�뒗以�
	public static final int SERVER_STATE_CONNECTED = 33; // �뿰寃곕릺�뼱�엳�쓬
	public static final int SERVER_STATE_LOST = 34; // �뿰寃곕릺�뼱�엳�쓬
	public static final int SERVER_STATE_CONNECT_FAIL= 35; // �뿰寃곕릺�뼱�엳�쓬
	
	public static final byte SERVER_PACKET_START = (byte) 0x80;
	public static final byte CHECK_SERVER_ALIVE = (byte) 0x90;
	public static final byte SERVER_EVENT_LOG = (byte) 0xa0;
	
	//private Spectrum mSpcBuff = null;
	private Vector<Isotope> mID_Result_Buff = null;  
	private MappingData mMappingData = null;
	private EventData mEventLog = null;
	public CcswService(Handler parent) {
		mSuper_Handler = parent;
	}
	
	public synchronized void Set_Data(Vector<Isotope> result,MappingData det){		
		
		mID_Result_Buff = result;
		mMappingData = new MappingData();
		mMappingData = det;		
			
	}
	public synchronized void connect(String IP_Address) {
	
		if (mState == SERVER_STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
		}
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		//"121.67.176.169"
		mConnectThread = new ConnectThread(mServerSocket,IP_Address, 5001);
		mConnectThread.start();
		setState(SERVER_STATE_CONNECTING);
	}
	private void Connect_Fail(){
		if(mServerSocket != null){
			try {
				mServerSocket.close();
			} catch (IOException e) {			
				NcLibrary.Write_ExceptionLog(e);
			}
			mServerSocket = null;
		}
		if(mSuper_Handler!=null) mSuper_Handler.obtainMessage(SERVER_STATE_CONNECT_FAIL,SERVER_STATE_CONNECT_FAIL,0,0).sendToTarget();
	}
	private void setState(int state) {
		mState = state;
	}
	public boolean Is_Connected(){
		if(mConnectedThread != null){
			if(mConnectedThread.mSock != null) return true;
		}
		/*
		if(mServerSocket != null) {
			if(mConnectedThread != null) return true;
		}*/
		return false;
	}
	public void Set_EventLog(EventData event){
		mEventLog = event;
	}
	public void Send_DataToServer(final MappingData data)	
	{
		new Thread(new Runnable() {			
			@Override
			public void run() {
				if(mConnectedThread != null) mConnectedThread.Send_ToServer(data);				
			}
		}).start();   
	}
	public synchronized void start() {
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		setState(SERVER_STATE_LISTEN);		
	}

	private synchronized void connected(Socket socket) {
		
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		
		setState(SERVER_STATE_CONNECTED);
		if(mSuper_Handler!=null) mSuper_Handler.obtainMessage(SERVER_STATE_CONNECTED,SERVER_STATE_CONNECTED,0,0).sendToTarget();
	}
	private synchronized void All_Tread_Stop(){
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
	}
	private void connectionLost() {
		setState(SERVER_STATE_LOST);
		
		mSuper_Handler.obtainMessage(SERVER_STATE_LOST,SERVER_STATE_LOST,0,0).sendToTarget();
		All_Tread_Stop();
	}
	
	
	private class ConnectThread extends Thread {
		private Socket mSock = null;
		private String mIpAddress = null;
		private int mPort = 0;
		
		
		public ConnectThread(Socket Sock,String Server_IP, int PortNumber) {
			mSock = Sock;
			mIpAddress = Server_IP;
			mPort = PortNumber;
		}
		private boolean Connect_WithServer(String Server_IP, int PortNumber){
			try {
	            // IP 二쇱냼�� �룷�듃 踰덊샇瑜� 愿�由ы븯�뒗 媛앹껜瑜� �깮�꽦�븳�떎.
	            SocketAddress sock_addr = new InetSocketAddress(Server_IP, PortNumber);
	            // �냼耳볦쓣 �깮�꽦�븳�떎.
	            mSock = new Socket();	            
	            // �닔�떊 踰꾪띁 �겕湲곕�� 1024 諛붿씠�듃濡� �꽕�젙�븳�떎.
	            //3Sock.setReceiveBufferSize(1024); 
	            // �넚�떊 踰꾪띁 �겕湲곕�� 1024 諛붿씠�듃濡� �꽕�젙�븳�떎.
	            //3Sock.setSendBufferSize(1024);
	            // �냼耳볦쓣 �떕�쓣 �븣 TIME_OUT �뿉 ���옣�빐�몦 諛�由ъ꽭而⑤쭔�겮 ��湲고븳 �썑 �떕�뒗�떎.
	            //3Sock.setSoLinger(true, 100000); 
	            //15遺꾧컙 �닔�떊�릺�뒗 �뜲�씠�꽣媛� �뾾�쑝硫� �뿰寃곗씠 �옄�룞�쑝濡� �걡湲대떎.
	            mSock.setSoTimeout(30000); 	            
	           // mSock.setTcpNoDelay(true);
	           // mSock.setKeepAlive(true);
	            
	            // �꽌踰꾩� �뿰寃곗쓣 �떆�룄�븳�떎. TIME_OUT �떆媛� �궡�뿉 �쓳�떟�씠 �삤吏� �븡�쑝硫� �뿰寃곗쓣 �룷湲고븳�떎.
	            mSock.connect(sock_addr, 10000);
	            
	            // �뿰寃곕맂 寃쎌슦
	            if(mSock != null){         
	            	if(mSock.isConnected()){
	            		return true;
	            	}            
	            }
	        } catch (Exception e) { 
	        	NcLibrary.Write_ExceptionLog(e);

	        } finally {
	        }
			return false;
		}
		@Override
		public void run() {			
				if(	Connect_WithServer(mIpAddress,mPort))
				{
					synchronized (CcswService.this) {
						mConnectThread = null;
					}
					connected(mSock);
				}else{
					Connect_Fail();
					CcswService.this.All_Tread_Stop();
					return;
					
				}
		}
		
		public void cancel() {
			try {
				if(mSock != null)mSock.close();
				mSock = null;
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}
	
	private class ConnectedThread extends Thread {
		
		
		private Socket mSock = null;	
		
		BufferedOutputStream m_out_stream = null;
		InputStream m_in_stream = null;
		
		
		public ConnectedThread(Socket Sock) {
			mSock = Sock;				
			try {
				m_out_stream = new BufferedOutputStream(mSock.getOutputStream());
				m_in_stream = mSock.getInputStream();
			} catch (IOException e) {				
				NcLibrary.Write_ExceptionLog(e);
			}		
		}		
		
		@Override
		public  void run() {			
			while (true) {		
				synchronized (CcswService.this) {	
					if( mMappingData==null){				
						boolean IsAlive_Server=false;
						IsAlive_Server = IsConnected_Server();
						if(IsAlive_Server == false)
						{
							NcLibrary.Write_ExceptionLog("\nIsAlive == false");
							//synchronized (CcswService.this) { // Stop This Thread
								cancel();
								mConnectedThread = null;
								connectionLost();
								return;
							//}
						}																
					}else
					{
						boolean Check=false;
						Check = Send_ToServer(mMappingData);										
						if(Check)
						{
							//mMappingData.Clear();
						}else{
							NcLibrary.Write_ExceptionLog("\nIsAlive == false");
						//	synchronized (CcswService.this) { // Stop This Thread
								cancel();
								mConnectedThread = null;
								connectionLost();
								return;
						//	}
						}
						
				       }
				    }
				
				
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						NcLibrary.Write_ExceptionLog(e);
					}					  
			}
	    }
		private boolean IsConnected_Server(){
			if(mSock != null){				
				if(mSock.isConnected()){					
					try {				
						
						m_out_stream.write(CHECK_SERVER_ALIVE);
						m_out_stream.flush();
						
						//byte[] buf = new byte[10];
						//while((len = m_in_stream.read(buff)) != -1){
						int checker = m_in_stream.read();
						return true;
						//if(checker != 144)return true;
						//else return false;
					} catch (IOException e) {
						NcLibrary.Write_ExceptionLog(e);
						
						return false;
					}
				}
				else{
					NcLibrary.Write_ExceptionLog("\nIsConnected_Server() - isConnect=fasle");
					return false;
				}
			}
			else return false;
		}
		public void cancel() {
			try {
				mSock.close();
				mSock = null;
				m_out_stream.close();
				m_in_stream.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
		
		private boolean Send_ToServer( MappingData det) {
			// �냼耳볦씠 �뿰寃곕릺�뼱 �엳�뒗 �긽�깭�씤 寃쎌슦
	        if(mSock != null && mSock.isConnected() ){
	        	String Data = "";
	            try {
	            	
	            	//Detector �씠由�
	            	String Name = det.InstrumentName;
	            	String Mac = det.InstrumentMacAddress;
	            	//String NameSize = (byte)(Name.length);	
	            	
	            	//GPS
	            	String Gps_Lati = Double.toString(det.Get_Lat());
	            	String Gps_Long = Double.toString(det.Get_Lng());
	            	
	            	//
	            	String Doserate = Double.toString(det.Doserate);
	                String cps = String.valueOf(det.CPS);
	            	
	            	//String Data;	            	
	            	Data = Name+"|"+Mac+"|"+Gps_Lati+"|"+Gps_Long+"|"+Doserate+"|"+cps;	            	            	
	                
	                
	                int data_size = Data.getBytes().length;
	                byte[] Data_size = new byte[4];              
	                Data_size[0] = (byte)((data_size >> 24)&0xFF);
	                Data_size[1] = (byte)((data_size >> 16)&0xFF);
	                Data_size[2] = (byte)((data_size >> 8)&0xFF);
	                Data_size[3] = (byte)(data_size);	                
	               	              
	              
	                m_out_stream.write(SERVER_PACKET_START);
	                m_out_stream.write(Data_size);
	                m_out_stream.write(Data.getBytes());
	                
	                m_out_stream.flush();
	                
	                int checker = m_in_stream.read();
	                 
	            } catch(IOException e) {
	            	NcLibrary.Write_ExceptionLog("\n CCSW-Send_ToServer()  "+e.getMessage() );
	            	NcLibrary.Write_ExceptionLog("\n Data = "+Data);
	            	return false;
	            }
	            return true;
	        }
	        else {
	        	return false;
	        }

		}
	}
	
	
	public static class MappingData{
		private double[] Coordinate = new double[2];
		String InstrumentName;
		String InstrumentMacAddress;
		double Doserate;
		int CPS;
		
		boolean IsInSetupMode=false;
	
		public double Get_Lat()
		{
			return Coordinate[0];
		}
		public double Get_Lng()
		{
			return Coordinate[1];
		}
		public void Set_Coordinate(double lat, double lng){
			Coordinate[0] = lat;
			Coordinate[1] = lng;
		}
		
		public void Clear()
		{
			InstrumentMacAddress = "";
			InstrumentName ="";
			Doserate = 0;
			CPS =0;
			Coordinate = new double[2];
		}
	}
}
