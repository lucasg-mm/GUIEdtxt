package Principal;

import InterfaceGrafica.JanelaInicial;
import javax.swing.SwingUtilities;

/**
 * Logica principal do programa.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class prog_principal {  //inicializa o programa 

    /**
     * Inicializa o programa.
     *
     * @param args
     */
    public static void main(String[] args) {
        //interface gráfica:
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JanelaInicial j = new JanelaInicial();
            }
        });        
    }
}
