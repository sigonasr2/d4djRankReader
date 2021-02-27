package sig.utils;

import java.io.IOException;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GithubUtils {
	public static int getSizeOfFileFromLatestGithubCommit(String filename) {
		try {
			JSONObject data = FileUtils.readJsonFromUrl("https://api.github.com/repos/sigonasr2/sigIRCv2");
			String url = data.getString("commits_url").replace("{/sha}", "");
			JSONArray data_array = FileUtils.readJsonArrayFromUrl(url);
			JSONObject dat = data_array.getJSONObject(0);
			JSONObject datapoint1 = dat.getJSONObject("commit");
			datapoint1 = datapoint1.getJSONObject("tree");
			url = datapoint1.getString("url");
			data = FileUtils.readJsonFromUrl(url);
			data_array = data.getJSONArray("tree");
			for (int i=0;i<data_array.length();i++) {
				JSONObject file_data = data_array.getJSONObject(i);
				if (file_data.getString("path").equals(filename)) {
					return file_data.getInt("size");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
