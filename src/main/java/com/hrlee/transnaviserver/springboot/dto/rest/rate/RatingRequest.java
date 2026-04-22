package com.hrlee.transnaviserver.springboot.dto.rest.rate;

import com.hrlee.transnaviserver.springboot.CorruptAble;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class RatingRequest implements CorruptAble {
    private final int grade;
    private final String comment;

    @Override
    public boolean isCorrupt() { return comment == null || comment.length() > 500 || grade < 0 || grade > 5; }
}
