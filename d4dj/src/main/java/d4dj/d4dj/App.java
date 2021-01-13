package d4dj.d4dj;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import sig.utils.ImageUtils;
class SubmitThread implements Runnable{
	String name,description,points;
	int event,rank;
	
	SubmitThread(String name,String description,String points,int event,int rank) {
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
		params.add(new BasicNameValuePair("eventid", Integer.toString(7)));
		params.add(new BasicNameValuePair("rank", Integer.toString(rank)));
		params.add(new BasicNameValuePair("name", name));
		params.add(new BasicNameValuePair("description", description));
		params.add(new BasicNameValuePair("points", points));
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
				System.out.println(result);
		    	instream.close();
		    } catch (UnsupportedOperationException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
public class App 
{
    public static void main( String[] args )
    {
    	/*
    	Tesseract tesseract = new Tesseract();
    	tesseract.setDatapath("C:\\Users\\sigon\\eclipse-workspace\\d4djRankReaderBot\\d4dj\\tessdata");
    	try {
    		tesseract.setLanguage("jpn");
    		//tesseract.setLanguage("eng");
    		String finaldata = tesseract.doOCR(new File("imgtest5.png"));
    		Thread.sleep(500);
			System.out.println(finaldata);
		} catch (TesseractException | InterruptedException e) {
			e.printStackTrace();
		}*/

    	Tesseract tesseract = new Tesseract();
    	tesseract.setTessVariable("tessedit_write_images", "true");
    	tesseract.setDatapath("C:\\Users\\sigon\\eclipse-workspace\\d4djRankReaderBot\\d4dj\\tessdata");
    	
    	
    	Rectangle[] namepositions = new Rectangle[] {new Rectangle(475,263,316,36),new Rectangle(475,384,316,36),new Rectangle(475,510,316,36),new Rectangle(475,636,316,36)};
    	Rectangle[] descriptions = new Rectangle[] {new Rectangle(475,328,544,36),new Rectangle(475,452,544,36),new Rectangle(475,576,544,36),new Rectangle(475,700,544,36)};
    	Rectangle[] pointpositions = new Rectangle[] {new Rectangle(1042,317,200,34),new Rectangle(1042,442,200,34),new Rectangle(1042,566,200,34),new Rectangle(1042,688,200,34)};
    	Point[] offsets = new Point[] {new Point(0,0),new Point(0,-2),new Point(0,-1),new Point(0,5),new Point(0,20)};
    	
    	String[][] namedata = new String[5][4];
    	String[][] descriptiondata = new String[5][4];
    	String[][] pointdata = new String[5][4];
    	
    	while (true) {
	    	File f = new File("C:\\Users\\sigon\\Pictures\\MEmu Photo\\Screenshots");
	    	//System.out.println(Arrays.deepToString(f.listFiles()));
	    	
	    	if (f.listFiles().length==5) {
	    		//New files found!

		    	
		    	//Grab the first 5 files and try to parse them.
		    	File[] list = f.listFiles();
		    	for (int i=0;i<5;i++) {
		    		try {
						BufferedImage img = ImageUtils.toBufferedImage(ImageIO.read(list[i]));
						//ImageIO.write(img,"png",new File("debug/img"+))
			    		for (int j=0;j<4;j++) {
				    		tesseract.setLanguage("jpn");
				    		//ImageIO.write(img.getSubimage(namepositions[j].x+offsets[j].x,namepositions[j].y+offsets[j].y,namepositions[j].width,namepositions[j].height),"png",new File("debug/img"+i+"_"+j+"_0.png"));
				    		//ImageIO.write(img.getSubimage(descriptions[j].x+offsets[j].x,descriptions[j].y+offsets[j].y,descriptions[j].width,descriptions[j].height),"png",new File("debug/img"+i+"_"+j+"_1.png"));
				    		//ImageIO.write(img.getSubimage(pointpositions[j].x+offsets[j].x,pointpositions[j].y+offsets[j].y,pointpositions[j].width,pointpositions[j].height),"png",new File("debug/img"+i+"_"+j+"_2.png"));
				    		String name = tesseract.doOCR(img,new Rectangle(namepositions[j].x+offsets[i].x,namepositions[j].y+offsets[i].y,namepositions[j].width,namepositions[j].height));
				    		String description = tesseract.doOCR(img,new Rectangle(descriptions[j].x+offsets[i].x,descriptions[j].y+offsets[i].y,descriptions[j].width,descriptions[j].height));
				    		tesseract.setLanguage("eng");
				    		String points = tesseract.doOCR(img,new Rectangle(pointpositions[j].x+offsets[i].x,pointpositions[j].y+offsets[i].y,pointpositions[j].width,pointpositions[j].height));
				    		
				    		namedata[i][j]=name;
				    		descriptiondata[i][j]=description;
				    		pointdata[i][j]=points;
			    		}
					} catch (IOException | TesseractException e) {
						e.printStackTrace();
					}
		    	}
	    		
	    		
	    		for (File ff : list) {
	    			ff.delete();
	    		}

				// Request parameters and other properties.
				for (int i=0;i<20;i++) {
					new Thread(
							new SubmitThread(namedata[i/4][i%4],descriptiondata[i/4][i%4],pointdata[i/4][i%4].replaceAll(" ","").replaceAll("\\.",""),7,i+1))
					.start();
				}
	    	} else {
		    	if (f.listFiles().length>5) {
			    	File[] list = f.listFiles();
		    		for (File ff : list) {
		    			ff.delete();
		    		}
		    	}
	    	}
	    	
	    	/*System.out.println(Arrays.deepToString(namedata));
	    	System.out.println(Arrays.deepToString(descriptiondata));
	    	System.out.println(Arrays.deepToString(pointdata));*/
	    	
			
	    	
	    	try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
}
