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

import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.SpigotImporter;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author tha
 */
@ServiceProvider(service = ImporterWizardUI.class)
public class ScenariImporterWizardUI implements ImporterWizardUI{
    private Panel[] fPanels = null;
    private ScenariConnector fConnector = new ScenariConnector();

    @Override
    public String getDisplayName() {
        return "Scenari Importer";
    }

    @Override
    public String getCategory() {
        return "Structured document";
    }

    @Override
    public String getDescription() {
        return "Import documents data from Scenari";
    }


    @Override
    public boolean isUIForImporter(Importer pImporter) {
        return pImporter instanceof ScenariImporter;
    }

    @Override
    public org.openide.WizardDescriptor.Panel[] getPanels() {
        if(fPanels==null) {
            fPanels = new Panel[2];
            fPanels[0] = new ScenariImporterPanel1();
            fPanels[1] = new ScenariImporterPanel2();
        }
        ((ScenariImporterPanel1)fPanels[0]).setNextPanel((ScenariImporterPanel2) fPanels[1]);
        return fPanels;
    }

    @Override
    public void setup(org.openide.WizardDescriptor.Panel pPanel) {
        ((ScenariImporterPanel)pPanel).setup(fConnector);
    }

    @Override
    public void unsetup(SpigotImporter pSI, org.openide.WizardDescriptor.Panel pPanel) {
        ((ScenariImporter)pSI).setConnector(fConnector);
    }
}
