package ru.practicum.shareit.request;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.Map;

@Component
public class ItemRequestClient extends BaseClient {

    public ItemRequestClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> create(long userId,
                                         ItemRequestCreateDto dto) {
        return post("/requests", userId, dto);
    }

    public ResponseEntity<Object> getOwn(long userId) {
        return get("/requests", userId);
    }

    public ResponseEntity<Object> getAll(long userId,
                                         int from,
                                         int size) {
        Map<String, Object> params = Map.of(
                "from", from,
                "size", size
        );
        return get("/requests/all?from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> getById(long userId,
                                          long requestId) {
        return get("/requests/" + requestId, userId);
    }
}