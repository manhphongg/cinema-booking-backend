package vn.cineshow.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData<T> {
    public int status;
    public String message;
    public T data;

    //PUT, PATH, DELETE
    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }

    //GET, POST
    public ResponseData(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
