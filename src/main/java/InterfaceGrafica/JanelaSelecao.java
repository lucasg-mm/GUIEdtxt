package InterfaceGrafica;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import javax.swing.*;

/**
 * Janela de seleção de um documento para editar.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class JanelaSelecao extends JDialog {

    /**
     * Contém a lista de documentos disponíveis.
     */
    private JList lista;
    /**
     * Botão para escolher um documento.
     */
    private JButton escolher;
    /**
     * Nome do documento escolhido.
     */
    private String escolhido;

    /**
     * Constrói o documento.
     *
     * @param pai janela inicial.
     * @param titulo titulo da janela.
     * @param documentos lista com os nomes dos documentos escolhidos.
     */
    public JanelaSelecao(JanelaInicial pai, String titulo, ArrayList<String> documentos) {
        super(pai, titulo, true);

        //cria a lista:
        lista = new JList(documentos.toArray());
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setLayoutOrientation(JList.VERTICAL);
        lista.setVisibleRowCount(-1);

        //cria o botão:
        escolher = new JButton("Selecionar");
        escolher.addActionListener(new SelecionarListener());
        this.add(escolher, BorderLayout.SOUTH);

        //cria o scroll:
        JScrollPane listScroller = new JScrollPane(lista);
        listScroller.setPreferredSize(new Dimension(250, 80));
        this.add(listScroller, BorderLayout.CENTER);

        //definições gerais:
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {  //listener para a operação de fechar
            @Override
            public void windowClosing(WindowEvent e) {
                escolhido = "#fechar#";
                JanelaSelecao.this.setVisible(false);
            }
        });
        this.pack();
        this.setVisible(true);
    }

    public String getEscolhido() {
        return this.escolhido;
    }

    /**
     * Listener para quando o botão for pressionado.
     */
    class SelecionarListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            escolhido = (String) lista.getSelectedValue();

            JanelaSelecao.this.setVisible(false);
        }

    }
}
