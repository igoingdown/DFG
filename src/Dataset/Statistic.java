package Dataset;

import FSG.GraphSet;
import FSG.LabelGraphList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by iejr on 2015/6/30.
 */
public class Statistic {

    public static void main( String[] args ){

        String sDatasetName = "AID-single";
        String sDatasetPath = DatasetParameter.getDatasetPath(sDatasetName);

        if( sDatasetPath == null ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }

        Statistic.getGraphDatasetStatistic(sDatasetPath);

    }


    public static void getGraphDatasetStatistic( String sDatasetPath ){

        int nGraphCount = 0;
        int nTotalSize  = 0;
        int nTotalRank  = 0;
        int nTotalDegree = 0;
        int nMaxSize = -1;
        int nMaxRank = -1;
        HashSet<String> hLabelVertex = new HashSet<String>();
        HashSet<Double> hLabelEdge   = new HashSet<Double>();

        try {
            BufferedReader r = new BufferedReader(new FileReader( sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nVertexUniqueID = 0;


            while( (sLine = r.readLine()) != null ){

                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split( " " );
                LabelGraphList lGraph = new LabelGraphList( sVertexLabel.length );
                for( int i = 0;i < sVertexLabel.length;i++ ){
                    lGraph.setVertexLabel( i, sVertexLabel[i] );
                    lGraph.setVerUniID( i, nVertexUniqueID++ );

                    hLabelVertex.add( sVertexLabel[i] );
                }
                nTotalSize += lGraph.getSize();

                while( (sLine = r.readLine())!=null ){
                    if( sLine.contains( sEndPattern ) ){
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split( " " );
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel  = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);

                    hLabelEdge.add( dEdgeLabel );
                }
                nTotalRank += lGraph.getRank();
                nTotalDegree += lGraph.getRank()*2;

                if( lGraph.getSize() > nMaxSize ){
                    nMaxSize = lGraph.getSize();
                }
                if( lGraph.getRank() > nMaxRank ){
                    nMaxRank = lGraph.getRank();
                }

                nGraphCount++;
            }
            r.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println( "Total graph number is:\t" + nGraphCount );
        System.out.println( "Total vertex number is:\t" + nTotalSize + "\tAverage vertex number is:\t" + nTotalSize/(double)nGraphCount );
        System.out.println( "Total edge number is:\t" + nTotalRank + "\tAverage edge number is:\t" + nTotalRank/(double)nGraphCount );
        System.out.println( "Total degree number is:\t" + nTotalDegree + "\tAverage degree number is:\t" + nTotalDegree/(double)nTotalSize );
        System.out.println( "Max vertex number is:\t" + nMaxSize );
        System.out.println( "Max edge number is:\t" + nMaxRank );
        System.out.println( "Total vertex label type number is:\t" + hLabelVertex.size() );
        System.out.println( "Total edge label type number is:\t" + hLabelEdge.size() );

        for( double dEdge : hLabelEdge ){
            System.out.println( dEdge );
        }
    }

}
