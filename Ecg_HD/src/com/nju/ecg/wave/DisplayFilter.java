package com.nju.ecg.wave;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
public class DisplayFilter {

	/**
	 * @param args
	 */
	//2-35 bandpass filter: N = 200, Wn = [2/500 35/500];b = fir1(N, Wn);a = 1;
	//40 lowpass filter: N=200, Wn = 40/500; 2012.12.28
	public static void my_filter_bp(int src[], int dst[], int len) {
		double sum = 0;
		double data[] = new double[len];
		double data_bp[] = new double[len];
		int i, j;
		for (i=0; i<len; i++)
			data[i] = src[i];
		/*double b[] = {-0.000241564441461938,	-0.000187552437319965,	-0.000135571517632132,	-8.74954240310426e-05,
				-4.53073573541483e-05,	-1.10995042526124e-05,	1.29529692756053e-05,	2.46492261373239e-05,
				2.18540652201831e-05,	2.62227847483367e-06,	-3.46488223067190e-05,	-9.10474788443667e-05,
				-0.000166968434127325,	-0.000261941815242181,	-0.000374486783338695,	-0.000502007302693992,
				-0.000640746494229281,	-0.000785813688438205,	-0.000931294528863289,	-0.00107044946077386,
				-0.00119599993679867,	-0.00130049504105997,	-0.00137674440805166,	-0.00141829677150400,
				-0.00141993771780824,	-0.00137817571749969,	-0.00129168269521107,	-0.00116165461835263,
				-0.000992059070216335,	-0.000789740621638223,	-0.000564360973564489,	-0.000328159100663849,
				-9.55266198742450e-05,	0.000117595165968933,	0.000294478260323873,	0.000418500964247554,
				0.000474129922779651,	0.000447940964770676,	0.000329621449754494,	0.000112892713681680,
				-0.000203709539702433,	-0.000616256123367848,	-0.00111509904321078,	-0.00168484212543340,
				-0.00230463609746504,	-0.00294881023571426,	-0.00358783433899375,	-0.00418958539725361,
				-0.00472087395058340,	-0.00514916688670796,	-0.00544442742287961,	-0.00558098031544469,
				-0.00553930186767868,	-0.00530763080920006,	-0.00488329810706276,	-0.00427368147043467,
				-0.00349670365372111,	-0.00258081225727907,	-0.00156440187088860,	-0.000494666117234275,
				0.000574103800241366,	0.00158272747902829,	0.00246977472733438,	0.00317453297479995,
				0.00364017628282138,	0.00381699134637412,	0.00366550404834559,	0.00315934560070126,
				0.00228770067174118,	0.00105719135734544,	-0.000506929802254381,	-0.00236037795396040,
				-0.00444029256136438,	-0.00666658217945073,	-0.00894414614010540,	-0.0111659197569196,
				-0.0132166482599260,	-0.0149772560993776,	-0.0163296445067368,	-0.0171617230884741,
				-0.0173724622980685,	-0.0168767440683092,	-0.0156097884454740,	-0.0135309450275169,
				-0.0106266591452567,	-0.00691245331044457,	-0.00243380328096833,	0.00273416647353292,
				0.00848819307320144,	0.0146995640532189,		0.0212179051079989,		0.0278759372900728,	
				0.0344950292150537,	0.0408913318911464,	0.0468822547805144,	0.0522930230794382,	0.0569630489661191,
				0.0607518541700823,	0.0635442975685125,	0.0652548889581505,	0.0658310075028817,	0.0652548889581505,
				0.0635442975685125,	0.0607518541700823,	0.0569630489661191,	0.0522930230794382,	0.0468822547805144,
				0.0408913318911464,	0.0344950292150537,	0.0278759372900728,	0.0212179051079989,	0.0146995640532189,
				0.00848819307320144,	0.00273416647353292,	-0.00243380328096833,	-0.00691245331044457,
				-0.0106266591452567,	-0.0135309450275169,	-0.0156097884454740,	-0.0168767440683092,
				-0.0173724622980685,	-0.0171617230884741,	-0.0163296445067368,	-0.0149772560993776,
				-0.0132166482599260,	-0.0111659197569196,	-0.00894414614010540,	-0.00666658217945073,
				-0.00444029256136438,	-0.00236037795396040,	-0.000506929802254381,	0.00105719135734544,
				0.00228770067174118,	0.00315934560070126,	0.00366550404834559,	0.00381699134637412,
				0.00364017628282138,	0.00317453297479995,	0.00246977472733438,	0.00158272747902829,
				0.000574103800241366,	-0.000494666117234275,	-0.00156440187088860,	-0.00258081225727907,
				-0.00349670365372111,	-0.00427368147043467,	-0.00488329810706276,	-0.00530763080920006,
				-0.00553930186767868,	-0.00558098031544469,	-0.00544442742287961,	-0.00514916688670796,
				-0.00472087395058340,	-0.00418958539725361,	-0.00358783433899375,	-0.00294881023571426,
				-0.00230463609746504,	-0.00168484212543340,	-0.00111509904321078,	-0.000616256123367848,
				-0.000203709539702433,	0.000112892713681680,	0.000329621449754494,	0.000447940964770676,
				0.000474129922779651,	0.000418500964247554,	0.000294478260323873,	0.000117595165968933,
				-9.55266198742450e-05,	-0.000328159100663849,	-0.000564360973564489,	-0.000789740621638223,
				-0.000992059070216335,	-0.00116165461835263,	-0.00129168269521107,	-0.00137817571749969,
				-0.00141993771780824,	-0.00141829677150400,	-0.00137674440805166,	-0.00130049504105997,
				-0.00119599993679867,	-0.00107044946077386,	-0.000931294528863289,	-0.000785813688438205,
				-0.000640746494229281,	-0.000502007302693992,	-0.000374486783338695,	-0.000261941815242181,
				-0.000166968434127325,	-9.10474788443667e-05,	-3.46488223067190e-05,	2.62227847483367e-06,
				2.18540652201831e-05,	2.46492261373239e-05,	1.29529692756053e-05,	-1.10995042526124e-05,
				-4.53073573541483e-05,	-8.74954240310426e-05,	-0.000135571517632132,	-0.000187552437319965,
				-0.000241564441461938};*/
		double b[] = {-2.49939870689846e-19,	-6.42670110592360e-05,	-0.000126833377254920,	-0.000184633240486872,	
				-0.000234548351952457,	-0.000273478101958888,	-0.000298449482449997,	-0.000306771591638274,	-0.000296232329902181,
				-0.000265327256792922,	-0.000213503048842499,	-0.000141391531522019,	-5.10057262510003e-05,	5.41325107414032e-05,
				0.000168962360534509,	0.000286975791118926,	0.000400447494845365,	0.000500808386513992,	0.000579154980041613,
				0.000626872831567505,	0.000636338275127471,	0.000601650228861446,	0.000519334246342653,	0.000388955424061570,
				0.000213576192850239,	-6.70872606797806e-19,	-0.000241247426496937,	-0.000496231492031623,	-0.000748228513573709,
				-0.000978639745396982,	-0.00116816219245839,	-0.00129815383037096,	-0.00135210844881791,	-0.00131713752003599,
				-0.00118534513241762,	-0.000954978620034798,	-0.000631243044098671,	-0.000226682490712754,	0.000238945118054209,
				0.000739341434844129,	0.00124289343742443,	0.00171441436360098,	0.00211732060293436,	0.00241612557874509,
				0.00257909743649307,	0.00258090166246110,	0.00240503517007988,	0.00204585684498851,	0.00151003213651511,
				0.000817236157334425,	-1.68709412715646e-18,	-0.000897363343599875,	-0.00182075686839449,	-0.00270915963246305,
				-0.00349822186227775,	-0.00412440646697335,	-0.00452943557987484,	-0.00466476096163301,	-0.00449575392418084,
				-0.00400530655602134,	-0.00319655295127927,	-0.00209445701934297,	-0.000746070996215034,	0.000780656709746723,
				0.00239955823493203,	0.00401034611412499,	0.00550397757381676,	0.00676903169525384,	0.00769876577559599,
				0.00819845186008093,	0.00819255096590246,	0.00763126492847945,	0.00649601651903026,	0.00480344821716343,
				0.00260759762691062,	-2.70331564751511e-18,	-0.00289241905015963,	-0.00591167100560782,	-0.00887512928615579,
				-0.0115837969890017,	-0.0138320410219482,	-0.0154183117158493,	-0.0161562872764043,	-0.0158858331591251,
				-0.0144831516676451,	-0.0118695188034352,	-0.00801806344813574,	-0.00295813590315619,	0.00322306593878704,
				0.0103818004522189,	0.0183220449900812,	0.0268025817645752,	0.0355466106793485,	0.0442534459023328,	0.0526117284346342,
				0.0603134925526133,	0.0670683657654135,	0.0726171637276426,	0.0767441649053693,	0.0792874137655850,	0.0801465024594331,
				0.0792874137655850,	0.0767441649053693,	0.0726171637276426,	0.0670683657654135,	0.0603134925526133,	0.0526117284346342,
				0.0442534459023328,	0.0355466106793485,	0.0268025817645752,	0.0183220449900812,	0.0103818004522189,	0.00322306593878704,
				-0.00295813590315619,	-0.00801806344813574,	-0.0118695188034352,	-0.0144831516676451,	-0.0158858331591251,
				-0.0161562872764043,	-0.0154183117158493,	-0.0138320410219482,	-0.0115837969890017,	-0.00887512928615579,
				-0.00591167100560782,	-0.00289241905015963,	-2.70331564751511e-18,	0.00260759762691062,	0.00480344821716343,
				0.00649601651903026,	0.00763126492847945,	0.00819255096590246,	0.00819845186008093,	0.00769876577559599,
				0.00676903169525384,	0.00550397757381676,	0.00401034611412499,	0.00239955823493203,	0.000780656709746723,
				-0.000746070996215034,	-0.00209445701934297,	-0.00319655295127927,	-0.00400530655602134,	-0.00449575392418084,
				-0.00466476096163301,	-0.00452943557987484,	-0.00412440646697335,	-0.00349822186227775,	-0.00270915963246305,
				-0.00182075686839449,	-0.000897363343599875,	-1.68709412715646e-18,	0.000817236157334425,	0.00151003213651511,
				0.00204585684498851,	0.00240503517007988,	0.00258090166246110,	0.00257909743649307,	0.00241612557874509,
				0.00211732060293436,	0.00171441436360098,	0.00124289343742443,	0.000739341434844129,	0.000238945118054209,
				-0.000226682490712754,	-0.000631243044098671,	-0.000954978620034798,	-0.00118534513241762,	-0.00131713752003599,
				-0.00135210844881791,	-0.00129815383037096,	-0.00116816219245839,	-0.000978639745396982,	-0.000748228513573709,
				-0.000496231492031623,	-0.000241247426496937,	-6.70872606797806e-19,	0.000213576192850239,	0.000388955424061570,
				0.000519334246342653,	0.000601650228861446,	0.000636338275127471,	0.000626872831567505,	0.000579154980041613,
				0.000500808386513992,	0.000400447494845365,	0.000286975791118926,	0.000168962360534509,	5.41325107414032e-05,
				-5.10057262510003e-05,	-0.000141391531522019,	-0.000213503048842499,	-0.000265327256792922,	-0.000296232329902181,
				-0.000306771591638274,	-0.000298449482449997,	-0.000273478101958888,	-0.000234548351952457,	-0.000184633240486872,
				-0.000126833377254920,	-6.42670110592360e-05,	-2.49939870689846e-19}; //40Hz lowpass filter;2012.12.28

		for (i=0; i<200; i++)
			data_bp[i] = data[i]; 
		for (i=200; i<len; i++) {
			for(j=0; j<201; j++) {
					sum += b[j] * data[i-j];
			}
			data_bp[i] = sum;
			sum = 0;
		}
		for (i=0; i<len; i++)
			//dst[i] = (int)(data_bp[i] + 0.5);
			dst[i] = (int)(data_bp[i] + 0.5); //2012.12.28
		
	}
    
