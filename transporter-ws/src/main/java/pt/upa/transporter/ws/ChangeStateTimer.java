package pt.upa.transporter.ws;

import java.util.TimerTask;
import java.util.Timer;
import java.util.Random;


public class ChangeStateTimer extends TimerTask {
	
	JobView _job;
	Timer _timer;
	int _maxTime;
	int _minTime;
	
	public ChangeStateTimer(JobView job, Timer timer, int minTime, int maxTime) {
		_job = job;
		_timer = timer;
		_minTime = minTime;
		_maxTime = maxTime;
	}
	
	@Override
	public void run() {
		if(_job.getJobState() == JobStateView.ACCEPTED) {
			System.out.println(_job.getJobIdentifier() + " ACCEPTED -> HEADING");
			_job.setJobState(JobStateView.HEADING);
			Timer newTimer = new Timer();
			newTimer.schedule(new ChangeStateTimer(_job, newTimer, _minTime, _maxTime), _minTime + (new Random().nextInt(_maxTime - _minTime)));
			_timer.cancel();

		} else {
			if(_job.getJobState() == JobStateView.HEADING) {
				System.out.println(_job.getJobIdentifier() + " HEADING -> ONGOING");
				_job.setJobState(JobStateView.ONGOING);
				Timer newTimer = new Timer();
				newTimer.schedule(new ChangeStateTimer(_job, newTimer, _minTime, _maxTime), _minTime + (new Random().nextInt(_maxTime - _minTime)));
				_timer.cancel();
				
			} else {
				if(_job.getJobState() == JobStateView.ONGOING) {
					System.out.println(_job.getJobIdentifier() + " ONGOING -> COMPLETED");
					_job.setJobState(JobStateView.COMPLETED);
					_timer.cancel();
				}
			}
		}
	}
}



