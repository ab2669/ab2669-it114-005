package common;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class TimedEvent 
{
    private int secondsRemaining;
    private Runnable expireCallback = null;
    private Consumer<Integer> tickCallback = null;
    final private Timer timer;

    public TimedEvent(int durationInSeconds, Runnable callback) 
    {
        this(durationInSeconds, callback, callback);
        this.expireCallback = callback;
    }
     
    public TimedEvent(int durationInSeconds, Runnable callback, Runnable expireCallback) 
    {
        this.expireCallback = expireCallback;
        timer = new Timer();
        secondsRemaining = durationInSeconds;
        timer.scheduleAtFixedRate(new TimerTask() 
        {
            public void run() {
                secondsRemaining--;
                if (tickCallback != null) 
                {
                    tickCallback.accept(secondsRemaining);
                }
                if (secondsRemaining <= 0) 
                {
                    timer.cancel();
                    secondsRemaining = 0;
                    if (expireCallback != null) 
                    {
                        expireCallback.run();
                    }
                }
            }
        }, 1000, 1000);
    }

    public void setTickCallback(Consumer<Integer> callback) 
    {
        tickCallback = callback;
    }

    public void setExpireCallback(Runnable callback) 
    {
        expireCallback = callback;
    }

    public void cancel() 
    {
        expireCallback = null;
        tickCallback = null;
        timer.cancel();
    }

    public void setDurationInSeconds(int d) 
    {
        secondsRemaining = d;
    }

    public int getRemainingTime() 
    {
        return secondsRemaining;
    }

    public static void main(String args[]) 
    {
        TimedEvent cd = new TimedEvent(10, () -> 
        {
            System.out.println("Time expired");
        });
        cd.setTickCallback((tick) -> 
        {
            System.out.println("Tick: " + tick);
        });
    }
}