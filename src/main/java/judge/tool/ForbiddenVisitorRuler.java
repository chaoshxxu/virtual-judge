package judge.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.Resource;

public class ForbiddenVisitorRuler {
    
    Resource config;
    
    List<Rule> rules = new ArrayList<ForbiddenVisitorRuler.Rule>();
    
    public ForbiddenVisitorRuler() {
    }
    
    public ForbiddenVisitorRuler(Resource config) {
        this.config = config;
    }
    
    @PostConstruct
    public void init() throws DocumentException, IOException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(config.getInputStream());
        
        List nodes = document.selectNodes( "/forbiddenVisitorRules/*/*" );
        for (Object obj : nodes) {
            Node rule = (Node) obj;
            String method = rule.getName();
            String value = rule.getText();
            if (!StringUtils.isEmpty(value)) {
                String item = rule.getParent().getName();
                rules.add(new Rule(
                        JUDGE_ITEM.valueOf(item),
                        JUDGE_METHOD.valueOf(method),
                        value
                ));
            }
        }
    }
    
    boolean forbidden(String ua, String ip) {
        if (StringUtils.isBlank(ua) || StringUtils.isBlank(ip)) {
            return true;
        }
        
        for (Rule rule : rules) {
            String value = rule.item == JUDGE_ITEM.ua ? ua : ip;

            if (rule.method == JUDGE_METHOD.equals) {
                if (StringUtils.equals(value, rule.value)) {
                    return true;
                }
            } else if (rule.method == JUDGE_METHOD.substring) {
                if (StringUtils.containsIgnoreCase(value, rule.value)) {
                    return true;
                }
            } else if (rule.method == JUDGE_METHOD.pattern) {
                if (value.matches(rule.value)) {
                    return true;
                }
            } else {
                throw new RuntimeException();
            }
        }
        return false;
    }
    
    
    enum JUDGE_ITEM {
        ua,
        ip
    }
    
    enum JUDGE_METHOD {
        
        //Exactly equals
        equals,
        
        //Substring ignoring case
        substring,
        
        //Regular expression match
        pattern
    }
    
    class Rule {
        JUDGE_ITEM item;
        JUDGE_METHOD method;
        String value;
        
        public Rule(JUDGE_ITEM item, JUDGE_METHOD method, String value) {
            this.item = item;
            this.method = method;
            this.value = value;
        }
    }
}
