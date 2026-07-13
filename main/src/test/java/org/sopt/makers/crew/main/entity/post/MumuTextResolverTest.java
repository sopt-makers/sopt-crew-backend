package org.sopt.makers.crew.main.entity.post;

import static org.instancio.Select.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MumuTextResolverTest {

	@Mock
	MumuTextRepository mumuTextRepository;

	@InjectMocks
	MumuTextResolver mumuTextResolver;

	//TODO :  case 1: localDate 적당해 db 조회가 된다면 뒷 메서드가 호출 안된다
	// TODO : db 조회가 안되는 경우
	@Test
	@DisplayName("DB 조회가 되었던 경우")
	void resolveMumuText() {

		LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(2026, 6, 25), LocalTime.of(11, 0));
		//given
		when(mumuTextRepository.findByInDateTimeText(dateTime)).thenReturn(Optional.of(testCompletelyData())) ;

		//when
		MumuText mumuText = mumuTextResolver.resolveMumuText(dateTime);

		//then
		verify(mumuTextRepository, never()).findAllByOrderByShowStartDateAsc();
	}

	@Test
	@DisplayName("DB 조회가 안 되었을 경우")
	void resolveMumuText_inDb_Null(){
		LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(2026, 7, 25), LocalTime.of(11, 0));
		//given
		when(mumuTextRepository.findByInDateTimeText(dateTime)).thenReturn(Optional.empty()) ;
		when(mumuTextRepository.findAllByOrderByShowStartDateAsc()).thenReturn(
			List.of(testSetLocalDate(LocalDate.of(2026,6,25)),
				testSetLocalDate(LocalDate.of(2026,6,26)))
		);

		//when
		MumuText mumuText = mumuTextResolver.resolveMumuText(dateTime);

		//then
		verify(mumuTextRepository).findAllByOrderByShowStartDateAsc();
		Assertions.assertThat(mumuText.getShowStartDate()).isEqualTo(LocalDate.of(2026,6,25).atStartOfDay());

	}

	@Test
	@DisplayName("날짜 차이가 텍스트 개수보다 커도 순환해서 조회한다")
	void resolveMumuText_repeatByModulo() {
		LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(2026, 7, 26), LocalTime.of(11, 0));
		MumuText firstText = testSetLocalDate(LocalDate.of(2026, 6, 25));
		MumuText secondText = testSetLocalDate(LocalDate.of(2026, 6, 26));

		//given
		when(mumuTextRepository.findByInDateTimeText(dateTime)).thenReturn(Optional.empty());
		when(mumuTextRepository.findAllByOrderByShowStartDateAsc()).thenReturn(
			List.of(firstText, secondText)
		);

		//when
		MumuText mumuText = mumuTextResolver.resolveMumuText(dateTime);

		//then
		Assertions.assertThat(mumuText).isSameAs(secondText);
	}

	private MumuText testCompletelyData(){
		return Instancio.of(MumuText.class).create();
	}


	private MumuText testSetLocalDate(LocalDate date){
		return Instancio.of(MumuText.class)
			.set(field(MumuText::getShowStartDate),  date.atStartOfDay())
			.create();
	}
}
