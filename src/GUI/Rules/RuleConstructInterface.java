package GUI.Rules;

import GUI.Layout.LayoutConstraints;
import Manager.Manager;
import Rules.RulesModule;
import AutomaticRules.WekaParser;
import GUI.MainInterface.DocumentsTab;
import GUI.MainInterface.InferenceFileChooser;
import Rules.Rule;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import static java.awt.image.ImageObserver.WIDTH;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 * Interface onde o usuário monta as regras para a realização de inferencia
 *
 * @author Guilherme Martins, Marcio Oliveira Junior e Celio H. N. Larcher
 * Junior
 */
public class RuleConstructInterface extends JDialog implements ActionListener {

    //Variáveis utilizadas
    private DocumentsTab documentsTab;
    private RulesModule rulesModule;
    private JButton btnAtributs;
    private static JPanel pnlRules; //onde ficam os campos da regra em construção
    private String[] factsPart;
    private String[] namesFacts;
    public String keyChoice; //chave de contexto
    private String nameFactInRule;
    private String factBase2v1, factBase2v2, factBase1v1, factBase1v2;
    private String factBase2, factBase1, baseRule;
    private final JLabel lblChoiceKey;
    private JButton btnFinishRule, btnCreateNewRule, btnFinishBuilder; //Botões do rodapé
    private JButton btnOpen, btnSave, btnExport; //Botões da barra de ferramentas
    private JTextField nameRule;
    private JComboBox comboOutput;
    private JLabel labelRuleName, labelOutput;
    private boolean terminalOpen;
    //Lista que contém todas as linhas de regra
    ArrayList<LineRule> lineRules;
    //Elementos de tela da mineração de regras
    private JButton btnMineRules; //avança para a etapa de mineração
    private static JPanel pnlGeneratedRules; //exibe as regras geradas a partir da mineração
    private ArrayList<JCheckBox> checkTagsArray = new ArrayList<JCheckBox>();
    private ArrayList<String> chosenTags = new ArrayList<String>();
    List<Set> listRules = new ArrayList<Set>();
    private JComboBox cmbKey; //permite escolher a chave de contexto
    private String results; //regras usadas pelo usuário
    private JPanel pnlBar, pnlBottom, pnlMining, pnlConstructRule, pnlResults, pnlOutput; //paineis principais
    private GridBagLayout gridBag;
    private InferenceFileChooser inferenceFileChooser;
    private ArrayList<JCheckBox> rulesSelect; //permite usuário escolher quais regras usar

