package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.aspectj.weaver.patterns.HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotPasswordController {
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	Random random = new Random(1000);

	// open forget password email form
	@GetMapping("/forgot")
	public String forgotPasswordForm(Model m) {
		m.addAttribute("title", "Forgot Password");
		return "forgot_password";
	}

	// send otp handler
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, Model m, HttpSession session) {
		m.addAttribute("title", "Forgot Password");

		// generating random OTP
		int otp = this.random.nextInt(9999);
		System.out.println("OTP " + otp);

		// send otp to email

		String from = "javacodersakinaka@gmail.com";
		String to = email;
		String subject = "SCM OTP";
		String message = " " + "<div style='border:1px solid #e2e2e2;'> " + "<h1> " + " OTP for Password Reset is "
				+ "<b> " + otp + " </b>" + "</h1> " + "</div> ";
		boolean flag = this.emailService.sendEmail(from, to, message, subject);

		if (flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			session.setAttribute("message", new Message("We have send OTP to your Register Email", "alert-success"));
			return "verify_otp";
		} else {
			session.setAttribute("message", "Check your Email");
			return "forgot_password";
		}
	}

	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp") int otp, HttpSession session) {
		int myotp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		System.out.println(myotp);
		System.out.println(email);
		User user = this.userRepository.getUserByUserName(email);
		if (user == null) {

			session.setAttribute("message", "Kindly enter valid email. User deos not exist with this email");
			return "forgot_password";
		} else {

			if (myotp == otp) {
				return "change_password";
			} else {

				session.setAttribute("message", new Message("Kinldy Enter Correct OTP", "alert-danger"));
				return "verify_otp";
			}
		}

	}

	// change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpass") String newpass, @RequestParam("conpass") String conpass,
			HttpSession session) {
		if (newpass.equals(conpass)) {
			String email = (String) session.getAttribute("email");
			User user = this.userRepository.getUserByUserName(email);
			user.setPassword(this.bCryptPasswordEncoder.encode(newpass));
			userRepository.save(user);
			return "redirect:/login?change=Password Changed Successfully";
		}
		session.setAttribute("message", "New password and confirm password does not match");
		return "change_password";
	}
}
