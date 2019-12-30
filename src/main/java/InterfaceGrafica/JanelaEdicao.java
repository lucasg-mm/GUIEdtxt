package InterfaceGrafica;

import Mensagem.Mensagem;
import Documento.documento;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import utilitarios.*;
import java.awt.*;
import java.net.*;

/**
 * Janela em que é realizada a edição de texto.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class JanelaEdicao extends JFrame {

    /**
     * A barra de menu que fica um pouco acima na janela.
     */
    private JMenuBar barraMenu;  //a barra de menu

    /**
     * O menu que fica na barra.
     */
    private JMenu menu;  //o menu

    /**
     * Cada item do menu.
     */
    private JMenuItem itemMenu;  //item de cada menu

    /**
     * A área de texto em que ocorrea a edição.
     */
    private JTextArea caixaEdicao = new JTextArea();  //cada aba terá uma caixa de edição

    /**
     * O documento que está sendo editado.
     */
    private documento docEditado;  //cada aba também terá um documento a ser editado

    /**
     * O cursor associado ao documento editado.
     */
    private cursor cursorDoc = new cursor();  //um cursor está sempre associado a um documento

    /**
     * Pilha de ações que podem ser desfeitas.
     */
    private LinkedList<acoes> refazer = new LinkedList();  //cada  documento tem suas listas de desfazer e refazer

    /**
     * Pilha de ações que podem ser refeitas.
     */
    private LinkedList<acoes> desfazer = new LinkedList();

    /**
     * Área de transferência para copiar e recortar.
     */
    private AreaTransferencia clipboard = new AreaTransferencia();

    /**
     * DocumentListener associado à área de texto.
     */
    private DocumentListener edicListener;  //precisamos guardar o DocumentListener para removê-lo nas horas a propriadas (ou ele pode atrapalhar a execução de algumas funções)

    /**
     * Botão de salvar. Quando ele está desativado, significa que um "novo
     * documento" de texto está sendo editado.
     */
    private JMenuItem salvar;  //quando o documento é um novo documento de texto, esse botão fica indisponível, por isso precisamos guardá-lo

    /**
     * Flag que diz se todas as alterações no documento foram salvas.
     */
    private boolean estaSalvo = true;  //flag que indica se o conteúdo do arquivo está salvo ou não

    /**
     * Flag que diz se o usuário apertou o botão de fechar.
     */
    private boolean querSair = false;  //indica se o usuário clicou no botão de sair

    /**
     * Janela de confirmação. Ela pergunta se o usuário deseja salvar as
     * alterações.
     */
    private JFrame janelaConfirmacao;
    /**
     * Conexão do cliente com o servidor.
     */
    private Socket conexao;
    /**
     * Fluxo de entrada para o cliente.
     */
    private ObjectInputStream input;
    /**
     * Fluxo de saída para o cliente.
     */
    private ObjectOutputStream output;
    /**
     * Flag que diz se uma ação executada no editor foi causada por outro cliente.
     */
    private boolean causadoOutro;   //flag que diz se uma certa ação foi causada por outro cliente
    /**
     * Executa a cada meio segundo o salvamento do documento.
     */
    private TimerTask salvarAutomatico;  //TimerTask responsável por realizar o salvamento automático do arquivo
    /**
     * Agenda o salvamento automático.
     */
    private java.util.Timer timer;
    /**
     * Thread que fica esperando mensagens do servidor.
     */
    private Thread esperaMensagem;
    /**
     * Número de clientes conectados ao documento.
     */
    protected int numClientes;
    /**
     * Item do menu que mostra o número de clientes conectados ao documento.
     */
    protected JMenuItem mostraConectados;  //item de menu que mostra o número de clientes conectados

    public void setCausadoOutro(boolean causadoOutro) {
        this.causadoOutro = causadoOutro;
    }
    
    /**
     * Constrói a janela.
     * @param docAberto documento que será editado.
     * @param input fluxo de entrada.
     * @param output fluxo de saída.
     * @param conexao conexão com o servidor.
     * @throws IOException 
     */
    public JanelaEdicao(documento docAberto, ObjectInputStream input, ObjectOutputStream output, Socket conexao) throws IOException {  //construtor para quando um documento é aberto
        super(docAberto.getNome());
        
        this.causadoOutro = false;
        this.numClientes = input.readInt();
        this.input = input;
        this.output = output;
        this.conexao = conexao;

        //adiciona o doc ao ArrayList e o cursor:
        docEditado = docAberto;
        constroiMenu();
        geraPainelTexto();
        listToTextArea(docAberto);

        edicListener = new edicaoListener();
        caixaEdicao.getDocument().addDocumentListener(edicListener);  //adiciona um document listener

        //configurações gerais da janela:
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {  //listener para a operação de fechar
            @Override
            public void windowClosing(WindowEvent e) {
                lidaFechamento();
            }
        });
        this.setSize(600, 600);
        this.setVisible(true);

        ouvir();

        //inicia o salvamento automático:
        salvarAutomatico = new TimerTask() {
            @Override
            public void run() {
                salvar.doClick();
            }

        };
        timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(salvarAutomatico, 500, 500);  //salva automaticamente a cada meio segundo
        caixaEdicao.getCaret().setDot(0);
    }
    
    /**
     * Inicia a thread que recebe mensagens e informa que um cliente foi conectado.
     */
    private void ouvir() {
        try {
            esperaMensagem = new Thread(new Recebedor(caixaEdicao, input, JanelaEdicao.this));
            esperaMensagem.start();
            Mensagem msgEnviada = new Mensagem();
            msgEnviada.setComando("p");  //informa que um cliente foi conectado
            output.writeObject(msgEnviada);
        } catch (IOException ex) {
            System.out.println("Erro ao iniciar o processo de listening.");
        }
    }

    /**
     * Lógica para quando o usuário aperta o botão de fechar.
     */
    private void lidaFechamento() {  //lida com o fechamento do programa (pergunta se o usuário deseja salvar, se precisar)
        if (estaSalvo == false) {
            //construção de uma janela de confirmação:
            janelaConfirmacao = new JFrame("Fechar");
            JLabel mensagem = new JLabel("Deseja salvar o documento antes de sair?");
            JButton sim = new JButton("Sim");
            JButton nao = new JButton("Não");

            //centraliza e adiciona o label:
            mensagem.setHorizontalAlignment(SwingConstants.CENTER);
            janelaConfirmacao.add(mensagem, BorderLayout.CENTER);

            //insere os botões num Container e insere este último na janela de confirmação:
            Container botoes = new Container();
            botoes.setLayout(new GridLayout(1, 2, 10, 0));
            botoes.add(sim);
            botoes.add(nao);
            nao.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {  //sai sem salvar
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            JanelaEdicao.this.encerraCliente();
                            janelaConfirmacao.dispose();
                        }
                    };

                    Thread t = new Thread(r);
                    t.start();
                }
            });
            janelaConfirmacao.add(botoes, BorderLayout.SOUTH);
            querSair = true;

            sim.addActionListener(new salvarListener());
            janelaConfirmacao.setSize(300, 100);
            janelaConfirmacao.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            janelaConfirmacao.setVisible(true);
        } else {
            encerraCliente();
        }
    }
    /**
     * fecha todos os fluxos e conexões e se livra da janela.
     */
    public void encerraCliente() {
        try {
            Mensagem msgEnviada = new Mensagem();
            msgEnviada.setComando("m");  //inserção
            output.writeObject(msgEnviada);
        } catch (IOException ex) {
            System.out.println("Erro ao desconectar.");
        }

        //para a thread do recebedor:
        esperaMensagem.interrupt();

        //para o TimerTask:
        timer.cancel();

        //desconecta o ObjectOutputStream, ObjectInputStream e o Socket:
        try {
            input.close();
            output.close();
            conexao.close();
        } catch (IOException ex) {
            System.out.println("Erro durante o fechamento do cliente.");
        }

        JanelaEdicao.this.dispose();
    }

    /**
     * Constrói o menu da parte superior.
     */
    private void constroiMenu() {
        //instanciação da barra:
        barraMenu = new JMenuBar();

        //construindo o menu "Arquivo":
        menu = new JMenu("Arquivo");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription(
                "Menu para abrir, salvar e criar arquivos.");
        barraMenu.add(menu);

        //construindo os itens do menu "Arquivo":
        itemMenu = new JMenuItem("Salvar");
        itemMenu.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        itemMenu.getAccessibleContext().setAccessibleDescription(
                "Salva o arquivo em edição.");
        itemMenu.addActionListener(new salvarListener());
        salvar = itemMenu;

        itemMenu = new JMenuItem("Conectados: " + numClientes);
        menu.add(itemMenu);
        mostraConectados = itemMenu;

        itemMenu = new JMenuItem("Desconectar todos");
        itemMenu.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_U, ActionEvent.CTRL_MASK));
        itemMenu.getAccessibleContext().setAccessibleDescription(
                "Desconecta todos os clientes conectados ao documento.");
        itemMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    Mensagem msgEnviada = new Mensagem();
                    msgEnviada.setComando("d");  //inserção
                    output.writeObject(msgEnviada);
                    output.flush();
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException ex) {
                }
                JanelaEdicao.this.encerraCliente();
            }

        });
        menu.add(itemMenu);

        //Construindo os itens do menu "Editar":        
        menu = new JMenu("Editar");
        menu.setMnemonic(KeyEvent.VK_B);
        menu.getAccessibleContext().setAccessibleDescription(
                "Menu com algumas ferramentas de edição");
        barraMenu.add(menu);

        //construindo os itens do menu "Editar":
        itemMenu = new JMenuItem("Recortar");
        itemMenu.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        itemMenu.getAccessibleContext().setAccessibleDescription(
                "Salva o arquivo em edição");
        itemMenu.addActionListener(new recortarListener());
        menu.add(itemMenu);

        itemMenu = new JMenuItem("Copiar");
        itemMenu.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        itemMenu.getAccessibleContext().setAccessibleDescription(
                "Abre um aquivo pré-existente para edição");
        itemMenu.addActionListener(new copiarListener());
        menu.add(itemMenu);

        itemMenu = new JMenuItem("Colar");
        itemMenu.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        itemMenu.getAccessibleContext().setAccessibleDescription(
                "Cria um novo arquivo para edição");
        itemMenu.addActionListener(new colarListener());
        menu.add(itemMenu);

        itemMenu = new JMenuItem("Desfazer");
        itemMenu.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        itemMenu.getAccessibleContext().setAccessibleDescription(
                "Desfaz uma ação");
        itemMenu.addActionListener(new desfazerListener());
        menu.add(itemMenu);

        itemMenu = new JMenuItem("Refazer");
        itemMenu.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        itemMenu.getAccessibleContext().setAccessibleDescription(
                "Refaz uma ação");
        itemMenu.addActionListener(new refazerListener());
        menu.add(itemMenu);

        //adiciona a barra:
        this.setJMenuBar(barraMenu);
    }

    /**
     * Gera um painel com uma área de texto, onde ocorre a edição do texto.
     */
    private void geraPainelTexto() {  //gera um painel com uma área de texto, onde ocorrerá a edição
        //cria o painel e a área de texto:
        JPanel painel = new JPanel(false);
        JTextArea areaTexto = new JTextArea();
        areaTexto.addCaretListener(new cursorListener());  //adiciona um caret listener

        //configura a área de texto para quebrar linha quando uma string muito longa é inserida:
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);

        //adiciona no ArrayList e insere scroll:
        caixaEdicao = areaTexto;
        caixaEdicao.setDocument(new CustomPlainDocument());
        cursorDoc = new cursor();  //gera o cursor
        desfazer = new LinkedList();
        refazer = new LinkedList();
        JScrollPane areaEdicaoScroll = new JScrollPane(areaTexto);

        //define estilo do painel e funde os dois:
        painel.setLayout(new GridLayout(1, 1));
        painel.add(areaEdicaoScroll);

        this.add(painel);
    }

    /**
     * Escreve numa área de texto o conteúdo de um objeto "documento".
     *
     * @param aEscrever O documento cujo conteúdo será escrito na área de texto.
     */
    private void listToTextArea(documento aEscrever) {  //escreve na área de texto o conteúdo da lista encadeada "conteudo"
        caixaEdicao.append(aEscrever.imprime());
    }

    /**
     * Listener que monitora qualquer alteração feita na área de texto.
     */
    class edicaoListener implements DocumentListener {  //monitora o Document atrelado à área de texto

        /**
         * CustomPlainDocument atrelado à área de texto.
         */
        private CustomPlainDocument documentCaixa;

        /**
         * Se algo for inserido na área de texto, esse algo também é inserido no
         * conteúdo do objeto da classe documento.
         *
         * @param de Evento.
         */
        @Override
        public void insertUpdate(DocumentEvent de) {  //conteúdo inserido no Document
            int menor;
            if (caixaEdicao.getCaret().getDot() < caixaEdicao.getCaret().getMark()) {  //se ocorrer seleção
                menor = caixaEdicao.getCaret().getDot();
            } else {
                menor = caixaEdicao.getCaret().getMark();
            }

            cursorDoc.setPosicao_corrente(menor);  //atualiza a posição do cursor no caso de uma seleção seguida de inserção

            if (de.getDocument() instanceof CustomPlainDocument) {
                documentCaixa = (CustomPlainDocument) de.getDocument();
                docEditado.geraAcao("i " + documentCaixa.getTextoInserido(), cursorDoc, refazer, desfazer);  //insere o texto escrito na área de texto
            }
            estaSalvo = false;

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    //envia mensagem com os detalhes da inserção para o servidor:
                    if (!causadoOutro) {  //só repassa a mensagem se a alteração não tiver sido causada por uma mensagem recebida de outro cliente
                        Mensagem msgEnviada = new Mensagem();
                        msgEnviada.setComando("i");  //inserção
                        msgEnviada.setAplicar_na_posi(menor);  //posição do cursor
                        msgEnviada.setTexto(documentCaixa.getTextoInserido());  //texto que foi inserido
                        try {
                            output.reset();
                            output.writeObject(msgEnviada);
                            output.flush();
                        } catch (IOException ex) {
                            System.out.println("Erro durante a conexão com o servidor");
                        }
                    } else {
                        causadoOutro = false;
                    }
                }
            };

            Thread t = new Thread(r);
            t.start();
            salvar.doClick();  //salva as alterações automaticamente
        }

        /**
         * Se algo for removido da área de texto, esse algo também é removido do
         * conteúdo do objeto da classe documento.
         *
         * @param de
         */
        @Override
        public void removeUpdate(DocumentEvent de) {  //conteúdo removido do document
            int maior;
            
            if (de.getDocument() instanceof CustomPlainDocument) {
                if (caixaEdicao.getCaret().getDot() < caixaEdicao.getCaret().getMark()) {  //se ocorrer seleção
                    maior = caixaEdicao.getCaret().getMark();
                } else {
                    maior = caixaEdicao.getCaret().getDot();
                }

                cursorDoc.setPosicao_corrente(maior);  //atualiza a posição do cursor no caso de uma seleção seguida de uma deleção

                documentCaixa = (CustomPlainDocument) de.getDocument();
                docEditado.geraAcao("r " + String.valueOf(documentCaixa.getTextoRemovido().length()), cursorDoc, refazer, desfazer);  //remove o que foi removido da área de texto
            }
            else{
                return;
            }
            estaSalvo = false;

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    //envia mensagem com os detalhes da remoção para o servidor:
                    if (!causadoOutro) {  //se a ação não for causada por outro cliente, repassa ela
                        Mensagem msgEnviada = new Mensagem();
                        msgEnviada.setComando("r");  //
                        msgEnviada.setAplicar_na_posi(maior);  //posição do cursor
                        msgEnviada.setNumDel(documentCaixa.getTextoRemovido().length());  //número de caracteres removidos            
                        try {
                            output.reset();
                            output.writeObject(msgEnviada);
                            output.flush();
                        } catch (IOException ex) {
                            System.out.println("Erro durante a conexão com o servidor");
                        }
                    } else {
                        causadoOutro = false;
                    }
                }
            };

            Thread t = new Thread(r);
            t.start();
            salvar.doClick();  //salva as alterações automaticamente
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
        }

    }

    /**
     * Listener que atualiza o cursor do objeto da classe documento, com base na
     * posição do caret da área de texto.
     */
    class cursorListener implements CaretListener {

        @Override
        public void caretUpdate(CaretEvent ce) {
            cursorDoc.setPosicao_corrente(ce.getDot());  //muda a posição corrente do cursor
            cursorDoc.setPosicaoFinal(ce.getMark());  //quando ocorre o ato de selecionar
        }

    }

    /**
     * Listener que realiza a ação de quando o botão de salvar for apertado.
     */
    class salvarListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {

            estaSalvo = true;

            Mensagem msgEnviada = new Mensagem();
            msgEnviada.setComando("s");  //comando para salvar
            try {
                msgEnviada.setDocumentoASalvar(docEditado.getConteudo());
                output.reset();
                output.writeObject(msgEnviada);
                output.flush();
            } catch (IOException ex) {
                System.out.println("Erro durante a conexão com o servidor");
            }

            if (querSair) {  //Se o usuário apertou o botão de fechar, sai da interface 
                JanelaEdicao.this.encerraCliente();
                janelaConfirmacao.dispose();
            }
        }

    }

    /**
     * Listener que realiza a ação de quando o botão de desfazer for apertado.
     */
    class desfazerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            String aInserir;
            int numeroRemover;  //numero de caracteres que devem ser removidos (quando algum tiver que ser removido)

            caixaEdicao.getDocument().removeDocumentListener(edicListener);

            if (!desfazer.isEmpty()) {  //se a pilha não estiver vazia, desfaz
                if (desfazer.getFirst().getComando().charAt(0) == 'i') {  //se for desfazer uma ação de remoção (retrocede na área de texto)
                    aInserir = String.valueOf(desfazer.get(0).getComando().substring(2));
                    caixaEdicao.insert(aInserir, desfazer.getFirst().getAplicar_na_posi().getPosicao_corrente());

                    //envia mensagem com os detalhes da inserção para o servidor:
                    Mensagem msgEnviada = new Mensagem();
                    msgEnviada.setComando("i");  //inserção
                    msgEnviada.setAplicar_na_posi(desfazer.getFirst().getAplicar_na_posi().getPosicao_corrente());  //posição do cursor
                    msgEnviada.setTexto(aInserir);  //texto que foi inserido                   

                    try {
                        output.writeObject(msgEnviada);
                        output.flush();
                    } catch (IOException ex) {
                        System.out.println("Erro durante a conexão com o servidor");
                    }

                } else {
                    numeroRemover = Integer.parseInt(desfazer.getFirst().getComando().substring(2));
                    caixaEdicao.replaceRange("", desfazer.getFirst().getAplicar_na_posi().getPosicao_corrente() - 1 * numeroRemover, desfazer.getFirst().getAplicar_na_posi().getPosicao_corrente());

                    //envia mensagem com os detalhes da remoção para o servidor:
                    Mensagem msgEnviada = new Mensagem();
                    msgEnviada.setComando("r");  //
                    msgEnviada.setAplicar_na_posi(desfazer.getFirst().getAplicar_na_posi().getPosicao_corrente());  //posição do cursor
                    msgEnviada.setNumDel(numeroRemover);  //número de caracteres removidos
                    try {
                        output.writeObject(msgEnviada);
                        output.flush();
                    } catch (IOException ex) {
                        System.out.println("Erro durante a conexão com o servidor");
                    }
                }

                cursorDoc.setPosicao_corrente(desfazer.getFirst().executar_d(docEditado, refazer, desfazer));
                estaSalvo = false;
                salvar.doClick();  //salva as alterações automaticamente
            }

            caixaEdicao.getDocument().addDocumentListener(edicListener);
        }

    }

    /**
     * Listener que realiza a ação de quando o botão de refazer for apertado.
     */
    class refazerListener implements ActionListener {  //mesma lógica do desfazer, muda a pilha usada e usa-se o método executar_r, ao invés de executar_d

        @Override
        public void actionPerformed(ActionEvent ae) {
            String aInserir;
            int numeroRemover;

            caixaEdicao.getDocument().removeDocumentListener(edicListener);
            if (!refazer.isEmpty()) {
                if (refazer.getFirst().getComando().charAt(0) == 'i') {
                    aInserir = String.valueOf(refazer.getFirst().getComando().substring(2));
                    caixaEdicao.insert(aInserir, refazer.get(0).getAplicar_na_posi().getPosicao_corrente());

                    //envia mensagem com os detalhes da inserção para o servidor:
                    Mensagem msgEnviada = new Mensagem();
                    msgEnviada.setComando("i");  //inserção
                    msgEnviada.setAplicar_na_posi(refazer.getFirst().getAplicar_na_posi().getPosicao_corrente());  //posição do cursor
                    msgEnviada.setTexto(aInserir);  //texto que foi inserido                     

                    try {
                        output.writeObject(msgEnviada);
                        output.flush();
                    } catch (IOException ex) {
                        System.out.println("Erro durante a conexão com o servidor");
                    }
                } else {
                    numeroRemover = Integer.parseInt(refazer.getFirst().getComando().substring(2));
                    caixaEdicao.replaceRange("", refazer.get(0).getAplicar_na_posi().getPosicao_corrente() - 1 * numeroRemover, refazer.getFirst().getAplicar_na_posi().getPosicao_corrente());

                    //envia mensagem com os detalhes da remoção para o servidor:
                    Mensagem msgEnviada = new Mensagem();
                    msgEnviada.setComando("r");  //
                    msgEnviada.setAplicar_na_posi(refazer.getFirst().getAplicar_na_posi().getPosicao_corrente());  //posição do cursor
                    msgEnviada.setNumDel(numeroRemover);  //número de caracteres removidos

                    try {
                        output.writeObject(msgEnviada);
                        output.flush();
                    } catch (IOException ex) {
                        System.out.println("Erro durante a conexão com o servidor");
                    }
                }

                cursorDoc.setPosicao_corrente(refazer.getFirst().executar_r(docEditado, refazer, desfazer));
                estaSalvo = false;
                salvar.doClick();  //salva as alterações automaticamente
            }
            caixaEdicao.getDocument().addDocumentListener(edicListener);
        }
    }

    /**
     * Listener que realiza a ação de quando o botão de copiar for apertado.
     */
    class copiarListener implements ActionListener {  //quando o botão de copiar for pressionado

        @Override
        public void actionPerformed(ActionEvent ae) {
            //guardam o início e o fim da seleção:
            int fim = caixaEdicao.getCaret().getMark();
            int inicio = caixaEdicao.getCaret().getDot();

            if (fim != inicio) {  //só prossegue se estiver selecionado
                clipboard.setConteudo(caixaEdicao.getSelectedText());  //agora está na área de transferência
            }
        }

    }

    /**
     * Listener que realiza a ação de quando o botão de colar for apertado.
     */
    class colarListener implements ActionListener {  //quando o botão colar for selecionado

        @Override
        public void actionPerformed(ActionEvent ae) {
            int posiCursor;
            String conteudo;

            if (!clipboard.getConteudo().equals("")) {  //só quando a área de transferência estiver vazia
                caixaEdicao.getDocument().removeDocumentListener(edicListener);

                docEditado.geraAcao("i " + clipboard.getConteudo(), cursorDoc, refazer, desfazer);  //insere na lista
                posiCursor = caixaEdicao.getCaretPosition();
                caixaEdicao.insert(clipboard.getConteudo(), caixaEdicao.getCaretPosition());  //imprime na caixa
                conteudo = clipboard.getConteudo();

                //torna a área de transferência vazia:
                clipboard.setConteudo("");

                caixaEdicao.getDocument().addDocumentListener(edicListener);
                estaSalvo = false;
                salvar.doClick();  //salva as alterações automaticamente

                //envia mensagem com os detalhes da inserção para o servidor:
                Mensagem msgEnviada = new Mensagem();
                msgEnviada.setComando("i");  //inserção
                msgEnviada.setAplicar_na_posi(posiCursor);  //posição do cursor
                msgEnviada.setTexto(conteudo);  //texto que foi inserido              

                try {
                    output.writeObject(msgEnviada);
                    output.flush();
                } catch (IOException ex) {
                    System.out.println("Erro durante a conexão com o servidor");
                }
            }
        }
    }

    /**
     * Listener que realiza a ação de quando o botão de recortar for apertado.
     */
    class recortarListener implements ActionListener {  //quando o botão recortar for selecionado

        @Override
        public void actionPerformed(ActionEvent ae) {
            //guardam o início e o fim da seleção:
            int fim = caixaEdicao.getCaret().getMark();
            int inicio = caixaEdicao.getCaret().getDot();
            int caracteresDeletados;  //registra o número de caracteres deletados no ato de recortar
            int maior;
            int menor;
            String txtRemovido;

            if (fim != inicio) {  //só prossegue se estiver selecionado
                caixaEdicao.getDocument().removeDocumentListener(edicListener);
                if (fim > inicio) {
                    caracteresDeletados = fim - inicio;
                    maior = fim;
                    menor = inicio;
                } else {
                    caracteresDeletados = inicio - fim;
                    maior = inicio;
                    menor = fim;
                }

                txtRemovido = caixaEdicao.getSelectedText();
                clipboard.setConteudo(caixaEdicao.getSelectedText());  //agora está na área de transferência
                cursorDoc.setPosicao_corrente(maior);
                docEditado.geraAcao("r " + String.valueOf(caracteresDeletados), cursorDoc, refazer, desfazer);
                caixaEdicao.replaceSelection("");
                caixaEdicao.setCaretPosition(menor);

                caixaEdicao.getDocument().addDocumentListener(edicListener);
                estaSalvo = false;
                salvar.doClick();  //salva as alterações automaticamente

                //envia mensagem com os detalhes da remoção para o servidor:
                Mensagem msgEnviada = new Mensagem();
                msgEnviada.setComando("r");  //
                msgEnviada.setAplicar_na_posi(maior);  //posição do cursor
                msgEnviada.setNumDel(caracteresDeletados);  //número de caracteres removidos 

                try {
                    output.writeObject(msgEnviada);
                    output.flush();
                } catch (IOException ex) {
                    System.out.println("Erro durante a conexão com o servidor");
                }
            }
        }
    }
}
