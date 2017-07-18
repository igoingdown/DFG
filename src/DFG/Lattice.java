package DFG;

import FSG.GraphSet;

import java.util.*;

/**
 * Created by iejr on 2015/7/6.
 */
public class Lattice {
    Random rSeed;
    protected HashSet<node> hHeader;
        //iejr: all node whose in-degrees are zero
    protected HashMap<Integer,node> hID2Node;
        //iejr: find node by id

    public Lattice(){
        hHeader = new HashSet<node>();
        hID2Node = new HashMap<Integer,node>();
        rSeed = new Random( System.currentTimeMillis() );
    }

    public Lattice( int nSeed ){
        hHeader = new HashSet<node>();
        hID2Node = new HashMap<Integer,node>();
        rSeed = new Random( nSeed );
    }

    public void addNode( int nNodeID ){
        if( !hID2Node.containsKey( nNodeID ) ){
            node N = new node( nNodeID );
            hID2Node.put( nNodeID, N );
        }
    }

    public void addNode( int nNodeID, int nFatherID ){
        node nChild = null;
        node nFather= null;

        if( hID2Node.containsKey( nNodeID ) ){
            nChild = hID2Node.get( nNodeID );
        }else{
            nChild = new node( nNodeID );
            hID2Node.put( nNodeID, nChild );
        }

        if( hID2Node.containsKey( nFatherID ) ){
            nFather = hID2Node.get( nFatherID );
        }else{
            nFather = new node( nFatherID );
            hID2Node.put( nFatherID, nFather );
        }

        if( nChild.hPrior == null ){
            nChild.hPrior = new HashSet<node>();
        }

        if( !nChild.hPrior.contains( nFather ) ){
            nChild.hPrior.add( nFather );
        }

        if( nFather.hNext == null ){
            nFather.hNext = new HashSet<node>();
        }

        if( !nFather.hNext.contains( nChild ) ){
            nFather.hNext.add( nChild );
        }
        /*
        if( hHeader.contains( nChild ) ){
            hHeader.remove( nChild );
        }

        if( nFather.hPrior == null && !hHeader.contains(nFather) ){
            hHeader.add( nFather );
        }
        */
    }


    protected void updateHeader(){

        this.hHeader = new HashSet<node>();

        for( int nNodeID : this.hID2Node.keySet() ){
            node nNode = this.hID2Node.get( nNodeID );

            if( nNode.hPrior == null || nNode.hPrior.size() == 0 ){
                this.hHeader.add( nNode );
            }
        }

    }

    public boolean setNodeLayer( int nNodeID, int nLayer ){
        if( this.hID2Node.containsKey( nNodeID ) ){
            node nNode = this.hID2Node.get( nNodeID );
            nNode.setNodeLayer( nLayer );
            return true;
        }else{
            return false;
        }
    }

    public HashSet<ArrayList<Integer>> PathConstruction(){

        HashSet<ArrayList<Integer>> hPathSet =
                new HashSet<ArrayList<Integer>>();
        this.updateHeader();

        while( this.hHeader.size() > 0 ){

            node nHeader = getHighLayerSmallOutDegree( this.hHeader );
            this.hHeader.remove( nHeader );
            ArrayList<Integer> aPath = new ArrayList<Integer>();
        //    aPath.add( nHeader.nNodeID );

            node nCurrent = nHeader;
            while( nCurrent != null ){
/*
                aPath.add( nCurrent.nNodeID );
                if( nCurrent.hPrior != null ) {
                    for (node N : nCurrent.hPrior) {
                        N.hNext.remove(nCurrent);
                        if (N.hNext.size() == 0) {
                            N.hNext = null;
                        }
                    }
                }

                node nNext = getHighLayerSmallInDegree( nCurrent.hNext );
                if( nCurrent.hNext != null ) {
                    for (node N : nCurrent.hNext) {
                        N.hPrior.remove(nCurrent);
                        if (N.hPrior.size() == 0 && N != nNext) {
                            N.hPrior = null;
                            this.hHeader.add(N);
                        }
                    }
                }
                */
                node nNext = getHighLayerSmallInDegree( nCurrent.hNext );
                aPath.add( nCurrent.nNodeID );
                nCurrent = nNext;
            }

            hPathSet.add( aPath );

            for( int i = 0;i < aPath.size();i++ ){
                int nID = aPath.get(i);
                node nNode = this.hID2Node.get( nID );
                this.deleteNode( nNode );
                if( nNode.hNext != null ){
                    int nNextID = -1;
                    if( i < aPath.size() - 1 ){
                        nNextID = aPath.get( i+1 );
                    }
                    for( node N : nNode.hNext ){
                        if( N.hPrior == null && N.nNodeID != nNextID ){
                            this.hHeader.add( N );
                        }
                    }
                }
            }

        }

        return hPathSet;
    }

