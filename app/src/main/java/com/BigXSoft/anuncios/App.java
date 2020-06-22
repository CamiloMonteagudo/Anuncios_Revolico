package com.BigXSoft.anuncios;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.BigXSoft.anuncios.AnunciosInfo.AnuncioData;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

//------------------------------------------------------------------------------------------------------------------------
// Esta clase maneja los objetos
final class App
  //======================================================================================================================
  {
  @SuppressLint( "StaticFieldLeak" )
  private static Context      Ctx;
  private static AnunciosInfo Anuncios;                 // Información sobre los anuncios que se van a publicar
  private static int          nowAnunc;                 // Número del anuncio actual
  private static String       lastFile;                 // Nombre del ultimo fichero de anuncio utilizado
  private static int          count;                    // Número de anuncios cargados

  private App(){}

  static int DataVer;                                 // Llava un seguimiento los cambios realizados en los datos de la App
  //---------------------------------------------------------------------------------------------------------------------
  /** Inicializa los datos de la aplicación */
  static void Init( Context appCtx )
    {
    Ctx = appCtx;

    if( Anuncios == null )
      {
      getSetting();
      LoadAnuncFile( lastFile );
      }
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Carga un fichero de auncio con el nombre especificado desde el directorio assets */
  static boolean LoadAnuncFile( String file )
    {
    AnunciosInfo NewAnunc = AnunciosInfo.LoadFromFile( Ctx, file );
    if( NewAnunc != null  )
      {
      Anuncios = NewAnunc;
      count = Anuncios.Items.size();
      if( !lastFile.equals( file ) )
        {
        nowAnunc = 0;
        lastFile = file;
        saveSetting();
        }

      ++DataVer;
      return true;
      }
    else
      FloatMsg( "El fichero '" + file + "' no puedo ser cargado como un fichero de Anuuncios" );

    if( Anuncios==null )
      {
      count = 0;
      nowAnunc = -1;
      }
    
    return false ;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene la configuración de la aplicación */
  private static void getSetting()
    {
    SharedPreferences cfg = Ctx.getSharedPreferences( "AnunnciosApp", Context.MODE_PRIVATE );

    nowAnunc = cfg.getInt( "nowAnunc", 0);
    lastFile = cfg.getString("lastFile", "Todo.txt" );
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Guarda la configuración de la aplicación */
  private static void saveSetting()
    {
    Editor ed = Ctx.getSharedPreferences( "AnunnciosApp", Context.MODE_PRIVATE ).edit();

    ed.putInt( "nowAnunc", nowAnunc );
    ed.putString("lastFile", lastFile );

    ed.apply();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene el indice del anuncio actual */
  static int getActualAnuncIdx()
    {
    return nowAnunc;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene el objeto que representa el anuncio actual */
  static AnuncioData getActualAnunc()
    {
    if( nowAnunc < 0 || nowAnunc >= count ) return null;

    return Anuncios.Items.get( nowAnunc );
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene la cantidad de anuncios que hay cargado */
  static int getAnuncCount()
    {
    return count;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Indica si el anuncio actual es el primero */
  static boolean isFirstAnunc()
    {
    return nowAnunc == 0;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Pone el primer anuncio con el actual */
  static void setFirstAnunc()
    {
    nowAnunc = 0;
    saveSetting();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Indica si el anuncio actual es el último */
  static boolean isLastAnunc() { return nowAnunc == count-1; }

  //---------------------------------------------------------------------------------------------------------------------
  /** Pone al ultimo anuncio como el anuncio actual */
  static void setLastAnunc()
    {
    nowAnunc = count-1;
    saveSetting();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Pone al anuncio anterior como el anuncio actual */
  static void setPrevAnunc()
    {
    if( nowAnunc <= 0 ) return;
    
    --nowAnunc;
    saveSetting();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Pone el proximo anuncio como el anuncio actual */
  static void setNextAnunc()
    {
    if( nowAnunc < count-1 )
      {
      ++nowAnunc;
      saveSetting();
      }
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Pone un mensaje flotante en la parte de abajo de la pantalla */
  static void FloatMsg( int idMsg )
    {
    FloatMsg( Ctx.getString( idMsg ) );
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Pone un mensaje flotante en la parte de abajo de la pantalla */
  static void FloatMsg( String sMsg )
    {
    Toast.makeText( Ctx, sMsg, Toast.LENGTH_SHORT )
      .show();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene el titulo del anuncio actual. evaluando las expresiones o no */
  static CharSequence getAnuncTitle( boolean eval, boolean escape )
    {
    if( nowAnunc<0 || nowAnunc>=count ) return "";

    AnuncioData Anuncio = Anuncios.Items.get( nowAnunc );

    String txt = Anuncio.getInfoTxt( "Titulo" );
    if( eval )
      txt = TextEval.TextEvalWithAnucio( Anuncio )
                    .ParseValue( txt,  escape );
    return txt;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene la descripción del anuncio actual. evaluando las expresiones o no */
  static CharSequence getAnuncDesc( boolean eval, boolean escape )
    {
    if( nowAnunc<0 || nowAnunc>=count ) return "";

    AnuncioData Anuncio = Anuncios.Items.get( nowAnunc );

    String txt = Anuncio.getInfoTxt("Descripción" );
    if( eval )
      txt = TextEval.TextEvalWithAnucio( Anuncio )
                    .ParseValue( txt, escape  );
    return txt;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene el nombre del fichero, por defecto toma el nombre del anuncio actual */
  static String getOnlyName( String file )
    {
    String name = file==null? lastFile : file;
    if( name.endsWith( ".txt" ) )
      name = name.substring( 0, name.length()-4 );
    
    return name;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Actualiza la URL de actualización para el anuncio actual */
  static void setURLs( String sUrlPag, String sUrlUpd, String sUrlDel )
    {
    if( nowAnunc<0 || nowAnunc>=count ) return;

    AnuncioData anunc = Anuncios.Items.get( nowAnunc );

    anunc.UrlPag = sUrlPag;
    anunc.UrlUpd = sUrlUpd;
    anunc.UrlDel = sUrlDel;

    SaveDatos();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Actualiza la URL de actualización para el anuncio actual */
  static boolean SaveDatos( )
    {
    ++DataVer;
    return Anuncios.SaveDatos();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Borra el anuncio actual */
  static void DeletaAnunc()
    {
    if( nowAnunc<0 || nowAnunc>=count ) return;
    
    Anuncios.Items.remove( nowAnunc );

    count = Anuncios.Items.size();
    if( nowAnunc>= count )
      nowAnunc = count-1;

    saveSetting();
    SaveDatos();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Borra el anuncio actual */
  static boolean InsertAnunc()
    {
    if( Anuncios==null ) return false;
    nowAnunc = Anuncios.InsertAnuncio( nowAnunc );
    count = Anuncios.Items.size();
  
    saveSetting();
    return SaveDatos();
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Borra el anuncio actual */
  static boolean CreateAnuncFile( String FileName )
    {
    FileName = ValidateFileName( FileName );
    if( FileName==null ) return false;
    
    AnunciosInfo AnunciosNew = new AnunciosInfo( Ctx, FileName );
    AnunciosNew.InsertAnuncio( 0 );

    if( !AnunciosNew.SaveDatos() ) return false;

    Anuncios = AnunciosNew;
    nowAnunc = 0;
    count    = Anuncios.Items.size();
    lastFile = FileName;

    ++DataVer;
    saveSetting();
    
    return true;
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Comprueba que 'fileName' sea un nombre valido para un fichero de anuncio nuevo, si es posible hace correcciones
   * sobre el nombre, si no es posible returna null */
  private static String ValidateFileName( String fileName )
    {
    if( fileName==null || fileName.length()==0 ) return null;
    
    if( !fileName.endsWith( ".txt" ) ) fileName = fileName + ".txt";

    String[]   files = GetAnuncioFiles();                                           // Lista de ficheros de anuncios disponibles
    String fileTitle = getOnlyName( fileName ).toLowerCase();                       // Nombre sin extesión y en minusculas

    for( String file: files)                                                        // Busca si ya el fichero existe
      if(  fileTitle.toLowerCase().equals( file ) )
        return null;
    
    return fileName;                                                                // Retorna el nombre del fichero
    }

  //---------------------------------------------------------------------------------------------------------------------
  /** Obtiene la lista de anuncios disponibles */
  static String[] GetAnuncioFiles()
    {
    HashSet<String> FilesSet = new HashSet<>();
    try
      {
      AddAnucFiles( FilesSet, Ctx.getResources().getAssets().list("") );          // Ficheros en el directorio Assets
      AddAnucFiles( FilesSet, Ctx.getFilesDir().list() );                               // Ficheros en el directorio Files
      }
    catch( IOException ignore ) {}
  
    return FilesSet.toArray( new String[0] );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Chequea todos los ficheros en 'files' y si es un fichero de anuncio lo agrega a 'filesSet', garantizando que no haya ficheros repetidos */
  static private void AddAnucFiles( HashSet<String> filesSet, @Nullable String[] files )
    {
    if( files == null ) return;
  
    for( String file : files )
      {
      if( !file.endsWith(".txt") ) continue;
    
      String fName = getOnlyName( file );
      filesSet.add( fName );
      }
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Determina si el fichero 'file' esta en el directorio Assets, si no suministra 'file' de busca el fichero de auncios actual */
  static boolean FileInAssets( String file )
    {
    try
      {
      if( file==null || file.length()==0 ) file = lastFile;
      
      String[] files =  Ctx.getResources().getAssets().list( "" );
      return FindInArray( file, files );
      }
    catch( IOException ignore ) {}

    return false;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Determina si el fichero 'file' esta en el directorio Files, si no suministra 'file' de busca el fichero de auncios actual */
  static boolean FileInFiles( String file )
    {
    if( file==null || file.length()==0 ) file = lastFile;

    String[] files = Ctx.getFilesDir().list();
    return FindInArray( file, files );
    }

  private static int  lstFoundIdx;                 // Indice del la ultima cadena encontrada
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Busca la cadena 'Str' en la lista 'list' */
  private static boolean FindInArray( String Str, String[] list )
    {
    lstFoundIdx = -1;
    if( list==null || Str==null ) return false;

    for( int i=0; i<list.length; ++i )
      if( list[i].equals( Str ) )
        {
        lstFoundIdx = i;
        return true;
        }
  
    return false;
    }
  
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Borra el fichero de anuncio actual */
  static boolean DeleteAnuncFile()
    {
    String[] files = GetAnuncioFiles();
    FindInArray( getOnlyName( null ), files );
    
    File fl = new File( Ctx.getFilesDir(), lastFile );
    if( fl.delete() )
      {
      if( ++lstFoundIdx >= files.length ) lstFoundIdx = files.length-2;
      if( lstFoundIdx >= 0 )
        LoadAnuncFile(  files[lstFoundIdx] + ".txt" );
      
      return true;
      }
    
    return false;
    }

  //======================================================================================================================
  }
