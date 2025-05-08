/* Cambio IMC_01 29.08.2008
IMC Motivo del cambio: Me di cuenta de que cuando el campo es numerico y contiene mas de 11 digitos numeros Excel lo transforma a exponencial ejemplo 123456789012 -> 1,23457E+11. Solucion: =T("1234567890123"). Las funciones de Excel =CONCATENATE() y =T() hacen lo mismo, pero =T() lo hace no solo para Office en Ingles sino para todos los idiomas lenguajes.
ReportTypeTransformation se refiere a si se transforma el fichero CSV que lee en la entrada a XLS=Excel o MDB=Access.
El fichero INI contiene una linea (COLNAMES) con la cabecera con los nombres de los campos, otra linea (COLTYPES) con sus tipos, otra linea (COLLENGTHS) con sus longitudes, y otra linea (DESTINAT) con el path donde copiar el fichero xls transformado a partir del csv. 

*/

package DownloadTool00;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.SQLException;
import java.sql.Types;

public class AqReportTypeTransformation {
		public AqConfigAndLog cl;
		// final static String SEPARADOR = ";"; // Caracter separador del fichero CSV
		final static String SEPARADOR_INI = ","; // Caracter separador del fichero INI coma ,
		final static char CHAR_SEPARADOR = ';'; // Caracter separador del fichero CSV punto y coma
		//final static String intType = "P";
		final static String textType = "A"; // Tipo de campo Alfa String
		final static String dateType = "Z"; // Tipo de campo fecha, No se usa
		final static String numType = "P"; // Tipo de campo P=Packed Numerico creo
		final int MAX_FIELDS = 70;
		char[] field_type = new char[MAX_FIELDS];

		AqReportTypeTransformation(String var) { // Constructor ; var=var0
		   cl = new AqConfigAndLog(var);
		}

