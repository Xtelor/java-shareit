package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable @Positive Long id) {
        return userService.getUser(id);
    }

    @PostMapping
    public UserDto postUser(@Valid @RequestBody UserDto dto) {
        return userService.addUser(dto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable @Positive Long id,
                              @RequestBody UserDto dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping
    public void deleteUsers() {
        userService.deleteAllUsers();
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable @Positive Long id) {
        userService.deleteUser(id);
    }
}
