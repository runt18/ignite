/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.rest.handlers.redis;

import java.nio.ByteBuffer;
import java.util.List;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.processors.rest.GridRestProtocolHandler;
import org.apache.ignite.internal.processors.rest.GridRestResponse;
import org.apache.ignite.internal.processors.rest.handlers.redis.exception.GridRedisTypeException;
import org.apache.ignite.internal.processors.rest.protocols.tcp.redis.GridRedisMessage;
import org.apache.ignite.internal.processors.rest.protocols.tcp.redis.GridRedisProtocolParser;
import org.apache.ignite.internal.processors.rest.request.GridRestRequest;
import org.apache.ignite.internal.util.future.GridFinishedFuture;
import org.apache.ignite.internal.util.typedef.CX1;

/**
 * Redis command handler done via REST.
 */
public abstract class GridRedisRestCommandHandler implements GridRedisCommandHandler {
    /** Logger. */
    protected final IgniteLogger log;

    /** REST protocol handler. */
    protected final GridRestProtocolHandler hnd;

    /**
     * Constructor.
     *
     * @param hnd REST protocol handler.
     */
    public GridRedisRestCommandHandler(final IgniteLogger log, final GridRestProtocolHandler hnd) {
        this.log = log;
        this.hnd = hnd;
    }

    /** {@inheritDoc} */
    @Override public IgniteInternalFuture<GridRedisMessage> handleAsync(final GridRedisMessage msg) {
        assert msg != null;

        try {
            return hnd.handleAsync(asRestRequest(msg))
                .chain(new CX1<IgniteInternalFuture<GridRestResponse>, GridRedisMessage>() {
                    @Override public GridRedisMessage applyx(IgniteInternalFuture<GridRestResponse> f)
                        throws IgniteCheckedException {
                        GridRestResponse restRes = f.get();

                        if (restRes.getSuccessStatus() == GridRestResponse.STATUS_SUCCESS)
                            msg.setResponse(makeResponse(restRes, msg.auxMKeys()));
                        else
                            msg.setResponse(GridRedisProtocolParser.toGenericError("Operation error"));

                        return msg;
                    }
                });
        }
        catch (IgniteCheckedException e) {
            if (e instanceof GridRedisTypeException)
                msg.setResponse(GridRedisProtocolParser.toTypeError(e.getMessage()));
            else
                msg.setResponse(GridRedisProtocolParser.toGenericError(e.getMessage()));

            return new GridFinishedFuture<>(msg);
        }
    }

    /**
     * Converts {@link GridRedisMessage} to {@link GridRestRequest}.
     *
     * @param msg {@link GridRedisMessage}
     * @return {@link GridRestRequest}
     * @throws IgniteCheckedException If fails.
     */
    public abstract GridRestRequest asRestRequest(GridRedisMessage msg) throws IgniteCheckedException;

    /**
     * Prepares a response according to the request.
     *
     * @param resp REST response.
     * @param params Auxiliary parameters.
     * @return Response for the command.
     */
    public abstract ByteBuffer makeResponse(GridRestResponse resp, List<String> params);
}
