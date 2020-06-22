package com.BigXSoft.anuncios;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

class AnunciosInfo
  //============================================= AnunciosInfo  ========================================================
  {
  ArrayList<AnuncioData> Items;                               // Lista de todas las modificaciones que hay que hacer en la pagina
  private int            nowLine;                             // Linea actual que se esta analizando
  private String         mFileName;                           // Nombre del fichero donde se leyeron los datos
  private Context        mCtx;                                // Contexto de la aplicación
  
  //---------------------------------------------------------------------------------------------------------------------
  /** Crea un objeto vacio */
  AnunciosInfo( Context ct, String fileName )
    {
    Items = new ArrayList<>();
    
    mFileName = fileName;
    mCtx      = ct;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene los datos de los anuncios desde un fichero */
  static AnunciosInfo LoadFromFile( Context ct, String fileName )
    {
    AnunciosInfo Datos = new AnunciosInfo( ct, fileName );    // Crea el objeto para mantener los datos de los anuncios
    
    ArrayList<String> Lines = Datos.ReadLinesOfFile();        // Lee todas las lines al fichero de anuncios
    if( Lines==null ) return null;                            // No pudo leer el fichero
    
    if( !Datos.ParseLines( Lines ) ) return null;             // No analizar correctamente todas las lines
    if(  Datos.Items.size()== 0  )   return null;             // No se puedo obtener ningún anuncio valido
    
    return Datos;                                             // Retorna los datos de todos los anuncios
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Analiza todas las lineas del fichero con los dato de los anuncios */
  private boolean ParseLines( ArrayList<String> lines )
    {
    nowLine = 0;
    AnuncioData LastAnuncio = null;

    while( nowLine<lines.size() )
      {
      AnuncioData anuncio = AnuncioFromLines( lines, LastAnuncio );
      if( anuncio == null ) return false;
      
      Items.add( anuncio );
      LastAnuncio = anuncio;
      }
    
    return true;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Analiza todas las lineas del fichero con los dato de los anuncios */
  int InsertAnuncio( int idx )
    {
    int count  = Items.size();
    
    if( idx >= count ) idx = count-1;
    if( idx <  0     ) idx = 0;

    AnuncioData lastAnunc = count>0 ? Items.get(idx++) : null;
    AnuncioData  newAnunc = new AnuncioData( lastAnunc );
    
    if( count>0 ) Items.add( idx , newAnunc );
    else          Items.add( newAnunc );
    
    return idx;
    }
  
  //---------------------------------------------------------------------------------------------------------------------
  /** Analiza toda la información de un anuncio a partir de la linea actual */
  private AnuncioData AnuncioFromLines( ArrayList<String> lines, AnuncioData LastAnuncio )
    {
    AnuncioData Info = new AnuncioData( LastAnuncio );

    while( nowLine < lines.size() )
      {
      String line = lines.get(nowLine).trim();

      if( line.length() == 0 ) { ++nowLine; continue; }

      boolean GetAnucio = Info.ParseLine( line );
      ++nowLine;

      if( GetAnucio )
        {
        int lstLine = Info.GetTitleAndDescription( lines, nowLine );
        if( lstLine > 0 )
          nowLine = lstLine;
        
        return Info;
        }
      }

    return null;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Trata de abrir el fichero de definición de anuncio desde el almacenamiento local */
  private InputStreamReader OpenFileLocal()
    {
    try
      {
      FileInputStream inFl = mCtx.openFileInput( mFileName );
      return new InputStreamReader( inFl );
      }
    catch( IOException e ) { return null; }
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Trata de abrir el fichero de definición de anuncio desde el directorio Assets */
  private InputStreamReader OpenFileAssets() throws IOException
    {
    InputStream outFl = mCtx.getAssets().open( mFileName );
    return new InputStreamReader( outFl );
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene las parte de la cadena que representa el identificador del anuncio */
  @Nullable private ArrayList<String> ReadLinesOfFile()
    {
    ArrayList<String> lines = new ArrayList<>();
  
    try
      {
      InputStreamReader    inStrm = OpenFileLocal();
      if( inStrm == null ) inStrm = OpenFileAssets();

      BufferedReader reader = new BufferedReader( inStrm );
      for(;;)
        {
        String line = reader.readLine();
        if( line == null ) break;
      
        lines.add( line );
        }
    
      reader.close();
      return lines;
      }
    catch( IOException e ) { return null; }
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene todos los datos de los anuncio en forma de texto */
  private String DatosToText()
    {
    StringBuilder text = new StringBuilder( 20 * 1024 );
    AnuncioData lastAnunc = new AnuncioData( null );
  
    for( int i=0; i<Items.size(); ++i )
      {
      AnuncioData Anunc = Items.get(i);
    
      if( !Anunc.ToText( text, lastAnunc ) ) return "";
    
      lastAnunc = Anunc;
      }
    
    return text.toString();
    }
  
  //---------------------------------------------------------------------------------------------------------------------
  /** Guarda todos los datos de manera permanente */
  boolean SaveDatos()
    {
    String text = DatosToText();
    if( text.length()==0 ) return false;
    
    try
      {
      FileOutputStream     outFl = mCtx.openFileOutput( mFileName, Context.MODE_PRIVATE );
      OutputStreamWriter outStrm = new OutputStreamWriter( outFl );

      outStrm.write( text );
      outStrm.close();
      }
    catch( Exception e ) { return false; }

    return true;
    }

  /// Datos de toda la información relacionada con un anuncio
  static class AnuncioData
    //====================================  AnuncioData  ==============================================================
    {
    String ID;                                   // Identificador del anuncio
    String Url;                                  // Url de la pagina para publecar el anuncio
    String UrlPag;                               // Url de la pagina que muestra el anuncio
    String UrlUpd;                               // Url para actualizar el anuncio
    String UrlDel;                               // Url pra borrar el anuncio
    
    ArrayList<HtmlInfo> FillInfo;                // Lista de todas las modificaciones que hay que hacer en la pagina

    //------------------------------------------------------------------------------------------------------------------
    /** Construye un nuevo anuncio con todos los datos del anucio anterior */
    AnuncioData( AnuncioData Last )
      {
      FillInfo = new ArrayList<>();

      if( Last != null )
        {
        ID  = Last.ID;
        Url = Last.Url;

        for( HtmlInfo item : Last.FillInfo )
          FillInfo.add( item.GetCopy() );
        }
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Obtiene el titulo del anuncio */
    String getInfoTxt( String InfoName )
      {
      for( HtmlInfo item : FillInfo )
        if( item.InfoName.equals( InfoName ) )
          return item.Txt;

      return "";
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Analiza la informacion de una linea en el fichero informacion de los anuncios */
    boolean ParseLine( String line )
      {
      if( !line.startsWith( "// " ) ) return false;

      line = line.substring( 3 );

      String[] CmdVal = line.split("=", 2 );
      if( CmdVal.length < 2) return false;

      String[] NameInfo = CmdVal[0].split("-" );
      if( NameInfo.length < 2 ) return false;

      String ItemName = NameInfo[0].trim();
      String ItemInfo = NameInfo[1].trim();
      String ItemVal  = CmdVal[1].trim();

      switch( ItemName )
        {
        case "Url"    : Url    = ItemVal;          return false;
        case "UrlPag" : UrlPag = ItemVal;          return false;
        case "UrlUpd" : UrlUpd = ItemVal;          return false;
        case "UrlDel" : UrlDel = ItemVal;          return false;
        case "Anuncio": ID     = GetID( ItemVal ); return true;
        }

      AddHtmlItemName( ItemName, ItemInfo, ItemVal );
      return false;
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Crea un objeto con informacion para poner en la pagina web */
    void AddHtmlItemName( String ItemName, String ItemInfo, String ItemVal )
      {
      HtmlInfo item = null;

      for( HtmlInfo itm  : FillInfo )
        if( itm.InfoName.equals( ItemName ) )
          { item = itm; break; }

      if( item == null )
        {
        item = new HtmlInfo( ItemName );
        FillInfo.add( item );
        }

      switch( ItemInfo )
        {
        case "TagName" : item.TagName  = ItemVal; break;
        case "AttrName": item.AttrName = ItemVal; break;
        case "Txt"     : item.Txt      = ItemVal; break;
        default:
          System.out.println( "La información '" + ItemName + "-" + ItemInfo + " fue ignorada" );
        }
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Obtiene el titulo y la descripción del anuncio */
    int GetTitleAndDescription( ArrayList<String> lines, int idx )
      {
      int count = lines.size();
      
      if( idx >= count ) return -1;

      String Title = lines.get( idx++ );
      StringBuilder Desc = new StringBuilder( 1500 );

      while( idx < count )
        {
        String line = lines.get( idx );
        if( line.startsWith("//") )   break;

        Desc.append( line );
        Desc.append( '\n' );
        ++idx;
        }

      int lstChar = Desc.length()-1;
      if( lstChar>=0 && Desc.charAt(lstChar) == '\n' )
        Desc.deleteCharAt( lstChar );

      AddHtmlItemName( "Titulo", "Txt", Title );
      AddHtmlItemName( "Descripción", "Txt", Desc.toString() );

      return idx;
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Obtiene las parte de la cadena que representa el identificador del anuncio */
    String GetID( String itemVal )
      {
      int i = itemVal.length()-1;
      for( ; i>=0; --i )
        if( itemVal.charAt(i) != '-' )
          break;
        
      if( i <= 0 ) return "";
      return itemVal.substring( 0, i );
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Crea una representación del objeto en forma de texto */
    boolean ToText( StringBuilder text, AnuncioData lastAnunc )
      {
      if( Url != null && lastAnunc != null && !Url.equals( lastAnunc.Url ) )
        text.append("// Url-Txt = "   ).append(Url   ).append('\n');

      if( UrlPag != null && UrlPag.length()!=0 ) text.append("// UrlPag-Txt = ").append(UrlPag).append('\n');
      if( UrlUpd != null && UrlUpd.length()!=0 ) text.append("// UrlUpd-Txt = ").append(UrlUpd).append('\n');
      if( UrlDel != null && UrlDel.length()!=0 ) text.append("// UrlDel-Txt = ").append(UrlDel).append('\n');

      HashMap<String,String> LastTags = new HashMap<>( );
      if( lastAnunc != null )
        for( HtmlInfo info : lastAnunc.FillInfo  )
          {
          LastTags.put(  info.InfoName + "-TagName" , info.TagName  );
          LastTags.put(  info.InfoName + "-AttrName", info.AttrName );
          LastTags.put(  info.InfoName + "-Txt"     , info.Txt      );
          }
      
      for( HtmlInfo info : FillInfo  )
        if( !info.ToText( text, LastTags ) )
          return false;

      String sID    = ID!=null ? ID : "";
      String sTitle = LastTags.get( "Titulo" );
      String sDesc  = LastTags.get( "Descripción" );
      
      text.append("// Anuncio-Txt = ").append(sID).append(" --------------------------------------------------------------------------------------------------------------------------------\n");
      
      if( sTitle!=null ) text.append( sTitle ).append( "\n" );
      if(  sDesc!=null ) text.append( sDesc  ).append( "\n" );
      
      return true;
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Determina si el anuncio tiene los datos minimos requeridos */
    boolean DatosRequeridos()
      {
      if( getInfoTxt( "Mail"   ).length() == 0 ) return false;
      if( getInfoTxt( "Titulo" ).length() == 0 ) return false;

      return Url != null && Url.length() != 0;
      }

    //====================================  AnuncioData  ==============================================================
    }

  /// Datos de toda la información relacionada con un anuncio
  static class HtmlInfo
    //====================================  HtmlInfo  ==================================================================
    {
    String InfoName;                            // Nombre de la información que se va a poner en la pagina web
    String TagName;                             // Nombre del tag donde hay que poner la información
    String AttrName;                            // Nombre del atributo name que identifica al tag
    String Txt;                                 // Texto que hay que colocar en la página web

    //------------------------------------------------------------------------------------------------------------------
    /// Crea un objeto con el nombre de la información que se va a manejar
    HtmlInfo( String infoName )
      {
      this.InfoName = infoName;
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Crea una copia del objeto y la retorna */
    HtmlInfo GetCopy()
      {
      HtmlInfo CpyInfo =  new HtmlInfo( InfoName );

      CpyInfo.TagName  = TagName;
      CpyInfo.AttrName = AttrName;

      if( InfoName.equals("Titulo") || InfoName.equals("Descripción") ) CpyInfo.Txt = "";
      else                                                              CpyInfo.Txt = Txt;

      return CpyInfo;
      }

    //------------------------------------------------------------------------------------------------------------------
    /** Crea una representación del objeto en forma de texto */
    boolean ToText( StringBuilder text, HashMap<String,String> LastTags )
      {
      if( NotInLast( "TagName", TagName, LastTags ) )
        text.append("// ").append(InfoName).append("-TagName = ").append(TagName).append('\n');

      if( NotInLast( "AttrName", AttrName, LastTags ) )
        text.append("// ").append(InfoName).append("-AttrName = ").append(AttrName).append('\n');

      if( InfoName.equals( "Titulo" ) )
        {
        LastTags.put( "Titulo", Txt );
        return true;
        }
      
      if( InfoName.equals( "Descripción" ) )
        {
        LastTags.put( "Descripción", Txt );
        return true;
        }

      if( NotInLast( "Txt", Txt, LastTags ) )
        text.append("// ").append(InfoName).append("-Txt = ").append(Txt).append('\n');
      
      return true;
      }
    
    //------------------------------------------------------------------------------------------------------------------
    /** Determina que el Tag definido por 'name = value' no esta en el conjunto 'LastTags' */
    boolean NotInLast( String name, String value, HashMap<String,String> LastTags )
      {
      if( value==null || value.length()==0 ) return false;  // Si no hay valor se considera que estan
      
      String elem = InfoName + "-" + name;            // Forma el nombre del elemento
      String sVal = LastTags.get( elem );             // Lo busca en el anuncio anterior
      
      return sVal==null || !sVal.equals(value);       // Si no se encuentra o el valor es diferente
      }

    //====================================  HtmlInfo  ==================================================================
    }

  //============================================= AnunciosInfo  ========================================================
  }
