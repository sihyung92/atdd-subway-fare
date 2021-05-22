package wooteco.subway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import wooteco.subway.auth.application.AuthService;
import wooteco.subway.member.application.MemberService;
import wooteco.subway.member.domain.LoginMember;
import wooteco.subway.member.dto.MemberRequest;
import wooteco.subway.member.dto.MemberResponse;
import wooteco.subway.member.ui.MemberController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MemberService memberService;
    @MockBean
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유저 생성 - 성공")
    public void createMember() throws Exception {
        //given
        String email = "email@email.com";
        int age = 20;
        MemberRequest memberRequest = new MemberRequest(email, "1234", age);
        MemberResponse memberResponse = new MemberResponse(1L, email, age);

        given(memberService.createMember(any(MemberRequest.class)))
                .willReturn(memberResponse);

        mockMvc.perform(
                post("/members")
                        .content(objectMapper.writeValueAsString(memberRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andDo(print())
                .andDo(document("member-create"));
    }

    @Test
    @DisplayName("현재 유저 조회 - 성공")
    void updateMember() throws Exception {
        //given
        String token = "이것은토큰입니다";

        long id = 1L;
        String email = "test@email.com";
        int age = 20;

        given(authService.findMemberByToken(token))
                .willReturn(new LoginMember(id, email, age));

        given(memberService.findMember(any(LoginMember.class)))
                .willReturn(new MemberResponse(id, email, age));

        mockMvc.perform(
                get("/members/me")
                        .header("Authorization", "Bearer " + token)
        )
                .andDo(print())
                .andDo(document("members-findme"));
    }

    @Test
    @DisplayName("현재 유저 삭제 - 성공")
    void deleteMember() throws Exception {
        //given
        String token = "이것은토큰입니다";

        long id = 1L;
        String email = "test@email.com";
        int age = 20;

        given(authService.findMemberByToken(token))
                .willReturn(new LoginMember(id, email, age));

        mockMvc.perform(
                delete("/members/me")
                        .header("Authorization", "Bearer " + token)
        )
                .andExpect(status().isNoContent())
                .andDo(print())
                .andDo(document("members-deleteme"));
    }

    @Test
    @DisplayName("현재 유저 수정 - 성공")
    public void updateMe() throws Exception{
        String token = "이것은토큰입니다";
        final long id = 1L;
        final String email = "test@email.com";
        final int newAge = 29;

        given(authService.findMemberByToken(token))
                .willReturn(new LoginMember(id, email, newAge));

        mockMvc.perform(put("/members/me")
                .header("Authorization", "Bearer "+token)
                .content(objectMapper.writeValueAsString(new MemberRequest(email, "1234", newAge)))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andDo(document("members-updateme"));
    }
}