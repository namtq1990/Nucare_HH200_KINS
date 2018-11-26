package android.HH100.Identification;

import java.io.File;
import java.util.Vector;

import NcLibrary.NcMath;
import NcLibrary.SpcAnalysis;
import android.HH100.Identification.FindPeaksM.FindPC1;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.Spectrum;
import android.widget.GridLayout.Spec;

public class FindPeak {

	public static Vector<NcPeak> Find_Peak(Spectrum MS, Spectrum BG, float input_PeakDetectionThreshold) {

		double[] tempMS = NcLibrary.ft_smooth(MS.ToDouble(), 0.015, 2.96474);
		double[] tempBG = NcLibrary.ft_smooth(BG.ToDouble(), 0.015, 2.96474);

		tempMS = Nomalization(tempMS, MS.Get_AcqTime(), 1);
		tempBG = Nomalization(tempBG, BG.Get_AcqTime(), 1);

		double[] sbt_Log = Subtraction(ToLogScale(tempMS), ToLogScale(tempBG));
		double[] sbt_Linear = Subtraction2(tempMS, tempBG);

		Spectrum sbt_LogSpc = new Spectrum(sbt_Log);
		sbt_LogSpc.Set_Coefficients(MS.Get_Coefficients());

		// Work 1-2
		// �α� �����͸� exponentional�� Ǯ����.
		// �α״� 10���� �ϰ� Ǫ�°� exp�� Ǫ�� ������ �������� ���� ������Ű���� �Ͽ���.
		double[] PrincipalComponent_D = sbt_Log;
		for (int i = 0; i < PrincipalComponent_D.length; i++) {
			PrincipalComponent_D[i] = Math.pow(Math.exp(1), PrincipalComponent_D[i]);
		}

		// Work 1-3
		// �α׸� ���ﶧ 1�� �����༭ �α׸� Ǯ�� �ּҰ��� 0�� �ƴ� 1�� ������.
		// ��ü������ ����Ʈ���� ������ �ִ� ���� ���߾� �ִ� �κ���.
		// ������ -1�� �Ͽ����� �������� �����Ͽ� 2�� ���� �� 2�� ���� �Ǿ���.
		for (int i = 0; i < PrincipalComponent_D.length; i++) {
			PrincipalComponent_D[i] = PrincipalComponent_D[i] * 2 - 2;
		}

		// Work 2. PCA
		double[] FirstPC = PCA(PrincipalComponent_D);
		// NcLibrary.Export_spectrum_data("mms_Pca.txt", FirstPC, 1024);

		// Work 3.
		// ���� ��ũ�� ã�� ���� ������ �κ��� ������ ��ü������ ����Ʈ���� x�� ���� �ø��� �κ���.
		double mmiinn = 0;
		for (int i = 1; i < FirstPC.length; i++) {
			if (i == 1) {
				if (FirstPC[0] < FirstPC[1])
					mmiinn = FirstPC[0];
				else
					mmiinn = FirstPC[1];
			} else {
				if (FirstPC[i] < mmiinn)
					mmiinn = FirstPC[0];
			}
		}

		for (int i = 1; i < FirstPC.length; i++) {
			FirstPC[i] = FirstPC[i] - mmiinn;
		}
		// --------------------

		// Work 4.
		Vector<NcPeak> FoundPeaks = WBPD(MS, FirstPC, ToLogScale(tempMS), sbt_Linear, input_PeakDetectionThreshold);

		// Work 5.
		FoundPeaks = Find_Valley(MS, BG, FoundPeaks);

		return FoundPeaks;
	}

	private static double[] Nomalization(double[] spc, int AcqTime, int ResultTime) {
		if (AcqTime == 0)
			return spc;

		double[] temp = new double[spc.length];

		for (int i = 0; i < spc.length; i++) {
			temp[i] = (spc[i] / AcqTime) * ResultTime;
		}

		return temp;
	}

