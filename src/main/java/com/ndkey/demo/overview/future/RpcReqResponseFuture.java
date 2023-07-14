package com.ndkey.demo.overview.future;

import com.ndkey.demo.overview.pojo.RpcRequest;
import com.ndkey.demo.overview.pojo.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author tangbu
 */
public class RpcReqResponseFuture implements Future<RpcResponse> {

    private RpcRequest request;
    private RpcResponse response;
    private Executor executor;

    private boolean done;
    private boolean failed;

    private Lock lock = new ReentrantLock();
    private Condition getCondition = lock.newCondition();


    public RpcReqResponseFuture(RpcRequest request, Executor executor) {
        this.request = request;
        this.executor = executor;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }


    public void done(RpcResponse response) {
        lock.lock();
        try {
            this.response = response;
            done = true;
            getCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }



    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        lock.lock();
        try {
            while (!done){
                getCondition.await();
            }
            return response;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        lock.lock();
        try {
            if (!done){
                boolean success = getCondition.await(timeout, unit);
                if (!success){
                    throw new TimeoutException();
                }
            }
            return response;
        } finally {
            lock.unlock();
        }
    }
}
