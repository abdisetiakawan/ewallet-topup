package com.berijalan.ewallet.config;

import com.berijalan.ewallet.logging.LoggingSupport;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class MdcFilterTest {

    private final MdcFilter mdcFilter = new MdcFilter();

    @Test
    void shouldPopulateRequestMetadataDuringFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/wallet/topup");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            assertThat(MDC.get(LoggingSupport.REQUEST_ID)).startsWith("req-");
            assertThat(MDC.get(LoggingSupport.HTTP_METHOD)).isEqualTo("POST");
            assertThat(MDC.get(LoggingSupport.REQUEST_PATH)).isEqualTo("/api/wallet/topup");
        };

        mdcFilter.doFilter(request, response, filterChain);

        assertThat(MDC.getCopyOfContextMap()).isEmpty();
        assertThat(response.getHeader(LoggingSupport.REQUEST_ID_HEADER)).startsWith("req-");
    }

    @Test
    void shouldReuseIncomingRequestIdHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        request.addHeader(LoggingSupport.REQUEST_ID_HEADER, "incoming-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            assertThat(MDC.get(LoggingSupport.REQUEST_ID)).isEqualTo("incoming-123");
        };

        mdcFilter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(LoggingSupport.REQUEST_ID_HEADER)).isEqualTo("incoming-123");
    }
}
