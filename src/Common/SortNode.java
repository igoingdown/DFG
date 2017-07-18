package Common;

/**
 * Created by iejr on 2015/6/11.
 */
public class SortNode {
    public double dElement;
    public int nIndex;

    public SortNode(){
        this.dElement = 0;
        this.nIndex =  0;
    }

    public SortNode( double dElement, int nIndex ){
        this.nIndex = nIndex;
        this.dElement = dElement;
    }
}
