package DFG;

import Common.*;
import Dataset.DatasetParameter;
import FSG.GraphSet;
import FSG.LabelGraphList;
// import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.util.*;

/**
 * Created by iejr on 2015/7/6.
 */
public class DFG extends FSG.FSG{

    protected double e1,e2;
    protected HashMap<String,Integer> hCanLabel2NodeID;
    protected HashMap<Integer,String> hNodeID2CanLabel;
    protected HashMap<String,Double>  hLabel2RealSupport;


    //iejr: main function declearation
    public static void main( String[] args ){

        String sDataset = "Cancer";
        double threshold = 0.4;
        int    dbSize = 32557;
        double e1 = 0.1;
        double e2 = 0.1;

        DFG.testThresholdMining(sDataset, threshold, dbSize, e1, e2);

    }

    public DFG(){
        super();
    }

    public void setParameter( String sDatasetPath, double dMinsup,
                              String sOutputFilePath, String sOutputDetailPath,
                              double e1, double e2 ){

        super.setParameter( sDatasetPath, dMinsup, sOutputFilePath, sOutputDetailPath );
        this.e1 = e1;
        this.e2 = e2;
    }

    protected void setNoisySupport( int k ){
        double dNoisy = Distribution.laplace( this.e1, 2 );

        //iejr: for debug
        //    dNoisy = -1500;
        System.out.println( "Th noise : " + dNoisy );

        this.dMinsup += dNoisy;
    }

    protected void setNoisySupport( double dThreshold ){
        double dNoisy = Distribution.laplace( this.e1, 1 );
        this.dMinsup += dNoisy;
    }

    public ArrayList<GraphSet> InitialPhase(){
        this.hCanLabel2NodeID = new HashMap<String,Integer>();
        this.hNodeID2CanLabel = new HashMap<Integer,String>();
        this.hCanLabel2Support = new HashMap<String,Double>();
        this.hLabel2RealSupport = new HashMap<String,Double>();
        return super.InitialPhase();
    }

    protected Lattice buildLattice(){
        Lattice lFreLattice = new Lattice();

        int nUniqueNodeID = 0;

        String sOutputLatticeFile = this.sOutputDetailPath + "DFG_" +
                this.dMinsup + "_" + Parameter.getTime() +".txt";
        try {

            FileWriter w = new FileWriter( sOutputLatticeFile, true );

            for (String sCanLabel : this.aFreCanLabel) {

                int nNodeID = -1;
                if (!this.hCanLabel2NodeID.containsKey(sCanLabel)) {
                    this.hCanLabel2NodeID.put(sCanLabel, nUniqueNodeID);
                    this.hNodeID2CanLabel.put(nUniqueNodeID, sCanLabel);
                    nNodeID = nUniqueNodeID;
                    nUniqueNodeID++;
                } else {
                    nNodeID = this.hCanLabel2NodeID.get(sCanLabel);
                }

                GraphSet gGraph = this.hCanLabel2Graph.get(sCanLabel);
                int nNodeLayer = gGraph.lGraph.getRank();
                if (gGraph.aSubCanLabel == null || gGraph.aSubCanLabel.size() == 0) {
                    if (gGraph.lGraph.getRank() == 1) {
                        lFreLattice.addNode(nNodeID);

                        w.write( nNodeID + "->" + "null"  + "\r\n" );
                    }
                    continue;
                }

                for (String sSubCanLabel : gGraph.aSubCanLabel) {
                    GraphSet gSubGraph = this.hCanLabel2Graph.get(sSubCanLabel);
                    if (!gSubGraph.lGraph.judgeConnected()) {
                        continue;
                    }

                    int nSubNodeID = -1;
                    if (!this.hCanLabel2NodeID.containsKey(sSubCanLabel)) {
                        this.hCanLabel2NodeID.put(sSubCanLabel, nUniqueNodeID);
                        this.hNodeID2CanLabel.put(nUniqueNodeID, sSubCanLabel);
                        nSubNodeID = nUniqueNodeID;
                        nUniqueNodeID++;
                    } else {
                        nSubNodeID = this.hCanLabel2NodeID.get(sSubCanLabel);
                    }

                    lFreLattice.addNode(nSubNodeID, nNodeID);

                    w.write( nNodeID + "->" + nSubNodeID + "\r\n" );
                    //   GraphSet gSubGraph = this.hCanLabel2Graph.get( sSubCanLabel );
                    int nSubNodeLayer = gSubGraph.lGraph.getRank();
                    lFreLattice.setNodeLayer(nSubNodeID, nSubNodeLayer);
                    lFreLattice.setNodeLayer(nNodeID, nNodeLayer);
                }
            }

            //iejr: write real support to file, identified by Node ID
            w.write( "\r\n" );
            for( String sCanLabel : this.hCanLabel2NodeID.keySet() ){
                int nNodeID = this.hCanLabel2NodeID.get( sCanLabel );
                double nRealSupport = this.hCanLabel2Support.get( sCanLabel );
                w.write( nNodeID + ":" + nRealSupport + "\r\n" );
            }

            w.close();
        } catch ( FileNotFoundException e ){
            e.printStackTrace();
        } catch ( IOException e ){
            e.printStackTrace();
        }

        return lFreLattice;
    }


