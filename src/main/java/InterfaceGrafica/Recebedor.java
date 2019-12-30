package InterfaceGrafica;

import Mensagem.Mensagem;
import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

/**
 * Fica esperando receber uma mensagem. Ao receber, realiza a ação que essa
 * mensagem especifica.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author 10265892
 * @author Engenharia de Computação
 */
public class Recebedor implements Runnable {
    /**
     * Janela de edição, onde as ações são realizadas.
     */
    private JanelaEdicao janela;
    /**
     * Fluxo de entrada.
     */
    private ObjectInputStream input;
    /**
     * Mensagem recebida do servidor.
     */
    private Mensagem msgRecebida;
    /**
     * Área onde se encontra o texto sendo editado.
     */
    private JTextArea areaEdicao;

    public Recebedor(JTextArea areaEdicao, ObjectInputStream input, JanelaEdicao janela) throws UnknownHostException, IOException {
        this.areaEdicao = areaEdicao;
        this.input = input;
        this.janela = janela;
    }

    /**
     * Realiza a ação especificada pela mensagem.
     */
    @SuppressWarnings("Convert2Lambda")
    private void fazAcao() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    janela.setCausadoOutro(true);
                    switch (msgRecebida.getComando().charAt(0)) {
                        case 'i':  //inserção
                            //insere a string que deve ser inserida na área de texto:
                            areaEdicao.insert(msgRecebida.getTexto(), msgRecebida.getAplicar_na_posi());

                            break;

                        case 'r':  //remoção
                            //faz a remoção na área de texto:
                            areaEdicao.replaceRange("", msgRecebida.getAplicar_na_posi() - msgRecebida.getNumDel(), msgRecebida.getAplicar_na_posi());

                            break;

                        case 'd':  //desconectar todos
                            janela.encerraCliente();

                            break;

                        case 'm':  //um cliente se desconectou do documento
                            janela.numClientes--;
                            janela.mostraConectados.setText("Conectados: " + janela.numClientes);
                            janela.setCausadoOutro(false);

                            break;

                        case 'p':  //um cliente se conectou ao documento
                            janela.numClientes++;
                            janela.mostraConectados.setText("Conectados: " + janela.numClientes);
                            janela.setCausadoOutro(false);

                            break;
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            System.out.println("Erro de sincronia.");
        }
    }
    
    /**
     * Thread em que acontece a espera por novas mensagens.
     */
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                //fica em listening:
                msgRecebida = (Mensagem) input.readObject();
                fazAcao();
            } catch (IOException ex) {
                try {
                    input.close();
                } catch (IOException ex1) {
                    System.out.println("Erro durante a desconexão.");
                }
                return;
            } catch (ClassNotFoundException ex) {
                System.out.println("Erro durante a troca de mensagens entre cliente e servidor.");
            }
        }
    }
}
