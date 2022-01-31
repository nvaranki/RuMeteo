/*
 * SetupView.java
 *
 * Created on 28 Февраль 2007 г., 13:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.varankin.RuMeteo;

import com.varankin.mobile.Dispatcher;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStoreException;

/**
 * @author  Nikolai Varankine
 */
public class SetupView extends Canvas
{
    private final static String RKEY_LAST = "Setup.Bookmark";
    private final static Font tagFonts[] = 
    {
        Font.getFont( Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE ),
        Font.getFont( Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM ),
        Font.getFont( Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL ),
        Font.getFont( Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL ),
        Font.getFont( Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL )
    };
    private final static int fgc = 0x00FFFFFF; // white
    private final static int bgc = 0x00E77817; // R:231 G:120 B:17 // 0x00000000; // black
    private CommandListener callback;
    private Image schema;
    private Command CMD_BACK, CMD_NEXT, CMD_SELECT;
    private int view;
    private final static String[] views = 
    {
        "5", // green, orange captions
        "11", // colorful
        "3", // pink
        "10", // sea, orange captions
        "9", // light blue
        "7", // blue
        "1", // b/w, orange captions
        "4", // b/w
        "6"/* schema is 3x3 image! , // dark pink
        "2" // grey*/
    };

    public Image logo, logo50;
    public Ticker help;
    
    /** Creates a new instance of SetupView */
    public SetupView( Application a_parent, CommandListener a_callback )
    {
        super();
        callback = a_callback;
        help = new Ticker( a_parent.getString(this,"Ticker") );
        CMD_BACK = new Command( a_parent.getString(".Menu.Back"), Command.BACK, 10 );
        CMD_NEXT = new Command( a_parent.getString(this,"Menu.Next"), Command.SCREEN, 2 );
        CMD_SELECT = new Command( a_parent.getString(this,"Menu.Select"), Command.ITEM, 1 );

        // setup default images
        String path = Dispatcher.getClassPath( this );
        try { logo50 = Image.createImage( path + "res/logo50x50.png" ); }
        catch( java.io.IOException e ) { logo50 = Image.createImage( 50, 50 ); }
        try { logo   = Image.createImage( path + "res/logo100x100.png" ); }
        catch( java.io.IOException e ) { logo   = Image.createImage( 100, 100 ); }
        try { schema = Image.createImage( path + "res/ColorMenu.png" ); }
        catch( java.io.IOException e ) { schema = Image.createImage( 100, 100 ); }

        // complete GUI
        setTitle( a_parent.getString(this,"Title") );
        setTicker( a_parent.getHelpMode() != Dispatcher.HELP_NO ? help : null );
        addCommand( CMD_SELECT );
        addCommand( CMD_BACK );
        addCommand( CMD_NEXT );
        setCommandListener( a_callback );
    }
    protected void paint( Graphics g )
    {
        // outline current view
        Image outlined = Image.createImage( schema.getWidth(), schema.getHeight() );
        Graphics gc = outlined.getGraphics();
        gc.drawImage( schema, 0, 0, 0 );
        int w3 = schema.getWidth()/3, h3 = schema.getHeight()/3;
        gc.setStrokeStyle( gc.SOLID );
        gc.setColor( 0x00FFFFFF ); // white
        gc.drawRect( (view % 3)*w3, (view / 3)*h3, w3-1, h3-1 );
        // use result
        paintImage( g, outlined, logo50, getWidth(), getHeight(), 1 );
        System.gc();
    }
    public static void paintImage( Graphics g, Image image, Image bgi, 
        int widthScreen, int heightScreen, int zoomIn )
    {
        // clear screen
        g.setColor( bgc ); // orange
        g.fillRect( 0, 0, widthScreen, heightScreen );

        // evaluate grid parameters
        int xAnchor = widthScreen/2;
        int yAnchor = heightScreen/2;

        if( bgi != null ) 
        {
            // draw wallpaper
            int widthLogo = bgi.getWidth(), heightLogo = bgi.getHeight();
            int xRepeat = (xAnchor+widthLogo -1)/widthLogo;
            int yRepeat = (yAnchor+heightLogo-1)/heightLogo;
            for( int r = -yRepeat; r <= yRepeat; r++ ) 
              for( int c = -xRepeat; c <= xRepeat; c++ )
                g.drawImage( bgi, xAnchor+c*widthLogo, yAnchor+r*heightLogo, 
                    Graphics.TOP|Graphics.LEFT );
        }

        // draw centered image
        if( image != null ) 
            g.drawImage( stretch( image, zoomIn, false ), 
                xAnchor, yAnchor, Graphics.HCENTER|Graphics.VCENTER );
    }
    /**
     * Returns stretched source image.
     */
    public static Image stretch( Image a_original, int a_ratio, boolean a_processAlpha )
    {
        if( a_ratio == 1 ) return a_original;

        int w = a_original.getWidth(), wRated = w*a_ratio;
        int h = a_original.getHeight(), hRated = h*a_ratio;
        // allocate buffer safely, it's too big!
        int buffer[];
        try { buffer = new int[wRated*hRated]; }
        catch( Exception e ) { return a_original; }
        // scan original into beginning of buffer
        a_original.getRGB( buffer, 0, w, 0, 0, w, h );
        // translate original pixel into stretched square
        for( int r = h-1; r >= 0; r-- )
          for( int c = w-1; c >= 0; c-- )
          {
            int pixel = buffer[ r*w+c ];
            int loc = (r*a_ratio) * wRated + (c*a_ratio);
            for( int y = 0; y < a_ratio; y++ )
              for( int x = 0; x < a_ratio; x++ )
                buffer[ loc + y * wRated + x ] = pixel;
          }
        // done
        return Image.createRGBImage( buffer, wRated, hRated, a_processAlpha );
    }
    /**
     * Makes a replacement image for missing forecast.
     */
    public static Image createBookmarkImage( Image a_bg, String a_label )
        throws IllegalArgumentException
    {
        Image tag = Image.createImage( a_bg.getWidth(), a_bg.getHeight() );
        Graphics gc = tag.getGraphics();
        // select font that fits caption width
        for( int f = 0; f < tagFonts.length; f++ )
        {
            gc.setFont( tagFonts[ f ] );
            if( tagFonts[ f ].charsWidth( a_label.toCharArray(), 0, a_label.length() ) <= 
                    a_bg.getWidth() - 2 ) break;
        }
        // draw background
        gc.drawImage( a_bg, 0, 0, Graphics.TOP|Graphics.LEFT );
        // fill caption and outline the tag
        gc.setColor( fgc );
        gc.fillRect( 0, 0, a_bg.getWidth(), gc.getFont().getHeight()+2 );
        gc.drawRect( 0, 0, a_bg.getWidth()-1, a_bg.getHeight()-1 );
        // put inversed text on caption
        gc.setColor( bgc );  
        gc.drawString( a_label, a_bg.getWidth()/2, 1, Graphics.TOP|Graphics.HCENTER );
        // done!
        return tag;
    }
    /**
     * Creates sample image of selected view type.
     */
    public Image createSampleImage()
    {
        int w3 = schema.getWidth()/3, h3 = schema.getHeight()/3;
        Image sel = Image.createImage( w3, h3 );
        sel.getGraphics().drawImage( schema, -(view % 3)*w3, -(view / 3)*h3, 0 );
        return sel;
    }
    /**
     * Saves supplied Bookmark setting into RMS.
     */
    public static void setView( Dispatcher a_dispatcher, int a_value )
        throws RecordStoreException
    {
        a_dispatcher.registry.setValue( RKEY_LAST, String.valueOf( a_value ) );
    }
    /**
     * Returns Bookmark setting from RMS.
     */
    public static int getView( Dispatcher a_dispatcher )
    {
        String rgstr = a_dispatcher.registry.getValue( RKEY_LAST );
        try{ return rgstr != null ? Integer.parseInt( rgstr ) : 0; }
        catch( NumberFormatException e ) { return 0; }
    }
    /**
     * Returns server based index related to current view type, as a string.
     */
    public String getView()
    {
        return getView( view );
    }
    /**
     * Returns server based index related to requested view type, as a string.
     */
    public static String getView( int a_view )
    {
        return "16_" + views[ a_view % views.length ];
    }
    /**
     * Returns table based index related to current view type, as an integer.
     */
    public int getViewIndex()
    {
        return view;
    }
    /**
     * Sets table based index related to current view type.
     */
    public void setViewIndex( int a_view )
    {
        view = a_view % views.length;
    }
    /*
     * Changes index of selected view type and returns from current form.
     */
    private void selectColorScheme( int a_index )
    {
        view = a_index % views.length;
        callback.commandAction( CMD_SELECT, this );
    }

