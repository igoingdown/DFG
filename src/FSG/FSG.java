package FSG;

import Common.Parameter;
import Common.Sort;
import Common.SortNode;
import Dataset.DatasetParameter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by iejr on 2015/6/10.
 */
public class FSG {
//iejr: parameters
    public String sDatasetPath;
    public double dMinsup;
    protected int nMaxFreGraphRank;

//iejr: global variables
    protected HashSet<LabelGraphList> hFrequentSubgraphSet;
    protected HashMap<String, GraphSet> hCanLabel2Graph;
    protected ArrayList<String> aFreCanLabel;
    protected HashMap<String,Double> hCanLabel2Support;
    protected int nUniVerID;

    protected String sOutputFilePath;
    protected String sOutputDetailPath;

    //iejr: main function declearation
    public static void main( String[] args ){

        String sDataset = "Cancer";
        int dbSize = 32557;
        double threshold = 0.4;

        FSG.testFSG(sDataset, threshold, dbSize );

    }

    public void setParameter( String sDatasetPath, double nMinsup,
                              String sOutFile, String sOutDetail ){
        this.sDatasetPath = sDatasetPath;
        this.dMinsup = nMinsup;
        this.sOutputFilePath = sOutFile;
        this.sOutputDetailPath = sOutDetail;
    }

    public FSG(){
        this.hCanLabel2Graph = new HashMap<String,GraphSet>();
        this.hFrequentSubgraphSet = new HashSet<LabelGraphList>();
        this.aFreCanLabel = new ArrayList<String>();
        this.hCanLabel2Support = new HashMap<String,Double>();
        this.nUniVerID = 0;

        this.sOutputFilePath = null;
        this.sOutputDetailPath = null;

        this.nMaxFreGraphRank = Parameter.nMaxRank;
    }

