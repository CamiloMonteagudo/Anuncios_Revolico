package com.BigXSoft.anuncios;

import com.BigXSoft.anuncios.AnunciosInfo.AnuncioData;
import com.BigXSoft.anuncios.AnunciosInfo.HtmlInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/** Maneja los comodines que puedan aparecen en el texto de llenado de un elemento HTML */
class TextEval
  //=============================================== TextEval  ==========================================================
  {
  private AnuncioData Anuncio;
  private LocalDate   Now;

  //--------------------------------------------------------------------------------------------------------------------------------------
  //Crea el objeto con la informción del anuncio
  static TextEval TextEvalWithAnucio(AnuncioData Anuncio)
    {
    TextEval obj = new TextEval();

    obj.Anuncio = Anuncio;
    obj.Now     = LocalDate.now();

    return obj;
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Escapa los caracteres que pueden ser conflictivo en las cadenas Javascript */
  private String EscapeText( String sVal )
    {
    sVal = sVal.replace( "\n", "\\n");
    sVal = sVal.replace( "\"", "\\\"");
    sVal = sVal.replace( "\'", "\\\'");

    return sVal;
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Si hay alguna marca de sustitución en texto de llenado lo resuelve en otro caso retorna la misma cadena */
  String ParseValue( String sVal, boolean Escape )
    {
    StringBuilder str = new StringBuilder( sVal );

    for(;;)
      {
      String cmd = stringForm( str, "{", "}" );
      if( cmd.length() == 0 ) break;

      String ret = "";
      String CMD = cmd.toUpperCase();

           if( CMD.equals( "ID"           ) ) ret = GetAnuncioID();
      else if( CMD.equals( "DIA"          ) ) ret = GetDia();
      else if( CMD.equals( "MES"          ) ) ret = GetMes();
      else if( CMD.equals( "H"            ) ) ret = GetTitleHeader();
      else if( CMD.equals( "DIASEM"       ) ) ret = GetSemanaDia();
      else if( CMD.equals( "DIASTR"       ) ) ret = GetStringDia();
      else if( CMD.equals( "EMAIL"        ) ) ret = GetRandomMail();
      else if( CMD.startsWith( "WORDSKEY" ) ) ret = GetWordKeys( cmd );
      else System.out.println( "El comando: '" + cmd + "' no existe" );

      int idx = str.indexOf( "{" + cmd + "}" );
      str.replace( idx, idx+cmd.length()+2, ret );
      }

    String txt = str.toString();
    if( Escape ) txt = EscapeText( txt );
    return txt;
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene la cadena encerrada entre lo */
  private String stringForm(StringBuilder str, String sIni, String sEnd)
    {
    int ini = str.indexOf( sIni );
    if( ini < 0 ) return "";

    int fin = str.indexOf( sEnd, ini+1 );
    if( fin < 0 ) return "";

    return str.substring( ini+1, fin );
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /// Obtiene el dia en forma de una palabra
  private String GetStringDia()
    {
    String[] Dias = { "Uno"      , "Dos"     , "Tres"     , "Cuatro"     , "Cinco"     , "Seis"     , "Siete"     , "Ocho"     , "Nueve"     , "Dies"  ,
                      "Once"     , "Doce"    , "Trece"    , "Catorce"    , "Quince"    , "DiesiSeis", "DiesiSiete", "DiesiOcho", "DiesiNueve", "Vente" ,
                      "VentiUno" , "VentiDos", "VentiTres", "VentiCuatro", "VentiCinco", "VentiSeis", "VentiSiete", "VentiOcho", "VentiNueve", "Trenta",
                      "TrentiUno" };

    int dia = Now.getDayOfMonth()-1;
    return Dias[ dia ];

    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene el nombre del dia de la semana */
  private String GetSemanaDia()
    {
    String[] Dias = { "Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado" };

    int dia = Now.getDayOfWeek().ordinal();
    return Dias[ dia ];
   }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene un emcabezamiento unico de 2 letras para el titulo */
  private String GetTitleHeader()
    {
    String Meses = "ZYXWVUTSRQPOMNLKJIHGFEDCBAÑ©°Æ®Øß∂¶§µ";
    String Dias  = "ABCDEFGHIJKLNMOPQRSTUVWXYZÇÆµÑØß∂¶§";

    int dia = Now.getDayOfMonth()-1;
    int mes = Now.getMonthValue()-1;

    Random rd = new Random();
    mes += rd.nextInt(12);

    char c1 = Meses.charAt( mes );
    char c2 = Dias.charAt( dia );

    return new String( new char[]{c1, c2} );
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene el nombre del mes actual */
  private String GetMes()
    {
    String[] Meses = {"Enero","Febrero","Marzo","Abrir", "Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre" };

    int mes = Now.getMonthValue()-1;
    return Meses[ mes ];
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene el dia actual en el formato de dos letras */
  private String GetDia()
    {
    int dia = Now.getDayOfMonth();

    if( dia<10 ) return "0" + dia;
    else         return ""  + dia;
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene el identificador del anuncio actual */
  private String GetAnuncioID()
    {
    return Anuncio.ID;
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene una dirección de Correo Aleatoria */
  private String GetRandomMail()
    {
    String[]   Names = {"Camilo","Carlos", "Pedro","Juan", "David", "Armando", "Felix", "Maria", "Juana", "Margar", "Tere", "Josefa", "Fran" ,"Pepe", "Maira", "Teresita", "Josefa", "Ana", "Anita", "Aida", "Lola", "Laura", "Ivan", "Manuel", "Elvis" ,"Eva", "Clara" ,"Gael", "Joel", "Jony", "Joana", "Yoan", "Yoani", "Lorena", "Patricia", "Gilda", "Ariel" ,"Alex", "Ernesto", "Jose", "Elsa", "Jainer", "Julian", "Victor", "Eve", "Fara", "Jorge", "Sivia", "Amelia" ,"Grettel", "Nicolas", "Fidel", "Raul", "Mariela", "Agustin", "Alfredo", "Daniel", "Fide", "Oscar", "Juaqin", "Malena" ,"Ivon", "Agusto" ,"Cecilia", "Celia", "Cesar", "Homero", "Ristro", "Lazaro", "Yeyo", "Miguel", "Rafael", "Andres", "Regla", "Bruno", "Elena", "Blanca", "Aurelio", "Yelianis", "Ledys", "Monica", "Rebeca", "Yenifer", "Yadira", "Esteban", "Sebastian", "Alturo", "Alejandro", "Diosdado", "Dematrio", "Abel", "Hector", "Roberto", "Alain", "Alicia", "Eduardo", "Angel", "Jesus", "Hugo", "Nestor", "Julian", "Armando", "Rodolfo", "Richar", "Cristian", "Alberto", "Julio", "Mandy", "Dinora", "Vivian", "Rigo", "Damian", "Oriol"  };
    String[]  Apllds = {"Fdez","Monte" ,"Dias" ,"Abrir", "Suares", "Valdez", "Castro", "Gzles", "Peres", "Ochoa", "Hrdez", "Sanchez", "Tabares", "Ramires", "Oliva", "Dmiguez", "Benites", "Orosco", "Mora", "Garcia", "Torres", "Almirar", "Enrique", "Blanco", "Cruz", "Palomo", "Caro", "Carrazana", "Pena", "Orozco", "Guevara", "Morales", "Pinera", "Prieto", "Carcaces", "Alonso", "Alfonso", "Chavez", "Dies", "Jimenes", "More", "Canel", "Ojeda", "Leon", "Noa", "Moa", "Reyes", "Ortega", "Alarcon", "Quesada", "Ortis", "Bernal", "Oropeza", "Kindelan", "Torientes", "Garcez", "Obregon", "Otamedis", "Medinas", "Menendez", "Menocal", "Batista", "Arteaga", "Agramonte", "Almenteros", "Acosta", "Alvares", "Almanza", "Tamayo", "Cuevas", "Cuervo", "Coldero", "Cabrera", "Infante", "Cardenas", "Linares", "Limas", "Limonta", "Llanes", "Labrada", "Lopez", "Lorenzo", "Montesuma", "Torriente", "Brau", "Maradona", "Calmona", "Orta", "Clavajal", "Castellanos", "Curbelo", "Cuba", "Anglada", "Valle", "Vasquez", "Valezuela", "Villegas", "Villanueva", "Vidal", "Sandoval", "Sarmiento", "Chinea", "Camejo", "Cobo", "Maso", "Moncada", "Rivera" };
    String[] Servers = {"gmail.com","outlook.com", "hotmail.com", "infomed.sld.cu", "yahoo.com", "yahoo.ar", "ms.net", "amazon.com", "cultur.cu", "aol.com", "nauta.cu" };

    Random rd = new Random();
    int iName    = rd.nextInt( Names.length-1   );
    int iApllds  = rd.nextInt( Apllds.length-1  );
    int iServers = rd.nextInt( Servers.length-1 );

    String sMail = Names[iName] + Apllds[iApllds] + "@" + Servers[iServers];

    return sMail.toLowerCase();
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene una lista de palabras clasves ordenadas de manera aleatoria */
  private String GetWordKeys( String cmd )
    {
    String[] CmdVal = cmd.split("/" );                                // Separa los comandos

    String KeysName = CmdVal[0];                                            // Primer comando "Nombre de la lista de llaves"
    String      Sep = (CmdVal.length>=2)? CmdVal[1] : " / ";                // Segundo comando "Separador utilizado para los elementos de la lista"

    for( HtmlInfo Info : Anuncio.FillInfo )                                 // Recorre informacion del anuncio
      if( KeysName.equals( Info.InfoName ) )                                // Si es el nombre de lista de llaves
        return WordsKeyList( Info, Sep );                                   // Obtine una lista ordenada aleatoriamente

    System.out.println( "No se obtubo la lista de palabras: " + cmd );     // Pone cartel de advertencia
    return "";                                                             // Obtiene una lista vacia
    }

  //--------------------------------------------------------------------------------------------------------------------------------------
  /** Obtiene una lista de palabras clasves ordenadas de manera aleatoria */
  private String WordsKeyList( HtmlInfo info, String Sep )
    {
    // Obtiene una lista con todas las palabras claves
    String[] wrds = info.Txt.split(", " );
    ArrayList<String> words =  new ArrayList<>( Arrays.asList( wrds ) );

    Random rd = new Random();
    StringBuilder lst = new StringBuilder();                            // Crea cadena vacia
    while( words.size() > 0 )                                           // Mientras haya palabras en la lista
      {
      int idx = rd.nextInt( words.size() );                             // Obtiene indice a una palabra aleatoriamente

      if( lst.length() > 0 ) lst.append( Sep );                         // Si no es la primera palabra adiciona un separador

      lst.append( words.get(idx) );                                     // Adiciona la palabra a la cadena
      words.remove( idx );                                              // Borra la palabra de la lista
      }

    return lst.toString();                                              // Retorna cadena con lista de palabras
    }


  //=============================================== TextEval  ==========================================================
  }
