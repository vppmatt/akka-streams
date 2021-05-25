import java.util.concurrent.Flow;

public class J9Subscriber implements Flow.Subscriber<String> {

    Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        System.out.println("on sub called");
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(String s) {
        System.out.println("Java 9 sink received " + s);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("on error");
        subscription.cancel();
    }

    @Override
    public void onComplete() {
        System.out.println("on complete");
        subscription.cancel();
    }
}