    protected void getNoisySupport( Lattice lFreLattice ){

        HashSet<ArrayList<Integer>> hPathSet = lFreLattice.PathConstruction();

        int nPartSize = hPathSet.size();
        double e = this.e2 / nPartSize;
        for( ArrayList<Integer> aPath : hPathSet ){

            for( int i = 0;i < aPath.size();i++ ){
                String sCanLabel = this.hNodeID2CanLabel.get( aPath.get( i ) );
                System.out.print( aPath.get(i) + "->" );
                double dNoisy = 0;
                for( int j = 0;j <= i;j++ ){
                    dNoisy += Distribution.laplace( e, 1 );
                }

                GraphSet gGraph = this.hCanLabel2Graph.get( sCanLabel );
                double dRealSuppot = this.hCanLabel2Support.get( sCanLabel );
                this.hCanLabel2Support.put( sCanLabel, dRealSuppot + dNoisy );
                this.hLabel2RealSupport.put( sCanLabel, dRealSuppot );
            }
            System.out.println();
        }
    }

    protected void getNoisySuppot(){
        int nFreGraphNum = this.aFreCanLabel.size();
        int nSensitivity = nFreGraphNum;

        for( String sCanLabel : this.aFreCanLabel ){
            double dNoisy = Distribution.laplace( this.e2, nSensitivity );
            double dNoisySupport = this.hCanLabel2Support.get(sCanLabel);
            dNoisySupport += dNoisy;
            this.hCanLabel2Support.put(sCanLabel, dNoisySupport);
            this.hLabel2RealSupport.put( sCanLabel,
                    this.hCanLabel2Support.get(sCanLabel) );
        }
    }


