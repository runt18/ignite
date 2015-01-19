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

package org.gridgain.grid.kernal.managers.failover;

import org.apache.ignite.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.compute.*;
import org.apache.ignite.spi.failover.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.kernal.managers.*;

import java.util.*;

/**
 * Grid failover spi manager.
 */
public class GridFailoverManager extends GridManagerAdapter<FailoverSpi> {
    /**
     * @param ctx Kernal context.
     */
    public GridFailoverManager(GridKernalContext ctx) {
        super(ctx, ctx.config().getFailoverSpi());
    }

    /** {@inheritDoc} */
    @Override public void start() throws IgniteCheckedException {
        startSpi();

        if (log.isDebugEnabled())
            log.debug(startInfo());
    }

    /** {@inheritDoc} */
    @Override public void stop(boolean cancel) throws IgniteCheckedException {
        stopSpi();

        if (log.isDebugEnabled())
            log.debug(stopInfo());
    }

    /**
     * @param taskSes Task session.
     * @param jobRes Job result.
     * @param top Collection of all top nodes that does not include the failed node.
     * @return New node to route this job to.
     */
    public ClusterNode failover(GridTaskSessionImpl taskSes, ComputeJobResult jobRes, List<ClusterNode> top) {
        return getSpi(taskSes.getFailoverSpi()).failover(new GridFailoverContextImpl(taskSes, jobRes,
            ctx.loadBalancing()), top);
    }
}
