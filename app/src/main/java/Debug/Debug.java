package Debug;

public class Debug {

	public static boolean IsDebugMode = true;

	public static boolean IsBattEnalbe = true;
	
	public static boolean IsShutdown = true;
	
	public static boolean IsSequenceCollect = false;
	
	public static boolean IsCalibrationMode = false;

	public static boolean IsVolumeDown = false;

	public static boolean IsSetSpectrumExcute = true;

	public static boolean IsAdminEnable = false;

	public static boolean IsGainStblizationSaveMode = false;
	
	public static boolean IsMailDefaultSetting = true;

	public static boolean IsIsotopeInvisibleViewFirstFiveSecond = false;

	public static Source SpecInfo = new K40();

	
	public static Source getSpecInfo() {
		return SpecInfo;
	}

	public static void setSpecInfo(Source specInfo) {
		SpecInfo = specInfo;
	}
}
