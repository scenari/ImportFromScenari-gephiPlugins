/*
 * LICENCE[[
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1/CeCILL 2.O 
 *
 * The contents of this file are subject to the Mozilla Public License Version 
 * 1.1 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/ 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
 * for the specific language governing rights and limitations under the 
 * License. 
 * 
 * The Original Code is kelis.fr code. 
 * 
 * The Initial Developer of the Original Code is 
 * thibaut.arribe@kelis.fr 
 * 
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved. 
 * 
 * Contributor(s): 
 * 
 * 
 * Alternatively, the contents of this file may be used under the terms of 
 * either of the GNU General Public License Version 2 or later (the "GPL"), 
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"), 
 * or the CeCILL Licence Version 2.0 (http://www.cecill.info/licences.en.html), 
 * in which case the provisions of the GPL, the LGPL or the CeCILL are applicable 
 * instead of those above. If you wish to allow use of your version of this file 
 * only under the terms of either the GPL or the LGPL, and not to allow others 
 * to use your version of this file under the terms of the MPL, indicate your 
 * decision by deleting the provisions above and replace them with the notice 
 * and other provisions required by the GPL or the LGPL. If you do not delete 
 * the provisions above, a recipient may use your version of this file under 
 * the terms of any one of the MPL, the GPL, the LGPL or the CeCILL.
 * ]]LICENCE
 */
package eu.scenari.gephi.importer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author tha
 */
public class ScenariImporterPanel2 extends JPanel implements WizardDescriptor.Panel, ScenariImporterPanel{
    private List<ChangeListener> fListeners;
    private ScenariConnector fConnector;
    private boolean isSelected = false;
    
    /**
     * Creates new form ScenariImporterPanel2
     */
    public ScenariImporterPanel2() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fWorkspaceLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        fScrollPane = new javax.swing.JScrollPane();
        fWorkspacesField = new javax.swing.JList();

        setName("Select Workspace"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(fWorkspaceLabel, org.openide.util.NbBundle.getMessage(ScenariImporterPanel2.class, "ScenariImporterPanel2.fWorkspaceLabel.text")); // NOI18N
        fWorkspaceLabel.setName("fWorkspaceLabel"); // NOI18N
        add(fWorkspaceLabel, java.awt.BorderLayout.PAGE_START);

        jPanel1.setMinimumSize(new java.awt.Dimension(400, 350));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 350));

        fScrollPane.setMinimumSize(new java.awt.Dimension(400, 300));
        fScrollPane.setName("fScrollPane"); // NOI18N
        fScrollPane.setPreferredSize(new java.awt.Dimension(400, 300));

        fWorkspacesField.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fWorkspacesField.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fWorkspacesField.setName("fWorkspacesField"); // NOI18N
        fWorkspacesField.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fWorkspacesFieldValueChanged(evt);
            }
        });
        fScrollPane.setViewportView(fWorkspacesField);

        jPanel1.add(fScrollPane);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ScenariImporterPanel2.class, "ScenariImporterPanel2.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void fWorkspacesFieldValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fWorkspacesFieldValueChanged
        if(fWorkspacesField.getSelectedIndex() >= 0){
            fConnector.setWorkspace(fWorkspacesField.getSelectedIndex());
            isSelected = true;
        }
        else isSelected = false;
        fireChangeEvent();
        
    }//GEN-LAST:event_fWorkspacesFieldValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane fScrollPane;
    private javax.swing.JLabel fWorkspaceLabel;
    private javax.swing.JList fWorkspacesField;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables


    @Override
    public Component getComponent() {
        return this;
    }
    

    @Override
    public HelpCtx getHelp() {
       return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(Object data) {
    }

    @Override
    public void storeSettings(Object data) {
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
        if(fListeners == null) fListeners = new ArrayList<ChangeListener>();
        fListeners.add(cl);
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        fListeners.remove(cl);
    }
    
    @Override
    public boolean isValid(){
        return isSelected;
    }

    @Override
    public void setup(ScenariConnector pConnector) {
        fConnector = pConnector;
    }
    
    public void setupList(){
        fWorkspacesField.setListData(fConnector.getWorkspacesLabels());
    }
    
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        it = new HashSet<ChangeListener>(fListeners).iterator();
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
}
