package com.bbook.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

	@GetMapping(value = "/orderMng")
	public String orderManage() {
		return "/admin/orders/orderMng";
	}

	@GetMapping(value = "/orderStat")
	public String orderStatistic() {
		return "/admin/orders/orderStat";
	}

}
