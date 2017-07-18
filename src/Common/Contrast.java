package Common;

import java.io.*;
import java.util.*;

/**
 * Created by iejr on 2015/7/9.
 */
public class Contrast {

    public static double dPrecision;
    public static double dRecall;
    public static double dFscore;
    public static double dRE;

    public static void main( String[] args ){
        String sResult = "D:\\Programs\\Java\\DPGRAPH\\out\\retail\\DFGFI1\\" +
                "Result_0.004_0.1_2015_07_21_14_12_20.txt";
        String sStand  = "D:\\Programs\\Java\\DPGRAPH\\out\\retail\\Apriori\\" +
                "Result_0.004_Standard.txt";
        String sOrg    = sStand;


    //    String sResult = "D:\\Programs\\Java\\DPGRAPH\\out\\retail\\DFGFI1\\" +
    //            "Result_0.007_0.1_2015_07_20_22_52_35.txt";
    //    String sStand  = "D:\\Programs\\Java\\DPGRAPH\\out\\retail\\Apriori\\" +
    //            "Result_0.007_Standard.txt";
    //    String sOrg    = sStand;

    //    Contrast.Contrast( sResult, sStand, sOrg, 100 );
        Contrast.ContrastSmart( sResult, sOrg, -1 );
    //    Contrast.ContrastSmartWithoutOneGraph( sResult );

    }

    static {
        dPrecision = 0;
        dRecall    = 0;
        dFscore    = 0;
        dRE        = 0;
    }

