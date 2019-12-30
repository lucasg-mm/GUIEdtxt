package utilitarios;  //Esse pacote contém algumas não corresponde a código do editor em si, mas algumas abstrações necessárias para o seu funcionamento

/**
 * Representa um cursor, ou seja a posição a partir das quais ações serão
 * desempenhadas num texto.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class cursor {  //o cursor da edição que ocorre no momento.

    /**
     * Guarda a posição a partir do começo do texto (o primeiro caractere de um
     * documento corresponde à posição zero). Inserções adicionam caracteres à
     * direita de onde está localizado o cursor.
     */
    private int posicao_corrente;  //localização dele na lista encadeada
    /**
     * Como o "posicao_corrente", mas guarda a posição final de uma seleção. (ou
     * seja, a extremidade direita).
     */
    private int posicaoFinal;  //só é utilizado durante o selecionar

    /**
     * Retorna a posição final de uma seleção.
     *
     * @return posição final de uma seleção.
     */
    public int getPosicaoFinal() {
        return posicaoFinal;
    }

    /**
     * Define a posição final de uma seleção.
     *
     * @param posicaoFinal posição final de uma seleção.
     */
    public void setPosicaoFinal(int posicaoFinal) {
        this.posicaoFinal = posicaoFinal;
    }

    /**
     * Inicializa o cursor no fim do texto. O fim do texto é representado por
     * -1.
     */
    public cursor() {
        this.posicao_corrente = -1;  //para indicar que corresponde à última posicao
    }

    /**
     * Inicializa o cursor numa dada posição.
     *
     * @param inicial posição dada
     */
    public cursor(int inicial) {
        this.posicao_corrente = inicial;
    }

    /**
     * Retorna a posição atual do cursor.
     *
     * @return posição do cursor.
     */
    public int getPosicao_corrente() {
        return posicao_corrente;
    }

    /**
     * Define a posição do cursor.
     *
     * @param posicao_corrente posição do cursor.
     */
    public void setPosicao_corrente(int posicao_corrente) {
        this.posicao_corrente = posicao_corrente;
    }

}
