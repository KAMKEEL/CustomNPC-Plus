package noppes.npcs.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class TranslateUtil {
	private static final String TranslateUrl = "http://translate.google.com/translate_a/t?client=t&text=%s&hl=en&sl=%s&tl=%s&ie=UTF-8&oe=UTF-8&multires=1&otf=1&pc=1&trs=1&ssel=3&tsel=6&sc=1";
	private static final String AudioUrl = "http://translate.google.com/translate_tts?q=%s&tl=%s";

	public static String Translate(String text){
		
		try {
			String urlStr = String.format(TranslateUrl, URLEncoder.encode(text,"utf8"), "auto", "nl");
			URL url = new URL(urlStr);
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		    connection.setDoOutput(true);
		    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
		    connection.setRequestProperty("X-HTTP-Method-Override", "GET");
		    //connection.setRequestProperty("referer", "accounterlive.com");
		    connection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = reader.readLine();
			reader.close();
			connection.disconnect();
			if(line != null){
				String parsed = parseJson(line);
				if(parsed != null)
					return parsed;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
//	public static void PlayAudio(String text){
//		try {
//			String urlStr = String.format(AudioUrl, URLEncoder.encode(text,"utf8"), "en");
//			System.out.println(urlStr);
//			URL url = new URL(urlStr);
//			URLConnection  connection = url.openConnection();
//
//		    //connection.setDoOutput(true);
//		    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
//		    connection.setRequestProperty("X-HTTP-Method-Override", "GET");
//		    
//	        InputStream audioSrc = new BufferedInputStream(connection.getInputStream());
//	        //DataInputStream read = new DataInputStream(audioSrc);
//	        AudioInputStream audioIn = AudioSystem.getAudioInputStream(audioSrc);
//	        Clip clip = AudioSystem.getClip();
//		    
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (UnsupportedAudioFileException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (LineUnavailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	private static String parseJson(String line) {
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(line);
		while(element.isJsonArray()){
			JsonArray array = (JsonArray) element;
			if(array.size() == 0)
				return null;
			element = array.get(0);
		}
		System.out.println(element.getAsString());
		return element.getAsString();
	}
}
