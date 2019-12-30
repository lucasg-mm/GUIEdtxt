package InterfaceGrafica;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Classe que extende de PlainDocument. Ela foi implentada para que fosse
 * possível recuperar a s strings inseridas e removidas da área de texto do
 * editor.
 *
 * @author Lucas Gabriel Mendes Miranda
 * @author Engenharia de Computação
 * @author 10265892
 */
public class CustomPlainDocument extends PlainDocument {  //essa é uma classe customizada de PlainDocument. Ela existe apenas para faciliar a extração de texto removido/inserido do Document atrelado a JTextArea

    /**
     * Texto inserido na área de texto do editor.
     */
    private String textoInserido;

    /**
     * Texto removido da área de texto do editor.
     */
    private String textoRemovido;

    public CustomPlainDocument() {
        super();
    }

    public String getTextoInserido() {
        return textoInserido;
    }

    public String getTextoRemovido() {
        return textoRemovido;
    }

    /**
     * Mesmo método da classe pai, mas define o textoInserido antes.
     *
     * @param offset
     * @param string
     * @param as
     * @throws BadLocationException
     */
    @Override
    public void insertString(int offset, String string, AttributeSet as) throws BadLocationException {
        textoInserido = string;
        super.insertString(offset, string, as);
    }

    /**
     * Mesmo método da classe pai, mas define o textoRemovido antes.
     *
     * @param offset
     * @param i1
     * @throws BadLocationException
     */
    @Override
    public void remove(int offset, int i1) throws BadLocationException {
        textoRemovido = getText(offset, i1);
        super.remove(offset, i1);
    }
}
