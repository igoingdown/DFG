package FSG_fake;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by iejr on 2015/6/15.
 */
public class Automorphisms {
    LabelGraphList lGraph;
    HashMap<String, ArrayList<Integer>> hVerLabel2ID;
    HashMap<Integer, ArrayList<Integer>> hVerUniID2Automor;
    HashMap<Integer, String> hVerUniID2VerLabel;

    public static void main(String[] args){

        LabelGraphList lGraph = new LabelGraphList( 3 );
        lGraph.setVertexLabel( 0,"C" );
        lGraph.setVertexLabel( 1,"H" );
        lGraph.setVertexLabel( 2,"N" );
        lGraph.setVerUniID( 0,0 );
        lGraph.setVerUniID( 1,1 );
        lGraph.setVerUniID( 2,2 );
        lGraph.setGraphEdge( 0,1,1 );
        lGraph.setGraphEdge( 1,2,1 );
        lGraph.setGraphEdge( 0,2,1 );
        lGraph.print();

        Automorphisms myAutomor = new Automorphisms( lGraph );
        ArrayList<Integer> aAutomor = myAutomor.getAutomor( 0 );
        System.out.println( aAutomor );
    }

    public Automorphisms(){
        this.lGraph = null;
        this.hVerLabel2ID = null;
        this.hVerUniID2Automor = null;
        this.hVerUniID2VerLabel = null;
    }

    public Automorphisms( LabelGraphList lGraph ){
        this.lGraph = new LabelGraphList(lGraph);
        this.hVerLabel2ID = new HashMap<String,ArrayList<Integer>>();
        this.hVerUniID2VerLabel = new HashMap<Integer,String>();

        for( int i = 0;i < lGraph.nSize;i++ ){
            int nVertexID =  lGraph.aVertex.get(i).nVertexID;
            int nVerUniID =  lGraph.aVertex.get(i).nVerUniID;
            String sVerLabel = lGraph.aVertex.get(i).sVertexLabel;

            ArrayList<Integer> aVerIDList = null;
            if( this.hVerLabel2ID.containsKey( sVerLabel ) ){
                aVerIDList = this.hVerLabel2ID.get( sVerLabel );
            }else{
                aVerIDList = new ArrayList<Integer>();
            }
            aVerIDList.add( nVertexID );
            this.hVerLabel2ID.put( sVerLabel, aVerIDList );
            this.hVerUniID2VerLabel.put( nVerUniID, sVerLabel );
        }

        this.hVerUniID2Automor = new HashMap<Integer,ArrayList<Integer>>();
    }

    public ArrayList<Integer> getAutomor( int nVerUniID ){
        if( !this.hVerUniID2VerLabel.containsKey( nVerUniID ) ){
            return null;
        }

        if( this.hVerUniID2Automor.containsKey( nVerUniID ) ){
            return hVerUniID2Automor.get( nVerUniID );
        }

        String sVerLabel = this.hVerUniID2VerLabel.get( nVerUniID );
    //    if( !this.hVerLabel2ID.containsKey( sVerLabel ) ){
    //        return null;
    //    }

        ArrayList<Integer> aVerIDList = this.hVerLabel2ID.get( sVerLabel );
        String sOrgCanLabel = null;
        int nNewVerID = this.lGraph.addVertex( "None" );

        for( int nFocusVerID : aVerIDList ){
            this.lGraph.setGraphEdge( nFocusVerID, nNewVerID, 1 );
        //    this.lGraph.print();
            LabelGraphMatrix lGraphM = new LabelGraphMatrix(this.lGraph);
            String sCanLabel = lGraphM.getCanonicalLabel();

            boolean bIsAdd = false;
            if( sOrgCanLabel == null ){
                sOrgCanLabel = sCanLabel;
                bIsAdd = true;
            }else{
                if( sCanLabel.equals(sOrgCanLabel) ){
                    bIsAdd = true;
                }
            }

            if( bIsAdd ){
                ArrayList<Integer> aAutomorList = null;
                if( this.hVerUniID2Automor.containsKey( nVerUniID ) ){
                    aAutomorList = this.hVerUniID2Automor.get( nVerUniID );
                }else{
                    aAutomorList = new ArrayList<Integer>();
                }
                aAutomorList.add( nFocusVerID );
                this.hVerUniID2Automor.put( nVerUniID, aAutomorList );
            }
            this.lGraph.deleteGraphEdge( nFocusVerID, nNewVerID, false );
        }

        return this.hVerUniID2Automor.get( nVerUniID );
    }
}
