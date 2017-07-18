package Common;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by iejr on 2015/6/11.
 */
public class Parameter {

    public static String sVertexLabel;
    public static int    nDoubleDesicion;
    public static int    nRandomSeed;

    public static int    nMaxRank;

    static {
        sVertexLabel = "HLiBBeCNOFNaMgAlSiPSClKCaScTiVCrMnFeCoNiCuZnGaGeAsSeBrRbSrYZrNbMoTcRuRhPdAgCdSnSbTeICsBaCeLuHfTaWReOsIrPtAuHgTlPbBiPoAtRaInUPrSmThEuNdDyErAcNone";
        nDoubleDesicion = 0;
        nRandomSeed = -1;

        nMaxRank = 13;
    }

    public static String getTime(){
        SimpleDateFormat df = new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss" );
        String sTime = df.format( new Date() );
        return sTime;
    }

}
