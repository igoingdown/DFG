package FSG_fake;

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
 * verion 1.0, bug version
 * todo: used for find small fresquent subgraph
 */
public class FSG {
//iejr: parameters
    public String sDatasetPath;
    public double dMinsup;

//iejr: global variables
    protected HashSet<LabelGraphList> hFrequentSubgraphSet;
    protected HashMap<String, GraphSet> hCanLabel2Graph;
    protected ArrayList<String> aFreCanLabel;

    protected String sOutputFilePath;
    protected String sOutputDetailPath;

    //iejr: main function declearation
    public static void main( String[] args ){

        String sDataset = "Cancer";
        int dbSize = 32557;
        double dThreshold = 0.1;

        FSG.testFSG( sDataset, dThreshold, dbSize );
    }

    public void setParameter( String sDatasetPath, double nMinsup, String sOutFile, String sOutDetail ){
        this.sDatasetPath = sDatasetPath;
        this.dMinsup = nMinsup;
        this.sOutputFilePath = sOutFile;
        this.sOutputDetailPath = sOutDetail;
    }

    public ArrayList<GraphSet> InitialPhase(){
        this.hCanLabel2Graph = new HashMap<String,GraphSet>();
        this.hFrequentSubgraphSet = new HashSet<LabelGraphList>();
        this.aFreCanLabel = new ArrayList<String>();

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
            //    System.out.println( "Tid:" + nTid );
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
                }
            }

            System.out.println("Candidate sub 1 graph: " + hCanLabel2Graph1.size() + "|Frequent sub 1 graph: " + hFrequentSubgraphSet.size());

            for( String sCanLabel : hCanLabel2Support2.keySet() ){
                int nSupport = hCanLabel2Support2.get( sCanLabel );
                if( nSupport > this.dMinsup ){
                    GraphSet gFreGraph = hCanLabel2Graph2.get( sCanLabel );
                //    hFreTwoGraph.add( gFreGraph );
                //    LabelGraphList lFreGraph = gFreGraph.lGraph;
                    HashSet<GraphSet> hSubOneGraph = getOneSubGraph( gFreGraph.lGraph, -1 );
                    for( GraphSet gSubOneGraph : hSubOneGraph ){
                        gFreGraph.addSubCanLabel( gSubOneGraph.sCanLabel );
                    }
                    hFrequentSubgraphSet.add( gFreGraph.lGraph );
                    hCanLabel2Graph.put( sCanLabel, gFreGraph );
                    aFreTwoGraph.add( gFreGraph );
                    aFreCanLabel.add( sCanLabel );
                }
            }

            System.out.println("Candidate sub 2 graph: " + hCanLabel2Graph2.size() + "|Frequent sub 2 graph: " + aFreTwoGraph.size());
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
                if( nMiddleVertexID < nSourVertexID ){
                    continue;
                }
                String sMiddleVertexLabel = lGraph.aVertex.get( nMiddleVertexID ).sVertexLabel;
                double sEdgeLabel1 = lVertex.hNeighborList.get( nMiddleVertexID );

                LabelVertexList lMiddleVertex = lGraph.aVertex.get( nMiddleVertexID );
                for( int nDestVertexID : lMiddleVertex.hNeighborList.keySet() ) {
                    if (nDestVertexID < nMiddleVertexID) {
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
        while( true ){
            System.out.println("Mining frequent " + fi + " sub graphing...");
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
        }
    }

    protected ArrayList<GraphSet> genCandidate( ArrayList<GraphSet> aGraphK ){
        HashMap<String, Automorphisms> hCanLabel2Automor = new HashMap<String, Automorphisms>();
        HashSet<String> hCanLabelSet = new HashSet<String>();
        ArrayList<GraphSet> aCandidate = new ArrayList<GraphSet>();

        for( int i = 0;i < aGraphK.size();i++ ){
            GraphSet g1 = aGraphK.get(i);
            for( int j = i;j < aGraphK.size();j++ ){
                GraphSet g2 = aGraphK.get(j);
                ArrayList<GraphSet> aSubCandidate = fsgGen( g1,g2,hCanLabel2Automor,hCanLabelSet );
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
        for( String sCanLabel1 : gGraph1.aSubCanLabel ){
            for( String sCanLabel2: gGraph2.aSubCanLabel ){

                if( sCanLabel1.equals(sCanLabel2) ){
                    sCore = sCanLabel1;
                    ArrayList<GraphSet> aCanSubList = fsgJoin( gGraph1, gGraph2, sCore, hCanLabel2Automor, hCanLabelSet );
                    if( aCanSubList != null ){
                        aCandidate.addAll( aCanSubList );
                    }
                }

            }
        }

        /*
        if( sCore == null ){
            return null;
        }

        GraphSet gCore = this.hCanLabel2Graph.get( sCore );
        LabelGraphList lGraph1 = gGraph1.lGraph;
        LabelGraphList lGraph2 = gGraph2.lGraph;

        //iejr: for debug
        lGraph1.print();
        lGraph2.print();
        gCore.lGraph.print();
        //

        LabelGraphList lEdge1  = null;
        LabelGraphList lEdge2  = null;
        //iejr: find the edge in graph1 apart from the common core
        for( LabelVertexList aVertexList : lGraph1.aVertex ){
        //    LabelVertex lSourVertex = aVertexList;
            for( int nDestVerID : aVertexList.hNeighborList.keySet() ){
                LabelVertex lDestVertex = lGraph1.aVertex.get( nDestVerID );

                LabelGraphList lSubOneGraph = new LabelGraphList( 2 );
                lSubOneGraph.setVertexLabel(0, aVertexList.sVertexLabel);
                lSubOneGraph.setVertexLabel( 1,lDestVertex.sVertexLabel );
                lSubOneGraph.setGraphEdge(0, 1, aVertexList.hNeighborList.get(nDestVerID));
                lSubOneGraph.setVerUniID( 0, aVertexList.nVerUniID );
                lSubOneGraph.setVerUniID( 1, lDestVertex.nVerUniID );

                if( !lSubOneGraph.judgeIsomorphism( gCore.lGraph ) ){
                    lEdge1 = lSubOneGraph;
                    break;
                }
            }

            if( lEdge1 != null ){
                break;
            }
        }

        //iejr: find the edge in graph2 apart from the common core
        for( LabelVertexList aVertexList : lGraph2.aVertex ){
        //    LabelVertex lSourVertex = aVertexList;

            for( int nDestVerID : aVertexList.hNeighborList.keySet() ){
                LabelVertex lDestVertex = lGraph2.aVertex.get( nDestVerID );

                LabelGraphList lSubOneGraph = new LabelGraphList( 2 );
                lSubOneGraph.setVertexLabel(0, aVertexList.sVertexLabel);
                lSubOneGraph.setVertexLabel( 1,lDestVertex.sVertexLabel );
                lSubOneGraph.setGraphEdge(0, 1, aVertexList.hNeighborList.get(nDestVerID));
                lSubOneGraph.setVerUniID( 0, aVertexList.nVerUniID );
                lSubOneGraph.setVerUniID( 1, lDestVertex.nVerUniID );

                if( !lSubOneGraph.judgeIsomorphism( gCore.lGraph ) ){
                    lEdge2 = lSubOneGraph;
                    break;
                }
            }

            if( lEdge2 != null ){
                break;
            }
        }

        if( lEdge1 == null ){
            System.out.println("Edge 1 cannot be found");
            System.exit(0);
        }
        if( lEdge2 == null ){
            System.out.println( "Edge 2 cannot be found" );
            System.exit(0);
        }

        //iejr: find automorphisms position to the edge1
        Automorphisms aCore = null;
        if( hCanLabel2Automor.containsKey( sCore ) ){
            aCore = hCanLabel2Automor.get( sCore );
        }else{
            aCore = new Automorphisms( gCore.lGraph );
        }

        ArrayList<Integer> aAutomorPos1 = null;
        ArrayList<Integer> aAutomorPos2 = null;
        int nNewVerIndex1 = -1;
        int nNewVerIndex2 = -1;
        int nOrgVerIndex1 = -1;
        int nOrgVerIndex2 = -1;
    //    int nCoreVerID1 = -1;
    //    int nCoreVerID2 = -1;
        for( int i = 0;i < lEdge1.aVertex.size();i++ ){
            int nVerUniID = lEdge1.aVertex.get(i).nVerUniID;
            if( aCore.hVerUniID2VerLabel.containsKey( nVerUniID ) ){
                aAutomorPos1 = aCore.getAutomor( nVerUniID );
            //    nCoreVerID1 = gCore.lGraph.getLocalVertexID( nVerUniID );
                nNewVerIndex1 = i;
            }else{
                nOrgVerIndex1 = i;
            }
        }

        for( int i = 0;i < lEdge2.aVertex.size();i++ ){
            int nVerUniID = lEdge2.aVertex.get(i).nVerUniID;
            if( aCore.hVerUniID2VerLabel.containsKey( nVerUniID ) ){
                aAutomorPos2 = aCore.getAutomor( nVerUniID );
            //    nCoreVerID2 = gCore.lGraph.getLocalVertexID( nVerUniID );
                nNewVerIndex2 = i;
            }else{
                nOrgVerIndex2 = i;
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
        double dNewEdgelabel1 = lEdge1.aVertex.get( nNewVerIndex1 ).hNeighborList.get( lEdge1.aVertex.get( nOrgVerIndex1 ).nVertexID );
        double dNewEdgelabel2 = lEdge2.aVertex.get( nNewVerIndex2 ).hNeighborList.get( lEdge2.aVertex.get( nOrgVerIndex2 ).nVertexID );

        //iejr: join all possible candidate k+1 subgraph
        ArrayList<GraphSet> aCandidate = new ArrayList<GraphSet>();
        for( int nAutomorVerID1 : aAutomorPos1 ){

            for( int nAutomorVerID2 : aAutomorPos2 ){
                LabelGraphList lCandidateGraph = new LabelGraphList( gCore.lGraph );
                int nNewVerID1 = lCandidateGraph.addVertex( sNewVerLabel1 );
                lCandidateGraph.setGraphEdge( nAutomorVerID1, nNewVerID1, dNewEdgelabel1 );
                int nNewVerID2 = lCandidateGraph.addVertex( sNewVerLabel2 );
                lCandidateGraph.setGraphEdge( nAutomorVerID2, nNewVerID2, dNewEdgelabel2 );

                LabelGraphMatrix lCandidateMatrix = new LabelGraphMatrix( lCandidateGraph );
                String sCandidateCanLabel = lCandidateMatrix.getCanonicalLabel();
                if( !hCanLabelSet.contains( sCandidateCanLabel ) ){
                    GraphSet gCandidateGraph = new GraphSet( lCandidateGraph );
                    gCandidateGraph.sCanLabel = sCandidateCanLabel;
                    hCanLabelSet.add( sCandidateCanLabel );
                    aCandidate.add( gCandidateGraph );
                }

                if( sNewVerLabel1.equals(sNewVerLabel2) ){
                    LabelGraphList lCandidateGraph2 = new LabelGraphList( gCore.lGraph );
                    nNewVerID1 = lCandidateGraph.addVertex( sNewVerLabel1 );
                    lCandidateGraph2.setGraphEdge( nAutomorVerID1, nNewVerID1, dNewEdgelabel1 );
                    lCandidateGraph2.setGraphEdge( nAutomorVerID2, nNewVerID1, dNewEdgelabel2 );

                    lCandidateMatrix = new LabelGraphMatrix( lCandidateGraph );
                    sCandidateCanLabel = lCandidateMatrix.getCanonicalLabel();
                    if( !hCanLabelSet.contains( sCandidateCanLabel ) ){
                        GraphSet gCandidateGraph = new GraphSet( lCandidateGraph );
                        gCandidateGraph.sCanLabel = sCandidateCanLabel;
                        hCanLabelSet.add( sCandidateCanLabel );
                        aCandidate.add( gCandidateGraph );
                        //iejr: for debug
                        gCandidateGraph.lGraph.print();
                        //
                    }
                }
            }
        }*/

        return aCandidate;
    }

    protected ArrayList<GraphSet> fsgJoin( GraphSet gGraph1, GraphSet gGraph2, String sCore,
                                         HashMap<String, Automorphisms> hCanLabel2Automor,
                                         HashSet<String> hCanLabelSet){

        if( sCore == null ){
            return null;
        }

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
        /*
        for( LabelVertexList aVertexList : lGraph1.aVertex ){
            //    LabelVertex lSourVertex = aVertexList;
            for( int nDestVerID : aVertexList.hNeighborList.keySet() ){
                LabelVertex lDestVertex = lGraph1.aVertex.get( nDestVerID );

                LabelGraphList lSubOneGraph = new LabelGraphList( 2 );
                lSubOneGraph.setVertexLabel(0, aVertexList.sVertexLabel);
                lSubOneGraph.setVertexLabel( 1,lDestVertex.sVertexLabel );
                lSubOneGraph.setGraphEdge(0, 1, aVertexList.hNeighborList.get(nDestVerID));
                lSubOneGraph.setVerUniID( 0, aVertexList.nVerUniID );
                lSubOneGraph.setVerUniID( 1, lDestVertex.nVerUniID );

                if( lSubOneGraph.judgeIsomorphism( gCore.lGraph )==null ){
                    lEdge1 = lSubOneGraph;
                    break;
                }
            }

            if( lEdge1 != null ){
                break;
            }
        }

        //iejr: find the edge in graph2 apart from the common core
        for( LabelVertexList aVertexList : lGraph2.aVertex ){
            //    LabelVertex lSourVertex = aVertexList;

            for( int nDestVerID : aVertexList.hNeighborList.keySet() ){
                LabelVertex lDestVertex = lGraph2.aVertex.get( nDestVerID );

                LabelGraphList lSubOneGraph = new LabelGraphList( 2 );
                lSubOneGraph.setVertexLabel(0, aVertexList.sVertexLabel);
                lSubOneGraph.setVertexLabel( 1,lDestVertex.sVertexLabel );
                lSubOneGraph.setGraphEdge(0, 1, aVertexList.hNeighborList.get(nDestVerID));
                lSubOneGraph.setVerUniID( 0, aVertexList.nVerUniID );
                lSubOneGraph.setVerUniID( 1, lDestVertex.nVerUniID );

                if( lSubOneGraph.judgeIsomorphism( gCore.lGraph )==null ){
                    lEdge2 = lSubOneGraph;
                    break;
                }
            }

            if( lEdge2 != null ){
                break;
            }
        }

        if( lEdge1 == null ){
            System.out.println("Edge 1 cannot be found");
            System.exit(0);
        }
        if( lEdge2 == null ){
            System.out.println( "Edge 2 cannot be found" );
            System.exit(0);
        }
        */
        //iejr: find automorphisms position to the edge1
        Automorphisms aCore = null;
        if( hCanLabel2Automor.containsKey( sCore ) ){
            aCore = hCanLabel2Automor.get( sCore );
        }else{
            aCore = new Automorphisms( gCore.lGraph );
        }

        ArrayList<Integer> aAutomorPos1 = null;
        ArrayList<Integer> aAutomorPos2 = null;

        //    int nCoreVerID1 = -1;
        //    int nCoreVerID2 = -1;
        /*
        for( int i = 0;i < lEdge1.aVertex.size();i++ ){
            int nVerUniID = lEdge1.aVertex.get(i).nVerUniID;
            if( aCore.hVerUniID2VerLabel.containsKey( nVerUniID ) ){
                aAutomorPos1 = aCore.getAutomor( nVerUniID );
                //    nCoreVerID1 = gCore.lGraph.getLocalVertexID( nVerUniID );
                nNewVerIndex1 = i;
            }else{
                nOrgVerIndex1 = i;
            }
        }

        for( int i = 0;i < lEdge2.aVertex.size();i++ ){
            int nVerUniID = lEdge2.aVertex.get(i).nVerUniID;
            if( aCore.hVerUniID2VerLabel.containsKey( nVerUniID ) ){
                aAutomorPos2 = aCore.getAutomor( nVerUniID );
                //    nCoreVerID2 = gCore.lGraph.getLocalVertexID( nVerUniID );
                nNewVerIndex2 = i;
            }else{
                nOrgVerIndex2 = i;
            }
        }
        */
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
        double dNewEdgelabel1 = lEdge1.aVertex.get( nNewVerIndex1 ).hNeighborList.get( lEdge1.aVertex.get( nOrgVerIndex1 ).nVertexID );
        double dNewEdgelabel2 = lEdge2.aVertex.get( nNewVerIndex2 ).hNeighborList.get( lEdge2.aVertex.get( nOrgVerIndex2 ).nVertexID );

        //iejr: join all possible candidate k+1 subgraph
        ArrayList<GraphSet> aCandidate = new ArrayList<GraphSet>();
        for( int nAutomorVerID1 : aAutomorPos1 ){

            for( int nAutomorVerID2 : aAutomorPos2 ){
                LabelGraphList lCandidateGraph = new LabelGraphList( gCore.lGraph );
                int nNewVerID1 = lCandidateGraph.addVertex( sNewVerLabel1 );
                lCandidateGraph.setVerUniID( nNewVerID1, nVerUniID1 );
                lCandidateGraph.setGraphEdge( nAutomorVerID1, nNewVerID1, dNewEdgelabel1 );
                int nNewVerID2 = lCandidateGraph.addVertex( sNewVerLabel2 );
                lCandidateGraph.setVerUniID( nNewVerID2, nVerUniID2 );
                lCandidateGraph.setGraphEdge( nAutomorVerID2, nNewVerID2, dNewEdgelabel2 );

                LabelGraphMatrix lCandidateMatrix = new LabelGraphMatrix( lCandidateGraph );
                String sCandidateCanLabel = lCandidateMatrix.getCanonicalLabel();
                if( !hCanLabelSet.contains( sCandidateCanLabel ) ){
                    GraphSet gCandidateGraph = new GraphSet( lCandidateGraph );
                    gCandidateGraph.sCanLabel = sCandidateCanLabel;
                    hCanLabelSet.add( sCandidateCanLabel );
                    aCandidate.add( gCandidateGraph );
                }

                if( (nAutomorVerID1 != nAutomorVerID2) && (sNewVerLabel1.equals(sNewVerLabel2)) ){
                    LabelGraphList lCandidateGraph2 = new LabelGraphList( gCore.lGraph );
                    nNewVerID1 = lCandidateGraph2.addVertex( sNewVerLabel1 );
                    lCandidateGraph.setVerUniID( nNewVerID1, nVerUniID1 );
                    lCandidateGraph2.setGraphEdge( nAutomorVerID1, nNewVerID1, dNewEdgelabel1 );
                    lCandidateGraph2.setGraphEdge( nAutomorVerID2, nNewVerID1, dNewEdgelabel2 );

                    lCandidateMatrix = new LabelGraphMatrix( lCandidateGraph2 );
                    sCandidateCanLabel = lCandidateMatrix.getCanonicalLabel();
                    if( !hCanLabelSet.contains( sCandidateCanLabel ) ){
                        GraphSet gCandidateGraph = new GraphSet( lCandidateGraph2 );
                        gCandidateGraph.sCanLabel = sCandidateCanLabel;
                        hCanLabelSet.add( sCandidateCanLabel );
                        aCandidate.add( gCandidateGraph );
                        //iejr: for debug
                        //gCandidateGraph.lGraph.print();
                        //
                    }
                }
            }
        }

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
                    aFreSubGraph.add( gFreGraph );
                }
            }


            //iejr: for debug
            printAllFreSubGraph();
            //

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return aFreSubGraph;
    }


    protected void writeFrequentGraph( String sOutputGraph, String sOutputDetail ){

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
            double dSupport = gGraph.hTidList.size();
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

                String sOutputDetailName = sOutputDetail + "/" + i + ".txt";

                file = new File(sOutputDetailName);
                try {
                    if (file.exists())
                        file.delete();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FileWriter wDetail = new FileWriter( sOutputDetailName , true);
                GraphSet gGraph = this.hCanLabel2Graph.get( sCanLabel );
                LabelGraphList lGraph = gGraph.lGraph;
                String sVerLabel = lGraph.getAllVertexLabel();
                String[] sEdgeRecord = lGraph.getAllEdgeRecord();

                wDetail.write( sVerLabel + "\r\n" );
                for( String sEdge : sEdgeRecord ){
                    wDetail.write( sEdge + "\r\n" );
                }
                wDetail.close();
            }

            w.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        System.out.println( "FSG fake program excuting..." );

        String sTime = Parameter.getTime();
        String sDatasetPath = "./Dataset/" + sDatasetName + ".dat";
        double dSupport = nDatasize * dThreshold;

        if( sDatasetPath == null || dSupport == -1 ){
            System.out.println( "Error in argument setting!" );
            System.exit( 0 );
        }

        String sOutPutPath = "./out/" + sDatasetName + "/FSG/" + "Result(bad)_" + dThreshold + "_" + sTime + ".txt";
        String sOutPutDetailPath = "./out/" + sDatasetName + "/FSG/badsample/";

        long lStart = System.currentTimeMillis();

        FSG myFSG = new FSG();
        myFSG.setParameter( sDatasetPath, dSupport, sOutPutPath, sOutPutDetailPath );
        System.out.println( myFSG.dMinsup );

        ArrayList<GraphSet> aFreTwoGraph = myFSG.InitialPhase();
        myFSG.ButtonUpPhase( aFreTwoGraph );

        long lEnd = System.currentTimeMillis();

        int nFreSubGraphNum = myFSG.hFrequentSubgraphSet.size();
        System.out.println( "Total frequent sub graph number is: " + nFreSubGraphNum );

        myFSG.writeFrequentGraph( sOutPutPath, sOutPutDetailPath );
    }

}
