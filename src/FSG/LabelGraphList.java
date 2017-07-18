package FSG;

import DiffFPM.LabelEdge;
import Jama.Matrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Queue;

/**
 * Created by iejr on 2015/6/11.
 * ��labelͼ���ڽӱ���
 */
public class LabelGraphList {
    int nSize;                              //iejr: vertex
    int nRank;                              //iejr: edge
    ArrayList<LabelVertexList> aVertex;

    public static void main( String[] args ){

    //    LabelGraphList.testIsomorphismLarge();
    //    LabelGraphList.testTraverse();
        LabelGraphList.testConnected();

    }

    public LabelGraphList(){
        this.nSize = 0;
        this.nRank = 0;
        aVertex = null;
    }

    public LabelGraphList( int nSize ){
        if( nSize <= 0 ){
            System.out.println( "Error:index exceed label size" );
            System.exit(0);
        }
        this.nSize = nSize;
        this.nRank = 0;
        aVertex = new ArrayList<LabelVertexList>();
        for( int i = 0;i < nSize;i++ ){
            aVertex.add( new LabelVertexList( i, null ) );
        }
    }

    public LabelGraphList( LabelGraphList lGraph ){
        if( lGraph != null ){
            this.nSize = lGraph.nSize;
            this.nRank = lGraph.nRank;
            this.aVertex = new ArrayList<LabelVertexList>();
            for( LabelVertexList lVerList : lGraph.aVertex ){
                LabelVertexList lVerListCopy = new LabelVertexList( lVerList );
                this.aVertex.add( lVerListCopy );
            }
        }
    }

    public int getSize(){ return this.nSize; }
    public int getRank(){ return this.nRank; }

    public void setVertexLabel( int nIndex, String sLabel ){
        if( nIndex >= this.nSize ){
            System.out.println( "Error:index exceed label size" );
            System.exit(0);
        }
        //    this.aVertex.set( nIndex, this.aVertex.get(nIndex).setVertexLabel(sLabel) );
        this.aVertex.get(nIndex).setVertexLabel(sLabel);
    }

    public void setVerUniID( int nIndex,int nUniID ){
        if( nIndex >= this.nSize ){
            System.out.println( "Error:index exceed label size" );
            System.exit(0);
        }

        this.aVertex.get(nIndex).setVerUniID( nUniID );
    }

    public void setGraphEdge( int nIndex, int nEnd, double dEdgeLabel ){
        if( nIndex >= this.nSize || nEnd >= this.nSize ){
            System.out.println( "Error:Line or Colomn exceeds range" );
            System.exit(0);
        }

        if( this.aVertex.get( nIndex ).setEdgeLabel( nEnd, dEdgeLabel ) &&
            this.aVertex.get( nEnd ).setEdgeLabel( nIndex, dEdgeLabel ) )  {
            this.nRank++;
        }
    }

    //iejr: only both two are sub one graph can this function effect
    public double getEdge( String sVerLabel1, String sVerLabel2 ){
        if( this.nSize != 2 ){
            return -1;
        }

        String sVerLabelOrg1 = this.aVertex.get(0).sVertexLabel;
        String sVerLabelOrg2 = this.aVertex.get(1).sVertexLabel;

        if( (sVerLabel1.equals( sVerLabelOrg1 ) &&
                sVerLabel2.equals( sVerLabelOrg2 )) ||
                (sVerLabel1.equals( sVerLabelOrg2 ) &&
                        sVerLabel2.equals( sVerLabelOrg1 )) ){
            return this.aVertex.get(0).hNeighborList.get(1);
        }else{
            return -1;
        }
    }

    public double getEdgeLabel( int nStart, int nEnd ){
        if( nStart >= this.nSize || nEnd >= this.nSize ){
            System.out.println( "Error:Line or Colomn exceeds range" );
        //    System.exit(0);
            return -1;
        }

        LabelVertexList lSourVerList = this.aVertex.get( nStart );
        if( !lSourVerList.hNeighborList.containsKey( nEnd ) ){
            return -1;
        }else {
            double dEdgeLabel = lSourVerList.hNeighborList.get(nEnd);
            return dEdgeLabel;
        }
    }

    //iejr: get a sub one graph by its canonical label
    public LabelGraphList getEdge(  int nStart, int nEnd ){
        if( nStart >= this.nSize || nEnd >= this.nSize ){
            System.out.println( "Error:Line or Colomn exceeds range" );
            System.exit(0);
        }

        LabelVertexList lSourVerList = this.aVertex.get( nStart );
        if( !lSourVerList.hNeighborList.containsKey( nEnd ) ){
            return null;
        }

        LabelVertex lDestVer = this.aVertex.get( nEnd );
        double dEdgeLabel = lSourVerList.hNeighborList.get( nEnd );
        LabelGraphList lSubGraph = new LabelGraphList( 2 );
        lSubGraph.setVertexLabel(0, lSourVerList.sVertexLabel);
        lSubGraph.setVertexLabel(1, lDestVer.sVertexLabel);
        lSubGraph.setGraphEdge( 0,1,dEdgeLabel );

        return lSubGraph;

    //    LabelGraphMatrix lSubGraphMatrix = new LabelGraphMatrix( lSubGraph );
    //    String sCanLabel = lSubGraphMatrix.getCanonicalLabel();
    //    return sCanLabel;
    }

    public double judgeEdge( int nStart, int nEnd ){
        if( nStart >= this.nSize || nEnd >= this.nSize ){
            System.out.println( "Error:Line or Colomn exceeds range" );
            System.exit(0);
        }

        LabelVertexList lVerList = this.aVertex.get( nStart );
        if( lVerList.hNeighborList.containsKey( nEnd ) ){
            return lVerList.hNeighborList.get( nEnd );
        }else{
            return -1;
        }
    }

    public HashSet<Double> judgeEdge( int nStart, String sEnd, HashSet<Integer> hFilter ){
        if( nStart >= this.nSize ){
            System.out.println( "Error:Line or Colomn exceeds range" );
            System.exit(0);
        }

        HashSet<Double> hEdgeLabel = new HashSet<Double>();
        LabelVertexList lSourVerList = this.aVertex.get(nStart);
        for( int nEnd : lSourVerList.hNeighborList.keySet() ){
            if( hFilter.contains( nEnd ) ){
                continue;
            }
            if( this.aVertex.get( nEnd ).sVertexLabel.equals( sEnd ) ){
                hEdgeLabel.add( lSourVerList.hNeighborList.get( nEnd ) );
            }
        }
        return hEdgeLabel;
    }

    public String getAllVertexLabel(){
        String sVerList = new String();
        for( LabelVertex lVertex : this.aVertex ){
            sVerList += lVertex.sVertexLabel + " ";
        }
        return sVerList;
    }

    public String[] getAllEdgeRecord(){
        String[] sEdgeList = new String[this.nRank];
        int nCount = 0;
        for( LabelVertexList lSourVerList : this.aVertex ){
            String sSourVerLabel = lSourVerList.sVertexLabel;
            int nSourVerID = lSourVerList.nVertexID;
            for( int nDestVerID : lSourVerList.hNeighborList.keySet() ){
                if( nDestVerID <= nSourVerID ){
                    continue;
                }

                String sDestVerLabel = this.aVertex.get( nDestVerID ).sVertexLabel;
                double dEdgeLabel = lSourVerList.hNeighborList.get( nDestVerID );

                String sRecord = new String();
                sRecord += nSourVerID + " " + nDestVerID + " " + dEdgeLabel;
                sEdgeList[nCount++] = sRecord;
            }
        }
        return sEdgeList;
    }

