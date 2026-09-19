package com.chari.chariapp.appointment.infrastructure;

import com.chari.chariapp.appointment.application.*;
import com.chari.chariapp.appointment.application.port.out.*;
import com.chari.chariapp.request.application.PassportRequestActorAccess; import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import java.time.Clock;
import org.springframework.context.annotation.*;

@Configuration
public class AppointmentConfiguration {
    @Bean AppointmentSlotService appointmentSlotService(PassportRequestActorAccess a, AppointmentSlotStore s, OperationalAuditStore audit, Clock c) { return new AppointmentSlotService(a,s,audit,c); }
    @Bean AppointmentService appointmentService(PassportRequestActorAccess a, AppointmentSlotStore s, AppointmentStore p, CitizenStore citizens, PersonalDataProtector protector, OperationalAuditStore audit, NotificationService n, Clock c) { return new AppointmentService(a,s,p,citizens,protector,audit,n,c); }
}
