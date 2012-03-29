package Java.descargas;

/**
 * Created by IntelliJ IDEA.
 * User: saysrodriguez
 * Date: 28/03/12
 * Time: 18:04
 * To change this template use File | Settings | File Templates.
 */

import com.sun.javaws.progress.Progress;
import com.sun.javaws.ui.DownloadWindow;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.jnlp.DownloadService;
import javax.swing.*;
import javax.swing.event.*;

public class DownloadManager extends JFrame implements Observer{

    private JTextField agregarTextField;
    private DownloadsTableModel modeloTabla;
    private JTable tabla;
    private JButton botonPausa, botonReanudar, botonCancelar, botonLimpiar;

    //Descarga seleccionada
    private Download seleccionarDescarga;

    //Marca si la seleccion de tabla se esta limpiando
    private boolean limpiando;

    //Metodo Constructor
    public DownloadManager(){
        setTitle("Administrador de Descargas");
        setSize(680, 480);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });

        //Configuracion del Menu
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuItem = new JMenuItem("Salir", KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                actionExit();
            }
        });
        menuArchivo.add(fileExitMenuItem);
        barraMenu.add(menuArchivo);
        setJMenuBar(barraMenu);

        //Configura un panel de adicion
        JPanel agregarPanel = new JPanel();
        agregarTextField = new JTextField(30);
        agregarPanel.add(agregarTextField);
        JButton agregarBoton = new JButton("Agregar Descarga");
        agregarBoton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                actionAdd();
            }
        });
        agregarPanel.add(agregarBoton);

        //Configurar una tabla de Descargas
        modeloTabla = new DownloadsTableModel();
        tabla = new JTable(modeloTabla);
        tabla.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                tableSelectionChanged();
            }
        });

        //Solo permite que se seleccione una fila a la vez
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Configurar ProgressBar para representar la columna de avance
        ProgressRender renderer = new ProgressRender(0, 100);
        renderer.setStringPainted(true); //muestra texto de avance
        tabla.setDefaultRenderer(JProgressBar.class, renderer);

        //Define  una altura de fila suficiente para JProgressBar
        tabla.setRowHeight(
                (int) renderer.getPreferredSize().getHeight()
        );

        //Configurar el panel de Descargas
        JPanel panelDescargas = new JPanel();
        panelDescargas.setBorder(BorderFactory.createTitledBorder("Descargas"));
        panelDescargas.setLayout(new BorderLayout());
        panelDescargas.add(new JScrollPane(tabla), BorderLayout.CENTER);

        //Configura un panel de botones
        JPanel panelBotones = new JPanel();
        botonPausa = new JButton("Pausa");
        botonPausa.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                actionPause();
            }
        });

        botonPausa.setEnabled(false);
        panelBotones.add(botonPausa);
        botonReanudar = new JButton("Reanudar");
        botonReanudar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                actionResume();
            }
        });

        botonReanudar.setEnabled(false);
        panelBotones.add(botonReanudar);
        botonCancelar = new JButton("Cancelar");
        botonCancelar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                actionCancel();
            }
        });

        botonCancelar.setEnabled(false);
        panelBotones.add(botonCancelar);
        botonLimpiar = new JButton("Limpiar");
        botonLimpiar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                actionClean();
            }
        });

        botonLimpiar.setEnabled(false);
        panelBotones.add(botonLimpiar);

        //Agregar paneles de despliegue
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(agregarPanel, BorderLayout.NORTH);
        getContentPane().add(panelDescargas, BorderLayout.CENTER);
        getContentPane().add(panelBotones, BorderLayout.SOUTH);

    }

    private void actionExit(){
        System.exit(0);
    }

    private void actionAdd(){
      URL verifiedURL = verifyURL(agregarTextField.getText());
        if (verifiedURL !=  null){
            modeloTabla.addDescarga(new Download(verifiedURL));
            agregarTextField.setText("");
        }else {
            JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Verificar URL Descargado
    private URL verifyURL(String url){
        //Permite URL´s de HTTP
        if (!url.toLowerCase().startsWith("http://"))
            return null;

        //Verifica formato de la URL
        URL verifiedURL = null;
        try{
            verifiedURL = new URL(url);
        } catch (Exception e){
            return null;
        }

        //Asegura que el URL especifica un archivo
        if (verifiedURL.getFile().length() < 2){
            return null;
        }
        return verifiedURL;
    }

    //Se llama cuando cambia la seleccion de una fila
    private void tableSelectionChanged(){

        if (seleccionarDescarga != null)
            seleccionarDescarga.deleteObserver(DownloadManager.this);

        if (!limpiando){
            seleccionarDescarga = modeloTabla.getDownload(tabla.getSelectedRow());
            seleccionarDescarga.addObserver(DownloadManager.this);
            actualizarBoton();
        }

    }

    private  void actionPause(){
        seleccionarDescarga.pausa();
        actualizarBoton();
    }

    private void actionResume(){
        seleccionarDescarga.resume();
        actualizarBoton();
    }

    private void actionCancel(){
        seleccionarDescarga.cancelar();
        actualizarBoton();
    }

    private void actionClean(){
        limpiando = true;
        modeloTabla.clearDescarga(tabla.getSelectedRow());
        limpiando = false;
        seleccionarDescarga = null;
        actualizarBoton();
    }

    //Actualizar el estado de cada boton
    private void actualizarBoton(){
        if (seleccionarDescarga != null){
            int estatus = seleccionarDescarga.getEstatus();
            switch (estatus){
                case Download.DESCARGANDO:
                    botonPausa.setEnabled(true);
                    botonReanudar.setEnabled(false);
                    botonCancelar.setEnabled(true);
                    botonLimpiar.setEnabled(false);
                    break;
                case Download.PAUSADO:
                    botonPausa.setEnabled(false);
                    botonReanudar.setEnabled(true);
                    botonCancelar.setEnabled(true);
                    botonLimpiar.setEnabled(false);
                    break;
                case Download.ERROR:
                    botonPausa.setEnabled(false);
                    botonReanudar.setEnabled(false);
                    botonCancelar.setEnabled(false);
                    botonLimpiar.setEnabled(true);
                    break;
                default: //completa o cancelada
                    botonPausa.setEnabled(false);
                    botonReanudar.setEnabled(false);
                    botonCancelar.setEnabled(false);
                    botonLimpiar.setEnabled(true);
            }
        }else {
            //no hay descargas seleccionadas
            botonPausa.setEnabled(false);
            botonReanudar.setEnabled(false);
            botonCancelar.setEnabled(false);
            botonLimpiar.setEnabled(false);
        }
    }

    public void update(Observable o, Object arg){
        if (seleccionarDescarga != null && seleccionarDescarga.equals(o))
            actualizarBoton();
    }

    public static void main(String [] args){
        DownloadManager manager = new DownloadManager();
        manager.show();
    }


}
