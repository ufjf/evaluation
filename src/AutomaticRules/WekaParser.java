package AutomaticRules;

import GUI.MainInterface.DocumentsTab;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import weka.associations.Apriori;
import weka.associations.AssociationRule;
import weka.associations.Item;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class WekaParser {

    public static void main(DocumentsTab documentsTab){
        gerarRegras(documentsTab);
    }
    
    public static List<Set> gerarRegras(DocumentsTab documentsTab) {
        try {
            ArrayList<String> paths = documentsTab.getDocuments().getPathWays();
            String document1 = paths.get(documentsTab.getLeftCBIndex());
            String document2 = paths.get(documentsTab.getRightCBIndex());
            String separator = System.getProperty("file.separator");
            String workingPath = System.getProperty("user.dir");
            String fileDiff = workingPath+separator+"temp"+separator+"mining_diff.xml";
            String fileArff = workingPath+separator+"temp"+separator+"mining_arff.arff";
            
            //Lendo todas as tags do <emp> baseado na segunda versão
            XMLInputFactory factory = XMLInputFactory.newInstance();
            InputStream is = new FileInputStream(document2);
            XMLStreamReader reader = factory.createXMLStreamReader(is);
            List<String> mapeamentoTags = new ArrayList<String>();

            String root = null;
            String each = null;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (root == null) {
                        root = reader.getLocalName();
                    } else if (each == null) {
                        each = reader.getLocalName();
                    } else if (reader.getLocalName().equalsIgnoreCase(each)) {
                        break;
                    } else {
                        mapeamentoTags.add(reader.getLocalName());
                    }
                }
            }
            is.close();
            
            //Selecionando a tag chave primaria
            String unchangedTag = "empno";
            //Selecionando tags a não serem consideradas
            List<String> removeTags = new ArrayList<String>();
            removeTags.add("deptno");
            removeTags.add("hiredate");
            
            //Removendo tags selecionadas do mapeamento de Tags
            mapeamentoTags.removeAll(removeTags);
            
            //Gerando o Diff
            XDiff diff = new XDiff(document1, document2, fileDiff);
            //Mepeando o Diff em Lists
            List<List> mapeamentoDiff = new ArrayList<List>();
            int i = 0;
            
            is = new FileInputStream(fileDiff);
            reader = factory.createXMLStreamReader(is);

            root = null;
            each = null;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (root == null) {
                        root = reader.getLocalName();
                    } else if (each == null) {
                        each = reader.getLocalName();
                        mapeamentoDiff.add(new ArrayList<String>());
                    } else if (reader.getLocalName().equalsIgnoreCase(each)) {
                        mapeamentoDiff.add(new ArrayList<String>());
                        i++;
                    } else {
                        mapeamentoDiff.get(i).add(reader.getLocalName());
                    }
                }
            }
            is.close();
            
            
            //Gerando ARFF baseado nas tags que você quer
            StringBuilder arff = new StringBuilder();
            arff.append("@relation ").append(root).append('\n');
            for (i = 0; i < mapeamentoTags.size(); i++) {
                    arff.append("@attribute '").append(mapeamentoTags.get(i)).append("' {y}\n");
            }
            arff.append("@data\n");

            for (i = 0; i < mapeamentoDiff.size(); i++) {
                
                if(mapeamentoDiff.get(i).contains(unchangedTag)) //Eliminando DIFF que possua a tag selecionada como chave primária
                    continue;
                
                for (int j = 0; j < mapeamentoTags.size(); j++) {
                    if (j != 0) {
                        arff.append(',');
                    }
                    arff.append(mapeamentoDiff.get(i).contains(mapeamentoTags.get(j)) ? 'y' : '?');
                }
                arff.append('\n');
            }

            System.out.println(arff);        

            //Passar arff para o Weka e tratar a sáida 
            BufferedWriter br = new BufferedWriter(new FileWriter(new File(fileArff)));  
            br.write(arff.toString());  
            br.close(); 

            InputStream isArff = new FileInputStream(fileArff);

            Instances data = ConverterUtils.DataSource.read(isArff);
            data.setClassIndex(data.numAttributes() - 1);

            // build associator
            Apriori apriori = new Apriori();        
            apriori.setClassIndex(data.classIndex());
            apriori.setNumRules(1000);
            apriori.setMinMetric(0);
            apriori.setLowerBoundMinSupport(0.0);
            apriori.buildAssociations(data);
            apriori.getAssociationRules();

            List<Set> listRules = new ArrayList<Set>();        

            for (AssociationRule rule : apriori.getAssociationRules().getRules()) {
                Set<String> currentRule = new HashSet<String>();

                Collection<Item> allTags = rule.getPremise();
                allTags.addAll(rule.getConsequence());
                for (Item tag : allTags) {
                    currentRule.add(tag.getAttribute().name());
                }

                if(!listRules.contains(currentRule)) {
                    listRules.add(currentRule);
                }           
            }

            System.out.println(apriori);

            System.out.println("Saída em lista a ser passada para o XChange");
            System.out.println(listRules);

            return listRules;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}