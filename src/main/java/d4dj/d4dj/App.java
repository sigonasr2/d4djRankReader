package d4dj.d4dj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import sig.utils.ImageUtils;
class SubmitThread implements Runnable{
	String name,description;
	int event,rank,points;
	
	SubmitThread(String name,String description,int points,int event,int rank) {
		this.name=name;
		this.description=description;
		this.points=points;
		this.event=event;
		this.rank=rank;
	}

	@Override
	public void run() {
		System.out.println(rank+": "+name+" ("+description+") - "+points);
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost("http://projectdivar.com/eventsubmit");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("eventid", Integer.toString(event)));
		params.add(new BasicNameValuePair("rank", Integer.toString(rank)));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("description", description));
		params.add(new BasicNameValuePair("points", Integer.toString(points)));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println("Rank "+rank+": ("+points+")");
		//Execute and get the response.
		/*HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		if (entity != null) {
		    try (InputStream instream = entity.getContent()) {
		    	Scanner s = new Scanner(instream).useDelimiter("\\A");
		    	String result = s.hasNext() ? s.next() : "";
				System.out.println("Rank "+rank+": "+result+"("+points+")");
				//System.out.println(rank+": "+name+" ("+description+") - "+points);
		    	instream.close();
		    } catch (UnsupportedOperationException | IOException e) {
				e.printStackTrace();
			}
		}*/
	}
}
public class App 
{
	public static Image paragraph/*,paragraph2,paragraph3*/;
	final static int EVENT = 16;
	public static int[] lastScores = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	public static int[] lastLowerTierScores = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	public static Font myFont = new Font("Serif", Font.BOLD, 26);
    public static int[] ranks = new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
    		50,100,500,1000,2000,5000,10000,20000,30000,50000};
    public static int[] drawRanks = new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
    		50,100,500,1000,2000,5000,10000,1000,2000,5000,10000,20000,30000,50000};
	
    public static void main( String[] args ) throws IOException
    {
    	Rectangle[] cropPoints1 = {
    			new Rectangle(305,163,200,23),
    			new Rectangle(305,262,200,23),
    			new Rectangle(305,358,200,23),
    			new Rectangle(305,453,200,23),
    			new Rectangle(305,550,200,23),
    			new Rectangle(305,645,200,23),
    			new Rectangle(305,740,200,23)
    	};
    	Rectangle[] cropPoints2 = {
    			new Rectangle(305,-191,200,23),
    			new Rectangle(305,-96,200,23),
    			new Rectangle(305,1,200,23),
    			new Rectangle(305,95,200,23),
    			new Rectangle(305,191,200,23),
    			new Rectangle(305,287,200,23)
    	};
    	Rectangle[] cropPoints3 = {
    			new Rectangle(305,206,200,23),
    			new Rectangle(305,302,200,23),
    			new Rectangle(305,398,200,23),
    			new Rectangle(305,494,200,23),
    			new Rectangle(305,589,200,23),
    			new Rectangle(305,686,200,23),
    			new Rectangle(305,781,200,23)
    	};

    	int lastCount=0;
    	
    	File folder = new File("Screenshots");
    	File[] list2 = folder.listFiles();
    	do {
			try {
				lastCount=list2.length;
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			list2 = folder.listFiles();
    	} while (list2.length!=lastCount);
		for (File fff : list2) {
			fff.delete();
		}
		paragraph = ImageIO.read(new File("paragraph.png"));
		/*paragraph2 = ImageIO.read(new File("paragraph2.png"));
		paragraph3 = ImageIO.read(new File("paragraph3.png"));*/
    	while (true) {
	    	File f = new File("Screenshots");
	    	if (f.listFiles().length==5) {
	    		//New files found!

		    	
		    	//Grab the first 5 files and try to parse them.
		    	File[] list = f.listFiles();
		    	try {
					Image leaderboard = ImageIO.read(list[0]);
					BufferedImage leaderboard2 = ImageIO.read(list[1]);
					Image leaderboard3 = ImageIO.read(list[2]);
					Image leaderboard4 = ImageIO.read(list[3]);
					Image leaderboard5 = ImageIO.read(list[4]);
					BufferedImage cropped1 = new BufferedImage(910,42*48,BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = cropped1.createGraphics();
					//For comments: down by 40, width is 340.
					for (int i=0;i<cropPoints1.length;i++) { //1-7
						drawData(g,1,leaderboard, 100, (i)*42, 100+200, (i)*42+23, cropPoints1[i].x, cropPoints1[i].y, cropPoints1[i].x+cropPoints1[i].width, cropPoints1[i].y+cropPoints1[i].height, null);
						drawData(g,2,leaderboard, 320, (i)*42, 320+340, (i)*42+22, cropPoints1[i].x, cropPoints1[i].y+61, cropPoints1[i].x+340, cropPoints1[i].y+84, null);
						drawData(g,0,leaderboard, 740, (i)*42, 740+130, (i)*42+24, cropPoints1[i].x+373, cropPoints1[i].y+48, cropPoints1[i].x+488, cropPoints1[i].y+72, null);
					}
					
					//start at X 146
					//Find R73,G40,B180
					//Scroll down until you find R>220,G>220,B>220
					//Scroll back up until you find 36,17,77
					//That is the Y position you start at.
					int MAXTRIES=1000;
					int count=0;
					int YMarker=145;
					while (count++<MAXTRIES) {
						Color c = new Color(leaderboard2.getRGB(140, YMarker++));
						if (c.getRed()==255&&c.getGreen()==255&&c.getBlue()==255) {
							System.out.println("Step 1 - Found! Y:"+(YMarker-1));
							break;
						}
					}count=0;
					/*System.out.println("Test");
					while (count++<MAXTRIES) {
						Color c = new Color(leaderboard2.getRGB(146, YMarker++));
						if (c.getRed()>=220&&c.getGreen()>=220&&c.getBlue()>=220) {
							System.out.println("Step 2 - Found! Y:"+(YMarker-1));
							break;
						}
					}count=0;
					System.out.println("Test");*/
					while (count++<MAXTRIES) {
						Color c = new Color(leaderboard2.getRGB(140, YMarker--));
						if (YMarker<=1) {
							break;
						}
						if (c.getRed()==0&&c.getGreen()==0&&c.getBlue()==0) {
							System.out.println("Step 3 - Found! Y:"+(YMarker+1));
							break;
						}
					}count=0;
					System.out.println("Test");
					int YOFFSET = YMarker+1;
					
					for (int i=0;i<cropPoints2.length;i++) {//8-13
						drawData(g,1,leaderboard2,100, (i+7)*42, 100+200, (i+7)*42+23, cropPoints2[i].x, YOFFSET+cropPoints2[i].y, cropPoints2[i].x+cropPoints2[i].width, YOFFSET+cropPoints2[i].y+cropPoints2[i].height, null);
						drawData(g,2,leaderboard2, 320, (i+7)*42, 320+340, (i+7)*42+22, cropPoints2[i].x, YOFFSET+cropPoints2[i].y+61, cropPoints2[i].x+340, YOFFSET+cropPoints2[i].y+84, null);
						drawData(g,0,leaderboard2, 740, (i+7)*42, 740+130, (i+7)*42+24, cropPoints2[i].x+373, YOFFSET+cropPoints2[i].y+48, cropPoints2[i].x+488, YOFFSET+cropPoints2[i].y+72, null);
					}
					for (int i=0;i<cropPoints3.length;i++) {//12-20
						drawData(g,1,leaderboard3, 100, (i+13)*42, 100+200, (i+13)*42+23, cropPoints3[i].x, cropPoints3[i].y, cropPoints3[i].x+cropPoints3[i].width, cropPoints3[i].y+cropPoints3[i].height, null);
						drawData(g,2,leaderboard3, 320, (i+13)*42, 320+340, (i+13)*42+22, cropPoints3[i].x, cropPoints3[i].y+61, cropPoints3[i].x+340, cropPoints3[i].y+84, null);
						drawData(g,0,leaderboard3, 740, (i+13)*42, 740+130, (i+13)*42+24, cropPoints3[i].x+373, cropPoints3[i].y+48, cropPoints3[i].x+488, cropPoints3[i].y+72, null);
					}
					for (int i=0;i<cropPoints1.length;i++) {//50-10000
						drawData(g,1,leaderboard4, 100, (i+20)*42, 100+200, (i+20)*42+23, cropPoints1[i].x, cropPoints1[i].y, cropPoints1[i].x+cropPoints1[i].width, cropPoints1[i].y+cropPoints1[i].height, null);
						drawData(g,2,leaderboard4, 320, (i+20)*42, 320+340, (i+20)*42+26, cropPoints1[i].x, cropPoints1[i].y+61, cropPoints1[i].x+340, cropPoints1[i].y+84, null);
						drawData(g,0,leaderboard4, 740, (i+20)*42, 740+130, (i+20)*42+24, cropPoints1[i].x+373, cropPoints1[i].y+48, cropPoints1[i].x+488, cropPoints1[i].y+72, null);
					}
					for (int i=0;i<cropPoints3.length;i++) {//10000-50000
						drawData(g,1,leaderboard5, 100, (i+27)*42, 100+200, (i+27)*42+23, cropPoints3[i].x, cropPoints3[i].y, cropPoints3[i].x+cropPoints3[i].width, cropPoints3[i].y+cropPoints3[i].height, null);
						drawData(g,2,leaderboard5, 320, (i+27)*42, 320+340, (i+27)*42+22, cropPoints3[i].x, cropPoints3[i].y+61, cropPoints3[i].x+340, cropPoints3[i].y+84, null);
						drawData(g,0,leaderboard5, 740, (i+27)*42, 740+130, (i+27)*42+24, cropPoints3[i].x+373, cropPoints3[i].y+48, cropPoints3[i].x+488, cropPoints3[i].y+72, null);
					}
					
					for (int i=0;i<drawRanks.length;i++) {
						drawRank(g,drawRanks[i],i*42);
					}
					
					ImageIO.write(cropped1,"png",new File("testing_leaderboardnames.png"));
					g.dispose();
					cropped1.flush();
					leaderboard.flush();
					leaderboard2.flush();
					leaderboard3.flush();
					leaderboard4.flush();
					leaderboard5.flush();
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
		    	File[] list3 = f.listFiles();
	    		for (File ff : list3) {
	    			ff.delete();
	    		}
		        String filePath = "testing_leaderboardnames.png";
		        try {
		        	detectText(filePath);
		        } catch (Exception e) {
		        	System.out.println(e.getMessage());
		        }
	    	} else {
		    	if (f.listFiles().length>5) {
			    	File[] list = f.listFiles();
		    		for (File ff : list) {
		    			ff.delete();
		    		}
		    	}
	    	}
			
	    	
	    	try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
      }

      private static void drawData(Graphics2D g, int type,Image leaderboard, int i, int j, int k, int l, int x, int y, int m,
			int n, ImageObserver object) {
    	  switch (type) {
	    	  case 0:{
	    		  //g.drawImage(paragraph, 0, j, 18, l, 0, 0, 18, 23, object);
	    	  }break;
	    	  case 1:{
	    		  //g.drawImage(paragraph, 2, j, 20, l, 0, 0, 18, 23, object);
	    	  }break;
	    	  case 2:{
	    		  ///g.drawImage(paragraph, i-18, j, i, l, 0, 0, 18, 23, object);
	    	  }break;
    	  }
    	  g.drawImage(leaderboard, i, j, k, l, x, y, m, n, object);
	}
      
      private static void drawRank(Graphics2D g,int number,int y) {
    	  g.setFont(myFont);
    	  g.setColor(Color.BLACK);
    	  String numb=Integer.toString(number);
    	  for (int i=0;i<5-Integer.toString(number).length();i++) {
    		  numb="0"+numb;
    	  }
    	  g.drawString(numb,0,y+24);
      }

	// Detects text in the specified image.
      public static void detectText(String filePath) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

        com.google.cloud.vision.v1.Image img = com.google.cloud.vision.v1.Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
            AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        StringBuilder reader = new StringBuilder();
        
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
          BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
          List<AnnotateImageResponse> responses = response.getResponsesList();

          for (AnnotateImageResponse res : responses) {
            if (res.hasError()) {
              System.out.format("Error: %s%n", res.getError().getMessage());
              return;
            }

            // For full list of available annotations, see http://g.co/cloud/vision/docs
            for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
            	reader.append(annotation.getDescription());
                //System.out.format("Position : %s%n", annotation.getBoundingPoly());
            	break;
              //System.out.println(annotation.getDescription());
              //System.out.format("Text: %s%n", annotation.getDescription());
            }
          }
          client.close();
        }
        System.out.println(reader);
        
        String[] s = reader.toString().split("\n");
        int count=0;
        int rankIndex=0;
        String[] orderedRanks= new String[] {
        		"00001","00002","00003","00004","00005","00006","00007","00008","00009","00010",
        		"00011","00012","00013","00014","00015","00016","00017","00018","00019","00020",
        		"00050","00100","00500","01000","02000","05000","10000","20000","30000","50000"
        };
        System.out.println(count+"/"+s.length);
    	while (count<s.length) {
	        if (s[count].equalsIgnoreCase(orderedRanks[rankIndex])) {
	        	List<String> mydata = new ArrayList<String>();
	        	count++;
	        	mydata.add(orderedRanks[rankIndex]);
	        	int rank = Integer.parseInt(orderedRanks[rankIndex]);
	        	while (count<s.length) {
	        		String nextRank=(rankIndex+1<orderedRanks.length)?orderedRanks[rankIndex+1]:"FDAJIVOAJVIAJJQ";
	        		if (s[count].equalsIgnoreCase(nextRank)) {
	        			break;
	        		}
	        		if (rank==30000&&s[count].equalsIgnoreCase("00100")) {
	        			break;
	        		}
	        		mydata.add(s[count++]);
	        	}
	        	System.out.println("Data: "+mydata);
	        	if (mydata.size()==4&&StringUtils.isNumeric(mydata.get(3))) {
	        		new Thread(
							new SubmitThread(mydata.get(1),mydata.get(2),Integer.parseInt(mydata.get(3)),EVENT,Integer.parseInt(mydata.get(0))))
					.start();
	        	} else
	        	if (mydata.size()==4&&StringUtils.isNumeric(mydata.get(2))) {
	        		new Thread(
							new SubmitThread(mydata.get(3),mydata.get(1),Integer.parseInt(mydata.get(2)),EVENT,Integer.parseInt(mydata.get(0))))
					.start();
	        	} else
	        	if (mydata.size()==3&&StringUtils.isNumeric(mydata.get(2))) {
	        		new Thread(
							new SubmitThread(mydata.get(1),"MuniMuni",Integer.parseInt(mydata.get(2)),EVENT,Integer.parseInt(mydata.get(0))))
					.start();
	        	} else
	        	if (mydata.size()==3&&StringUtils.isNumeric(mydata.get(1))) {
	        		new Thread(
							new SubmitThread(mydata.get(2),"MuniMuni",Integer.parseInt(mydata.get(1)),EVENT,Integer.parseInt(mydata.get(0))))
					.start();
	        	} else
	        	if (mydata.size()==2&&StringUtils.isNumeric(mydata.get(1))) {
	        		new Thread(
							new SubmitThread("Muni","MuniMuni",Integer.parseInt(mydata.get(1)),EVENT,Integer.parseInt(mydata.get(0))))
					.start();
	        	} else
	        	if (mydata.size()>4) {
	        		int marker=1;
	        		boolean foundName=false;
	        		boolean foundDesc=false;
	        		boolean foundPoints=false;
	        		String name = "";
	        		String desc = "";
	        		int points = 0;
	        		boolean submitted=false;
	        		while (marker<mydata.size()) {
	        			if (!foundName&&mydata.get(marker).length()>0) {
	        				foundName=true;
	        				name = mydata.get(marker);
	        			} else
	        			if (!foundDesc&&mydata.get(marker).length()>0) {
	        				foundDesc=true;
	        				desc = mydata.get(marker);
	        			} else 
	        			if (!foundPoints&&StringUtils.isNumeric(mydata.get(marker))&&mydata.get(marker).length()>0) {
	        				foundPoints=true;
	        				points = Integer.parseInt(mydata.get(marker));
	        			}
	        			marker++;
	        		}
	        		if (foundName&&foundDesc&&foundPoints) {
        				new Thread(
    							new SubmitThread(name,desc,points,EVENT,Integer.parseInt(mydata.get(0))))
    					.start();
        				submitted=true;
        			} else
	        		{
	        			System.out.println("Could not make a template from this: "+mydata);
	        			System.out.println("Name:"+foundName+"-"+name+", Description:"+foundDesc+"-"+desc+", Points:"+foundPoints+"-"+points);
	        		}
	        	} else {
	        		System.out.println("Could not find a suitable format for:"+mydata);
	        	}
	        	rankIndex++;
	        } else {
	        	count++;
	        }
    	}
    	
        /*System.out.println(scores);
        System.out.println(names);
        System.out.println(descriptions);*/
        

        
        /*
        for (int i=0;i<data.length;i++) {
        	//System.out.print(data[i].length()+",");
        	if (data[i].length()>0) {
        		if (scores.size()<)
        	}
        }*/
        
        //System.out.println(Arrays.toString(t20scores));
        //System.out.println(Arrays.toString(tempLowerTierScores));
        //Lower tier order is: 50,100,500,1000,2000,5000,10000,20000,30000,50000
        /*
        if (!error1) {
		    for (int i=0;i<20;i++) {
		    	new Thread(
						new SubmitThread(t20names[i],t20desc[i],t20scores[i],EVENT,i+1))
				.start();
		    }
        } else {
	        System.out.println("Cannot submit t20 scores. Invalid array params.");
        }
        int[] ranks = new int[] {50,100,500,1000,2000,5000,10000,20000,30000,50000};
        if (!error2) {
	        for (int j=0;j<10;j++) {
	        	try {
			    	//if (j>=1&&j<9&&Integer.parseInt(lowerTierScores[j])>Integer.parseInt(lowerTierScores[j+1])&&Integer.parseInt(lowerTierScores[j])<Integer.parseInt(lowerTierScores[j+1])) {
			        	new Thread(
								new SubmitThread(lowerTierNames[j],lowerTierDesc[j],lowerTierScores[j],EVENT,ranks[j]))
						.start();
			    	//}
	        	} catch (Exception e) {
			        System.out.println("Cannot parse score for rank "+ranks[j]+".");
	        	}
	        }
	      } else {
		        System.out.println("Cannot submit lower tier scores. Invalid array params.");
	      }
        */
      }

	private static void AddScore(List<Integer> scores, int[] ranks, String ss) {
		if (scores.size()>20) {
			//See if this score is already in list.
			if (StringUtils.isNumeric(ss)) {
				int thisScore = Integer.parseInt(ss);
				boolean found=false;
				for (int i=0;i<scores.size();i++) {
					if (thisScore==scores.get(i)) {
						found=true;
						break;
					}
				}
				if (StringUtils.isNumeric(ss)) {
					if (!found&&scores.size()<ranks.length) {
						scores.add(thisScore);
					}
				} else {
					scores.add(0);
				}
			} else {
				scores.add(0);
			}
		} else {
			if (StringUtils.isNumeric(ss)) {
				scores.add(Integer.parseInt(ss));
			} else {
				scores.add(0);
			}
		}
	}
      
}
