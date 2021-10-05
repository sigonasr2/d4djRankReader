package d4dj.d4dj;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import sig.utils.FileUtils;
import sig.utils.ImageUtils;
import sig.utils.WebUtils;

public class App {
	public static String APIKEY = "";
	public static void main(String[] args) {
		if (new File("apikey").exists()) {
			APIKEY = FileUtils.readFromFile("apikey")[0];
		} else {
			System.err.println("API Key file does not exist! It has been created at:");
			File f = new File("apikey");
			try {
				f.createNewFile();
				FileUtils.writetoFile(new String[] {"API key goes here."},"apikey");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.err.println("  "+f.getAbsolutePath());
			System.err.println("Please fill it in then run this program again.");
			System.exit(1);
		}
		while (true) {
	    	File f = new File("Screenshots");
	    	File[] fileList = f.listFiles();
	    	for (File ff : fileList) {
	    		ParseImage(ff);
	    		ff.delete();
	    	}
	    	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void ParseImage(File ff) {
		int pointerX=108;
		int pointerY=160;
		int counter=0;
		try {
			BufferedImage leaderboard = ImageIO.read(ff);
			BufferedImage template = new BufferedImage(2400,60*7,BufferedImage.TYPE_INT_RGB);
			template.getGraphics().setColor(Color.WHITE);
			template.getGraphics().fillRect(0, 0, 2400, 60*7);
			boolean foundPurple = false;
			for (pointerY=230;pointerY<900;pointerY++) {
				Color c = new Color(leaderboard.getRGB(pointerX, pointerY));
				if (c.getRed()>130&&c.getGreen()>=50&&c.getGreen()<=140&&c.getBlue()>=190&&c.getBlue()>130) {
					foundPurple=true;
				}
				if (foundPurple&&c.getRed()<=10&&c.getGreen()<=10&&c.getBlue()<=10) {
					for (int i=0;i<7;i++) {
						BufferedImage rank = ImageUtils.removeBrightPixels(ImageUtils.invertImage(ImageUtils.deepCopy(leaderboard).getSubimage(pointerX,pointerY-60,105,40)),400);
						Image name = ImageUtils.removeBrightPixels(ImageUtils.invertImage(ImageUtils.deepCopy(leaderboard).getSubimage(306,pointerY-85,200,20)),400).getScaledInstance(-1, 40, Image.SCALE_SMOOTH);
						Image desc = ImageUtils.removeBrightPixels(ImageUtils.invertImage(ImageUtils.deepCopy(leaderboard).getSubimage(306,pointerY-25,356,20)),200).getScaledInstance(-1, 40, Image.SCALE_SMOOTH);
						Image score = ImageUtils.removeBrightPixels(ImageUtils.invertImage(ImageUtils.deepCopy(leaderboard).getSubimage(677,pointerY-37,118,22)),200).getScaledInstance(-1, 40, Image.SCALE_SMOOTH);
						template.getGraphics().drawImage(rank,0,60*counter+5,null);
						template.getGraphics().drawImage(name,120,60*counter+5,null);
						template.getGraphics().drawImage(desc,540,60*counter+5,null);
						template.getGraphics().drawImage(score,1272,60*counter+5,null);
						foundPurple=false;
						pointerY+=96;
						counter++;
					}
					break;
				}
			}
			if (counter>0) {
				template.getGraphics().setColor(Color.BLACK);
				for (int i=0;i<7;i++) {
					template.getGraphics().clearRect(0,60*i-2,2400,4);
				}
				template.getGraphics().clearRect(110,0,4,60*7);
				template.getGraphics().clearRect(530,0,4,60*7);
				template.getGraphics().clearRect(1262,0,4,60*7);
				ImageIO.write(template,"png",new File("output.png"));
				Map<String,String> values = new HashMap<String,String>();
				values.put("language","jpn");
				values.put("filetype","PNG");
				values.put("isTable","true");
				values.put("apikey",APIKEY);
				System.out.println(WebUtils.POSTimage("https://apipro1.ocr.space/parse/image", new File("output.png"), values));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
