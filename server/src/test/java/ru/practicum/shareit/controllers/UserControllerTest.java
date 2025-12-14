package ru.practicum.shareit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @MockBean private UserService userService;

    // Вспомогательный метод для создания UserDto
    private UserDto createUserDto(Long id, String name, String email) {
        return UserDto.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    // Тест создания пользователя
    @Test
    void shouldCreateUser() throws Exception {
        UserDto dto = createUserDto(null, "Иван", "ivan@yandex.ru");
        UserDto saved = createUserDto(1L, "Иван", "ivan@yandex.ru");

        when(userService.addUser(any(UserDto.class))).thenReturn(saved);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.email").value("ivan@yandex.ru"));
    }

    // Тест получения всех пользователей
    @Test
    void shouldGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                createUserDto(1L, "Иван", "ivan@yandex.ru"),
                createUserDto(2L, "Пётр", "petr@yandex.ru")
        ));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // Тест получения пользователя по ID
    @Test
    void shouldGetUserById() throws Exception {
        UserDto user = createUserDto(1L, "Иван", "ivan@yandex.ru");
        when(userService.getUser(1L)).thenReturn(user);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // Тест обновления пользователя
    @Test
    void shouldUpdateUser() throws Exception {
        UserDto updated = createUserDto(1L, "Иван Иванов", "ivan.new@yandex.ru");
        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updated);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(updated))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иван Иванов"));
    }

    // Тест удаления пользователя по ID
    @Test
    void shouldDeleteUserById() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }

    // Тест удаления всех пользователей
    @Test
    void shouldDeleteAllUsers() throws Exception {
        doNothing().when(userService).deleteAllUsers();

        mvc.perform(delete("/users"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteAllUsers();
    }

    // Тест ошибки — пользователь не найден при получении
    @Test
    void shouldReturn404WhenUserNotFoundOnGet() throws Exception {
        when(userService.getUser(999L)).thenThrow(new NotFoundException("Пользователь не найден"));

        mvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    // Тест ошибки — пользователь не найден при обновлении
    @Test
    void shouldReturn404WhenUserNotFoundOnUpdate() throws Exception {
        when(userService.updateUser(eq(999L), any(UserDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        UserDto dto = createUserDto(null, "Имя", "email@yandex.ru");

        mvc.perform(patch("/users/999")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Тест ошибки валидации при создании — некорректный email
    @Test
    void shouldReturn400WhenInvalidEmailOnCreate() throws Exception {
        UserDto invalid = createUserDto(null, "Иван", "это не email");

        when(userService.addUser(any(UserDto.class)))
                .thenThrow(new ValidationException("Некорректный email"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(invalid))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Тест ошибки валидации при обновлении — некорректный email
    @Test
    void shouldReturn400WhenInvalidEmailOnUpdate() throws Exception {
        UserDto invalid = createUserDto(1L, "Иван", "неправильный email");

        when(userService.updateUser(eq(1L), any(UserDto.class)))
                .thenThrow(new ValidationException("Некорректный email"));

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(invalid))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}