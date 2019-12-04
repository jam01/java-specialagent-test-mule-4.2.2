package io.opentracing.contrib.specialagent.test.mule;

import io.opentracing.mock.MockTracer;
import io.opentracing.util.GlobalTracer;
import org.junit.Before;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.core.api.event.CoreEvent;
import org.springframework.util.Assert;

import java.util.stream.Collectors;

public class MuleITest extends MuleArtifactFunctionalTestCase {
    private MockTracer mockTracer = new MockTracer();

    @Before
    public void setup(){
        GlobalTracer.registerIfAbsent(mockTracer);
    }

    @Override
    public int getTestTimeoutSecs() {
        return 3000;
    }

    @Override
    protected boolean isFailOnTimeout() {
        return false;
    }

    @Override
    protected String getConfigFile() {
        return "test-config.xml";
    }

    @Test
    public void httpRequesterTest() throws Exception {
        CoreEvent result = runFlow("test-flow");

        Assert.notEmpty(mockTracer.finishedSpans().stream()
                        .filter(mockSpan -> mockSpan.tags().containsKey("component"))
                        .filter(mockSpan -> mockSpan.tags().get("component").equals("java-grizzly-ahc"))
                        .collect(Collectors.toList()),
                "There should be one span from the AHC's TracingRequestFilter"
        );
    }
}
