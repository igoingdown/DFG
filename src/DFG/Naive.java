package DFG;

import Common.*;
import Dataset.DatasetParameter;
import FSG.GraphSet;
import FSG.LabelGraphList;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by iejr on 2015/7/15.
 */
public class Naive extends FSG.FSG {

    protected double e1;
    protected double ee;
    protected int nMaxFreGraphRank;
    protected HashMap<String, Double> hLabel2RealSupport;

    public static void main( String[] args ){

        String Dataset = "Cancer";
        int    dbSize   = 32557;
        int    vertexType = 67;
        double threshold = 0.4;
        double epsilon = 0.1;
        int    maxGraphSize = 10;

        Naive.testThresholdMining( Dataset,threshold, dbSize, maxGraphSize,
                epsilon, vertexType );

    }

    public void setParameter( String sDatasetPath, double dMinsup,
                              String sOutputFilePath, String sOutputDetailPath,
                              double e1, int nMax ){
        super.setParameter( sDatasetPath, dMinsup, sOutputFilePath,
                            sOutputDetailPath );
        this.e1 = e1;
        this.nMaxFreGraphRank = nMax;
        this.ee = e1 / nMax;
    }


    public ArrayList<GraphSet> InitialPhase( int nVertexType ){
        this.hCanLabel2Graph = new HashMap<String,GraphSet>();
        this.hFrequentSubgraphSet = new HashSet<LabelGraphList>();
        this.aFreCanLabel = new ArrayList<String>();
        this.hLabel2RealSupport = new HashMap<String, Double>();

        System.out.println("Mining frequent sub 1,2 graphing...");

        //    HashSet<GraphSet> hFreOneGraph = new HashSet<GraphSet>();
        //    HashSet<GraphSet> hFreTwoGraph = new HashSet<GraphSet>();
        HashMap<String,Integer> hCanLabel2Support1 = new HashMap<String,Integer>();
        HashMap<String,Integer> hCanLabel2Support2 = new HashMap<String,Integer>();
        HashMap<String,GraphSet> hCanLabel2Graph1 = new HashMap<String,GraphSet>();
        HashMap<String,GraphSet> hCanLabel2Graph2 = new HashMap<String,GraphSet>();

        ArrayList<GraphSet> aFreTwoGraph = new ArrayList<GraphSet>();

        try {
            BufferedReader r = new BufferedReader(new FileReader( this.sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            int nVertexUniqueID = 0;

            int nLineID = 0;

            while( (sLine = r.readLine()) != null ){
                //    if( sLine.trim().equals( sStartPattern ) ){
                //    sLine = r.readLine();
                sLine = sLine.trim();

                nLineID++;

                String[] sVertexLabel = sLine.split( " " );
                //    ArrayList<Integer> aVertexLabel = new ArrayList<Integer>();

                //    for( String sVerLabel : sVertexLabel ) {
                //        int nVertexLabel = Parameter.sVertexLabel.indexOf(sVerLabel);
                //        assert( nVertexLabel > -1 );
                //        aVertexLabel.add( nVertexLabel );
                //    }

                LabelGraphList lGraph = new LabelGraphList( sVertexLabel.length );
                for( int i = 0;i < sVertexLabel.length;i++ ){
                    lGraph.setVertexLabel( i, sVertexLabel[i] );
                    lGraph.setVerUniID( i, nVertexUniqueID++ );
                }

                while( (sLine = r.readLine())!=null ){
                    nLineID++;
                    if( sLine.contains( sEndPattern ) ){
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split( " " );
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel  = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                    //    lGraph.setGraphEdge( nExecuteColumn, nExecuteLine, dEdgeLabel );

                    //    System.out.println( nLineID );
                }

                //    lGraph.print();
                //    System.out.println();

                HashSet<GraphSet> hCanSubGraph = getOneSubGraph(lGraph, nTid);
                for( GraphSet gGraph : hCanSubGraph ){
                    String sCanLabel = gGraph.sCanLabel;
                    if( hCanLabel2Support1.containsKey( sCanLabel ) ){
                        int nSupport = hCanLabel2Support1.get( sCanLabel ) + 1;
                        hCanLabel2Support1.put( sCanLabel, nSupport );
                    }else{
                        hCanLabel2Support1.put( sCanLabel, 1 );
                    }

                    if( !hCanLabel2Graph1.containsKey( sCanLabel ) ){
                        hCanLabel2Graph1.put( sCanLabel, gGraph );
                    }else{
                        HashSet<Integer> hTidList =
                                hCanLabel2Graph1.get( sCanLabel ).hTidList;
                        hTidList.addAll(gGraph.hTidList);
                    }
                }
                //    System.out.println();

                hCanSubGraph = getTwoSubGraph(lGraph, nTid);
                for( GraphSet gGraph : hCanSubGraph ){
                    String sCanLabel = gGraph.sCanLabel;
                    if( hCanLabel2Support2.containsKey( sCanLabel ) ){
                        int nSupport = hCanLabel2Support2.get( sCanLabel ) + 1;
                        hCanLabel2Support2.put( sCanLabel, nSupport );
                    }else{
                        hCanLabel2Support2.put( sCanLabel, 1 );
                    }

                    if( !hCanLabel2Graph2.containsKey( sCanLabel ) ){
                        hCanLabel2Graph2.put( sCanLabel, gGraph );
                        //    aFreTwoGraph.add( gGraph );
                    }else{
                        HashSet<Integer> hTidList =
                                hCanLabel2Graph2.get( sCanLabel ).hTidList;
                        hTidList.addAll( gGraph.hTidList );
                    }
                }

                nTid++;
                //    System.out.println( "Tid:" + nTid );
                //    break;
                //    }
            }
            r.close();

            int nSensitivity1 = this.getSubOneGraphSensitivity( nVertexType );
        //    int nSensitivity1 = hCanLabel2Support1.size();

            //iejr: we prune the one is not frequent
            for( String sCanLabel : hCanLabel2Support1.keySet() ){
                int nSupport = hCanLabel2Support1.get( sCanLabel );

                double dNoisy = Distribution.laplace( this.ee, nSensitivity1 );
                double dNoisySupport = nSupport + dNoisy;

                if( dNoisySupport > this.dMinsup ){
                    GraphSet gFreGraph = hCanLabel2Graph1.get( sCanLabel );
                    //    hFreOneGraph.add( gFreGraph );
                    hFrequentSubgraphSet.add( gFreGraph.lGraph );
                    hCanLabel2Graph.put( sCanLabel, gFreGraph );
                    aFreCanLabel.add( sCanLabel );
                    this.hCanLabel2Support.put( sCanLabel, dNoisySupport );
                    this.hLabel2RealSupport.put( sCanLabel, (double)nSupport );
                }
            }

            System.out.println("Candidate sub 1 graph: " +
                    hCanLabel2Graph1.size() + "|Frequent sub 1 graph: "
                    + hFrequentSubgraphSet.size());
            System.out.println( "Sensitivity : " + nSensitivity1 +
                    "|" + "Avg noise scoope : " + (nSensitivity1/this.ee) );

            int nSensitivity2 = this.getSubTwoGraphSensitivity( this.aFreCanLabel );

            for( String sCanLabel : hCanLabel2Support2.keySet() ){
                int nSupport = hCanLabel2Support2.get( sCanLabel );

                double dNoisy = Distribution.laplace( this.ee, nSensitivity2 );
                double dNoisySupport = nSupport + dNoisy;

                System.out.println( "\t" + sCanLabel + " : " + nSupport +
                        " + " + dNoisy + " = " + dNoisySupport );

                if( dNoisySupport > this.dMinsup ){
                    GraphSet gFreGraph = hCanLabel2Graph2.get( sCanLabel );
                    //    hFreTwoGraph.add( gFreGraph );
                    //    LabelGraphList lFreGraph = gFreGraph.lGraph;
                    boolean bIsFrequent = true;
                    HashSet<GraphSet> hSubOneGraph =
                            getOneSubGraph( gFreGraph.lGraph, -1 );
                    for( GraphSet gSubOneGraph : hSubOneGraph ){
                        if( !this.hCanLabel2Graph.containsKey( gSubOneGraph.sCanLabel ) ){
                            bIsFrequent = false;
                            break;
                        }
                        gFreGraph.addSubCanLabel( gSubOneGraph.sCanLabel );
                    }

                    if( !bIsFrequent ){
                        continue;
                    }
                    hFrequentSubgraphSet.add( gFreGraph.lGraph );
                    hCanLabel2Graph.put( sCanLabel, gFreGraph );
                    aFreTwoGraph.add( gFreGraph );
                    aFreCanLabel.add( sCanLabel );
                    this.hCanLabel2Support.put( sCanLabel, dNoisySupport );
                    this.hLabel2RealSupport.put( sCanLabel, (double)nSupport );
                }
            }

            System.out.println("Candidate sub 2 graph: " +
                    hCanLabel2Graph2.size() + "|Frequent sub 2 graph: "
                    + aFreTwoGraph.size());
            System.out.println( "Sensitivity : " + nSensitivity2 +
                    "|" + "Avg noise scoope : " + (nSensitivity2/this.ee) );
            //iejr: for debug
            //printAllFreSubGraph();
            //

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeFrequentGraph();

        return aFreTwoGraph;
    }


    protected ArrayList<GraphSet> countSupport( ArrayList<GraphSet> sCandidate ){
        HashMap<String,Integer> hCanLabel2Support = new HashMap<String,Integer>();
        HashMap<String,GraphSet> hCanLabel2Graph = new HashMap<String,GraphSet>();

        //iejr: get intersection tid list
        System.out.println( "Getting intersection tid list..." );
        ArrayList<GraphSet> aPrunedCandidate = new ArrayList<GraphSet>();

        for( GraphSet gNegativeCandidate : sCandidate ){
            HashSet<Integer> hUpdateTid = null;
            for( String sSubCanLabel : gNegativeCandidate.aSubCanLabel ){
                GraphSet gSubCandidate = this.hCanLabel2Graph.get( sSubCanLabel );
                HashSet<Integer> hSubTidList = gSubCandidate.hTidList;
                if( hSubTidList == null ){
                    continue;
                }
                if( hUpdateTid == null ){
                    hUpdateTid = new HashSet<Integer> (hSubTidList);
                }else{
                    hUpdateTid.retainAll( hSubTidList );
                }
            }

            if( hUpdateTid.size() >= this.dMinsup ){
                gNegativeCandidate.hTidList = hUpdateTid;
                aPrunedCandidate.add( gNegativeCandidate );
            }
        }

        System.out.println( "Start counting support in database..." );
        ArrayList<GraphSet> aFreSubGraph = new ArrayList<GraphSet>();

        try {
            BufferedReader r = new BufferedReader(new FileReader( this.sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            while( (sLine = r.readLine()) != null ){
                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split( " " );

                LabelGraphList lGraph = new LabelGraphList( sVertexLabel.length );
                for( int i = 0;i < sVertexLabel.length;i++ ){
                    lGraph.setVertexLabel( i, sVertexLabel[i] );
                }

                while( !(sLine = r.readLine()).contains( sEndPattern ) ){
                    String[] sEdgeInfo = sLine.trim().split( " " );
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel  = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                    //    lGraph.setGraphEdge( nExecuteColumn, nExecuteLine, dEdgeLabel );
                }

                //    lGraph.print();
                //    System.out.println();

                for( GraphSet gCandidate : aPrunedCandidate ){
                    LabelGraphList lCandidate = gCandidate.lGraph;
                    String sCanLabel = gCandidate.sCanLabel;

                    if( !gCandidate.hTidList.contains( nTid ) ){
                        continue;
                    }

                    if( lCandidate.judgeIsomorphismPrun( lGraph ) != null ){
                        if( hCanLabel2Support.containsKey( sCanLabel ) ){
                            int nSupport = hCanLabel2Support.get( sCanLabel ) + 1;
                            hCanLabel2Support.put( sCanLabel, nSupport );
                        }else{
                            hCanLabel2Support.put( sCanLabel, 1 );
                        }
                    }else{
                        gCandidate.hTidList.remove(new Integer(nTid));
                    }

                    if( !hCanLabel2Graph.containsKey( sCanLabel ) ){
                        hCanLabel2Graph.put( sCanLabel, gCandidate );
                    }
                    //    ArrayList<Integer> aTidList =
                    //        hCanLabel2Graph.get( sCanLabel ).aTidList;
                    //    if( aTidList == null ) {
                    //        aTidList = new ArrayList<Integer>();
                    //    }
                    //    aTidList.add( nTid );
                    //    hCanLabel2Graph.get( sCanLabel ).aTidList = aTidList;
                }

                nTid++;
                if( nTid % 1000 == 0 ) {
                    System.out.println("Tid:" + nTid);
                }
                //    break;
                //    }
            }
            r.close();

            int nSensitivity = hCanLabel2Support.size();
            System.out.println( "Sensitivity : " + nSensitivity + "|"
                    + "Avg noise scoope : " + (nSensitivity/this.ee) );
            //iejr: we prune the one is not frequent
            for( String sCanLabel : hCanLabel2Support.keySet() ){
                int nSupport = hCanLabel2Support.get( sCanLabel );

                double dNoisy = Distribution.laplace( this.ee, nSensitivity );
                double dNoisySupport = nSupport + dNoisy;

                System.out.println( "Candidate: " + sCanLabel + "| Support: " + nSupport );
                if( dNoisySupport > this.dMinsup ){
                    GraphSet gFreGraph = hCanLabel2Graph.get( sCanLabel );
                    this.hFrequentSubgraphSet.add( gFreGraph.lGraph );
                    this.hCanLabel2Graph.put( sCanLabel, gFreGraph );
                    aFreCanLabel.add( gFreGraph.sCanLabel );
                    aFreSubGraph.add(gFreGraph);
                    this.hCanLabel2Support.put( sCanLabel, dNoisySupport );
                    this.hLabel2RealSupport.put( sCanLabel, (double)nSupport );
                }
            }


            //iejr: for debug
        //    printAllFreSubGraph();
            //

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return aFreSubGraph;
    }


    public void ButtonUpPhase( ArrayList<GraphSet> aFreSubTwoGraph ){
        ArrayList<GraphSet> aFreSubGraphK = aFreSubTwoGraph;
        int fi = 3;
        while( true ){
            System.out.println("Mining frequent " + fi + " sub graphing...");
            ArrayList<GraphSet> aLastFreSubGraphK = aFreSubGraphK;
            ArrayList<GraphSet> aCandidateK = genCandidate( aFreSubGraphK );
            if( aCandidateK == null || aCandidateK.size() == 0 ){
                break;
            }
            System.out.println( "Candidate " + fi + " size: " + aCandidateK.size() );

            aFreSubGraphK = countSupport( aCandidateK );
            System.out.println( "Frequent  " + fi + " size: " + aFreSubGraphK.size() );
            fi++;
            //iejr: for debug
            //printAllFreSubGraph();
            //
            this.writeFrequentGraph();

            for( GraphSet gGraph : aLastFreSubGraphK ){
                gGraph.hTidList.clear();
            }
            aLastFreSubGraphK = null;

            if( fi > this.nMaxFreGraphRank ){
                break;
            }
        }
    }

    protected int getSubOneGraphSensitivity( int nVertexType ){
        double dTemp = nVertexType + 1;
        double dResult = dTemp*(dTemp-1);
        dResult /= 2;
        return (int)dResult;
    }

    protected int getSubTwoGraphSensitivity( ArrayList<String> aSubOneGraphLabel ){

        HashSet<String> hSubTwoGraphLabel = new HashSet<String>();

        for( int i = 0;i < aSubOneGraphLabel.size();i++ ){
            for( int j = i;j < aSubOneGraphLabel.size();j++ ){

                String sCanLabel1 = aSubOneGraphLabel.get( i ).substring( 0,2 );
                String sCanLabel2 = aSubOneGraphLabel.get( j ).substring( 0,2 );

                if( sCanLabel1.charAt( 0 ) == sCanLabel2.charAt( 0 ) ){
                    char cCommon = sCanLabel1.charAt( 0 );
                    String sNewLabel = String.valueOf( cCommon );

                    String sOther1 = sCanLabel1.substring( 1, 2 );
                    String sOther2 = sCanLabel1.substring( 1, 2 );
                    if( sOther1.hashCode() > sOther2.hashCode() ){
                        sNewLabel += sOther1 + sOther2;
                    } else {
                        sNewLabel += sOther2 + sOther1;
                    }

                    hSubTwoGraphLabel.add( sNewLabel );
                    continue;
                }

                if( sCanLabel1.charAt( 0 ) == sCanLabel2.charAt( 1 ) ){
                    char cCommon = sCanLabel1.charAt( 0 );
                    String sNewLabel = String.valueOf( cCommon );

                    String sOther1 = sCanLabel1.substring( 1, 2 );
                    String sOther2 = sCanLabel1.substring( 0, 1 );
                    if( sOther1.hashCode() > sOther2.hashCode() ){
                        sNewLabel += sOther1 + sOther2;
                    } else {
                        sNewLabel += sOther2 + sOther1;
                    }

                    hSubTwoGraphLabel.add( sNewLabel );
                    continue;
                }

                if( sCanLabel1.charAt( 1 ) == sCanLabel2.charAt( 0 ) ){
                    char cCommon = sCanLabel1.charAt( 1 );
                    String sNewLabel = String.valueOf( cCommon );

                    String sOther1 = sCanLabel1.substring( 0, 1 );
                    String sOther2 = sCanLabel1.substring( 1, 2 );
                    if( sOther1.hashCode() > sOther2.hashCode() ){
                        sNewLabel += sOther1 + sOther2;
                    } else {
                        sNewLabel += sOther2 + sOther1;
                    }

                    hSubTwoGraphLabel.add( sNewLabel );
                    continue;
                }

                if( sCanLabel1.charAt( 1 ) == sCanLabel2.charAt( 1 ) ){
                    char cCommon = sCanLabel1.charAt( 1 );
                    String sNewLabel = String.valueOf( cCommon );

                    String sOther1 = sCanLabel1.substring( 0, 1 );
                    String sOther2 = sCanLabel1.substring( 0, 1 );
                    if( sOther1.hashCode() > sOther2.hashCode() ){
                        sNewLabel += sOther1 + sOther2;
                    } else {
                        sNewLabel += sOther2 + sOther1;
                    }

                    hSubTwoGraphLabel.add( sNewLabel );
                    continue;
                }
            }
        }

        return hSubTwoGraphLabel.size();
    }

    protected void writeFrequentGraph(){

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
            String sCanLabel = this.aFreCanLabel.get( i );
            double dSupport = this.hCanLabel2Support.get( sCanLabel );
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

    //iejr: no use
    public static void testTopKMining( String sDatasetName, int k, double e1 ){
        //    String sDatasetName = sDataset;
        String sThreshold   = "One";
        double dThreshold   = DatasetParameter.getCorrespondThreshold( sDatasetName, k );
        String sTime = Parameter.getTime();
        String sDatasetPath = DatasetParameter.getDatasetPath(sDatasetName);
        double dSupport = DatasetParameter.getAbusoluteThreshold(sDatasetName, dThreshold);

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }

        int nMaxFreGraphRank = DatasetParameter.getMaxGraphRankByTopk(sDatasetName, k);

        String sOutPutPath = DatasetParameter.sDataResultPrefix +
                sDatasetName + "/Naive/top/" + "Result_top" + k + "_"
                + e1 + "_" + sTime + ".txt";
        String sOutPutDetailPath = DatasetParameter.sDataResultPrefix
                + sDatasetName + "/Naive/detail/";
        sOutPutDetailPath = null;

        long lStart = System.currentTimeMillis();

        Naive myNaive = new Naive();
        myNaive.setParameter( sDatasetPath, dSupport, sOutPutPath,
                sOutPutDetailPath, e1, nMaxFreGraphRank );
        System.out.println( myNaive.dMinsup );

        int nVertexType = DatasetParameter.getVertexType( sDatasetName );

        ArrayList<GraphSet> aFreTwoGraph = myNaive.InitialPhase( nVertexType );
        myNaive.ButtonUpPhase( aFreTwoGraph );

        long lEnd = System.currentTimeMillis();
        myNaive.writeTime( lEnd - lStart, sOutPutPath );

        int nFreSubGraphNum = myNaive.hFrequentSubgraphSet.size();
        System.out.println( "Total frequent sub graph number is: " + nFreSubGraphNum );

        String sStand = DatasetParameter.sDataResultPrefix + sDatasetName
                + DatasetParameter.getContrastFile( sDatasetName );
        Contrast.ContrastSmart(sOutPutPath, sStand, k);
    }


    public static void testThresholdMining( String sDatasetName,
                                            double dThreshold, int nDatasize,
                                            int nMaxFreGraphRank, double e1,
                                            int nVertexType ){

        // double dThreshold   = DatasetParameter.getCorrespondThresholdByRank(
        //    sDatasetName, nThresholdRank );
        String sTime = Parameter.getTime();
        String sDatasetPath = "./Dataset/" + sDatasetName + ".dat";
        double dSupport = dThreshold * nDatasize;

        if (sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }

        String sOutPutPath = "./out/" + sDatasetName + "/Naive/threshold/"
                + "Result_th" + dThreshold + "_"  + e1 + "_" + sTime + ".txt";
        String sOutPutDetailPath = "./out/" + sDatasetName + sDatasetName
                + "/Naive/detail/";
        sOutPutDetailPath = null;

        long lStart = System.currentTimeMillis();

        Naive myNaive = new Naive();
        myNaive.setParameter( sDatasetPath, dSupport, sOutPutPath,
                sOutPutDetailPath, e1, nMaxFreGraphRank );
        System.out.println( myNaive.dMinsup );

    //    int nVertexType = DatasetParameter.getVertexType( sDatasetName );

        ArrayList<GraphSet> aFreTwoGraph = myNaive.InitialPhase( nVertexType );
        myNaive.ButtonUpPhase( aFreTwoGraph );

        long lEnd = System.currentTimeMillis();
        myNaive.writeTime( lEnd - lStart, sOutPutPath );

        int nFreSubGraphNum = myNaive.hFrequentSubgraphSet.size();
        System.out.println( "Total frequent sub graph number is: " + nFreSubGraphNum );

        String sStand = "./out/" + sDatasetName + "/FSG/" + "Result_" +
                dThreshold + "_" + "Standard" + ".txt";
        Contrast.ContrastSmart(sOutPutPath, sStand, -1);
    }

}