	private static double[] ToLogScale(double[] spc) {
		double[] result = new double[spc.length];

		for (int i = 0; i < spc.length; i++) {
			// ���⼭ 1�� ���� ������ log(0)�� �����ϱ� ������.
			// ���߿� �� �κ��� �α׸� Ǯ���� ������ ����ȭ ��.
			result[i] = Math.log10(spc[i] + 1) / Math.log10(i + 10.0);
		}

		return result;
	}

	private static double[] Subtraction(double[] spc1, double[] spc2) {
		double[] result = new double[spc1.length];

		for (int i = 0; i < result.length; i++) {
			result[i] = spc1[i] - spc2[i];
			// ��׶��� ���� ����Ʈ���� ������ 0���� �������.
			if (result[i] < 0)
				result[i] = 0;
		}

		return result;
	}

	private static double[] Subtraction2(double[] Nomalized_spc1, double[] Nomalized_spc2) {
		double[] result = new double[Nomalized_spc1.length];

		double prst = 0;
		for (int i = 0; i < Nomalized_spc1.length; i++) {

			double tempval = (float) (Nomalized_spc1[i] - Nomalized_spc2[i]);

			if (Nomalized_spc1[i] <= 0 || (Nomalized_spc1[i] < Nomalized_spc2[i])) {

				prst = 1;

			}

			else {

				prst = (float) (tempval / (Nomalized_spc1[i]));

			}

			result[i] = tempval * prst;
		}

		return result;
	}

	private static double[] PCA(double[] spc) {
		// ǥ��ȭ �����ִ� �κ���.
		// ����Ʈ�� ������ ����� ���� ǥ�������� ������ �ְ� ��.
		// Ư�̻����� ���� �� ������ ���� ���� ����.

		// double[] Smtd_Spc = NcLibrary.ft_smooth(spc,0.015, 2.96474);
		double[] FirstPC = new double[spc.length];
		int NumOfData_PCA = 1;

		FindPC1 CFPC = new FindPC1();
		CFPC.FindPC_main(spc, FirstPC, NumOfData_PCA);

		if (FindMax(FirstPC) < (FindMin(FirstPC) * (-1))) {

			for (int i = 0; i < FirstPC.length; i++) {
				FirstPC[i] *= -1;
			}
		}

		return FirstPC;
	}

	private static double FindMax(double data[]) {

		double maxVal = -10000;
		for (int i = 0; i < data.length; i++) {
			if (maxVal < data[i])
				maxVal = data[i];
		}
		return maxVal;
	}

	private static double FindMin(double data[]) {

		double minVal = 10000;
		for (int i = 0; i < data.length; i++) {
			if (minVal > data[i])
				minVal = data[i];
		}
		return minVal;

	}