    public HashSet<String> getAllEdge(){
        HashSet<String> hCanLabel = new HashSet<String>();

        for( LabelVertexList lSourVerList : this.aVertex ){
            String sSourVerLabel = lSourVerList.sVertexLabel;
            int nSourVerID = lSourVerList.nVertexID;
            for( int nDestVerID : lSourVerList.hNeighborList.keySet() ){
                if( nDestVerID <= nSourVerID ){
                    continue;
                }

                String sDestVerLabel = this.aVertex.get( nDestVerID ).sVertexLabel;
                double dEdgeLabel = lSourVerList.hNeighborList.get( nDestVerID );
                LabelGraphList lSubGraphList = new LabelGraphList( 2 );
                lSubGraphList.setVertexLabel( 0, sSourVerLabel );
                lSubGraphList.setVertexLabel( 1, sDestVerLabel );
                lSubGraphList.setGraphEdge( 0,1,dEdgeLabel );

                LabelGraphMatrix lSubGraphMatrix = new LabelGraphMatrix( lSubGraphList );
                String sCanLabel = lSubGraphMatrix.getCanonicalLabel();
                hCanLabel.add(sCanLabel);
            }
        }

        return hCanLabel;
    }

    public void deleteGraphEdge( int nIndex, int nEnd, boolean bConnect ){
        if( nIndex >= this.nSize || nEnd >= this.nSize ){
            System.out.println( "Error:Line or Colomn exceeds range" );
            System.exit(0);
        }

        if( this.aVertex.get( nIndex ).deleteEdgeLabel( nEnd ) &&
            this.aVertex.get( nEnd ).deleteEdgeLabel( nIndex ) ){
            this.nRank--;
        }

        //iejr: if the graph isn't connect then we delete vertex such that it is connected
        int nDelIndex = -1;
        if( bConnect ){
            Iterator<LabelVertexList> iter = this.aVertex.iterator();
            int fi = 0;
            while(iter.hasNext()){
                LabelVertexList lVerList = iter.next();
                if( lVerList.hNeighborList.isEmpty() ){
                    iter.remove();
                    nDelIndex = fi;
                    this.nSize--;
                    break;
                }
                fi++;
            }
        }

        if( nDelIndex > -1 ) {
            for ( LabelVertexList lVerList : this.aVertex ){
                int nSourVerID = lVerList.nVertexID;
                if( nSourVerID > nDelIndex ){
                    lVerList.nVertexID--;
                }

                HashMap<Integer,Double> hNewNeiList = new HashMap<Integer,Double>();
                Iterator<Integer> iter = lVerList.hNeighborList.keySet().iterator();
                while(iter.hasNext()){
                    int nDestVerID = iter.next();
                    if( nDestVerID > nDelIndex ){
                        double dEdgeLabel = lVerList.hNeighborList.get( nDestVerID );
                        iter.remove();
                        hNewNeiList.put( nDestVerID - 1, dEdgeLabel );
                    }
                }
                lVerList.hNeighborList.putAll( hNewNeiList );
            }
        }
        /*
        if( nDelIndex > -1 ) {
            for ( LabelVertexList lVerList : this.aVertex ){
                int nSourVerID = lVerList.nVertexID;
                if( nSourVerID > nDelIndex ){
                    lVerList.nVertexID--;
                }
                for( int nDestVerID : lVerList.hNeighborList.keySet() ){
                    if( nDestVerID > nDelIndex ){
                        double dEdgeLabel = lVerList.hNeighborList.get( nDestVerID );
                        lVerList.hNeighborList.remove( nDestVerID );
                        lVerList.hNeighborList.put( nDestVerID - 1, dEdgeLabel );
                    }
                }
            }
        }
        */
    }

    public int addVertex( String sVerLabel ){
        LabelVertexList lNewVerList = new LabelVertexList( this.nSize, sVerLabel );
        this.aVertex.add( lNewVerList );
    //    this.nSize++;
        return nSize++;
    }

    public int getLocalVertexID( int nVerUniID ){
        int nLocalVerID = -1;
        for( int i = 0;i < this.aVertex.size();i++ ){
            if( this.aVertex.get(i).nVerUniID == nVerUniID ){
                nLocalVerID = i;
                break;
            }
        }
        return nLocalVerID;
    }

    public boolean judgeConnected(){
        if( this.nRank < this.nSize - 1 ){
            return false;
        }

        HashSet<Integer> hConnectedVerID = new HashSet<Integer>();
        Queue<LabelVertexList> qVerQueue = new LinkedList<LabelVertexList>();

        qVerQueue.offer( this.aVertex.get(0) );
        hConnectedVerID.add( this.aVertex.get(0).nVertexID );
        while( !qVerQueue.isEmpty() && hConnectedVerID.size() < this.nSize ){
            LabelVertexList lElement = qVerQueue.poll();
            for( int nNextVertex : lElement.hNeighborList.keySet() ){
                if( !hConnectedVerID.contains( nNextVertex ) ){
                    qVerQueue.offer( this.aVertex.get( nNextVertex ) );
                    hConnectedVerID.add( nNextVertex );
                }
            }
        }

        if( hConnectedVerID.size() == this.nSize ){
            return true;
        }else{
            return false;
        }
    }

    public ArrayList<Integer> traverseGraph(){

        HashMap<String,ArrayList<Integer>> hVerLabel2Count =
                new HashMap<String,ArrayList<Integer>>();

        for( LabelVertex lVertex : this.aVertex ){
            String sVerLabel = lVertex.sVertexLabel;
            ArrayList<Integer> aIndex = null;
            if( !hVerLabel2Count.containsKey( sVerLabel ) ){
                aIndex = new ArrayList<Integer>();
            }else{
                aIndex = hVerLabel2Count.get( sVerLabel );
            }
            aIndex.add( lVertex.nVertexID );
            hVerLabel2Count.put( sVerLabel, aIndex );
        }

        ArrayList<Integer> aFirstVertex = null;
        int nMinCount = -1;
        for( String sVerLabel : hVerLabel2Count.keySet() ){
            ArrayList<Integer> aVerList = hVerLabel2Count.get( sVerLabel );
            if( nMinCount == -1 ){
                aFirstVertex = aVerList;
                nMinCount = aFirstVertex.size();
            }

            if( aVerList.size() < nMinCount ){
                aFirstVertex = aVerList;
                nMinCount = aFirstVertex.size();
            }
        }

        Random rSeed = new Random( System.currentTimeMillis() );
        int nRandom = rSeed.nextInt( aFirstVertex.size() );
        int nFirstIndex = aFirstVertex.get( nRandom );

        HashSet<Integer> hVertexIDOccured = new HashSet<Integer>();
        ArrayList<Integer> aTraverseSequence = new ArrayList<Integer>();

        Queue<LabelVertexList> qTraverse = new LinkedList<LabelVertexList>();
        qTraverse.offer( this.aVertex.get( nFirstIndex ) );
        hVertexIDOccured.add( nFirstIndex );
        aTraverseSequence.add( nFirstIndex );

        while( qTraverse.size() > 0 ){
            LabelVertexList lVertexHeader = qTraverse.remove();

            for( int nDestID : lVertexHeader.hNeighborList.keySet() ){
                if( hVertexIDOccured.contains( nDestID ) ){
                    continue;
                }

                LabelVertexList lVertexDest = this.aVertex.get( nDestID );
                qTraverse.offer( lVertexDest );
                hVertexIDOccured.add( nDestID );
                aTraverseSequence.add( nDestID );
            }
        }

        return aTraverseSequence;
    }

