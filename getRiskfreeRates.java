package getRiskfreeRates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;



public class getRiskfreeRates 
{
	
	private String REC_ID="ERROR";
	private String NEW_DATE="";
    private String BC_1MONTH="";
    private String BC_3MONTH="";
    private String BC_6MONTH="";
    private String BC_1YEAR="";
    private String BC_2YEAR="";
    private String BC_3YEAR="";
    private String BC_5YEAR="";
    private String BC_7YEAR="";
    private String BC_10YEAR="";
    private String BC_20YEAR="";
    private String BC_30YEAR="";
    
    private static String treasuryURL="";
    
    //================================================
    // JDBC driver name and database URL
    final String DB_URL = "jdbc:mysql://localhost/grandCentralData";
    //  Database credentials
    final String USER = "admin";
    final String PASS = "admin";
    //================================================  
    
    
    
    public getRiskfreeRates()
    {
    	System.out.println("==in  getRiskfreeRates() Constructor ==");
    }
    
	public static void main(String[] args) 
	{
		System.out.println("==in  MAIN ===");
		getRiskfreeRates rf = new getRiskfreeRates();
		getTreasHTML gt = new getTreasHTML();
		gt.getTreasHTMLLink();
		treasuryURL = gt.getTreasuryRateURL();
		rf.getRates();
		rf.dumpObject();
		rf.dbInsertRiskFreeRate();
		//rf.dbGetCurrentRiskFreeRates();
		//rf.dumpObject();
	}
		
