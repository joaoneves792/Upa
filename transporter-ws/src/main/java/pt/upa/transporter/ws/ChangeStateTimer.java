package pt.upa.transporter.ws;

import java.util.TimerTask;
import java.util.Timer;
import java.util.Random;


public class ChangeStateTimer extends TimerTask {
	
	JobView _job;
	Timer _timer;
	
	public ChangeStateTimer(JobView job, Timer timer) {
		_job = job;
		_timer = timer;
	}
	
	@Override
	public void run() {
		if(_job.getJobState() == JobStateView.ACCEPTED) {
			System.out.println(_job.getJobIdentifier() + " ACCEPTED -> HEADING");
			_job.setJobState(JobStateView.HEADING);
			Timer newTimer = new Timer();
			newTimer.schedule(new ChangeStateTimer(_job, newTimer), new Random().nextInt(5000));
			_timer.cancel();

		} else {
			if(_job.getJobState() == JobStateView.HEADING) {
				System.out.println(_job.getJobIdentifier() + " HEADING -> ONGOING");
				_job.setJobState(JobStateView.ONGOING);
				Timer newTimer = new Timer();
				newTimer.schedule(new ChangeStateTimer(_job, newTimer), new Random().nextInt(5000));
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



