package io.choerodon.oauth.infra.common.util

import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.security.web.session.SessionInformationExpiredEvent
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@PrepareForTest([SessionInformationExpiredEvent.class])
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
class SingleLoginSessionInformationExpiredStrategySpec extends Specification {
    private SingleLoginSessionInformationExpiredStrategy strategy = new SingleLoginSessionInformationExpiredStrategy()

    def "OnExpiredSessionDetected"() {
        given: "参数准备"

        def response = Mock(HttpServletResponse)
        response.getWriter() >> { return Mock(PrintWriter) }
        def event = PowerMockito.mock(SessionInformationExpiredEvent.class)
        PowerMockito.when(event.getResponse()).thenReturn(response)

        when: "方法调用"
        strategy.onExpiredSessionDetected(event)

        then: "结果比对"
        noExceptionThrown()
    }
}
