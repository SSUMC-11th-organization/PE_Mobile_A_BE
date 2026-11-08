package org.example.umc11th.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.example.umc11th.domain.auth.exception.AuthErrorCode;
import org.example.umc11th.domain.auth.exception.AuthException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// @LoginMember Long 파라미터에 인터셉터가 요청에 담아둔 memberId를 넣어준다.
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Long resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Object memberId = request == null ? null : request.getAttribute(AuthInterceptor.MEMBER_ID);

        // 인터셉터 예외 경로에 @LoginMember를 붙이면 값이 없다. 이때도 로그인이 필요한 요청으로 응답한다.
        if (!(memberId instanceof Long id)) {
            throw new AuthException(AuthErrorCode.TOKEN_MISSING);
        }
        return id;
    }
}
