package getRiskfreeRates;

import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class getTreasHTML 
{
      private String treasuryRateURL="";
      
	  public getTreasHTML()
	  {
	    	System.out.println("==in  getTreasHTML Constructor ==");
	  }
	  
	  public String getTreasuryRateURL() {
	    	   return treasuryRateURL;
	  } 
	  
	  public static void main(String[] args) 
	  {
		  getTreasHTML gt = new getTreasHTML();
		  gt.getTreasHTMLLink();
	  }
	  
	  public void getTreasHTMLLink()
	  {
		    System.out.println("Enter getTreasHTMLLink()");
			Document doc;
			try {
				// get the URL reference for the current treasury rate XML
			
				String tmpUrl ="https://www.treasury.gov/resource-center/data-chart-center/interest-rates";
				       tmpUrl+="/Pages/TextView.aspx?data=yield";
				System.out.println("CALLING with URL="+tmpUrl);
				doc = Jsoup.connect(tmpUrl).timeout(90*1000).get();

				// get page title
				String title = doc.title();
				System.out.println("title : " + title);

				// get all links
				Elements links = doc.select("a[href]");
				for (Element link : links) {

					// get the value from href attribute
					
					String tmpstr = link.attr("href").toString();
					if( tmpstr.contains("http://data.treasury.gov/feed.svc/DailyTreasuryYieldCurveRateData"))
					{
						String t2 = tmpstr.replaceAll(" ", "%20");
					    System.out.println("\nlink : " + t2);
					    treasuryRateURL = t2;				    
					}

				}
					        
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Exit getTreasHTMLLink()");
	  }
}