    public ArrayList<GraphSet> InitialPhase(){
    //    this.hCanLabel2Graph = new HashMap<String,GraphSet>();
    //    this.hFrequentSubgraphSet = new HashSet<LabelGraphList>();
    //    this.aFreCanLabel = new ArrayList<String>();

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
                    lGraph.setVerUniID( i, this.nUniVerID++ );
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
                        HashSet<Integer> hTidList = hCanLabel2Graph1.get( sCanLabel ).hTidList;
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
                        HashSet<Integer> hTidList = hCanLabel2Graph2.get( sCanLabel ).hTidList;
                        hTidList.addAll( gGraph.hTidList );
                    }
                }

                nTid++;
            //    if( nTid % 10 == 0 ){
            //        System.out.println( "Tid:" + nTid );
            //    break;
            //    }
            }
            r.close();

            //iejr: we prune the one is not frequent
            for( String sCanLabel : hCanLabel2Support1.keySet() ){
                int nSupport = hCanLabel2Support1.get( sCanLabel );
                if( nSupport > this.dMinsup ){
                    GraphSet gFreGraph = hCanLabel2Graph1.get( sCanLabel );
                //    hFreOneGraph.add( gFreGraph );
                    hFrequentSubgraphSet.add( gFreGraph.lGraph );
                    hCanLabel2Graph.put( sCanLabel, gFreGraph );
                    aFreCanLabel.add( sCanLabel );
                    this.hCanLabel2Support.put( sCanLabel, (double)gFreGraph.hTidList.size() );
                }
            }

            System.out.println("Candidate sub 1 graph: " + hCanLabel2Graph1.size()
                    + "|Frequent sub 1 graph: " + hFrequentSubgraphSet.size());

            for( String sCanLabel : hCanLabel2Support2.keySet() ){
                int nSupport = hCanLabel2Support2.get( sCanLabel );
                if( nSupport > this.dMinsup ){
                    GraphSet gFreGraph = hCanLabel2Graph2.get( sCanLabel );
                //    hFreTwoGraph.add( gFreGraph );
                //    LabelGraphList lFreGraph = gFreGraph.lGraph;
                    HashSet<GraphSet> hSubOneGraph = getOneSubGraph( gFreGraph.lGraph,
                            -1 );
                    for( GraphSet gSubOneGraph : hSubOneGraph ){
                        gFreGraph.addSubCanLabel( gSubOneGraph.sCanLabel );
                    }
                    hFrequentSubgraphSet.add( gFreGraph.lGraph );
                    hCanLabel2Graph.put( sCanLabel, gFreGraph );
                    aFreTwoGraph.add( gFreGraph );
                    aFreCanLabel.add( sCanLabel );
                    this.hCanLabel2Support.put( sCanLabel, (double)gFreGraph.hTidList.size() );
                }
            }

            System.out.println("Candidate sub 2 graph: " + hCanLabel2Graph2.size()
                    + "|Frequent sub 2 graph: " + aFreTwoGraph.size());
            //iejr: for debug
            //printAllFreSubGraph();
            //

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return aFreTwoGraph;
    }

    protected HashSet<GraphSet> getOneSubGraph( LabelGraphList lGraph, int nTid ){
        HashSet<GraphSet> hSubOneSet = new HashSet<GraphSet>();
        HashSet<String> hCanLabelSet   = new HashSet<String>();

        for( LabelVertexList lVertex : lGraph.aVertex ){
            int nSourVertexID = lVertex.nVertexID;
            String sSourVertexLabel = lVertex.sVertexLabel;
            for( int nDestVertexID : lVertex.hNeighborList.keySet() ){
                if( nDestVertexID < nSourVertexID ){
                    continue;
                }
                String sDestVertexLabel = lGraph.aVertex.get( nDestVertexID ).sVertexLabel;
                double sEdgeLabel = lVertex.hNeighborList.get( nDestVertexID );
                LabelGraphList lSubGraphL = new LabelGraphList( 2 );
                lSubGraphL.setVertexLabel( 0, sSourVertexLabel );
                lSubGraphL.setVertexLabel( 1, sDestVertexLabel );
                lSubGraphL.setGraphEdge( 0,1,sEdgeLabel );
                lSubGraphL.setVerUniID( 0, lGraph.aVertex.get( nSourVertexID ).nVerUniID );
                lSubGraphL.setVerUniID( 1, lGraph.aVertex.get( nDestVertexID ).nVerUniID );

                LabelGraphMatrix lSubGraphM = new LabelGraphMatrix( lSubGraphL );
                String sCanLabel = lSubGraphM.getCanonicalLabel();

                if( !hCanLabelSet.contains(sCanLabel) ){
                    hCanLabelSet.add( sCanLabel );
                    GraphSet gSubGraph = new GraphSet( lSubGraphL );
                    gSubGraph.setCanLabel( sCanLabel );
                    gSubGraph.setTidList( nTid );
                    hSubOneSet.add( gSubGraph );

                //    lSubGraphM.print();
                }
            }
        }

        return hSubOneSet;
    }

    protected HashSet<GraphSet> getTwoSubGraph( LabelGraphList lGraph, int nTid ){
        HashSet<GraphSet> hSubTwoSet = new HashSet<GraphSet>();
        HashSet<String> hCanLabelSet   = new HashSet<String>();

        for( LabelVertexList lVertex : lGraph.aVertex ){
            int nSourVertexID = lVertex.nVertexID;
            String sSourVertexLabel = lVertex.sVertexLabel;
            for( int nMiddleVertexID : lVertex.hNeighborList.keySet() ){
            //    if( nMiddleVertexID < nSourVertexID ){
            //        continue;
            //    }
                String sMiddleVertexLabel = lGraph.aVertex.get( nMiddleVertexID ).sVertexLabel;
                double sEdgeLabel1 = lVertex.hNeighborList.get( nMiddleVertexID );

                LabelVertexList lMiddleVertex = lGraph.aVertex.get(nMiddleVertexID);
                for( int nDestVertexID : lMiddleVertex.hNeighborList.keySet() ) {
                    if (nDestVertexID == nSourVertexID) {
                        continue;
                    }

                    String sDestVertexLabel = lGraph.aVertex.get(nDestVertexID).sVertexLabel;
                    double sEdgeLabel2 = lMiddleVertex.hNeighborList.get(nDestVertexID);

                    LabelGraphList lSubGraphL = new LabelGraphList(3);
                    lSubGraphL.setVertexLabel(0, sSourVertexLabel);
                    lSubGraphL.setVertexLabel(1, sMiddleVertexLabel);
                    lSubGraphL.setVertexLabel(2, sDestVertexLabel);
                    lSubGraphL.setGraphEdge(0, 1, sEdgeLabel1);
                    lSubGraphL.setGraphEdge(1, 2, sEdgeLabel2);
                    lSubGraphL.setVerUniID( 0, lGraph.aVertex.get( nSourVertexID ).nVerUniID );
                    lSubGraphL.setVerUniID( 1, lGraph.aVertex.get( nMiddleVertexID ).nVerUniID );
                    lSubGraphL.setVerUniID( 2, lGraph.aVertex.get( nDestVertexID ).nVerUniID );


                    LabelGraphMatrix lSubGraphM = new LabelGraphMatrix(lSubGraphL);
                    String sCanLabel = lSubGraphM.getCanonicalLabel();



                    if (!hCanLabelSet.contains(sCanLabel)) {
                        hCanLabelSet.add(sCanLabel);
                        GraphSet gSubGraph = new GraphSet(lSubGraphL);
                        gSubGraph.setCanLabel(sCanLabel);
                        gSubGraph.setTidList(nTid);
                        hSubTwoSet.add(gSubGraph);

                        //iejr: for debug
                    //    if( sCanLabel.equals( "CCH101" ) ){
                    //        System.out.println( nTid );
                    //    }
                        //
                    //    lSubGraphM.print();
                    }
                }

            }
        }
        return hSubTwoSet;
    }

    public void ButtonUpPhase( ArrayList<GraphSet> aFreSubTwoGraph ){
        ArrayList<GraphSet> aFreSubGraphK = aFreSubTwoGraph;
        int fi = 3;
        while( fi < this.nMaxFreGraphRank ){
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
        }
    }

    public ArrayList<GraphSet> genCandidate( ArrayList<GraphSet> aGraphK ){
        HashMap<String, Automorphisms> hCanLabel2Automor = new HashMap<String, Automorphisms>();
        HashSet<String> hCanLabelSet = new HashSet<String>();
        ArrayList<GraphSet> aCandidate = new ArrayList<GraphSet>();



        for( int i = 0;i < aGraphK.size();i++ ){
            GraphSet g1 = aGraphK.get(i);
            for( int j = i;j < aGraphK.size();j++ ){
                GraphSet g2 = aGraphK.get(j);
                ArrayList<GraphSet> aSubCandidate = fsgGen( g1,g2,
                        hCanLabel2Automor,hCanLabelSet );

                //iejr: for debug
            //    System.out.println( "|hCanLabel2Automor|" + hCanLabel2Automor.size() );
            //    System.out.println( "|hCanLabelSet|     " + hCanLabelSet.size() );
            //    System.out.println( "|aCandidate|       " + aCandidate.size() );
                //

                if( aSubCandidate == null ){
                    continue;
                }
                for( GraphSet gCandidate : aSubCandidate ){
                //    LabelGraphList lCandidate = gCandidate.lGraph;
                    if( authCandidate( gCandidate ) ){
                        aCandidate.add( gCandidate );
                    }
                }
            }
        }

        return aCandidate;
    }

    protected boolean authCandidate( GraphSet gCandidate ){
        LabelGraphList lCandidate = gCandidate.lGraph;

    //    System.out.println( "auth..." );

        for( LabelVertexList lVertexList : lCandidate.aVertex ){
            int nSourVerID = lVertexList.nVertexID;
            for( int nDestVerID : lVertexList.hNeighborList.keySet() ){

                if( nDestVerID < nSourVerID ){
                    continue;
                }

                double dEdgeLabel = lVertexList.hNeighborList.get( nDestVerID );

                LabelGraphList lCandidateCopy = new LabelGraphList( lCandidate );
                lCandidateCopy.deleteGraphEdge( nSourVerID,nDestVerID,true );
                //iejr: for debug
                //lCandidateCopy.print();
                //
                LabelGraphMatrix lCandidateMatrix = new LabelGraphMatrix( lCandidateCopy );
                String sCanLabel = lCandidateMatrix.getCanonicalLabel();

                if( lCandidateCopy.judgeConnected() &&
                        !this.hCanLabel2Graph.containsKey( sCanLabel ) ){
                    return false;
                }else{
                    if( !this.hCanLabel2Graph.containsKey( sCanLabel ) ){
                        GraphSet gNewCandidate = new GraphSet( lCandidateCopy );
                        gNewCandidate.sCanLabel = sCanLabel;
                        this.hCanLabel2Graph.put( sCanLabel, gNewCandidate );
                    }
                    gCandidate.addSubCanLabel( sCanLabel );
                }
            }
        }

        return true;
    }

    protected ArrayList<GraphSet> fsgGen( GraphSet gGraph1, GraphSet gGraph2,
                                        HashMap<String, Automorphisms> hCanLabel2Automor,
                                        HashSet<String> hCanLabelSet){
        String sCore = null;
        ArrayList<GraphSet> aCandidate = new ArrayList<GraphSet>();

    //    System.out.println( "fsg-gen..." );

        for( String sCanLabel1 : gGraph1.aSubCanLabel ){
            for( String sCanLabel2: gGraph2.aSubCanLabel ){

                if( sCanLabel1.equals(sCanLabel2) ){
                    sCore = sCanLabel1;
                    ArrayList<GraphSet> aCanSubList = fsgJoin( gGraph1, gGraph2,
                            sCore, hCanLabel2Automor, hCanLabelSet );
                    if( aCanSubList != null ){
                        aCandidate.addAll( aCanSubList );
                    }
                }

            }
        }


        return aCandidate;
    }

    protected ArrayList<GraphSet> fsgJoin( GraphSet gGraph1, GraphSet gGraph2, String sCore,
                                         HashMap<String, Automorphisms> hCanLabel2Automor,
                                         HashSet<String> hCanLabelSet){

        if( sCore == null ){
            return null;
        }

    //    System.out.println( "fsg-join..." );

        GraphSet gCore = this.hCanLabel2Graph.get( sCore );
        LabelGraphList lGraph1 = gGraph1.lGraph;
        LabelGraphList lGraph2 = gGraph2.lGraph;

        //iejr: for debug
    //    lGraph1.print();
    //    lGraph2.print();
    //    gCore.lGraph.print();
        //

        LabelGraphList lEdge1  = null;
        LabelGraphList lEdge2  = null;
        //iejr: find the edge in graph1 apart from the common core
        HashMap<Integer,Integer> hVer2Ver1 = gCore.lGraph.judgeIsomorphism( lGraph1 );
        HashMap<Integer,Integer> hVer2Ver2 = gCore.lGraph.judgeIsomorphism( lGraph2 );
        if( hVer2Ver1 == null || hVer2Ver2 == null ){
            return null;
        }
        if( hVer2Ver1.size() == lGraph1.nSize || hVer2Ver2.size() == lGraph2.nSize ){
            return null;
        }

        int nNewVerIndex1 = 1;
        int nNewVerIndex2 = 1;
        int nOrgVerIndex1 = 0;
        int nOrgVerIndex2 = 0;
        for( LabelVertexList lVertexList : lGraph1.aVertex ){
            int nVertexID = lVertexList.nVertexID;
            if( !hVer2Ver1.containsKey( nVertexID ) ){
                LabelVertexList lVertexListG1 = lGraph1.aVertex.get( nVertexID );
                int nVerIDCore = -1;
                double dEdgeLabel = -1;
                for( int nDestVerID : lVertexListG1.hNeighborList.keySet() ){
                    nVerIDCore = hVer2Ver1.get( nDestVerID );
                    dEdgeLabel = lVertexListG1.hNeighborList.get( nDestVerID );
                }
                LabelVertexList lVertexListCore = gCore.lGraph.aVertex.get( nVerIDCore );
                lEdge1 = new LabelGraphList( 2 );
                lEdge1.setVertexLabel( nNewVerIndex1, lVertexListG1.sVertexLabel );
                lEdge1.setVerUniID( nNewVerIndex1, lVertexListG1.nVerUniID );
                lEdge1.setVertexLabel( nOrgVerIndex1, lVertexListCore.sVertexLabel );
                lEdge1.setVerUniID( nOrgVerIndex1, lVertexListCore.nVerUniID );
                lEdge1.setGraphEdge( nNewVerIndex1, nOrgVerIndex1, dEdgeLabel );
                break;
            }
        }

        for( LabelVertexList lVertexList : lGraph2.aVertex ){
            int nVertexID = lVertexList.nVertexID;
            if( !hVer2Ver2.containsKey( nVertexID ) ){
                int nVerIDCore = -1;
                double dEdgeLabel = -1;
                LabelVertexList lVertexListG2 = lGraph2.aVertex.get( nVertexID );
                for( int nDestVerID : lVertexListG2.hNeighborList.keySet() ){
                    nVerIDCore = hVer2Ver2.get( nDestVerID );
                    dEdgeLabel = lVertexListG2.hNeighborList.get( nDestVerID );
                }
                LabelVertexList lVertexListCore = gCore.lGraph.aVertex.get( nVerIDCore );
                lEdge2 = new LabelGraphList( 2 );
                lEdge2.setVertexLabel( nNewVerIndex2, lVertexListG2.sVertexLabel );
                lEdge2.setVerUniID( nNewVerIndex2, lVertexListG2.nVerUniID );
                lEdge2.setVertexLabel( nOrgVerIndex2, lVertexListCore.sVertexLabel );
                lEdge2.setVerUniID( nOrgVerIndex2, lVertexListCore.nVerUniID );
                lEdge2.setGraphEdge( nNewVerIndex2, nOrgVerIndex2, dEdgeLabel );
                break;
            }
        }

        if( lEdge1 == null || lEdge2 == null ){
            System.out.println( "edge in graph1 or graph2 cannot be found" );
            System.exit(0);
        }


        //iejr: find automorphisms position to the edge1
        Automorphisms aCore = null;
        if( hCanLabel2Automor.containsKey( sCore ) ){
            aCore = hCanLabel2Automor.get( sCore );
        }else{
            aCore = new Automorphisms( gCore.lGraph );
            hCanLabel2Automor.put( sCore, aCore);
        }

        ArrayList<Integer> aAutomorPos1 = null;
        ArrayList<Integer> aAutomorPos2 = null;


        {
            int nVerUniID = lEdge1.aVertex.get(nOrgVerIndex1).nVerUniID;
            if( aCore.hVerUniID2VerLabel.containsKey( nVerUniID ) ){
                aAutomorPos1 = aCore.getAutomor( nVerUniID );
            }
        }
        {
            int nVerUniID = lEdge2.aVertex.get(nOrgVerIndex2).nVerUniID;
            if( aCore.hVerUniID2VerLabel.containsKey( nVerUniID ) ){
                aAutomorPos2 = aCore.getAutomor( nVerUniID );
            }
        }

        if( aAutomorPos1 == null ){
            System.out.println( "the automorphisms to the graph 1 is null" );
            System.exit(0);
        }
        if( aAutomorPos2 == null ){
            System.out.println( "the automorphisms to the graph 2 is null" );
            System.exit(0);
        }

        String sNewVerLabel1 = lEdge1.aVertex.get( nNewVerIndex1 ).sVertexLabel;
        String sNewVerLabel2 = lEdge2.aVertex.get( nNewVerIndex2 ).sVertexLabel;
        int nVerUniID1 = lEdge1.aVertex.get( nNewVerIndex1 ).nVerUniID;
        int nVerUniID2 = lEdge2.aVertex.get( nNewVerIndex2 ).nVerUniID;
        double dNewEdgelabel1 = lEdge1.aVertex.get( nNewVerIndex1 ).hNeighborList.get(
                lEdge1.aVertex.get( nOrgVerIndex1 ).nVertexID );
        double dNewEdgelabel2 = lEdge2.aVertex.get( nNewVerIndex2 ).hNeighborList.get(
                lEdge2.aVertex.get( nOrgVerIndex2 ).nVertexID );

        //iejr: join all possible candidate k+1 subgraph
        //iejr: for debug
        int nTryCount = 0;
        ArrayList<GraphSet> aCandidate = new ArrayList<GraphSet>();
        for( int nAutomorVerID1 : aAutomorPos1 ){

            for( int nAutomorVerID2 : aAutomorPos2 ){
                //iejr: for debug
            //    System.out.println( "Try count:     " + nTryCount++ );

                LabelGraphList lCandidateGraph = new LabelGraphList( gCore.lGraph );
                int nNewVerID1 = lCandidateGraph.addVertex( sNewVerLabel1 );
            //    lCandidateGraph.setVerUniID( nNewVerID1, nVerUniID1 );
                lCandidateGraph.setVerUniID( nNewVerID1, this.nUniVerID++ );
                lCandidateGraph.setGraphEdge( nAutomorVerID1, nNewVerID1, dNewEdgelabel1 );
                int nNewVerID2 = lCandidateGraph.addVertex( sNewVerLabel2 );
            //    lCandidateGraph.setVerUniID( nNewVerID2, nVerUniID2 );
                lCandidateGraph.setVerUniID( nNewVerID2, this.nUniVerID++ );
                lCandidateGraph.setGraphEdge( nAutomorVerID2, nNewVerID2, dNewEdgelabel2 );

                LabelGraphMatrix lCandidateMatrix = new LabelGraphMatrix( lCandidateGraph );

                //iejr: for debug
            //    System.out.println( "Getting canonical labeling..." );
                String sCandidateCanLabel = lCandidateMatrix.getCanonicalLabel();
            //    System.out.println( "Getting over" );

                if( !hCanLabelSet.contains( sCandidateCanLabel ) ){
                    GraphSet gCandidateGraph = new GraphSet( lCandidateGraph );
                    gCandidateGraph.sCanLabel = sCandidateCanLabel;
                    hCanLabelSet.add( sCandidateCanLabel );
                    aCandidate.add( gCandidateGraph );

                    //iejr: for debug
                //    System.out.println( "add candidate over" );
                    //
                }

                if( (nAutomorVerID1 != nAutomorVerID2) && (sNewVerLabel1.equals(sNewVerLabel2)) ){

                    //iejr: for debug
                //    System.out.println( "Alternative super graph finding..." );
                    //

                    LabelGraphList lCandidateGraph2 = new LabelGraphList( gCore.lGraph );
                    nNewVerID1 = lCandidateGraph2.addVertex( sNewVerLabel1 );
                    lCandidateGraph2.setVerUniID( nNewVerID1, this.nUniVerID++ );
                    lCandidateGraph2.setGraphEdge( nAutomorVerID1, nNewVerID1, dNewEdgelabel1 );
                    lCandidateGraph2.setGraphEdge( nAutomorVerID2, nNewVerID1, dNewEdgelabel2 );

                    lCandidateMatrix = new LabelGraphMatrix( lCandidateGraph2 );
                    sCandidateCanLabel = lCandidateMatrix.getCanonicalLabel();

                    //iejr: for debug
                //    System.out.println( "Alternative super graph " + sCandidateCanLabel );
                    //

                    if( !hCanLabelSet.contains( sCandidateCanLabel ) ){
                        GraphSet gCandidateGraph = new GraphSet( lCandidateGraph2 );
                        gCandidateGraph.sCanLabel = sCandidateCanLabel;
                        hCanLabelSet.add( sCandidateCanLabel );
                        aCandidate.add( gCandidateGraph );
                        //iejr: for debug
                        //gCandidateGraph.lGraph.print();
                        //
                    }

                    //iejr: for debug
                //    System.out.println( "Alternative super graph finding over" );
                    //
                }
            }
        }

        //iejr: for debug
    //    System.out.println( "fsg-join over" );
        //

        return aCandidate;

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
                //    ArrayList<Integer> aTidList = hCanLabel2Graph.get( sCanLabel ).aTidList;
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

            //iejr: we prune the one is not frequent
            for( String sCanLabel : hCanLabel2Support.keySet() ){
                int nSupport = hCanLabel2Support.get( sCanLabel );
                System.out.println( "Candidate: " + sCanLabel + "| Support: " + nSupport );
                if( nSupport > this.dMinsup ){
                    GraphSet gFreGraph = hCanLabel2Graph.get( sCanLabel );
                    this.hFrequentSubgraphSet.add( gFreGraph.lGraph );
                    this.hCanLabel2Graph.put( sCanLabel, gFreGraph );
                    aFreCanLabel.add( gFreGraph.sCanLabel );
                    aFreSubGraph.add(gFreGraph);
                    this.hCanLabel2Support.put( sCanLabel, (double)gFreGraph.hTidList.size() );
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

                w.write( sCanLabel + ":" + dSupport + "\r\n" );

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


    public void writeTime( long lTime, String writeFilePath ){

        try{

            FileWriter w = new FileWriter( writeFilePath, true );

            w.write( "\r\n" + "Time    " + lTime + "\r\n");
            w.write( "\r\n" + "Th    " + this.dMinsup + "\r\n");

            w.close();

        } catch( FileNotFoundException e ){
            e.printStackTrace();
        } catch( IOException e ){
            e.printStackTrace();
        }

    }


    public void printAllFreSubGraph(){
        for( LabelGraphList lGraph : this.hFrequentSubgraphSet ){
            lGraph.print();
            System.out.println();
        }
    }

    public static void testFSG( String sDatasetName, double dThreshold, int nDatasize ){

        String sTime = Parameter.getTime();
        String sDatasetPath = "./Dataset/" + sDatasetName + ".dat";
        double dSupport = nDatasize * dThreshold;

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }

        String sOutPutPath = "./out/" + sDatasetName + "/FSG/" + "Result_"
                + dThreshold + "_" + "Standard" + ".txt";
        String sOutPutDetailPath = null;

        long lStart = System.currentTimeMillis();

        FSG myFSG = new FSG();
        myFSG.setParameter( sDatasetPath, dSupport, sOutPutPath, sOutPutDetailPath );
        System.out.println( myFSG.dMinsup );

        ArrayList<GraphSet> aFreTwoGraph = myFSG.InitialPhase();
        myFSG.ButtonUpPhase( aFreTwoGraph );

        long lEnd = System.currentTimeMillis();
        myFSG.writeTime( lEnd - lStart, sOutPutPath );

        int nFreSubGraphNum = myFSG.hFrequentSubgraphSet.size();
        System.out.println( "Total frequent sub graph number is: " + nFreSubGraphNum );

    }

    public static void testSupportCounting(){
        String sDatasetName = "FDA-single";
        String sTestGraph   = "FDA-single-count";
        String sThreshold   = "One";
        double dThreshold   = 0.07;
        String sDatasetPath = DatasetParameter.getDatasetPath(sDatasetName);
        String sTestGraphPath = DatasetParameter.getDatasetPath(sTestGraph);
        double dSupport = DatasetParameter.getAbusoluteThreshold( sDatasetName, dThreshold );

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }

        FSG myFSG = new FSG();
        myFSG.setParameter(sDatasetPath, dSupport, null ,null);
        System.out.println( myFSG.dMinsup );

        ArrayList<LabelGraphList> aTestGraph = new ArrayList<LabelGraphList>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(sTestGraphPath));

            String sLine = null;
            String sEndPattern = "EOF";
            while ((sLine = r.readLine()) != null) {
                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");

                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                }

                while (!(sLine = r.readLine()).contains(sEndPattern)) {
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                aTestGraph.add( lGraph );
            }
            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Test graph is: ");
        for( LabelGraphList lGraph : aTestGraph ){
            GraphSet gGraph = new GraphSet( lGraph );
            String sCanLabel = gGraph.getCanLabel();
            System.out.println( sCanLabel );
            lGraph.print();
            System.out.println();
        }

        int[] nSupport = new int[aTestGraph.size()];
        int nTid = 0;
        try {
            BufferedReader r = new BufferedReader(new FileReader(sDatasetPath));

            String sLine = null;
            String sEndPattern = "EOF";
            while ((sLine = r.readLine()) != null) {
                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");

                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                }

                while (!(sLine = r.readLine()).contains(sEndPattern)) {
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                nTid++;

                for( int i = 0;i < aTestGraph.size();i++ ){
                    LabelGraphList lCandidateGraph = aTestGraph.get( i );
                    if( lCandidateGraph.judgeIsomorphismPrun( lGraph ) != null ){
                        nSupport[i]++;
                        System.out.println( nTid-1 );
                    }
                }

            }
            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Test result is: ");
        for( int i = 0;i < aTestGraph.size();i++ ){
            LabelGraphList lCandidateGraph = aTestGraph.get( i );
            GraphSet gGraph = new GraphSet( lCandidateGraph );
            String sCanLabel = gGraph.getCanLabel();
            System.out.println( sCanLabel + ":" + nSupport[i] + "   "
                    + (nSupport[i] > myFSG.dMinsup) );
            System.out.println();
        }
        LabelGraphList lTypicalGraph = null;
        for( int i = 0;i < aTestGraph.size();i++ ){
            LabelGraphList lCandidateGraph = aTestGraph.get( i );
            GraphSet gGraph = new GraphSet( lCandidateGraph );
            String sCanLabel = gGraph.getCanLabel();
            System.out.println( sCanLabel + ":" + nSupport[i] + "   "
                    + (nSupport[i] > myFSG.dMinsup) );
            System.out.println();
        }

    }

    public static void testSupport( int nNumber ){
        String sDatasetName = "FDA-single";
        String sTestGraph   = "FDA-single-count";
        String sThreshold   = "One";
        double dThreshold   = 0.07;
        String sDatasetPath = DatasetParameter.sDatasetPathPrefix + sDatasetName + "/";
        String sTestGraphPath = DatasetParameter.getDatasetPath(sTestGraph);
        double dSupport = DatasetParameter.getAbusoluteThreshold( sDatasetName, dThreshold );

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }

        FSG myFSG = new FSG();

        ArrayList<LabelGraphList> aTestGraph = new ArrayList<LabelGraphList>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(sTestGraphPath));

            String sLine = null;
            String sEndPattern = "EOF";
            while ((sLine = r.readLine()) != null) {
                sLine = sLine.trim();

                String[] sVertexLabel = sLine.split(" ");

                LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
                for (int i = 0; i < sVertexLabel.length; i++) {
                    lGraph.setVertexLabel(i, sVertexLabel[i]);
                }

                while (!(sLine = r.readLine()).contains(sEndPattern)) {
                    String[] sEdgeInfo = sLine.trim().split(" ");
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                }

                aTestGraph.add( lGraph );
            }
            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LabelGraphList lTestGraph = null;
        sDatasetPath += nNumber + ".txt";
        try {
            BufferedReader r = new BufferedReader(new FileReader(sDatasetPath));

            String sLine = null;
            String sEndPattern = "EOF";

            sLine = r.readLine();
            sLine = sLine.trim();

            String[] sVertexLabel = sLine.split(" ");

            LabelGraphList lGraph = new LabelGraphList(sVertexLabel.length);
            for (int i = 0; i < sVertexLabel.length; i++) {
                lGraph.setVertexLabel(i, sVertexLabel[i]);
            }

            while ( (sLine = r.readLine()) != null) {
                String[] sEdgeInfo = sLine.trim().split(" ");
                int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                double dEdgeLabel = Integer.parseInt(sEdgeInfo[2]);
                lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
            }

            lTestGraph = lGraph;

            if( lGraph.judgeIsomorphismPrun( aTestGraph.get( 0 ) ) != null ){
                System.out.println( "Isomorphism false" );
            }else{
                System.out.println( "Isomorphism true" );
            }

            r.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //////////////////////////////////////////////////////////
        lTestGraph.print();
        HashSet<GraphSet> hSubTwoSet = new HashSet<GraphSet>();
        HashSet<String> hCanLabelSet   = new HashSet<String>();


        for( LabelVertexList lVertex : lTestGraph.aVertex ){
            int nSourVertexID = lVertex.nVertexID;
            String sSourVertexLabel = lVertex.sVertexLabel;
            for( int nMiddleVertexID : lVertex.hNeighborList.keySet() ){
            //    if( nMiddleVertexID < nSourVertexID ){
            //        continue;
            //    }
                String sMiddleVertexLabel = lTestGraph.aVertex.get(
                        nMiddleVertexID ).sVertexLabel;
                double sEdgeLabel1 = lVertex.hNeighborList.get( nMiddleVertexID );

                LabelVertexList lMiddleVertex = lTestGraph.aVertex.get(nMiddleVertexID);
                for( int nDestVertexID : lMiddleVertex.hNeighborList.keySet() ) {
                    if (nDestVertexID == nSourVertexID) {
                        continue;
                    }

                    String sDestVertexLabel = lTestGraph.aVertex.get(
                            nDestVertexID).sVertexLabel;
                    double sEdgeLabel2 = lMiddleVertex.hNeighborList.get(nDestVertexID);

                    LabelGraphList lSubGraphL = new LabelGraphList(3);
                    lSubGraphL.setVertexLabel(0, sSourVertexLabel);
                    lSubGraphL.setVertexLabel(1, sMiddleVertexLabel);
                    lSubGraphL.setVertexLabel(2, sDestVertexLabel);
                    lSubGraphL.setGraphEdge(0, 1, sEdgeLabel1);
                    lSubGraphL.setGraphEdge(1, 2, sEdgeLabel2);
                    lSubGraphL.setVerUniID( 0, lTestGraph.aVertex.get(
                            nSourVertexID ).nVerUniID );
                    lSubGraphL.setVerUniID( 1, lTestGraph.aVertex.get(
                            nMiddleVertexID ).nVerUniID );
                    lSubGraphL.setVerUniID( 2, lTestGraph.aVertex.get(
                            nDestVertexID ).nVerUniID );


                    LabelGraphMatrix lSubGraphM = new LabelGraphMatrix(lSubGraphL);
                    String sCanLabel = lSubGraphM.getCanonicalLabel();



                    if (!hCanLabelSet.contains(sCanLabel)) {
                        hCanLabelSet.add(sCanLabel);
                        GraphSet gSubGraph = new GraphSet(lSubGraphL);
                        gSubGraph.setCanLabel(sCanLabel);
                    //    gSubGraph.setTidList(nTid);
                        hSubTwoSet.add(gSubGraph);

                        System.out.println( sCanLabel );

                        //    lSubGraphM.print();
                    }
                }

            }
        }
    }

    public static void testJoin(){

        LabelGraphList lGraph1 = new LabelGraphList( 5 );
        lGraph1.setVertexLabel( 0, "C" );
        lGraph1.setVertexLabel( 1, "C" );
        lGraph1.setVertexLabel( 2, "C" );
        lGraph1.setVertexLabel( 3, "C" );
        lGraph1.setVertexLabel( 4, "C" );
        lGraph1.setGraphEdge( 0,1,1 );
        lGraph1.setGraphEdge( 1,2,1 );
        lGraph1.setGraphEdge( 2,3,1 );
        lGraph1.setGraphEdge( 3,4,1 );


        LabelGraphList lGraph2 = new LabelGraphList( 5 );
        lGraph2.setVertexLabel( 0, "C" );
        lGraph2.setVertexLabel( 1, "C" );
        lGraph2.setVertexLabel( 2, "C" );
        lGraph2.setVertexLabel( 3, "C" );
        lGraph2.setVertexLabel( 4, "H" );
        lGraph2.setGraphEdge( 0,1,1 );
        lGraph2.setGraphEdge( 1,2,1 );
        lGraph2.setGraphEdge( 2,3,1 );
        lGraph2.setGraphEdge( 3,4,1 );

        GraphSet gGraph1 = new GraphSet( lGraph1 );
        String sCanLabel1 = gGraph1.getCanLabel();
        gGraph1.setCanLabel( sCanLabel1 );

        GraphSet gGraph2 = new GraphSet( lGraph2 );
        String sCanLabel2 = gGraph2.getCanLabel();
        gGraph1.setCanLabel( sCanLabel2 );

        System.out.println( "Test candidate is : " );
        System.out.println( sCanLabel1 );
        System.out.println( sCanLabel2 );
        System.out.println();

        ArrayList<GraphSet> aCandiate = new ArrayList<GraphSet>();
        aCandiate.add( gGraph1 );
        aCandiate.add( gGraph2 );

        FSG myFSG = new FSG();
        ArrayList<GraphSet> aResult = myFSG.genCandidate( aCandiate );

        for( GraphSet gGraph : aResult ){
            System.out.println( gGraph.sCanLabel );
        }
    }

}
