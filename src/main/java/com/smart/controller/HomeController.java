package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder bEncoder;
	@Autowired
	private UserRepository userRepository;

	// Home Handler
	@RequestMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home-SmartContact Manager");
		return "home";
	}

	// Signup Handler
	@RequestMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Signup-SmartContact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}

	// Registration Handler
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model m,
			HttpSession session) {
		m.addAttribute("title", "Register-SmartContact Manager");
		try {

			if (result.hasErrors()) {
				System.out.println("Errors" + result.toString());
				m.addAttribute("user", user);
				return "signup";
			}

			if (!agreement) {
				System.out.println("You have not agreed the term and conditions");
				throw new Exception("You have not agreed the term and conditions");
			}
			user.setRole("ROLE_USER");
			user.setEnabaled(true);
			user.setImageUrl("default.png");
			user.setPassword(bEncoder.encode(user.getPassword()));
			
			System.out.println(agreement);
			System.out.println(user);
			User save = userRepository.save(user);
			System.out.println(save);
			m.addAttribute("user", new User());
			session.setAttribute("message", new Message("Succesfully Registered!!", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went Wrong !!" + e.getMessage(), "alert-danger"));
		}
		return "signup";
	}
	
	@RequestMapping("/login")
	public String signin(Model m) {
		m.addAttribute("title","Login-SmartContact Manager");
		return "login";
	}
}