	public void getRates()
	{
		System.out.println("Entry getRates()");
		StringBuilder content = new StringBuilder();
		try {
			
			String buf = "";
			String urlstr = treasuryURL;
			
			//http://data.treasury.gov/feed.svc/DailyTreasuryYieldCurveRateData?$filter=month(NEW_DATE)%20eq%2012%20and%20year(NEW_DATE)%20eq%202016
			URL url = new URL(urlstr);
	        InputStream is = url.openStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	             
	        while ( (buf = br.readLine()) != null)
	        {
	            //System.out.println("READ VAL="+buf);
	            content.append(buf + "\n");
	            //System.out.println("READ VAL="+content.toString());	            
	        }    
	        br.close();
	        is.close();
	        
	        //===========================
	        // parse the document string
	        
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	InputSource ds = new InputSource(new StringReader(content.toString()));
	        Document doc = dBuilder.parse(ds);
	        
    	    //optional, but recommended
	    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    	doc.getDocumentElement().normalize();

	    	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	    	
	    	NodeList nList = doc.getElementsByTagName("entry");

	    	System.out.println("----------------------------");
	    	
	    	// get last entry, it contains the latest rates
	    	int ecnt = nList.getLength();
	    	
	    	for (int temp = 0; temp < nList.getLength(); temp++) 
	    	{
	    		Node nNode = nList.item(temp);
	    		ecnt--;
	    		System.out.println(temp+" ecnt="+ecnt+" > Current Element :" + nNode.getNodeName());

	    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {

	    			Element eElement = (Element) nNode;

	    			//System.out.println("id : " + eElement.getAttribute("id"));
	    			//System.out.println("id : " + eElement.getElementsByTagName("id").item(0).getTextContent());
	    			//System.out.println("Attrs="+eElement.hasChildNodes());
	    			//System.out.println("kids="+eElement.getChildNodes().toString());
	    			NodeList kids =  eElement.getChildNodes();
	    			for (int i = 0; i < kids.getLength(); i++)
	    			{
	    				Node kidNode = kids.item(i);
	    				//System.out.println("kidNodeName="+kidNode.getNodeName());
	    				if(kidNode.getNodeName().equals("content"))
	    				{
	    					//System.out.println("kidNode has kids="+kidNode.hasChildNodes());
	    					//System.out.println("kidNode="+kidNode.getTextContent());
	    					NodeList gkids =  kidNode.getChildNodes();
	    					for (int j = 0; j < gkids.getLength(); j++)
	    					{
	    						Node gkid = gkids.item(j);
	    						if(gkid.getNodeName().toString().equals("m:properties"))
	    						{
	    						   //System.out.println(" gkidNodeName="+gkid.getNodeName());
	    						   //System.out.println(" gkid has kids="+gkid.hasChildNodes());
	    						   //System.out.println(" gkidNode="+gkid.getTextContent());
	    						   NodeList ggkids =  gkid.getChildNodes();
	    						   for (int k = 0; k < ggkids.getLength(); k++)
	    						   {
	    							   Node ggkid = ggkids.item(k);
	    							   if(ggkid.getNodeName().contains("d:") && ecnt==0)
	    							   {
	    							       if(ggkid.getNodeName().equals("d:Id"))
	    							       {
	    							    	   REC_ID=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:NEW_DATE"))
	    							       {
	    							    	   String tmpstr = ggkid.getTextContent();
	    							    	   String tmpstr1 = "";
	    							    	   System.out.println(" LEN ="+tmpstr.length());
	    							    	   if( tmpstr.length() > 10 )  // err check
	    							    	   {
	    							    		   tmpstr1=tmpstr.substring(0, 10);
	    							    	   }
	    							    	   NEW_DATE=tmpstr1;
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_1MONTH"))
	    							       {
	    							    	   BC_1MONTH=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_3MONTH"))
	    							       {
	    							    	   BC_3MONTH=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_6MONTH"))
	    							       {
	    							    	   BC_6MONTH=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_1YEAR"))
	    							       {
	    							    	   BC_1YEAR=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_2YEAR"))
	    							       {
	    							    	   BC_2YEAR=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_3YEAR"))
	    							       {
	    							    	   BC_3YEAR=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_5YEAR"))
	    							       {
	    							    	   BC_5YEAR=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_7YEAR"))
	    							       {
	    							    	   BC_7YEAR=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_10YEAR"))
	    							       {
	    							    	   BC_10YEAR=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_20YEAR"))
	    							       {
	    							    	   BC_20YEAR=ggkid.getTextContent();
	    							       }
	    							       if(ggkid.getNodeName().equals("d:BC_30YEAR"))
	    							       {
	    							    	   BC_30YEAR=ggkid.getTextContent();
	    							       }
	    							   }
	    						   }
	    						}
	    					}
	    				    
	    				}
	    			}
	    			//System.out.println("ele text="+eElement.getTextContent());
	    			//System.out.println("kids="+eElement.getTagName());
	    			//System.out.println(" tag : " + eElement.getNodeType());
	    		}
	    	}


	        
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void dumpObject()
	{
		System.out.println("===DUMP OBJECT BEGIN===");
		System.out.println("REC_ID   ="+REC_ID);
		System.out.println("NEW_DATE ="+ NEW_DATE);
		System.out.println("BC_1MONTH="+BC_1MONTH);
		System.out.println("BC_3MONTH="+BC_3MONTH);
		System.out.println("BC_6MONTH="+BC_6MONTH);
		System.out.println("BC_1YEAR ="+BC_1YEAR);
		System.out.println("BC_2YEAR ="+BC_2YEAR);
		System.out.println("BC_3YEAR ="+BC_3YEAR);
		System.out.println("BC_5YEAR ="+BC_5YEAR);
		System.out.println("BC_7YEAR ="+BC_7YEAR);
		System.out.println("BC_10YEAR="+BC_10YEAR);
		System.out.println("BC_20YEAR="+BC_20YEAR);
		System.out.println("BC_30YEAR="+BC_30YEAR);
		System.out.println("===DUMP OBJECT END  ===");
	}
	
	public void dbInsertRiskFreeRate()
	{
		        if(REC_ID.equals("ERROR"))
		        {
		        	System.out.println("dbInsertRiskFreeRate() CAUGHT ERROR! NO INSERT!");
	                return;
		        }
		        
		        Connection conn = null;
		        Statement stmt = null;
		       
		    	System.out.println("Entry Point dbInsertRiskFreeRate() ");
		        try{
		          //STEP 2: Register JDBC driver
		          Class.forName("com.mysql.jdbc.Driver");

		          //STEP 3: Open a connection
		          //System.out.println("Connecting to database...");
		          conn = DriverManager.getConnection(DB_URL,USER,PASS);
	
		          stmt = conn.createStatement();
		          String sql;
		          sql  = "INSERT INTO riskFreeRates (";
		          sql += "REC_ID,";
		          sql += "NEW_DATE,";
		          sql += "BC_1MONTH,BC_3MONTH,BC_6MONTH,";
		          sql += "BC_1YEAR,BC_2YEAR,BC_3YEAR,";
		          sql += "BC_5YEAR,BC_7YEAR,BC_10YEAR,";
		          sql += "BC_20YEAR,BC_30YEAR) ";
		          sql += "VALUES ('"+REC_ID+"',";
		          sql += "'"+NEW_DATE+"',";
		          sql += "'"+BC_1MONTH+"',";
		          sql += "'"+BC_3MONTH+"',";
		          sql += "'"+BC_6MONTH+"',";
		          sql += "'"+BC_1YEAR+"',";
		          sql += "'"+BC_2YEAR+"',";
		          sql += "'"+BC_3YEAR+"',";
		          sql += "'"+BC_5YEAR+"',";
		          sql += "'"+BC_7YEAR+"',";
		          sql += "'"+BC_10YEAR+"',";
		          sql += "'"+BC_20YEAR+"',";
		          sql += "'"+BC_30YEAR+"')";
		          System.out.println("debug INSERT SQL= "+sql);
		          stmt.execute(sql);
	          
		          stmt.close();
		          conn.close();
		        }catch(SQLException se){
		          se.printStackTrace();
		        }catch(Exception e){
		          e.printStackTrace();
		        }finally{
		            try{
		               if(stmt!=null)
		                  stmt.close();
		            }catch(SQLException se){
		              se.printStackTrace();
		            }//end finally try
		        }//end try
		     
		        System.out.println("Exit Point dbInsertRiskFreeRate() ");
	}//end dbInsertRiskFreeRate
	
	public void dbGetCurrentRiskFreeRates()
	{
	   //SELECT * FROM `riskFreeRates` WHERE 
		//NEW_DATE = (SELECT max(NEW_DATE) FROM riskFreeRates)
		
	  	Connection conn = null;
    	Statement stmt = null;
    	       
    	System.out.println("dbGetCurrentRiskFreeRates() EntryPoint");
    	try{
    	          //STEP 2: Register JDBC driver
    	          Class.forName("com.mysql.jdbc.Driver");

    	          //STEP 3: Open a connection
    	          //System.out.println("Connecting to database...");
    	          conn = DriverManager.getConnection(DB_URL,USER,PASS);

    	          //STEP 4: Execute a query
    	          //System.out.println("Creating statement...");
    	          stmt = conn.createStatement();
    	          String sql;
    	          sql  = "SELECT ";
		          sql += "REC_ID,";
		          sql += "NEW_DATE,";
		          sql += "BC_1MONTH,BC_3MONTH,BC_6MONTH,";
		          sql += "BC_1YEAR,BC_2YEAR,BC_3YEAR,";
		          sql += "BC_5YEAR,BC_7YEAR,BC_10YEAR,";
		          sql += "BC_20YEAR,BC_30YEAR  ";		          
    	          sql += "FROM riskFreeRates ";
    	          sql += "WHERE NEW_DATE = (SELECT max(NEW_DATE) FROM riskFreeRates) LIMIT 1";
    	          
    	          System.out.println("debug SELECT SQL= "+sql);
    	          
    	          ResultSet rs = stmt.executeQuery(sql);

    	          //STEP 5: Extract data from result set
    	          while(rs.next())
    	          {
    	             //Retrieve by column name
    	             REC_ID = rs.getString("REC_ID");
    	             NEW_DATE = rs.getDate("NEW_DATE").toString();
    	             BC_1MONTH = rs.getString("BC_1MONTH");
    	             BC_3MONTH = rs.getString("BC_3MONTH");
    	             BC_6MONTH = rs.getString("BC_6MONTH");
    	             BC_1YEAR = rs.getString("BC_1YEAR");
    	             BC_2YEAR = rs.getString("BC_2YEAR");
    	             BC_3YEAR = rs.getString("BC_3YEAR");
    	             BC_5YEAR = rs.getString("BC_5YEAR");
    	             BC_7YEAR = rs.getString("BC_7YEAR");
    	             BC_10YEAR = rs.getString("BC_10YEAR");
    	             BC_20YEAR = rs.getString("BC_20YEAR");
    	             BC_30YEAR = rs.getString("BC_30YEAR");
    	          }
    	          //STEP 6: Clean-up environment
    	          rs.close();
    	          stmt.close();
    	          conn.close();
    	}catch(SQLException se){
    	          //Handle errors for JDBC
    	          se.printStackTrace();
    	}catch(Exception e){
    	          //Handle errors for Class.forName
    	          e.printStackTrace();
    	}finally{
    	//finally block used to close resources
    	     try{
    	          if(stmt!=null)
    	                stmt.close();
    	     }catch(SQLException se){
    	              se.printStackTrace();
    	     }//end finally try
    	}//end try
    	System.out.println("dbGetCurrentRiskFreeRates() Goodbye! loaded object ");
	}
}