    public LabelGraphList transform( ArrayList<Integer> aTransList ){
        if( aTransList.size() != this.nSize ){
            return null;
        }

        HashMap<Integer,Integer> hSourVerID2DestVerID = new HashMap<Integer,Integer>();

        LabelGraphList lNewGraph = new LabelGraphList( this.nSize );
        for( int i = 0;i < aTransList.size();i++ ){
            int nOrgVerID = aTransList.get( i );
            LabelVertex lOrgVertex = this.aVertex.get( nOrgVerID );
            int nUniVerID = lOrgVertex.nVerUniID;
            String sOrgLabel = this.aVertex.get( nOrgVerID ).sVertexLabel;
            lNewGraph.setVertexLabel( i, sOrgLabel );
            lNewGraph.setVerUniID( i, nUniVerID );
            hSourVerID2DestVerID.put( aTransList.get(i), i );
        }

        for( LabelVertexList lSourVerList : this.aVertex ){

            int nSourVerID = lSourVerList.nVertexID;
            for( int nDestVerID : lSourVerList.hNeighborList.keySet() ){
                if( nDestVerID < nSourVerID ){
                    continue;
                }

                double dEdgeLabel = lSourVerList.hNeighborList.get( nDestVerID );
                int nNewSourVerID = hSourVerID2DestVerID.get( nSourVerID );
                int nNewDestVerID = hSourVerID2DestVerID.get( nDestVerID );

                lNewGraph.setGraphEdge( nNewSourVerID, nNewDestVerID, dEdgeLabel );
            }

        }

    //    this.aVertex = lNewGraph.aVertex;
        return lNewGraph;
    }

    public HashMap<Integer,Integer> judgeIsomorphism( LabelGraphList lLargeGraph ){
        if( this.nRank > lLargeGraph.nRank ){
            return null;
        }

        //iejr: first, we scan lLargeGraph for the number
        //      how many vertex labels it has and
        //      how many number for each of them
        HashMap< String, ArrayList<Integer> > hVerIDMap =
                new HashMap< String, ArrayList<Integer> >();
        for( LabelVertex lVertex : lLargeGraph.aVertex ){
            ArrayList<Integer> aLargeVertex = null;
            if( hVerIDMap.containsKey( lVertex.sVertexLabel ) ){
                aLargeVertex = hVerIDMap.get( lVertex.sVertexLabel );
            }else{
                aLargeVertex = new ArrayList<Integer>();
            }
            aLargeVertex.add( lVertex.nVertexID + 1 );
            hVerIDMap.put( lVertex.sVertexLabel, aLargeVertex );
        }

        //iejr: if the vertex label to the subgraph is not cantained
        //      by the lLargeGraph, then it can't be isomophismed
        for( LabelVertex lVertex : this.aVertex ){
            if( !hVerIDMap.containsKey( lVertex.sVertexLabel ) ){
                return null;
            }
        }

        //iejr: we try all combinations to map this.vertex to
        //      lLargeGraph.vertex to find a matching
        String[] sVerLabel = new String[nSize];
        int[] nVerMapList = new int[this.nSize];
        HashMap< LabelVertexList, LabelVertexList > hVer2VerMap =
                new HashMap< LabelVertexList, LabelVertexList >();
        for( int i = 0;i < this.nSize;i++ ){
            nVerMapList[i] = -1;
            sVerLabel[i] = this.aVertex.get(i).sVertexLabel;
        }

        int nCurrentVertex = 0;
        int nTotalNegMatchCnt = 0;
    //    while( nVerMapList[0] < hVerIDMap.get( this.aVertex.get(0).sVertexLabel ).size() ){
        while( true ){
            if( nCurrentVertex == this.nSize ){
                nTotalNegMatchCnt++;
                //iejr: authenticate all edges mapping
                boolean bIsMatch = true;
                for( LabelVertexList lSubSourVerList : this.aVertex ){
                    LabelVertexList lLargeSourVerList = hVer2VerMap.get( lSubSourVerList );
                    for( int nSubDestVerID : lSubSourVerList.hNeighborList.keySet() ){
                        if( nSubDestVerID < lSubSourVerList.nVertexID ){
                            continue;
                        }
                        LabelVertexList lSubDestVerList = this.aVertex.get(nSubDestVerID);
                        LabelVertexList lLargeDestVerList = hVer2VerMap.get( lSubDestVerList );
                        int nLargeDestVerID = lLargeDestVerList.nVertexID;

                        if (lSubSourVerList.hNeighborList.get(nSubDestVerID) !=
                                lLargeSourVerList.getEdgeLabel(nLargeDestVerID)) {
                            bIsMatch = false;
                            break;
                        }
                    }
                    if( !bIsMatch ){
                        break;
                    }
                }
                if( bIsMatch ){
                    HashMap<Integer,Integer> hVerID2VerID = new HashMap<Integer,Integer>();
                    for( LabelVertexList lVertex : hVer2VerMap.keySet() ){
                        int nSourVerID = lVertex.nVertexID;
                        int nDestVerID = hVer2VerMap.get( lVertex ).nVertexID;
                        hVerID2VerID.put( nDestVerID, nSourVerID );
                    }
                //    System.out.println("Total try count: " + nTotalNegMatchCnt);
                    return hVerID2VerID;
                }else {
                    nCurrentVertex--;
                //    continue;
                }
            }

           if( nCurrentVertex == -1 ){
               System.out.println("Total try count: " + nTotalNegMatchCnt);
                return null;
           }

            String sCurVerLabel = sVerLabel[nCurrentVertex];
            ArrayList<Integer> aCurVerMapList = hVerIDMap.get( sCurVerLabel );
            int nMappedVertexID = 0;
            if( nVerMapList[nCurrentVertex] >= 0 ){
                nMappedVertexID = aCurVerMapList.get(nVerMapList[nCurrentVertex]);
                aCurVerMapList.set(nVerMapList[nCurrentVertex], nMappedVertexID*(-1));
            }
            for( int i = nVerMapList[nCurrentVertex]+1;i < aCurVerMapList.size();i++ ){
                if( aCurVerMapList.get(i) > 0 ){
                    //iejr: these represent for the vertex nCurVerMapList[i] is
                    //      the one has not been chosen
                    nVerMapList[nCurrentVertex] = i;
                    nMappedVertexID = aCurVerMapList.get(i);
                    aCurVerMapList.set( i, nMappedVertexID*-1 );
                    //iejr: Negative field means it is chosen

                    hVer2VerMap.put( this.aVertex.get(nCurrentVertex),
                            lLargeGraph.aVertex.get(nMappedVertexID - 1) );
                    break;
                }
            }
            if( nVerMapList[nCurrentVertex] < 0 ){
                //iejr: this vertex isn't mapped to any vertex,
                //      where lLargeGraph has not enough vertex for this label
                return null;
            }
            if( aCurVerMapList.get( nVerMapList[nCurrentVertex] ) < 0 ){
                //iejr: these represent for we get a new mapping,
                //      thus we can continue to map the next vertex
                nCurrentVertex++;
            }else{
                //iejr: current vertex exhausts the way mapping,
                //      thus we focus on its previous vertex
                nVerMapList[nCurrentVertex] = -1;
                nCurrentVertex--;
            }
        }

    }


