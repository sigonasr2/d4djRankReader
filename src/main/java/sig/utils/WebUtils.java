package sig.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class WebUtils {
	public static String POSTimage(String url,File file,Map<String,String> params) {
		String res = "No response.";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost uploadFile = new HttpPost(url);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		for (String s : params.keySet()) {
			builder.addTextBody(s, params.get(s), ContentType.TEXT_PLAIN);
		}
		try {
			// This attaches the file to the POST:
			builder.addBinaryBody(
			    "file",
			    new FileInputStream(file),
			    ContentType.APPLICATION_OCTET_STREAM,
			    file.getName()
			);

			HttpEntity multipart = builder.build();
			uploadFile.setEntity(multipart);
			CloseableHttpResponse response;
			response = httpClient.execute(uploadFile);
			HttpEntity responseEntity = response.getEntity();
			Thread.sleep(2000);
			String result = "";
			if (responseEntity != null) {
			    try (InputStream instream = responseEntity.getContent()) {
			    	Scanner s = new Scanner(instream).useDelimiter("\\A");
			    	result = s.hasNext() ? s.next() : "";
			    	res=result;
			    	instream.close();
			    } catch (UnsupportedOperationException | IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return res;
	}
}