package com.raghu.fta.services;

import com.raghu.fta.dao.CustomersRepo;
import com.raghu.fta.exceptions.EntityNotFoundException;
import com.raghu.fta.exceptions.ServiceException;
import com.raghu.fta.model.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomersRepo customersRepo;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void getCustomersReturnsRepositoryList() {
        List<Customer> customers = Collections.singletonList(new Customer());
        when(customersRepo.findAll()).thenReturn(customers);

        assertSame(customers, customerService.getCustomers());
    }

    @Test
    void getCustomersWrapsRepositoryFailure() {
        when(customersRepo.findAll()).thenThrow(new RuntimeException("db error"));

        assertThrows(ServiceException.class, customerService::getCustomers);
    }

    @Test
    void getCustomerByIdReturnsCustomerWhenFound() {
        Customer customer = new Customer();
        when(customersRepo.findById("c-1")).thenReturn(Optional.of(customer));

        assertSame(customer, customerService.getCustomerById("c-1"));
    }

    @Test
    void getCustomerByIdThrowsWhenMissing() {
        when(customersRepo.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> customerService.getCustomerById("missing"));
    }

    @Test
    void updateCustomerChecksExistenceAndSaves() {
        Customer customer = new Customer();
        customer.setId("c-1");
        when(customersRepo.findById("c-1")).thenReturn(Optional.of(customer));
        when(customersRepo.save(customer)).thenReturn(customer);

        assertSame(customer, customerService.updateCustomer(customer));
    }

    @Test
    void saveCustomerReturnsSavedCustomer() {
        Customer customer = new Customer();
        when(customersRepo.save(customer)).thenReturn(customer);

        assertSame(customer, customerService.saveCustomer(customer));
    }

    @Test
    void deleteDelegatesToRepository() {
        customerService.delete("c-1");

        verify(customersRepo).deleteById("c-1");
    }
}