	private static Vector<NcPeak> WBPD(Spectrum OriginalSPC, double[] FirstPC_SPC, double[] LogSPC,
			double[] LinearSbtSPC, float Threshold) {

		double[] Orgiginal_SPC = OriginalSPC.ToDouble();
		Vector<NcPeak> result = new Vector<NcPeak>();

		int i;

		double deltta = Threshold;
		double peakdet_temp = 0;
		double mn = 1.7 * Math.pow(10.0, 308);
		double mx = -1.7 * 1.7 * Math.pow(10.0, 308);

		int mnpos = 0, mxpos = 0;
		boolean lookformax = true;

		int maxvalue = 0;

		int right = 0;
		int left = 0;

		int energy_max = 0;
		int energy_right = 0;
		int energy_left = 0;

		// M.Comment 5-1
		// �⺻������ ��ũã�� ����� ���������� �ϳ��� ���� Max ���� ã��
		// Max ���� ã���� �������� Max-���簪 > Threshold �� �Ѵ� ������ ����� Max �� peak�� ������.
		// �嵥 peak ��翡 ���� �Ķ���Ͱ� ���� ��� peak�� �ƴ� �͵� ã�� �� ����.
		// �׷��� �յ� 7���θ� �˻��Ͽ� �⺻������ �ﰢ�� ����� ������ �߰��� �˻��ϰ� ��.
		// ���� ��׶��� ���� FP_m_nor_sbt �� ã�ƺ��� ������ ī��Ʈ�� ���ϰ� �ִ��� ���� �˻��ϰ� ��.
		for (i = 1; i < FirstPC_SPC.length; i++) {
			if (i > 800)
				deltta = 0.005;

			peakdet_temp = FirstPC_SPC[i];

			if (peakdet_temp > mx) {
				mx = peakdet_temp;
				mxpos = i;
			}

			if (peakdet_temp < mn) {
				mn = peakdet_temp;
				mnpos = i;
			}

			if (lookformax) {

				if (peakdet_temp < (mx - deltta) && mxpos > 3) {
					// ��׶��带 ���鼭 peak ��ġ�� ���ϴ� �Ϳ� ���� ������ ����.
					// peak�� FP_m_FirstPC�� ã�� ������ FP_log_data(���� ����Ʈ�� �α����� ��)��
					// �ϰ� ��.
					// ���������� ���� ������.
					for (int xxx = mxpos - 4; xxx < mxpos + 4; xxx++) {
						if (xxx == mxpos - 4) {
							maxvalue = xxx;
							continue;
						}

						if (LogSPC[maxvalue] < LogSPC[xxx]) {
							maxvalue = xxx;
						}
					}

					for (int j1 = maxvalue + 1; j1 < LogSPC.length; j1++) {

						energy_max = (int) Math.rint(SpcAnalysis.ToEnergy(maxvalue, OriginalSPC.Get_Coefficients()));
						energy_right = (int) Math.rint(SpcAnalysis.ToEnergy(j1, OriginalSPC.Get_Coefficients()));

						if (energy_right - energy_max > energy_max * 0.07) {
							right = j1;
							break;
						}
					}

					for (int j1 = maxvalue - 1; j1 > -1; j1--) {

						energy_max = (int) Math.rint(SpcAnalysis.ToEnergy(maxvalue, OriginalSPC.Get_Coefficients()));
						energy_left = (int) Math.rint(SpcAnalysis.ToEnergy(j1, OriginalSPC.Get_Coefficients()));

						if (energy_max - energy_left > energy_max * 0.07) {
							left = j1;
							break;
						}
					}

					// ���� �յ� 7���ο��� ��ũ������ ���ϴ��� ���� FP_log_data�� �˻��ϰԵ�.
					// ���緮 ī��Ʈ�� ���ϴ°� FP_m_nor_sbt�� �����.
					if (((LogSPC[maxvalue] - LogSPC[right] > deltta) && (LogSPC[maxvalue] - LogSPC[left] > deltta))
							&& (LinearSbtSPC[maxvalue] > 0.1)) // wavelet 0.7
					{

						mn = peakdet_temp;
						mnpos = i;
						// ã�� peak�� ���� ����Ʈ������ ������ �𸣴� peak shift �� ���� ������.
						// �Ʊ�� FP_log_data�� �Ͽ��� ������ ���� ����Ʈ���� ������ ������.
						for (int xxx = mxpos - 4; xxx < mxpos + 4; xxx++) {
							if (xxx == mxpos - 4) {
								maxvalue = xxx;
								continue;
							}

							if (Orgiginal_SPC[maxvalue] < Orgiginal_SPC[xxx]) {
								maxvalue = xxx;
							}
						}

						// �븮ã�� �κ���.
						// ���� �յ� 7���θ� �븮�� �Ǵ��Ͽ� ���� �Ѱ��ְ� �Ǿ�����.
						// 2013.0911 ����������.
						for (int j1 = maxvalue + 1; j1 < OriginalSPC.Get_Ch_Size(); j1++) {

							energy_max = (int) Math
									.rint(SpcAnalysis.ToEnergy(maxvalue, OriginalSPC.Get_Coefficients()));
							energy_right = (int) Math.rint(SpcAnalysis.ToEnergy(j1, OriginalSPC.Get_Coefficients()));

							if (energy_right - energy_max > energy_max * 0.10) //// M.Comment
																				//// 6
																				//// window
							{
								right = j1;
								break;
							}
						}

						for (int j1 = maxvalue - 1; j1 > -1; j1--) {

							energy_max = (int) Math
									.rint(SpcAnalysis.ToEnergy(maxvalue, OriginalSPC.Get_Coefficients()));

							energy_left = (int) Math.rint(SpcAnalysis.ToEnergy(j1, OriginalSPC.Get_Coefficients()));

							if (energy_max - energy_left > energy_max * 0.10) //// M.Comment
																				//// 6
																				//// window
							{
								left = j1;
								break;
							}
						}

						NcPeak peak = new NcPeak();

						peak.Channel = maxvalue;
						peak.Peak_Energy = SpcAnalysis.ToEnergy(maxvalue, OriginalSPC.Get_Coefficients());
						peak.Vally_CH.x = left;
						peak.Vally_CH.y = right;

						result.add(peak);
						maxvalue = 0;
					}

					////////////////////// min0610

					lookformax = false;
					if (result.size() > 100)
						break;
				}
			} else {
				if (peakdet_temp > (mn + deltta / 100)) {
					mx = peakdet_temp;
					mxpos = i;
					lookformax = true;
				}
			}
		}

		return result;
	}

