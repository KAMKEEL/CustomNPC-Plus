package noppes.npcs.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public class AnalyticsTracking{

	public static void sendData(final EntityPlayer player, final String event, final String data){
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					 UUID uuid = player.getUniqueID();
					 
			         String analyticsPostData = "v=1" +
			                         "&tid=UA-29079943-5" +
			                         "&cid=" + uuid.toString() +
			                         "&t=event" +
			                         "&ec=customnpcs_1.7d" +
			                         "&ea=" + event +
			                         "&el=" + data +
			                         "&ev=300";

			         URL url = new URL("http://www.google-analytics.com/collect");
			         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			         connection.setConnectTimeout(10000);
			         connection.setReadTimeout(10000);
			         connection.setDoOutput(true);
			         connection.setUseCaches(false);
			         connection.setRequestMethod("POST");
			         connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			         connection.setRequestProperty("Content-Length", Integer.toString(analyticsPostData.getBytes().length));
			         OutputStream dataOutput = connection.getOutputStream();
			         dataOutput.write(analyticsPostData.getBytes());
			         dataOutput.close();
			         connection.getInputStream().close();
				}
				catch (IOException e) {
					
				}
				
			}
		};
		Thread t = new Thread(r);
		t.start();
	}
	
}
