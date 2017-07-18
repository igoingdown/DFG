package DiffFPM;

import Common.Contrast;
import Common.Distribution;
import Common.Parameter;
import Dataset.DatasetParameter;
import FSG.GraphSet;
import FSG.LabelGraphList;


import java.io.*;
import java.util.*;

/**
 * Created by iejr on 2015/6/23.
 * version 1.0, stable version
 * todo: an acceptable program for diffFPM afford to top 100 without optimization
 */
public class DiffFPMv1 extends FSG.FSG {
    private double e1,e2;
    private double ita1,ita2;
    private Random rSeed;
    private int nConThrehsold;
    HashSet<String> hVerLabel;
        //iejr: all vertexs
    HashMap<String,GraphSet> hCanLabel2SubOneGraph;
        //iejr: all edges
    public HashSet<String> hFilterLabel;
        //iejr: the graph which has been finded
    public HashMap<String,GraphSet> hCanLabel2FreGraph;
        //iejr: all frequent graphs and their canonical labels

    public static void main( String[] args ){

        String sDataset = "CAN-single";
        int k = 50;
        double e1 = 0.2;
        double e2 = 0.2;
        DiffFPMv1.testTopKMining( sDataset, k, e1, e2 );
    }

    public DiffFPMv1(){
        this.hVerLabel = new HashSet<String>();
        this.hCanLabel2SubOneGraph = new HashMap<String,GraphSet>();
        this.hFilterLabel = new HashSet<String>();
        this.hCanLabel2FreGraph = new HashMap<String,GraphSet>();
    }

    public void setParameter(
            String sDatasetPath,
            double nMinsup, double e1, double e2, double ita1, double ita2,
            int nRandomSeed, int nConThrehsold
    ){

        super.setParameter(sDatasetPath, nMinsup, null ,null);
        this.e1 = e1;
        this.e2 = e2;
        this.ita1 = ita1;
        this.ita2 = ita2;

        if( nRandomSeed == -1 ){
            this.rSeed = new Random( System.currentTimeMillis() );
        }else{
            this.rSeed = new Random( nRandomSeed );
        }

        this.nConThrehsold = nConThrehsold;
    }


