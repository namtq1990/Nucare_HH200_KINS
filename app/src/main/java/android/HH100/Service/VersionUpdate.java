package android.HH100.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import Debug.Version;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class VersionUpdate{
 
	private final String SERVERIP = "nucaremed.com";//"211.247.237.33";	
	private final String SERVERID = "nucare";
	private final String SERVERPW = "medical2all*";
	private final int PORT = 21;
	private final String VERSION_FILE_NAME = "SAM III PeakAbout III Kins";
	
	private final String VERSION_FILE_NAME_Kor = "SAM III PeakAbout III";
	
	public static final int MESSAGE_UPDATE_SW = 235261;
	
	private FTPClient mFtpClient = null;	
	private String mAppVersion ="";
	private Handler mSuperHandler = null;
	
	public VersionUpdate(String AppVersion, Handler SuperHandler){
		mAppVersion = AppVersion;
		mSuperHandler = SuperHandler; 
	}
	
	public boolean Update_Version_FromFTP(){
		mFtpClient=new FTPClient();
		connect();
		if(login(SERVERID, SERVERPW)){
			///cd("root/xxx");//input u r directory	
	
	 		FTPFile[] files = list();
	 		String filename = "";
	 		String mFileName2 = "";
	 		if(Version.IsKinsVersion) {
	 		filename = VERSION_FILE_NAME;
	 		mFileName2 = VERSION_FILE_NAME;
	 		}else {
	 			filename = VERSION_FILE_NAME_Kor;
		 		mFileName2 = VERSION_FILE_NAME_Kor;	
	 			
	 		}
	 		if(files!=null){ 			
	 	 		for(int i =0 ;i<files.length;i++){
	 				String fileName = files[i].getName();
	 	            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
	 	            long size = files[i].getSize();
	 	            extension=extension.toUpperCase();  
	 	           
					if (size > 0) {
						try{							
							String temp1 = fileName.replace(" ", "_");
							String temp2 = mFileName2.replace(" ", "_");
							
							if(temp1.matches(temp2+".*"))
							{
								String RecentVersion = temp1;
								
								RecentVersion = RecentVersion.replace(").apk", "");
								RecentVersion = RecentVersion.replace(temp2+"_(v", "");	
								
								int WebVer = Integer.valueOf(RecentVersion.replace(".", ""));
								String temp3 = mAppVersion.replace("b2", "");
								int AppVer = Integer.valueOf(temp3.replace(".", ""));
								
								if(WebVer > AppVer){ 								
									if(!mAppVersion.matches(RecentVersion)){
										mSuperHandler.obtainMessage(MESSAGE_UPDATE_SW, 0, 0, null).sendToTarget();
										
										//get(fileName, fileName);
										break;
									}							
								}
							}	
						}catch(Exception e)
						{
							
						}
					}         
	 			}	 			
	 		}
	 		logout();	
 	        disconnect();
	 		return true;
		}
		else return false;				
	}
	
	public void SendDebug(String fileName) {
		if(mAppVersion.matches("")){
			
		}
		
	}
	
	public boolean login(String user, String password) {

        try {
            this.connect();
            return this.mFtpClient.login(user, password);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
          
        }
        return false;

    }
	 private boolean logout() {

	        try {
	            return this.mFtpClient.logout();
	        }
	        catch (IOException ioe) {
	            ioe.printStackTrace();
	        }
	        return false;

	    }
	public void connect() {

        try {

        	this.mFtpClient.connect(SERVERIP, PORT);

            int reply;

            reply = this.mFtpClient.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {

            	this.mFtpClient.disconnect();

            }

        }

        catch (IOException ioe) {

            if(this.mFtpClient.isConnected()) {

                try {

                	this.mFtpClient.disconnect();

                } catch(IOException f) {;}

            }

        } 

    }
	 public FTPFile[] list() {

	        FTPFile[] files = null;

	        try {

	            files = this.mFtpClient.listFiles();

	            return files;

	        }

	        catch (IOException ioe) {

	            ioe.printStackTrace();

	        }

	        return null;

	    }




     public File get(String source, String target) {
  
	        OutputStream output = null;
	        try {
	        	
				StringBuffer furl = new StringBuffer(Environment.getExternalStorageDirectory().getAbsolutePath()+"/");
				furl.append(target);
				
	        	File path = new File(furl.toString());

	            if(!path.isDirectory()) {
	                 path.mkdirs();
	            }	           
	            
	            if(!path.isFile())
	            {
	            	path.createNewFile();
	            }
	            
	            output = new FileOutputStream(path);
	            int asdfq = 0;
	        }
	        catch (FileNotFoundException fnfe) {;}
	        catch (IOException e) {e.printStackTrace();}
	        
	        
	        File file = new File(source);	      
	        try {

	            if (this.mFtpClient.retrieveFile(source, output)) {

	                return file;

	            }

	        }

	        catch (IOException ioe) {;}

	        return null;

	    }


	    public void cd(String path) {

	        try {
	        	this.mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
	        	this.mFtpClient.enterLocalPassiveMode();
	        	this.mFtpClient.changeWorkingDirectory(path);
	        }

	        catch (IOException ioe) {

	            ioe.printStackTrace();

	        }

	    }




	    private void disconnect() {
	        try {
	        	this.mFtpClient.disconnect();
	        }
	        catch (IOException ioe) {
	            ioe.printStackTrace();
	        }

	    }
	    //-----	   
}
