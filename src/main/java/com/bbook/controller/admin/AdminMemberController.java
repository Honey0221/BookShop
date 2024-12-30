package com.bbook.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

	@GetMapping(value = "/memberMng")
	public String memberManage() {
		return "/admin/members/memberMng";
	}

	@GetMapping(value = "/memberStat")
	public String memberStatistic() {
		return "/admin/members/memberStat";
	}

}