    public HashMap<Integer,Integer> judgeIsomorphismPrun( LabelGraphList lLargeGraph ){
        if( this.nRank > lLargeGraph.nRank ){
            return null;
        }

        ArrayList<Integer> aTraverse = this.traverseGraph();
        LabelGraphList lNewGraph = this.transform(aTraverse);
        aTraverse = null;

        //iejr: first, we scan lLargeGraph for the number
        //      how many vertex labels it has and how many number for each of them
        HashMap< String, ArrayList<Integer> > hVerIDMap =
                new HashMap< String, ArrayList<Integer> >();
        for( LabelVertex lVertex : lLargeGraph.aVertex ){
            ArrayList<Integer> aLargeVertex = null;
            if( hVerIDMap.containsKey( lVertex.sVertexLabel ) ){
                aLargeVertex = hVerIDMap.get( lVertex.sVertexLabel );
            }else{
                aLargeVertex = new ArrayList<Integer>();
            }
            aLargeVertex.add( lVertex.nVertexID + 1 );
            hVerIDMap.put( lVertex.sVertexLabel, aLargeVertex );
        }

        //iejr: if the vertex label to the subgraph is not cantained by
        //      the lLargeGraph, then it can't be isomophismed
        for( LabelVertex lVertex : lNewGraph.aVertex ){
            if( !hVerIDMap.containsKey( lVertex.sVertexLabel ) ){
                return null;
            }
        }

        //iejr: we try all combinations to map this.vertex to
        //      lLargeGraph.vertex to find a matching
        String[] sVerLabel = new String[lNewGraph.nSize];
        int[] nVerMapList = new int[lNewGraph.nSize];
        HashMap< LabelVertexList, LabelVertexList > hVer2VerMap =
                new HashMap< LabelVertexList, LabelVertexList >();
        for( int i = 0;i < lNewGraph.nSize;i++ ){
            nVerMapList[i] = -1;
            sVerLabel[i] = lNewGraph.aVertex.get(i).sVertexLabel;
        }

        int nCurrentVertex = 0;
        int nTotalNegMatchCnt = 0;
        while( true ){
            if( nCurrentVertex == lNewGraph.nSize ){
                nTotalNegMatchCnt++;
                //iejr: authenticate all edges mapping
                boolean bIsMatch = true;
            //    for( LabelVertexList lSubSourVerList : this.aVertex ){
                    LabelVertexList lSubSourVerList =
                            lNewGraph.aVertex.get( lNewGraph.nSize - 1 );
                    LabelVertexList lLargeSourVerList = hVer2VerMap.get( lSubSourVerList );
                    for( int nSubDestVerID : lSubSourVerList.hNeighborList.keySet() ){
                    //    if( nSubDestVerID < lSubSourVerList.nVertexID ){
                    //        continue;
                    //    }
                        LabelVertexList lSubDestVerList = lNewGraph.aVertex.get(nSubDestVerID);
                        LabelVertexList lLargeDestVerList = hVer2VerMap.get( lSubDestVerList );
                        int nLargeDestVerID = lLargeDestVerList.nVertexID;

                        if (lSubSourVerList.hNeighborList.get(nSubDestVerID) !=
                                lLargeSourVerList.getEdgeLabel(nLargeDestVerID)) {
                            bIsMatch = false;
                            break;
                        }
                    }
                //    if( !bIsMatch ){
                //        break;
                //    }
            //    }
                if( bIsMatch ){
                    HashMap<Integer,Integer> hVerID2VerID = new HashMap<Integer,Integer>();
                    for( LabelVertexList lVertex : hVer2VerMap.keySet() ){
                        int nSourVerID = lVertex.nVertexID;
                        int nDestVerID = hVer2VerMap.get( lVertex ).nVertexID;
                        hVerID2VerID.put( nDestVerID, nSourVerID );
                    }
                    //    System.out.println("Total try count: " + nTotalNegMatchCnt);
                    return hVerID2VerID;
                }else {
                    nCurrentVertex--;
                    //    continue;
                }
            }

            if( nCurrentVertex == -1 ){
            //    System.out.println("Total try count: " + nTotalNegMatchCnt);
                return null;
            }

            String sCurVerLabel = sVerLabel[nCurrentVertex];
            ArrayList<Integer> aCurVerMapList = hVerIDMap.get( sCurVerLabel );
            int nMappedVertexID = 0;
            if( nVerMapList[nCurrentVertex] >= 0 ){
                nMappedVertexID = aCurVerMapList.get(nVerMapList[nCurrentVertex]);
                aCurVerMapList.set(nVerMapList[nCurrentVertex], nMappedVertexID*(-1));
            }
            for( int i = nVerMapList[nCurrentVertex]+1;i < aCurVerMapList.size();i++ ){
                if( aCurVerMapList.get(i) > 0 ){
                    //iejr: these represent for the vertex nCurVerMapList[i]
                    //      is the one has not been chosen

                    LabelVertexList lSubSourVerList = lNewGraph.aVertex.get( nCurrentVertex );
                    LabelVertexList lLargeSourVerList = lLargeGraph.aVertex.get( aCurVerMapList.get(i)-1 );
                    boolean bIsMatch = true;
                    for( int nSubDestVerID : lSubSourVerList.hNeighborList.keySet() ){
                        if( nSubDestVerID > nCurrentVertex ){
                            continue;
                        }
                        LabelVertexList lSubDestVerList = lNewGraph.aVertex.get(nSubDestVerID);
                        LabelVertexList lLargeDestVerList = hVer2VerMap.get( lSubDestVerList );
                        int nLargeDestVerID = lLargeDestVerList.nVertexID;
                        if (lSubSourVerList.hNeighborList.get(nSubDestVerID) !=
                                lLargeSourVerList.getEdgeLabel(nLargeDestVerID)) {
                            bIsMatch = false;
                            break;
                        }
                    }

                    if( bIsMatch ) {
                        nVerMapList[nCurrentVertex] = i;
                        nMappedVertexID = aCurVerMapList.get(i);
                        aCurVerMapList.set(i, nMappedVertexID * -1);
                        //iejr: Negative field means it is chosen

                        hVer2VerMap.put(lNewGraph.aVertex.get(nCurrentVertex),
                                lLargeGraph.aVertex.get(nMappedVertexID - 1));
                        break;
                    }
                    else{
                        continue;
                    }
                }
            }
            if( nVerMapList[nCurrentVertex] < 0 ){
                //iejr: this vertex isn't mapped to any vertex,
                //      where lLargeGraph has not enough vertex for this label
                nCurrentVertex--;
                continue;
            }
            if( aCurVerMapList.get( nVerMapList[nCurrentVertex] ) < 0 ){
                //iejr: these represent for we get a new mapping,
                //      thus we can continue to map the next vertex
                nCurrentVertex++;
            }else{
                //iejr: current vertex exhausts the way mapping,
                //      thus we focus on its previous vertex
                nVerMapList[nCurrentVertex] = -1;
                nCurrentVertex--;
            }
        }

    }

