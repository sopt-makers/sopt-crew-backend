package org.sopt.makers.crew.main.meeting.v2.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.common.config.TestConfig;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetApplyListCommand;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SqlGroup({
        @Sql(value = "/sql/apply-repository-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)

})
public class ApplyRepositoryTest {

    @Autowired
    private ApplyRepository applyRepository;

    @Test
    void 스터디장이_신청자_리스트_최신순으로_조회() {
        // given
        MeetingGetApplyListCommand queryCommand = new MeetingGetApplyListCommand(List.of(0, 1, 2), "desc");
        int page = 1;
        int take = 12;
        Integer meetingId = 1;
        Integer studyCreatorId = 1;
        Integer userId = 1;

        // when
        Page<ApplyInfoDto> applyInfoDtos = applyRepository.findApplyList(queryCommand, PageRequest.of(page - 1, take),
                meetingId, studyCreatorId, userId);

        // then
        assertThat(applyInfoDtos.getTotalElements()).isEqualTo(3);
        assertThat(applyInfoDtos.getSize()).isEqualTo(take);
        assertThat(applyInfoDtos.getNumber()).isEqualTo(page-1);
        assertThat(applyInfoDtos.getTotalPages()).isEqualTo(1);


        ApplyInfoDto applyInfoDto1 = applyInfoDtos.getContent().get(0);
        assertThat(applyInfoDto1)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(3, 0, "전할말입니다3",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 3, 413489000)), 0);
        assertThat(applyInfoDto1.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(4, "이영지", 1004, "profile4.jpg", "010-5555-5555");
        assertThat(applyInfoDto1.getApplicant().getRecentActivity().getGeneration()).isEqualTo(32);


        ApplyInfoDto applyInfoDto2 = applyInfoDtos.getContent().get(1);
        assertThat(applyInfoDto2)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(2, 0, "전할말입니다2",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 2, 413489000)), 1);
        assertThat(applyInfoDto2.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(3, "김철수", 1003, "profile3.jpg", "010-3333-4444");
        assertThat(applyInfoDto2.getApplicant().getRecentActivity().getGeneration()).isEqualTo(34);


        ApplyInfoDto applyInfoDto3 = applyInfoDtos.getContent().get(2);
        assertThat(applyInfoDto3)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(1, 0, "전할말입니다1",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 0, 913489000)), 1);
        assertThat(applyInfoDto3.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(2, "홍길동", 1002, "profile2.jpg", "010-1111-2222");
        assertThat(applyInfoDto3.getApplicant().getRecentActivity().getGeneration()).isEqualTo(33);
    }

    @Test
    void 스터디장이_신청자_리스트_오래된순으로_조회() {
        // given
        MeetingGetApplyListCommand queryCommand = new MeetingGetApplyListCommand(List.of(0, 1, 2), "asc");
        int page = 1;
        int take = 12;
        Integer meetingId = 1;
        Integer studyCreatorId = 1;
        Integer userId = 1;

        // when
        Page<ApplyInfoDto> applyInfoDtos = applyRepository.findApplyList(queryCommand, PageRequest.of(page - 1, take),
                meetingId, studyCreatorId, userId);

        // then
        assertThat(applyInfoDtos.getTotalElements()).isEqualTo(3);
        assertThat(applyInfoDtos.getSize()).isEqualTo(take);
        assertThat(applyInfoDtos.getNumber()).isEqualTo(page-1);
        assertThat(applyInfoDtos.getTotalPages()).isEqualTo(1);

        ApplyInfoDto applyInfoDto3 = applyInfoDtos.getContent().get(0);
        assertThat(applyInfoDto3)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(1, 0, "전할말입니다1",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 0, 913489000)), 1);
        assertThat(applyInfoDto3.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(2, "홍길동", 1002, "profile2.jpg", "010-1111-2222");
        assertThat(applyInfoDto3.getApplicant().getRecentActivity().getGeneration()).isEqualTo(33);

        ApplyInfoDto applyInfoDto2 = applyInfoDtos.getContent().get(1);
        assertThat(applyInfoDto2)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(2, 0, "전할말입니다2",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 2, 413489000)), 1);
        assertThat(applyInfoDto2.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(3, "김철수", 1003, "profile3.jpg", "010-3333-4444");
        assertThat(applyInfoDto2.getApplicant().getRecentActivity().getGeneration()).isEqualTo(34);

        ApplyInfoDto applyInfoDto1 = applyInfoDtos.getContent().get(2);
        assertThat(applyInfoDto1)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(3, 0, "전할말입니다3",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 3, 413489000)), 0);
        assertThat(applyInfoDto1.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(4, "이영지", 1004, "profile4.jpg", "010-5555-5555");
        assertThat(applyInfoDto1.getApplicant().getRecentActivity().getGeneration()).isEqualTo(32);

    }

    @Test
    void 스터디장이_신청자_리스트_대기상태만_오래된순으로_조회() {
        // given
        MeetingGetApplyListCommand queryCommand = new MeetingGetApplyListCommand(List.of(0), "asc");
        int page = 1;
        int take = 12;
        Integer meetingId = 1;
        Integer studyCreatorId = 1;
        Integer userId = 1;

        // when
        Page<ApplyInfoDto> applyInfoDtos = applyRepository.findApplyList(queryCommand, PageRequest.of(page - 1, take),
                meetingId, studyCreatorId, userId);

        // then
        assertThat(applyInfoDtos.getTotalElements()).isEqualTo(1);
        assertThat(applyInfoDtos.getSize()).isEqualTo(take);
        assertThat(applyInfoDtos.getNumber()).isEqualTo(page-1);
        assertThat(applyInfoDtos.getTotalPages()).isEqualTo(1);

        ApplyInfoDto applyInfoDto1 = applyInfoDtos.getContent().get(0);
        assertThat(applyInfoDto1)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(3, 0, "전할말입니다3",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 3, 413489000)), 0);
        assertThat(applyInfoDto1.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(4, "이영지", 1004, "profile4.jpg", "010-5555-5555");
        assertThat(applyInfoDto1.getApplicant().getRecentActivity().getGeneration()).isEqualTo(32);

    }

    @Test
    void 스터디장이_신청자_리스트_승인상태만_오래된순으로_조회() {
        // given
        MeetingGetApplyListCommand queryCommand = new MeetingGetApplyListCommand(List.of(1), "asc");
        int page = 1;
        int take = 12;
        Integer meetingId = 1;
        Integer studyCreatorId = 1;
        Integer userId = 1;

        // when
        Page<ApplyInfoDto> applyInfoDtos = applyRepository.findApplyList(queryCommand, PageRequest.of(page - 1, take),
                meetingId, studyCreatorId, userId);

        // then
        assertThat(applyInfoDtos.getTotalElements()).isEqualTo(2);
        assertThat(applyInfoDtos.getSize()).isEqualTo(take);
        assertThat(applyInfoDtos.getNumber()).isEqualTo(page-1);
        assertThat(applyInfoDtos.getTotalPages()).isEqualTo(1);

        ApplyInfoDto applyInfoDto3 = applyInfoDtos.getContent().get(0);
        assertThat(applyInfoDto3)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(1, 0, "전할말입니다1",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 0, 913489000)), 1);
        assertThat(applyInfoDto3.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(2, "홍길동", 1002, "profile2.jpg", "010-1111-2222");
        assertThat(applyInfoDto3.getApplicant().getRecentActivity().getGeneration()).isEqualTo(33);

        ApplyInfoDto applyInfoDto2 = applyInfoDtos.getContent().get(1);
        assertThat(applyInfoDto2)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(2, 0, "전할말입니다2",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 2, 413489000)), 1);
        assertThat(applyInfoDto2.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(3, "김철수", 1003, "profile3.jpg", "010-3333-4444");
        assertThat(applyInfoDto2.getApplicant().getRecentActivity().getGeneration()).isEqualTo(34);

    }


    @Test
    void 스터디장이_아닌_사람이_신청자_리스트_조회() {
        // given
        MeetingGetApplyListCommand queryCommand = new MeetingGetApplyListCommand(List.of(0, 1, 2), "desc");
        int page = 1;
        int take = 12;
        Integer meetingId = 1;
        Integer studyCreatorId = 1;
        Integer userId = 2;

        // when
        Page<ApplyInfoDto> applyInfoDtos = applyRepository.findApplyList(queryCommand, PageRequest.of(page - 1, take),
                meetingId, studyCreatorId, userId);

        // then
        assertThat(applyInfoDtos.getTotalElements()).isEqualTo(3);
        assertThat(applyInfoDtos.getSize()).isEqualTo(take);
        assertThat(applyInfoDtos.getNumber()).isEqualTo(page-1);
        assertThat(applyInfoDtos.getTotalPages()).isEqualTo(1);

        ApplyInfoDto applyInfoDto1 = applyInfoDtos.getContent().get(0);
        assertThat(applyInfoDto1)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(3, 0, "",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 3, 413489000)), 0);
        assertThat(applyInfoDto1.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(4, "이영지", 1004, "profile4.jpg", "010-5555-5555");
        assertThat(applyInfoDto1.getApplicant().getRecentActivity().getGeneration()).isEqualTo(32);


        ApplyInfoDto applyInfoDto2 = applyInfoDtos.getContent().get(1);
        assertThat(applyInfoDto2)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(2, 0, "",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 2, 413489000)), 1);
        assertThat(applyInfoDto2.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(3, "김철수", 1003, "profile3.jpg", "010-3333-4444");
        assertThat(applyInfoDto2.getApplicant().getRecentActivity().getGeneration()).isEqualTo(34);


        ApplyInfoDto applyInfoDto3 = applyInfoDtos.getContent().get(2);
        assertThat(applyInfoDto3)
                .extracting("applyId", "type", "content", "appliedDate", "status")
                .containsExactly(1, 0, "",
                        LocalDateTime.of(LocalDate.of(2024, 05, 19), LocalTime.of(0, 0, 0, 913489000)), 1);
        assertThat(applyInfoDto3.getApplicant())
                .extracting("id", "name", "orgId", "profileImage", "phone")
                .containsExactly(2, "홍길동", 1002, "profile2.jpg", "010-1111-2222");
        assertThat(applyInfoDto3.getApplicant().getRecentActivity().getGeneration()).isEqualTo(33);
    }



}
