package middleware.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import atlassharedclasses.ATLASObjectMapper;

public abstract class ATLASEventQueue<E> extends ArrayBlockingQueue<E> implements Runnable {
	
	public interface VoidLambda {
		public void op();
	}
	
	private static final long serialVersionUID = 1L;
	protected ATLASObjectMapper atlasOMapper;
	private boolean continueLoop = true;
	private char progressChar;
	private List<VoidLambda> afterHooks = new ArrayList<VoidLambda>();
	private int charCount = 0;
	private final int CHAR_COUNT_LIMIT = 80;

	public ATLASEventQueue(int capacity, char progressChar) {
		super(capacity);
		progressChar = '.';
		this.progressChar = progressChar;
	}
	
	public void registerAfterHook(VoidLambda v) {
		afterHooks.add(v);
	}
	
    public static void startThread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }
	
	public abstract void setup();
	
	private void printChar() {
		charCount++;
		System.out.print(progressChar);
		if (charCount > CHAR_COUNT_LIMIT) {
			System.out.println();
			charCount = 0;
		} 
	}
	
	public void run() {
		System.out.println("run() in ATLASEventQueue - " + this.getClass().getName());
		while (continueLoop) {
			try {
				E e = take();
				printChar();
				
				for (VoidLambda v : afterHooks) {
					v.op();
				}

				if (e != null) {
					handleEvent(e);
					// TODO: put logger calls here for the new event
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
	
	public abstract void handleEvent(E event);
}