package com.chari.chariapp.notification.application.port.out;

import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.notification.domain.UserNotification;
import java.util.*;
public interface NotificationStore { UserNotification save(UserNotification notification); Optional<UserNotification> findById(UUID id); List<UserNotification> findAll(); List<UserNotification> findByCitizenId(CitizenId citizenId); List<UserNotification> findUnreadByCitizenId(CitizenId citizenId); long countUnreadByCitizenId(CitizenId citizenId); }
