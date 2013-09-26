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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;

import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;

/**
 *
 * @author tha
 */
public class ScenariImporter implements SpigotImporter, LongTask {
    
    private ContainerLoader fContainer;
    private Report fReport = new Report();
    private ProgressTicket fProgressTicket;
    private ScenariConnector fConnector;
    
    @Override
    public boolean execute(ContainerLoader pContainer) {
        fProgressTicket.start();
        fProgressTicket.switchToIndeterminate();
        Object vJson;
        try {
            vJson = new JsonParser().parseValue(fConnector.sendRequest(ScenariConnector.GET_GRAPH));
            
            if(vJson instanceof HashMap){
                fProgressTicket.switchToDeterminate(((Map<String, List<Object>>) vJson).get("results").size());
                
                //Node Columns
                Map<String, AttributeColumn> vNodeColumns = new HashMap<String, AttributeColumn>();
                vNodeColumns.put("uri", pContainer.getAttributeModel().getNodeTable().addColumn("Uri", AttributeType.STRING));
                vNodeColumns.put("ti", pContainer.getAttributeModel().getNodeTable().addColumn("Title", AttributeType.STRING));
                vNodeColumns.put("status", pContainer.getAttributeModel().getNodeTable().addColumn("Status", AttributeType.INT));
                vNodeColumns.put("sgn", pContainer.getAttributeModel().getNodeTable().addColumn("Signature", AttributeType.STRING));
                vNodeColumns.put("URIlevel1", pContainer.getAttributeModel().getNodeTable().addColumn("Path Level 1", AttributeType.STRING));
                vNodeColumns.put("URIlevel2", pContainer.getAttributeModel().getNodeTable().addColumn("Path Level 2", AttributeType.STRING));
                
                //Link Columns
                Map<String, AttributeColumn> vEdgeColumns = new HashMap<String, AttributeColumn>();
                vEdgeColumns.put("nm", pContainer.getAttributeModel().getEdgeTable().addColumn("Name", AttributeType.STRING));
                
                for(List<Object> vNodeJson : ((Map<String, List<List<Object>>>)vJson).get("results")){
                    fProgressTicket.progress(1);
                    NodeDraft vNode;
                    if(vNodeJson.get(0) != null){
                        if(pContainer.nodeExists((String) vNodeJson.get(0)))
                            vNode = pContainer.getNode((String)vNodeJson.get(0));
                        else{
                            vNode = pContainer.factory().newNodeDraft();
                            vNode.setId((String)vNodeJson.get(0));
                            pContainer.addNode(vNode);
                        }
                        for(String vKey : ((Map<String, String>)vNodeJson.get(1)).keySet()){
                            if(!vKey.equals("attrs")){
                                if(!vNodeColumns.containsKey(vKey))
                                    vNodeColumns.put(vKey, pContainer.getAttributeModel().getNodeTable().addColumn(vKey, AttributeType.STRING));
                                vNode.addAttributeValue(vNodeColumns.get(vKey), ((Map<String, String>)vNodeJson.get(1)).get(vKey));
                                if(vKey.equals("uri")){
                                    String[] vParsedURI = ((Map<String, String>)vNodeJson.get(1)).get(vKey).split("/");
                                    if(vParsedURI.length > 2){
                                        vNode.addAttributeValue(vNodeColumns.get("URIlevel1"), vParsedURI[0]+"/"+vParsedURI[1]);
                                    
                                        if(vParsedURI.length > 3)
                                            vNode.addAttributeValue(vNodeColumns.get("URIlevel2"), vParsedURI[0]+"/"+vParsedURI[1]+"/"+vParsedURI[2]);
                                        else
                                            vNode.addAttributeValue(vNodeColumns.get("URIlevel2"), vParsedURI[0]+"/"+vParsedURI[1]);
                                    }
                                    else{
                                        vNode.addAttributeValue(vNodeColumns.get("URIlevel1"), vParsedURI[0]+"/");
                                        vNode.addAttributeValue(vNodeColumns.get("URIlevel2"), vParsedURI[0]+"/");
                                    }
                                    
                                }
                            }
                            else{
                                for(Map<String, String> vEdgeJson : ((Map<String, List< Map <String, String>>>) vNodeJson.get(1)).get("attrs")){
                                    if(vEdgeJson.get("type").equals("Ref")){
                                        EdgeDraft vEdge = pContainer.factory().newEdgeDraft();
                                        vEdge.setSource(vNode);
                                        
                                        NodeDraft vNode2;
                                        for(String vAttrsKey : vEdgeJson.keySet()){
                                            if(vAttrsKey.equals("value")){
                                                if(pContainer.nodeExists(vEdgeJson.get(vAttrsKey)))
                                                    vNode2 = pContainer.getNode(vEdgeJson.get(vAttrsKey));
                                                else{
                                                    vNode2 = pContainer.factory().newNodeDraft();
                                                    vNode2.setId(vEdgeJson.get(vAttrsKey));
                                                    pContainer.addNode(vNode2);
                                                }
                                                vEdge.setTarget(vNode2);
                                            }
                                            else if(!vAttrsKey.equals("type")){
                                                if(!vEdgeColumns.containsKey(vAttrsKey))
                                                    vEdgeColumns.put(vAttrsKey, pContainer.getAttributeModel().getEdgeTable().addColumn(vAttrsKey, AttributeType.STRING));
                                                vEdge.addAttributeValue(vEdgeColumns.get(vAttrsKey), vEdgeJson.get(vAttrsKey));
                                            }   
                                        }                                        
                                        pContainer.addEdge(vEdge);
                                    }
                                }
                            }
                        }
                    }
                }
                fProgressTicket.finish();
                return true;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            fReport.log(ex.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public ContainerLoader getContainer() {
        return fContainer;
    }

    @Override
    public Report getReport() {
        return fReport;
    }
    
    @Override
    public void setProgressTicket(ProgressTicket pProgressTicket){
        fProgressTicket = pProgressTicket;
    }

    @Override
    public boolean cancel() {
        fReport = null;
        fContainer = null;
        fConnector = null;
        fProgressTicket = null;
        return true;
    }
    
    public void setConnector(ScenariConnector pConnector){
        fConnector = pConnector;
    }
}
