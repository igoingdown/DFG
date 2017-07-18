package FSG;

import Common.Parameter;
import Common.Permutation;
import Common.Sort;
import Common.SortNode;
import Jama.Matrix;

import java.util.*;

/**
 * Created by iejr on 2015/6/11.
 */
public class LabelGraphMatrix {

    int nSize;
    int nRank;
    ArrayList<LabelVertex> aVertex;
    Matrix mMat;

 //   HashMap< Integer, Double >[] hList;

    public static void main( String[] args ){

        LabelGraphList myLGL = new LabelGraphList( 10 );
        myLGL.setVertexLabel( 0, "C" );
        myLGL.setVertexLabel( 1, "C" );
        myLGL.setVertexLabel( 2, "C" );
        myLGL.setVertexLabel( 3, "C" );
        myLGL.setVertexLabel( 4, "C" );
        myLGL.setVertexLabel( 5, "C" );
        myLGL.setVertexLabel( 6, "C" );
        myLGL.setVertexLabel( 7, "C" );
        myLGL.setVertexLabel( 8, "C" );
        myLGL.setVertexLabel( 9, "C" );
    //    myLGL.setVertexLabel( 10, "C" );


        myLGL.setGraphEdge( 0,1,1 );
        myLGL.setGraphEdge( 1,2,1 );
        myLGL.setGraphEdge( 2,3,1 );
        myLGL.setGraphEdge( 3,4,1 );
        myLGL.setGraphEdge( 4,5,1 );
        myLGL.setGraphEdge( 5,6,1 );
        myLGL.setGraphEdge( 6,7,1 );
        myLGL.setGraphEdge( 7,8,1 );
        myLGL.setGraphEdge( 8,9,1 );
        myLGL.setGraphEdge( 0,9,1 );
    //    myLGL.setGraphEdge( 10,9,1 );
    //    myLGL.setGraphEdge( 10,0,1 );

        myLGL.print();

        LabelGraphMatrix myLGM = new LabelGraphMatrix( myLGL );
        myLGM.print();

        for( int i = 0;i < 100;i++ ) {
            String CL = myLGM.getCanonicalLabel();
            System.out.println(CL);
        }
    }

    public LabelGraphMatrix(){
        nSize = 0;
        nRank = 0;
        aVertex = null;
        mMat = null;
    }

    public LabelGraphMatrix( int nSize ){
        if( nSize <= 0 ){
            System.out.println( "Error:size must be a positive number" );
            System.exit(0);
        }
        this.nSize = nSize;
        this.nRank = 0;
        this.aVertex = new ArrayList<LabelVertex>();
        for( int i = 0;i < nSize;i++ ){
            aVertex.add( new LabelVertex( i, null ) );
        }
        mMat = new Matrix( nSize, nSize, 0 );
    }

    public LabelGraphMatrix( LabelGraphList lNeighborList ){
        this.nSize = lNeighborList.nSize;
        this.nRank = lNeighborList.nRank;
        this.aVertex = new ArrayList<LabelVertex>();
        for( LabelVertexList lVertex : lNeighborList.aVertex ){
            this.aVertex.add( new LabelVertex( lVertex.nVertexID, lVertex.sVertexLabel ) );
        }

        mMat = new Matrix( nSize, nSize, 0 );
        for( int i = 0;i < nSize;i++ ){
            HashMap<Integer,Double> hEdgeList = lNeighborList.aVertex.get(i).hNeighborList;
            for( int j : hEdgeList.keySet() ){
                this.mMat.set( i, j, hEdgeList.get( j ) );
            }
        }
    }

    public int getVertexDegree( int nVertexID ){

        if( nVertexID < 0 || nVertexID >= this.aVertex.size() ){
            return -1;
        }

        int nDegree = 0;
        for( int i = 0;i < this.aVertex.size();i++ ){
            if( this.mMat.get( nVertexID, i ) != 0 ){
                nDegree++;
            }
        }

        return nDegree;
    }


    public void setVertexLabel( int nIndex, String sLabel ){
        if( nIndex >= this.nSize ){
            System.out.println( "Error:index exceed label size" );
            System.exit(0);
        }
    //    this.aVertex.set( nIndex, this.aVertex.get(nIndex).setVertexLabel(sLabel) );
        this.aVertex.get(nIndex).setVertexLabel(sLabel);
    }

    public void setMatrixLabel( int m, int n, double lEdgeLabel ){
        if( m >= nSize || n >= nSize ){
            System.out.println( "Error:Line or Colomn exceeds range" );
            System.exit(0);
        }
        if( mMat==null ){
            System.out.println( "Error:Matrix is null" );
            System.exit(0);
        }
        mMat.set( m, n, lEdgeLabel );
        mMat.set( n, m, lEdgeLabel );
    }

