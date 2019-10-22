package io.choerodon.oauth.infra.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.core.exception.ExceptionResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wanghao
 * @Date 2019/10/22 9:55
 */
@Component
public class CustomerAccessDeniedHandler implements AccessDeniedHandler {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ExceptionResponse exceptionResponse = new ExceptionResponse(true, "403", accessDeniedException.getMessage());
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(mapper.writeValueAsString(exceptionResponse));
    }
}
