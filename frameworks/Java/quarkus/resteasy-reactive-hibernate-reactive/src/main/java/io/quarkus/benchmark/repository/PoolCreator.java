package io.quarkus.benchmark.repository;

import io.netty.util.concurrent.FastThreadLocal;
import io.quarkus.reactive.pg.client.PgPoolCreator;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.ContextInternal;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.SqlConnectionInternal;
import io.vertx.sqlclient.impl.pool.SqlConnectionPool.PooledConnection;
import jakarta.inject.Singleton;

import java.util.function.Function;

@Singleton
public class PoolCreator implements PgPoolCreator {

    private static FastThreadLocal<PoolMetric> POOL_METRICS = new FastThreadLocal<>() {
        @Override
        protected PoolMetric initialValue() {
            return new PoolMetric();
        }

        @Override
        protected void onRemoval(PoolMetric poolMetric) {
            poolMetric.cancelTimer();
        }
    };

    @Override
    public PgPool create(Input input) {
        PgPool delegate = PgPool.pool(input.vertx(), input.pgConnectOptionsList(), input.poolOptions());
        return new MonitoredPool(delegate);
    }

    private static class MonitoredPool implements PgPool {

        final PgPool delegate;

        MonitoredPool(PgPool delegate) {
            this.delegate = delegate;
        }

        @Override
        public void getConnection(Handler<AsyncResult<SqlConnection>> handler) {
            getConnection().onComplete(handler);
        }

        @Override
        public Future<SqlConnection> getConnection() {
            return delegate.getConnection().onSuccess(MonitoredPool::updateMetric);
        }

        private static void updateMetric(SqlConnection conn) {
            SqlConnectionInternal connectionInternal = (SqlConnectionInternal) conn;
            PooledConnection pooledConnection = (PooledConnection) connectionInternal.unwrap();
            SocketConnectionBase socketConnection = (SocketConnectionBase) pooledConnection.unwrap();
            ContextInternal connectionCtx = (ContextInternal) socketConnection.context();

            PoolMetric poolMetric = POOL_METRICS.get();
            poolMetric.update(connectionCtx);
        }

        @Override
        public Query<RowSet<Row>> query(String s) {
            return delegate.query(s);
        }

        @Override
        public PreparedQuery<RowSet<Row>> preparedQuery(String s) {
            return delegate.preparedQuery(s);
        }

        @Override
        public PreparedQuery<RowSet<Row>> preparedQuery(String s, PrepareOptions prepareOptions) {
            return delegate.preparedQuery(s, prepareOptions);
        }

        @Override
        public void close(Handler<AsyncResult<Void>> handler) {
            delegate.close(handler);
        }

        @Override
        public Future<Void> close() {
            return delegate.close();
        }

        @Override
        public PgPool connectHandler(Handler<SqlConnection> handler) {
            return delegate.connectHandler(handler);
        }

        @Override
        public PgPool connectionProvider(Function<Context, Future<SqlConnection>> function) {
            return delegate.connectionProvider(function);
        }

        @Override
        public int size() {
            return delegate.size();
        }
    }

    private static class PoolMetric implements Handler<Long> {
        final ContextInternal context;
        final long timerId;
        long total;
        long sameEventloop;

        PoolMetric() {
            context = ContextInternal.current();
            timerId = context.setPeriodic(5000, this);
        }

        void update(ContextInternal other) {
            total++;
            sameEventloop += (context.nettyEventLoop() == other.nettyEventLoop()) ? 1 : 0;
        }

        @Override
        public void handle(Long event) {
            System.out.println("Ratio for event loop " + context.nettyEventLoop() + " is " + ((double) sameEventloop / total));
        }

        void cancelTimer() {
            context.owner().cancelTimer(timerId);
        }
    }
}