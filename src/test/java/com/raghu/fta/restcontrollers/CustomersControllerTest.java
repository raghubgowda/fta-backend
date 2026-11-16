package com.raghu.fta.restcontrollers;

import com.raghu.fta.model.Customer;
import com.raghu.fta.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomersControllerTest {
    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomersController controller;

    @Test
    void getCustomersReturnsOk() {
        List<Customer> customers = Arrays.asList(new Customer(), new Customer());
        when(customerService.getCustomers()).thenReturn(customers);

        ResponseEntity<?> response = controller.getCustomers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(customers, response.getBody());
    }

    @Test
    void getCustomerByIdReturnsOk() {
        Customer customer = new Customer();
        customer.setId("c-1");
        when(customerService.getCustomerById("c-1")).thenReturn(customer);

        ResponseEntity<?> response = controller.getCustomerById("c-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(customer, response.getBody());
    }

    @Test
    void addCustomerRejectsExplicitId() {
        Customer customer = new Customer();
        customer.setId("c-1");

        ResponseEntity<?> response = controller.addCustomer(customer);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Customer id should be null", response.getBody());
        verifyNoInteractions(customerService);
    }

    @Test
    void addCustomerReturnsSuccess() {
        Customer customer = new Customer();
        when(customerService.saveCustomer(customer)).thenReturn(customer);

        ResponseEntity<?> response = controller.addCustomer(customer);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Customer added successfully", response.getBody());
    }

    @Test
    void updateCustomerRejectsMissingId() {
        Customer customer = new Customer();

        ResponseEntity<?> response = controller.updateCustomer(customer);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(customerService);
    }

    @Test
    void deleteCustomerReturnsNoContent() {
        ResponseEntity<?> response = controller.deleteCustomer("c-1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerService).delete("c-1");
    }
}
