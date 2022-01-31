/*
 * Application.java
 * Created on April 29, 2006, 12:02 AM
 * Copyright 2006 Nikolai Varankine. All rights reserved.
 *
 * This class implements main midlet.
 */

package com.varankin.RuMeteo;

import com.varankin.mobile.Dispatcher;
import java.io.*;
import java.lang.Long;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

/**
 * @author  Nikolai Varankine
 */
public class Application extends Dispatcher 
{
    private boolean m_auto;
    private int m_zoom;
    private String m_url;
    private long m_validity;
    Bookmark[] bookmarks;

    /** Creates a new instance of Application */
    public Application() throws IOException, RecordStoreException
    {
//#if FreeDemo
//#         super( 7L*24L*60L*60L*1000L ); // limited to 7 days
//#else
        super( Long.MAX_VALUE >> 1 ); // unlimited
//#endif
        // registry settings
        m_auto = SetupOptions.getAuto( this );
        m_zoom = SetupOptions.getZoom( this );
        m_url = SetupOptions.getURL( this );
        m_validity = SetupOptions.getValidity( this );

        // do not load extra bookmarks
        bookmarks = new Bookmark[10]; // 0, 1..9
        for( int b = 0; b < bookmarks.length; b++ ) bookmarks[ b ] = null;

        // complete important checks before start
        if( ! isMIDletValid( 46931 ) || m_url == null ) exitRequested(); // shame on you, hackers

        // start last opened forecast first
        // check memory availability while starting GUI
        System.gc();
        ( new Weather( this, runtime.freeMemory() < 200L*1024L ) ).setBookmark();
    }

    public final boolean getAuto()
    {
        return m_auto;
    }
    public final void setAuto( boolean a_new_mode )
    {
        try { SetupOptions.setAuto( this, m_auto = a_new_mode ); }
        catch( RecordStoreException e ) {} // recoverable problem
    }

    public final int getZoom()
    {
        return m_zoom;
    }
    public final void setZoom( int a_new_mode )
    {
        try { SetupOptions.setZoom( this, m_zoom = a_new_mode ); }
        catch( RecordStoreException e ) {} // recoverable problem
    }

    public long getValidity()
    {
        return m_validity;
    }
    public void setValidity( long a_value )
    {
        try { SetupOptions.setValidity( this, m_validity = a_value ); }
        catch( RecordStoreException e ) {} // recoverable problem
    }

    public String getURL()
    {
        return m_url;
    }
    public void setURL( String a_url )
    {
        try { SetupOptions.setURL( this, m_url = a_url ); }
        catch( RecordStoreException e ) {} // recoverable problem
    }

    public void startApp()
    {
        setCurrent();
    }
    public void pauseApp() 
    {
    }
    public void destroyApp(boolean unconditional) 
    {
    }
    public void exitRequested() 
    { 
        destroyApp(false); notifyDestroyed(); 
    }
}
