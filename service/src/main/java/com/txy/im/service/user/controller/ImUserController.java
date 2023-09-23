package com.txy.im.service.user.controller;

import com.txy.im.common.ClientType;
import com.txy.im.common.ResponseVO;
import com.txy.im.common.route.RouteHandle;
import com.txy.im.common.route.RouteInfo;
import com.txy.im.common.utils.RouteInfoParseUtil;
import com.txy.im.service.user.model.req.DeleteUserReq;
import com.txy.im.service.user.model.req.ImportUserReq;
import com.txy.im.service.user.model.req.LoginReq;
import com.txy.im.service.user.service.ImUserService;
import com.txy.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tianxueyang
 * @version 1.0
 * @description
 * @date 2023/8/30 9:55
 */

@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    private ZKit zKit;
    @Autowired
    ImUserService imUserService;

    @Autowired
    RouteHandle routeHandle;

    @RequestMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appid) {
      //  req.setAppId(appid);
        return imUserService.importUser(req);
    }


    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description im的登录接口，返回im地址
     * @author chackylee
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);

        ResponseVO login = imUserService.login(req);
        if (login.isOk()) { //登陆成功

            // 获取一个Im的地址
            List<String> allNode = new ArrayList<>();
            if (req.getClientType() == ClientType.WEB.getCode()) {
                allNode = zKit.getAllWebNode();
            } else {
                allNode = zKit.getAllTcpNode();
            }
            String s = routeHandle.routeServer(allNode, req
                    .getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }

        return ResponseVO.errorResponse();
    }
}
