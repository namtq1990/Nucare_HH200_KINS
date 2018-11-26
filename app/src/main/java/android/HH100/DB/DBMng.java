package android.HH100.DB;


import android.HH100.Identification.Isotope;
import android.HH100.Structure.Detector;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import static android.HH100.DB.EventDBOper.*;
import static android.HH100.Structure.NcLibrary.Write_ExceptionLog;


/**
 * Created by inseon.ahn on 2018-08-23
 * reachback용 DB
 */

public class DBMng
{
    public static final String REACHBACK_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SAM_ReachBack/";

    private final int DBVERSION = 1;
    private Context m_Ctxt = null;
    private static SQLiteDatabase m_DB = null;
    private DBHelper m_DBHelper = null;
    private final DB m_MyDb = new DB();
    public static DBMng mInst = null;
    private final String DBNAME = "ReachBack.sql";

    public DBMng(Context ctxt)
    {
        m_Ctxt = ctxt;

        File path = new File(REACHBACK_FILE_PATH);
        if(!path.isDirectory())
        {
            path.mkdirs();
        }

        if (m_DBHelper == null)
            m_DBHelper = new DBHelper(m_Ctxt,  REACHBACK_FILE_PATH+DBNAME, null, DBVERSION);

        if (m_DB == null)
            m_DB = m_DBHelper.getWritableDatabase();

        Log.e("DBMNG", "DB Create And Use Ready!");
    }


    public static DBMng GetInst(Context ctxt)
    {
        if (mInst == null)
        {
            mInst = new DBMng(ctxt);
        }
        return mInst;
    }


    private final class DB implements BaseColumns
    {
        private final String TABLENAME = "ReachBack";

        public String onCreateReachBackDB()
        {
            //reachback db
             String q =
                     "create table if not exists ReachBack" + "("
                            + "idx integer primary key autoincrement,"
                            + ROWID + " integer,"
                            + DB_FORMAT_VERSION+ " text, "
                            + INSTRUMENT_MODEL + " text, "
                            + DATE + " text, "
                            + DATE_BG + " text, "
                            + BEGIN+ " text, "
                            + FINISH + " text, "
                            + LOCATION + " text,"
                            + AGENT + " text,"
                            + AVG_GAMMA + " text, "
                            + AVG_NEUTRON + " text, "
                            + MAX_GAMMA + " text, "
                            + MAX_NEUTRON + " text, "
                            + ACQ_TIME + " text, "
                            + REAL_ACQ_TIME + " real, "
                            + ACQ_TIME_BG + " text,"// -- v1.4
                            + REAL_ACQ_TIME_BG + " real, "
                            + COMMENT + " text, "
                            + PHOTO + " text,"
                            + VIDEO + " text,"
                            + GMT+ " text,"
                            + CALIB_A + " real,"
                            + CALIB_B + " real,"
                            + CALIB_C + " real,"
                            + LONGITUDE + " real,"
                            + LATITUDE + " real,"
                            + SPECTRUM + " text,"
                            + SPECTRUM_BG + " text,"
                            + MANUAL_ID + " text,"
                            + IDENTIFICATION + " text,"
                            + EVENT_DETECTOR + " text,"
                            + FAVORITE + " text,"
                            + RECODE + " text,"
                            + FILL_CPS_AVG + " text,"
                             + "xml" + " text," //xml path
                             + "success" + " text);"; //리치백 성공여부

            return q;
        }




    }


    private class DBHelper extends SQLiteOpenHelper
    {

        public DBHelper(Context ctxt, String name, CursorFactory factory, int ver)
        {

            super(ctxt,REACHBACK_FILE_PATH+ DBNAME, factory, ver);
            onCreateDir();
        }

        public void onCreateDir()
        {
            File dir = new File(REACHBACK_FILE_PATH);
            if (! dir.exists())
            {
                dir.mkdirs();
            }

        }


        public void exdbfile(String path, int rawId)
        {

        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            // TODO Auto-generated method stub
            try
            {
                db.execSQL(m_MyDb.onCreateReachBackDB());

            }
            catch (SQLException e)
            {
                Log.e("ERROR", "테이블 생성 실패 > " + e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + m_MyDb.TABLENAME);
            onCreate(db);
        }
    };

    public SQLiteDatabase GetDbInst()
    {
        return m_DB;
    }



