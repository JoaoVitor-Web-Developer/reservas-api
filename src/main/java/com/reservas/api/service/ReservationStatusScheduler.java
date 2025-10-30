package com.reservas.api.service;

import com.reservas.api.entities.enums.ReservationStatus;
import com.reservas.api.entities.model.Reservations;
import com.reservas.api.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationStatusScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void updateExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        
        List<Reservations> pendingExpired = reservationRepository
            .findByStatusAndEndDateBefore(ReservationStatus.PENDING, now);
            
        if (!pendingExpired.isEmpty()) {
            pendingExpired.forEach(reservation -> {
                reservation.setStatus(ReservationStatus.CANCELED);
            });
            reservationRepository.saveAll(pendingExpired);
        }
        
        List<Reservations> confirmedExpired = reservationRepository
            .findByStatusAndEndDateBefore(ReservationStatus.CONFIRMED, now);
            
        if (!confirmedExpired.isEmpty()) {
            confirmedExpired.forEach(reservation -> {
                reservation.setStatus(ReservationStatus.COMPLETED);
            });
            reservationRepository.saveAll(confirmedExpired);
        }
    }
}
