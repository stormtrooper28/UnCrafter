package github.stormtrooper28.unCrafter.Utils;

import github.stormtrooper28.unCrafter.UnCrafter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


					


/**
 *
 * Update checker for Spigot plugins <br/>
 * <br/>
 * Feel free to copy this class into your project, just give credits to me and don't remove the copyright ;)
 *
 * @author inventivetalent
 */
public class AutoUpdater extends Thread {

	private UnCrafter					uc;
	private final Plugin				plugin;
	//private static Plugin				target;
	private final boolean				log = true;

	private boolean						enabled	= true;

	public static URL					download;
	private URL							url;
	//private static String 				pps; //pluginPathString
	//private static java.nio.file.Path 	pluginPath = null;

	/**
	 * Create a new {@link AutoUpdater} instance <br/>
	 * <br/>
	 *
	 * <b>Note:</b> The updater runs automatically after initializing it, no need to call {@link #start()}
	 *
	 * @param plugin
	 *            instance of your {@link Plugin}
	 * @param resourceID
	 *            id of the resource on SpigotMC.org
	 */
	public AutoUpdater(UnCrafter uc) throws IOException {
		this(uc, true);
		this.uc = uc;
	}

	/**
	 * Create a new {@link AutoUpdater} instance <br/>
	 * <br/>
	 *
	 * <b>Note:</b> The updater runs automatically after initializing it, no need to call {@link #start()}
	 *
	 * @param plugin
	 *            instance of your {@link Plugin}
	 * @param resourceID
	 *            id of the resource on SpigotMC.org
	 * @param log
	 *            if <code>true</code>, messages about the current updater actions will be logged to the console
	 */
	public AutoUpdater(Plugin plugin, boolean log) throws IOException {
		if (plugin == null) throw new IllegalArgumentException("Plugin cannot be null");

		this.plugin = plugin;
		this.url = new URL("http://pastebin.com/raw.php?i=e72zBYhb");

		

		super.start();
	}

	@Override
	public synchronized void start() {
		// Override so the Thread doesn't run multiple times
	}

	@Override
	public void run() {
		if (!this.plugin.isEnabled()) return;
		if (!this.enabled) return;
		if (this.log) {
			this.plugin.getLogger().info("Searching for updates...");
		}
		HttpURLConnection connection = null;

		try {
			try{
			connection = (HttpURLConnection) this.url.openConnection();
			connection.setRequestMethod("GET");
			} catch (UnknownHostException e){
				uc.getLogger().severe("Update Server could not be reached!");
				return;
			}
			BufferedReader in = null;
			try{
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} catch (Exception e){
				uc.getLogger().severe("Update Server could not be reached!");		
				return;
			}

			String content = "";
			String line = null;
			while ((line = in.readLine()) != null) {
				content += line;
			}
			in.close();

			org.json.simple.JSONObject json = null;
			try {
				json = (org.json.simple.JSONObject) new JSONParser().parse(content);
			} catch (ParseException e) {
			}

			if (json != null && json.containsKey("updatenews")) {
				uc.updateNews = (String) json.get("updatenews");
			}

			if (json != null && json.containsKey("download")) {
				uc.downloadUrl = (String) json.get("download");
			}			
			
			String newVersion = null;

			if (json != null && json.containsKey("version")) {
				String version = (String) json.get("version");
				if (version != null && !version.isEmpty()) {
					newVersion = version;
				}
			}
			if (newVersion == null) {
				if (this.log) {
					this.plugin.getLogger().warning("Invalid response received.");
					this.plugin.getLogger().warning("The AutoUpdater is experiencing some issues.");
				}
				return;
			}
						
			List<Character> chars = new ArrayList<>();
			
			//for(int c = 0; c < 10; c++)
			//	chars.add((char) c);
			chars.add('0');
			chars.add('1');
			chars.add('2');
			chars.add('3');
			chars.add('4');
			chars.add('5');
			chars.add('6');
			chars.add('7');
			chars.add('8');
			chars.add('9');
			chars.add('#');

			newVersion = newVersion.replaceFirst(".", "#");
			
			for(int c = 0; c < newVersion.length(); c++){
				if(!chars.contains(newVersion.charAt(c)))
					newVersion = newVersion.replace(newVersion.charAt(c), ' ');
			} newVersion = newVersion.replaceAll(" ", "");

			newVersion = newVersion.replaceFirst("#", ".");
			
			float oV = UnCrafter.scv;
			float cV = Float.valueOf(newVersion);
			
			if ( cV > oV) {
				this.plugin.getLogger().info("Found new version: " + newVersion + "! (Your version is " + this.plugin.getDescription().getVersion() + ")");
				uc.updateAvailable = true;
				uc.updateMessage = UnCrafter.prefix+"Found new version: " + newVersion + "! (Your version is " + this.plugin.getDescription().getVersion() + ")"+
				"\n   Your update can be found at " + uc.downloadUrl;
			}
			else if (cV == oV){
				this.plugin.getLogger().info("No udates found.");
			}
			else{
				this.plugin.getLogger().info("Warning: You are using a development build of this plugin!");
			}
		} catch (IOException e) {
			if (this.log) {
				if (connection != null) {
					try {
						int code = connection.getResponseCode();
						this.plugin.getLogger().warning("API connection returned response code " + code);
					} catch (IOException ex) {
					}
				}
				e.printStackTrace();
			}
		}
	}
		
}







