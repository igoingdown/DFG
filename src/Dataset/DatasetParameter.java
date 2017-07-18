package Dataset;

import java.util.HashMap;

/**
 * Created by iejr on 2015/6/24.
 */
public class DatasetParameter {
    public static HashMap<String,Integer> hDataset2Index;
    public static HashMap<String,Integer> hThreshold2Index;


    // Top k:    5     10      20     40    60     80    100
    // th   :  0.903  0.863  0.624  0.354  0.27  0.188  0.128

    public static int[] nTopK = {
            5,
            10,
            20,
            40,
            60,
            80,
            100,
            50,
            200,
            150
    };

    public static String[] sDatasetFullName = {
            "NCI.dat",                  //1
            "FDA.dat",                  //2
            "NCI-single.dat",           //3
            "FDA-single.dat",           //4
            "FDA-single-sample.txt",    //5
            "AID-single.dat",           //6
            "CAN-single.dat",           //7
            "FDA_sample_0.2.dat",           //8
            "AID_sample_0.2.dat",           //9
            "CAN_sample_0.2.dat"           //10
    };

    public static int nDataSize[] = {
            265242,                                         //iejr: for NCI
            53084,                                          //iejr: for FDA
            265242,                                         //iejr: for NCI-single
            53804,                                           //iejr: for FDA-single
            1,
            42689,                                          //iejr: AID
            32557,                                          //iejr: CAN
            10740,                                           //iejr: FDA sample 0.2
            8530,                                            //iejr: AID sample 0.2
            6508                                             //iejr: CAN sample 0.2
    };

    public static int nVertexLabelType[] = {
            85,                      //iejr: for NCI
            104,                      //iejr: for FDA
            85,                      //iejr: for NCI-single
            104,                      //iejr: for FDA-single
            1,                      //iejr:
            65,                      //iejr: for AID-single
            67,                      //iejr: for CAN-single
            1,                      //iejr: for FDA sample 0.2
            1,                      //iejr: for AID sample 0.2
            1,                      //iejr: for CAN sample 0.2
    };

    public static double dCorrespondThreshold[][] = {
            {0.04, 0.05, 0.06, 0.07, 0.08, 1.0},                 //iejr: for NCI
            {0.05, 0.06, 0.07, 0.08, 0.09, 1.0},                 //iejr: for FDA
            {0.65, 0.7, 0.75, 0.8, 0.85, 1.0},                 //iejr: for NCI-single
            {0.6, 0.65, 0.7, 0.75, 0.8, 1.0},                 //iejr: for FDA-single
            {0.05, 0.06, 0.07, 0.08, 0.09, 1.0},                 //iejr:
            {0.4, 0.45, 0.5, 0.55, 0.6, 1.0},                 //iejr: for AID-single
            {0.4, 0.45, 0.5, 0.55, 0.6, 1.0},                 //iejr: for CAN-single
            {0.6, 0.65, 0.7, 0.75, 0.8, 1.0},                 //iejr: for FDA_sample_0.2
            {0.4, 0.45, 0.5, 0.55, 0.6, 1.0},                 //iejr: for AID_sample_0.2
            {0.45, 0.5, 0.55, 0.6, 0.65, 1.0},                 //iejr: for CAN_sample_0.2
    };

    public static int nCorrespongTh2MaxGraphRank[][] = {
            {1,1,1,1,1,1},                                 //iejr: NCI
            {1,1,1,1,1,1},                                 //iejr: FDA
            {9,8,8,7,6,1},                                 //iejr: NCI-single
            {9,9,8,8,6,1},                                 //iejr: FDA-single
            {1,1,1,1,1,1},                                 //iejr:
            {10,9,9,8,7,1},                                 //iejr: AID-single
            {10,10,9,9,8,1},                                 //iejr: CAN-single
            {1,1,1,1,1,1},                                 //iejr: FDA_sample_0.2
            {1,1,1,1,1,1},                                 //iejr: AID_sample_0.2
            {1,1,1,1,1,1},                                 //iejr: CAN_sample_0.2
    };

