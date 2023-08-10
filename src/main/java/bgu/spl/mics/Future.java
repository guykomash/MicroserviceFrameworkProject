package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * <p>
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
    private boolean isDone;
    private T result;
    private Object lock = new Object();

    /**
     * This should be the only public constructor in this class.
     */
    public Future() {
        isDone = false;
        result = null;
    }

    /**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     *
     * @return return the result of type T if it is available, if not wait until it is available.
     * @pre none
     * @post get()!=null
     */
    public synchronized T get() {
        while (!isDone) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        return result;
    }

    /**
     * Resolves the result of this Future object.
     *
     * @pre
     * @post get()==result
     */
    public synchronized void resolve(T result) {
        this.result = result;
        this.isDone = true;
        this.notifyAll();
    }

    /**
     * @return true if this object has been resolved, false otherwise
     * @pre
     * @post get!=null return true ,else return false
     */

    public boolean isDone() {
        return isDone == true;
    }

    /**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     *
     * @param timeout the maximal amount of time units to wait for the result.
     * @param unit    the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not,
     * wait for {@code timeout} TimeUnits {@code unit}. If time has
     * elapsed, return null.
     * @pre
     * @post get()==result || get()==null
     */

    public T get(long timeout, TimeUnit unit) {
        if (!isDone) {
            try {
                unit.sleep(timeout);
            } catch (InterruptedException e) {
                return result;
            }
        }
        return result;
    }
}