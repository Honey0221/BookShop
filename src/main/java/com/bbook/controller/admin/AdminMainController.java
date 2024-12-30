package com.bbook.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminMainController {

	@GetMapping(value = "/dashboard")
	public String admin() {
		return "/admin/dashboard";
	}

}
