package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.signIn.SignInQueryRequest;
import com.yupi.springbootinit.model.entity.SignIn;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.service.SignInService;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

/**
 * <p>
 * 签到 前端控制器
 * </p>
 *
 * @author 加棉
 * @since 2024-04-17
 */
@RestController
@RequestMapping("/signIn")
@Slf4j
public class SignInController {

    @Resource
    private UserService userService;
    @Resource
    private SignInService signInService;


    /**
     * 创建用户
     *
     * @param request
     * @return
     */
    //开启事务
    @Transactional
    @PostMapping("/add")
    public BaseResponse<Long> addSignIn(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        //要求登录
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //查看今日是否已经签到过
        //根据日期和用户Id查询
        LambdaQueryWrapper<SignIn> signInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        signInLambdaQueryWrapper.eq(SignIn::getUserId, loginUser.getId());
        signInLambdaQueryWrapper.eq(SignIn::getSignInDate, LocalDate.now());
        if (signInService.getOne(signInLambdaQueryWrapper) != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "今日已经签到过了");
        }
        //今日还没签到
        SignIn signIn = new SignIn();
        signIn.setUserId(loginUser.getId());
        signIn.setSignInDate(LocalDate.now());
        //保存签到记录
        boolean result = signInService.save(signIn);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        //增加用户调用AI的次数 +1
        userService.updateUserRemainderNum(loginUser.getId());
        return ResultUtils.success(signIn.getId());
    }


    /**
     *
     */
    @GetMapping("/get")
    public BaseResponse<SignIn> getSignInById(SignInQueryRequest signInQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(signInQueryRequest == null, ErrorCode.PARAMS_ERROR);
        SignIn signIn = signInService.getById(signInQueryRequest.getId());
        ThrowUtils.throwIf(signIn == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(signIn);
    }


    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteSignIn(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = signInService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }


    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param signInQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<SignIn>> listSignInByPage(@RequestBody SignInQueryRequest signInQueryRequest,
                                                       HttpServletRequest request) {
        long current = signInQueryRequest.getCurrentPage();
        long size = signInQueryRequest.getPageSize();
        Page<SignIn> signInPage = signInService.page(new Page<>(current, size),
                signInService.getQueryWrapper(signInQueryRequest));
        return ResultUtils.success(signInPage);
    }

}
