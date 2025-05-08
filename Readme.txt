1.12.2008

Servidor de ficheros .xls Excel y .mdb Access. Lee ficheros planos descargados de AS400 .txt de un directorio de la red, graba .xls Excel y .mdb Access en otro directorio, y genera un log historico.

Genera ficheros .xls Excel tipo csv y .mdb Access a partir de ficheros de texto. Ejemplo:
FILENAME:MDB00.CSV.TXT
COLNAMES:CDDOCUME,NUSECUPE,DSFUNCIO,FEPETICI,FEFIN,TIPETICI,ESTADOPE,ESTADOAP,NOTRABAJ,INPROCES,NUPRIORI,ININMDIF,INBAONLI,INEXTRAE,INIMPRIM,INENVIO,INVISUAL,INMODO,NUCOPIAD,NUENVIOS,CDCADENA,FECADENA,CDORDCAD,CDTEXTO,NUVERTXT,INESTADD,CDUSUPET,CDTIPUSU,INTIPOCD,CDCENTRO,CDIMPRES,DSIP,CDAPLICA,CDENTOR,HOINITRZ,HOFINTRZ,CDERREDI,CDERRAPL,INAUXIL1,INAUXIL2,INAUXIL3,INAUXIL4,CDEMPUSU,CDUSUARI,NOPROGRA,TSULTMOD
COLTYPES:A,P,A,P,P,A,A,A,A,A,A,A,A,A,A,A,A,A,P,P,A,P,P,A,P,A,A,A,A,A,A,A,A,A,P,P,P,P,A,A,A,A,A,A,A,P <-- A=Alfanume String, P=Packed Numerico
COLLENGTHS:00012,09,00030,08,08,00002,00002,00002,00010,00001,00003,00001,00001,00001,00001,00001,00001,00001,03,03,00010,08,09,00008,03,00001,00010,00004,00001,00007,00030,00015,00002,00003,08,08,04,04,00001,00001,00001,00001,00004,00010,00008,13
DESTINAT:localhost,,C:\Java\DownloadTool00\Files <-- El primer elemento es el servidor maquina, el segundo el recurso y el tercero el path del fichero creo
REPOTYPE:MDB ó XLS <-- MDB=Access, XLS=Excel

Tabulador es el caracter separador de columnas en el fichero de salida XLS

Añadir los .jar siguientes y por este orden al proyecto:
   commons-collections.jar
   commons-logging-1.0.4.jar
   commons-lang-2.0.jar
   log4j-1.2.8.jar
   jutil.jar?
   jackcess-v1.0.jar <-- Microsoft Access database mdb's

- - -
PARA PROBAR/EJECUTAR:
Eclipse / Open project / Folder: C:\Users\ignac\Documents\Java\DownloadTool00

Copiar KTOR34T.INI.TXT y KTOR34T.CSV.TXT (juego de pruebas con 1 solo campo) al directorio D:\Mis Documentos\Java\DownloadTool00\Files
El programa habrá funcionado correctamente cuando borre estos ficheros y genere un .xls excel o .mdb access en D:\Mis Documentos\Java\DownloadTool00\Files\Temp\errors y actualice el fichero D:\Mis Documentos\Java\DownloadTool00\Files\Temp\errors\error.txt. Parar el debug manualm porque no acaba sino que se queda esperando nuevos ficheros mirando cada 6 segundos sleep.


Lanzar el AqNewAS400FileDetection.java, que tiene main:
   Crear un Name: DownloadTool
   Project: Info2000
   - En la 1ª pestaña del WASD, Main class, poner: zurich.ae.downloadTool.AqNewAS400FileDetection (sin el .java al final, sinó da error no encuentra rt.jar).
   - En la 2ª pestaña del WASD poner, Argumentos, var0 (la 1ª linea del fichero config.ini).

WebSphere / Hijack Apropiarse el AqConfigAndLog.java y cambiar:
   //IMC-01 Sólo para debug local
   //this.configName = System.getProperty("config");
   // configName = "D:\\Mis Documentos\\Java\\DownloadTool00\\Files\\config.ini";
   //configName = "C:\\Documents and Settings\\tuMacipeI\\My Documents\\IMBORRAR\\Java\\DownloadTool\\config.ini";
   //IMC-01

CAGADA (NO REPETIR):
   Cambiar
   this.configName = System.getProperty("config");
   por
   this.configName = System.getProperty("C:\\Documents and Settings\\tuMacipeI\\My Documents\\IMBORRAR\\Java\\DownloadTool\\config.ini");
   // No hay que poner el this. delante del configName

AqNewAS400FileDetection 00.java: Asteriscar/comentar el sleep.
   //Thread.sleep(60000);

CAGADA: Si pones breakpoint en el main no se para. Hay que ponerlo en la linea siguiente.
- - -
C:\Workspaces\Views\CH_UAT_HOTFIX_RICH\VOB_RICH\Info2000\zurich\ae\downloadTool