    public static double dTopk2CorrespondTr[][] = {
            //5      10     20     40     60     80     100     50     200   150  k
            {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0},          //iejr: NCI
            {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0},          //iejr: FDA
            {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0},          //iejr: NCI-single
            {0.9159,0.8706,0.811, 0.7369,0.6765,0.616, 0.582, 0.713, 0.4974,1.0},          //iejr: FDA-single
            {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0},          //iejr: FDA-single-sample
            {0.8771,0.7939,0.7231,0.5542,0.4894,0.4386,0.3971,0.5165,0.3019,1.0},          //iejr: AIDS
            {0.8963,0.8702,0.7611,0.5902,0.5207,0.4705,0.4310,0.5592,0.3318,1.0},          //iejr: CANCER
            {1.0,1.0,0.8189,0.7441,0.6835,0.6242,0.5932,1.0,1.0},          //iejr: FDA-sample
            {1.0,1.0,0.7284,0.5595,0.4914,0.4371,0.3976,1.0,1.0},          //iejr: AID-sample
            {1.0,1.0,0.7621,0.5909,0.5169,0.4732,0.4343,1.0,1.0},          //iejr: CAN-sample
    };

    public static int nCorrespond2Topk[][] = {
            {1,1,1,1,1},                                 //iejr: NCI
            {1,1,1,1,1},                                 //iejr: FDA
            {109,67,55,41,24},                                 //iejr: NCI-single
            {88,68,51,38,22},                                 //iejr: FDA-single
            {1,1,1,1,1},                                 //iejr:
            {98,74,56,41,32},                                 //iejr: AID-single
            {119,90,66,51,37},                                 //iejr: CAN-single
            {93,69,54,38,24},                             //iejr: FDA_sample_0.2
            {1,1,1,1,1},                                 //iejr: AID_sample_0.2
            {1,1,1,1,1},                                 //iejr: CAN_sample_0.2
    };

