package DiffFPM;

import FSG.LabelVertex;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by iejr on 2015/6/24.
 */
public class LabelEdge {
//    public HashSet<String> hVertexLabel;
    public ArrayList<LabelVertex> aVertex;
    public double dEdgeLabel;

    public LabelEdge(){
        aVertex = null;
        dEdgeLabel = -1;
    }

    public LabelEdge( String sVerLabel ){
        aVertex = new ArrayList<LabelVertex>();
        aVertex.add( new LabelVertex(0, sVerLabel) );
        dEdgeLabel = -1;
    }

    public void addVertexLabel( int nVerID, String sVerLabel ){
        if( aVertex == null ){
            aVertex = new ArrayList<LabelVertex>();
        }

        if( aVertex.size() >= 2 ){
            return;
        }

        int nSize = aVertex.size();
        aVertex.add( new LabelVertex( nVerID, sVerLabel ) );
    }

    public void addVertexLabel( String sVerLabel ){
    //    if( this.hVertexLabel == null ){
    //        this.hVertexLabel = new HashSet<String>();
    //    }
    //    hVertexLabel.add( sVerLabel );
        if( aVertex == null ){
            aVertex = new ArrayList<LabelVertex>();
        }

        if( aVertex.size() >= 2 ){
            return;
        }

        int nSize = aVertex.size();
        aVertex.add( new LabelVertex( nSize - 1, sVerLabel ) );
    }

    public void setEdgeLabel( double dEdgeLabel ){
        this.dEdgeLabel = dEdgeLabel;
    }

    public boolean setVertex( int nID, int nNewID ){
        for( LabelVertex lVertex : aVertex ){
            if( lVertex.nVertexID == nID ){
                lVertex.nVertexID = nNewID;
                return true;
            }
        }
        return false;
    }
}
