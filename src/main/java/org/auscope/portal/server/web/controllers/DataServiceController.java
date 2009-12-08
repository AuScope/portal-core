package org.auscope.portal.server.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.auscope.portal.server.util.GeodesyUtil;
import org.auscope.portal.server.util.PortalPropertyPlaceholderConfigurer;
import org.auscope.portal.server.web.view.JSONModelAndView;
import org.auscope.portal.server.gridjob.GridAccessController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

/**
 * Controller that handles all Data Service related requests.
 *
 * @author Abdi Jama
 */
@Controller
public class DataServiceController {
   
   protected final Log logger = LogFactory.getLog(getClass());
   
   @Autowired
   @Qualifier(value = "propertyConfigurer")
   private PortalPropertyPlaceholderConfigurer hostConfigurer;
   
     
   @RequestMapping("/saveSelection.do")
   public void saveSelection(HttpServletRequest request,
		                     HttpServletResponse response,
		                     @RequestParam("mySelection") String selectedList) {
	   request.getSession().setAttribute("selectedGPSfiles", selectedList);
       logger.debug("Saved user's selected GPS files "+selectedList);
   } 
   
   @RequestMapping("/getSelection.do")
   public void saveSelection(HttpServletRequest request,
		                     HttpServletResponse response) throws Exception {
	   String selectedList = (String) request.getSession().getAttribute("selectedGPSfiles");
	   
       logger.debug("Return user's selected GPS files "+selectedList);
       response.setContentType("text/xml");
       response.getWriter().print(selectedList);
       //response.getWriter().print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><data><url_date><select_item>false</select_item><fileDate>2009-Sep-1</fileDate><fileUrl>http://files.ivec.org/geodesy/gpsdata/09244/alby2440.09g.Z</fileUrl></url_date><url_date><select_item>false</select_item><fileDate>2009-Sep-1</fileDate><fileUrl>http://files.ivec.org/geodesy/gpsdata/09244/alby2440.09n.Z</fileUrl></url_date><url_date><select_item>false</select_item><fileDate>2009-Sep-1</fileDate><fileUrl>http://files.ivec.org/geodesy/gpsdata/09244/alby2440.09o.Z</fileUrl></url_date></data>");
       response.getWriter().close();
   } 
   
   @RequestMapping("/sendToGrid.do")
   public ModelAndView sendToGrid(HttpServletRequest request,
                                  HttpServletResponse response, ModelMap model)throws Exception {
	   String selectedFiles = request.getParameter("myFiles");
	   logger.debug("selected GPS files for grid job: "+selectedFiles);
	   request.getSession().setAttribute("gridInputFiles", selectedFiles);

	   model.put("success", true);
	   
	   return new JSONModelAndView(model);
   }
   
   @RequestMapping("/zipDownload.do")
   public ModelAndView zipDownload(HttpServletRequest request,
                           HttpServletResponse response, ModelMap model)throws Exception {
	   String selectedFiles = request.getParameter("myFiles");
	   logger.debug("selected GPS files for zip: "+selectedFiles);
	   //Zip and Download is on hold.
	   /*List<String> urlsList = GeodesyUtil.getSelectedGPSFiles(selectedFiles);

	   if(urlsList == null){
		   logger.error("No files to Zip for download");
		   return;
	   }
	   
       // Create a new directory to zip all files into.
	   //String user = request.getRemoteUser();
	   String user = "user";
	   SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
       String dateFmt = sdf.format(new Date());
       String jobID = user + "-" + dateFmt + File.separator;
       String jobInputDir = jobID;

       boolean success = (new File(jobInputDir)).mkdir();

       if (!success) {
           logger.error("Could not create directory "+jobInputDir);
           jobInputDir = gridAccess.getLocalGridFtpStageInDir();
       }
	   
      
       // These are the files to include in the ZIP file
       String[] filenames =  urlsList.toArray(new String[]{});
       
       // Create a buffer for reading the files
       byte[] buf = new byte[1024];
       ZipOutputStream out = null;
       InputStream in = null;
       try {
           // The ZIP file
           String outFilename = jobInputDir+"outfile.zip";
           out = new ZipOutputStream(new FileOutputStream(outFilename));
           out.setLevel(9);
           // add the files
           for (int i=0; i<filenames.length; i++) {
        	   URL url = new URL(filenames[i]);
        	   logger.debug("Zipping file: "+filenames[i]);
        	   in = url.openStream();
       
               // Add ZIP entry to output stream.
        	   int myIndex = filenames[i].lastIndexOf("/");
        	   String fName = filenames[i].substring(myIndex+1, filenames[i].length());
               out.putNextEntry(new ZipEntry(fName));
               logger.debug("File: "+fName);
       
               // Transfer bytes from the file to the ZIP file
               int len;
               while ((len = in.read(buf)) > 0) {
                   out.write(buf, 0, len);
               }
       
               // Complete the entry
               out.closeEntry();
               in.close();
           }
       
           // Complete the ZIP file
           out.close();
       } catch (IOException e) {
       }finally{
    	   in.close();
    	   out.close();
       }
	   return new ModelAndView("zipDownload");*/
	   model.put("success", true);
	   
	   return new JSONModelAndView(model);
   }
   
}
