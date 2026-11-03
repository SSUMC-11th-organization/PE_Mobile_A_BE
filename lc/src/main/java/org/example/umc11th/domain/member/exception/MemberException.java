package org.example.umc11th.domain.member.exception;

import org.example.umc11th.global.exception.GeneralException;

public class MemberException extends GeneralException {

    public MemberException(MemberErrorCode code) {
        super(code);
    }
}
