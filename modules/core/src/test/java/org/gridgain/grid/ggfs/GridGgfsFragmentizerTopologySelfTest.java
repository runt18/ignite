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

package org.gridgain.grid.ggfs;

import org.apache.ignite.*;
import org.apache.ignite.fs.*;

/**
 * Tests coordinator transfer from one node to other.
 */
public class GridGgfsFragmentizerTopologySelfTest extends GridGgfsFragmentizerAbstractSelfTest {
    /**
     * @throws Exception If failed.
     */
    public void testCoordinatorLeave() throws Exception {
        stopGrid(0);

        // Now node 1 should be coordinator.
        try {
            IgniteFsPath path = new IgniteFsPath("/someFile");

            IgniteFs ggfs = grid(1).fileSystem("ggfs");

            try (IgniteFsOutputStream out = ggfs.create(path, true)) {
                for (int i = 0; i < 10 * GGFS_GROUP_SIZE; i++)
                    out.write(new byte[GGFS_BLOCK_SIZE]);
            }

            awaitFileFragmenting(1, path);
        }
        finally {
            startGrid(0);
        }
    }
}