    //iejr: Exchange several line and column respect to iTranArray
    //iejr: for example: origanal array is { 5,9,3,6,7 }
    //iejr:              final    array is { 9,7,5,6,3 }
    //iejr: then             iTranArray is { 1,4,0,3,2 }
    public LabelGraphMatrix transformMat( int[] iTranArray, boolean bReplace ){

        Matrix W = new Matrix( this.nSize, this.nSize, 0 );
        ArrayList<LabelVertex> aNewVertex = new ArrayList<>();
        for( int i = 0;i < this.nSize;i++ ){
            int j = iTranArray[i];
            W.set( j, i, 1 );
            aNewVertex.add( this.aVertex.get( j ) );
        }
    //    this.aVertex.clear();
    //    this.aVertex = aNewVertex;
        Matrix mTemp = W.transpose().times( mMat );
        W = mTemp.times( W );

        if( bReplace ){
            this.mMat = W;
            this.aVertex = aNewVertex;
            return null;
        }else {
            LabelGraphMatrix lTransMatrix = new LabelGraphMatrix();
            lTransMatrix.nRank = this.nRank;
            lTransMatrix.nSize = this.nSize;
            lTransMatrix.aVertex = aNewVertex;
            lTransMatrix.mMat = W;
            return lTransMatrix;
        }
    //    this.print();
    }

    public String getCode(){
        StringBuffer sCode = new StringBuffer();

        for( int i = 1;i < this.nSize;i++ ){
            for( int j = 0;j < i;j++ ){
                int nElement = (int)this.mMat.get( j,i );
                sCode.append( nElement );
            }
        }

        String sResult = sCode.toString();
        return sResult;
    }

