package android.HH100.Identification;

import java.io.*;
import java.util.*;

import NcLibrary.Coefficients;
import NcLibrary.NewNcAnalsys;
import NcLibrary.SpcAnalysis;
import android.HH100.Control.*;
import android.HH100.DB.EventDBOper;
import android.HH100.R.string;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.Spectrum;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.os.*;

public class IsotopesLibrary {
	private SQLiteDatabase _db;
	private Isotope[][] mIsotopes;
	private Vector<Isotope> mSel_Library = new Vector<Isotope>();
	private Vector<String> mIsoLib_List;
	private boolean mLoadCheck = false;
	/*
	    180907 DB_VERSION \n추가 이전버전 byte array[3]으로 선언 2자리수 버전만 처리가능
	     \n추가해서 한줄로 통채로 인식
	 */
	public static String DB_VERSION = "1.1.1\n"; //180313 버전업(2.4->2.5) 180727 1.1.0 -> 180831 v1.1.1 ->

	public IsotopesLibrary(Context context) {
		// super(context, "SS.db", null, 1);
		File file = new File(getFilePath("SwLibrary.sql"));

		if (file.isFile() == true) {
			_db = SQLiteDatabase.openDatabase(getFilePath("SwLibrary.sql"), null, SQLiteDatabase.OPEN_READWRITE);
			String aas = _db.getPath();
			Read_data_From_DBfile();
			mLoadCheck = true;
			_db.close();
		} else {

			mLoadCheck = false;
		}
	}

	public SQLiteDatabase Get_DB() {
		return _db;
	}

	public void Set_LibraryName(String name) {

		for (int i = 0; i < mIsoLib_List.size(); i++) {
			String temp = mIsoLib_List.get(i).toString();
			if (name.matches(temp)) {
				for (int k = 0; k < mIsotopes[i].length; k++) {
					mSel_Library.add(mIsotopes[i][k]);
				}
				return;
			}
		}
	}

	public Vector<Isotope> Get_Isotopes() {

		return mSel_Library;
	}

	/*
	 * private Vector<Isotope> find_Ba133_81keV(Vector<Isotope> Target, int[]
	 * Energy, int[] channel, int[] VallyCh,double[] VallyAB,int Energy_Count){
	 * boolean WasFound = false; if(Target.isEmpty()) return Target;
	 * 
	 * Vector<Isotope> result2 = new Vector<Isotope>();
	 * 
	 * for(int i =0 ; i<Target.size(); i++){ result2.add(Target.get(i));
	 * if(Target.get(i).isotopes.matches("Ba-133")){ WasFound = true; } }
	 * if(WasFound)return result2; //result2.remove(result2.size());
	 * 
	 * double L_ROI_Percent = 0; double R_ROI_Percent = 0; double measu_sum = 0;
	 * boolean check = false; Isotope TempResult = new Isotope(); Isotope Ba133 =
	 * Get_Isotope("Ba-133"); if(Ba133.Energy1 == 161) Ba133.Energy1 = 81;
	 * if(Ba133.Energy2 == 161) Ba133.Energy2 = 81; if(Ba133.Energy3 == 161)
	 * Ba133.Energy3 = 81; TempResult = Ba133;
	 * 
	 * for(int k=0; k<Energy_Count;k++){ if(Ba133.Energy1== 0) break; double
	 * Roi_window = NcLibrary.Get_Roi_window_by_energy(Ba133.Energy1); L_ROI_Percent
	 * = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
	 * 
	 * //L_ROI_Percent = L_ROI_Percent*1.01; //R_ROI_Percent = R_ROI_Percent*0.99;
	 * if(Ba133.Energy1*L_ROI_Percent < Energy[k] && Ba133.Energy1*R_ROI_Percent >
	 * Energy[k]) { check=true; double
	 * measu=Math.abs((((Energy[k]-Ba133.Energy1)/Ba133.Energy1)*100)); measu = 100
	 * - measu; measu_sum = measu;
	 * 
	 * TempResult.isotopes = Ba133.isotopes; TempResult.Energy1 = Ba133.Energy1;
	 * TempResult.Channel1 = channel[k]; TempResult.Channel1_Vally.x =
	 * VallyCh[(k*2)]; TempResult.Channel1_Vally.y = VallyCh[(k*2)+1];
	 * TempResult.Channel1_AB.x = (float) VallyAB[(k*2)]; TempResult.Channel1_AB.y =
	 * (float) VallyAB[(k*2)+1];
	 * 
	 * break; } else { check=false; }
	 * 
	 * }
	 * 
	 * for(int k=0; k<Energy_Count;k++){ if(Ba133.Energy2 == 0) break; double
	 * Roi_window = NcLibrary.Get_Roi_window_by_energy(Ba133.Energy2); L_ROI_Percent
	 * = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
	 * 
	 * if(Ba133.Energy2*L_ROI_Percent < Energy[k] &&Ba133.Energy2*R_ROI_Percent >
	 * Energy[k]) { check=true;
	 * 
	 * double measu=Math.abs((((Energy[k]-Ba133.Energy2)/Ba133.Energy2)*100)); measu
	 * = 100 - measu; measu_sum += measu; measu_sum = (measu_sum/2);
	 * 
	 * TempResult.isotopes = Ba133.isotopes; TempResult.Energy2 = Ba133.Energy2;
	 * TempResult.Channel2 = channel[k]; TempResult.Channel2_Vally.x =
	 * VallyCh[(k*2)]; TempResult.Channel2_Vally.y = VallyCh[(k*2)+1];
	 * TempResult.Channel2_AB.x = (float) VallyAB[(k*2)]; TempResult.Channel2_AB.y =
	 * (float) VallyAB[(k*2)+1]; break;} else { check=false;
	 * 
	 * } }
	 * 
	 * for(int k=0; k<Energy_Count;k++){ if(Ba133.Energy3 == 0) break; double
	 * Roi_window = NcLibrary.Get_Roi_window_by_energy(Ba133.Energy3); L_ROI_Percent
	 * = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
	 * 
	 * if(Ba133.Energy3*L_ROI_Percent < Energy[k] && Ba133.Energy3*R_ROI_Percent >
	 * Energy[k]){ check=true; double
	 * measu=Math.abs((((Energy[k]-Ba133.Energy3)/Ba133.Energy3)*100)); measu = 100
	 * - measu; measu_sum += measu; measu_sum = (measu_sum/2);
	 * 
	 * TempResult.isotopes = Ba133.isotopes; TempResult.Energy3 = Ba133.Energy3;
	 * TempResult.Channel3 = channel[k]; TempResult.Channel3_Vally.x =
	 * VallyCh[(k*2)]; TempResult.Channel3_Vally.y = VallyCh[(k*2)+1];
	 * TempResult.Channel3_AB.x = (float) VallyAB[(k*2)]; TempResult.Channel3_AB.y =
	 * (float) VallyAB[(k*2)+1]; break;} else { check=false;
	 * 
	 * } }
	 * 
	 * 
	 * if(check == true){ int re = (int)measu_sum;
	 * 
	 * TempResult.Confidence_Level = re; TempResult.measure_eff =measu_sum;
	 * TempResult.Class = Ba133.Class; result2.add(TempResult); check = false; }
	 * 
	 * return result2; }
	 */
	private Vector<Isotope> find_Ba133_81keV(Vector<Isotope> Target, Vector<NcPeak> Peak_data) {
		boolean WasFound = false;
		// if(Target.isEmpty()) return Target;

		Vector<Isotope> result2 = new Vector<Isotope>();

		for (int i = 0; i < Target.size(); i++) {
			result2.add(Target.get(i));
			if (Target.get(i).isotopes.matches("Ba-133")) {
				WasFound = true;
			}
		}
		if (WasFound)
			return result2;
		// result2.remove(result2.size());

		double L_ROI_Percent = 0;
		double R_ROI_Percent = 0;
		double measu_sum = 0;
		boolean check = false;
		Isotope TempResult = new Isotope();
		Isotope Ba133 = Get_Isotope("Ba-133");
		for (int i = 0; i < Ba133.Peaks.size(); i++)
			if (Ba133.Peaks.get(i).Peak_Energy == 161)
				Ba133.Peaks.get(i).Peak_Energy = 81;
		for (int i = 0; i < Ba133.Unknown_Peak.size(); i++)
			if (Ba133.Unknown_Peak.get(i).Peak_Energy == 81)
				Ba133.Unknown_Peak.get(i).Peak_Energy = 161;

		TempResult = Ba133;

		for (int EnCnt = 0; EnCnt < TempResult.Peaks.size(); EnCnt++) {

			NcPeak TempPeak = TempResult.Peaks.get(EnCnt);
			for (int k = 0; k < Peak_data.size(); k++) {

				boolean isIn = TempPeak.Energy_InWindow(Peak_data.get(k).Peak_Energy);
				if (isIn) {

					check = true;
					double measu = Math.abs((((Peak_data.get(k).Peak_Energy - TempPeak.Peak_Energy) / TempPeak.Peak_Energy) * 100));
					measu = 100 - measu;
					measu_sum += measu;

					TempResult.FoundPeaks.add(Peak_data.get(k));
					break;
				} else {
					check = false;
				}
			}
			if (check == false)
				break;
		}
		if (check) {
			TempResult.Confidence_Level = measu_sum / TempResult.Get_OnlyIdEnergy_Cnt();
			result2.add(TempResult);

		}
		measu_sum = 0;
		check = false;

		return result2;
	}

