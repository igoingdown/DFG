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
public class DFGFI1 extends Apriori {

    protected double e;

    public static void main(String[] args) {
        String dataset = "BMS1";
        int dbSize = 56901; //88162;
        double th = 0.002;
        double e  = 0.1;
        DFGFI1 apriori = new DFGFI1( dbSize * th, e );

        String path = DatasetParameter.sDatasetPathPrefix + dataset + ".dat";
        String sResult = DatasetParameter.sDataResultPrefix +
                dataset +  "\\DFGFI1\\Result_" + th + "_"  + e + "_"
                +  Parameter.getTime() + ".txt";
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
        Contrast.ContrastSmart(sResult, sStand, -1);
    }

    public DFGFI1( double dSupport, double e ){
        this.e = e;

        //iejr: we aim at top-k mining, so sensitivity is 2
        int nSensitivity = 2;
        double dNoisy = Distribution.laplace( this.e, nSensitivity );

        this.support = dSupport + dNoisy;

        System.out.println( "Support + Noisy = NSupport" );
        System.out.println( dSupport + "+" + dNoisy + "=" + this.support );
    }


    protected void writeFrequentItem( String sItem, double dNoisySupport, int nRealSupport ){

        try {
            writer.write(sItem + ":" + dNoisySupport + ":" + nRealSupport + "\r\n");
        } catch ( FileNotFoundException e ){
            e.printStackTrace();
        } catch ( IOException e ){
            e.printStackTrace();
        }

    }

}
