package Documents;

import GUI.Util.ProgressHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe que representa o documento XML carregado com suas operações de
 * modificação.
 *
 * @author Marcio Tadeu de Oliveira Jr.
 */
public class Document {

    private File document;//o documento em si
    private int id;//id do arquivo
    private static int count = 1;//contador de arquivos

    /**
     * Cria um objeto da classe Document que possui o caminho do arquivo e uma
     * ID.
     *
     * @param document
     */
    Document(File document) {
        this.document = document;
        this.id = Document.count;
        Document.count++;
    }

    /**
     * Retorna o documento associado a classe
     *
     * @return document
     */
    public File getFile() {
        return this.document;
    }

    /**
     * Retorna o nome do arquivo concatenado com sua data de modificação.
     *
     * @return getName()+lastModified()
     */
    public String getCode() {
        Date date = new Date(this.getFile().lastModified());
        return this.getFile().getName().substring(0, this.getFile().getName().lastIndexOf(".")) + " - " + date.toString();
    }

    /**
     * Verifica se dois Documents fazer referencia ao mesmo arquivo
     *
     * @param document
     * @return boolean
     */
    public boolean equals(File document) {
        return this.document.equals(document);
    }

    /**
     * Retorna o identificador que referencia o objeto atual.
     *
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Retorna o caminho absoluto do arquivo referenciado no objeto.
     *
     * @return getAbsolutePath()
     */
    public String getPathWay() {
        return this.document.getAbsolutePath().replace("\\", "/");
    }

    /**
     * Retorna o conteudo do arquivo.
     *
     * @return content
     */
    public String getContent() {
        String content = "";

        if (!ProgressHandler.isInUse()) {
            int i = 0;
            try {
                BufferedReader countBuff = new BufferedReader(new FileReader(this.document));
                for (i = 0; countBuff.ready(); i++) {
                    countBuff.readLine();
                }
            } catch (Exception ex) {
                Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
            }
            ProgressHandler.restart(i, "Reading XML File");
        } else {
            ProgressHandler.setLabel("Reading XML");
        }
        
        boolean reading = ProgressHandler.getLabel().equals("Reading XML File");

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(this.document));
            while (br.ready()) {
                if (reading) {
                    ProgressHandler.increase();
                }
                content += br.readLine();
            }
        } catch (Exception ex) {
            Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (reading) {
                ProgressHandler.stop();
            }
        }
        return XMLFormatter.format(content);
    }

    /**
     * Reinicia o contador de documentos presente na classe para um novo
     * projeto.
     */
    public static void differentProject() {
        Document.count = 1;
    }
}