    //---------------旧的滤波算法--------------//
//    /**
//     * @param args
//     */
//    //2-35 bandpass filter: N = 200, Wn = [2/500 35/500];b = fir1(N, Wn);a = 1;
//    public static void my_filter_bp(int src[], int dst[], int len) {
//        double sum = 0;
//        double data[] = new double[len];
//        double data_bp[] = new double[len];
//        int i, j;
//        for (i=0; i<len; i++)
//            data[i] = src[i];
//        double b[] = {-0.000241564441461938,    -0.000187552437319965,  -0.000135571517632132,  -8.74954240310426e-05,
//                -4.53073573541483e-05,  -1.10995042526124e-05,  1.29529692756053e-05,   2.46492261373239e-05,
//                2.18540652201831e-05,   2.62227847483367e-06,   -3.46488223067190e-05,  -9.10474788443667e-05,
//                -0.000166968434127325,  -0.000261941815242181,  -0.000374486783338695,  -0.000502007302693992,
//                -0.000640746494229281,  -0.000785813688438205,  -0.000931294528863289,  -0.00107044946077386,
//                -0.00119599993679867,   -0.00130049504105997,   -0.00137674440805166,   -0.00141829677150400,
//                -0.00141993771780824,   -0.00137817571749969,   -0.00129168269521107,   -0.00116165461835263,
//                -0.000992059070216335,  -0.000789740621638223,  -0.000564360973564489,  -0.000328159100663849,
//                -9.55266198742450e-05,  0.000117595165968933,   0.000294478260323873,   0.000418500964247554,
//                0.000474129922779651,   0.000447940964770676,   0.000329621449754494,   0.000112892713681680,
//                -0.000203709539702433,  -0.000616256123367848,  -0.00111509904321078,   -0.00168484212543340,
//                -0.00230463609746504,   -0.00294881023571426,   -0.00358783433899375,   -0.00418958539725361,
//                -0.00472087395058340,   -0.00514916688670796,   -0.00544442742287961,   -0.00558098031544469,
//                -0.00553930186767868,   -0.00530763080920006,   -0.00488329810706276,   -0.00427368147043467,
//                -0.00349670365372111,   -0.00258081225727907,   -0.00156440187088860,   -0.000494666117234275,
//                0.000574103800241366,   0.00158272747902829,    0.00246977472733438,    0.00317453297479995,
//                0.00364017628282138,    0.00381699134637412,    0.00366550404834559,    0.00315934560070126,
//                0.00228770067174118,    0.00105719135734544,    -0.000506929802254381,  -0.00236037795396040,
//                -0.00444029256136438,   -0.00666658217945073,   -0.00894414614010540,   -0.0111659197569196,
//                -0.0132166482599260,    -0.0149772560993776,    -0.0163296445067368,    -0.0171617230884741,
//                -0.0173724622980685,    -0.0168767440683092,    -0.0156097884454740,    -0.0135309450275169,
//                -0.0106266591452567,    -0.00691245331044457,   -0.00243380328096833,   0.00273416647353292,
//                0.00848819307320144,    0.0146995640532189,     0.0212179051079989,     0.0278759372900728, 
//                0.0344950292150537, 0.0408913318911464, 0.0468822547805144, 0.0522930230794382, 0.0569630489661191,
//                0.0607518541700823, 0.0635442975685125, 0.0652548889581505, 0.0658310075028817, 0.0652548889581505,
//                0.0635442975685125, 0.0607518541700823, 0.0569630489661191, 0.0522930230794382, 0.0468822547805144,
//                0.0408913318911464, 0.0344950292150537, 0.0278759372900728, 0.0212179051079989, 0.0146995640532189,
//                0.00848819307320144,    0.00273416647353292,    -0.00243380328096833,   -0.00691245331044457,
//                -0.0106266591452567,    -0.0135309450275169,    -0.0156097884454740,    -0.0168767440683092,
//                -0.0173724622980685,    -0.0171617230884741,    -0.0163296445067368,    -0.0149772560993776,
//                -0.0132166482599260,    -0.0111659197569196,    -0.00894414614010540,   -0.00666658217945073,
//                -0.00444029256136438,   -0.00236037795396040,   -0.000506929802254381,  0.00105719135734544,
//                0.00228770067174118,    0.00315934560070126,    0.00366550404834559,    0.00381699134637412,
//                0.00364017628282138,    0.00317453297479995,    0.00246977472733438,    0.00158272747902829,
//                0.000574103800241366,   -0.000494666117234275,  -0.00156440187088860,   -0.00258081225727907,
//                -0.00349670365372111,   -0.00427368147043467,   -0.00488329810706276,   -0.00530763080920006,
//                -0.00553930186767868,   -0.00558098031544469,   -0.00544442742287961,   -0.00514916688670796,
//                -0.00472087395058340,   -0.00418958539725361,   -0.00358783433899375,   -0.00294881023571426,
//                -0.00230463609746504,   -0.00168484212543340,   -0.00111509904321078,   -0.000616256123367848,
//                -0.000203709539702433,  0.000112892713681680,   0.000329621449754494,   0.000447940964770676,
//                0.000474129922779651,   0.000418500964247554,   0.000294478260323873,   0.000117595165968933,
//                -9.55266198742450e-05,  -0.000328159100663849,  -0.000564360973564489,  -0.000789740621638223,
//                -0.000992059070216335,  -0.00116165461835263,   -0.00129168269521107,   -0.00137817571749969,
//                -0.00141993771780824,   -0.00141829677150400,   -0.00137674440805166,   -0.00130049504105997,
//                -0.00119599993679867,   -0.00107044946077386,   -0.000931294528863289,  -0.000785813688438205,
//                -0.000640746494229281,  -0.000502007302693992,  -0.000374486783338695,  -0.000261941815242181,
//                -0.000166968434127325,  -9.10474788443667e-05,  -3.46488223067190e-05,  2.62227847483367e-06,
//                2.18540652201831e-05,   2.46492261373239e-05,   1.29529692756053e-05,   -1.10995042526124e-05,
//                -4.53073573541483e-05,  -8.74954240310426e-05,  -0.000135571517632132,  -0.000187552437319965,
//                -0.000241564441461938};
//        for (i=0; i<200; i++)
//            data_bp[i] = data[i]; 
//        for (i=200; i<len; i++) {
//            for(j=0; j<201; j++) {
//                    sum += b[j] * data[i-j];
//            }
//            data_bp[i] = sum;
//            sum = 0;
//        }
//        for (i=0; i<len; i++)
//            dst[i] = (int)(data_bp[i] + 0.5);   
//        
//    }
	
		
	public static double doubleFromByte(byte[] temp){
		//convert little-edian to bit-edian
        long val = (((long)(temp[7] & 0xff) << 56) | ((long)(temp[6] & 0xff) << 48) | ((long)(temp[5] & 0xff) << 40) | ((long)(temp[4] & 0xff) << 32) | ((long)(temp[3] & 0xff) << 24) | ((long)(temp[2] & 0xff) << 16) | ((long)(temp[1] & 0xff) << 8) | (long)(temp[0] & 0xff));
        double value = Double.longBitsToDouble(val);
        return value;

    }
	