		public void getFileType(String csvFileName) throws AqExceptionDownloadTool { // csvFileName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.CSV.TXT ; csvFileName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
				String TIFIVCHER = null;
				String data = null;
				StringBuffer iniFile = new StringBuffer(csvFileName); // iniFile = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
				iniFile.replace(iniFile.length()-7, iniFile.length()-4, "INI"); // iniFile = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT ; Los ultimos 7 caracteres son INI.TXT ; Sustituye CSV por INI
				try {
                     // Reading ini file
                     data = cl.findParamInFile("repotype", iniFile.toString()); // Devuelve REPOTYPE:XLS o REPOTYPE:MDB o REPOTYPE:TXT del fichero INI de la linea REPOTYPE
				} catch(Exception e) {
						cl.writeInError("Error reading: " + iniFile + " " +  e.getMessage());
						cl.error(csvFileName);
						cl.error(iniFile.toString());
						throw new AqExceptionDownloadTool();
				}

				if (data.indexOf("TXT") > -1 || data.indexOf("txt") > -1) {
						TIFIVCHER = "txt";
				} else if (data.indexOf("XLS") > -1 || data.indexOf("xls") > -1) { // Excel ; Comprueba si contiene XLS
						TIFIVCHER = "xls";
				} else if (data.indexOf("MDB") > -1 || data.indexOf("mdb") > -1) { // Access ; Comprueba si contiene MDB
						TIFIVCHER = "mdb";
				}

				generateFile(TIFIVCHER, csvFileName);
		}
// Cambio IMC_01 29.08.2008
// Builds an array with all the field types of the table.
// If the destination file type is Excel, then the numerical fields will treated differently, depending on the number of digits.
		public void column_type_vector(File data) throws AqExceptionDownloadTool { // data = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
				int index, index2;
				StringBuffer iniFile = new StringBuffer(data.toString()); // iniFile = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT 
				iniFile.replace(iniFile.length() - 7, iniFile.length()-4, "ini"); // iniFile = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.ini.TXT ; Los ultimos 7 caracteres son INI.TXT ; Reemplaza ini por ini

				String temp;
				int numCol = 0;
				int c = 0;
				String tableName;
				int i = 0;
				try {
						// reading ini file
						temp = cl.findParamInFile("tablename", iniFile.toString()); // MDB=Access, no he encontrado ningun *.ini.txt que contenga tablename
						if(temp == null)
							tableName = data.getName().substring(0,data.getName().length()-8); // tableName = 02 ; Elimina los 8 ultimos caracteres (.CSV.TXT) del nombre de fichero y el path de delante ; data.getName()=02.CSV.TXT ; data = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
						else
							tableName =temp.substring(temp.indexOf(":")+1);

						temp = new String();

						// read column types
						temp = cl.findParamInFile("coltypes", iniFile.toString()); // COLTYPES:A ; temp = COLTYPES:A,P,A,P,P,A,A,A,A,A,A,A,A,A,A,A,A,A,P,P,A,P,P,A,P,A,A,A,A,A,A,A,A,A,P,P,P,P,A,A,A,A,A,A,A,P 
						index = temp.indexOf(":"); // Busca en que posicion se encuentran los dos puntos
						index2 = temp.indexOf(SEPARADOR_INI, ++index); // Busca caracter separador del fichero INI coma , despues de los dos puntos
						if (index2 == 0) { // There is only 1 field
	                                        if(temp.substring(index, ++index).indexOf(numType)>-1) { // Tipo de campo Numerico
	                                                // Put in a vector the type (Numeric) of each field.
	                                                field_type[i] = numType.charAt(0); // Tipo de campo Numerico
	                                        }else { // Tipo de campo Alfa String
	                                                // Put in a vector the type (Alfanumeric) of each field.
	                                                field_type[i] = textType.charAt(0); // Tipo de campo Alfa String
	                                              }
                                   		}
						try {
	                            while(index2 > 0) { // Hay varios campos. Crea vector de tipos de campos
	                                if(temp.substring(index, index2).indexOf(numType)>-1) { // Tipo de campo Numerico
	                                                // INICIO Cambio IMC_01
	                                                // Put in a vector the type (alfa or numeric) of each field.
	                                                field_type[i] = numType.charAt(0); // Tipo de campo Numerico
	                                                // FIN Cambio IMC_01
	                                } else { // Tipo de campo Alfa String
	                                                // INICIO Cambio IMC_01
	                                                // Put in a vector the type (alfa or numeric) of each field.
	                                                field_type[i] = textType.charAt(0); // Tipo de campo Alfa String
	                                                // FIN Cambio IMC_01
	                                                }
	                                i++;
	                                index = index2;
	                                index2 = temp.indexOf(SEPARADOR_INI, ++index); // Busca siguiente caracter separador del fichero INI coma ,
	                            }
	
	                            if(temp.substring(index, ++index).indexOf(numType)>-1) { // Tipo de campo Numerico
	                                    // Put in a vector the type (Numeric) of each field.
	                                    field_type[i] = numType.charAt(0); // Tipo de campo Numerico
	                            } else { // Tipo de campo Alfa String
	                                    // Put in a vector the type (Alfanumeric) of each field.
	                                    field_type[i] = textType.charAt(0); // Tipo de campo Alfa String
	                                  }

						} catch(Exception exc) {
                            cl.writeInError("Error in INI file " + iniFile.toString() + " while setting column type vector in field number " + String.valueOf(i));
                            cl.error(data.toString());
                            cl.error(iniFile.toString());
                            //throw new AqExceptionDownloadTool();
						}

				} catch(IOException e) {
						cl.writeInError("Error in INI file " + iniFile.toString() + " while setting column type vector in field number " + String.valueOf(i));
						cl.error(data.getPath());
						cl.error(iniFile.toString());
				}
		}

