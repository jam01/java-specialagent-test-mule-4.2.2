package io.opentracing.contrib.specialagent.test.mule;

import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.runtime.core.api.util.ClassUtils;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.service.http.impl.service.HttpServiceImplementation;
import org.mule.tck.SimpleUnitTestSupportSchedulerService;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.OK;

@RunWith(Parameterized.class)
public class HttpServiceTest extends AbstractMuleTestCase {
    private MockTracer mockTracer = new MockTracer();

    @Parameter
    public String serviceToLoad;
    private HttpServiceImplementation service;
    private SimpleUnitTestSupportSchedulerService schedulerService;
    private HttpClient client;


    public HttpServiceTest(String serviceToLoad) {
        this.serviceToLoad = serviceToLoad;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> params() {
        return Collections.singletonList(new String[]{HttpServiceImplementation.class.getName()});
    }

    @Before
    public void setup() throws Exception {
        GlobalTracer.registerIfAbsent(mockTracer);

        schedulerService = new SimpleUnitTestSupportSchedulerService();
        service = (HttpServiceImplementation) ClassUtils.instantiateClass(serviceToLoad, new Object[]{schedulerService},
                this.getClass().getClassLoader());
        service.start();

        client = service.getClientFactory().create(new HttpClientConfiguration.Builder()
                .setName("no-body-test")
                .build());
        client.start();
    }


    @After
    public void teardown() throws Exception {
        if (client != null) {
            client.stop();
        }


        if (service != null) {
            service.stop();
        }
        if (schedulerService != null) {
            schedulerService.stop();
        }
    }

    @Test
    public void httpServiceTest() throws Exception {
        HttpResponse response = client.send(HttpRequest.builder().uri("https://postman-echo.com/get").build());
        assertThat(response.getStatusCode(), is(OK.getStatusCode()));

        Assert.notEmpty(mockTracer.finishedSpans().stream()
                        .filter(mockSpan -> mockSpan.tags().containsKey("component"))
                        .filter(mockSpan -> mockSpan.tags().get("component").equals("java-grizzly-ahc"))
                        .collect(Collectors.toList()),
                "There should be one span from the AHC's TracingRequestFilter"
        );
    }
}
