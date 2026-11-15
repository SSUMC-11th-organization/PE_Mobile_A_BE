package com.umc.movie.auth;

import com.umc.movie.exception.BusinessException;
import com.umc.movie.exception.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// @LoginMember Long 파라미터에 인터셉터가 담아 둔 memberId를 넣어 줍니다.
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object memberId = webRequest.getAttribute(AuthInterceptor.MEMBER_ID, RequestAttributes.SCOPE_REQUEST);
        // 인터셉터 예외 경로(공개 API)에 @LoginMember를 잘못 붙인 경우에도 조용히 null이 들어가지 않게 막습니다.
        if (memberId == null) {
            throw new BusinessException(ErrorCode.TOKEN_MISSING);
        }
        return memberId;
    }
}
