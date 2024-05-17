package com.yupi.springbootinit.model.dto.signIn;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * chart图表查询参数
 */
@Data
public class SignInQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 8878326647736641880L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 图表类型
     */
    private LocalDate signInDate;
}
