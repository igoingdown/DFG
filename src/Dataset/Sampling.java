package Dataset;

import Common.Parameter;

import java.io.*;
import java.util.Random;

/**
 * Created by iejr on 2015/7/14.
 */
public class Sampling {

    public static void main( String[] args ){

        String sPathPrefix = DatasetParameter.sDatasetPathPrefix;
        String sDatasetName = "CAN2DA99.sdz";
        double dProperty = 0.2;

        Sampling.Sample( sDatasetName , dProperty );

    }

    public static void Sample( String sDataset, double dProperty ){

        String sSampleDatasetPath = DatasetParameter.sDatasetPathPrefix
                + sDataset + "_sample_" + dProperty + ".txt";
        String sDatasetPath = DatasetParameter.sDatasetPathPrefix + sDataset;
        Random rSeed = new Random(Parameter.nRandomSeed);

        if( sDatasetPath == null ){
            return;
        }

        try{
            File f = new File( sSampleDatasetPath );
            if( f.exists() ){
                f.delete();
            }


            BufferedReader r = new BufferedReader(new FileReader(sDatasetPath));
            String sLine = null;

            FileWriter w = new FileWriter(sSampleDatasetPath, true);
            //    w.write(seq + "\r\n");

            String sStartPattern       = "V2000";   //0999 V2000
            String sVertexLabelPattern = "-?[0-9]  [0-9]  [0-9]  [0-9]  [0-9] " +
                    "[\\s\\d][0-9]  [0-9]  [0-9]  [0-9]  [0-9]  [0-9] ";
            String sEdgeLabelPattern   = "0  0  0  0";
            boolean bVertexFlag = false;

            boolean bIsChosen = false;
            int nTid = 0;
            while ((sLine = r.readLine()) != null) {

                sLine = sLine.trim();

                if( sLine.contains( sStartPattern ) ){

                    double dRandom = rSeed.nextDouble();
                    if( dRandom < dProperty ){
                        bIsChosen = true;

                        w.write( "\r\n" + "EOF" + "\r\n" );
                        nTid++;

                    }else{
                        bIsChosen = false;
                    }

                    continue;

                }

                //iejr: matching vertex label
                String[] sMatching = sLine.split( sVertexLabelPattern );
                if( sMatching.length > 1 ){
                    //    System.out.println( sMatching.length );
                    //iejr: find vertex label
                    bVertexFlag = true;
                    String[] sVertexMatching = sMatching[0].trim().split( "\\s+" );
                    int nLength = sVertexMatching.length;
                    String sVertexLabel = sVertexMatching[ nLength - 1 ];
                    //   System.out.print(sVertexLabel + " " );
                    if( bIsChosen ) {
                        w.write(sVertexLabel + " ");
                    }
                    continue;
                }


                //    sMatching = sLine.split( sEdgeLabelPattern );
                if( sLine.contains( sEdgeLabelPattern ) ){
                    if( bVertexFlag ){
                        //    System.out.println();
                        //    w.write( "\r\n" );
                        ;
                    }
                    //iejr: find edge label
                    bVertexFlag = false;
                    String sEdgeInfo = sMatching[0].trim();
                    String[] sEdge = new String[4];
                    int nLength = sEdgeInfo.length();
                    sEdge[3] = sEdgeInfo.substring( nLength-12,nLength-9 ).trim();
                    sEdge[2] = sEdgeInfo.substring( nLength-15, nLength-12 ).trim();
                    sEdge[1] = sEdgeInfo.substring( nLength-18, nLength-15 ).trim();
                    sEdge[0] = sEdgeInfo.substring( 0, nLength - 18 ).trim();
                    //    System.out.println(sEdgeInfo);
                    //    w.write( "\r\n" + sEdge[0] + " " + sEdge[1] + " " +
                    //    sEdge[2] + sEdge[3] );
                    if( bIsChosen ){
                        w.write( "\r\n" + sEdge[0] + " " + sEdge[1] + " 1" );

                    }
                    //    continue;
                }

            }
            r.close();
            w.close();


            System.out.println( "Size : " + nTid );

        } catch(FileNotFoundException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

    }

}