	@SuppressWarnings("null")
	private void Read_data_From_DBfile_old() {

		Vector<String> IsoLib_list = Read_Isolib_List();
		mIsoLib_List = IsoLib_list;
		/////
		Cursor cu = _db.rawQuery("SELECT * FROM IsoLibName", null);
		cu.moveToFirst();
		if (cu.getCount() == 0)
			return;
		else {
			mIsotopes = new Isotope[cu.getCount()][0];
		}
		/////

		for (int i = 0; i < IsoLib_list.size(); i++) {

			String sql = "SELECT * FROM " + IsoLib_list.get(i);

			cu = _db.rawQuery(sql, null);

			cu.moveToFirst();
			if (cu.getCount() == 0)
				return;
			mIsotopes[i] = new Isotope[cu.getCount()];

			int count = 0;
			while (true) {
				Isotope temp = new Isotope();
				temp.isotopes = cu.getString(0);

				String Energy = "";
				if (!cu.isNull(1)) {
					Energy = cu.getString(1);
					Vector<String> EnBr = NcLibrary.Separate_EveryDash2(Energy);

					for (int q = 0; q < EnBr.size(); q += 2) {
						NcPeak tempPeak = new NcPeak();
						tempPeak.Peak_Energy = Integer.valueOf(EnBr.get(q));

						temp.Peaks.add(tempPeak);
					}
				}
				if (!cu.isNull(2)) {
					Energy = cu.getString(2);
					Vector<String> EnBr = NcLibrary.Separate_EveryDash2(Energy);

					for (int q = 0; q < EnBr.size(); q += 2) {
						NcPeak tempPeak = new NcPeak();
						tempPeak.Peak_Energy = Integer.valueOf(EnBr.get(q));

						temp.Unknown_Peak.add(tempPeak);
					}
				}

				if (!cu.isNull(3))
					temp.Class = cu.getString(3);
				if (!cu.isNull(4))
					temp.Comment = cu.getString(4);
				if (!cu.isNull(5))
					temp.HelpVideo = cu.getString(5);

				mIsotopes[i][count] = temp;
				count += 1;
				if (cu.isLast() == true)
					break;
				cu.moveToNext();
			}

			cu.close();
		}
	}
	
	private void Read_data_From_DBfile() {

		Vector<String> IsoLib_list = Read_Isolib_List();
		mIsoLib_List = IsoLib_list;
		/////
		Cursor cu = _db.rawQuery("SELECT * FROM IsoLibName", null);
		cu.moveToFirst();
		if (cu.getCount() == 0)
			return;
		else {
			mIsotopes = new Isotope[cu.getCount()][0];
		}
		/////

		for (int i = 0; i < IsoLib_list.size(); i++) {

			String sql = "SELECT * FROM " + IsoLib_list.get(i);

			cu = _db.rawQuery(sql, null);

			cu.moveToFirst();
			if (cu.getCount() == 0)
				return;
			mIsotopes[i] = new Isotope[cu.getCount()];

			int count = 0;
			while (true) {
				Isotope temp = new Isotope();
				temp.isotopes = cu.getString(0);

				String Energy = "";
				if (!cu.isNull(1)) {

					Energy = cu.getString(1);
					Vector<String> EnBr = NcLibrary.Separate_EveryDash2(Energy);

					for (int q = 0; q < EnBr.size(); q += 2) {
						NcPeak tempPeak = new NcPeak();
						tempPeak.Peak_Energy = Double.valueOf(EnBr.get(q));
						tempPeak.Isotope_Gamma_En_BR = Double.valueOf(EnBr.get(q + 1));
						temp.Peaks.add(tempPeak);
					}
				}
				if (!cu.isNull(2)) {
					Energy = cu.getString(2);
					Vector<String> EnBr = NcLibrary.Separate_EveryDash2(Energy);

					for (int q = 0; q < EnBr.size(); q += 2) {
						NcPeak tempPeak = new NcPeak();
						tempPeak.Peak_Energy = Double.valueOf(EnBr.get(q));

						temp.Unknown_Peak.add(tempPeak);
					}

					// String MinorEnergy = "";
					// MinorEnergy = IsotopeData.get(count).MinorEnergy;
					// if (IsotopeData.get(count).MinorEnergy != null) {
					Vector<String> MinorEnBr = NcLibrary.Separate_EveryDash2(Energy);

					for (int j = 0; j < MinorEnBr.size(); j += 2) {
						temp.IsoMinorPeakEn.add(Double.valueOf(MinorEnBr.get(j)));
						temp.IsoMinorPeakBR.add(Double.valueOf(MinorEnBr.get(j + 1)));

					}

					// }

				}

				if (!cu.isNull(3))
					temp.Class = cu.getString(3);
				if (!cu.isNull(4))
					temp.Comment = cu.getString(4);
				if (!cu.isNull(5))
					temp.HelpVideo = cu.getString(5);

				mIsotopes[i][count] = temp;
				count += 1;
				if (cu.isLast() == true)
					break;
				cu.moveToNext();
			}

			cu.close();
		}
	}
	public Vector<String> Read_Isolib_List() {
		Vector<String> result = new Vector<String>();

		Cursor cu;
		cu = _db.rawQuery("SELECT * FROM IsoLibName", null);
		cu.moveToFirst();
		if (cu.getCount() == 0)
			return result;
		////////// -----------------

		while (true) {
			String name = cu.getString(0);
			result.add(name);

			if (cu.isLast() == true)
				break;
			cu.moveToNext();
		}
		cu.close();

		return result;
	}

	//////////////////////////////////////////////////////////
	public String getFilePath(String FileName) {
		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_LIB_FOLDER);
		if (!dbpath.exists()) {
			dbpath.mkdirs();
		}