    public HashMap<Integer,Integer> judgeIsomorphismUllmann( LabelGraphList lLargeGraph ){

        if( this.nRank > lLargeGraph.nRank ){
            return null;
        }

        int nTotolTryCount = 0;

        //iejr: preprocessing
        int nSubSize = this.nSize;
        int nLargeSize = lLargeGraph.nSize;

        boolean bMask[] = new boolean[nLargeSize];
        for( int i = 0;i < bMask.length;i++ ){
            bMask[i] = true;
        }
        int nSubIndex = 0;
        int nLargeIndex = 0;

        int nMatrixIndex[] = new int[nSubSize];
        for( int i = 0;i < nMatrixIndex.length;i++ ){
            nMatrixIndex[i] = -1;
        }

        //iejr: construct matrix
        LabelGraphMatrix lMatrixSub = new LabelGraphMatrix(this);
        LabelGraphMatrix lMatrixLarge = new LabelGraphMatrix( lLargeGraph );
        Matrix mTransform = new Matrix( nSubSize, nLargeSize, 0 );

        //iejr: neighbor set
        ArrayList<HashMap<String,Double>> aSubNeighbor =
                new ArrayList<HashMap<String,Double>>();
        ArrayList<HashMap<String,Double>> aLargeNeighbor =
                new ArrayList<HashMap<String,Double>>();

        for( LabelVertexList lSourVer : this.aVertex ){
            HashMap<String,Double> hNeighbor = new HashMap<String,Double>();
            for( int nDestVerID : lSourVer.hNeighborList.keySet() ){
                String sVerLabel = this.aVertex.get( nDestVerID ).sVertexLabel;
                double dEdgeLabel = lSourVer.hNeighborList.get( nDestVerID );
                if( hNeighbor.containsKey(sVerLabel) ){
                    double dSupport = hNeighbor.get( sVerLabel ) + 1;
                    hNeighbor.put( sVerLabel, dSupport );
                }else{
                    hNeighbor.put( sVerLabel, 1.0 );
                }
            }
            aSubNeighbor.add( hNeighbor );
        }

        for( LabelVertexList lSourVer : lLargeGraph.aVertex ){
            HashMap<String,Double> hNeighbor = new HashMap<String,Double>();
            for( int nDestVerID : lSourVer.hNeighborList.keySet() ){
                String sVerLabel = lLargeGraph.aVertex.get( nDestVerID ).sVertexLabel;
                double dEdgeLabel = lSourVer.hNeighborList.get( nDestVerID );
                if( hNeighbor.containsKey( sVerLabel ) ){
                    double dSupport = hNeighbor.get( sVerLabel ) + 1;
                    hNeighbor.put( sVerLabel, dSupport );
                }else{
                    hNeighbor.put( sVerLabel, 1.0 );
                }
            }
            aLargeNeighbor.add( hNeighbor );
        }

        //
        while(true){
            if( nSubIndex == this.nSize ){
                nTotolTryCount++;
                //iejr: verify edge set
                Matrix mTemp = mTransform.times( lMatrixLarge.mMat );
                mTemp = mTemp.transpose();
                mTemp = mTransform.times( mTemp );

            //    mTemp.print( nSubSize, Parameter.nDoubleDesicion );

                boolean bIsPrune = false;
                for( int i = 0;i < nSubSize;i++ ){
                //    for( int j = i;j < nSubSize;j++ ){
                        if( mTemp.get( i,nSubIndex-1 ) !=
                                lMatrixSub.mMat.get( i,nSubIndex-1 ) ){
                            bIsPrune = true;
                            break;
                        }
                //    }
                //    if( bIsPrune ){
                //        break;
                //    }
                }

                if( bIsPrune ){
                    nSubIndex--;
                //    nLargeIndex = nMatrixIndex[nSubIndex];
                //    if( nLargeIndex > -1 ){
                //        nMatrixIndex[nSubIndex] = -1;
                    //    mTransform.set( nSubIndex, nLargeIndex, 0 );
                    //    bMask[nLargeIndex] = true;
                    //    nSubIndex--;
                //    }
                }else{
                    HashMap<Integer,Integer> hVer2Ver = new HashMap<Integer,Integer>();
                    for( int i = 0;i < nSubSize;i++ ){
                        hVer2Ver.put( nMatrixIndex[i],i );
                    }
                //    System.out.println( "New Method try count: " + nTotolTryCount );
                    return hVer2Ver;
                }
            }

            if( nSubIndex < 0 ){
            //    System.out.println( "New Method try count: " + nTotolTryCount );
                return null;
            }

        //    mTransform.print( nLargeSize, Parameter.nDoubleDesicion);

            boolean bIsFind = false;
            int nStartFind = 0;
            nLargeIndex = nMatrixIndex[nSubIndex];
            if( nLargeIndex > -1 ){
                nStartFind = nMatrixIndex[nSubIndex] + 1;
                bMask[nLargeIndex] = true;
                mTransform.set( nSubIndex,nLargeIndex,0 );
            }
            for( int i = nStartFind;i < bMask.length;i++ ){
                if(     bMask[i] &&
                        this.aVertex.get( nSubIndex ).sVertexLabel.equals(
                                lLargeGraph.aVertex.get( i ).sVertexLabel )
                        ){

                    boolean bIsPrune = false;
                    nLargeIndex = i;
                    HashMap<String,Double> hSubNeighbor = aSubNeighbor.get( nSubIndex );
                    HashMap<String,Double> hLargeNeighbor = aLargeNeighbor.get( nLargeIndex );
                    for( String sLabel : hSubNeighbor.keySet() ){
                        if( hLargeNeighbor.containsKey( sLabel ) ){
                            if( hLargeNeighbor.get( sLabel ) < hSubNeighbor.get( sLabel ) ){
                                bIsPrune = true;
                                break;
                            }
                        }else{
                            bIsPrune = true;
                            break;
                        }
                    }

                    if( !bIsPrune ) {
                        mTransform.set(nSubIndex, nLargeIndex, 1);

                        Matrix mTemp = mTransform.times( lMatrixLarge.mMat );
                        mTemp = mTemp.transpose();
                        mTemp = mTransform.times( mTemp );
                        bIsPrune = false;
                        for( int j = 0;j < nSubIndex + 1;j++ ){
                        //    for( int k = j;k < nSubIndex;k++ ){
                                if( mTemp.get( j,nSubIndex ) !=
                                        lMatrixSub.mMat.get( j,nSubIndex ) ){
                                    bIsPrune = true;
                                    break;
                                }
                        //    }
                        //    if( bIsPrune ){
                        //        break;
                        //    }
                        }

                        if( bIsPrune ){
                            mTransform.set( nSubIndex, nLargeIndex, 0 );
                            continue;
                        }
                        nMatrixIndex[nSubIndex] = nLargeIndex;
                        bMask[nLargeIndex] = false;
                        bIsFind = true;
                        break;
                    }
                }
            }

            if( bIsFind ){
                nSubIndex++;
            }else{
                nLargeIndex = nMatrixIndex[nSubIndex];
                if( nLargeIndex > -1 ){
                    nMatrixIndex[nSubIndex] = -1;
            //        mTransform.set( nSubIndex, nLargeIndex, 0 );
            //        bMask[nLargeIndex] = true;

                }
                nSubIndex--;
            }
        }
    }

