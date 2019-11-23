package io.opentracing.contrib.specialagent.test.mule;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.core.api.event.CoreEvent;

public class MuleITest extends MuleArtifactFunctionalTestCase {
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
        System.out.println(result.getMessage().getPayload().getValue());
    }
}
