package Dataset;

import java.io.*;


/**
 * Created by iejr on 2015/6/10.
 */
public class Preprocessing {

    public static void main( String[] args ){
        String sPathPrefix = DatasetParameter.sDatasetPathPrefix;
        String sDatasetName = "CAN2DA99.sdz";
        String sConvertDatasetName = "CAN-single.dat";
        Preprocessing.Convertion( sPathPrefix + sDatasetName, sPathPrefix + sConvertDatasetName );

    }

    public static void Convertion( String sInputFileName, String sOutputFileName ){

        File file = new File(sOutputFileName);
    //    try {
            if (file.exists())
                file.delete();
    //    //    file.createNewFile();
    //    } catch (IOException e) {
    //        e.printStackTrace();
    //    }
        int nTid = 0;

        try {
            BufferedReader r = new BufferedReader(new FileReader(sInputFileName));
            String sLine = null;

            FileWriter w = new FileWriter(sOutputFileName, true);
        //    w.write(seq + "\r\n");

            String sStartPattern       = "V2000";   //0999 V2000
            String sVertexLabelPattern = "-?[0-9]  [0-9]  [0-9]  [0-9]  [0-9] " +
                    "[\\s\\d][0-9]  [0-9]  [0-9]  [0-9]  [0-9]  [0-9] ";
            String sEdgeLabelPattern   = "0  0  0  0";
            boolean bVertexFlag = false;


            while ((sLine = r.readLine()) != null) {

                sLine = sLine.trim();

                if( sLine.contains( sStartPattern ) ){
                //    System.out.println(sStartPattern);
                    w.write( "\r\n" + "EOF" + "\r\n" );
                    nTid++;
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
                    w.write( sVertexLabel + " " );
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
                //    w.write( "\r\n" + sEdge[0] + " " + sEdge[1] + " " + sEdge[2] + sEdge[3] );
                    w.write( "\r\n" + sEdge[0] + " " + sEdge[1] + " 1" );

                //    continue;
                }

            }
            r.close();
            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println( "Dataset size: " + nTid );
    }
}