C:\Users\ignac\Documents\Java\Zurich\zurich\ae\downloadTool: 10 clases javas:
AqConfigAndLog.java
AqDirectoryChangeEvent.java
AqDirectoryChangeListener.java
AqDirectoryMonitor.java
AqExceptionDownloadTool.java
AqFileDetectionRunnable.java
AqNewAS400FileDetection.java
AqReportDispatcher.java
AqReportTypeTransformation.java
AqWrapperJackcess.java
- - -

main(
AqNewAS400FileDetection*.java
AqReportTypeTransformation 00.java --> AqReportDispatcher*.java

- - -
KK Flujo

- - -

KK
OJO

HashMap
toMap(
.put(
.get(
Thread
.clone()
Vector list = (Vector)_listeners.clone();

EventObject

run()

Runnable

start()


AqDirectoryMonitor.java:
run()
Runnable
start()

Funcion anonima o lambda ({})
AqNewAS400FileDetection.java

- - -
OJO Debug Te puede mostrar el fuente no activo / Para que entre en AqReportTypeTransformation hay que ir a AqFileDetectionRunnable.java para hacer F6=Paso porque lo normal es que se quede en AqFileDetectionRunnable.java que tamb permite F6=Paso y el otro se queda suspendido. Esto es porque el thread es una ejecucion paralela. Comprobar que en panel izquierdo de debug AqFileDetectionRunnable.java no esté Suspended 1 o vafias veces.
- - -

Google: Ascii table

- - -
Vector

- - -

Eclipse / Debug Configurations / Argumentos: var0

F5=Paso Debug se mete mas que F6
F6=Paso Step Debug Eclipse downloadTool

- - -

IMC Motivo del cambio: Me di cuenta de que cuando el campo es numerico y contiene mas de 11 digitos numeros Excel lo transforma a exponencial ejemplo 123456789012 -> 1,23457E+11. Solucion: =T("1234567890123"). Las funciones de Excel =CONCATENATE() y =T() hacen lo mismo, pero =T() lo hace no solo para Office en Ingles sino para todos los idiomas lenguajes.

- - -

Graba un fichero con extension .xls que es ASCII como un csv que se puede abrir con notepad.

- - -

El unico caso de prueba que tiene varios campos (45) de distinto tipo es:
02.INI.TXT
02.CSV.TXT

El fichero INI contiene una linea (COLNAMES) con la cabecera con los nombres de los campos, otra linea (COLTYPES) con sus tipos, otra linea (COLLENGTHS) con sus longitudes, y otra linea (DESTINAT) con el path donde copiar el fichero xls transformado a partir del csv. 

Los campos de entrada pueden ser A=Alfa/String o P=Numerico.

- - -
jackcess v1*.jar no funcionan desde el principio para generar ficheros Access.
Pendiente:
Probar jackcess-4.0.8.jar que es mas reciente. Añadirla al build path

- - -

Se podria ampliar mejorar enviando los ficheros transformados por email o por ftp.

- - -
run:
AqFileDetectionRunnable.java
AqDirectoryMonitor.java

- - -


- - -

ReportTypeTransformation en AqReportTypeTransformation.java se refiere a si se transforma a XLS=Excel o MDB=Access.

- - -

Lee los ficheros txt, ini, etc caracter a caracter, No linea a linea

- - -
config.ini
Diferencia entre monitorpath y rootpath:
monitorpath:,C:\Users\ignac\Documents\Java\DownloadTool00\Files
rootpath:,C:\Users\ignac\Documents\Java\DownloadTool00\Files\Temp
Solucion:
monitorpath es la carpeta donde se leen los ficheros de entrada. Despues de hacer la transformacion se eliminan.
rootpath contiene la carpeta Temp que contiene las carpetas errors (se graban los errores) y logs (se graban los logs).


Por que hay var0, var1, etc? Que utilidad, mejora o ampliacion podria tener?
Solucion:
Podria ser una forma de pasar entornos donde ejecutar la app, ejemplo:
Desarrollo:
C:\Desarrollo\DownloadTool00\Files
C:\Desarrollo\DownloadTool00\Files\Temp
Integracion:
C:\Integracion\DownloadTool00\Files
C:\Integracion\DownloadTool00\Files\Temp
Usuario:
C:\Usuario\DownloadTool00\Files
C:\Usuario\DownloadTool00\Files\Temp
Produccion:
C:\Produccion\DownloadTool00\Files
C:\Produccion\DownloadTool00\Files\Temp

- - -

Oriol programa bastante modularizado ed varias clases y con varias funcione metodos ed no le gusta metodos muy largos

- - -

Despues de transformar el fichero csv a xls, genera log y/o error y lo elimina del directorio de trabajo.

- - -