    public void preProcessing(){

        System.out.println( "Preprocessing..." );

        if( this.hVerLabel == null ){
            hVerLabel = new HashSet<String>();
        }
        if( this.hCanLabel2SubOneGraph == null ){
            this.hCanLabel2SubOneGraph = new HashMap<String,GraphSet>();
        }
        if( this.hFilterLabel == null ){
            this.hFilterLabel = new HashSet<String>();
        }

        try{
            BufferedReader r = new BufferedReader(new FileReader( this.sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            int nVertexUniqueID = 0;

            //    int nLineID = 0;

            while( (sLine = r.readLine()) != null ) {

                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");
                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                    lGraph.setVerUniID(i, nVertexUniqueID++);
                    hVerLabel.add( sVertexLabel[i] );
                }

                while ((sLine = r.readLine()) != null) {
                    //    nLineID++;
                    if (sLine.contains(sEndPattern)) {
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                HashSet<GraphSet> hCanSubOneGraph = super.getOneSubGraph(lGraph, nTid);

                for( GraphSet gGraph : hCanSubOneGraph ){
                    if( !hCanLabel2SubOneGraph.containsKey( gGraph.sCanLabel ) ) {
                        hCanLabel2SubOneGraph.put(gGraph.sCanLabel, gGraph);
                    }else{
                        HashSet<Integer> aUpdateList = gGraph.hTidList;
                        hCanLabel2SubOneGraph.get( gGraph.sCanLabel ).hTidList.addAll(
                                aUpdateList );
                    }
                }

                nTid++;
            }
            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println( "Preprocessing phase done~" );
    }

    public void InitFilter( String sFilterFile ){
        System.out.println( "Initialize filter..." );

        try{
            BufferedReader r = new BufferedReader(new FileReader( sFilterFile ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            //    int nVertexUniqueID = 0;

            while( (sLine = r.readLine()) != null ) {

                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");
                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                    //    lGraph.setVerUniID(i, nVertexUniqueID++);
                    hVerLabel.add( sVertexLabel[i] );
                }

                while ((sLine = r.readLine()) != null) {
                    //    nLineID++;
                    if (sLine.contains(sEndPattern)) {
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                GraphSet gGraph = new GraphSet( lGraph );
                String sCanLabel = gGraph.getCanLabel();
                this.hFilterLabel.add( sCanLabel );
                this.hCanLabel2FreGraph.put( sCanLabel, gGraph );

            }
            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println( "Filter initializing phase done~" );;
    }

    public void EEN( GraphSet gGraphPattern, HashSet<GraphSet> hPositive,
                     HashSet<GraphSet> hNegtive, HashSet<GraphSet> hHalfPositive ){
        System.out.println( "Start EEN phase..." );

        //    System.out.println( "Current graph is: " + gGraphPattern.sCanLabel );

        if( hPositive == null ){
            hPositive = new HashSet<GraphSet>();
        }
        if( hNegtive == null ){
            hNegtive = new HashSet<GraphSet>();
        }

        HashSet<String> hOccursInFilters = new HashSet<String>();

        LabelGraphList lTestGraph = gGraphPattern.lGraph;
        HashMap<Integer,HashSet<int[]>> hTid2VerMap = countPattern( gGraphPattern );

        HashSet<GraphSet> hSubNeighbors = lTestGraph.getSubNeighbors(
                this.hFilterLabel, hOccursInFilters );
        //   System.out.println("The sub graph set has been populated : "
        //   + hSubNeighbors.size());

        HashMap<GraphSet,LabelEdge> hSuperBackNeighbors =
                lTestGraph.getSuperBackNeighbors( this.hCanLabel2SubOneGraph,
                        this.hFilterLabel, hOccursInFilters );
        //    System.out.println( "The super back set has been populated : "
        //    + hSuperBackNeighbors.size() );

        HashMap<GraphSet, LabelEdge> hSuperForwardNeighbors =
                lTestGraph.getSuperForwardNeighbors( this.hVerLabel,
                        this.hCanLabel2SubOneGraph, this.hFilterLabel,
                        hOccursInFilters );
        //    System.out.println( "The super forward set has been populated : "
        //     + hSuperForwardNeighbors.size() );

        System.out.println( "Start counting candidate..." );

        //iejr: Explore sub neighbors
        if( gGraphPattern.hTidList.size() >= this.dMinsup ){
            hPositive.addAll( hSubNeighbors );
        }else{
            countSubPattern(gGraphPattern, hSubNeighbors, hPositive, hNegtive);
        }

        //iejr: Explore super back neighbors
        if( gGraphPattern.hTidList.size() < this.dMinsup ){
            for( GraphSet gGraph : hSuperBackNeighbors.keySet() ){
                hNegtive.add( gGraph );
            }
            //    hNegtive.addAll( hSuperBackNeighbors.keySet() );
        }else{
            countSuperBackPattern( gGraphPattern, hSuperBackNeighbors,
                    hTid2VerMap, hPositive, hNegtive );
        }

        //iejr: Explore super forward neighbors
        if( gGraphPattern.hTidList.size() < this.dMinsup ){
            for( GraphSet gGraph : hSuperForwardNeighbors.keySet() ){
                hNegtive.add( gGraph );
            }
            //    hNegtive.addAll( hSuperBackNeighbors.keySet() );
        }else{
            countSuperForwardPattern(gGraphPattern, hSuperForwardNeighbors,
                    hTid2VerMap, hPositive, hNegtive);
        }

        //iejr: Explore one on the condition of Pattern Removal
        HashSet<String> hCandidateCanLabel = new HashSet<String>();
        for( GraphSet gGraph : hPositive ){
            hCandidateCanLabel.add( gGraph.sCanLabel );
        }
        for( GraphSet gGraph : hNegtive ){
            hCandidateCanLabel.add( gGraph.sCanLabel );
        }

        Queue<String> qSearch = new LinkedList<String>();
        for( String sCanLabel : hOccursInFilters ){
            qSearch.offer( sCanLabel );
        }
        //    hOccursInFilters.clear();

        System.out.println( "Start finding new neighbors..." );

        HashSet<String> hOccuredInNewSet = new HashSet<String>();
        HashSet<GraphSet> hNewCandidate  = new HashSet<GraphSet>();
        while( qSearch.size() > 0 ){
            String sCanLabel = qSearch.remove();

            //iejr: for debug
            System.out.println( "conflict!" );
            //

            GraphSet gCurrentGraph = this.hCanLabel2FreGraph.get( sCanLabel );
            lTestGraph = gCurrentGraph.lGraph;

            hSubNeighbors = lTestGraph.getSubNeighbors( null,null );
            //    System.out.print( "The sub graph set has been populated : "
            //    + hSubNeighbors.size() );

            hSuperBackNeighbors = lTestGraph.getSuperBackNeighbors(
                    this.hCanLabel2SubOneGraph, null,null );
            //    System.out.println( "The super back set has been populated : "
            //    + hSuperBackNeighbors.size() );

            hSuperForwardNeighbors = lTestGraph.getSuperForwardNeighbors(
                    this.hVerLabel, this.hCanLabel2SubOneGraph, null,null );
            //    System.out.println( "The super forward set has been populated : "
            //    + hSuperForwardNeighbors.size() );

            HashSet<GraphSet> hNewGraph = hSubNeighbors;
            for( GraphSet gGraph : hSuperBackNeighbors.keySet() ){
                hNewGraph.add( gGraph );
            }
            for( GraphSet gGraph : hSuperForwardNeighbors.keySet()){
                hNewGraph.add( gGraph );
            }

            for( GraphSet gGraph : hNewGraph ){
                String sLabel = gGraph.sCanLabel;
                if( hCandidateCanLabel.contains( sLabel ) ){
                    continue;
                }
                if( hOccursInFilters.contains( sLabel ) ){
                    continue;
                }
                if( gGraphPattern.sCanLabel.equals( sLabel ) ){
                    continue;
                }
                if( this.hFilterLabel.contains( sLabel ) ){
                    qSearch.offer( sLabel );
                    hOccursInFilters.add( sLabel );
                    continue;
                }
                if( !hOccuredInNewSet.contains( sLabel ) ){
                    hOccuredInNewSet.add( sLabel );
                    hNewCandidate.add( gGraph );
                }
            }
        }

        System.out.println( "Start counting new neighbors..." );

        countPattern(hNewCandidate, hHalfPositive, hNegtive);


        //iejr: for debug
        System.out.println( "    |s|: " + hSubNeighbors.size() +
                "   |B|: " + hSuperBackNeighbors.size() +
                "   |F|: " + hSuperForwardNeighbors.size() );
        //    System.out.println( "Positive sub graph is: " );
        //    for( GraphSet gGraph : hPositive ){
        //        System.out.println( gGraph.sCanLabel );
        //    }
        //    System.out.println( "Negtive sub graph is: " );
        //    for( GraphSet gGraph : hNegtive ){
        //        System.out.println( gGraph.sCanLabel );
        //    }
        //

        //    System.out.println( "EEN phase done~" );
    }

    protected HashMap<Integer,HashSet<int[]>> countPattern( GraphSet X ){
        X.aSubCanLabel = null;
        X.hTidList = new HashSet<Integer>();

        //    System.out.println("count pattern in database...");

        HashMap<Integer,HashSet<int[]>> hTid2VerMap = new HashMap<Integer,HashSet<int[]>>();

        try{
            BufferedReader r = new BufferedReader(new FileReader( this.sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            int nVertexUniqueID = 0;

            //    int nLineID = 0;

            while( (sLine = r.readLine()) != null ) {

                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");
                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                    lGraph.setVerUniID(i, nVertexUniqueID++);
                    hVerLabel.add( sVertexLabel[i] );
                }

                while ((sLine = r.readLine()) != null) {
                    //    nLineID++;
                    if (sLine.contains(sEndPattern)) {
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                HashSet<int[]> hVerMap = X.lGraph.judgeIsomorphismPrunWhole( lGraph );
                if( hVerMap != null && hVerMap.size() > 0 ) {
                    hTid2VerMap.put(nTid, hVerMap);
                    X.hTidList.add( nTid );
                }
                //    HashMap<Integer,Integer> hVerMap = X.lGraph.judgeIsomorphismPrun( lGraph );
                //    if( hVerMap != null ){
                //        System.out.println( "Duang" );
                //    }
                nTid++;
                //iejr: for debug
                //    if( nTid % 100 == 0 ) {
                //        System.out.println("Tid: " + nTid);
                //    }
                //
            }
            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        //    System.out.println( "counting phase done~" );
        return hTid2VerMap;
    }


    protected void countPattern( HashSet<GraphSet> hGraphPattern,
                                 HashSet<GraphSet> hHalfPositive,
                                 HashSet<GraphSet> hNegtive ){
        if( hGraphPattern == null || hGraphPattern.size() == 0 ){
            return;
        }

        //iejr: for each sub graph, find all edges and
        //      count the intersaction of their tid list
        int nNegtiveCount = 0;
        for( GraphSet gGraph : hGraphPattern ){
            //    gGraph.hTidList = new HashSet<Integer>();

            HashSet<String> hGraphCanLabel = gGraph.lGraph.getAllEdge();
            HashSet<Integer> hUpdateTid = null;
            for( String sCanLabel : hGraphCanLabel ){
                if( !this.hCanLabel2SubOneGraph.containsKey( sCanLabel) ){
                    //    System.out.println( "Error! no edge matched in
                    //         the global domain!" + sCanLabel );
                    //    System.exit( 0 );
                    hUpdateTid = new HashSet<Integer>();
                    break;
                }
                GraphSet gEdge = this.hCanLabel2SubOneGraph.get( sCanLabel );
                if( hUpdateTid == null ){
                    hUpdateTid = new HashSet<Integer>( gEdge.hTidList );
                }else{
                    hUpdateTid.retainAll( gEdge.hTidList );
                }
            }
            //    gGraph.hTidList = hUpdateTid;

            //    System.out.println( "Tidlist: " + hUpdateTid.size() );
            if( hUpdateTid.size() < this.dMinsup ){
                nNegtiveCount++;
                hNegtive.add( gGraph );
            }else{
                hHalfPositive.add( gGraph );
            }
        }
        //    System.out.println( "|C|: " + hGraphPattern.size() + "  |CN|: "
        //    + nNegtiveCount );


        //   System.out.println( "classify sub patterns in database..." );
/*
        HashMap<GraphSet,Integer> hGraph2Support = new HashMap<GraphSet,Integer>();
        try{
            BufferedReader r = new BufferedReader(new FileReader( this.sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            int nVertexUniqueID = 0;

            //    int nLineID = 0;

            while( (sLine = r.readLine()) != null ) {

                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");
                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                    lGraph.setVerUniID(i, nVertexUniqueID++);
                    hVerLabel.add( sVertexLabel[i] );
                }


                while ((sLine = r.readLine()) != null) {
                    //    nLineID++;
                    if (sLine.contains(sEndPattern)) {
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                int nSupportCount = 0;
                for( GraphSet gCanGraph : hGraphPattern ){
                    if( !gCanGraph.hTidList.contains( nTid ) ){
                        continue;
                    }

                    if( gCanGraph.hTidList.size() < this.dMinsup ){
                //        System.out.println( "Jump!" );
                        continue;
                    }

                    if( hGraph2Support.containsKey( gCanGraph ) &&
                    hGraph2Support.get( gCanGraph ) > this.dMinsup ){
                        continue;
                    }

                    if( gCanGraph.lGraph.judgeIsomorphismPrun(lGraph) != null ){
                        nSupportCount++;
                        if( hGraph2Support.containsKey( gCanGraph ) ){
                            int nSupport = hGraph2Support.get( gCanGraph ) + 1;
                            hGraph2Support.put( gCanGraph, nSupport );
                        }else {
                            hGraph2Support.put(gCanGraph, 1);
                        }
                    }

                }
                nTid++;

                //iejr: for debug
            //    System.out.println( "Count: " + nSupportCount );
                if( nTid % 100 == 0 ){
                    System.out.println( "Tid: " + nTid );
                }
                //

            }
            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        //iejr: classify
        for( GraphSet gGraph : hGraph2Support.keySet() ){
            if( hGraph2Support.get( gGraph ) > this.dMinsup ){
                hPositive.add( gGraph );
                //    System.out.println( hGraph2Support.get( gGraph ) );
            }else{
                hNegtive.add( gGraph );
                //    System.out.println( hGraph2Support.get( gGraph ) );
            }
        }
        */
        //    System.out.println( "counting phase done~" );
    }

    protected void countSubPattern( GraphSet X, HashSet<GraphSet> hGraphPattern,
                                    HashSet<GraphSet> hPositive,
                                    HashSet<GraphSet> hNegtive ){
        if( hGraphPattern == null || hGraphPattern.size() == 0 ){
            return;
        }

        //iejr: for each sub graph, find all edges and
        //      count the intersaction of their tid list
        for( GraphSet gGraph : hGraphPattern ){
            //    gGraph.hTidList = new HashSet<Integer>();

            HashSet<String> hGraphCanLabel = gGraph.lGraph.getAllEdge();
            HashSet<Integer> hUpdateTid = null;
            for( String sCanLabel : hGraphCanLabel ){
                if( !this.hCanLabel2SubOneGraph.containsKey( sCanLabel) ){
                    //    System.out.println( "Error! no edge matched in the
                    //        global domain!" + sCanLabel );
                    //    System.exit( 0 );
                    hUpdateTid = new HashSet<Integer>();
                    break;
                }
                GraphSet gEdge = this.hCanLabel2SubOneGraph.get( sCanLabel );
                if( hUpdateTid == null ){
                    hUpdateTid = new HashSet<Integer>( gEdge.hTidList );
                }else{
                    hUpdateTid.retainAll( gEdge.hTidList );
                }
            }
            gGraph.hTidList = hUpdateTid;
        }

        System.out.println( "classify sub patterns in database..." );

        HashMap<GraphSet,Integer> hGraph2Support = new HashMap<GraphSet,Integer>();
        try{
            BufferedReader r = new BufferedReader(new FileReader( this.sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            int nVertexUniqueID = 0;

            //    int nLineID = 0;

            while( (sLine = r.readLine()) != null ) {

                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");
                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                    lGraph.setVerUniID(i, nVertexUniqueID++);
                    hVerLabel.add( sVertexLabel[i] );
                }

                while ((sLine = r.readLine()) != null) {
                    //    nLineID++;
                    if (sLine.contains(sEndPattern)) {
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                for( GraphSet gCanGraph : hGraphPattern ){
                    if( !gCanGraph.hTidList.contains( nTid ) ){
                        continue;
                    }

                    if( hGraph2Support.containsKey( gCanGraph ) &&
                            hGraph2Support.get( gCanGraph ) > this.dMinsup ){
                        continue;
                    }

                    if( X.hTidList.contains( nTid ) ||
                            gCanGraph.lGraph.judgeIsomorphismPrun( lGraph ) != null ){
                        if( hGraph2Support.containsKey( gCanGraph ) ){
                            int nSupport = hGraph2Support.get( gCanGraph ) + 1;
                            hGraph2Support.put( gCanGraph, nSupport );
                        }else {
                            hGraph2Support.put(gCanGraph, 1);
                        }
                    }

                }
                nTid++;

                //    if( nTid % 100 == 0 ){
                //        System.out.println( "Tid : " + nTid );
                //    }
            }
            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        //iejr: classify
        for( GraphSet gGraph : hGraph2Support.keySet() ){
            if( hGraph2Support.get( gGraph ) > this.dMinsup ){
                hPositive.add( gGraph );
                //    System.out.println( hGraph2Support.get( gGraph ) );
            }else{
                hNegtive.add( gGraph );
                //    System.out.println( hGraph2Support.get( gGraph ) );
            }
        }

        //    System.out.println( "counting phase done~" );
    }


    protected void countSuperBackPattern(
            GraphSet X, HashMap<GraphSet,LabelEdge> hSuperBackGraphPattern,
            HashMap<Integer,HashSet<int[]>> hTid2Map,
            HashSet<GraphSet> hPositive, HashSet<GraphSet> hNegtive
    ) {
        if (hSuperBackGraphPattern == null) {
            return;
        }

        //   System.out.println( "classify super back patterns in database..." );

        HashMap<GraphSet,Integer> hGraph2Support = new HashMap<GraphSet,Integer>();
        try{
            BufferedReader r = new BufferedReader(new FileReader( this.sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            int nVertexUniqueID = 0;

            //    int nLineID = 0;

            while( (sLine = r.readLine()) != null ) {

                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");
                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                    lGraph.setVerUniID(i, nVertexUniqueID++);
                    hVerLabel.add( sVertexLabel[i] );
                }

                while ((sLine = r.readLine()) != null) {
                    //    nLineID++;
                    if (sLine.contains(sEndPattern)) {
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                for( GraphSet gCanGraph : hSuperBackGraphPattern.keySet() ){
                    if( !X.hTidList.contains( nTid ) ){
                        continue;
                    }

                    if( hGraph2Support.containsKey( gCanGraph ) &&
                            hGraph2Support.get( gCanGraph ) > this.dMinsup ){
                        continue;
                    }

                    HashSet<int[]> hVer2VerMap = hTid2Map.get( nTid );
                    LabelEdge lNewEdge = hSuperBackGraphPattern.get( gCanGraph );
                    for( int[] nVer2VerMap : hVer2VerMap ){
                        int nSourMappedVerID = nVer2VerMap[lNewEdge.aVertex.get(0).nVertexID];
                        int nDestMappedVerID = nVer2VerMap[lNewEdge.aVertex.get(1).nVertexID];
                        double dEdgeLabel = lNewEdge.dEdgeLabel;

                        if( lGraph.judgeEdge(nSourMappedVerID, nDestMappedVerID) == dEdgeLabel ){
                            if( hGraph2Support.containsKey( gCanGraph ) ){
                                int nSupport = hGraph2Support.get( gCanGraph ) + 1;
                                hGraph2Support.put( gCanGraph, nSupport );
                            }else {
                                hGraph2Support.put(gCanGraph, 1);
                            }
                        }
                    }

                }
                nTid++;
            }
            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        //iejr: classify
        for( GraphSet gGraph : hGraph2Support.keySet() ){
            if( hGraph2Support.get( gGraph ) > this.dMinsup ){
                hPositive.add( gGraph );
                //    System.out.println( hGraph2Support.get( gGraph ) );
            }else{
                hNegtive.add( gGraph );
                //    System.out.println( hGraph2Support.get( gGraph ) );
            }
        }

        //    System.out.println( "counting phase done~" );
    }

    protected void countSuperForwardPattern(
            GraphSet X, HashMap<GraphSet,LabelEdge> hSuperBackGraphPattern,
            HashMap<Integer,HashSet<int[]>> hTid2Map,
            HashSet<GraphSet> hPositive, HashSet<GraphSet> hNegtive
    ) {
        if (hSuperBackGraphPattern == null) {
            return;
        }

        //    System.out.println( "classify super forward patterns in database..." );

        HashMap<GraphSet,Integer> hGraph2Support = new HashMap<GraphSet,Integer>();
        try{
            BufferedReader r = new BufferedReader(new FileReader( this.sDatasetPath ));

            String sLine = null;
            String sEndPattern = "EOF";
            int nTid = 0;
            int nVertexUniqueID = 0;

            //    int nLineID = 0;

            while( (sLine = r.readLine()) != null ) {

                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");
                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                    lGraph.setVerUniID(i, nVertexUniqueID++);
                    hVerLabel.add( sVertexLabel[i] );
                }

                while ((sLine = r.readLine()) != null) {
                    //    nLineID++;
                    if (sLine.contains(sEndPattern)) {
                        break;
                    }
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                for( GraphSet gCanGraph : hSuperBackGraphPattern.keySet() ){
                    if( !X.hTidList.contains( nTid ) ){
                        continue;
                    }

                    if( hGraph2Support.containsKey( gCanGraph ) &&
                            hGraph2Support.get( gCanGraph ) > this.dMinsup ){
                        continue;
                    }

                    HashSet<int[]> hVer2VerMap = hTid2Map.get( nTid );
                    LabelEdge lNewEdge = hSuperBackGraphPattern.get( gCanGraph );
                    for( int[] nVer2VerMap : hVer2VerMap ){
                        int nSourMappedVerID = nVer2VerMap[lNewEdge.aVertex.get(0).nVertexID];
                        String sEndLabel = lNewEdge.aVertex.get(1).sVertexLabel;
                        double dEdgeLabel = lNewEdge.dEdgeLabel;
                        HashSet<Integer> hFilter = new HashSet<Integer>();
                        for( int i = 0;i < nVer2VerMap.length;i++ ){
                            hFilter.add( nVer2VerMap[i] );
                        }

                        if( lGraph.judgeEdge(nSourMappedVerID,
                                sEndLabel, hFilter ).contains(dEdgeLabel) ){
                            if( hGraph2Support.containsKey( gCanGraph ) ){
                                int nSupport = hGraph2Support.get( gCanGraph ) + 1;
                                hGraph2Support.put( gCanGraph, nSupport );
                            }else {
                                hGraph2Support.put(gCanGraph, 1);
                            }
                        }
                    }

                }
                nTid++;
            }
            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        //iejr: classify
        for( GraphSet gGraph : hGraph2Support.keySet() ){
            if( hGraph2Support.get( gGraph ) > this.dMinsup ){
                hPositive.add( gGraph );
                //    System.out.println( hGraph2Support.get( gGraph ) );
            }else{
                hNegtive.add( gGraph );
                //    System.out.println( hGraph2Support.get( gGraph ) );
            }
        }

        //    System.out.println( "counting phase done~" );
    }

    public ArrayList<GraphSet> mainDiffFPMv1( int k,
                                              String sRandomSelectPath,
                                              String sOutputPath ){

        if( k <= 0 ){
            return null;
        }

        ArrayList<GraphSet> aResult = new ArrayList<GraphSet>();
        //    HashSet<String> hResultCanLabel = new HashSet<String>();

        HashMap<GraphSet,Double> hTopkGraph = new HashMap<GraphSet,Double>();

        while( aResult.size() < k ){

            GraphSet gCurrentGraph = null;
            String sSelCanLabel = null;
            do {
                LabelGraphList lRandom = selRandomGraph( k, sRandomSelectPath);
                gCurrentGraph = new GraphSet(lRandom);
                sSelCanLabel = gCurrentGraph.getCanLabel();
            }while( hCanLabel2FreGraph.containsKey( sSelCanLabel ) );

            gCurrentGraph.setCanLabel(sSelCanLabel);
            System.out.println( "Random select : " + sSelCanLabel );

            ArrayList<Integer> aState = new ArrayList<Integer>();

            int nStateCnt = 0;
            int nConvergenceStateCnt = 0;
            int nSupportX = 0;
            GraphSet gOrgGraph = null;
            HashSet<GraphSet> hPositiveX = null;
            HashSet<GraphSet> hNegitiveX = null;
            HashSet<GraphSet> hHalfPositiveX = null;
            while( true ){

                if( nStateCnt > 200 ){
                    System.out.println("Iterate too long to complete, auto exit loop...");
                    aResult.add( gCurrentGraph );
                    this.hFilterLabel.add( gCurrentGraph.sCanLabel );
                    this.hCanLabel2FreGraph.put( gCurrentGraph.sCanLabel,gCurrentGraph );
                    double dNoisySupport = getNoisySupport( nSupportX, k );
                    writeFrequentGraph( gCurrentGraph, sOutputPath,
                            dNoisySupport, (double)nSupportX );
                    break;
                }

                if( gOrgGraph != gCurrentGraph ) {
                    hPositiveX = new HashSet<GraphSet>();
                    hNegitiveX = new HashSet<GraphSet>();
                    hHalfPositiveX = new HashSet<GraphSet>();
                    this.EEN(gCurrentGraph, hPositiveX, hNegitiveX, hHalfPositiveX);
                }

                //iejr: for debug
                System.out.println( "Current state: " + gCurrentGraph.sCanLabel +
                        "   |S|: " + gCurrentGraph.hTidList.size() +
                        "   |P|: " + hPositiveX.size() +
                        "   |N|: " + hNegitiveX.size() +
                        "   |H|: " + hHalfPositiveX.size() );
                //

                //iejr: add current state as one node of Markov Chain
                Integer nCurrentNeighborNum = hPositiveX.size() +
                        hNegitiveX.size() + hHalfPositiveX.size();
                aState.add( nCurrentNeighborNum );

                //iejr: get a random neighbor
                double dProQxy = 0;
                double dRandom = rSeed.nextDouble();
                double dSelectPro1 = this.ita1;
                double dSelectPro2 = this.ita2;
                double dSelectPro3 = 1 - this.ita1 - this.ita2;
                if( hPositiveX.size() == 0 ){
                    double dTemp = dSelectPro2 + dSelectPro3;
                    dSelectPro1 = 0;
                    dSelectPro2 /= dTemp;
                    dSelectPro3 /= dTemp;
                }
                if( hHalfPositiveX.size() == 0 ){
                    double dTemp = dSelectPro1 + dSelectPro3;
                    dSelectPro2 = 0;
                    dSelectPro1 /= dTemp;
                    dSelectPro3 /= dTemp;
                }
                if( hNegitiveX.size() == 0 ){
                    double dTemp = dSelectPro1 + dSelectPro2;
                    dSelectPro3 = 0;
                    dSelectPro1 /= dTemp;
                    dSelectPro2 /= dTemp;
                }
                HashSet<GraphSet> hSelSet = null;
                if( dRandom < dSelectPro1 ){
                    hSelSet = hPositiveX;
                    dProQxy = dSelectPro1 / hPositiveX.size();
                    //iejr: for debug
                    System.out.println( "   Select Positive -> " );
                    //
                }else if( dRandom < dSelectPro1 + dSelectPro2 ) {
                    hSelSet = hHalfPositiveX;
                    dProQxy = ( dSelectPro2 ) / hHalfPositiveX.size();
                    //iejr: for debug
                    System.out.println( "   Select Half-Positive -> " );
                    //
                }else{
                    hSelSet = hNegitiveX;
                    dProQxy = (1 - dSelectPro1 - dSelectPro2) / hNegitiveX.size();
                    //iejr: for debug
                    System.out.println( "   Select Negtive -> " );
                    //
                }

                int i = 0;
                int nConstanceC = hSelSet.size();
                GraphSet gNextGraph = null;
                for( GraphSet gGraph : hSelSet ){
                    double dAccpetPro = 1.0/( nConstanceC - i );
                    i++;
                    dRandom = rSeed.nextDouble();
                    if( dRandom <= dAccpetPro ){
                        gNextGraph = gGraph;
                        break;
                    }
                }

                //iejr: calculate pro
                System.out.println( "Next Candidate : " + gNextGraph.sCanLabel );
                HashSet<GraphSet> hPositiveY = new HashSet<GraphSet>();
                HashSet<GraphSet> hNegitiveY = new HashSet<GraphSet>();
                HashSet<GraphSet> hHalfPositiveY = new HashSet<GraphSet>();
                this.EEN( gNextGraph, hPositiveY, hNegitiveY, hHalfPositiveY );

                //iejr: for debug
                System.out.println( "Next state  : " + gNextGraph.sCanLabel +
                        "   |S|: " + gNextGraph.hTidList.size() +
                        "   |P|: " + hPositiveY.size() +
                        "   |N|: " + hNegitiveY.size() +
                        "   |H|: " + hHalfPositiveY.size() );
                //

                double dProQyx = 0;
                nSupportX = gCurrentGraph.hTidList.size();
                int nSupportY = gNextGraph.hTidList.size();

                dSelectPro1 = this.ita1;
                dSelectPro2 = this.ita2;
                dSelectPro3 = 1 - this.ita1 - this.ita2;
                if( hPositiveY.size() == 0 ){
                    double dTemp = dSelectPro2 + dSelectPro3;
                    dSelectPro1 = 0;
                    dSelectPro2 /= dTemp;
                    dSelectPro3 /= dTemp;
                }
                if( hHalfPositiveY.size() == 0 ){
                    double dTemp = dSelectPro1 + dSelectPro3;
                    dSelectPro2 = 0;
                    dSelectPro1 /= dTemp;
                    dSelectPro3 /= dTemp;
                }
                if( hNegitiveY.size() == 0 ){
                    double dTemp = dSelectPro1 + dSelectPro2;
                    dSelectPro3 = 0;
                    dSelectPro1 /= dTemp;
                    dSelectPro2 /= dTemp;
                }
                if( nSupportX >= this.dMinsup ){
                    dProQyx = dSelectPro1 / hPositiveY.size();
                }else{
                    dProQyx = (1 - dSelectPro1 - dSelectPro2) / hNegitiveY.size();
                }

                double dTemp1 =  this.e1*nSupportY / ( 2*k ) ;
                double dTemp2 =  this.e1*nSupportX / ( 2*k ) ;
                double dAcceptPro = Math.exp( dTemp1 - dTemp2 );
                dAcceptPro *= ( dProQyx / dProQxy );
                dAcceptPro = Math.min( dAcceptPro, 1 );

                //iejr: for debug
                System.out.println( "   AcceptPro: " + dAcceptPro );
                //

                dRandom = rSeed.nextDouble();
                gOrgGraph = gCurrentGraph;
                if( dRandom <= dAcceptPro ){
                    gCurrentGraph = gNextGraph;
                }

                nStateCnt++;
                boolean bIsTransform = ( gOrgGraph != gCurrentGraph );
                System.out.println( "Iterat : " + nStateCnt + "    "
                        + gOrgGraph.sCanLabel + "    " + gNextGraph.sCanLabel
                        + "   " + bIsTransform );
                //    System.out.println( "         Support:  "
                //         + nSupportX + "  " + nSupportY );

                if( judgeConvergence( aState ) ){
                    nConvergenceStateCnt++;
                }else{
                    nConvergenceStateCnt = 0;
                }

                if( nConvergenceStateCnt >= this.nConThrehsold ||
                        (gCurrentGraph.lGraph.getRank() > 15 &&
                                hPositiveX.size() == 0 && hHalfPositiveX.size() == 0)
                        ){           //iejr: the chain convergenced
                    aResult.add( gCurrentGraph );
                    this.hFilterLabel.add( gCurrentGraph.sCanLabel );
                    this.hCanLabel2FreGraph.put( gCurrentGraph.sCanLabel,gCurrentGraph );
                    double dNoisySupport = getNoisySupport( nSupportX, k );
                    writeFrequentGraph( gCurrentGraph, sOutputPath,
                            dNoisySupport, (double)nSupportX );
                    break;
                }

                System.out.println();
            }

        }


        System.out.println( "The result is: " );
        for( GraphSet gGraph : aResult ){
            System.out.println( gGraph.sCanLabel + ":" + gGraph.hTidList.size() );
            double dNoisy = 0;
            hTopkGraph.put( gGraph, gGraph.hTidList.size() + dNoisy );
        }

        return aResult;
    }

    protected LabelGraphList selRandomGraph( int k, String sRandomSelectPath ){
        int nMaxSize = k + 1;
        int nOffset  = (int) (50 * Math.log((double)k/2));
        int nRandomGraphIndex = this.rSeed.nextInt( nMaxSize ) + nOffset;
        String sRandomSampleFilePath = sRandomSelectPath + "/"
                + nRandomGraphIndex + ".txt";

        System.out.println( "Random select : " + nRandomGraphIndex );
        LabelGraphList lRandomGraph = null;
        try{
            BufferedReader r = new BufferedReader(new FileReader( sRandomSampleFilePath ));

            String sLine = null;
            sLine = r.readLine();
            sLine = sLine.trim();

            String[] sVertexLabel = sLine.split(" ");
            LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
            for (int i = 0; i < sVertexLabel.length; i++) {
                lGraph.setVertexLabel(i, sVertexLabel[i]);
                //    lGraph.setVerUniID(i, nVertexUniqueID++);
                hVerLabel.add( sVertexLabel[i] );
            }

            while ((sLine = r.readLine()) != null) {

                String[] sEdgeInfo = sLine.trim().split(" ");
                int nExecuteLine = Integer.parseInt(sEdgeInfo[0]);
                int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]);
                double dEdgeLabel = Double.parseDouble(sEdgeInfo[2]);
                lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
            }

            lRandomGraph = lGraph;

            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }



        //iejr: for debug
        int nRandomRank = 4;
        //    lRandomGraph = new LabelGraphList( nRandomRank );
        //    int nVerSize = this.hVerLabel.size();
/*
        for( int i = 0;i < nRandomRank;i++ ){
            int nRandomIndex = this.rSeed.nextInt( nVerSize );
            int nCount = 0;
            for( String sVerLabel : this.hVerLabel ){
                if( nCount == nRandomIndex ){
                    lRandomGraph.setVertexLabel( i, sVerLabel );
                    break;
                }
                nCount++;
            }
        }
        for( int i = 0;i < nRandomRank - 1;i++ ){
            lRandomGraph.setGraphEdge( i, i+1, 1 );
        }

        lRandomGraph.setVertexLabel( 0, "C" );
        lRandomGraph.setVertexLabel( 1, "C" );
        lRandomGraph.setVertexLabel( 2, "F" );
        lRandomGraph.setVertexLabel( 3, "H" );
        lRandomGraph.setGraphEdge( 0,1,1 );
        lRandomGraph.setGraphEdge( 0,3,1 );
        lRandomGraph.setGraphEdge( 1,2,1 );
        */
        return lRandomGraph;
    }


    protected boolean judgeConvergence( ArrayList<Integer> aStateList ){
        if( aStateList.size() < 10 ){
            return false;
        }

        double dFrontPro = 0.1;             //iejr: default value is 0.5
        double dBackPro  = 0.5;             //iejr: default value is 0.5
        int nFrontLength = (int)(aStateList.size() * dFrontPro);
        int nBackLength = (int)(aStateList.size() * dBackPro);

        double dFrontE = 0;
        double dBackE  = 0;
        double dFrontD = 0;
        double dBackD  = 0;

        //iejr: for debug
        for( int i = 0;i < aStateList.size();i++ ){
            System.out.print( aStateList.get( i ) + " " );
        }
        System.out.println();
        //

        for( int i = 0;i < nFrontLength;i++ ){
            dFrontE += aStateList.get( i );
            //iejr: for debug
            System.out.print( aStateList.get( i ) + " " );
            //
        }

        //iejr: for debug
        System.out.println();
        //

        for( int i = aStateList.size()-1;i >= aStateList.size() - nBackLength;i-- ){
            dBackE += aStateList.get( i );
            //iejr: for debug
            System.out.print( aStateList.get( i ) + " " );
            //
        }

        //iejr: for debug
        System.out.println();
        //

        dFrontE /= nFrontLength;
        dBackE /= nBackLength;

        for( int i = 0;i < nFrontLength;i++ ){
            dFrontD += (aStateList.get( i ) - dFrontE)*(aStateList.get( i ) - dFrontE);
        }
        for( int i = aStateList.size() - 1;i > aStateList.size() - nBackLength;i-- ){
            dBackD += (aStateList.get( i ) - dBackE)*(aStateList.get( i ) - dBackE);
        }

        //iejr: for debug
        System.out.println( "E(F): " + dFrontE );
        System.out.println( "E(B): " + dBackE );
        System.out.println( "D(F): " + dFrontD );
        System.out.println( "D(B): " + dBackD);
        //

        double ZScore = dFrontE - dBackE;
        ZScore /= Math.sqrt( dFrontD + dBackD );

        System.out.println( "Z-Score is: " + ZScore );

        if( ZScore >= -1 && ZScore <= 1 ){
            return true;
        }else{
            return false;
        }
    }

    protected double getNoisySupport( double dSupport, int k ){
        double dNoisy = Distribution.laplace(this.e2, k);

        return dSupport + dNoisy;
    }

    protected void writeFrequentGraph( GraphSet gGraph,
                                       String sFilePath, double dNoisySupport,
                                       double dRealSupport ){
        String sCanLabel = gGraph.sCanLabel;

        try {

            FileWriter w = new FileWriter( sFilePath , true);

            w.write( sCanLabel + ":" + dNoisySupport + ":" + dRealSupport + "\r\n" );

            w.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testEEN(){
        DiffFPMv1 myDFPM = this;

        HashSet<GraphSet> hPositive = new HashSet<>();
        HashSet<GraphSet> hNegtive  = new HashSet<>();
        HashSet<GraphSet> hHalfPositive  = new HashSet<>();

        HashSet<String> sVerLabel = new HashSet<String>();

        LabelGraphList lTestGraph = new LabelGraphList( 7 );
        lTestGraph.setVertexLabel( 0, "C" );
        lTestGraph.setVertexLabel( 1, "C" );
        lTestGraph.setVertexLabel( 2, "C" );
        lTestGraph.setVertexLabel( 3, "C");
        lTestGraph.setVertexLabel( 4, "H");
        lTestGraph.setVertexLabel( 5, "N");
        lTestGraph.setVertexLabel( 6, "N");
        lTestGraph.setGraphEdge( 2,3,1 );
        lTestGraph.setGraphEdge( 0,5,1 );
        lTestGraph.setGraphEdge( 0,6,1 );
        lTestGraph.setGraphEdge( 1,5,1 );
        lTestGraph.setGraphEdge( 1,6,1 );
        lTestGraph.setGraphEdge( 3,6,1 );
        lTestGraph.setGraphEdge( 4,6,1 );
        GraphSet gTestGraph = new GraphSet( lTestGraph );
        String sTestCanLabel = gTestGraph.getCanLabel();
        gTestGraph.setCanLabel( sTestCanLabel );
        System.out.println( "Test graph is : " + sTestCanLabel );
        myDFPM.preProcessing();
        System.out.println( "All vertex patterns are: " + myDFPM.hVerLabel.size() );
        System.out.println( "All edge patterns are  : " +
                myDFPM.hCanLabel2SubOneGraph.size() );
/*
        LabelGraphList lTestFilter = new LabelGraphList( 3 );
        lTestFilter.setVertexLabel( 0, "C" );
        lTestFilter.setVertexLabel( 1, "C" );
        lTestFilter.setVertexLabel( 2, "H" );
        lTestFilter.setGraphEdge( 0,1,10 );
        lTestFilter.setGraphEdge( 0,2,10 );
        GraphSet gTestFilter = new GraphSet( lTestFilter );
        String sCanLabel = gTestFilter.getCanLabel();

        myDFPM.hFilterLabel.add( sCanLabel );
        myDFPM.hCanLabel2FreGraph.put( sCanLabel, gTestFilter );
        */
        myDFPM.EEN( gTestGraph, hPositive, hNegtive, hHalfPositive );
    }

    public static void testTopKMining( String sDataset, int k,
                                       double epsilon1, double epsilon2 ){


        DiffFPMv1 myDFPM = new DiffFPMv1();

        String sDatasetName = sDataset;
        String sThreshold   = "One";
        double dThreshold   =  DatasetParameter.getCorrespondThreshold(sDatasetName, k);
        String sTime = Parameter.getTime();
        double dSupport = DatasetParameter.getAbusoluteThreshold( sDatasetName, dThreshold );
        double e1 = epsilon1;
        double e2 = epsilon2;
        double ita1 = 0.8;
        double ita2 = 0.1;
        int nRandomSeed = Parameter.nRandomSeed;
            //iejr: -1 for random and other for pseudo random sequence
        int nConvergenceThreshold = 20;
        String sDatasetPath = DatasetParameter.getDatasetPath( sDatasetName );
        String sRandomStartPath = DatasetParameter.sDataResultPrefix +
                sDatasetName + "/FSG/badsample";
        String sOutputPath = DatasetParameter.sDataResultPrefix +
                sDatasetName + "/DiffFPM/top/" + "Result_top" + k + "_"
                + e1 + "_" + sTime + ".txt";
        String sFilterPath = DatasetParameter.sDataResultPrefix +
                sDatasetName + "/DiffFPM/filter.txt";

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }else{
            System.out.println( "Threshold: " + dSupport );
        }

        myDFPM.setParameter( sDatasetPath, dSupport, e1, e2, ita1, ita2,
                nRandomSeed, nConvergenceThreshold );

        System.out.println( "DFPM top-k based mining..." );

        long nStart = System.currentTimeMillis();

        myDFPM.preProcessing();
        myDFPM.InitFilter(sFilterPath);
        myDFPM.mainDiffFPMv1(k, sRandomStartPath, sOutputPath);

        long nEnd = System.currentTimeMillis();
        System.out.println( "Time consuming : " + (nEnd - nStart) );
        myDFPM.writeTime( nEnd-nStart, sOutputPath );

        String sStand = DatasetParameter.sDataResultPrefix + sDatasetName
                + DatasetParameter.getContrastFile( sDatasetName );
        Contrast.ContrastSmart(sOutputPath, sStand, k);
    }


    public static void testThresholdMining( String sDataset,
                                            int nThresholdRank, double epsilon1,
                                            double epsilon2 ){


        DiffFPMv1 myDFPM = new DiffFPMv1();

        String sDatasetName = sDataset;
        String sThreshold   = "One";
        double dThreshold   =  DatasetParameter.getCorrespondThresholdByRank(
                sDatasetName, nThresholdRank);
        int k = DatasetParameter.getTopK( sDatasetName, nThresholdRank );
        String sTime = Parameter.getTime();
        double dSupport = DatasetParameter.getAbusoluteThreshold(
                sDatasetName, dThreshold );
        double e1 = epsilon1;
        double e2 = epsilon2;
        double ita1 = 0.8;
        double ita2 = 0.1;
        int nRandomSeed = Parameter.nRandomSeed;
            //iejr: -1 for random and other for pseudo random sequence
        int nConvergenceThreshold = 20;
        String sDatasetPath = DatasetParameter.getDatasetPath( sDatasetName );
        String sRandomStartPath = DatasetParameter.sDataResultPrefix
                + sDatasetName + "/FSG/badsample";
        String sOutputPath = DatasetParameter.sDataResultPrefix
                + sDatasetName + "/DiffFPM/threshold/" + "Result_th"
                + dThreshold + "_" + e1 + "_" + sTime + ".txt";
        String sFilterPath = DatasetParameter.sDataResultPrefix
                + sDatasetName + "/DiffFPM/filter.txt";

        if( sDatasetPath == null || k == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }else{
            System.out.println( "Top k : " + k );
        }

        myDFPM.setParameter( sDatasetPath, dSupport, e1, e2, ita1,
                ita2, nRandomSeed, nConvergenceThreshold );

        System.out.println( "DFPM threshold based mining..." );

        long nStart = System.currentTimeMillis();

        myDFPM.preProcessing();
        myDFPM.InitFilter(sFilterPath);
        myDFPM.mainDiffFPMv1(k, sRandomStartPath, sOutputPath);

        long nEnd = System.currentTimeMillis();
        System.out.println( "Time consuming : " + (nEnd - nStart) );
        myDFPM.writeTime( nEnd-nStart, sOutputPath );

        String sStand = DatasetParameter.sDataResultPrefix
                + sDatasetName + DatasetParameter.getContrastFile(
                        sDatasetName, dThreshold );
        Contrast.ContrastSmart(sOutputPath, sStand, k);
    }

    public void testPatternRemoval(){
        DiffFPMv1 myDFPM = this;

        HashSet<GraphSet> hPositive = new HashSet<>();
        HashSet<GraphSet> hNegtive  = new HashSet<>();
        HashSet<GraphSet> hHalfPositive  = new HashSet<>();

        HashSet<String> sVerLabel = new HashSet<String>();

        LabelGraphList lTestGraph = new LabelGraphList( 4 );
        lTestGraph.setVertexLabel( 0, "C" );
        lTestGraph.setVertexLabel( 1, "C" );
        lTestGraph.setVertexLabel( 2, "C" );
        lTestGraph.setVertexLabel( 3, "Cl");
        lTestGraph.setGraphEdge( 0,2,1 );
        lTestGraph.setGraphEdge( 1,2,1 );
        lTestGraph.setGraphEdge( 2,3,1 );
        GraphSet gTestGraph = new GraphSet( lTestGraph );
        String sTestCanLabel = gTestGraph.getCanLabel();
        gTestGraph.setCanLabel( sTestCanLabel );
        System.out.println( "Test graph is : " + sTestCanLabel );
        myDFPM.preProcessing();
        System.out.println( "All vertex patterns are: "
                + myDFPM.hVerLabel.size() );
        System.out.println( "All edge patterns are  : "
                + myDFPM.hCanLabel2SubOneGraph.size() );

        LabelGraphList lTestFilter = new LabelGraphList( 3 );
        lTestFilter.setVertexLabel( 0, "C" );
        lTestFilter.setVertexLabel( 1, "C" );
        lTestFilter.setVertexLabel( 2, "H" );
        lTestFilter.setGraphEdge( 0,1,1 );
        lTestFilter.setGraphEdge( 1,2,1 );
        GraphSet gTestFilter = new GraphSet( lTestFilter );
        String sCanLabel = gTestFilter.getCanLabel();

        myDFPM.hFilterLabel.add( sCanLabel );
        myDFPM.hCanLabel2FreGraph.put( sCanLabel, gTestFilter );

        lTestFilter = new LabelGraphList( 3 );
        lTestFilter.setVertexLabel( 0, "C" );
        lTestFilter.setVertexLabel( 1, "C" );
        lTestFilter.setVertexLabel( 2, "C" );
        lTestFilter.setGraphEdge( 0,2,1 );
        lTestFilter.setGraphEdge( 1,2,1 );
        gTestFilter = new GraphSet( lTestFilter );
        sCanLabel = gTestFilter.getCanLabel();

        myDFPM.hFilterLabel.add( sCanLabel );
        myDFPM.hCanLabel2FreGraph.put( sCanLabel, gTestFilter );

        lTestFilter = new LabelGraphList( 2 );
        lTestFilter.setVertexLabel( 0, "C" );
        lTestFilter.setVertexLabel( 1, "C" );
        lTestFilter.setGraphEdge( 0,1,1 );
        gTestFilter = new GraphSet( lTestFilter );
        sCanLabel = gTestFilter.getCanLabel();

        myDFPM.hFilterLabel.add( sCanLabel );
        myDFPM.hCanLabel2FreGraph.put( sCanLabel, gTestFilter );

        myDFPM.EEN( gTestGraph, hPositive, hNegtive, hHalfPositive );
    }

}
