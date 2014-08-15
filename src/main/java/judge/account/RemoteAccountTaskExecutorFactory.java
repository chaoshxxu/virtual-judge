package judge.account;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;

import judge.account.config.RemoteAccountOJConfig;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class RemoteAccountTaskExecutorFactory {
	
	public File jsonConfig;
	
	public RemoteAccountTaskExecutorFactory(String jsonConfigPath) {
		this.jsonConfig = new File(jsonConfigPath);
	}

	public RemoteAccountTaskExecutor create() throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		Type type = new TypeToken<HashMap<String, RemoteAccountOJConfig>>(){}.getType();
		HashMap<String, RemoteAccountOJConfig> map = new Gson().fromJson(new FileReader(jsonConfig), type);
		return new RemoteAccountTaskExecutor(map);
	}

}
