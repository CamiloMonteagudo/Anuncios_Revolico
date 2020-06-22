package com.BigXSoft.anuncios;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.BigXSoft.anuncios.AnunciosInfo.AnuncioData;
import com.BigXSoft.anuncios.AnunciosInfo.HtmlInfo;

import java.util.HashMap;

public class EditDataActivity extends AppCompatActivity implements TextWatcher, View.OnFocusChangeListener
  //==============================================================================================================================================================================
  /* Actividad para moficiar un anuncio*/
  {
  Toolbar mAppBar;                      // Barra de herramientas de la aplicación

  HashMap<String,HtmlInfo> InfoMap = new HashMap<>();

  // Define los datos relacionados con la información que se va a modificar dentro de la página web
                     enum DTInfo { TITLE          , DESC              , MAIL             , TELEF             , PRECIO             , WRDKEYS1         , WRDKEYS2     }
  static String[] NameEdits    = { "Titulo"       , "Descripción"     , "Mail"           , "Telef"           , "Precio"           , "WordsKey1"      , "WordsKey2"  };
  static String[] DefaultTags  = { "input"        , "textarea"        , "input"          , "input"           , "input"            , ""               , ""           };
  static String[] DefaultAttrs = { "ad-title"     , "ad-description"  , "ad-email"       , "ad-phone"        , "ad-price"         , ""               , ""           };
  static String[] NameField    = { "Valor"        , "Tag"             , "Atributo"  };

  static int[]    idEdits   = { R.id.txtValueTitle, R.id.txtValueAnunc, R.id.txtValueMail, R.id.txtValueTelef, R.id.txtValuePrecio, R.id.txtKeyWords1, R.id.txtKeyWords2,
                                R.id.txtTagTitle  , R.id.txtTagAnunc  , R.id.txtTagMail  , R.id.txtTagTelef  , R.id.txtTagPrecio  , -1               , -1               ,
                                R.id.txtAttrTitle , R.id.txtAttrAnunc , R.id.txtAttrMail , R.id.txtAttrTelef , R.id.txtAttrPrecio , -1               , -1                 };

  // Define los datos asociados directamente a anuncio
                    enum DTAnunc { ID             , URL_INS           , URL_VIEW          , URL_MODIFY          , URL_DEL           }
  static int[]      IdDTAnuncs = { R.id.txtIdAnunc, R.id.txtUrlIsertar, R.id.txtUrlMostrar, R.id.txtUrlModificar, R.id.txtUrlBorrar };
  static String[] NameDTAnuncs = { "Identificador", "URL Insertar"    , "URL Mostrar"     , "URL Modificar"     , "URL Borrar"      };

  static int nDatos = NameEdits.length;

  // Define los conjuntos de vistas que se ocultan/muestan según el nivel de detalle definido por el usuario
  static int[] IDsHtml = { R.id.lbTagTitle   , R.id.txtTagTitle , R.id.lbAttrTitle , R.id.txtAttrTitle, R.id.lbValueTitle, R.id.lbTagAnunc  , R.id.txtTagAnunc , R.id.lbAttrAnunc  ,
                           R.id.txtAttrAnunc , R.id.lbValueAnunc, R.id.lbTagMail   , R.id.txtTagMail  , R.id.lbAttrMail  , R.id.txtAttrMail , R.id.lbValueMail , R.id.lbTagTelef   ,
                           R.id.txtTagTelef  , R.id.lbAttrTelef , R.id.txtAttrTelef, R.id.lbValueTelef, R.id.lbTagPrecio , R.id.txtTagPrecio, R.id.lbAttrPrecio, R.id.txtAttrPrecio,
                           R.id.lbValuePrecio };

  static int[] IDsUrls = { R.id.lbUrls, R.id.lbUrlIsertar, R.id.txtUrlIsertar, R.id.lbUrlMostrar, R.id.txtUrlMostrar, R.id.lbUrlModificar, R.id.txtUrlModificar, R.id.lbUrlBorrar, R.id.txtUrlBorrar };
  
  AnuncioData mAnuncio;

  boolean htmlInfo = false;                                     // Bandera para mostrar/editar la información relacionada con el HTMl a actualizar
  boolean urlsInfo = false;                                     // Bandera para mostrar/editar las URLs de las paginas de anuncios

  int mIdxInfo  = -1;                                            // Indice al tipo de dato localizado con findIdxInfoWithId
  int mOffInfo  = -1;                                            // Indica el dato dentro de tipo de dato localizado con findIdxInfoWithId
  int mIdxAnunc = -1;                                            // Indice al tipo de dato localizado con findIdxAnuncWithId

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @Override protected void onCreate( Bundle savedInstanceState )
    {
    App.Init( getApplicationContext() );                          // Inicializa datos y funciones globales de la aplicación
    mAnuncio = App.getActualAnunc();                              // Obtiene el anuncio actual para editar
    
    super.onCreate( savedInstanceState );
    setContentView( R.layout.activity_edit_data );                // Aplica el layaut de la actuvidad

    setToolBar();                                                 // Configura la barra superior de la actividad
    
    InitEditsDatos();                                             // Inicializa todas las vistas de edicción

    String Url = mAnuncio.Url;                                    // Determina si mostrar las URLs inicialmente o no
    urlsInfo = Url == null || Url.length()==0;
    
    ShowViews( IDsHtml, htmlInfo );                               // Oculta la ediccion de los datos HTML
    ShowViews( IDsUrls, urlsInfo );                               // Oculta la ediccion de las URLs utilizada para la publicación
    SetTitleCount();                                              // Pone el número de caracteres en el titulo
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Inicializa todas las vistas de edicción con los datos del anuncio o con los valores por defecto */
  private void InitEditsDatos()
    {
    if( mAnuncio == null ) return;

    for( DTAnunc dato : DTAnunc.values() )
      initEditAnunc( dato );
      
    CreateInfoMap();
  
    for( DTInfo dato : DTInfo.values() )
      initEditsInfo( dato );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Inicializa el dato asociado directamente al anuncio identificado por dato */
  private void initEditAnunc( DTAnunc dato )
    {
    EditText Edit = setEditAnuncDato( dato );
    Edit.addTextChangedListener( this );
    }

  //☎7✴832✴8768 ☎7✳832✳8768
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Inicializa las vistas de edicción asociadas a la información definida por 'dato' */
  private void initEditsInfo( DTInfo dato )
    {
    int Idx = dato.ordinal();                                       // Toma el indice de definición del tipo de informacion

    EditText Val = setEditInfoFromDato( Idx, 0 );             // Inicializa las vista de edicción con los datos
    EditText Tag = setEditInfoFromDato( Idx, 1 );
    EditText Atr = setEditInfoFromDato( Idx, 2 );
    
    if( Val !=null )                                                // Pone Callback a las vista de edición
      {
      Val.addTextChangedListener( this );
      Val.setOnFocusChangeListener( this );
      }
    
    if( Tag !=null ) Tag.addTextChangedListener( this );
    if( Atr !=null ) Atr.addTextChangedListener( this );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Pone el dato definido por 'idxInfo' y 'nField' en la vista de edicción correspondiente */
  private EditText setEditInfoFromDato( int idxInfo, int nField )
    {
    int idx    = idxInfo + (nField * nDatos);                       // Calcula indice a la lista de identificadores
    int idEdit = idEdits[ idx ];                                    // Obtiene el identificador de la vista de edicción
    
    EditText edit  = findViewById( idEdit );                        // Obtiene la vista de edicción
    if( edit==null )return null;

    String sVal = "";
    HtmlInfo item = InfoMap.get( NameEdits[idxInfo] );              // Obtiene la información por el indice del dato
    boolean noDt = item!=null;                                      // Bandera que indica que no esta la información
    
    switch( nField )                                                // Obtien el valor segun el campo correspondiente
      {
      case 0 : sVal = noDt? item.Txt     : ""                   ; break;
      case 1 : sVal = noDt? item.TagName : DefaultTags [idxInfo]; break;
      case 2 : sVal = noDt? item.AttrName: DefaultAttrs[idxInfo]; break;
      }

    edit.setText( sVal );                                           // Pone el valor obtenido en el editor
    return edit;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Crea un diccionario con la información del anuncio, indenzada por el nombre de la información  */
  private void CreateInfoMap()
    {
    InfoMap = new HashMap<>();
    
    for( HtmlInfo item : mAnuncio.FillInfo )
      InfoMap.put( item.InfoName, item );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama para crear el menu de la appbar (hay que llamar setSupportActionBar antes) */
  @Override public boolean onCreateOptionsMenu( Menu menu )
    {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate( R.menu.edict_data_appbar, menu );
    return true;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Actualiza el estado del menú antes de ser mostrado */
  @Override public boolean onPrepareOptionsMenu( Menu menu )
    {
    boolean chgd = isDataChanged();
    
    menu.findItem( R.id.mnuSaveChanges ).setVisible( chgd );

    menu.findItem( R.id.mnuShowTags ).setChecked( htmlInfo );
    menu.findItem( R.id.mnuShowUrls ).setChecked( urlsInfo );

    setMenuRetoreActual( menu.findItem(R.id.mnuRestoreSource) );
    setMenuInsertMark( menu.findItem(R.id.mnuInsertMark) );
    
    return super.onPrepareOptionsMenu( menu );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Configura la opción de menú de restuar el texto, en función de la edicción en el momento de llamada */
  private void setMenuRetoreActual( MenuItem item  )
    {
    EditText edit = (EditText)getCurrentFocus();
    if( edit==null )
      {
      item.setVisible( false );
      return;
      }
  
    int idEdit = edit.getId();
    if( findIdxInfoWithId( idEdit ) )
      {
      if( notEquals( getEditDato(idEdit), getInfoDato() ))
        {
        item.setTitle( "Texto anterior de " +  NameField[mOffInfo] + " " + NameEdits[mIdxInfo] );
        item.setVisible( true );
        return;
        }
      }
    else if( findIdxAnuncWithId( idEdit ) )
      {
      if( notEquals( DTAnunc.values()[mIdxAnunc] ) )
        {
        item.setTitle( "Texto anterior de " +  NameDTAnuncs[mIdxAnunc] );
        item.setVisible( true );
        return;
        }
      }
  
    item.setVisible( false );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Evalua la vista de edicción que tiene el foco y evelua si se muestra el menu o no */
  private void setMenuInsertMark( MenuItem item )
    {
    EditText edit = (EditText)getCurrentFocus();

    if( edit!=null && findIdxInfoWithId( edit.getId() ) && mOffInfo==0 )
      {
      item.setVisible( true );
      return;
      }

    item.setVisible( false );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama cuando se selecciona una item de la action bar */
  @Override public boolean onOptionsItemSelected( MenuItem item )
    {
    switch( item.getItemId() )
      {
      case R.id.mnuSaveChanges:
        if( UpdateChanges() )
          {
          App.SaveDatos();
          invalidateOptionsMenu();
          }
        return true;
      case R.id.mnuInsertMark:    InsertTextMark();      return true;
      case R.id.mnuRestoreSource: RetoreActualEditing(); return true;
      case R.id.mnuShowTags:      htmlInfo = !htmlInfo; ShowViews( IDsHtml, htmlInfo ); return true;
      case R.id.mnuShowUrls:      urlsInfo = !urlsInfo; ShowViews( IDsUrls, urlsInfo ); return true;
      }

    return super.onOptionsItemSelected( item );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Inserta una marca para sustitución en el editor que tiene el foco */
  private void InsertTextMark()
    {
    ShowDlg msg = new ShowDlg( this );

    msg.setTitle( "Seleccione el Marcador" );
    msg.setBnt2( R.string.btnClose );
    msg.setItemsList( R.array.marks_name );

    
    msg.show( new OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int idx )
        {
        EditText edit = (EditText)getCurrentFocus();
        if( idx >= 0 && edit != null )
          {
          String[] Marcas = getResources().getStringArray( R.array.marks_code);
          String     mark = Marcas[idx];
          
          String txt = edit.getText().toString();
          int      i = edit.getSelectionStart();

          String newTxt = txt.substring( 0,i ) + mark + txt.substring( i );
          edit.setText( newTxt );
          edit.setSelection( i + mark.length() );
          }
        }
      });
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Restaura el texto de la vista que se esta editando en el momento de llamada */
  private void RetoreActualEditing()
    {
    EditText edit = (EditText)getCurrentFocus();
    if( edit == null )  return;

    int idEdit = edit.getId();
    
         if( findIdxAnuncWithId( idEdit ) ) setEditAnuncDato( DTAnunc.values()[mIdxAnunc] );
    else if(  findIdxInfoWithId( idEdit ) ) setEditInfoFromDato( mIdxInfo, mOffInfo );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Busca el indice para la información vinculada a la vista 'idView' (actualiza mIdxInfo y mOffInfo) */
  private boolean findIdxInfoWithId( int idView )
    {
    for( int i=0; i<idEdits.length; ++i )
      if( idEdits[i] == idView )
        {
        int n    = DTInfo.values().length;
        mOffInfo = i / n;
        mIdxInfo = i % n;
        return true;
        }

    mOffInfo = -1;
    mIdxInfo = -1;
    return false;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Busca el indice del dato del aununcio vinculado a la vista 'idView' (actualiza mIdxAnunc) */
  private boolean findIdxAnuncWithId( int idView )
    {
    for( int i=0; i<IdDTAnuncs.length; ++i )
      if( IdDTAnuncs[i] == idView )
        {
        mIdxAnunc = i;
        return true;
        }

    mIdxAnunc = -1;
    return false;
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
      @Override public void onClick( View v ) { CheckChangesAndClose(); }
      } );
    }

  //---------------------------------------------------------------------------------------------------------------------
  // Atiene cuando se oprime una techa en el formulario
  @Override public boolean onKeyDown( int keyCode, KeyEvent event )
    {
    // Chequea si se oprimio la tecla de retroceder y hay paginas en la hostoria
    if( keyCode == KeyEvent.KEYCODE_BACK  )
      {
      CheckChangesAndClose();
      return true;
      }
  
    return super.onKeyDown( keyCode, event );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene todos los datos modificados por el usuario y los incorpora a la estructura de datos */
  private boolean UpdateChanges()
    {
    if( !isDataChanged() ) return false;
    if( !DataValidate()  ) return false;

    for( DTAnunc dato : DTAnunc.values() )                  // Actuliaza datos asociados direcamente al anuncio
      setAnuncDatoFromEdit( dato );
      
    for( DTInfo dato : DTInfo.values() )                    // Actualiza datos de la información del HTML a modificar
      {
      String infoName = NameEdits[ dato.ordinal() ];        // Nombre de la información
      
      HtmlInfo InfoEd = getEditHtmlInfo( dato );            // Información en edicción
      HtmlInfo InfoDt = InfoMap.get( infoName );            // Información en los datos
      
      boolean hasVal = InfoEd.Txt.length()>0;               // Hay información para guardar
      
      if( InfoDt == null )                                  // No existe el dato
        {
        if( !hasVal ) continue;                             // Si no hay texto, ignora el dato
        
        InfoDt =  new HtmlInfo( infoName );                 // Lo crea objeto para los datos que no estan
        mAnuncio.FillInfo.add( InfoDt );                    // Lo agraga a la información del anuncio
        CreateInfoMap();                                    // Actualiza el diccionario de información
        }

      if( hasVal )                                          // Si hay un texto editado
        {
        InfoDt.Txt      = InfoEd.Txt;                       // Actualiza todos los datos
        InfoDt.TagName  = InfoEd.TagName;
        InfoDt.AttrName = InfoEd.AttrName;
        }
      else                                                  // Si no hay ningun texto editado
        {
        mAnuncio.FillInfo.remove( InfoDt);                  // Lo quita
        CreateInfoMap();                                    // Actualiza el diccionario de información
        }
      }

    return true;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Determina si los datos que se estan editando han cambiado con respecto a los datos guardados */
  private boolean isDataChanged()
    {
    if( mAnuncio == null ) return false;

    for( DTAnunc dato : DTAnunc.values() )          // Verifica todos los datos asociados directamente al anuncio
      if( notEquals( dato ) ) return true;

    for( DTInfo dato : DTInfo.values() )            // Verifica todos los datos para maneja el HTML de la página
      if( notEquals( dato ) ) return true;
    
    return false;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Determina si el dato identificado por 'dato' fue modificado durante la sección de edicción */
  private boolean notEquals( DTAnunc dato )
    {
    int IdEdit = IdDTAnuncs[ dato.ordinal() ];

    String sEdit = getEditDato( IdEdit );
    String sDato = getAnuncDato( dato );

    return notEquals( sEdit, sDato );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Determina si la información definida por 'dato' ha cambiado */
  private boolean notEquals( DTInfo dato )
    {
    HtmlInfo Info1 = InfoMap.get( NameEdits[ dato.ordinal() ] );
    HtmlInfo Info2 = getEditHtmlInfo( dato );
  
    if( Info1==null && Info2==null ) return false;
  
    if( Info1==null ) return !( Info2.Txt==null || Info2.Txt.length()==0 );
    if( Info2==null ) return !( Info1.Txt==null || Info1.Txt.length()==0 );
  
    if( notEquals( Info1.InfoName, Info2.InfoName ) ) return true;
    if( notEquals( Info1.TagName , Info2.TagName  ) ) return true;
    if( notEquals( Info1.AttrName, Info2.AttrName ) ) return true;
  
    return notEquals( Info1.Txt, Info2.Txt );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Determina si las dos cadenas son diferentes */
  private boolean notEquals( String sVal1, String sVal2 )
    {
    if( (sVal1==null || sVal1.length()==0) &&
        (sVal2==null || sVal2.length()==0) ) return false;
    if( sVal1 == null  )                     return true;
  
    return !sVal1.equals( sVal2 );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene un objeto HtmlInfo con la información del tipo 'dato' que se esta editando */
  private HtmlInfo getEditHtmlInfo( DTInfo dato )
    {
    int idx1 = dato.ordinal();
    int idx2 = idx1 + nDatos;
    int idx3 = idx2 + nDatos;
    
    HtmlInfo item = new HtmlInfo( NameEdits[idx1] );
  
    item.Txt      = getEditDato( idEdits[idx1] );
    item.TagName  = getEditDato( idEdits[idx2] );
    item.AttrName = getEditDato( idEdits[idx3] );

    return item;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** CallBack que se llama cada vez que se cambia alguno de los editores de texto */
  @Override public void onTextChanged( CharSequence s, int st, int b, int c )
    {
    EditText edit = (EditText)getCurrentFocus();
    if( edit!=null && edit.getId()==R.id.txtValueTitle )
      SetTitleCount();
    
    invalidateOptionsMenu();
    }
  @Override public void onFocusChange( View v, boolean hasFocus             ) { invalidateOptionsMenu(); }
  @Override public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}
  @Override public void afterTextChanged( Editable s ) {}

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra el número de caracteres contenido en el titulo */
  private void SetTitleCount()
    {
    EditText vTitle      = findViewById( R.id.txtValueTitle );
    TextView vTitleCount = findViewById( R.id.lbTitleCount  );
  
    int Count = vTitle.getText().length();
    vTitleCount.setText(  String.valueOf( Count ) );
  
    vTitleCount.setTextColor( Count>120? 0xFFFF0000 : 0xFF000000  );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Actualiza la viata la vista asociada a 'dato' con el valor asociado al anuncio actual para 'dato' */
  private EditText setEditAnuncDato( DTAnunc dato )
    {
    int idx = dato.ordinal();
    int IdView = IdDTAnuncs[idx];
    
    EditText txt = findViewById( IdView );
    switch( dato )
      {
      case ID         : txt.setText( mAnuncio.ID     ); break;
      case URL_INS    : txt.setText( mAnuncio.Url    ); break;
      case URL_VIEW   : txt.setText( mAnuncio.UrlPag ); break;
      case URL_MODIFY : txt.setText( mAnuncio.UrlUpd ); break;
      case URL_DEL    : txt.setText( mAnuncio.UrlDel ); break;
      }
    
    return txt;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Actualiza el valor del dato del anuncio actual asociado a 'dato' con el de la vista correspondiente */
  private void setAnuncDatoFromEdit( DTAnunc dato )
    {
    String sVal = getEditDato( IdDTAnuncs[dato.ordinal()] );
    switch( dato )
      {
      case ID         : mAnuncio.ID     = sVal; break;
      case URL_INS    : mAnuncio.Url    = sVal; break;
      case URL_VIEW   : mAnuncio.UrlPag = sVal; break;
      case URL_MODIFY : mAnuncio.UrlUpd = sVal; break;
      case URL_DEL    : mAnuncio.UrlDel = sVal; break;
      }
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Retorna el dato del anuncio identificado por 'dato' */
  private String getAnuncDato( DTAnunc dato )
    {
    switch( dato )
      {
      case ID         : return( mAnuncio.ID     );
      case URL_INS    : return( mAnuncio.Url    );
      case URL_VIEW   : return( mAnuncio.UrlPag );
      case URL_MODIFY : return( mAnuncio.UrlUpd );
      case URL_DEL    : return( mAnuncio.UrlDel );
      }
  
    return null;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Retorna el dato de informacion vigente definido por mIdxInfo y mOffInfo (despues de findIdxInfoWithId) */
  private String getInfoDato()
    {
    HtmlInfo Info = InfoMap.get( NameEdits[ mIdxInfo ] );
    if( Info != null )
      switch( mOffInfo )
        {
        case 0 : return Info.Txt;
        case 1 : return Info.TagName;
        case 2 : return Info.AttrName;
        }
    
    return null;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Busca la viata con el identificado 'IdView' y retorna el texto que tiene */
  private String getEditDato( int IdView )
    {
    EditText txt = findViewById( IdView );
    if( txt == null  ) return null;

    return String.valueOf( txt.getText() );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra/Oculta todas las vistas dadas en 'IdViews' */
  private void ShowViews( int[] IdViews, boolean show )
    {
    int vis = show? View.VISIBLE : View.GONE;
    
    for( int id : IdViews )
      findViewById( id ).setVisibility( vis );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Valida los datos para comprobar que estan correctos */
  private boolean DataValidate()
    {
    if( !ValidateTitle() ) return false;
    if( !ValidateMail()  ) return false;
    
    if( !ValidateUrl( R.id.txtUrlIsertar   ) ) return false;
    if( !ValidateUrl( R.id.txtUrlModificar ) ) return false;
    
    return ValidateUrl( R.id.txtUrlBorrar );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Valida que el titulo del anuncio no exceda los 120 caracteres */
  private boolean ValidateTitle()
    {
    EditText vTitle = findViewById( R.id.txtValueTitle );

    int Count = vTitle.getText().length();
    if( Count == 0 )
      ShowError( vTitle, R.string.msjErrorTitleVacio );
    else if( Count > 120 )
      ShowError( vTitle, R.string.msjErrorTitle );
      
    return Count>0 && Count <= 120;
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Valida que el correo electronico tenga un formato adecuado */
  private boolean ValidateMail()
    {
    EditText vMail = findViewById( R.id.txtValueMail );
    String    txt  = vMail.getText().toString().trim();
  
    if( txt.length() == 0  )
      {
      ShowError( vMail, R.string.msgErrorMailVacio );
      return false;
      }
    
    if( txt.toUpperCase().equals("{EMAIL}") ) return true;
  
    boolean mailOk = txt.matches("\\w+(\\.\\w+)*@\\w+(\\.\\w{1,3})+" );
    if( !mailOk )
      ShowError( vMail, R.string.msgErrorMail );
    
    return mailOk;
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Valida si el contenido de la vista con identificador 'idUrl' es una URL */
  private boolean ValidateUrl( int idUrl )
    {
    EditText vUrl = findViewById( idUrl );
    String    txt = vUrl.getText().toString().trim();
    if( txt.length() == 0 )
      {
      boolean Insert = idUrl == R.id.txtUrlIsertar;
      
      if( Insert ) ShowError( vUrl, R.string.msgErrorUrlVacio );
      return !Insert;
      }

    boolean urlOk = txt.matches("(https?://)?(\\w+\\.)+\\w{1,3}(/[\\w-]*)*(\\.\\w+)?(\\?.*)?" );
    if( !urlOk )
      ShowError( vUrl, R.string.msgErrorUrl );

    return urlOk;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra un mensaje de error y pone el foco en la vista con el error */
  private void ShowError( EditText edit, int idMsj )
    {
    SetFocus( edit );

    ShowDlg.Msg( this, idMsj );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Pone el foco en la vista de edición dada y muestra el teclado */
  private void SetFocus( EditText edit )
    {
    edit.setFocusableInTouchMode(true);
    edit.requestFocus();

    final InputMethodManager inMethodMng = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
    if( inMethodMng != null)
      inMethodMng.showSoftInput( edit, InputMethodManager.SHOW_IMPLICIT );
    }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama cuando la se va a cerrar la actividad y verifica si hay cambios sin guardar*/
  private void CheckChangesAndClose()
    {
    if( !isDataChanged() )
      {
      finish();
      return;
      }

    ShowDlg msg = new ShowDlg( this );
    
    msg.setTitle  ( R.string.editCloseTilte );
    msg.setMessage( R.string.editCloseMsg );
    
    msg.setBnt1( R.string.btnGuardar );
    msg.setBnt2( R.string.bntDescartar );
    msg.setBnt3( R.string.btnCancel );

    msg.show( new OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        switch( which )
          {
          case DialogInterface.BUTTON_POSITIVE:       // (Boton Guardar)
            if( UpdateChanges() )                     // Si se cambiaron los datos correctamente
              {
              App.SaveDatos();                        // Guarda los cambios permanentemente
              finish();                               // Cierra la actividad
              }
            break;
          case DialogInterface.BUTTON_NEGATIVE:       // (Boton Descartar)
            finish();                                 // Cierra la actividad incondicionalmente
            break;
          case DialogInterface.BUTTON_NEUTRAL:        // (Boton Cancelar) no hace nada
          }
        }
      } );
    }
  
  //===============================================================================================================================================================================
  }