    public HashSet<int[]> judgeIsomorphismPrunWhole( LabelGraphList lLargeGraph ){
        if( this.nRank > lLargeGraph.nRank ){
            return null;
        }

        ArrayList<Integer> aTraverse = this.traverseGraph();
        LabelGraphList lNewGraph = this.transform(aTraverse);
        aTraverse = null;

        //iejr: first, we scan lLargeGraph for the number
        //      how many vertex labels it has and how many number for each of them
        HashMap< String, ArrayList<Integer> > hVerIDMap =
                new HashMap< String, ArrayList<Integer> >();
        for( LabelVertex lVertex : lLargeGraph.aVertex ){
            ArrayList<Integer> aLargeVertex = null;
            if( hVerIDMap.containsKey( lVertex.sVertexLabel ) ){
                aLargeVertex = hVerIDMap.get( lVertex.sVertexLabel );
            }else{
                aLargeVertex = new ArrayList<Integer>();
            }
            aLargeVertex.add( lVertex.nVertexID + 1 );
            hVerIDMap.put( lVertex.sVertexLabel, aLargeVertex );
        }

        //iejr: if the vertex label to the subgraph is not cantained
        //      by the lLargeGraph, then it can't be isomophismed
        for( LabelVertex lVertex : lNewGraph.aVertex ){
            if( !hVerIDMap.containsKey( lVertex.sVertexLabel ) ){
                return null;
            }
        }

        //iejr: we try all combinations to map this.vertex to
        //      lLargeGraph.vertex to find a matching
        String[] sVerLabel = new String[lNewGraph.nSize];
        int[] nVerMapList = new int[lNewGraph.nSize];
        HashMap< LabelVertexList, LabelVertexList > hVer2VerMap =
                new HashMap< LabelVertexList, LabelVertexList >();
        for( int i = 0;i < lNewGraph.nSize;i++ ){
            nVerMapList[i] = -1;
            sVerLabel[i] = lNewGraph.aVertex.get(i).sVertexLabel;
        }

        HashSet<int[]> hMapResult = new HashSet<int[]>();

        int nCurrentVertex = 0;
        int nTotalNegMatchCnt = 0;
        while( true ){

            //iejr: for debug
        //    for( int i = 0;i < nVerMapList.length;i++ )
        //        System.out.print( nVerMapList[i] + " " );
        //    System.out.println();
        //    try {
        //        Thread.sleep(50);
        //    } catch ( InterruptedException e ){
        //        e.printStackTrace();
        //    }
            //

            if( nCurrentVertex == lNewGraph.nSize ){
                nTotalNegMatchCnt++;
                //iejr: for debug
            //    System.out.println( "Match count: " + nTotalNegMatchCnt );
                //
                //iejr: authenticate all edges mapping
                boolean bIsMatch = true;
                //    for( LabelVertexList lSubSourVerList : this.aVertex ){
                LabelVertexList lSubSourVerList = lNewGraph.aVertex.get( lNewGraph.nSize - 1 );
                LabelVertexList lLargeSourVerList = hVer2VerMap.get( lSubSourVerList );
                for( int nSubDestVerID : lSubSourVerList.hNeighborList.keySet() ){
                    //    if( nSubDestVerID < lSubSourVerList.nVertexID ){
                    //        continue;
                    //    }
                    LabelVertexList lSubDestVerList = lNewGraph.aVertex.get(nSubDestVerID);
                    LabelVertexList lLargeDestVerList = hVer2VerMap.get( lSubDestVerList );
                    int nLargeDestVerID = lLargeDestVerList.nVertexID;

                    if (lSubSourVerList.hNeighborList.get(nSubDestVerID) !=
                            lLargeSourVerList.getEdgeLabel(nLargeDestVerID)) {
                        bIsMatch = false;
                        break;
                    }
                }
                //    if( !bIsMatch ){
                //        break;
                //    }
                //    }
                if( bIsMatch ){
                //    HashMap<Integer,Integer> hVerID2VerID = new HashMap<Integer,Integer>();
                    int[] nVerID2VerID = new int[lNewGraph.nSize];
                    for( LabelVertexList lVertex : hVer2VerMap.keySet() ){
                        int nSourVerID = lVertex.nVertexID;
                        int nDestVerID = hVer2VerMap.get( lVertex ).nVertexID;
                    //    hVerID2VerID.put( nDestVerID, nSourVerID );
                        nVerID2VerID[nSourVerID] = nDestVerID;
                    }
                    hMapResult.add( nVerID2VerID );
                    //    System.out.println("Total try count: " + nTotalNegMatchCnt);
                //    return hVerID2VerID;
                    nCurrentVertex--;
                }else {
                    nCurrentVertex--;
                    //    continue;
                }
            }

            if( nCurrentVertex == -1 ){
                //    System.out.println("Total try count: " + nTotalNegMatchCnt);
                return hMapResult;
            }

            String sCurVerLabel = sVerLabel[nCurrentVertex];
            ArrayList<Integer> aCurVerMapList = hVerIDMap.get( sCurVerLabel );
            int nMappedVertexID = 0;
            if( nVerMapList[nCurrentVertex] >= 0 ){
                nMappedVertexID = aCurVerMapList.get(nVerMapList[nCurrentVertex]);
                aCurVerMapList.set(nVerMapList[nCurrentVertex], nMappedVertexID*(-1));
            }
            for( int i = nVerMapList[nCurrentVertex]+1;i < aCurVerMapList.size();i++ ){
                if( aCurVerMapList.get(i) > 0 ){
                    //iejr: these represent for the vertex nCurVerMapList[i]
                    //      is the one has not been chosen

                    LabelVertexList lSubSourVerList = lNewGraph.aVertex.get( nCurrentVertex );
                    LabelVertexList lLargeSourVerList =
                            lLargeGraph.aVertex.get( aCurVerMapList.get(i)-1 );
                    boolean bIsMatch = true;
                    for( int nSubDestVerID : lSubSourVerList.hNeighborList.keySet() ){
                        if( nSubDestVerID > nCurrentVertex ){
                            continue;
                        }
                        LabelVertexList lSubDestVerList = lNewGraph.aVertex.get(nSubDestVerID);
                        LabelVertexList lLargeDestVerList = hVer2VerMap.get( lSubDestVerList );
                        int nLargeDestVerID = lLargeDestVerList.nVertexID;
                        if (lSubSourVerList.hNeighborList.get(nSubDestVerID) !=
                                 lLargeSourVerList.getEdgeLabel(nLargeDestVerID)) {
                            bIsMatch = false;
                            break;
                        }
                    }

                    if( bIsMatch ) {
                        nVerMapList[nCurrentVertex] = i;
                        nMappedVertexID = aCurVerMapList.get(i);
                        aCurVerMapList.set(i, nMappedVertexID * -1);
                        //iejr: Negative field means it is chosen

                        hVer2VerMap.put(lNewGraph.aVertex.get(nCurrentVertex),
                                lLargeGraph.aVertex.get(nMappedVertexID - 1));
                        break;
                    }
                    else{
                        continue;
                    }
                }
            }
            if( nVerMapList[nCurrentVertex] < 0 ){
                //iejr: this vertex isn't mapped to any vertex,
                //      where lLargeGraph has not enough vertex for this label
                nCurrentVertex--;
                continue;
            }
            if( aCurVerMapList.get( nVerMapList[nCurrentVertex] ) < 0 ){
                //iejr: these represent for we get a new mapping,
                //      thus we can continue to map the next vertex
                nCurrentVertex++;
            }else{
                //iejr: current vertex exhausts the way mapping,
                //      thus we focus on its previous vertex
                nVerMapList[nCurrentVertex] = -1;
                nCurrentVertex--;
            }
        }

    }

    public HashSet<GraphSet> getSubNeighbors( HashSet<String> hFilters,
                                              HashSet<String> hOccursInFilters ){
    //    HashMap<String,GraphSet> hCanLabel2Graph = new HashMap<String,GraphSet>();

        HashSet<GraphSet> hGraphSet = new HashSet<GraphSet>();
        HashSet<String> hCanLabelSet = new HashSet<String>();
        if( this.nSize == 2 ){
            return hGraphSet;
        }

        LabelGraphList lOrgGraph = this;
        for( LabelVertexList lSourVerList : lOrgGraph.aVertex ){

            int nSourVerID = lSourVerList.nVertexID;
            for( int nDestVerID : lSourVerList.hNeighborList.keySet() ){
                if( nDestVerID < nSourVerID ){
                    continue;
                }
                LabelGraphList lGraphCopy = new LabelGraphList( lOrgGraph );
                lGraphCopy.deleteGraphEdge( nSourVerID, nDestVerID, true );

                LabelGraphMatrix lGraphMatrix = new LabelGraphMatrix( lGraphCopy );
                String sCanLabel = lGraphMatrix.getCanonicalLabel();

                if( hFilters != null && hFilters.contains( sCanLabel ) ){
                    hOccursInFilters.add( sCanLabel );
                    continue;
                }

                if( !lGraphCopy.judgeConnected() ){
                    continue;
                }

                if( !hCanLabelSet.contains(sCanLabel) ){
                    GraphSet gNewGraph = new GraphSet( lGraphCopy );
                    gNewGraph.setCanLabel( sCanLabel );
                    hCanLabelSet.add( sCanLabel );
                    hGraphSet.add( gNewGraph );
                }
            }
        }
        return hGraphSet;
    }

