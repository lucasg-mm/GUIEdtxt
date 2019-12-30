package Documento;

import java.io.*;
import java.util.*;
import utilitarios.acoes;
import utilitarios.cursor;

/**
 * Representa um documento aberto para a edição do cliente.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class documento {

    /**
     * Uma lista encadeada que representa o conteúdo do arquivo. Cada nó é um
     * char do texto.
     */
    private LinkedList<Character> conteudo;  //essa lista encadeada de caracteres contem o conteudo do arquivo sendo atualmente editado.

    /**
     * Contém o nome do arquivo.
     */
    private String nome;  //nome do arquivo.

    /**
     * Descritor do arquivo de texto.
     */
    private File descritor;  //descritor de arquivo associado ao documento

    /**
     * Retorna o nome do documento.
     *
     * @return nome do documento.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do documento.
     *
     * @param nome nome que se deseja dar ao documento.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Retorna o conteudo do documento.
     *
     * @return lista que representa o conteúdo do documento.
     */
    public LinkedList<Character> getConteudo() {
        return conteudo;
    }

    /**
     * Define o conteúdo do documento.
     *
     * @param conteudo lista que representa o conteúdo do documento.
     */
    public void setConteudo(LinkedList<Character> conteudo) {
        this.conteudo = conteudo;
    }

    /**
     * Insere um caracteres no documento e move o cursor de acordo com essa
     * inserção.
     *
     * @param chars o conteúdo que se quer adicionar ao arquivo.
     * @param cursor_atual a posição a partir da qual o conteúdo será
     * adicionado.
     */
    public void inserir_chars(String chars, cursor cursor_atual) {  //insere um ou mais caracteres na lista conteudo (de acordo com a posicao atual do cursor)       
        if (cursor_atual.getPosicao_corrente() < 0) {  //insere no final
            for (int i = 0; i < chars.length(); i++) {
                conteudo.add(chars.charAt(i));
            }
        } else {  //insere em qualquer outro lugar especificado 
            for (int i = 0; i < chars.length(); i++) {
                conteudo.add(cursor_atual.getPosicao_corrente(), chars.charAt(i));
                cursor_atual.setPosicao_corrente(cursor_atual.getPosicao_corrente() + 1);
            }
        }
    }

    /**
     * Imprime o conteúdo do arquivo numa string.
     *
     * @return String com o conteúdo do arquivo
     */
    public String imprime() {  //imprime o conteúdo do arquivo numa string
        String conteudoStr = "";
        ListIterator iterador = conteudo.listIterator();

        while (iterador.hasNext()) {
            conteudoStr = conteudoStr + iterador.next();
        }

        return conteudoStr;
    }

    /**
     * Remove um dado número de caracteres do documento.
     *
     * @param removidos o número de caracteres que o usuário deseja remover do
     * texto.
     * @param cursor_atual a posição a partir da qual os caracteres serão
     * removidos.
     * @return a frase que foi removida.
     */
    public String remover_chars(int removidos, cursor cursor_atual) {  //remove um certo numero de caracteres da lista "conteudo", de acordo com o cursor
        String frase_removida = "";  //guarda os caracteres removidos

        if (cursor_atual.getPosicao_corrente() < 0) {  //remove a partir do final
            for (int i = 1; i <= removidos; i++) {
                frase_removida = conteudo.getLast() + frase_removida;
                conteudo.removeLast();

                if (conteudo.isEmpty()) {
                    break;  //para evitar que continue removendo, mesmo que já não tenha mais chars.
                }
            }
        } else {  //remove a partir de qualquer outro lugar especificado 
            for (int i = 1; i <= removidos; i++) {
                frase_removida = conteudo.get(cursor_atual.getPosicao_corrente() - 1) + frase_removida;
                conteudo.remove(cursor_atual.getPosicao_corrente() - 1);  //##CUIDADO COM EXCEÇÃO!!
                cursor_atual.setPosicao_corrente(cursor_atual.getPosicao_corrente() - 1);

                if (cursor_atual.getPosicao_corrente() == 0) {
                    break;  //para evitar que continue removendo, mesmo que já não tenha mais chars.
                }
            }
        }

        return frase_removida;
    }

    /**
     * Gera uma ação no editor de texto e a executa.
     *
     * @param comando Comando que se refere à ação.
     * @param cursor_atual Cursor atual do texto
     * @param refazer Pilha de ações que podem ser refeitas.
     * @param desfazer Pilha de ações que podem ser desfeitas.
     */
    public void geraAcao(String comando, cursor cursor_atual, LinkedList<acoes> refazer, LinkedList<acoes> desfazer) {  //cria uma ação (para colocar nas pilhas) e a executa
        acoes acao_normal = new acoes();
        acao_normal.setComando(comando);
        try {
            acao_normal.executar_n(this, refazer, desfazer, cursor_atual);
        } catch (NumberFormatException e) {
            System.out.println("##Insira o numero de caracteres que deseja remover apos o r!\n");
        } catch (InputMismatchException b) {
            System.out.println("##Insira um numero positivo!\n");
        }
    }

}
