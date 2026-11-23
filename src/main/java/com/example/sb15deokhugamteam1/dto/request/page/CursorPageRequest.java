package com.example.sb15deokhugamteam1.dto.request.page;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CursorPageRequest {

    private String cursor;

    @Min(1)
    @Max(100)
    private int limit = 20;


}