    /**
     * Called when a key is pressed.
     */
    protected void keyPressed( int a_keyCode )
    {
        String keyName = getKeyName( a_keyCode );

        if( a_keyCode != KEY_NUM6 && 
            ( keyName.compareTo( "RIGHT" ) == 0 || keyName.charAt( 0 ) == 0x2192 ) )
        {
            setViewIndex( view + 1 );
            repaint();
        }

        else if( a_keyCode != KEY_NUM4 && 
            ( keyName.compareTo( "LEFT" ) == 0 || keyName.charAt( 0 ) == 0x2190 )  )
        {
            setViewIndex( view - 1 + views.length );
            repaint();
        }

        else if( a_keyCode != KEY_NUM2 && 
            ( keyName.compareTo( "UP" ) == 0 || keyName.charAt( 0 ) == 0x2191 )  )
        {
            setViewIndex( view - 3 + views.length );
            repaint();
        }

        else if( a_keyCode != KEY_NUM8 && 
            ( keyName.compareTo( "DOWN" ) == 0 || keyName.charAt( 0 ) == 0x2193 )  )
        {
            setViewIndex( view + 3 );
            repaint();
        }

        else if( keyName.compareTo( "SEND" ) == 0 || keyName.compareTo( "SELECT" ) == 0 )
        {
            selectColorScheme( view );
        }

        else switch( a_keyCode )
        {
            case KEY_NUM1: selectColorScheme( 0 ); break;
            case KEY_NUM2: selectColorScheme( 1 ); break;
            case KEY_NUM3: selectColorScheme( 2 ); break;
            case KEY_NUM4: selectColorScheme( 3 ); break;
            case KEY_NUM5: selectColorScheme( 4 ); break;
            case KEY_NUM6: selectColorScheme( 5 ); break;
            case KEY_NUM7: selectColorScheme( 6 ); break;
            case KEY_NUM8: selectColorScheme( 7 ); break;
            case KEY_NUM9: selectColorScheme( 8 ); break;
            case KEY_POUND: callback.commandAction( CMD_NEXT, this ); break;
            default: break; // play sound here
        }

    }
}
