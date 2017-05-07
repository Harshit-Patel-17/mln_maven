package iitd.data_analytics.mln.mln;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import iitd.data_analytics.mln.gpu.GpuConfig;;

public class InputParams {
  
  private String mlnFile;
  private boolean useGpu;
  
  public InputParams(String xmlFilePath) throws ParserConfigurationException, SAXException, IOException {
    File xmlFile = new File(xmlFilePath);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(xmlFile);
    
    doc.getDocumentElement().normalize();
    
    mlnFile = doc.getElementsByTagName("mln").item(0).getTextContent();
    useGpu = (doc.getElementsByTagName("gpu").item(0).getTextContent().equalsIgnoreCase("yes"));
    GpuConfig.ptxBase = doc.getElementsByTagName("ptxPath").item(0).getTextContent();
  }
  
  public String getMlnFile() {
    return mlnFile;
  }
  
  public boolean useGpu() {
    return useGpu;
  }
}
