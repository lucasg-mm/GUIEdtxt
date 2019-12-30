package utilitarios;

import java.util.*;
import Documento.documento;

/**
 * Representa que devem ser feitas. São armazenadas nas pilhas de desfazer e de
 * refazer.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class acoes {  //classe para guardar ações que foram executadas anteriormente

    /**
     * Comando que corresponde à ação.
     */
    private String comando;  //especificação da ação
    /**
     * Posição do cursor à qual a ação deverá ser aplicada.
     */
    private cursor aplicar_na_posi;

    /**
     * Retorna a posição do cursor de uma ação.
     *
     * @return posuição do cursor.
     */
    public cursor getAplicar_na_posi() {
        return aplicar_na_posi;
    }

    /**
     * Define a posição do cursor de uma ação.
     *
     * @param aplicar_na_posi posição do cursor.
     */
    public void setAplicar_na_posi(cursor aplicar_na_posi) {
        this.aplicar_na_posi = aplicar_na_posi;
    }

    /**
     * Retorna o comando necessário para executar a ação.
     *
     * @return comando.
     */
    public String getComando() {
        return comando;
    }

    /**
     * Define o comando necessário para executar uma dada ação.
     *
     * @param comando comando correspondente à ação.
     */
    public void setComando(String comando) {
        this.comando = comando;
    }

    /**
     * Executa uma ação. A execução difere a depender do fato de ela ser
     * disparada diretamente pelo usuário, ou indiretamente pelas opções de
     * desfazer ou refazer.
     *
     * @param d documento ao qual a ação será aplicada.
     * @param cursor posição do cursor antes da ação ser aplicada .
     * @return o contrário da ação que acabou de ser feita, para viabilizar as
     * opções de fazer e de refazer.
     */
    public acoes execucao(documento d, cursor cursor) throws NumberFormatException, InputMismatchException {
        String espec;
        int quantidade;
        acoes nova_acao = new acoes();

        switch (comando.charAt(0)) {
            case 'i':  //insere caracteres
                if (comando.length() != 1) {
                    espec = comando.substring(2);  //obtem a segunda parte de 'comando'
                } else {  //se digitar só 'i', insere quebra de linha
                    espec = "\n";
                }

                nova_acao.setComando("r " + espec.length());  //armazena o contrário da ação atual                
                d.inserir_chars(espec, cursor);  //insere e move o cursor 
                nova_acao.setAplicar_na_posi(new cursor(cursor.getPosicao_corrente()));

                break;

            case 'r':  //remove um dado número de caracteres
                quantidade = Integer.parseInt(comando.substring(2));  //converte o número de caracteres que o usuário deseja remover para int
                if (quantidade < 0) {
                    throw new InputMismatchException();
                }

                nova_acao.setComando("i " + d.remover_chars(quantidade, cursor));
                nova_acao.setAplicar_na_posi(new cursor(cursor.getPosicao_corrente()));

                break;
        }

        return nova_acao;
    }

    /**
     * Executa uma ação da pilha de desfazer.
     *
     * @param d documento ao qual a ação será aplicada.
     * @param refazer pilha de refazer.
     * @param desfazer pilha de desfazer.
     * @return nova posição do cursor.
     */
    public int executar_d(documento d, LinkedList<acoes> refazer, LinkedList<acoes> desfazer) {  //executa a ação a desfazer (é usada no contexto de pilha)
        cursor cursor;

        cursor = getAplicar_na_posi();
        refazer.push(execucao(d, cursor));
        desfazer.pop();

        return cursor.getPosicao_corrente();
    }

    /**
     * Executa uma ação da pilha de refazer.
     *
     * @param d documento ao qual a ação será aplicada.
     * @param refazer pilha de refazer.
     * @param desfazer pilha de desfazer.
     * @return nova posição do cursor.
     */
    public int executar_r(documento d, LinkedList<acoes> refazer, LinkedList<acoes> desfazer) {  //executaa a ação de refazer (é usada no contexto de pilha)
        cursor cursor;

        cursor = getAplicar_na_posi();
        desfazer.push(execucao(d, cursor));
        refazer.pop();

        return cursor.getPosicao_corrente();
    }

    /**
     * Executa uma ação normal.
     *
     * @param d documento ao qual a ação será aplicada.
     * @param refazer pilha de refazer.
     * @param desfazer pilha de desfazer.
     * @param cursor nova posição do cursor.
     */
    public void executar_n(documento d, LinkedList<acoes> refazer, LinkedList<acoes> desfazer, cursor cursor) {  //executa uma ação normal (que não é disparada por refazer/desfazer)
        desfazer.push(execucao(d, cursor));
        refazer.clear();
    }
}
