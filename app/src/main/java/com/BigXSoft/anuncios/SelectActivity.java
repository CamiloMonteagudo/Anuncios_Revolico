package com.BigXSoft.anuncios;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.BigXSoft.anuncios.AnunciosInfo.AnuncioData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class SelectActivity extends AppCompatActivity
  //====================================================================== SelectActivity =========================================================================================
  {
  TextView mTxtTitle;                 // Titulo del anuncio actual
  TextView mTxtBody;                  // Cuerpo del anuncio actual
  Toolbar  toolbar;                   // Toolbar de la a aplicación
  
  boolean mOver = false;              // Bandera para hacer una pausa cuando el ununcio actua lleva a un limite
  private int ShowDataVer;            // Guarda la versión de los datos que se estan mostrando

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  @Override protected void onCreate(Bundle savedInstanceState)
    {
    App.Init( getApplicationContext() );                        // Inicializa datos y funciones globales de la aplicación

    super.onCreate( savedInstanceState );
    setContentView( R.layout.activity_sel_anuncio );

    toolbar = findViewById( R.id.toolbar );
    setSupportActionBar( toolbar );

    mTxtTitle = findViewById( R.id.txtAncTitle );
    mTxtBody = findViewById( R.id.txtAncBody );

    setNavigation();
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama en el momento que se va a mostrar la aplicación al usuario */
  @Override protected void onResume()
    {
    if( ShowDataVer != App.DataVer )
      {
      toolbar.setTitle( App.getOnlyName(null) );
      FillDatos();
      }
    
    super.onResume();
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama para crear el menu de la appbar (hay que llamar setSupportActionBar antes) */
  @Override public boolean onCreateOptionsMenu( Menu menu )
    {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_select_activity, menu);
    return true;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Actualiza el estado del menú antes de ser mostrado */
  @Override public boolean onPrepareOptionsMenu( Menu menu )
    {
    boolean inFiles =  App.FileInFiles( null );
    boolean inAsset =  App.FileInAssets( null );
    
    menu.findItem( R.id.mnuRestoreAll   ).setVisible( inFiles &&  inAsset );
    menu.findItem( R.id.mnuDelAnuncFile ).setVisible( inFiles && !inAsset );
  
    return super.onPrepareOptionsMenu( menu );
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama cuando se selecciona una item de la action bar */
  @Override public boolean onOptionsItemSelected( MenuItem item )
    {
    switch( item.getItemId() )
      {
      case R.id.mnuLoadAnuncFile: SelAnuncFileForLoad(); break;
      case R.id.mnuDelAnuncio:    DeleteAnuncio(); break;
      case R.id.mnuNewAnuncio:    NuevoAnuncio(); break;
      case R.id.mnuNewFileAnunc:  NewAnuncFile(); break;
      case R.id.mnuRestoreAll:    RestoreAnuncios(); break;
      case R.id.mnuDelAnuncFile:  DelAnuncFile(); break;
      default:
        return super.onOptionsItemSelected(item);
      }

    return true;
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra un dialo para seleccionar el fichero de anuncio que se quiere cargar */
  private void SelAnuncFileForLoad()
    {
    ShowDlg msg = new ShowDlg( this );

    msg.setTitle( R.string.SelAnuncFile );
    msg.setBnt2( R.string.btnClose );

    final String[] files = App.GetAnuncioFiles();
    if( files.length>0 )
      msg.setItemsList( files );
    else
      msg.setMessage( R.string.noAnuncFiles );

    msg.show( new OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int idx )
        {
        if( idx >= 0 ) LoadAnuncFile( files[idx] + ".txt" );
        }
      });
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Carga un  fichero de anuncio y actualiza la pantalla en consecuencia*/
  private void LoadAnuncFile( String fileName )
    {
    if( App.LoadAnuncFile( fileName ) )
      {
      FillDatos();
      toolbar.setTitle( App.getOnlyName(null) );
      }
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Borra el anuncio actual si el usuario da su consentimiento  */
  private void DeleteAnuncio()
    {
    ShowDlg.Confirm( this, R.string.dlgDelAnuncio, new OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        App.DeletaAnunc();
        FillDatos();
        }
      } );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Crea un anuncio nuevo y pasa a la pantalla de edicción */
  private void NuevoAnuncio()
    {
    if( App.InsertAnunc() )
      {
      FillDatos();
      OnEditarAnuncio();
      }
    else
      ShowDlg.Msg( this, R.string.msgInsertErr );
    }

  String LastNewFile;         // Nombre del ultimo fichero que se trato de cargar
  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Crea un fichero nuevo de anuncios y crea un anuncio */
  private void NewAnuncFile()
    {
    ShowDlg msg = new ShowDlg( this );

    msg.setTitle( R.string.lbAnuncFileName );
    msg.setBnt1( R.string.btnAceptar );
    msg.setBnt2( R.string.btnClose );

    @SuppressLint( "InflateParams" )
    View body = getLayoutInflater().inflate( R.layout.dlg_get_filename, null );

    final EditText newFile = body.findViewById( R.id.txtNewName );
    if( LastNewFile != null ) newFile.setText( LastNewFile );

    msg.setLayout( body );
    msg.show( new DialogInterface.OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        if( which == DialogInterface.BUTTON_POSITIVE )
          {
          LastNewFile = newFile.getText().toString();

          if( App.CreateAnuncFile( LastNewFile ) )
            {
            LastNewFile = null;
            FillDatos();
            toolbar.setTitle( App.getOnlyName(null) );
            OnEditarAnuncio();
            }
          else
            OnCreateAnuncFileError();
          }
        }
      } );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama cuando se produce un error creando un fichero de anuncios nuevo */
  private void OnCreateAnuncFileError()
    {
    ShowDlg msg = new ShowDlg( this );

    msg.setTitle( R.string.NewAnuncFileErrTitle );
    msg.setMessage( R.string.NewAnuncFileErrMsg );
    msg.setBnt1( R.string.btnAceptar );

    msg.show( new DialogInterface.OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        NewAnuncFile();
        }
      } );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Restaura los datos de los anuncios de fichero de anuncio actual, tal como estaban cuando se instalo la aplicación */
  private void RestoreAnuncios()
    {
    ShowDlg.Confirm( this, R.string.dlgRestAnunc, new OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        String fName = App.getOnlyName(null) + ".txt";
        File      fl = new File( getFilesDir(), fName );
        
        if( fl.delete() )
          LoadAnuncFile( fName );
        else
          Toast.makeText( getBaseContext(), R.string.msgFileNoRestore, Toast.LENGTH_LONG ).show();
        }
      } );
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Borra el fichero de anuncio actual */
  private void DelAnuncFile()
    {
    ShowDlg.Confirm( this, R.string.dlgDelAnuncFile, new OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        if( App.DeleteAnuncFile() )
          {
          FillDatos();
          toolbar.setTitle( App.getOnlyName(null) );
          }
        else
          Toast.makeText( getBaseContext(), R.string.msgFileNoDelete, Toast.LENGTH_LONG ).show();
        }
      } );
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Llena todos los datos del anuncio actual */
  private void FillDatos()
    {
    int count = App.getAnuncCount();
    if( count != 0 )
      {
      int idxAnunc = App.getActualAnuncIdx();

      mTxtTitle.setText( App.getAnuncTitle( true, false ) );
      mTxtBody.setText( App.getAnuncDesc( true, false ) );

      toolbar.setSubtitle( (idxAnunc+1) + " de " + count );
      }
    else
      {
      mTxtTitle.setText( "" );
      mTxtBody.setText( "" );
      toolbar.setSubtitle( "No hay anuncios cargados" );
      }

    ShowDataVer = App.DataVer;
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama al tocar el boton para pasar al auncio anterior */
  public void OnPrevAnuncio()
    {
    if( App.isFirstAnunc() )
      {
      if( !mOver )
        {
        mOver=true;
        App.FloatMsg( R.string.OverFirstAnunc );
        return;
        }
      
      App.setLastAnunc();
      }
    else
      App.setPrevAnunc();

    mOver = false;
    FillDatos();
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Atiende todas las acciones que se generan en la barra de navegación inferior */
  void setNavigation()
    {
    BottomNavigationView nav = findViewById( R.id.navBottom );
    nav.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener()
      {
      @Override public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
        switch( menuItem.getItemId() )
          {
          case R.id.mnu_nav_previo:    OnPrevAnuncio();     return true;
          case R.id.mnu_nav_publicar : OnPublicarAnuncio(); return true;
          case R.id.mnu_nav_editar:    OnEditarAnuncio();   return true;
          case R.id.mnu_nav_proximo:   OnNextAnuncio();     return true;
          }
      
        return false;
        }
      });
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama al tocar el boton para pasar al auncio siguiente */
  public void OnNextAnuncio()
    {
    if( App.isLastAnunc() )
      {
      if( !mOver )
        {
        mOver=true;
        App.FloatMsg( R.string.OverLastAnunc );
        return;
        }

      App.setFirstAnunc();
      }
    else
      App.setNextAnunc();

    mOver = false;
    FillDatos();
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama al tocar el boton para pasar al auncio siguiente */
  public void OnPublicarAnuncio()
    {
    AnuncioData Anunc = App.getActualAnunc();
    if( Anunc == null )
      {
      ShowDlg.Msg( this, R.string.msgNoAnunc );
      return;
      }

    if( !Anunc.DatosRequeridos() )
      {
      ShowDlg.Msg( this, R.string.msgNoReqDotos );
      return;
      }

    Intent itn = new Intent( this, PublishActivity.class );
    startActivity( itn );
    }

  // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Se llama al tocar el boton para pasar al auncio siguiente */
  public void OnEditarAnuncio()
    {
    if( App.getActualAnunc() != null )
      {
      Intent itn = new Intent( this, EditDataActivity.class );
      startActivity( itn );
      }
    else
      ShowDlg.Msg( this, R.string.msgNoAnunc );
    }

  //================================================================ Fin SelectActivity ===========================================================================================
  }
