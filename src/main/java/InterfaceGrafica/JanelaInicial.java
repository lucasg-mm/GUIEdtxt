package InterfaceGrafica;

import Documento.documento;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.*;

/**
 * Janela inicial do editor.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class JanelaInicial extends JFrame {

    /**
     * JLabel que contém o nome do editor de texto.
     */
    private JLabel nomeEditor;      //contém o nome do editor, "EdTXT"

    /**
     * Container com botões.
     */
    private Container botoes;  //segura os botões

    /**
     * Estilo do container.
     */
    private GridLayout estiloContainer;  //define o gerenciador de estilo do container acima

    /**
     * Botão para abrir um documento já existente.
     */
    private JButton abrir;  //botão para abrir um documento já existente

    /**
     * Thread utilizada para se conectar ao servidor.
     */
    private Thread conectando;  //thread utilizada para conectar

    /**
     * Conexão com o servidor.
     */
    private Socket conexao;

    /**
     * Botão para criar um novo documento de texto.
     */
    private JButton novo;  //botão para criar um novo documento
    /**
     * Fluxo de entrada.
     */
    private ObjectInputStream input;
    /**
     * Fluxo de saída.
     */
    private ObjectOutputStream output;

    /**
     * Constrói a janela.
     */
    public JanelaInicial() {  //construtor da janela
        //define o título da janela:
        super("Início");

        //instancia os dois botões:
        abrir = new JButton("Abrir");
        novo = new JButton("Novo");

        //adiciona action listeners aos botões:
        abrir.addActionListener(new abrirDocListener());
        novo.addActionListener(new novoDocListener());

        //instancia o container e define seu estilo:
        botoes = new Container();
        estiloContainer = new GridLayout(1, 2, 10, 0);
        botoes.setLayout(estiloContainer);

        //adiciona os botões ao container:
        botoes.add(abrir);
        botoes.add(novo);

        //adiciona o container à janela:
        this.add(botoes, BorderLayout.SOUTH);

        //instancia o label:
        nomeEditor = new JLabel("EdTXT");
        nomeEditor.setHorizontalAlignment(SwingConstants.CENTER);

        //adiciona o label à janela:
        this.add(nomeEditor, BorderLayout.CENTER);

        //Configurações gerais da janela:
        ImageIcon img = new ImageIcon("images/icon.png");  //define icone
        this.setIconImage(img.getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 100);
        this.setResizable(false);
        this.setVisible(true);
    }

    //action listeners dos botões:
    /**
     * Listener que age quando o botão de "novo" é apertado.
     */
    class novoDocListener implements ActionListener {  //action listener do evento de pressionar o botão "Novo"

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {

                conectaServidor();
                conectando.join();

                criarDocumento();
            } catch (InterruptedException ex) {
                System.out.println("Erro durante a conexão com o servidor.");
            }
        }

    }

    /**
     * Listener que age quando o botão de "abrir" é apertado.
     */
    class abrirDocListener implements ActionListener {  //action listener do evento de pressionar o botão "Abrir"

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {

                conectaServidor();
                conectando.join();

                abrirDocumento();
            } catch (InterruptedException ex) {
                System.out.println("Erro durante a conexão com o servidor.");
            }
        }

    }

    /**
     * Cria uma thread para se conectar ao servidor e criar os fluxos de entrada
     * e saída.
     */
    private void conectaServidor() {  //conecta-se ao servidor e cria os fluxos de i/o

        conectando = new Thread() {
            @Override
            public void run() {
                try {
                    conexao = new Socket("127.0.0.1", 12345);  //conecta-se ao servidor
                    output = new ObjectOutputStream(conexao.getOutputStream());
                    output.flush();
                    input = new ObjectInputStream(conexao.getInputStream());
                } catch (IOException ex) {
                    System.out.println("Erro durante a conexão com o servidor.");
                }
            }
        };

        conectando.start();
    }

    /**
     * Abre a janela de criação de documentos. Quando o usuário terminar de
     * criar o documento, manda uma mensagem para o server criar o doc
     * remotamente.
     */
    private void criarDocumento() {  //cria um documento no servidor e inicia sua edição
        Thread queryThread = new Thread() {
            ArrayList<String> docsDisponiveis;
            LinkedList<Character> docEmList;
            String docACriar;

            @Override
            public void run() {
                //escolhe um dos documentos disponibilizado pelo servidor:
                try {
                    docsDisponiveis = (ArrayList<String>) input.readObject();
                    JanelaCriacao c = new JanelaCriacao(JanelaInicial.this, docsDisponiveis);
                    docACriar = c.getNome();
                    output.writeUTF(docACriar);
                    output.flush();

                    if (docACriar.equals("#fechar#")) {
                        input.close();
                        output.close();
                        conexao.close();
                        c.dispose();
                        return;
                    }

                    //recebe o documento em forma de lista encadeada:
                    docEmList = (LinkedList<Character>) input.readObject();
                } catch (ClassNotFoundException | IOException ex) {
                    System.out.println("Erro durante a conexão com o servidor.");
                }
                documento docAberto = new documento();  //cria um novo documento
                docAberto.setConteudo(docEmList);  //define o conteudo
                docAberto.setNome(docACriar);

                //abre a janela de edição:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JanelaEdicao je = new JanelaEdicao(docAberto, input, output, conexao);
                        } catch (IOException ex) {
                            System.out.println("Erro durante a conexão com o servidor.");
                        }
                    }
                });

                JanelaInicial.this.dispose();  //fecha a janela inicial                
            }
        };

        queryThread.start();
    }

    /**
     * Pede para o servidor um documento cujo nome é informado pelo usuário. O
     * servidor manda em formato de lista  encadeada de caracteres.
     */
    private void abrirDocumento() {  //abre um documento já existente no servidor
        Thread queryThread = new Thread() {
            ArrayList<String> docsDisponiveis;
            LinkedList<Character> docEmList = null;

            String escolhido;

            @Override
            public void run() {
                try {
                    //escolhe um dos documentos disponibilizado pelo servidor:
                    docsDisponiveis = (ArrayList<String>) input.readObject();
                    JanelaSelecao s = new JanelaSelecao(JanelaInicial.this, "Escolha um documento", docsDisponiveis);
                    escolhido = s.getEscolhido();
                    output.writeUTF(escolhido);
                    output.flush();
                    if (escolhido.equals("#fechar#")) {
                        input.close();
                        output.close();
                        conexao.close();
                        s.dispose();
                        return;
                    }

                    docEmList = (LinkedList<Character>) input.readObject();

                } catch (ClassNotFoundException | IOException ex) {
                    System.out.println("Erro durante a conexão com o servidor");
                }

                documento docAberto = new documento();  //cria um novo documento
                docAberto.setConteudo(docEmList);  //define o conteudo
                docAberto.setNome(escolhido);

                //abre a janela de edição:
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JanelaEdicao je = new JanelaEdicao(docAberto, input, output, conexao);
                        } catch (IOException ex) {
                            System.out.println("Erro durante a conexão com o servidor.");
                        }
                    }
                });

                JanelaInicial.this.dispose();  //fecha a janela inicial
            }
        };

        queryThread.start();
    }
}