    protected void writeFrequentGraph( boolean bIsNoisySupport ){

        String sOutputGraph = this.sOutputFilePath;
        String sOutputDetail = this.sOutputDetailPath;

        if( sOutputGraph == null ){
            return;
        }

        File file = new File(sOutputGraph);
        try {
            if (file.exists())
                file.delete();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[] dSupportArray = new double[this.aFreCanLabel.size()];
        for( int i = 0;i < dSupportArray.length;i++ ){
            GraphSet gGraph = this.hCanLabel2Graph.get( this.aFreCanLabel.get( i ) );
            double dSupport = this.hCanLabel2Support.get( this.aFreCanLabel.get( i ) );
            dSupportArray[i] = dSupport * -1;
        }

        Sort sSort = new Sort( dSupportArray );
        SortNode[] sResult = sSort.FastSort();

        try {

            FileWriter w = new FileWriter( sOutputGraph , true);
            for (int i = 0; i < sResult.length; i++) {
                int nIndex = sResult[i].nIndex;
                String sCanLabel = this.aFreCanLabel.get(nIndex);
                double dSupport = sResult[i].dElement * -1;
                double dRealSupport = this.hLabel2RealSupport.get( sCanLabel );

                w.write( sCanLabel + ":" + dSupport + ":" + dRealSupport + "\r\n" );

                if( sOutputDetail != null ) {
                    String sOutputDetailName = sOutputDetail + "/" + i + ".txt";

                    file = new File(sOutputDetailName);
                    try {
                        if (file.exists())
                            file.delete();
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    FileWriter wDetail = new FileWriter(sOutputDetailName, true);
                    GraphSet gGraph = this.hCanLabel2Graph.get(sCanLabel);
                    LabelGraphList lGraph = gGraph.lGraph;
                    String sVerLabel = lGraph.getAllVertexLabel();
                    String[] sEdgeRecord = lGraph.getAllEdgeRecord();

                    wDetail.write(sVerLabel + "\r\n");
                    for (String sEdge : sEdgeRecord) {
                        wDetail.write(sEdge + "\r\n");
                    }
                    wDetail.close();
                }
            }

            w.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public double getREFromFile( String sFilePath, double e2, int nRepeart ){

        Lattice lFreLattice = new Lattice();

        HashMap<Integer,Double> hNodeID2Support = new HashMap<Integer,Double>();

        try{
            BufferedReader r = new BufferedReader( new FileReader( sFilePath ) );

            String sLine;
            int nState = 0;   //iejr: 0 represent for read relationship between nodes
                                //    1 represent for read real support for each node
            while( ( sLine = r.readLine() ) != null ){

                sLine = sLine.trim();
                if( !sLine.contains( "->" ) ){
                    nState = 1;
                }

                switch ( nState ){
                    case 0 : {

                        String[] sNodeIDs = sLine.split( "->" );

                        if( sNodeIDs[1].equals( "null" ) ){
                            int nNodeID = Integer.parseInt( sNodeIDs[0] );
                            lFreLattice.addNode( nNodeID );
                        } else {
                            int nNodeID = Integer.parseInt( sNodeIDs[0] );
                            int nSubNodeID = Integer.parseInt( sNodeIDs[1] );
                            lFreLattice.addNode( nSubNodeID, nNodeID );
                        }

                    }

                    case 1 : {

                        String[] sDetail = sLine.split( ":" );
                        if( sDetail.length != 2 ){
                            continue;
                        }

                        int nNodeID = Integer.parseInt( sDetail[0] );
                        double dRealSupport = Double.parseDouble( sDetail[1] );

                        hNodeID2Support.put( nNodeID, dRealSupport );
                    }
                }

            }

            r.close();
        } catch ( FileNotFoundException e ){
            e.printStackTrace();
        } catch ( IOException e ){
            e.printStackTrace();
        }

        System.out.println( "Frequent graph number : " + hNodeID2Support.size() );

        HashSet<ArrayList<Integer>> hPathSet = lFreLattice.PathConstruction();

        int nPartSize = hPathSet.size();
        double e = e2 / nPartSize;
        LinkedList<Double> lREList = new LinkedList<Double>();
        for( ArrayList<Integer> aPath : hPathSet ){

            for( int i = 0;i < aPath.size();i++ ){
            //    String sCanLabel = this.hNodeID2CanLabel.get( aPath.get( i ) );

                int nNodeID = aPath.get( i );
                System.out.print( nNodeID + "->" );
                double dNoisy = 0;
                for( int j = 0;j <= i;j++ ){
                    dNoisy += Distribution.laplace( e, 1 );
                }

                double dRealSupport = hNodeID2Support.get( nNodeID );
                double dRelativeError = Math.abs( dNoisy ) / dRealSupport;

                lREList.add( dRelativeError );
            //    GraphSet gGraph = this.hCanLabel2Graph.get( sCanLabel );
            //    double dRealSuppot = this.hCanLabel2Support.get( sCanLabel );
            //    this.hCanLabel2Support.put( sCanLabel, dRealSuppot + dNoisy );
            //    this.hLabel2RealSupport.put( sCanLabel, dRealSuppot );
            }
            System.out.println();
        }

        Collections.sort( lREList );

        double dRe = lREList.get( lREList.size() / 2 );

        System.out.println( "RE : " + dRe );

        return dRe;
    }


    public static void testPhase1( String sDataset, int k, double e1, double e2 ){
        String sDatasetName = sDataset;
        String sThreshold   = "One";
        double dThreshold   = 1;
        if( k == 50 ){
            dThreshold = 0.713;//0.713;
        }else if( k == 100 ){
            dThreshold = 0.5823;
        }else if( k == 200 ){
            dThreshold = 0.4974;
        }
        String sDatasetPath = DatasetParameter.getDatasetPath(sDatasetName);
        double dSupport = DatasetParameter.getAbusoluteThreshold( sDatasetName,
                dThreshold );

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }
        String sTime = Parameter.getTime();

        String sOutPutPath = DatasetParameter.sDataResultPrefix + sDatasetName
                + "/DFG/threshold/Result_Top" + k + "_" + e1 + "_" + e2 +
                "_" + sTime + ".txt";
        String sOutPutDetailPath = DatasetParameter.sDataResultPrefix +
                sDatasetName + "/DFG/detail/";

        long nStart = System.currentTimeMillis();

        DFG myDFG = new DFG();
        myDFG.setParameter( sDatasetPath, dSupport, sOutPutPath,
                sOutPutDetailPath, e1, e2 );
        myDFG.setNoisySupport( k );
        System.out.println( myDFG.dMinsup );

        ArrayList<GraphSet> aFreTwoGraph = myDFG.InitialPhase();
        myDFG.ButtonUpPhase( aFreTwoGraph );

        long nEnd = System.currentTimeMillis();

        int nFreSubGraphNum = myDFG.hFrequentSubgraphSet.size();
        System.out.println( "Total frequent sub graph number is: " + nFreSubGraphNum );

    //    Lattice lFreGraphLattice = myDFG.buildLattice();
    //    myDFG.getNoisySupport( lFreGraphLattice );
        myDFG.writeFrequentGraph();
        myDFG.writeTime( nEnd - nStart, sOutPutPath );
    }


    public static void testTopkMining( String sDataset, int k, double e1, double e2 ){
        String sDatasetName = sDataset;
        String sThreshold   = "One";
        double dThreshold   = DatasetParameter.getCorrespondThreshold(sDatasetName, k);
        if( dThreshold == -1 ){
            System.out.println( "No such k prepared!" );
            System.exit( 0 );
        }

        String sDatasetPath = DatasetParameter.getDatasetPath(sDatasetName);
        double dSupport = DatasetParameter.getAbusoluteThreshold( sDatasetName,
                dThreshold );

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }
        String sTime = Parameter.getTime();

        String sOutPutPath = DatasetParameter.sDataResultPrefix + sDatasetName
                + "/DFG/top/Result_Top" + k + "_" + e1 + "_" + e2
                + "_" + sTime + ".txt";
        String sOutPutDetailPath = DatasetParameter.sDataResultPrefix
                + sDatasetName + "/DFG/detail/";

        System.out.println( "DFG top-k based mining..." );

        long nStart = System.currentTimeMillis();

        DFG myDFG = new DFG();
        myDFG.setParameter( sDatasetPath, dSupport, sOutPutPath,
                sOutPutDetailPath, e1, e2 );
        myDFG.setNoisySupport( k );
        System.out.println( myDFG.dMinsup );

        ArrayList<GraphSet> aFreTwoGraph = myDFG.InitialPhase();
        myDFG.ButtonUpPhase( aFreTwoGraph );

        long nEnd = System.currentTimeMillis();

        int nFreSubGraphNum = myDFG.hFrequentSubgraphSet.size();
        System.out.println( "Total frequent sub graph number is: " + nFreSubGraphNum );

        Lattice lFreGraphLattice = myDFG.buildLattice();
        myDFG.getNoisySupport( lFreGraphLattice );
        myDFG.writeFrequentGraph( true );
        myDFG.writeTime( nEnd - nStart, sOutPutPath );

        String sStand = DatasetParameter.sDataResultPrefix + sDatasetName
                + DatasetParameter.getContrastFile( sDatasetName );
        Contrast.Contrast(sOutPutPath, sStand, sStand, k);
    }

    // what to do?
    public static void testThresholdMining( String sDataset, double dThreshold,
                                            int nDatasize, double e1,
                                            double e2 ){

        String sDatasetName = sDataset;
        String sDatasetPath = "./Dataset/" + sDatasetName + ".dat";
        double dSupport = dThreshold * nDatasize;

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }
        String sTime = Parameter.getTime();

        String sOutPutPath = "./out/" + sDatasetName +
                "/DFG/threshold/Result_Th" + dThreshold + "_" + e1 + "_" +
                e2 + "_" + sTime + ".txt";
        String sOutPutDetailPath = "./out/" + sDatasetName + "/DFG/detail/";

        System.out.println( "DFG threshold based mining..." );

        long nStart = System.currentTimeMillis();

        DFG myDFG = new DFG();
        myDFG.setParameter( sDatasetPath, dSupport, sOutPutPath,
                sOutPutDetailPath, e1, e2 );
        myDFG.setNoisySupport(dThreshold);
        System.out.println( myDFG.dMinsup );

        ArrayList<GraphSet> aFreTwoGraph = myDFG.InitialPhase();
        myDFG.ButtonUpPhase( aFreTwoGraph );

        long nEnd = System.currentTimeMillis();

        int nFreSubGraphNum = myDFG.hFrequentSubgraphSet.size();
        System.out.println( "Total frequent sub graph number is: " + nFreSubGraphNum );

        Lattice lFreGraphLattice = myDFG.buildLattice();
        myDFG.getNoisySupport( lFreGraphLattice );
        myDFG.writeFrequentGraph( true );
        myDFG.writeTime( nEnd - nStart, sOutPutPath);

        String sStand = "./out/" + sDatasetName + "/FSG/" + "Result_" +
                dThreshold + "_" + "Standard" + ".txt";
        Contrast.ContrastSmart(sOutPutPath, sStand, -1);
    }


    public static void testThresholdWithoutNSD( String sDataset,
                                                int nThresholdRank,
                                                double e1, double e2 ){
        String sDatasetName = sDataset;
        String sThreshold   = "One";
        double dThreshold   = DatasetParameter.getCorrespondThresholdByRank(sDatasetName,
                nThresholdRank);
        if( dThreshold == -1 ){
            System.out.println( "No such k prepared!" );
            System.exit( 0 );
        }

        String sDatasetPath = DatasetParameter.getDatasetPath(sDatasetName);
        double dSupport = DatasetParameter.getAbusoluteThreshold( sDatasetName,
                dThreshold );

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }
        String sTime = Parameter.getTime();

        String sOutPutPath = DatasetParameter.sDataResultPrefix + sDatasetName
                + "/DFG/threshold/ResultNoNSD_Th" + nThresholdRank + "_" + e1
                + "_" + e2 + "_" + sTime + ".txt";
        String sOutPutDetailPath = DatasetParameter.sDataResultPrefix
                + sDatasetName + "/DFG/detail/";

        System.out.println( "DFG threshold based mining without " +
                "Noisy Support Derivation..." );

        long nStart = System.currentTimeMillis();

        DFG myDFG = new DFG();
        myDFG.setParameter( sDatasetPath, dSupport, sOutPutPath,
                sOutPutDetailPath, e1, e2 );
        myDFG.setNoisySupport( dThreshold );
        System.out.println( myDFG.dMinsup );

        ArrayList<GraphSet> aFreTwoGraph = myDFG.InitialPhase();
        myDFG.ButtonUpPhase( aFreTwoGraph );

        long nEnd = System.currentTimeMillis();

        int nFreSubGraphNum = myDFG.hFrequentSubgraphSet.size();
        System.out.println( "Total frequent sub graph number is: " + nFreSubGraphNum );

        myDFG.getNoisySuppot();
        myDFG.writeFrequentGraph( true );
        myDFG.writeTime( nEnd - nStart, sOutPutPath);

        String sStand = DatasetParameter.sDataResultPrefix + sDatasetName +
                DatasetParameter.getContrastFile( sDatasetName, dThreshold );
        Contrast.ContrastSmart(sOutPutPath, sStand, -1);
    }

    public static void testThresholdWithoutNSD( String sFilePath, double e2 ){

        Contrast.RERecount( sFilePath, e2, 20 );

    }

    public static void testPhase2( String sFilePath, double e2 ){

        DFG myDFG = new DFG();

        myDFG.getREFromFile( sFilePath, e2, -1 );

    }
}
