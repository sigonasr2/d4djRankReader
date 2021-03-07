package d4dj.d4dj;

import java.awt.Color;
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

		//Execute and get the response.
		HttpResponse response = null;
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
		    	instream.close();
		    } catch (UnsupportedOperationException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
public class App 
{
	public static Image paragraph,paragraph2,paragraph3;
	final static int EVENT = 12;
	public static int[] lastScores = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	public static int[] lastLowerTierScores = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
    public static void main( String[] args ) throws IOException
    {
    	Rectangle[] cropPoints1 = {
    			new Rectangle(295,164,200,26),
    			new Rectangle(295,240,200,26),
    			new Rectangle(295,318,200,26),
    			new Rectangle(295,396,200,26),
    			new Rectangle(295,474,200,26),
    			new Rectangle(295,551,200,26),
    			new Rectangle(295,628,200,26),
    			new Rectangle(295,705,200,26),
    			new Rectangle(295,783,200,26),
    	};
    	Rectangle[] cropPoints2 = {
    			new Rectangle(295,0,200,26),
    			new Rectangle(295,77,200,26)
    	};
    	Rectangle[] cropPoints3 = {
    			new Rectangle(295,183,200,26),
    			new Rectangle(295,259,200,26),
    			new Rectangle(295,338,200,26),
    			new Rectangle(295,415,200,26),
    			new Rectangle(295,493,200,26),
    			new Rectangle(295,570,200,26),
    			new Rectangle(295,647,200,26),
    			new Rectangle(295,724,200,26),
    			new Rectangle(295,802,200,26)
    	};

    	File folder = new File("Screenshots");
    	File[] list2 = folder.listFiles();
    	do {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			list2 = folder.listFiles();
    	} while (list2.length%5!=0);
		for (File fff : list2) {
			fff.delete();
		}
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
					paragraph = ImageIO.read(new File("paragraph.png"));
					paragraph2 = ImageIO.read(new File("paragraph2.png"));
					paragraph3 = ImageIO.read(new File("paragraph3.png"));
					BufferedImage cropped1 = new BufferedImage(360,26*114,BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = cropped1.createGraphics();
					//For comments: down by 40, width is 340.
					for (int i=0;i<cropPoints1.length;i++) {
						drawData(g,1,leaderboard, 20, (i+38)*26, 200, (i+38)*26+26, cropPoints1[i].x, cropPoints1[i].y, cropPoints1[i].x+cropPoints1[i].width, cropPoints1[i].y+cropPoints1[i].height, null);
						drawData(g,2,leaderboard, 20, (i+76)*26, 340, (i+76)*26+26, cropPoints1[i].x, cropPoints1[i].y+40, cropPoints1[i].x+340, cropPoints1[i].y+cropPoints1[i].height+40, null);
						drawData(g,0,leaderboard, 40, (i)*26, 150, (i)*26+24, cropPoints1[i].x+351, cropPoints1[i].y+34, cropPoints1[i].x+481, cropPoints1[i].y+58, null);
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
						Color c = new Color(leaderboard2.getRGB(146, YMarker++));
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
						Color c = new Color(leaderboard2.getRGB(146, YMarker--));
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
					
					for (int i=0;i<cropPoints2.length;i++) {
						drawData(g,1,leaderboard2, 20, (i+47)*26, 200, (i+47)*26+26, cropPoints2[i].x, YOFFSET+cropPoints2[i].y, cropPoints2[i].x+cropPoints2[i].width, YOFFSET+cropPoints2[i].y+cropPoints2[i].height, null);
						drawData(g,2,leaderboard2, 20, (i+85)*26, 340, (i+85)*26+26, cropPoints2[i].x, YOFFSET+cropPoints2[i].y+40, cropPoints2[i].x+340, YOFFSET+cropPoints2[i].y+cropPoints2[i].height+40, null);
						drawData(g,0,leaderboard2, 40, (i+9)*26, 150, (i+9)*26+24, cropPoints2[i].x+351, YOFFSET+cropPoints2[i].y+34, cropPoints2[i].x+481, YOFFSET+cropPoints2[i].y+58, null);
					}
					for (int i=0;i<cropPoints3.length;i++) {
						drawData(g,1,leaderboard3, 20, (i+49)*26, 200, (i+49)*26+26, cropPoints3[i].x, cropPoints3[i].y, cropPoints3[i].x+cropPoints3[i].width, cropPoints3[i].y+cropPoints3[i].height, null);
						drawData(g,2,leaderboard3, 20, (i+87)*26, 340, (i+87)*26+26, cropPoints3[i].x, cropPoints3[i].y+40, cropPoints3[i].x+340, cropPoints3[i].y+cropPoints3[i].height+40, null);
						drawData(g,0,leaderboard3, 40, (i+11)*26, 150, (i+11)*26+24, cropPoints3[i].x+351, cropPoints3[i].y+34, cropPoints3[i].x+481, cropPoints3[i].y+58, null);
					}
					for (int i=0;i<cropPoints1.length;i++) {
						drawData(g,1,leaderboard4, 20, (i+58)*26, 200, (i+58)*26+26, cropPoints1[i].x, cropPoints1[i].y, cropPoints1[i].x+cropPoints1[i].width, cropPoints1[i].y+cropPoints1[i].height, null);
						drawData(g,2,leaderboard4, 20, (i+96)*26, 340, (i+96)*26+26, cropPoints1[i].x, cropPoints1[i].y+40, cropPoints1[i].x+340, cropPoints1[i].y+cropPoints1[i].height+40, null);
						drawData(g,0,leaderboard4, 40, (i+20)*26, 150, (i+20)*26+24, cropPoints1[i].x+351, cropPoints1[i].y+34, cropPoints1[i].x+481, cropPoints1[i].y+58, null);
					}
					for (int i=0;i<cropPoints3.length;i++) {
						drawData(g,1,leaderboard5, 20, (i+67)*26, 200, (i+67)*26+26, cropPoints3[i].x, cropPoints3[i].y, cropPoints3[i].x+cropPoints3[i].width, cropPoints3[i].y+cropPoints3[i].height, null);
						drawData(g,2,leaderboard5, 20, (i+105)*26, 340, (i+105)*26+26, cropPoints3[i].x, cropPoints3[i].y+40, cropPoints3[i].x+340, cropPoints3[i].y+cropPoints3[i].height+40, null);
						drawData(g,0,leaderboard5, 40, (i+29)*26, 150, (i+29)*26+24, cropPoints3[i].x+351, cropPoints3[i].y+34, cropPoints3[i].x+481, cropPoints3[i].y+58, null);
					}
					ImageIO.write(cropped1,"png",new File("testing_leaderboardnames.png"));
					g.dispose();
					cropped1.flush();
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
	    		  g.drawImage(paragraph, 0, j, 18, l, 0, 0, 18, 23, object);
	    	  }break;
	    	  case 1:{
	    		  g.drawImage(paragraph2, 0, j, 18, l, 0, 0, 18, 23, object);
	    	  }break;
	    	  case 2:{
	    		  g.drawImage(paragraph3, 0, j, 18, l, 0, 0, 18, 23, object);
	    	  }break;
    	  }
    	  g.drawImage(leaderboard, i, j, k, l, x, y, m, n, object);
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
        //System.out.println(reader);


        List<String> descriptions = new ArrayList<String>();
        List<String> names = new ArrayList<String>();
        List<Integer> scores = new ArrayList<Integer>();
        int[] ranks = new int[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
        		50,100,500,1000,2000,5000,10000,20000,30000,50000};
        
        
        String[] s = reader.toString().split("\n");
        String[] data = new String[s.length];
        int count=0;
        boolean scoreNext=false;
        for (String ss : s) {
        	//System.out.println(ss.replaceFirst("A","").trim());
        	//String newString = ss;//.replaceFirst("A","").trim();
        	if (StringUtils.isNumeric(ss)) {
        		if (scores.size()>20) {
        			//See if this score is already in list.
        			int thisScore = Integer.parseInt(ss);
        			boolean found=false;
        			for (int i=0;i<scores.size();i++) {
        				if (thisScore==scores.get(i)) {
        					found=true;
        					break;
        				}
        			}
        			if (!found&&scores.size()<ranks.length) {
        				scores.add(thisScore);
        			}
        		} else {
	        		scores.add(Integer.parseInt(ss));
        		}
        	} else
        	if (ss.charAt(0)=='A') {
        		//Expect the next value to be a score.
        		//scoreNext=true;
        	} else
        	if (ss.charAt(0)=='B') {
        		//Expect the next value to be a score.
        		//scoreNext=true;
        		String newString = ss.replaceFirst("B","").trim();
        		names.add(newString);
        	} else
        	if (ss.charAt(0)=='C') {
        		//Expect the next value to be a score.
        		//scoreNext=true;
        		String newString = ss.replaceFirst("C","").trim();
        		descriptions.add(newString);
        	}
        	//data[count++]=newString;
        }
        for (int i=0;i<scores.size();i++) {
        	//System.out.println(names.get(i)+": "+scores.get(i)+"  "+descriptions.get(i));
        	String desc = (descriptions.size()>i)?descriptions.get(i):"";
        	String name = (names.size()>i)?names.get(i):"";
        	System.out.println(ranks[i]+"-"+name+": "+scores.get(i));
        	
        	new Thread(
					new SubmitThread(name,desc,scores.get(i),EVENT,ranks[i]))
			.start();
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
      
}
