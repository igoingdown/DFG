package FSG_fake;

import java.util.HashMap;

/**
 * Created by iejr on 2015/6/11.
 */
public class LabelVertexList extends LabelVertex {
    public HashMap<Integer, Double> hNeighborList;

    public LabelVertexList(){
        super();
        hNeighborList = null;
    }

    public LabelVertexList( int nID, String sLabel ){
        super( nID, sLabel );
        hNeighborList = new HashMap< Integer, Double >();
    }

    public LabelVertexList( LabelVertexList lVerList ){
        if( lVerList != null ){
            this.nVertexID = lVerList.nVertexID;
            this.nVerUniID = lVerList.nVerUniID;
            this.sVertexLabel = lVerList.sVertexLabel;
            this.hNeighborList = new HashMap<Integer, Double>();
            for( int nDestVerID : lVerList.hNeighborList.keySet() ){
                double dEdgeLabel = lVerList.hNeighborList.get( nDestVerID );
                this.hNeighborList.put( nDestVerID, dEdgeLabel );
            }
        }
    }

    public void setVertexLabel( String sLabel ){
        super.setVertexLabel( sLabel );
    }

    public boolean setEdgeLabel( int nEnd, Double dLabel ){
        if( !this.hNeighborList.containsKey( nEnd ) ){
            this.hNeighborList.put( nEnd, dLabel );
            return true;
        }
        return false;
    }

    public boolean deleteEdgeLabel( int nEnd ){
        if( this.hNeighborList.containsKey( nEnd ) ){
            this.hNeighborList.remove( nEnd );
            return true;
        }
        return false;
    }

    public double getEdgeLabel( int nDestVerListID ){
        if( this.hNeighborList.containsKey(nDestVerListID) ){
            return this.hNeighborList.get( nDestVerListID );
        }else{
            return -1;
        }
    }

    public void print(){
        super.print();
        for( int nVertex : this.hNeighborList.keySet() ){
            System.out.print(nVertex + "(" + this.hNeighborList.get(nVertex) + ")\t");
        }
    }
}
