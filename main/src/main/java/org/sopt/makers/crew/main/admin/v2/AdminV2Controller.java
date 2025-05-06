package org.sopt.makers.crew.main.admin.v2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.service.AdminService;
import org.sopt.makers.crew.main.entity.property.Property;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/v2/${custom.paths.adminKey}")
@RequiredArgsConstructor
public class AdminV2Controller {

	private final AdminService adminService;
	private final ObjectMapper objectMapper;

	@Value("${custom.paths.adminKey}")
	private String adminKey;

	@GetMapping("/propertyPage")
	public ModelAndView propertyPage(ModelAndView model) throws JsonProcessingException {
		List<Property> allProperties = adminService.findAllProperties();

		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		Map<String, String> formattedJsonMap = new HashMap<>();
		for (Property property : allProperties) {
			if (property.getProperties() != null) {
				String prettyJson = objectMapper.writeValueAsString(property.getProperties());
				formattedJsonMap.put(property.getKey(), prettyJson);
			}
		}

		model.addObject("allProperties", allProperties);
		model.addObject("formattedJsonMap", formattedJsonMap);
		model.addObject("adminKey", adminKey);
		model.setViewName("propertyPage");

		return model;
	}

	@PostMapping("/property/update")
	public String updateProperty(@RequestParam String key, @RequestParam String propertyKey,
		@RequestParam Object propertyValue, RedirectAttributes redirectAttributes) {
		try {
			Property property = adminService.findPropertyByKey(key);
			property.getProperties().put(propertyKey, propertyValue);
			adminService.updateProperty(key, property.getProperties());
			redirectAttributes.addFlashAttribute("message", "Property가 성공적으로 업데이트되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Property 업데이트 중 오류가 발생했습니다: " + e.getMessage());
		}
		return getRedirectUrl();
	}

	@PostMapping("/property/update-json")
	public String updatePropertyJson(@RequestParam String key, @RequestParam String json,
		RedirectAttributes redirectAttributes) {
		try {
			Map<String, Object> updatedProperties = objectMapper.readValue(json,
				new TypeReference<>() {
				});
			adminService.updateProperty(key, updatedProperties);
			redirectAttributes.addFlashAttribute("message", "Property가 성공적으로 업데이트되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Property 업데이트 중 오류가 발생했습니다: " + e.getMessage());
		}
		return getRedirectUrl();
	}

	@PostMapping("/property/add")
	public String addProperty(@RequestParam String propertyKey, @RequestParam String propertyValue,
		RedirectAttributes redirectAttributes) {
		try {
			Map<String, Object> addProperties = objectMapper.readValue(propertyValue,
				new TypeReference<>() {
				});
			adminService.addProperty(propertyKey, addProperties);
			redirectAttributes.addFlashAttribute("message", "Property가 성공적으로 추가되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Property 추가 중 오류가 발생했습니다: " + e.getMessage());
		}
		return getRedirectUrl();
	}

	@PostMapping("/property/delete")
	public String deletePropertyWithDeleteMapping(@RequestParam String propertyKey,
		RedirectAttributes redirectAttributes) {
		try {
			adminService.deleteProperty(propertyKey);
			redirectAttributes.addFlashAttribute("message", "Property가 성공적으로 삭제되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Property 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
		return getRedirectUrl();
	}

	private String getRedirectUrl() {
		return "redirect:/admin/v2/" + adminKey + "/propertyPage";
	}
}
