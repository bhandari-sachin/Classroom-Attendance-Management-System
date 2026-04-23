package frontend.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

class TestBodySubscriber implements Flow.Subscriber<ByteBuffer> {

    private final List<ByteBuffer> buffers = new ArrayList<>();
    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ByteBuffer item) {
        buffers.add(item);
    }

    @Override
    public void onError(Throwable throwable) {
        throw new RuntimeException(throwable);
    }

    @Override
    public void onComplete() {
        if (subscription != null) {
            subscription.cancel();
        }
    }

    String getBodyAsString() {
        int total = buffers.stream().mapToInt(ByteBuffer::remaining).sum();
        byte[] bytes = new byte[total];
        int offset = 0;

        for (ByteBuffer buffer : buffers) {
            ByteBuffer copy = buffer.asReadOnlyBuffer();
            int length = copy.remaining();
            copy.get(bytes, offset, length);
            offset += length;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}