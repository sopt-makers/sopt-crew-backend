package org.sopt.makers.crew.main.admin.v2;

import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.dto.AdvertisementImageUploadResponse;
import org.sopt.makers.crew.main.admin.v2.dto.AdvertisementMeetingTopUpdateRequest;
import org.sopt.makers.crew.main.admin.v2.dto.AdvertisementMeetingTopUpdateResponse;
import org.sopt.makers.crew.main.admin.v2.dto.AdminPagePresenter;
import org.sopt.makers.crew.main.advertisement.service.AdvertisementService;
import org.sopt.makers.crew.main.admin.v2.service.AdminFacade;
import org.sopt.makers.crew.main.admin.v2.service.AdminKeyProvider;
import org.sopt.makers.crew.main.admin.v2.service.AdminService;
import org.sopt.makers.crew.main.admin.v2.service.JsonPrettierService;
import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.external.s3.service.S3Service;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/v2/${custom.paths.adminKey}")
@RequiredArgsConstructor
public class AdminV2Controller {

	private final AdminService adminService;
	private final JsonPrettierService jsonPrettierService;
	private final AdminKeyProvider adminKeyProvider;
	private final AdminFacade adminFacade;
	private final AdvertisementService advertisementService;
	private final S3Service s3Service;

	/**
	 * propertyPage 조회
	 *
	 * @return propertyPage로 이동
	 */
	@GetMapping("/propertyPage")
	public ModelAndView propertyPage() {

		AdminPagePresenter mainPagePresenter = adminFacade.madeViewData();

		ModelAndView model = new ModelAndView("propertyPage");

		model.addObject("allProperties", mainPagePresenter.properties());
		model.addObject("formattedJsonMap", mainPagePresenter.formattedJson());
		model.addObject("adminKey", adminKeyProvider.getAdminKey());
		model.addObject("meetingTopAdvertisements", advertisementService.getMeetingTopAdvertisementsForAdmin());

		return model;
	}

	/**
	 * property 수정
	 *
	 * @param key                : 프로퍼티 키 값
	 * @param json               :  프로퍼티 json 값
	 * @param redirectAttributes : redirect 시 메시지를 보여주기 위한 값
	 * @return propertyPage로 이동
	 */
	@PostMapping("/property/update")
	public String updatePropertyJson(@RequestParam String key, @RequestParam String json,
		RedirectAttributes redirectAttributes) {
		try {
			Map<String, Object> updatedProperties = jsonPrettierService.readValue(json);

			adminService.updateProperty(key, updatedProperties);
			redirectAttributes.addFlashAttribute("message", "Property가 성공적으로 업데이트되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Property 업데이트 중 오류가 발생했습니다: " + e.getMessage());
		}
		return getRedirectUrl();
	}

	/**
	 * property 추가
	 *
	 * @param key                : 프로퍼티 키 값
	 * @param json               :  프로퍼티 json 값
	 * @param redirectAttributes : redirect 시 메시지를 보여주기 위한 값
	 * @return propertyPage로 이동
	 */
	@PostMapping("/property/add")
	public String addProperty(@RequestParam String key, @RequestParam String json,
		RedirectAttributes redirectAttributes) {
		try {
			Map<String, Object> addProperties = jsonPrettierService.readValue(json);
			adminService.addProperty(key, addProperties);
			redirectAttributes.addFlashAttribute("message", "Property가 성공적으로 추가되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Property 추가 중 오류가 발생했습니다: " + e.getMessage());
		}
		return getRedirectUrl();
	}

	/**
	 * property 삭제
	 *
	 * @param key                : 프로퍼티 키 값
	 * @param redirectAttributes : redirect 시 메시지를 보여주기 위한 값
	 * @return propertyPage로 이동
	 */
	@PostMapping("/property/delete")
	public String deletePropertyWithDeleteMapping(@RequestParam String key,
		RedirectAttributes redirectAttributes) {
		try {
			adminService.deleteProperty(key);
			redirectAttributes.addFlashAttribute("message", "Property가 성공적으로 삭제되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Property 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
		return getRedirectUrl();
	}

	@PatchMapping("/advertisement/meeting-top/{advertisementId}")
	public ResponseEntity<AdvertisementMeetingTopUpdateResponse> updateMeetingTopAdvertisement(
		@PathVariable Integer advertisementId,
		@Valid @RequestBody AdvertisementMeetingTopUpdateRequest request
	) {
		Advertisement advertisement = advertisementService.updateMeetingTopAdvertisement(advertisementId, request);
		return ResponseEntity.ok(AdvertisementMeetingTopUpdateResponse.from(advertisement));
	}

	@PostMapping(value = "/advertisement/meeting-top/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<AdvertisementImageUploadResponse> uploadMeetingTopAdvertisementImage(
		@RequestParam("file") MultipartFile file
	) {
		String publicUrl = s3Service.uploadMeetingTopImage(file);
		return ResponseEntity.ok(AdvertisementImageUploadResponse.of(publicUrl));
	}

	private String getRedirectUrl() {
		return "redirect:/admin/v2/" + adminKeyProvider.getAdminKey() + "/propertyPage";
	}
}