    protected node getHighLayerSmallOutDegree( HashSet<node> hNodeSet ){

        if( hNodeSet == null || hNodeSet.size() == 0 ){
            return null;
        }

        Stack<node> sNodeStack = new Stack<node>();

        int nHighestLayer = -1;
        int nMinmumOutDegree = -1;
        for( node nNode : hNodeSet ){
            int nNextNum = -1;
            if( nNode.hNext == null || nNode.hNext.size() == 0 ){
                nNextNum = 0;
            }else{
                nNextNum = nNode.hNext.size();
            }

            if( nNode.nLayer > nHighestLayer ){
                sNodeStack.clear();
                sNodeStack.push( nNode );
                nHighestLayer = nNode.nLayer;
                nMinmumOutDegree = nNextNum;              //iejr: Error
            }else if( nNode.nLayer == nHighestLayer ){
                if( (nNextNum < nMinmumOutDegree) ||
                        (nMinmumOutDegree == -1) ){

                    sNodeStack.clear();
                    sNodeStack.push( nNode );
                    nMinmumOutDegree = nNextNum;
                }else if( nNextNum == nMinmumOutDegree ){
                    sNodeStack.push( nNode );
                }
            }

        }

        int nMax = sNodeStack.size();
        int nRandomSelect = rSeed.nextInt( nMax ) + 1;

        node nResult = null;
        for( int i = 0;i < nRandomSelect;i++ ){
            nResult = sNodeStack.pop();
        }

        return nResult;
    }


    protected node getHighLayerSmallInDegree( HashSet<node> hNodeSet ){

        if( hNodeSet == null || hNodeSet.size() == 0 ){
            return null;
        }

        Stack<node> sNodeStack = new Stack<node>();

        int nHighestLayer = -1;
        int nMinmumOutDegree = -1;
        for( node nNode : hNodeSet ){
            int nPriorNum = -1;
            if( nNode.hPrior == null || nNode.hPrior.size() == 0 ){
                nPriorNum = 0;
            }else{
                nPriorNum = nNode.hPrior.size();
            }

            if( nNode.nLayer > nHighestLayer ){
                sNodeStack.clear();
                sNodeStack.push( nNode );
                nHighestLayer = nNode.nLayer;
                nMinmumOutDegree = nPriorNum;
                    //iejr: Error!
            }else if( nNode.nLayer == nHighestLayer ){
                if( (nPriorNum < nMinmumOutDegree) ||
                        (nMinmumOutDegree == -1) ){
                    sNodeStack.clear();
                    sNodeStack.push( nNode );
                    nMinmumOutDegree = nPriorNum;
                }else if( nPriorNum == nMinmumOutDegree ){
                    sNodeStack.push( nNode );
                }
            }

        }

        int nMax = sNodeStack.size();
        int nRandomSelect = rSeed.nextInt( nMax ) + 1;

        node nResult = null;
        for( int i = 0;i < nRandomSelect;i++ ){
            nResult = sNodeStack.pop();
        }

        return nResult;
    }

    protected void deleteNode( node nNodeDel ){

        //iejr: get lowest child
        int nLowestLayer = -1;
        if( nNodeDel.hNext != null ) {
            for (node nChild : nNodeDel.hNext) {
                if (nLowestLayer == -1) {
                    nLowestLayer = nChild.nLayer;
                } else {
                    if (nChild.nLayer < nLowestLayer) {
                        nLowestLayer = nChild.nLayer;
                    }
                }
            }

            if( nNodeDel.hPrior != null ) {
                for (node nFather : nNodeDel.hPrior) {
                    HashSet<node> hChildSet = new HashSet<node>(nNodeDel.hNext);

                    Queue<Integer> qSearch = new LinkedList<Integer>();
                    qSearch.offer(nFather.nNodeID);
                    while (qSearch.size() > 0) {
                        int nNodeID = qSearch.remove();
                        node nNode = this.hID2Node.get(nNodeID);

                        if (nNode.hNext != null) {
                            for (node N : nNode.hNext) {
                                if (hChildSet.contains(N)) {
                                    //iejr: nFather is connected to N,
                                    //      where N is one of nNodeDel's
                                    //      children
                                    hChildSet.remove(N);
                                } else {
                                    if (N.nNodeID != nNodeDel.nNodeID &&
                                            N.nLayer >= nLowestLayer) {

                                        qSearch.offer(N.nNodeID);
                                    }
                                }
                            }
                        }
                    }

                    nFather.hNext.addAll(hChildSet);
                    for (node nChild : hChildSet) {
                        nChild.hPrior.add(nFather);
                    }
                }
            }

        }

        if( nNodeDel.hPrior != null ) {
            for (node nFather : nNodeDel.hPrior) {
                nFather.hNext.remove(nNodeDel);
                if(  nFather.hNext.size() == 0 ){
                    nFather.hNext = null;
                }
            }
        }

        if( nNodeDel.hNext != null ) {
            for (node nChild : nNodeDel.hNext) {
                nChild.hPrior.remove(nNodeDel);
                if( nChild.hPrior.size() == 0 ){
                    nChild.hPrior = null;
                //    this.hHeader.add( nChild );
                }
            }
        }
    }

}



class node{

    public HashSet<node> hPrior;
    public HashSet<node> hNext;
    public int nNodeID;
    public int nLayer;

    public node(){
        hPrior = null;
        hNext  = null;
        nNodeID = -1;
        nLayer  = -1;
    }

    public node( int nID ){
        nNodeID = nID;
        hPrior = null;
        hNext  = null;
        nLayer = -1;
    }

    public void setNodeLayer( int nLayer ){
        this.nLayer = nLayer;
    }

}