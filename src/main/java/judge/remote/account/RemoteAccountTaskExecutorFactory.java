package judge.remote.account;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;

import judge.remote.RemoteOj;
import judge.remote.account.config.RemoteAccountOJConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class RemoteAccountTaskExecutorFactory {
	private final static Logger log = LoggerFactory.getLogger(RemoteAccountTaskExecutorFactory.class);

	public File jsonConfig;
	
	public RemoteAccountTaskExecutorFactory(String jsonConfigPath) {
		this.jsonConfig = new File(jsonConfigPath);
	}

	public RemoteAccountTaskExecutor create() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		Type type = new TypeToken<HashMap<RemoteOj, RemoteAccountOJConfig>>(){}.getType();
		HashMap<RemoteOj, RemoteAccountOJConfig> map = new Gson().fromJson(new FileReader(jsonConfig), type);
		if (map.containsKey(null)) {
			log.error("Remote OJ account config contains unknown OJ name");
			System.exit(-1);
		}
		return new RemoteAccountTaskExecutor(map);
	}

}
