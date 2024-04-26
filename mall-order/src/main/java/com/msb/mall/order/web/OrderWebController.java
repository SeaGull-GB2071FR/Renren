package com.msb.mall.order.web;

import com.msb.common.exception.NoStockExecption;
import com.msb.mall.order.service.OmsOrderService;
import com.msb.mall.order.vo.OrderConfirmVo;
import com.msb.mall.order.vo.OrderResponseVO;
import com.msb.mall.order.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OrderWebController {

    @Autowired
    private OmsOrderService omsOrderService;


    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        // TODO 查询订单确认页需要的信息
        OrderConfirmVo confirmVo = omsOrderService.confirmOrder();
        model.addAttribute("confirmVo", confirmVo);
        return "confirm";
    }

    @PostMapping("orderSubmit")
    public String orderSubmit(OrderSubmitVO vo) {
        Integer code = 0;
        OrderResponseVO responseVO = null;
        try{
            responseVO = omsOrderService.submitOrder(vo);
            code = responseVO.getCode();
        }catch (NoStockExecption e){
            code = 2;
        }
        //  验证是否 重复提交

        //  下订单
        //  跳转刀支付页面

        return "";
    }

}
