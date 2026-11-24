package com.umc.movie.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// [선택 2] 컨트롤러 파라미터에 붙이면 AuthInterceptor가 토큰에서 꺼낸 로그인 회원 ID가 들어옵니다.
// 예) public ... create(@LoginMember Long memberId, ...)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {
}
