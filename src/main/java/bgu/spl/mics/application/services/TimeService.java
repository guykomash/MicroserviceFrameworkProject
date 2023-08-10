package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	public MessageBusImpl bus;
	public TickBroadcast b;
	int speed; //final?
	int duration; //final?
	Timer timer;
	int timeLeft;


	public TimeService(int speed,int duration) {
		super("TimeService");
		this.bus=MessageBusImpl.getInstance();
		this.speed=speed;
		this.duration=duration;
		TickBroadcast b=new TickBroadcast();
		this.timer=new Timer();
		timeLeft=duration;

		// TODO Implement this
	}

	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class,(TerminateBroadcast b)->{
			terminate();
		});
		timer.scheduleAtFixedRate(new TimerTask() {  //
			public void run() {
				if(timeLeft>0) {
					sendBroadcast(new TickBroadcast());
					timeLeft--;
				}
				else {
					sendBroadcast(new TerminateBroadcast());
					timer.cancel();

				}
			}
		},0, speed);
	}

}