	public static void doubleToByte(byte[] b, double value){
//		convert big-edian to little-edian
		long temp = Double.doubleToLongBits(value);
		b[0] = (byte) (temp & 0x000000000000FFL);
		b[1] = (byte) ((temp & 0x0000000000FF00L) >> 8);
		b[2] = (byte) ((temp & 0x0000000000FF0000L) >> 16);
		b[3] = (byte) ((temp & 0x00000000FF000000L) >> 24);
		b[4] = (byte) ((temp & 0x000000FF00000000L) >> 32);
		b[5] = (byte) ((temp & 0x0000FF0000000000L) >> 40);
		b[6] = (byte) ((temp & 0x00FF000000000000L) >> 48);
		b[7] = (byte) ((temp & 0xFF00000000000000L) >> 56); 
	}
	
	public static void IntToByte(byte[] b, int value){
//		convert big-edian to little-edian
		int temp = value;
		b[0] = (byte) (temp & 0x000000FF);
		b[1] = (byte) ((temp & 0x0000FF00) >> 8);
		b[2] = (byte) ((temp & 0x00FF0000) >> 16);
		b[3] = (byte) ((temp & 0xFF000000) >> 24);
	}
	
	
	public static void main(String[] args) {
		//读取测试用数据
		byte tempdata[] = new byte[8];
		double data[] = new double[12200];
	
		try {
			FileInputStream in = new FileInputStream("F:\\test4.dat");
			for (int i=0; i<data.length; i++) {
				in.read(tempdata);
				data[i] = doubleFromByte(tempdata);
			}

			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("Cannot open the file!");
		} catch (IOException e) {
			System.out.println("Cannot read from file!");
		}
		int data_int[] = new int[data.length];
		for (int i=0; i<data.length; i++)
			data_int[i] = (int)data[i];
		
 
		
		
		//测试3:200点前导数据
		int shortsrc[] = new int[200+64];
		int shortdata_filter[] = new int[200+64];
		int data_filter2[] = new int[12000];
		for (int i=0; i<187; i++){
			System.arraycopy(data_int, i*64, shortsrc, 0, 200+64); //每次输入64个新数据，与前面保留的200个点合在一起进入滤波
			my_filter_bp(shortsrc, shortdata_filter, shortdata_filter.length);
			System.arraycopy(shortdata_filter, 200, data_filter2, i*64, 64);//第201点开始的后40点作为滤波结果用于显示
		}
		byte bdata_filter_int[] = new byte[4];
		try {
			FileOutputStream out = new FileOutputStream("F:\\test4_filter_lp.dat");
			for (int i=0; i<data_filter2.length; i++) {
				IntToByte(bdata_filter_int, data_filter2[i]);
				out.write(bdata_filter_int);				
			}
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Cannot open the file!");
		} catch (IOException e) {
			System.out.println("Cannot write to file!");
		}
		
		
		

	}

}