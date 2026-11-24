package org.example.umc11th.global.config;

// 컨트롤러마다 반복되는 Swagger 문구와 응답 예시를 한곳에 모아둔다
public final class SwaggerDocs {

    private SwaggerDocs() {
    }

    public static final String JSON = "application/json";

    public static final String DESC_401 = "토큰 없음·위조·만료 (AUTH401_2 / AUTH401_3 / AUTH401_4)";

    public static final String EX_MEMBER_404 = """
            {"isSuccess": false, "code": "MEMBER404_1", "message": "해당 회원을 찾을 수 없습니다.", "result": null}""";

    public static final String EX_401 = """
            {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}""";
}
