package ru.practicum.shareit.item;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.Map;

@Component
public class ItemClient extends BaseClient {

    public ItemClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> addItem(long ownerId,
                                          ItemDto dto) {
        return post("/items", ownerId, dto);
    }

    public ResponseEntity<Object> updateItem(long ownerId,
                                             long itemId,
                                             ItemDto dto) {
        return patch("/items/" + itemId, ownerId, dto);
    }

    public ResponseEntity<Object> getItemById(long userId,
                                              long itemId) {
        return get("/items/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByOwner(long ownerId,
                                                  int from,
                                                  int size) {
        Map<String, Object> params = Map.of(
                "from", from,
                "size", size
        );
        return get("/items?from={from}&size={size}", ownerId, params);
    }

    public ResponseEntity<Object> search(String text,
                                         int from,
                                         int size) {
        Map<String, Object> params = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/items/search?text={text}&from={from}&size={size}", null, params);
    }

    public ResponseEntity<Object> addComment(long userId,
                                             long itemId,
                                             Object commentDto) {
        return post("/items/" + itemId + "/comment", userId, commentDto);
    }
}