		public File toTab(File data, String tipo) throws AqExceptionDownloadTool {
				StringBuffer fileName = new StringBuffer(data.toString());
				fileName.delete(fileName.length()-7,fileName.length()); // Los ultimos 7 caracteres son INI.TXT
				String iniName = fileName + "ini.txt";
				fileName.append("txt");
				File f1 = new File(fileName.toString());
				//check file format
				String colName = null;
				try {
					colName = cl.findParamInFile("colnames", iniName);

				} catch(Exception e){}
				if(colName.indexOf(SEPARADOR_INI) == -1) { // Busca caracter separador del fichero INI coma ,
						try {
								BufferedReader in = new BufferedReader(new FileReader(data));
								BufferedWriter out = new BufferedWriter(new FileWriter(fileName.toString()));
								String line = in.readLine();
								do{
										if(line.startsWith("\"") && line.endsWith("\"")) {
												out.write(line.substring(1,line.length()-1)+ "\r\n");
												line = in.readLine();
										}else {
												out.write(line);
												line = in.readLine();
										}
								} while(line != null);
								out.close();
								in.close();
								data.delete();
								cl.writeInLog("Transformation completed: " + f1.toString());
								cl.writeInLog("Original file: " + data.toString() + " deleted");
						} catch(Exception e){}

				} else {
						data.renameTo(f1);
						cl.writeInLog("Transformation completed: " + f1.toString());
				}
				return f1;
		}