    public HashMap<GraphSet, LabelEdge> getSuperBackNeighbors(
            HashMap<String,GraphSet> hCanLabel2Graph,
            HashSet<String> hFilters, HashSet<String> hOccursInFilters
    ){
    //    HashMap<String,GraphSet> hCanLabel2SuperGraph = new HashMap<String,GraphSet>();

        HashMap<GraphSet, LabelEdge> hGraph2Edge = new HashMap<GraphSet,LabelEdge>();
        HashSet<String> hCanLabel = new HashSet<String>();

        LabelGraphList lOrgGraph = this;
        for( LabelVertexList lSourVerList : lOrgGraph.aVertex ){

            int nSourVerID = lSourVerList.nVertexID;
            for( int i = nSourVerID + 1;i < lOrgGraph.nSize;i++ ){
                int nDestVerID = i;
                if( lSourVerList.hNeighborList.containsKey( nDestVerID ) ){
                    continue;
                }

                for( GraphSet gGraph : hCanLabel2Graph.values() ) {
                    String sLabel1 = lSourVerList.sVertexLabel;
                    String sLabel2 = this.aVertex.get(nDestVerID).sVertexLabel;
                    double dEdgeLabel = -1;
                    if( (dEdgeLabel=gGraph.lGraph.getEdge( sLabel1,sLabel2 )) == -1 ){
                        continue;
                    }

                    LabelGraphList lGraphCopy = new LabelGraphList(lOrgGraph);
                    lGraphCopy.setGraphEdge(nSourVerID, nDestVerID, dEdgeLabel);

                    LabelGraphMatrix lGraphMatrix = new LabelGraphMatrix(lGraphCopy);
                    String sCanLabel = lGraphMatrix.getCanonicalLabel();
                //    if (!hCanLabel2SuperGraph.containsKey(sCanLabel)) {
                //        GraphSet gNewGraph = new GraphSet(lGraphCopy);
                //        gNewGraph.setCanLabel(sCanLabel);
                //        hCanLabel2SuperGraph.put(sCanLabel, gNewGraph);
                //    }
                    if( hFilters != null && hFilters.contains( sCanLabel ) ){
                        hOccursInFilters.add( sCanLabel );
                        continue;
                    }

                    if( !hCanLabel.contains( sCanLabel ) ){
                        hCanLabel.add( sCanLabel );

                        LabelEdge lNewEdge = new LabelEdge();
                        lNewEdge.addVertexLabel( nSourVerID, sLabel1 );
                        lNewEdge.addVertexLabel( nDestVerID, sLabel2 );
                        lNewEdge.setEdgeLabel( dEdgeLabel );

                        GraphSet gNewGraph = new GraphSet(lGraphCopy);
                        gNewGraph.setCanLabel(sCanLabel);
                        hGraph2Edge.put( gNewGraph, lNewEdge );
                    }
                }
            }
        }
        return hGraph2Edge;
    }

    public HashMap<GraphSet, LabelEdge> getSuperForwardNeighbors(
            HashSet<String> hVertexLabelSet, HashMap<String,GraphSet> hCanLabel2Graph,
            HashSet<String> hFilters, HashSet<String> hOccursInFilters
    ){
    //    HashMap<String,GraphSet> hCanLabel2SuperGraph = new HashMap<String,GraphSet>();

        HashMap<GraphSet, LabelEdge> hGraph2Edge = new HashMap<GraphSet,LabelEdge>();
        HashSet<String> hCanLabel = new HashSet<String>();

        LabelGraphList lOrgGraph = this;
        for( LabelVertexList lSourVerList : lOrgGraph.aVertex ){
            int nSourVerID = lSourVerList.nVertexID;

            for( String sVerLabel : hVertexLabelSet ) {
                for ( GraphSet gGraph : hCanLabel2Graph.values() ) {
                    String sLabel1 = lSourVerList.sVertexLabel;
                    String sLabel2 = sVerLabel;
                    double dEdgeLabel = -1;
                    if( (dEdgeLabel = gGraph.lGraph.getEdge( sLabel1,sLabel2 )) == -1 ){
                        continue;
                    }

                    LabelGraphList lGraphCopy = new LabelGraphList(lOrgGraph);
                    int nDestVerID = lGraphCopy.addVertex( sVerLabel );
                    lGraphCopy.setGraphEdge(nSourVerID, nDestVerID, dEdgeLabel);

                    LabelGraphMatrix lGraphMatrix = new LabelGraphMatrix(lGraphCopy);
                    String sCanLabel = lGraphMatrix.getCanonicalLabel();
                    //if (!hCanLabel2SuperGraph.containsKey(sCanLabel)) {
                    //    GraphSet gNewGraph = new GraphSet(lGraphCopy);
                    //    gNewGraph.setCanLabel(sCanLabel);
                    //    hCanLabel2SuperGraph.put(sCanLabel, gNewGraph);
                    //}
                    if( hFilters != null && hFilters.contains( sCanLabel ) ){
                        hOccursInFilters.add( sCanLabel );
                        continue;
                    }

                    if( !hCanLabel.contains( sCanLabel ) ){
                        hCanLabel.add( sCanLabel );

                        LabelEdge lNewEdge = new LabelEdge();
                        lNewEdge.addVertexLabel( nSourVerID, sLabel1 );
                        lNewEdge.addVertexLabel( nDestVerID, sLabel2 );
                        lNewEdge.setEdgeLabel( dEdgeLabel );

                        GraphSet gNewGraph = new GraphSet(lGraphCopy);
                        gNewGraph.setCanLabel(sCanLabel);
                        hGraph2Edge.put( gNewGraph, lNewEdge );
                    }
                }
            }
        }
        return hGraph2Edge;
    }



    public void print(){
        for( LabelVertexList lVertexList : this.aVertex ){
            lVertexList.print();
            System.out.println( "" );
        }
    }

    public static void testIsomorphismSmall(){
        LabelGraphList lSubG = new LabelGraphList( 2 );
        lSubG.setVertexLabel( 0,"C" );
        lSubG.setVertexLabel( 1,"H" );
    //    lSubG.setVertexLabel( 2,"C" );
    //    lSubG.setVertexLabel( 3,"H" );
        //    lSubG.setVertexLabel( 4,"C" );
        lSubG.setGraphEdge( 0,1,1 );
    //    lSubG.setGraphEdge( 1,2,1 );
    //    lSubG.setGraphEdge( 2,3,1 );
        lSubG.print();

        LabelGraphList lLargeG = new LabelGraphList( 8 );
        lLargeG.setVertexLabel( 0,"C" );
        lLargeG.setVertexLabel( 1,"C" );
        lLargeG.setVertexLabel( 2,"C" );
        lLargeG.setVertexLabel( 3,"Cl" );
        lLargeG.setVertexLabel( 4,"H" );
        lLargeG.setVertexLabel( 5,"H" );
        lLargeG.setVertexLabel( 6,"O" );
        lLargeG.setVertexLabel( 7,"S" );
        lLargeG.setGraphEdge( 0,3,1 );
        lLargeG.setGraphEdge( 0,1,2 );
        lLargeG.setGraphEdge( 0,4,1 );
        lLargeG.setGraphEdge( 1,2,1 );
        lLargeG.setGraphEdge( 1,5,1 );
        lLargeG.setGraphEdge( 2,6,2 );
        lLargeG.setGraphEdge( 2,7,1 );

        lLargeG.print();

    //    HashMap<Integer,Integer> hMap = lSubG.judgeIsomorphismPrun(lLargeG);
    //    System.out.println( hMap );

        HashSet<int[]> hMap = lSubG.judgeIsomorphismPrunWhole( lLargeG );
        for( int[] nMap : hMap ){
            for( int i = 0;i < nMap.length;i++ )
                System.out.print( nMap[i] );
            System.out.println();
        }
    }

