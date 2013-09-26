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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.gephi.io.importer.api.ImportUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author tha
 */
public class ScenariConnector {
    public static final int GET_WSP_LIST = 0;
    public static final int GET_GRAPH = 1;
    
    private String fUrl = null;
    private String fLogin = null;
    private String fPassword = null;
    private String fEnvironnement = null;
    private boolean fOdbBackend ;
    
    private List<String> fWorkspacesCodes;
    private List<String> fWorkspacesLabels;
    
    protected String fWorkspace = null;
    
    protected String sendRequest(int pRequest){
        HttpURLConnection vConnection = null;
        String vResult = null;
        try{
            URL vUrl;
            switch(pRequest){
                case GET_WSP_LIST :
                    vUrl = new URL(fUrl+"/s/"+fEnvironnement+"/u/adminWsp?cdaction=List&withWspTypes=true");

                break;

                case GET_GRAPH :
                    String vStrUrl = fUrl+"/s/"+fEnvironnement+"/u/search/?param="+fWorkspace+"&request=<request><select><column%20dataKey=\"";
                    if(fOdbBackend) vStrUrl+= "srcId";
                    else vStrUrl+="srcUri";
                    vStrUrl+="\"/><column%20dataKey=\"itItemObject\"/></select><where><exp%20type=\"ItemSrcType\"%20srcTypes=\"12\"/><exp%20type=\"RootFolder\"%20path=\"\"/></where></request>";
                    vUrl = new URL(vStrUrl);
                break;

                default: vUrl = new URL(null);
                break;
            }
            vConnection = (HttpURLConnection) vUrl.openConnection();
            vConnection.setRequestMethod("GET");
            if(fLogin != null){
                Base64 vEncoder = new Base64();
                String vAuth = fLogin + ":" + fPassword;
                String vEncodedAuth = "Basic "+vEncoder.encodeAsString(vAuth.getBytes() );
                System.out.println(vEncodedAuth);
                vConnection.setRequestProperty("Authorization", vEncodedAuth);
            }

            vConnection.setDoInput(true);


            //Get Response	
            InputStream vInput = vConnection.getInputStream();
            BufferedReader vReader = new BufferedReader(new InputStreamReader(vInput));
            String vLine;
            StringBuilder vResponse = new StringBuilder(); 
            while((vLine = vReader.readLine()) != null) {
              vResponse.append(vLine);
            }
            vReader.close();

            vResult =  vResponse.toString(); 
        }
        catch(Exception ex){
            //Exceptions.printStackTrace(ex);
        }
        finally{
            if(vConnection != null) vConnection.disconnect();
        }
        return vResult;     
    }
    
    public void setEnvironnement(String pEnvironnement){
        fEnvironnement = pEnvironnement;
    }
    
    public void setLogin(String pLogin){
        fLogin = pLogin;
    }
    
    public void setOdbBackend(boolean pOdbBackend){
        fOdbBackend = pOdbBackend;
    }
    
    public void setPassword(String pPassword){
        fPassword = pPassword;
    }
    
    public void setWorkspace(int i){
        fWorkspace = fWorkspacesCodes.get(i);
    }
    
    public Object[] getWorkspacesLabels(){
        return fWorkspacesLabels.toArray();
    }
    
    public void setUrl(String pUrl){
        fUrl = pUrl;
        if(fUrl.endsWith("/")) fUrl=fUrl.substring(0, fUrl.length()-1);
        if(!fUrl.startsWith("http://") && !fUrl.startsWith("https://")) fUrl="http://"+fUrl;
    }
    
    public boolean initConnection(){
        try{
            String vXmlDocument = sendRequest(GET_WSP_LIST);
            Document vDoc = ImportUtils.getXMLDocument(new ByteArrayInputStream(vXmlDocument.getBytes()));
            Element vRootElement = vDoc.getDocumentElement();
            NodeList vNodeList = vRootElement.getChildNodes();
            fWorkspacesCodes = new ArrayList<String>();
            fWorkspacesLabels = new ArrayList<String>();
            
            for (int i = 0 ; i < vNodeList.getLength() ; i++){
                if(vNodeList.item(i).getNodeType() == Node.ELEMENT_NODE){
                    Element vElement = (Element)vNodeList.item(i);
                    if(vElement.getNodeName()!= null){
                        if(vElement.getNodeName().equals("wspProviderProperties")){
                            if(vElement.getAttributes().getNamedItem("backEnd").getNodeValue().equals("fs")) fOdbBackend = false;
                            else fOdbBackend = true;
                        }
                        if(vElement.getNodeName().equals("wsp")){
                            String vCode = vElement.getAttributes().getNamedItem("code").getNodeValue();
                            String vLabel;
                            if(!fOdbBackend) vLabel = new String(vCode);
                            else vLabel = vElement.getAttributes().getNamedItem("title").getNodeValue();
                            vLabel += " ("+ vElement.getAttributes().getNamedItem("wspTypes").getNodeValue().split(" ")[0]+")";
                            
                            fWorkspacesCodes.add(vCode);
                            fWorkspacesLabels.add(vLabel);
                        }
                    }    
                }
            }
            return true;
        }catch(Exception ex){
            //Exceptions.printStackTrace(ex);            
            return false;
        }
    }
}
