package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	 private BCryptPasswordEncoder bCryptPasswordEncoder;

	// method for adding common data to load user
	@ModelAttribute
	public void addCommon(Model m, Principal principal) {
		String name = principal.getName();
		// get the user from database
		User user = this.userRepository.getUserByUserName(name);
//		 User user = this.userRepository.findByEmail(name); 
		System.out.println(user.getName());
		m.addAttribute("user", user);
	}

	// to show user dashboard
	@RequestMapping("/dashboard")
	public String dashboard(Model m, Principal principal) {
		m.addAttribute("title", "User Dashboard");
		return "/normal/user_dashboard";
	}

	// show add contact
	@GetMapping("/addcontact")
	public String addContact(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "/normal/add_contact";
	}

	// process add contact form
	@PostMapping("/process-contact")
	public String savecontact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		// System.out.println(contact);
		// fetching current user details
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// fetch image file
			if (file.isEmpty()) {
				// throw new Exception("File is empty");
				contact.setImageUrl("user.png");
			} else {

				contact.setImageUrl(file.getOriginalFilename());
				String absolutePath = new ClassPathResource("static/img").getFile().getAbsolutePath();
				Path path = Paths.get(absolutePath + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image upload");
			}
			// saving contact and user
			contact.setUser(user);
			user.getContacts().add(contact);
			userRepository.save(user);
			session.setAttribute("message", new Message("Your Contact Save Successfully!! Add more ", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong!! " + e.getMessage(), "alert-danger"));
		}
		return "/normal/add_contact";
	}

	// show contact Handler
	// for pagination
	// current page =n[page]
	// content per page= n[4]
	@GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "View Contact");
		// Fetch Contact
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		// create Pageable object

		PageRequest pageable = PageRequest.of(page, 4, Sort.by("name"));

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		// Collections.sort(contacts,new NameComparator());
		if (!contacts.isEmpty()) {
			m.addAttribute("contact", contacts);
			m.addAttribute("currentPage", page);
			m.addAttribute("totalPage", contacts.getTotalPages());
		}
		return "/normal/show_contact";
	}

	// processing particular Contact details
	@GetMapping("/{id}/contact")
	public String showParticularConatct(@PathVariable("id") Integer id, Model m, Principal principal) {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		Optional<Contact> optional = this.contactRepository.findById(id);
		Contact contact = optional.get();

		if (user.getId() == contact.getUser().getId()) {
			m.addAttribute("title", contact.getName());
			m.addAttribute("contact", contact);
		}

		return "/normal/contact_details";
	}

	// delete contact Handler
	@GetMapping("/delete-contact/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model m, Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			Contact contact = this.contactRepository.findById(cid).get();
			if (user.getId() == contact.getUser().getId()) {
				// delete file(image) from target folder
				String Path = new ClassPathResource("static/img").getFile().getAbsolutePath();
				if (!contact.getImageUrl().equals("user.png")) {
					File file = new File(Path + File.separator + contact.getImageUrl());
					if (file.exists()) {
						file.delete();
						System.out.println("deleted...");
					}
				}
				// delete contact from database
				this.contactRepository.delete(contact);
				System.out.println("deleted...");
				session.setAttribute("message", new Message("Contact Deleted Suceesfully!!", "alert-success"));
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		return "redirect:/user/show-contact/0"; // here we need to give url of mapping not view name
	}

	// show update form
	@GetMapping("/update/{cid}")
	public String showUpdate(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Conatct");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// process update Handler

	@PostMapping("/process-update")
	public String processUpdate(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, Model m, HttpSession session, Principal principal) {
		m.addAttribute("title", "Update Contact");
		try {
			User user = this.userRepository.getUserByUserName(principal.getName());
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();
			String oldImage = oldContact.getImageUrl();
			System.out.println(contact.getCid());

			// if updated by same user for his contact
			if (user.getId() == oldContact.getUser().getId()) {

				// if new image is upload
				if (!file.isEmpty()) {
					// save new image
					String path = new ClassPathResource("static/img").getFile().getAbsolutePath();
					Path path2 = Paths.get(path + File.separator + file.getOriginalFilename());
					Files.copy(file.getInputStream(), path2, StandardCopyOption.REPLACE_EXISTING);
					contact.setImageUrl(file.getOriginalFilename());

					// delete old image
					if (!oldImage.equals("user.png")) {
						File f = new File(path + File.separator + oldImage);
						if (f.exists())
							f.delete();
						System.out.println("deleted...");
					}
				} else {

					contact.setImageUrl(oldImage);
				}
				contact.setUser(user);
				this.contactRepository.save(contact);
				session.setAttribute("message", new Message("Contact Update Sucessfully!!", "alert-success"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/" + contact.getCid() + "/contact";
	}

	// Profile Handler
	@GetMapping("/profile")
	public String profile(Model m) {
		m.addAttribute("titile", "Profile Page");
		return "normal/profile";
	}

	// open setting
	@GetMapping("/setting")
	public String openSetting(Model m) {
		m.addAttribute("title", "Settings");
		return "normal/setting";
	}

	// process change Password
	@PostMapping("/change-setting")
	public String changePassword(@RequestParam("oldpass") String oldPassword,
			@RequestParam("newpass") String newPassword, Principal principal, HttpSession session) {
		System.out.println(oldPassword);
		System.out.println(newPassword);
		User currentUser = this.userRepository.getUserByUserName(principal.getName());
		String encodedPassword = currentUser.getPassword();
		if(this.bCryptPasswordEncoder.matches(oldPassword, encodedPassword)) {
			//change the password..
		  currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		  this.userRepository.save(currentUser);
		  session.setAttribute("message", new Message("Password Change Successfully","alert-success"));
		}else {
			//don't change the password.
			session.setAttribute("message", new Message("kindly Enter Correct Old Password","alert-danger"));
			return "redirect:/user/setting";
		}		
		return "redirect:/user/dashboard";
	}
}