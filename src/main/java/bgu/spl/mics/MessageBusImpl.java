package bgu.spl.mics;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    // MicroServicesMessagesQueuesMap : HashMap , KEY= Microservice , VALUE = Queue<Message>
    private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> MicroServicesMessagesQueuesMap;

    // MessageSubscribedMSQueueMap : HashMap : KEY = Message , VALUE = Queue<Microservice>
    private ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<MicroService>> MessageSubscribedMSQueueMap;

    // resultMap : HashMap = Maps Event to Future Object.
    private ConcurrentHashMap<Event, Future> resultsMap;


    private Object lockSend = new Object();


    /**
     * private constructor for the messageBus
     */
    private MessageBusImpl() {
        MicroServicesMessagesQueuesMap = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
        MessageSubscribedMSQueueMap = new ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<MicroService>>();
        resultsMap = new ConcurrentHashMap<Event, Future>(); // key: Events , Value: Future
    }

    /**
     * getInstance returns the instance of the MessageBus.
     *
     * @return returns the instance of the MessageBus.
     * @post instance!=null
     */
    public static MessageBusImpl getInstance() {
        return MessageBusHolder.instance;
    }

    /**
     * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
     * <p>
     *
     * @param <T>  The type of the result expected by the completed event.
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     */
    public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if (MicroServicesMessagesQueuesMap.containsKey(m)) {
            // m is registered
            if (!MessageSubscribedMSQueueMap.containsKey(type)) {
                // type has no MSQueue. create one, add m to it and add it to the map.
                LinkedBlockingQueue<MicroService> queue = new LinkedBlockingQueue<MicroService>();
                queue.add(m); // Add m to the queue.
                MessageSubscribedMSQueueMap.put(type, queue); // Add queue to the map.
            } else {
                // type already has a MSQueue. add m to it (m can be subscribed more than once?)
                MessageSubscribedMSQueueMap.get(type).add(m);
            }
        }
    }


    /**
     * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
     * <p>
     *
     * @param type The type to subscribe to.
     * @param m    The subscribing micro-service.
     */
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if (MicroServicesMessagesQueuesMap.containsKey(m)) {
            // m is registered
            if (!MessageSubscribedMSQueueMap.containsKey(type)) {
                // type (event) doesn't have a MSQueue
                // create a new m-s queue for type and add m to it. add the queue to the map
                LinkedBlockingQueue<MicroService> queue = new LinkedBlockingQueue<MicroService>();
                queue.add(m);
                MessageSubscribedMSQueueMap.put(type, queue);
            } else {
                // type has a MSQueue. add m to it.(m can be added more than once?)
                MessageSubscribedMSQueueMap.get(type).add(m);
            }
        }
    }


    /**
     * Notifies the MessageBus that the event {@code e} is completed and its
     * result was {@code result}.
     * When this method is called, the message-bus will resolve the {@link Future}
     * object associated with {@link Event} {@code e}.
     * <p>
     *
     * @param <T>    The type of the result expected by the completed event.
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     */
    public <T> void complete(Event<T> e, T result) {
        if (resultsMap.containsKey(e)) {
            Future future = resultsMap.remove(e);
            future.resolve(result);
        } else {
            // for debugging.
            //System.out.println("MessageBusImpl : complete() : Event e not resultsMap. ");
        }
    }

    /**
     * Adds the {@link Broadcast} {@code b} to the message queues of all the
     * micro-services subscribed to {@code b.getClass()}.
     * <p>
     *
     * @param b The message to added to the queues.
     */
    public void sendBroadcast(Broadcast b) {
        // NOTE : b.getClass() returns the run-time class of b. this is the 'type' used at subscribing.

        if (MessageSubscribedMSQueueMap.containsKey(b.getClass())) {
            // b's type has a MSQueue. get the queue and for each m in that queue, add b to the Message Queue of that m,
            // fetched by MicroServicesMessagesQueuesMap
            LinkedBlockingQueue<MicroService> bTypeMSQueue = MessageSubscribedMSQueueMap.get(b.getClass());
            bTypeMSQueue.forEach((MicroService m) -> {
                LinkedBlockingQueue<Message> mMessagesQueue = MicroServicesMessagesQueuesMap.get(m);
                if (mMessagesQueue != null) mMessagesQueue.add(b);
            });
        }
    }

    /**
     * Adds the {@link Event} {@code e} to the message queue of one of the
     * micro-services subscribed to {@code e.getClass()} in a round-robin
     * fashion. This method should be non-blocking.
     * <p>
     *
     * @param <T> The type of the result expected by the event and its corresponding future object.
     * @param e   The event to add to the queue.
     * @return {@link Future<T>} object to be resolved once the processing is complete,
     * null in case no micro-service has subscribed to {@code e.getClass()}.
     */
    public <T> Future<T> sendEvent(Event<T> e) {
        // NOTE : e.getClass() returns the run-time class of e. this is the 'type' used when subscribing.
        synchronized (lockSend) {
            if (MessageSubscribedMSQueueMap.containsKey(e.getClass())) {
                // e's type has a MSQueue.

                //create a Future Object for e , add it to resultsMap for later use.
                Future<T> future = new Future<>();
                resultsMap.put(e, future);

                // fetch e's type MSQueue from MessageSubscribedMSQueueMap.
                // Implement Round-Robin by fetching the messages queue of the MS that is first in e's type MSQueue and moving that MS to the end of e's type MSQueue.
                LinkedBlockingQueue<MicroService> eTypeMSQueue = MessageSubscribedMSQueueMap.get(e.getClass());
                if (!eTypeMSQueue.isEmpty()) {
                    // at least one MS to send the event to.
                    MicroService m = eTypeMSQueue.poll();
                    LinkedBlockingQueue<Message> mMessagesQueue = MicroServicesMessagesQueuesMap.get(m);
                    mMessagesQueue.add(e);
                    eTypeMSQueue.add(m);
                    return future;
                }
            }
        }
        return null;
    }

    /**
     * Allocates a message-queue for the {@link MicroService} {@code m}.
     * <p>
     *
     * @param m the micro-service to create a queue for.
     * @pre m.isRegistered()==false
     * @inv m!=null
     * @post m.isRegistered()==true
     */
    public synchronized void register(MicroService m) {
        // act only if m is NOT registered.

        if (!MicroServicesMessagesQueuesMap.containsKey(m)) {
            // m is not registered..
            LinkedBlockingQueue<Message> microQueue = new LinkedBlockingQueue<Message>();
            MicroServicesMessagesQueuesMap.put(m, microQueue);
        }
    }

    /**
     * Removes the message queue allocated to {@code m} via the call to
     * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
     * related to {@code m} in this message-bus. If {@code m} was not
     * registered, nothing should happen.
     * <p>
     *
     * @param m the micro-service to unregister.
     * @pre m.isRegistered()==true
     * @inv m!=null
     * @post m.isRegistered()==false
     */
    public synchronized void unregister(MicroService m) {
        // act only if m is registered.
        if (MicroServicesMessagesQueuesMap.containsKey(m)) {
            // m i registered

            //remove the message queue assigned to m at the MicroServicesMessagesQueuesMap.
            MicroServicesMessagesQueuesMap.remove(m);

            // iterate over the MessageSubscribedMSQueueMap. for each (type,MSQueue) remove m from the MSQueue.
            MessageSubscribedMSQueueMap.forEach((type, queue) -> {
                LinkedBlockingQueue<MicroService> typeMSQueue = MessageSubscribedMSQueueMap.get(type);
                typeMSQueue.remove(m);
            });
        }
    }

    /**
     * Using this method, a <b>registered</b> micro-service can take message
     * from its allocated queue.
     * This method is blocking meaning that if no messages
     * are available in the micro-service queue it
     * should wait until a message becomes available.
     * The method should throw the {@link IllegalStateException} in the case
     * where {@code m} was never registered.
     * <p>
     *
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return The next message in the {@code m}'s queue (blocking).
     * @throws InterruptedException if interrupted while waiting for a message
     *                              to became available.
     * @pre m!=null, m.isRegistered()==true
     * @inv m is not interrupted.
     * @post @pre(m.queue.size()) = m.queue.size()-1
     */
    public Message awaitMessage(MicroService m) throws InterruptedException {

        // if m is not registered throw exception.
        if (!MicroServicesMessagesQueuesMap.containsKey(m)) throw new IllegalStateException();

        // get m's message queue.
        LinkedBlockingQueue<Message> messagesQueue = MicroServicesMessagesQueuesMap.get(m);

        // take() is a blocking function, if the queue is empty it waits until something is being inserted (because we are using LinkedBlockingQueue).
        Message msg = messagesQueue.take();

        return msg;
    }

    private static class MessageBusHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }
}
