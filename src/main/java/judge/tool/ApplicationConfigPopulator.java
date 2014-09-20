package judge.tool;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class ApplicationConfigPopulator {

    @Resource(name = "configProperties")
    private Properties configProperties;
    
    @PostConstruct
    public void init() {
        for (Object key : configProperties.keySet()) {
            Object value = configProperties.get(key);
            ApplicationContainer.serveletContext.setAttribute((String) key, value);
        }
        ApplicationContainer.serveletContext.setAttribute("version", System.currentTimeMillis());
    }

}
