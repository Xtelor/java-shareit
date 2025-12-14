package ru.practicum.shareit.user;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserClient extends BaseClient {

    public UserClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> create(UserDto dto) {
        return post("/users", dto);
    }

    public ResponseEntity<Object> update(Long userId,
                                         UserDto dto) {
        return patch("/users/" + userId, dto);
    }

    public ResponseEntity<Object> getAll() {
        return get("/users");
    }

    public ResponseEntity<Object> getById(Long userId) {
        return get("/users/" + userId);
    }

    public ResponseEntity<Object> delete(Long userId) {
        return delete("/users/" + userId);
    }
}