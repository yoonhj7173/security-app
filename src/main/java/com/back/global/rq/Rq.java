package com.back.global.rq;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
// @RequestScope - 각 요청마다 새로운 Rq 객체가 생성되고, 요청이 끝나면 소멸됨
public class Rq {

    // client가 보낸 모든 정보가 담겨있음. Spring이 자동으로 해줌
    private final HttpServletRequest request;
    private final MemberService memberService;
    private final HttpServletResponse response;

    public void addCookie(String name, String value) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");

        response.addCookie(
                cookie
        );
    }

    public void deleteCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    public Member getActor() {

        String authorizationHeader = request.getHeader("Authorization");

        String apiKey = null;

        // 헤더 방식 vs 쿠키 방식
        if (authorizationHeader != null) {
            // throw new ServiceException("401-1", "인증 정보가 헤더에 존재하지 않습니다.");
            if (!authorizationHeader.startsWith("Bearer ")) {
                throw new ServiceException("401-2", "잘못된 형식의 인증 데이터입니다.");
            }
            apiKey = authorizationHeader.replace("Bearer ", "");
        } else {
            apiKey = request.getCookies() == null ? ""
                    : Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals("apiKey"))
                    .map(cookie -> cookie.getValue())
                    .findFirst()
                    .orElse("");

        }

        if (apiKey.isBlank()) {
            throw new ServiceException("401-3", "인증 정보가 존재하지 않습니다.");
        }

        return memberService.findByApiKey(apiKey).orElseThrow(
                () -> new ServiceException("401-1", "유효하지 않은 API 키입니다.")
        );
    }
}
