/*
 *
 * Copyright 2013 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.netflix.http4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Test;

import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.config.ConfigurationManager;

public class NamedConnectionPoolTest {
    @Test
    public void testConnectionPoolCounters() throws Exception {
        // LogManager.getRootLogger().setLevel((Level)Level.DEBUG);
        NFHttpClient client = NFHttpClientFactory.getNamedNFHttpClient("google-NamedConnectionPoolTest");
        assertTrue(client.getConnectionManager() instanceof MonitoredConnectionManager);
        MonitoredConnectionManager connectionPoolManager = (MonitoredConnectionManager) client.getConnectionManager(); 
        connectionPoolManager.setDefaultMaxPerRoute(100);
        connectionPoolManager.setMaxTotal(200);
        assertTrue(connectionPoolManager.getConnectionPool() instanceof NamedConnectionPool);
        NamedConnectionPool connectionPool = (NamedConnectionPool) connectionPoolManager.getConnectionPool(); 
        System.out.println("Entries created: " + connectionPool.getCreatedEntryCount());
        System.out.println("Requests count: " + connectionPool.getRequestsCount());
        System.out.println("Free entries: " + connectionPool.getFreeEntryCount());
        System.out.println("Deleted :" + connectionPool.getDeleteCount());
        System.out.println("Released: " + connectionPool.getReleaseCount());
        for (int i = 0; i < 10; i++) {
            HttpUriRequest request = new HttpGet("http://www.google.com/");
            HttpResponse response = client.execute(request);
            EntityUtils.consume(response.getEntity());
            assertEquals(200, response.getStatusLine().getStatusCode());
            Thread.sleep(500);
        }
        System.out.println("Entries created: " + connectionPool.getCreatedEntryCount());
        System.out.println("Requests count: " + connectionPool.getRequestsCount());
        System.out.println("Free entries: " + connectionPool.getFreeEntryCount());
        System.out.println("Deleted :" + connectionPool.getDeleteCount());
        System.out.println("Released: " + connectionPool.getReleaseCount());
        assertTrue(connectionPool.getCreatedEntryCount() >= 1);
        assertTrue(connectionPool.getRequestsCount() >= 10);
        assertTrue(connectionPool.getFreeEntryCount() >= 9);
        assertEquals(0, connectionPool.getDeleteCount());
        assertEquals(connectionPool.getReleaseCount(), connectionPool.getRequestsCount());
        assertEquals(connectionPool.getRequestsCount(), connectionPool.getCreatedEntryCount() + connectionPool.getFreeEntryCount());
        ConfigurationManager.getConfigInstance().setProperty("google-NamedConnectionPoolTest.ribbon." + CommonClientConfigKey.MaxTotalHttpConnections.key(), "50");
        ConfigurationManager.getConfigInstance().setProperty("google-NamedConnectionPoolTest.ribbon." + CommonClientConfigKey.MaxHttpConnectionsPerHost.key(), "10");
        assertEquals(50, connectionPoolManager.getMaxTotal());
        assertEquals(10, connectionPoolManager.getDefaultMaxPerRoute());

    }

}