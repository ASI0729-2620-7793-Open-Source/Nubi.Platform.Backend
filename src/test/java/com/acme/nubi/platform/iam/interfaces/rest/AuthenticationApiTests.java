package com.acme.nubi.platform.iam.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class AuthenticationApiTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void signsUpSignsInAndReadsTheAccount() throws Exception {
        var created = mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":" Rosa@Nubi.app ","fullName":"Rosa Díaz","password":"secreta1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("rosa@nubi.app"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
        Integer id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"rosa@nubi.app","password":"secreta1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.fullName").value("Rosa Díaz"));

        mockMvc.perform(get("/api/v1/authentication/me").header("X-Account-Id", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("rosa@nubi.app"));
    }

    @Test
    void rejectsAWrongPasswordADuplicatedEmailAndAWeakPassword() throws Exception {
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"pablo@nubi.app","fullName":"Pablo","password":"secreta1"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"pablo@nubi.app","password":"otra-clave"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("iam.credentials.invalid"));

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"PABLO@nubi.app","fullName":"Otro","password":"secreta1"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("iam.email.duplicated"));

        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo@nubi.app","fullName":"Nuevo","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("iam.password.weak"));
    }
}