    public static int nTopk2MaxGraphRank[][] = {
            //5 10 20 40 60 80 100 50 200 150  k
            {1,1,1,1,1,1,1,1,1, 1},                            //iejr: NCI
            {1,1,1,1,1,1,1,1,1, 1},                            //iejr: FDA
            {1,1,1,1,1,1,1,1,1, 1},                            //iejr: NCI-single
            {1, 1, 7, 7, 9, 9, 9,  8,1, 1},                            //iejr: FDA-single
            {1,1,1,1,1,1,1,1,1, 1},                            //iejr: FDA-single-sample
            {1, 1, 6, 8, 9, 9,10,  8, 1, 1},                            //iejr: AID
            {1, 1, 6, 8, 9, 9, 9,  9, 1, 1},                            //iejr: CAN
            {1,1,1,1,1,1,1,1,1, 1},                            //iejr: FDA-sample
            {1,1,1,1,1,1,1,1,1, 1},                            //iejr: AID-sample
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},                            //iejr: CAN-sample
    };

    public static String sStandardFile[]={
            "",                                         //iejr: for NCI
            "",                                          //iejr: for FDA
            "/FSG/threshold/Result_0.6_Standard.txt",                                         //iejr: for NCI-single
            "/FSG/threshold/Result_0.45_Standard.txt",                                           //iejr: for FDA-single
            "",
            "/FSG/threshold/Result_0.3_Standard.txt",            //iejr: AID-single
            "/FSG/threshold/Result_0.32_Standard.txt",           //iejr: CAN-single
            "",                                          //iejr: FDA sample 0.2
            "",                                          //iejr: AID-sample
            "/FSG/threshold/Result_0.321_Standard.txt",                                          //iejr: CAN-simple
    };

    public static String sDatasetPathPrefix = "./Dataset/";
    public static String sDataResultPrefix = "./out/";

    static {

        hDataset2Index = new HashMap<String,Integer>();
        hDataset2Index.put( "NCI", 0 );
        hDataset2Index.put( "FDA", 1 );
        hDataset2Index.put( "NCI-single", 2 );
        hDataset2Index.put( "FDA-single", 3 );
        hDataset2Index.put( "FDA-single-count", 4 );
        hDataset2Index.put( "AID-single", 5 );
        hDataset2Index.put( "CAN-single", 6 );
        hDataset2Index.put( "FDA_sample_0.2", 7 );
        hDataset2Index.put( "AID_sample_0.2", 8 );
        hDataset2Index.put( "CAN_sample_0.2", 9 );

        hThreshold2Index = new HashMap<String,Integer>();
        hThreshold2Index.put( "One", 0 );
        hThreshold2Index.put( "Two", 1 );
        hThreshold2Index.put( "Three", 2 );
        hThreshold2Index.put( "Four", 3 );
        hThreshold2Index.put( "Five", 4 );
        hThreshold2Index.put( "Full", 5 );
    }

    public static String getDatasetPath( String sDatasetName ){
        if( !hDataset2Index.containsKey( sDatasetName ) ){
            return null;
        }else{
            String sDataName = sDatasetFullName[hDataset2Index.get( sDatasetName )];
            return sDatasetPathPrefix+sDataName;
        }
    }

    public static double getAbusoluteThreshold( String sDatasetName, double dCorrespondTheshold ){

        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return -1;
        }
        if( dCorrespondTheshold < 0 || dCorrespondTheshold > 1.0 ){
            return -1;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );
        double dAbThreshold = nDataSize[nDatasetIndex] * dCorrespondTheshold;

        return dAbThreshold;
    }

    public static double getAbusoluteThreshold( String sDatasetName, int nThresholdRank ){

        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return -1;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );

        if( nThresholdRank > 5 || nThresholdRank < 1 ){
            return -1;
        }

        nThresholdRank--;

        return nDataSize[nDatasetIndex] * dCorrespondThreshold[nDatasetIndex][nThresholdRank];

    }

    public static double getCorrespondThreshold( String sDatasetName, int k ){
        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return -1;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );

        for( int i = 0;i < nTopK.length;i++ ){
            if( nTopK[i] == k ){
                return dTopk2CorrespondTr[nDatasetIndex][i];
            }
        }

        return -1;
    }

    public static double getCorrespondThresholdByRank( String sDatasetName, int nThresholdRank ){
        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return -1;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );

        if( nThresholdRank > 5 || nThresholdRank < 1 ){
            return -1;
        }

        nThresholdRank--;

        return dCorrespondThreshold[nDatasetIndex][nThresholdRank];
    }

    public static int getTopK( String sDatasetName, int nThresholdRank ){
        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return -1;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );

        if( nThresholdRank > 5 || nThresholdRank < 1 ){
            return -1;
        }

        nThresholdRank--;

        return nCorrespond2Topk[nDatasetIndex][nThresholdRank];
    }

    public static String getContrastFile( String sDatasetName ){
        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return null;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );

        return sStandardFile[nDatasetIndex];
    }

    public static String getContrastFile( String sDatasetName, double dCorrespondTh ){
        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return null;
        }

        return "/FSG/threshold/Result_" + dCorrespondTh + "_Standard.txt";
    }

    public static int getMaxGraphRankByTopk( String sDatasetName, int k ){
        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return -1;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );

        for( int i = 0;i < nTopK.length;i++ ){
            if( nTopK[i] == k ){
                return nTopk2MaxGraphRank[nDatasetIndex][i];
            }
        }

        return -1;
    }

    public static int getMaxGraphRankByThrehsold( String sDatasetName, int nThresholdRank ){
        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return -1;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );

        if( nThresholdRank > 5 || nThresholdRank < 1 ){
            return -1;
        }

        nThresholdRank--;

        return nCorrespongTh2MaxGraphRank[nDatasetIndex][nThresholdRank];
    }

    public static int getVertexType( String sDatasetName ){
        if( !hDataset2Index.containsKey( sDatasetName ) ) {
            return -1;
        }

        int nDatasetIndex = hDataset2Index.get( sDatasetName );

        return nVertexLabelType[nDatasetIndex];
    }
}