		public File toXls(File data) throws AqExceptionDownloadTool { // A partir del fichero CSV genera Microsoft Excel XLS tipo CSV ed editable con notepad ; data=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
			int index, index2;

			StringBuffer newName = new StringBuffer(data.toString()); // newName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
			newName.delete(newName.length()-7,newName.length()); // newName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02. ; Los ultimos 7 caracteres son INI.TXT
			newName.append("xls"); // newName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls
			StringBuffer iniFile = new StringBuffer(data.toString()); // iniFile=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
			iniFile.replace(iniFile.toString().length() - 7, iniFile.length()-4, "INI"); // Los ultimos 7 caracteres son INI.TXT ; iniFile = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.INI.TXT ; Sustituye CSV por INI

			String temp;
			String tableName;
			FileInputStream iniReader = null;
			OutputStreamWriter f1Writer = null;
			FileInputStream dataReader = null;

			File f1 = null;
			try {
                // read ini file
                tableName = newName.substring(0,newName.lastIndexOf(".")); // tableName = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02
                f1 = new File(newName.toString()); // f1 = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls
                f1Writer = new OutputStreamWriter(new FileOutputStream(f1.toString()));
                // read column names
                temp = cl.findParamInFile("colnames", iniFile.toString()); // Lee nombres de columnas de fichero INI ; temp = COLNAMES:CDDOCUME,NUSECUPE,DSFUNCIO,FEPETICI,FEFIN,TIPETICI,ESTADOPE,ESTADOAP,NOTRABAJ,INPROCES,NUPRIORI,ININMDIF,INBAONLI,INEXTRAE,INIMPRIM,INENVIO,INVISUAL,INMODO,NUCOPIAD,NUENVIOS,CDCADENA,FECADENA,CDORDCAD,CDTEXTO,NUVERTXT,INESTADD,CDUSUPET,CDTIPUSU,INTIPOCD,CDCENTRO,CDIMPRES,DSIP,CDAPLICA,CDENTOR,HOINITRZ,HOFINTRZ,CDERREDI,CDERRAPL,INAUXIL1,INAUXIL2,INAUXIL3,INAUXIL4,CDEMPUSU,CDUSUARI,NOPROGRA,TSULTMOD
                if(temp == null) {
                        cl.writeInError("Error reading " + iniFile.toString() + " parameter colnames not found");
                        cl.error(iniFile.toString());
                        cl.error(data.getPath());
                        f1Writer.close();
                        f1.delete();
                        throw new AqExceptionDownloadTool();
                }

                index = temp.indexOf(":"); // Busca caracter dos puntos :
                index2=temp.indexOf(SEPARADOR_INI, ++index); // Busca el primer separador coma , despues de dos puntos :
                int colNum = 0;
                while(index2 > 0) { // Escribe el nombre de cada columna en la cabecera del xls y Cuenta el numero de columnas en la cabecera de INI
                    f1Writer.write(temp.substring(index, index2)); // Escribe el nombre de cada columna en la cabecera del xls
                    colNum++; // Cuenta el numero de columnas en la cabecera de INI
                    f1Writer.write("\t"); // Tabulador es el caracter separador de columnas en el fichero de salida XLS
                    index = index2;
                    index2 = temp.indexOf(SEPARADOR_INI, ++index); // Busca el siguiente separador coma ,
                }
                f1Writer.write(temp.substring(index)); // Escribe el nombre de la ultima columna en la cabecera del xls
                colNum++; // Cuenta el numero de columnas en la cabecera de INI
                f1Writer.write('\r');
                f1Writer.write('\n');

                // read data file
                dataReader = new FileInputStream(data); // data=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
                int c = dataReader.read(); // Lee fichero CSV caracter a caracter
                char ch;
                boolean open = false;
                boolean endElement = false;
                boolean newLine = true;
//************************************************** INICIO Cambio IMC_01 ***********************************************************
                String StringToWrite = "";
                int character_counter = 0; // Contador de digitos de campos numericos
                int field_number = 0; // Numero de campo de csv
                // BufferedWriter out = new BufferedWriter(new FileWriter(fileName.toString()));

                if(colNum > 1) { // Varias columnas
                   while (c > -1) { // Lee fichero CSV caracter a caracter ; Filtra caracter valido
                         if (c == CHAR_SEPARADOR && !open) { // Caracter separador del fichero CSV punto y coma
                                 if (field_number != 0)
                                     f1Writer.write('\t'); // Tabulador es el caracter separador de columnas en el fichero de salida XLS
                                 open = false;
                                 endElement = false;
                                 // If it is a numerical field (file .INI) and it has more than 11 digits:
                                 if (field_type[field_number] == numType.charAt(0) && (character_counter > 11)) // Si es Tipo de campo Numerico y Contador de digitos de campos numericos mayor que 11, escribe CONCATENATE() o T() a XLS para que Excel no lo ponga en formato exponencial
                                     StringToWrite = "=CONCATENATE(" + "\"" + StringToWrite + "\"" + ")";
                                 field_number++; // Incrementa Numero de campo de csv
                                 f1Writer.write(StringToWrite);
                                 StringToWrite = "";
                                 character_counter = 0; // Inicializa Contador de digitos de campos numericos
                              }
                          else if(c == '\r') { // Fin de linea
                                  f1Writer.write('\t'); // Tabulador es el caracter separador de columnas en el fichero de salida XLS
                                  if (field_type[field_number] == numType.charAt(0) && (character_counter > 11)) // Si es Tipo de campo Numerico y Contador de digitos de campos numericos mayor que 11, escribe CONCATENATE() o T() a XLS para que Excel no lo ponga en formato exponencial
                                         StringToWrite = "=CONCATENATE(" + "\"" + StringToWrite + "\"" + ")";
                                  f1Writer.write(StringToWrite);
                                  f1Writer.write('\r');
                                  //out.write("\r");
                                  f1Writer.write('\n');
                                  //out.write("\n");
                                  c = dataReader.read(); // Lee fichero caracter a caracter
                                  open = false;
                                  endElement = false;
                          }
	                        else if(c != '\"') { // Si no es comilla doble " graba el caracter leido
	                                ch = (char) c;
	                                if(c!=13 && c!=10) // Si no es CR ni LF graba caracter
	                                {
	                                   StringToWrite = StringToWrite + Character.toString(ch);
	                                   // If it is a numerical field (file .INI), counts the number of digits between separators (;).
	                                   if (field_type[field_number] == numType.charAt(0)) // Tipo de campo Numerico
	                                      character_counter++; // Contador de digitos de campos numericos
	                                     }
	                   } else if(open) {
	                              c = dataReader.read(); // Lee fichero caracter a caracter
	                              if(c == CHAR_SEPARADOR) { // Caracter separador del fichero CSV punto y coma
	                                  open = false;
	                                  endElement = true;
	                              } else if(c=='\r') {
	                                              f1Writer.write('\t'); // Tabulador es el caracter separador de columnas en el fichero de salida XLS
	                                              if (field_type[field_number] == numType.charAt(0) && (character_counter > 11)) // Si es Tipo de campo Numerico y Contador de digitos de campos numericos mayor que 11, escribe CONCATENATE() o T() a XLS para que Excel no lo ponga en formato exponencial
	                                                     StringToWrite = "=CONCATENATE(" + "\"" + StringToWrite + "\"" + ")";
	                                              f1Writer.write(StringToWrite);
	                                              f1Writer.write('\r');
	                                              //out.write("\r");
	                                              f1Writer.write('\n');
	                                              //out.write("\n");
	                                              c = dataReader.read(); // Lee fichero caracter a caracter
	                                              open = false;
	                                              endElement = false;
	                              } else if(c != '\r') { // No fin de linea
	                                              f1Writer.write('\"'); // Graba comilla doble
	                                              //out.write("\"");
	                                              ch = (char) c;
	                                              StringToWrite = StringToWrite + Character.toString(ch);
	                                              // If it is a numerical field (file .INI), counts the number of digits between separators (;).
	                                              if (field_type[field_number] == numType.charAt(0)) // Tipo de campo Numerico
	                                                  if(c != 13 && c != 10)
	                                                     character_counter++; // Contador de digitos de campos numericos
	                              }
	                        } else
	                            	open = true;
	
	                        if(!endElement)
	                                c = dataReader.read(); // Lee fichero caracter a caracter
	                        } // while Lee fichero CSV caracter a caracter
                } else { // Solo hay 1 columna en el CSV
                        while(c > 0) {
                                  if (field_type[field_number] == textType.charAt(0)) { // Tipo de Campo Alfa String
                                     if(c == CHAR_SEPARADOR) // Caracter separador del fichero CSV punto y coma
                                            f1Writer.write('\t'); // Tabulador es el caracter separador de columnas en el fichero de salida XLS
                                     else if(c == '\"') { // Comilla doble "
                                                    if(!newLine) {
                                                                c = dataReader.read(); // Lee fichero caracter a caracter
                                                                if(c != '\r') // No fin de linea
                                                                     f1Writer.write("\"" + c); // Escribe comilla doble "
                                                                else { // Fin de linea
                                                                     f1Writer.write(c);
                                                                     newLine = true;
                                                                    }
                                                    } else
                                                          newLine = false;
                                     }
                                    else
                                         f1Writer.write(c);
                                }
                                  else { // Numerical field
                                       if(c == '\r') {
                                            if (character_counter > 11) // Contador de digitos de campos numericos
                                                StringToWrite = "=CONCATENATE(" + "\"" + StringToWrite + "\"" + ")";
                                            f1Writer.write(StringToWrite);
                                            f1Writer.write('\r');
                                            f1Writer.write('\n');
                                       }
                                       if(c != '\r' && c != '\n') { // Si no es CR ni LF
                                            ch = (char) c;
                                            StringToWrite = StringToWrite + Character.toString(ch);
                                            character_counter++;
                                       }
                                  }
                                  c = dataReader.read(); // Lee fichero caracter a caracter
                        }
                }
//******************************************* FIN Cambio IMC_01 *****************************************************
                dataReader.close();
                f1Writer.close();
                cl.writeInLog("Transformation completed: " + f1.toString());
                data.delete(); // Elimina fichero ; data=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
                cl.writeInLog("Original file: " + data.toString() + " deleted");
				} catch (IOException e) {
	                    cl.writeInError("Error reading data file: " + data.toString() + ". " + e.getMessage());
	                    try {
	                        iniReader.close();
	                        f1Writer.close();
	                        dataReader.close();
	                    } catch(Exception exc){}
	                    cl.error(data.getPath());
	                    cl.error(iniFile.toString());
	                    f1.delete();
	                    throw new AqExceptionDownloadTool();
				}

			return f1; // f1=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls ; f1=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls
		}