    public static void Contrast( String sResultFile, String sStandardFile, String sOrgFile, int k ){

        HashMap<String,Double> hResult = new HashMap<String,Double>();
        HashMap<String,Double> hStandard = new HashMap<String,Double>();
        HashMap<String,Double> hOrg   = new HashMap<String,Double>();

        try{
            BufferedReader rResult = new BufferedReader(new FileReader( sResultFile ));
            BufferedReader rStand = new BufferedReader(new FileReader( sStandardFile ));
            String sLine = null;
            while( (sLine = rResult.readLine()) != null ){
                sLine = sLine.trim();

                if( !sLine.contains( ":" ) ){
                    break;
                }

                String[] sPart = sLine.split( ":" );
                double dSupport = 0;
                if( sPart.length >= 2 ){
                    dSupport = Double.parseDouble( sPart[1] );
                }

                hResult.put(sPart[0], dSupport);
            }
            rResult.close();

            while( (sLine = rStand.readLine()) != null ){
                sLine = sLine.trim();

                if( !sLine.contains( ":" ) ){
                    break;
                }

                String[] sPart = sLine.split( ":" );
                double dSupport = 0;
                if( sPart.length == 2 ){
                    dSupport = Double.parseDouble( sPart[1] );
                }

                hOrg.put( sPart[0], dSupport );

                if( hStandard.size() < k ){
                    hStandard.put(sPart[0], dSupport);
                }
            }
            rStand.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int nResultNum = hResult.size();
        int nStandNum  = hStandard.size();

        Set<String> hResultSet = hResult.keySet();
        Set<String> hStandSet  = hStandard.keySet();
        hStandSet.retainAll( hResultSet );
        int nIntersaction = hStandSet.size();

        System.out.println( "Intersaction : " + nIntersaction );
        System.out.println( "|Result| : " + nResultNum );
        System.out.println( "|Stand|  : " + nStandNum );
        System.out.println( "|Original|   : " + hOrg.size() );

        dPrecision = nIntersaction / (double)nResultNum;
        dRecall = nIntersaction / (double)nStandNum;
        dFscore = 2*dPrecision*dRecall / ( dPrecision + dRecall );

        LinkedList<Double> lREList = new LinkedList<Double>();

    //    int nCnt = 0;
        for( String sCanLabel : hResult.keySet() ){
            double dResultSupport = hResult.get(sCanLabel);
            double dStandSupport  = 0;
            if( hOrg.containsKey( sCanLabel ) ) {
                dStandSupport  = hOrg.get( sCanLabel );
            //    nCnt++;
            }
            double dRelativeError = Math.abs( dResultSupport - dStandSupport );
            dRelativeError /= Math.abs( dStandSupport );
            lREList.add(dRelativeError);
        }

    //    System.out.println( nCnt );

        Collections.sort( lREList );
    //    System.out.println( lREList );
        dRE = lREList.get( (int)(lREList.size()/2) );          //iejr: RE

        if( dRE > 1024000 ){
            for( double element : lREList ){
                if( element < 1024000 ){
                    dRE = element;
                }else{
                    break;
                }
            }
        }


        /*

        for( String sCanLabel : hStandard.keySet() ){
            double dResultSupport = 0;
            if( hResult.containsKey( sCanLabel ) ) {
                dResultSupport = hResult.get(sCanLabel);
            }
            double dStandSupport  = hStandard.get( sCanLabel );
            double dRelativeError = Math.abs( dResultSupport - dStandSupport );
            dRelativeError /= dStandSupport;
            lREList.add(dRelativeError);
        }

        for( double d : lREList ){                      //iejr: ARE
            dRE += d;
        }
        dRE /= lREList.size();
        dRE = 1 - dRE;
        */
        try{
            FileWriter w = new FileWriter( sResultFile, true );

            w.write( "\r\n" );
            w.write( dPrecision + "\r\n" + dRecall + "\r\n" +
                    dFscore + "\r\n" + dRE + "\r\n");

            w.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch ( IOException e ){
            e.printStackTrace();
        }

        System.out.println( "Precision:\t" + dPrecision );
        System.out.println( "Recall:\t" + dRecall );
        System.out.println( "F-score:\t" + dFscore );
        System.out.println( "RE:\t" + dRE );

    }

//iejr: when k = -1, it means it's a threshold mining
    public static void ContrastSmart( String sResultFile, String sStandardFile, int k ){

        HashMap<String,Double> hResult = new HashMap<String,Double>();
        HashMap<String,Double> hStandard = new HashMap<String,Double>();
        HashMap<String,Double> hOrg   = new HashMap<String,Double>();

        if( k == -1 ){
            k = Integer.MAX_VALUE;
        }

        try{
            BufferedReader rResult = new BufferedReader(new FileReader( sResultFile ));
            BufferedReader rStand = new BufferedReader(new FileReader( sStandardFile ));
            String sLine = null;

            int nCnt = 1;

            while( (sLine = rResult.readLine()) != null ){
                sLine = sLine.trim();

                if( !sLine.contains( ":" ) ){
                    break;
                }

                String[] sPart = sLine.split( ":" );
                double dSupport = 0;
                double dNoisySupport = 0;
                if( sPart.length == 3 ){
                    dSupport = Double.parseDouble( sPart[2] );
                    dNoisySupport = Double.parseDouble( sPart[1] );
                }else{
                    System.out.println( sLine );
                }

                hResult.put(sPart[0], dNoisySupport );
                hOrg.put( sPart[0], dSupport );
                nCnt++;
            }
            rResult.close();

            while( (sLine = rStand.readLine()) != null ){
                sLine = sLine.trim();

                if( !sLine.contains( ":" ) ){
                    break;
                }

                String[] sPart = sLine.split( ":" );
                double dSupport = 0;
                if( sPart.length == 2 ){
                    dSupport = Double.parseDouble( sPart[1] );
                }

            //    hOrg.put( sPart[0], dSupport );

                if( hStandard.size() < k ){
                    hStandard.put(sPart[0], dSupport);
                }
            }
            rStand.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int nResultNum = hResult.size();
        int nStandNum  = hStandard.size();

        Set<String> hResultSet = hResult.keySet();
        Set<String> hStandSet  = hStandard.keySet();
        hStandSet.retainAll( hResultSet );
        int nIntersaction = hStandSet.size();

        System.out.println( "Intersaction : " + nIntersaction );
        System.out.println( "|Result| : " + nResultNum );
        System.out.println( "|Stand|  : " + nStandNum );
        System.out.println( "|Original|   : " + hOrg.size() );

        dPrecision = nIntersaction / (double)nResultNum;
        dRecall = nIntersaction / (double)nStandNum;
        dFscore = 2*dPrecision*dRecall / ( dPrecision + dRecall );

        LinkedList<Double> lREList = new LinkedList<Double>();

        //    int nCnt = 0;
        for( String sCanLabel : hResult.keySet() ){
            double dResultSupport = hResult.get(sCanLabel);
            double dStandSupport  = 0;
            if( hOrg.containsKey( sCanLabel ) ) {
                dStandSupport  = hOrg.get( sCanLabel );
                //    nCnt++;
            }
            double dRelativeError = Math.abs( dResultSupport - dStandSupport );
            dRelativeError /= dStandSupport;
            lREList.add(dRelativeError);
        }

        //    System.out.println( nCnt );

        Collections.sort( lREList );
        //    System.out.println( lREList );
        dRE = lREList.get( (int)(lREList.size()/2) );          //iejr: RE


        if( dRE > Integer.MAX_VALUE ){
            for( double element : lREList ){
                if( element < Integer.MAX_VALUE ){
                    dRE = element;
                }else{
                    break;
                }
            }
        }
        /*

        for( String sCanLabel : hStandard.keySet() ){
            double dResultSupport = 0;
            if( hResult.containsKey( sCanLabel ) ) {
                dResultSupport = hResult.get(sCanLabel);
            }
            double dStandSupport  = hStandard.get( sCanLabel );
            double dRelativeError = Math.abs( dResultSupport - dStandSupport );
            dRelativeError /= dStandSupport;
            lREList.add(dRelativeError);
        }

        for( double d : lREList ){                      //iejr: ARE
            dRE += d;
        }
        dRE /= lREList.size();
        dRE = 1 - dRE;
        */
        try{
            FileWriter w = new FileWriter( sResultFile, true );

            w.write( "\r\n" );
            w.write( dPrecision + "\r\n" + dRecall + "\r\n" + dFscore + "\r\n" + dRE + "\r\n");

            w.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch ( IOException e ){
            e.printStackTrace();
        }

        System.out.println( "Precision:\t" + dPrecision );
        System.out.println( "Recall:\t" + dRecall );
        System.out.println( "F-score:\t" + dFscore );
        System.out.println( "RE:\t" + dRE );

    }


    public static void ContrastSmartWithoutOneGraph( String sResultFile ){

        HashMap<String,Double> hResult = new HashMap<String,Double>();
        HashMap<String,Double> hStandard = new HashMap<String,Double>();
        HashMap<String,Double> hOrg   = new HashMap<String,Double>();

    //    if( k == -1 ){
    //        k = Integer.MAX_VALUE;
    //    }

        try{
            BufferedReader rResult = new BufferedReader(new FileReader( sResultFile ));
        //    BufferedReader rStand = new BufferedReader(new FileReader( sStandardFile ));
            String sLine = null;
            while( (sLine = rResult.readLine()) != null ){
                sLine = sLine.trim();

                if( !sLine.contains( ":" ) ){
                    break;
                }

                String[] sPart = sLine.split( ":" );
                double dSupport = 0;
                double dNoisySupport = 0;
                if( sPart.length == 3 ){
                    dSupport = Double.parseDouble( sPart[2] );
                    dNoisySupport = Double.parseDouble( sPart[1] );
                }

                if( sPart[0].length() < 6 ){
                    continue;
                }
                hResult.put(sPart[0], dNoisySupport );
                hOrg.put( sPart[0], dSupport );
            }
            rResult.close();

            /*
            while( (sLine = rStand.readLine()) != null ){
                sLine = sLine.trim();

                if( !sLine.contains( ":" ) ){
                    break;
                }

                String[] sPart = sLine.split( ":" );
                double dSupport = 0;
                if( sPart.length == 2 ){
                    dSupport = Double.parseDouble( sPart[1] );
                }

                //    hOrg.put( sPart[0], dSupport );

                if( hStandard.size() < k ){
                    hStandard.put(sPart[0], dSupport);
                }
            }
            rStand.close();
            */
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        int nResultNum = hResult.size();
        int nStandNum  = hStandard.size();

        Set<String> hResultSet = hResult.keySet();
        Set<String> hStandSet  = hStandard.keySet();
        hStandSet.retainAll( hResultSet );
        int nIntersaction = hStandSet.size();

        System.out.println( "Intersaction : " + nIntersaction );
        System.out.println( "|Result| : " + nResultNum );
        System.out.println( "|Stand|  : " + nStandNum );
        System.out.println( "|Original|   : " + hOrg.size() );

        dPrecision = nIntersaction / (double)nResultNum;
        dRecall = nIntersaction / (double)nStandNum;
        dFscore = 2*dPrecision*dRecall / ( dPrecision + dRecall );
*/
        LinkedList<Double> lREList = new LinkedList<Double>();

        //    int nCnt = 0;
        for( String sCanLabel : hResult.keySet() ){
            double dResultSupport = hResult.get(sCanLabel);
            double dStandSupport  = 0;
            if( hOrg.containsKey( sCanLabel ) ) {
                dStandSupport  = hOrg.get( sCanLabel );
                //    nCnt++;
            }
            double dRelativeError = Math.abs( dResultSupport - dStandSupport );
            dRelativeError /= dStandSupport;
           lREList.add(dRelativeError);
        }

        //    System.out.println( nCnt );

        Collections.sort( lREList );
        //    System.out.println( lREList );
        dRE = lREList.get( lREList.size() - 1 );          //iejr: RE

        int nThreshold = 1;
        if( dRE > nThreshold ){
            for( double element : lREList ){
                if( element < nThreshold ){
                    dRE = element;
                }else{
                    break;
                }
            }
        }

    //    try{
    //        FileWriter w = new FileWriter( sResultFile, true );

    //        w.write( "\r\n" );
    //        w.write( dPrecision + "\r\n" + dRecall + "\r\n" + dFscore + "\r\n" + dRE + "\r\n");

    //        w.close();
    //    } catch (FileNotFoundException e){
    //        e.printStackTrace();
    //    } catch ( IOException e ){
    //        e.printStackTrace();
    //    }

    //    System.out.println( "Precision:\t" + dPrecision );
    //    System.out.println( "Recall:\t" + dRecall );
    //    System.out.println( "F-score:\t" + dFscore );
        System.out.println( "RE:\t" + dRE );

    }


    public static double RERecount( String sFilePath, double e2, int nRepeat ){

        ArrayList<Double> aRealSupport = new ArrayList<Double>();

        try{
            BufferedReader rResult = new BufferedReader(new FileReader( sFilePath ));
            String sLine = null;
            while( (sLine = rResult.readLine()) != null ){
                sLine = sLine.trim();

                if( !sLine.contains( ":" ) ){
                    break;
                }

                String[] sPart = sLine.split( ":" );
                double dSupport = 0;
                if( sPart.length == 3 ){
                    dSupport = Double.parseDouble( sPart[2] );
                    aRealSupport.add( dSupport );
                }

            }
            rResult.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int nSensitivity = aRealSupport.size();

        System.out.println("Sensitivity : " + nSensitivity);

        double dRE = -1;
        double dREavg = 0;
        for( int i = 0;i < nRepeat;i++ ) {
            LinkedList<Double> lREList = new LinkedList<Double>();

            for (double dRealSupport : aRealSupport) {
                double dNoisyDelta = Distribution.laplace(e2, nSensitivity);
                dNoisyDelta = Math.abs(dNoisyDelta);
                double dRelativeError = dNoisyDelta / dRealSupport;

                lREList.add(dRelativeError);
            }

            Collections.sort(lREList);

            dRE = lREList.get(nSensitivity / 2);

            if (dRE > Integer.MAX_VALUE) {
                for (double element : lREList) {
                    if (element < Integer.MAX_VALUE) {
                        dRE = element;
                    } else {
                        break;
                    }
                }
            }

            System.out.println("RE : " + dRE);

            dREavg += dRE;
        }

        dREavg /= nRepeat;
        System.out.println("avg. RE : " + dREavg);
        return dREavg;
    }

}