    public String getCanonicalLabel(){
    //iejr: Vertex invariants
        //iejr: sort by each vertex's label
        SortNode[] sSortByVertexLabel = this.sortByVertexLabel();

            //iejr: transform matrix into the one has been permutationed
        int[] nSortPattern = new int[this.nSize];
        for( int i = 0;i < sSortByVertexLabel.length;i++ ){
            nSortPattern[i] = sSortByVertexLabel[i].nIndex;
        }
        transformMat( nSortPattern,true );
        /*
        Matrix W = new Matrix( this.nSize, this.nSize, 0 );
        ArrayList<LabelVertex> aNewVertex = new ArrayList<>();
        for( int i = 0;i < this.nSize;i++ ){
            int j = sSortByVertexLabel[i].nIndex;
            W.set( j, i, 1 );
            aNewVertex.add( this.aVertex.get( j ) );
        }
        this.aVertex.clear();
        this.aVertex = aNewVertex;
        this.mMat = W.transpose().times( mMat );
        this.mMat = mMat.times( W );

        this.print();
        */
            //iejr: get each partitioned start boundary
            //iejr: for example: sortelement[]  = { 1,1,1,2,2,3,3,3,3 }
            //iejr: then         aPartationList = { 0,    3,  5,      9}
        double dStartPattern = -1;
        ArrayList<Integer> aPartationList = new ArrayList<Integer>();
        for( int i = 0;i < sSortByVertexLabel.length;i++ ){
            double dSortElement = sSortByVertexLabel[i].dElement;
            if( dSortElement > dStartPattern ){
                dStartPattern = dSortElement;
                aPartationList.add( i );
            }
        }
        aPartationList.add( sSortByVertexLabel.length );

        //iejr: sort by each vertex's degree
        aPartationList = this.ItePartioning( aPartationList );

        //iejr: for circle
        if( this.nRank > 2 && aPartationList.size() == 2 ){
            int nSize = this.nSize;
            int nOneSize = nSize;
            int nZeroSize = (nSize*(nSize-1))/2 - nOneSize;

            String sCanLabel = "";
            for( int i = 0;i < nSize;i++ ){
                sCanLabel += this.aVertex.get(0).sVertexLabel;
            }
            for( int i = 0;i < nZeroSize;i++ ){
                sCanLabel += "0";
            }
            for( int i = 0;i < nOneSize;i++ ){
                sCanLabel += "1";
            }

            //    System.out.println( "Circle : " + sCanLabel );

            return sCanLabel;
        }

        //iejr: try all permutations for each partition to
        //      get the minimum of the code, which represents the canonical label
        ArrayList<ArrayList<int[]>> aPartitionPermutation =
                new ArrayList<ArrayList<int[]>>();
        int[] nPartElementNum = new int[aPartationList.size() - 1];
        for( int i = 0;i < aPartationList.size() - 1;i++ ){
            int nFullPerLen = aPartationList.get(i+1) - aPartationList.get(i);
            nPartElementNum[i] = nFullPerLen;
            Permutation pFullPer = new Permutation();
            aPartitionPermutation.add(pFullPer.getFullPermutation( nFullPerLen ));
        }
            //iejr: we use a stack to brute force all the combination among all partitions
        int nPartNum = aPartitionPermutation.size();
            //iejr: there are nPartNum partitions total
        int[] nPartSize = new int[nPartNum];
            //iejr: each partition has nPartSize[i] types of full permutations
        for( int i = 0;i < nPartNum;i++ ){
            nPartSize[i] = aPartitionPermutation.get(i).size();
        }
        Stack<int[]> sCombination = new Stack<int[]>();
        int[] nPartitionPointer = new int[nPartNum];
        for( int i = 0;i < nPartNum;i++ ){
            nPartitionPointer[i] = 0;
        }

        //iejr: for debug
        int nTrycount = 0;

        String sCanonicalLabel = "zzzzzzz";
        //iejr: define canonical label as the mininum code,
        // thus we set it a large value initially
        boolean bIsFinished = false;
    //    sCombination.push( aPartitionPermutation.get(0).get(0) );
        do{

            if( sCombination.size() == nPartNum ){
            //    int[] nSubPermutation = sCombination.pop();
            //    for( int i = 0;i < nPartSize[nPartNum - 1];i++ ){
            //        int nFillStart = aPartationList.get( nPartNum - 1 );
            //        nSortPattern[nFillStart + i] = nSubPermutation
            //    }
                sCombination.pop();
                LabelGraphMatrix lTest = transformMat( nSortPattern, false);
                String sCode = lTest.getCode();
                if( sCode.compareTo( sCanonicalLabel ) < 0 ){
                    sCanonicalLabel = sCode;
                    //iejr: for debug
                //    lTest.print();
                    //
                }

                //iejr: for debug
            //    System.out.println( "try count : " + ++nTrycount );
            }

            int nStackSize = sCombination.size();
            if( nPartitionPointer[nStackSize] < nPartSize[nStackSize] ){
                int[] nSubPermutation = aPartitionPermutation.get(nStackSize).get(
                        nPartitionPointer[nStackSize] );
                nPartitionPointer[nStackSize]++;
                sCombination.push( nSubPermutation );

                int nFillStart = aPartationList.get( nStackSize );
                for( int i = 0;i < nPartElementNum[nStackSize];i++ ){
                    nSortPattern[nFillStart + i] = nSubPermutation[i] + nFillStart;
                }
            }else{
                if( nStackSize == 0 ){
                    bIsFinished = true;
                    break;
                }
                nPartitionPointer[nStackSize] = 0;
                sCombination.pop();
            }
        }while( !bIsFinished );

        String sVertexLabel = "";
        for( int i = 0;i < this.aVertex.size();i++ ){
            LabelVertex lVertex = this.aVertex.get(i);
            sVertexLabel += lVertex.sVertexLabel;
        }
        sCanonicalLabel = sVertexLabel + sCanonicalLabel;
        return sCanonicalLabel;
    }

    public SortNode[] sortByVertexLabel(){
        double[] dVertexLabel = new double[this.nSize];
        for( int i = 0;i < this.aVertex.size();i++ ){
        //    int nLabel = Parameter.sVertexLabel.indexOf( this.aVertex.get(i).sVertexLabel );
            int nLabel = this.aVertex.get(i).sVertexLabel.hashCode();
            if( nLabel < 0 ){
                System.out.println("Error! no vertex label was matched:" +
                        this.aVertex.get(i).sVertexLabel);
                System.exit(0);
            }
            dVertexLabel[i] = nLabel;
        }

        Sort sByLabel = new Sort( dVertexLabel );
        SortNode[] sResult = sByLabel.FastSort();
        return sResult;
    }