    /**
     * Exibe a janela para construção das regras.
     *
     * @param manager Objeto do tipo "Manager" que chamou esta função.
     * @param isSimilarity Booleano que indica se o método escolhido foi
     * "Context Key" ou "Similarity".
     */
    public RuleConstructInterface(Manager manager, boolean isSimilarity, InferenceFileChooser inferenceFileChooser, DocumentsTab documentsTab) {
        this.documentsTab = documentsTab;
        this.inferenceFileChooser = inferenceFileChooser;
        setModal(true);
        setTitle("Key Attribute");
        this.rulesModule = manager.getRulesModule();
        lineRules = new ArrayList<LineRule>();
        LineRule.setLinerules(lineRules);
        terminalOpen = false;
        pnlRules = new JPanel(new FlowLayout());
        lblChoiceKey = new JLabel("Select the Key Attribute:");
        cmbKey = new JComboBox<String>();

        btnAtributs = new JButton("List the attributes");
        btnAtributs.addActionListener(this);
        btnFinishRule = new JButton();
        btnFinishRule.addActionListener(this);
        btnCreateNewRule = new JButton();
        btnCreateNewRule.addActionListener(this);
        btnFinishBuilder = new JButton();
        btnFinishBuilder.addActionListener(this);
        btnMineRules = new JButton();
        btnMineRules.addActionListener(this);

        if (isSimilarity) { //Se o metodo utilizado for o "Similarity"
            factsPart = manager.getSimilarity().get(0).partFacts(manager.getSimilarity().get(0).getFacts());
            namesFacts = manager.getSimilarity().get(0).getNameFacts();
            nameFactInRule = manager.getSimilarity().get(0).getElementName().toUpperCase();

            factBase1v1 = manager.getSimilarity().get(0).getMainFact(factsPart, "before");
            factBase1v2 = manager.getSimilarity().get(0).getMainFact(factsPart, "after");
            keyChoice = "id"; //No modulo "Similarity" a chave de contexto sempre será o atributo ID
        } else { //Se o metodo utilizado for o "Context Key"
            factsPart = manager.getContextKey().get(0).partFacts(manager.getContextKey().get(0).getFacts());
            List<String> listNameFacts = new WekaParser().getTags(this.documentsTab.getDocuments().getPathWays().get(this.documentsTab.getRightCBIndex()));
            nameFactInRule = listNameFacts.get(0).toUpperCase();
            listNameFacts.remove(0);
            namesFacts = listNameFacts.toArray(new String[listNameFacts.size()]);
            factBase1v1 = manager.getContextKey().get(0).getMainFact(factsPart, "before");
            factBase1v2 = manager.getContextKey().get(0).getMainFact(factsPart, "after");

            keyChoice = listNameFacts.get(0);

            cmbKey = new JComboBox(listNameFacts.toArray());
            cmbKey.setSelectedIndex(0);
            cmbKey.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        keyChoice = ((JComboBox) e.getSource()).getSelectedItem().toString();
                        constructRules();
                    }
                }
            });
        }

        LineRule.setNamesFacts(namesFacts);
        factBase1 = factBase1v1 + "," + factBase1v2;

        constructRules();

        setPanelTerminal();

        setVisible(true);
    }

    /**
     * Trata as ações dos botões e das seleções realizadas pelo usuário.
     *
     * @param e Recebe um evento gerado.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAtributs) {
        } else if (e.getSource() == btnFinishRule) {
            if (nameRule.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "It's necessary give the rule a name", "Error", JOptionPane.ERROR_MESSAGE);
                nameRule.requestFocus();
            }
            if (comboOutput.getSelectedItem().equals("")) {
                JOptionPane.showMessageDialog(this, "It's necessary choose output type", "Error", JOptionPane.ERROR_MESSAGE);
                comboOutput.requestFocus();
            } else {
                Iterator iter = lineRules.iterator();
                System.out.println(lineRules.size());
                lineRules = LineRule.getLinerules();
                System.out.println(lineRules.size());
                LineRule.setLinerules(lineRules);
                int validRows = 0;
                while (iter.hasNext()) {
                    LineRule condition = (LineRule) iter.next();
                    if ((!condition.getComboTerm1().getSelectedItem().toString().equals("") && !condition.getComboTerm2().getSelectedItem().toString().equals("")) || (condition.getComboOperator().getSelectedItem().toString().equals("new_element") || (condition.getComboOperator().getSelectedItem().toString().equals("deleted_element")))) {
                        validRows += 1;
                        break;
                    }
                }
                if (validRows == 0) {
                    JOptionPane.showMessageDialog(this, "You must select at least one valid condition!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    String regraConst = "";
                    for (Iterator it = lineRules.iterator(); it.hasNext();) {
                        LineRule condition = (LineRule) it.next();
                        if ((!condition.getComboTerm1().getSelectedItem().toString().equals("") && !condition.getComboTerm2().getSelectedItem().toString().equals("")) || (condition.getComboOperator().getSelectedItem().toString().equals("new_element") || (condition.getComboOperator().getSelectedItem().toString().equals("deleted_element")))) {
                            String aux = buildCondition(comboOutput.getSelectedItem().toString(), condition.getComboTerm1().getSelectedItem().toString(), condition.getComboOperator().getSelectedItem().toString(), condition.getComboTerm2().getSelectedItem().toString());
                            if (regraConst.equals("")) {
                                regraConst = aux;
                            } else {
                                regraConst = regraConst + "," + aux;
                            }
                        }
                    }
                    //Adiciona as regras construídas
                    if (lineRules.get(0).getComboOperator().getSelectedItem().toString().indexOf("_") < 0) {
                        regraConst = nameRule.getText().toLowerCase() + "(" + comboOutput.getSelectedItem().toString().toUpperCase() + "):-" + baseRule + "," + comboOutput.getSelectedItem().toString() + "(" + nameFactInRule + "Before," + comboOutput.getSelectedItem().toString().toUpperCase() + ")," + regraConst + ".";
                        rulesModule.addRules(regraConst);
                    } else {
                        regraConst = nameRule.getText().toLowerCase() + "(" + comboOutput.getSelectedItem().toString().toUpperCase() + "):-" + "" + regraConst + ".";
                        rulesModule.addRules(regraConst);
                    }
                    //dispose();

                    results = formatSetTextPane(rulesModule.getRulesString()); //Formata as regras que serão exibidas na tela

                    if (!results.isEmpty()) {
                        String[] partRules = rulesModule.partRules(results); //Pega o cabeçalho das regras (ex: salary(NAME))
                        identifyRules(rulesModule.getNameAndArgumentsRules(partRules));
                    } else {
                        JOptionPane.showMessageDialog(this, "It's necessary to difine the rules to "
                                + "realize inference of informations.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else if (e.getSource() == btnCreateNewRule) { //Valida as opções de selecionadas na construção da regra e a adiciona ao conjunto de regras
            if (nameRule.getText().isEmpty()) { //Caso falte o "nome" da regra
                JOptionPane.showMessageDialog(this, "It's necessary give the rule a name", "Error", JOptionPane.ERROR_MESSAGE);
                nameRule.requestFocus();
            }
            if (comboOutput.getSelectedItem().equals("")) { //Caso falte a "saída" da regra
                JOptionPane.showMessageDialog(this, "It's necessary choose output type", "Error", JOptionPane.ERROR_MESSAGE);
                comboOutput.requestFocus();
            } else {
                Iterator iter = lineRules.iterator();
                int validRows = 0;
                while (iter.hasNext()) {
                    LineRule condition = (LineRule) iter.next();
                    if ((!condition.getComboTerm1().getSelectedItem().toString().equals("") && !condition.getComboTerm2().getSelectedItem().toString().equals("")) || (condition.getComboOperator().getSelectedItem().toString().equals("new_element") || (condition.getComboOperator().getSelectedItem().toString().equals("deleted_element")))) {
                        validRows += 1;
                        break;
                    }
                }
                if (validRows == 0) {
                    JOptionPane.showMessageDialog(this, "You must select at least one valid condition!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    String regraConst = "";
                    for (Iterator it = lineRules.iterator(); it.hasNext();) {
                        LineRule condition = (LineRule) it.next();
                        if ((!condition.getComboTerm1().getSelectedItem().toString().equals("") && !condition.getComboTerm2().getSelectedItem().toString().equals("")) || (condition.getComboOperator().getSelectedItem().toString().equals("new_element") || (condition.getComboOperator().getSelectedItem().toString().equals("deleted_element")))) {
                            String aux = buildCondition(comboOutput.getSelectedItem().toString(), condition.getComboTerm1().getSelectedItem().toString(), condition.getComboOperator().getSelectedItem().toString(), condition.getComboTerm2().getSelectedItem().toString());
                            if (regraConst.equals("")) {
                                regraConst = aux;
                            } else {
                                regraConst = regraConst + "," + aux;
                            }
                        }
                        condition.setVisible(false);
                    }
                    if (lineRules.get(0).getComboOperator().getSelectedItem().toString().indexOf("_") < 0) {
                        regraConst = nameRule.getText().toLowerCase() + "(" + comboOutput.getSelectedItem().toString().toUpperCase() + "):-" + baseRule + "," + comboOutput.getSelectedItem().toString() + "(" + nameFactInRule + "Before," + comboOutput.getSelectedItem().toString().toUpperCase() + ")," + regraConst + ".";
                        rulesModule.addRules(regraConst);
                    } else {
                        regraConst = nameRule.getText().toLowerCase() + "(" + comboOutput.getSelectedItem().toString().toUpperCase() + "):-" + regraConst + ".";
                        rulesModule.addRules(regraConst);
                    }

                    comboOutput.setSelectedItem("");
                    nameRule.setText("");

                    btnCreateNewRule.setEnabled(true);
                    pnlRules.removeAll();
                    lineRules.clear();
                    LineRule aux = new LineRule();
                    lineRules.add(aux);
                    pnlRules.add(aux);
                    aux.getComboTerm1().requestFocus();
                    pnlRules.revalidate();
                }
            }
        } else if (e.getSource() == btnMineRules) { //Mineração de regras de associação                        
            createListRules(listRules);
        } else if (e.getSource() == btnFinishBuilder) {
            System.out.println(this.getSize());
            if (!results.isEmpty()) {
                ArrayList<String> selectedRules = new ArrayList<String>();
                int cont = 0;
                for (JCheckBox item : rulesSelect) { //Verifica quais regras foram selecionadas pelo usuário
                    if (item.isSelected()) {
                        cont++;
                        selectedRules.add(item.getName());
                    }
                }
                if (cont > 0) {
                    String paneRules = formatGetTextPane(results); //Formata as regras obtidas através do Painel

                    this.rulesModule.addRules(paneRules); //Adiciona as regras do painel
                    this.rulesModule.addSelectRules(selectedRules); //Adiciona as regras selecionadas em sua respectiva variável
                    this.rulesModule.adjustRules(); //Remove as regras repetidas
                    this.inferenceFileChooser.setSelectedRules(selectedRules);//Envia ao InferenceFileChooser a lista de regras selecionadas
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "It's necessary to define the rules to "
                            + "realize inference of informations.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "It's necessary to define the rules to "
                        + "realize inference of informations.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == btnSave) {
            if (!results.isEmpty()) {
                saveRules("/teste.xml");
            } else {
                JOptionPane.showMessageDialog(this, "It's necessary to define the rules to "
                        + "save them.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    private String formatGetTextPane(String paneRules) {
        String textFormated = paneRules;
        textFormated = textFormated.replaceAll("\n", ""); //Eliminando quebras de linha
        textFormated = textFormated.replaceAll("\r", ""); //Eliminando quebras de linha
        textFormated = textFormated.replaceAll("\t", ""); //Eliminando tabulações
        textFormated = textFormated.replaceAll(" ", ""); //Eliminando espaçoes em branco
        textFormated = textFormated.replaceAll("\\.", "\\.\n"); //Acrescenta quebra de linha entre as regras do painel

        return textFormated;
    }

    private void identifyRules(String[] results) {
        rulesSelect = new ArrayList<JCheckBox>();
        JPanel p = new JPanel();

        p.setLayout(new BoxLayout(p, WIDTH));
        pnlResults.setLayout(new BoxLayout(pnlResults, WIDTH));

        pnlResults.removeAll();
        JScrollPane jscPane = new JScrollPane(p);
        pnlResults.add(jscPane);

        GridBagLayout centerGridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1);
        constraints.fill = GridBagConstraints.BOTH;
        centerGridBag.addLayoutComponent(p, constraints);

        p.setVisible(true);
        for (String rule : results) { //Cria os campos do CheckBox de acordo com as regras inseridas pelo usuário
            JCheckBox chkItem = new JCheckBox(rule);
            chkItem.setName(rule);
            rulesSelect.add(chkItem);
            p.add(chkItem);
        }

        pnlResults.updateUI();
    }

    /**
     * Exibe a interface de construção de regras.
     */
    private void setPanelTerminal() {
        JPanel allPane = new JPanel();
        this.setMinimumSize(new Dimension(1100, 600));
        this.setSize(this.getMinimumSize());

        this.setContentPane(allPane);
        if (!terminalOpen) { //Se for a primeira vez que ele é acionado
            nameRule = new JTextField();
            labelRuleName = new JLabel();
            comboOutput = new JComboBox();
            labelOutput = new JLabel();
        }
        terminalOpen = true;

        //declara objetos de controle do layout
        gridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        allPane.setLayout(gridBag);


        //paineis da janela de criação de regras
        pnlBar = new JPanel();
        pnlBar.setLayout(new BoxLayout(pnlBar, BoxLayout.Y_AXIS));
        pnlBar.setBorder(javax.swing.BorderFactory.createTitledBorder("Association Rules:"));

        pnlOutput = new JPanel();
        pnlOutput.setBorder(javax.swing.BorderFactory.createTitledBorder("Output:"));

        pnlConstructRule = new JPanel();
        pnlConstructRule.setBorder(javax.swing.BorderFactory.createTitledBorder("Conditions:"));

        pnlMining = new JPanel();
        pnlMining.setBorder(javax.swing.BorderFactory.createTitledBorder("Association Rules:"));

        pnlResults = new JPanel();
        pnlResults.setBorder(javax.swing.BorderFactory.createTitledBorder("Results"));

        pnlBottom = new JPanel();


        //adiciona os paineis à janela de construção de regras
        LayoutConstraints.setConstraints(constraints, 0, 0, 3, 1, 1000, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        allPane.add(pnlBar, constraints);

        LayoutConstraints.setConstraints(constraints, 1, 1, 1, 1, 1, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        allPane.add(pnlOutput, constraints);

        LayoutConstraints.setConstraints(constraints, 1, 2, 1, 1, 1000, 1000);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        allPane.add(pnlConstructRule, constraints);

        LayoutConstraints.setConstraints(constraints, 0, 3, 3, 1, 1000, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.SOUTH;
        allPane.add(pnlBottom, constraints);

        LayoutConstraints.setConstraints(constraints, 0, 1, 1, 2, 600, 1);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        allPane.add(pnlMining, constraints);

        LayoutConstraints.setConstraints(constraints, 2, 1, 1, 2, 600, 1);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        allPane.add(pnlResults, constraints);


        //Cria a barra de ferramentas
        JToolBar tBar = new JToolBar();

        //Define os icones que serão usados nos botões
        ImageIcon openIcon = new ImageIcon(getClass().getResource("/GUI/icons/open.png"));
        ImageIcon saveIcon = new ImageIcon(getClass().getResource("/GUI/icons/save.png"));

        //Cria os botões e seus eventos        
        btnOpen = new JButton(openIcon);
        btnOpen.setToolTipText("Open Project");
        btnOpen.setEnabled(true);
        btnOpen.addActionListener(this);

        btnSave = new JButton(saveIcon);
        btnSave.setToolTipText("Save Project");
        btnSave.setEnabled(true);
        btnSave.addActionListener(this);

        btnExport = new JButton(openIcon);
        btnExport.setToolTipText("Export to Prolog Facts");
        btnExport.setEnabled(true);
        btnExport.addActionListener(this);

        //Adiciona os botões à barra de ferramentas
        tBar.add(btnOpen);
        tBar.add(btnSave);
        tBar.add(btnExport);
        if (!keyChoice.equals("id")) {
            tBar.add(cmbKey);
        }

        tBar.setAlignmentX(0);

        tBar.setFloatable(false); //Fixa a barra de ferramentas à sua posição

        //indica a posição e layout da barra de ferramentas
        LayoutConstraints.setConstraints(constraints, 0, 0, 3, 1, 1000, 0);
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        gridBag.setConstraints(pnlBar, constraints);

        //Adiciona a barra de ferramentas ao seu painel
        pnlBar.add(tBar);
        tBar.setVisible(true);
        pnlBar.setVisible(true);

        allPane.add(pnlBar);

        //declara objetos de controle do layout do painel do topo
        GridBagLayout gridBagTop = new GridBagLayout();
        pnlOutput.setLayout(gridBagTop);

        JPanel ruleNamePnl = new JPanel();//painel para o label e a area de texto para o nome de regra
        JPanel ruleOutputPnl = new JPanel();//painel para o label e o combobox para a saida da regra

        //layout para os paineis com nome de regra e saida de regra
        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        pnlOutput.add(ruleNamePnl, constraints);

        LayoutConstraints.setConstraints(constraints, 1, 0, 1, 1, 1, 1);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        pnlOutput.add(ruleOutputPnl, constraints);

        //layout para painel com o label e a area de texto para o nome de regra
        GridBagLayout gridBagRuleName = new GridBagLayout();
        ruleNamePnl.setLayout(gridBagRuleName);

        labelRuleName.setText("Rule Name:");
        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1);
        constraints.insets = new Insets(10, 10, 10, 5);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        ruleNamePnl.add(labelRuleName, constraints);

        nameRule.setText("");
        constraints.insets = new Insets(10, 0, 10, 5);
        LayoutConstraints.setConstraints(constraints, 1, 0, 1000, 1, 10, 1);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        ruleNamePnl.add(nameRule, constraints);

        //layout para painel com o label e o combobox para a saida da regra
        GridBagLayout gridBagRuleOutput = new GridBagLayout();
        ruleOutputPnl.setLayout(gridBagRuleOutput);

        labelOutput.setText("Output:");
        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1);
        constraints.insets = new Insets(10, 5, 10, 5);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        ruleOutputPnl.add(labelOutput, constraints);

        comboOutput.setModel(new javax.swing.DefaultComboBoxModel(namesFacts));
        comboOutput.insertItemAt("", 0);
        comboOutput.setSelectedItem("");
        constraints.insets = new Insets(10, 0, 10, 10);
        LayoutConstraints.setConstraints(constraints, 1, 0, 1000, 1, 10, 1);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        ruleOutputPnl.add(comboOutput, constraints);

        //declara objetos de controle do layout do painel central
        GridBagLayout gridBagMid = new GridBagLayout();
        pnlConstructRule.setLayout(gridBagMid);

        pnlRules = new JPanel();

        JScrollPane jsPane = new JScrollPane(pnlRules);
        jsPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        constraints = new GridBagConstraints();
        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        pnlConstructRule.add(jsPane, constraints);

        LineRule firstLineRule = new LineRule();
        pnlRules.setLayout(new BoxLayout(pnlRules, BoxLayout.PAGE_AXIS));
        pnlRules.add(firstLineRule);
        LineRule.setPnlRules(pnlRules);
        lineRules.add(firstLineRule);

        //declara objetos de controle do layout do painel de baixo (botões)
        GridBagLayout gridBagBottom = new GridBagLayout();
        pnlBottom.setLayout(gridBagBottom);
        constraints = new GridBagConstraints();

        btnFinishRule.setText("Save rule");
        btnFinishRule.setSize(new Dimension(350, 25));
        btnFinishRule.setMinimumSize(btnFinishRule.getSize());
        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1);
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;
        pnlBottom.add(btnFinishRule, constraints);

        btnCreateNewRule.setText("Create new rule");
        btnCreateNewRule.setSize(btnFinishRule.getSize());
        btnCreateNewRule.setMinimumSize(btnFinishRule.getSize());
        LayoutConstraints.setConstraints(constraints, 1, 0, 1, 1, 1, 1);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        pnlBottom.add(btnCreateNewRule, constraints);

        btnFinishBuilder.setText("Show the results");
        btnFinishBuilder.setSize(btnFinishRule.getSize());
        btnFinishBuilder.setMinimumSize(btnFinishRule.getSize());
        LayoutConstraints.setConstraints(constraints, 2, 0, 1, 1, 1, 1);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        pnlBottom.add(btnFinishBuilder, constraints);

        //declara objetos de controle do layout do painel da esquerda (regras de associação)        
        GridBagLayout gridBagLeft = new GridBagLayout();
        pnlMining.setLayout(gridBagLeft);

        JPanel pnBtnMining = new JPanel();
        pnBtnMining.add(btnMineRules);
        btnMineRules.setText("Mine Rules");

        //
        chosenTags = new ArrayList<String>();
        checkTagsArray = new ArrayList<JCheckBox>();

        JPanel pnlTop = new JPanel();
        JPanel pnlCenter = new JPanel();
        JPanel pnlBotton = new JPanel();

        JLabel btnOpenRules = new JLabel("Select the tags you want to mine:");
        btnOpenRules.setPreferredSize(new Dimension(300, 30));
        btnOpenRules.revalidate();
        pnlTop.add(btnOpenRules);

        //Painel central
        GridBagLayout centerGridBag = new GridBagLayout();
        pnlCenter.setLayout(centerGridBag);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, WIDTH));
        p.setVisible(true);
        pnlCenter.setLayout(new BoxLayout(pnlCenter, WIDTH));

        pnlCenter.removeAll();
        JScrollPane jscPane = new JScrollPane(p);
        pnlCenter.add(jscPane);

        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1);
        constraints.fill = GridBagConstraints.BOTH;
        centerGridBag.addLayoutComponent(p, constraints);

        //Recupera a lista de tags a partir do segundo arquivo carregado no projeto
        List<String> tags = new WekaParser().getTags(documentsTab.getDocuments().getPathWays().get(documentsTab.getRightCBIndex()));
        for (String tag : tags) { //Cria os campos do CheckBox de acordo com as regras inseridas pelo usuário
            if (tags.get(0).equals(tag)) {
                continue;
            }
            JCheckBox chkItem = new JCheckBox(tag);
            chkItem.setName(tag);
            chkItem.setSelected(true);
            checkTagsArray.add(chkItem);
            p.add(chkItem);
        }

        //Painel inferior
        JButton btnDone = new JButton("Mine Rules");
        btnDone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox item : checkTagsArray) {
                    if (item.isSelected()) {
                        chosenTags.add(item.getName());
                    }
                }
                listRules = new WekaParser().generateRules(documentsTab, chosenTags, keyChoice);
                createListRules(listRules);
            }
        });

        btnDone.setVisible(true);
        btnDone.setPreferredSize(new Dimension(110, 25));
        pnlBotton.add(btnDone);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        LayoutConstraints.setConstraints(constraints, 0, 2, 1, 1, 1, 1);
        constraints.anchor = GridBagConstraints.SOUTH;
        pnlMining.add(pnlBotton, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        LayoutConstraints.setConstraints(constraints, 0, 1, 1, 1, 1, 1);
        constraints.anchor = GridBagConstraints.SOUTH;
        pnlMining.add(pnlCenter, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1);
        constraints.anchor = GridBagConstraints.SOUTH;
        pnlMining.add(pnlTop, constraints);

        results = formatSetTextPane(rulesModule.getRulesString()); //Formata as regras que serão exibidas na tela

        setVisible(true);
    }

    private String formatSetTextPane(String paneRules) {
        String textFormated = paneRules;
        textFormated = textFormated.replaceAll("\n", ""); //Eliminando quebras de linha
        textFormated = textFormated.replaceAll("\r", ""); //Eliminando quebras de linha
        textFormated = textFormated.replaceAll("\t", ""); //Eliminando tabulações
        textFormated = textFormated.replaceAll(" ", ""); //Eliminando espaçoes em branco
        textFormated = textFormated.replaceAll("\\.", "\\.\n\n"); //Acrescenta quebra de linha entre as regras do painel
        textFormated = textFormated.replaceAll("(\\:-|\\:-\n)", "\\:-\n    "); //Acrescenta quebra de linha e espaços depois de ":-"
        textFormated = textFormated.replaceAll("(\\),|\\),\n)", "\\),\n    "); //Acrescenta quebra de linha e espaços depois de "),"

        return textFormated;
    }

    /**
     * Função para criar cada condição existente na regra composta.
     *
     * @param exit O que se deseja como saída da regra.
     * @param term1 Primeiro termo a ser utilizado.
     * @param operator Operador a ser aplicado.
     * @param term2 Segundo termo a ser utilizado.
     * @return newRule Nova regra construída.
     */
    private String buildCondition(String exit, String term1, String operator, String term2) {
        String newRule;
        String ruleAux;
        String term1After;
        String term2After;
        String arg1term1;
        String arg1term2;

        ruleAux = "";
        newRule = "";

        if (operator.equals("new_element")) {
            /*
             ex: exists_after(NOME):-	funcionario(after,Fa),	nome(Fa,NOME).
             existeAfter = "exists_after("+saida.toUpperCase()+"):-"+fatoBase1v2+","+saida+"(Fa,"+saida.toUpperCase()+").";
             ex: exists_before(NOME):-	funcionario(before,Fb),	nome(Fb,NOME).
             existeBefore = "exists_before("+saida.toUpperCase()+"):-"+fatoBase1v1+","+saida+"(Fb,"+saida.toUpperCase()+").";
             new_element(X):- funcionario(after,Fa),nome(Fa,NOME),not((funcionario(before,Fb),nome(Fb,NOME))).
             */
            newRule = factBase1v2 + "," + keyChoice + "(" + nameFactInRule + "After," + keyChoice.toUpperCase() + ")," + exit + "(" + nameFactInRule + "After," + exit.toUpperCase() + ")," + "not((" + factBase1v1 + "," + keyChoice + "(" + nameFactInRule + "Before," + keyChoice.toUpperCase() + ")))";
        } else if (operator.equals("deleted_element")) {
            /*
             ex: exists_after(NOME):-	funcionario(after,Fa),	nome(Fa,NOME).
             existeAfter = "exists_after("+saida.toUpperCase()+"):-"+fatoBase1v2+","+saida+"(Fa,"+saida.toUpperCase()+").";
             ex: exists_before(NOME):-	funcionario(before,Fb),	nome(Fb,NOME).
             existeBefore = "exists_before("+saida.toUpperCase()+"):-"+fatoBase1v1+","+saida+"(Fb,"+saida.toUpperCase()+").";
             element_deleted(X):-funcionario(before,Fb),nome(Fb,NOME),not((funcionario(after,Fa),nome(Fa,NOME))).
             */
            newRule = factBase1v1 + "," + keyChoice + "(" + nameFactInRule + "Before," + keyChoice.toUpperCase() + ")," + exit + "(" + nameFactInRule + "Before," + exit.toUpperCase() + ")," + "not((" + factBase1v2 + "," + keyChoice + "(" + nameFactInRule + "After," + keyChoice.toUpperCase() + ")))";
        } else {
            String[] term1part = term1.split("\\-");
            //Indice 0: nome do fato que compõe o termo
            //Indice 1: v. Before OU v. After
            String[] term2part = term2.split("\\-");

            //pegando o "Before" ou "After"
            term1part[1] = term1part[1].substring(term1part[1].lastIndexOf(".") + 2);
            term2part[1] = term2part[1].substring(term2part[1].lastIndexOf(".") + 2);

            if (term1part[1].equals("Before")) {
                arg1term1 = nameFactInRule + "Before";
            } else {
                arg1term1 = nameFactInRule + "After";
            }

            if (term2part[1].equals("Before")) {
                arg1term2 = nameFactInRule + "Before";
            } else {
                arg1term2 = nameFactInRule + "After";
            }

            term1part[0] = term1part[0].replaceAll(" ", "");
            term1part[1] = term1part[1].replaceAll(" ", "");
            term2part[0] = term2part[0].replaceAll(" ", "");
            term2part[1] = term2part[1].replaceAll(" ", "");

            //salario(Fb,SALARIOBefore)
            term1After = term1part[0] + "(" + arg1term1 + "," + term1part[0].toUpperCase() + term1part[1] + ")";
            term2After = term2part[0] + "(" + arg1term2 + "," + term1part[0].toUpperCase() + term2part[1] + ")";

            if (operator.equals("and")) {
                //Nao faz nada
                ruleAux = "";
            } else if (operator.equals(">")) {
                //Adiciona uma regra do tipo SalarioB>SalarioM
                ruleAux = term1part[0].toUpperCase() + term1part[1] + ">" + term1part[0].toUpperCase() + term2part[1];
            } else if (operator.equals("<")) {
                //Adiciona uma regra do tipo SalarioB<SalarioM
                ruleAux = term1part[0].toUpperCase() + term1part[1] + "<" + term1part[0].toUpperCase() + term2part[1];
            } else if (operator.equals("=")) {
                ruleAux = term1part[0].toUpperCase() + term1part[1] + "==" + term1part[0].toUpperCase() + term2part[1];
            } else if (operator.equals("!=")) {
                ruleAux = term1part[0].toUpperCase() + term1part[1] + "\\=" + term1part[0].toUpperCase() + term2part[1];
            }
            newRule = term1After + "," + term2After + "," + ruleAux;
            return newRule;
        }//Fecha else do teste dos operadores new_element ou element_deleted
        return newRule;
    }

    /**
     * Retorna o nome de um único fato regra.
     *
     * @param fact String contendo um fato completo.
     * @return nameFact String contendo o nome de um fato.
     */
    public String getNameRule(String fact) {
        String nameFact = "";

        int indexParenthesis = fact.indexOf("(");
        nameFact = fact.substring(0, indexParenthesis);

        return nameFact;
    }

    /**
     * Retorna os argumentos de uma regra passada para a função
     *
     * @param rule A regra completa, com o nome e os argumentos.
     * @return Um argumento da regra recebida é representado em cada índice do
     * vetor.
     */
    public String[] getArgumentsRule(String rule) {
        int idxOpenParenthesis = rule.indexOf("(");
        int idxCloseParenthesis = rule.indexOf(")");
        String[] argumentos = rule.substring(idxOpenParenthesis + 1, idxCloseParenthesis).split(",");
        return argumentos;
    }

    /**
     * Método que cria a estrutura das regras a partir da chave escolhida
     */
    private void constructRules() {
        setTitle("Rule Builder");
        setPreferredSize(new Dimension(660, 330));
        setLocation(250, 100);
        pack();
        factBase2v1 = keyChoice + "(" + nameFactInRule + "Before," + keyChoice.toUpperCase() + ")";
        factBase2v2 = keyChoice + "(" + nameFactInRule + "After," + keyChoice.toUpperCase() + ")";
        factBase2 = factBase2v1 + "," + factBase2v2;

        baseRule = factBase1 + "," + factBase2;

        lblChoiceKey.setVisible(true);
    }

    private void createListRules(List<Set> listRules) {
        pnlMining.removeAll();

        pnlGeneratedRules = new JPanel();
        JScrollPane jsPaneWest = new JScrollPane(pnlGeneratedRules);
        jsPaneWest.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsPaneWest.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        GridBagConstraints constraints = new GridBagConstraints();
        LayoutConstraints.setConstraints(constraints, 0, 0, 1, 1, 1, 1000);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        pnlMining.add(jsPaneWest, constraints);

        pnlGeneratedRules.setLayout(new BoxLayout(pnlGeneratedRules, BoxLayout.PAGE_AXIS));
        pnlGeneratedRules.setAutoscrolls(true);
        pnlGeneratedRules.removeAll();

        int i = 1;

        for (final Set<String> rules : listRules) {
            JPanel painel = new JPanel();
            FlowLayout flow = new FlowLayout();
            flow.setAlignment(FlowLayout.LEFT);
            painel.setLayout(flow);
            painel.setAlignmentX(LEFT_ALIGNMENT);

            JLabel label = new JLabel("label" + i);
            label.setText(rules.toString().replace("[", "").replace("]", ""));

            final JButton button = new JButton("+");
            button.setSize(15, 15);
            button.setMaximumSize(new Dimension(15, 15));
            button.setMinimumSize(new Dimension(15, 15));
            button.setPreferredSize(new Dimension(15, 15));
            button.setFont(new Font("verdana", 1, 8));
            button.setHorizontalTextPosition(SwingConstants.LEFT);
            button.setVerticalTextPosition(SwingConstants.TOP);
            button.setMargin(new Insets(1, 1, 1, 1));
            button.setName("button" + 1);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pnlRules.removeAll();
                    pnlRules.updateUI();
                    lineRules.removeAll(lineRules);
                    pnlRules.revalidate();
                    pnlRules.updateUI();

                    for (String rule : rules) {
                        LineRule aux = new LineRule();
                        pnlRules.add(aux);

                        aux.getComboOperator().setSelectedItem("!=");
                        aux.getComboOperator().setEnabled(false);

                        aux.getComboTerm1().setSelectedItem(rule + " - v. Before");
                        aux.getComboTerm1().setEnabled(false);

                        aux.getComboTerm2().setSelectedItem(rule + " - v. After");
                        aux.getComboTerm2().setEnabled(false);

                        aux.getBtnAddCondition().setEnabled(false);
                        aux.getComboTerm1().requestFocus();

                        lineRules.add(aux);
                        LineRule.setLinerules(lineRules);
                        pnlRules.revalidate();
                    }

                    LineRule aux = new LineRule();
                    lineRules.add(aux);
                    pnlRules.add(aux);
                    aux.getComboTerm1().requestFocus();
                    LineRule.setPnlRules(pnlRules);
                    pnlRules.revalidate();
                }
            });

            painel.add(button);
            painel.add(label);

            pnlGeneratedRules.add(painel);
            pnlGeneratedRules.updateUI();
            i++;
        }
        pnlGeneratedRules.setAlignmentX(LEFT_ALIGNMENT);

        pnlMining.revalidate();
    }

    private void saveRules(String xml) {
        Document dom;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.newDocument();

            // cria o elemento raiz
            Element rootEle = dom.createElement("project");
            rootEle.setAttribute("context-key", keyChoice.toLowerCase());

            // cria o elemento rule
            /*for (Rule rule : rulesModule.getRules()) {

                Element ruleEle = dom.createElement("rule");
                ruleEle.setAttribute("output", xml);
                ruleEle.setAttribute("enabled", xml);
                ruleEle.setAttribute("name", rule.getRule());

                Element fieldEle = dom.createElement("field");
                fieldEle.setAttribute("change", xml);
                fieldEle.appendChild(dom.createTextNode("Tag teste"));

                ruleEle.appendChild(fieldEle);
                rootEle.appendChild(ruleEle);
            }*/

            ArrayList<Rule> rules = rulesModule.getRules();
            for (int i = 0; i < rules.size(); i++) {
                Rule rule = rules.get(i);
                String[] aux = rule.getRule().split(":");

                Element ruleEle = dom.createElement("rule");
                ruleEle.setAttribute("output", rule.getRule().substring(rule.getRule().indexOf("(") + 1, rule.getRule().indexOf(")")).toLowerCase());
                ruleEle.setAttribute("enabled", rulesSelect.get(i).isSelected() ? "true" : "false");
                ruleEle.setAttribute("name", aux[0].substring(0, aux[0].indexOf("(")));

                for (String s : aux[1].split(",")) {
                    String operator = "";
                    String change = "";
                    if (s.contains("\\=")) {
                        operator = "\\=";
                        change = "difference";
                    } else if (s.contains("<")) {
                        operator = "<";
                        change = "increase";
                    } else if (s.contains(">")) {
                        operator = ">";
                        change = "decrease";
                    } else if (s.contains("==")) {
                        operator = "==";
                        change = "nothing";
                    }

                    if (operator != "") {
                        s = s.replace("After", "");
                        String s2[] = s.split(operator);
                        s2[1] = s2[1].replace(".", "");

                        Element fieldEle = dom.createElement("field");
                        fieldEle.setAttribute("change", change);
                        fieldEle.appendChild(dom.createTextNode(s2[1].toLowerCase()));

                        ruleEle.appendChild(fieldEle);
                    }
                }

                rootEle.appendChild(ruleEle);
            }

            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(xml)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }
}
