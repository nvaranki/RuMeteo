/*
 * Index.java
 *
 * Created on 13 Март 2007 г., 19:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.varankin.RuMeteo;

import com.varankin.mobile.Dispatcher;
import com.varankin.mobile.PropertyResourceBundle;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

/**
 * @author  Nikolai Varankine
 */
public class Index
{
    public final static int ROOT = 100000000;
    private String name[]; // visible name of city
    private int code[];   // accosiated city weather index
    private int id;
    private String title;

    public class SearchPointer implements Enumeration
    {
        private int pointer;
        private String filter;
        private Enumeration inside;
        /**
         * Creates new instance.
         */
        public SearchPointer( String a_filter )
        {
            pointer = 0;
            filter = a_filter;
            inside = null;
        }
        /** 
         * Tests if this enumeration contains more elements. 
         */
        public boolean hasMoreElements()
        {
            while( pointer < name.length )
                if( code[ pointer ] >= ROOT )
                    if( code[ pointer ] > id )
                    {
                        if( inside == null ) 
                        {
                            inside = ( new Index( code[ pointer ] ) ).elements( filter );
                        }
                        if( inside.hasMoreElements() ) return true;
                        else { inside = null; pointer++; }
                    }
                    else pointer++;
                else if( name[ pointer ].indexOf( filter ) >= 0 ) return true;
                else pointer++;
            return false;
        }
        /** 
         * Returns the next element of this enumeration if this enumeration 
         * object has at least one more element to provide.
         */
        public Object nextElement()
        {
            while( pointer < name.length )
                if( code[ pointer ] >= ROOT )
                    if( code[ pointer ] > id )
                    {
                        if( inside == null ) 
                        {
                            inside = ( new Index( code[ pointer ] ) ).elements( filter );
                        }
                        IndexRecord io = (IndexRecord) inside.nextElement();
                        if( io != null ) 
                        {
                            if( title != null && id != Index.ROOT ) 
                                io.title += ", " + title;
                            return io;
                        }
                        else { inside = null; pointer++; }
                    }
                    else pointer++;
                else if( name[ pointer ].indexOf( filter ) >= 0 ) 
                    return new IndexRecord( id, title, code[ pointer ], name[ pointer ], pointer++ );
                else pointer++;
            return null;
        }
    }

    /** Creates a new instance of Index */
    public Index( int a_parentIndex )
    {
        title = null;
        try { load( a_parentIndex ); }
        catch( Exception e ) { name = new String[0]; code = new int[0]; id = ROOT; }
        sort();
    }

    public int getId()
    {
        return id;
    }
    public int getCode( int a_id )
    {
        return code[ a_id ];
    }
    public String getName( int a_id )
    {
        return name[ a_id ];
    }
    public String getTitle()
    {
        return title;
    }
    public int size()
    {
        return java.lang.Math.min( name.length, code.length );
    }
    public Enumeration elements( String a_filter )
    {
        return new SearchPointer( a_filter );
    }

    /**
     * Loads resource like file into pair of synchronized arrays
     */
    public void load( int a_parentIndex ) 
        throws IllegalArgumentException, IOException, UnsupportedEncodingException
    {
        // load localized resources into list
        PropertyResourceBundle messages = new PropertyResourceBundle( 
            Dispatcher.getClassPath( this ) + "res/" 
            + String.valueOf( a_parentIndex ) ); 

        // once file exists and loaded, update ID of city list
        id = a_parentIndex;

        // allocate list of cities
        code = new int[ messages.size() ];
        name = new String[ code.length ];

        // stuff list of cities
        Enumeration e = messages.keys();
        for( int c = 0; e.hasMoreElements(); c++ )
        {
            String key = (String) e.nextElement();
            name[c] = (String) messages.get( key );
            code[c] = Integer.parseInt( key );
            if( code[c] == id ) title = name[c];
        }
    }

    /**
     * Sorts arrays syncronously: top indexes first, other as alpha`s
     */
    private static void sort( int[] a_code, int a_parentIndex, String[] a_name )
    {
        for( boolean repeat = true; repeat; )
        {
            repeat = false;
            for( int c = 1; c < a_name.length; c++ )
                if( a_code[c] >= ROOT && a_code[c] <= a_parentIndex
                    ? ( a_code[c-1] < ROOT || a_code[c-1] > a_code[c] )
                    : ( !( a_code[c-1] >= ROOT && a_code[c-1] <= a_parentIndex ) 
                        && a_name[c-1].compareTo( a_name[c] ) > 0 )
                  )
                {
                    repeat = true; 
                    // swap pairs
                    String ss = a_name[c-1]; a_name[c-1] = a_name[c]; a_name[c] = ss;
                    int si = a_code[c-1]; a_code[c-1] = a_code[c]; a_code[c] = si;
                }
        }
    }

    /**
     * Sorts internal arrays syncronously: top indexes first, other as alpha`s
     */
    public void sort()
    {
        sort( code, id, name );
    }

}