	private static Vector<NcPeak> Find_Valley(Spectrum MS, Spectrum BG, Vector<NcPeak> Peaks) {
		Vector<NcPeak> resultPeaks = new Vector<NcPeak>();

		double[] TempMS = MS.ToDouble();
		double[] TempBG = BG.ToDouble();
		// �븮ã�� �κ�
		// 5pt�� �̿��Ͽ� ã��. ����� log data�� �̿��Ͽ���.

		for (int i = 0; i < MS.Get_Ch_Size(); i++) {
			TempMS[i] = TempMS[i] - ((TempBG[i] / (double) BG.Get_AcqTime()) * (double) MS.Get_AcqTime());
		}

		TempMS = NcLibrary.ft_smooth(TempMS, 0.015, 2.96474);// 0.007413

		for (int i = 0; i < Peaks.size(); i++) {

			double Roi_window = NcLibrary.Get_Roi_window_by_energy_VAllY(Peaks.get(i).Peak_Energy);
			double L_ROI_Percent = 1.0 - (Roi_window * 0.01);
			double R_ROI_Percent = 1.0 + (Roi_window * 0.01);

			L_ROI_Percent = (SpcAnalysis.ToChannel(Peaks.get(i).Peak_Energy * L_ROI_Percent, MS.Get_Coefficients()));// *(1-factor);
			R_ROI_Percent = (SpcAnalysis.ToChannel(Peaks.get(i).Peak_Energy * R_ROI_Percent, MS.Get_Coefficients()));// *(1+factor);

			NcPeak Result = Find_Vally(TempMS, (int) L_ROI_Percent, (int) R_ROI_Percent, Peaks.get(i).Channel);
			Result.Peak_Energy = Peaks.get(i).Peak_Energy;
			resultPeaks.add(Result);

		}
		return resultPeaks;
	}

