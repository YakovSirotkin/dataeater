package ru.telamon.dataeater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiverService {

    private MessageDao messageDao;

    @Autowired
    public ReceiverService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    @PostMapping("message/{deviceId}/{deviceTime}")
    public ResponseEntity acceptMessage(@PathVariable("deviceId") String deviceId,
                              @PathVariable("deviceTime") long deviceTime,
                              @RequestBody String message) {
        messageDao.addMessage(deviceId, deviceTime, message);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}