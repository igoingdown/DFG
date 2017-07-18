package FSG_fake;

import Common.Parameter;
import Common.Permutation;
import Common.Sort;
import Common.SortNode;
import Jama.Matrix;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by iejr on 2015/6/11.
 * ��labelͼ���ڽӾ�����
 */
public class LabelGraphMatrix {

    int nSize;
    int nRank;
    ArrayList<LabelVertex> aVertex;
    Matrix mMat;

 //   HashMap< Integer, Double >[] hList;

    public static void main( String[] args ){

        LabelGraphList myLGL = new LabelGraphList( 4 );
        myLGL.setVertexLabel( 0, "C" );
        myLGL.setVertexLabel( 1, "H" );
        myLGL.setVertexLabel( 2, "C" );
        myLGL.setVertexLabel( 3, "None" );

        myLGL.setGraphEdge( 0,1,1 );
        myLGL.setGraphEdge( 1,2,1 );
        myLGL.setGraphEdge( 0,2,1 );
        myLGL.setGraphEdge( 2,3,1 );

        myLGL.print();

        LabelGraphMatrix myLGM = new LabelGraphMatrix( myLGL );
        myLGM.print();
        String CL = myLGM.getCanonicalLabel();
        System.out.println( CL );
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

    //iejr: ���ڽӱ��������ڽӾ���
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

    //iejr:�����ͼ��canonical��ǩ
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

        //iejr: try all permutations for each partition to get the minimum of the code, which represents the canonical label
        ArrayList<ArrayList<int[]>> aPartitionPermutation = new ArrayList<ArrayList<int[]>>();
        int[] nPartElementNum = new int[aPartationList.size() - 1];
        for( int i = 0;i < aPartationList.size() - 1;i++ ){
            int nFullPerLen = aPartationList.get(i+1) - aPartationList.get(i);
            nPartElementNum[i] = nFullPerLen;
            Permutation pSort = new Permutation();
            aPartitionPermutation.add(pSort.getFullPermutation( nFullPerLen ));
        }
            //iejr: we use a stack to brute force all the combination among all partitions
        int nPartNum = aPartitionPermutation.size();                //iejr: there are nPartNum partitions total
        int[] nPartSize = new int[nPartNum];                        //iejr: each partition has nPartSize[i] types of full permutations
        for( int i = 0;i < nPartNum;i++ ){
            nPartSize[i] = aPartitionPermutation.get(i).size();
        }
        Stack<int[]> sCombination = new Stack<int[]>();
        int[] nPartitionPointer = new int[nPartNum];
        for( int i = 0;i < nPartNum;i++ ){
            nPartitionPointer[i] = 0;
        }

        String sCanonicalLabel = "zzzzzzz";                         //iejr: define canonical label as the mininum code, thus we set it a large value initially
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
            }

            int nStackSize = sCombination.size();
            if( nPartitionPointer[nStackSize] < nPartSize[nStackSize] ){
                int[] nSubPermutation = aPartitionPermutation.get(nStackSize).get( nPartitionPointer[nStackSize] );
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
                System.out.println("Error! no vertex label was matched:" + this.aVertex.get(i).sVertexLabel);
                System.exit(0);
            }
            dVertexLabel[i] = nLabel;
        }

        Sort sByLabel = new Sort( dVertexLabel );
        SortNode[] sResult = sByLabel.FastSort();
        return sResult;
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
