package Common;

/**
 * Created by iejr on 2015/6/11.
 */
public class Sort {
    public SortNode[] sSortResult;

    public static void main( String[] args ){
        double[] sSortElement = { 49,14,38,74,96,65,8,49,55,27 };
        Sort mySort = new Sort( sSortElement );
        mySort.FastSort();
        mySort.printResult();
    }

    public Sort(){
        sSortResult = null;
    }

    public Sort( double[] dSortKey ){
        this.sSortResult = new SortNode[dSortKey.length];
        for( int i = 0;i < dSortKey.length;i++ ){
            sSortResult[i] = new SortNode();
            sSortResult[i].dElement = dSortKey[i];
            sSortResult[i].nIndex   = i;
        }
    }

    public SortNode[] FastSort(){
        this.FastSort( 0, this.sSortResult.length - 1 );
        return this.sSortResult;
    }

    private void FastSort( int nStart, int nEnd ){

        if( nStart < nEnd ) {
            int nHold = this.Partition(nStart, nEnd);
            this.FastSort(nStart, nHold - 1);
            this.FastSort(nHold + 1, nEnd);
        }
    }

    private int Partition( int nLow, int nHigh ){
        double dHold = this.sSortResult[nLow].dElement;
        int nIndex   = this.sSortResult[nLow].nIndex;

        while( nLow < nHigh ){
            while( (nLow < nHigh) && (this.sSortResult[nHigh].dElement >= dHold) ){ nHigh--;}
            if( nLow < nHigh ){
                this.sSortResult[nLow].dElement = this.sSortResult[nHigh].dElement;
                this.sSortResult[nLow].nIndex   = this.sSortResult[nHigh].nIndex;
                nLow++;
            }
            while( (nLow < nHigh) && (this.sSortResult[nLow].dElement <= dHold) ){ nLow++; }
            if( nLow < nHigh ){
                this.sSortResult[nHigh].dElement = this.sSortResult[nLow].dElement;
                this.sSortResult[nHigh].nIndex   = this.sSortResult[nLow].nIndex;
                nHigh--;
            }
        }
        this.sSortResult[nLow].dElement = dHold;
        this.sSortResult[nLow].nIndex   = nIndex;
        return nLow;
    }

    public void printResult(){
        for( int i = 0;i < this.sSortResult.length;i++ ) {
            System.out.print( this.sSortResult[i].dElement + "  " );
        }
        System.out.println("");
        for( int i = 0;i < this.sSortResult.length;i++ ) {
            System.out.print( this.sSortResult[i].nIndex + "  " );
        }
        System.out.println("");
    }
}
