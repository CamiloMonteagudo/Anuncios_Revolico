package com.BigXSoft.anuncios;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.BigXSoft.anuncios.AnunciosInfo.AnuncioData;
import com.BigXSoft.anuncios.AnunciosInfo.HtmlInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

@SuppressWarnings( "UnusedReturnValue" )
public class PublishActivity extends AppCompatActivity
  //==================================================================== PublishActivity =========================================================================================
  {
  private WebView      mWebPg;                        // Vista donde se muestran las páginas web
  private Toolbar      mAppBar;                       // Barra de herramientas de la aplicación
  private LinearLayout mLoadPanel;                    // Panel que se muestra durante la carga de las páginas
  private boolean      mUrlOut;                       // Define si la páginas se cargan dentro de la aplicación o no
  private String       mLastUrl = "";                 // Ultima URL editada

  // Define los tipos de páginas que maneja el sistema
  @SuppressWarnings("unused") enum PGType {  INSERT                ,  INSERTED               ,  MODIFY                 ,  DELETE                ,  VIEW              ,  NONE  }
  private static final String[] PGName  = { "insertar-anuncio.html", "anuncio-insertado.html", "modificar-anuncio.html", "eliminar-anuncio.html", ".html?token="              };
  private static final int[]    PGTitle = { R.string.InsertAnunc   , R.string.InsertedAnunc  , R.string.ModifyAnunc    , R.string.DeleteAnunc   , R.string.ViewAnunc , R.string.ViewPage };
  

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Controla el proceso se creación de la vista */
  @SuppressLint( "SetJavaScriptEnabled" )
  @Override protected void onCreate( Bundle savedInstanceState )
    {
    App.Init( getApplicationContext() );                        // Inicializa datos y funciones globales de la aplicación
    
    super.onCreate( savedInstanceState );
    setContentView( R.layout.activity_publish );
    
    setToolBar();
    
    mLoadPanel = findViewById( R.id.LoadPanel );
    mLoadPanel.setVisibility( View.GONE );

    mWebPg = findViewById( R.id.WebPage );
    
    WebSettings conf = mWebPg.getSettings();
    conf.setJavaScriptEnabled( true );
    conf.setJavaScriptCanOpenWindowsAutomatically( true );
    conf.setLoadWithOverviewMode( true );
    conf.setUseWideViewPort( true );
    //conf.setBuiltInZoomControls(true);
    conf.setLoadsImagesAutomatically( true );
    conf.setSupportZoom( true );
    conf.setAllowContentAccess( true );
    conf.setDomStorageEnabled( true );
    conf.setAllowFileAccess( true );
    conf.setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW );
    //conf.setSupportMultipleWindows( true );
    
    mWebPg.setWebChromeClient( new MyWebChromeClient() );
    mWebPg.setWebViewClient( new MyWebViewClient() );

    clearWebData();
    //ShowDlg.Msg( this, getCookie( "https://www.revolico.com" ) );
    
    String      Url     = "file:///android_asset/PageTest.html";
    AnuncioData Anuncio = App.getActualAnunc();
    if( Anuncio != null )
      Url = (Anuncio.UrlUpd==null || Anuncio.UrlUpd.length()==0)? Anuncio.Url : Anuncio.UrlUpd;

    registerForContextMenu( mWebPg );
    
    mWebPg.loadUrl( Url );
    SetFloatBtn();
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  @Override public void onCreateContextMenu( ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo )
    {
    super.onCreateContextMenu( menu, view, contextMenuInfo );
  
    final HitTestResult hitTst = mWebPg.getHitTestResult();
    final int           hitTyp = hitTst.getType();
    final String        hitUrl = hitTst.getExtra();

    OnMenuItemClickListener handler = new OnMenuItemClickListener()
      {
      public boolean onMenuItemClick( MenuItem item )
        {
        int Id = item.getItemId();
        if( Id == 100 )
          copyToClipboard( hitUrl );
        else
          launchABrowser( Browse.values()[Id], hitUrl );
        
        return true;
        }
      };

    if( hitTyp == HitTestResult.SRC_ANCHOR_TYPE )
      {
      menu.add( Menu.NONE, Browse.DEFAULT.ordinal(), 0, R.string.ShowInDefault ).setOnMenuItemClickListener( handler );
      
      AddMenuItem( menu, Browse.CROME     , R.string.ShowInCrome     , handler );
      AddMenuItem( menu, Browse.FIREFOX   , R.string.ShowInFireFox   , handler );
      AddMenuItem( menu, Browse.OPERA     , R.string.ShowInOpera     , handler );
      AddMenuItem( menu, Browse.OPERA_MINI, R.string.ShowInOperaMini , handler );
      AddMenuItem( menu, Browse.ANDROID   , R.string.ShowInAndroid   , handler );

      menu.add( Menu.NONE, 100 , 0, R.string.CopyUrl ).setOnMenuItemClickListener( handler );

//      // 1. the picture must be focused, so we simulate a DPAD enter event to trigger the hyperlink
//      KeyEvent event1 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER);
//      mWebPg.dispatchKeyEvent(event1);
//      KeyEvent event2 = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER);
//      mWebPg.dispatchKeyEvent(event2);
      
      }

    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Adicina un item en el menu, de utilizar el tipo de navegador indicado */
  private boolean AddMenuItem( ContextMenu menu, Browse bs, int idTitle, OnMenuItemClickListener handler )
    {
    if( !launchABrowser( bs, null ) ) return false;
    
    MenuItem mnu = menu.add( Menu.NONE, bs.ordinal(), 0, getString( idTitle ) );

    mnu.setOnMenuItemClickListener( handler );
    return true;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Copia la cadena text en el portapapeles */
  private boolean copyToClipboard( String text )
    {
    try
      {
      ClipData         clip      = ClipData.newPlainText( "label", text );
      ClipboardManager clipboard = (ClipboardManager) getSystemService( CLIPBOARD_SERVICE );
      
      if( clipboard != null )
        {
        clipboard.setPrimaryClip( clip );
        return true;
        }
      }
    catch( Exception ignore ) {}
    
    return false;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                 enum Browse {  CROME                ,  FIREFOX           ,  OPERA             ,  OPERA_MINI             ,  ANDROID              ,  DEFAULT  }
  private static final String[] KGsName = { "com.android.chrome", "org.mozilla.firefox", "com.opera.browser", "com.opera.mini.android", "com.android.browser" , null      };
 
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Abre la pagina web especificada en 'url' en el navegador especificado en 'bs', Si url es null solo verifica que el browse puese utilizarse */
  private boolean launchABrowser( Browse bs, String url )
    {
    Intent itn = null;

    if( bs != Browse.DEFAULT )
      {
      PackageManager pkgeMng = getPackageManager();

      String pkgName = KGsName[ bs.ordinal() ];
      itn = pkgeMng.getLaunchIntentForPackage( pkgName );

      if( itn != null )
        {
        if( pkgeMng.queryIntentActivities( itn, 0 ).size() == 0 )
          itn = null;
        }
      }

    if( url == null ) return (bs==Browse.DEFAULT || itn!=null);

    if( itn == null ) itn = new Intent( Intent.ACTION_VIEW );

    itn.setData( Uri.parse( url ) );
    itn.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );

    startActivity( itn );
    
    return true;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene todos los cookies registrados para un dominio dado */
  @SuppressWarnings( "unused" )
  public String getCookie( String siteName )
    {
    CookieManager ckMng   = CookieManager.getInstance();
    String        cookies = ckMng.getCookie( siteName );

    if( cookies==null ) return "";

    return cookies.replace( ';', '\n' );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Borra los datos que guardan las páginas web en el lado cliente */
  private static void clearWebData()
    {
    CookieManager ckMng = CookieManager.getInstance();

    ckMng.removeAllCookies( null );
    ckMng.flush();

    WebStorage.getInstance().deleteAllData();
    }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Configura el soporte para el toolbar de la aplicación */
  private void setToolBar()
    {
    mAppBar = findViewById( R.id.toolbar );
    setSupportActionBar( mAppBar );
    
    ActionBar ab = getSupportActionBar();
    if( ab != null )
      ab.setDisplayHomeAsUpEnabled( true );
    
    // Atiende cuando se oprime el botón de ir hacia atras
    mAppBar.setNavigationOnClickListener( new View.OnClickListener()
      {
      @Override public void onClick( View v )
        {
        CheckGetURLsAndClose();
        }
      } );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Chequea cuando se oprime la tecla de retroceso en el telefono */
  @Override public boolean onKeyDown( int keyCode, KeyEvent event )
    {
    // Chequea si se oprimio la tecla de retroceder y hay paginas en la hostoria
    if( (keyCode == KeyEvent.KEYCODE_BACK) && mWebPg.canGoBack() )
      {
      CheckGetURLsAndClose();
      return true;
      }
    
    return super.onKeyDown( keyCode, event );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama cuando la se va a cerrar la actividad y verifica no se ha guardado los datos del anuncio publicado si es el caso */
  private void CheckGetURLsAndClose()
    {
    if( !UrlsNeeded( false ) )
      {
      finish();
      return;
      }

    // Preginta al usuario si quiere Guardar las URLs, Descartarlas o No hacer nada (quedarse en la actividad)
    ShowDlg.SelectAction( this, R.string.msgSaveUrls, new DialogInterface.OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        switch( which )
          {
          case DialogInterface.BUTTON_POSITIVE:                           // (Boton Guardar)
            if( UrlsNeeded( true ) ) GetURLs( true );       // Si se puede trata de obtenr los datos de la página
              else                           GetURLsByUrl(true);    // Si no, trata de obtener los datos del URL
            finish();                                                     // Cierra la actividad
            break;
          case DialogInterface.BUTTON_NEGATIVE:                           // (Boton Descartar)
            finish();                                                     // Cierra la actividad incondicionalmente
            break;
          case DialogInterface.BUTTON_NEUTRAL:                            // (Boton Cancelar) no hace nada
          }
        }
      } );
    }
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama para crear el menu de la appbar (hay que llamar setSupportActionBar antes) */
  @Override public boolean onCreateOptionsMenu( Menu menu )
    {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate( R.menu.menu_webview, menu );
    return true;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Actualiza el estado del menú antes de ser mostrado */
  @Override public boolean onPrepareOptionsMenu( Menu menu )
    {
    menu.findItem( R.id.mnu_web_openOut ).setChecked( mUrlOut );

    menu.findItem( R.id.mnu_web_prevpg ).setVisible( mWebPg.canGoBack() );
    menu.findItem( R.id.mnu_web_nextpg ).setVisible( mWebPg.canGoForward() );

    boolean ByScript = UrlsNeeded( true  );
    boolean ByUrl    = UrlsNeeded( false  );
    
    menu.findItem( R.id.mnu_web_getdatos1 ).setVisible( ByScript );
    menu.findItem( R.id.mnu_web_getdatos2 ).setVisible( !ByScript && ByUrl );
    
    PGType      tp    = getPageTipo();
    AnuncioData anunc = App.getActualAnunc();
    
    if( anunc != null )
      {
      menu.findItem( R.id.mnu_web_view   ).setVisible( tp!=PGType.VIEW   && anunc.UrlPag!=null );
      menu.findItem( R.id.mnu_web_modify ).setVisible( tp!=PGType.MODIFY && anunc.UrlUpd!=null );
      menu.findItem( R.id.mnu_web_delete ).setVisible( tp!=PGType.DELETE && anunc.UrlDel!=null );
      }
    
    return super.onPrepareOptionsMenu( menu );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama cuando se selecciona una item de la action bar */
  @Override public boolean onOptionsItemSelected( MenuItem item )
    {
    switch( item.getItemId() )
      {
      case R.id.mnu_web_url:     return LoadUserUrl();
      case R.id.mnu_web_openOut: mUrlOut = !mUrlOut;   return true;
      case R.id.mnu_web_prevpg:  mWebPg.goBack();      return true;
      case R.id.mnu_web_nextpg:  mWebPg.goForward();   return true;
      case R.id.mnu_web_reload:  mWebPg.reload();      return true;
      
      case R.id.mnu_web_getdatos1: GetURLs( false );     return true;
      case R.id.mnu_web_getdatos2: GetURLsByUrl(false ); return true;

      case R.id.mnu_web_view  : loadPage( "0" ); return true;
      case R.id.mnu_web_modify: loadPage( "1" ); return true;
      case R.id.mnu_web_delete: loadPage( "2" ); return true;
      }

    String str = item.getTitle() + " (SIN ATENDER)";
    Toast  msg = Toast.makeText( getApplicationContext(), str, Toast.LENGTH_LONG );
    msg.show();
    
    return super.onOptionsItemSelected( item );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra la página web dada */
  private void loadPage( String Url )
    {
    AnuncioData anunc = App.getActualAnunc();

    if( anunc != null )
      {
      if( Url.equals("0") ) Url = anunc.UrlPag;
      if( Url.equals("1") ) Url = anunc.UrlUpd;
      if( Url.equals("2") ) Url = anunc.UrlDel;
      }

    if( mUrlOut )
      {
      Intent itn = new Intent( Intent.ACTION_VIEW, Uri.parse( Url ) );
      startActivity( itn );
      }
    else
      mWebPg.loadUrl( Url );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Abre la url dada en el navegador especificado en pkg, si es nulo la abre en el navegador por defecto */
  @SuppressWarnings( "unused" )
  private void LoadUrlInBrowse( String sUrl, String pkg )
    {
    Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( sUrl ) );
  
    intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
    intent.setPackage( pkg );
    try
      {
      startActivity( intent );
      }
    catch( Exception ex )
      {
      intent.setPackage( null );
      startActivity( intent );
      }
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Determina si es necesario y se puede obtener la informacion de las urls relacionadas con el anuncio o no */
  private boolean UrlsNeeded( boolean byScript )
    {
    String url = mWebPg.getUrl();
    if( url==null ) return false;
    
    if( !url.contains( "revolico.com" )          ) return false;
    if(  url.contains( "insertar-anuncio.html" ) ) return false;
  
    if( byScript && !url.contains( "anuncio-insertado" ) ) return false;
  
    AnuncioData anunc = App.getActualAnunc();
    if( anunc == null ) return false;
  
    if( anunc.UrlUpd == null ) return true;
  
    return anunc.UrlUpd.trim().length()==0;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String urlPag;
  private String urlUpd;
  private String urlDel;
  private final Pattern Ptron1 = Pattern.compile( "\\.html\\?adid=([0-9]{8})&token=(\\w{12})" );
  private final Pattern Ptron2 = Pattern.compile( "([0-9]{8})\\.html\\?token=(\\w{12})" );
  private final Pattern Ptron3 = Pattern.compile( "\\.html\\?key=(\\w{20})" );
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Obtine las urls de actualización y borrado analizando la URL de la página */
  private void GetURLsByUrl(  final boolean auto )
    {
    if( auto && !UrlsNeeded( false  )) return;
    
    urlPag = mWebPg.getUrl();
    if( urlPag==null ) return;
    
    Matcher mch1 = Ptron1.matcher( urlPag );
    Matcher mch2 = Ptron2.matcher( urlPag );

    String Key = null;

    boolean fnd1 = mch1.find();
    if(  fnd1 || mch2.find() )
      {
      String ID = fnd1? mch1.group(1) : mch2.group(1) ;
      String Tk = fnd1? mch1.group(2) : mch2.group(2) ;

      Key = Tk + ID;
      }
    else
      {
      Matcher mch3 = Ptron3.matcher( urlPag );
      if(  mch3.find() )
        Key = mch3.group(1);
      }

    if( Key != null )
      {
      if( getPageTipo() != PGType.VIEW ) urlPag = null;
      
      urlUpd = "https://www.revolico.com/modificar-anuncio.html?key=" + Key;
      urlDel = "https://www.revolico.com/eliminar-anuncio.html?key=" + Key;

      if( auto  ) App.setURLs( urlPag, urlUpd, urlDel );
      
      invalidateOptionsMenu();
      }

    if( !auto  )
      {
      if( Key != null ) ShowUrls();
      else              ShowDlg.Msg(this, getString( R.string.msgBadUrl ) + urlPag );
      }
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Obtine las URLs relecionadas con la publicación del auncio, 'auto' indica que el proceso se ara automaticamente, en otro caso el usuario define manualmente*/
  private void GetURLs( final boolean auto )
    {
    if( auto && !UrlsNeeded( true  )) return;
    
    mWebPg.evaluateJavascript( "GetLinks()", new ValueCallback<String>()
      {
      @Override public void onReceiveValue( String value )
        {
        String[] Parts = value.replaceAll( "\"","" ).split( ";" );
        if( Parts.length == 3 )
          {
          urlPag =  Parts[0];
          urlUpd =  Parts[1];
          urlDel =  Parts[2];

          if( auto )
            {
            App.setURLs( urlPag, urlUpd, urlDel );
            invalidateOptionsMenu();
            }
          }
        
        if( !auto ) ShowUrls();
        }
      } );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra los valores de las URLs solicitadas */
  private void ShowUrls()
    {
    String Msg = "Datos sobre los URLs|" +
                 "urlPag: " + urlPag + "\n" +
                 "urlUpd: " + urlUpd + "\n" +
                 "urlDel: " + urlDel + "\n" +
                 "|Guardar|Cerrar";
      
    ShowDlg.Confirm( this, Msg, new DialogInterface.OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        App.setURLs( urlPag, urlUpd, urlDel );
        invalidateOptionsMenu();
        }
      } );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Solicita una ULR y trata de navegar hacia ella */
  @SuppressWarnings( "SameReturnValue" )
  private boolean LoadUserUrl()
    {
    ShowDlg msg = new ShowDlg( this );
    
    msg.setTitle( "Dirección a navegar" );
    msg.setBnt1( R.string.btnAceptar );
    msg.setBnt2( R.string.btnClose );
    
    @SuppressLint( "InflateParams" )
    View body = getLayoutInflater().inflate( R.layout.dlg_get_url, null );
    
    final EditText newURL = body.findViewById( R.id.txtNewUrl );
    newURL.setText( mLastUrl );
    
    msg.setLayout( body );
    msg.show(  new DialogInterface.OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        if( which == DialogInterface.BUTTON_POSITIVE )
          {
          mLastUrl = newURL.getText().toString().toLowerCase();
          if( !mLastUrl.startsWith( "http" ) )
            mLastUrl = "http://" + mLastUrl;
          
          loadPage( mLastUrl );
          }
        }
      } );
    
    return true;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama al tocar el boton de detener la carga de la pagina */
  public void OnStopPageLoad( @SuppressWarnings( "unused" ) View view )
    {
    mWebPg.stopLoading();
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Configura y muestra el boton flotante */
  private void SetFloatBtn()
    {
    FloatingActionButton fab = findViewById( R.id.btnFill );
    fab.hide();
    
    fab.setOnClickListener( new View.OnClickListener()
      {
      @Override public void onClick( View view )
        {
        FillDatos();
        }
      } );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Inyecta las funciones de Javascript necesarias dentro de la pagina */
  private void SetJavascriptFunctions()
    {
    String jsSet = "" +
      "function FindElem( TagName, Name )" +
      "  {" +
      "  var elem = document.all( Name );" +
      "  if( !elem || !elem.length ) return elem;" +
      
      "  for( var i=0; i<elem.length; ++i )" +
      "    if( elem[i].tagName == TagName )" +
      "      return elem[i];" +
      "  }" +
      
      "function FillInput( TagName, Name, Val )" +
      "  {" +
      "  var elem = FindElem( TagName, Name );" +
      "  if( !elem ) return 1;" +
      
      "  elem.value = Val;" +
      "  if( elem.onchange ) elem.onchange( this );" +
      "  return 0;" +
      "  }" +
      
      "function SelectValue( AttrName, sVal )" +
      "  {" +
      "  var elems = document.getElementsByTagName( \"select\" );" +
      "  if( !elems ) return 1;" +
      
      "  for( var i=0; i<elems.length; ++i )" +
      "    {" +
      "    var elem = elems[i];" +
      
      "    if( elem.name == AttrName )" +
      "      {" +
      "      elem.value = sVal;" +
      "      if( elem.onchange ) elem.onchange( this );" +
      "      return 0;" +
      "      }" +
      
      "    return 3;" +
      "    }" +
      "  }" +
  
      "function GetLinks()" +
      "  {" +
      "  var urlPag='';" +
      "  var urlUpd='';" +
      "  var urlDel='';" +
      "  var base = 'https://www.revolico.com';" +
      "  var links = document.getElementsByTagName('a');" +
      "  for( var i=0; i<links.length; ++i )" +
      "    {" +
      "    var attrs = links[i].attributes;" +
      "    var attr = attrs['data-cy'];" +
      "    if( attr )" +
      "      {" +
      "      var link = base + attrs['href'].value;" +
      "      if( attr.value=='linkToDetails' ) urlPag = link;" +
      "      if( attr.value=='linkToUpdate'  ) urlUpd = link;" +
      "      if( attr.value=='linkToDelete'  ) urlDel = link;" +
      "      }" +
      "    }" +
      "  return urlPag + ';' + urlUpd + ';' + urlDel;" +
      "  }" ;

    
    mWebPg.evaluateJavascript( jsSet, null );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Llena el un Tag en en la pagina Web de acuerdo a la información suministrada */
  private void FillTag( final String Tipo, final String Name, final String Value )
    {
    String jsSet;
    
    String Typ = Tipo.toUpperCase();
    if( Tipo.equals( "SELECT" ) )
      jsSet = "SelectValue('" + Name + "','" + Value + "')";
    else
      jsSet = "FillInput('" + Typ + "','" + Name + "','" + Value + "')";
    
    mWebPg.evaluateJavascript( jsSet, new ValueCallback<String>()
      {
      @Override public void onReceiveValue( String value )
        {
        if( !value.equals( "0" ) )
          {
          out.println( " ********** ERROR EVALUANDO JS **************" );
          out.println( "TagName: " + Tipo );
          out.println( "AttrName: " + Name );
          out.println( "Value: " + Value );
          }
        }
      } );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /// Pone todos los datos definidos en el anuncio en la pagina actual
  private void FillDatos()
    {
    AnuncioData Anuncio = App.getActualAnunc();
    if( Anuncio == null ) return;
    
    TextEval eVal = TextEval.TextEvalWithAnucio( Anuncio );
    
    for( int i = 0; i < Anuncio.FillInfo.size(); ++i )
      {
      HtmlInfo info = Anuncio.FillInfo.get( i );
      
      if( info.Txt == null || info.AttrName == null || info.TagName == null )
        {
        out.println( "El dato '" + info.InfoName + "' fue ignorado" );
        continue;
        }
      
      String sVal = eVal.ParseValue( info.Txt, true );
      
      FillTag( info.TagName, info.AttrName, sVal );
      }
    }

  //-----------------------------------------------------------------------------------------------------------------------------------
  /** Se llama cuando se termina de cargar una página */
  private void OnPageLoaded()
    {
    mLoadPanel.setVisibility( View.GONE );

    new Handler().postDelayed( new Runnable()
      {
      @Override public void run()
        {
        invalidateOptionsMenu();
        if(  UrlsNeeded( true  ) ) GetURLs( true );
        else                               GetURLsByUrl( true );
        
        ConfPageType();
        }
      }, 1000 );
    }

  //-----------------------------------------------------------------------------------------------------------------------------------
  /** Configura el Titulo de la página y el botón de acción de acuerdo al tipo de página que se muestra */
  private void ConfPageType()
    {
    PGType tp = getPageTipo();

    mAppBar.setTitle( PGTitle[tp.ordinal()] );
    mAppBar.setSubtitle( (App.getActualAnuncIdx() + 1) + " de " + App.getAnuncCount() );

    FloatingActionButton fab = findViewById( R.id.btnFill );
    
    if( tp == PGType.INSERT ) fab.show();
    else                      fab.hide();
    }

  //-----------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene el tipo de página que se esta mostrando, de acuerdo a su URL */
  private PGType getPageTipo()
    {
    String url = mWebPg.getUrl();
    if( url != null )
      for( int i=0; i<PGName.length; ++i )
        if( url.contains( PGName[i] ) )
          return PGType.values()[i];
    
    return PGType.NONE;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Maneja el proceso de carga de las paginas */
  class MyWebViewClient extends WebViewClient
    //================================================== MyWebViewClient ================================================================
    {
    //-----------------------------------------------------------------------------------------------------------------------------------
    /** Se llama cuando se va a cargar una pagina nueva */
    @Override public boolean shouldOverrideUrlLoading( WebView view, WebResourceRequest request )
      {
      GetURLs( true );
      
      loadPage( request.getUrl().toString() );
      return true;
      }

    //-----------------------------------------------------------------------------------------------------------------------------------
    /** Se llama cuando finaliza la carga de los datos de la página */
    @Override public void onPageFinished( WebView view, String url )
      {
      super.onPageFinished( view, url );
      }

    //-----------------------------------------------------------------------------------------------------------------------------------
    /** Se llama cuando va a descargar un fichero de internet */
    @Override public void onLoadResource( WebView view, final String url )
      {
      runOnUiThread( new Runnable()
        {
        @Override public void run()
          {
          TextView sUrl = mLoadPanel.findViewById( R.id.PgUrl );
          sUrl.setText( url );
          }
        } );
      
      super.onLoadResource( view, url );
//      GetURLs( true );
      }

    //-----------------------------------------------------------------------------------------------------------------------------------
    @Override public void onPageCommitVisible( WebView view, String url )
      {
      super.onPageCommitVisible( view, url );
      
      SetJavascriptFunctions();
      }

    //=============================================== Fin MyWebViewClient ==============================================================
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Maneja la visualisación de los elementos de la página */
  class MyWebChromeClient extends WebChromeClient
    //================================================== MyWebChromeClient ==============================================================
    {
    //-----------------------------------------------------------------------------------------------------------------------------------
    @Override public void onProgressChanged( WebView view, int newProgress )
      {
      if( newProgress == 100 )
        {
        OnPageLoaded();
        }
      else
        mLoadPanel.setVisibility( View.VISIBLE );
        
      super.onProgressChanged( view, newProgress );
      }

    //-----------------------------------------------------------------------------------------------------------------------------------
    @Override public void onReceivedTitle( WebView view, String title )
      {
      super.onReceivedTitle( view, title );
      }

    //-----------------------------------------------------------------------------------------------------------------------------------
    @Override public void getVisitedHistory( ValueCallback<String[]> callback )
      {
      super.getVisitedHistory( callback );
      }
    
    //============================================ Fin MyWebChromeClient ================================================================
    }

  //===================================================================== Fin PublishActivity ====================================================================================
  }