    @SuppressWarnings("deprecation")
    public DBMng Open() throws SQLException
    {
        if(m_DB == null)
        {
            Log.e("DBMNG", "DB Inst is Null");
            return null;
        }

        if(m_DB.isOpen())
        {
            Log.e("DBMNG", "DB already!!");
            return this;
        }

        if (m_DBHelper == null)
            m_DBHelper = new DBHelper(m_Ctxt,  REACHBACK_FILE_PATH+DBNAME, null, DBVERSION);
        m_DB = m_DBHelper.getWritableDatabase();

        Log.e("DBMNG", "DB READY!");
        return this;
    }


    public void Close()
    {
        if(m_DB.isOpen())
            m_DB.close();
    }


    public boolean isDB(String dbname)
    {
        File fn = new File(m_Ctxt.getFilesDir(), dbname);
        return fn.exists();
    }

    public static boolean writeReachBackDB(EventData event)
    {
        try
        {
            m_DB.beginTransaction();

            ContentValues newValue = new ContentValues();
            newValue.put(ROWID, event.Event_Number);
            newValue.put(DB_FORMAT_VERSION, event.mColumn_Version);
            newValue.put(INSTRUMENT_MODEL, event.mInstrument_Name);
            newValue.put(DATE, event.Event_Date);
            newValue.put(DATE_BG, event.BG.Get_MesurementDate());
            newValue.put(BEGIN, event.StartTime);
            newValue.put(FINISH, event.EndTime);
            newValue.put(ACQ_TIME, event.MS.Get_AcqTime());
            newValue.put(REAL_ACQ_TIME, event.MS.Get_SystemElapsedTime().getTime() * 0.001);
            newValue.put(ACQ_TIME_BG, event.BG.Get_AcqTime());
            newValue.put(REAL_ACQ_TIME_BG, event.BG.Get_SystemElapsedTime().getTime() * 0.001);
            newValue.put(LOCATION, event.mLocation);
            newValue.put(AGENT, event.mUser);
            newValue.put(FILL_CPS_AVG, Integer.toString(event.MS.GetAvgFillCps()));
         //   newValue.put(AVG_GAMMA, NcLibrary.SvToString(event.Doserate_AVG, true,  (event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false));
            newValue.put(AVG_GAMMA, event.Doserate_AVGs);
            newValue.put(AVG_NEUTRON, String.valueOf(event.Neutron_AVG + " cps"));
            newValue.put(MAX_GAMMA, NcLibrary.SvToString(event.Doserate_MAX, true,  (event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false));
         //   newValue.put(MAX_NEUTRON, String.valueOf(event.Neutron_MAX + " cps"));
            newValue.put(MAX_NEUTRON, String.valueOf(event.Neutron_MAXs));
            newValue.put(FAVORITE, event.Favorite_Checked);
            newValue.put(CALIB_A, event.MS.Get_Coefficients().get_Coefficients()[0]);
            newValue.put(CALIB_B, event.MS.Get_Coefficients().get_Coefficients()[1]);
            newValue.put(CALIB_C, event.MS.Get_Coefficients().get_Coefficients()[2]);
            newValue.put(GMT, event.mGMT);
            newValue.put(LATITUDE, event.GPS_Latitude);
            newValue.put(LONGITUDE, event.GPS_Longitude);
            newValue.put(SPECTRUM, event.MS.ToString());
            newValue.put(SPECTRUM_BG, event.BG.ToString());
            newValue.put(MANUAL_ID, event.IsManualID);
            String temp = "";
            for (int i = 0; i < event.Detected_Isotope.size(); i++) {
                if (event.Detected_Isotope.get(i).Class != null) {
                    if (event.Detected_Isotope.get(i).Class.matches("UNK") == false)
                        temp += event.Detected_Isotope.get(i).Get_Result_OnlyDB(DB_verion);
                } else {
                    temp += event.Detected_Isotope.get(i).Get_Result_OnlyDB(DB_verion);
                }
            }
            newValue.put(IDENTIFICATION, temp);
            newValue.put(EVENT_DETECTOR, event.Event_Detector);
            newValue.put(COMMENT, event.Comment);

            newValue.put(PHOTO,	 event.reachBackPic);
            newValue.put("xml", event.reachBackXml);
            newValue.put("success", event.reachBackSuccess+"");


            m_DB.insertWithOnConflict("ReachBack", null, newValue, SQLiteDatabase.CONFLICT_REPLACE);
            m_DB.setTransactionSuccessful();
            m_DB.endTransaction();
            return  true;
        }
        catch (Exception e)
        {
            return false;
        }


    }


    public EventData loadReachBackDB(int index)
    {


        //List에 사용될 DB내용들 저장
        //  ArrayList<ConbiMenuStruct> arr = new ArrayList<ConbiMenuStruct>();
        EventData  event = null;
        try
        {
            Cursor cu = m_DB.rawQuery("SELECT * FROM ReachBack WHERE idx = '"+index+"'",  null);

            if (cu.getCount() != 0) {
                String temp = "";

                event = new EventData();
                while (cu.moveToNext())
                {
                    event.idx = cu.getInt(cu.getColumnIndex("idx"));
                    event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
                    event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
                    event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
                    event.Event_Date = cu.getString(cu.getColumnIndex(DATE));
                    event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));
                    event.AvgFillCps = Integer.parseInt(cu.getString(cu.getColumnIndex(FILL_CPS_AVG)));
                    event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
                    event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
                    event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
                    event.mUser = cu.getString(cu.getColumnIndex(AGENT));
                    event.Doserate_AVGs = cu.getString(cu.getColumnIndex(AVG_GAMMA));
                    event.Neutron_AVGs = cu.getString(cu.getColumnIndex(AVG_NEUTRON));
                    event.Doserate_MAXs = cu.getString(cu.getColumnIndex(MAX_GAMMA));
                    event.Neutron_MAXs = cu.getString(cu.getColumnIndex(MAX_NEUTRON));
                    event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
                    double ABC[] = new double[3];
                    ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
                    ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
                    ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));
                    event.MS.Set_Coefficients(ABC);
                    event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
                    event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
                    event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),cu.getInt(cu.getColumnIndex(ACQ_TIME)));
                    event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),  cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));
                    event.MS.Set_StartSystemTime(new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME)) * 1000)));
                    event.BG.Set_StartSystemTime( new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME_BG)) * 1000)));
                    event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

                    event.reachBackXml = cu.getString(cu.getColumnIndex("xml"));
                    //event.reachBackSuccess = Boolean.parseBoolean(cu.getString(cu.getColumnIndex("success")));
                    event.reachBackPic = cu.getString(cu.getColumnIndex("Photo"));

                    temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
                    Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
                    Vector<Isotope> IsoData = new Vector<Isotope>();
                    for (int i = 0; i < IsoTemp.size(); i++)
                    {
                        Isotope iso = new Isotope();
                        iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
                        IsoData.add(iso);
                    }
                    event.Detected_Isotope = IsoData;
                    event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
                }



                cu.close();
            }
            else
            {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return event;

    }

    //180629 추가
    public boolean updateReachBack(int index, String pic, String xml, String success)
    {
        try
        {
            m_DB.beginTransaction();
            ContentValues v = new ContentValues();
            v.put("Photo", pic);
            v.put("xml", xml);
            v.put("success", success);
            m_DB.update("ReachBack", v, "idx='" + index + "'", null);
            m_DB.setTransactionSuccessful();
            m_DB.endTransaction();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }


    //delete
    public boolean deleteReachBack(int index)
    {
        try
        {
            m_DB.beginTransaction();
            m_DB.delete("ReachBack", "idx='" + index + "'", null);
            m_DB.setTransactionSuccessful();
            m_DB.endTransaction();
        }
        catch (Exception e)
        {
            return  false;
        }
        return true;
    }


    //180827 추가 inseon.
    public Cursor loadAllReachBackDB(boolean success,int start, int end)
    {
    //    m_DB.beginTransaction();
        String sql = "";
        if(success)
        {
            sql = "SELECT idx, _id , Date, Avg_Gamma, AcqTime, Identification, Event_Detector,begin, success, Photo, xml FROM ReachBack order by idx DESC limit "+start+","+end;
        }
        else
        {
            //reachback 실패는 페이징 처리 안함
            sql = "SELECT idx, _id , Date, Avg_Gamma, AcqTime, Identification, Event_Detector,begin, success, Photo, xml FROM ReachBack WHERE success = 'false' order by idx DESC";
        }
        Cursor cu = m_DB.rawQuery(sql,  null);
        return cu;
    }

    public int loadReahBackDB(String date ,String begin)
    {
        Cursor cu = null;
        try
        {
            //SELECT * FROM ReachBack WHERE Date = "2018-8-28" AND begin = "11:45:9"
            m_DB.beginTransaction();
            String q ="SELECT idx FROM ReachBack WHERE Date = '"+date+"' AND begin = '"+begin+"'";
            cu = m_DB.rawQuery(q,  null);
            cu.moveToFirst();
            m_DB.setTransactionSuccessful();
            m_DB.endTransaction();
            return cu.getInt(cu.getColumnIndex("idx"));
        }
        catch (Exception e)
        {
            return -1;
        }

    }

}


