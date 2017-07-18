package FSG;

import Dataset.DatasetParameter;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by iejr on 2015/7/7.
 */
public class Convertion {

    public static void main( String[] args ){

        String sDatasetName = "FDA-single";
        String sFileInput = DatasetParameter.sDataResultPrefix + sDatasetName
                + "\\DiffFPM\\result.txt";
        String sFileOutput = DatasetParameter.sDataResultPrefix + sDatasetName
                + "\\DiffFPM\\filter.txt";

        Convertion.CanLabel2GraphList( sFileInput, sFileOutput );

    }

    public static void CanLabel2GraphList( String sInput, String sOutput ){

        try {
            BufferedReader r = new BufferedReader(new FileReader( sInput ));
            FileWriter w = new FileWriter( sOutput, true);

            String sLine = null;
            while( (sLine = r.readLine()) != null ){

                sLine = sLine.trim();
                System.out.println( sLine );

                String[] sIndex = new String[2];
                int nSplitIndex = -1;
                for( int i = 0;i < sLine.length();i++ ){
                    if( Character.isDigit( sLine.charAt( i ) ) ){
                        nSplitIndex = i;
                        break;
                    }
                }

                sIndex[0] = sLine.substring( 0, nSplitIndex );
                sIndex[1] = sLine.substring( nSplitIndex, sLine.length() - 1 );
                int nSize = sIndex[0].length();

                for( int i = 0;i < sIndex[0].length();i++ ){
                    w.write( sIndex[0].charAt(i) + " " );
                    System.out.print( sIndex[0].charAt(i) + " " );
                }
                w.write( "\r\n" );
                System.out.println();

                int nCount = 0;
                for( int i = 1;i < nSize;i++ ){
                    for( int j = 0;j < i;j++ ){
                        char c = sIndex[1].charAt( nCount++ );
                        if( c == '1' ){
                            w.write( (i+1) + " " + (j+1) + " " + "1" + "\r\n" );
                            System.out.print( (i+1) + " " + (j+1) + " " + "1" + "\r\n" );
                        }
                    }
                }

                w.write( "EOF\r\n" );
                System.out.println( "EOF" );

            }

            w.close();
            r.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
