package Java.descargas;
/**
 * Created by IntelliJ IDEA.
 * User: saysrodriguez
 * Date: 28/03/12
 * Time: 13:16
 * To change this template use File | Settings | File Templates.
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class ProgressRender extends JProgressBar implements TableCellRenderer{

    public ProgressRender(int minimo, int maximo){
        super(minimo, maximo);
    }

    public Component getTableCellRendererComponent(
            JTable tabla, Object value, boolean isSelected, boolean hasFocus, int row, int column){
         //Establece el valor  de porcentaje completo de  la barra de progreso.
        setValue((int) ((Float) value).floatValue());
        return this;
    }
}

