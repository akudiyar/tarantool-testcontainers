package org.testcontainers.containers;


import io.tarantool.driver.TarantoolVersion;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolClientFactory;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Oleg Kuznetsov
 */
public class TarantoolSdkContainerTest {

    @Test
    public void test_should_createTarantoolContainerFromSdk() {
        try (final TarantoolContainer tarantoolContainer = new TarantoolContainer(
                new TarantoolImageParams("tarantool-enterprise-bundle-2.8.3-21-g7d35cd2be-r470")
        )) {
            tarantoolContainer.start();

            final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                    TarantoolClientFactory.createClient()
                            .withCredentials("api_user", "secret")
                            .withAddress(tarantoolContainer.getHost(), tarantoolContainer.getMappedPort(3301))
                            .build();

            final List<?> result = client.eval("return 'test'").join();
            final TarantoolVersion version = client.getVersion();

            assertEquals("test", result.get(0));
            assertTrue(version.toString().startsWith("Tarantool 2.8.3 (Binary)"));
        }
    }

    @Test
    public void test_should_createTarantoolContainerFromSdk_ifDockerfileSpecified() throws URISyntaxException {
        final File dockerfile = new File(
                TarantoolSdkContainerTest.class.getClassLoader().getResource("testsdk/Dockerfile").toURI()
        );

        try (final TarantoolContainer tarantoolContainer = new TarantoolContainer(
                new TarantoolImageParams("testsdk", dockerfile))
                .withDirectoryBinding("testsdk")) {

            tarantoolContainer.start();

            final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client =
                    TarantoolClientFactory.createClient()
                            .withCredentials("api_user", "secret")
                            .withAddress(tarantoolContainer.getHost(), tarantoolContainer.getMappedPort(3301))
                            .build();

            final List<?> result = client.eval("return 'test'").join();
            final TarantoolVersion version = client.getVersion();

            assertEquals("test", result.get(0));
            assertTrue(version.toString().startsWith("Tarantool 2.7.3 (Binary)"));
        }
    }
}
