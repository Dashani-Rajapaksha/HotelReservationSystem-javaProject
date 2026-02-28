package com.hotel.service;
import com.hotel.model.BookingResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationServiceTest {
    
    //TEST 01 - INVALID NIC
    @Test
    public void testBookRoom_InvalidNIC() {

        ReservationService service = new ReservationService();

        BookingResult result = service.bookRoom(
                "John",
                "Colombo",
                "0712345678",
                "1234",                 //<<<<<----------invalid NIC
                1,
                "2025-12-01",
                "2025-12-05"
        );

        assertFalse(result.isSuccess());
    }
    
    //TEST 02 - INAVALID CONTACT
    @Test
    public void testBookRoom_InvalidContact() {

        ReservationService service = new ReservationService();

        BookingResult result = service.bookRoom(
                "John",
                "Colombo",
                "ABC123",              //<<<<<----------invalid contact
                "200012345678",
                1,
                "2025-12-01",
                "2025-12-05"
        );

        assertFalse(result.isSuccess());
    }
    
    //TEST 03 - CHECKOUT DATE BEFORE CHECKIN
    @Test
    public void testBookRoom_InvalidDates() {

        ReservationService service = new ReservationService();

        BookingResult result = service.bookRoom(
                "John",
                "Colombo",
                "0712345678",
                "200012345678",
                1,
                "2025-12-05",
                "2025-12-01"     //<<<<<----------invalid
        );

        assertFalse(result.isSuccess());
    }
    
    //TEST 04 - AUTOMATE CHECKOUT
    @Test
    public void testCheckout_InvalidReservation() {

        ReservationService service = new ReservationService();

        boolean result = service.checkout(5536); //<<<<<----------non-existing

        assertFalse(result);
    }
    
    //TEST 05 - AUTOMATE BILL JSON
    @Test
    public void testGenerateBillJson_InvalidReservation() {

        ReservationService service = new ReservationService();

        String json = service.generateBillJson(5000);

        assertTrue(json.contains("\"success\": false"));
    }
}