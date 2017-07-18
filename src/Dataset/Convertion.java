package Dataset;

import FSG.GraphSet;
import FSG.LabelGraphList;

import java.io.*;
import java.util.HashSet;

/**
 * Created by iejr on 2015/7/7.
 */
public class Convertion {

    public static void main( String[] args ){
        Dataset.Convertion.BuildIndex();
    }

    public static void BuildIndex(  ){
        String sDataSetPath = DatasetParameter.getDatasetPath( "FDA-single" );
        String sOutputPath = DatasetParameter.sDatasetPathPrefix + "FDA/";

        try {
            BufferedReader r = new BufferedReader(new FileReader( sDataSetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            int nVertexUniqueID = 0;

            while( (sLine = r.readLine()) != null ){
                sLine = sLine.trim();

                String sWriteFile = sOutputPath + nTid + ".txt";
                FileWriter w = new FileWriter( sWriteFile, true);
                w.write( sLine + "\r\n" );

                String[] sVertexLabel = sLine.split( " " );

                LabelGraphList lGraph = new LabelGraphList( sVertexLabel.length );
                for( int i = 0;i < sVertexLabel.length;i++ ){
                    lGraph.setVertexLabel( i, sVertexLabel[i] );
                    lGraph.setVerUniID( i, nVertexUniqueID++ );
                }

                while( (sLine = r.readLine())!=null ){
                    if( sLine.contains( sEndPattern ) ){
                        break;
                    }
                    w.write( sLine + "\r\n" );
                    String[] sEdgeInfo = sLine.trim().split( " " );
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel  = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }
                w.close();
                nTid++;
                System.out.println( "Tid:" + nTid );
                //    break;
                //    }
            }
            r.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