		String dbfile = dbpath.getAbsolutePath() + File.separator + FileName;
		return dbfile;


	}

	public Vector<String> get_IsotopeLibrary_List() {
		return mIsoLib_List;
	}/*
		 * public Vector<Isotope> Find_Isotopes_with_Energy(int Energy[],int channel[],
		 * int VallyCh[],double VallyAB[], int Energy_Count) { Vector<String> result =
		 * new Vector<String>(); Vector<Isotope> result2 = new Vector<Isotope>();
		 * Isotope TempResult = new Isotope();
		 * 
		 * if(mLoadCheck == false) return result2; //
		 * 
		 * boolean check = false;
		 * 
		 * 
		 * 
		 * double L_ROI_Percent = 0.94; double R_ROI_Percent = 1.06; double measu_sum =
		 * 0;
		 * 
		 * 
		 * for(int i=0; i<mSel_Library.size(); i++){ int PeakCnt = 0 ;
		 * if(mSel_Library.get(i).IdPeak_En1) PeakCnt +=1;
		 * if(mSel_Library.get(i).IdPeak_En2) PeakCnt +=1;
		 * if(mSel_Library.get(i).IdPeak_En3) PeakCnt +=1;
		 * if(mSel_Library.get(i).IdPeak_En4) PeakCnt +=1;
		 * if(mSel_Library.get(i).IdPeak_En5) PeakCnt +=1;
		 * 
		 * TempResult = mSel_Library.get(i); measu_sum = 0;
		 * 
		 * for(int k=0; k<Energy_Count;k++){ if(mSel_Library.get(i).Energy1== 0 |
		 * mSel_Library.get(i).IdPeak_En1 == false) { if(mSel_Library.get(i).Energy1 !=
		 * 0 & mSel_Library.get(i).IdPeak_En1 == false) check=true; break; } double
		 * Roi_window = NcLibrary.Get_Roi_window_by_energy(mSel_Library.get(i).Energy1);
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
		 * 
		 * if(mSel_Library.get(i).Energy1*L_ROI_Percent < Energy[k] &&
		 * mSel_Library.get(i).Energy1*R_ROI_Percent > Energy[k]) { check=true; double
		 * measu=Math.abs((((Energy[k]-mSel_Library.get(i).Energy1)/mSel_Library
		 * .get(i).Energy1)*100)); measu = 100 - measu; measu_sum = measu;
		 * 
		 * TempResult.isotopes = mSel_Library.get(i).isotopes; TempResult.Energy1 =
		 * mSel_Library.get(i).Energy1; TempResult.Channel1 = channel[k];
		 * TempResult.Channel1_Vally.x = VallyCh[(k*2)]; TempResult.Channel1_Vally.y =
		 * VallyCh[(k*2)+1]; TempResult.Channel1_AB.x = (float) VallyAB[(k*2)];
		 * TempResult.Channel1_AB.y = (float) VallyAB[(k*2)+1];
		 * 
		 * 
		 * break; } else { check=false; }
		 * 
		 * } if(check == false) continue; for(int k=0; k<Energy_Count;k++){
		 * if(mSel_Library.get(i).Energy2== 0 | mSel_Library.get(i).IdPeak_En2 == false)
		 * { if(mSel_Library.get(i).Energy2 != 0 & mSel_Library.get(i).IdPeak_En2 ==
		 * false) check=true; break; } double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(mSel_Library.get(i).Energy2);
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
		 * 
		 * if(mSel_Library.get(i).Energy2*L_ROI_Percent < Energy[k]
		 * &&mSel_Library.get(i).Energy2*R_ROI_Percent > Energy[k]) { check=true;
		 * 
		 * double measu=Math.abs((((Energy[k]-mSel_Library.get(i).Energy2)/mSel_Library
		 * .get(i).Energy2)*100)); measu = 100 - measu; measu_sum += measu;
		 * 
		 * TempResult.isotopes = mSel_Library.get(i).isotopes; TempResult.Energy2 =
		 * mSel_Library.get(i).Energy2; TempResult.Channel2 = channel[k];
		 * TempResult.Channel2_Vally.x = VallyCh[(k*2)]; TempResult.Channel2_Vally.y =
		 * VallyCh[(k*2)+1]; TempResult.Channel2_AB.x = (float) VallyAB[(k*2)];
		 * TempResult.Channel2_AB.y = (float) VallyAB[(k*2)+1];
		 * 
		 * 
		 * break;} else { check=false;
		 * 
		 * } } if(check == false) continue; for(int k=0; k<Energy_Count;k++){
		 * if(mSel_Library.get(i).Energy3== 0 | mSel_Library.get(i).IdPeak_En3 == false)
		 * { if(mSel_Library.get(i).Energy3 != 0 & mSel_Library.get(i).IdPeak_En3 ==
		 * false) check=true; break; } double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(mSel_Library.get(i).Energy3);
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
		 * 
		 * if(mSel_Library.get(i).Energy3*L_ROI_Percent < Energy[k] &&
		 * mSel_Library.get(i).Energy3*R_ROI_Percent > Energy[k]) { check=true; double
		 * measu=Math.abs((((Energy[k]-mSel_Library.get(i).Energy3)/mSel_Library
		 * .get(i).Energy3)*100)); measu = 100 - measu; measu_sum += measu;
		 * 
		 * TempResult.isotopes = mSel_Library.get(i).isotopes; TempResult.Energy3 =
		 * mSel_Library.get(i).Energy3; TempResult.Channel3 = channel[k];
		 * TempResult.Channel3_Vally.x = VallyCh[(k*2)]; TempResult.Channel3_Vally.y =
		 * VallyCh[(k*2)+1]; TempResult.Channel3_AB.x = (float) VallyAB[(k*2)];
		 * TempResult.Channel3_AB.y = (float) VallyAB[(k*2)+1];
		 * 
		 * 
		 * break;} else { check=false;
		 * 
		 * } } if(check == false) continue; for(int k=0; k<Energy_Count;k++){
		 * if(mSel_Library.get(i).Energy4== 0 | mSel_Library.get(i).IdPeak_En4 == false)
		 * { if(mSel_Library.get(i).Energy4 != 0 & mSel_Library.get(i).IdPeak_En4 ==
		 * false) check=true; break; }
		 * 
		 * double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(mSel_Library.get(i).Energy4);
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
		 * 
		 * if(mSel_Library.get(i).Energy4*L_ROI_Percent < Energy[k] &&
		 * mSel_Library.get(i).Energy4*R_ROI_Percent > Energy[k]) { check=true; double
		 * measu=Math.abs((((Energy[k]-mSel_Library.get(i).Energy4)/mSel_Library
		 * .get(i).Energy4)*100)); measu = 100 - measu; measu_sum += measu;
		 * 
		 * TempResult.isotopes = mSel_Library.get(i).isotopes; TempResult.Energy4 =
		 * mSel_Library.get(i).Energy4; TempResult.Channel4 = channel[k];
		 * TempResult.Channel4_Vally.x = VallyCh[(k*2)]; TempResult.Channel4_Vally.y =
		 * VallyCh[(k*2)+1]; TempResult.Channel4_AB.x = (float) VallyAB[(k*2)];
		 * TempResult.Channel4_AB.y = (float) VallyAB[(k*2)+1];
		 * 
		 * 
		 * break;} else { check=false;
		 * 
		 * } } if(check == false) continue; for(int k=0; k<Energy_Count;k++){
		 * if(mSel_Library.get(i).Energy5== 0 | mSel_Library.get(i).IdPeak_En5 == false)
		 * { if(mSel_Library.get(i).Energy5 != 0 & mSel_Library.get(i).IdPeak_En5 ==
		 * false) check=true; break; }
		 * 
		 * double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(mSel_Library.get(i).Energy5);
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
		 * 
		 * if(mSel_Library.get(i).Energy5*L_ROI_Percent < Energy[k] &&
		 * mSel_Library.get(i).Energy5*R_ROI_Percent > Energy[k]) { check=true; double
		 * measu=Math.abs((((Energy[k]-mSel_Library.get(i).Energy5)/mSel_Library
		 * .get(i).Energy5)*100)); measu = 100 - measu; measu_sum += measu;
		 * 
		 * TempResult.isotopes = mSel_Library.get(i).isotopes; TempResult.Energy5 =
		 * mSel_Library.get(i).Energy5; TempResult.Channel5 = channel[k];
		 * TempResult.Channel5_Vally.x = VallyCh[(k*2)]; TempResult.Channel5_Vally.y =
		 * VallyCh[(k*2)+1]; TempResult.Channel5_AB.x = (float) VallyAB[(k*2)];
		 * TempResult.Channel5_AB.y = (float) VallyAB[(k*2)+1];
		 * 
		 * 
		 * break; } else { check=false;
		 * 
		 * } } if(check == false) continue;
		 * 
		 * if(check == true){ // found isotope
		 * 
		 * int re = (int)measu_sum/PeakCnt; String temp =
		 * mSel_Library.get(i).isotopes+"   "+String.valueOf(re)+"%"; result.add(temp);
		 * 
		 * 
		 * TempResult.Confidence_Level =re; TempResult.measure_eff =measu_sum;
		 * TempResult.Class = mSel_Library.get(i).Class; result2.add(TempResult);
		 * TempResult = null; TempResult = new Isotope(); check = false; // /*int
		 * TempCount = 0; for(int q=0; q<FoundEnegy.size(); q++){ for(int w=0;
		 * w<Energy_Count; w++){ if(FoundEnegy.get(q) == Energy[w]){ Energy[w] = 0;
		 * TempCount +=1; } } }
		 * 
		 * }
		 * 
		 * }
		 * 
		 * result2 =
		 * find_Ba133_81keV(result2,Energy,channel,VallyCh,VallyAB,Energy_Count) ;
		 * 
		 * 
		 * result2 = Filter_IdLogic_Algorithm_1(result2); result2 =
		 * Filter_IdLogic_Algorithm_2(result2); result2 =
		 * Filter_IdLogic_Algorithm_3(result2); result2 =
		 * Filter_IdLogic_Algorithm_6(result2); result2 =
		 * Filter_IdLogic_Algorithm_7(result2); result2 =
		 * Filter_IdLogic_Algorithm_8(result2); result2 =
		 * Filter_IdLogic_Algorithm_9(result2); result2 =
		 * Filter_IdLogic_Algorithm_12(result2); result2 =
		 * Filter_IdLogic_Algorithm_13(result2);
		 * 
		 * result2 = Find_UnknownPeak(result2, Energy, channel, VallyCh, VallyAB,
		 * Energy_Count); return result2; }//end Find_Isotopes_with_Energy
		 */

	public Vector<Isotope> Find_Isotopes_with_Energy(Spectrum SPC, Spectrum BG, Vector<NcPeak> Peak_data) {
		Vector<NcPeak> PeakTemp = Peak_data;
		Vector<NcPeak> UsedPeak = new Vector<NcPeak>();
		Vector<Isotope> result2 = new Vector<Isotope>();
		Isotope TempResult = new Isotope();

		boolean check = false;
		double measu_sum = 0;

		if (mLoadCheck == false)
			return result2;
		//

		// start

		for (int i = 0; i < mSel_Library.size(); i++) {
			TempResult = mSel_Library.get(i);

			for (int EnCnt = 0; EnCnt < TempResult.Peaks.size(); EnCnt++) {
				NcPeak TempPeak = TempResult.Peaks.get(EnCnt);

				for (int k = 0; k < PeakTemp.size(); k++) {

					boolean isIn = TempPeak.Energy_InWindow(PeakTemp.get(k).Peak_Energy);
					if (isIn) {
						check = true;
						double measu = Math.abs((((PeakTemp.get(k).Peak_Energy - TempPeak.Peak_Energy) / TempPeak.Peak_Energy) * 100));
						measu = 100 - measu;
						measu_sum += measu;

						TempResult.FoundPeaks.add(PeakTemp.get(k));

						// UsedPeak.add(TempPeak);
						break;
					} else {
						check = false;
					}
				}
				if (check == false) {
					// UsedPeak.clear();
					break;
				}
			}
			if (check) {
				TempResult.Confidence_Level = measu_sum / TempResult.Get_OnlyIdEnergy_Cnt();

				/*
				 * for(int q=0; q<PeakTemp.size();q++) { for(int k=0; k<UsedPeak.size();i++){
				 * if(PeakTemp.equals(UsedPeak)){ PeakTemp.remove(q); q -=1; break; } } }
				 * UsedPeak.clear();
				 */
				result2.add(TempResult);
			}
			measu_sum = 0;
			check = false;
		}

		// end

		try {
			result2 = find_Ba133_81keV(result2, Peak_data);
			result2 = Filter_IdLogic_Algorithm_Nucare1(SPC, result2, PeakTemp);

			result2 = Filter_IdLogic_Algorithm_1(result2);
			result2 = Filter_IdLogic_Algorithm_2(result2);
			result2 = Filter_IdLogic_Algorithm_3(result2);
			result2 = Filter_IdLogic_Algorithm_6(result2);
			result2 = Filter_IdLogic_Algorithm_7(result2);
			result2 = Filter_IdLogic_Algorithm_8(result2);
			/// result2 = Filter_IdLogic_Algorithm_9(result2);
			result2 = Filter_IdLogic_Algorithm_12(result2);
			result2 = Filter_IdLogic_Algorithm_13(result2);
			result2 = Filter_IdLogic_Algorithm_14(result2);
			result2 = Filter_IdLogic_Algorithm_15(result2, PeakTemp);

			result2 = Filter_IdLogic_Algorithm_4(result2,
					SPC.at_ToInt(SpcAnalysis.ToChannel(100, SPC.Get_Coefficients())),
					SPC.at_ToInt(SpcAnalysis.ToChannel(186, SPC.Get_Coefficients())));
			result2 = Filter_IdLogic_Algorithm_5(SPC.ToInteger(), SPC.Get_AcqTime(), BG.ToInteger(), BG.Get_AcqTime(),
					result2);
			result2 = Filter_IdLogic_Algorithm_10(SPC.ToInteger(), SPC.Get_AcqTime(), BG.ToInteger(), BG.Get_AcqTime(),
					SPC.Get_Coefficients().get_Coefficients()[0], SPC.Get_Coefficients().get_Coefficients()[1],
					SPC.Get_Coefficients().get_Coefficients()[2], result2);
			result2 = Filter_IdLogic_Algorithm_11(SPC.ToInteger(), SPC.Get_AcqTime(), BG.ToInteger(), BG.Get_AcqTime(),
					SPC.Get_Coefficients().get_Coefficients()[0], SPC.Get_Coefficients().get_Coefficients()[1],
					SPC.Get_Coefficients().get_Coefficients()[2], result2);

			result2 = Filter_IdLogic_Algorithm_16(result2, SPC, SPC.ToInteger(), SPC.Get_AcqTime(), BG.ToInteger(),
					BG.Get_AcqTime());

			result2 = Find_UnknownPeak(result2, Peak_data);
		} catch (Exception e) {

		}

		return result2;
	}// end Find_Isotopes_with_Energy


	/*...............................................
	 * Function for finding Peak and Source ID are implemented here. 
	 * Date: 201.03.05
	 */
	public Vector<Isotope> Find_Isotopes_with_Energy(Spectrum SPC, Spectrum BG) {

		Vector<Isotope> result2 = new Vector<Isotope>();

		if (SPC.getFWHM() != null) {

			double Thrshld_Index1 = 0.95;
			double Thrshld_Index2 = 0.9;
			double Thrshld_UnClaimed_Index1 = 0.95;
			double Thrshld_UnClaimed_Index2 = 0.2;
			double Thrshld_Index2_MinorMajor_Ra226 = 0.9; // Calculate confidence index for all minor and major
			double ThrShld_Act_Except_Ra226 = 0.1;
			double ThrShld_Act_Except_OtherIso = 0.1;
			double Act_Thsold = 0.05; // 5% // Actvity theshold
			double WndROI_CEPeak = 1.0;

			Coefficients EnCoeff_Cali = SPC.Get_Coefficients();// Energy Calibration

			double WndROI = SPC.getWnd_Roi();

			double[] FWHM_gen = SPC.getFWHM();

			double[] Eff_Coeff = SPC.getFindPeakN_Coefficients();

			// Step 1: Processing: Find PeakInfor. BG Subtracted is minus in this step

			Vector<NcPeak> mFoundPeak_data = NewNcAnalsys.Find_Peak(SPC, BG);

			// Step 2: ID Isotope by template matching
			result2 = NewNcAnalsys.PeakMatchIsotope_H(mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI, mSel_Library);

			// Step 3: Applied Confidence filter
			result2 = NewNcAnalsys.IndexFillter_H(result2, FWHM_gen, EnCoeff_Cali, Eff_Coeff, WndROI, Thrshld_Index1,Thrshld_Index2);

			int CHSIZE = 1024;
			double[] ChSpec = new double[CHSIZE];
			for (int i = 0; i < SPC.Get_Ch_Size(); i++) {
				ChSpec[i] = SPC.at(i);
			}

			// remove K-40
			if (result2.size() > 0) {
				for (int i = 0; i < result2.size(); i++) {
					if (result2.get(i).isotopes.equals("K-40")) {

						// result2.remove(i);
						// --i;
					}

				}
			}
			// 18.05.16: adding logic table C.E peak for Cs137 and Co60
			result2 = NewNcAnalsys.LogicComptonPeakCs_Co60(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali,
					WndROI_CEPeak);
			result2 = NewNcAnalsys.LogicHighEnricUranium(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);

			// Step 4: Activity Calculation

			// 1st Screening Processing
			if (result2.size() > 0) {

				// result2 = NewNcAnalsys.CValue_Filter_H(NewNcAnalsys.Smooth_Spc(ChSpec),
				// result2, mFoundPeak_data, FWHM_gen,EnCoeff_Cali, Eff_Coeff,
				// SPC.Get_AcqTime(), WndROI);
				result2 = NewNcAnalsys.CValue_Filter_H(NewNcAnalsys.Smooth_Spc(ChSpec), result2, mFoundPeak_data,
						FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI, Act_Thsold);

				// Adding information
				for (int i = 0; i < result2.size(); i++) {
					result2.get(i).Screening_Process = 1;
				}

			}

			// Step 5: Last condition for validation isotope

			// 2nd Sreening Processing
			if (result2.size() >= 0) {

				Vector<NcPeak> UnClaimedPeak = NewNcAnalsys.CValue_Return_UnclaimedEn(NewNcAnalsys.Smooth_Spc(ChSpec),
						result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI);

				// condition
				int NoFoundPeak = mFoundPeak_data.size();

				int NoUnclaimedPeak = UnClaimedPeak.size();

				double RatioPeak = (double) NoUnclaimedPeak / (double) NoFoundPeak;

				if (RatioPeak > 0.5) // Peak: 50% is unclaime peak
				{
					Act_Thsold = 0.01; // 1%
				}

				if (RatioPeak > 0.25) // at least more than 25% unclaimed peak will
				// be processed
				{
					Vector<Isotope> Unclaimed_Result = new Vector<Isotope>();

					// Template Matching
					Unclaimed_Result = NewNcAnalsys.PeakMatchIsotope_H(UnClaimedPeak, FWHM_gen, EnCoeff_Cali, WndROI,
							mSel_Library);

					// Remove Peaks which was still remembered in memory in Java
					Unclaimed_Result = IsoRemoveLines(Unclaimed_Result, UnClaimedPeak);

					// Re calcute confidence index
					Unclaimed_Result = NewNcAnalsys.IndexFillter_H(Unclaimed_Result, FWHM_gen, EnCoeff_Cali, Eff_Coeff,
							WndROI, Thrshld_UnClaimed_Index1, Thrshld_UnClaimed_Index2);

					// 18.05.16: adding logic table C.E peak for Cs137 and Co60
					Unclaimed_Result = NewNcAnalsys.LogicComptonPeakCs_Co60(Unclaimed_Result, UnClaimedPeak, FWHM_gen,
							EnCoeff_Cali, WndROI_CEPeak);

					// Condition for High enriched uranium
					Unclaimed_Result = NewNcAnalsys.LogicHighEnricUranium(Unclaimed_Result, UnClaimedPeak, FWHM_gen,
							EnCoeff_Cali, WndROI);

					// Step 1: Find best isotope with max number of line
					if (Unclaimed_Result.size() > 0) {
						Unclaimed_Result = NewNcAnalsys.IsotopeID_UnClaimedLine(NewNcAnalsys.Smooth_Spc(ChSpec),
								Unclaimed_Result, UnClaimedPeak, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(),
								WndROI);

						// Adding information
						for (int i = 0; i < Unclaimed_Result.size(); i++) {
							Unclaimed_Result.get(i).Screening_Process = 2;
						}
					}

					// Step2 : Final calculation

					if (Unclaimed_Result.size() > 0) {

						result2 = NewNcAnalsys.AddingIsotope(result2, Unclaimed_Result);

						if (result2.size() > 0) {
							// 18.05.16: adding logic table C.E peak for Cs137 and Co60
							result2 = NewNcAnalsys.LogicComptonPeakCs_Co60(result2, mFoundPeak_data, FWHM_gen,
									EnCoeff_Cali, WndROI_CEPeak);
							result2 = NewNcAnalsys.LogicHighEnricUranium(result2, mFoundPeak_data, FWHM_gen,
									EnCoeff_Cali, WndROI);

							result2 = NewNcAnalsys.CValue_Filter_H(NewNcAnalsys.Smooth_Spc(ChSpec), result2,
									mFoundPeak_data, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI,
									Act_Thsold);
						}
					}

				}

				// Adding condition to prohibit Co57 and Tc99m

				// result2=NewNcAnalsys. AddCondition_Ra_Ba(result2);

				// Adding condition to prohibit WGPU by logic when RGPu is IDed
				// Adding condition to prohibit Ra and Ba
				if (result2.size() > 0) {
					// result2=NewNcAnalsys.AddCondition_Ra_Ba(result2);
					// result2=NewNcAnalsys.AddCondition_Ra_Ba(result2,mFoundPeak_data,
					// FWHM_gen,EnCoeff_Cali,WndROI,Eff_Coeff,
					// SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
					result2 = NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226", "Ba-133", result2, mFoundPeak_data,
							FWHM_gen, EnCoeff_Cali, 0.6, Eff_Coeff, SPC.Get_AcqTime(), Thrshld_Index2_MinorMajor_Ra226,
							ThrShld_Act_Except_Ra226);
					result2 = NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226", "U-235", result2, mFoundPeak_data,
							FWHM_gen, EnCoeff_Cali, 0.6, Eff_Coeff, SPC.Get_AcqTime(), Thrshld_Index2_MinorMajor_Ra226,
							ThrShld_Act_Except_Ra226);
					result2 = NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226", "Ga-67", result2, mFoundPeak_data,
							FWHM_gen, EnCoeff_Cali, 0.6, Eff_Coeff, SPC.Get_AcqTime(), Thrshld_Index2_MinorMajor_Ra226,
							ThrShld_Act_Except_Ra226);
					result2 = NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226", "In-111", result2, mFoundPeak_data,
							FWHM_gen, EnCoeff_Cali, 0.6, Eff_Coeff, SPC.Get_AcqTime(), Thrshld_Index2_MinorMajor_Ra226,
							ThrShld_Act_Except_Ra226);

					// Co57 vs Eu152: Activty Ratio of Co57/Eu=11~15%, so take theshold 0.2
					result2 = NewNcAnalsys.AddCondition_Exception_Isopte("Eu-152", "Co-57", result2, mFoundPeak_data,
							FWHM_gen, EnCoeff_Cali, 0.6, Eff_Coeff, SPC.Get_AcqTime(), Thrshld_Index2_MinorMajor_Ra226,
							0.2);
					result2 = NewNcAnalsys.AddCondition_Exception_Isopte("Eu-152", "U-235", result2, mFoundPeak_data,
							FWHM_gen, EnCoeff_Cali, 0.6, Eff_Coeff, SPC.Get_AcqTime(), Thrshld_Index2_MinorMajor_Ra226,
							ThrShld_Act_Except_OtherIso);

					result2 = NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133", "Tl-201", result2, mFoundPeak_data,
							FWHM_gen, EnCoeff_Cali, 0.6, Eff_Coeff, SPC.Get_AcqTime(), Thrshld_Index2_MinorMajor_Ra226,
							ThrShld_Act_Except_OtherIso);

					result2 = NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133", "I-131", result2, mFoundPeak_data,
							FWHM_gen, EnCoeff_Cali, 0.6, Eff_Coeff, SPC.Get_AcqTime(), Thrshld_Index2_MinorMajor_Ra226,
							ThrShld_Act_Except_Ra226);

				}

			}
			// sTEP 4: Adding Minor Peak
			// Adding Minor Peak for Showing
			if (result2.size() > 0) {
				result2 = AddPeakDraw(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);
			}

		}
		return result2;
	}// end Find_Isotopes_with_Energy


	private Vector<Isotope> Find_UnknownPeak(Vector<Isotope> FoundIsotopes, Vector<NcPeak> Peak_Data) {
		Vector<Isotope> Result = new Vector<Isotope>();
		if (FoundIsotopes == null)
			return Result;

		Result.addAll(FoundIsotopes);
		// for(int i=0; i<Result.size();i++){
		// Result.get(i).Peaks.addAll(Result.get(i).Unknown_Peak);
		//// }
		Vector<Integer> UnknownPeakArrayNum = new Vector<Integer>();

		/// 1
		boolean check = false;
		for (int i = 0; i < Peak_Data.size(); i++) {

			for (int k = 0; k < FoundIsotopes.size(); k++) {
				Isotope tempIso = FoundIsotopes.get(k);

				for (int q = 0; q < tempIso.Peaks.size(); q++) {
					if (tempIso.Peaks.get(q).Energy_InWindow(Peak_Data.get(i).Peak_Energy))
						check = true;
				}
				for (int q = 0; q < tempIso.Unknown_Peak.size(); q++) {
					if (tempIso.Unknown_Peak.get(q).Energy_InWindow(Peak_Data.get(i).Peak_Energy))
						check = true;
				}
			}
			if (check == false)
				UnknownPeakArrayNum.add(i);

			check = false;
		}
		/// 2

		Vector<NcPeak> UnknownPeaks = new Vector<NcPeak>();

		/// 3
		for (int i = 0; i < UnknownPeakArrayNum.size(); i++) {
			UnknownPeaks.add(Peak_Data.get(UnknownPeakArrayNum.get(i)));
		}
		/// 3.2
		if (UnknownPeaks.size() != 0) {
			Isotope uknown_iso = new Isotope();
			// uknown_iso.isotopes = "Unknown";
			uknown_iso.isotopes = "UNK";
			uknown_iso.FoundPeaks = UnknownPeaks;
			uknown_iso.Class = uknown_iso.CLASS_UNK;
			Result.add(uknown_iso);
		}
		/// 4 Ba-133 _ 161 kev Unkown
		/*
		 * for(int i=0; i<FoundIsotopes.size(); i++){
		 * if(FoundIsotopes.get(i).isotopes.matches("Ba-133")){ //if there is Ba133
		 * for(int k=0; k<Result.size(); k++){
		 * if(Result.get(k).Class.matches(".*UNK.*")){ // in UNKs
		 * 
		 * Vector<NcPeak> TempPeak = Result.get(k).FoundPeaks; for(int q =0;
		 * q<TempPeak.size();q++){
		 * if(TempPeak.get(q).Energy_InWindow(161)){Result.remove(q);break;} }
		 * 
		 * } } } }
		 */

		return Result;
	}/*
		 * private Vector<Isotope> Find_UnknownPeak(Vector<Isotope> FoundIsotopes, int
		 * Energy[],int channel[], int VallyCh[],double VallyAB[], int Energy_Count){
		 * Vector<Isotope> Result = new Vector<Isotope>(); if(FoundIsotopes == null)
		 * return Result;
		 * 
		 * 
		 * Result.addAll(FoundIsotopes); Vector<Integer> UnknownPeakArrayNum = new
		 * Vector<Integer>();
		 * 
		 * 
		 * ///1 boolean check =false; for(int i=0; i<Energy_Count; i++){ for(int k=0;
		 * k<FoundIsotopes.size(); k++){
		 * 
		 * if(FoundIsotopes.get(k).Energy1 != 0){ double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(FoundIsotopes.get(k).Energy1); double
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); double R_ROI_Percent =
		 * 1.0+(Roi_window*0.01);
		 * 
		 * if(FoundIsotopes.get(k).Energy1*L_ROI_Percent < Energy[i] &&
		 * FoundIsotopes.get(k).Energy1*R_ROI_Percent > Energy[i]) check =true; }
		 * if(FoundIsotopes.get(k).Energy2 != 0){ double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(FoundIsotopes.get(k).Energy2); double
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); double R_ROI_Percent =
		 * 1.0+(Roi_window*0.01);
		 * 
		 * if(FoundIsotopes.get(k).Energy2*L_ROI_Percent < Energy[i] &&
		 * FoundIsotopes.get(k).Energy2*R_ROI_Percent > Energy[i]) check =true;
		 * 
		 * } if(FoundIsotopes.get(k).Energy3 != 0){ double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(FoundIsotopes.get(k).Energy3); double
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); double R_ROI_Percent =
		 * 1.0+(Roi_window*0.01);
		 * 
		 * if(FoundIsotopes.get(k).Energy3*L_ROI_Percent < Energy[i] &&
		 * FoundIsotopes.get(k).Energy3*R_ROI_Percent > Energy[i]) check =true;
		 * 
		 * } if(FoundIsotopes.get(k).Energy4 != 0){ double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(FoundIsotopes.get(k).Energy4); double
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); double R_ROI_Percent =
		 * 1.0+(Roi_window*0.01);
		 * 
		 * if(FoundIsotopes.get(k).Energy4*L_ROI_Percent < Energy[i] &&
		 * FoundIsotopes.get(k).Energy4*R_ROI_Percent > Energy[i]) check =true;
		 * 
		 * } if(FoundIsotopes.get(k).Energy5 != 0){ double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(FoundIsotopes.get(k).Energy5); double
		 * L_ROI_Percent = 1.0-(Roi_window*0.01); double R_ROI_Percent =
		 * 1.0+(Roi_window*0.01);
		 * 
		 * if(FoundIsotopes.get(k).Energy5*L_ROI_Percent < Energy[i] &&
		 * FoundIsotopes.get(k).Energy5*R_ROI_Percent > Energy[i]) check =true;
		 * 
		 * } } if(check == false) UnknownPeakArrayNum.add(i);
		 * 
		 * check = false; } ///2 /*for(int i=0; i<FoundIsotopes.size(); i++){ // if() }
		 * 
		 * ///3 for(int i=0; i<UnknownPeakArrayNum.size(); i++){ Isotope temp = new
		 * Isotope(); temp.isotopes = "Uknown"; temp.Class ="UNK"; temp.Channel1 =
		 * channel[UnknownPeakArrayNum.get(i)]; temp.Energy1 =
		 * Energy[UnknownPeakArrayNum.get(i)]; temp.Channel1_AB.x = (float)
		 * VallyAB[UnknownPeakArrayNum.get(i)*2]; temp.Channel1_AB.y = (float)
		 * VallyAB[(UnknownPeakArrayNum.get(i)*2)+1]; temp.Channel1_Vally.x =
		 * VallyCh[(UnknownPeakArrayNum.get(i)*2)]; temp.Channel1_Vally.y =
		 * VallyCh[(UnknownPeakArrayNum.get(i)*2)+1];
		 * 
		 * Result.add(temp); } ///4 Ba-133 _ 161 kev Unkown for(int i=0;
		 * i<FoundIsotopes.size(); i++){
		 * if(FoundIsotopes.get(i).isotopes.matches("Ba-133")){ //if there is Ba133
		 * for(int k=0; k<Result.size(); k++){
		 * if(Result.get(k).Class.matches(".*UNK.*")){ // in UNKs double Roi_window =
		 * NcLibrary.Get_Roi_window_by_energy(161); double L_ROI_Percent =
		 * 1.0-(Roi_window*0.01); double R_ROI_Percent = 1.0+(Roi_window*0.01);
		 * 
		 * if(161*L_ROI_Percent < Result.get(k).Energy1 &&161*R_ROI_Percent >
		 * Result.get(k).Energy1){ Result.remove(k); break; } } } } }
		 * 
		 * return Result; }
		 */

	public Isotope Get_Isotope(String IsotopeName) {
		Isotope result = new Isotope();
		if (mSel_Library.isEmpty() == true)
			return result;

		for (int i = 0; i < mSel_Library.size(); i++) {
			if (mSel_Library.get(i).isotopes.matches(IsotopeName))
				return mSel_Library.get(i);
		}

		return result;
	}

	private Vector<Isotope> Filter_IdLogic_Algorithm_1(Vector<Isotope> targetIsotope) { // if
																						// ID
																						// U238,
																						// Ga67,Ra226
																						// or
																						// Cs137,
																						// inhibit
																						// U235
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		boolean IsThereCs137 = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-238"))
				IsThereTagetIso = true;
			else if (targetIsotope.get(i).isotopes.matches("Ga-67"))
				IsThereTagetIso = true;
			else if (targetIsotope.get(i).isotopes.matches("Ra-226"))
				IsThereTagetIso = true;
			else if (targetIsotope.get(i).isotopes.matches("Cs-137")) {
				IsThereTagetIso = true;
				IsThereCs137 = true;
			}
		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-235"))
					targetIsotope.remove(i);
			}
		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-235HE"))
					targetIsotope.remove(i);
			}
		}

		if (IsThereCs137 == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-233"))
					targetIsotope.remove(i);
			}
		}
		return targetIsotope;
	}

	private Vector<Isotope> Filter_IdLogic_Algorithm_2(Vector<Isotope> targetIsotope) { // if
																						// ID
																						// U235
																						// or
																						// U235HE
																						// ,
																						// inhibit
																						// Tc-99m
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235"))
				IsThereTagetIso = true;
			else if (targetIsotope.get(i).isotopes.matches("U-235HE"))
				IsThereTagetIso = true;
		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Tc-99m"))
					targetIsotope.remove(i);
			}
		}

		return targetIsotope;
	}

	private Vector<Isotope> Filter_IdLogic_Algorithm_3(Vector<Isotope> targetIsotope) { // if
																						// is
																						// there
																						// Th-232
																						// ,
																						// inhibit
																						// Co-57,Tc-99m
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Th-232"))
				IsThereTagetIso = true;
		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Co-57"))
					targetIsotope.remove(i);
				else if (targetIsotope.get(i).isotopes.matches("Tc-99m"))
					targetIsotope.remove(i);
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_4(Vector<Isotope> targetIsotope, int cnt_100keV, int cnt_186keV) { // If
																														// ID
																														// U235
																														// then
																														// check
																														// ratio
																														// of
																														// 100
																														// keV
																														// (x-rays)
																														// peak/186
																														// keV
																														// peak,
																														// if
																														// >1
																														// inhibit
																														// U235
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235"))
				IsThereTagetIso = true;
		}

		if (IsThereTagetIso == true & cnt_100keV > cnt_186keV) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-235"))
					targetIsotope.remove(i);
			}
		}
		/// ----------------
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235HE"))
				IsThereTagetIso = true;
		}

		if (IsThereTagetIso == true & cnt_100keV > cnt_186keV) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-235HE"))
					targetIsotope.remove(i);
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_5(int[] RealSPC, int AcqTime, int[] RealBG, int BgAcqTime,
			Vector<Isotope> targetIsotope) { // If ID I125, I123, Am241, Co57,
												// U235 or Tc99m and Np/BKG (in
												// those ROIs) is <4 for that
												// specific isotope then inhibit
												// the ID of that specific
												// isotope (for ID of I123 use
												// ratio for 28 keV peak).
		// Vector<Isotope> temp;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		int[] ms = new int[1024];
		double[] bg = new double[1024];

		for (int i = 0; i < 1024; i++) {
			ms[i] = RealSPC[i];
			bg[i] = ((double) RealBG[i] / BgAcqTime) * AcqTime;
		}

		// -
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("I-125")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("I-123")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Co-57")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235HE")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Tc-99m")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Am-241")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_6(Vector<Isotope> targetIsotope) { // If
																						// ID
																						// Cs137,
																						// Am241,
																						// Pu239,
																						// 131
																						// or
																						// In111
																						// then
																						// inhibit
																						// I125
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Cs-137") | targetIsotope.get(i).isotopes.matches("Am-241")
					| targetIsotope.get(i).isotopes.matches("Pu-239") | targetIsotope.get(i).isotopes.matches("Pu-131")
					| targetIsotope.get(i).isotopes.matches("In-111")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-125")) {
					targetIsotope.remove(i);
				}
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_7(Vector<Isotope> targetIsotope) { // If
																						// ID
																						// Ba133
																						// inhibit
																						// I123
																						// and
																						// I125
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-123") | targetIsotope.get(i).isotopes.matches("I-125")) {
					targetIsotope.remove(i);
					i -= 1;
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_8(Vector<Isotope> targetIsotope) { // If
																						// ID
																						// U235
																						// inhibit
																						// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235") | targetIsotope.get(i).isotopes.matches("U-235HE")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-123")) {
					targetIsotope.remove(i);
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_10(int[] RealSPC, int AcqTime, int[] RealBG, int BgAcqTime,
			double CaliA, double CaliB, double CaliC, Vector<Isotope> targetIsotope) { // If
																						// ID
																						// U235
																						// inhibit
																						// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		int[] ms = new int[1024];
		double[] bg = new double[1024];

		for (int i = 0; i < 1024; i++) {
			ms[i] = RealSPC[i];
			bg[i] = ((double) RealBG[i] / BgAcqTime) * AcqTime;
		}

		////////// Co-60
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Co-60")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			if (ms[NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(225, CaliA, CaliB, CaliC))] < bg[NcLibrary
					.Auto_floor(NcLibrary.Energy_to_Channel(225, CaliA, CaliB, CaliC))] * 4) {
				for (int i = 0; i < targetIsotope.size(); i++) {
					if (targetIsotope.get(i).isotopes.matches("U-233")) {
						targetIsotope.remove(i);
					}
				}
			}
		}
		IsThereTagetIso = false;
		//// Ba-133
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			if (ms[(int) NcLibrary.Energy_to_Channel(295, CaliA, CaliB, CaliC)]
					* 2 > ms[(int) NcLibrary.Energy_to_Channel(141, CaliA, CaliB, CaliC)]) {
				for (int i = 0; i < targetIsotope.size(); i++) {
					if (targetIsotope.get(i).isotopes.matches("Tc-99m")) {
						targetIsotope.remove(i);
					}
				}
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_11(int[] RealSPC, int AcqTime, int[] RealBG, int BgAcqTime,
			double CaliA, double CaliB, double CaliC, Vector<Isotope> targetIsotope) { // If
																						// ID
																						// U235
																						// inhibit
																						// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		int[] ms = new int[1024];
		double[] bg = new double[1024];

		for (int i = 0; i < 1024; i++) {
			ms[i] = RealSPC[i];
			bg[i] = ((double) RealBG[i] / BgAcqTime) * AcqTime;
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ra-226")) {

				IsThereTagetIso = true;
			}

		}
		//////////
		if (IsThereTagetIso) {
			if (ms[(int) NcLibrary.Energy_to_Channel(636, CaliA, CaliB,
					CaliC)] < bg[(int) NcLibrary.Energy_to_Channel(636, CaliA, CaliB, CaliC)] * 4) {
				for (int i = 0; i < targetIsotope.size(); i++) {
					if (targetIsotope.get(i).isotopes.matches("I-131")) {
						targetIsotope.remove(i);
					}
				}
			}
		}

		return targetIsotope;
	}

	public Isotope get_iso() {
		Isotope iso = new Isotope();
		return iso;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_12(Vector<Isotope> targetIsotope) { // If
																						// ID
																						// U235
																						// inhibit
																						// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ra-226")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-131")) {
					targetIsotope.remove(i);
				}
			}
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
					targetIsotope.remove(i);
				}
			}
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("In-111")) {
					targetIsotope.remove(i);
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_13(Vector<Isotope> targetIsotope) { // If
																						// ID
																						// Ba133
																						// inhibit
																						// I123
																						// and
																						// I125
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
				for (int k = 0; k < targetIsotope.size(); k++) {
					if (targetIsotope.get(k).isotopes.matches("Cs-137")) {
						IsThereTagetIso = true;
						break;
					}
				}
				break;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-131")) {
					targetIsotope.remove(i);
					break;
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_14(Vector<Isotope> targetIsotope) { // If
																						// ID
																						// Ba133
																						// inhibit
																						// I123
																						// and
																						// I125
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Th-232")) {
				IsThereTagetIso = true;
				break;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("F-18")) {
					targetIsotope.remove(i);
					break;
				}
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Tc-99m")) {
					targetIsotope.remove(i);
					break;
				}
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_15(Vector<Isotope> targetIsotope, Vector<NcPeak> Peak_Data) { // If
		// ID
		// //
		// I-131
		// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("I-131")) {
				IsThereTagetIso = true;
			}
		}

		for (int i = 0; i < Peak_Data.size(); i++) {

			if (Energy_Check(Peak_Data.get(i).Peak_Energy)) {
				IsThereTagetIso = true;
			}

		}

		if (IsThereTagetIso)

		{
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
					targetIsotope.remove(i);
				}
			}
		}

		return targetIsotope;

	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_16(Vector<Isotope> targetIsotope, Spectrum SPC, int[] RealSPC,
			int AcqTime, int[] RealBG, int BgAcqTime) { // If

		int[] ms = new int[1024];
		double[] bg = new double[1024];

		for (int i = 0; i < 1024; i++) {
			ms[i] = RealSPC[i];
			bg[i] = ((double) RealBG[i] / BgAcqTime) * AcqTime;
		}
		int I131_364En = SpcAnalysis.ToChannel(364, SPC.Get_Coefficients());

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("I-131")) {
				if (ms[I131_364En] < (int) (bg[I131_364En] * 10)) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_Nucare1(Spectrum spc, Vector<Isotope> targetIsotope,
			Vector<NcPeak> FoundPeak) { // If ID U235 inhibit I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Cs-137")) {
				IsThereTagetIso = true;
			}
			if (targetIsotope.get(i).isotopes.matches("Ra-226")) {
				return targetIsotope;
			}
		}
		if (IsThereTagetIso) {
			double measu_sum = 0;
			boolean check = false;
			Isotope Ra226 = Get_Isotope("Ra-226");
			for (int i = 0; i < FoundPeak.size(); i++) {
				if (FoundPeak.get(i).Energy_InWindow(1120)) { // Ra133 Peak
					double measu = Math.abs((((FoundPeak.get(i).Peak_Energy - 1120) / 1120) * 100));
					measu = 100 - measu;
					measu_sum += measu;

					check = true;
					Ra226.FoundPeaks.add(FoundPeak.get(i));
					break;
				}
			}
			if (check) {
				check = false;
				for (int i = 0; i < FoundPeak.size(); i++) {
					if (FoundPeak.get(i).Energy_InWindow(1780)) { // Ra133 Peak
						double measu = Math.abs((((FoundPeak.get(i).Peak_Energy - 1780) / 1780) * 100));
						measu = 100 - measu;
						measu_sum += measu;

						check = true;
						Ra226.FoundPeaks.add(FoundPeak.get(i));
						break;
					}
				}
				if (check) {
					Ra226.Confidence_Level = measu_sum / 2;
					targetIsotope.add(Ra226);
					return targetIsotope;
				}
			}
		}

		return targetIsotope;
	}

	//18.06.07
	//because: all always keep memory in PeakIsoMatching_H
	public Vector<Isotope> IsoRemoveLines(Vector<Isotope> result2,Vector<NcPeak> FoundPeak)
	{
		//Step1: Remove energy which is not  peak

		int NoPeak=FoundPeak.size();

		for(int noiso=0;noiso<result2.size();noiso++)
		{
			for(int EnIdx=0;EnIdx<result2.get(noiso).FoundPeaks.size();EnIdx++)
			{
				boolean flg=false;

				for(int k=0;k<NoPeak;k++)
				{
					if(result2.get(noiso).FoundPeaks.get(EnIdx).Peak_Energy==FoundPeak.get(k).Peak_Energy)
					{
						flg=true;
					}
				}

				if(flg==false)
				{
					result2.get(noiso).FoundPeaks.remove(EnIdx);
					result2.get(noiso).FoundPeakBR.remove(EnIdx);
					result2.get(noiso).IsoPeakEn.remove(EnIdx);
					EnIdx--;
				}

			}
		}



		//Step 2: Remove energy which is copied double times
		for(int noiso=0;noiso<result2.size();noiso++)
		{
			for(int EnIdx=0;EnIdx<result2.get(noiso).FoundPeaks.size();EnIdx++)
			{
				int cnt=0;
				for(int EnIdx1=0;EnIdx1<result2.get(noiso).FoundPeaks.size();EnIdx1++)
				{
					if(EnIdx!=EnIdx1)
					{
						if(EnIdx1!=EnIdx)
						{
							if(result2.get(noiso).FoundPeaks.get(EnIdx).Peak_Energy==result2.get(noiso).FoundPeaks.get(EnIdx1).Peak_Energy)
							{
								cnt=cnt+1;
							}
						}
					}
				}

				//remove peak
				if(cnt>0)
				{
					result2.get(noiso).FoundPeaks.remove(EnIdx);
					result2.get(noiso).FoundPeakBR.remove(EnIdx);
					result2.get(noiso).IsoPeakEn.remove(EnIdx);
					EnIdx--;
				}
			}
		}
		return result2;
	}

	public boolean Energy_Check(double energy) {

		double mCompareEnergy = 634;
		double L_ROI_Percent = mCompareEnergy - (mCompareEnergy * 0.07);
		double R_ROI_Percent = mCompareEnergy + (mCompareEnergy * 0.07);

		if (L_ROI_Percent <= energy && R_ROI_Percent >= energy) {
			return true;
		} else
			return false;
	}

	public Vector<Isotope> AddPeakDraw(Vector<Isotope> result2,Vector<NcPeak> mFoundPeak_data,double[] FWHMCoeff,Coefficients coeff, double WndROI)
	{
		double EnTemp, BrTemp,L_ROI,R_ROI;
		double [] Thshold=new double [2];

		Isotope SourceInfo = new Isotope();

		for (int i = 0; i < mSel_Library.size(); i++)
		{
			//Step 1: Reset memory
			SourceInfo = mSel_Library.get(i);

			/*
				if(SourceInfo.FoundPeaks.size()>0)
				{
					for(int ii=0;ii<SourceInfo.FoundPeaks.size();ii++)
					{
						SourceInfo.FoundPeaks.remove(ii);

						ii--;
					}

					for(int ii=0;ii<SourceInfo.FoundPeakBR .size();ii++)
					{
						SourceInfo.FoundPeakBR.remove(ii);

						ii--;
					}

					for(int ii=0;ii<SourceInfo.FoundPeakBR .size();ii++)
					{
						SourceInfo.FoundPeakBR.remove(ii);

						ii--;
					}
				}

			 */

			//Step 2: Compare with library isotope
			for(int j=0;j<result2.size();j++)
			{
				//Adding: Major peak
				if (result2.get(j).isotopes.equals(SourceInfo.isotopes))
				{
					//Adding Major Peak
					for(int k=0;k<SourceInfo.Peaks.size();k++)
					{
						EnTemp=SourceInfo.Peaks.get(k).Peak_Energy;
						BrTemp=SourceInfo.Peaks.get(k).Isotope_Gamma_En_BR;

						Thshold=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(EnTemp, FWHMCoeff,coeff,WndROI);

						L_ROI=Thshold[0];
						R_ROI = Thshold[1];

						for (int q = 0; q < mFoundPeak_data.size(); q++)
						{
							double Entemp=mFoundPeak_data.get(q).Peak_Energy;

							if(Entemp>L_ROI&&Entemp<R_ROI)
							{
								result2.get(j).ListPeakDrawEn.add(mFoundPeak_data.get(q));

								result2.get(j).ListPeakDrawBR.add(BrTemp);

								break;
							}
						}
					}

					//Adding: Minor Peak
					for(int k=0;k<SourceInfo.IsoMinorPeakEn.size();k++)
					{
						EnTemp=SourceInfo.IsoMinorPeakEn.get(k);
						BrTemp=SourceInfo.IsoMinorPeakBR.get(k);

						Thshold=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(EnTemp, FWHMCoeff,coeff,WndROI);

						L_ROI=Thshold[0];
						R_ROI = Thshold[1];

						for (int q = 0; q < mFoundPeak_data.size(); q++)
						{
							double Entemp=mFoundPeak_data.get(q).Peak_Energy;

							if(Entemp>L_ROI&&Entemp<R_ROI)
							{
								result2.get(j).ListPeakDrawEn.add(mFoundPeak_data.get(q));

								result2.get(j).ListPeakDrawBR.add(BrTemp);

								break;
							}
						}
					}
				}

			}
		}

		return result2;
	}

}// end class
