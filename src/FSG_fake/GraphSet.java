package FSG_fake;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by iejr on 2015/6/15.
 */
public class GraphSet {
    public LabelGraphList lGraph;
    public String sCanLabel;
    public HashSet<Integer> hTidList;
    public HashSet<String>  aSubCanLabel;

    public GraphSet(){
        this.lGraph = null;
        this.sCanLabel = null;
        this.hTidList = null;
        this.aSubCanLabel = null;
    }

    public GraphSet( LabelGraphList lGraph ){
        this.lGraph = lGraph;
        this.sCanLabel = null;
        this.hTidList = null;
        this.aSubCanLabel = null;
    }

    public void setCanLabel( String sLabel ){
        this.sCanLabel = sLabel;
    }
    public void addSubCanLabel( String sCanLabel ){
        if( this.aSubCanLabel == null ){
            this.aSubCanLabel = new HashSet<String>();
        }
        aSubCanLabel.add( sCanLabel );
    }

    public void setTidList( int nTid ){
        if( this.hTidList == null ){
            this.hTidList = new HashSet<Integer>();
        }

        this.hTidList.add( nTid );
    }

    public String getCanLabel(){
        if( this.lGraph != null ){
            LabelGraphMatrix lGraph2 = new LabelGraphMatrix( this.lGraph );
            return lGraph2.getCanonicalLabel();
        }else{
            return null;
        }
    }
}
