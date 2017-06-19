/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.metrics;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @since Mar 15, 2017
 * @see 
 */
public class TestMetricsThread {

    MetricsThread oMetricsThread = null;

    @Before
    public void setUp() throws Exception {
        oMetricsThread = new MetricsThread();
    }

    @After
    public void tearDown() throws Exception {
        oMetricsThread.shutdown();
        oMetricsThread = null;
    }

    @Test
    public void test() {
        oMetricsThread.start();
        Assert.assertEquals(true, oMetricsThread.isRunning());
        oMetricsThread.shutdown();
        Assert.assertEquals(false, oMetricsThread.isRunning());

    }

}
