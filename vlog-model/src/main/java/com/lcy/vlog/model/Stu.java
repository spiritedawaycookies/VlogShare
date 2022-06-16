package com.lcy.vlog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data //免得生成get set tostring
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Stu {

    private String name;
    private Integer age;

}
