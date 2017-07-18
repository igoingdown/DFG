package Apriori;

import Common.Contrast;
import Common.Distribution;
import Common.Parameter;
import Dataset.DatasetParameter;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by iejr on 2015/7/20.
 */
public class NoiseCut extends Apriori {

    protected double e;
    protected double e1;
    protected double e2;

    public static void main(String[] args) {
        String dataset = "BMS1";
        int dbSize = 59601; //88162;
        double th = 0.002;
        double e  = 0.1;
        NoiseCut apriori = new NoiseCut( dbSize * th, e );

        String path = DatasetParameter.sDatasetPathPrefix + dataset + ".dat";
        String sResult = DatasetParameter.sDataResultPrefix + dataset +
                "\\NoiseCut\\Result_" + th + "_"  + e + "_" +
                Parameter.getTime() + ".txt";
        apriori.initWriter( sResult );

        apriori.visitTime = 27;
        long start = System.currentTimeMillis();
        apriori.firstScan(path);
        apriori.getFrequentPattern(path);
        long end = System.currentTimeMillis();
        System.out.println("Running time: " + (end - start));
        apriori.closeWriter();
        System.out.println("Done!");

        String sStand = DatasetParameter.sDataResultPrefix + dataset
                + "\\Apriori\\Result_" + th + "_Standard.txt";
        Contrast.ContrastSmart( sResult, sStand, -1 );
    }

    public NoiseCut( double dSupport, double e ){
        this.e = e;
        this.e1 = 0.25 * e;
        this.e2 = 0.75 * e;

        double dNoisy = Distribution.laplace( e1, 1 );
        this.support = dSupport + dNoisy;

        System.out.println( "Support + Noisy = NSupport" );
        System.out.println( dSupport + "+" + dNoisy + "=" + this.support );
    }

    protected double getNoisySupport( int nRealSupport ){

        double dNoisy = Distribution.laplace( e2, 1 );

        return nRealSupport + dNoisy;
    }

    protected void writeFrequentItem( String sItem, double dNoisySupport,
                                      int nRealSupport ){

        try {
            writer.write(sItem + ":" + dNoisySupport + ":" + nRealSupport + "\r\n");
        } catch ( FileNotFoundException e ){
            e.printStackTrace();
        } catch ( IOException e ){
            e.printStackTrace();
        }

    }

}
