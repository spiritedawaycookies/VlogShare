package com.lcy.vlog.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogBO {
    private String id;
    @NotNull(message = "userid cannot be null")
    private String vlogerId;
    @NotNull(message = "url cannot be null")
    private String url;
    @NotNull(message = "cover cannot be null")
    private String cover;
    @NotBlank(message = "title cannot be blank")
    private String title;
    private Integer width;
    private Integer height;
    private Integer likeCounts;
    private Integer commentsCounts;
}