package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private UserClient userClient;

    // Успешное создание пользователя
    @Test
    void shouldCreateUser_whenValidData() throws Exception {
        UserDto dto = UserDto.builder()
                .name("Вася")
                .email("vasya@example.com")
                .build();

        when(userClient.create(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().body("User created"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Ошибка: name пустой при создании
    @Test
    void shouldReturn400_whenNameIsBlankOnCreate() throws Exception {
        UserDto dto = UserDto.builder()
                .name("   ")
                .email("vasya@example.com")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Ошибка: email пустой при создании
    @Test
    void shouldReturn400_whenEmailIsBlankOnCreate() throws Exception {
        UserDto dto = UserDto.builder()
                .name("Вася")
                .email("")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Ошибка: email без @ при создании
    @Test
    void shouldReturn400_whenEmailInvalidOnCreate() throws Exception {
        UserDto dto = UserDto.builder()
                .name("Вася")
                .email("vasyaexample.com")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Успешное обновление пользователя (только name)
    @Test
    void shouldUpdateUser_whenOnlyNameProvided() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("Новое имя")
                .build();

        when(userClient.update(eq(1L), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().body("Updated"));

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Успешное обновление только email
    @Test
    void shouldUpdateUser_whenOnlyEmailProvided() throws Exception {
        UserDto updateDto = UserDto.builder()
                .email("new@example.com")
                .build();

        when(userClient.update(eq(1L), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok().body("Updated"));

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Обновление с невалидным email
    @Test
    void shouldReturn400_whenEmailInvalidOnUpdate() throws Exception {
        UserDto updateDto = UserDto.builder()
                .email("неправильный_емейл")
                .build();

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Получение всех пользователей
    @Test
    void shouldGetAllUsers() throws Exception {
        when(userClient.getAll())
                .thenReturn(ResponseEntity.ok().body(List.of("User1", "User2")));

        mvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    // Получение пользователя по ID
    @Test
    void shouldGetUserById() throws Exception {
        when(userClient.getById(eq(1L)))
                .thenReturn(ResponseEntity.ok().body("User details"));

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    // Удаление пользователя
    @Test
    void shouldDeleteUser() throws Exception {
        when(userClient.delete(eq(1L)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}