	private static NcPeak Find_Vally(double[] arrfloat, int firstlocation, int secondlocation, int peakvalue) {
		NcPeak result = new NcPeak();
		// peak value�� ������ ����� 0�� return
		if (peakvalue == 0) {
			return result; //
		}
		double startpx = 0;
		double startpy = 0;
		double endpx = 0;
		double endpy = 0;
		double midpx = 0;
		double midpy = 0;

		// int center = firstlocation+ (int)((secondlocation -
		// firstlocation)/2);
		int center = peakvalue;
		int contcnt = 0;
		int cntlimit = 2;// NcLibrary.Auto_floor(NcLibrary.Get_Roi_window_by_energy(NcLibrary.Channel_to_Energy(peakvalue,
							// calib_A2, calib_B2, calib_C2)));
		double threshold = 0;

		int findflag = 0;
		for (int i = center - (int) (center * 0.01); i > firstlocation; i--) {
			if ((arrfloat[i - 1] - arrfloat[i]) >= threshold) {
				if (findflag == 0) {
					findflag = 1;
					contcnt = 1;
				} else {
					contcnt++;
					if (contcnt >= cntlimit) {
						startpx = i + (cntlimit - 1);
						startpy = arrfloat[i];
						break;
					}
				}
			} else {
				findflag = 0;
				contcnt = 0;
			}
		}
		if (findflag == 0 || contcnt < cntlimit) {
			startpx = firstlocation;
			startpy = arrfloat[firstlocation];
		}
		contcnt = 0;
		findflag = 0;
		for (int i = center + (int) (center * 0.01); i < secondlocation; i++) {
			if ((arrfloat[i + 1] - arrfloat[i]) >= threshold) {
				if (findflag == 0) {
					findflag = 1;
					contcnt = 1;
				} else {
					contcnt++;
					if (contcnt >= cntlimit) {
						endpx = i - (cntlimit - 1);
						endpy = arrfloat[i];
						break;
					}
				}
			} else {
				findflag = 0;
				contcnt = 0;
			}
		}
		if (findflag == 0 || contcnt < cntlimit) {
			endpx = secondlocation;
			endpy = arrfloat[secondlocation];
		}

		midpx = peakvalue;
		midpy = arrfloat[peakvalue];

		// final A, B �����
		// resulta=(float)((startpy-endpy)/(startpx-endpx));
		// resultb=(float)(startpy-(resulta*startpx));

		if (startpy == endpy & startpx == endpx) {
			result.Vally_CH.x = 0;
			result.Vally_CH.y = 0;
		} else if (startpx == endpx) {
			result.Vally_CH.x = 0;
			result.Vally_CH.y = 0;
		} else {
			double A = (startpy - endpy) / (startpx - endpx);
			double B = startpy - (A * startpx);

			result.Vally_Coefficients = new double[] { A, B };
		}
		result.Channel = peakvalue;
		result.Vally_CH.x = (int) startpx;
		result.Vally_CH.y = (int) endpx;
		return result;
	}

	public static class FindPC1 {

		int ROW = 1024;

		int COL = 0;

		void FindPC_main(double[] dataTemp, double[] FirstPC, int NumOfData)

		{

			int n, m, i, j, k, k2;

			double symmat2, evals = 0, interm = 0;

			double in_value;

			char option;// , strncpy();

			n = dataTemp.length;

			m = COL = NumOfData;

			double[] data = new double[n + 1];

			double symmat = 0;

			for (i = 1; i <= n; i++)

			{

				data[i] = dataTemp[i - 1];// CFPeaks.FP_m_PrincipalComponent[i-1][j-1];

			}

			corcol(data, n, m, symmat);

			for (i = 1; i <= n; i++) {

				FirstPC[i - 1] = data[i];

			}

		}

		void tqli(double d, double e, int n, double z)

		{

			int m, l, iter, i, k;

			double s, r, p, g, f, dd, c, b;

			e = 0.0;

			z = 0.0;

			for (l = 1; l <= n; l++)

			{

				iter = 0;

				for (m = l; m <= n - 1; m++)

				{

					dd = Math.abs(d);

					if (Math.abs(e) + dd == dd)
						break;

				}

			}

		}

		void tred2(double a, int n, double d, double e)

		{

			int l, k, j, i;

			double scale, hh, h, g, f;

			d = 0.0;

			e = 0.0;

			d = a;

			a = 0.0;

		}

		void corcol(double[] data, int n, int m, double symmat)

		{

			double eps = 0.005;

			double x, mean = 0, stddev = 0;// , *vector();

			int i, j, j1, j2;

			for (j = 1; j <= m; j++)

			{

				mean = 0.0;

				for (i = 1; i <= n; i++)

				{

					mean += data[i];

				}

				mean /= n;

			}

			for (j = 1; j <= m; j++)

			{

				stddev = 0.0;

				for (i = 1; i <= n; i++)

				{

					stddev += ((data[i] - mean) *

					(data[i] - mean));

				}

				stddev /= n;

				stddev = Math.sqrt(stddev);

				if (stddev <= eps)
					stddev = 1.0;

			}

			for (i = 1; i <= n; i++)

			{

				for (j = 1; j <= m; j++)

				{

					// min data[i] -= mean;

					x = Math.sqrt(n);

					x *= stddev;

					data[i] /= x;

				}

			}

			symmat = 1.0;

		}

	}

}
