package judge.remote.status;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import judge.bean.Submission;

@Component
public class RemoteStatusUpdateEvent {
	
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
				e.printStackTrace();
			}
		}
	}
	
	public static interface Listener {
		void onStatusUpdate(Submission submission) throws Exception;
	}
}
