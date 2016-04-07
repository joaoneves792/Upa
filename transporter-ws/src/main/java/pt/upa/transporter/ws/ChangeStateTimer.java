package pt.upa.transporter.ws;

import java.util.TimerTask;
import java.util.Timer;
import java.util.Random;


public class ChangeStateTimer extends TimerTask {
	
	JobView _job;
	
	public ChangeStateTimer(JobView job) {
		_job = job;
	}
	
	@Override
	public void run() {
		if(_job.getJobState() == JobStateView.ACCEPTED) {
			System.out.printf(_job.getJobIdentifier() + " ACCEPTED -> HEADING");
			_job.setJobState(JobStateView.HEADING);
			Timer timer = new Timer();
			timer.schedule(new ChangeStateTimer(_job), new Random().nextInt(5000));

		} else {
			if(_job.getJobState() == JobStateView.HEADING) {
				System.out.printf(_job.getJobIdentifier() + " HEADING -> ONGOING");
				_job.setJobState(JobStateView.ONGOING);
				Timer timer = new Timer();
				timer.schedule(new ChangeStateTimer(_job), new Random().nextInt(5000));
				
			} else {
				if(_job.getJobState() == JobStateView.ONGOING) {
					System.out.printf(_job.getJobIdentifier() + " ONGOING -> COMPLETED");
					_job.setJobState(JobStateView.COMPLETED);
				}
			}
		}
	}
}

// public class ChangeStateTimer extends TimerTask {
// 	
// 	JobView _job;
// 	
// 	public ChangeStateTimer(JobView job) {
// 		_job = job;
// 	}
// 	
// 	@Override
// 	public void run() {
// 		if(_job.getJobState() == JobStateView.ACCEPTED) {
// 			_job.setJobState(JobStateView.HEADING);
// 
// 		} else {
// 			if(_job.getJobState() == JobStateView.HEADING) {
// 				_job.setJobState(JobStateView.ONGOING);
// 				
// 			} else {
// 				if(_job.getJobState() == JobStateView.ONGOING) {
// 					_job.setJobState(JobStateView.COMPLETED);
// 					cancel(false);
// 				}
// 			}
// 		}
// 	}
// }