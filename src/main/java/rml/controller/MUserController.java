package rml.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import rml.listener.ConfigListener;
import rml.model.MUser;
import rml.service.MUserServiceI;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/muserController")
public class MUserController {

	private MUserServiceI muserService;

	public MUserServiceI getMuserService() {
		return muserService;
	}

	@Autowired
	public void setMuserService(MUserServiceI muserService) {
		this.muserService = muserService;
	}


	@RequestMapping(value="/getConfig")
	@ResponseBody
	public JSONObject getConfig() {
		JSONObject result = new JSONObject();
		result.put("redis.host",ConfigListener.getProperty("redis.host"));
		result.put("jdbc.driver",ConfigListener.getProperty("jdbc.driver"));
		return result;
	}

	@RequestMapping(value="/listUser")
	public String listUser(HttpServletRequest request) {
		List <MUser> list = muserService.getAll();
		request.setAttribute("userlist", list);
		return "listUser";
	}

	@RequestMapping(value="/addUser")
	public String addUser(MUser muser) {

		String id = UUID.randomUUID().toString();
		muser.setId(id);
		muserService.insert(muser);
		return "redirect:/muserController/listUser.do";
	}

	@RequestMapping(value="/deleteUser")
	public String deleteUser(String id) {

		muserService.delete(id);
		return "redirect:/muserController/listUser.do";
	}

	@RequestMapping(value="/updateUserUI")
	public String updateUserUI(String id, HttpServletRequest request) {

		MUser muser = muserService.selectByPrimaryKey(id);
		request.setAttribute("user", muser);
		return "updateUser";
	}

	@RequestMapping(value="/updateUser")
	public String updateUser(MUser muser) {

		muserService.update(muser);
		return "redirect:/muserController/listUser.do";
	}
}
