package Common;


import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by iejr on 2015/6/11.
 */
public class Permutation {

    public static void main( String[] args ){
        int nFi = 10;
        Permutation myPerTes = new Permutation();
        ArrayList<int[]> aFullPerRes = myPerTes.getFullPermutation( nFi );

    //    for( int[] iFullPer : aFullPerRes ) {
    //        for( int iEachPer : iFullPer )
    //            System.out.print( (iEachPer + 1) + "  ");
    //        System.out.println();
    //    }
        System.out.println( aFullPerRes.size() );
    }

    public ArrayList<int[]> getFullPermutation( int nLength ){
        LinkedList<Integer> lAllElement = new LinkedList<Integer>();
        for( int i = 0;i < nLength;i++ ){
            lAllElement.add(i);
        }
        return getFullPermutation(lAllElement, nLength);
    }

    private ArrayList<int[]> getFullPermutation( LinkedList<Integer> lAllElement,
                                                 int nOrgLength ){
        ArrayList<int[]> aFullPerRes = new ArrayList<int[]>();

        if( lAllElement.size() == 1 ){
            int[] iSubOneElement = new int[nOrgLength];
            iSubOneElement[0] = lAllElement.getFirst();
            aFullPerRes.add( iSubOneElement );
            return aFullPerRes;
        }

        for (int i = 0; i < lAllElement.size(); i++) {
            int nHold = 0;
            LinkedList<Integer> lSubElement = new LinkedList<Integer>();
            int j = 0;
            for( int element : lAllElement ){
                if( j==i ) {
                    nHold = element;
                    j++;
                    continue;
                }
                lSubElement.add( element );
                j++;
            }

            ArrayList<int[]> aSubPerRes = getFullPermutation(lSubElement, nOrgLength);
            int nFi = lAllElement.size();
            for (int[] iSubFullPer : aSubPerRes) {
                iSubFullPer[nFi - 1] = nHold;
            }
            aFullPerRes.addAll(aSubPerRes);
        }
        return aFullPerRes;
    }
}
