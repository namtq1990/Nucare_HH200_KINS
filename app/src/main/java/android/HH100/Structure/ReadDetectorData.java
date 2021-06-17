package android.HH100.Structure;

public class ReadDetectorData
{
	/*
		181102 추가
		bnc와 동일하게 처리
	 */

	public int[] pdata = new int[1024];
	public int Neutron = 0;
	public boolean IsThereNeutron = true;
	public int GM = 0;
	public double GetAVGNeutron = 0;
	/*
		*181005 inseon.ahn
		 *UUSN(4)	+MCU(3)+FPGA(4)+BOARD(6)+Serial(6byte)
	 */
	public String MCU = "";
	public String FPGA = "";
	public String board = "";
	public String serial = "";

	//190123 추후 galaxy j3
	public int mRealTime= -1;
	public double time= -1;
	//시간정보 있는지 없는지 체크
	public boolean isRealTime = false;

	//190425 test hv
	public double mHighVoltage =0;

}
