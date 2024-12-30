package com.bbook.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/items")
public class AdminItemController {

	@GetMapping(value = "/itemMng")
	public String itemManage() {
		return "/admin/items/itemMng";
	}

	@GetMapping(value = "/itemStat")
	public String itemStatistic() {
		return "/admin/items/itemStat";
	}

}