    public static void testIsomorphismLarge(){
        String sFileName = "D:\\Programs\\Java\\DPGRAPH\\Dataset\\EfficiencyTest";
        LabelGraphList lLargeGraph = null;
        try {
            BufferedReader r = new BufferedReader(new FileReader(sFileName));
            String sLine;
            String sEndPattern = "EOF";
            int nVertexUniqueID = 0;
            while( (sLine = r.readLine()) != null ){
                sLine = sLine.trim();

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

                while( !(sLine = r.readLine()).contains( sEndPattern ) ){
                    String[] sEdgeInfo = sLine.trim().split( " " );
                    int nExecuteLine = Integer.parseInt(sEdgeInfo[0]) - 1;
                    int nExecuteColumn = Integer.parseInt(sEdgeInfo[1]) - 1;
                    double dEdgeLabel  = Integer.parseInt(sEdgeInfo[2]);
                    lGraph.setGraphEdge(nExecuteLine, nExecuteColumn, dEdgeLabel);
                    //    lGraph.setGraphEdge( nExecuteColumn, nExecuteLine, dEdgeLabel );
                    lLargeGraph = lGraph;
                }
            }

            r.close();
        }catch ( FileNotFoundException e){
            e.printStackTrace();
        }catch ( IOException e){
            e.printStackTrace();
        }

        lLargeGraph.print();

        LabelGraphList lSubG= new LabelGraphList( 7 );
        lSubG.setVertexLabel( 0, "C" );
        lSubG.setVertexLabel( 1, "C" );
        lSubG.setVertexLabel( 2, "C" );
        lSubG.setVertexLabel( 3, "C");
        lSubG.setVertexLabel( 4, "H");
        lSubG.setVertexLabel( 5, "N");
        lSubG.setVertexLabel( 6, "N");
        lSubG.setGraphEdge(2, 3, 1);
        lSubG.setGraphEdge(0, 5, 1);
        lSubG.setGraphEdge(0, 6, 1);
        lSubG.setGraphEdge(1, 5, 1);
        lSubG.setGraphEdge( 1,6,1 );
        lSubG.setGraphEdge( 3,6,1 );
        lSubG.setGraphEdge( 4,6,1 );
    //    lSubG.setGraphEdge( 4,3,1 );
        lSubG.print();
        LabelGraphMatrix lMatrix = new LabelGraphMatrix( lSubG );
        String sLabel = lMatrix.getCanonicalLabel();
        System.out.println( sLabel );


    //    HashMap<Integer,Integer> hMap = lSubG.judgeIsomorphism( lLargeGraph );
    //    System.out.println( hMap );
    //    HashMap<Integer,Integer> hMap = lSubG.judgeIsomorphismPrun(lLargeGraph);
    //    System.out.println( hMap );
        HashSet<int[]> hMap = lSubG.judgeIsomorphismPrunWhole( lLargeGraph );
        for( int[] nMap : hMap ){
            for( int i = 0;i < nMap.length;i++ )
                System.out.print( nMap[i] + " " );
            System.out.println();
        }
    }

    public static void testDeleteEdge(){
        LabelGraphList lSubG = new LabelGraphList( 4 );
        lSubG.setVertexLabel( 0,"Cl" );
        lSubG.setVertexLabel( 1,"Cl" );
        lSubG.setVertexLabel( 2,"H" );
        lSubG.setVertexLabel( 3,"H" );
        lSubG.setGraphEdge( 0,1,1 );
        lSubG.setGraphEdge( 0,2,1 );
        lSubG.setGraphEdge( 0,3,1 );
        lSubG.print();

        lSubG.deleteGraphEdge( 0,1,true );
        lSubG.print();
        lSubG.deleteGraphEdge( 0,2,true );
        lSubG.print();
    }

    public static void testConnected(){
        LabelGraphList lSubG = new LabelGraphList( 7 );
        lSubG.setVertexLabel( 0, "C" );
        lSubG.setVertexLabel( 1, "C" );
        lSubG.setVertexLabel( 2, "C" );
        lSubG.setVertexLabel( 3, "C");
        lSubG.setVertexLabel( 4, "H");
        lSubG.setVertexLabel( 5, "N");
        lSubG.setVertexLabel( 6, "N");
        lSubG.setGraphEdge(2, 3, 1);
        lSubG.setGraphEdge(0, 5, 1);
        lSubG.setGraphEdge(0, 6, 1);
        lSubG.setGraphEdge(1, 5, 1);
        lSubG.setGraphEdge( 1, 6, 1 );
        lSubG.setGraphEdge( 3, 6, 1 );
        lSubG.setGraphEdge( 4, 6, 1 );
        lSubG.print();

        boolean bConnected = lSubG.judgeConnected();
        System.out.println( bConnected );
    }

    public static void testTraverse(){
        LabelGraphList lSubG= new LabelGraphList( 7 );
        lSubG.setVertexLabel( 0, "C" );
        lSubG.setVertexLabel( 1, "C" );
        lSubG.setVertexLabel( 2, "C" );
        lSubG.setVertexLabel( 3, "C");
        lSubG.setVertexLabel( 4, "H");
        lSubG.setVertexLabel( 5, "N");
        lSubG.setVertexLabel( 6, "N");
        lSubG.setGraphEdge( 2, 3, 1 );
        lSubG.setGraphEdge( 0, 5, 1 );
        lSubG.setGraphEdge( 0, 6, 1 );
        lSubG.setGraphEdge( 1, 5, 1 );
        lSubG.setGraphEdge( 1, 6, 1 );
        lSubG.setGraphEdge( 3, 6, 1 );
        lSubG.setGraphEdge( 4, 6, 1 );
        lSubG.setVerUniID( 0,1000 );
        lSubG.setVerUniID( 1,1001 );
        lSubG.setVerUniID( 2,1002 );
        lSubG.setVerUniID( 3,1003 );
        lSubG.setVerUniID( 4,1004 );
        lSubG.setVerUniID( 5,1005 );
        lSubG.setVerUniID( 6,1006 );

        ArrayList<Integer> aTraverse = lSubG.traverseGraph();
        System.out.println( aTraverse );

        LabelGraphMatrix lMatrix = new LabelGraphMatrix( lSubG );
        String sCanLabel = lMatrix.getCanonicalLabel();
        System.out.println( "Original Canonical Label : " + sCanLabel );
        System.out.println( "Detail : " );
        lSubG.print();

        LabelGraphList lNew = lSubG.transform( aTraverse );
        LabelGraphMatrix lMatrixTransformed = new LabelGraphMatrix( lNew );
        sCanLabel = lMatrixTransformed.getCanonicalLabel();
        System.out.println( "Transformed Canonical Label : " + sCanLabel );
        System.out.println( "Detail : " );
        lNew.print();
    }
}