    protected ArrayList<Integer> ItePartioning( ArrayList<Integer> aPartitionList ){
        //iejr: something wrong has happen
        if( aPartitionList == null || aPartitionList.size() < 2 ){
            return null;
        }

        //iejr: first we part each partition by vertex's degree
        ArrayList<Integer> aAttachedPartition = new ArrayList<Integer>();

        int[] nSortPattern = new int[this.nSize];
        for( int i = 0;i < aPartitionList.size() - 1;i++ ){

            ArrayList<Integer> aVerDegreePartition = new ArrayList<Integer>();

            int nStartBound = aPartitionList.get( i );
            int nEndBound   = aPartitionList.get( i + 1 );
            double[] dSortKey = new double[nEndBound - nStartBound];

            for( int j = nStartBound;j < nEndBound;j++ ){
                dSortKey[j-nStartBound] = this.getVertexDegree( j );
            }

            Sort sByDegree = new Sort( dSortKey );
            SortNode[] sResult = sByDegree.FastSort();
            //iejr: Initialize
            for( int j = 0;j < nSortPattern.length;j++ ){
                nSortPattern[j] = j;
            }

            for( int j = 0;j < dSortKey.length;j++ ){
                nSortPattern[j + nStartBound] = sResult[j].nIndex + nStartBound;
            }

            for( int j = 1;j < dSortKey.length;j++ ){
                if( sResult[j].dElement != sResult[j-1].dElement ){
                    aVerDegreePartition.add( j + nStartBound );
                }
            }

            this.transformMat( nSortPattern, true );

            //iejr: for debug
        //    this.print();
            //

            if(  /*aPartitionList.size() == 2*/ false  ) {

                aVerDegreePartition.add(0, nStartBound);
                aVerDegreePartition.add(nEndBound);

                //iejr: Iterative Partitioning
                do {
                    ArrayList<Integer> aIterativePartition = new ArrayList<Integer>();
                    for (int k = 0; k < aVerDegreePartition.size() - 1; k++) {
                        int nStartBoundIte = aVerDegreePartition.get(k);
                        int nEndBoundIte = aVerDegreePartition.get(k + 1);
                        if (nEndBoundIte - nStartBoundIte < 2) {
                            continue;
                        }

                        for (int l = nStartBoundIte + 1; l < nEndBoundIte; l++) {
                            int nResult = contrastVertex(l - 1, l, aVerDegreePartition);
                            if (nResult == 0) {
                                continue;
                            } else if (nResult > 0) {
                                int[] nSort = new int[this.nSize];
                                for (int m = 0; m < this.nSize; m++) {
                                    nSort[m] = m;
                                }
                                for (int m = l; m < nEndBoundIte; m++) {
                                    nSort[nStartBoundIte + m - l] = m;
                                }
                                for (int m = nStartBoundIte; m < l; m++) {
                                    nSort[nStartBoundIte + nEndBoundIte - l] = m;
                                }
                                this.transformMat(nSort, true);
                                aIterativePartition.add(nStartBoundIte + nEndBoundIte - l);
                                break;
                            } else {
                                aIterativePartition.add(l);
                                break;
                            }
                        }

                        if (aIterativePartition.size() > 0) {
                            break;
                        }
                    }

                    if (aIterativePartition.size() > 0) {
                        aVerDegreePartition.addAll(aIterativePartition);
                        Collections.sort(aVerDegreePartition);
                    } else {
                        break;
                    }
                } while (true);

                for (int j = 1; j < aVerDegreePartition.size() - 1; j++) {
                    aAttachedPartition.add(aVerDegreePartition.get(j));
                }

            } else {
                aAttachedPartition.addAll( aVerDegreePartition );
            }
        }

        aPartitionList.addAll( aAttachedPartition );
        Collections.sort(aPartitionList);

    //    this.transformMat( nSortPattern,true );

        //iejr: for debug
    //    this.print();

        return aPartitionList;
    }


    protected int contrastVertex( int nVerID1, int nVerID2, ArrayList<Integer> aPartition ){
        if( aPartition == null ){
            return 0;
        }

        int nStartBound = aPartition.get(0);
        int nEndBound   = aPartition.get( aPartition.size() - 1 );

        if( nVerID1 < nStartBound || nVerID1 > nEndBound ||
                nVerID2 < nStartBound || nVerID2 > nEndBound ){
            return 0;
        }

        int[] nNeighborNumber1 = new int[aPartition.size() - 1];
        int[] nNeighborNumber2 = new int[aPartition.size() - 1];

        for( int i = 0;i < aPartition.size() - 1;i++ ){
            nStartBound = aPartition.get( i );
            nEndBound   = aPartition.get( i+1 );

            for( int j = nStartBound;j < nEndBound;j++ ){
                if( this.mMat.get( nVerID1, j ) != 0 ){
                    nNeighborNumber1[i]++;
                }
                if( this.mMat.get( nVerID2, j ) != 0 ){
                    nNeighborNumber2[i]++;
                }
            }

            if( nNeighborNumber1[i] != nNeighborNumber2[i] ){
                return nNeighborNumber1[i] - nNeighborNumber2[i];
            }
        }

        return 0;
    }


    public void print() {
        for( LabelVertex lVertex : this.aVertex ){
            lVertex.print();
        }
        System.out.println("");
        if( mMat==null ){
            System.out.println( "Error:Matrix is null" );
            System.exit(0);
        }
        this.mMat.print(nSize, Parameter.nDoubleDesicion);
    }

}
