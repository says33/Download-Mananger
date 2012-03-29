package Java.descargas;

/**
 * Created by IntelliJ IDEA.
 * User: saysrodriguez
 * Date: 28/03/12
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class DownloadsTableModel extends AbstractTableModel implements Observer{

    //son los nombres de las columnas de la tabla
    private static final String[] nombresColumnas = {"URL","TAMAÑO", "PROGRESO", "ESTATUS"};

    //Son la clase de  los valores de cada columna
    private static final Class[] clasesColumnas = {String.class, String.class, JProgressBar.class, String.class};

    //la lista de descargas de la tabla
    private ArrayList listaDecargas = new ArrayList();

    //Agregar una nueva descarga a la tabla
    public void addDescarga(Download download){
        download.addObserver(this);
        listaDecargas.add(download);
        //Dispara la notificacion de insercion de fila en la tabla
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    //Obtiene una descarga de la fila especifica
    public Download getDownload(int row){
        return (Download) listaDecargas.get(row);
    }

    //Eliminar una descarga de la lista
    public void clearDescarga(int row){
        listaDecargas.remove(row);
        //Dispara la notificacion de eliminacion de fila de la tabla
        fireTableRowsDeleted(row, row);
    }

    //obtiene la cuenta de columnas de la tabla
    public  int getColumnCount(){
        return nombresColumnas.length;
    }

    //obtiene el nombre d ela columna
    public String getColumnName(int col){
        return nombresColumnas[col];
    }

    //Obtiene la clase de la columna
    public Class getColumnClass(int col){
        return clasesColumnas[col];
    }

    //Obtiene  la cuenta de filas de una tabla
    public int getRowCount(){
        return listaDecargas.size();
    }

    //obtiene el valor de combinacion de fila y columna especifica
    public Object getValueAt(int row, int col){
        Download download = (Download) listaDecargas.get(row);
        switch (col){
            case 0: //URL
                return download.getUrl();
            case 1: //Tamaño
                int size = download.getTamaño();
                return (size == -1) ? "" : Integer.toString(size);
            case 2: //Proceso
                return new Float(download.getProceso());
            case 3: //Estatus
                return  Download.ESTATUS[download.getEstatus()];
        }
        return "";
    }

    //Se manda llamar al metodo Update cuando  Download notifica algun cambio
    public void update(Observable o, Object arg){
        int indice = listaDecargas.indexOf(o);
        //La primera fila de una tabla actualiza la notificacion
        fireTableCellUpdated(indice, indice);
    }


}

