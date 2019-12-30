package utilitarios;

/**
 * Essa classe armazena texto recortado ou copiado do editor de texto.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class AreaTransferencia {  //armazena texto recortado/copiado

    /**
     * Texto recortado/copiado em string.
     */
    private String conteudo;  //texto recortado/copiado

    /**
     *
     * @return Texto recortado/copiado.
     */
    public String getConteudo() {
        return conteudo;
    }

    /**
     * Define o texto recortado/copiado.
     *
     * @param conteudo
     */
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

}
