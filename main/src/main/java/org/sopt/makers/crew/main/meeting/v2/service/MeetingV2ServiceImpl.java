package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus.*;
import static org.sopt.makers.crew.main.global.constant.CrewConst.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.entity.apply.Applies;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.flash.Flash;
import org.sopt.makers.crew.main.entity.flash.FlashRepository;
import org.sopt.makers.crew.main.entity.like.LikeRepository;
import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderReader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.CoLeaders;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingReader;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.tag.TagRepository;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserReader;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.enums.UserPart;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.s3.service.S3Service;
import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto;
import org.sopt.makers.crew.main.global.config.ImageSettingProperties;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.global.dto.MeetingResponseDto;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.global.pagination.AdvertisementPageableStrategy;
import org.sopt.makers.crew.main.global.pagination.DefaultPageableStrategy;
import org.sopt.makers.crew.main.global.pagination.PageableStrategy;
import org.sopt.makers.crew.main.global.pagination.PaginationType;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.global.util.UserPartUtil;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.FlashMeetingMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.MeetingMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.ApplyV2UpdateStatusBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateAndUpdateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.AppliesCsvFileUrlResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDetailDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyWholeInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingLeaderUserIdDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateAndUpdateMeetingForFlashResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingByIdResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetRecommendDto;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2CreateGeneralMeetingTagResponseDto;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2MeetingTagsResponseDto;
import org.sopt.makers.crew.main.tag.v2.service.TagV2Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingV2ServiceImpl implements MeetingV2Service {

	private static final int ZERO = 0;

	private final UserRepository userRepository;
	private final ApplyRepository applyRepository;
	private final MeetingRepository meetingRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final LikeRepository likeRepository;
	private final CoLeaderRepository coLeaderRepository;
	private final FlashRepository flashRepository;
	private final TagRepository tagRepository;
	private final MeetingReader meetingReader;
	private final CoLeaderReader coLeaderReader;
	private final UserReader userReader;

	private final S3Service s3Service;
	private final TagV2Service tagV2Service;

	private final MeetingMapper meetingMapper;
	private final FlashMeetingMapper flashMeetingMapper;
	private final ApplyMapper applyMapper;

	private final ImageSettingProperties imageSettingProperties;
	private final ActiveGenerationProvider activeGenerationProvider;

	private final Time time;

	@Override
	public MeetingV2GetAllMeetingByOrgUserDto getAllMeetingByOrgUser(
		MeetingV2GetAllMeetingByOrgUserQueryDto queryDto) {
		int page = queryDto.getPage();
		int take = queryDto.getTake();

		Optional<User> user = userRepository.findByOrgId(queryDto.getOrgUserId());
		List<MeetingV2GetAllMeetingByOrgUserMeetingDto> userJoinedList = new ArrayList<>();

		if (user.isPresent()) {
			User existUser = user.get();
			List<Integer> coLeaderInMeetingIds = coLeaderRepository.findAllByUserId(existUser.getId())
				.stream().map(CoLeader::getMeeting).map(Meeting::getId).toList();

			List<Meeting> myMeetings = meetingRepository.findAllByUserIdOrIdInWithUser(existUser.getId(),
				coLeaderInMeetingIds);

			userJoinedList = Stream
				.concat(myMeetings.stream(),
					applyRepository.findAllByUserIdAndStatus(existUser.getId(), EnApplyStatus.APPROVE)
						.stream().map(Apply::getMeeting))
				.map(meeting -> MeetingV2GetAllMeetingByOrgUserMeetingDto.of(meeting.getId(),
					meeting.checkMeetingLeader(existUser.getId()), meeting.getTitle(),
					meeting.getImageURL().get(0).getUrl(), meeting.getCategory().getValue(),
					meeting.getmStartDate(), meeting.getmEndDate(), checkActivityStatus(meeting)))
				.sorted(Comparator.comparing(MeetingV2GetAllMeetingByOrgUserMeetingDto::getId).reversed())
				.collect(Collectors.toList());
		}

		List<MeetingV2GetAllMeetingByOrgUserMeetingDto> pagedUserJoinedList =
			userJoinedList.stream().skip((long)(page - 1) * take) // 스킵할 아이템 수 계산
				.limit(take) // 페이지당 아이템 수 제한
				.collect(Collectors.toList());
		PageOptionsDto pageOptionsDto = new PageOptionsDto(page, take);
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, userJoinedList.size());
		return MeetingV2GetAllMeetingByOrgUserDto.of(pagedUserJoinedList, pageMetaDto);
	}

	/**
	 * @Note: 최근 활동 여부는 게시글 생성일자를 기준으로 진행한다.
	 */
	@Override
	public List<MeetingV2GetMeetingBannerResponseDto> getMeetingBanner() {

		List<Meeting> meetings = meetingRepository.findTop20ByOrderByIdDesc();
		List<Integer> meetingIds = meetings.stream().map(Meeting::getId).toList();

		List<Post> posts = postRepository.findAllByMeetingIdIn(meetingIds);
		List<Post> filterLatestPosts = filterLatestPostsByMeetingId(posts);
		Map<Integer, Post> postMap = filterLatestPosts.stream()
			.collect(Collectors.toMap(post -> post.getMeeting().getId(), post -> post));

		Applies applies = new Applies(applyRepository.findAllByMeetingIdIn(meetingIds));

		return getResponseDto(meetings, postMap, applies);
	}

	@Override
	@Transactional
	public MeetingV2CreateMeetingResponseDto createMeeting(MeetingV2CreateAndUpdateMeetingBodyDto requestBody,
		Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);

		if (user.getActivities() == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		if (requestBody.getFiles().isEmpty() || requestBody.getJoinableParts().length == ZERO) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		Meeting meeting = meetingMapper.toMeetingEntity(requestBody,
			createTargetActiveGeneration(requestBody.getCanJoinOnlyActiveGeneration()),
			activeGenerationProvider.getActiveGeneration(), user,
			user.getId());

		Meeting savedMeeting = meetingRepository.save(meeting);

		List<Integer> coLeaderUserIds = requestBody.getCoLeaderUserIds();
		if (coLeaderUserIds != null && !coLeaderUserIds.isEmpty()) {
			List<User> users = userRepository.findAllByIdInOrThrow(coLeaderUserIds);
			List<CoLeader> coLeaders = createCoLeaders(users, savedMeeting);
			coLeaderRepository.saveAll(coLeaders);
		}

		TagV2CreateGeneralMeetingTagResponseDto tagResponseDto = tagV2Service.createGeneralMeetingTag(
			requestBody.getWelcomeMessageTypes(), requestBody.getMeetingKeywordTypes(), meeting.getId());

		return MeetingV2CreateMeetingResponseDto.of(savedMeeting.getId(), tagResponseDto.tagId());
	}

	@Override
	@Transactional
	public MeetingV2ApplyMeetingResponseDto applyGeneralMeeting(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());

		validateMeetingCategoryNotEvent(meeting);

		User user = userRepository.findByIdOrThrow(userId);
		CoLeaders coLeaders = new CoLeaders(coLeaderRepository.findAllByMeetingId(meeting.getId()));

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());

		validateMeetingCapacity(meeting, applies);
		validateUserAlreadyApplied(userId, applies);
		validateApplyPeriod(meeting);
		validateUserActivities(user);
		validateUserJoinableParts(user, meeting);
		coLeaders.validateCoLeader(meeting.getId(), user.getId());
		meeting.validateIsNotMeetingLeader(userId);

		Apply apply = applyMapper.toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		Apply savedApply = applyRepository.save(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}

	@Override
	@Transactional
	public MeetingV2ApplyMeetingResponseDto applyEventMeeting(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());

		validateMeetingCategoryEvent(meeting);

		User user = userRepository.findByIdOrThrow(userId);
		CoLeaders coLeaders = new CoLeaders(coLeaderRepository.findAllByMeetingId(meeting.getId()));

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());

		validateMeetingCapacity(meeting, applies);
		validateUserAlreadyApplied(userId, applies);
		validateApplyPeriod(meeting);
		validateUserActivities(user);
		validateUserJoinableParts(user, meeting);
		coLeaders.validateCoLeader(meeting.getId(), user.getId());
		meeting.validateIsNotMeetingLeader(userId);

		Apply apply = applyMapper.toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user, userId);
		Apply savedApply = applyRepository.save(apply);
		return MeetingV2ApplyMeetingResponseDto.of(savedApply.getId());
	}

	@Override
	@Transactional
	public void applyMeetingCancel(Integer meetingId, Integer userId) {
		boolean exists = applyRepository.existsByMeetingIdAndUserId(meetingId, userId);

		if (!exists) {
			throw new BadRequestException(NOT_FOUND_APPLY.getErrorCode());
		}

		applyRepository.deleteByMeetingIdAndUserId(meetingId, userId);
	}

	@Override
	public MeetingGetApplyListResponseDto findApplyList(MeetingGetAppliesQueryDto queryCommand,
		Integer meetingId,
		Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);

		Page<ApplyInfoDetailDto> applyInfoDetails = madeApplyInfoDetails(
			queryCommand, meetingId, userId, meeting);

		PageMetaDto pageMetaDto = madePageMetaDto(queryCommand,
			applyInfoDetails);

		return MeetingGetApplyListResponseDto.of(applyInfoDetails.getContent(), pageMetaDto);
	}

	@Override
	public MeetingV2GetAllMeetingDto getMeetings(MeetingV2GetAllMeetingQueryDto queryCommand) {
		PageableStrategy pageableStrategy = getPageableStrategy(queryCommand);
		Pageable pageable = pageableStrategy.createPageable(queryCommand);

		Page<Meeting> meetings = meetingRepository.findAllByQuery(queryCommand, pageable, time,
			activeGenerationProvider.getActiveGeneration());

		List<Integer> meetingIds = meetings.stream()
			.map(Meeting::getId)
			.toList();

		Applies allApplies = new Applies(applyRepository.findAllByMeetingIdIn(meetingIds));

		Map<Integer, TagV2MeetingTagsResponseDto> allTagsResponseDto = tagV2Service.getMeetingTagsByMeetingIds(
			meetingIds);

		List<MeetingResponseDto> meetingResponseDtos = meetings.getContent().stream()
			.map(meeting -> MeetingResponseDto.of(
				meeting,
				meeting.getUser(),
				allApplies.getApprovedCount(meeting.getId()),
				time.now(),
				activeGenerationProvider.getActiveGeneration(),
				allTagsResponseDto)
			)
			.toList();

		PageOptionsDto pageOptionsDto = new PageOptionsDto(meetings.getPageable().getPageNumber() + 1,
			meetings.getPageable().getPageSize());
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)meetings.getTotalElements());

		return MeetingV2GetAllMeetingDto.of(meetingResponseDtos, pageMetaDto);
	}

	/**
	 * @note: 1. like(Comment, post 관련) -> comment -> post 순으로 삭제
	 * 2. apply 삭제
	 * 3. meeting 삭제
	 */

	@Caching(evict = {
		@CacheEvict(value = "meetingCache", key = "#meetingId"),
		@CacheEvict(value = "meetingLeaderCache", key = "#userId"),
		@CacheEvict(value = "coLeadersCache", key = "#meetingId")
	})
	@Override
	@Transactional
	public void deleteMeeting(Integer meetingId, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		meeting.validateMeetingCreator(userId);

		List<Post> posts = postRepository.findAllByMeetingId(meetingId);
		List<Integer> postIds = posts.stream()
			.map(Post::getId)
			.toList();

		List<Comment> comments = commentRepository.findAllByPostIdIsIn(postIds);
		List<Integer> commentIds = comments.stream()
			.map(Comment::getId)
			.toList();

		likeRepository.deleteAllByPostIdsInQuery(postIds);
		likeRepository.deleteAllByCommentIdsInQuery(commentIds);

		commentRepository.deleteAllByPostIdsInQuery(postIds);
		postRepository.deleteAllByMeetingIdQuery(meetingId);
		applyRepository.deleteAllByMeetingIdQuery(meetingId);
		coLeaderRepository.deleteAllByMeetingId(meetingId);

		if (meeting.getCategory() == MeetingCategory.FLASH) {
			Flash flash = flashRepository.findByMeetingId(meetingId)
				.orElseThrow(() -> new NotFoundException(NOT_FOUND_FLASH.getErrorCode()));

			tagRepository.deleteByFlashId(flash.getId());
			flashRepository.delete(flash);
		}

		meetingRepository.delete(meeting);
	}

	@Caching(evict = {
		@CacheEvict(value = "meetingCache", key = "#meetingId"),
		@CacheEvict(value = "meetingLeaderCache", key = "#userId"),
		@CacheEvict(value = "coLeadersCache", key = "#meetingId")
	})
	@Override
	@Transactional
	public void updateMeeting(Integer meetingId, MeetingV2CreateAndUpdateMeetingBodyDto requestBody,
		Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);

		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		meeting.validateMeetingCreator(userId);

		updateCoLeaders(requestBody.getCoLeaderUserIds(), meeting);

		Meeting updatedMeeting = meetingMapper.toMeetingEntity(requestBody,
			createTargetActiveGeneration(requestBody.getCanJoinOnlyActiveGeneration()),
			activeGenerationProvider.getActiveGeneration(), user,
			user.getId());

		meeting.updateMeeting(updatedMeeting);

		tagV2Service.updateGeneralMeetingTag(requestBody.getWelcomeMessageTypes(), requestBody.getMeetingKeywordTypes(),
			meeting.getId());
	}

	private void updateCoLeaders(List<Integer> coLeaderUserIds, Meeting updatedMeeting) {
		coLeaderRepository.deleteAllByMeetingId(updatedMeeting.getId());

		if (coLeaderUserIds == null || coLeaderUserIds.isEmpty()) {
			return;
		}

		List<User> users = userRepository.findAllById(coLeaderUserIds);
		List<CoLeader> coLeaders = createCoLeaders(users, updatedMeeting);
		coLeaderRepository.saveAll(coLeaders);
	}

	@Override
	@Transactional
	public void updateApplyStatus(Integer meetingId, ApplyV2UpdateStatusBodyDto requestBody, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		meeting.validateMeetingCreator(userId);

		Apply apply = applyRepository.findByIdOrThrow(requestBody.getApplyId());
		EnApplyStatus updatedApplyStatus = EnApplyStatus.ofValue(requestBody.getStatus());
		apply.validateDuplicateUpdateApplyStatus(updatedApplyStatus);

		if (updatedApplyStatus.equals(EnApplyStatus.APPROVE)) {
			List<Apply> applies = applyRepository.findAllByMeetingIdAndStatus(meetingId,
				EnApplyStatus.APPROVE);

			meeting.validateCapacity(applies.size());
		}

		apply.updateApplyStatus(updatedApplyStatus);

	}

	@Override
	public AppliesCsvFileUrlResponseDto getAppliesCsvFileUrl(Integer meetingId, List<Integer> status, String order,
		Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		meeting.validateMeetingCreator(userId);

		List<EnApplyStatus> statuses = status.stream().map(EnApplyStatus::ofValue).toList();
		List<Apply> applies = applyRepository.findAllByMeetingIdWithUser(meetingId, statuses, order);

		String csvFilePath = createCsvFile(applies);
		String csvFileUrl = s3Service.uploadCSVFile(csvFilePath);
		deleteCsvFile(csvFilePath);

		return AppliesCsvFileUrlResponseDto.of(csvFileUrl);
	}

	@Override
	public MeetingV2GetMeetingByIdResponseDto getMeetingDetail(Integer meetingId, Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);

		Meeting meeting = meetingReader.getMeetingById(meetingId).toEntity();
		MeetingCreatorDto meetingLeader = userReader.getMeetingLeader(meeting.getUserId());
		CoLeaders coLeaders = coLeaderReader.getCoLeaders(meetingId).toEntity();

		Applies applies = new Applies(
			applyRepository.findAllByMeetingIdWithUser(meetingId, List.of(WAITING, APPROVE, REJECT), ORDER_ASC));

		Boolean isHost = meeting.checkMeetingLeader(user.getId());
		Boolean isApply = applies.isApply(meetingId, user.getId());
		Boolean isApproved = applies.isApproved(meetingId, user.getId());
		boolean isCoLeader = coLeaders.isCoLeader(meetingId, userId);
		long approvedCount = applies.getApprovedCount(meetingId);

		List<ApplyWholeInfoDto> applyWholeInfoDtos = new ArrayList<>();
		if (applies.hasApplies(meetingId)) {
			AtomicInteger applyNumber = new AtomicInteger(1);
			applyWholeInfoDtos = applies.getAppliesMap().get(meetingId).stream()
				.sorted(Comparator.comparing(Apply::getAppliedDate))
				.map(apply -> ApplyWholeInfoDto.of(apply, apply.getUser(), userId,
					applyNumber.getAndIncrement()))
				.toList();
		}

		List<WelcomeMessageType> welcomeMessageTypes = tagV2Service.getWelcomeMessageTypesByMeetingId(meetingId);
		List<MeetingKeywordType> meetingKeywordTypes = tagV2Service.getMeetingKeywordsTypesByMeetingId(meetingId);

		return MeetingV2GetMeetingByIdResponseDto.of(meetingId, meeting, coLeaders.getCoLeaders(meetingId), isCoLeader,
			approvedCount, isHost, isApply, isApproved,
			meetingLeader, applyWholeInfoDtos, welcomeMessageTypes, meetingKeywordTypes, time.now());
	}

	@Override
	public MeetingV2GetRecommendDto getRecommendMeetingsByIds(List<Integer> meetingIds, Integer userId) {

		List<Meeting> meetings = meetingRepository.findRecommendMeetings(meetingIds, time);
		List<Integer> foundMeetingIds = meetings.stream()
			.map(Meeting::getId)
			.toList();

		Applies allApplies = new Applies(applyRepository.findAllByMeetingIdIn(foundMeetingIds));

		Map<Integer, TagV2MeetingTagsResponseDto> allTagsResponseDto = tagV2Service.getMeetingTagsByMeetingIds(
			meetingIds);

		List<MeetingResponseDto> meetingResponseDtos = meetings.stream()
			.map(meeting -> MeetingResponseDto.of(
				meeting,
				meeting.getUser(),
				allApplies.getApprovedCount(meeting.getId()),
				time.now(),
				activeGenerationProvider.getActiveGeneration(),
				allTagsResponseDto)
			)
			.toList();

		return MeetingV2GetRecommendDto.from(meetingResponseDtos);
	}

	@Override
	@Transactional
	public MeetingV2CreateAndUpdateMeetingForFlashResponseDto createMeetingForFlash(Integer userId,
		FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto flashBody) {

		User user = userRepository.findByIdOrThrow(userId);

		if (flashBody.files().isEmpty()) {
			flashBody.files().add(imageSettingProperties.getDefaultFlashImage());
		}

		Meeting flashMeeting = flashMeetingMapper.toMeetingEntityForFlash(flashBody, user, user.getId(), time.now(),
			activeGenerationProvider.getActiveGeneration());

		meetingRepository.save(flashMeeting);

		return MeetingV2CreateAndUpdateMeetingForFlashResponseDto.of(flashMeeting.getId(), flashBody);
	}

	@Override
	@Transactional
	public MeetingV2CreateAndUpdateMeetingForFlashResponseDto updateMeetingForFlash(Integer meetingId, Integer userId,
		FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto updatedFlashBody) {

		User user = userRepository.findByIdOrThrow(userId);

		if (updatedFlashBody.files().isEmpty()) {
			updatedFlashBody.files().add(imageSettingProperties.getDefaultFlashImage());
		}

		Meeting flashMeeting = meetingRepository.findById(meetingId)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_MEETING.getErrorCode()));

		flashMeeting.validateMeetingCreator(userId);

		Meeting updatedFlashMeeting = flashMeetingMapper.toMeetingEntityForFlash(updatedFlashBody, user, user.getId(),
			time.now(), activeGenerationProvider.getActiveGeneration());

		flashMeeting.updateMeeting(updatedFlashMeeting);

		return MeetingV2CreateAndUpdateMeetingForFlashResponseDto.of(flashMeeting.getId(), updatedFlashBody);
	}

	@Override
	public MeetingLeaderUserIdDto getMeetingLeaderUserIdByMeetingId(Integer meetingId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		return MeetingLeaderUserIdDto.from(meeting);
	}

	@Override
	@Caching(evict = {
		@CacheEvict(value = "meetingCache", key = "#meetingId"),
	})
	public void evictMeetingCache(Integer meetingId) {
	}

	@Override
	@Caching(evict = {
		@CacheEvict(value = "meetingLeaderCache", key = "#userId"),
	})
	public void evictMeetingLeaderCache(Integer userId) {
	}

	private PageableStrategy getPageableStrategy(MeetingV2GetAllMeetingQueryDto queryCommand) {
		if (queryCommand.getPaginationType().equals(PaginationType.ADVERTISEMENT)) {
			return new AdvertisementPageableStrategy();
		} else {
			return new DefaultPageableStrategy();
		}
	}

	private Page<ApplyInfoDetailDto> madeApplyInfoDetails(MeetingGetAppliesQueryDto queryCommand, Integer meetingId,
		Integer userId, Meeting meeting) {
		Page<ApplyInfoDto> applyInfoDtos = applyRepository.findApplyList(queryCommand,
			PageRequest.of(queryCommand.getPage() - 1, queryCommand.getTake()),
			meetingId, meeting.getUserId(), userId);

		Integer startPage = (queryCommand.getPage() - 1) * queryCommand.getTake();

		AtomicInteger applyNumbers = new AtomicInteger(startPage + 1);
		return applyInfoDtos.map(
			a -> ApplyInfoDetailDto.toApplyInfoDetailDto(a, applyNumbers.getAndIncrement()));
	}

	private PageMetaDto madePageMetaDto(MeetingGetAppliesQueryDto queryCommand,
		Page<ApplyInfoDetailDto> applyInfoDetailDtos) {
		PageOptionsDto pageOptionsDto = new PageOptionsDto(queryCommand.getPage(), queryCommand.getTake());
		return new PageMetaDto(pageOptionsDto, (int)applyInfoDetailDtos.getTotalElements());
	}

	private List<Post> filterLatestPostsByMeetingId(List<Post> posts) {
		return posts.stream()
			.collect(Collectors.groupingBy(Post::getMeetingId,
				Collectors.maxBy(Comparator.comparing(Post::getCreatedDate))))
			.values()
			.stream()
			.flatMap(Optional::stream)
			.collect(Collectors.toList());
	}

	private List<MeetingV2GetMeetingBannerResponseDto> getResponseDto(List<Meeting> meetings,
		Map<Integer, Post> postMap, Applies applies) {
		return meetings.stream()
			.map(meeting -> {
				MeetingV2GetMeetingBannerResponseUserDto meetingCreatorDto = MeetingV2GetMeetingBannerResponseUserDto.of(
					meeting.getUser());

				LocalDateTime recentActivityDate = null;
				if (postMap.containsKey(meeting.getId())) {
					recentActivityDate = postMap.get(meeting.getId()).getCreatedDate();
				}

				return MeetingV2GetMeetingBannerResponseDto.of(meeting, recentActivityDate,
					applies.getAppliedCount(meeting.getId()), applies.getApprovedCount(meeting.getId()),
					meetingCreatorDto, time.now());
			}).toList();
	}

	private void deleteCsvFile(String filePath) {
		try {
			Files.deleteIfExists(Paths.get(filePath));
		} catch (IOException e) {
			throw new ServerException(CSV_ERROR.getErrorCode());
		}
	}

	private String createCsvFile(List<Apply> applies) {
		String filePath = UUID.randomUUID() + ".csv";

		try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
			new FileOutputStream(filePath), StandardCharsets.UTF_8);
			 CSVWriter writer = new CSVWriter(outputStreamWriter)) {

			// BOM 추가 (Excel에서 UTF-8 파일을 제대로 처리하기 위함)
			outputStreamWriter.write("\uFEFF");

			// CSV 파일의 헤더 정의
			String[] header = {"이름", "최근 활동 파트", "최근 활동 기수", "전화번호", "신청 날짜 및 시간", "신청 상태"};
			writer.writeNext(header);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			for (Apply apply : applies) {
				User user = apply.getUser();
				UserActivityVO activity = user.getRecentActivityVO();

				String[] data = {
					user.getName(),
					activity.getPart(),
					String.valueOf(activity.getGeneration()),
					String.format("\"%s\"", user.getPhone()),
					apply.getAppliedDate().format(formatter),
					apply.getStatus().getDescription()
				};
				writer.writeNext(data);
			}
		} catch (Exception e) {
			throw new ServerException(CSV_ERROR.getErrorCode());
		}

		return filePath;
	}

	private Boolean checkActivityStatus(Meeting meeting) {
		LocalDateTime now = time.now();
		LocalDateTime mStartDate = meeting.getmStartDate();
		LocalDateTime mEndDate = meeting.getmEndDate();
		return now.isEqual(mStartDate) || (now.isAfter(mStartDate) && now.isBefore(mEndDate));
	}

	private Integer createTargetActiveGeneration(Boolean canJoinOnlyActiveGeneration) {
		return Boolean.TRUE.equals(canJoinOnlyActiveGeneration) ? activeGenerationProvider.getActiveGeneration() : null;
	}

	private List<UserActivityVO> filterUserActivities(User user, Meeting meeting) {
		// 현재 활동기수만 지원 가능할 경우 -> 현재 활동 기수에 해당하는 파트만 필터링
		if (meeting.getTargetActiveGeneration() == activeGenerationProvider.getActiveGeneration()
			&& meeting.getCanJoinOnlyActiveGeneration()) {
			List<UserActivityVO> filteredActivities = user.getActivities().stream()
				.filter(activity -> activity.getGeneration() == activeGenerationProvider.getActiveGeneration())
				.collect(Collectors.toList());

			// 활동 기수가 아닌 경우 예외 처리
			if (filteredActivities.isEmpty()) {
				throw new BadRequestException(NOT_ACTIVE_GENERATION.getErrorCode());
			}

			return filteredActivities;
		}
		return user.getActivities();
	}

	private void validateMeetingCategoryNotEvent(Meeting meeting) {
		if (meeting.getCategory() == MeetingCategory.EVENT) {
			throw new BadRequestException(NOT_IN_APPLY_PERIOD.getErrorCode());
		}
	}

	private void validateMeetingCategoryEvent(Meeting meeting) {
		if (meeting.getCategory() != MeetingCategory.EVENT) {
			throw new BadRequestException(NOT_IN_APPLY_PERIOD.getErrorCode());
		}
	}

	private void validateMeetingCapacity(Meeting meeting, List<Apply> applies) {
		List<Apply> approvedApplies = applies.stream()
			.filter(apply -> EnApplyStatus.APPROVE.equals(apply.getStatus()))
			.toList();

		meeting.validateCapacity(approvedApplies.size());
	}

	private void validateUserAlreadyApplied(Integer userId, List<Apply> applies) {
		boolean hasApplied = applies.stream()
			.anyMatch(appliedInfo -> appliedInfo.getUser().getId().equals(userId));

		if (hasApplied) {
			throw new BadRequestException(ALREADY_APPLIED_MEETING.getErrorCode());
		}
	}

	private void validateApplyPeriod(Meeting meeting) {
		LocalDateTime now = time.now();
		if (now.isAfter(meeting.getEndDate()) || now.isBefore(meeting.getStartDate())) {
			throw new BadRequestException(NOT_IN_APPLY_PERIOD.getErrorCode());
		}
	}

	private void validateUserActivities(User user) {
		if (user.getActivities() == null || user.getActivities().isEmpty()) {
			throw new BadRequestException(MISSING_GENERATION_PART.getErrorCode());
		}
	}

	private void validateUserJoinableParts(User user, Meeting meeting) {
		if (meeting.getJoinableParts().length == 0) {
			return;
		}

		List<UserActivityVO> userActivities = filterUserActivities(user, meeting);
		List<String> userJoinableParts = userActivities.stream()
			.map(UserActivityVO::getPart)
			.filter(part -> {
				MeetingJoinablePart meetingJoinablePart = UserPartUtil.getMeetingJoinablePart(
					UserPart.ofValue(part));

				// 임원진이라면 패스
				if (meetingJoinablePart == null) {
					return true;
				}

				return Arrays.stream(meeting.getJoinableParts())
					.anyMatch(joinablePart -> joinablePart == meetingJoinablePart);
			})
			.collect(Collectors.toList());

		if (userJoinableParts.isEmpty()) {
			throw new BadRequestException(NOT_TARGET_PART.getErrorCode());
		}
	}

	private List<CoLeader> createCoLeaders(List<User> coLeaders, Meeting savedMeeting) {
		return coLeaders.stream()
			.map(coLeader -> CoLeader.builder()
				.meeting(savedMeeting)
				.user(coLeader)
				.build())
			.toList();
	}
}