	public File toMdb2(File data) throws AqExceptionDownloadTool { // Genera Microsoft Access MDB BD
		int index, index2;
		StringBuffer newName = new StringBuffer(data.toString());
		newName.delete(newName.length()-7,newName.length()); // Los ultimos 7 caracteres son INI.TXT
		newName.append("mdb");
		StringBuffer iniFile = new StringBuffer(data.toString());
		iniFile.replace(iniFile.length() - 7, iniFile.length()-4, "ini"); // Los ultimos 7 caracteres son INI.TXT ; Reemplaza ini por ini

		String temp;
		AqWrapperJackcess dbManager = new AqWrapperJackcess();
		FileInputStream dataReader = null;
		int numCol = 0;
		int c = 0;

		String tableName;

		File dbFile = null;
		try {
			int i;

			// reading ini file

			// read table name
			temp = cl.findParamInFile("tablename", iniFile.toString());
			if(temp==null)
				tableName = data.getName().substring(0,data.getName().length()-8);
			else
				tableName =temp.substring(temp.indexOf(":")+1);

			temp = new String();

			// create database
			dbFile = new File(newName.toString());
			dbManager.crateDatabase(dbFile);

			// reading column names
			temp = cl.findParamInFile("colnames", iniFile.toString());
			index = temp.indexOf(":");
			index2 = temp.indexOf(SEPARADOR_INI, ++index); // Busca el primer caracter separador del fichero INI coma , despues de dos puntos :
			numCol = 0;
			while(index2 > 0) {
				dbManager.addColumn(temp.substring(index, index2));
				index = index2;
				index2 = temp.indexOf(SEPARADOR_INI, ++index); // Busca siguiente caracter separador del fichero INI coma ,
				numCol++;
			}
			dbManager.addColumn(temp.substring(index));
			numCol++;
			temp = new String();

			// read column types
			temp = cl.findParamInFile("coltypes", iniFile.toString());
			index = temp.indexOf(":");
			index2 = temp.indexOf(SEPARADOR_INI, ++index); // Busca el primer caracter separador del fichero INI coma , despues de dos puntos :
			i = 0;
			try {
				while(index2 > 0) {
					if(temp.substring(index, index2).indexOf(numType)>-1) { // Tipo de campo Numerico
						dbManager.setColumnType(i, Types.DOUBLE);
					} else // Tipo Alfa String
						dbManager.setColumnType(i, Types.VARCHAR);
					i++;
					index = index2;
					index2 = temp.indexOf(SEPARADOR_INI, ++index); // Busca siguiente caracter separador del fichero INI coma ,
				}
				if(temp.substring(index).indexOf(numType)>-1) { // Tipo de campo Numerico
					dbManager.setColumnType(i, Types.DOUBLE);
				} else // Tipo Alfa String
					dbManager.setColumnType(i, Types.VARCHAR);
			} catch(SQLException e) {
				cl.writeInError("SQL error in file " + data.toString() + " while setting column types: " + e.getMessage());
				try {
					dbManager.close();
				}catch(Exception exc){}
				cl.error(data.toString());
				cl.error(iniFile.toString());
				dbFile.delete();
				throw new AqExceptionDownloadTool();
			}catch(IndexOutOfBoundsException e) {
				cl.writeInError("Error in file " + data.toString() + ". Not enough columns: " + e.getMessage());
				try {
					dbManager.close();
				} catch(Exception exc){}
				cl.error(data.toString());
				cl.error(iniFile.toString());
				dbFile.delete();
				throw new AqExceptionDownloadTool();
			}
			temp = new String();

			//read column length
			temp = cl.findParamInFile("collength", iniFile.toString());
			index = temp.indexOf(":");
			index2=temp.indexOf(SEPARADOR_INI, ++index); // Busca el primer caracter separador del fichero INI coma , despues de dos puntos :
			i=0;

			while(index2 > 0) {
				try {
					String type = dbManager.getColumnType(i);
					if(type.equals("10"))
						dbManager.setColumnLength(i, Integer.parseInt(temp.substring(index, index2)));
				} catch(SQLException e){}
					i++;
				index = index2;
				index2=temp.indexOf(SEPARADOR_INI, ++index);
			}
			try {
				if(dbManager.getColumnType(i).equals("10"))
					dbManager.setColumnLength(i,Integer.parseInt(temp.substring(index)));
			} catch(SQLException e){}

			try {
				dbManager.createTable(tableName);
			} catch(Exception e) {
				cl.writeInError("Error creating table from file : " + data.toString() + " " + e.getMessage());
				try {
					dbManager.close();
				} catch(Exception exc){}
				cl.error(data.toString());
				cl.error(iniFile.toString());
				dbFile.delete();
				throw new AqExceptionDownloadTool();
			}

		}catch(IOException e) {
			cl.writeInError("Error reading ini file: " + iniFile.toString() + " "+ e.getMessage());
			try {
				dbManager.close();
			}catch(Exception exc){}
			cl.error(data.getPath());
			cl.error(iniFile.toString());
			dbFile.delete();
			throw new AqExceptionDownloadTool();
		}

			// open data file
			try {
				dataReader = new FileInputStream(data);
				c = dataReader.read();
			} catch(IOException e) {
				cl.writeInError("Error opening data file: " + data.toString() + " " + e.getMessage());
				try {
					dbManager.close();
					dataReader.close();
				} catch(Exception exc){}
				cl.error(data.getPath());
				cl.error(iniFile.toString());
				dbFile.delete();
				throw new AqExceptionDownloadTool();
			}
			// read data file
			Object[] row = new Object[numCol];
			int i=0;
			int rowCounter = 0;
			boolean open=false;
			boolean endElement = false;
			int dataErrors = 0;
			StringBuffer name = new StringBuffer();
			// read data file
			while(c>-1) {
				// read next row
				while(c!='\n' && c>-1) {
					if(c == CHAR_SEPARADOR && !open) { // Caracter separador del fichero CSV punto y coma
						//new element obtained
						open = false;
						endElement = false;
						String type = null;
						try {
							type = dbManager.getColumnType(i);
						} catch(Exception e) {
							cl.writeInError("Error form file: " + data.toString()+ ". In column " + i+1 +": " + e.getMessage());
						}
						// save data to row

						try {
							if(!type.equals("10"))
								row[i] = Double.valueOf(name.toString());
							else
								row[i] = name.toString();
							name = new StringBuffer();
						}catch(Exception e) {
							cl.writeInError("Error adding data from file: " + data.toString() + ". " + e.toString());
							dataErrors++;
							if(dataErrors > 5) {
								try {
									dataReader.close();
									dbManager.close();
								} catch(Exception e1){}
								cl.error(data.getPath());
								cl.error(iniFile.toString());
								dbFile.delete();
								throw new AqExceptionDownloadTool();
							}
						}
						// read next character

						i++; //new element added in the row
					} // end of element

					else {
						//check special characters
						if(c!= '\"') {
							if(!(c==' ' && !open))
								name.append((char) c);
						}
						else {
							// open = !open;

							if(open) {
								try {
									c = dataReader.read();
								} catch (IOException e) {
									cl.writeInError("Error reading data file" + data.toString()+ ". " + e.getMessage());
									dataErrors++;
									if(dataErrors > 5) {
										try {
											dataReader.close();
											dbManager.close();
										}catch(Exception e1){}
										cl.error(data.getPath());
										cl.error(iniFile.toString());
										dbFile.delete();
										throw new AqExceptionDownloadTool();
									}
								}
								if(c == CHAR_SEPARADOR || c=='\n') { // Caracter separador del fichero CSV punto y coma
									endElement = true;
									open = false;
								}
								else if (c!='\r')
									name.append("\"" + (char) c);
							}
							else
								open = true;
						}
					}//end else new element in row
					if(!endElement) {
							try {
								c = dataReader.read();
							} catch (IOException e) {
								cl.writeInError("Error reading data file" + data.toString()+ ". " + e.getMessage());
								dataErrors++;
								if(dataErrors > 5) {
									try {
										dataReader.close();
										dbManager.close();
									}catch(Exception e1){}
									cl.error(data.getPath());
									cl.error(iniFile.toString());
									dbFile.delete();
									throw new AqExceptionDownloadTool();
									}
								}
						}
				}//end while read row

				//adding new row to the table
				String type = null;
				open=false;
				endElement=false;
				try {
					type = dbManager.getColumnType(i);
				}catch(Exception e) {
					int col = i+1;
					cl.writeInError("Error from file: " + data.toString() + ". In column " + col +": "+  e.getMessage());
				}
				//save data according to the type
				try {
					if(type.equals("LONG"))
						row[i] = Integer.valueOf(name.toString());
					else if(type.equals("DATE"))
						row[i] = name.toString(); 		// escriure format DATE
					else if(!type.equals("10"))
						row[i] = Float.valueOf(name.toString());
					else
						row[i] = name.toString();
					}catch(Exception e) {
						cl.writeInError("Error adding data from file: " + data.toString() + ". " + e.toString());
						dataErrors++;
						if(dataErrors > 5) {
							try {
								dataReader.close();
								dbManager.close();
							} catch(Exception e1){}
							cl.error(data.getPath());
							cl.error(iniFile.toString());
							dbFile.delete();
							throw new AqExceptionDownloadTool();
						}
					}
					//reading next character to know if its the last row
					try {
						c = dataReader.read(); // nova linia
						dbManager.addRow(tableName, row,c==-1); //adding row to table
					} catch (Exception e) {
						cl.writeInError("Error reading data file: " + data.toString()+ ". " + e.getMessage());
						dataErrors++;
						if(dataErrors > 5) {
							try {
								dataReader.close();
								dbManager.close();
							}catch(Exception e1){}
							cl.error(data.getPath());
							cl.error(iniFile.toString());
							dbFile.delete();
							throw new AqExceptionDownloadTool();
						}
					}

					rowCounter++;
					name = new StringBuffer();
					//c = dataReader.read();
					i=0;
			}//end while read file

			//closing files
			try {
				dbManager.close();
				dataReader.close();
				data.delete();
			} catch(Exception e) {
				cl.writeInError("Error closing file: " + data.toString()+ ". " + e.getMessage());
			}
			cl.writeInLog("Data Base completed: " + dbFile.getName());
			cl.writeInLog("Original file: " + data.getName() + " deleted");

			return dbFile;
	}
		public void generateFile(String TIFIVCHER, String csvFileName) throws AqExceptionDownloadTool { // TIFIVCHER=xls ; csvFileName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.CSV.TXT ; csvFileName=C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
				File data = new File(csvFileName); // data = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.CSV.TXT
				File f1;
				cl.writeInLog("Transforming " + csvFileName + " to " + TIFIVCHER);

                if (TIFIVCHER == "mdb") // Access db
                        f1 = toMdb2(data); // create Access db
				else if(TIFIVCHER=="xls") { // Excel
		                column_type_vector(data); // column type vector
						f1 = toXls(data); // create xls
						}
				else
						f1 = toTab(data, TIFIVCHER); // create txt

				String args[] = {f1.toString(),cl.getVar()}; // args = [C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls, var0] ; f1 = C:\Users\ignac\Documents\Java\DownloadTool00\Files\KTOR34T.xls ; f1 = C:\Users\ignac\Documents\Java\DownloadTool00\Files\02.xls ; cl.getVar()=var0
				cl.writeInLog("Calling Dispatcher");
				AqReportDispatcher.main(args);
		}
}
