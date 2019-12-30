package InterfaceGrafica;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * Classe que representa a janela de criação de um novo documento no servidor.
 * 
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class JanelaCriacao extends JDialog {
    /**
     * Campo de texto em que o usuário deve inserir o nome do novo documento que deseja criar.
     */
    JTextField nomeNovoDoc;  //nome do novo documento a ser criado pelo cliente
    /**
     * Botão para concluir a ação de criar o documento.
     */
    JButton criar;
    /**
     * Nome do novo documento a ser criado.
     */
    String nome;
    /**
     * Nomes dos documentos já existentes no servidor.
     */
    ArrayList<String> nomesDocs;  //nomes dos documentos já existentes no servidor

    /**
     * Constrói a janela.
     * @param pai a janela inicial.
     * @param nomesDocs nomes dos documentos já existentes no servidor.
     */
    public JanelaCriacao(JanelaInicial pai, ArrayList<String> nomesDocs) {
        super(pai, "Insira o nome do arquivo", true);
        
        this.nomesDocs = nomesDocs;

        nomeNovoDoc = new JTextField();
        nomeNovoDoc.setEditable(true);
        this.add(nomeNovoDoc, BorderLayout.CENTER);

        criar = new JButton("Criar");
        criar.addActionListener(new CriarListener());
        this.add(criar, BorderLayout.SOUTH);

        //definições gerais:
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {  //listener para a operação de fechar
            @Override
            public void windowClosing(WindowEvent e) {
                nome = "#fechar#";
                JanelaCriacao.this.setVisible(false);
            }
        });        
        this.setSize(350, 80);
        this.setVisible(true);
        this.setResizable(false);
    }

    public String getNome() {

        return nome;
    }

    public void setNomesDocs(ArrayList<String> nomesDocs) {
        this.nomesDocs = nomesDocs;
    }
    
    /**
     * Listener para quando o botão de criar documento for pressionado.
     */
    class CriarListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            nome = nomeNovoDoc.getText() + ".txt";

            if (nome.equals(".txt")) {  //usuário inseriu um nome vazio
                JOptionPane.showMessageDialog(JanelaCriacao.this, "Insira um nome válido.");
                return;
            }
            if(nomesDocs.contains(nome)){  //usuário tentou criar um documento com o mesmo nome de outro no servidor
                JOptionPane.showMessageDialog(JanelaCriacao.this, "Insira um nome válido.");                
                return;
            }
            
            JanelaCriacao.this.setVisible(false);
        }

    }
}
