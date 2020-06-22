package com.BigXSoft.anuncios;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShowDlg extends DialogFragment
  //==============================================================================================================================================================================
  {
  private String mTitle;
  private String mMessage;
  private String mPositive;
  private String mNegative;
  private String mNeutral;
  private String[] mLista;
  private int      mLstType;
  private int      mLayout;

  private FragmentActivity fActv;
  private OnClickListener mRetFun;

  private boolean[] mChecked;
  private View      mView;

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra un mensaje desde una cadena de recurso, puede venir en la forma Titulo/Msg, si viene sola se ignora el titulo */
  static void Msg(FragmentActivity fm, int idMsg )
    {
    String Msg = fm.getString( idMsg );                                         // Lee la cadena
    Msg( fm, Msg );
    }
  
  static void Msg(FragmentActivity fm, String Msg )
    {
    ShowDlg dlg = new ShowDlg( fm );
    
    String[] strs =  ParseMsg( fm, Msg, 2 );

    if( strs[0].length()>0 )
      dlg.setTitle( strs[0] );

    dlg.setMessage( strs[1] );

    dlg.show( null );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra un dialogo de confirmación con dos botones y solo se notifica cuando se oprime en primer botón */
  static void Confirm( FragmentActivity fm, int idMsg, final OnClickListener callBack )
    {
    String Msg = fm.getString( idMsg );                                         // Lee la cadena
    Confirm( fm, Msg, callBack );
    }

  static void Confirm( FragmentActivity fm, String Msg, final OnClickListener callBack )
    {
    ShowDlg msg = new ShowDlg( fm );

    String[] strs =  ParseMsg( fm, Msg, 4 );

    if( strs[0].length()>0 )
      msg.setTitle( strs[0] );
    
    msg.setMessage( strs[1] );
    msg.setBnt1   ( strs[2] );
    msg.setBnt2   ( strs[3] );

    msg.show( new DialogInterface.OnClickListener()
      {
      @Override public void onClick( DialogInterface dialog, int which )
        {
        if( callBack!=null && which == DialogInterface.BUTTON_POSITIVE )
          callBack.onClick( dialog, which );
        }
      });
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Muestra un dialogo que permite seleccionar entre 3 opciones, por defecto Acectar, Descarter y Cancelar */
  static void SelectAction(  FragmentActivity fm, int idMsg, final OnClickListener callBack  )
    {
    ShowDlg msg = new ShowDlg( fm );

    String Msg = fm.getString( idMsg );                                         // Lee la cadena
    String[] strs =  ParseMsg( fm, Msg, 5 );

    if( strs[0].length()>0 )
      msg.setTitle( strs[0] );

    msg.setMessage( strs[1] );
    msg.setBnt1   ( strs[2] );
    msg.setBnt2   ( strs[3] );
    msg.setBnt3   ( strs[4] );

    msg.show( callBack );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /** Obtine los datos desde una sola cadena en el formato Titulo/Mensaje/Botón Aceptar/Botón Cancelar, si faltan datos se agregan según convenga */
  private static String[] ParseMsg( FragmentActivity fm, String Msg, int nSeg )
    {
    List<String>   lst = Arrays.asList( Msg.split( "\\|", nSeg ) ) ;       // Divide la cadena con un maximo de 'nSeg'
    List<String> Parts = new ArrayList<>( lst );
    int nParts = Parts.size();
    
    if( nParts<2 && nSeg>=2 )                                                               // Si hay uno son 2 o más
      Parts.add( 0, "" );                                                     // Pone vacio el primero (el titulo)

    if( nParts<3 && nSeg>=3 )                                                               // Si hay menos de 3 y son 3 o más
      Parts.add(  fm.getString( R.string.btnAceptar ) );                                    // Pone "Aceptar" por defecto

    if( nParts<4 && nSeg>=4 )                                                               // Si hay menos de 4 y son 4 o más
      Parts.add( fm.getString( nSeg==4? R.string.btnCancel : R.string.bntDescartar ) );     // Pone "Cancelar" o "Descartar" por defecto

    if( nParts<5 && nSeg>=5 )                                                               // Si hay menos de 5 y son 5 o más
      Parts.add( fm.getString( R.string.btnCancel ) );                                      // Pone "Cancelar" por defecto

    return Parts.toArray( new String[0] );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  ShowDlg( FragmentActivity fm )
    {
    fActv = fm;
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  public void setTitle( String Title) { mTitle = Title; }
  public void setTitle( int IdTitle)  { mTitle = fActv.getString(IdTitle); }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  void setMessage( String Message) { mMessage = Message; }
  void setMessage( int IdMessage ) { mMessage = fActv.getString(IdMessage); }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  void setBnt1( String Title) { mPositive = Title; }
  void setBnt1( int IdTitle ) { mPositive = fActv.getString(IdTitle); }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  void setBnt2( String Title) { mNegative = Title; }
  void setBnt2( int IdTitle ) { mNegative = fActv.getString(IdTitle); }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  void setBnt3( String Title) { mNeutral = Title; }
  void setBnt3( int IdTitle ) { mNeutral = fActv.getString(IdTitle); }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  void setItemsList( String[] Lst ) { mLista = Lst; mLstType=0; }
  void setItemsList( int IdList  )
    {
    Resources res = fActv.getResources();
    setItemsList( res.getStringArray( IdList ) );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  public void setCheckList( String[] Lst, boolean[] checked )
    {
    mLista = Lst;
    mLstType = 1;
    mChecked = checked;
    }

  public void setCheckList( int IdList, boolean[] checked  )
    {
    Resources res = fActv.getResources();
    setCheckList( res.getStringArray( IdList ), checked );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  public void setOptionsList( String[] Lst ) { mLista = Lst; mLstType=2; }
  public void setOptionsList( int IdList  )
    {
    Resources res = fActv.getResources();
    setItemsList( res.getStringArray( IdList ) );
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  void setLayout( int IdDlgName) { mLayout = IdDlgName; }
  void setLayout( View Layaut  ) { mView = Layaut;      }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  void show( OnClickListener listener )
    {
    mRetFun = listener;

    super.show( fActv.getSupportFragmentManager(), "ShowMessaje");
    }

  //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  /**  */
  @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
    super.onCreate(savedInstanceState);

    Activity atvd = getActivity();
    if( atvd==null ) return getDialog();

    AlertDialog.Builder dlg = new AlertDialog.Builder( atvd );

    if( mTitle   != null ) dlg.setTitle(mTitle);
    if( mMessage != null ) dlg.setMessage(mMessage);
    if( mPositive!= null ) dlg.setPositiveButton(mPositive,mRetFun);
    if( mNegative!= null ) dlg.setNegativeButton(mNegative,mRetFun);
    if( mNeutral != null ) dlg.setNeutralButton (mNeutral ,mRetFun);
    if( mLayout  != 0    ) dlg.setView( mLayout );
    if( mView    != null ) dlg.setView( mView );

    if( mLista != null )
      {
      if( mLstType == 1 )
        dlg.setMultiChoiceItems(mLista, mChecked, new DialogInterface.OnMultiChoiceClickListener()
          {
          @Override
          public void onClick(DialogInterface dialog, int which, boolean isChecked)
            {
            mChecked[which ] = isChecked;
            }
          });
      else if( mLstType ==2 )
        dlg.setSingleChoiceItems( mLista, 0, mRetFun );
      else
        dlg.setItems( mLista, mRetFun );
      }

    return dlg.create();
    }

  //==============================================================================================================================================================================
  }
