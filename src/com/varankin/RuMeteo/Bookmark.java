/*
 * Bookmark.java
 *
 * Created on 26 ******* 2007, 1:15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.varankin.RuMeteo;

import com.varankin.mobile.Dispatcher;
import java.util.*;
import javax.microedition.lcdui.Image;

/**
 * @author  Nikolai Varankine
 */
public class Bookmark
{
    public final static long INFINITY = Long.MAX_VALUE >> 1;
    public final static long PAST = 0L; //Long.MIN_VALUE;
    public final static byte[] EMPTY = new byte[0];
    private final static String RKEY_IMAGE = "Image";
    private final static String RKEY_DATE = "Created";
    private final static String RKEY_TITLE = "Title";
    private final static String RKEY_CITY = "City";
    private final static String RKEY_LIST = "Country";
    private final static String RKEY_VIEW = "View";
    
    private Dispatcher application;

    public Image image; // convenience field, is never saved
    public byte[] data;
    public long created;
    public String title;
    public int city, list, view, id;

    public Bookmark( Dispatcher a_application, int a_id )
    {
        application = a_application;
        id = a_id;
        image = null;
        data = getBytes( application, a_id );
        created = data.length > 0 ? getDate( application, a_id ) : INFINITY;
        title = getTitle( application, a_id );
        city = getCity( application, a_id );
        list = getList( application, a_id );
        view = getView( application, a_id );
    }

    public static byte[] getBytes( Dispatcher a_dispatcher, int a_id )
    {
        byte[] regbts = a_dispatcher.registry.getBinaryValue( getKey( RKEY_IMAGE, a_id ) );
        return regbts != null ? regbts : EMPTY;
    }
    public static long getDate( Dispatcher a_dispatcher, int a_id )
    {
        String regstr = a_dispatcher.registry.getValue( getKey( RKEY_DATE, a_id ) );
        return regstr != null ? Long.parseLong( regstr ) : PAST;
    }
    public static String getTitle( Dispatcher a_dispatcher, int a_id )
    {
        return a_dispatcher.registry.getValue( getKey( RKEY_TITLE, a_id ) );
    }
    public static int getCity( Dispatcher a_dispatcher, int a_id )
    {
        String regstr = a_dispatcher.registry.getValue( getKey( RKEY_CITY, a_id ) );
        return regstr != null ? Integer.parseInt( regstr ) : Index.ROOT;
    }
    public static int getList( Dispatcher a_dispatcher, int a_id )
    {
        String regstr = a_dispatcher.registry.getValue( getKey( RKEY_LIST, a_id ) );
        return regstr != null ? Integer.parseInt( regstr ) : Index.ROOT;
    }
    public static int getView( Dispatcher a_dispatcher, int a_id )
    {
        String regstr = a_dispatcher.registry.getValue( getKey( RKEY_VIEW, a_id ) );
        return regstr != null ? Integer.parseInt( regstr ) : 0;
    }

    private static String getKey( String a_main_key, int a_id )
    {
        return "Forecast." + a_main_key + "." + String.valueOf( a_id );
    }

    public boolean saveAs( int a_id )
    {
        try
        {
            application.registry.setValue( getKey( RKEY_IMAGE, a_id ), data );
            application.registry.setValue( getKey( RKEY_TITLE, a_id ), title );
            application.registry.setValue( getKey( RKEY_DATE, a_id ), String.valueOf( created ) );
            application.registry.setValue( getKey( RKEY_CITY, a_id ), String.valueOf( city ) );
            application.registry.setValue( getKey( RKEY_LIST, a_id ), String.valueOf( list ) );
            application.registry.setValue( getKey( RKEY_VIEW, a_id ), String.valueOf( view ) );
        }
        catch( Exception e ) { return false; }
        return true;
    }

    public boolean save()
    {
        return saveAs( id );
    }

    public boolean isExpired( long a_validity )
    {
        // HTTP requires to return expiration date for GMT zone; so we do
        return Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) )
                .getTime().getTime() > created + a_validity;
    }
}
