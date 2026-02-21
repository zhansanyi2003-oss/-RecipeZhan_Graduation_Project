package org.zhan.recipe_backend.common;

import lombok.Data;

@Data
public class Result {
    private Integer code ;
    private String msg ;
    private Object data ;
    public static Result Success() {
        Result result = new Result();
        result.code=1;
        result.msg="success";
        return result;

    }
    public static Result Success( Object data) {
        Result result = new Result();
        result.code=1;
        result.msg="success";
        result.data = data;
        return result;

    }
    public static Result Error(String message) {
        Result result = new Result();
        result.code=0;
        result.msg=message;
        return result;

    }



}
