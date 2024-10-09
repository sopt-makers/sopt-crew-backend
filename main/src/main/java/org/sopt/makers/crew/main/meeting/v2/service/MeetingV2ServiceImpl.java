package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.global.constant.CrewConst.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;
import static org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus.*;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

import org.sopt.makers.crew.main.entity.meeting.JointLeader;
import org.sopt.makers.crew.main.entity.meeting.JointLeaderRepository;
import org.sopt.makers.crew.main.global.dto.MeetingResponseDto;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.global.util.CustomPageable;
import org.sopt.makers.crew.main.global.util.Time;
import org.sopt.makers.crew.main.global.util.UserPartUtil;
import org.sopt.makers.crew.main.entity.apply.Applies;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.comment.Comment;
import org.sopt.makers.crew.main.entity.comment.CommentRepository;
import org.sopt.makers.crew.main.entity.like.LikeRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.post.Post;
import org.sopt.makers.crew.main.entity.post.PostRepository;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.enums.UserPart;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.s3.service.S3Service;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.MeetingMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingByOrgUserQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.ApplyV2UpdateStatusBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.AppliesCsvFileUrlResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyWholeInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingByOrgUserMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingBannerResponseUserDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingByIdResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

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
	private final JointLeaderRepository jointLeaderRepository;

	private final S3Service s3Service;

	private final MeetingMapper meetingMapper;
	private final ApplyMapper applyMapper;

	private final Time time;

	@Override
	public MeetingV2GetAllMeetingByOrgUserDto getAllMeetingByOrgUser(
		MeetingV2GetAllMeetingByOrgUserQueryDto queryDto) {
		int page = queryDto.getPage();
		int take = queryDto.getTake();

		Optional<User> user = userRepository.findByOrgId(queryDto.getOrgUserId());
		List<MeetingV2GetAllMeetingByOrgUserMeetingDto> userJoinedList = new ArrayList<>();

		if (!user.isEmpty()) {
			User existUser = user.get();
			List<Meeting> myMeetings = meetingRepository.findAllByUserId(existUser.getId());

			userJoinedList = Stream
				.concat(myMeetings.stream(),
					applyRepository.findAllByUserIdAndStatus(existUser.getId(), EnApplyStatus.APPROVE)
						.stream().map(Apply::getMeeting))
				.map(meeting -> MeetingV2GetAllMeetingByOrgUserMeetingDto.of(meeting.getId(),
					meeting.checkMeetingLeader(existUser.getId()), meeting.getTitle(),
					meeting.getImageURL().get(0).getUrl(), meeting.getCategory().getValue(),
					meeting.getMStartDate(), meeting.getMEndDate(), checkActivityStatus(meeting)))
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
	 * */
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

	@Override
	@Transactional
	public MeetingV2CreateMeetingResponseDto createMeeting(MeetingV2CreateMeetingBodyDto requestBody, Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);

		if (user.getActivities() == null) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		if (requestBody.getFiles().size() == ZERO || requestBody.getJoinableParts().length == ZERO) {
			throw new BadRequestException(VALIDATION_EXCEPTION.getErrorCode());
		}

		Meeting meeting = meetingMapper.toMeetingEntity(requestBody,
			createTargetActiveGeneration(requestBody.getCanJoinOnlyActiveGeneration()), ACTIVE_GENERATION, user,
			user.getId());

		Meeting savedMeeting = meetingRepository.save(meeting);

		if (requestBody.getJointMeetingLeaderUserIds() != null) {
			List<User> users = userRepository.findAllById(requestBody.getJointMeetingLeaderUserIds());
			List<JointLeader> jointLeaders = getJointLeaders(users, savedMeeting);
			jointLeaderRepository.saveAll(jointLeaders);
		}

		return MeetingV2CreateMeetingResponseDto.of(savedMeeting.getId());
	}

	@Override
	@Transactional
	public MeetingV2ApplyMeetingResponseDto applyMeeting(MeetingV2ApplyMeetingDto requestBody, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(requestBody.getMeetingId());
		User user = userRepository.findByIdOrThrow(userId);

		List<Apply> applies = applyRepository.findAllByMeetingId(meeting.getId());

		validateMeetingCapacity(meeting, applies);
		validateUserAlreadyApplied(userId, applies);
		validateApplyPeriod(meeting);
		validateUserActivities(user);
		validateUserJoinableParts(user, meeting);

		Apply apply = applyMapper.toApplyEntity(requestBody, EnApplyType.APPLY, meeting, user,
			userId);
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
		Page<ApplyInfoDto> applyInfoDtos = applyRepository.findApplyList(queryCommand,
			PageRequest.of(queryCommand.getPage() - 1, queryCommand.getTake()),
			meetingId, meeting.getUserId(), userId);
		PageOptionsDto pageOptionsDto = new PageOptionsDto(queryCommand.getPage(), queryCommand.getTake());
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)applyInfoDtos.getTotalElements());

		return MeetingGetApplyListResponseDto.of(applyInfoDtos.getContent(), pageMetaDto);
	}

	@Override
	public MeetingV2GetAllMeetingDto getMeetings(MeetingV2GetAllMeetingQueryDto queryCommand) {
		Sort sort = Sort.by(Sort.Direction.ASC, "id");
		Page<Meeting> meetings = meetingRepository.findAllByQuery(queryCommand,
			new CustomPageable(queryCommand.getPage() - 1, sort), time);
		List<Integer> meetingIds = meetings.stream().map(Meeting::getId).toList();

		Applies allApplies = new Applies(applyRepository.findAllByMeetingIdIn(meetingIds));

		List<MeetingResponseDto> meetingResponseDtos = meetings.getContent().stream()
			.map(meeting -> MeetingResponseDto.of(meeting, meeting.getUser(),
				allApplies.getApprovedCount(meeting.getId()), time.now()))
			.toList();

		PageOptionsDto pageOptionsDto = new PageOptionsDto(meetings.getPageable().getPageNumber() + 1,
			meetings.getPageable().getPageSize());
		PageMetaDto pageMetaDto = new PageMetaDto(pageOptionsDto, (int)meetings.getTotalElements());

		return MeetingV2GetAllMeetingDto.of(meetingResponseDtos, pageMetaDto);
	}

	/**
	 * @note: 1. like(Comment, post 관련) -> comment -> post 순으로 삭제
	 * 		  2. apply 삭제
	 * 		  3. meeting 삭제
	 * */

	@Override
	@Transactional
	public void deleteMeeting(Integer meetingId, Integer userId) {
		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		meeting.validateMeetingCreator(userId);

		List<Post> posts = postRepository.findAllByMeetingId(meetingId);
		List<Integer> postIds = posts.stream().map(Post::getId).toList();

		List<Comment> comments = commentRepository.findAllByPostIdIsIn(postIds);
		List<Integer> commentIds = comments.stream().map(Comment::getId).toList();

		likeRepository.deleteAllByPostIdsInQuery(postIds);
		likeRepository.deleteAllByCommentIdsInQuery(commentIds);

		commentRepository.deleteAllByPostIdsInQuery(postIds);
		postRepository.deleteAllByMeetingIdQuery(meetingId);
		applyRepository.deleteAllByMeetingIdQuery(meetingId);
		jointLeaderRepository.deleteAllByMeetingId(meetingId);

		meetingRepository.delete(meeting);
	}

	@Override
	@Transactional
	public void updateMeeting(Integer meetingId, MeetingV2CreateMeetingBodyDto requestBody, Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);

		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		meeting.validateMeetingCreator(userId);

		Meeting updatedMeeting = meetingMapper.toMeetingEntity(requestBody,
			createTargetActiveGeneration(requestBody.getCanJoinOnlyActiveGeneration()), ACTIVE_GENERATION, user,
			user.getId());

		jointLeaderRepository.deleteAllByMeetingId(updatedMeeting.getId());
		if (requestBody.getJointMeetingLeaderUserIds() != null) {
			List<User> users = userRepository.findAllById(requestBody.getJointMeetingLeaderUserIds());
			List<JointLeader> jointLeaders = getJointLeaders(users, updatedMeeting);
			jointLeaderRepository.saveAll(jointLeaders);
		}

		meeting.updateMeeting(updatedMeeting);
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
	public MeetingV2GetMeetingByIdResponseDto getMeetingById(Integer meetingId, Integer userId) {
		User user = userRepository.findByIdOrThrow(userId);

		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		User meetingLeader = userRepository.findByIdOrThrow(meeting.getUserId());
		List<JointLeader> jointLeaders = jointLeaderRepository.findAllByMeetingId(meetingId);

		Applies applies = new Applies(
			applyRepository.findAllByMeetingIdWithUser(meetingId, List.of(WAITING, APPROVE, REJECT), ORDER_ASC));

		Boolean isHost = meeting.checkMeetingLeader(user.getId());
		Boolean isApply = applies.isApply(meetingId, user.getId());
		Boolean isApproved = applies.isApproved(meetingId, user.getId());
		long approvedCount = applies.getApprovedCount(meetingId);

		List<ApplyWholeInfoDto> applyWholeInfoDtos = new ArrayList<>();
		if (applies.hasApplies(meetingId)) {
			applyWholeInfoDtos = applies.getAppliesMap().get(meetingId).stream()
				.map(apply -> ApplyWholeInfoDto.of(apply, apply.getUser(), userId))
				.toList();
		}

		return MeetingV2GetMeetingByIdResponseDto.of(meeting, jointLeaders, approvedCount, isHost, isApply, isApproved,
			meetingLeader, applyWholeInfoDtos, time.now());
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
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime mStartDate = meeting.getMStartDate();
		LocalDateTime mEndDate = meeting.getMEndDate();
		return now.isEqual(mStartDate) || (now.isAfter(mStartDate) && now.isBefore(mEndDate));
	}

	private Integer createTargetActiveGeneration(Boolean canJoinOnlyActiveGeneration) {
		return Boolean.TRUE.equals(canJoinOnlyActiveGeneration) ? ACTIVE_GENERATION : null;
	}

	private List<UserActivityVO> filterUserActivities(User user, Meeting meeting) {
		// 현재 활동기수만 지원 가능할 경우 -> 현재 활동 기수에 해당하는 파트만 필터링
		if (meeting.getTargetActiveGeneration() == ACTIVE_GENERATION && meeting.getCanJoinOnlyActiveGeneration()) {
			List<UserActivityVO> filteredActivities = user.getActivities().stream()
				.filter(activity -> activity.getGeneration() == ACTIVE_GENERATION)
				.collect(Collectors.toList());

			// 활동 기수가 아닌 경우 예외 처리
			if (filteredActivities.isEmpty()) {
				throw new BadRequestException(NOT_ACTIVE_GENERATION.getErrorCode());
			}

			return filteredActivities;
		}
		return user.getActivities();
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
		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(meeting.getEndDate()) || now.isBefore(meeting.getStartDate())) {
			throw new BadRequestException(NOT_IN_APPLY_PERIOD.getErrorCode());
		}
	}

	private void validateUserActivities(User user) {
		if (user.getActivities().isEmpty()) {
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

	private List<JointLeader> getJointLeaders(List<User> jointLeaders, Meeting savedMeeting) {
		return jointLeaders.stream()
			.map(jointLeader -> JointLeader.builder()
				.meeting(savedMeeting)
				.user(jointLeader)
				.build())
			.toList();
	}
}
