package Java.descargas;

/**
 * Created by IntelliJ IDEA.
 * User: saysrodriguez
 * Date: 27/03/12
 * Time: 17:43
 * To change this template use File | Settings | File Templates.
 */
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Observable;
import java.net.*;

public class Download  extends Observable implements Runnable {

    //tamaño maximo del buffer de Descarga.
    private static final int  TAMAÑO_MAXIMO_BUFFER = 1024;

    //nombre de los estados que puede tener la descarga.
    public static final String ESTATUS[] = {"DESCARGANDO", "PAUSADO", "DESCARGA COMPLETA", "CANCELADA", "ERROR"};

    public static final int DESCARGANDO = 0;
    public static final int PAUSADO = 1;
    public static final int DESCARGA_COMPLETA = 2;
    public static final int CANCELADA = 3;
    public static final int ERROR = 4;

    private URL url;
    private int tamaño;//tamaño de la descarga
    private int descargando;//numero  de bytes descargados
    private int estatus;//estatus actual de la descarga

    public Download(URL url){
        this.url = url;
        tamaño = -1;
        descargando = 0;
        estatus = DESCARGANDO;

        descarga();
    }

    public String getUrl(){
        return url.toString();
    }

    public int getTamaño(){
        return tamaño;
    }

    public float getProceso(){
        return ((float) descargando / tamaño) * 100;
    }

    public int getEstatus(){
        return estatus;
    }

    //Realiza una pausa en la descarga
    public void pausa(){
        estatus = PAUSADO;
        stateChanged();
    }

    //Reanudar la descarga
    public void resume(){
        estatus = DESCARGANDO;
        stateChanged();
        descarga();
    }

    //Cancelar descarga
    public void cancelar(){
        estatus = CANCELADA;
        stateChanged();
    }

    //Descarga con error
    private void error(){
        estatus = ERROR;
        stateChanged();
    }

    //Iniciar o reanudar la descarga
    private void descarga(){
        Thread hilo = new Thread(this);
        hilo.start();
    }

    //Obtiene la parte de nombre de archivo del URL
    private String getNombreDelArchivo(URL url){
        String nombreArchivo = url.getFile();
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('/') + 1);
    }

    //Descargar Archivo
    public void run(){
        RandomAccessFile archivo = null;
        InputStream corrida = null;

        try{
            //Abre conexion con URL
            HttpURLConnection coneccion = (HttpURLConnection) url.openConnection();
            //Especificar la parte del archivo que se descargara
            coneccion.setRequestProperty("Range", "bytes = " + descargando + "-");
            //Conectar con el servidor
            coneccion.connect();

            if (coneccion.getResponseCode() / 100 != 2){
                error();
            }

            //Revisar la longitud de contenido sea valido
            int tamañoContenido = coneccion.getContentLength();
            if (tamañoContenido < 1){
                tamaño = tamañoContenido;
                error();
            }

            /* Establese el tamaño de la descarga si
                aun no se ha establecido  */
            if (tamaño == -1 ){
                tamaño = tamañoContenido;
                stateChanged();
            }

            //Abre el archovo y busca su fin
            archivo = new RandomAccessFile(getNombreDelArchivo(url), "rw");
            archivo.seek(descargando);

            corrida = coneccion.getInputStream();
            while (estatus == DESCARGANDO){
                //Fija el tamaño del buffer de acuerdo con la parte del archivo que falta por descargar
                byte buffer[];
                if (tamaño - descargando > TAMAÑO_MAXIMO_BUFFER){
                    buffer = new byte[TAMAÑO_MAXIMO_BUFFER];
                }else {
                buffer = new byte[tamaño - descargando];
                }

                //Lee del servidor al buffer
                int leer = corrida.read(buffer);
                if (leer == -1) break;

                //Escribe del buffer al archivo
                archivo.write(buffer, 0, leer);
                descargando += leer;
                stateChanged();
            }

            //Cambia el estatus a completo, por que se ha terminado la descarga.
            if (estatus == DESCARGANDO){
                estatus = DESCARGA_COMPLETA;
                stateChanged();
            }



        } catch (Exception e) {
            error();
        } finally {
            //Cerrar el archivo
            if (archivo != null){
                try{
                    archivo.close();
                } catch (Exception e){
                    //Crear metodo de ventana que indique que el archivo no se cerro correctamente
                }
            }
        }

        //Cerrar la conexion con el servidor
        if (corrida != null){
            try{
                corrida.close();
            } catch (Exception e){
                   //Crear metodo de ventana que indique que el archivo no se cerro correctamente
            }
        }
    }

    //Notifica a los observadores l cambio del estatus de la descarga
    private void stateChanged(){
        setChanged();
        notifyObservers();
    }

}
