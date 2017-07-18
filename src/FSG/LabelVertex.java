package FSG;

/**
 * Created by iejr on 2015/6/11.
 */
public class LabelVertex {
    public int nVertexID;        //iejr: it is local vertex id for the domain in the Graph
    public int nVerUniID;        //iejr: it is global vertex id for the program
    public String sVertexLabel;

    public LabelVertex(){
        this.nVertexID = -1;
        this.nVerUniID = -1;
        this.sVertexLabel = "";
    }

    public LabelVertex( int nID, String sLabel ){
        this.nVertexID = nID;
        this.nVerUniID = -1;
        this.sVertexLabel = sLabel;
    }

    public void setVertexLabel( String sLabel ){
        this.sVertexLabel = sLabel;
    }

    public void setVertexID( int nID ){
        this.nVertexID = nID;
    }

    public void setVerUniID( int nUniID ){ this.nVerUniID = nUniID; }

    public void print(){
        System.out.print( this.nVerUniID + "\t" + this.nVertexID + " " +
                this.sVertexLabel + ":  " );
    }
}
