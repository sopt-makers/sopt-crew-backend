package org.sopt.makers.crew.main.common.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.sopt.makers.crew.main.common.response.CommonResponseDto;
import org.sopt.makers.crew.main.common.response.ErrorStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {
    setResponse(response, ErrorStatus.UNAUTHORIZED_TOKEN);
  }


  public void setResponse(HttpServletResponse response, ErrorStatus status) throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    CommonResponseDto apiResponse = CommonResponseDto.fail(status.getErrorCode());
    response.getWriter().println(mapper.writeValueAsString(apiResponse));
  }

}
