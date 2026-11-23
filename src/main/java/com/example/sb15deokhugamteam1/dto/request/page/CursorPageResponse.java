package com.example.sb15deokhugamteam1.dto.request.page;


import lombok.Getter;

import java.util.List;

@Getter
public class CursorPageResponse<T> {

    private final List<T> content;
    private final String nextCursor;
    private final boolean hasNext;

    public CursorPageResponse(
            List<T> content,
            String nextCursor,
            boolean hasNext
    ) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }

}
