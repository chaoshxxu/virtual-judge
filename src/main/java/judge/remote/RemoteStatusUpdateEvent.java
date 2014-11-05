package judge.remote;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import judge.bean.Submission;

@Component
public class RemoteStatusUpdateEvent {
    private final static Logger log = LoggerFactory.getLogger(RemoteStatusUpdateEvent.class);

    private List<Listener> listeners = new ArrayList<Listener>();
    
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
    
    public void fireStatusUpdate(Submission submission) {
        for (Listener listener : listeners) {
            try {
                listener.onStatusUpdate(submission);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public static interface Listener {
        void onStatusUpdate(Submission submission) throws Exception;
    }
}
